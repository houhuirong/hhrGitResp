package com.nantian.erp.salary.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.salary.data.dao.*;
import com.nantian.erp.salary.data.model.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mysql.fabric.xmlrpc.base.Array;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.common.security.ErpAuthorizationFilter;
import com.nantian.erp.salary.util.AesUtils;
import com.nantian.erp.salary.util.RestTemplateUtils;

/**
 *
 * Description: 超时未转正自动转正 定时器
 *
 * @author houhuirong
 * @version 1.0
 * 
 *          <pre>
* Modification History: 
* Date                  Author           Version     
* ------------------------------------------------
* 2018年10月19日      		houhuirong       1.0
 * 
 *          </pre>
 */
@Component
@PropertySource(value = { "classpath:config/host.properties",
		"file:${spring.profiles.path}/config/host.properties" }, ignoreResourceNotFound = true)
public class AutomaticPositionScheduler {

	private static final Logger logger = LoggerFactory.getLogger(AutomaticPositionScheduler.class);

	@Autowired
	RestTemplate restTemplate;

	@Value("${protocol.type}")
	private String protocolType;// http或https

	@Autowired
	private ErpPositivePayrollMapper erpPositivePayrollMapper;

	@Autowired
	private ErpPositiveSalaryMapper erpPositiveSalaryMapper;

	@Autowired
	private ErpPositiveRecordMapper erpPositiveRecordMapper;

	@Autowired
	private ErpBasePayrollMapper erpBasePayrollMapper;

	@Autowired
	private RestTemplateUtils restTemplateUtils;

	@Autowired
	private RedisTemplate<?, ?> redisTemplate;

	@Autowired
	private ErpAdminFunctionMapper erpAdminFunctionMapper;

	@Autowired
	private ErpBasePayrollUpdateRecordMapper erpBasePayrollUpdateRecordMapper;


	@Scheduled(cron = "0 0 3 * * ?")
	// @Scheduled(cron = "0 0/3 * * * ?")
	public void automaticPosition() {
		logger.info("定时任务按照预先设定时间开始执行");
		List<Integer> empIdlist = new ArrayList<Integer>();
		List<String> empNameList = new ArrayList<>();
		try {
			// 调用ERP-人力资源工程操作层服务接口--将未按时转正员工自动转正
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/postive/autoPosition";
			HttpHeaders requestHeaders = new HttpHeaders();
			Map<String, Object> params = new HashMap<>();
			params.put("flag", 1);
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, requestHeaders);
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
			if (response.getStatusCodeValue() != 200) {
				logger.info("调用hr工程接口执行失败");
			} else {
				@SuppressWarnings("unchecked")
				List<Integer> mapList = (List<Integer>) response.getBody().get("data");
				for (int i = 0; i < mapList.size(); i++) {
					Integer employeeId = mapList.get(i);
					ErpPositivePayroll positivePayroll = this.erpPositivePayrollMapper
							.selectOnePositivePayroll(employeeId);
					/*
					 * 如果转正工资单存在，则允许转正，并依此更新基础薪资表
					 * 如果转正工资单不存在，则再查询转正薪资表是否存在，如果有数据，则允许转正并依此更新基础表，否则不允许转正 将不允许转正的员工返回
					 */
					if (positivePayroll == null) {
						ErpPositiveSalary erpPositiveSalary = erpPositiveSalaryMapper
								.findPositiveSalaryByEmpId(employeeId);
						if (erpPositiveSalary == null) {
							empIdlist.add(employeeId);
							ErpPositiveRecord erpPositiveRecord = new ErpPositiveRecord();
							erpPositiveRecord.setErpEmployeeId(employeeId);
							erpPositiveRecord.setErpPositiveHandler("自动转正定时器");
							erpPositiveRecord.setPositiveTime(ExDateUtils.getCurrentStringDateTime());
							erpPositiveRecord.setContent("没有上岗工资单转正薪资，拒绝自动转正，请手动操作！");
							erpPositiveRecordMapper.insertPositiveRecord(erpPositiveRecord);
							// 给未做转正处理的一级部门领导发邮件
							String url3 = protocolType
									+ "nantian-erp-hr/nantian-erp/erp/employee/findEmployeeDetailForLogin?employeeId="
									+ employeeId;
							ResponseEntity<RestResponse> response4 = restTemplate.getForEntity(url3,
									RestResponse.class);
							// 跨工程调用响应失败
							if (response4.getStatusCodeValue() != 200) {
								logger.error(employeeId + "调用hr工程接口执行失败");
							}
							// 解析请求的结果
							Map<String, Object> employeeMap = (Map<String, Object>) response4.getBody().getData();
							Integer userId = (Integer) employeeMap.get("userId");// 获取该员工一级部门经理
							String token = null;
							String tomail = restTemplateUtils.findUsernameByEmployeeId(token, userId);// 收件人
							if (tomail != null && employeeId != null) {
								Map<String, Object> employeeInfo = (Map<String, Object>) redisTemplate.opsForValue()
										.get("employee_" + employeeId);
								String employeeName = (String) employeeInfo.get("employeeName");
								String frommail = "nt_admin@nantian.com.cn";// 发件人
								String bcc = "";// 抄送人
								String subject = "自动转正未处理";// 主题
								String text = "您部门下" + employeeName + "未完成自动转正处理，请及时做上岗工资单确认！";// 邮件内容
								Boolean sendSuccess = restTemplateUtils.sendEmail(frommail, bcc, subject, text, tomail);
								if (!sendSuccess) {
									logger.error(employeeId + "自动转正为处理，邮件发送失败！");
								}
							}
						}
						else{//20201102生产中，当转正工资单未审批时，超时转正 不能自动转正，查看原因是此段代码被注释掉了，
							//更新基础薪资表
							ErpBasePayroll erpBasePayroll = new ErpBasePayroll();
							erpBasePayroll.setErpBaseWage(erpPositiveSalary.getErpPositiveBaseWage());
							erpBasePayroll.setErpPostWage(erpPositiveSalary.getErpPositivePostWage());
							erpBasePayroll.setErpAllowance(erpPositiveSalary.getErpPositiveAllowance());
							erpBasePayroll.setErpPerformance(erpPositiveSalary.getErpPositivePerformance());
							erpBasePayroll.setErpTelFarePerquisite(erpPositiveSalary.getErpTelFarePerquisite());
							erpBasePayroll.setErpEmployeeId(employeeId);
							this.erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);
							//调用ERP-人力资源工程操作层服务接口--将未按时转正员工自动转正
							String requrl= protocolType+"nantian-erp-hr/nantian-erp/erp/postive/autoPosition";
							HttpHeaders requestHeader2=new HttpHeaders();
							Map<String,Object> param = new HashMap<>();
							param.put("flag", 2);
							param.put("employeeId", employeeId);
							HttpEntity<Map<String,Object>> requestEntity2=new HttpEntity<>(param,requestHeader2);
							ResponseEntity<Map> responses = restTemplate.exchange(requrl, HttpMethod.POST, requestEntity2, Map.class);
							if(responses.getStatusCodeValue() != 200){
								logger.info(employeeId+"调用hr工程接口执行自动转正失败");
							}else{
								Map<String, Object> employeeMessage = (Map<String, Object>) redisTemplate.opsForValue()
										.get("employee_" + employeeId);
								String employeeName = (String) employeeMessage.get("employeeName");
								empNameList.add(employeeName);
							}
						}
					} else {
						// 更新基础薪资表
						ErpBasePayroll erpBasePayrolls = new ErpBasePayroll();
						erpBasePayrolls.setErpBaseWage(positivePayroll.getErpPositiveBaseWage());
						erpBasePayrolls.setErpPostWage(positivePayroll.getErpPositivePostWage());
						erpBasePayrolls.setErpAllowance(positivePayroll.getErpPositiveAllowance());
						erpBasePayrolls.setErpPerformance(positivePayroll.getErpPositivePerformance());
						erpBasePayrolls.setErpSocialSecurityBase(positivePayroll.getErpSocialSecurityIndex());
						erpBasePayrolls.setErpAccumulationFundBase(positivePayroll.getErpAccumulationFundIndex());
						erpBasePayrolls.setErpTelFarePerquisite(positivePayroll.getErpTelFarePerquisite());
						erpBasePayrolls.setErpEmployeeId(employeeId);
						this.erpBasePayrollMapper.updateBasePayroll(erpBasePayrolls);
						/*
						 * 将该员工的修改信息加入日志中
						 */
						ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
						basePayrollUpdateRecord.setEmployee(null);// 被修改的员工
						basePayrollUpdateRecord.setEmployeeId(employeeId);
						basePayrollUpdateRecord
								.setProcessor("未按时转正员工自动转正");// 修改人
						basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
						basePayrollUpdateRecord.setContent(erpBasePayrolls.toString());// 修改内容
						this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);

						// 调用ERP-人力资源工程操作层服务接口--将未按时转正员工自动转正
						String requrl = protocolType + "nantian-erp-hr/nantian-erp/erp/postive/autoPosition";
						HttpHeaders requestHeader3 = new HttpHeaders();
						Map<String, Object> param2 = new HashMap<>();
						param2.put("flag", 2);
						param2.put("employeeId", employeeId);
						HttpEntity<Map<String, Object>> requestEntity3 = new HttpEntity<>(param2, requestHeader3);
						ResponseEntity<Map> response3 = restTemplate.exchange(requrl, HttpMethod.POST, requestEntity3,
								Map.class);
						if (response3.getStatusCodeValue() != 200) {
							logger.info(employeeId + "调用hr工程接口执行自动转正失败");
						} else {
							Map<String, Object> employeeMessage = (Map<String, Object>) redisTemplate.opsForValue()
									.get("employee_" + employeeId);
							String employeeName = (String) employeeMessage.get("employeeName");
							empNameList.add(employeeName);
						}
					}
				}
				logger.info(ExDateUtils.getCurrentStringDateTime() + "未做转正处理 的人员为：{}", empIdlist);
				logger.info(ExDateUtils.getCurrentStringDateTime() + "已做转正处理 的人员为：{}", empNameList);
			}
		} catch (Exception e) {
			logger.error("超时自动转正定时器 发生异常:" + e.getMessage(), e);
		}
	}

	/**
	 * 功能：HR转正与salary转正数据一致性检查
	 * 
	 * @author songxiugong
	 * @date 2019年11月10日
	 */
//	@Scheduled(cron = "0 0/1 * * * ?")
	public RestResponse automaticComareEmpSalary(String token) {
		logger.info("HR转正与salary转正数据一致性检查");

		// 薪资正常状态
		List<String> list0 = new ArrayList<>();
		List<String> list1 = new ArrayList<>();
		List<String> list2 = new ArrayList<>();
		List<String> list3 = new ArrayList<>();
		List<String> list4 = new ArrayList<>();

		// 基础薪资不等于上岗且不等于等于转正异常情况
		List<String> list5 = new ArrayList<>();
		List<String> list6 = new ArrayList<>();
		List<String> list7 = new ArrayList<>();
		List<String> list8 = new ArrayList<>();
		List<String> list9 = new ArrayList<>();
		List<String> list10 = new ArrayList<>();
		List<String> list11 = new ArrayList<>();
		List<String> list12 = new ArrayList<>();
		List<String> list13 = new ArrayList<>();
		List<String> list14 = new ArrayList<>();
		List<String> list15 = new ArrayList<>();
		List<String> list16 = new ArrayList<>();
		List<String> list17 = new ArrayList<>();
		List<String> list18 = new ArrayList<>();
		List<String> list19 = new ArrayList<>();
		List<String> list20 = new ArrayList<>();
		List<String> list35 = new ArrayList<>();

		// 无适应期薪资【没有进行异常判断】
		List<String> list21 = new ArrayList<>();
		List<String> list22 = new ArrayList<>();

		// 其它各类对象为空异常情况
		List<String> list23 = new ArrayList<>();
		List<String> list24 = new ArrayList<>();
		List<String> list25 = new ArrayList<>();
		List<String> list26 = new ArrayList<>();
		List<String> list27 = new ArrayList<>();

		// others【全局其余情况】
		List<String> list28 = new ArrayList<>();

		// 此情况暂时废除
		List<String> list29 = new ArrayList<>();
		List<String> list30 = new ArrayList<>();
		List<String> list31 = new ArrayList<>();
		List<String> list32 = new ArrayList<>();
		List<String> list33 = new ArrayList<>();
		List<String> list34 = new ArrayList<>();
		List<String> list36 = new ArrayList<>();

		// 离职人员 employ status=4【没有进行异常判断】
		List<String> list40 = new ArrayList<>();
		List<String> list41 = new ArrayList<>();	//试用期结束时间为空字符串记录

		// 转正等于基础薪资异常情况
		List<String> list101 = new ArrayList<>();
		List<String> list102 = new ArrayList<>();
		List<String> list103 = new ArrayList<>();
		List<String> list104 = new ArrayList<>();
		List<String> list105 = new ArrayList<>();
		List<String> list106 = new ArrayList<>();
		List<String> list107 = new ArrayList<>();
		List<String> list108 = new ArrayList<>();

		// 上岗等于基础薪资异常情况
		List<String> list201 = new ArrayList<>();
		List<String> list20101 = new ArrayList<>();
		List<String> list20102 = new ArrayList<>();

		List<String> list202 = new ArrayList<>();
		List<String> list203 = new ArrayList<>();
		List<String> list20301 = new ArrayList<>();
		List<String> list2030101 = new ArrayList<>();
		List<String> list2030102 = new ArrayList<>();
		List<String> list20302 = new ArrayList<>();
		List<String> list20303 = new ArrayList<>();
		List<String> list20304 = new ArrayList<>();
		List<String> list20305 = new ArrayList<>();
		List<String> list204 = new ArrayList<>();
		List<String> list205 = new ArrayList<>();
		List<String> list20501 = new ArrayList<>();
		List<String> list20502 = new ArrayList<>();
		List<String> list2050201 = new ArrayList<>();
		List<String> list2050202 = new ArrayList<>();
		List<String> list20503 = new ArrayList<>();
		List<String> list206 = new ArrayList<>();
		List<String> list207 = new ArrayList<>();
		List<String> list208 = new ArrayList<>();
		List<String> list20801 = new ArrayList<>();
		List<String> list20802 = new ArrayList<>();
		List<String> list20803 = new ArrayList<>();

		Map<String, List<Map<String, Object>>> others = new HashMap<String, List<Map<String, Object>>>();

		try {
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/automaticComareEmpSalary";
			HttpHeaders requestHeaders = new HttpHeaders();
//			Map<String,Object> params = new HashMap<String, Object>();
//			params.put("token",token);
			requestHeaders.add("token", token);

			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(null, requestHeaders);
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
			if (response.getStatusCodeValue() != 200) {
				logger.info("调用HR工程automaticComareEmpSalary接口执行失败");
				return RestUtils.returnFailure("调用HR工程automaticComareEmpSalary接口执行失败");
			} else {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> mapList = (List<Map<String, Object>>) response.getBody().get("data");

				Map<Integer, Map<String, Object>> employees = listToMap(mapList);
//				logger.info(employees.toString() + employees.size());

				// 1.将获取的的各类薪酬数据先解密然后将List<Map<String, Object>> 数据格式转为 Map<Integer<Map<String,
				// Object>>> 数据格式，便于查找比较
				// BaseSalary
				mapList = null;
				mapList = erpAdminFunctionMapper.selectPayRollFlowUnionBaseSalary();
				Map<Integer, Map<String, Object>> payRollFlowUnionBaseSalary;
				if (mapList != null) {
					mapList = decryptBasePayrollList(mapList);
					payRollFlowUnionBaseSalary = listToMap(mapList);
				} else {
					payRollFlowUnionBaseSalary = null;
				}

				// periodParoll
				mapList = null;
				mapList = erpAdminFunctionMapper.selectPayRollFlowUnionperiodParoll();
				Map<Integer, Map<String, Object>> payRollFlowUnionperiodParoll;
				if (mapList != null) {
					mapList = decryptPeriodPayrollList(mapList);
					payRollFlowUnionperiodParoll = listToMap(mapList);
				} else {
					payRollFlowUnionperiodParoll = null;
				}

				// Positivesalary
				mapList = null;
				mapList = erpAdminFunctionMapper.selectPayRollFlowUnionPositivesalary();
				Map<Integer, Map<String, Object>> payRollFlowUnionPositivesalary;
				if (mapList != null) {
					mapList = decryptPositiveSalary(mapList);
					payRollFlowUnionPositivesalary = listToMap(mapList);
				} else {
					payRollFlowUnionPositivesalary = null;
				}

				// Positivepeyroll
				mapList = null;
				mapList = erpAdminFunctionMapper.selectPayRollFlowUnionPositivepeyroll();
				Map<Integer, Map<String, Object>> payRollFlowUnionPositivepeyroll;
				if (mapList != null) {
					mapList = decryptPositivePayroll(mapList);
					payRollFlowUnionPositivepeyroll = listToMap(mapList);
				} else {
					payRollFlowUnionPositivepeyroll = null;
				}

				// 2.按照员工ID进行查找薪酬相关数据并进行对比
				for (Map.Entry<Integer, Map<String, Object>> employee : employees.entrySet()) {
					Integer key = employee.getKey();
					Map<String, Object> employeeMap = employee.getValue();

					Map<String, Object> baseSalary = (payRollFlowUnionBaseSalary != null)
							? payRollFlowUnionBaseSalary.get(key)
							: null;
					Map<String, Object> periodSalary = (payRollFlowUnionperiodParoll != null)
							? payRollFlowUnionperiodParoll.get(key)
							: null;
					Map<String, Object> positiveSalary = (payRollFlowUnionPositivesalary != null)
							? payRollFlowUnionPositivesalary.get(key)
							: null;
					Map<String, Object> positivePayroll = (payRollFlowUnionPositivepeyroll != null)
							? payRollFlowUnionPositivepeyroll.get(key)
							: null;

					// 2-1. 各类判断变量
					Integer status = null; // HR-employee-Status
					Boolean dateCompareFlag = false; // 转正日期与当前日期对比
					Integer epstatus = null; // HR-employeePositive-Status
					Boolean salaryCompareFlag = false; // Salary-base与positive、period、salary比较
					Integer pfstatus = null; // Salary-PayrollFlow-Status
							
					if (Integer.valueOf(String.valueOf(employeeMap.get("employeeId"))).equals(2726)) {
						System.out.println("=====++++" + String.valueOf(employeeMap.get("name")));
					}
					// 2-2. 获取status、pstatus 对employeeMap 和 baseSalary 进行判空
					if (employeeMap != null && baseSalary != null && baseSalary.containsKey("erp_payroll_id")) {
						if (employeeMap.get("status") == null || String.valueOf(employeeMap.get("status")).equals("")) {
							list23.add(String.valueOf(employeeMap.get("name")));
							list23.add(String.valueOf(employeeMap.get("employeeId")));
							list23.add(String.valueOf(String.valueOf(employeeMap.get("probationEndTime"))));
							logger.info("员工：" + employeeMap.get("name") + "信息中没有员工status，无法进行匹配！");
							continue;
						} else {
							status = Integer.valueOf(String.valueOf(employeeMap.get("status")));
						}

						if (employeeMap.get("epstatus") == null || String.valueOf(employeeMap.get("epstatus")).equals("")) {
							list24.add(String.valueOf(employeeMap.get("name")));
							list24.add(String.valueOf(employeeMap.get("employeeId")));
							list24.add(String.valueOf(String.valueOf(employeeMap.get("probationEndTime"))));
							logger.info("员工：" + employeeMap.get("name") + "信息中没有员工employePositivestatus，无法进行匹配！");
							continue;
						} else {
							epstatus = Integer.valueOf(String.valueOf(employeeMap.get("epstatus")));
						}

						if (baseSalary.get("status") == null || String.valueOf(baseSalary.get("status")).equals("")) {
							list25.add(String.valueOf(employeeMap.get("name")));
							list25.add(String.valueOf(employeeMap.get("employeeId")));
							list25.add(String.valueOf(String.valueOf(employeeMap.get("probationEndTime"))));
							logger.info("员工：" + employeeMap.get("name") + " PayrollFlow没有status，无法进行匹配！");
							continue;
						} else {
							pfstatus = Integer.valueOf(String.valueOf(baseSalary.get("status")));
						}

					} else if(!(String.valueOf(employeeMap.get("status")).equals("4"))) {	//状态为4的状态由后续List40抓取
						if (baseSalary == null || !baseSalary.containsKey("erp_payroll_id")) {
							list26.add(String.valueOf(employeeMap.get("name")));
							list26.add(String.valueOf(employeeMap.get("employeeId")));
							list26.add(String.valueOf(String.valueOf(employeeMap.get("probationEndTime"))));
							logger.info("员工：" + employeeMap.get("name") + " 没有基础薪资");
						}
						if (employeeMap == null) {
							list27.add(String.valueOf(employeeMap.get("name")));
							list27.add(String.valueOf(employeeMap.get("employeeId")));
							list27.add(String.valueOf(String.valueOf(employeeMap.get("probationEndTime"))));
							logger.info("员工：" + employeeMap.get("name") + " 没有员工信息");
						}
						continue;
					}

					Boolean flag = false; // 判断薪酬是否相等的标识

					// 2-3. probationEndTime 对employeeMap 和 baseSalary 进行判空，
					Object probationEndTime = employeeMap.get("probationEndTime");
					Object beginTime = employeeMap.get("beginTime");

					Boolean flagDateCampare = false;
					Boolean flagNoPeriod = false;
					
					if (probationEndTime != null && !String.valueOf(probationEndTime).equals("") && beginTime != null
							&& !String.valueOf(beginTime).equals("")) {
						flagDateCampare = comPareDate(String.valueOf(beginTime), String.valueOf(probationEndTime));
						if(flagDateCampare) {
							flagNoPeriod = true;
						}						
					} 
					
//					logger.info("==" + employeeMap.get("name").toString() + String.valueOf(probationEndTime));
					if (flagNoPeriod) {

						// 没有试用期，直接对比转正薪资
						logger.info("员工：" + employeeMap.get("name") + " 没有试应期，直接进行比较！");
						flag = compareBaseSalaryAndPositivePayroll(baseSalary, periodSalary, employeeMap);
						if (flag && dateCompareFlag && pfstatus == 3) {
							list21.add(String.valueOf(employeeMap.get("name")));
							continue;
						} else {
							list22.add(String.valueOf(employeeMap.get("name")));
							continue;
						}

					} else {

						// 2-4. 有试应期，获取dateCompareFlag：按照员工转正日期跟当前日期比较大小分类进行判断
						//试用期为空字符串，不进行处理，只进行记录
						if(String.valueOf(probationEndTime).equals("")) {
							list41.add(String.valueOf(employeeMap.get("name")));
							continue;
						}
						
						dateCompareFlag = comPareDate(String.valueOf(probationEndTime));

						Boolean flagPeriod;
						Boolean flagPositiveSalary;
						Boolean flagPositivePayroll;
						
						flagPeriod = compareBaseSalaryAndPeriodPayroll(baseSalary, periodSalary, employeeMap);
//						flagPositiveSalary = compareBaseSalaryAndPositiveSalary(baseSalary, positiveSalary,
//								employeeMap);
						flagPositivePayroll = compareBaseSalaryAndPositivePayroll(baseSalary, positivePayroll,
								employeeMap);

//						logger.info(status.toString() + "::" + (status.toString() == "4"));
//						logger.info(status + "::" + (status == 4));

						// D.基础薪资不等于上岗且不懂等于转正情况
						if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
								&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
								&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
								&& (!flagPeriod && !flagPositivePayroll)) {

							if((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 1 || epstatus == 2)
									&& !dateCompareFlag && pfstatus == 1) {

								list5.add(String.valueOf(employeeMap.get("name")));
								list5.add(String.valueOf(employeeMap.get("employeeId")));
								list5.add(String.valueOf(status));
								list5.add(String.valueOf(epstatus));
								list5.add(String.valueOf(pfstatus));
								list5.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 1 || epstatus == 2)
									&& !dateCompareFlag && pfstatus == 2) {

								list6.add(String.valueOf(employeeMap.get("name")));
								list6.add(String.valueOf(employeeMap.get("employeeId")));
								list6.add(String.valueOf(status));
								list6.add(String.valueOf(epstatus));
								list6.add(String.valueOf(pfstatus));
								list6.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 1 || epstatus == 2)
									&& !dateCompareFlag && pfstatus == 3) {

								list7.add(String.valueOf(employeeMap.get("name")));
								list7.add(String.valueOf(employeeMap.get("employeeId")));
								list7.add(String.valueOf(status));
								list7.add(String.valueOf(epstatus));
								list7.add(String.valueOf(pfstatus));
								list7.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 1 || epstatus == 2)
									&& !dateCompareFlag && pfstatus == 4) {

								list8.add(String.valueOf(employeeMap.get("name")));
								list8.add(String.valueOf(employeeMap.get("employeeId")));
								list8.add(String.valueOf(status));
								list8.add(String.valueOf(epstatus));
								list8.add(String.valueOf(pfstatus));
								list8.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 1 || epstatus == 2)
									&& dateCompareFlag && pfstatus == 1) {

								list9.add(String.valueOf(employeeMap.get("name")));
								list9.add(String.valueOf(employeeMap.get("employeeId")));
								list9.add(String.valueOf(status));
								list9.add(String.valueOf(epstatus));
								list9.add(String.valueOf(pfstatus));
								list9.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 1 || epstatus == 2)
									&& dateCompareFlag && pfstatus == 2) {

								list10.add(String.valueOf(employeeMap.get("name")));
								list10.add(String.valueOf(employeeMap.get("employeeId")));
								list10.add(String.valueOf(status));
								list10.add(String.valueOf(epstatus));
								list10.add(String.valueOf(pfstatus));
								list10.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 1 || epstatus == 2)
									&& dateCompareFlag && pfstatus == 3) {

								list11.add(String.valueOf(employeeMap.get("name")));
								list11.add(String.valueOf(employeeMap.get("employeeId")));
								list11.add(String.valueOf(status));
								list11.add(String.valueOf(epstatus));
								list11.add(String.valueOf(pfstatus));
								list11.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 1 || epstatus == 2)
									&& dateCompareFlag && pfstatus == 4) {

								list12.add(String.valueOf(employeeMap.get("name")));
								list12.add(String.valueOf(employeeMap.get("employeeId")));
								list12.add(String.valueOf(status));
								list12.add(String.valueOf(epstatus));
								list12.add(String.valueOf(pfstatus));
								list12.add(String.valueOf(String.valueOf(probationEndTime)));
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 3) && !dateCompareFlag
									&& pfstatus == 1) {

								list13.add(String.valueOf(employeeMap.get("name")));
								list13.add(String.valueOf(employeeMap.get("employeeId")));
								list13.add(String.valueOf(status));
								list13.add(String.valueOf(epstatus));
								list13.add(String.valueOf(pfstatus));
								list13.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 3) && !dateCompareFlag
									&& pfstatus == 2) {

								list14.add(String.valueOf(employeeMap.get("name")));
								list14.add(String.valueOf(employeeMap.get("employeeId")));
								list14.add(String.valueOf(status));
								list14.add(String.valueOf(epstatus));
								list14.add(String.valueOf(pfstatus));
								list14.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 3) && !dateCompareFlag
									&& pfstatus == 3) {

								list15.add(String.valueOf(employeeMap.get("name")));
								list15.add(String.valueOf(employeeMap.get("employeeId")));
								list15.add(String.valueOf(status));
								list15.add(String.valueOf(epstatus));
								list15.add(String.valueOf(pfstatus));
								list15.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 3) && !dateCompareFlag
									&& pfstatus == 4) {

								list16.add(String.valueOf(employeeMap.get("name")));
								list16.add(String.valueOf(employeeMap.get("employeeId")));
								list16.add(String.valueOf(status));
								list16.add(String.valueOf(epstatus));
								list16.add(String.valueOf(pfstatus));
								list16.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 3) && dateCompareFlag
									&& pfstatus == 1) {

								list17.add(String.valueOf(employeeMap.get("name")));
								list17.add(String.valueOf(employeeMap.get("employeeId")));
								list17.add(String.valueOf(status));
								list17.add(String.valueOf(epstatus));
								list17.add(String.valueOf(pfstatus));
								list17.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 3) && dateCompareFlag
									&& pfstatus == 2) {

								list18.add(String.valueOf(employeeMap.get("name")));
								list18.add(String.valueOf(employeeMap.get("employeeId")));
								list18.add(String.valueOf(status));
								list18.add(String.valueOf(epstatus));
								list18.add(String.valueOf(pfstatus));
								list18.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 3) && dateCompareFlag
									&& pfstatus == 3) {

								list19.add(String.valueOf(employeeMap.get("name")));
								list19.add(String.valueOf(employeeMap.get("employeeId")));
								list19.add(String.valueOf(status));
								list19.add(String.valueOf(epstatus));
								list19.add(String.valueOf(pfstatus));
								list19.add(String.valueOf(String.valueOf(probationEndTime)));
							} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id")
									&& periodSalary != null && periodSalary.containsKey("erp_payroll_id")
									&& positivePayroll != null) && positivePayroll.containsKey("erp_positive_id")
									&& (!flagPeriod && !flagPositivePayroll) && (epstatus == 3) && dateCompareFlag
									&& pfstatus == 4) {

								list20.add(String.valueOf(employeeMap.get("name")));
								list20.add(String.valueOf(employeeMap.get("employeeId")));
								list20.add(String.valueOf(status));
								list20.add(String.valueOf(epstatus));
								list20.add(String.valueOf(pfstatus));
								list20.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else {

								list35.add(String.valueOf(employeeMap.get("name")));
								list35.add(String.valueOf(employeeMap.get("employeeId")));
								list35.add(String.valueOf(status));
								list35.add(String.valueOf(epstatus));
								list35.add(String.valueOf(pfstatus));
								list35.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							}
						}
						
						// A.基础薪资正常情况
						status = Integer.valueOf(String.valueOf(employeeMap.get("status")));
						if (status == 4) {
//							logger.info(String.valueOf("员工：" + employeeMap.get("name")) + " 已经离职");
							list40.add(String.valueOf(employeeMap.get("name")));
							list40.add(String.valueOf(employeeMap.get("employeeId")));
							list40.add(String.valueOf(String.valueOf(probationEndTime)));
							continue;
						} else if (flagPositivePayroll && (epstatus == 1 || epstatus == 2) && dateCompareFlag
								&& (pfstatus == 4)) {

							list0.add(String.valueOf(employeeMap.get("name")));
							list0.add(String.valueOf(employeeMap.get("employeeId")));
							list0.add(String.valueOf(String.valueOf(probationEndTime)));
							continue;

						} else if (flagPositivePayroll && epstatus == 3 && dateCompareFlag
								&& (pfstatus == 3 || pfstatus == 4)) {

							list1.add(String.valueOf(employeeMap.get("name")));
							list1.add(String.valueOf(employeeMap.get("employeeId")));
							list1.add(String.valueOf(String.valueOf(probationEndTime)));
							continue;

						} else if (flagPeriod && (epstatus == 1 || epstatus == 2) && !dateCompareFlag
								&& pfstatus == 2) {

							list2.add(String.valueOf(employeeMap.get("name")));
							list2.add(String.valueOf(employeeMap.get("employeeId")));
							list2.add(String.valueOf(String.valueOf(probationEndTime)));
							continue;
						} else if (flagPeriod && (epstatus == 1 || epstatus == 2) && !dateCompareFlag
								&& pfstatus == 3) {

							list3.add(String.valueOf(employeeMap.get("name")));
							list3.add(String.valueOf(employeeMap.get("employeeId")));
							list3.add(String.valueOf(String.valueOf(probationEndTime)));
							continue;
						} else if ((baseSalary != null && baseSalary.containsKey("erp_payroll_id") 
								&& (periodSalary == null || !periodSalary.containsKey("erp_payroll_id"))) 
								&& (epstatus == 1 || epstatus == 2)
								&& !dateCompareFlag && pfstatus == 1) {

							list4.add(String.valueOf(employeeMap.get("name")));
							list4.add(String.valueOf(employeeMap.get("employeeId")));
							list4.add(String.valueOf(String.valueOf(probationEndTime)));
							continue;
						}

						// B.上岗等于基础薪资异常情况
						if (periodSalary != null && periodSalary.containsKey("erp_payroll_id")
								&& flagPeriod) {
							if ((epstatus != 1 && epstatus != 2) && !dateCompareFlag
									&& (pfstatus == 2 || pfstatus == 3)) {
								list201.add(String.valueOf(employeeMap.get("name")));
								list201.add(String.valueOf(employeeMap.get("employeeId")));
								list201.add(String.valueOf(status));
								list201.add(String.valueOf(epstatus));
								list201.add(String.valueOf(pfstatus));
								list201.add(String.valueOf(String.valueOf(probationEndTime)));

								if ((epstatus == 3) && !dateCompareFlag && (pfstatus == 3)) { // 正常
									list20101.add(String.valueOf(employeeMap.get("name")));
									list20101.add(String.valueOf(employeeMap.get("employeeId")));
									list20101.add(String.valueOf(status));
									list20101.add(String.valueOf(epstatus));
									list20101.add(String.valueOf(pfstatus));
									list20101.add(String.valueOf(String.valueOf(probationEndTime)));
								} else { // 异常
									list20102.add(String.valueOf(employeeMap.get("name")));
									list20102.add(String.valueOf(employeeMap.get("employeeId")));
									list20102.add(String.valueOf(status));
									list20102.add(String.valueOf(epstatus));
									list20102.add(String.valueOf(String.valueOf(probationEndTime)));
								}
								continue;
							} else if ((epstatus == 1 || epstatus == 2) && dateCompareFlag
									&& (pfstatus == 2 || pfstatus == 3)) {
								list202.add(String.valueOf(employeeMap.get("name")));
								list202.add(String.valueOf(employeeMap.get("employeeId")));
								list202.add(String.valueOf(status));
								list202.add(String.valueOf(epstatus));
								list202.add(String.valueOf(pfstatus));
								list202.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((epstatus == 1 || epstatus == 2) && !dateCompareFlag
									&& (pfstatus != 2 && pfstatus != 3)) {
								list203.add(String.valueOf(employeeMap.get("name")));
								list203.add(String.valueOf(employeeMap.get("employeeId")));
								list203.add(String.valueOf(status));
								list203.add(String.valueOf(epstatus));
								list203.add(String.valueOf(pfstatus));
								list203.add(String.valueOf(String.valueOf(probationEndTime)));
								if ((epstatus == 1) && !dateCompareFlag && (pfstatus == 1)) {
									list20301.add(String.valueOf(employeeMap.get("name")));
									list20301.add(String.valueOf(employeeMap.get("employeeId")));
									list20301.add(String.valueOf(status));
									list20301.add(String.valueOf(epstatus));
									list20301.add(String.valueOf(pfstatus));
									list20301.add(String.valueOf(String.valueOf(probationEndTime)));
									if (status == 1) {
										list2030101.add(String.valueOf(employeeMap.get("name")));
										list2030101.add(String.valueOf(employeeMap.get("employeeId")));
										list2030101.add(String.valueOf(status));
										list2030101.add(String.valueOf(epstatus));
										list2030101.add(String.valueOf(pfstatus));
										list2030101.add(String.valueOf(String.valueOf(probationEndTime)));
									} else {
										list2030102.add(String.valueOf(employeeMap.get("name")));
										list2030102.add(String.valueOf(employeeMap.get("employeeId")));
										list2030102.add(String.valueOf(status));
										list2030102.add(String.valueOf(epstatus));
										list2030102.add(String.valueOf(pfstatus));
										list2030102.add(String.valueOf(String.valueOf(probationEndTime)));
									}
								} else if ((epstatus == 1) && !dateCompareFlag && (pfstatus == 4)) {
									list20302.add(String.valueOf(employeeMap.get("name")));
									list20302.add(String.valueOf(employeeMap.get("employeeId")));
									list20302.add(String.valueOf(status));
									list20302.add(String.valueOf(epstatus));
									list20302.add(String.valueOf(pfstatus));
									list20302.add(String.valueOf(String.valueOf(probationEndTime)));
								} else if ((epstatus == 2) && !dateCompareFlag && (pfstatus == 1)) {
									list20304.add(String.valueOf(employeeMap.get("name")));
									list20304.add(String.valueOf(employeeMap.get("employeeId")));
									list20304.add(String.valueOf(status));
									list20304.add(String.valueOf(epstatus));
									list20304.add(String.valueOf(pfstatus));
									list20304.add(String.valueOf(String.valueOf(probationEndTime)));
								} else {
									list20305.add(String.valueOf(employeeMap.get("name")));
									list20305.add(String.valueOf(employeeMap.get("employeeId")));
									list20305.add(String.valueOf(status));
									list20305.add(String.valueOf(epstatus));
									list20305.add(String.valueOf(pfstatus));
									list20305.add(String.valueOf(String.valueOf(probationEndTime)));
								}
								continue;
							} else if ((epstatus == 1 || epstatus == 2) && dateCompareFlag
									&& (pfstatus != 2 && pfstatus != 3)) {
								list204.add(String.valueOf(employeeMap.get("name")));
								list204.add(String.valueOf(employeeMap.get("employeeId")));
								list204.add(String.valueOf(status));
								list204.add(String.valueOf(epstatus));
								list204.add(String.valueOf(pfstatus));
								list204.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((epstatus != 1 && epstatus != 2) && !dateCompareFlag
									&& (pfstatus != 2 && pfstatus != 3)) {
								list205.add(String.valueOf(employeeMap.get("name")));
								list205.add(String.valueOf(employeeMap.get("employeeId")));
								list205.add(String.valueOf(status));
								list205.add(String.valueOf(epstatus));
								list205.add(String.valueOf(pfstatus));
								list205.add(String.valueOf(String.valueOf(probationEndTime)));
								if ((epstatus == 3) && !dateCompareFlag && (pfstatus == 1)) {
									list20501.add(String.valueOf(employeeMap.get("name")));
									list20501.add(String.valueOf(employeeMap.get("employeeId")));
									list20501.add(String.valueOf(status));
									list20501.add(String.valueOf(epstatus));
									list20501.add(String.valueOf(pfstatus));
									list20501.add(String.valueOf(String.valueOf(probationEndTime)));
								} else if ((epstatus == 3) && !dateCompareFlag && (pfstatus == 4)) {
									list20502.add(String.valueOf(employeeMap.get("name")));
									list20502.add(String.valueOf(employeeMap.get("employeeId")));
									list20502.add(String.valueOf(status));
									list20502.add(String.valueOf(epstatus));
									list20502.add(String.valueOf(pfstatus));
									list20502.add(String.valueOf(String.valueOf(probationEndTime)));
									if (status == 1) {
										list2050201.add(String.valueOf(employeeMap.get("name")));
										list2050201.add(String.valueOf(employeeMap.get("employeeId")));
										list2050201.add(String.valueOf(status));
										list2050201.add(String.valueOf(epstatus));
										list2050201.add(String.valueOf(pfstatus));
										list2050201.add(String.valueOf(String.valueOf(probationEndTime)));
									} else {
										list2050202.add(String.valueOf(employeeMap.get("name")));
										list2050202.add(String.valueOf(employeeMap.get("employeeId")));
										list2050202.add(String.valueOf(status));
										list2050202.add(String.valueOf(epstatus));
										list2050202.add(String.valueOf(pfstatus));
										list2050202.add(String.valueOf(String.valueOf(probationEndTime)));
									}
								} else {
									list20503.add(String.valueOf(employeeMap.get("name")));
									list20503.add(String.valueOf(employeeMap.get("employeeId")));
									list20503.add(String.valueOf(status));
									list20503.add(String.valueOf(epstatus));
									list20503.add(String.valueOf(pfstatus));
									list20503.add(String.valueOf(String.valueOf(probationEndTime)));
								}
								continue;
							} else if ((epstatus != 1 && epstatus != 2) && dateCompareFlag
									&& (pfstatus == 2 || pfstatus == 3)) {
								list206.add(String.valueOf(employeeMap.get("name")));
								list206.add(String.valueOf(employeeMap.get("employeeId")));
								list206.add(String.valueOf(status));
								list206.add(String.valueOf(epstatus));
								list206.add(String.valueOf(pfstatus));
								list206.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((epstatus != 1 && epstatus != 2) && dateCompareFlag
									&& (pfstatus != 2 && pfstatus != 3)) {
								list207.add(String.valueOf(employeeMap.get("name")));
								list207.add(String.valueOf(employeeMap.get("employeeId")));
								list207.add(String.valueOf(status));
								list207.add(String.valueOf(epstatus));
								list207.add(String.valueOf(pfstatus));
								list207.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else {
								list208.add(String.valueOf(employeeMap.get("name")));
								list208.add(String.valueOf(employeeMap.get("employeeId")));
								list208.add(String.valueOf(status));
								list208.add(String.valueOf(epstatus));
								list208.add(String.valueOf(pfstatus));
								list208.add(String.valueOf(String.valueOf(probationEndTime)));
								if (epstatus == 1 && pfstatus == 3) {
									list20801.add(String.valueOf(employeeMap.get("name")));
									list20801.add(String.valueOf(employeeMap.get("employeeId")));
									list20801.add(String.valueOf(status));
									list20801.add(String.valueOf(epstatus));
									list20801.add(String.valueOf(pfstatus));
									list20801.add(String.valueOf(dateCompareFlag));
									list20801.add(String.valueOf(String.valueOf(probationEndTime)));
								} else if (epstatus == 2 && pfstatus == 3) {
									list20802.add(String.valueOf(employeeMap.get("name")));
									list20802.add(String.valueOf(employeeMap.get("employeeId")));
									list20802.add(String.valueOf(status));
									list20802.add(String.valueOf(epstatus));
									list20802.add(String.valueOf(pfstatus));
									list20802.add(String.valueOf(dateCompareFlag));
									list20802.add(String.valueOf(String.valueOf(probationEndTime)));
								} else {
									list20803.add(String.valueOf(employeeMap.get("name")));
									list20803.add(String.valueOf(employeeMap.get("employeeId")));
									list20803.add(String.valueOf(status));
									list20803.add(String.valueOf(epstatus));
									list20803.add(String.valueOf(pfstatus));
									list20803.add(String.valueOf(dateCompareFlag));
									list20803.add(String.valueOf(String.valueOf(probationEndTime)));
								}
								continue;
							}
						}

						// C.转正等于基础薪资异常情况
						if (positivePayroll != null && positivePayroll.containsKey("erp_positive_id")
								&& flagPositivePayroll) {
							if ((epstatus != 3) && dateCompareFlag && (pfstatus == 3 || pfstatus == 4)) {
								list101.add(String.valueOf(employeeMap.get("name")));
								list101.add(String.valueOf(employeeMap.get("employeeId")));
								list101.add(String.valueOf(status));
								list101.add(String.valueOf(epstatus));
								list101.add(String.valueOf(pfstatus));
								list101.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((epstatus == 3) && !dateCompareFlag && (pfstatus == 3 || pfstatus == 4)) {
								list102.add(String.valueOf(employeeMap.get("name")));
								list102.add(String.valueOf(employeeMap.get("employeeId")));
								list102.add(String.valueOf(status));
								list102.add(String.valueOf(epstatus));
								list102.add(String.valueOf(pfstatus));
								list102.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((epstatus == 3) && dateCompareFlag && (pfstatus != 3 && pfstatus != 4)) {
								list103.add(String.valueOf(employeeMap.get("name")));
								list103.add(String.valueOf(employeeMap.get("employeeId")));
								list103.add(String.valueOf(status));
								list103.add(String.valueOf(epstatus));
								list103.add(String.valueOf(pfstatus));
								list103.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((epstatus == 3) && !dateCompareFlag && (pfstatus != 3 && pfstatus != 4)) {
								list104.add(String.valueOf(employeeMap.get("name")));
								list104.add(String.valueOf(employeeMap.get("employeeId")));
								list104.add(String.valueOf(status));
								list104.add(String.valueOf(epstatus));
								list104.add(String.valueOf(pfstatus));
								list104.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((epstatus != 3) && dateCompareFlag && (pfstatus != 3 && pfstatus != 4)) {
								list105.add(String.valueOf(employeeMap.get("name")));
								list105.add(String.valueOf(employeeMap.get("employeeId")));
								list105.add(String.valueOf(status));
								list105.add(String.valueOf(epstatus));
								list105.add(String.valueOf(pfstatus));
								list105.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((epstatus != 3) && !dateCompareFlag && (pfstatus == 3 || pfstatus == 4)) {
								list106.add(String.valueOf(employeeMap.get("name")));
								list106.add(String.valueOf(employeeMap.get("employeeId")));
								list106.add(String.valueOf(status));
								list106.add(String.valueOf(epstatus));
								list106.add(String.valueOf(pfstatus));
								list106.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else if ((epstatus != 3) && !dateCompareFlag && (pfstatus != 3 && pfstatus != 4)) {
								list107.add(String.valueOf(employeeMap.get("name")));
								list107.add(String.valueOf(employeeMap.get("employeeId")));
								list107.add(String.valueOf(status));
								list107.add(String.valueOf(epstatus));
								list107.add(String.valueOf(pfstatus));
								list107.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							} else {
								list108.add(String.valueOf(employeeMap.get("name")));
								list108.add(String.valueOf(employeeMap.get("employeeId")));
								list108.add(String.valueOf(status));
								list108.add(String.valueOf(epstatus));
								list108.add(String.valueOf(pfstatus));
								list108.add(String.valueOf(String.valueOf(probationEndTime)));
								continue;
							}
						}

						

						List<Map<String, Object>> tempVar = new ArrayList<Map<String, Object>>();
						tempVar.add(employeeMap);
						tempVar.add(baseSalary);
						tempVar.add(periodSalary);
						tempVar.add(positivePayroll);

						others.put(String.valueOf(employeeMap.get("name")), tempVar);
						list28.add(String.valueOf(employeeMap.get("name")));
						list28.add(String.valueOf(employeeMap.get("employeeId")));
						list28.add(String.valueOf(status));
						list28.add(String.valueOf(epstatus));
						list28.add(String.valueOf(pfstatus));
						list28.add(String.valueOf(String.valueOf(probationEndTime)));
						continue;

//						
//						if (flagPositivePayroll && (epstatus == 3) && !dateCompareFlag
//								&& (pfstatus == 3 || pfstatus == 4)) {
//
//							list29.add(String.valueOf(employeeMap.get("name")));
//							continue;
//
//						} else if (flagPositivePayroll && (epstatus == 1 || epstatus == 2) && dateCompareFlag
//								&& (pfstatus == 3 || pfstatus == 4)) {
//
//							list30.add(String.valueOf(employeeMap.get("name")));
//							continue;
//
//						} else if (flagPositivePayroll && (epstatus == 1 || epstatus == 2) && !dateCompareFlag
//								&& (pfstatus == 3 || pfstatus == 4)) {
//
//							list31.add(String.valueOf(employeeMap.get("name")));
//							continue;
//
//						} else if (flagPositivePayroll && (epstatus == 1 || epstatus == 2) && !dateCompareFlag
//								&& (pfstatus == 2)) {
//
//							list32.add(String.valueOf(employeeMap.get("name")));
//							continue;
//
//						} else if (flagPositivePayroll && (epstatus == 1 || epstatus == 2) && !dateCompareFlag
//								&& (pfstatus == 1)) {
//
//							list33.add(String.valueOf(employeeMap.get("name")));
//							continue;
//
//						} else if (flagPeriod && (epstatus == 3) && !dateCompareFlag) {
//
//							list34.add(String.valueOf(employeeMap.get("name")));
//							continue;
//
//						} else if (dateCompareFlag) {
//
//							list36.add(String.valueOf(employeeMap.get("name")));
//							continue;
//
//						} else {
//							List<Map<String, Object>> tempVar = new ArrayList<Map<String, Object>>();
//							tempVar.add(employeeMap);
//							tempVar.add(baseSalary);
//							tempVar.add(periodSalary);
//							tempVar.add(positivePayroll);
//
//							others.put(String.valueOf(employeeMap.get("name")), tempVar);
//							list28.add(String.valueOf(employeeMap.get("name")));
//							continue;
//						}
					}
				}
			}
			logger.info("*************************1 - 基础薪酬正常情况*************************\n");
			logger.info("List0:{}\n", list0);
			logger.info("List1:{}\n", list1);
			logger.info("List2:{}\n", list2);
			logger.info("List3:{}\n", list3);
			logger.info("List4:{}\n", list4);

			logger.info("*************************2 - 基础薪酬不等于转正也不等于上岗异常情况*************************\n");
			logger.info("List5:{}\n", list5);
			logger.info("List6:{}\n", list6);
			logger.info("List7:{}\n", list7);
			logger.info("List8:{}\n", list8);
			logger.info("List9:{}\n", list9);
			logger.info("List10:{}\n", list10);
			logger.info("List11:{}\n", list11);
			logger.info("List12:{}\n", list12);
			logger.info("List13:{}\n", list13);
			logger.info("List14:{}\n", list14);
			logger.info("List15:{}\n", list15);
			logger.info("List16:{}\n", list16);
			logger.info("List17:{}\n", list17);
			logger.info("List18:{}\n", list18);
			logger.info("List19:{}\n", list19);
			logger.info("List20:{}\n", list20);
			logger.info("List35:{}\n", list35);

			logger.info("*************************3 - 基础薪酬等于上岗异常情况*************************\n");
			logger.info("list201:{}\n", list201);
			logger.info("list20101:{}\n", list20101);
			logger.info("list20102:{}\n", list20102);
			logger.info("list202:{}\n", list202);
			logger.info("list203:{}\n", list203);
			logger.info("list20301:{}\n", list20301);
			logger.info("list2030101:{}\n", list2030101);
			logger.info("list2030102:{}\n", list2030102);
			logger.info("list20302:{}\n", list20302);
			logger.info("list20303:{}\n", list20303);
			logger.info("list20304:{}\n", list20304);
			logger.info("list20305:{}\n", list20305);
			logger.info("list204:{}\n", list204);
			logger.info("list205:{}\n", list205);
			logger.info("list20501:{}\n", list20501);
			logger.info("list20502:{}\n", list20502);
			logger.info("list2050201:{}\n", list2050201);
			logger.info("list2050202:{}\n", list2050202);
			logger.info("list20503:{}\n", list20503);
			logger.info("list206:{}\n", list206);
			logger.info("list207:{}\n", list207);
			logger.info("list208:{}\n", list208);
			logger.info("list20801:{}\n", list20801);
			logger.info("list20802:{}\n", list20802);
			logger.info("list20803:{}\n", list20803);

			logger.info("*************************4 - 基础薪酬等于转正异常情况*************************\n");
			logger.info("List101:{}\n", list101);
			logger.info("list102:{}\n", list102);
			logger.info("list103:{}\n", list103);
			logger.info("list104:{}\n", list104);
			logger.info("list105:{}\n", list105);
			logger.info("list106:{}\n", list106);
			logger.info("list107:{}\n", list107);
			logger.info("list108:{}\n", list108);

			logger.info("*************************5 - 对象为空或者status为空异常情况*************************\n");
			logger.info("List23:{}\n", list23);
			logger.info("List24:{}\n", list24);
			logger.info("List25:{}\n", list25);
			logger.info("List26:{}\n", list26);
			logger.info("List27:{}\n", list27);

			logger.info("*************************6 - 无试应期限情况【没有进行异常判断】*************************\n");
			logger.info("List21:{}\n", list21);
			logger.info("List22:{}\n", list22);

			logger.info("*************************7 - 离职人员情况【没有进行异常判断】*************************\n");
			logger.info("List40:{}\n", list40);
			
			logger.info("*************************8- 试用期结束其为空字符，只记录【没有进行异常判断】*************************\n");
			logger.info("List41:{}\n", list41);
			
			logger.info("*************************9 - others【全局其余情况】*************************\n");
			logger.info("List28:{}\n", list28);
//			logger.info("others:", others);

//			logger.info("List29:{}\n", list29);
//			logger.info("List30:{}\n", list30);
//			logger.info("List31:{}\n", list31);
//			logger.info("List32:{}\n", list32);
//			logger.info("List33:{}\n", list33);
//			logger.info("List34:{}\n", list34);
//			logger.info("List36:{}\n", list36);
			return RestUtils.returnSuccess("OK");
		} catch (Exception e) {
			logger.error("HR转正与salary转正数据一致性检查:" + e.getMessage(), e);
			return RestUtils.returnFailure("HR转正与salary转正数据一致性检查" + e.getMessage());
		}
	}

	/**
	 * 功能：List<Map<String,Object>> TO Map<Integer, Map<String,Object>>
	 * 
	 * @author songxiugong
	 * @date 2019年11月11日
	 */
	public Map<Integer, Map<String, Object>> listToMap(List<Map<String, Object>> employees) {
		Map<String, Object> employeeMapped = new HashMap<String, Object>();
		Map<Integer, Map<String, Object>> employeesMapped = new HashMap<Integer, Map<String, Object>>();

		Integer iplus;

		for (iplus = 0; iplus < employees.size(); iplus++) {
			employeeMapped = employees.get(iplus);
			if (employeeMapped.containsKey("employeeId")) {
				employeesMapped.put(Integer.valueOf(String.valueOf(employeeMapped.get("employeeId"))), employeeMapped);
			} else if (employeeMapped.containsKey("userId")) {
				employeesMapped.put(Integer.valueOf(String.valueOf(employeeMapped.get("userId"))), employeeMapped);
			}
		}

		return employeesMapped;
	}

	public Boolean compareBaseSalaryAndPeriodPayroll(Map<String, Object> baseSalary, Map<String, Object> periodSalary,
			Map<String, Object> employeeMap) {
		Boolean flag = false;
		if (baseSalary != null && periodSalary != null) {
			flag = true;
		} else {
			if (baseSalary == null) {
				logger.info("员工：" + String.valueOf(employeeMap.get("name")) + "基础薪酬为空，无法进行比较");
			}
			if (periodSalary == null) {
				logger.info("员工：" + String.valueOf(employeeMap.get("name")) + "上岗工资单为空，无法进行比较");
			}
		}

		// 如果为空,则退出，不进行比较
		if (!flag) {
			return flag;
		}

		//Flag 初试化为False，进行薪资对比，如果一致，则变为true
		flag = false;
		
		String baseTemp;
		String periodTemp;
		baseTemp = String.valueOf(baseSalary.get("erp_base_wage"));
		periodTemp = String.valueOf(periodSalary.get("erp_period_base_wage"));
		if (!baseTemp.equals("null") && !periodTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((periodTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		baseTemp = String.valueOf(baseSalary.get("erp_post_wage"));
		periodTemp = String.valueOf(periodSalary.get("erp_period_post_wage"));
		if (!baseTemp.equals("null") && !periodTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((periodTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		baseTemp = String.valueOf(baseSalary.get("erp_performance"));
		periodTemp = String.valueOf(periodSalary.get("erp_period_performance"));
		if (!baseTemp.equals("null") && !periodTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((periodTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		baseTemp = String.valueOf(baseSalary.get("erp_allowance"));
		periodTemp = String.valueOf(periodSalary.get("erp_period_allowance"));
		if (!baseTemp.equals("null") && !periodTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((periodTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		return flag;
	}

	public Boolean compareBaseSalaryAndPositiveSalary(Map<String, Object> baseSalary,
			Map<String, Object> positiveSalary, Map<String, Object> employeeMap) {
		Boolean flag = false;
		if (baseSalary != null && positiveSalary != null) {
			flag = true;
		} else {
			if (baseSalary == null) {
				logger.info("员工：" + String.valueOf(employeeMap.get("name")) + "基础薪酬为空，无法进行比较");
			}
			if (positiveSalary == null) {
				logger.info("员工：" + String.valueOf(employeeMap.get("name")) + "上岗转正薪资为空，无法进行比较");
			}
		}

		// 如果为空,则退出，不进行比较
		if (!flag) {
			return flag;
		}

		//Flag 初试化为False，进行薪资对比，如果一致，则变为true
		flag = false;
		
		Map<String, Object> otherSalary = positiveSalary;
		String baseTemp;
		String otherTemp;
		baseTemp = String.valueOf(baseSalary.get("erp_base_wage"));
		otherTemp = String.valueOf(otherSalary.get("erp_positive_base_wage"));
		if (!baseTemp.equals("null") && !otherTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((otherTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		baseTemp = String.valueOf(baseSalary.get("erp_post_wage"));
		otherTemp = String.valueOf(otherSalary.get("erp_positive_post_wage"));
		if (!baseTemp.equals("null") && !otherTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((otherTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		baseTemp = String.valueOf(baseSalary.get("erp_performance"));
		otherTemp = String.valueOf(otherSalary.get("erp_positive_performance"));
		if (!baseTemp.equals("null") && !otherTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((otherTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		baseTemp = String.valueOf(baseSalary.get("erp_allowance"));
		otherTemp = String.valueOf(otherSalary.get("erp_positive_income"));
		if (!baseTemp.equals("null") && !otherTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((otherTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		return flag;
	}

	public Boolean compareBaseSalaryAndPositivePayroll(Map<String, Object> baseSalary,
			Map<String, Object> positivePayroll, Map<String, Object> employeeMap) {
		Boolean flag = false;
		if (baseSalary != null && positivePayroll != null) {
			flag = true;
		} else {
			if (baseSalary == null) {
				logger.info("员工：" + String.valueOf(employeeMap.get("name")) + "基础薪酬为空，无法进行比较");
			}
			if (positivePayroll == null) {
				logger.info("员工：" + String.valueOf(employeeMap.get("name")) + "转正薪酬为空，无法进行比较");
			}
		}

		// 如果为空,则退出，不进行比较
		if (!flag) {
			return flag;
		}
		
		//Flag 初试化为False，进行薪资对比，如果一致，则变为true
		flag = false;
		
		Map<String, Object> otherSalary = positivePayroll;
		String baseTemp;
		String otherTemp;
		baseTemp = String.valueOf(baseSalary.get("erp_base_wage"));
		otherTemp = String.valueOf(otherSalary.get("erp_positive_base_wage"));
		if (!baseTemp.equals("null") && !otherTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((otherTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		baseTemp = String.valueOf(baseSalary.get("erp_post_wage"));
		otherTemp = String.valueOf(otherSalary.get("erp_positive_post_wage"));
		if (!baseTemp.equals("null") && !otherTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((otherTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		baseTemp = String.valueOf(baseSalary.get("erp_performance"));
		otherTemp = String.valueOf(otherSalary.get("erp_positive_performance"));
		if (!baseTemp.equals("null") && !otherTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((otherTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		baseTemp = String.valueOf(baseSalary.get("erp_allowance"));
		otherTemp = String.valueOf(otherSalary.get("erp_positive_allowance"));
		if (!baseTemp.equals("null") && !otherTemp.equals("null")) {
			Double base = Double.valueOf(baseTemp);
			Double period = Double.valueOf((otherTemp));
			flag = base.equals(period) ? true : false;
			if(!flag) {
				return false;
			}
		} else {
			return flag;
		}

		return flag;
	}

	public List<Map<String, Object>> decryptBasePayrollList(List<Map<String, Object>> list) {
		Integer iPlus;
		Map<String, Object> temp = new HashMap<String, Object>();
		for (iPlus = 0; iPlus < list.size(); iPlus++) {
			temp = null;
			temp = list.get(iPlus);
			encryptAndDecryptBasePayroll(false, temp);
		}
		return list;
	}

	//

	public List<Map<String, Object>> decryptPeriodPayrollList(List<Map<String, Object>> list) {
		Integer iPlus;
		Map<String, Object> temp = new HashMap<String, Object>();
		for (iPlus = 0; iPlus < list.size(); iPlus++) {
			temp = null;
			temp = list.get(iPlus);
			encryptAndDecryptPeriodPayroll(false, temp);
		}
		return list;
	}

	public List<Map<String, Object>> decryptPositiveSalary(List<Map<String, Object>> list) {
		Integer iPlus;
		Map<String, Object> temp = new HashMap<String, Object>();
		for (iPlus = 0; iPlus < list.size(); iPlus++) {
			temp = null;
			temp = list.get(iPlus);
			encryptAndDecryptPositiveSalary(false, temp);
		}
		return list;
	}

	public List<Map<String, Object>> decryptPositivePayroll(List<Map<String, Object>> list) {
		Integer iPlus;
		Map<String, Object> temp = new HashMap<String, Object>();
		for (iPlus = 0; iPlus < list.size(); iPlus++) {
			temp = null;
			temp = list.get(iPlus);
			encryptAndDecryptPositivePayroll(false, temp);
		}
		return list;
	}

	//
	public Map<String, Object> encryptAndDecryptBasePayroll(Boolean flag, Map<String, Object> erpBasePayroll) {
		if (erpBasePayroll != null) {
			if (flag) {// 加密
				if (erpBasePayroll.containsKey("erp_base_wage")) {
					erpBasePayroll.put("erp_base_wage",
							encryptDataRsa(String.valueOf(erpBasePayroll.get("erp_base_wage"))));
				}

				if (erpBasePayroll.containsKey("erp_post_wage")) {
					erpBasePayroll.put("erp_post_wage",
							encryptDataRsa(String.valueOf(erpBasePayroll.get("erp_post_wage"))));
				}

				if (erpBasePayroll.containsKey("erp_performance")) {
					erpBasePayroll.put("erp_performance",
							encryptDataRsa(String.valueOf(erpBasePayroll.get("erp_performance"))));
				}
				if (erpBasePayroll.containsKey("erp_allowance")) {
					erpBasePayroll.put("erp_allowance",
							encryptDataRsa(String.valueOf(erpBasePayroll.get("erp_allowance"))));
				}

			} else {// 解密
				if (erpBasePayroll.containsKey("erp_base_wage")) {
					erpBasePayroll.put("erp_base_wage",
							decryptDataRsa(String.valueOf(erpBasePayroll.get("erp_base_wage"))));
				} else {
					erpBasePayroll.put("erp_base_wage", "null");
				}

				if (erpBasePayroll.containsKey("erp_post_wage")) {
					erpBasePayroll.put("erp_post_wage",
							decryptDataRsa(String.valueOf(erpBasePayroll.get("erp_post_wage"))));
				} else {
					erpBasePayroll.put("erp_post_wage", "null");
				}

				if (erpBasePayroll.containsKey("erp_performance")) {
					erpBasePayroll.put("erp_performance",
							decryptDataRsa(String.valueOf(erpBasePayroll.get("erp_performance"))));
				} else {
					erpBasePayroll.put("erp_performance", "null");
				}

				if (erpBasePayroll.containsKey("erp_allowance")) {
					erpBasePayroll.put("erp_allowance",
							decryptDataRsa(String.valueOf(erpBasePayroll.get("erp_allowance"))));
				} else {
					erpBasePayroll.put("erp_allowance", "null");
				}

			}
		}
		return erpBasePayroll;
	}

	//

	//
	public Map<String, Object> encryptAndDecryptPeriodPayroll(Boolean flag, Map<String, Object> erpPeriodPayroll) {
		if (erpPeriodPayroll != null) {
			if (flag) {// 加密
				if (erpPeriodPayroll.containsKey("erp_period_base_wage")) {
					erpPeriodPayroll.put("erp_period_base_wage",
							encryptDataRsa(String.valueOf(erpPeriodPayroll.get("erp_period_base_wage"))));
				}

				if (erpPeriodPayroll.containsKey("erp_period_post_wage")) {
					erpPeriodPayroll.put("erp_period_post_wage",
							encryptDataRsa(String.valueOf(erpPeriodPayroll.get("erp_period_post_wage"))));
				}

				if (erpPeriodPayroll.containsKey("erp_period_performance")) {
					erpPeriodPayroll.put("erp_period_performance",
							encryptDataRsa(String.valueOf(erpPeriodPayroll.get("erp_period_performance"))));
				}

				if (erpPeriodPayroll.containsKey("erp_period_allowance")) {
					erpPeriodPayroll.put("erp_period_allowance",
							encryptDataRsa(String.valueOf(erpPeriodPayroll.get("erp_period_allowance"))));
				}

			} else {// 解密
				if (erpPeriodPayroll.containsKey("erp_period_base_wage")) {
					erpPeriodPayroll.put("erp_period_base_wage",
							decryptDataRsa(String.valueOf(erpPeriodPayroll.get("erp_period_base_wage"))));
				} else {
					erpPeriodPayroll.put("erp_period_base_wage", "null");
				}

				if (erpPeriodPayroll.containsKey("erp_period_post_wage")) {
					erpPeriodPayroll.put("erp_period_post_wage",
							decryptDataRsa(String.valueOf(erpPeriodPayroll.get("erp_period_post_wage"))));
				} else {
					erpPeriodPayroll.put("erp_period_post_wage", "null");
				}

				if (erpPeriodPayroll.containsKey("erp_period_performance")) {
					erpPeriodPayroll.put("erp_period_performance",
							decryptDataRsa(String.valueOf(erpPeriodPayroll.get("erp_period_performance"))));
				} else {
					erpPeriodPayroll.put("erp_period_performance", "null");
				}

				if (erpPeriodPayroll.containsKey("erp_period_allowance")) {
					erpPeriodPayroll.put("erp_period_allowance",
							decryptDataRsa(String.valueOf(erpPeriodPayroll.get("erp_period_allowance"))));
				} else {
					erpPeriodPayroll.put("erp_period_allowance", "null");
				}
			}
		}
		return erpPeriodPayroll;
	}

	//

	public Map<String, Object> encryptAndDecryptPositiveSalary(Boolean flag, Map<String, Object> erpPositiveSalary) {
		if (erpPositiveSalary != null) {
			if (flag) {// 加密

				if (erpPositiveSalary.containsKey("erp_positive_base_wage")) {
					erpPositiveSalary.put("erp_positive_base_wage",
							encryptDataRsa(String.valueOf(erpPositiveSalary.get("erp_positive_base_wage"))));
				}

				if (erpPositiveSalary.containsKey("erp_positive_post_wage")) {
					erpPositiveSalary.put("erp_positive_post_wage",
							encryptDataRsa(String.valueOf(erpPositiveSalary.get("erp_positive_post_wage"))));
				}

				if (erpPositiveSalary.containsKey("erp_positive_performance")) {
					erpPositiveSalary.put("erp_positive_performance",
							encryptDataRsa(String.valueOf(erpPositiveSalary.get("erp_positive_performance"))));
				}

				if (erpPositiveSalary.containsKey("erp_positive_allowance")) {
					erpPositiveSalary.put("erp_positive_allowance",
							encryptDataRsa(String.valueOf(erpPositiveSalary.get("erp_positive_allowance"))));
				}

			} else {// 解密
				if (erpPositiveSalary.containsKey("erp_positive_base_wage")) {
					erpPositiveSalary.put("erp_positive_base_wage",
							decryptDataRsa(String.valueOf(erpPositiveSalary.get("erp_positive_base_wage"))));
				} else {
					erpPositiveSalary.put("erp_positive_base_wage", "null");
				}

				if (erpPositiveSalary.containsKey("erp_positive_post_wage")) {
					erpPositiveSalary.put("erp_positive_post_wage",
							decryptDataRsa(String.valueOf(erpPositiveSalary.get("erp_positive_post_wage"))));
				} else {
					erpPositiveSalary.put("erp_positive_post_wage", "null");
				}

				if (erpPositiveSalary.containsKey("erp_positive_performance")) {
					erpPositiveSalary.put("erp_positive_performance",
							decryptDataRsa(String.valueOf(erpPositiveSalary.get("erp_positive_performance"))));
				} else {
					erpPositiveSalary.put("erp_positive_performance", "null");
				}

				if (erpPositiveSalary.containsKey("erp_positive_allowance")) {
					erpPositiveSalary.put("erp_positive_allowance",
							decryptDataRsa(String.valueOf(erpPositiveSalary.get("erp_positive_allowance"))));
				} else {
					erpPositiveSalary.put("erp_positive_allowance", "null");
				}

			}
		}
		return erpPositiveSalary;
	}

	//
	public Map<String, Object> encryptAndDecryptPositivePayroll(Boolean flag, Map<String, Object> erpPositivePayroll) {
		if (erpPositivePayroll != null) {
			if (flag) {// 加密

				if (erpPositivePayroll.containsKey("erp_positive_base_wage")) {
					erpPositivePayroll.put("erp_positive_base_wage",
							encryptDataRsa(String.valueOf(erpPositivePayroll.get("erp_positive_base_wage"))));
				}

				if (erpPositivePayroll.containsKey("erp_positive_post_wage")) {
					erpPositivePayroll.put("erp_positive_post_wage",
							encryptDataRsa(String.valueOf(erpPositivePayroll.get("erp_positive_post_wage"))));
				}

				if (erpPositivePayroll.containsKey("erp_positive_performance")) {
					erpPositivePayroll.put("erp_positive_performance",
							encryptDataRsa(String.valueOf(erpPositivePayroll.get("erp_positive_performance"))));
				}

				if (erpPositivePayroll.containsKey("erp_positive_allowance")) {
					erpPositivePayroll.put("erp_positive_allowance",
							encryptDataRsa(String.valueOf(erpPositivePayroll.get("erp_positive_allowance"))));
				}

			} else {// 解密
				if (erpPositivePayroll.containsKey("erp_positive_base_wage")) {
					erpPositivePayroll.put("erp_positive_base_wage",
							decryptDataRsa(String.valueOf(erpPositivePayroll.get("erp_positive_base_wage"))));
				} else {
					erpPositivePayroll.put("erp_positive_base_wage", "null");
				}

				if (erpPositivePayroll.containsKey("erp_positive_post_wage")) {
					erpPositivePayroll.put("erp_positive_post_wage",
							decryptDataRsa(String.valueOf(erpPositivePayroll.get("erp_positive_post_wage"))));
				} else {
					erpPositivePayroll.put("erp_positive_post_wage", "null");
				}

				if (erpPositivePayroll.containsKey("erp_positive_performance")) {
					erpPositivePayroll.put("erp_positive_performance",
							decryptDataRsa(String.valueOf(erpPositivePayroll.get("erp_positive_performance"))));
				} else {
					erpPositivePayroll.put("erp_positive_performance", "null");
				}

				if (erpPositivePayroll.containsKey("erp_positive_allowance")) {
					erpPositivePayroll.put("erp_positive_allowance",
							decryptDataRsa(String.valueOf(erpPositivePayroll.get("erp_positive_allowance"))));
				} else {
					erpPositivePayroll.put("erp_positive_allowance", "null");
				}

			}
		}
		return erpPositivePayroll;
	}

	/**
	 * Description: 加密
	 *
	 * @return
	 * 
	 * @Author HouHuiRong
	 * 
	 * @Create Date: 2018年11月9日 下午15:31:01
	 */
	public String encryptDataRsa(String salary) {
		String result = null;
		String defaultSalaryValue = AesUtils.encrypt(String.valueOf(0.0));
		if (salary == null) {
			result = defaultSalaryValue;
		} else {
			result = AesUtils.encrypt(String.valueOf(salary));
		}
		return result;
	}

	/**
	 * Description: 解密
	 *
	 * @return
	 * @Author HouHuiRong
	 * @Create Date: 2018年11月9日 下午15:31:01
	 */
	public String decryptDataRsa(String salary) {
		String result = "";
		if (salary == null) {
			return result;
		} else {
			result = String.valueOf(AesUtils.decrypt(salary));
		}
		return result;
	}

	/**
	 * 功能：指定一个日期与当前日期进行yyyy-MM-dd格式的时间进行比较。指定日期小于当前日期，返回ture，否则，返回false
	 * 
	 * @author songxiugong
	 * @date 2019年11月22日
	 */
	public Boolean comPareDate(String probationEndTime) {

		try {
			Date now = new Date();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date dateProbationEndTime = simpleDateFormat.parse(String.valueOf(probationEndTime));
			Date nowFormated = simpleDateFormat.parse(simpleDateFormat.format(now));

			Boolean flag;
			flag = (dateProbationEndTime.compareTo(nowFormated) < 0) ? true : false;
//			logger.info(flag.toString());
			return flag;
		} catch (Exception e) {
			logger.error("comPareDate方法出现错误:" + e.getMessage(), e);
			return false;
		}

	}

	/**
	 * 功能：指定两个日期进行yyyy-MM-dd格式的时间进行比较。相等，返回ture，不相等，返回false
	 * 
	 * @author songxiugong
	 * @date 2019年11月22日
	 */
	public Boolean comPareDate(String dateA, String dateB) {

		try {

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date timeA = simpleDateFormat.parse(String.valueOf(dateA));
			Date timeB = simpleDateFormat.parse(String.valueOf(dateB));

			Boolean flag;
			flag = (timeA.compareTo(timeB) == 0) ? true : false;
//			logger.info(flag.toString());
			return flag;
		} catch (Exception e) {
			logger.error("comPareDate方法出现错误:" + e.getMessage(), e);
			return false;
		}

	}

	/**
	 * 功能：指定日期和当前日期进行yyyy-MM格式的时间进行比较。相等，返回ture，不相等，返回false
	 * 
	 * @author songxiugong
	 * @date 2019年11月22日
	 */
	public Boolean comPareYearMonth(String toBecompared) {
		try {

			Date now = new Date();
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
			Date dateToBecompared = simpleDateFormat.parse(String.valueOf(toBecompared));
			Date nowFormated = simpleDateFormat.parse(simpleDateFormat.format(now));

			Boolean flag;
			flag = (dateToBecompared.compareTo(nowFormated) == 0) ? true : false;

			return flag;
		} catch (Exception e) {
			logger.error("comPareYearMonth方法出现错误:" + e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 功能：上岗转正薪资根据员工转正日期更新到实时薪资中，并更新员工对应状态
	 * 
	 * @author songxiugong
	 * @date 2019年11月18日
	 */
	public RestResponse checkAndUpdateBaseSalary(String token) {

		List<String> list1 = new ArrayList<>();			// 信息中没有员工status，无法进行匹配！
		List<String> list2 = new ArrayList<>();			// PayrollFlow没有status，无法进行匹配！
		List<String> list3 = new ArrayList<>();			// 没有基础薪资
		List<String> list4 = new ArrayList<>();			// 没有员工信息
		List<String> list5 = new ArrayList<>();			// 没有试用期，员工Salary被转正---需要处理【OK】
		List<String> list5_1 = new ArrayList<>();		// 没有试用期，员工Salary没有转正--需要处理【OK】
		List<String> list6 = new ArrayList<>();			// 没有试用期的 其它情况
		List<String> list7 = new ArrayList<>();			// 有试用期，Base Period Positive 都有且试用期最后一天小于当前日期		【OK】
		List<String> list8 = new ArrayList<>();			// 有试用期，Base Period Positive 都有且试用期最后一天大于等于当前日期	【OK】
		List<String> list9 = new ArrayList<>();			// 有试用期，Base Period都有 Positive 没有 且试用期最后一天大于等于当前日期	【OK】
		List<String> list10 = new ArrayList<>();		// 全局其它情况
		List<String> list11 = new ArrayList<>();		// 当月离职人员
		List<String> list12 = new ArrayList<>();		// 非当月的离职人员
		
		try {
			// 通过token从redis缓存中获取用户信息
			ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
			String employeeName = userInfo.getEmployeeName();// 员工姓名
			String username = userInfo.getUsername();// 用户名

			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/automaticComareEmpSalary";
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);

			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(null, requestHeaders);
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
			if (response.getStatusCodeValue() != 200) {
				logger.info("调用HR工程automaticComareEmpSalary接口执行失败");
				return RestUtils.returnFailure("调用HR工程automaticComareEmpSalary接口执行失败,无法进行后续操作");
			} else {
				@SuppressWarnings("unchecked")

				List<Map<String, Object>> mapList = (List<Map<String, Object>>) response.getBody().get("data");
				Map<Integer, Map<String, Object>> employees = listToMap(mapList);

				/*1.将获取的的各类薪酬数据先解密然后将List<Map<String, Object>> 数据格式转为 Map<Integer,<Map<String,
				 *  Object>>> 数据格式，便于查找比较
				 */
				
				// 1-1. BaseSalary 解密并转化基础薪资
				mapList = null;
				mapList = erpAdminFunctionMapper.selectPayRollFlowUnionBaseSalary();
				Map<Integer, Map<String, Object>> payRollFlowUnionBaseSalary;
				Map<Integer, Map<String, Object>> payRollFlowUnionBaseSalaryReplica = new HashMap<Integer, Map<String,Object>>();
				if (mapList != null) {
//					payRollFlowUnionBaseSalaryReplica = listToMap(mapList);
					mapList = decryptBasePayrollList(mapList);
					payRollFlowUnionBaseSalary = listToMap(mapList);
				} else {
					payRollFlowUnionBaseSalary = null;
				}

				// 1-2. periodParoll 解密并转化上岗薪资
				mapList = null;
				mapList = erpAdminFunctionMapper.selectPayRollFlowUnionperiodParoll();
				Map<Integer, Map<String, Object>> payRollFlowUnionperiodParoll;
				Map<Integer, Map<String, Object>> payRollFlowUnionperiodParollReplica = new HashMap<Integer, Map<String,Object>>();
				if (mapList != null) {
//					payRollFlowUnionperiodParollReplica = listToMap(mapList);
					mapList = decryptPeriodPayrollList(mapList);
					payRollFlowUnionperiodParoll = listToMap(mapList);
				} else {
					payRollFlowUnionperiodParoll = null;
				}

				//1-3. positiveSalary 解密并转化上岗转正薪资
				mapList = null;
				mapList = erpAdminFunctionMapper.selectPayRollFlowUnionPositivesalary();
				Map<Integer, Map<String, Object>> payRollFlowUnionPositiveSalary;
//				Map<Integer, Map<String, Object>> payRollFlowUnionPositiveSalaryReplica = new HashMap<Integer, Map<String,Object>>();
				if (mapList != null) {
//					payRollFlowUnionPositiveSalaryReplica = listToMap(mapList);
					mapList = decryptPositiveSalary(mapList);
					payRollFlowUnionPositiveSalary = listToMap(mapList);
				} else {
					payRollFlowUnionPositiveSalary = null;
				}
				
				
				//1-4. Positivepeyroll 解密并转化转正薪资
				mapList = null;
				mapList = erpAdminFunctionMapper.selectPayRollFlowUnionPositivepeyroll();
				
				Map<Integer, Map<String, Object>> payRollFlowUnionPositivepeyroll;
				Map<Integer, Map<String, Object>> payRollFlowUnionPositivepeyrollRepica = new HashMap<Integer, Map<String,Object>>();
				
				if (mapList != null) {
//					payRollFlowUnionPositivepeyrollRepica = listToMap(mapList);
					mapList = decryptPositivePayroll(mapList);
					payRollFlowUnionPositivepeyroll = listToMap(mapList);
				} else {
					payRollFlowUnionPositivepeyroll = null;
				}

				//2. 循环每一个员工，进行分类判断处理
				for (Map.Entry<Integer, Map<String, Object>> employee : employees.entrySet()) {
													
					Integer key = employee.getKey();
					Map<String, Object> employeeMap = employee.getValue();

					Integer employeeId = null;
					employeeId = Integer.valueOf(String.valueOf(employeeMap.get("employeeId")));
//					ErpBasePayroll erpBasePayrollTemp = new ErpBasePayroll();
//					erpBasePayrollTemp.setErpBaseWage("1635819DD682D487578FF4314B5857E1");
//					erpBasePayrollTemp.setErpPostWage("1635819DD682D487578FF4314B5857E1");
//					erpBasePayrollTemp.setErpPerformance("1635819DD682D487578FF4314B5857E1");
//					erpBasePayrollTemp.setErpAllowance("1635819DD682D487578FF4314B5857E1");
//					erpBasePayrollTemp.setErpEmployeeId(employeeId);
//					erpBasePayrollMapper.updateBasePayroll(erpBasePayrollTemp);
					
//					if (Integer.valueOf((String.valueOf(employeeMap.get("employeeId")))).equals(2726)) {
//						logger.info("***********：" + employeeMap.get("name"));
//					}
					
					//2-1 获取转换后的包含payrollFlow的各类薪酬
					//2-1-1. 根据员工id 获取转换后的 包含payrollFlow的基础薪酬
					Map<String, Object> baseSalary = (payRollFlowUnionBaseSalary != null)
							? payRollFlowUnionBaseSalary.get(key)
							: null;
					//2-1-2. 根据员工id 获取转换后的 包含payrollFlow的上岗薪酬
					Map<String, Object> periodSalary = (payRollFlowUnionperiodParoll != null)
							? payRollFlowUnionperiodParoll.get(key)
							: null;
					//2-1-3. 根据员工id 获取转换后的 包含payrollFlow的上岗转正薪酬
					Map<String, Object> positiveSalary = (payRollFlowUnionPositiveSalary != null)
							? payRollFlowUnionPositiveSalary.get(key)
							: null;
									
					//2-1-4. 根据员工id 获取转换后的 包含payrollFlow的转正薪酬
					Map<String, Object> positivePayroll = (payRollFlowUnionPositivepeyroll != null)
							? payRollFlowUnionPositivepeyroll.get(key)
							: null;

					// 2-2. 各类空的情况分类判断
					// 2-2-1. 各类判断变量定义
					Integer status = null; 				// HR-employee-Status
					Boolean dateCompareFlag = false; 	// 转正日期与当前日期对比
					Integer epstatus = null; 			// HR-employeePositive-Status
					Boolean salaryCompareFlag = false; 	// Salary-base与positive、period、salary比较
					Integer pfstatus = null; 			// Salary-PayrollFlow-Status

					// 2-2-2. 获取status、pstatus 对employeeMap 和 baseSalary 进行判空
					if (employeeMap != null && baseSalary != null) {
						if (employeeMap.get("status") == null || String.valueOf(employeeMap.get("status")).equals("")) {

							logger.info("员工：" + String.valueOf(employeeMap.get("name")) + "信息中没有员工status，无法进行匹配！");
							list1.add(String.valueOf(employeeMap.get("name")));
							list1.add(String.valueOf(employeeMap.get("employeeId")));
							continue;
						} else {
							status = Integer.valueOf(String.valueOf(employeeMap.get("status")));
						}

						if (baseSalary.get("status") == null || String.valueOf(baseSalary.get("status")).equals("")) {

							logger.info("员工：" + employeeMap.get("name") + " PayrollFlow没有status，无法进行匹配！");
							list2.add(String.valueOf(employeeMap.get("name")));
							list2.add(String.valueOf(employeeMap.get("employeeId")));
							continue;
						} else {
							pfstatus = Integer.valueOf(String.valueOf(baseSalary.get("status")));
						}

					} else {
						if (baseSalary == null) {

							logger.info("员工：" + employeeMap.get("name") + " 没有基础薪资");
							list3.add(String.valueOf(employeeMap.get("name")));
							list3.add(String.valueOf(employeeMap.get("employeeId")));

						}
						if (employeeMap == null) {

							logger.info("员工：" + employeeMap.get("name") + " 没有员工信息");
							list4.add(String.valueOf(employeeMap.get("name")));
							list4.add(String.valueOf(employeeMap.get("employeeId")));
						}
						continue;
					}

					//2-3.判断员工是否为当月离职人员，如果是离职人员但不是当月离职则不对该人员进行实时薪酬更新操作
					
					Integer employeeStatus = Integer.valueOf(String.valueOf(employeeMap.get("status")));
					
					//排除情况：是离职人员且非当月离职人员
					if(employeeStatus.equals(4)) {
						Object dimissionTime = employeeMap.get("dimissionTime");
						//离职时间为空--非离职人员
						if(dimissionTime == null || (String.valueOf(dimissionTime).equals(""))) {
							
						}else {
						//离职时间不为空
							//不是当月离职
							if(!comPareYearMonth(String.valueOf(dimissionTime))) {
								list12.add(String.valueOf(employeeMap.get("name")));
								list12.add(String.valueOf(employeeMap.get("employeeId")));
								continue;
							}else {
							//是当月离职
								list11.add(String.valueOf(employeeMap.get("name")));
								list11.add(String.valueOf(employeeMap.get("employeeId")));
							}
						}
					}
					
					// 2-4. 各类薪酬数据的判空处理，并记录标识
					Boolean flag = false; // 判断薪酬是否相等的标识
					Boolean flagBase;
					Boolean flagPeriod;
					Boolean flagPositivePayroll;
					Boolean flagPositiveSalary;

					//2-4-1.  判断是否有基础薪资
					if (!baseSalary.containsKey("erp_base_wage") 
							|| !baseSalary.containsKey("erp_post_wage") 
							|| !baseSalary.containsKey("erp_performance") 
							|| !baseSalary.containsKey("erp_allowance")
							|| !baseSalary.containsKey("erp_payroll_id")
//							|| !(String.valueOf(baseSalary.get("erp_base_wage")).equals("null")) 
//							|| !(String.valueOf(baseSalary.get("erp_post_wage")).equals("null")) 
//							|| !(String.valueOf(baseSalary.get("erp_performance")).equals("null")) 
//							|| !(String.valueOf(baseSalary.get("erp_allowance")).equals("null"))
							) {
						flagBase = false;
					} else {
						flagBase = true;
					}

					//2-4-2.  判断是否有上岗工资
					if (!periodSalary.containsKey("erp_period_base_wage")
							|| !periodSalary.containsKey("erp_period_post_wage")
							|| !periodSalary.containsKey("erp_period_performance")
							|| !periodSalary.containsKey("erp_period_allowance")
							|| !periodSalary.containsKey("erp_payroll_id")
//							|| !(String.valueOf(periodSalary.get("erp_period_base_wage")).equals("null")) 
//							|| !(String.valueOf(periodSalary.get("erp_period_post_wage")).equals("null")) 
//							|| !(String.valueOf(periodSalary.get("erp_period_performance")).equals("null")) 
//							|| !(String.valueOf(periodSalary.get("erp_period_allowance")).equals("null"))
							) {
						flagPeriod = false;
					} else {
						flagPeriod = true;
					}
					
					//2-4-3.  判断是否有上岗中的转正薪资
					if (!positiveSalary.containsKey("erp_positive_base_wage")
							|| !positiveSalary.containsKey("erp_positive_post_wage")
							|| !positiveSalary.containsKey("erp_positive_performance")
							|| !positiveSalary.containsKey("erp_positive_allowance")
							|| !positiveSalary.containsKey("erp_pst_salary_id")
//							|| !(String.valueOf(positivePayroll.get("erp_positive_base_wage")).equals("null")) 
//							|| !(String.valueOf(positivePayroll.get("erp_positive_post_wage")).equals("null")) 
//							|| !(String.valueOf(positivePayroll.get("erp_positive_performance")).equals("null")) 
//							|| !(String.valueOf(positivePayroll.get("erp_positive_income")).equals("null"))
							) {
						flagPositiveSalary = false;
					} else {
						flagPositiveSalary = true;
					}

					//2-4-4.  判断是否有转正工资
					if (!positivePayroll.containsKey("erp_positive_base_wage")
							|| !positivePayroll.containsKey("erp_positive_post_wage")
							|| !positivePayroll.containsKey("erp_positive_performance")
							|| !positivePayroll.containsKey("erp_positive_allowance")
							|| !positivePayroll.containsKey("erp_positive_id")
//							|| !(String.valueOf(positivePayroll.get("erp_positive_base_wage")).equals("null")) 
//							|| !(String.valueOf(positivePayroll.get("erp_positive_post_wage")).equals("null")) 
//							|| !(String.valueOf(positivePayroll.get("erp_positive_performance")).equals("null")) 
//							|| !(String.valueOf(positivePayroll.get("erp_positive_income")).equals("null"))
							) {
						flagPositivePayroll = false;
					} else {
						flagPositivePayroll = true;
					}

					// 2-5.是否有试用期的判断：probationEndTime 对employeeMap 和 baseSalary 进行判空，
					Object probationEndTime = employeeMap.get("probationEndTime");
					Object beginTime = employeeMap.get("beginTime");

					ErpBasePayroll erpBasePayroll = new ErpBasePayroll();
					String baseWage = null;
					String postWage = null;
					String performance = null;
					String allowance = null;
					
					Boolean flagDateCampare;
					Boolean flagNoPeriod;
					flagNoPeriod = false;
					
					if (probationEndTime != null && !String.valueOf(probationEndTime).equals("") && beginTime != null
							&& !String.valueOf(beginTime).equals("")) {
						flagDateCampare = comPareDate(String.valueOf(beginTime), String.valueOf(probationEndTime));
						if(flagDateCampare) {
							flagNoPeriod = true;
						}						
					} 
					
					
					// 2-6. 是否有试用期的分类处理
					// 2-6-1. 跨HR 服务器请求变量初始化
					url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/updateEmployeeBySalary";
					requestHeaders = new HttpHeaders();
					Map<String,Object> paramsUpdate = new HashMap<String, Object>();
					paramsUpdate.put("token",token);
					paramsUpdate.put("employeeId",employeeId);
					requestHeaders.add("token", token);
					
					String urlEmpPositive = protocolType + "nantian-erp-hr/nantian-erp/erp/postive/updateEmployeePostive"; 
					HttpHeaders requestHeadersEmpPositive = new HttpHeaders();
					Map<String,Object> paramsUpdateEmpPositive = new HashMap<String, Object>();
//					paramsUpdateEmpPositive.put("token",token);
					paramsUpdateEmpPositive.put("employeeId",employeeId);
					requestHeadersEmpPositive.add("token", token);
					
					// 2-6-2. 没有试用期的情况处理
					if (flagNoPeriod) {

						// 没有试用期，直接对比转正薪资
						logger.info("员工：" + employeeMap.get("name") + " 没有试应期，直接进行比较！");

						if (flagBase && flagPositivePayroll) {
//
							baseWage = String.valueOf(positivePayroll.get("erp_positive_base_wage"));
							baseWage = encryptDataRsa(baseWage);
//							baseWage = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_base_wage"));
//							
							postWage = String.valueOf(positivePayroll.get("erp_positive_post_wage"));
							postWage = encryptDataRsa(postWage);
//							postWage = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_post_wage"));
//							
							performance = String.valueOf(positivePayroll.get("erp_positive_performance"));
							performance = encryptDataRsa(performance);
//							performance = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_performance"));

							allowance = String.valueOf(positivePayroll.get("erp_positive_allowance"));
							allowance = encryptDataRsa(allowance);
//							allowance = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_allowance"));

							erpBasePayroll.setErpBaseWage(baseWage);
							erpBasePayroll.setErpPostWage(postWage);
							erpBasePayroll.setErpPerformance(performance);
							erpBasePayroll.setErpAllowance(allowance);
							erpBasePayroll.setErpEmployeeId(employeeId);

							erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);

							/*
							 * 将该员工的修改信息加入日志中
							 */
							ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
							basePayrollUpdateRecord.setEmployee(restTemplateUtils.findEmpNameByEmployeeId(token,employeeId));// 被修改的员工
							basePayrollUpdateRecord.setEmployeeId(employeeId);
							basePayrollUpdateRecord
									.setProcessor(employeeName == null || "".equals(employeeName) ? username : employeeName);// 修改人
							basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
							basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
							this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);

							list5.add(String.valueOf(employeeMap.get("name")));
							list5.add(String.valueOf(employeeMap.get("employeeId")));
							
							if(!employeeStatus.equals(4) && !employeeStatus.equals(3)) {
								//员工状态更新
								paramsUpdate.put("status","2");
								HttpEntity<Map<String, Object>> requestEntityNoPeriod = new HttpEntity<>(paramsUpdate, requestHeaders);
								response = restTemplate.postForEntity(url, requestEntityNoPeriod, Map.class);
								
								if (response.getStatusCodeValue() != 200) {
									logger.info("调用HR工程updateEmployeeBySalary接口执行失败");
									return RestUtils.returnFailure("调用HR工程updateEmployeeBySalary接口执行失败,无法进行更新操作");
								} else {
									
								}
							}					
							
							//员工转正状态更新
							paramsUpdateEmpPositive.put("status","3");
							HttpEntity<Map<String, Object>> requestEntityEmpPositive = new HttpEntity<>(paramsUpdateEmpPositive, requestHeadersEmpPositive);
							response = restTemplate.postForEntity(urlEmpPositive, requestEntityEmpPositive, Map.class);
							
							if (response.getStatusCodeValue() != 200) {
								logger.info("调用HR工程updateEmployeePostive接口执行失败");
								return RestUtils.returnFailure("调用HR工程updateEmployeePostive接口执行失败,无法进行更新操作");
							} else {
								
							}
							continue;
						}else if(flagBase && flagPositiveSalary)  {
							
							baseWage = String.valueOf(positiveSalary.get("erp_positive_base_wage"));
							baseWage = encryptDataRsa(baseWage);
//							baseWage = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_base_wage"));
//							
							postWage = String.valueOf(positiveSalary.get("erp_positive_post_wage"));
							postWage = encryptDataRsa(postWage);
//							postWage = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_post_wage"));
//							
							performance = String.valueOf(positiveSalary.get("erp_positive_performance"));
							performance = encryptDataRsa(performance);
//							performance = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_performance"));

							allowance = String.valueOf(positiveSalary.get("erp_positive_allowance"));
							allowance = encryptDataRsa(allowance);
//							allowance = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_allowance"));

							erpBasePayroll.setErpBaseWage(baseWage);
							erpBasePayroll.setErpPostWage(postWage);
							erpBasePayroll.setErpPerformance(performance);
							erpBasePayroll.setErpAllowance(allowance);
							erpBasePayroll.setErpEmployeeId(employeeId);

							erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);
							/*
							 * 将该员工的修改信息加入日志中
							 */
							ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
							basePayrollUpdateRecord.setEmployee(restTemplateUtils.findEmpNameByEmployeeId(token,employeeId));// 被修改的员工
							basePayrollUpdateRecord.setEmployeeId(employeeId);
							basePayrollUpdateRecord
									.setProcessor(employeeName == null || "".equals(employeeName) ? username : employeeName);// 修改人
							basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
							basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
							this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);

							list5_1.add(String.valueOf(employeeMap.get("name")));
							list5_1.add(String.valueOf(employeeMap.get("employeeId")));
							
							if(!employeeStatus.equals(4) && !employeeStatus.equals(3)) {
								
								//员工状态更新
								paramsUpdate.put("status","2");
								
								HttpEntity<Map<String, Object>> requestEntityNoPeriod = new HttpEntity<>(paramsUpdate, requestHeaders);
								response = restTemplate.postForEntity(url, requestEntityNoPeriod, Map.class);
								
								if (response.getStatusCodeValue() != 200) {
									logger.info("调用HR工程updateEmployeeBySalary接口执行失败");
									return RestUtils.returnFailure("调用HR工程updateEmployeeBySalary接口执行失败,无法进行更新操作");
								} else {
									
								}
							}
							
							//员工转正状态更新
							paramsUpdateEmpPositive.put("status","3");
							HttpEntity<Map<String, Object>> requestEntityEmpPositive = new HttpEntity<>(paramsUpdateEmpPositive, requestHeadersEmpPositive);
							response = restTemplate.postForEntity(urlEmpPositive, requestEntityEmpPositive, Map.class);
							
							if (response.getStatusCodeValue() != 200) {
								logger.info("调用HR工程updateEmployeePostive接口执行失败");
								return RestUtils.returnFailure("调用HR工程updateEmployeePostive接口执行失败,无法进行更新操作");
							} else {
								
							}
							
							continue;
							
						}else {
							list6.add(String.valueOf(employeeMap.get("name")));
							list6.add(String.valueOf(employeeMap.get("employeeId")));
							continue;
						}
					} else {
					// 2-6-3 有试用期的情况处理
						// 2-6-3-1. 有试应期，但数据异常情况：
						//   		获取dateCompareFlag：按照员工转正日期跟当前日期比较大小分类进行判断
						if(String.valueOf(probationEndTime) == null || String.valueOf(probationEndTime).equals("")) {
							list6.add(String.valueOf(employeeMap.get("name")));
							list6.add(String.valueOf(employeeMap.get("employeeId")));
							logger.info("员工：" + employeeMap.get("name") + " probationEndTime 为 null 或者 空字符串");
							continue;
						}
						
						dateCompareFlag = comPareDate(String.valueOf(probationEndTime));

						// 2-6-3-2. 有试应期，但数据正常情况， 判断逻辑如下：
						/*
						 * 1. 找到所有入职员工【有简历ID并且有offerID的非离职员工】。
						 * 2. 以1查询到的人员在薪酬中查找 实时薪资、上岗薪资、转正薪资都有数据的，
						 * 		如果转正日期 < 当前日期，将 转正薪资 更新到 实时薪资 中。 更新员工状态为“正式员工
						 * 3.以1查询到的人员在薪酬中查找 实时薪资、上岗薪资、转正薪资都有数据的，
						 * 		如果转正日期 >= 当前日期，将上岗薪资 更新到实时薪资中。更新员工状态为“试用期员工” 
						 * 4. 以1查询到的人员在薪酬中查找 实时薪资 和 上岗薪资都存在，转正薪资没有的，
						 *		如果转正日期 >=当前日期，将上岗薪资 更新到 实时薪资中。更新员工状态为“试用期员工”。
						 * 5.其余情况不做处理
						 */
						// 逻辑的步骤2种情况
						if (flagBase && flagPeriod && flagPositivePayroll && dateCompareFlag) {
							baseWage = String.valueOf(positivePayroll.get("erp_positive_base_wage"));
							baseWage = encryptDataRsa(baseWage);
//							baseWage = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_base_wage"));
							
							postWage = String.valueOf(positivePayroll.get("erp_positive_post_wage"));
							postWage = encryptDataRsa(postWage);
//							postWage = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_post_wage"));
							
							performance = String.valueOf(positivePayroll.get("erp_positive_performance"));
							performance = encryptDataRsa(performance);
//							performance = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_performance"));
														
							allowance = String.valueOf(positivePayroll.get("erp_positive_allowance"));
							allowance = encryptDataRsa(allowance);
//							allowance = String.valueOf(payRollFlowUnionPositivepeyrollRepica.get(key).get("erp_positive_allowance"));
							
							erpBasePayroll =  new ErpBasePayroll();
							erpBasePayroll.setErpBaseWage(baseWage);
							erpBasePayroll.setErpPostWage(postWage);
							erpBasePayroll.setErpPerformance(performance);
							erpBasePayroll.setErpAllowance(allowance);
							
							erpBasePayroll.setErpEmployeeId(employeeId);
							erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);

							/*
							 * 将该员工的修改信息加入日志中
							 */
							ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
							basePayrollUpdateRecord.setEmployee(restTemplateUtils.findEmpNameByEmployeeId(token,employeeId));// 被修改的员工
							basePayrollUpdateRecord.setEmployeeId(employeeId);
							basePayrollUpdateRecord
									.setProcessor(employeeName == null || "".equals(employeeName) ? username : employeeName);// 修改人
							basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
							basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
							this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);
							
							list7.add(String.valueOf(employeeMap.get("name")));
							list7.add(String.valueOf(employeeMap.get("employeeId")));
					
							// 跨工程更新员工状态
							if(!employeeStatus.equals(4) && !employeeStatus.equals(3)) {
								//员工状态更新
								paramsUpdate.put("status","2");
								
								HttpEntity<Map<String, Object>> requestEntityPositive = new HttpEntity<>(paramsUpdate, requestHeaders);
								response = restTemplate.postForEntity(url, requestEntityPositive, Map.class);
								
								if (response.getStatusCodeValue() != 200) {
									logger.info("调用HR工程updateEmployeeBySalary接口执行失败");
									return RestUtils.returnFailure("调用HR工程updateEmployeeBySalary接口执行失败,无法进行更新操作");
								} else {
									
								}
							}
							
							//员工转正状态更新
							paramsUpdateEmpPositive.put("status","3");
							HttpEntity<Map<String, Object>> requestEntityEmpPositive = new HttpEntity<>(paramsUpdateEmpPositive, requestHeadersEmpPositive);
							response = restTemplate.postForEntity(urlEmpPositive, requestEntityEmpPositive, Map.class);
							
							if (response.getStatusCodeValue() != 200) {
								logger.info("调用HR工程updateEmployeePostive接口执行失败");
								return RestUtils.returnFailure("调用HR工程updateEmployeePostive接口执行失败,无法进行更新操作");
							} else {
								
							}
							
							continue;

						} else if (flagBase && flagPeriod && flagPositivePayroll && !dateCompareFlag) {
						// 逻辑的步骤3种情况
							baseWage = String.valueOf(periodSalary.get("erp_period_base_wage"));
							baseWage = encryptDataRsa(baseWage);
//							baseWage = String.valueOf(payRollFlowUnionperiodParollReplica.get(key).get("erp_period_base_wage"));
							
							postWage = String.valueOf(periodSalary.get("erp_period_post_wage"));
							postWage = encryptDataRsa(postWage);
//							postWage = String.valueOf(payRollFlowUnionperiodParollReplica.get(key).get("erp_period_post_wage"));
							
							performance = String.valueOf(periodSalary.get("erp_period_performance"));
							performance = encryptDataRsa(performance);
//							performance = String.valueOf(payRollFlowUnionperiodParollReplica.get(key).get("erp_period_performance"));
							
							allowance = String.valueOf(periodSalary.get("erp_period_allowance"));
							allowance = encryptDataRsa(allowance);
//							allowance = String.valueOf(payRollFlowUnionperiodParollReplica.get(key).get("erp_period_allowance"));
												
							erpBasePayroll =  new ErpBasePayroll();
							erpBasePayroll.setErpBaseWage(baseWage);
							erpBasePayroll.setErpPostWage(postWage);
							erpBasePayroll.setErpPerformance(performance);
							erpBasePayroll.setErpAllowance(allowance);
							erpBasePayroll.setErpEmployeeId(employeeId);
							erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);
							/*
							 * 将该员工的修改信息加入日志中
							 */
							ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
							basePayrollUpdateRecord.setEmployee(restTemplateUtils.findEmpNameByEmployeeId(token,employeeId));// 被修改的员工
							basePayrollUpdateRecord.setEmployeeId(employeeId);
							basePayrollUpdateRecord
									.setProcessor(employeeName == null || "".equals(employeeName) ? username : employeeName);// 修改人
							basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
							basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
							this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);
							
							list8.add(String.valueOf(employeeMap.get("name")));
							list8.add(String.valueOf(employeeMap.get("employeeId")));
							
							// 跨工程更新员工状态
							if(!employeeStatus.equals(4) && !employeeStatus.equals(3)) {
								paramsUpdate.put("status","1");
								
								HttpEntity<Map<String, Object>> requestEntityPositive = new HttpEntity<>(paramsUpdate, requestHeaders);
								response = restTemplate.postForEntity(url, requestEntityPositive, Map.class);
								
								if (response.getStatusCodeValue() != 200) {
									logger.info("调用HR工程updateEmployeeBySalary接口执行失败");
									return RestUtils.returnFailure("调用HR工程updateEmployeeBySalary接口执行失败,无法进行更新操作");
								} else {
									
								}
							}
								
							continue;

						} else if (flagBase && flagPeriod && !flagPositivePayroll && !dateCompareFlag) {
						// 逻辑的步骤4种情况
							baseWage = String.valueOf(periodSalary.get("erp_period_base_wage"));
							baseWage = encryptDataRsa(baseWage);
//							baseWage = String.valueOf(payRollFlowUnionperiodParollReplica.get(key).get("erp_period_base_wage"));
							
							postWage = String.valueOf(periodSalary.get("erp_period_post_wage"));
							postWage = encryptDataRsa(postWage);
//							postWage = String.valueOf(payRollFlowUnionperiodParollReplica.get(key).get("erp_period_post_wage"));
							
							performance = String.valueOf(periodSalary.get("erp_period_performance"));
							performance = encryptDataRsa(performance);
//							performance = String.valueOf(payRollFlowUnionperiodParollReplica.get(key).get("erp_period_performance"));
							
							allowance = String.valueOf(periodSalary.get("erp_period_allowance"));
							allowance = encryptDataRsa(allowance);
//							allowance = String.valueOf(payRollFlowUnionperiodParollReplica.get(key).get("erp_period_allowance"));
																										
							erpBasePayroll =  new ErpBasePayroll();
							erpBasePayroll.setErpBaseWage(baseWage);
							erpBasePayroll.setErpPostWage(postWage);
							erpBasePayroll.setErpPerformance(performance);
							erpBasePayroll.setErpAllowance(allowance);
							erpBasePayroll.setErpEmployeeId(employeeId);
							erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);
							/*
							 * 将该员工的修改信息加入日志中
							 */
							ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
							basePayrollUpdateRecord.setEmployee(restTemplateUtils.findEmpNameByEmployeeId(token,employeeId));// 被修改的员工
							basePayrollUpdateRecord.setEmployeeId(employeeId);
							basePayrollUpdateRecord
									.setProcessor(employeeName == null || "".equals(employeeName) ? username : employeeName);// 修改人
							basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
							basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
							this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);

							list9.add(String.valueOf(employeeMap.get("name")));
							list9.add(String.valueOf(employeeMap.get("employeeId")));
							
							// 跨工程更新员工状态
							if(!employeeStatus.equals(4) && !employeeStatus.equals(3)) {
								paramsUpdate.put("status","1");
								
								HttpEntity<Map<String, Object>> requestEntityPositive = new HttpEntity<>(paramsUpdate, requestHeaders);
								response = restTemplate.postForEntity(url, requestEntityPositive, Map.class);
								
								if (response.getStatusCodeValue() != 200) {
									logger.info("调用HR工程updateEmployeeBySalary接口执行失败");
									return RestUtils.returnFailure("调用HR工程updateEmployeeBySalary接口执行失败,无法进行更新操作");
								} else {
									
								}
							}
										
							continue;
						} else {
						// 逻辑的步骤5种情况
							logger.info("员工：" + employeeMap.get("name") + " 其它情况，不做处理");
							list10.add(String.valueOf(employeeMap.get("name")));
							list10.add(String.valueOf(employeeMap.get("employeeId")));
						}
					}
				}

				logger.info("List1:{}\n", list1);
				logger.info("List2:{}\n", list2);
				logger.info("List3:{}\n", list3);
				logger.info("List4:{}\n", list4);
				logger.info("List5:{}\n", list5);
				logger.info("List5_1:{}\n", list5_1);
				logger.info("List6:{}\n", list6);
				logger.info("List7:{}\n", list7);
				logger.info("List8:{}\n", list8);
				logger.info("List9:{}\n", list9);
				logger.info("List10:{}\n", list10);
				logger.info("List11:{}\n", list11);
				logger.info("List12:{}\n", list12);
				return RestUtils.returnSuccess("OK");
			}
		} catch (Exception e) {
			logger.error("updateBaseSalary出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("updateBaseSalary方法出现异常：" + e.getMessage());
		}
	}
}
