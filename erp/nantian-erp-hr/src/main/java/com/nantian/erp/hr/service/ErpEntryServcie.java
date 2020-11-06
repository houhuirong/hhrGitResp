package com.nantian.erp.hr.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.ErpContractMapper;
import com.nantian.erp.hr.data.dao.ErpDepartmentMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeEntryMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeePostiveMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeRecordMapper;
import com.nantian.erp.hr.data.dao.ErpEntryRecordMapper;
import com.nantian.erp.hr.data.dao.ErpOfferMapper;
import com.nantian.erp.hr.data.dao.ErpPostMapper;
import com.nantian.erp.hr.data.dao.ErpRecordMapper;
import com.nantian.erp.hr.data.dao.ErpResumeMapper;
import com.nantian.erp.hr.data.dao.ErpResumePostMapper;
import com.nantian.erp.hr.data.dao.ErpTotalEntriedMapper;
import com.nantian.erp.hr.data.dao.PositionOperReordMapper;
import com.nantian.erp.hr.data.model.ErpContract;
import com.nantian.erp.hr.data.model.ErpEmployee;
import com.nantian.erp.hr.data.model.ErpEmployeeEntry;
import com.nantian.erp.hr.data.model.ErpEmployeePostive;
import com.nantian.erp.hr.data.model.ErpEmployeeRecord;
import com.nantian.erp.hr.data.model.ErpEntryRecord;
import com.nantian.erp.hr.data.model.ErpOffer;
import com.nantian.erp.hr.data.model.ErpPositionRankRelation;
import com.nantian.erp.hr.data.model.ErpPost;
import com.nantian.erp.hr.data.model.ErpRecord;
import com.nantian.erp.hr.data.model.ErpResume;
import com.nantian.erp.hr.data.model.ErpResumePost;
import com.nantian.erp.hr.data.model.PositionOperRecond;
import com.nantian.erp.hr.data.vo.ErpEmployeeQueryVo;

@Service
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties","classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties","classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpEntryServcie  {

	@Value("${protocol.type}")
    private String protocolType;//http或https
	@Autowired
	private ErpEmployeeEntryMapper employeeEntryMapper;
	@Autowired
	private ErpTotalEntriedMapper totalEntriedMapper;
	@Autowired
	private ErpResumeMapper resumeMapper;
	@Autowired
	private ErpResumePostMapper resumePostMapper;
	@Autowired
	private ErpContractMapper contractMapper;
	@Autowired
	private ErpEntryRecordMapper entryRecordMapper;
	@Autowired
	private ErpEmployeeRecordMapper employeeRecordMapper;
	@Autowired
	private ErpEmployeeMapper employeeMapper;
	@Autowired
	private ErpOfferMapper offerMapper;
	@Autowired
	private ErpEmployeePostiveMapper employeePostiveMapper;
	@Autowired
	private ErpDepartmentMapper erpDepartmentMapper;
	@Autowired
	private ErpRecordMapper erpRecordMapper;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@SuppressWarnings({ "rawtypes" })
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private ErpPostMapper postMapper;
	@Autowired
	PositionOperReordMapper operRecordMapper;
	@Autowired
	ErpRecordMapper recordMapper;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * **
	 * update by 侯慧蓉 20181026 部门经理仅显示自己部门，hr显示所有待入职
	 * @param token
	 * @return
	 * */
	public Map<String, Object> findAll(String token) {
		logger.info("findAll方法开始执行，传递参数：token:"+token);

		ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
	    List<Map<String,Object>> tempList=new ArrayList<Map<String,Object>>();
	    Map<String, Object> queryHashMap = new HashMap<String, Object>();
//	    Integer roleId=null;
	   
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
				queryHashMap.put("allWaitEntry", tempList);
				return queryHashMap;
			}

			tempList=this.employeeEntryMapper.findAllWaitingEntry(queryMap);  //所有待入职的OfferId列表
			queryHashMap.put("allWaitEntry", tempList);
		} catch (Exception e) {
			 
			logger.info("findAll方法出现异常：" + e.getMessage(),e);
		}
		return queryHashMap;
	}
	
	public Map<String, Object> findAll(String token,String startTime,String endTime) {
		logger.info("findAll方法开始执行，传递参数：token={}，startTime={}，endTime={}",token,startTime,endTime);

		ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
	    List<Map<String,Object>> tempList=new ArrayList<Map<String,Object>>();
	    Map<String, Object> queryHashMap = new HashMap<String, Object>();
//	    Integer roleId=null;
	   
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
				queryHashMap.put("allWaitEntry", tempList);
				return queryHashMap;
			}

			//可查看所有待入职,即status=1,2,3
			queryMap.put("startTime", startTime+"-01");
			queryMap.put("endTime", endTime+"-31");
			tempList=this.employeeEntryMapper.findAllWaitingEntry(queryMap);  //所有待入职的OfferId列表
			List<Map<String,Object>> allEntried=this.employeeEntryMapper.findAllEntried(queryMap);  //所有待入职的OfferId列表
			tempList.addAll(allEntried);
			queryHashMap.put("allWaitEntry", tempList);
		} catch (Exception e) {
			 
			logger.info("findAll方法出现异常：" + e.getMessage(),e);
		}
		return queryHashMap;
	}
	
	/**
	 * update by 张玉伟 20180917 修改员工信息
	 * @param employeeQueryVo
	 * @param username
	 * @return
	 */
	@Transactional
	public RestResponse updateEmployee(ErpEmployeeQueryVo employeeQueryVo, ErpUser erpUser,String token) {
		
		logger.info("updateEmployee方法开始执行，传递参数1：ErpEmployeeQueryVo:" + employeeQueryVo.toString() + ",参数2:erpUser:" + erpUser.toString());		
		Map<String,Object> queryMap=new HashMap<String, Object>();
		Integer employeeId = 0;
		String statusName = null;
		try {

			Integer offerId=employeeQueryVo.getOfferId();
			Integer resumeId=employeeQueryVo.getResumeId();
			queryMap.put("offerId", offerId);
			queryMap.put("resumeId", resumeId);
			List<Map<String,Object>> list=this.employeeMapper.findSameEmployeeByOfferId(queryMap);
			if(list.size()>0){
				return RestUtils.returnSuccess("该人员已入职,请勿重复入职！");
			}
			// 添加入职记录
			ErpEntryRecord entryRecord = new ErpEntryRecord();
			entryRecord.setTime(ExDateUtils.getCurrentStringDateTime()); //处理时间
			entryRecord.setContent("hr已处理");  
			entryRecord.setOfferId(employeeQueryVo.getOfferId());
			entryRecord.setProcessoer(erpUser.getEmployeeName()); //处理人
			entryRecord.setProcessoerID(erpUser.getUserId());
			entryRecordMapper.insertEntryRecord(entryRecord);

			ErpRecord record = new ErpRecord();
			record.setResumeId(resumeId);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent("已入职");
			record.setProcessor(erpUser.getEmployeeName());
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);

			// 更新员工表
			ErpEmployee employee = new ErpEmployee();
			employee.setFirstDepartment(employeeQueryVo.getFirstDepartmentId());
			employee.setSecondDepartment(employeeQueryVo.getSecondDepartmentId());
			employee.setIsActive(true);
			employee.setPersonalEmail(employeeQueryVo.getPersonalEmail());
			employee.setName(employeeQueryVo.getName());
			employee.setSex(employeeQueryVo.getSex());
			String status = employeeQueryVo.getStatus();
			employee.setStatus(status);
			employee.setPositionId(employeeQueryVo.getPositionNo());//职位ID
			employee.setPosition(employeeQueryVo.getPosition());//职位名称
			employee.setRank(employeeQueryVo.getRank());//职级
			if(!"0".equals(status)){
			employee.setSocialSecurity(employeeQueryVo.getSocialSecurityPlace());
			}
			employee.setOfferId(employeeQueryVo.getOfferId());
			employee.setResumeId(employeeQueryVo.getResumeId());			
			employee.setEntryTime(employeeQueryVo.getEntryTime());
			employee.setPoliticalStatus(employeeQueryVo.getPoliticalStatus());
			employee.setGroups(employeeQueryVo.getGroups());
			
			//从简历表和offer表中查询手机号、个人邮箱、学历、身份证号
			Map<String, Object> resumeInfo = resumeMapper.selectResumeDetail(resumeId);
			Map<String, Object> offerInfo = offerMapper.selectOfferDetail(offerId);
			
			employee.setPersonalEmail(String.valueOf(resumeInfo.get("email")));
			employee.setSchool(String.valueOf(resumeInfo.get("school")));
			employee.setEducation(String.valueOf(resumeInfo.get("degree")));
			employee.setIdCardNumber(String.valueOf(offerInfo.get("idCardNumber")));
			
			/*
			employee.setContractBeginTime(employeeQueryVo.getEntryTime());
			employee.setContractEndTime(employeeQueryVo.getContractEndTime());
			employee.setProbationEndTime(employeeQueryVo.getProbationEndTime());*/
			
			//通过员工姓名、身份证号查询员工ID。如果能查询到，就更新该员工；如果查不到，就新增员工
			Map<String,Object> tempParams = new HashMap<>();
			tempParams.put("name", employeeQueryVo.getName());
			tempParams.put("idCardNumber", String.valueOf(offerInfo.get("idCardNumber")));
 			Integer tempEmployeeId = employeeMapper.findEmpIdByIdCardNumAndName(tempParams);
 			if(tempEmployeeId==null) {
 				employeeMapper.insertEmployee(employee);
 				employeeId = employee.getEmployeeId();
 			}else {
 				employee.setEmployeeId(tempEmployeeId);
 				employeeMapper.updateEmployee(employee);
 				employeeId = tempEmployeeId;
 			}
 			//入职时将员工信息存入redis
			Map<String,Object> employeeMap = new HashMap<>();//员工基本信息
			employeeMap.put("employeeId", employeeId);
			employeeMap.put("employeeName", employeeQueryVo.getName());
			employeeMap.put("sex", employeeQueryVo.getSex());
			employeeMap.put("firstDepartmentId", employeeQueryVo.getFirstDepartmentId());
			employeeMap.put("secondDepartmentId", employeeQueryVo.getSecondDepartmentId());
			employeeMap.put("status", status);
			if(status.equals("0")){
				statusName = "实习生";
			}else if(status.equals("1")){
				statusName = "试用期员工";
			}
			employeeMap.put("statusName", statusName);
			employeeMap.put("entryTime", employeeQueryVo.getEntryTime());
			employeeMap.put("position", employeeQueryVo.getPosition());
			redisTemplate.opsForValue().set(DicConstants.REDIS_PREFIX_EMPLOYEE+employeeId, employeeMap);
			
			//通过offerId和resumeId查询出员工ID
			//employeeId = employeeMapper.findEmployeeIdByOfferIdAndResumeId(employee);
			Integer userId = employeeMapper.findUserIdByDepartID(employeeQueryVo.getSecondDepartmentId()); //二级部门经理用户编号
			if(userId == null){
				//二级部门经理查找不到时由一级部门经理负责
				userId = employeeMapper.findUserIdByDepartID(employeeQueryVo.getFirstDepartmentId());
			}
			
			//增加员工与部门的关联表
			Map<String,Object> empDepRelation =new HashMap<String, Object>();
			empDepRelation.put("employeeId", employeeId);
			empDepRelation.put("departmentId", employeeQueryVo.getFirstDepartmentId());
			empDepRelation.put("startTime", employeeQueryVo.getEntryTime());
			erpDepartmentMapper.insertEmpDepRelation(empDepRelation);
			
			// 添加合同
			ErpContract contact = new ErpContract();
			contact.setEndTime(employeeQueryVo.getContractEndTime());
			contact.setProbationEndTime(employeeQueryVo.getProbationEndTime());
			contact.setBeginTime(employeeQueryVo.getContractBeginTime());
			contact.setEmployeeId(employeeId);
			contractMapper.insertContract(contact);
			//当status为"1"时，填写转正流程表			
			if("1".equals(status)) {//待转正
				ErpEmployeePostive employeePostive = new ErpEmployeePostive();
				employeePostive.setCurrentPersonID(userId);//用户编号
				employeePostive.setEmployeeId(employeeId);
				employeePostive.setStatus(1);//待转正的状态标识
				employeePostiveMapper.insertEmployeePostive(employeePostive);
			}			
			// 更改offer状态
			ErpOffer offer = new ErpOffer();
			offer.setOfferId(employeeQueryVo.getOfferId());
			offer.setStatus(DicConstants.OFFER_STATUS_INVALID); //offer状态改为归档
			offer.setReason("已入职");
			offerMapper.updateOffer(offer);
			// 更改简历状态
			ErpResume resume = new ErpResume();
			resume.setResumeId(employeeQueryVo.getResumeId());
			resume.setStatus(DicConstants.RESUME_STATUS_ENTRY);
			//resume.setIsValid(false);
			resumeMapper.updateResume(resume);
			//更改流程表状态，通过offerID修改当前处理人为部门经理			
			ErpEmployeeEntry employeeEntry = new ErpEmployeeEntry();
//			employeeEntry.setCurrentPersonID(userId);   暂时取消一级部门经理处理
			employeeEntry.setStatus(2);//已入职
			employeeEntry.setOfferId(employeeQueryVo.getOfferId());
			employeeEntryMapper.updateEmployeeEntryByOfferId(employeeEntry);
			
			//给用户表增加信息			
			//已通过接口和角色权限验证，调用ERP-权限工程操作层服务接口-新增用户 add by hhr start
			String url=protocolType+"nantian-erp-authentication/nantian-erp/erp/insertErpUserForHr";
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token",token);
			MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();		
	 		String username=employeeQueryVo.getUsername();
	 		if(username.indexOf("@")==-1){
	 			username=username+"@nantian.com.cn";
		 		paramMap.add("username", username);//用户邮箱
	 		}else{
		 		String d = username.substring(0, username.indexOf("@"));
	 			username=d+"@nantian.com.cn";
		 		paramMap.add("username", username);//用户邮箱
	 		}
			paramMap.add("userType", 1);//内部员工
			paramMap.add("userId", employeeId);
			paramMap.add("userPhone", employeeQueryVo.getPhone());
			Md5Hash md5 = new Md5Hash("000000", "nantian-erp");
			paramMap.add("password",md5.toString());
			HttpEntity<MultiValueMap<String,Object >> request = new HttpEntity<MultiValueMap<String, Object>>(paramMap, requestHeaders); 
			ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request, RestResponse.class);
			RestResponse response = responseEntity.getBody();
			if(!"200".equals(response.getStatus())){
				logger.info("调用权限工程新增用户信息失败"+response.getMsg());
				return RestUtils.returnFailure("调用权限工程新增用户信息失败:"+response.getMsg());
			}			
			if(null == response.getData()|| "".equals(String.valueOf(response.getData()))) {
				logger.info("调用权限工程新增用户返回结果为空"+response.getMsg());
				return RestUtils.returnFailure("新增用户失败");
			}
			Integer id=(Integer)response.getData();//新增用户成功，返回用户编号

			//开通权限，给用户角色表user_role增加信息
			List<Map<String,Object>> userRoleList=new ArrayList<Map<String,Object>>();

			Map<String,Object> roleMap=new HashMap<String, Object>();
			roleMap.put("uId", id);
			roleMap.put("rId",3); //入职新员工默认开普通员工权限
			userRoleList.add(roleMap);
		
			String url2=protocolType+"nantian-erp-authentication/nantian-erp/userRole/insertUserRoleForHrList";
			MultiValueMap<String, Object> userRoleMap=new LinkedMultiValueMap<String, Object>();
			userRoleMap.add("userRoleList", userRoleList);
			HttpEntity<MultiValueMap<String, Object>> reqEntity=new HttpEntity<MultiValueMap<String,Object>>(userRoleMap,requestHeaders);
			ResponseEntity<RestResponse> res=restTemplate.postForEntity(url2, reqEntity, RestResponse.class);
			RestResponse resRole = res.getBody();
			if(!"200".equals(resRole.getStatus())){
				logger.info("调用权限工程新增角色信息失败"+resRole.getMsg());
				return RestUtils.returnFailure("调用权限工程新增角色信息失败:"+resRole.getMsg());
			}			
			if(null == resRole.getData()|| "".equals(String.valueOf(resRole.getData()))) {
				logger.info("调用权限工程新增角色返回结果为空"+resRole.getMsg());
				return RestUtils.returnFailure("新增角色失败");
			}
			
			//调用ERP-salary工程-给上岗工资单流程审批表增加一个待处理的上岗工资单
			String url3=protocolType+"nantian-erp-salary/nantian-erp/salary/payRollFlow/insertPayRollFlow";
			MultiValueMap<String, Object> flowMap = new LinkedMultiValueMap<String, Object>();
			userId = employeeMapper.findUserIdByDepartID(employeeQueryVo.getFirstDepartmentId());
			flowMap.add("status", 1);
			flowMap.add("userId", employeeId);//入职员工编号
			flowMap.add("periodIsLock", 0);
			flowMap.add("currentPersonID", userId);//当前处理人用户编号
			flowMap.add("firstDepartment", employeeQueryVo.getFirstDepartmentId());
			flowMap.add("secondDepartment", employeeQueryVo.getSecondDepartmentId());
			
			String contractBeginTime = employeeQueryVo.getContractBeginTime();
			
			String[]  str=contractBeginTime.split("-");
			String commitMonth = str[0]+'-'+str[1];	
			
			//实习生前端没有传递getProbationEndTime字段，此处需要判空处理
			String probationEndTime = employeeQueryVo.getProbationEndTime();
			String positiveMonth = null;
			
			if(probationEndTime !=null && probationEndTime != "") {
				str=probationEndTime.split("-");
				positiveMonth = str[0]+'-'+str[1];
			}

						
			String entryTime = employeeQueryVo.getEntryTime();//入职时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
			Calendar calendar = Calendar.getInstance();
			Date entryTimeDate = null;
			try {
				entryTimeDate = sdf.parse(entryTime);
				calendar.setTime(entryTimeDate);
			} catch (ParseException e) {
				logger.error("日期转换失败："+entryTime);
			}
			
			String socialSecMonth = null;//社保基数提交月份
			int day = calendar.get(Calendar.DATE);
			if(day>15) {//大于15号，算下个月
				calendar.add(Calendar.MONTH, 1);// 月份+1
				Date date = calendar.getTime();
				socialSecMonth = monthFormat.format(date);
			}else {
				socialSecMonth = monthFormat.format(entryTimeDate);
			}
			
			flowMap.add("commitMonth", commitMonth);//工资单提交月份
			flowMap.add("positiveMonth", positiveMonth);//工资单提交月份
			flowMap.add("socialSecMonth", socialSecMonth);//社保基数提交月份
			flowMap.add("beginTime", contractBeginTime);
			
			HttpEntity<MultiValueMap<String, Object>> reqEntity1 = new HttpEntity<MultiValueMap<String, Object>>(flowMap,requestHeaders);
			ResponseEntity<RestResponse> flowrRes = restTemplate.postForEntity( url3, reqEntity1 , RestResponse.class);
			RestResponse res1 = flowrRes.getBody();
			if(!"200".equals(res1.getStatus())){
				logger.info("修改入职流程表:添加部门经理ID失败"+res1.getMsg());
				return RestUtils.returnFailure("修改入职流程表:添加部门经理ID失败"+res1.getMsg());
			}			
			if(null == res1.getData()|| "".equals(String.valueOf(res1.getData()))) {
				logger.info("修改入职流程表:添加部门经理ID失败"+res1.getMsg());
				return RestUtils.returnFailure("修改入职流程表:添加部门经理ID失败");
			}
			
			//增加员工在职记录表
			ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
			employeeRecord.setEmployeeId(employeeId); 
			employeeRecord.setTime(employeeQueryVo.getEntryTime());
			employeeRecord.setContent("员工入职");
			employeeRecord.setProcessoer(user.getEmployeeName());
			employeeRecordMapper.insertEmployeeRecord(employeeRecord);		
			
			Integer postId = employeeQueryVo.getPostId();

			//在岗位记录表中插入记录
    		PositionOperRecond operRec = new PositionOperRecond();
    		Date date = new Date();
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
    		operRec.setCreateTime(format.format(date));
    		operRec.setOperContext("员工入职："+employeeQueryVo.getName()); //处理内容
    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
    		operRec.setCurrentPersonName(erpUser.getEmployeeName());//当前处理人Id
    		operRec.setPostId(postId);

    		operRecordMapper.addPositionOperReord(operRec);
    		
			//入职人数达到岗位上限，岗位自动关闭
			Integer  CountAllEntry =  employeeEntryMapper.selectCountAllEntry(postId);
			
			Map<String, Object> postMap = postMapper.findByPostId(postId);
		    if(postMap.containsKey("numberPeople")) {
		    	Integer numberPeople = (Integer) postMap.get("numberPeople");
		    	
		    	if (CountAllEntry.equals(numberPeople)){
			    	//入职人数达到岗位需求，关闭岗位
		    		ErpPost post = new ErpPost();
		    		post.setPostId(postId);
		    		post.setStatus(3); 
		    		postMapper.updatePost(post);
		    		
		    		//在岗位记录表中插入记录
		    		date = new Date(date.getTime()+1000);
		    		operRec.setCreateTime(format.format(date));
		    		operRec.setOperContext("入职人数达到岗位需求，自动关闭岗位"); //处理内容
		    		operRec.setCurrentPersonName("系统自动处理");//当前处理人Id		    		
		    		operRecordMapper.addPositionOperReord(operRec);
			    }
		    }
		    return RestUtils.returnSuccess(employeeId, "OK");
		} catch (Exception e) {
			logger.error("updateEmployee方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("更新失败："+e.getMessage());
		}
	}

	/**
	 * update by 张玉伟  20180917  修改项目组信息
	 * @param projectId
	 * @param username
	 * @param offerId
	 * @return
	 */
	@Transactional
	public String updateProjectInfo(Integer projectId, ErpUser erpUser, Integer offerId) {
		
		logger.info("updateProjectInfo方法开始执行，传递参数1：projectId:" + projectId + ",参数2:erpUser:" + erpUser.toString() + ",参数3:offerId:" + offerId);

		try {
			//通过offerID查询出员工表的一条员工记录信息
			Integer employeeId = employeeMapper.selectByOfferId(offerId);
			//更新员工表，将项目信息ID写入该条记录中
			ErpEmployee employee = new ErpEmployee();
			employee.setProjectInfoId(projectId);
			employee.setEmployeeId(employeeId);
			//employee.setPositionId(null);
			employeeMapper.updateEmployee(employee);
			//记录表
			ErpEntryRecord entryRecord = new ErpEntryRecord();
			entryRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			entryRecord.setContent("项目经理已处理");
			entryRecord.setOfferId(offerId);
			entryRecord.setProcessoer(erpUser.getEmployeeName());
			entryRecord.setProcessoerID(erpUser.getUserId());
			entryRecordMapper.insertEntryRecord(entryRecord);
			//删除流程表
			//employeeEntryMapper.deleteByOfferId(offerId);
			
			//更改流程表状态，通过offerID修改当前处理人为部门经理			
			ErpEmployeeEntry employeeEntry = new ErpEmployeeEntry();
			employeeEntry.setStatus(3);  //項目已分配
			employeeEntry.setOfferId(offerId);
			employeeEntryMapper.updateEmployeeEntryByOfferId(employeeEntry);
		} catch (Exception e) {
			 
			logger.info("updateProjectInfo方法出现异常：" + e.getMessage(),e);
		}
		return "OK";
	}

	/**
	 * update by 张玉伟  20180917
	 * @param resumeId
	 * @param offerId
	 * @param info
	 * @param username
	 * @return
	 */
	@Transactional
	public String cancelEntry( Integer resumeId, Integer offerId, String info,
			String username, Integer userId) {
		logger.info("cancelEntry方法开始执行，传递参数1：resumeId:" + resumeId + ",参数2:offerId:" + offerId + ",参数3:info:" + info + ",参数4:username" + username);
		
		try {
			// 更新offer
			ErpOffer offer = new ErpOffer();
			offer.setOfferId(offerId);
			offer.setStatus(DicConstants.CANCER_ENTRY_INVALID); //offer状态改为归档
			offer.setReason(info);
			offerMapper.updateOffer(offer);
			// 更新入职记录表
			ErpEntryRecord entryRecord = new ErpEntryRecord();
			entryRecord.setProcessoer(username);
			entryRecord.setOfferId(offerId);
			entryRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			entryRecord.setContent(info);
			entryRecordMapper.insertEntryRecord(entryRecord);
			//更新面试记录表
			ErpRecord erpRecord=new ErpRecord();
			erpRecord.setProcessor(username);
			erpRecord.setResumeId(resumeId);
			erpRecord.setContent(info);
			erpRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			erpRecord.setProcessorId(userId);
			this.erpRecordMapper.insertRecord(erpRecord);
			//删除流程表
//			employeeEntryMapper.deleteByOfferId(offerId);
			
			//更改流程表状态，通过offerID修改当前处理人为部门经理			
			ErpEmployeeEntry employeeEntry = new ErpEmployeeEntry();
			employeeEntry.setStatus(4);  //放弃入职
			employeeEntry.setOfferId(offerId);
			employeeEntryMapper.updateEmployeeEntryByOfferId(employeeEntry);
		} catch (Exception e) {
			 
			logger.info("cancelEntry方法出现异常：" + e.getMessage(),e);
		}
		return "OK";
	}

	/**
	 * 
	 * Description: 将所有基础权限列表与基础一起返回到前端，距入职时间3天内的所有有效offer信息
	 *
	 * @param username 用户名，token 角色信息
	 * @return
	 * @Author houhuirong
	 * @Update Date: 2018年10月15日
	 * @Update Date: 2018年10月18日
	 */
	public Map<String, Object> findByRole(ErpUser erpUser) {
		
		logger.info("findByRole方法开始执行，传递参数:erpUser" + erpUser.toString());
		Map<String, Object> queryHashMap = new HashMap<String, Object>();
		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();	
		try {
			List<Integer> roles=erpUser.getRoles();	
			if(roles.size()!=0){
				Map<String,Object> queryMap=new HashMap<String, Object>();	
				//查询所有待我处理
				queryMap.put("currentPersonID", erpUser.getUserId());			
				list=this.employeeEntryMapper.findAllWaitingForMe(queryMap);
				
				if(roles.contains(1)){//hr可以看到所有部门的待入职
					Map<String,Object> hrQueryMap=new HashMap<String, Object>();	
					hrQueryMap.put("hrFlag", true);
					list.addAll(this.employeeEntryMapper.findAllWaitingForMe(hrQueryMap));
				}
				queryHashMap.put("waitEntryList", list);
			}
		} catch (Exception e) {
			 
			logger.info("findByRole方法出现异常：" + e.getMessage());
		}
		return queryHashMap;
	}

	/**
	 * update by 张玉伟  20180917  通过offerId按照时间倒序查询入职记录信息
	 * @param offerId
	 * @return
	 */
	public List<ErpEntryRecord> findRecord(Integer offerId) {
		logger.info("findRecord方法开始执行，传递参数:offerId" + offerId);
		List<ErpEntryRecord> list = null;
		try {
			list = entryRecordMapper.findByOfferId(offerId);
		} catch (Exception e) {
			 
			logger.info("findRecord方法出现异常：" + e.getMessage());
		}
		return list;
	}

	public List<Object> findAllBaseRole(String token) {
		logger.info("查询所有基础角色方法开始执行");
		List<Object> resultRoleList=new ArrayList<Object>();
		new ArrayList<Map<String,Object>>();
		try{
			//已通过接口和角色权限验证，调用ERP-权限工程操作层服务接口-获取所有基础权限 add by hhr start
			String url=protocolType+"nantian-erp-authentication/nantian-erp/authentication/role/findAllBaseRole";
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token",token);//封装token
			HttpEntity<String> requestEntity=new HttpEntity<String>(null,requestHeaders);
			ResponseEntity<String> response=restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
			String resultStr=response.getBody();		//解析数据
			JSONObject resultJson=JSON.parseObject(resultStr);
			if(null == resultJson.get("data") || "".equals(String.valueOf(resultJson.get("data")))) {
				logger.info("访问authentication工程接口,返回msg为:"+resultJson.get("msg"));
				return resultRoleList;
			}
			resultRoleList=resultJson.getJSONArray("data");
		}catch(Exception e){
			 
			logger.info("findAllBaseRole方法出现异常："+e.getMessage(),e);
		}
		return resultRoleList;
	}
	/**
	 * Description: 新增入职--系统上线过渡期使用
	 *
	 * @return
	 * @Author Zhangqian
	 * @Create Date: 2019年01月29日 下午15:08:28
	 */
	@Transactional
	public RestResponse addEntry(String token,Map<String,Object> params) {
		logger.info("addEntrys方法开始执行，参数是：token="+token+","+params);
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			
			/*
			 * 接收前端传递过来的参数
			 */
			Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));
			Integer postId = Integer.valueOf(String.valueOf(params.get("postId")));
			String entryTime = String.valueOf(params.get("entryTime"));//入职时间
			Integer position = Integer.valueOf(String.valueOf(params.get("position")));//职位
			Boolean isTrainee = Boolean.valueOf(String.valueOf(params.get("isTrainee")));//实习生
			Integer rank = Integer.valueOf(String.valueOf(params.get("rank")));//职级
			String socialSecurityPlace = String.valueOf(params.get("socialSecurityPlace"));//社保地
			Integer probationPeriod = Integer.valueOf(String.valueOf(params.get("probationPeriod")));//试用期期限
			Integer contractPeriod = Integer.valueOf(String.valueOf(params.get("contractPeriod")));//合同期限
			
			//修改简历状态
			ErpResume resume = new ErpResume();
			resume.setResumeId(resumeId);
			resume.setIsValid(false);
			resume.setStatus(DicConstants.RESUME_STATUS_SENT_OFFER);
			resumeMapper.updateResume(resume);
			
			//新增面试表
			ErpResumePost resumePost = new ErpResumePost();
			resumePost.setIsValid(false);//流程是否有效
			resumePost.setPersonId(erpUser.getUserId());  //处理人为岗位申请人
			resumePost.setPostId(postId);
			resumePost.setResumeId(resumeId);
			resumePost.setStatus(DicConstants.INTERVIEW_STATUS_PASS);
			if(isTrainee) {
				resumePost.setSegment(DicConstants.INTERVIEW_SEGMENT_TRAINEE_INTERVIEW);//实习生
			}else {
				resumePost.setSegment(DicConstants.INTERVIEW_SEGMENT_SOCIAL_REEXAM);//初试
			}
			resumePostMapper.insertResumePost(resumePost);
			
			//新增offer表
			ErpOffer offer = new ErpOffer();
			offer.setInterviewId(resumePost.getId());
			offer.setEntryTime(entryTime);
			if(isTrainee) {				
				ErpPositionRankRelation erpPositionRankRelation = resumePostMapper.findTraineePositionRankList();
				position = erpPositionRankRelation.getPositionNo();//实习生职位ID
				rank = erpPositionRankRelation.getRank();//实习生职级ID							
			}else{
				offer.setSocialSecurityPlace(socialSecurityPlace);
				offer.setProbationPeriod(probationPeriod);
				offer.setContractPeriod(contractPeriod);
			}
			
			offer.setPosition(position);			
			offer.setRank(rank);
			
			offer.setStatus(DicConstants.OFFER_STATUS_VALID);
			offerMapper.insertOffer(offer);
			
			//新增员工入职流程表
			ErpEmployeeEntry employeeEntry = new ErpEmployeeEntry();
			employeeEntry.setRoleID(1);
			employeeEntry.setStatus(1);
			employeeEntry.setOfferId(offer.getOfferId());
			Integer getUserId = erpUser.getUserId();//从用户信息中获取用户名
			employeeEntry.setCurrentPersonID(getUserId);
			employeeEntryMapper.insertEmployeeEntry(employeeEntry);

			ErpRecord record = new ErpRecord();
			record.setResumeId(resumeId);
			record.setTime(ExDateUtils.getCurrentStringDateTime());
			record.setContent("已入职");
			record.setProcessor(erpUser.getEmployeeName());
			record.setProcessorId(erpUser.getUserId());
			recordMapper.insertRecord(record);
			return RestUtils.returnSuccess("新增成功！");
		} catch (Exception e) {
			logger.error("addEntry方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！"+e.getMessage());
		}
	}
	

}
