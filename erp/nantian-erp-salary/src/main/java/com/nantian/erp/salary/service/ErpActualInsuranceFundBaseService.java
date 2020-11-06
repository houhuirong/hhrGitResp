package com.nantian.erp.salary.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.binding.corba.wsdl.Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.dao.ErpActualInsuranceFundBaseMapper;
import com.nantian.erp.salary.data.dao.ErpBasePayrollMapper;
import com.nantian.erp.salary.data.dao.ErpSocialSecurityMapper;
import com.nantian.erp.salary.data.model.ErpActualInsuranceFundBase;
import com.nantian.erp.salary.data.model.ErpBasePayroll;
import com.nantian.erp.salary.data.model.ErpSocialSecurity;
import com.nantian.erp.salary.data.vo.EmployeeQueryByDeptUserVo;
import com.nantian.erp.salary.util.AesUtils;

/**
 * Description: 员工社保公积金基数
 *
 * @author songxiugong
 * @date 2020年02月18日
 * @version 1.0
 */
@Service
@PropertySource(value = { "classpath:config/sftp.properties", "file:${spring.profiles.path}/config/sftp.properties",
		"classpath:config/email.properties", "file:${spring.profiles.path}/config/email.properties",
		"classpath:config/host.properties",
		"file:${spring.profiles.path}/config/host.properties" }, ignoreResourceNotFound = true)
public class ErpActualInsuranceFundBaseService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private ErpActualInsuranceFundBaseMapper erpActualInsuranceFundBaseMapper;

	@Autowired
	private ErpBasePayrollMapper erpBasePayrollMapper;

	@Autowired
	private ErpSocialSecurityMapper erpSocialSecurityMapper;

	@Autowired
	RestTemplate restTemplate;

	@Value("${environment.type}")
	private  String environmentType;//环境类型（test或者prod）

	@Value("${protocol.type}")
	private String protocolType;// http或https

	public String getLocalHostName(String environmentType){
		logger.info("返回环境类型environmentType:"+environmentType);
		if("prod".equals(environmentType)){
			return "Eureka1".toLowerCase();
		}else{
			return "java01";
		}
	}

	/**
	 * Description: 插入或者更新员工的当前月份的社保公积金比例基数数据
	 *
	 * @return
	 * @Author songxiugong
	 * @Create Date: 2020年02月18日
	 */
	public RestResponse insertActualInsuranceFundBase() throws Exception{
		if(!InetAddress.getLocalHost().toString().contains(getLocalHostName(environmentType))){//Eureka1
			logger.info("返回本地主机的地址:"+InetAddress.getLocalHost().toString());
			return RestUtils.returnSuccess("初始化完毕", "OK");
		}else{
			logger.info("返回本地主机的地址:"+InetAddress.getLocalHost().toString());
			logger.info("insertActualInsuranceFundBase Begin，方法无参数");
			String info = null;
			String returnInfo = null;

			try {
				// 0.跨HR工程调用获取所有有效的用户【当前月份离职的和当前月份入职的所有员工（包含实习生）】
				Map<String, Object> queryMap = new HashMap<String, Object>(); // 用户对象 map
				String curDate = getCurYearMonthDay();

				// 0-1.月份的第一天 YYYY-MM-DD 2020-02-01
				String firstDate = getCurYearMonthFirstDay();
				queryMap.put("dimissionTime", firstDate);

				// 0-2.月份的最后一天格式 YYYY-MM-DD 2020-02-29 2019-02-28 2020-03-31 2020-04-30
				Date monthLastDate = ExDateUtils.getMonthEnd(ExDateUtils.convertToDate(curDate));
				String lastDate = dateToString(monthLastDate);
				queryMap.put("entryTime", lastDate);

				// status 员工状态
				queryMap.put("status", "1,2,3,4");
				queryMap.put("roles", "7");

				String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmployeeByDeptAndUserNoToken";
				HttpHeaders requestHeaders = new HttpHeaders();
				//requestHeaders.add("token", token);

				HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(queryMap, requestHeaders);

				ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request,
						RestResponse.class);
				RestResponse response = responseEntity.getBody();
				// 1-1 如果没有获取到数据

				Map<String, Object> valueReturn = new HashMap<String, Object>(); // 返回提示信息

				if (null == response.getData() || "".equals(response.getData())) {
					logger.info("获取数据返回结果为空！");
					valueReturn.put("employeList", new ArrayList<String>());
					valueReturn.put("count", 0);
					return RestUtils.returnSuccess(valueReturn, "OK");
				}

				if (!response.getMsg().equals("OK")) {
					if (response.getMsg().equals("NotAuth")) {
						valueReturn.put("employeList", new ArrayList<String>());
						valueReturn.put("count", 0);
						return RestUtils.returnSuccess(valueReturn, "OK");
					}
					logger.info("获取数据返回数据！");
					valueReturn.put("employeList", new ArrayList<String>());
					valueReturn.put("count", 0);
					return RestUtils.returnSuccess(valueReturn, "OK");
				}

				// 解析返回结果
				List<Map<String, Object>> erpUserList = new ArrayList<Map<String, Object>>();
				Map<String, Object> returnedValue = new HashMap<String, Object>();

				returnedValue = (Map<String, Object>) response.getData();
				erpUserList = (List<Map<String, Object>>) (returnedValue.get("employeList"));

				// 1.获取当前月份的社保公积金比例基数数据
				ErpSocialSecurity erpSocialSecurity = erpSocialSecurityMapper.selectSocialSecurityLastOne();

				// 2.获取所有入职员工的实时薪资表【包含实习生】
				List<ErpBasePayroll> erpBasePayrollList = erpBasePayrollMapper.findBasePayrollAll();

				// 2-1.过滤实时薪资【将符合需求的筛选出来（通过HR查出的数据是有效的人员）】
				Map<String, Object> alertInfo = new HashMap<String, Object>();
				List<ErpBasePayroll> basePayrollListNew = new ArrayList<ErpBasePayroll>();
				basePayrollListNew = this.getRequiredBasePayrollList(erpBasePayrollList, erpUserList);
				if (basePayrollListNew == null) {
					alertInfo.put("匹配HR失败，请联系管理员！", info);
					return RestUtils.returnSuccess(alertInfo, "OK");
				}

				int iPlus = 0;
				for (iPlus = 0; iPlus < basePayrollListNew.size(); iPlus++) {

					ErpBasePayroll erpBasePayroll = basePayrollListNew.get(iPlus);

//				// 2.1如果基础薪资为null，则代表用户的上岗工资单没有生成，是实习生，不能生成对应的社保公积金基数数据
//				if (erpBasePayroll.getErpBaseWage().equals("") || erpBasePayroll.getErpBaseWage().equals("null")) {
//					info = erpBasePayroll.getErpEmployeeId().toString() + "用户的上岗工资单没有生成，不能生成对应的社保公积金基数数据D\n";
//					returnInfo = returnInfo + info;
//					logger.info(returnInfo);
//					continue;
//				}

					// 2.2解密数据 并转化成float类型数据
					String insuranceBase = erpBasePayroll.getErpSocialSecurityBase(); // 社保基数
					String fundBase = erpBasePayroll.getErpAccumulationFundBase(); // 公积金基数

//				if (insuranceBase == null || insuranceBase.equals("") || insuranceBase.equals("null")) {
//					info = erpBasePayroll.getErpEmployeeId().toString() + "实施薪资没有填写社保基数，社保公积金基数数据不生成或更新A\n";
//					returnInfo = returnInfo + info;
//					logger.info(returnInfo);
//					continue;
//				}
//
//				if (fundBase == null || fundBase.equals("") || fundBase.equals("null")) {
//					info = erpBasePayroll.getErpEmployeeId().toString() + "实施薪资没有填写公积金基数，社保公积金基数数据不生成或更新B\n";
//					returnInfo = returnInfo + info;
//					logger.info(returnInfo);
//					continue;
//				}

					insuranceBase = decryptDataRsa(insuranceBase);
					fundBase = decryptDataRsa(fundBase);

					double insuranceBaseDouble = (insuranceBase.equals("") || insuranceBase.equals("null")) ? 0.0
							: Double.valueOf(insuranceBase).doubleValue(); // 社保基数
					double fundBaseDouble = (fundBase.equals("") || fundBase.equals("null")) ? 0.0
							: Double.valueOf(fundBase).doubleValue(); // 公积金基数

					// 3.获取准确的社保基数(五种)和公积金基数(一种)
					double actualEndowmentInsuranceBase; // 员工真正的养老保险基数
					double actualUnemploymentInsuranceBase; // 员工真正的失业保险基数
					double actualMaternityInsuranceBase; // 员工真正的生育保险基数
					double actualMedicalInsuranceBase; // 员工真正的医疗保险基数
					double actualInjuryInsuranceBase; // 员工真正的工伤保险基数
					double actualAccumulationFundBase; // 员工真正的公积金基数

					Map<String, Double> actualInsuranceFund = this.insuranceFundDoubleValueAsMap(erpSocialSecurity,
							fundBaseDouble, insuranceBaseDouble);
					if (actualInsuranceFund.get("status").doubleValue() == -1.0) {
						info = erpBasePayroll.getErpEmployeeId().toString() + "：的社保公积金基数获取异常，社保公积金基数数据不生成或更新C";
						returnInfo = returnInfo + info;
						logger.info(returnInfo);
						continue;
					}

					actualAccumulationFundBase = actualInsuranceFund.get("actualAccumulationFundBase").doubleValue(); // 员工真正的公积金基数
					actualEndowmentInsuranceBase = actualInsuranceFund.get("actualEndowmentInsuranceBase").doubleValue(); // 员工真正的养老保险基数
					actualUnemploymentInsuranceBase = actualInsuranceFund.get("actualUnemploymentInsuranceBase")
							.doubleValue(); // 员工真正的失业保险基数
					actualMaternityInsuranceBase = actualInsuranceFund.get("actualMaternityInsuranceBase").doubleValue(); // 员工真正的生育保险基数
					actualMedicalInsuranceBase = actualInsuranceFund.get("actualMedicalInsuranceBase").doubleValue(); // 员工真正的医疗保险基数
					actualInjuryInsuranceBase = actualInsuranceFund.get("actualInjuryInsuranceBase").doubleValue(); // 员工真正的工伤保险基数

					// 4.生成员工当前月份的社保公积金基数（先查询员工的社保公积金基数,如果已经存在，则更新）
					// 4-1.加密数据并保存到 ErpActualInsuranceFundBase 定义的对象中
					ErpActualInsuranceFundBase erpActualInsuranceFundBase = new ErpActualInsuranceFundBase();
					erpActualInsuranceFundBase
							.setAccumulationFundBase(encryptDataRsa(String.valueOf(actualAccumulationFundBase)));
					erpActualInsuranceFundBase
							.setEndowmentInsuranceBase(encryptDataRsa(String.valueOf(actualEndowmentInsuranceBase)));
					erpActualInsuranceFundBase
							.setUnemploymentInsuranceBase(encryptDataRsa(String.valueOf(actualUnemploymentInsuranceBase)));
					erpActualInsuranceFundBase
							.setMaternityInsuranceBase(encryptDataRsa(String.valueOf(actualMaternityInsuranceBase)));
					erpActualInsuranceFundBase
							.setMedicalInsuranceBase(encryptDataRsa(String.valueOf(actualMedicalInsuranceBase)));
					erpActualInsuranceFundBase
							.setInjuryInsuranceBase(encryptDataRsa(String.valueOf(actualInjuryInsuranceBase)));
					erpActualInsuranceFundBase.setEmployeeId(Integer.valueOf(erpBasePayroll.getErpEmployeeId()));

					// 当前日期及格式化
					Date dateNow = Calendar.getInstance().getTime();
					SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat formatYMhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					// 将tookEffectMonth字段值的日期调整为每个月的1日号
					Calendar calendar = Calendar.getInstance();
					Integer year = calendar.get(Calendar.YEAR);
					Integer month = calendar.get(Calendar.MONTH) + 1;
					String tookEffectMonth = String.valueOf(year) + "-" + String.valueOf(month) + "-" + "01";

					erpActualInsuranceFundBase.setTookEffectDate(formatYMD.format(formatYMD.parse(tookEffectMonth)));
					erpActualInsuranceFundBase.setGmtCreate(formatYMhms.format(dateNow));
					erpActualInsuranceFundBase.setGmtModified(formatYMhms.format(dateNow));

					// 4-2.先查询员工的社保公积金基数
					Map<String, Object> paramsSearch = new HashMap<String, Object>();
					paramsSearch.put("employeeId", erpBasePayroll.getErpEmployeeId());
					ErpActualInsuranceFundBase erpActualInsuranceFundBaseExisted = erpActualInsuranceFundBaseMapper
							.selectActualInsuranceFundBaseByEmployeeID(paramsSearch);
					if (erpActualInsuranceFundBaseExisted != null) {
						// 4-3.更新员工的社保公积金基数
						erpActualInsuranceFundBaseMapper.updateActualInsuranceFundBase(erpActualInsuranceFundBase);
					} else {
						// 4-3.插入员工的社保公积金基数
						erpActualInsuranceFundBaseMapper.insertActualInsuranceFundBase(erpActualInsuranceFundBase);
					}

				}

				logger.info("insertActualInsuranceFundBase End");
				if (returnInfo != null) {
					logger.info(returnInfo);
					return RestUtils.returnSuccess("初始化完毕", "OK");
				} else {
					return RestUtils.returnSuccess("初始化完毕", "OK");
				}
			} catch (Exception e) {
				logger.error("insertActualInsuranceFundBase 出现异常:" + e.getMessage(), e);
				return RestUtils.returnFailure("insertActualInsuranceFundBaseu异常。" + "出现异常的数据： " + returnInfo);
			}
		}
	}

	/**
	 * Description: 过滤有效数据
	 * 
	 * @return erpBasePayrollList
	 * @Author songxiugong
	 * @Create Date: 2020年02月29日
	 */
	public List<ErpBasePayroll> getRequiredBasePayrollList(List<ErpBasePayroll> basePayrollList,
			List<Map<String, Object>> employeeList) {
		List<ErpBasePayroll> basePayrollListNew = new ArrayList<ErpBasePayroll>();
		try {
			for (int iplus = 0; iplus < employeeList.size(); iplus++) {

				Map<String, Object> employee = employeeList.get(iplus);
				Integer employeeId = Integer.valueOf(String.valueOf(employee.get("employeeId")));

				for (int jplus = 0; jplus < basePayrollList.size(); jplus++) {
					ErpBasePayroll basePayroll = basePayrollList.get(jplus);

					if (employeeId.equals(basePayroll.getErpEmployeeId())) {
						basePayrollListNew.add(basePayroll);
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.error("getRequiredBasePayrollList 出现异常:" + e.getMessage(), e);
		}
		return basePayrollListNew;
	}

	/**
	 * Description: 获取当前的日期 ，格式YYYY-MM-DD
	 * 
	 * @return 当前的日期 String ，格式YYYY-MM-DD
	 * @Author songxiugong
	 * @Create Date: 2020年02月29日
	 */
	public String getCurYearMonthDay() {

		String dateReturn = null;
		try {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			int day = calendar.get(Calendar.DAY_OF_MONTH);

			String monthStr = month > 9 ? String.valueOf(month) : "0" + String.valueOf(month);
			String dayStr = day > 9 ? String.valueOf(day) : "0" + String.valueOf(day);

			dateReturn = String.valueOf(year) + "-" + monthStr + "-" + dayStr;
		} catch (Exception e) {
			logger.error("getCurYearMonthDay 出现异常:" + e.getMessage(), e);
		}
		return dateReturn;
	}

	/**
	 * Description: 获取当前月份第一天日期 ，格式YYYY-MM-01
	 * 
	 * @return 当前的日期 String ，格式YYYY-MM-01
	 * @Author songxiugong
	 * @Create Date: 2020年02月29日
	 */
	public String getCurYearMonthFirstDay() {

		String dateReturn = null;
		try {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;

			String monthStr = month > 9 ? String.valueOf(month) : "0" + String.valueOf(month);

			dateReturn = String.valueOf(year) + "-" + monthStr + "-01";
		} catch (Exception e) {
			logger.error("getCurYearMonthFirstDay 出现异常:" + e.getMessage(), e);
		}
		return dateReturn;
	}

	/**
	 * Description: 日期转为字符串
	 * 
	 * @return 当前的日期 String ，格式YYYY-MM-DD
	 * @Author songxiugong
	 * @Create Date: 2020年02月29日
	 */
	public String dateToString(Date date) {
		String monthLastDate = null;
		try {
			SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM-dd");
			monthLastDate = formatYMD.format(date);

		} catch (Exception e) {
			logger.error("dateToString 出现异常:" + e.getMessage(), e);
		}
		return monthLastDate;
	}

	/**
	 * Description: 根据传入的员工ID、部门ID、月份查找员工的社保公积金比例基数数据 逻辑：1.先查询HR中的人员部门信息并进行分页
	 * 注意：对HR的查询是进行分页查询的，由于每个人的社保公积金数据是加密的，以减少一次查出时引起的性能问题
	 * 2.根据查询到的人员部门信息查询社保公积金基数并进行解密 注意：对数据库通过一次查询将数查出来，减少多次访问数据库引起的性能问题
	 * 对查出的社保公积金各项数据只进行了一次解密，提高性能 3.通过员工ID将两者查询到的值进行关联后返回
	 * 
	 * @return
	 * @Author songxiugong
	 * @Create Date: 2020年02月18日
	 */
	public RestResponse searchInsuranceFundByParameters(String token, Map<String, Object> params) {
		try {
			// 当前日期及格式化
			Date dateNow = Calendar.getInstance().getTime();
			SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat formatYMhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			// 将tookEffectMonth字段值的日期调整为每个月的1日号
			Calendar calendar = Calendar.getInstance();
			Integer yearCalendar = calendar.get(Calendar.YEAR);
			Integer monthCalendar = calendar.get(Calendar.MONTH) + 1;
			String tookEffectMonth = String.valueOf(yearCalendar) + "-" + String.valueOf(monthCalendar) + "-" + "01";

			String info = null;
			String returnInfo = null;
			// 0.获取各个参数配置赋值给查询参数（Map）
			// 0-1 给查询HR的参数赋值
			Map<String, Object> queryMap = new HashMap<String, Object>(); // 用户对象 map
			String deptId, employeeName;
			Integer employeeStatus;
			// 0-1-plus entryTime dimissionTime 当前月份
			String month = ExDateUtils.dateToString(ExDateUtils.getToday(), "yyyy-MM");
//			String month;
//			month = String.valueOf(params.get("month"));
//			if (month != null) {
//				if (month != "" && month != "null") {
					queryMap.put("entryTime", month + "-31");
					queryMap.put("dimissionTime", month + "-01");
//				}
//			}

			// deptId
			deptId = String.valueOf(params.get("deptId"));
			if (deptId != null) {
				if (deptId != "" && deptId != "null") {
					queryMap.put("deptId", Integer.valueOf(deptId));
				}
			}

			// employeeName
			employeeName = String.valueOf(params.get("employeeName"));
			if (employeeName != null) {
				if (employeeName != "" && employeeName != "null") {
					queryMap.put("employeeName", employeeName);
				}
			}
			if (params.get("employeeStatus") != null) {
				employeeStatus = Integer.valueOf(String.valueOf(params.get("employeeStatus")));
				if(employeeStatus == 0){
					//在职
					queryMap.put("status", "0,1,2,3");
				}else if(employeeStatus == 1){
					//离职
					queryMap.put("status", "4");
				}else{
					queryMap.put("status", "0,1,2,3");
				}
			}


			// page
			String page = String.valueOf(params.get("page"));
			String limit = String.valueOf(params.get("limit"));
			if (page != null && limit != null) {
				if (page != "" && page != "null" && limit != "" && limit != "null") {
					queryMap.put("page", Integer.valueOf(page));
					queryMap.put("limit", Integer.valueOf(limit));
				}
			}

			// 1.从HR中找人员部门信息【默认（没有选择部门的时候）显示所有管辖的部门人员信息】
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmployeeByDeptAndUser";
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);

			HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(queryMap, requestHeaders);

			ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request,
					RestResponse.class);
			RestResponse response = responseEntity.getBody();
			// 1-1 如果没有获取到数据

			Map<String, Object> valueReturn = new HashMap<String, Object>(); // 返回提示信息

			if (null == response.getData() || "".equals(response.getData())) {
				logger.info("获取数据返回结果为空！");
				valueReturn.put("employeList", new ArrayList<String>());
				valueReturn.put("count", 0);
				return RestUtils.returnSuccess(valueReturn, "OK");
			}

			if (!response.getMsg().equals("OK")) {
				if (response.getMsg().equals("NotAuth")) {
					valueReturn.put("employeList", new ArrayList<String>());
					valueReturn.put("count", 0);
					return RestUtils.returnSuccess(valueReturn, "OK");
				}
				logger.info("获取数据返回数据！");
				valueReturn.put("employeList", new ArrayList<String>());
				valueReturn.put("count", 0);
				return RestUtils.returnSuccess(valueReturn, "OK");
			}

			// 解析返回结果
			List<Map<String, Object>> erpUserList = new ArrayList<Map<String, Object>>();
			Map<String, Object> returnedValue = new HashMap<String, Object>();

			returnedValue = (Map<String, Object>) response.getData();
			erpUserList = (List<Map<String, Object>>) (returnedValue.get("employeList"));

			if (!response.getMsg().equals("OK")) {
				if (response.getMsg().equals("NotAuth")) {
					valueReturn.put("employeList", new ArrayList<String>());
					valueReturn.put("count", 0);
					return RestUtils.returnSuccess(valueReturn, "OK");
				}
				logger.info("获取数据返回异常！");
				valueReturn.put("employeList", new ArrayList<String>());
				valueReturn.put("count", 0);
				return RestUtils.returnSuccess(valueReturn, "OK");
			}

			if (erpUserList == null || erpUserList.size() == 0) {
				logger.info("指定部门没有获取到人员信息！");
				valueReturn.put("employeList", new ArrayList<String>());
				valueReturn.put("count", 0);
				return RestUtils.returnSuccess(valueReturn, "OK");
			}

			// 2.根据找的员工信息取获取员工对应的社保公积金比例基数数据，然后给前端进行展示。默认获取所管辖所有人员的社保公积金比例基数数据。

			// 2-1 查询社保公积金
			// 2-1-1给查询参数赋值
			Map<String, Object> queryInsuranceFundMap = new HashMap<String, Object>();

			if (month != null) {
				if (month != "" && month != "null") {

					month = month + "-01";
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					queryInsuranceFundMap.put("month", format.format(format.parse(month)));
				}
			}

			// 2-2-2 待查询的人员
			List<Integer> userList = new ArrayList<Integer>();
			int plus;
			for (plus = 0; plus < erpUserList.size(); plus++) {
				Map<String, Object> employeeInfo = erpUserList.get(plus);
				Integer temp = Integer.valueOf(String.valueOf(employeeInfo.get("employeeId")));

				// 字段值为null的字段赋值为""
				for (Map.Entry<String, Object> employee : employeeInfo.entrySet()) {
					String key = employee.getKey();
					String value = String.valueOf(employee.getValue());

					if (value == null || value.equals("null")) {
						employeeInfo.put(key, "");
					}
				}
				userList.add(temp);
			}
			queryInsuranceFundMap.put("list", userList);

			if (userList.size() == 0) {
				return RestUtils.returnSuccess(returnedValue, "OK");
			}

			// 2-2-3 查询社保公积金  2020-04-13改为获取实时薪资
			// 1.获取当前月份的社保公积金比例基数数据
			ErpSocialSecurity erpSocialSecurity = erpSocialSecurityMapper.selectSocialSecurityLastOne();

			// 2.获取所有入职员工的实时薪资表【包含实习生】
			List<ErpBasePayroll> erpBasePayrollList = erpBasePayrollMapper.findBasePayrollAll();

			// 2-1.过滤实时薪资【将符合需求的筛选出来（通过HR查出的数据是有效的人员）】
			Map<String, Object> alertInfo = new HashMap<String, Object>();
			List<ErpBasePayroll> basePayrollListNew = new ArrayList<ErpBasePayroll>();
			basePayrollListNew = this.getRequiredBasePayrollList(erpBasePayrollList, erpUserList);
			if (basePayrollListNew == null) {
				alertInfo.put("匹配HR失败，请联系管理员！", null);
				return RestUtils.returnSuccess(alertInfo, "OK");
			}

//			List<ErpActualInsuranceFundBase> insuranceFundBaseList = erpActualInsuranceFundBaseMapper
//					.selectActualInsuranceFundBaseByParameters(queryInsuranceFundMap);
			List<ErpActualInsuranceFundBase> insuranceFundBaseList = new ArrayList<>();

			// 2-2-3合并HR和公积金数据
			// 解密社保公积金基数
			int ifbPlus;
			for (ifbPlus = 0; ifbPlus < basePayrollListNew.size(); ifbPlus++) {
				ErpBasePayroll erpBasePayroll = basePayrollListNew.get(ifbPlus);

				// 2.2解密数据 并转化成float类型数据
				String insuranceBase = erpBasePayroll.getErpSocialSecurityBase(); // 社保基数
				String fundBase = erpBasePayroll.getErpAccumulationFundBase(); // 公积金基数

				insuranceBase = decryptDataRsa(insuranceBase);
				fundBase = decryptDataRsa(fundBase);

				double insuranceBaseDouble = (insuranceBase.equals("") || insuranceBase.equals("null")) ? 0.0
						: Double.valueOf(insuranceBase).doubleValue(); // 社保基数
				double fundBaseDouble = (fundBase.equals("") || fundBase.equals("null")) ? 0.0
						: Double.valueOf(fundBase).doubleValue(); // 公积金基数

				// 3.获取准确的社保基数(五种)和公积金基数(一种)
				double actualEndowmentInsuranceBase; // 员工真正的养老保险基数
				double actualUnemploymentInsuranceBase; // 员工真正的失业保险基数
				double actualMaternityInsuranceBase; // 员工真正的生育保险基数
				double actualMedicalInsuranceBase; // 员工真正的医疗保险基数
				double actualInjuryInsuranceBase; // 员工真正的工伤保险基数
				double actualAccumulationFundBase; // 员工真正的公积金基数

				Map<String, Double> actualInsuranceFund = this.insuranceFundDoubleValueAsMap(erpSocialSecurity,
						fundBaseDouble, insuranceBaseDouble);
				if (actualInsuranceFund.get("status").doubleValue() == -1.0) {
					info = erpBasePayroll.getErpEmployeeId().toString() + "：的社保公积金基数获取异常，社保公积金基数数据不生成或更新C";
					returnInfo = returnInfo + info;
					logger.info(returnInfo);
					continue;
				}
				actualAccumulationFundBase = actualInsuranceFund.get("actualAccumulationFundBase").doubleValue(); // 员工真正的公积金基数
				actualEndowmentInsuranceBase = actualInsuranceFund.get("actualEndowmentInsuranceBase").doubleValue(); // 员工真正的养老保险基数
				actualUnemploymentInsuranceBase = actualInsuranceFund.get("actualUnemploymentInsuranceBase")
						.doubleValue(); // 员工真正的失业保险基数
				actualMaternityInsuranceBase = actualInsuranceFund.get("actualMaternityInsuranceBase").doubleValue(); // 员工真正的生育保险基数
				actualMedicalInsuranceBase = actualInsuranceFund.get("actualMedicalInsuranceBase").doubleValue(); // 员工真正的医疗保险基数
				actualInjuryInsuranceBase = actualInsuranceFund.get("actualInjuryInsuranceBase").doubleValue(); // 员工真正的工伤保险基数

				// 4-1.解密数据并保存到 ErpActualInsuranceFundBase 定义的对象中
				ErpActualInsuranceFundBase erpActualInsuranceFundBase = new ErpActualInsuranceFundBase();
				erpActualInsuranceFundBase
						.setAccumulationFundBase(String.valueOf(actualAccumulationFundBase));
				erpActualInsuranceFundBase
						.setEndowmentInsuranceBase(String.valueOf(actualEndowmentInsuranceBase));
				erpActualInsuranceFundBase
						.setUnemploymentInsuranceBase(String.valueOf(actualUnemploymentInsuranceBase));
				erpActualInsuranceFundBase
						.setMaternityInsuranceBase(String.valueOf(actualMaternityInsuranceBase));
				erpActualInsuranceFundBase
						.setMedicalInsuranceBase(String.valueOf(actualMedicalInsuranceBase));
				erpActualInsuranceFundBase
						.setInjuryInsuranceBase(String.valueOf(actualInjuryInsuranceBase));
				erpActualInsuranceFundBase.setEmployeeId(Integer.valueOf(erpBasePayroll.getErpEmployeeId()));

				erpActualInsuranceFundBase.setTookEffectDate(formatYMD.format(formatYMD.parse(tookEffectMonth)));
				insuranceFundBaseList.add(erpActualInsuranceFundBase);
			}

			// 合并
			for (plus = 0; plus < erpUserList.size(); plus++) {
				Map<String, Object> employeeInfo = erpUserList.get(plus);
				Integer employeeId = Integer.valueOf(String.valueOf(employeeInfo.get("employeeId")));

				int flag = 0; // 是否匹配的标识 1匹配 0 不匹配
				for (ifbPlus = 0; ifbPlus < insuranceFundBaseList.size(); ifbPlus++) {
					ErpActualInsuranceFundBase insuranceFundBase = insuranceFundBaseList.get(ifbPlus);

					if (insuranceFundBase == null) {
						break;
					}

					if (insuranceFundBase.getEmployeeId() == null) {
						break;
					}

					Integer ifbUserId = Integer.valueOf(String.valueOf(insuranceFundBase.getEmployeeId()));

					if (employeeId.equals(ifbUserId)) {
						employeeInfo.put("endowmentInsuranceBase",
								(Object) (insuranceFundBase.getEndowmentInsuranceBase()));
						employeeInfo.put("unemploymentInsuranceBase",
								(Object) (insuranceFundBase.getUnemploymentInsuranceBase()));
						employeeInfo.put("maternityInsuranceBase",
								(Object) (insuranceFundBase.getMaternityInsuranceBase()));
						employeeInfo.put("medicalInsuranceBase",
								(Object) (insuranceFundBase.getMedicalInsuranceBase()));
						employeeInfo.put("injuryInsuranceBase", (Object) (insuranceFundBase.getInjuryInsuranceBase()));
						employeeInfo.put("accumulationFundBase",
								(Object) (insuranceFundBase.getAccumulationFundBase()));
						employeeInfo.put("tookEffectDate", (Object) (insuranceFundBase.getTookEffectDate()));
						flag = 1;
						break;
					}
				}
				if (flag == 0) {
					employeeInfo.put("endowmentInsuranceBase", erpSocialSecurity.getEndowmentInsuranceBaseLower());
					employeeInfo.put("unemploymentInsuranceBase", erpSocialSecurity.getUnemploymentInsuranceBaseLower());
					employeeInfo.put("maternityInsuranceBase", erpSocialSecurity.getMaternityInsuranceBaseLower());
					employeeInfo.put("medicalInsuranceBase", erpSocialSecurity.getMedicalInsuranceBaseLower());
					employeeInfo.put("injuryInsuranceBase", erpSocialSecurity.getInjuryInsuranceBaseLower());
					employeeInfo.put("accumulationFundBase", erpSocialSecurity.getAccumulationFundBaseLower());
					employeeInfo.put("tookEffectDate", formatYMD.format(formatYMD.parse(tookEffectMonth)));
				}
			}

			returnedValue.put("employeList", erpUserList);
			return RestUtils.returnSuccess(returnedValue, "OK");
		} catch (Exception e) {
			logger.error("searchInsuranceFundByParameters 出现异常:" + e.getMessage(), e);
			return RestUtils.returnFailure("searchInsuranceFundByParameters 出现异常!");
		}
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
	 * Description: 加密
	 *
	 * @return
	 * @Author HouHuiRong
	 * @Create Date: 2018年11月12日 下午10:31:01
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
	 * Description: 比较值得大小 B<A<C 返回 A；B>=A 返回B；A>=C 返回C。A、B、C为>=0得数值
	 * 
	 * @return double，如果遇到异常，返回0
	 * @Author songxiugong
	 * @Create Date: 2020年02月18日
	 */
	public double compareThreeValue(double first, double last, double middle) {
		try {
			double returnValvue = 0;
			if (first < middle && middle < last) {
				returnValvue = middle;
			} else if (first >= middle) {
				returnValvue = first;
			} else {
				returnValvue = last;
			}
			return returnValvue;
		} catch (Exception e) {
			logger.error("compareThreeValue 出现异常:" + e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * Description: 获取公积金基数的各个值
	 * 
	 * @return Map对象 status = 1 正常状态，status = -1 异常状态
	 *         ；fundBaseDouble员工实时薪资中的社保基数，insuranceBaseDouble 员工实时薪资中的公积金基数
	 * @Author songxiugong
	 * @Create Date: 2020年02月18日
	 */
	public Map<String, Double> insuranceFundDoubleValueAsMap(ErpSocialSecurity erpSocialSecurity, double fundBaseDouble,
			double insuranceBaseDouble) {
		Map<String, Double> returnValue = new HashMap<String, Double>();

		try {
			double actualEndowmentInsuranceBase; // 员工真正的养老保险基数
			double actualUnemploymentInsuranceBase; // 员工真正的失业保险基数
			double actualMaternityInsuranceBase; // 员工真正的生育保险基数
			double actualMedicalInsuranceBase; // 员工真正的医疗保险基数
			double actualInjuryInsuranceBase; // 员工真正的工伤保险基数
			double actualAccumulationFundBase; // 员工真正的公积金基数

			// 员工真正的公积金基数
			double valueLower, valueUpper, valueActual;
			valueLower = erpSocialSecurity.getAccumulationFundBaseLower().doubleValue();
			valueUpper = erpSocialSecurity.getAccumulationFundBaseUpper().doubleValue();
			actualAccumulationFundBase = compareThreeValue(valueLower, valueUpper, fundBaseDouble);
			returnValue.put("actualAccumulationFundBase", Double.valueOf(actualAccumulationFundBase));

			// 员工真正的养老保险基数
			valueLower = erpSocialSecurity.getEndowmentInsuranceBaseLower().doubleValue();
			valueUpper = erpSocialSecurity.getEndowmentInsuranceBaseUpper().doubleValue();
			actualEndowmentInsuranceBase = compareThreeValue(valueLower, valueUpper, insuranceBaseDouble);
			returnValue.put("actualEndowmentInsuranceBase", Double.valueOf(actualEndowmentInsuranceBase));

			// 员工真正的失业保险基数
			valueLower = erpSocialSecurity.getUnemploymentInsuranceBaseLower().doubleValue();
			valueUpper = erpSocialSecurity.getUnemploymentInsuranceBaseUpper().doubleValue();
			actualUnemploymentInsuranceBase = compareThreeValue(valueLower, valueUpper, insuranceBaseDouble);
			returnValue.put("actualUnemploymentInsuranceBase", Double.valueOf(actualUnemploymentInsuranceBase));

			// 员工真正的生育保险基数
			valueLower = erpSocialSecurity.getMaternityInsuranceBaseLower().doubleValue();
			valueUpper = erpSocialSecurity.getMaternityInsuranceBaseUpper().doubleValue();
			actualMaternityInsuranceBase = compareThreeValue(valueLower, valueUpper, insuranceBaseDouble);
			returnValue.put("actualMaternityInsuranceBase", Double.valueOf(actualMaternityInsuranceBase));

			// 员工真正的医疗保险基数
			valueLower = erpSocialSecurity.getMedicalInsuranceBaseLower().doubleValue();
			valueUpper = erpSocialSecurity.getMedicalInsuranceBaseUpper().doubleValue();
			actualMedicalInsuranceBase = compareThreeValue(valueLower, valueUpper, insuranceBaseDouble);
			returnValue.put("actualMedicalInsuranceBase", Double.valueOf(actualMedicalInsuranceBase));

			// 员工真正的工伤保险基数
			valueLower = erpSocialSecurity.getInjuryInsuranceBaseLower().doubleValue();
			valueUpper = erpSocialSecurity.getInjuryInsuranceBaseUpper().doubleValue();
			actualInjuryInsuranceBase = compareThreeValue(valueLower, valueUpper, insuranceBaseDouble);
			returnValue.put("actualInjuryInsuranceBase", Double.valueOf(actualInjuryInsuranceBase));

			// 状态说明
			returnValue.put("status", Double.valueOf(1.0));

			return returnValue;
		} catch (Exception e) {
			logger.error("compareThreeValue 出现异常:" + e.getMessage(), e);
			returnValue.put("status", Double.valueOf(-1.0));
			return returnValue;
		}
	}

	/**
	 * Description: 解密社保公积金基数值
	 * 
	 * @return ErpActualInsuranceFundBase 对象
	 * @Author songxiugong
	 * @Create Date: 2020年02月20日
	 */
	public ErpActualInsuranceFundBase decryptDataRsaObject(ErpActualInsuranceFundBase erpActualInsuranceFundBase) {
		try {

			String temp = erpActualInsuranceFundBase.getAccumulationFundBase();
			temp = decryptDataRsa(temp);
			if (temp == null || temp.equals("null")) {
				erpActualInsuranceFundBase.setAccumulationFundBase("");
			} else {
				erpActualInsuranceFundBase.setAccumulationFundBase(temp);
			}

			temp = erpActualInsuranceFundBase.getEndowmentInsuranceBase();
			temp = decryptDataRsa(temp);
			if (temp == null || temp.equals("null")) {
				erpActualInsuranceFundBase.setEndowmentInsuranceBase("");
			} else {
				erpActualInsuranceFundBase.setEndowmentInsuranceBase(temp);
			}

			temp = erpActualInsuranceFundBase.getInjuryInsuranceBase();
			temp = decryptDataRsa(temp);
			if (temp == null || temp.equals("null")) {
				erpActualInsuranceFundBase.setInjuryInsuranceBase("");
			} else {
				erpActualInsuranceFundBase.setInjuryInsuranceBase(temp);
			}

			temp = erpActualInsuranceFundBase.getMaternityInsuranceBase();
			temp = decryptDataRsa(temp);
			if (temp == null || temp.equals("null")) {
				erpActualInsuranceFundBase.setMaternityInsuranceBase("");
			} else {
				erpActualInsuranceFundBase.setMaternityInsuranceBase(temp);
			}

			temp = erpActualInsuranceFundBase.getMedicalInsuranceBase();
			temp = decryptDataRsa(temp);
			if (temp == null || temp.equals("null")) {
				erpActualInsuranceFundBase.setMedicalInsuranceBase("");
			} else {
				erpActualInsuranceFundBase.setMedicalInsuranceBase(temp);
			}

			temp = erpActualInsuranceFundBase.getUnemploymentInsuranceBase();
			temp = decryptDataRsa(temp);
			if (temp == null || temp.equals("null")) {
				erpActualInsuranceFundBase.setUnemploymentInsuranceBase("");
			} else {
				erpActualInsuranceFundBase.setUnemploymentInsuranceBase(temp);
			}

			return erpActualInsuranceFundBase;
		} catch (Exception e) {
			logger.error("decryptDataRsaObject:" + e.getMessage(), e);
			return null;
		}
	}
}
