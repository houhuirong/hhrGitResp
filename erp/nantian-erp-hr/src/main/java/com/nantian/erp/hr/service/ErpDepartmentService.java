package com.nantian.erp.hr.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.nantian.erp.hr.data.model.*;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.AdminDicMapper;
import com.nantian.erp.hr.data.dao.DepartmentTransfApplyMapper;
import com.nantian.erp.hr.data.dao.DepartmentTransfRecodMapper;
import com.nantian.erp.hr.data.dao.ErpDepartmentMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeePostiveMapper;
import com.nantian.erp.hr.data.dao.ErpPositionRankRelationMapper;
import com.nantian.erp.hr.data.dao.ErpPostMapper;
import com.nantian.erp.hr.data.dao.ErrorEmailLogMapper;
import com.nantian.erp.hr.data.dao.PositionApplyProgressMapper;
import com.nantian.erp.hr.data.dao.TEmailServiceConfigMapper;
import com.nantian.erp.hr.util.FileUtils;
import com.nantian.erp.hr.util.RestTemplateUtils;
import com.nantian.erp.hr.util.SFTPUtil;
/**
 * 部门（组织架构、员工信息）service
 * @author ZhangYuWei
 */
@Service
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties","classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties","classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)

public class ErpDepartmentService {
	 @Value("${protocol.type}")
	  private String protocolType;//http或https
	 
	@Value("${prod.email.interview.bcc}")
	private String prodEmailInterviewBcc;// 生产环境抄送人
	
	/*
	 * 从配置文件中获取SFTP相关属性
	 */
    @Value("${sftp.basePath}")
    private String basePath;//服务器基本路径
    
    @Value("${sftp.departmentTransfPath}")
    private String departmentTransfPath;//岗位信息文件路径
	 
	@Autowired
	private ErpDepartmentMapper departmentMapper;
	
	@Autowired
	
	private AdminDicMapper adminDicMapper;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	ErpEmployeeService employeeService;

	@Autowired
	private ErpEmployeeMapper employeeMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private DepartmentTransfApplyMapper deptransfapplymapper;
	
	@Autowired
	private DepartmentTransfRecodMapper deptransfrecodmapper;
	@Autowired
	private ErpPositionRankRelationMapper erpPositionRankRelationMapper;
	@Autowired
	private ErpEmployeePostiveMapper erpEmployeePostiveMapper;
	@Autowired
	private RestTemplateUtils restTemplateUtils;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	PositionApplyProgressMapper  applyProgressMapper;
	@Autowired
	private ErpPostMapper postMapper;
	@Autowired
	private FileUtils fileUtils;
	@Autowired
	private TEmailServiceConfigMapper emailConfigMapper;
	@Autowired
	private ErrorEmailLogMapper errorEmailLogMapper;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public String addDepartment(ErpDepartment department) {
		logger.info("addDepartment方法开始执行，传递参数：ErpDepartment:" + department.toString());
		Integer superLeader = 0; //上级领导Id
		//判断当他是一级部门时，有上级领导
		try {	
			departmentMapper.insertDepartment2(department);
			logger.info("department=========="+department.toString());
			Map<String,Object> departmentMap = new HashMap<>();//部门基本信息
			departmentMap.put("departmentId", department.getDepartmentId());
			departmentMap.put("departmentName", department.getDepartmentName());
			departmentMap.put("userId", department.getUserId());
			departmentMap.put("superLeader", department.getSuperLeader());
			logger.info("departmentMap=========="+departmentMap);
			redisTemplate.opsForValue().getAndSet(DicConstants.REDIS_PREFIX_DEPARTMENT+ department.getDepartmentId(), departmentMap);

		} catch (Exception e) {
			logger.error("addDepartment方法出现异常：" + e.getMessage(),e);
		}
		return "OK";
	}
	
	public String deleteDepartment(Integer departmentId) {
		//删除部门前，先查询部门有没有被员工占用
		logger.info("deleteDepartment方法开始执行，传递参数：departmentId=" + departmentId);
		try {
			List<Map<String,Object>> employeeList = departmentMapper.findEmployeeList(departmentId);
			if(employeeList!=null && employeeList.size()>0) {
				return "该部门下有员工，不允许删除！";
			}
			
			//删除部门前，先查询部门下有没有二级部门
			List<Map<String,Object>> departmentList = departmentMapper.findAllSecondDepartmentByFirDepId(departmentId);
			if(departmentList!=null && departmentList.size()>0) {
				return "该部门下有二级部门，不允许删除！";
			}
			
			//更新员工与部门的关联表
//			departmentMapper.updateDepartmentName(departmentId);
			
			//删除部门
//			departmentMapper.deleteDepartment(departmentId);
			//部门失效
			Map<String,Object> map = new HashMap<>();			
			map.put("departmentId", departmentId);
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy");
			Date date = new Date();
			map.put("year", format.format(date));
			departmentMapper.invalidDepartment(map);
			
		} catch (Exception e) {
			logger.error("deleteDepartment方法出现异常：" + e.getMessage(),e);
		}
		return "OK";
	}

	public String updateDepartment(ErpDepartment department, String token) {
		logger.info("updateDepartment方法开始执行，传递参数：ErpDepartment:" + department.toString());
		try {
			ErpDepartment oldDepartment = departmentMapper.findByDepartmentId(department.getDepartmentId());

			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer loginUserId = erpUser.getId();
			List<Integer> roles = erpUser.getRoles();// 角色列表
			Integer oldUserId = oldDepartment.getUserId();
			Integer oldSuperLeader = oldDepartment.getSuperLeader();

			if(!roles.contains(8) &&"1".equals(department.getRank())&& 
					((department.getUserId()!=null&&!oldUserId.equals(department.getUserId()))|| 
							(department.getSuperLeader()!=null &&!department.getSuperLeader().equals(oldSuperLeader)))){
				Integer codeUserId = 0;
				if(department.getChangeType() != null && department.getChangeType() == 1){
					codeUserId = oldDepartment.getSuperLeader();
				}else if(department.getChangeType() != null && department.getChangeType() == 2){
					List<Map<String,Object>> list = restTemplateUtils.findAllUserByRoleId(token, 8);
					Map<String,Object> map = list.get(0);
					Integer nextPerson = (Integer) map.get("userId");//注意：userId是员工ID
					codeUserId =nextPerson;
				}else{
					return "验证码无效!";
				}
				MultiValueMap<String, Object> erpUserHttp = new  LinkedMultiValueMap<String, Object>();
				erpUserHttp.add("userId", codeUserId);//员工Id
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.add("token",token);
				HttpEntity<MultiValueMap<String,Object>> request = new HttpEntity<>(erpUserHttp,requestHeaders);
				String userUrl =  protocolType+"nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId";
				ResponseEntity<Map> userResponse = this.restTemplate.postForEntity(userUrl, request, Map.class);
				//跨工程调用响应失败
				if(200 != userResponse.getStatusCodeValue() || !"200".equals(userResponse.getBody().get("status"))) {
					return "调用权限工程时发生异常，响应失败！";
				}
				if(userResponse.getBody().get("data")==null || "".equals(userResponse.getBody().get("data"))) {
					return "调用权限工程时发生异常，响应失败！";
				}
				Map<String,Object> userInfo = (Map<String, Object>) userResponse.getBody().get("data");
				Integer userId = Integer.valueOf(String.valueOf(userInfo.get("id")));//用户Id
				//不是总裁需要发送短信验证
				if (department.getCode() == null ||  !department.getCode().equals(stringRedisTemplate.opsForValue().get(DicConstants.REDIS_PREFIX_EMPLOYEE_SMS + userId))) {
					return "验证码无效!";
				}
			}
			departmentMapper.updateDepartment(department);
			department = departmentMapper.findByDepartmentId(department.getDepartmentId());
			//更新redis中deparetment的内容
			Integer departmentId = department.getDepartmentId();//部门id
			String departmentName = department.getDepartmentName();//部门名
			Integer userId = department.getUserId();//部门经理
			Integer superLeader = department.getSuperLeader();//上级领导
			
			Map<String,Object> departmentMap = new HashMap<>();//部门基本信息
			departmentMap.put("departmentId", departmentId);
			departmentMap.put("departmentName", departmentName);
			departmentMap.put("userId", userId);
			departmentMap.put("superLeader", superLeader);
			logger.info("departmentMap=========="+departmentMap);
			redisTemplate.opsForValue().getAndSet(DicConstants.REDIS_PREFIX_DEPARTMENT+departmentId, departmentMap);
		} catch (Exception e) {
			logger.error("updateDepartment方法出现异常：" + e.getMessage(),e);
		}
		return "OK";
	}

	public List<Map<String, Object>> findAllDepartment() {
		logger.info("findAllDepartment方法开始执行，传递参数：无");
		List<ErpDepartment> resultList = null;
		List<Map<String, Object>> listvo = new ArrayList<>();
 		Map<String,Object> returnMap=new HashMap<>();
		Integer leaderId = 0; //上级领导ID
		try {
			resultList = departmentMapper.findAllDepartment();
			List<Integer> superLeaderIds=new ArrayList<Integer>();
			for(ErpDepartment department:resultList){
				superLeaderIds.add(department.getSuperLeader());
			}
			//add by hhr
			String url = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/findMobileByUserList";
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", request.getHeader("token"));
			Map<String,Object> userBody  = new HashMap<>();
			userBody.put("userList", superLeaderIds);
			HttpEntity<Map<String,Object>> requestEntity=new HttpEntity<>(userBody,requestHeaders);
		    ResponseEntity<Map> response = restTemplate.exchange(url,HttpMethod.POST,requestEntity,Map.class);
		    logger.info("跨工程调用的响应结果response={}",response);
		    Map<String,Object> resultMap = response.getBody();
		    if(response.getStatusCodeValue() != 200 || resultMap.get("data")==null || "".equals(resultMap.get("data"))){
		    	logger.error("权限工程响应失败！");
		    	return listvo;
		    }
		    returnMap = (Map<String, Object>) resultMap.get("data");//解析结果：erp系统员工信息
			for(ErpDepartment dep:resultList){
				Map<String, Object> vo = new HashMap<>();
				vo.put("departmentDuty", dep.getDepartmentDuty());
				vo.put("departmentId", dep.getDepartmentId());
				vo.put("departmentManagerEmail", dep.getDepartmentManagerEmail());
				vo.put("departmentName", dep.getDepartmentName());
				vo.put("departmentType", dep.getDepartmentType());
				vo.put("rank", dep.getRank());
				vo.put("upperDepartment", dep.getUpperDepartment());
				vo.put("userId", dep.getUserId());
				vo.put("superLeaderPhone", returnMap.get(String.valueOf(dep.getSuperLeader())));
				vo.put("bossPhone", returnMap.get("bossPhone"));
				
				leaderId = dep.getSuperLeader();
				vo.put("superLeader", leaderId);//上级领导ID
				vo.put("departmentOrder", dep.getDepartmentOrder());
				Map<String, Object>  mapEmp = null;
				if(leaderId != null){					
					//获取上级领导的姓名
					mapEmp = employeeMapper.selectByEmployeeIdForlx(leaderId);
					if (mapEmp == null)
					{
						continue;
					}
					String  leaderName = String.valueOf(mapEmp.get("name")); //上级领导的姓名
					vo.put("leaderName", leaderName); 

				}else{
					vo.put("leaderName", ""); 
				}
				
				
				
				listvo.add(vo);
//				superLeaders.add(leaderId);
			}
			//调用权限工程 获取上级领导名称
//			List<Map<String, Object>> list = getUserInfo(superLeaders);
			
			
		} catch (Exception e) {
			logger.error("findAllDepartment方法出现异常：" + e.getMessage(),e);

		}
//		return resultList;
		return listvo;
	}
	
	/**
	 * 通过部门ID查看一个部门（ID可为空）
	 * 
	 * @param departmentId
	 * @Author ZhangYuWei
	 */
	public RestResponse findDepartmentBySelectableId(Integer departmentId) {
		logger.info("findDepartmentBySelectableId方法开始执行，传递参数：departmentId="+departmentId);
		List<Map<String,Object>> list = new ArrayList<>();
		try {
			if(departmentId == null) {
				list = departmentMapper.findAllFirstDepartment();
			}else {
				//list = departmentMapper.findAllSecondDepartmentByFirDepId(departmentId);
				list = departmentMapper.findAllSecondDepartmentBySupperId(departmentId);

			}
			return RestUtils.returnSuccess(list);
		} catch (Exception e) {
			logger.error("findDepartmentBySelectableId方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
	}
	
	/**
	 * Description: 条件查询所有一级部门信息（供薪酬工程调用）
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月06日 下午21:52:41
	 */
	public RestResponse findAllFirstDepartmentByPowerParams(Map<String,Object> params) {
		logger.info("findAllFirstDepartmentByPowerParams方法开始执行，传递参数：params="+params);
		try {
			//根据指定的条件查询所有的一级部门
			List<Map<String,Object>> firstDepartmentList = departmentMapper.findAllFirstDepartmentByParams(params);
			return RestUtils.returnSuccess(firstDepartmentList);
		} catch (Exception e) {
			logger.info("findAllFirstDepartmentByPowerParams方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
	}

	/**
	 * 查询权限二级部门列表
	 * @param params
	 * @return
	 */
	public RestResponse findSecondDepartmentByPowerParams(Map<String, Object> params) {
		logger.info("findSecondDepartmentByPowerParams方法开始执行，传递参数：params="+params);
		try {
			//根据指定的条件查询所有的二级部门
			List<Map<String,Object>> secondDepartmentList = departmentMapper.findSecondDepartmentByPowerParams(params);
			return RestUtils.returnSuccess(secondDepartmentList);
		} catch (Exception e) {
			logger.info("findSecondDepartmentByPowerParams方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
	}

	/**
	 * 查询所有的部门类型
	 * @param 
	 * @return  所有部门类型
	 */
	public List<AdminDic> findAllDepartmentType() {
		logger.info("findAllDepartmentType方法开始执行，传递参数：departmentId=" );
		List<AdminDic> list = null;
		try {
				list = adminDicMapper.findApprove(DicConstants.DEPARTMENT_TYPE);
		} catch (Exception e) {
			logger.error("findAllDepartmentType方法出现异常：" + e.getMessage(),e);
		} 
		
		return list;
	}
	/**
	 * 添加部门类型
	 * @param  departMentTypeName
	 * @return  
	 */
	public RestResponse addDepartmentType(String departMentTypeName) {
		logger.info("addDepartmentType方法开始执行，传递参数：departMentTypeName="+departMentTypeName );
		List<AdminDic> list = null;
		AdminDic adminDic = 	adminDicMapper.findLastInsertPostCategory(DicConstants.DEPARTMENT_TYPE);
		Integer code = 0;
		if(adminDic != null){
			code = Integer.valueOf(String.valueOf(adminDic.getDicCode()));
			code = code +1;
		}
		AdminDic  dic = new AdminDic();
		dic.setDicCode(String.valueOf(code));
		dic.setDicName(departMentTypeName);
		dic.setDicType(DicConstants.DEPARTMENT_TYPE);
		
		try {
			int  n = adminDicMapper.addPositionCategory(dic);
		} catch (Exception e) {
			logger.error("addDepartmentType方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("addDepartmentType方法出现异常：" + e.getMessage());
		} 
		
		return RestUtils.returnSuccess("success");
	}

	/**
	 * 通过用户主键查找用户信息
	 * @param id 用户主键
	 * @return  用户userID
	 */
	private Integer getUserInfo(Integer id){
		logger.info("调用权限工程getUserInfo方法参数 {id=}"+id);
	    String	token=	request.getHeader("token");
		List<String> approvePersonBoss = null; // 当申请人二级部门为“本部”时 审批人 返回魏总何总
		//根据用户Id调用权限工程获取 userId 
		MultiValueMap<String, Object> erpUser = new  LinkedMultiValueMap<String, Object>(); //用户对象 map
		erpUser.add("id", id); //参数
		HttpHeaders requestHeaders=new HttpHeaders();
		requestHeaders.add("token",token);//封装token
		HttpEntity<MultiValueMap<String,Object >> request = new HttpEntity<MultiValueMap<String, Object>>(erpUser, requestHeaders); 
		String url = protocolType+"nantian-erp-authentication/nantian-erp/erp/getErpUserForHr"; 
//		String url = "http://nantian-erp-authentication/nantian-erp/erp/getErpUserForHr"; 
		 ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request, RestResponse.class);
		 RestResponse ResponseUser = responseEntity.getBody();
		//{result={"status":"200","msg":"新增成功 ！","data":""}, code=200}
		if(!"200".equals(ResponseUser.getStatus())){
			logger.error("调用权限工程获取用户信息失败"+ResponseUser.getMsg());
			RestUtils.returnFailure("调用权限工程获取用户信息失败"+ResponseUser.getMsg());
		} 
		
		Map<String, Object> erpUser2 = null;
		Integer  ErpUserId  = null; //用户表 userID
		if(ResponseUser.getStatus().equals("200")){
			String jsondata = JSON.toJSONString(ResponseUser.getData());
//			System.out.println("sdjflsdf------"+jsondata);
			if(!jsondata.equals("\"\"")){
				
				JSONObject user = JSONObject.parseObject(jsondata);
				erpUser2 = (Map<String, Object>)ResponseUser.getData();
				if(user != null){
					if(ObjToInteger(user.get("userType")) == 1){
						ErpUserId = ObjToInteger(user.get("userId"));
					}
					
				}
			}
			
		}
		return  ErpUserId;
	}
	
	/**
	 * map对象值转换为 int类型
	 * @param obj
	 * @return
	 */
	public Integer ObjToInteger(Object obj){
		return Integer.valueOf(String.valueOf(obj)) ;
		
	}
	/**
	 * 通过用户主键查找用户信息
	 * @param id 用户主键
	 * @return  用户信息
	 */
	private List<Map<String, Object>> getUserInfo2(List<Integer> ids){
		logger.info("调用权限工程getUserInfo方法参数 {ids=}"+ids);
	    String	token=	request.getHeader("token");
		List<String> approvePersonBoss = null; // 当申请人二级部门为“本部”时 审批人 返回魏总何总
		//根据用户Id调用权限工程获取 userId 
		MultiValueMap<String, Object> erpUser = new  LinkedMultiValueMap<String, Object>(); //用户对象 map
		erpUser.add("ids", ids); //参数
		HttpHeaders requestHeaders=new HttpHeaders();
		requestHeaders.add("token",token);//封装token
		HttpEntity<MultiValueMap<String,Object >> request = new HttpEntity<MultiValueMap<String, Object>>(erpUser, requestHeaders); 
		String url = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/getErpUserForInIds";// /authentication/user
//		String url = "http://nantian-erp-authentication/nantian-erp/erp/getErpUserForHr"; 
		 ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request, RestResponse.class);
		 RestResponse ResponseUser = responseEntity.getBody();
		//{result={"status":"200","msg":"新增成功 ！","data":""}, code=200}
		if(!"200".equals(ResponseUser.getStatus())){
			logger.error("调用权限工程获取用户信息失败"+ResponseUser.getMsg());
			RestUtils.returnFailure("调用权限工程获取用户信息失败"+ResponseUser.getMsg());
		} 
		
		Map<String, Object> erpUser2 = null;
		List<Map<String, Object>> list = null;
		Integer  ErpUserId  = null; //用户表 userID
		if(ResponseUser.getStatus().equals("200")){
			list = (List<Map<String, Object>>)ResponseUser.getData();
			if(list != null && list.size() > 0){
//				System.out.println("aaaaaaaaaa");
			}
			
		}
		return  list;
	}
	
	public RestResponse findDepartmentByUserID(Integer userId) {
		// TODO Auto-generated method stub	
		List<Map> list = null;
		Map<String,Object> map = null;
		List<ErpDepartment> findDepartmentByUserID = null;
		try {
			list = new ArrayList<>();
			
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("rank", "1");
			queryMap.put("userId", userId);
			findDepartmentByUserID = departmentMapper.findDepartmentByUserID(queryMap);
			for (ErpDepartment erpDepartment : findDepartmentByUserID) {
				map = new HashMap<>();
			/*	String superLeaderId= String.valueOf(erpDepartment.getSuperLeader());
				String superLeaderName = adminDicMapper.findDicNameByDicCode(superLeaderId);*/
				map.put("departmentId",erpDepartment.getDepartmentId());
				map.put("departmentName",erpDepartment.getDepartmentName());
				System.out.println("查看...." + erpDepartment.getUpperDepartment() );
				System.out.println("查看2..." + erpDepartment.getSuperLeader());
				if(erpDepartment.getSuperLeader() != null){
					map.put("superLeaderId",erpDepartment.getSuperLeader());
				}else{
					String superLeaderId = adminDicMapper.findUpperUIdByDictype("APPROVER");
					map.put("superLeaderId", superLeaderId);
				}
				list.add(map);
			}
		} catch (Exception e) {
			logger.error("findDepartmentByUserID方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("findDepartmentByUserID方法出现异常：" + e.getMessage());
		} 
		return RestUtils.returnSuccess(list);
	}

	public RestResponse findDepMessByDepartmentID(Integer departmentId) {
		// TODO Auto-generated method stub
		List<Map<String, Object>> list = null;
		try {		
			list = departmentMapper.findSDepMessByDepartmentID(departmentId);
			for (Map<String, Object> map : list) {
					Integer sDepartmentId = Integer.valueOf(map.get("departmentId").toString());	
					List<Map<String, Object>> employeeBySId = employeeMapper.selectEmployeeBySId(sDepartmentId);
					map.put("employeeMess", employeeBySId);
			}
		} catch (Exception e) {
			logger.error("findDepMessByDepartmentID方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("findDepMessByDepartmentID方法出现异常：" + e.getMessage());
		} 	
		return RestUtils.returnSuccess(list);
	}

	public RestResponse findDepartmentBySLeaderId(Integer id){
		List<Map<String, Object>> list = null;
		try {
			Map<String, Object> mapL = new HashMap<>();
			mapL.put("sLeaderId",id);
			String token = request.getHeader("token");
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			List<Integer> roles = erpUser.getRoles();
			for (Integer role : roles) {
				if(role == 8 ) {
					mapL.put("role",role);
				}
			}
			System.out.println("findDepartmentBySLeaderId 该方法中的 erpUser : " + erpUser);
		
			list = departmentMapper.findDepMessBySLeader(mapL);
			for (Map<String, Object> map : list) {
				Integer departmentId = Integer.valueOf(map.get("departmentId").toString());
				List<Map<String, Object>> secondDepByFirstDep = departmentMapper.findSecondDepByFirstDep(departmentId);
				map.put("SecondDep",secondDepByFirstDep);
				map.put("approverId",id);
				if(secondDepByFirstDep != null){
					for (Map<String, Object> map2 : secondDepByFirstDep) {
						Integer sDepartmentId = Integer.valueOf(map2.get("departmentId").toString());
						List<Map<String, Object>> employeeBySId = employeeMapper.selectEmployeeBySId(sDepartmentId);
						map2.put("employeeMess", employeeBySId);
					}
				}
			}
		} catch (Exception e) {
			logger.error("findDepartmentBySLeaderId方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("findDepartmentBySLeaderId方法出现异常：" + e.getMessage());
		} 	
		return RestUtils.returnSuccess(list);
	}
	
	public RestResponse findFirstDepartments() {
		List<Map<String, Object>> findFirstDepartments = null;
		try {
			findFirstDepartments = departmentMapper.findFirstDepartments();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return RestUtils.returnSuccess(findFirstDepartments);
	}

	public RestResponse findFirstDepBySuperById(Integer employeeId) {
		try {
			List<Map<String, Object>> firstDepBySuperById = departmentMapper.findFirstDepBySuperById(employeeId);
			return RestUtils.returnSuccess(firstDepBySuperById);
		} catch (Exception e) {
			logger.error("findFirstDepBySuperById方法出现异常：" + e.getMessage());
			return RestUtils.returnFailure("findFirstDepBySuperById方法出现异常：" + e.getMessage());
		}
	}
	/*==================================部门调动=============================================*/
	/**
	 * @author gaolp
	 * 新增调整申请
	 * @param params
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse addgrpTransfApply(Map<String,Object> params, String token) throws Exception{
		logger.info("addgrpTransfApply方法开始执行，参数是：params={}",params);

		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
		Integer loginEmployeeId = erpUser.getUserId();   //员工id
		//接收前端传递的参数
		Integer employeeId = (Integer) params.get("employeeId"); //员工id
		Integer newFirstDepartment = (Integer) params.get("newFirstDepartment"); //新一级部门
		Integer newSecDepartment = (Integer) params.get("newSecDepartment"); //新二级部门
		Integer oldFirstDepartment = (Integer) params.get("oldFirstDepartment"); //原一级部门
		Integer oldSecDepartment = (Integer) params.get("oldSecDepartment"); //原二级部门
		String startTime = (String) params.get("time"); //生效时间
		Integer status = (Integer) params.get("status"); //状态0暂存，1提交
		//Integer newPositionId =  params.get("newPositionId") == null  ? null : (Integer) params.get("newPositionId"); //新职位ID

		//Boolean isChangePosition = (Boolean) params.get("isChangePosition"); //是否改变职位

		String reason = (String) params.get("reason"); //调整原因

		//根据申请状态做部门调动申请的重复判断
		List<Map<String, Object>> result = deptransfapplymapper.findtransfRecodeByStartTime(employeeId);
		if(result != null && !result.isEmpty()){
			Map<String,Object> returnMessage = new HashMap<String, Object>();
			returnMessage.put("Message", "已有调动申请，请勿重复操作！");
			return RestUtils.returnSuccess(returnMessage, "Alert");
		}
		ErrorEmailLog errorEmailLog = saveTransf(employeeId, newFirstDepartment, newSecDepartment, startTime, status, reason, loginEmployeeId, null);
		//发送邮件
		if(errorEmailLog != null){
			try {
				boolean sendSuccess = restTemplateUtils.sendEmail(errorEmailLog.getSender(), errorEmailLog.getBcc(), errorEmailLog.getSubject(), errorEmailLog.getEmailMessage(), 
						errorEmailLog.getRecipient(),DicConstants.DEPARTMENT_TRANSF_APPLY_EMAIL_TYPE,null);
				if(sendSuccess){
					return RestUtils.returnSuccess("提交成功！");
				}else{
					return RestUtils.returnSuccess("提交成功,但邮件发送失败！");
				}
			}catch (Exception e){
				logger.error("addgrpTransfApply 发送邮件数据错误"+e, e);
			}
		}
		return RestUtils.returnSuccess("已提交");
	}
	
	/**
	 * @author gaolp
	 * 申请审批
	 * 根据登录人员id查询待其审批的申请
	 */
	public RestResponse findWaitMsgById(Map<String,Object> param){
		logger.info("findWaitMsgById方法开始执行");
		List<Map<String, Object>> results = null;
		try{
			String token = request.getHeader("token");
			ErpUser erpuser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer processor = erpuser.getUserId();
			param.put("processor", processor);
			results = deptransfapplymapper.findWaitMsgById(param);			
			return RestUtils.returnSuccess(results);
		}catch (Exception e) {
			logger.error("findWaitMsgById方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致查询失败！");
		}
		
	}
	
	/**
	 * 部门调动申请——批准
	 * @param token
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor = Exception.class)
	public RestResponse agreeTransfApply(String token,Map<String,Object> params) throws  Exception{

		logger.info("agreeTransfApply方法开始执行；参数：params={}",params);
		ErpUser erpuser =  (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
		//接收前端传递的参数
		Integer transferApplyID = params.get("id") == null ? null : (Integer) params.get("id"); //申请id
		List<Integer> transferApplyIdList =  params.get("idList") == null ? null : (List)params.get("idList"); //申请id列表
		String startTime = (String) params.get("startTime");
		//查询部门审批邮件发送开关
		Map<String,Object> queryMap=new HashMap<>();
		queryMap.put("type", 0);
		List<Map<String, Object>> emailConfigList = emailConfigMapper.selectByParam(queryMap);
		Integer sendStatus=Integer.valueOf(String.valueOf(emailConfigList.get(0).get("send")));
		boolean sendSuccess=true;
		if(transferApplyIdList != null){
			//批量审批
			for( Integer transferApplyId: transferApplyIdList){
				RestResponse restResponse = agreeTransfDept(erpuser, transferApplyId, startTime, token);
				if(restResponse != null){
					continue;
				}
			}
		int size=10;//审批通过后批量发送邮件规模
		int number=transferApplyIdList.size()/size;
		int remainder=transferApplyIdList.size()%size;
		for(int i=0;i<number;i++){
			List<Integer> list=new ArrayList<>();
			List<Integer> subTransferApplyIdList=new ArrayList<>(transferApplyIdList.subList(i*size, (i+1)*size));
			for(Integer applyId:subTransferApplyIdList){
				Map<String, Object> transfapply = deptransfapplymapper.findtransfRecodeById(applyId);
				if(transfapply.get("newManager").equals(erpuser.getUserId())){
					list.add(applyId);
				}
			}
			if(list.size()!=0){
				ErrorEmailLog errorEmailLog = transfDeptSendEmail(token,list,startTime);
				if(sendStatus.equals(1)){
				String attachmentPath=errorEmailLog.getAttachmentPath();
				String[] strs=attachmentPath.split("/");
				String fileName=strs[strs.length-1];
				sendSuccess = restTemplateUtils.sendEmailWithAttachment(errorEmailLog.getSender(), errorEmailLog.getBcc(), errorEmailLog.getSubject(), 
						errorEmailLog.getEmailMessage(), errorEmailLog.getRecipient(),fileName,attachmentPath,
						DicConstants.DEPARTMENT_TRANSF_APPROVAL_OA_EMAIL_TYPE,null);
				}else{
					errorEmailLog.setCreateTime(ExDateUtils.getCurrentDateTime());
					errorEmailLog.setErrorLog("邮件审批邮件不发送");
					errorEmailLog.setEmailServiceType(DicConstants.DEPARTMENT_TRANSF_APPROVAL_OA_EMAIL_TYPE);
					this.errorEmailLogMapper.insertSelective(errorEmailLog);
				}
			}
		}
		if(remainder > 0){
			List<Integer> list=new ArrayList<>();
			List<Integer> subTransfApplyList=new ArrayList<>(transferApplyIdList.subList(number*size, transferApplyIdList.size()));
			for(Integer applyId:subTransfApplyList){
				Map<String, Object> transfapply = deptransfapplymapper.findtransfRecodeById(applyId);
				if(transfapply.get("newManager").equals(erpuser.getUserId())){
					list.add(applyId);
				}
			}
			if(list.size()!=0){
				ErrorEmailLog errorEmailLog = transfDeptSendEmail(token,subTransfApplyList,startTime);
				if(sendStatus.equals(1)){
					String attachmentPath=errorEmailLog.getAttachmentPath();
					String[] strs=attachmentPath.split("/");
					String fileName=strs[strs.length-1];
					sendSuccess = restTemplateUtils.sendEmailWithAttachment(errorEmailLog.getSender(), errorEmailLog.getBcc(), 
							errorEmailLog.getSubject(), errorEmailLog.getEmailMessage(), errorEmailLog.getRecipient(),
							fileName,attachmentPath,DicConstants.DEPARTMENT_TRANSF_APPROVAL_OA_EMAIL_TYPE,null);
				}else{
					errorEmailLog.setCreateTime(ExDateUtils.getCurrentDateTime());
					errorEmailLog.setErrorLog("邮件审批邮件不发送");
					errorEmailLog.setEmailServiceType(DicConstants.DEPARTMENT_TRANSF_APPROVAL_OA_EMAIL_TYPE);
					this.errorEmailLogMapper.insertSelective(errorEmailLog);
				}
			}
		}
		if(sendSuccess){
			return RestUtils.returnSuccess("邮件发送成功！");
		}else{
			return RestUtils.returnSuccess("邮件发送失败！");
		}
		}else{
			//单个审批
			RestResponse restResponse = agreeTransfDept(erpuser, transferApplyID, startTime, token);
			if(restResponse != null){
				return restResponse;
			}
			
			List<Integer> transferApplyIDs=new ArrayList<>();
			Map<String, Object> transfapply = deptransfapplymapper.findtransfRecodeById(transferApplyID);
			if(transfapply.get("newManager").equals(erpuser.getUserId())){
				transferApplyIDs.add(transferApplyID);
			}
			if(transferApplyIDs.size()!=0){
				ErrorEmailLog errorEmailLog = transfDeptSendEmail(token,transferApplyIDs,startTime);
				if(sendStatus.equals(1)){
					String attachmentPath=errorEmailLog.getAttachmentPath();
					String[] strs=attachmentPath.split("/");
					String fileName=strs[strs.length-1];
					sendSuccess= restTemplateUtils.sendEmailWithAttachment(errorEmailLog.getSender(), errorEmailLog.getBcc(), errorEmailLog.getSubject(), 
							errorEmailLog.getEmailMessage(), errorEmailLog.getRecipient(),fileName,attachmentPath,DicConstants.DEPARTMENT_TRANSF_APPROVAL_OA_EMAIL_TYPE,null);
				}else{
					errorEmailLog.setCreateTime(ExDateUtils.getCurrentDateTime());
					errorEmailLog.setErrorLog("邮件审批邮件不发送");
					errorEmailLog.setEmailServiceType(DicConstants.DEPARTMENT_TRANSF_APPROVAL_OA_EMAIL_TYPE);
					this.errorEmailLogMapper.insertSelective(errorEmailLog);
				}
				if(sendSuccess){
					return RestUtils.returnSuccess("邮件发送成功！");
				}else{
					return RestUtils.returnSuccess("邮件发送失败！");
				}
			}
		}
		return RestUtils.returnSuccess("OK");
	}

	@Transactional(rollbackFor = Exception.class)
	public RestResponse  agreeTransfDept(ErpUser erpuser, Integer transferApplyId, String startTime,String token) throws Exception{

		//新建对象
		Map<String, Object> transfapply = deptransfapplymapper.findtransfRecodeById(transferApplyId);


		//当前登录人的信息
		ErpUser currentUser = (ErpUser) redisTemplate.opsForValue().get(token);
		Integer currentEmployeeId = currentUser.getUserId();//员工ID
		String currentEmployeeEmail = currentUser.getUsername();//员工邮箱
		Map<String, Object> currentEmployeeInfo = (Map<String, Object>) redisTemplate.opsForValue()
				.get(DicConstants.REDIS_PREFIX_EMPLOYEE + currentEmployeeId);//员工信息
		String currentEmployeeName = (String) currentEmployeeInfo.get("employeeName");//员工姓名
		logger.info("当前登录人的员工信息"+currentEmployeeInfo);

		//部门调动员工的信息
		Integer transfrEmployeeId = (Integer) transfapply.get("employeeId");//员工ID
		Map<String, Object> transfrEmployeeInfo = (Map<String, Object>) redisTemplate.opsForValue()
				.get(DicConstants.REDIS_PREFIX_EMPLOYEE + transfrEmployeeId);//员工信息
		String transfrEmployeeName = (String) transfrEmployeeInfo.get("employeeName");//员工姓名
		logger.info("部门调动员工的员工信息"+transfrEmployeeInfo);
		String frommail = currentEmployeeEmail;//发件人（当前登录人）
		String bcc = prodEmailInterviewBcc;//抄送HR
		String subject = "员工部门调动情况提醒";



		String newFirstDepartmentName = String.valueOf(transfapply.get("newFirstDepartmentName"));
		String newSecDepartmentName = String.valueOf(transfapply.get("newSecDepartmentName"));
		String oldFirstDepartmentName = String.valueOf(transfapply.get("oldFirstDepartmentName"));
		String oldSecDepartmentName = String.valueOf(transfapply.get("oldSecDepartmentName"));

		Map<String, Object> oldManagerInfo = (Map<String, Object>) redisTemplate.opsForValue()
				.get(DicConstants.REDIS_PREFIX_EMPLOYEE + Integer.valueOf(String.valueOf(transfapply.get("oldManager"))));//旧一级部门经理
		String oldManagerName = oldManagerInfo == null ? "" : (String) oldManagerInfo.get("employeeName");//旧一级部门经理姓名



		if (transfapply == null){
			return RestUtils.returnFailure("通过id无法查询到调动申请");
		}

		DepartmentTransfApply updateApply = new DepartmentTransfApply();
		updateApply.setId(transferApplyId);
		updateApply.setStartTime(startTime);

		if(transfapply.get("newManager").equals(erpuser.getUserId())){
			//当前处理人是新部门经理
			//修改申请状态为通过

			updateApply.setStatus(DicConstants.TSAPPLY_SUCCESS);//审批通过
			updateApply.setProcessor(0);

			//如果申请的生效时间<=当天时间；更新员工部门信息
			String nowdate = ExDateUtils.getTodayString();
			if(startTime.compareTo(nowdate)<=0){
				transfapply.put("startTime", startTime);
				changeEmployeeDepInfo(transfapply);
				updateApply.setStatus(DicConstants.TSAPPLY_END);//调整完成
			}


			Integer newsecdeptManager = Integer.valueOf(String.valueOf(transfapply.get("newsecdeptManager")));
			Integer newFirstManager = Integer.valueOf(String.valueOf(transfapply.get("newManager")));
			Integer newFirstDepartmentSuperLeader = Integer.valueOf(String.valueOf(transfapply.get("newFirstDepartmentSuperLeader")));


			//查询员工是否有待转正
			Integer employeeId = Integer.valueOf(String.valueOf(transfapply.get("employeeId")));
			List<ErpEmployeePostive> employeePostiveList = erpEmployeePostiveMapper.findPostiveByEmployeeId(employeeId);

			//更新
			for(ErpEmployeePostive employeePostive : employeePostiveList){
				erpEmployeePostiveMapper.updateCurrentPersonIdById(employeePostive.getId(), newsecdeptManager);
			}
//			//查询员工是否有待审批的岗位申请
//			List<ErpPost> postList = postMapper.findWaitPostByProposerId(employeeId);
//			for(ErpPost post : postList){
//
//				Map<String, Object> updateMap = new HashMap();
//				updateMap.put("postId", post.getPostId());
//				if(post.getStatus() == 1){
//					updateMap.put("processor", newFirstDepartmentSuperLeader);
//				}else if(post.getStatus() == 4){
//					updateMap.put("processor", newFirstManager);
//				}else{
//					continue;
//				}
//				applyProgressMapper.updateCurrentPersonIdByPostId(updateMap);
//			}

			try{
				HttpHeaders requestHeaders=new HttpHeaders();
				requestHeaders.add("token",token);//将token放到请求头中
				//调用项目工程更新请假审批人
				String projectUrl = protocolType+"nantian-erp-project/nantian-erp/project/leave/updateProcessorById";
				Map<String,Object> projectParam = new HashMap<>();
				projectParam.put("newFirstManager", newFirstManager);
				projectParam.put("newsecdeptManager", newsecdeptManager);
				projectParam.put("employeeId", employeeId);

				HttpEntity<Map<String,Object>> projectRequest = new HttpEntity<>(projectParam, requestHeaders);

				ResponseEntity<Map> projectResponse = this.restTemplate.postForEntity(projectUrl, projectRequest, Map.class);
				if(projectResponse.getStatusCodeValue() != 200){
					logger.error("项目工程响应失败！导致请假处理人修改失败！");
					//return RestUtils.returnFailure("项目工程响应失败！导致请假处理人修改失败！");
				}

				//调用薪酬工程、待处理的上岗工资单、待处理的转正工资单
				String url = protocolType+"nantian-erp-salary/nantian-erp/salary/payRollFlow/updateCurrentPersonIdById";
				Map<String,Object> payRollFlow = new HashMap<>();
				payRollFlow.put("newFirstManager", newFirstManager);
				payRollFlow.put("newFirstDepartmentSuperLeader", newFirstDepartmentSuperLeader);
				payRollFlow.put("employeeId", employeeId);


				HttpEntity<Map<String,Object>> request = new HttpEntity<>(payRollFlow, requestHeaders);

				ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
				if(response.getStatusCodeValue() != 200){
					logger.error("薪酬工程响应失败！导致工资单处理人修改失败！");
					//return RestUtils.returnFailure("薪酬工程响应失败！导致工资单处理人修改失败！");
				}
			}catch (Exception e){
				logger.error("部门调动调用其他工程失败！"+ e , e);
			}

            // add by ZhangYuWei 20190614  新部门的一级部门经理批准后，给两个一级部门经理、被调动员工、HR发邮件提醒(staring)
			try{
				String text = "员工部门调动审批完成通知：<br/>" +
							   "&nbsp;&nbsp;&nbsp;&nbsp;" + transfrEmployeeName + " 的部门调动已经审批完成,调出部门为：" + oldFirstDepartmentName+"-" + oldSecDepartmentName + ";审批人： " +  oldManagerName +
								",调入部门为：" + newFirstDepartmentName + "-" + newSecDepartmentName + ";审批人：" + currentEmployeeName+",调动生效时间为："+ startTime + "。<br/>"+
								"&nbsp;&nbsp;&nbsp;&nbsp;请各位知悉。";
				//通过员工ID查询用户的邮箱（原一级部门经理、现一级部门经理，调整部门的员工）
				String oldManagerEmail = restTemplateUtils.findUsernameByEmployeeId(token, (Integer)transfapply.get("oldManager"));
				String newManagerEmail = restTemplateUtils.findUsernameByEmployeeId(token, (Integer)transfapply.get("newManager"));
				String transfrEmployeeEmail="";//调转员工邮箱
				//查询调转员工详情
				Map<String, Object>  employeeDetail = employeeMapper.findEmployeeDetail(employeeId);
				String status =String.valueOf(employeeDetail.get("status"));
				if("0".equals(status)){
					transfrEmployeeEmail=String.valueOf(employeeDetail.get("personalEmail"));//员工表中的个人邮箱
				}else{
					transfrEmployeeEmail = restTemplateUtils.findUsernameByEmployeeId(token, (Integer)transfapply.get("employeeId"));
				}
				String tomail = oldManagerEmail+","+newManagerEmail+","+transfrEmployeeEmail;
				boolean sendSuccess = restTemplateUtils.sendEmail(frommail, bcc, subject, text, tomail,DicConstants.DEP_TRANSF_IN_APPROVAL_EMAIL_TYPE,null);
				if (!sendSuccess) {
					logger.error("审批操作已完成，但是通知邮件发送失败！");
					//return RestUtils.returnSuccess("审批操作已完成，但是通知邮件发送失败！");
				}
			}catch (Exception e){
				logger.error("部门调动审批通过调用邮件工程失败！"+ e , e);
			}
            // add by ZhangYuWei 20190614  新部门的一级部门经理批准后，给两个一级部门经理、被调动员工、HR发邮件提醒(end)
		} else{
			updateApply.setStatus(DicConstants.TSAPPLY_LOADDING);//审批中
			updateApply.setProcessor((Integer) transfapply.get("newManager"));

			String text = transfrEmployeeName+"的部门调动申请，调出部门已经审批，请您尽快审批调入。";
			//通过员工ID查询用户的邮箱（现一级部门经理）
			String newManagerEmail = restTemplateUtils.findUsernameByEmployeeId(token, (Integer)transfapply.get("newManager"));
			String tomail = newManagerEmail;
			boolean sendSuccess = restTemplateUtils.sendEmail(frommail, bcc, subject, text, tomail,DicConstants.DEP_TRANSF_OUT_APPROVAL_EMAIL_TYPE,null);
			if (!sendSuccess) {
				logger.error("审批操作已完成，但是通知邮件发送失败！");
				//return RestUtils.returnSuccess("审批操作已完成，但是通知邮件发送失败！");
			}
		}

		//更新部门调整流程
		deptransfapplymapper.updataTransfStatus(updateApply);

		//新增审批记录
		DepartmentTransfRecod transfrcode = new DepartmentTransfRecod();
		transfrcode.setProcessor(erpuser.getUserId());
		transfrcode.setContent("同意申请");
		transfrcode.setTransferApplyID(transferApplyId);//申请id
		transfrcode.setRcTime(ExDateUtils.getCurrentStringDateTime());
		deptransfrecodmapper.insertTransfRecod(transfrcode);
		if(transfapply.get("newManager").equals(erpuser.getUserId())){

			//部门调动审批通过后自动使总裁审批(审批记录里加总裁审批)
			DepartmentTransfRecod transfrBossCode = new DepartmentTransfRecod();
			//通过权限工程查询总裁的员工ID
			List<Map<String,Object>> list = restTemplateUtils.findAllUserByRoleId(token, 8);
			Map<String,Object> map = list.get(0);
			Integer bossUserId = (Integer) map.get("userId");//注意：userId是员工ID
			transfrBossCode.setProcessor(bossUserId);
			transfrBossCode.setContent("同意申请");
			transfrBossCode.setTransferApplyID(transferApplyId);//申请id
			Calendar nowTime=Calendar.getInstance();
			nowTime.add(Calendar.MINUTE, 2);
			transfrBossCode.setRcTime(ExDateUtils.dateToString(nowTime.getTime(), "yyyy-MM-dd HH:mm:ss"));
			deptransfrecodmapper.insertTransfRecod(transfrBossCode);

		}

		return null;
	}
	
	public void changeEmployeeDepInfo(Map<String, Object> transfapply){
		//修改员工表
		ErpEmployee employee = new ErpEmployee();
		Integer employeeId = (Integer) transfapply.get("employeeId");
		Integer newFirstDepartment = (Integer) transfapply.get("newFirstDepartment");
		Integer newSecDepartment = (Integer) transfapply.get("newSecDepartment");
		//Integer newPositionId = (Integer) transfapply.get("new_position_id");
		String startTime = (String) transfapply.get("startTime");
		
		try{
			//根据职位职级id查询职位详情信息
			//ErpPositionRankRelation erpPositionRankRelation = erpPositionRankRelationMapper.selectErpPositionRankRelationByPostionNo(newPositionId);
			employee.setEmployeeId(employeeId);
			employee.setFirstDepartment(newFirstDepartment);
			employee.setSecondDepartment(newSecDepartment);
			//employee.setPositionId(newPositionId);
			//employee.setPosition(erpPositionRankRelation.getPositionName());
			//employee.setRank(erpPositionRankRelation.getRank());
			logger.info("changeEmployeeDepInfo    employeeId=" + employeeId + "    newFirstDepartment="+newFirstDepartment + "    newSecDepartment" + newSecDepartment);
			employeeMapper.updateEmployee(employee);

			ErpEmployee employeeInfo  = employeeMapper.findEmployeeDetailById(employeeId);

			Map<String,Object> employeeMap = new HashMap<>();//员工基本信息
			employeeMap.put("employeeId", employeeId);
			employeeMap.put("employeeName", employeeInfo.getName());
			employeeMap.put("sex", employeeInfo.getSex());
			employeeMap.put("firstDepartmentId", employeeInfo.getFirstDepartment());
			employeeMap.put("secondDepartmentId", employeeInfo.getSecondDepartment());
			employeeMap.put("status", employeeInfo.getStatus());
			employeeMap.put("statusName", employeeInfo.getStatusName());
			employeeMap.put("position", employeeInfo.getPosition());
			employeeMap.put("dimissionTime", employeeInfo.getDimissionTime());
			//员工信息放入redis
			redisTemplate.opsForValue().set(DicConstants.REDIS_PREFIX_EMPLOYEE+employeeId, employeeMap);



			/*更新部门与员工的归属关系*/      
			//查询员工当前部门归属
			Map<String, Object> key = new HashMap<>();
			key.put("employeeId",employeeId);
			key.put("currentDepartment",true);
			List<Map<String, Object>> relationList = this.departmentMapper.getEmpDepRelation(key);
			if (relationList.size()>1){
				logger.error("员工存在多个当前部门关联："+employeeId);
			}
			for(Map<String, Object> relation : relationList){
				if(!relation.get("departmentId").equals(newFirstDepartment)){
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date startNew = format.parse(startTime);
					Date startCurrent = format.parse(String.valueOf(relation.get("startTime")));
					
					if (startNew.compareTo(startCurrent) < 0){
						//当前部门归属的开始时间大于导入的生效时间，直接修改部门id
						relation.put("departmentId", newFirstDepartment);
						this.departmentMapper.updateEmpDepRelation(relation);
					}
					else{
						Date last = new Date(startNew.getTime()-1000*3600*24);
						String lastDay = format.format(last);	
						relation.put("endTime", lastDay);
						this.departmentMapper.updateEmpDepRelation(relation);
						
						Map<String, Object> newRelation = new HashMap<>();
						newRelation.put("employeeId",employeeId);
						newRelation.put("departmentId",newFirstDepartment);
						newRelation.put("startTime",startTime);
						this.departmentMapper.insertEmpDepRelation(newRelation);
					}
				}
			}		
		}catch (Exception e){
			logger.error("changeEmployeeDepInfo方法发生异常",e.getMessage(),e);
		}
		return;
	}
	
	/**
	 * 部门调动申请——驳回
	 * @param token
	 * @param params
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse disagreeTransfApply(String token,Map<String,Object> params) throws  Exception{
		logger.info("进入disagreeTransfApply方法，参数是：token={},params={}",token,params);

		//从缓存中获取登录用户信息
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
		Integer processor = erpUser.getUserId();//审批人Id

		//前端参数
		Integer transferApplyID = params.get("id") == null? null : (Integer) params.get("id"); //申请id
		List<Integer> transferApplyIdList =  params.get("idList") == null ? null : (List)params.get("idList"); //申请id列表
		//Integer employeeId = (Integer) params.get("employeeId"); //员工id
		String content = (String) params.get("content");      //驳回理由
		if(transferApplyIdList != null){
			for (Integer transferApplyId : transferApplyIdList){
				DepartmentTransfApply departmentTransfApply = deptransfapplymapper.findtransfInfoById(transferApplyId);

				//修改工作申请调动的状态
				DepartmentTransfApply transfApply = new DepartmentTransfApply();
				transfApply.setId(transferApplyId);
				transfApply.setStatus(DicConstants.TSAPPLY_FAIL);//调整结束
				transfApply.setProcessor(departmentTransfApply.getApplyPersonId());
				deptransfapplymapper.updataTransfStatus(transfApply);

				//新增审批记录
				DepartmentTransfRecod transfrcode = new DepartmentTransfRecod();
				transfrcode.setProcessor(processor);
				transfrcode.setContent("驳回申请，原因："+content);
				transfrcode.setTransferApplyID(transferApplyId);//申请id
				transfrcode.setRcTime(ExDateUtils.getCurrentStringDateTime());
				deptransfrecodmapper.insertTransfRecod(transfrcode);
			}
		}else {
			DepartmentTransfApply departmentTransfApply = deptransfapplymapper.findtransfInfoById(transferApplyID);

			//修改工作申请调动的状态
			DepartmentTransfApply transfApply = new DepartmentTransfApply();
			transfApply.setId(transferApplyID);
			transfApply.setStatus(DicConstants.TSAPPLY_FAIL);//调整结束
			transfApply.setProcessor(departmentTransfApply.getApplyPersonId());
			deptransfapplymapper.updataTransfStatus(transfApply);

			//新增审批记录
			DepartmentTransfRecod transfrcode = new DepartmentTransfRecod();
			transfrcode.setProcessor(processor);
			transfrcode.setContent("驳回申请，原因："+content);
			transfrcode.setTransferApplyID(transferApplyID);//申请id
			transfrcode.setRcTime(ExDateUtils.getCurrentStringDateTime());
			deptransfrecodmapper.insertTransfRecod(transfrcode);
		}

		return RestUtils.returnSuccess("OK");
	}
	
	/**
	 * 部门调动申请——撤回
	 * @param token
	 * @param params
	 * @return
	 */
	public RestResponse cancleTransfApply(String token,Map<String,Object> params){
		logger.info("进入cancleTransfApply方法，参数是：token={},params={}",token,params);
		try{
			//从缓存中获取登录用户信息
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			Integer processor = erpUser.getUserId();//处理人Id
			
			//前端参数
			Integer transferApplyID = (Integer) params.get("id"); //申请id
			Integer employeeId = (Integer) params.get("employeeId"); //员工id
			String content = (String) params.get("content");      //撤回理由
						
			//修改工作申请调动的状态
			DepartmentTransfApply transfApply = new DepartmentTransfApply();
			transfApply.setId(transferApplyID);
			transfApply.setStatus(DicConstants.TSAPPLY_FAIL);//调整结束
			transfApply.setProcessor(employeeId);
			deptransfapplymapper.updataTransfStatus(transfApply);
			
			//新增审批记录
			DepartmentTransfRecod transfrcode = new DepartmentTransfRecod();
			transfrcode.setProcessor(processor);
			transfrcode.setContent("撤回申请，原因："+content);
			transfrcode.setTransferApplyID(transferApplyID);//申请id
			transfrcode.setRcTime(ExDateUtils.getCurrentStringDateTime());
			deptransfrecodmapper.insertTransfRecod(transfrcode);
			
			return RestUtils.returnSuccess("OK");
		}catch (Exception e) {
			logger.error("cancleTransfApply方法出现异常:",e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致驳回工时审批失败！");
		}
	}
	
	//查询审批记录
	public RestResponse findtransfRecode(Integer transferApplyID,String token){
		logger.info("进入findtransfRecode方法，参数是：transferApplyID={}",transferApplyID);
		List<Map<String, Object>> result = null;
		try{
			result = this.deptransfrecodmapper.findtransfRecode(transferApplyID);
			return RestUtils.returnSuccess(result);
		}catch (Exception e) {
			logger.error("findtransfRecode方法出现异常:",e.getMessage(),e);
			return RestUtils.returnFailure("方法已发生异常，查询失败！");
		}
	}
	
	//查询登录人提交的申请以及申请状态
	public RestResponse findtransfRecodeBytoken(String token){
		logger.info("进入findtransfRecodeBytoken方法，token={}",token);
		List<Map<String, Object>> results = null;
		List<Map<String,Object>> params = null;
		try{
			//从缓存中获取登录用户信息
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			Integer employeeId = erpUser.getUserId();//员工Id
			results = this.deptransfapplymapper.findtransfRecodeBytoken(employeeId);			
			return RestUtils.returnSuccess(results);
		}catch (Exception e) {
			logger.error("findtransfRecodeBytoken方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("查询调动申请方法发生异常，导致失败！");
		}
		
	}
	
	/**
	 * 删除申请
	 * @param id
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse deleteTransf(Integer id) throws Exception{
			logger.info("进入deleteTransf方法，参数是：id={}",id);
			//删除申请
			deptransfapplymapper.deleteTransferApply(id);
			
			//删除审批记录
			Map<String, Object> map = new HashMap<>();
			map.put("transferApplyID", id);
			deptransfrecodmapper.deleteTransfRecode(map);
			
			return RestUtils.returnSuccess("删除成功！");
	}

	/**
	 * 批量删除调整部门申请
	 * @param params
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse batchDeleteTransfApply(Map<String, Object> params) throws Exception {
		logger.info("进入batchDeleteTransfApply方法，参数是：params={}",params);
		if(params != null && params.get("idList") != null && ((List<Integer>)params.get("idList")).size() > 0){
			List<Integer> idList = (List<Integer>)params.get("idList");
			//删除申请
			deptransfapplymapper.batchDeleteTransferApply(idList);

			//删除审批记录
			deptransfrecodmapper.batchDeleteTransfRecode(idList);

			return RestUtils.returnSuccess("删除成功！");
		}else{
			return RestUtils.returnSuccess("请选择调动记录！");
		}
	}

	/**
	 * 修改工作调动申请
	 * @param params
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse updataTranf(Map<String,Object> params, String token) throws Exception{
		logger.info("进入updataTranf方法；params={}",params);

		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
		Integer loginEmployeeId = erpUser.getUserId();   //员工id
		//接收前端传递的参数

		Integer id = (Integer) params.get("id");                //申请id
		Integer oldFirstDepartment = (Integer) params.get("oldFirstDepartment");    //原项目组id
		Integer newFirstDepartment = (Integer) params.get("newFirstDepartment");    //新项目组id
		Integer	newSecDepartment = (Integer) params.get("newSecDepartment");   //处理人（当前项目经理id）
		String  startTime  = (String) params.get("time");
		Integer status = (Integer) params.get("status"); //状态0暂存，1提交
		//Integer newPositionId =  params.get("newPositionId") == null  ? null : (Integer) params.get("newPositionId"); //新职位ID

		//Boolean isChangePosition = (Boolean) params.get("isChangePosition"); //是否改变职位

		String reason = (String) params.get("reason"); //调整原因


		ErrorEmailLog errorEmailLog = saveTransf(null, newFirstDepartment, newSecDepartment, startTime, status, reason, loginEmployeeId, id);
		if (errorEmailLog != null) {
			//发送邮件
			try {
				boolean sendSuccess = restTemplateUtils.sendEmail(errorEmailLog.getSender(), errorEmailLog.getBcc(), errorEmailLog.getSubject(), 
						errorEmailLog.getEmailMessage(), errorEmailLog.getRecipient(),DicConstants.DEP_TRANSF_APPLY_UPDATE_EMAIL_TYPE,null);
				if(sendSuccess){
					return RestUtils.returnSuccess("提交成功！");
				}else{
					return RestUtils.returnSuccess("提交成功,但邮件发送失败！");
				}
			}catch (Exception e){
				logger.error("updataTranf 发送邮件数据错误"+e, e);
			}
		}
		return RestUtils.returnSuccess("修改成功！");
	}

	/**
	 * 批量提交部门调动申请
	 * @param params
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse batchSubmitTransfApply(Map<String, Object> params, String token) throws Exception{
		logger.info("进入batchSubmitTransfApply方法；params={}",params);
		if(params != null && params.get("idList") != null && ((List<Integer>)params.get("idList")).size() > 0){
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			Integer loginEmployeeId = erpUser.getUserId();   //员工id
			List<Integer> idList = (List<Integer>)params.get("idList");
			List<ErrorEmailLog> errorEmailLogList = new ArrayList<>();
			for(Integer id : idList){
				DepartmentTransfApply departmentTransfApply = deptransfapplymapper.findtransfInfoById(id);

				//查找原部门经理
				ErpDepartment departmentInfo = departmentMapper.findByDepartmentId(departmentTransfApply.getOldFirstDepartment());
				//查找新部门经理
				ErpDepartment newDepartmentInfo = departmentMapper.findByDepartmentId(departmentTransfApply.getNewFirstDepartment());
				departmentTransfApply.setStatus(DicConstants.TSAPPLY_LOADDING);
				if(loginEmployeeId.equals(departmentInfo.getUserId())){
					departmentTransfApply.setProcessor(newDepartmentInfo.getUserId());
				}else{
					departmentTransfApply.setProcessor(departmentInfo.getUserId());
				}
				departmentTransfApply.setModifiedTime(ExDateUtils.getCurrentDateTime());
				departmentTransfApply.setApplyTime(ExDateUtils.getCurrentDateTime());
				departmentTransfApply.setId(id);
				deptransfapplymapper.updataTransfStatus(departmentTransfApply);


				//新增审批记录
				DepartmentTransfRecod transfrcode = new DepartmentTransfRecod();
				transfrcode.setProcessor(loginEmployeeId);
				transfrcode.setContent("提交申请");
				transfrcode.setTransferApplyID(departmentTransfApply.getId());//申请id
				transfrcode.setRcTime(ExDateUtils.getCurrentStringDateTime());
				deptransfrecodmapper.insertTransfRecod(transfrcode);

				//组装邮件数据
				try{
					ErrorEmailLog errorEmailLog = new ErrorEmailLog();
					Map<String,Object> map=new HashMap<>();
					map.put("type", 1);
					List<Map<String,Object>> tempList=this.emailConfigMapper.selectByParam(map);  //所有邮件配置
					//获取发件人
					List<Map<String, Object>> emailSender = adminDicMapper.findAllJobCategory(DicConstants.EMAIL_USERNAME);
					//发件人
					errorEmailLog.setSender(String.valueOf(emailSender.get(0).get("jobName")));
					//收件人
					//通过员工ID在redis查询用户的邮箱
					Map<String,Object> userInfo = (Map<String,Object>)redisTemplate.opsForValue().get(DicConstants.REDIS_PREFIX_USER + departmentTransfApply.getProcessor());
					errorEmailLog.setRecipient(String.valueOf(userInfo.get("username")));
					//抄送人
					/*List<Map<String, Object>> emailCcList = adminDicMapper.findAllJobCategory(DicConstants.DEPARTMENT_TRANSF_APPLY_CC);
					StringBuilder emailCcStringBuilder = new StringBuilder();
					for(Map<String, Object> emailCc : emailCcList){
						emailCcStringBuilder.append(emailCc.get("jobName")).append(",");
					}
					if(emailCcStringBuilder.length() > 0){
						emailCcStringBuilder = emailCcStringBuilder.deleteCharAt(emailCcStringBuilder.length()-1);
					}*/
					errorEmailLog.setBcc(String.valueOf(tempList.get(0).get("bcc")));
					errorEmailLog.setSubject("员工部门调动通知");
					Map<String, Object> erpEmployee  = (Map<String, Object>)redisTemplate.opsForValue().get(DicConstants.REDIS_PREFIX_EMPLOYEE+departmentTransfApply.getEmployeeId());
					String text = erpEmployee.get("employeeName")+"的部门调动申请已提交，请您尽快审批。";

					errorEmailLog.setEmailMessage(text);
					errorEmailLogList.add(errorEmailLog);
				}catch (Exception e){
					logger.error("batchSubmitTransfApply 组装邮件数据错误"+e, e);
					return RestUtils.returnSuccess("提交成功,但部分邮件发送失败！");
				}
			}
			//发送邮件
			try {
				boolean sendSuccess = true;
				for(ErrorEmailLog errorEmailLog : errorEmailLogList){
					sendSuccess = restTemplateUtils.sendEmail(errorEmailLog.getSender(), errorEmailLog.getBcc(), errorEmailLog.getSubject(), errorEmailLog.getEmailMessage(), 
							errorEmailLog.getRecipient(),DicConstants.DEPARTMENT_TRANSF_APPLY_EMAIL_TYPE,null);
				}
				if(sendSuccess){
					return RestUtils.returnSuccess("提交成功！");
				}else{
					return RestUtils.returnSuccess("提交成功,但部分邮件发送失败！");
				}
			}catch (Exception e){
				logger.error("batchSubmitTransfApply 发送邮件数据错误"+e, e);
				return RestUtils.returnSuccess("提交成功,但部分邮件发送失败！");
			}

		}else{
			return RestUtils.returnSuccess("请选择调动记录！");
		}

	}

	/**
	 * 查询登录人审批过的及其审批状态
	 * @param token
	 * @param params
	 * @return
	 */
	public RestResponse findAllTransf(Map<String,Object> params){
		logger.info("开始执行findAllTransf查询所有的申请及其审批状态；参数：params",params);
		
		try{
			List<Map<String,Object>> results = null;
			
			String token=request.getHeader("token");
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			List<Integer> roles = erpUser.getRoles();	//员工角色
			Integer employeeId = erpUser.getUserId();   //员工id
			if (roles.contains(8) || roles.contains(1)){
				//总裁、hr可以看到所有
			}
			else if (roles.contains(9)){
				//副总裁
				params.put("superLeader", employeeId);
			}
			else if (roles.contains(2)) {
				// 一级部门经理角色
				params.put("leader", employeeId);
			}
			else if (roles.contains(5)){
				// 二级部门经理角色
				params.put("secLeader", employeeId);
			}
			else{
				return RestUtils.returnSuccess(results); 
			}
			
			results = deptransfapplymapper.findAllTransfApply(params);
			
			return RestUtils.returnSuccess(results);
		}catch (Exception e) {
			logger.error("findAllTransf方法出现异常:",e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，查询失败！");
		}
	}
		
	/**
	 * 定时任务：执行修改审批通过的部门调动申请
	 */
	@Transactional
	public void antuoUpdateEmpEmpDepInfochedule(){
		logger.info("antuoUpdateEmpEmpDepInfochedule定时器任务开始执行；无参数");
		try{
			//查询出当天时间=生效时间以及审批通过的所有数据
			List<Map<String, Object>> list = deptransfapplymapper.selectMsgForschedule();
			if(list != null && !list.isEmpty()) {
				for (Map<String, Object> map : list) {
					changeEmployeeDepInfo(map);
					//更新调整流程状态
					DepartmentTransfApply transfApply = new DepartmentTransfApply();
					transfApply.setId((Integer) map.get("id"));
					transfApply.setStatus(DicConstants.TSAPPLY_END);//调整结束
				
					deptransfapplymapper.updataTransfStatus(transfApply);
				}
			}
			
		}catch (Exception e) {
			logger.error("antuoUpdateEmpEmpDepInfochedule方法发生异常",e.getMessage(),e);
		}
	}
	
	/**
	 * Description: 根据部门权限查询所负责的员工（供项目工程调用）
	 *
	 * @return
	 * @Author zhangqian
	 * @Create Date: 2019年6月14日 下午21:52:41
	 */
	public RestResponse findAllEmployeeByDepartmentUser(Map<String,Object> params, String token) {
		logger.info("findAllFirstDepartmentByPowerParams方法开始执行，传递参数：params="+params);
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			
			// 调用权限工程获取员工的公司邮箱，和手机号
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);// 封装token
			HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(null,
					requestHeaders);

			String url = protocolType + "nantian-erp-authentication/nantian-erp/erp/findAllErpUserInfo";
			ResponseEntity<RestResponse> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, request,
					RestResponse.class);
			RestResponse response = responseEntity.getBody();
			List<Map<String, Object>> list = new ArrayList<>(); // 调用权限工程获取userList
			if (response.getStatus().equals("200")) {
				list = (List<Map<String, Object>>) response.getData();
			}
			
			//根据负责的所有的一级部门
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("rank", "1");
			if ("queryAll".equals(params.get("filter"))){
			}
			else if ("bySleader".equals(params.get("filter"))){
				queryMap.put("superLeader", erpUser.getUserId());
			}
			else if ("byDepartment".equals(params.get("filter"))){
				queryMap.put("userId", erpUser.getUserId());
				
			}
			else{
				return RestUtils.returnFailure("无可查看的部门");
			}
			List<ErpDepartment>  firstDepartmentList = departmentMapper.findDepartmentByUserID(queryMap);
			for(ErpDepartment firstDepartment:firstDepartmentList){
				Map<String, Object> firstMap = new HashMap<>();
				firstMap.put("firstDepartmentName", firstDepartment.getDepartmentName());
				firstMap.put("firstDepartmentId", firstDepartment.getDepartmentId());
				
				//查询一级部门下的所有二级部门
				List<Map<String, Object>> secondList = new ArrayList<>();
				List<Map<String,Object>> secondDepartmentList = departmentMapper.findAllSecondDepartmentByFirDepId(firstDepartment.getDepartmentId());
				
				for(Map<String,Object> secondDepartment:secondDepartmentList){
					Map<String, Object> secondMap = new HashMap<>();
					secondMap.put("secondDepartmentName", secondDepartment.get("departmentName"));
					secondMap.put("secondDepartmentId", secondDepartment.get("departmentId"));
					
					//查询二级部门下的所有员工
					Map<String, Object> params2 = new HashMap<>();			
					params2.put("secondDepartmentId", secondDepartment.get("departmentId"));
					if(params.get("entryTime") != null) {
						params2.put("entryTime", params.get("entryTime"));
					}
					List<Map<String,Object>> employeeList = employeeMapper.findEmployeeTable(params2);
					
					// 根据erpuser表中员工编号比对员工信息
					for (Map<String, Object> empMap : employeeList) {// 最外层是employee list
						for (Map<String, Object> mapUser : list) {// 内层user list
							if (mapUser.containsKey("userId")) {
								Integer userId = Integer.valueOf(String.valueOf(mapUser.get("userId")));
								if (userId.equals(Integer.valueOf(String.valueOf(empMap.get("employeeId"))))) {
									empMap.put("userPhone", mapUser.get("userPhone"));
									break; // 跳出循环
								}
							}
						}
					}				
					secondMap.put("employeeList", employeeList);
					secondList.add(secondMap);
				}
				firstMap.put("secondDepartmentList", secondList);
				resultList.add(firstMap);
			}
			
			//根据二级部门查询
			queryMap.put("rank", "2");
			List<ErpDepartment> secondDepartmentList = departmentMapper.findDepartmentByUserID(queryMap);
			
			for(ErpDepartment secondDepartment:secondDepartmentList){
				//ErpDepartment upperDepartment = departmentMapper.findByDepartmentId(secondDepartment.getUpperDepartment());
				ErpDepartment upperDepartment = departmentMapper.findByDepartmentIdAndValid(secondDepartment.getUpperDepartment());

				if (upperDepartment == null || upperDepartment.getUserId().equals(erpUser.getUserId())){
					//父部门失效，二级部门经理与一级经理是同一人,不再处理
					continue;
				}
				
				Map<String, Object> firstMap = null;
				List<Map<String,Object>> secondList = null;
				for (Map<String, Object> first:resultList){
					if (first.get("firstDepartmentId").equals(upperDepartment.getDepartmentId())){
						firstMap = first;
						//如果直接用=赋值后边操作secondList会影响first.get("secondDepartmentList")里的数据，改为 = new ArrayList<>();
						secondList = new ArrayList<>((List<Map<String, Object>>) first.get("secondDepartmentList"));
					}
				}
				
				if (firstMap == null)
				{
					firstMap = new HashMap<>();
					firstMap.put("firstDepartmentName", upperDepartment.getDepartmentName());
					firstMap.put("firstDepartmentId", upperDepartment.getDepartmentId());
					secondList = new ArrayList<>();
					firstMap.put("secondDepartmentList", secondList);
					resultList.add(firstMap);
				}				
				
				Map<String, Object> secondMap = new HashMap<>();
				secondMap.put("secondDepartmentName", secondDepartment.getDepartmentName());
				secondMap.put("secondDepartmentId", secondDepartment.getDepartmentId());			
				
				//查询二级部门下的所有员工
				Map<String, Object> params2 = new HashMap<>();			
				params2.put("secondDepartmentId", secondDepartment.getDepartmentId());
				if(params.get("entryTime") != null) {
					params2.put("entryTime", params.get("entryTime"));
				}
				List<Map<String,Object>> employeeList = employeeMapper.findEmployeeTable(params2);
				secondMap.put("employeeList", employeeList);
				
				secondList.add(secondMap);		
			}
			
			return RestUtils.returnSuccess(resultList);
		} catch (Exception e) {
			logger.info("findAllFirstDepartmentByPowerParams方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
	}
	
	/**
	 * Description: 查询部门当月在职的所有员工
	 *
	 * @return
	 * @Author zhangqian
	 * @Create Date: 2019年7月2日 下午21:52:41
	 */
	public RestResponse findAllEmployeeByDepartmentAndMonth(Map<String,Object> params, String token) {
		logger.info("findAllEmployeeByDepartmentAndMonth方法开始执行，传递参数：params=");
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			Integer firstDepartment = (Integer) params.get("firstDepartmentId");
			String month = (String) params.get("month");
			
			List<Map<String,Object>>  secondDepartmentList = departmentMapper.findAllSecondDepartmentByFirDepId(firstDepartment);
			for(Map<String,Object> secondDepartment:secondDepartmentList){
				Map<String, Object> secondMap = new HashMap<>();
				secondMap.put("secondDepartmentName", secondDepartment.get("departmentName"));
				secondMap.put("secondDepartmentId", secondDepartment.get("departmentId"));
				
				//查询二级部门下的所有员工
				Map<String, Object> params2 = new HashMap<>();			
				params2.put("secondDepartmentId", secondDepartment.get("departmentId"));
				params2.put("dimissionTimeStart", month+"-01");
				params2.put("dimissionTimeEnd", month+"-31");
				//此月份用于过滤月度绩效，查询小于等于当月时间的数据
				params2.put("entryTime", month+"-31");
				List<Map<String,Object>> employeeList = employeeMapper.findEmployeeTable(params2);				
				secondMap.put("employeeList", employeeList);
				resultList.add(secondMap);
			}
			
			return RestUtils.returnSuccess(resultList);
		} catch (Exception e) {
			logger.info("findAllEmployeeByDepartmentAndMonth方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
	}

	/**
	 * 根据部门权限查询部门当月在职的所有员工
	 * @param params
	 * @param token
	 * @return
	 */
    public RestResponse findAllEmployeeByDepartmentIdAndTime(Map<String, Object> params , String token) {
    	//员工id、一级部门名称、二级部门名称、姓名、性别
		logger.info("findAllEmployeeByDepartmentIdAndTime方法开始执行，传递参数：params="+params);
		try {
			String departmentId = String.valueOf(params.get("departmentId"));
			String entryTime = String.valueOf(params.get("entryTime"));
			String dimissionTime = String.valueOf(params.get("dimissionTime"));
	  		//根据部门与入职离职时间查询人员列表
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("departmentId", departmentId);
			queryMap.put("entryTime", entryTime);
			queryMap.put("dimissionTime", dimissionTime);
			List<Map<String, Object>> employeeList = employeeMapper.findEmployeeByDepartmentIdAndMonth(queryMap);
			return RestUtils.returnSuccess(employeeList);
		} catch (Exception e) {
			logger.info("findAllEmployeeByDepartmentIdAndTime方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
    }

	/**
	 * @Description:根据当前登录人的角色，获取当前登录的所有一级部门信息和二级部门信息
	 * @param  token
	 * @author songxiugong
	 * @create 2020-02-20
	 * @return
	 */
    public RestResponse findFirstAndSeconDeptInfo(Boolean isAllData, String token) {
    	//员工id、一级部门名称、二级部门名称、姓名、性别
		logger.info("findFirstAndSeconDeptInfo Begin");
		try {
			//0.获取当前登录的角色
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();			// 从用户信息中获取角色信息
			List<Integer> roles = erpUser.getRoles();	// 角色列表
			Map<String, Object> employeeMap = employeeMapper.findEmployeeDetail(id);

			Map<String,Object> queryMap = new HashMap<String, Object>();	//查询的参数
			
			if(isAllData != null && isAllData){
				//如果是考勤管理员查询所有月度考勤则查询所有数据
				roles.add(1);
			}
			//获取总裁、经管、HR及子角色ID、副总裁及子角色ID、一级部门经理角色及子角色ID、二级部门经理角色及子角色ID
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);// 封装token
			HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(null,
					requestHeaders);

			String url=protocolType+"nantian-erp-authentication/nantian-erp/authentication/role/findRoleIdsAndChildRoleIds";
			ResponseEntity<RestResponse> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, request,
					RestResponse.class);
			RestResponse response = responseEntity.getBody();
			Map<String, Object> roleMap = null; // 调用权限工程获取userList
			if (response.getStatus().equals("200")) {
				roleMap = (Map<String, Object>) response.getData();
			}else{
				logger.error("调用权限工程获取角色列表信息失败!!!");
				return RestUtils.returnFailure("调用权限工程获取角色列表信息失败");
			}
			if(roleMap != null){
				//总裁、经管、HR及子角色ID
				List<Integer> ceoManageHrAndChildRoleIdList = (List<Integer>) roleMap.get("ceoManageHrAndChildRoleIdList");
				//副总裁及子角色ID
				List<Integer> deputyAndChildRoleIdList = (List<Integer>) roleMap.get("deputyAndChildRoleIdList");
				//一级部门经理角色及子角色ID
				List<Integer> firstDepartmentManageAndChildRoleIdList = (List<Integer>) roleMap.get("firstDepartmentManageAndChildRoleIdList");
				//二级部门经理角色及子角色ID
				List<Integer> secondDepartmentManageAndChildRoleIdList = (List<Integer>) roleMap.get("secondDepartmentManageAndChildRoleIdList");

				List<Integer> ceoManageHrRoleIds = roles.stream().filter(item -> ceoManageHrAndChildRoleIdList.contains(item)).collect(Collectors.toList());;
				List<Integer> deputyRoleIds = roles.stream().filter(item -> deputyAndChildRoleIdList.contains(item)).collect(Collectors.toList());;
				List<Integer> firstDepartmentManageRoleIds = roles.stream().filter(item -> firstDepartmentManageAndChildRoleIdList.contains(item)).collect(Collectors.toList());;
				List<Integer> secondDepartmentManageRoleIds = roles.stream().filter(item -> secondDepartmentManageAndChildRoleIdList.contains(item)).collect(Collectors.toList());;
				if(ceoManageHrRoleIds != null && ceoManageHrRoleIds.size() > 0){
					// 总裁、经管、HR
				}else if(deputyRoleIds != null && deputyRoleIds.size() > 0){
					//副总裁
					queryMap.put("superLeaderId", id);
				}else if(firstDepartmentManageRoleIds != null && firstDepartmentManageRoleIds.size() > 0){
					if(firstDepartmentManageRoleIds.contains(2)){
						//一级部门经理角色
						queryMap.put("leaderId", id);
					}else{
						//一级部门经理子角色
						queryMap.put("firstDeptId", employeeMap.get("firstDepartmentId"));
					}

				}else if(secondDepartmentManageRoleIds != null && secondDepartmentManageRoleIds.size() > 0){
					if(secondDepartmentManageRoleIds.contains(5)){
						//二级部门经理
						queryMap.put("sdLeaderID", id);	//预留
					}else{
						//二级部门经理子角色
						queryMap.put("secondDeptId", employeeMap.get("secondDepartmentId"));
					}
				}else {
					queryMap.put("other", id);	//其它角色
					return RestUtils.returnSuccess("没有获取数据的权限，请联系管理员！", "NotAuth");
				}
			}else {
				return RestUtils.returnSuccess("没有获取数据的权限，请联系管理员！", "NotAuth");
			}
//			//0-1.角色判断
//			if (roles.contains(8) || roles.contains(7) || roles.contains(1)) {// 总裁、经管、HR
//
//			} else if (roles.contains(9)) { //副总裁
//				queryMap.put("superLeaderId", id);
//			} else if (roles.contains(2)) {	// 一级部门经理角色
//				queryMap.put("leaderId", id);
//			} else if (roles.contains(5)) {	//二级部门经理
//				queryMap.put("sdLeaderID", id);	//预留
//			} else {
////				queryMap.put("other", id);	//其它角色
//				return RestUtils.returnSuccess("没有获取数据的权限，请联系管理员！", "NotAuth");
//			}

			//1.组合一级和二级部门
			//1-1.集团：
			Map<String,Object> company = new HashMap<String, Object>();
			List<Map<String,Object>> allFirstDepList = new ArrayList<Map<String,Object>>();
			company.put("departmentName", "集成服务集团");
			company.put("depts", allFirstDepList);
					
			//1-2.如果当前登录人是二级部门经理，则查找其所有的一级部门信息
			if(queryMap.get("sdLeaderID") != null || queryMap.get("secondDeptId") != null) {
				List<Map<String,Object>> secondDeptList = departmentMapper.findLoginUserSecondDeptByParams(queryMap);
				if(secondDeptList==null) {
					return RestUtils.returnSuccess(company,"OK");
				}

				List<Map<String,Object>> firstDeptSUniq = new ArrayList<Map<String,Object>>();
				
				//由于逆向查找没有好的解决方案，使用笨方法，后续优化

				Map<Integer,Object> firstDeptUniq = new HashMap<Integer, Object>();	//key 一级部门ID，Value一级部门名称
				for(Map<String, Object> secondDept : secondDeptList) {
					
				//1-2-1.先分类一级部门
					if(secondDept.get("fdId") != null) {
						Integer fdId = Integer.valueOf(String.valueOf(secondDept.get("fdId")));
						firstDeptUniq.put(fdId,secondDept);
					}
				}
				
				//1-2-2.再根据唯一化处理的一级部门ID和用户的登录ID作为关联查询数据库，获取数据
				
				if(firstDeptUniq == null || firstDeptUniq.isEmpty()) {
					
				}else {
					for(Map.Entry<Integer, Object> entry : firstDeptUniq.entrySet()) {
						Map<String, Object> firstDepartmentTemp = new HashMap<String, Object>(); // 最终保存使用
						
						queryMap.put("firstDeptId",Integer.valueOf(String.valueOf(entry.getKey())));
						List<Map<String,Object>> secondDeptS= new ArrayList<Map<String,Object>>();
						secondDeptS = departmentMapper.findLoginUserSecondDeptByParams(queryMap);
						firstDepartmentTemp.put("depts",secondDeptS);
						
						Map<Integer,Object> secondTemp = (Map<Integer,Object>)entry.getValue();
						firstDepartmentTemp.put("departmentName", secondTemp.get("fdName"));
						firstDepartmentTemp.put("departmentId",String.valueOf(secondTemp.get("fdId")));
						firstDepartmentTemp.put("leaderId",String.valueOf(secondTemp.get("fdUserId")));
						firstDepartmentTemp.put("superLeaderId",String.valueOf(secondTemp.get("fdSupperLeaderID")));
						firstDepartmentTemp.put("inValid",String.valueOf(secondTemp.get("fdInValid")));
						firstDepartmentTemp.put("rank",String.valueOf(secondTemp.get("fdRank")));
						
						allFirstDepList.add(firstDepartmentTemp);
					}
				}
			}else {
				
				//1-3.如果是一级部门经理及以上领导获取 一级二部门
				List<Map<String, Object>> firstDepartmentList = departmentMapper.findLoginUserDepartmentByParams(queryMap);
				
				for (Map<String, Object> firstDepartment : firstDepartmentList) {
					Map<String, Object> myfirstDepartment = new HashMap<String, Object>();
					myfirstDepartment.put("departmentName", firstDepartment.get("departmentName"));
					
					Integer firstDepartmentId = Integer.valueOf(String.valueOf(firstDepartment.get("departmentId")));
					myfirstDepartment.put("departmentId",String.valueOf(firstDepartmentId));
					myfirstDepartment.put("leaderId",String.valueOf(firstDepartment.get("userId")));
					myfirstDepartment.put("superLeaderId",String.valueOf(firstDepartment.get("superLeader")));
					myfirstDepartment.put("inValid",String.valueOf(firstDepartment.get("inValid")));
					myfirstDepartment.put("rank",String.valueOf(firstDepartment.get("rank")));
					
					// 根据一级部门Id查询所有的二级部门
					List<Map<String, Object>> secondDepartmentList = departmentMapper.findAllSecondDepartmentByFirDepId(firstDepartmentId);
					
					if(secondDepartmentList != null && secondDepartmentList.size() != 0) {
						myfirstDepartment.put("depts",secondDepartmentList);
						allFirstDepList.add(myfirstDepartment);
					}
				}
			}
									
			logger.info("findFirstAndSeconDeptInfo End");
			return RestUtils.returnSuccess(company,"OK");
		} catch (Exception e) {
			logger.info("findFirstAndSeconDeptInfo方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
    }

	/**
	 * 查询部门的兄弟部门列表
	 * @param params  departmentId 部门id
	 * @param token
	 * @return
	 */
    public RestResponse findBrotherDepartmentList(Map<String, Object> params, String token) {
		logger.info("findBrotherDepartmentList Begin");
		try {
			Map<String, Object> returnMap = new HashMap<>();
			Integer departmentId = Integer.valueOf(String.valueOf(params.get("departmentId")));
			ErpDepartment erpDepartment = departmentMapper.findByDepartmentId(departmentId);
			returnMap.put("rank", erpDepartment.getRank());
			List<Map<String,Object>> brotherDepartmentList = null;
			List<Map<String,Object>> sonDepartmentList = null;

			if("2".equals(erpDepartment.getRank())){
				brotherDepartmentList= departmentMapper.findBrotherDepartmentList(departmentId);
				List<Integer> brotherDepartmentIdList = new ArrayList<>();
				for (Map<String,Object> brotherDepartment : brotherDepartmentList){
					returnMap.put("parentDepartmentId", brotherDepartment.get("upperDepartment"));
					brotherDepartmentIdList.add(Integer.valueOf(String.valueOf(brotherDepartment.get("departmentId"))));
				}
				returnMap.put("brotherDepartmentIdList", brotherDepartmentIdList);
			}
			if("1".equals(erpDepartment.getRank())){
				sonDepartmentList= departmentMapper.findSecondDepartmentByFirstDepartmentId(departmentId);
				List<Integer> sonDepartmentIdList = new ArrayList<>();
				for (Map<String,Object> sonDepartment : sonDepartmentList){
					sonDepartmentIdList.add(Integer.valueOf(String.valueOf(sonDepartment.get("departmentId"))));
				}
				returnMap.put("sonDepartmentIdList", sonDepartmentIdList);
			}
			return RestUtils.returnSuccess(returnMap);
		} catch (Exception e) {
			logger.info("findBrotherDepartmentList方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
	}
    
    /**
	 * Description: 条件查询所有包含二级部门的一级部门列表(供薪酬工程调用)
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月06日 下午21:52:41
	 */
	public RestResponse findContainSecDepAllFirstDepartment(Map<String,Object> params) {
		logger.info("findContainSecDepAllFirstDepartment方法开始执行，传递参数：params="+params);
		List<Map<String,Object>> firstDepartmentList = new ArrayList<>();// 所有一级部门下的员工信息

		try {
			//根据指定的条件查询所有的一级部门
			List<Map<String,Object>> allFirstDepartmentList = departmentMapper.findAllFirstDepartmentByParams(params);
			for (Map<String, Object> firstDepartment:allFirstDepartmentList) {
				// 根据一级部门Id查询所有的二级部门
				Integer firstDepartmentId = Integer.valueOf(String.valueOf(firstDepartment.get("departmentId")));
				List<Map<String, Object>> secondDepartmentList = departmentMapper.findAllSecondDepartmentByFirDepId(firstDepartmentId);
				if (!secondDepartmentList.isEmpty()) {
					firstDepartmentList.add(firstDepartment);
				}
			}
			
			return RestUtils.returnSuccess(firstDepartmentList);
		} catch (Exception e) {
			logger.info("findContainSecDepAllFirstDepartment方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
	}

	/**
	 * 查询各个一级部门员工数量
	 * @return
	 */
	public RestResponse findFirstDepartmentCountMap() {
		logger.info("findFirstDepartmentCountMap方法开始执行");
		Map<String,Object> firstDepartmentMap = new HashMap<>();
		try {
			List<Map<String,Object>> firstDepartmentList = employeeMapper.findFirstDepartmentCountMap();
			for (Map<String,Object> map : firstDepartmentList){
				firstDepartmentMap.put(String.valueOf(map.get("firstDepartment")), Integer.valueOf(String.valueOf(map.get("employeeCount"))));
			}
			return RestUtils.returnSuccess(firstDepartmentMap);
		} catch (Exception e) {
			logger.info("findFirstDepartmentCountMap方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
	}

	/**
	 * 根据登录人查看所管理的一级部门及上级领导列表
	 * @param token
	 * @return
	 */
	public RestResponse findFirstDepartmentAndSuperLeaderByUserId(String token) {
		logger.info("findFirstDepartmentAndSuperLeaderByUserId方法开始执行");
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer employeeId = erpUser.getUserId();			// 从用户信息中获取角色信息
			List<Integer> roles = erpUser.getRoles();	// 角色列表
			Map<String,Object> params = new HashMap<>();
			if(roles.contains(8)){
				//总裁
			}else if(roles.contains(9)){
				//副总裁
				params.put("superLeader", employeeId);
			}else if (roles.contains(2)){
				//一级部门经理
				params.put("leaderId", erpUser.getUserId());
			}else{
				return RestUtils.returnSuccessWithString(null);
			}
			List<Map<String,Object>> firstDepartmentList = departmentMapper.findFirstDepartmentAndSuperLeaderByUserId(params);

			return RestUtils.returnSuccess(firstDepartmentList);
		} catch (Exception e) {
			logger.info("findFirstDepartmentAndSuperLeaderByUserId方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
	}

	/**
	 * 根据登录人查询本人申请的部门调动列表
	 * @param token
	 * @return
	 */
	public RestResponse queryDepartmentTransfApplyList(Integer oldFirstDepartment, Integer newFirstDepartment,
													   Integer oldSecDepartment, Integer newSecDepartment,
													   String employeeName, String startTime,
													   String endTime, String token) {
		logger.info("开始执行queryDepartmentTransfApplyList根据登录人查询本人申请的部门调动列表；参数：token={}",token);

		try{
			List<Map<String,Object>> results = null;

			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			List<Integer> roles = erpUser.getRoles();	//员工角色
			Integer employeeId = erpUser.getUserId();   //员工id
			Map<String,Object> params = new HashMap<>();
			params.put("applyPersonId", employeeId);

			params.put("oldFirstDepartment", oldFirstDepartment);
			params.put("newFirstDepartment", newFirstDepartment);
			params.put("oldSecDepartment", oldSecDepartment);
			params.put("newSecDepartment", newSecDepartment);
			params.put("employeeName", employeeName == null ? null : "%"+employeeName+"%");
			params.put("startTime", startTime);
			params.put("endTime", endTime);

			results = deptransfapplymapper.queryDepartmentTransfApplyList(params);

			return RestUtils.returnSuccess(results);
		}catch (Exception e) {
			logger.error("queryDepartmentTransfApplyList方法出现异常:",e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，查询失败！");
		}
	}

	/**
	 * 批量保存调动申请
	 * @param params
	 * @param token
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse batchSaveTransf(Map<String, Object> params, String token) throws Exception{
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
		Integer loginEmployeeId = erpUser.getUserId();   //员工id

		List<Integer> employeeIdList = (List<Integer>)params.get("employeeIdList");//员工Id列表
		Integer newFirstDepartment = (Integer) params.get("newFirstDepartment"); //新一级部门
		Integer newSecDepartment = (Integer) params.get("newSecDepartment"); //新二级部门

		//Boolean isChangePosition = (Boolean) params.get("isChangePosition"); //是否改变职位

		//Integer newPositionId =  params.get("newPositionId") == null  ? null : (Integer) params.get("newPositionId"); //新职位ID

		String startTime = (String) params.get("time"); //生效时间
		String reason = (String) params.get("reason"); //调整原因

		Integer status = (Integer) params.get("status"); //状态0暂存，1提交

		//Integer id =  params.get("id") == null  ? null : (Integer) params.get("id"); //部门调整申请ID


		List<ErrorEmailLog>  errorEmailLogList = new ArrayList<>();
		for(Integer employeeId : employeeIdList){
			//根据申请状态做部门调动申请的重复判断
			List<Map<String, Object>> result = deptransfapplymapper.findtransfRecodeByStartTime(employeeId);
			if(result != null && !result.isEmpty()){
				Map<String,Object> returnMessage = new HashMap<String, Object>();
				returnMessage.put("Message", "已有调动申请，请勿重复操作！");
				return RestUtils.returnSuccess(returnMessage, "Alert");
			}
			ErrorEmailLog errorEmailLog = saveTransf(employeeId, newFirstDepartment, newSecDepartment, startTime, status, reason, loginEmployeeId, null);
			if(errorEmailLog != null ){
				errorEmailLogList.add(errorEmailLog);
			}
		}
		//发送邮件
		try {
			boolean sendSuccess = true;
			for(ErrorEmailLog errorEmailLog : errorEmailLogList){
				sendSuccess = restTemplateUtils.sendEmail(errorEmailLog.getSender(), errorEmailLog.getBcc(), errorEmailLog.getSubject(), 
						errorEmailLog.getEmailMessage(), errorEmailLog.getRecipient(),DicConstants.DEPARTMENT_TRANSF_APPLY_EMAIL_TYPE,null);
			}
			if(sendSuccess){
				return RestUtils.returnSuccess("提交成功！");
			}else{
				return RestUtils.returnSuccess("提交成功,但部分邮件发送失败！");
			}
		}catch (Exception e){
			logger.error("batchSubmitTransfApply 发送邮件数据错误"+e, e);
		}
		return RestUtils.returnSuccess("保存成功！");

	}
	@Transactional(rollbackFor = Exception.class)
	public ErrorEmailLog saveTransf(Integer employeeId, Integer newFirstDepartment, Integer newSecDepartment,String startTime, Integer status, String reason, Integer loginEmployeeId, Integer id) throws Exception{

		//查询员工的原部门、职位信息
		Integer oldFirstDepartment = null;
		Integer oldSecDepartment = null;
		DepartmentTransfApply departmentTransfApply = null;
		if(id == null){
			Map<String, Object>  employeeDetail = employeeMapper.findEmployeeDetail(employeeId);
			oldFirstDepartment = employeeDetail.get("firstDepartmentId") == null ? null : Integer.valueOf(String.valueOf(employeeDetail.get("firstDepartmentId"))); //旧一级部门
			oldSecDepartment = employeeDetail.get("secondDepartmentId") == null ? null : Integer.valueOf(String.valueOf(employeeDetail.get("secondDepartmentId")));//旧二级部门
			departmentTransfApply = new DepartmentTransfApply();
			departmentTransfApply.setEmployeeId(employeeId);
			departmentTransfApply.setOldFirstDepartment(oldFirstDepartment);
			departmentTransfApply.setOldSecDepartment(oldSecDepartment);
		}else{
			departmentTransfApply = deptransfapplymapper.findtransfInfoById(id);
		}

		//查找原部门经理
		ErpDepartment departmentInfo = departmentMapper.findByDepartmentId(departmentTransfApply.getOldFirstDepartment());
		//查找新部门经理
		ErpDepartment newDepartmentInfo = departmentMapper.findByDepartmentId(newFirstDepartment);

		departmentTransfApply.setNewFirstDepartment(newFirstDepartment);
		departmentTransfApply.setNewSecDepartment(newSecDepartment);
		departmentTransfApply.setStartTime(startTime);
		departmentTransfApply.setStatus(String.valueOf(status));
		if(status == 1){
			if(loginEmployeeId.equals(departmentInfo.getUserId())){
				departmentTransfApply.setProcessor(newDepartmentInfo.getUserId());
			}else{
				departmentTransfApply.setProcessor(departmentInfo.getUserId());
			}
		}else{
			departmentTransfApply.setProcessor(loginEmployeeId);
		}
		departmentTransfApply.setReason(reason);
		departmentTransfApply.setApplyPersonId(loginEmployeeId);
		departmentTransfApply.setModifiedTime(ExDateUtils.getCurrentDateTime());
		departmentTransfApply.setApplyTime(ExDateUtils.getCurrentDateTime());
		if(id == null){
			departmentTransfApply.setCreateTime(ExDateUtils.getCurrentDateTime());
			deptransfapplymapper.addDepTransfApply(departmentTransfApply);
		}else{
			departmentTransfApply.setId(id);
			deptransfapplymapper.updataTransfStatus(departmentTransfApply);
		}
		if (status == 1) {
			//新增审批记录
			DepartmentTransfRecod transfrcode = new DepartmentTransfRecod();
			transfrcode.setProcessor(loginEmployeeId);
			transfrcode.setContent("提交申请");
			transfrcode.setTransferApplyID(departmentTransfApply.getId());//申请id
			transfrcode.setRcTime(ExDateUtils.getCurrentStringDateTime());
			deptransfrecodmapper.insertTransfRecod(transfrcode);

			//构造邮件数据
			//组装邮件数据
			try{
				ErrorEmailLog errorEmailLog = new ErrorEmailLog();
				Map<String,Object> queryMap=new HashMap<>();
				queryMap.put("type", 1);
				List<Map<String,Object>> tempList=this.emailConfigMapper.selectByParam(queryMap);  //所有邮件配置
				//获取发件人
				List<Map<String, Object>> emailSender = adminDicMapper.findAllJobCategory(DicConstants.EMAIL_USERNAME);
				//发件人
				errorEmailLog.setSender(String.valueOf(emailSender.get(0).get("jobName")));
				//收件人
				//通过员工ID在redis查询用户的邮箱
				Map<String,Object> oldDepMangerInfoInfo = (Map<String,Object>)redisTemplate.opsForValue().get(DicConstants.REDIS_PREFIX_USER + departmentInfo.getUserId());
				Map<String,Object> newDepMangerInfo=(Map<String,Object>)redisTemplate.opsForValue().get(DicConstants.REDIS_PREFIX_USER+newDepartmentInfo.getUserId());
				String tomail=String.valueOf(oldDepMangerInfoInfo.get("username"))+","+String.valueOf(newDepMangerInfo.get("username"));
				errorEmailLog.setRecipient(tomail);
				//抄送人
				/*List<Map<String, Object>> emailCcList = adminDicMapper.findAllJobCategory(DicConstants.DEPARTMENT_TRANSF_APPLY_CC);
				StringBuilder emailCcStringBuilder = new StringBuilder();
				for(Map<String, Object> emailCc : emailCcList){
					emailCcStringBuilder.append(emailCc.get("jobName")).append(",");
				}
				if(emailCcStringBuilder.length() > 0){
					emailCcStringBuilder = emailCcStringBuilder.deleteCharAt(emailCcStringBuilder.length()-1);
				}*/
				errorEmailLog.setBcc(String.valueOf(tempList.get(0).get("bcc")));
				errorEmailLog.setSubject("员工部门调动通知");
				Map<String, Object> erpEmployee  = (Map<String, Object>)redisTemplate.opsForValue().get(DicConstants.REDIS_PREFIX_EMPLOYEE+departmentTransfApply.getEmployeeId());
				String text = erpEmployee.get("employeeName")+"的部门调动申请已提交，请您尽快审批。";

				errorEmailLog.setEmailMessage(text);
				return errorEmailLog;
			}catch (Exception e){
				logger.error("saveTransf 组装邮件数据错误"+e, e);
			}


		}
		return null;
	}
	public RestResponse queryDepartmentTransfApplyInfo(Integer id, String token) {
		Map<String, Object> departmentTransfApplyInfo = deptransfapplymapper.queryDepartmentTransfApplyInfo(id);
		return  RestUtils.returnSuccess(departmentTransfApplyInfo);
	}

	public RestResponse findAllFirstDepartmentBySecDep(
			Map<String, Object> params) {
		logger.info("findAllFirstDepartmentBySecDep方法开始执行，传递参数：params="+params);
		try {
			//根据指定的条件查询所有的一级部门
			List<Map<String,Object>> firstDepartmentList = departmentMapper.findAllFirstDepartmentBySecDep(params);
			return RestUtils.returnSuccess(firstDepartmentList);
		} catch (Exception e) {
			logger.info("findAllFirstDepartmentByPowerParams方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常：" + e.getMessage());
		}
	}



	public void modifyCurrentPerson() throws Exception {
		logger.info( "开始执行 modifyCurrentPerson");
		//查询待转正并且当前处理人不正确的员工列表
		List<Map<String, Object>> employeePostiveList = erpEmployeePostiveMapper.findErrorCurrentPersonPostive();

		//更新
		for(Map<String, Object> employeePostive : employeePostiveList){
			erpEmployeePostiveMapper.updateCurrentPersonIdById(Integer.valueOf(String.valueOf(employeePostive.get("id"))), Integer.valueOf(String.valueOf(employeePostive.get("seconduserId"))));
		}

		//查询员工对应的各个部门负责人信息

		Map<String, Object> allEmployeeInfoMap = (Map<String, Object>) employeeService.findEmployeeInfoMap(null, null).getData();


		HttpHeaders requestHeaders=new HttpHeaders();

		//调用项目工程更新请假审批人
		String projectUrl = protocolType+"nantian-erp-project/nantian-erp/project/leave/batchUpdateProcessor";

		HttpEntity<Map<String,Object>> projectRequest = new HttpEntity<>(allEmployeeInfoMap, requestHeaders);

		ResponseEntity<Map> projectResponse = this.restTemplate.postForEntity(projectUrl, projectRequest, Map.class);

		if(projectResponse.getStatusCodeValue() != 200){
			logger.info("方法modifyCurrentPerson中 调用项目工程响应失败！导致请假处理人修改失败！");
		}

		//调用薪酬工程、待处理的上岗工资单、待处理的转正工资单
		String url = protocolType+"nantian-erp-salary/nantian-erp/salary/payRollFlow/batchUpdateCurrentPersonId";

		HttpEntity<Map<String,Object>> request = new HttpEntity<>(allEmployeeInfoMap, requestHeaders);

		ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
		if(response.getStatusCodeValue() != 200){
			logger.info("方法modifyCurrentPerson中 薪酬工程响应失败！导致工资单处理人修改失败！");
		}

	}
	
	/**
	 * 批量/单个部门调动审批通过后发送邮件
	 * @param params
	 * @param token
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor = Exception.class)
	public ErrorEmailLog transfDeptSendEmail(String token,List<Integer> transferApplyIds,String startTime) throws Exception{
		logger.info("transfDeptSendEmail方法开始执行,参数:");
		ErrorEmailLog errorEmailLog = new ErrorEmailLog();
		Map<String,Object> queryMap=new HashMap<>();
		queryMap.put("type", 0);
		List<Map<String,Object>> tempList=this.emailConfigMapper.selectByParam(queryMap);  //所有邮件配置
		//发件人
		List<Map<String,Object>> bccList=adminDicMapper.findAllCategoryFromAdminDic(DicConstants.EMAIL_USERNAME);
		errorEmailLog.setSender(String.valueOf(bccList.get(0).get("categoryName")));
		//收件人
		errorEmailLog.setRecipient(String.valueOf(tempList.get(0).get("recipient")));
		//抄送人
		errorEmailLog.setBcc(String.valueOf(tempList.get(0).get("bcc")));
		errorEmailLog.setSubject("员工部门调动情况");
		/*
		 * 封装附件
		 * */
		//生成excel
		XSSFWorkbook workBook =new XSSFWorkbook();
		XSSFSheet sheet=workBook.createSheet("人员部门调整信息确认表");
		sheet.setDefaultColumnWidth((short) 15);
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0,1,0,0));//姓名
		sheet.addMergedRegion(new CellRangeAddress(0,1,1,1));//OA账号
		sheet.addMergedRegion(new CellRangeAddress(0,1,2,2));//内部异动时间
		sheet.addMergedRegion(new CellRangeAddress(0,1,3,3));//部门调整类型
		sheet.addMergedRegion(new CellRangeAddress(0,1,4,4));//部门调整说明
		sheet.addMergedRegion(new CellRangeAddress(0,0,5,8));//原所属部门
		sheet.addMergedRegion(new CellRangeAddress(0,0,9,16));//新所属部门
		sheet.addMergedRegion(new CellRangeAddress(0,1,17,17));//新的末级部门编号
		//第一行
		XSSFRow row0=sheet.createRow(0);
		row0.setHeightInPoints(18);
		Font font2 = workBook.createFont();
		font2.setColor(IndexedColors.BLACK.getIndex());
		CellStyle style1 = workBook.createCellStyle();
		style1.setFillForegroundColor(IndexedColors.GREEN.getIndex());
		style1.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style1.setAlignment(CellStyle.ALIGN_CENTER);//水平
		style1.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//垂直
		style1.setFont(font2);
		
		row0.createCell(0).setCellValue("姓名");
		row0.getCell(0).setCellStyle(style1);
		row0.createCell(1).setCellValue("OA账号");
		row0.getCell(1).setCellStyle(style1);
		row0.createCell(2).setCellValue("内部异动时间");
		row0.getCell(2).setCellStyle(style1);
		row0.createCell(3).setCellValue("部门调整类型");
		row0.getCell(3).setCellStyle(style1);
		row0.createCell(4).setCellValue("部门调整说明");
		row0.getCell(4).setCellStyle(style1);
		row0.createCell(5).setCellValue("原所属部门");
		row0.getCell(5).setCellStyle(style1);
		row0.createCell(9).setCellValue("新所属部门");
		row0.getCell(9).setCellStyle(style1);
		row0.createCell(17).setCellValue("新的末级部门编号");
		row0.getCell(17).setCellStyle(style1);
		//第二行
		XSSFRow row1=sheet.createRow(1);
		row1.setHeightInPoints(18);
		CellStyle style4 = workBook.createCellStyle();
		style4.setFillForegroundColor(IndexedColors.GOLD.getIndex());
		style4.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style4.setAlignment(CellStyle.ALIGN_CENTER);//水平
		style4.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//垂直
		style4.setWrapText(true);
		style4.setFont(font2);
		CellStyle style5 = workBook.createCellStyle();
		style5.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
		style5.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style5.setAlignment(CellStyle.ALIGN_CENTER);//水平
		style5.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//垂直
		style5.setFont(font2);
		style5.setWrapText(true);

		row1.createCell(5).setCellValue("一级部门");
		row1.getCell(5).setCellStyle(style4);
		row1.createCell(6).setCellValue("二级部门");
		row1.getCell(6).setCellStyle(style4);
		row1.createCell(7).setCellValue("三级部门");
		row1.getCell(7).setCellStyle(style4);
		row1.createCell(8).setCellValue("四级部门");
		row1.getCell(8).setCellStyle(style4);
		row1.createCell(9).setCellValue("一级部门");
		row1.getCell(9).setCellStyle(style5);
		row1.createCell(10).setCellValue("一级部门审批人");
		row1.getCell(10).setCellStyle(style5);
		row1.createCell(11).setCellValue("二级部门");
		row1.getCell(11).setCellStyle(style5);
		row1.createCell(12).setCellValue("二级部门审批人");
		row1.getCell(12).setCellStyle(style5);
		row1.createCell(13).setCellValue("三级部门");
		row1.getCell(13).setCellStyle(style5);
		row1.createCell(14).setCellValue("三级部门审批人");
		row1.getCell(14).setCellStyle(style5);
		row1.createCell(15).setCellValue("四级部门");
		row1.getCell(15).setCellStyle(style5);
		row1.createCell(16).setCellValue("四级部门审批人");
		row1.getCell(16).setCellStyle(style5);
		String textSum="";
		int lineIndex=2;
		for(Integer transferApplyId:transferApplyIds){
			//新建对象
			Map<String, Object> transfapply = deptransfapplymapper.findtransfRecodeById(transferApplyId);
			//部门调动员工的信息
			Integer transfrEmployeeId = (Integer) transfapply.get("employeeId");//员工ID
			Map<String, Object> transfrEmployeeInfo = (Map<String, Object>) redisTemplate.opsForValue()
					.get(DicConstants.REDIS_PREFIX_EMPLOYEE + transfrEmployeeId);//员工信息
			String transfrEmployeeName = (String) transfrEmployeeInfo.get("employeeName");//调动员工姓名
	        /*
	         * 封装邮件内容text
	         */
	 		String idCardNumber =String.valueOf(transfapply.get("idCardNumber"));//员工身份证号码
	 		String resson=String.valueOf(transfapply.get("reason")) ;//调动原因
			//新旧部门名称
			String newFirstDepartmentName = String.valueOf(transfapply.get("newFirstDepartmentName"));
			String newSecDepartmentName = String.valueOf(transfapply.get("newSecDepartmentName"));
			String oldFirstDepartmentName = String.valueOf(transfapply.get("oldFirstDepartmentName"));
			String oldSecDepartmentName = String.valueOf(transfapply.get("oldSecDepartmentName"));

			//审批记录
			RestResponse restResponse=findtransfRecode(transferApplyId,token);
			List<Map<String,Object>> transfRecode=(List<Map<String,Object>>)restResponse.getData();
			StringBuilder content = new StringBuilder("<html><head></head><body>");
			content.append("<table class=\"customTableClassName\" border=\"0\">");
			content.append("<tr align=\"center\"><td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">时间</td>"
					+ "<td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">处理人</td>"
					+ "<td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">处理记录</td></tr>");
			for (Map<String,Object> data : transfRecode) {
				Map<String, Object> empInfo = (Map<String, Object>) redisTemplate.opsForValue()
						.get(DicConstants.REDIS_PREFIX_EMPLOYEE + Integer.valueOf(String.valueOf(data.get("processor"))));//员工信息
				String employeeName = (String) empInfo.get("employeeName");//员工姓名
				content.append("<tr align=\"center\">");
				content.append(" <td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">" + data.get("rcTime") + "</td>"); //第一列
				content.append(" <td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">" + employeeName + "</td>"); //第二列
				content.append(" <td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">" + data.get("content") + "</td>"); //第三列
				content.append("</tr>");
			}
			content.append("</table>");
			content.append("</body></html>");
			//邮件正文
			String text = "员工部门调动审批已完成：<br/>" +
	                 "&nbsp;&nbsp;&nbsp;&nbsp;" + "调转部门人："+transfrEmployeeName +"， (身份证号："+idCardNumber+")"+ " 的部门调转已经完成，部门内部异动时间(生效时间)为：" + startTime+
	                 "；调出部门："+oldFirstDepartmentName+"-" + oldSecDepartmentName + "；调入部门："+newFirstDepartmentName + "-" + newSecDepartmentName +
	                 " ；调转部门审批过程如下："+content;
			textSum+=text+"<br/><br/>";

			//通过员工transfrEmployeeId在redis中查询用户邮箱
			Map<String,Object> userInfo = (Map<String,Object>)redisTemplate.opsForValue().get(DicConstants.REDIS_PREFIX_USER + transfrEmployeeId);
			String transfrEmpEmail=(String)userInfo.get("username");
			int index = transfrEmpEmail.indexOf("@");
	        String OA = transfrEmpEmail.substring(0, index);
	        //通过权限工程查询总裁的员工ID
			List<Map<String,Object>> list = restTemplateUtils.findAllUserByRoleId(token, 8);
			Map<String,Object> map = list.get(0);
			Integer bossUserId = (Integer) map.get("userId");//userId是员工ID
			Map<String, Object> bossInfo = (Map<String, Object>) redisTemplate.opsForValue()
					.get(DicConstants.REDIS_PREFIX_EMPLOYEE + bossUserId);//总裁信息
			String bossInfoName = (String) bossInfo.get("employeeName");//员工姓名
	        //新一级部门经理
	  		Integer newManager=Integer.valueOf(String.valueOf(transfapply.get("newManager")));
	  		Map<String, Object> newManagerInfo = (Map<String, Object>) redisTemplate.opsForValue()
	  				.get(DicConstants.REDIS_PREFIX_EMPLOYEE + newManager);//新一级部门经理信息
	  		String newManagerName = (String) newManagerInfo.get("employeeName");//员工姓名
	  		//新二级部门经理
	  		Integer newsecdeptManager=Integer.valueOf(String.valueOf(transfapply.get("newsecdeptManager")));
	  		Map<String, Object> newsecdeptManagerInfo = (Map<String, Object>) redisTemplate.opsForValue()
	  				.get(DicConstants.REDIS_PREFIX_EMPLOYEE + newsecdeptManager);//新二级部门经理信息
	  		String newsecdeptManagerName = (String) newsecdeptManagerInfo.get("employeeName");//员工姓名
	  		//末级部门编号-新二级部门编号
			Integer newSecDepId=Integer.valueOf(String.valueOf(transfapply.get("departmentId")));
			//下一行-根据具体数据进行填充
			XSSFRow nextRow=sheet.createRow(lineIndex++);
			//数据插入excel
			nextRow.createCell(0).setCellValue(transfrEmployeeName);
			nextRow.createCell(1).setCellValue(OA);
			nextRow.createCell(2).setCellValue(startTime);
			nextRow.createCell(3).setCellValue("部门转移");
			nextRow.createCell(4).setCellValue(resson);
			nextRow.createCell(5).setCellValue("集成服务集团");
			nextRow.createCell(6).setCellValue(oldFirstDepartmentName);
			nextRow.createCell(7).setCellValue(oldSecDepartmentName);
			nextRow.createCell(8).setCellValue("");
			nextRow.createCell(9).setCellValue("集成服务集团");
			nextRow.createCell(10).setCellValue(bossInfoName);
			nextRow.createCell(11).setCellValue(newFirstDepartmentName);
			nextRow.createCell(12).setCellValue(newManagerName);
			nextRow.createCell(13).setCellValue(newSecDepartmentName);
			nextRow.createCell(14).setCellValue(newsecdeptManagerName);
			nextRow.createCell(15).setCellValue("");
			nextRow.createCell(16).setCellValue("");
			nextRow.createCell(17).setCellValue(newSecDepId);
		}
		errorEmailLog.setEmailMessage(textSum);//邮件内容
		//上传附件
		String fileOnlyName = "部门调动邮件附件" + "_" + System.currentTimeMillis() + ".xlsx"; // 文件唯一名称
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		workBook.write(bos);
		byte[] barray=bos.toByteArray();
		//is第一转
		InputStream is=new ByteArrayInputStream(barray);
		// 文件的路径 
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DATE);
		// 通过时间，在服务器动态产生路径，来保存文件
		String datePath = "/" + year + "/" + month + "/" + day + "/";
		String filepath = departmentTransfPath + datePath;// 附件路径
		this.fileUtils.uploadFile(is, filepath, fileOnlyName);
		logger.info("部门调动审批附件上传成功!");
		errorEmailLog.setAttachmentPath(basePath+filepath+fileOnlyName);
		return errorEmailLog;
	}
	
}
