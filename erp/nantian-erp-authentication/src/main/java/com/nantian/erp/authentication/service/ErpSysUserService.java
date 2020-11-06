package com.nantian.erp.authentication.service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nantian.erp.authentication.constants.DicConstants;
import com.nantian.erp.authentication.data.dao.ErpRoleMapper;
import com.nantian.erp.authentication.data.dao.ErpSysRecordMapper;
import com.nantian.erp.authentication.data.dao.ErpSysUserMapper;
import com.nantian.erp.authentication.data.dao.ErpUserRoleMapper;
import com.nantian.erp.authentication.data.model.ErpRole;
import com.nantian.erp.authentication.data.model.ErpSysRecord;
import com.nantian.erp.authentication.data.model.ErpUserRole;
import com.nantian.erp.authentication.data.vo.ErpSysUserVo;
import com.nantian.erp.authentication.data.vo.ErpUserRoleVo;
import com.nantian.erp.authentication.util.RestTemplateUtils;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.HttpClientUtil;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

/** 
 * Description: 用户信息管理service接口
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   caoxiubin         1.0        
 * </pre>
 */
@Service
@PropertySource(value= {"classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
@Transactional(readOnly=true)
public class ErpSysUserService {
	/*
	 * 从配置文件中获取主机相关属性
	 */
	@Value("${protocol.type}")
    private String protocolType;//http或https
	@Value("${email.service.host}")
	private  String emailServiceHost;//邮件服务的IP地址和端口号
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	RestTemplate restTemplate;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	@Autowired
	private ErpSysUserMapper erpSysUserMapper;
	
	@Autowired
	private ErpUserRoleMapper erpUserRoleMapper;
	
	@Autowired
	private ErpRoleMapper erpRoleMapper;
	
	@Autowired
	private ErpSysRecordMapper erpSysRecordMapper;

	@Autowired
	private ErpSysUserService erpSysUserService;
	@Autowired
	private RestTemplateUtils restTemplateUtils;
	
	/**
	 * Description: 用户登录
	 * 
	 * @param userInput 用户输入的用户名、密码
	 * @return
	 * @Author ZhangYuWei
	 * @Update Date: 2018年12月18日 上午10:30:46
	 */
	@SuppressWarnings({ "unchecked" })
	public RestResponse login(ErpUser userInput) {
		logger.info("进入login方法");
		try {
			String username = userInput.getUsername();//用户输入的用户名
			//判断是邮箱还是手机号的正则表达式
			String em = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";//java用正则表达式验证邮箱
			String ph = "^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$"; //java用验证手机号
			//String ph = "^[1][34578]\\d{9}$";
			ErpUser userLogin = new ErpUser();
			if (username.matches(ph)) {//手机号登录 			
				userLogin.setUserPhone(username);
			} else if (username.matches(em)) {//邮箱登录
				userLogin.setUsername(username);
			} else {//就是用户名登录
				username = username + "@nantian.com.cn";
				userLogin.setUsername(username);
			}
			
			//从Redis中获取用户输错密码的次数，错误次数超过5次，锁定账号，不让登录
			Integer errorCount = (Integer) redisTemplate.opsForValue().get(DicConstants.REDIS_PREFIX_ERRORCOUNT+username);
			if(errorCount!=null && errorCount==5) {
				return RestUtils.returnFailure("由于您连续输错了五次密码，账号已被锁定！请于12小时后重新尝试！");
			}
			
			ErpUser userLoginResult = this.erpSysUserMapper.login(userLogin);
			if (userLoginResult == null) {
				return RestUtils.returnFailure("用户名错误！请重新输入！");
			}
			if (userInput.getPassword() != null) {
				Md5Hash md5 = new Md5Hash(userInput.getPassword(), "nantian-erp");
				if (!md5.toString().equals(userLoginResult.getPassword())) {//密码错误
					if(errorCount==null) {
						errorCount = 1;//初始化错误次数为1
					}else {
						errorCount += 1;//错误次数+1
					}
					redisTemplate.opsForValue().set(DicConstants.REDIS_PREFIX_ERRORCOUNT+username, errorCount, 12, TimeUnit.HOURS);
					return RestUtils.returnFailure("密码错误！请重新输入！连续输错五次密码后账号将被锁定！剩余次数："+(5-errorCount));
				}else {//密码正确
					redisTemplate.delete(DicConstants.REDIS_PREFIX_ERRORCOUNT+username);
				}
			}
			String uuid = UUID.randomUUID().toString();
			//根据用户ID获取角色信息
			List<Integer> roles = this.erpSysUserMapper.findRoles(userLoginResult.getId());
			userLoginResult.setPassword(null);
			userLoginResult.setRoles(roles);
			
			//给前端返回的用户信息列表
			HashMap<String, Object> hashMap = new HashMap<String, Object>();
			hashMap.put("token", uuid);
			hashMap.put("username", userLoginResult.getUsername());//用户名
			hashMap.put("userPhone", userLoginResult.getUserPhone());//用户手机号码
			hashMap.put("id", userLoginResult.getId());//用户编号
			hashMap.put("employeeId", userLoginResult.getUserId());//员工id
			hashMap.put("roles", userLoginResult.getRoles());//角色
			String employeeName = "";//员工姓名（初始化为空字符串）
			
			//跨工程查询员工姓名
			RestResponse response = this.findEmployeeNameByHrProject(userLoginResult.getUserId());
			if("200".equals(response.getStatus())) {
				employeeName = response.getData().toString();
			}
			
			//将用户信息加缓存中，然后返回给前端
			userLoginResult.setEmployeeName(employeeName);
			logger.info("redis缓存中的数据userLoginResult="+userLoginResult);
			redisTemplate.opsForValue().set(uuid, userLoginResult, 2, TimeUnit.HOURS);
			hashMap.put("employeeName", employeeName);//员工姓名
			logger.info("给前端返回的数据hashMap="+hashMap);
			return RestUtils.returnSuccess(hashMap, "登录成功后，返回的用户信息");
		} catch (Exception e) {
			logger.error("login方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致登录失败！");
		}
	}
	
	/**
	 * Description: 调用人力资源工程查询用户的员工姓名
	 * 
	 * @param token
	 * @param employeeId
	 * @return
	 * @Author ZhangYuWei
	 * @Update Date: 2018年12月18日 上午11:19:21
	 */
	@SuppressWarnings("unchecked")
	public RestResponse findEmployeeNameByHrProject(Integer employeeId) {
		logger.info("进入findEmployeeNameByHrProject方法，参数是：employeeId="+employeeId);
		try {
			/*
			 * 调用人力资源工程查询用户的员工姓名
			 */
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeDetailForLogin?employeeId="+employeeId;
//			HttpHeaders requestHeaders = new HttpHeaders();
//			requestHeaders.add("token", null);
//		    HttpEntity<String> request = new HttpEntity<String>(null,requestHeaders);
//		    ResponseEntity<RestResponse> response = restTemplate.exchange(url,HttpMethod.GET,request,RestResponse.class);
			ResponseEntity<RestResponse> response = restTemplate.getForEntity(url, RestResponse.class);
		    //跨工程调用响应失败
		    if(200 != response.getStatusCodeValue() || !"200".equals(response.getBody().getStatus())) {
		    	logger.error("调用人力资源工程发生异常，响应失败！"+response);
				return RestUtils.returnFailure("调用人力资源工程发生异常，响应失败！");
			}
			//解析请求的结果
			Map<String,Object> employeeMap = (Map<String, Object>) response.getBody().getData();
			logger.info(employeeMap.toString());
			String employeeName = null;//员工姓名
			if(employeeMap!=null && employeeMap.get("name")!=null && !"".equals(employeeMap.get("name").toString())) {
				employeeName = String.valueOf(employeeMap.get("name"));
			}
			return RestUtils.returnSuccessWithString(employeeName);
		} catch (Exception e) {
			logger.error("findEmployeeNameByHrProject方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致查询用户的员工姓名失败！");
		}
	}
	
	/**
	 * Description: 修改用户密码（修改之前，需要校验旧密码是否正确）
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月28日 下午14:20:02
	 */
	@Transactional
	public RestResponse updateUserPassword(ErpUser erpUser){
		logger.info("进入updateUserPassword方法，参数是："+erpUser);
		try {
			/*
			 * 接收前端传递过来的参数
			 */
			Integer id = erpUser.getId();//用户编号
			String oldPassword = erpUser.getOldPassword();//旧密码
			String newPassword = erpUser.getPassword();//新密码
			
			ErpUser erpUserRes = erpSysUserMapper.login(erpUser);
			
			//校验当前密码是否正确
			Md5Hash md5 = new Md5Hash(oldPassword, "nantian-erp");
			if (!md5.toString().equals(erpUserRes.getPassword())) {
				return RestUtils.returnSuccessWithString("当前密码不正确！修改密码失败！");
			}
			
			//更新密码
			Md5Hash md5New = new Md5Hash(newPassword, "nantian-erp");
			ErpSysUserVo erpSysUserVo = new ErpSysUserVo();
			erpSysUserVo.setId(id);
			erpSysUserVo.setPassword(md5New.toString());
			erpSysUserMapper.updateUser(erpSysUserVo);
			
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("updateUserPassword方法出现异常："+e.getMessage(),e);
			return RestUtils.returnSuccessWithString("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 检查用户是否是第一次登录薪酬模块
	 * 决定用户是登录？还是注册？Y表示首次登录，需要注册；N表示可以登录
	 * 
	 * @param userInput 用户输入的用户名、密码
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月30日 下午16:17:06
	 */
	public RestResponse checkFirstLogin(Integer id) {
		logger.info("进入checkFirstLogin方法，参数是：id="+id);
		try {
			ErpUser erpUser = new ErpUser();
			erpUser.setId(id);
			ErpUser userLoginResult = erpSysUserMapper.loginSecondaryPassword(erpUser);
			if(userLoginResult.getSecondaryPassword()==null) {
				return RestUtils.returnSuccessWithString("Y");
			}else {
				return RestUtils.returnSuccessWithString("N");
			}
		} catch (Exception e) {
			logger.error("checkFirstLogin方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致校验失败！");
		}
	}
	
	/**
	 * Description: 检查用户是否是第一次登录薪酬模块
	 * 决定用户是登录？还是注册？Y表示首次登录，需要注册；N表示可以登录
	 * 
	 * @param userInput 用户输入的用户名、密码
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月30日 下午16:49:23
	 */
	@Transactional
	public RestResponse registerSecondaryPassword(ErpUser user) {
		logger.info("进入registerSecondaryPassword方法，参数是："+user);
		try {
			//给用户新增二级密码
			Md5Hash md5 = new Md5Hash(user.getSecondaryPassword(), "nantian-erp");
			ErpSysUserVo erpSysUserVo = new ErpSysUserVo();
			erpSysUserVo.setId(user.getId());
			erpSysUserVo.setSecondaryPassword(md5.toString());
			erpSysUserMapper.updateUser(erpSysUserVo);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("registerSecondaryPassword方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致校验失败！");
		}
	}
	
	/**
	 * Description: 用户登录二级密码
	 * 
	 * @param userInput 用户输入的用户名、密码
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月29日 下午17:09:05
	 */
	public RestResponse loginSecondaryPassword(ErpUser userInput) {
		logger.info("进入loginSecondaryPassword方法，参数是：userInput="+userInput);
		try {
			//通过用户Id、二级密码查询数据库中有没有一条记录
			Md5Hash md5 = new Md5Hash(userInput.getSecondaryPassword(), "nantian-erp");
			userInput.setSecondaryPassword(md5.toString());
			ErpUser userLoginResult = erpSysUserMapper.loginSecondaryPassword(userInput);
			
			if(userLoginResult==null) {
				return RestUtils.returnSuccessWithString("二级密码错误！请重新输入！");
			}else {
				return RestUtils.returnSuccessWithString("OK");
			}
		} catch (Exception e) {
			logger.error("loginSecondaryPassword方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致登录失败！");
		}
	}
	
	/**
	 * Description: 修改用户二级密码（修改之前，需要校验旧密码是否正确）
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月29日 下午13:39:08
	 */
	@Transactional
	public RestResponse updateUserSecondaryPassword(ErpUser userInput){
		logger.info("进入updateUserSecondaryPassword方法，参数是："+userInput);
		try {
			//通过用户Id、二级密码查询数据库中有没有一条记录
			Md5Hash md5 = new Md5Hash(userInput.getOldPassword(), "nantian-erp");
			userInput.setPassword(md5.toString());
			ErpUser userLoginResult = erpSysUserMapper.loginSecondaryPassword(userInput);
			
			if(userLoginResult==null) {
				return RestUtils.returnSuccessWithString("当前二级密码不正确！修改密码失败！");
			}
			
			//更新密码
			Md5Hash md5New = new Md5Hash(userInput.getSecondaryPassword(), "nantian-erp");
			ErpSysUserVo erpSysUserVo = new ErpSysUserVo();
			erpSysUserVo.setId(userInput.getId());
			erpSysUserVo.setSecondaryPassword(md5New.toString());
			erpSysUserMapper.updateUser(erpSysUserVo);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("updateUserSecondaryPassword方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}
	
	/**
	 * Description: 用户信息-查询用户列表信息
	 * @param isDimissionPage 是否为离职页面（为true不查询“离职中”与“已离职”的数据）
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月14日 下午18:32:53
	 */
	@SuppressWarnings("unchecked")
	public RestResponse findAllUser(String token, String getAllFlag, Boolean isDimissionPage){
		logger.info("进入findAllUser方法");
		List<ErpSysUserVo> returnList = new ArrayList<ErpSysUserVo>();
		try {			
			ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			
			if (user.getRoles().contains(4)){
				//管理员查询所有用户
				getAllFlag = "true";
			}
			List<Object> result = this.findAllEmpFromHr(token, getAllFlag, isDimissionPage);
			logger.info("返回结果的条数："+result.size());
			for (Object object : result) {
				Map<String, Object> resultMap = (Map<String, Object>) object;
				Integer userId = Integer.valueOf(String.valueOf(resultMap.get("employeeId")));
				ErpSysUserVo erpSysUserVo = this.erpSysUserMapper.findUserByEmpId(userId);
				if(null != erpSysUserVo) {
					//该用户所有角色信息
					List<ErpRole> roleList = this.erpRoleMapper.findRoleByUserId(erpSysUserVo.getId());					
					
					for (ErpRole erpRole : roleList){						
						if (erpRole.getRoleType().equals(2)){
							String employeeName = "";//员工姓名（初始化为空字符串）
							
							//跨工程查询员工姓名
							RestResponse response = this.erpSysUserService.findEmployeeNameByHrProject(erpRole.getChildRoleOwner());
							if("200".equals(response.getStatus())) {
								employeeName = response.getData().toString();
								erpRole.setChlidRoleOwnerName(employeeName);
							}
							
							ErpRole fatherRole = this.erpRoleMapper.findByRoleId(erpRole.getFatherRoleId());
							if (fatherRole != null){
								erpRole.setFatherRoleName(fatherRole.getName());
							}				
						}
					}
					
					erpSysUserVo.setRoleList(roleList);
					erpSysUserVo.setEmpInfo(resultMap);
					returnList.add(erpSysUserVo);
				}
			}
		} catch (Exception e) {
			logger.error("用户信息-查询用户列表信息出现错误 方法 findAllUser ："+ e.getMessage(),e);
		}
		logger.info("返回的用户列表条数：" + returnList.size());
		return RestUtils.returnSuccess(returnList);
	}
	/**
	 * Description: 用户信息-查询临时用户列表信息
	 * @param null     
	 * @return RestResponse             
	 * @Author 张倩
	 * @Create Date: 2019年03月05日 下午18:32:53
	 */
	@SuppressWarnings({ "unchecked", "null" })
	public RestResponse findAllTempUser(String token){
		logger.info("进入findAllTempUser方法");
		List<Map<String,Object>> returnList = null;
		try {			
			List<Map<String,Object>> tempuserList = this.erpSysUserMapper.findAllTempUser();
			
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoById";
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token",token);//将token放到请求头中
			HttpEntity<List<Map<String,Object>>> request = new HttpEntity<>(tempuserList, requestHeaders);
			
			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
			if(response.getStatusCodeValue() != 200){
				logger.info("hr工程返回错误，查询临时用户信息失败");
				return RestUtils.returnFailure("hr工程返回错误，查询临时用户信息失败");
			}
			
			returnList = (List<Map<String, Object>>) response.getBody().get("data");
			
		} catch (Exception e) {
			logger.error("查询临时用户列表信息出现错误 方法 findAllUser ："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(returnList);
	}
	
	/**
	 * Description: 修改用户名
	 * @param null     
	 * @return RestResponse             
	 * @Author 张倩
	 * @Create Date: 2019年03月05日 下午18:32:53
	 */
	@SuppressWarnings({ "unchecked", "null" })
	@Transactional(readOnly=false)
	public RestResponse changeUserName(String token, Map<String, Object> map) {
		logger.info("changeUserName方法开始执行，参数:"+map);
		ErpSysUserVo erpSysUserVo=new ErpSysUserVo();
		erpSysUserVo.setUsername(String.valueOf(map.get("username")));
		erpSysUserVo.setId(Integer.valueOf(String.valueOf(map.get("id"))));
		try{
			this.erpSysUserMapper.updateUser(erpSysUserVo);
		}catch(Exception e){
			logger.error("changeUserName方法报错："+e.getMessage(),e);
			return RestUtils.returnFailure("changeUserName方法报错："+e.getMessage());
		}
		return RestUtils.returnSuccess("修改成功");
	}
	
	/**
	 * Description: 用户信息-新增用户信息
	 * @param  erpRole  用户对象 带有用户信息的用户    
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月14日 上午11:32:53
	 */
	@Transactional(rollbackFor=Exception.class)
	public RestResponse insertUser (String token, ErpSysUserVo erpSysUserVo){
		
		String str = null;
		
		try {
			/*
			 * 新增用户表本身信息
			 */	
			Md5Hash md5Hash = new Md5Hash("000000", "nantian-erp");
			erpSysUserVo.setPassword(md5Hash.toString());//新建用户时  给定一个初始密码-000000
			this.erpSysUserMapper.insertUser(erpSysUserVo);
			/*
			 * 新增特权信息
			 */
			Integer[] roles = erpSysUserVo.getRoles();
			ErpUserRoleVo erpUserRoleVo = new ErpUserRoleVo();
			for (int i = 0; i < roles.length; i++) {
				erpUserRoleVo.setUserId(erpSysUserVo.getId());
				erpUserRoleVo.setRoleId(roles[i]);
				this.erpUserRoleMapper.insert(erpUserRoleVo);
			}
			
			//插入权限修改记录
			ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			
			ErpSysRecord record = new ErpSysRecord();
			record.setOpType(2);//用户修改
			record.setOpId(erpSysUserVo.getId());//用户id
			record.setProcessor(user.getUsername());
			record.setTime(ExDateUtils.getCurrentStringDateTime());				
			record.setOpRecord("新建用户");

			this.erpSysRecordMapper.insertRecord(record);
			
			str = "新增成功 ！";
		} catch (Exception e) {
			str = "新增失败 ！";
			logger.error("用户信息-新增用户信息出现错误  方法 insertUser："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(str);
	}
	
	/**
	 * Description: 用户信息-修改用户信息
	 * @param  erpRole    用户对象 带有用户信息的用户   
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月14日 下午18:32:53
	 */
	@Transactional(rollbackFor=Exception.class)
	public RestResponse updateUser (String token, ErpSysUserVo erpSysUserVo){
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
		String str = null;
		try {
			/*
			 * 修改已有用户信息
			 */
			this.erpSysUserMapper.updateUser(erpSysUserVo);
			/*
			 * 修改用户下对应的角色信息
			 */			
			Integer[] roles = erpSysUserVo.getRoles();
			List<Integer> addList = new ArrayList<>();
			addList.addAll(Arrays.asList(roles));
			
			//查询角色的当前角色
			List<ErpUserRole> roleList = this.erpUserRoleMapper.findUserRoleByUserId(erpSysUserVo.getId());
			List<ErpUserRole> deleteList = new ArrayList<>();
			deleteList.addAll(roleList);
			
			for (int i=0; i<roles.length; i++){
				for (ErpUserRole role : roleList){
					if (roles[i].equals(role.getrId())){
						// 移除相同的剩下要新增的
						addList.remove(roles[i]);

						// 移除相同的剩下要删除的
						deleteList.remove(role);
					}
				}
			}

			//新增部分
			if (!addList.isEmpty()) {
				String addRecord = "";
				ErpUserRoleVo erpUserRoleVo = new ErpUserRoleVo();
				for (Integer addRole : addList) {
					erpUserRoleVo.setUserId(erpSysUserVo.getId());
					erpUserRoleVo.setRoleId(addRole);
					this.erpUserRoleMapper.insert(erpUserRoleVo);
					
					ErpRole roleInfo = this.erpRoleMapper.findByRoleId(addRole);
					addRecord=addRecord+roleInfo.getName()+" ";
				}
				
				//插入权限修改记录
				ErpSysRecord record = new ErpSysRecord();
				record.setOpType(2);//用户修改
				record.setOpId(erpSysUserVo.getId());//用户id
				record.setProcessor(erpUser.getUsername());
				record.setTime(ExDateUtils.getCurrentStringDateTime());				
				record.setOpRecord("用户："+erpSysUserVo.getUsername()+"增加角色："+addRecord);
				
				this.erpSysRecordMapper.insertRecord(record);
			}
			
			//删除部分
			if(!deleteList.isEmpty()) {
				String delRecord = "";
				for (ErpUserRole userRole : deleteList) {
					this.erpUserRoleMapper.deleteUserRoleByUserRole(erpSysUserVo.getId(), userRole.getrId());
					
					ErpRole roleInfo = this.erpRoleMapper.findByRoleId(userRole.getrId());
					delRecord=delRecord+roleInfo.getName()+" ";
				}

				//插入权限修改记录
				ErpSysRecord record = new ErpSysRecord();
				record.setOpType(2);//用户修改
				record.setOpId(erpSysUserVo.getId());//用户id
				record.setProcessor(erpUser.getUsername());
				record.setTime(ExDateUtils.getCurrentStringDateTime());				
				record.setOpRecord("用户："+erpSysUserVo.getUsername()+"删除角色："+delRecord);
				
				this.erpSysRecordMapper.insertRecord(record);
			}			
			
			str = "修改成功 ！";
		} catch (Exception e) {
			str = "修改失败 ！";
			logger.error("用户信息-修改用户信息出现错误  方法 updateUser："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(str);
	}
	
	/**
	 * Description: 用户信息-删除用户信息
	 * @param  userId      
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月14日 上午11:32:53
	 */
	@Transactional(rollbackFor=Exception.class)
	public RestResponse deleteUser (Integer userId){
		
		String str = null;
		try {
			/*
			 * 删除用户表本身信息
			 */
			this.erpSysUserMapper.deleteUser(userId);
			/*
			 * 删除特权信息
			 */
			this.erpUserRoleMapper.deleteUserRoleByUserId(userId);
			
			str = "删除成功 ！";
		} catch (Exception e) {
			str = "删除失败 ！";
			logger.error("用户信息-删除用户信息出现错误  方法 deleteUser："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(str);
	}
	
	/**
	 * Description: 用户信息-用户名去重
	 * @param  username      
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月15日 上午11:32:53
	 */
	public RestResponse checkUsername (String username){
		
		boolean flag = false;
		try {
			ErpSysUserVo erpSysUserVo = this.erpSysUserMapper.findUserByUsername(username);
			if(null == erpSysUserVo) {
				flag = true;
			}
		} catch (Exception e) {
			logger.error("用户信息-用户名去重出现错误  方法 checkUsername："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(flag);
	}
	
	/**
	 * Description: 用户信息-查询所有员工信息
	 * @param  username      
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月15日 上午11:32:53
	 */
	@SuppressWarnings("unchecked")
	public RestResponse findAllEmp (HttpServletRequest request){
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			String token = request.getHeader("token");
			List<Object> resultList = this.findAllEmpFromHr(token, "true", null);
			for (Object object : resultList) {
				Map<String, Object> resultMap = (Map<String, Object>) object;
				/*
				 * 判断该员工是否已创建了用户
				 */
				Integer userId = Integer.valueOf(String.valueOf(resultMap.get("employeeId")));
				ErpSysUserVo erpSysUserVo = this.erpSysUserMapper.findUserByEmpId(userId);
				if(null == erpSysUserVo) {
					list.add(resultMap);
				}
			}
		} catch (Exception e) {
			logger.error("用户信息-查询所有员工信息出现错误  方法 findAllEmp："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(list);
	}
	
	/**
	 * Description: 用户信息-查询所有未创建用户内部员工信息
	 * @param  token      
	 * @return RestResponse             
	 * @Author caoxiubin
	 * @Create Date: 2018年10月15日 上午11:32:53
	 */
	private List<Object> findAllEmpFromHr (String token, String getAllFlag, Boolean isDimissionPage){
		List<Object> returnList = new ArrayList<>();
		try {
			//调用ERP-人力资源 工程 的操作层服务接口-获取所有一级部门
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findAllEmployee";
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);
			requestHeaders.add("allFlag", getAllFlag);
			requestHeaders.add("isDimissionPage", (isDimissionPage == null || !isDimissionPage)? "0": "1");
			HttpEntity<String> requestEntity=new HttpEntity<String>(null,requestHeaders);
			ResponseEntity<String> response= restTemplate.exchange(url,HttpMethod.GET,requestEntity,String.class);
			String resultStr=response.getBody();
			JSONObject jsStr=JSON.parseObject(resultStr);
			returnList=jsStr.getJSONArray("data");
			/*JSONObject js = this.restTemplate.getForObject(url, JSONObject.class);
			if(null == js.get("data") || "".equals(String.valueOf(js.get("data")))) {
				return null;
			}
			returnList = js.getJSONArray("data");*/
			/*
			 * 带请求头的请求
			 */
			/*Map<String, String> headers = Maps.newHashMap();
			headers.put("token", token);
			Map<String, String> resultString = HttpClientUtil.executeGetMethodWithParas(url, null, headers, 30000);
			returnList = (List<Object>) JSONArray.parse(resultString.get("data"));*/
			
			logger.info("返回所有员工记录条数："+returnList.size());
		} catch (Exception e) {
			logger.error("用户信息-查询所有未创建用户员工信息出现错误  方法 findAllEmp："+ e.getMessage(),e);
		}
		return returnList;
	}

	public RestResponse findUserByEmpId(Integer empId) {
		
		logger.info("findUserByEmpId方法开始执行 参数empId:"+empId);
		ErpSysUserVo erpSysUserVo=new ErpSysUserVo();
		try {
			erpSysUserVo = this.erpSysUserMapper.findUserByEmpId(empId);

		} catch (Exception e) {
			logger.error("用户信息-根据员工ID查询信息出现错误  方法 findUserByEmpId："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(erpSysUserVo);
	}

	public RestResponse findUserInfoByUserId(Integer userId) {

		logger.info("findUserInfoByUserId方法开始执行，参数userId:"+userId);
		ErpSysUserVo erpSysUserVo=new ErpSysUserVo();
		try{
			erpSysUserVo=this.erpSysUserMapper.findUserInfoByUserId(userId);
		}catch(Exception e){
			logger.error("findUserInfoByUserId方法报错："+e.getMessage(),e);
		}
		return RestUtils.returnSuccess(erpSysUserVo);
	}

	//修改用户信息for HR工程调用
	@Transactional(readOnly=false)
	public RestResponse updateUserForHr(Map<String, Object> map) {
		logger.info("updateUserForHr方法开始执行，参数:"+map);
		ErpSysUserVo erpSysUserVo=new ErpSysUserVo();
		erpSysUserVo.setUsername(String.valueOf(map.get("username")));
		erpSysUserVo.setUserPhone(String.valueOf(map.get("userPhone")));
		erpSysUserVo.setId(Integer.valueOf(String.valueOf(map.get("id"))));
		try{
			this.erpSysUserMapper.updateUser(erpSysUserVo);
		}catch(Exception e){
			logger.error("updateUserForHr方法报错："+e.getMessage(),e);
			return RestUtils.returnFailure("updateUserForHr方法报错："+e.getMessage());
		}
		return RestUtils.returnSuccess(erpSysUserVo);
	}
	
	@SuppressWarnings("unchecked")
	public RestResponse findUserByEmpIds(Map<String,Object> map) {
		
		logger.info("findUserByEmpId方法开始执行 参数empId:"+map.toString());
		List<Map<String,Object>> resultList=null;
		Integer[] curPersonIds=null;
		int count=0;
		try {
			String str=String.valueOf(map.get("curPersonIds"));
			List<Object> list=JSON.parseArray(str);
			List< Map<String,Object>> listw = new ArrayList<Map<String,Object>>();
			for(Object object:list){
				Map<String,Object> ret=(Map<String, Object>) object;
				listw.add(ret);
			}
			int len=listw.size();
			curPersonIds=new Integer[len];	
			for(Map<String,Object> m:listw){						
				curPersonIds[count]=Integer.valueOf(String.valueOf(m.get("employeeId")));
				count++;
			}					
			resultList = this.erpSysUserMapper.findUserByEmpIds(curPersonIds);

		} catch (Exception e) {
			logger.error("用户信息-根据员工ID查询信息出现错误  方法 findUserByEmpId："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(resultList);
	}

	public RestResponse findEmpIdListByUserId(Map<String, Object> map) {
		logger.info("findUserListByUserId方法开始执行，参数map:"+map.toString());
		List<Map<String,Object>> resultList=null;
		Integer[] curPersonIds=null;
		int count=0;
		try {
			String str=String.valueOf(map.get("curPersonIds"));
			List<Object> list=JSON.parseArray(str);
			List< Map<String,Object>> listw = new ArrayList<Map<String,Object>>();
			for(Object object:list){
				Map<String,Object> ret=(Map<String, Object>) object;
				listw.add(ret);
			}
			int len=listw.size();
			curPersonIds=new Integer[len];	
			for(Map<String,Object> m:listw){						
				curPersonIds[count]=Integer.valueOf(String.valueOf(m.get("userId")));
				count++;
			}					
			resultList = this.erpSysUserMapper.findEmpIdListByUserId(curPersonIds);

		} catch (Exception e) {
			logger.error("用户信息-根据员工ID查询信息出现错误  方法 findUserByEmpId："+ e.getMessage(),e);
		}
		return RestUtils.returnSuccess(resultList);
	}
		
	/**
	 * add by 张倩 20190212
	 * 根据员工名查询现有基础角色
	 * @return
	 */
	public List<ErpRole> findBaseRoleByUser(String token) {
		
		List<ErpRole> returnList = new ArrayList<>();
		
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
		
		List<ErpRole> roleList = erpRoleMapper.findRoleByUserId(erpUser.getId());
		
		for (ErpRole role : roleList){
			if ((role.getRoleType().equals(1)) && (role.getChildRoleRight().equals(1))){
				//基础角色
				returnList.add(role);
			}
		}		
		return returnList;
	}

	/**
	 * 权限管理
	 * 发送薪酬二次权限校验短信
	 * Copyright: Copyright (C) 2019 Nantian, Inc. All rights reserved.
	 * Company: 北京南天软件有限公司
	 *
	 * @author Libaokun
	 * @since 2019-02-19 22:53
	 */
	@SuppressWarnings("unchecked")
	public RestResponse sendSms(String token) {

		// 短信应用SDK AppID
		int appid = DicConstants.APPID;

		// 短信应用SDK AppKey
		String appkey = DicConstants.APPKEY;

		// get user phone number
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
		String phone = erpUser.getUserPhone();
		String userId = erpUser.getId().toString();

		// 需要发送短信的手机号码
		//String[] phoneNumbers = {phone};

		// 短信模板ID，需要在短信应用中申请
		// NOTE: 这里的模板ID`7839`只是一个示例，真实的模板ID需要在短信控制台中申请
		int templateId = DicConstants.TEMPLATE_ID;

		// 签名
		String smsSign = DicConstants.SMSSIGN;

		String checkCode = getNonceStr();
		// save the sms code into redis
		stringRedisTemplate.opsForValue().set(userId, checkCode, 1, TimeUnit.MINUTES);

		//调用邮件管理工程，将发送短信验证码的必要信息传递过去
		Map<String,Object> smvcParams = new HashMap<>();
		smvcParams.put("checkCode", checkCode);
		smvcParams.put("appid", appid);
		smvcParams.put("appkey", appkey);
		smvcParams.put("phone", phone);
		smvcParams.put("templateId", templateId);
		smvcParams.put("smsSign", smsSign);
		String url = emailServiceHost+"/nantian-erp/secondarycheck/smvc/send";
		Map<String,String> headers = new HashMap<>();
		headers.put("token", token);
		//headers.put("Content-Type", "application/json; charset=utf-8");
		Map<String, String> response = HttpClientUtil.executePostMethodWithParas(url, JSON.toJSONString(smvcParams), headers, "application/json", 30000);
		logger.info("发短信验证码的结果response="+response);
		if(!"200".equals(response.get("code"))) {
			return RestUtils.returnSuccessWithString("短信验证码发送失败！");
		}
		Map<String,Object> resultMap = (Map<String,Object>) JSON.parse(response.get("result"));
		if(!"200".equals(resultMap.get("status"))) {
			return RestUtils.returnSuccessWithString("短信验证码发送失败！");
		}
//		try {
//			//数组具体的元素个数和模板中变量个数必须一致，例如事例中templateId:5678对应一个变量，参数数组中元素个数也必须是一个
//			String[] params = {checkCode};
//			SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
//			SmsSingleSenderResult result = ssender.sendWithParam("86", phoneNumbers[0],
//					// 签名参数未提供或者为空时，会使用默认签名发送短信
//					templateId, params, smsSign, "", "");
//			logger.info("发送结果：result=" + result);
//		} catch (HTTPException e) {
//			// HTTP响应码错误
//			logger.error("HTTP错误" + e.getMessage());
//		} catch (JSONException e) {
//			// json解析错误
//			logger.error("json错误" + e.getMessage());
//		} catch (IOException e) {
//			// 网络IO错误
//			logger.error("网络IO错误" + e.getMessage());
//		}
		return RestUtils.returnSuccessWithString("OK");
	}

	public RestResponse checkSms(String code, String token){
		logger.info("code="+code+",token="+token);
		// get user id
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
		logger.info("erpUser="+erpUser);
		String userId = erpUser.getId().toString();
		if (code.equals(stringRedisTemplate.opsForValue().get(userId))) {
			return RestUtils.returnSuccessWithString("OK");
		} else {
			return RestUtils.returnSuccessWithString("验证码无效！");
		}
	}

	/**
	 * 权限管理
	 * 生成随机验证码：4位数字
	 * Copyright: Copyright (C) 2019 Nantian, Inc. All rights reserved.
	 * Company: 北京南天软件有限公司
	 *
	 * @author Libaokun
	 * @since 2019-02-19 22:53
	 */
	private  String getNonceStr() {
		String symbols = "0123456789";
		Random random = new SecureRandom();
		char[] nonceChars = new char[4];

		for (int index = 0; index < nonceChars.length; ++index) {
			nonceChars[index] = symbols.charAt(random.nextInt(symbols.length()));
		}

		return new String(nonceChars);
	}
	
	/**
	 * Description: 找回密码-发送验证码
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2019年05月14日
	 */
	public RestResponse findPasswordSend(String username) {
		logger.info("进入findPasswordSend方法，参数是：username=" + username);
		//判断用户是否存在
		ErpUser userLogin = new ErpUser();
		username += "@nantian.com.cn";
		userLogin.setUsername(username);
		ErpUser userLoginResult = this.erpSysUserMapper.login(userLogin);
		if (userLoginResult == null) {
			return RestUtils.returnFailure("用户名错误！请重新输入！");
		}
		
		//产生八位随机数，如果是0再产生一次。将随机数作为验证码存入Redis中，保存十分钟
		int checkCodeInt = (int) (Math.random()*100000000);
		while(checkCodeInt==0 || checkCodeInt<10000000) {
			checkCodeInt = (int) (Math.random()*100000000);
		}
		String checkCode = String.valueOf(checkCodeInt);
		stringRedisTemplate.opsForValue().set(DicConstants.REDIS_PREFIX_CHECKCODE+username, checkCode, 10, TimeUnit.MINUTES);
		
		//将验证码发送邮件给用户
		String frommail = "nt_admin@nantian.com.cn";
		String bcc = null;
		String subject = "南天ERP系统找回密码";
		String text = "您正在找回密码，验证码十分钟内有效！验证码为："+checkCode;
		String tomail = username;
		boolean sendSuccess = restTemplateUtils.sendEmail(frommail, bcc, subject, text, tomail);
		if(sendSuccess) {
			return RestUtils.returnSuccess("邮件发送成功，请到邮件中获取验证码！");
		}else {
			return RestUtils.returnFailure("邮件发送失败，请稍后尝试！");
		}
	}
	
	/**
	 * Description: 找回密码-通过验证码校验
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2019年05月15日
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=false)
	public RestResponse findPasswordReset(Map<String,Object> params) {
		logger.info("进入findPasswordReset方法，参数是：params=" + params);
		//获取用户输入的信息
		String username = (String) params.get("username") + "@nantian.com.cn";//用户名
		String checkCode = (String) params.get("checkCode");//验证码
		
		//判断用户验证码输入的是否正确
		String checkCodeFromRedis = stringRedisTemplate.opsForValue().get(DicConstants.REDIS_PREFIX_CHECKCODE + username);
		if(checkCodeFromRedis==null) {
			return RestUtils.returnFailure("验证码已过期！");
		}
		if(!checkCodeFromRedis.equals(checkCode)) {
			return RestUtils.returnFailure("验证码错误！");
		}
		
		//如果验证码验证成功，将12小时的锁定的状态解除
		redisTemplate.delete(DicConstants.REDIS_PREFIX_ERRORCOUNT+username);
		
		// 产生八位随机数，如果是0再产生一次。
		int randomPasswordInt = (int) (Math.random()*100000000);
		while(randomPasswordInt==0 || randomPasswordInt<10000000) {
			randomPasswordInt = (int) (Math.random()*100000000);
		}
		String randomPassword = String.valueOf(randomPasswordInt);
		
		//获取用户ID
		ErpUser userLogin = new ErpUser();
		userLogin.setUsername(username);
		ErpUser userLoginResult = erpSysUserMapper.login(userLogin);
		Integer userId = userLoginResult.getId();
		
		//将随机数作为新密码存入数据库中
		Md5Hash md5 = new Md5Hash(randomPassword, "nantian-erp");
		ErpSysUserVo erpSysUserVo = new ErpSysUserVo();
		erpSysUserVo.setId(userId);
		erpSysUserVo.setPassword(md5.toString());
		erpSysUserMapper.updateUser(erpSysUserVo);
		
		//将新密码发送邮件给用户
		String frommail = "nt_admin@nantian.com.cn";
		String bcc = null;
		String subject = "南天ERP系统找回密码";
		String text = "您的密码已经重置！新密码为："+randomPassword;
		String tomail = username;
		boolean sendSuccess = restTemplateUtils.sendEmail(frommail, bcc, subject, text, tomail);
		if(sendSuccess) {
			return RestUtils.returnSuccess("邮件发送成功，请到邮件中获取新密码！");
		}else {
			return RestUtils.returnFailure("邮件发送失败，请稍后尝试！");
		}
	}
	
	/**
	 * Description: 通过角色ID查询一个角色对应的所有用户信息
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2019年06月04日
	 */
	public RestResponse findAllUserByRoleId(Integer roleId) {
		logger.info("进入findAllUserByRoleId方法，参数是：roleId=" + roleId);
		List<Map<String,Object>> userList = erpSysUserMapper.findAllUserByRoleId(roleId);
		return RestUtils.returnSuccess(userList);
	}

	/**
	 * Description: 根据手机号查询用户信息
	 * 
	 * @Author hhr
	 * @Create Date: 2020年04月08日
	 */
	@SuppressWarnings("unchecked")
	public RestResponse findUserByMobile(Map<String, Object> param) {
		logger.info("findUserByMobile开始执行,参数param:"+param.toString());
		List<Map<String, Object>> userList=new ArrayList<>();
		try{
			List<String> mobilesList=(List<String>) param.get("mobiles");
			if(mobilesList.size()>0){
				userList = this.erpSysUserMapper.findUserByMobile(mobilesList);
			}
		}catch(Exception e){
			logger.error("findUserByMobile发生异常,异常信息:"+e.getMessage());
			return RestUtils.returnFailure("findUserByMobile发生异常");
		}
		return RestUtils.returnSuccess(userList);
	}

	@SuppressWarnings("unchecked")
	public RestResponse findMobileByUserList(Map<String, Object> param) {
		logger.info("findMobileByUserList开始执行,参数param:"+param.toString());
		 Map<String,Object> returnMap=new HashMap<String, Object>();
		List<ErpUser> userList=new ArrayList<>();//返回结果列表
		try{
			List<Integer> userIds=(List<Integer>) param.get("userList");
			if(userIds.size()>0){
				userList = this.erpSysUserMapper.findMobileByUserList(userIds);
				 for(ErpUser erpUser:userList){
					  String userId=String.valueOf(erpUser.getUserId());
					  returnMap.put(userId, erpUser.getUserPhone());
				  }
			}
		  //查询总裁手机号
		  List<Map<String,Object>> userList1 = erpSysUserMapper.findAllUserByRoleId(8);
		  returnMap.put("bossPhone", String.valueOf(userList1.get(0).get("userPhone")));
		}catch(Exception e){
			logger.error("findUserByMobile发生异常,异常信息:"+e.getMessage());
			return RestUtils.returnFailure("findUserByMobile发生异常");
		}
		return RestUtils.returnSuccess(returnMap);
	}
	
}

