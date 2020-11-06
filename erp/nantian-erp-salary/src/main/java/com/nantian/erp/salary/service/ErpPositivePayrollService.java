package com.nantian.erp.salary.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.dao.*;
import com.nantian.erp.salary.data.model.*;
import com.nantian.erp.salary.util.RestTemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Maps;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.vo.ParamSalaryPositiveVo;
import com.nantian.erp.salary.util.AesUtils;

/**
 * 转正-上岗工资单  service 层
 * @author caoxb
 * @date 2018-09-14
 */
@Service
@PropertySource(value= {"classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpPositivePayrollService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${protocol.type}")
    private String protocolType;//http或https
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired 
	private ErpPositivePayrollMapper erpPositivePayrollMapper;
	
	@Autowired
	private ErpPositiveRecordMapper erpPositiveRecordMapper;
	@Autowired
	private ErpPayRollFlowMapper erpPayRollFlowMapper;
	@Autowired
	private StringRedisTemplate stringRedisTemplate; 
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired 
	private ErpTalkSalaryMapper erpTalkSalaryMapper;
	@Autowired
	private ErpPositiveSalaryMapper erpPositiveSalaryMapper;
	@Autowired
	private ErpBasePayrollMapper erpBasePayrollMapper;
	@Autowired
	private ErpPeriodPayrollMapper erpPeriodPayrollMapper;
	@Autowired
	private ErpSalaryMonthPerformanceService erpSalaryMonthPerformanceService;
	@Autowired
	private RestTemplateUtils restTemplateUtils;
	@Autowired
	private ErpBasePayrollUpdateRecordMapper erpBasePayrollUpdateRecordMapper;
	/*
	 * 查询 转正-待我处理上岗工资单
	 * 参数：token
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findAllPayrollForMe(String token) {
		
		logger.info("进入 查询 转正-待我处理上岗工资单-列表  参数token: " + token);		
		List<Map<String, Object>> returnList = new ArrayList<>();//返回结果
		Boolean flag=false;//解密标志
		try {
			ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);//登录用户信息
			Map<String, Object> queryMap=new HashMap<String, Object>();
			queryMap.put("currentPersonID", erpUser.getUserId());//当前登录人用户编号
			queryMap.put("status", 2);//转正未处理
			List<Map<String, Object>> UserList=this.erpPayRollFlowMapper.findUserIdsByCurrentPerID(queryMap);//当前处理人与登录人一致，查询未审批用户IDs	
			String queryFlag = "wait";
			if(UserList.size()>0){
				returnList=allPositivePayroll(UserList,token,flag,queryFlag);
			}
		} catch (Exception e) {
			logger.error("查询 转正-待我处理上岗工资单-列表 findAllPayrollForMe 出现异常:"+e.getMessage(),e);
		}
		logger.info("调用人力nantian-erp-hr工程返回员工记录条数："+returnList.size());
		return returnList;
	}
	
	/*
	 * 转正-所有上岗工资单
	 * 参数：null
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findAllPayroll(String token,String startMonth,String endMonth) {
		
		logger.info("进入 查询转正-所有上岗工资单 参数token : " + token);		
		List<Map<String, Object>> returnList = new ArrayList<>();//返回结果
		List<Map<String, Object>> returnTempList = new ArrayList<Map<String,Object>>();

		Boolean flag=false;//解密标志		
		try {
			ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);//登录用户
			List<Integer> roles=erpUser.getRoles();//登录用户角色
			Map<String, Object> queryMap=new HashMap<String, Object>();
			
			if(roles.contains(8) || roles.contains(7)){//总经理可以看到所有部门的待入职
				queryMap.put("managerId", erpUser.getUserId());
			}else if(roles.contains(9)){	//副总经理
				queryMap.put("superLeaderId", erpUser.getUserId());
			}
			else if(roles.contains(2)){//一级部门经理角色
				queryMap.put("currentPersonID", erpUser.getUserId());
			}
			else{
				return returnList;
			}
			
			//按起止月份查询
			queryMap.put("startMonth", startMonth);
			queryMap.put("endMonth", endMonth);
			List<Map<String,Object>> UserList=this.erpPayRollFlowMapper.findAllPositivePayRoll(queryMap);
			if(UserList.size()>0){
				String queryFlag = "all";
				returnList=allPositivePayroll(UserList,token,flag,queryFlag);
			}
		} catch (Exception e) {
			logger.error("查询转正-所有上岗工资单 方法findAllPayroll 出现异常："+e.getMessage(),e);
		}
		logger.info("调用人力nantian-erp-hr工程返回员工记录条数："+returnList.size());
		return returnList;
	}
	
	
	/*
	 * 转正-新增转正-上岗工资单
	 * 参数：erpPayroll
	 */
	@Transactional(rollbackFor = Exception.class)
	public String insertErpPayroll(ParamSalaryPositiveVo paramSalaryPositiveVo,String token) {
		logger.info("进入 转正-新增上岗工资单 方法 参数 封装对象  : " + paramSalaryPositiveVo.toString());
		String str = null;
		Boolean flag=true;//加密标识
		try {
			ErpPositivePayroll erpPositivePayroll = paramSalaryPositiveVo.getErpPositivePayroll();
			Integer empId=erpPositivePayroll.getErpEmployeeId();
			ErpPositivePayroll PositiveEmpId=this.erpPositivePayrollMapper.selectOnePositivePayroll(empId);
			if(PositiveEmpId==null){
				//新增转正工资单			
				ErpPositivePayroll encryptPositivePayroll=encryptAndDecryptPositivePayroll(flag,erpPositivePayroll);
				this.erpPositivePayrollMapper.insert(encryptPositivePayroll);
				
				//add by ZhangYuWei 20190128  新增转正 记录
				ErpPositiveRecord erpPositiveRecord = paramSalaryPositiveVo.getErpPositiveRecord();
				//通过token从redis缓存中获取用户信息
				ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
				String employeeName = userInfo.getEmployeeName();//员工姓名
				String username = userInfo.getUsername();//用户名
				String erpPositiveHandler = employeeName==null||"".equals(employeeName)?username:employeeName;//新增月度绩效的操作人
				erpPositiveRecord.setErpPositiveHandler(erpPositiveHandler);
				erpPositiveRecord.setPositiveTime(ExDateUtils.getCurrentStringDateTime());
				erpPositiveRecord.setContent("新增转正工资单");
				erpPositiveRecordMapper.insertPositiveRecord(erpPositiveRecord);
				
				//变更上岗工资流程审批表中状态
				ErpPayRollFlow erpPayRollFlow=new ErpPayRollFlow();
				erpPayRollFlow.setStatus(3);//转正工资单已处理
				erpPayRollFlow.setUserId(empId);
				erpPayRollFlow.setPeriodIsLock(1);//锁定上岗工资单
				this.erpPayRollFlowMapper.updatePayRollFlow(erpPayRollFlow);
				str = "新增成功！";
	
				//更新基础薪资表
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date postiveDate = format.parse(paramSalaryPositiveVo.getProbationEndTime()); 
				Date timeNow = ExDateUtils.getCurrentDateTime();
				
				if (postiveDate.getTime() <= timeNow.getTime()){
					//如果转正工资单修改时已经转正，则将转正工资直接写入基本薪资表
					ErpBasePayroll erpBasePayroll=paramSalaryPositiveVo.getErpBasePayroll();
					ErpBasePayroll encryptBasePayroll=encryptAndDecryptBasePayroll(flag,erpBasePayroll);
					encryptBasePayroll.setErpEmployeeId(empId);
					this.erpBasePayrollMapper.updateBasePayroll(encryptBasePayroll);
				}
			}else{
				return "该人员已录入转正工资单,请勿重复录入！";
			}
		} catch (Exception e) {
			logger.error("转正-新增上岗工资单 方法insertErpPayroll 出现异常："+e.getMessage(),e);
			//str = "新增失败 ！";
		}
		return str;
	}
	
	/*
	 * 招聘-修改转正-上岗工资单
	 * 参数：erpPayroll
	 */
	@Transactional
	public RestResponse updateErpPayroll(ParamSalaryPositiveVo paramSalaryPositiveVo,String token) {
		logger.info("转正-修改上岗工资单 参数 员工ID : " + paramSalaryPositiveVo.getErpPositivePayroll().getErpEmployeeId().toString());
		String str = null;
		Boolean flag=true;//加密标识
		try {
			Integer empId=paramSalaryPositiveVo.getErpPositivePayroll().getErpEmployeeId();
			//转正工资单
			ErpPositivePayroll erpPositivePayroll = paramSalaryPositiveVo.getErpPositivePayroll();
			ErpPositivePayroll encryptPositivePayroll=encryptAndDecryptPositivePayroll(flag,erpPositivePayroll);
			this.erpPositivePayrollMapper.updateById(encryptPositivePayroll);
			
			//add by ZhangYuWei 20190128  新增转正 记录
			ErpPositiveRecord erpPositiveRecord = paramSalaryPositiveVo.getErpPositiveRecord();
			//通过token从redis缓存中获取用户信息
			ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
			String employeeName = userInfo.getEmployeeName();//员工姓名
			String username = userInfo.getUsername();//用户名
			String erpPositiveHandler = employeeName==null||"".equals(employeeName)?username:employeeName;//新增月度绩效的操作人
			erpPositiveRecord.setErpPositiveHandler(erpPositiveHandler);
			erpPositiveRecord.setPositiveTime(ExDateUtils.getCurrentStringDateTime());
			erpPositiveRecord.setContent("修改转正工资单");
			erpPositiveRecordMapper.insertPositiveRecord(erpPositiveRecord);
			str="修改成功";
			
			//更新基础薪资表
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date postiveDate = format.parse(paramSalaryPositiveVo.getProbationEndTime()); 
			Date timeNow = ExDateUtils.getCurrentDateTime();
			
			if (postiveDate.getTime() <= timeNow.getTime()){
				//如果转正工资单修改时已经转正，则将转正工资直接写入基本薪资表
				ErpBasePayroll erpBasePayroll=paramSalaryPositiveVo.getErpBasePayroll();
				ErpBasePayroll encryptBasePayroll=encryptAndDecryptBasePayroll(flag,erpBasePayroll);
				this.erpBasePayrollMapper.updateBasePayroll(encryptBasePayroll);
			}
		} catch (Exception e) {
			logger.error("转正-修改上岗工资单 方法updateErpPayroll 出现异常："+ e.getMessage(),e);
			return RestUtils.returnFailure("修改失败 ！");
		}
		return RestUtils.returnSuccessWithString(str);
	}
	
	/*
	 * 调用人力资源接口去修改员工状态
	 * 参数：erpEmployeeId
	 */
	private String updateEmpStatus(Integer erpEmployeeId) {
		logger.info("进入调用人力资源接口去修改员工状态 方法  参数员工ID "+erpEmployeeId);
		String retrunString = null;
		try {
			//调用nantian-erp-hr工程  去更新员工状态
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/processedPayroll";
			Map<String, Object> tempParam = Maps.newHashMap();
			tempParam.put("payrollType", "positive");
			tempParam.put("employeeId", erpEmployeeId);
			RestResponse response = this.restTemplate.postForObject(url, tempParam, RestResponse.class);
			retrunString = (String) response.getData();
		} catch (Exception e) {
			logger.error("调用人力资源接口去修改员工状态 方法updateEmpStatus 出现错误"+e.getMessage(),e);
		}
		return retrunString;
	}
	
	/**
	 * Description: 测试加密
	 *
	 * @return
	 * @Author HouHuiRong
	 * @Create Date: 2018年11月12日 下午10:31:01
	 */
	public String encryptDataRsa(String salary) {
		String result=null;
		String defaultSalaryValue=AesUtils.encrypt(String.valueOf(0.0));
		if(salary==null){
			result=defaultSalaryValue;
		}else{
			result = AesUtils.encrypt(String.valueOf(salary));
		}
		return result;
	}
	
	/**
	 * Description: 测试解密
	 *
	 * @return
	 * @Author HouHuiRong
	 * @Create Date: 2018年11月12日 下午10:31:01
	 */
	public String decryptDataRsa(String salary) {
		String result="";
		if(salary==null){
			return result;
		}else{
		result = String.valueOf(AesUtils.decrypt(salary));
		}
		return result;
	}
	
	public ErpPositiveSalary encryptAndDecryptPositiveSalary(Boolean flag,ErpPositiveSalary erpPositiveSalary){
		if(erpPositiveSalary!=null){
		if(flag){//加密
			erpPositiveSalary.setErpPositiveBaseWage(encryptDataRsa(erpPositiveSalary.getErpPositiveBaseWage()));
			erpPositiveSalary.setErpPositivePostWage(encryptDataRsa(erpPositiveSalary.getErpPositivePostWage()));
			erpPositiveSalary.setErpPositivePerformance(encryptDataRsa(erpPositiveSalary.getErpPositivePerformance()));
			erpPositiveSalary.setErpPositiveAllowance(encryptDataRsa(erpPositiveSalary.getErpPositiveAllowance()));
			erpPositiveSalary.setErpPositiveIncome(encryptDataRsa(erpPositiveSalary.getErpPositiveIncome()));
			erpPositiveSalary.setErpTelFarePerquisite(encryptDataRsa(erpPositiveSalary.getErpTelFarePerquisite()));
		}else{//解密
			erpPositiveSalary.setErpPositiveBaseWage(decryptDataRsa(erpPositiveSalary.getErpPositiveBaseWage()));
			erpPositiveSalary.setErpPositivePostWage(decryptDataRsa(erpPositiveSalary.getErpPositivePostWage()));
			erpPositiveSalary.setErpPositivePerformance(decryptDataRsa(erpPositiveSalary.getErpPositivePerformance()));
			erpPositiveSalary.setErpPositiveAllowance(decryptDataRsa(erpPositiveSalary.getErpPositiveAllowance()));
			erpPositiveSalary.setErpPositiveIncome(decryptDataRsa(erpPositiveSalary.getErpPositiveIncome()));	
			erpPositiveSalary.setErpTelFarePerquisite(decryptDataRsa(erpPositiveSalary.getErpTelFarePerquisite()));
		}
		}
		return erpPositiveSalary;
	}
	
	public ErpBasePayroll encryptAndDecryptBasePayroll(Boolean flag,ErpBasePayroll erpBasePayroll){
		if(erpBasePayroll!=null){
		if(flag){//加密
			erpBasePayroll.setErpBaseWage(encryptDataRsa(erpBasePayroll.getErpBaseWage()));
			erpBasePayroll.setErpPostWage(encryptDataRsa(erpBasePayroll.getErpPostWage()));
			erpBasePayroll.setErpPerformance(encryptDataRsa(erpBasePayroll.getErpPerformance()));
			erpBasePayroll.setErpAllowance(encryptDataRsa(erpBasePayroll.getErpAllowance()));
			erpBasePayroll.setErpSocialSecurityBase(encryptDataRsa(erpBasePayroll.getErpSocialSecurityBase()));
			erpBasePayroll.setErpAccumulationFundBase(encryptDataRsa(erpBasePayroll.getErpAccumulationFundBase()));
			erpBasePayroll.setErpTelFarePerquisite(encryptDataRsa(erpBasePayroll.getErpTelFarePerquisite()));
		}else{//解密
			erpBasePayroll.setErpBaseWage(decryptDataRsa(erpBasePayroll.getErpBaseWage()));
			erpBasePayroll.setErpPostWage(decryptDataRsa(erpBasePayroll.getErpPostWage()));
			erpBasePayroll.setErpPerformance(decryptDataRsa(erpBasePayroll.getErpPerformance()));
			erpBasePayroll.setErpAllowance(decryptDataRsa(erpBasePayroll.getErpAllowance()));
			erpBasePayroll.setErpSocialSecurityBase(decryptDataRsa(erpBasePayroll.getErpSocialSecurityBase()));
			erpBasePayroll.setErpAccumulationFundBase(decryptDataRsa(erpBasePayroll.getErpAccumulationFundBase()));
			erpBasePayroll.setErpTelFarePerquisite(decryptDataRsa(erpBasePayroll.getErpTelFarePerquisite()));
		}
		}
		return erpBasePayroll;
	}
	
	public ErpPositivePayroll encryptAndDecryptPositivePayroll(Boolean flag,ErpPositivePayroll erpPositivePayroll){
		if(erpPositivePayroll!=null){
		if(flag){//加密
			erpPositivePayroll.setErpPositiveBaseWage(encryptDataRsa(erpPositivePayroll.getErpPositiveBaseWage()));
			erpPositivePayroll.setErpPositivePostWage(encryptDataRsa(erpPositivePayroll.getErpPositivePostWage()));
			erpPositivePayroll.setErpPositivePerformance(encryptDataRsa(erpPositivePayroll.getErpPositivePerformance()));
			erpPositivePayroll.setErpPositiveAllowance(encryptDataRsa(erpPositivePayroll.getErpPositiveAllowance()));
			erpPositivePayroll.setErpPositiveIncome(encryptDataRsa(erpPositivePayroll.getErpPositiveIncome()));
			erpPositivePayroll.setErpTelFarePerquisite(encryptDataRsa(erpPositivePayroll.getErpTelFarePerquisite()));
		}else{//解密
			erpPositivePayroll.setErpPositiveBaseWage(decryptDataRsa(erpPositivePayroll.getErpPositiveBaseWage()));
			erpPositivePayroll.setErpPositivePostWage(decryptDataRsa(erpPositivePayroll.getErpPositivePostWage()));
			erpPositivePayroll.setErpPositivePerformance(decryptDataRsa(erpPositivePayroll.getErpPositivePerformance()));
			erpPositivePayroll.setErpPositiveAllowance(decryptDataRsa(erpPositivePayroll.getErpPositiveAllowance()));
			erpPositivePayroll.setErpPositiveIncome(decryptDataRsa(erpPositivePayroll.getErpPositiveIncome()));
			erpPositivePayroll.setErpTelFarePerquisite(decryptDataRsa(erpPositivePayroll.getErpTelFarePerquisite()));
		}
		}
		return erpPositivePayroll;
	}
	
	public List<Map<String,Object>> allPositivePayroll(List<Map<String,Object>> list,String token,Boolean flag,String queryFlag){
		logger.info("转正工资单 allPeriodPayroll开始执行，参数为list:"+list);
		List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
		try{
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoById";
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token",token);//将token放到请求头中
			HttpEntity<List<Map<String,Object>>> request = new HttpEntity<>(list, requestHeaders);
			
			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
			if(response.getStatusCodeValue() != 200){
				logger.info("转正工资单查询员工信息，hr工程返回错误");
				return mapList;
			}
			
			mapList = (List<Map<String, Object>>) response.getBody().get("data");
			for(int i=0;i<mapList.size();i++){
				Map<String,Object> userInfoMap=mapList.get(i);
				String status = String.valueOf(userInfoMap.get("status"));
				if (queryFlag.equals("wait") && status.equals("4")) {//查询待我处理的转正工资单去除已离职的
					mapList.remove(userInfoMap);
					i--;
					continue;
				}
		      	//根据offerID查询员工的面试谈薪
				Integer offerId = Integer.valueOf(String.valueOf(userInfoMap.get("offerId")==null?0:userInfoMap.get("offerId")));
				Boolean isTrainee = Boolean.valueOf(String.valueOf(userInfoMap.get("isTrainee")));//是否是实习生
				Integer employeeId=Integer.valueOf(String.valueOf(userInfoMap.get("employeeId")));
				for(Map<String,Object> confimMap:list){//返回该员工工资单是否被确认
					Integer userId=Integer.valueOf(String.valueOf(confimMap.get("userId")));
					if(userId==employeeId){
						userInfoMap.put("isConfirmed", confimMap.get("isConfirmed"));
						break;
					}
				}
				if(isTrainee==false){//社招生
					ErpTalkSalary erpTalkSalary = this.erpTalkSalaryMapper.findOneByOfferId(offerId);
					if (erpTalkSalary != null){
						erpTalkSalary.setAccumulationFundBase(decryptDataRsa(erpTalkSalary.getAccumulationFundBase()));
						erpTalkSalary.setMonthIncome(decryptDataRsa(erpTalkSalary.getMonthIncome()));
						erpTalkSalary.setSocialSecurityBase(decryptDataRsa(erpTalkSalary.getSocialSecurityBase()));
					}
					else{
						erpTalkSalary = new ErpTalkSalary();
					}
					userInfoMap.put("erpTalkSalaryInfo", erpTalkSalary);
					userInfoMap.put("entryStatus", userInfoMap.get("status"));//是否转正状态
					userInfoMap.put("positiveIsLock", userInfoMap.get("positiveIsLock")==null?0:userInfoMap.get("positiveIsLock"));//转正是否锁定

					//上岗工资单中的转正薪资
					ErpPositiveSalary erpPositiveSalary=this.erpPositiveSalaryMapper.findPositiveSalaryByEmpId(employeeId);
					ErpPositiveSalary decryptPositiveSalary=encryptAndDecryptPositiveSalary(flag,erpPositiveSalary);
					userInfoMap.put("erpPositiveSalaryInfo", decryptPositiveSalary);
					//转正工资单
					ErpPositivePayroll erpPositivePayroll=this.erpPositivePayrollMapper.selectOnePositivePayroll(employeeId);
					if (erpPositivePayroll != null){							
						ErpPositivePayroll decryptPositivePayroll=encryptAndDecryptPositivePayroll(flag,erpPositivePayroll);
						userInfoMap.put("erpPositivePayrollInfo", decryptPositivePayroll);
					}
						
					//returnList.add(userInfoMap);
				}				
			}
		}catch(Exception e){
			logger.error("转正工资单 allPositivePayroll 出现异常 ："+ e.getMessage(),e);
		}
		return mapList;
	}
	
	/**
	 * Description: 根据员工ID查询上转正工资单的操作记录
	 * 
	 * @param  firstDepartmentId 员工Id
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月28日 下午16:29:06
	 */
	public RestResponse findPayrollRecord(Integer employeeId) {
		logger.info("进入findPayrollRecord方法，参数是：employeeId="+employeeId);
		try {
			List<ErpPositiveRecord> recordList = erpPositiveRecordMapper.selectPositiveRecord(employeeId);
			return RestUtils.returnSuccess(recordList);
		} catch (Exception e) {
			logger.error("方法findPayrollRecord出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致查询失败！");
		}
	}
	
	/**
	 * Description: 转正工资单超过提交月份的次月72小时后系统自动处理，自动修改状态并锁定，不可修改
	 * 每月4号0点5分自动锁定上个月未处理的转正工资单
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2019年7月3日
	 */
	@Transactional
	public void automaticLockScheduler() {
		logger.info("进入automaticLockScheduler方法，无参数");
		Calendar dateObj = Calendar.getInstance();
		dateObj.add(Calendar.MONTH, -1);//上一个月
		int year = dateObj.get(Calendar.YEAR);
		int month = dateObj.get(Calendar.MONTH)+1;
		String startMonth = null;//转正工资单提交月份（开始）
		if(month<10) {
			startMonth = year+"-0"+month;
		}else {
			startMonth = year+"-"+month;
		}
		String endMonth = null;//转正工资单提交月份（结束）
		if(month<10) {
			endMonth = year+"-0"+month;
		}else {
			endMonth = year+"-"+month;
		}
		try{
		// 按起止月份查询状态为2的数据
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("startMonth", startMonth);
		queryMap.put("endMonth", endMonth);
		List<Map<String, Object>> employeeList = erpPayRollFlowMapper.findAllPositiveWaitapprove(queryMap);
		for(Map<String,Object> employee : employeeList) {
			
			//如果为空，继续跳出本次循环	SXG 2019-08-11
			if (employee == null) {
				continue;
			}
			
			Integer employeeId = (Integer) employee.get("userId");// 员工ID
			ErpPayRollFlow erpPayRollFlow = new ErpPayRollFlow();
			erpPayRollFlow.setUserId(employeeId);
			erpPayRollFlow.setPeriodIsLock(1);//上岗工资单已锁定
			erpPayRollFlow.setPositiveIsLock(1);// 转正工资单已锁定
			erpPayRollFlow.setStatus(4);// 转正工资单系统自动处理
			erpPayRollFlowMapper.updatePayRollFlow(erpPayRollFlow);

			//给转正薪资赋值 2019-08-12 SXG Add  Begin
			ErpBasePayroll erpBasePayrolls = new ErpBasePayroll();
			ErpPositiveSalary erpPositiveSalary=this.erpPositiveSalaryMapper.findPositiveSalaryByEmpId(employeeId);
			ErpPositiveSalary decryptPositiveSalary=encryptAndDecryptPositiveSalary(false,erpPositiveSalary);
			ErpPositivePayroll erpPositivePayroll = erpPositivePayrollMapper.selectOnePositivePayroll(employeeId);
			
			if(erpPositivePayroll != null) {
				// do nothing
			}else {
				//从上岗工资单的转正薪资中获取
				erpPositivePayroll = new ErpPositivePayroll();
				erpPositivePayroll.setErpEmployeeId(employeeId);
				String income = decryptPositiveSalary.getErpPositiveIncome();//月度收入
				String basewage = decryptPositiveSalary.getErpPositiveBaseWage();//基本工资
				String postwage = decryptPositiveSalary.getErpPositivePostWage();//岗位工资
				String performance = decryptPositiveSalary.getErpPositivePerformance();//月度绩效
				String allowance = decryptPositiveSalary.getErpPositiveAllowance();//项目津贴
				String telfare = decryptPositiveSalary.getErpTelFarePerquisite();//话费补助
				
				if (basewage != null ||basewage != "") {
					erpPositivePayroll.setErpPositiveBaseWage(encryptDataRsa(basewage));
					erpBasePayrolls.setErpBaseWage(encryptDataRsa(basewage));
				}
				if (postwage != "" || postwage != null) {
					erpPositivePayroll.setErpPositivePostWage(encryptDataRsa(postwage));
					erpBasePayrolls.setErpPostWage(encryptDataRsa(postwage));//岗位工资
				}

				if (performance != "" || performance != null) {
					erpPositivePayroll.setErpPositivePerformance(encryptDataRsa(performance));
					erpBasePayrolls.setErpPerformance(encryptDataRsa(performance));//月度绩效
				}
				
				if (income != "" || income != null) {
					erpPositivePayroll.setErpPositiveIncome(encryptDataRsa(income));
				}
				
				if (allowance != "" || allowance != null) {
					erpPositivePayroll.setErpPositiveAllowance(encryptDataRsa(allowance));
					erpBasePayrolls.setErpAllowance(encryptDataRsa(allowance));//月度项目津贴
				}
				
				if (telfare != "" || telfare != null) {
					erpPositivePayroll.setErpTelFarePerquisite(encryptDataRsa(telfare));
					erpBasePayrolls.setErpTelFarePerquisite(encryptDataRsa(telfare));
				}
				
				
				//从基础薪资表中获取社保和公积金
				ErpBasePayroll erpBasePayroll=this.erpBasePayrollMapper.findBasePayrollDetailByEmpId(employeeId);
				if( erpBasePayroll != null) {
					erpPositivePayroll.setErpSocialSecurityIndex(encryptDataRsa(erpBasePayroll.getErpSocialSecurityBase()));
					erpPositivePayroll.setErpAccumulationFundIndex(encryptDataRsa(erpBasePayroll.getErpAccumulationFundBase()));
				}else {
					this.logger.info("user: " + employeeId + " 基础薪资表不存在！");
				}
				//自动锁定时新增转正工资单
				erpPositivePayrollMapper.insert(erpPositivePayroll);
				//更新基础新增  2019-08-13 gaolp
				erpBasePayrolls.setErpEmployeeId(employeeId);//员工ID
				erpBasePayrollMapper.updateBasePayroll(erpBasePayrolls);

				/*
				 * 将该员工的修改信息加入日志中
				 */
				ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
				basePayrollUpdateRecord.setEmployee(null);// 被修改的员工
				basePayrollUpdateRecord.setEmployeeId(employeeId);
				basePayrollUpdateRecord
						.setProcessor("自动锁定上个月未处理的转正工资单");// 修改人
				basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
				basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
				this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);
			}
			// 转正工资单日志的打印
			ErpPositiveRecord erpPositiveRecord = new ErpPositiveRecord();
			erpPositiveRecord.setErpEmployeeId(employeeId);
			erpPositiveRecord.setErpPositiveHandler("定时器");
			erpPositiveRecord.setPositiveTime(ExDateUtils.getCurrentStringDateTime());
			erpPositiveRecord.setContent("转正工资单锁定成功");
			erpPositiveRecordMapper.insertPositiveRecord(erpPositiveRecord);
		}
	}catch (Exception e) {
		logger.error("automaticLockScheduler自动锁定发送异常:"+e.getMessage(),e);
	}
	}
	
}
