package com.nantian.erp.hr.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.ErpContractMapper;
import com.nantian.erp.hr.data.dao.ErpDepartmentMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeePostiveMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeRecordMapper;
import com.nantian.erp.hr.data.dao.ErpPositiveRecordMapper;
import com.nantian.erp.hr.data.model.ErpContract;
import com.nantian.erp.hr.data.model.ErpEmployee;
import com.nantian.erp.hr.data.model.ErpEmployeePostive;
import com.nantian.erp.hr.data.model.ErpEmployeeRecord;
import com.nantian.erp.hr.data.model.ErpPositiveRecord;
import com.nantian.erp.hr.data.vo.ErpDeployQueryVo;

@Service
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties","classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties","classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpPostiveService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Value("${protocol.type}")
    private String protocolType;//http或https
	@Autowired
	private ErpEmployeePostiveMapper erpEmployeePostiveMapper;
	@Autowired
	private ErpPositiveRecordMapper erpPositiveRecordMapper;
	@Autowired
	private ErpContractMapper erpContractMapper;
	@Autowired
	private ErpEmployeeMapper employeeMapper;

	@Autowired
	RestTemplate restTemplate;
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private ErpDepartmentMapper erpDepartmentMapper;
	@Autowired
	private ErpEmployeeRecordMapper employeeRecordMapper;
	
	public List<Map<String, Object>> findAll(ErpUser erpUser,String token) {
		logger.info("进入findAll方法，参数 erpUser"+erpUser.toString());
		List<Map<String, Object>> list =new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> listResult = new ArrayList<Map<String,Object>>();
		int count=0;
		try {			
			List<Integer> roles=erpUser.getRoles();	//当前登录人角色列表
			Map<String, Object> queryMap=new HashMap<String, Object>();
			if(roles.contains(8) || roles.contains(1)){//总经理和hr可以看到所有部门的待入职
				queryMap.put("managerId", erpUser.getUserId());
			}else if(roles.contains(9)){	//副总经理
				queryMap.put("superLeaderId", erpUser.getUserId());
			}
			else if(roles.contains(2)){//一级部门经理角色
				queryMap.put("leaderId", erpUser.getUserId());
			}
			else if(roles.contains(5)){//二级部门经理角色
				queryMap.put("secondLeaderId", erpUser.getUserId());
			}
			else{
				return listResult;
			}
		
			listResult = this.erpEmployeePostiveMapper.findAllNonPositive(queryMap);
		} catch (Exception e) {
			 
			logger.info("findAll方法出现异常：" + e.getMessage(),e);
		}
		return listResult;
	}

	/**
	 * update by 张玉伟  20180917
	 * 通过用户名找到用户id，通过用户id查找用户和角色关联表信息，最后通过角色来决定回显的列表
	 * @param username
	 * @return
	 */
	public List<Map<String, Object>> findByRole(ErpUser erpUser,String token) {
		
		logger.info("进入findByRole方法，参数是：erpUser="+erpUser.toString());
		List<Map<String,Object>> listRes = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> listResult = new ArrayList<Map<String,Object>>();
		
		try {
			Integer userType=erpUser.getUserType();
			if(userType.equals(1)){
				Integer userId=erpUser.getUserId();
				List<Integer> dmList=this.erpEmployeePostiveMapper.findDmProcess(userId);
				if(dmList.size()>0){
					HashMap<String, Object> queryHashMap=new HashMap<String, Object>();
					queryHashMap.put("currentPersonID", erpUser.getUserId());
					listRes = erpEmployeePostiveMapper.findAll(queryHashMap);
					List<Map<String,Object>> tempList=findUserByEmpIds(listRes,token);
					for(int i=0;i<listRes.size();i++){						
						Map<String,Object> mapTemp=listRes.get(i);
						Integer empId=Integer.valueOf(String.valueOf(mapTemp.get("employeeId")));
						for(Map<String,Object> map:tempList){
							Integer empId2=Integer.valueOf(String.valueOf(map.get("userId")));
							if(empId2.intValue()==empId.intValue()){
								 mapTemp.put("phone", map.get("userPhone"));
								 break;
							}
						}
				        listResult.add(mapTemp);
					}
				}
			}
		} catch (Exception e) {
			 
			logger.info("findByRole方法出现异常：" + e.getMessage(),e);
		}
		return listResult;
	}

	/**
	 * update by 张玉伟  20180917
	 * @param onTimeHrQueryVo
	 * @param username
	 * @return
	 */
	/*public String ontimeHr(ErpOnTimeHrQueryVo onTimeHrQueryVo,String username) {
		logger.info("进入ontimeHr方法，参数是："+onTimeHrQueryVo.toString()+",username="+username);
		try {
			//增加一条转正记录
			ErpPositiveRecord positiveRecord = new ErpPositiveRecord();
			positiveRecord.setEmployeeId(onTimeHrQueryVo.getEmployeeId());
			positiveRecord.setPerson(username);
			positiveRecord.setTime(ErpDateUtils.getCurrentStringDateTime());
			positiveRecord.setContext(onTimeHrQueryVo.getContent());
			erpPositiveRecordMapper.insertPositiveRecord(positiveRecord);
			//通过员工ID修改员工转正流程表一条记录
			ErpEmployeePostive employeePostive = new ErpEmployeePostive();
			employeePostive.setCurrentPerson(onTimeHrQueryVo.getProcessoer());
			employeePostive.setEmployeeId(onTimeHrQueryVo.getEmployeeId());
			erpEmployeePostiveMapper.updateEmployeePostive(employeePostive);
		} catch (Exception e) {
			 
			logger.info("ontimeHr方法出现异常：" + e.getMessage());
		}
		return "OK";
	}*/

	/**
	 * update by 张玉伟  20180917
	 * @param employeeId
	 * @param content
	 * @param assistance
	 * @param username
	 */
	@Transactional
	public String ontimeDm(Integer employeeId, String content, ErpUser erpUser) {
		logger.info("进入ontimeDm方法，参数是：employeeId="+employeeId+",content="+content+",erpUser="+erpUser.toString());
		try {
			//增加一条转正记录
			ErpPositiveRecord positiveRecord = new ErpPositiveRecord();
			positiveRecord.setEmployeeId(employeeId);
			positiveRecord.setPerson(erpUser.getEmployeeName());
			positiveRecord.setPersonID(erpUser.getUserId());
			positiveRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			positiveRecord.setContext(content);
			erpPositiveRecordMapper.insertPositiveRecord(positiveRecord);
			
			//通过员工ID删除员工转正流程表一条记录
			ErpEmployeePostive erpEmployeePostive=new ErpEmployeePostive();
			erpEmployeePostive.setEmployeeId(employeeId);
			erpEmployeePostive.setStatus(2);
			this.erpEmployeePostiveMapper.updateEmployeePostive(erpEmployeePostive);
			
			//查询转正时间
			ErpContract erpContrcat = erpContractMapper.findContractByEmpId(employeeId);
			
			//增加员工在职记录表
			ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
			employeeRecord.setEmployeeId(employeeId); 
			employeeRecord.setTime(erpContrcat.getProbationEndTime());
			employeeRecord.setContent("转正通过");
			employeeRecord.setProcessoer(erpUser.getEmployeeName());
			employeeRecordMapper.insertEmployeeRecord(employeeRecord);
			
		} catch (Exception e) {
			 
			logger.info("ontimeDm方法出现异常：" + e.getMessage(),e);
		}
		return "OK";
	}

	/**
	 * update by 张玉伟  20180917
	 * @param deployQueryVo
	 * @param username
	 */
	@Transactional
	public String delay(ErpDeployQueryVo deployQueryVo, ErpUser erpUser, String token) {
		logger.info("进入delay方法，参数是："+deployQueryVo.toString()+",erpUser="+erpUser);		
		try {			
			ErpContract contract = new ErpContract();
			contract.setEmployeeId(deployQueryVo.getEmployeeId());
			contract.setProbationEndTime(deployQueryVo.getTime());
			erpContractMapper.updateContractByEmployeeId(contract);
			//新增一条转正记录表信息
			ErpPositiveRecord positiveRecord = new ErpPositiveRecord();
			positiveRecord.setEmployeeId(deployQueryVo.getEmployeeId());
			positiveRecord.setPerson(erpUser.getEmployeeName());
			positiveRecord.setPersonID(erpUser.getUserId());
			positiveRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			positiveRecord.setContext(deployQueryVo.getContent());
			erpPositiveRecordMapper.insertPositiveRecord(positiveRecord);
			
			//增加员工在职记录表
			ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
			employeeRecord.setEmployeeId(deployQueryVo.getEmployeeId()); 
			employeeRecord.setTime(deployQueryVo.getTime());
			employeeRecord.setContent("修改转正时间");
			employeeRecord.setProcessoer(erpUser.getEmployeeName());
			employeeRecordMapper.insertEmployeeRecord(employeeRecord);
			
			
			//调用ERP-salary工程-将用户的erp_payRoll_flow positiveMonth 更新
			String url=protocolType+"nantian-erp-salary/nantian-erp/salary/payRollFlow/updatePositiveMonth";
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token",token);
			Map<String,Object> payRoll = new HashMap<>();
		
			//只获取年月
			String positiveMonth = deployQueryVo.getTime();
			SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
			Date datePositiveMonth = monthFormat.parse(positiveMonth);
			positiveMonth =  monthFormat.format(datePositiveMonth);
			
			//获取当前登陆用户的ID
//			ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token); //从缓存中获取登录用户信息
//			Integer userID = user.getId();
			
			//更新flowMap 对象
			payRoll.put("positiveMonth", positiveMonth);
			payRoll.put("userId",deployQueryVo.getEmployeeId());
			
			//远程请求
			HttpEntity<Map<String,Object>> request = new HttpEntity<>(payRoll, requestHeaders);
			ResponseEntity<Map> flowrRes = this.restTemplate.postForEntity(url, request, Map.class);
			
			//判断请求是否失败
			if(flowrRes.getStatusCodeValue() != 200){
				logger.info("修改erp_payRoll_flow表:更positiveMonth失败");
				return ("修改erp_payRoll_flow表:更positiveMonth失败");
			}			
			
		} catch (Exception e) {
			 
			logger.info("delay方法出现异常：" + e.getMessage(), e);
		}
		return "OK";
	}

	/**
	 * update by 张玉伟  20180917
	 * @param employeeId
	 * @return
	 */
	public List<ErpPositiveRecord> findRecord(Integer employeeId) {
		logger.info("进入findRecord方法，参数是：employeeId="+employeeId);
		List<ErpPositiveRecord> list = null;
		try {
			list = erpPositiveRecordMapper.findByEmployeeId(employeeId);
		} catch (Exception e) {
			 
			logger.info("findRecord方法出现异常：" + e.getMessage());
		}
		return list;
	}
	
	/**
	 * created by hhr 20181019
	 * update by gaolp 2019-08-19
	 * 1、用于查询待转正人员
	 * 2、将存在转正薪资的人员进行自动转正
	 * */
	@Transactional
	public List<Integer> automaticPosition(Map<String,Object> params){
		logger.info("automaticPosition方法开始执行");
		Integer flag = (Integer) params.get("flag");
		List<Integer> empIdList = new ArrayList<>();
		try{
			if (flag.equals(1)) {
				 empIdList=this.erpEmployeePostiveMapper.findTimeoutNotPositionEmpId();
			}else if (flag.equals(2)){
				Integer employId = (Integer) params.get("employeeId");
			    if (employId != null ) {
			    		ErpEmployee erpEmployee = new ErpEmployee();
						erpEmployee.setEmployeeId(employId);
						erpEmployee.setStatus(DicConstants.EMPLOYEE_STATUS_FORMAL);
						employeeMapper.updateEmployee(erpEmployee);
						
						ErpEmployeePostive erpEmployeePostive=new ErpEmployeePostive();
						erpEmployeePostive.setEmployeeId(employId);
						erpEmployeePostive.setStatus(3);//已转正
						this.erpEmployeePostiveMapper.updateEmployeePostive(erpEmployeePostive);					

						//增加员工在职记录表
						ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
						employeeRecord.setEmployeeId(employId); 
						employeeRecord.setTime(ExDateUtils.getCurrentStringDateTime());
						employeeRecord.setContent("超时自动转正");
						employeeRecord.setProcessoer("系统");
						employeeRecordMapper.insertEmployeeRecord(employeeRecord);
		    	}
			}
		}catch(Exception e){
			logger.info("automaticPosition方法执行异常：{}",e.getMessage());
		}
		return empIdList;
	}
	
	public List<Map<String,Object>> findUserByEmpIds(List<Map<String,Object>> list,String token){
		logger.info("findUserByEmpIds方法开始执行,参数list："+list.toString());
		List<Map<String, Object>> erpUserList=new ArrayList<Map<String,Object>>();
		try{
			
			String url=protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/findUserByEmpIds";
			MultiValueMap<String, Object> userMap = new  LinkedMultiValueMap<String, Object>(); //用户对象 map
			userMap.add("curPersonIds", list); //参数
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);
			HttpEntity<MultiValueMap<String,Object >> request = new HttpEntity<MultiValueMap<String, Object>>(userMap, requestHeaders);					
			ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request, RestResponse.class);	        RestResponse response = responseEntity.getBody();
	        if(null == response.getData() || "".equals(response.getData())) {
				logger.info("通过用户ID获取员工ID方法返回结果为空！");
				return new ArrayList<Map<String,Object>>();
			}
			erpUserList =(List<Map<String, Object>>) response.getData();//解析结果
		}catch(Exception e){
			 
			logger.info("findUserByEmpIds方法出现异常"+e.getMessage(),e);
		}
		return erpUserList;
	}

	/**
	 * created by sxg 201911123
	 * 1.更新员工转正表的状态
	 * */
	public String updateEmployeePostive(ErpEmployeePostive params) {
		try{
			this.erpEmployeePostiveMapper.updateEmployeePostive(params);
			return "OK";
		}catch(Exception e) {
			logger.info("updateEmployeePostive方法出现异常"+e.getMessage(),e);
			return "Error";
		}
	}
}
