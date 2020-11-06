package com.nantian.erp.hr.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.dao.*;
import com.nantian.erp.hr.data.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

/** 
 * Description: 员工离职service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月17日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
@PropertySource(value= {"classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpDimissionService {

	/*
	 * 从配置文件中获取主机相关属性
	 */
	@Value("${protocol.type}")
    private String protocolType;//http或https
	
	@Autowired
	private ErpEmployeeDimissionMapper employeeDimissionMapper;
	@Autowired
	private ErpEmployeeMapper employeeMapper;
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ErpEmployeeRecordMapper employeeRecordMapper;
	@Autowired 
	private ErpResumePostMapper resumePostMapper;
	@Autowired
	private ErpResumeMapper resumeMapper;
	@Autowired
	private ErpDepartmentMapper erpDepartmentMapper;
	@Autowired
	private ErpRecordMapper recordMapper;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Description: 通过员工编号查询该员工的全部信息
	 * 
	 * @return        
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月17日 上午11:26:27
	 */
	public RestResponse findEmployeeDetail(Integer employeeId) {
		logger.info("findEmployeeDetail方法开始执行，传递参数：employeeId="+employeeId);
		try {
			Map<String, Object> employeeInfo = employeeDimissionMapper.selectEmployeeDetail(employeeId);
			return RestUtils.returnSuccess(employeeInfo);
		} catch (Exception e) {
			logger.error("findEmployeeDetail方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致查询失败！");
		}
	}

	/**
	 * Description: 保存离职申请
	 * 
	 * @return        
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月17日 下午17:43:54
	 */
	@Transactional
	public RestResponse insertDimission(String token,ErpDimission dimission) {
		logger.info("insertDimission方法开始执行，参数是："+dimission);
		try {
			if(dimission.getDimissionTime() == null || !ExDateUtils.checkDateString(dimission.getDimissionTime()) || dimission.getDimissionTime().indexOf("T") > -1){
				return RestUtils.returnFailure("离职日期格式不正确！");
			}
			Map<String, Object> employeeDetail = employeeMapper.findEmployeeDetail(dimission.getEmployeeId());

			//插入一条员工离职记录信息
			employeeDimissionMapper.insertDimission(dimission);
			
			//更新员工表，修改员工状态为“离职中”
			ErpEmployee employee = new ErpEmployee();
			employee.setEmployeeId(dimission.getEmployeeId());
			employee.setStatus(DicConstants.EMPLOYEE_STATUS_DIMISSION_ING);
			employeeMapper.updateEmployee(employee);			

			//增加员工在职记录表
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
			employeeRecord.setEmployeeId(dimission.getEmployeeId()); 
			employeeRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			employeeRecord.setContent("提交离职申请");			
			employeeRecord.setProcessoer(erpUser.getEmployeeName());
			employeeRecordMapper.insertEmployeeRecord(employeeRecord);

			if(employeeDetail != null && employeeDetail.get("resumeId") != null){
				ErpRecord record = new ErpRecord();
				record.setResumeId(Integer.valueOf(String.valueOf(employeeDetail.get("resumeId"))));
				record.setTime(ExDateUtils.getCurrentStringDateTime());
				record.setContent("提交离职申请");
				record.setProcessor(erpUser.getEmployeeName());
				record.setProcessorId(erpUser.getUserId());
				recordMapper.insertRecord(record);
			}

			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("insertDimission方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致操作失败！");
		}
	}
	
	/**
	 * Description: 查询全部的离职申请
	 * 
	 * @param token 登录令牌
	 * @param page 页码
	 * @param rows 一页显示的行数
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月18日 上午10:09:16
	 */
	public RestResponse findEmployeeAllDimission(String token, Integer page, Integer rows,String key,String type) {
		logger.info("findEmployeeAllDimission方法开始执行，参数是：token="+token+",page="+page+",rows="+rows+",key="+key);
		Map<String,Object> resultMap = new HashMap<>();
		List<Map<String,Object>> list = new ArrayList<>();
		long total = 0;
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			List<Integer> roles = erpUser.getRoles();//从用户信息中获取角色数组
			Map<String, Object> queryMap=new HashMap<String, Object>();
			
			if(roles.contains(8) || roles.contains(1)){//总经理和hr可以看到所有部门
			}else if(roles.contains(9)){	//副总经理
				queryMap.put("superLeaderId", erpUser.getUserId());
			}
			else if(roles.contains(2)){//一级部门经理角色
				queryMap.put("leaderId", erpUser.getUserId());
			}
			else{
				RestUtils.returnSuccess(resultMap);
			}

			if (key != ""){
				//前端分页
				queryMap.put("key", "%"+key+"%");
			}			
			
			if (type.equals("1")){
				queryMap.put("status", "3");
			}
			else if (type.equals("2")){
				queryMap.put("status", "4");
				queryMap.put("dealTimeIsNull", true);
			}
			else if (type.equals("3")){
				queryMap.put("status", "4");
				queryMap.put("dealTimeNotNull", true);
			}
			
			queryMap.put("limit", rows);
			queryMap.put("offset", rows*(page-1));
			list = employeeDimissionMapper.selectEmployeeAllDimissionByParams(queryMap);
			total = employeeDimissionMapper.findTotalCountOfDimissionApply(queryMap);		
			
			resultMap.put("list", list);
			resultMap.put("total", total);
			return RestUtils.returnSuccess(resultMap);
		} catch (Exception e) {
			logger.info("findEmployeeAllDimission方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
	}
	
	/**
	 * @author gaolp
	 * add time 2019-08-01
	 * @param dimission
	 * 修改离职申请
	 */
	@Transactional
	public RestResponse updateDimission(String token,ErpDimission dimission) throws ParseException {
		logger.info("进入updateDimission方法：dimission="+dimission);
		if(dimission.getDimissionTime() == null || !ExDateUtils.checkDateString(dimission.getDimissionTime()) || dimission.getDimissionTime().indexOf("T") > -1){
			return RestUtils.returnFailure("离职日期格式不正确！");
		}
		employeeDimissionMapper.updateDimission(dimission);
		return RestUtils.returnSuccess("修改成功！");
	}
	
	
	
	
	/**
	 * Description: 取消离职
	 * 
	 * @param dimissionId
	 * @param employeeId
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月18日 上午11:21:41
	 */
	@Transactional
	public RestResponse deleteDimission(String token, Integer dimissionId,Integer employeeId) {
		logger.info("deleteDimission方法开始执行，参数是：dimissionId="+dimissionId+",employeeId="+employeeId);
		try {
			//删除离职申请
			employeeDimissionMapper.deleteDimission(dimissionId);
			//更新员工表，将项目信息ID写入该条记录中
			ErpEmployee employee = new ErpEmployee();
			employee.setEmployeeId(employeeId);
			employee.setStatus(DicConstants.EMPLOYEE_STATUS_FORMAL);
			employeeMapper.updateEmployee(employee);			

			//增加员工在职记录表
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
			employeeRecord.setEmployeeId(dimissionId); 
			employeeRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			employeeRecord.setContent("取消离职");
			employeeRecord.setProcessoer(erpUser.getEmployeeName());
			employeeRecordMapper.insertEmployeeRecord(employeeRecord);
			
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("deleteDimission方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致操作失败！");
		}
	}
	
	/**
	 * Description: 更新离职申请
	 * 
	 * @param dimission
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月18日 上午11:33:04
	 */
	@Transactional
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RestResponse confirmDimission(String token,ErpDimission dimission) {
		logger.info("confirmDimission方法开始执行，参数是："+dimission);
		try {
			if(dimission.getDimissionTime() == null || !ExDateUtils.checkDateString(dimission.getDimissionTime()) || dimission.getDimissionTime().indexOf("T") > -1){
				return RestUtils.returnFailure("离职日期格式不正确！");
			}
			/*
			 * 更新办理手续时间
			 */
			employeeDimissionMapper.updateDimission(dimission);
			
			/*
			 * 更新员工表，修改员工状态为“已离职”,保存当前部门名称
			 */
			Map<String, Object> empInfo = employeeMapper.selectByEmployeeIdForlx(dimission.getEmployeeId());
			if (!empInfo.get("status").equals("4")){
				employeeMapper.updateDimissionEmployeeInfo(dimission.getEmployeeId());
				
				/*更新部门与员工的归属关系*/      
				//查询员工当前部门归属
				Map<String, Object> key = new HashMap<>();
				key.put("employeeId",dimission.getEmployeeId());
				key.put("currentDepartment",true);
				List<Map<String, Object>> relationList = this.erpDepartmentMapper.getEmpDepRelation(key);
				if (relationList.size()>1){
					logger.error("员工存在多个当前部门关联："+dimission.getEmployeeId());
				}
				for(Map<String, Object> relation : relationList){
					relation.put("endTime", dimission.getDimissionTime());
					this.erpDepartmentMapper.updateEmpDepRelation(relation);
	
				}
				
				//离职人员将部门名称写入员工表，部门id清零
				
				/*
				 * 调用权限工程，通过员工Id查询用户Id
				 */
				MultiValueMap<String, Object> erpUser = new  LinkedMultiValueMap<String, Object>();
				erpUser.add("userId", dimission.getEmployeeId());//员工Id
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.add("token",token);
				HttpEntity<MultiValueMap<String,Object>> request = new HttpEntity<>(erpUser,requestHeaders); 
				String url =  protocolType+"nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId"; 
				ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
				//跨工程调用响应失败
			    if(200 != response.getStatusCodeValue() || !"200".equals(response.getBody().get("status"))) {
					return RestUtils.returnFailure("调用权限工程时发生异常，响应失败！");
				}
			    if(response.getBody().get("data")==null || "".equals(response.getBody().get("data"))) {
			    	return RestUtils.returnSuccessWithString("OK");
			    }
				Map<String,Object> userInfo = (Map<String, Object>) response.getBody().get("data");
				Integer id = Integer.valueOf(String.valueOf(userInfo.get("id")));//用户Id
				/*
				 * 删除用户表一条记录，同时删除用户和角色关联记录数据
				 */
				String url1 = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/deleteUser?userId="+id;
				HttpHeaders requestHeaders1 = new HttpHeaders();
				requestHeaders1.add("token", token);
			    HttpEntity<String> request1 = new HttpEntity<String>(null,requestHeaders1);
			    ResponseEntity<Map> response1 = restTemplate.exchange(url1,HttpMethod.GET,request1,Map.class);
			    //跨工程调用响应失败
			    if(200 != response1.getStatusCodeValue() || !"200".equals(response1.getBody().get("status"))) {
					return RestUtils.returnFailure("调用权限工程时发生异常，响应失败！");
				}		    
	
				//增加员工在职记录表
			    ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
				ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
				employeeRecord.setEmployeeId(dimission.getEmployeeId()); 
				employeeRecord.setTime(dimission.getDealWithTime());
				employeeRecord.setContent("离职办理完成");
				employeeRecord.setProcessoer(user.getEmployeeName());
				employeeRecordMapper.insertEmployeeRecord(employeeRecord);

				if(empInfo != null && empInfo.get("resumeId") != null) {
					ErpRecord record = new ErpRecord();
					record.setResumeId(Integer.valueOf(String.valueOf(empInfo.get("resumeId"))));
					record.setTime(ExDateUtils.getCurrentStringDateTime());
					record.setContent("离职办理完成");
					record.setProcessor(user.getEmployeeName());
					record.setProcessorId(user.getUserId());
					recordMapper.insertRecord(record);
				}
				//判断是否试用期离职
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String probationEndTime = (String) empInfo.get("probationEndTime");
				if(probationEndTime == null || probationEndTime.equals("")){
					return RestUtils.returnSuccessWithString("该员工试用期时间为空！");
				}
				Date probationDate = format.parse(probationEndTime); 
				Date dimissionDate = format.parse(dimission.getDimissionTime()); 
				
				if (dimissionDate.getTime() < probationDate.getTime()){
					//通知薪酬模块，如果尚未转正，删除转正工资单流程
					String url2 = protocolType+"nantian-erp-salary/nantian-erp/salary/payRollFlow/deleteEmployee?employeeId="+dimission.getEmployeeId();
					HttpHeaders requestHeaders2 = new HttpHeaders();
					requestHeaders2.add("token", token);
				    HttpEntity<String> request2 = new HttpEntity<String>(null,requestHeaders2);
				    ResponseEntity<Map> response2 = restTemplate.exchange(url2,HttpMethod.GET,request2,Map.class);
				    //跨工程调用响应失败
				    if(200 != response1.getStatusCodeValue() || !"200".equals(response2.getBody().get("status"))) {
						return RestUtils.returnFailure("调用salary工程时发生异常，响应失败！");
					}	
				}	
			}
		    
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.info("confirmDimission方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致操作失败！");
		}
	}
	
	/**
	 * Description: 离职员工再入职
	 * 
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年03月07日 16:39:07
	 */
	@Transactional
	public RestResponse entryAgain(String token,Map<String,Object> params) {
		logger.info("insertDimission方法开始执行，参数是：params="+params);
		try {
			//从缓存中获取登录用户信息
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			
			//接收前端传过来的参数
			Integer employeeId = (Integer) params.get("employeeId");//员工ID
			Integer postId = (Integer) params.get("postId");//岗位ID
			Integer proposerId = (Integer) params.get("proposerId");//面试官的员工ID
			Integer dimissionId = (Integer) params.get("id");//离职申请ID
			Integer resumeId;//简历ID
			
			//只有通过流程新增的员工有简历；导入的员工没有简历，需要通过员工ID查询信息，并新建简历
			if(params.get("resumeId")!=null) {
				resumeId = (Integer) params.get("resumeId");
				
				//修改简历的状态为“面试中”
				ErpResume resume = new ErpResume();
				resume.setResumeId(resumeId);
				resume.setIsValid(true);
				resume.setStatus(DicConstants.RESUME_STATUS_IN_THE_INTERVIEW);
				resumeMapper.updateResume(resume);
			}else {
				Map<String, Object> employeeMap = employeeMapper.findEmployeeDetail(employeeId);
				
				Map<String,Object> validParam = new HashMap<>();
				validParam.put("email", employeeMap.get("personalEmail"));
				ErpResume validResume = resumeMapper.validPhoneAndEmail(validParam);
				//如果简历表中不存在该员工的邮箱，则新增简历；如果存在，就直接使用简历ID
				if(validResume==null) {
					ErpResume resume = new ErpResume();
					resume.setName(String.valueOf(employeeMap.get("name")==null?"":employeeMap.get("name")));
					resume.setSex(String.valueOf(employeeMap.get("sex")==null?"":employeeMap.get("sex")));
					resume.setDegree(String.valueOf(employeeMap.get("education")==null?"":employeeMap.get("education")));
					resume.setIsValid(true);
					resume.setStatus(DicConstants.RESUME_STATUS_IN_THE_INTERVIEW);
					resume.setEmail(String.valueOf(employeeMap.get("personalEmail")==null?"":employeeMap.get("personalEmail")));
					resume.setIsTrainee(false);
					resume.setSchool(String.valueOf(employeeMap.get("school")==null?"":employeeMap.get("school")));
					resumeMapper.insertResume(resume);
					resumeId = resume.getResumeId();
				}else {
					resumeId = validResume.getResumeId();
				}
			}
			
			//面试流程表新增一条，插入岗位简历关联表，进入复试阶段
			ErpResumePost resumePost = new ErpResumePost();
			resumePost.setResumeId(resumeId);//简历ID
			resumePost.setPostId(postId);//岗位ID
			resumePost.setPersonId(proposerId);//复试面试官
			resumePost.setStatus(DicConstants.INTERVIEW_STATUS_IN_THE_INTERVIEW);//面试中
			resumePost.setIsValid(true);//流程是否有效
			resumePost.setSegment(DicConstants.INTERVIEW_SEGMENT_SOCIAL_REEXAM);//复试
			resumePostMapper.insertResumePost(resumePost);
			
			//增加员工在职记录表
			ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
			employeeRecord.setEmployeeId(employeeId);
			employeeRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			employeeRecord.setContent("离职员工再入职");
			employeeRecord.setProcessoer(erpUser.getEmployeeName());
			employeeRecordMapper.insertEmployeeRecord(employeeRecord);
			
			//删除离职申请
			employeeDimissionMapper.deleteDimission(dimissionId);
			
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("insertDimission方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致操作失败！");
		}
	}
	
	
	/*
	 * add by 曹秀斌
	 * 查询员工的离职信息
	 */
	public ErpDimission findOneById(Integer employeeId) {
		try {
			logger.info("findOneById方法开始执行，传递参数1：employeeId:" + employeeId);
		} catch (Exception e) {
			logger.error("findOneById方法出现异常：" + e.getMessage(),e);
		}
		return this.employeeDimissionMapper.findOneById(employeeId);
	}

}
