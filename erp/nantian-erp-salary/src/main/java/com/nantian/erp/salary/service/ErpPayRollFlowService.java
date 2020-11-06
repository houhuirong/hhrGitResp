package com.nantian.erp.salary.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.dao.*;
import com.nantian.erp.salary.data.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.util.AesUtils;

/**
 * Description: 上岗工资单审批流程service
 *
 * @author HouHuiRong
 * @version 1.0
 * 
 *          <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月06日      		HouHuiRong          1.0
 *          </pre>
 */
@Service
@PropertySource(value = { "classpath:config/sftp.properties", "file:${spring.profiles.path}/config/sftp.properties",
		"classpath:config/email.properties", "file:${spring.profiles.path}/config/email.properties",
		"classpath:config/host.properties",
		"file:${spring.profiles.path}/config/host.properties" }, ignoreResourceNotFound = true)
public class ErpPayRollFlowService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${protocol.type}")
	private String protocolType;// http或https
	@Autowired
	private ErpPayRollFlowMapper erpPayRollFlowMapper;
	@Autowired
	private ErpPositivePayrollMapper erpPositivePayrollMapper;
	@Autowired
	private ErpBasePayrollMapper erpBasePayrollMapper;
	@Autowired
	private ErpSalaryMonthPerformanceService erpSalaryMonthPerformanceService;
	@Autowired
	private ErpPositiveConfirMapper erpPositiveConfirMapper;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private ErpSalaryAdjustMapper erpSalaryAdjustMapper;

	public RestResponse insertPayRollFlow(Map<String, Object> map) {
		logger.info("进入insertPayRollFlow新增上岗工资单流程审批方法，参数是: " + map.toString());
		try {
			this.erpPayRollFlowMapper.insertPayRollFlow(map);

		} catch (Exception e) {
			logger.error("新增上岗工资流程审批方法insertPayRollFlow 出现异常" + e.getMessage(), e);
			return RestUtils.returnFailure("新增失败 ！");
		}
		return RestUtils.returnSuccessWithString("OK");
	}

	/**
	 * Description: 初始化社保基数的提交时间
	 *
	 * @Author ZhangYuWei
	 * @Create Date: 2019年06月11日
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RestResponse initSocailSecMonth(String token) {
		logger.info("进入initSocailSecMonth方法");
		// 查询全部员工
		String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmployeeAll";
		String body = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add("token", token);
		HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
		if (200 != response.getStatusCodeValue() || !"200".equals(response.getBody().get("status"))) {
			logger.info("调用人力资源工程过程中发生了异常，导致查询失败！");
			return RestUtils.returnFailure("调用人力资源工程过程中发生了异常，导致查询失败！");
		}
		List<Map<String, Object>> allEmployeeList = (List<Map<String, Object>>) response.getBody().get("data");
		for (Map<String, Object> employeeMap : allEmployeeList) {
			Integer employeeId = Integer.valueOf(String.valueOf(employeeMap.get("employeeId")));
			if (employeeMap.get("entryTime") == null) {
				continue;
			}

			// 社保提交日期如果已经有值，不需要初始化
			ErpPayRollFlow payRollFlow = erpPayRollFlowMapper.findPeriodPayRollByEmpId(employeeId);
			if (payRollFlow != null && payRollFlow.getSocialSecMonth() != null) {
				continue;
			}

			String entryTime = String.valueOf(employeeMap.get("entryTime"));// 入职时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
			Calendar calendar = Calendar.getInstance();
			Date entryTimeDate = null;
			try {
				entryTimeDate = sdf.parse(entryTime);
				calendar.setTime(entryTimeDate);
			} catch (ParseException e) {
				logger.error("日期转换失败：" + entryTime);
				continue;
			}

			String socialSecMonth = null;// 社保基数提交月份
			int day = calendar.get(Calendar.DATE);
			if (day > 15) {// 大于15号，算下个月
				calendar.add(Calendar.MONTH, 1);// 月份+1
				Date date = calendar.getTime();
				socialSecMonth = monthFormat.format(date);
			} else {
				socialSecMonth = monthFormat.format(entryTimeDate);
			}

			// 初始化社保提交日期
			ErpPayRollFlow params = new ErpPayRollFlow();
			params.setUserId(employeeId);
			params.setSocialSecMonth(socialSecMonth);
			erpPayRollFlowMapper.updatePayRollFlow(params);
		}
		return RestUtils.returnSuccessWithString("OK");
	}

	/**
	 * Description: 按照提交时间查询社保基数
	 *
	 * @Author ZhangYuWei
	 * @Create Date: 2019年06月11日
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RestResponse queryPayrollSecMonth(String token, String type, String startMonth, String endMonth) {
		logger.info("进入queryPayrollSecMonth方法，参数是:startMonth=" + startMonth + ",endMonth=" + endMonth);
		// 按起止月份查询
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("startMonth", startMonth);
		queryMap.put("endMonth", endMonth);

		if (type.equals("social")) {
			queryMap.put("type", 1);
		} else if (type.equals("entry")) {
			queryMap.put("type", 2);
		} else if (type.equals("positive")) {
			queryMap.put("type", 3);
			queryMap.put("status", "positiveStatus"); // 2019-09-27
		} else {
			return RestUtils.returnSuccess(null);
		}

		List<ErpPayRollFlow> socailSecMonthList = erpPayRollFlowMapper.findAllPayRollSecMonth(queryMap);
		List<Map<String, Object>> employeeList = new ArrayList<>();
		if (socailSecMonthList.size() > 0) {
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoById";
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);// 将token放到请求头中
			HttpEntity<List<ErpPayRollFlow>> request = new HttpEntity<>(socailSecMonthList, requestHeaders);
			ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
			if (response.getStatusCodeValue() != 200) {
				logger.error("调用HR工程发生异常！");
			}

			List<Map<String, Object>> mapList = (List<Map<String, Object>>) response.getBody().get("data");
			for (int i = 0; i < mapList.size(); i++) {
				Map<String, Object> userInfoMap = mapList.get(i);// 一名员工的信息
				Integer employeeId = (Integer) userInfoMap.get("employeeId");// 员工ID
				ErpPayRollFlow payRollFlow = erpPayRollFlowMapper.findPeriodPayRollByEmpId(employeeId);
				Integer currentPersonID=payRollFlow.getCurrentPersonID();
				Map<String, Object> employeeMessage = (Map<String, Object>) redisTemplate.opsForValue()
						.get("employee_" + currentPersonID);
				if (type.equals("social")) {
					userInfoMap.put("socialSecMonth", payRollFlow.getSocialSecMonth());// 社保基数提交月份
					ErpBasePayroll erpBasePayroll = erpBasePayrollMapper.findBasePayrollDetailByEmpId(employeeId);
					userInfoMap.put("socialSec",
							erpBasePayroll == null ? "" : AesUtils.decrypt(erpBasePayroll.getErpSocialSecurityBase()));// 社保基数
				} else if (type.equals("entry")) {
					userInfoMap.put("commitMonth", payRollFlow.getCommitMonth());// 上岗工资单提交月份
					userInfoMap.put("currentPersonName", (String) employeeMessage.get("employeeName"));// 上岗工资单审批人姓名
				} else if (type.equals("positive")) {
					userInfoMap.put("positiveMonth", payRollFlow.getPositiveMonth());// 转正工资单提交月份
					userInfoMap.put("currentPersonName", (String) employeeMessage.get("employeeName"));// 转正工资单审批人姓名
				} else {
					continue;
				}
				employeeList.add(userInfoMap);
			}
		}
		return RestUtils.returnSuccess(employeeList);
	}

	/**
	 * Description: 修改社保基数的提交时间
	 *
	 * @Author ZhangYuWei
	 * @Create Date: 2019年06月11日
	 */
	public RestResponse updateSocailSecMonth(ErpPayRollFlow params) {
		logger.info("进入updateSocailSecMonth方法，参数是: " + params);
		// 更新社保提交日期
		ErpPayRollFlow payRollFlow = new ErpPayRollFlow();
		payRollFlow.setUserId(params.getUserId());
		payRollFlow.setSocialSecMonth(params.getSocialSecMonth());
		erpPayRollFlowMapper.updatePayRollFlow(payRollFlow);
		return RestUtils.returnSuccessWithString("OK");
	}
	
	/**
	 * Description: 修改positiveMonth日期
	 *
	 * @Author SongXiuGong
	 * @Create Date: 2019年10月14日
	 */
	public RestResponse updatePositiveMonth(ErpPayRollFlow params) {
		logger.info("进入updatePositiveMonth方法，参数是: " + params);
		// 更新positiveMonth日期
		ErpPayRollFlow payRollFlow = new ErpPayRollFlow();
		payRollFlow.setUserId(params.getUserId());
		payRollFlow.setPositiveMonth(params.getPositiveMonth());
		erpPayRollFlowMapper.updatePayRollFlow(payRollFlow);
		return RestUtils.returnSuccessWithString("OK");
	}

	public RestResponse deleteEmployee(String token, Integer employeeId) {
		logger.info("进入deleteEmployee，参数是: employeeId：" + employeeId);
		try {
			ErpPayRollFlow erpPayRollFlow = new ErpPayRollFlow();
			erpPayRollFlow.setUserId(employeeId);
			erpPayRollFlow.setPositiveMonth("");
			this.erpPayRollFlowMapper.updatePayRollFlow(erpPayRollFlow);

		} catch (Exception e) {
			logger.error("deleteEmployee 出现异常" + e.getMessage(), e);
			return RestUtils.returnFailure("新增失败 ！");
		}
		return RestUtils.returnSuccessWithString("OK");
	}

	
	/**
	 * @author gaolp
	 * @date 2019年10月14日
	 * @description:新增转正异常信息
	 * @param token
	 * @param erpPositiveConfirm
	 * @return RestResponse
	 */
	@SuppressWarnings("unchecked")
	public RestResponse insertPositiveExcetion(String token,Map<String,Object> params) {
		logger.info("进入insertPositiveExcetion新增转正异常方法！");
		try {
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
		int employeeId = erpUser.getUserId();//员工ID
		Map<String,Object> emploeeInfo = (Map<String,Object>)redisTemplate.opsForValue().get("employee_"+employeeId);
		String empName = (String) emploeeInfo.get("employeeName");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String yearMonth = (String) params.get("yearMonth");//获取年月
		String exceptionMsg = (String) params.get("exceptionMsg");//获取异常备注
		Integer number = (Integer) params.get("number");//确认的转正人数
		String flag = "";
		if (exceptionMsg != null) {
			flag = "异常信息如下："+exceptionMsg;
		}
		String newException = "人力已确认的转正人数为"+number+"人;   "+flag;
		Map<String,Object> confirMap = erpPositiveConfirMapper.seleConfirmByear(yearMonth);
		if (confirMap != null) {
			return RestUtils.returnSuccess("该月份转正人数已确认，请勿重复提交！");
		}
		ErpPositiveConfirm erpPositiveConfirm = new ErpPositiveConfirm();
		erpPositiveConfirm.setIsConfirm(1);
		erpPositiveConfirm.setExceptionMsg(newException);
		erpPositiveConfirm.setYearMonth(yearMonth);
		erpPositiveConfirm.setCreatTime(sdf.format(date));
		erpPositiveConfirm.setOperator(empName);
		erpPositiveConfirMapper.insertConfirm(erpPositiveConfirm);
		
		//对确认名单进行标记 ADD BY HHR 2020/05/09
		Map<String,Object> queryMap=new HashMap<>();
		queryMap.put("startMonth", yearMonth);
		queryMap.put("endMonth", yearMonth);
		queryMap.put("type", 3);
		queryMap.put("status", "positiveStatus"); // 2019-09-27
		List<ErpPayRollFlow> empIds = erpPayRollFlowMapper.findAllPayRollSecMonth(queryMap);
		for(ErpPayRollFlow positiveEmpIds:empIds){
			ErpPayRollFlow erpPayRollFlow=new ErpPayRollFlow();
			erpPayRollFlow.setUserId(positiveEmpIds.getUserId());
			erpPayRollFlow.setIsConfirmed(true);
			erpPayRollFlowMapper.updatePayRollFlow(erpPayRollFlow);
		}
	 }catch (Exception e) {
		 logger.error("insertPositiveExcetion 出现异常" + e.getMessage(), e);
			return RestUtils.returnFailure("操作失败 ！");
	}
		return RestUtils.returnSuccess("操作成功！");
	}
	
	public RestResponse queryPositiveConfirm(String token,String startMonth,String endMonth) {
		logger.info("进入queryPositiveConfirm查询转正确认表！");
		List<Map<String,Object>> list = new ArrayList<>();
		Map<String,Object> parmas = new HashMap<>();
		try {
			parmas.put("startMonth",startMonth);
			parmas.put("endMonth",endMonth);
			list = erpPositiveConfirMapper.seleConfirmByparam(parmas);//按照起始月查询转正确认表
		}catch (Exception e) {
			logger.error("queryPositiveConfirm 出现异常" + e.getMessage(), e);
			return RestUtils.returnFailure("查询失败 ！");
		}
		return RestUtils.returnSuccess(list);
	}

	/**
	 * 修改待处理的上岗工资单、待处理的转正工资单当前处理人
	 * @param token
	 * @param params
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
    public RestResponse updateCurrentPersonIdById(String token, Map<String, Object> params) throws Exception{

		Integer newFirstManager = Integer.valueOf(String.valueOf(params.get("newFirstManager")));
		Integer employeeId = Integer.valueOf(String.valueOf(params.get("employeeId")));
		Integer newFirstDepartmentSuperLeader = Integer.valueOf(String.valueOf(params.get("newFirstDepartmentSuperLeader")));

		//查询待处理的上岗工资单、转正工资单
		List<ErpPayRollFlow> erpPayRollFlowList = erpPayRollFlowMapper.findWaitPayRollFlowByUserId(employeeId);
		for (ErpPayRollFlow erpPayRollFlow : erpPayRollFlowList){
			erpPayRollFlowMapper.updateCurrentPersonIdById(erpPayRollFlow.getId(), newFirstManager);
		}
		//查询待处理的调薪
//		List<SalaryAdjustRecord> salaryAdjustRecordList = erpSalaryAdjustMapper.findWaitSalaryAdjustByEmployeeId(employeeId);
//		for(SalaryAdjustRecord salaryAdjustRecord : salaryAdjustRecordList){
//			erpSalaryAdjustMapper.updateApproverIdById(salaryAdjustRecord.getId(), newFirstDepartmentSuperLeader);
//		}
		return RestUtils.returnSuccess("修改成功!");
	}

	/**
	 * 批量修改待处理的上岗工资单、待处理的转正工资单、调薪申请 当前处理人
	 * @param allEmployeeInfoMap
	 * @return
	 */
    public RestResponse batchUpdateCurrentPersonId(Map<String, Object> allEmployeeInfoMap) throws Exception {

		//查询待处理的上岗工资单、转正工资单
		List<ErpPayRollFlow> erpPayRollFlowList = erpPayRollFlowMapper.findWaitPayRollFlowList();
		for(ErpPayRollFlow erpPayRollFlow : erpPayRollFlowList){

			String payRollFlowUserIdString = String.valueOf(erpPayRollFlow.getUserId());

			Map<String,Object> employeeInfo = (Map<String,Object>) allEmployeeInfoMap.get(payRollFlowUserIdString);

			if(employeeInfo != null && employeeInfo.get("firstDepartmentUserId") != null){
				Integer firstDepartmentUserId = Integer.valueOf(String.valueOf(employeeInfo.get("firstDepartmentUserId")));
				if(!firstDepartmentUserId.equals(erpPayRollFlow.getCurrentPersonID())){
					erpPayRollFlowMapper.updateCurrentPersonIdById(erpPayRollFlow.getId(), firstDepartmentUserId);
				}
			}
		}

		//查询待处理的调薪
//		List<SalaryAdjustRecord> salaryAdjustRecordList = erpSalaryAdjustMapper.findWaitSalaryAdjustList();
//
//		for(SalaryAdjustRecord salaryAdjustRecord : salaryAdjustRecordList){
//			String salaryAdjustRecordEmployeeId = String.valueOf(salaryAdjustRecord.getEmployeeId());
//			Map<String,Object> employeeInfo = (Map<String,Object>) allEmployeeInfoMap.get(salaryAdjustRecordEmployeeId);
//
//			if(employeeInfo != null && employeeInfo.get("firstDepartmentSuperLeaderId") != null){
//				Integer firstDepartmentSuperLeaderId = Integer.valueOf(String.valueOf(employeeInfo.get("firstDepartmentSuperLeaderId")));
//				if(!salaryAdjustRecord.getApproverid().equals(firstDepartmentSuperLeaderId)) {
//					erpSalaryAdjustMapper.updateApproverIdById(salaryAdjustRecord.getId(), firstDepartmentSuperLeaderId);
//				}
//			}
//
//		}
		return RestUtils.returnSuccess("修改成功!");

	}
}
