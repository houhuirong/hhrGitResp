package com.nantian.erp.salary.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.salary.constants.DicConstants;
import com.nantian.erp.salary.data.dao.*;
import com.nantian.erp.salary.data.model.*;
import com.nantian.erp.salary.data.vo.DepartmentSalaryAdjustVO;
import com.nantian.erp.salary.util.RestTemplateUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.util.AesUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * Description: 薪资调整service实现层
 *
 * @author houhuirong
 * @version 1.0
 * 
 *          <pre>
* Modification History: 
* Date                  Author           Version     
* ------------------------------------------------
* 2018年9月12日      		houhuirong       1.0
 * 
 *          </pre>
 */

@Service
public class ErpSalaryAdjustService {

	@Value("${protocol.type}")
	private String protocolType;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private ErpSalaryAdjustMapper erpSalaryAdjustMapper;

	@Autowired
	private ErpPositivePayrollMapper erpPositivePayrollMapper;

	@Autowired
	private ErpPeriodPayrollMapper erpPeriodPayrollMapper;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RedisTemplate<?, ?> redisTemplate;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RestTemplateUtils restTemplateUtils;

	@Autowired
	private ErpBasePayrollMapper erpBasePayrollMapper;

	@Autowired
	private ErpBasePayrollService erpBasePayrollService;
	@Autowired
	private DepartmentSalaryAdjustMapper departmentSalaryAdjustMapper;
	@Autowired
	private DepartmentSalaryAdjustApproveRecordMapper departmentSalaryAdjustApproveRecordMapper;
	@Autowired
	private ErpBasePayrollUpdateRecordMapper erpBasePayrollUpdateRecordMapper;


	/**
	 * 
	 * Description: 通过邮箱获取员工部门工资等基本信息
	 *
	 * @param employeeEmail
	 *            员工邮箱
	 * @return
	 * @Author houhuirong
	 * @Create Date: 2018年9月12日
	 */

	public RestResponse findDepAndAppr(String token) {
		List<Map<String, Object>> resultMap = null;
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			resultMap = new ArrayList<>();
			// 调用ERP-人力资源 工程 的操作层服务接口-获取员工名字及一级部门信息
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/department/findDepartmentByUserID?userId="
					+ erpUser.getUserId();
			String body = null;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.add("token", token);
			HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
			if (200 != response.getStatusCodeValue()) {
				return RestUtils.returnFailure("调用人力资源工程失败！");
			}
			/*
			 * 解析请求的结果
			 */
			Map<String, Object> responseBody = response.getBody();
			if (!"200".equals(responseBody.get("status"))) {
				return RestUtils.returnFailure("人力资源工程发生异常！");
			}
			List<Map<String, Object>> list = (List<Map<String, Object>>) responseBody.get("data");
			System.out.println("list:" + list);
		
			String url3 = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmpNameMapById";
			System.out.println("url3 :" + url3);
			Map<String, Object> listMap2 = new HashMap<>();
			listMap2.put("listMap", list);
			HttpEntity<Map<String, Object>> requestFir2 = new HttpEntity<>(listMap2, headers);

			ResponseEntity<Map> response3 = this.restTemplate.postForEntity(url3, requestFir2, Map.class);
			if (200 != response3.getStatusCodeValue()) {
				return RestUtils.returnFailure("调用HR工程失败！");
			}
			/*
			 * 解析请求的结果
			 */
			Map<String, Object> responseBody3 = response3.getBody();
			System.out.println("responseBody3:  " + responseBody3);
			if (!"200".equals(responseBody.get("status"))) {
				return RestUtils.returnFailure("HR工程工程发生异常！");
			}
			System.out.println(responseBody3.get("data"));
			resultMap = (List<Map<String, Object>>) responseBody3.get("data");
			System.out.println("resultMap  :  " + resultMap);
		} catch (Exception e) {
			logger.error(" 该方法findDepAndAppr()" + e.getMessage(),e);
		}
		return RestUtils.returnSuccess(resultMap);
	}

	public RestResponse findDepMessSalary(Integer id, Integer approverEId, String token) {
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
		System.out.println("erpUser:  " + erpUser);
		String url = protocolType + "nantian-erp-hr/nantian-erp/erp/department/findDepMessByDepartmentID?departmentId="
				+ id;
		System.out.println("url :" + url);
		String body = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add("token", token);
		HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
		if (200 != response.getStatusCodeValue()) {
			return RestUtils.returnFailure("调用人力资源工程失败！");
		}

		// 解析请求的结果

		Map<String, Object> responseBody = response.getBody();
		if (!"200".equals(responseBody.get("status"))) {
			return RestUtils.returnFailure("人力资源工程发生异常！");
		}
		List<Map<String, Object>> list = (List<Map<String, Object>>) responseBody.get("data");
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> map1 = null;

		List<Map<String, Object>> messli = new ArrayList<>();
		Integer deppeoptotal = 0;
		Integer AdjPeopTotal = 0;
		Double AdjSalaryTotal = 0.0;
		for (Map<String, Object> listMap0 : list) {
			map1 = new HashMap<>();
			List<Map<String, Object>> employeeMess = (List<Map<String, Object>>) listMap0.get("employeeMess");
			map1.put("departmentName", listMap0.get("departmentName"));
			map1.put("oneAdjPeopTotal", 0);
			map1.put("oneAdjSalaryTotal", 0);
			Integer oneAdjPeopTotal = 0;
			Double oneAdjSalaryTotal = 0.0;
			if (employeeMess!=null) {
				Integer oneDeppeoptotal = employeeMess.size();
				deppeoptotal += oneDeppeoptotal;
				map1.put("oneDeppeoptotal", oneDeppeoptotal);
				for (Map<String, Object> employee : employeeMess) {
					try {
						Integer employeeId = (Integer) employee.get("employeeId");
						String str = (String) employee.get("status");
						if ("0".equals(str)) {
							employee.put("status", "实习生");
						} else if ("1".equals(str)) {
							employee.put("status", "试用期员工");
						} else if ("2".equals(str)) {
							employee.put("status", "正式员工");
						} else if ("3".equals(str)) {
							employee.put("status", "离职中");
						} else {
							employee.put("status", "已离职");
						}
						// 查看调薪记录模块
						List<Map<String, Object>> salAdjRec = this.erpSalaryAdjustMapper
								.selectSalAdjRecByEId(employeeId);
						if (salAdjRec != null) {

							String url2 = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmpNameByEIdMap";
							Map<String, Object> listMap = new HashMap<>();
							listMap.put("listMap", salAdjRec);
							HttpEntity<Map<String, Object>> requestFir2 = new HttpEntity<>(listMap, headers);
							ResponseEntity<Map> response3 = this.restTemplate.postForEntity(url2, requestFir2,
									Map.class);
							if (200 != response3.getStatusCodeValue()) {
								return RestUtils.returnFailure("调用HR工程失败！");
							}
							/*
							 * 解析请求的结果
							 */
							Map<String, Object> responseBody3 = response3.getBody();
							if (!"200".equals(responseBody.get("status"))) {
								return RestUtils.returnFailure("HR工程工程发生异常！");
							}
							List<Map<String, Object>> backSalAdjRec = (List<Map<String, Object>>) responseBody3.get("data");

							for (Map<String, Object> map2 : backSalAdjRec) {
								Double former_base_wage = Double
										.valueOf(AesUtils.decrypt((String) map2.get("former_base_wage")));
								Double former_post_wage = Double
										.valueOf(AesUtils.decrypt((String) map2.get("former_post_wage")));
								Double former_performance = Double
										.valueOf(AesUtils.decrypt((String) map2.get("former_performance")));
								Double former_allowance = Double
										.valueOf(AesUtils.decrypt((String) map2.get("former_allowance")));
								Double former_tel_fare_perquisite = Double
										.valueOf(AesUtils.decrypt((String) map2.get("former_tel_fare_perquisite")));
								Double adjust_base_wage = Double
										.valueOf(AesUtils.decrypt((String) map2.get("adjust_base_wage")));
								Double adjust_post_wage = Double
										.valueOf(AesUtils.decrypt((String) map2.get("adjust_post_wage")));
								Double adjust_performance = Double
										.valueOf(AesUtils.decrypt((String) map2.get("adjust_performance")));
								Double adjust_allowance = Double
										.valueOf(AesUtils.decrypt((String) map2.get("adjust_allowance")));
								Double adjust_tel_fare_perquisite = Double
										.valueOf(AesUtils.decrypt((String) map2.get("adjust_tel_fare_perquisite")));
								Double adjust_salary = adjust_base_wage + adjust_post_wage + adjust_performance
										+ adjust_allowance + adjust_tel_fare_perquisite;
								Double former_salary = former_base_wage + former_post_wage + former_performance
										+ former_allowance + former_tel_fare_perquisite;
								map2.put("adjust_salary", adjust_salary);
								map2.put("former_salary", former_salary);
							}
							employee.put("adjust_salary_record", backSalAdjRec);
						}

						ErpBasePayroll erpBasePayroll = this.erpBasePayrollMapper
								.findBasePayrollDetailByEmpId(employeeId);
						if (erpBasePayroll != null) {
							// 将数据库中加密后的薪酬信息解密
							Map<String, Double> decryptedExcelData = erpBasePayrollService
									.decryptExcelDataAes(erpBasePayroll);
							Double erpBaseWage = decryptedExcelData.get("erpBaseWage");// 基本工资
							Double erpPostWage = decryptedExcelData.get("erpPostWage");// 岗位工资
							Double erpPerformance = decryptedExcelData.get("erpPerformance");// 月度绩效
							Double erpAllowance = decryptedExcelData.get("erpAllowance");// 月度项目津贴
							Double erpSocialSecurityBase = decryptedExcelData.get("erpSocialSecurityBase");// 社保基数
							Double erpAccumulationFundBase = decryptedExcelData.get("erpAccumulationFundBase");// 公积金基数
							Double erpTelFarePerquisite = decryptedExcelData.get("erpTelFarePerquisite");// 话费补助
							employee.put("erpBaseWage", erpBaseWage);
							employee.put("erpPostWage", erpPostWage);
							employee.put("erpPerformance", erpPerformance);
							employee.put("erpAllowance", erpAllowance);
							employee.put("erpSocialSecurityBase", erpSocialSecurityBase);
							employee.put("erpAccumulationFundBase", erpAccumulationFundBase);
							employee.put("erpTelFarePerquisite", erpTelFarePerquisite);
							employee.put("wageTotel",
									erpBaseWage + erpPostWage + erpPerformance + erpAllowance + erpTelFarePerquisite);
						} else {
							employee.put("erpBaseWage", 0.0);
							employee.put("erpPostWage", 0.0);
							employee.put("erpPerformance", 0.0);
							employee.put("erpAllowance", 0.0);
							employee.put("erpSocialSecurityBase", 0.0);
							employee.put("erpAccumulationFundBase", 0.0);
							employee.put("erpTelFarePerquisite", 0.0);
							employee.put("wageTotel", 0.0);
						}

						Map<String, Object> adjByEId = erpSalaryAdjustMapper.selectSarAdjByEId(employeeId);
						if (adjByEId == null) {
							employee.put("erpAdjwage", null);
							employee.put("newWageTotel", null);
							employee.put("erpAdjBecause", null);
							employee.put("erpAdjIndex", null);
							employee.put("erpNewBaseWage", null);
							employee.put("erpNewPostWage", null);
							employee.put("erpNewPerformance", null);
							employee.put("erpNewAllowance", null);
							employee.put("erpNewTelFarePerquisite", null);
							employee.put("newAndOldCha", "填写");
						} else {
							Double wageTotel = Double.valueOf(employee.get("wageTotel").toString());
							Double erpNewBaseWage = Double
									.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_base_wage")));
							Double erpNewPostWage = Double
									.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_post_wage")));
							Double erpNewPerformance = Double
									.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_performance")));
							Double erpNewAllowance = Double
									.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_allowance")));
							Double erpNewTelFarePerquisite = Double
									.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_tel_fare_perquisite")));
							Object erpAdjBecause = adjByEId.get("adjust_reason");
							Object erpAdjIndex = adjByEId.get("adjust_batch");
							Double newAndOldCha = erpNewBaseWage + erpNewPostWage + erpNewPerformance + erpNewAllowance
									+ erpNewTelFarePerquisite - wageTotel;
							oneAdjPeopTotal++;
							oneAdjSalaryTotal += newAndOldCha;
							employee.put("erpAdjBecause", erpAdjBecause);
							employee.put("erpAdjIndex", erpAdjIndex);
							employee.put("erpNewBaseWage", erpNewBaseWage);
							employee.put("erpNewPostWage", erpNewPostWage);
							employee.put("erpNewPerformance", erpNewPerformance);
							employee.put("erpNewAllowance", erpNewAllowance);
							employee.put("erpNewTelFarePerquisite", erpNewTelFarePerquisite);
							employee.put("newAndOldCha", newAndOldCha);
						}
					} catch (Exception e) {
						logger.error("findBasePayrollDetailByEmpId方法出现异常：" + e.getMessage(), e);
						return RestUtils.returnFailure("findBasePayrollDetailByEmpId方法出现异常：" + e.getMessage());
					}
					map1.put("oneAdjPeopTotal", oneAdjPeopTotal);
					map1.put("oneAdjSalaryTotal", oneAdjSalaryTotal);
				}
				AdjPeopTotal += oneAdjPeopTotal;
				AdjSalaryTotal += oneAdjSalaryTotal;
				map1.put("PeopleMess", employeeMess);
				messli.add(map1);
			}
		}
		map.put("deppeoptotal", deppeoptotal);
		map.put("sencondList", messli);
		map.put("ApproverEId", approverEId);
		map.put("AdjPeopTotal", AdjPeopTotal);
		map.put("AdjSalaryTotal", AdjSalaryTotal);
		return RestUtils.returnSuccess(map);
	}

	public RestResponse backDepMessSalary(Map<String, Object> map) {
		List<Map<String, Object>> oneDepartMess = (List<Map<String, Object>>) map.get("sencondList");
		Integer AdjPeopTotal = 0;
		Double AdjSalaryTotal = 0.0;
		for (Map<String, Object> oneDepPeopMessMap : oneDepartMess) {
			List<Map<String, Object>> oneDepOnePeopMess = (List<Map<String, Object>>) oneDepPeopMessMap
					.get("PeopleMess");
			Integer oneAdjPeopTotal = 0;
			Double oneAdjSalaryTotal = 0.0;
			for (Map<String, Object> oneDepOnePeopMessMap : oneDepOnePeopMess) {

				Object erpNewBaseWage = oneDepOnePeopMessMap.get("erpNewBaseWage");
				Object erpNewPostWage = oneDepOnePeopMessMap.get("erpNewPostWage");
				Object erpNewPerformance = oneDepOnePeopMessMap.get("erpNewPerformance");
				Object erpNewAllowance = oneDepOnePeopMessMap.get("erpNewAllowance");
				Object erpNewTelFarePerquisite = oneDepOnePeopMessMap.get("erpNewTelFarePerquisite");
				Double wageTotel = Double.valueOf(oneDepOnePeopMessMap.get("wageTotel").toString());
				Double IerpNewBaseWage;
				Double IerpNewPostWage;
				Double IerpNewPerformance;
				Double IerpNewAllowance;
				Double IerpNewTelFarePerquisite;
				Double newWageTotel = 0.0;
				if (erpNewBaseWage != null || erpNewPostWage != null || erpNewPerformance != null
						|| erpNewAllowance != null || erpNewTelFarePerquisite != null) {
					if (erpNewBaseWage == null) {
						IerpNewBaseWage = 0.0;
					} else {
						IerpNewBaseWage = Double.valueOf(erpNewBaseWage.toString());
					}

					if (erpNewPostWage == null) {
						IerpNewPostWage = 0.0;
					} else {
						IerpNewPostWage = Double.valueOf(erpNewPostWage.toString());
					}

					if (erpNewPerformance == null) {
						IerpNewPerformance = 0.0;
					} else {
						IerpNewPerformance = Double.valueOf(erpNewPerformance.toString());
					}

					if (erpNewAllowance == null) {
						IerpNewAllowance = 0.0;
					} else {
						IerpNewAllowance = Double.valueOf(erpNewAllowance.toString());
					}

					if (erpNewTelFarePerquisite == null) {
						IerpNewTelFarePerquisite = 0.0;
					} else {
						IerpNewTelFarePerquisite = Double.valueOf(erpNewTelFarePerquisite.toString());
					}

					newWageTotel = IerpNewBaseWage + IerpNewPostWage + IerpNewPerformance + IerpNewAllowance
							+ IerpNewTelFarePerquisite;
					oneDepOnePeopMessMap.put("newWageTotel", newWageTotel);
					Double newAndOldCha = newWageTotel - wageTotel;
					oneDepOnePeopMessMap.put("newAndOldCha", newAndOldCha);
					oneAdjPeopTotal++;
					oneAdjSalaryTotal += newAndOldCha;
				} else {
					oneDepOnePeopMessMap.put("newAndOldCha", "填写");
				}
				oneDepPeopMessMap.put("oneAdjPeopTotal", oneAdjPeopTotal);
				oneDepPeopMessMap.put("oneAdjSalaryTotal", oneAdjSalaryTotal);
			}
			AdjPeopTotal += oneAdjPeopTotal;
			AdjSalaryTotal += oneAdjSalaryTotal;
		}
		map.put("AdjPeopTotal", AdjPeopTotal);
		map.put("AdjSalaryTotal", AdjSalaryTotal);
		return RestUtils.returnSuccess(map);
	}

	public RestResponse saveAdjectMess(Map<String, Object> map) {
		Object approverId = map.get("ApproverEId");
		List<Map<String, Object>> oneDepartMess = (List<Map<String, Object>>) map.get("sencondList");
		for (Map<String, Object> oneDepPeopMessMap : oneDepartMess) {
			List<Map<String, Object>> oneDepOnePeopMess = (List<Map<String, Object>>) oneDepPeopMessMap
					.get("PeopleMess");
			for (Map<String, Object> oneDepOnePeopMessMap : oneDepOnePeopMess) {
				Integer employeeId = (Integer) oneDepOnePeopMessMap.get("employeeId");
				Object erpAdjBecause = oneDepOnePeopMessMap.get("erpAdjBecause");
				Object erpAdjIndex = oneDepOnePeopMessMap.get("erpAdjIndex");
				Object object = oneDepOnePeopMessMap.get("erpBaseWage");
				Object object2 = oneDepOnePeopMessMap.get("erpPostWage");
				Object object3 = oneDepOnePeopMessMap.get("erpPerformance");
				Object object4 = oneDepOnePeopMessMap.get("erpAllowance");
				Object object5 = oneDepOnePeopMessMap.get("erpTelFarePerquisite");
				Object object6 = oneDepOnePeopMessMap.get("erpNewBaseWage");
				Object object7 = oneDepOnePeopMessMap.get("erpNewPostWage");
				Object object8 = oneDepOnePeopMessMap.get("erpNewPerformance");
				Object object9 = oneDepOnePeopMessMap.get("erpNewAllowance");
				Object object10 = oneDepOnePeopMessMap.get("erpNewTelFarePerquisite");
				if (object6 != null || object7 != null || object8 != null || object9 != null || object10 != null) {
					String erpBaseWage = AesUtils.encrypt(String.valueOf(object));
					String erpPostWage = AesUtils.encrypt(String.valueOf(object2));
					String erpPerformance = AesUtils.encrypt(String.valueOf(object3));
					String erpAllowance = AesUtils.encrypt(String.valueOf(object4));
					String erpTelFarePerquisite = AesUtils.encrypt(String.valueOf(object5));
					String erpNewBaseWage = AesUtils.encrypt(String.valueOf(object6));
					String erpNewPostWage = AesUtils.encrypt(String.valueOf(object7));
					String erpNewPerformance = AesUtils.encrypt(String.valueOf(object8));
					String erpNewAllowance = AesUtils.encrypt(String.valueOf(object9));
					String erpNewTelFarePerquisite = AesUtils.encrypt(String.valueOf(object10));
					Map<String, Object> adjByEId = erpSalaryAdjustMapper.selectSarAdjByEId(employeeId);
					if (adjByEId != null) {
						Double erpNewBaseWage1 = Double
								.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_base_wage")));
						Double erpNewPostWage1 = Double
								.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_post_wage")));
						Double erpNewPerformance1 = Double
								.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_performance")));
						Double erpNewAllowance1 = Double
								.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_allowance")));
						Double erpNewTelFarePerquisite1 = Double
								.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_tel_fare_perquisite")));
						Object erpAdjBecause1 = adjByEId.get("adjust_reason");
						Object erpAdjIndex1 = adjByEId.get("adjust_batch");
						Double erpNewBaseWage2 = Double.valueOf(object6.toString());
						Double erpNewPostWage2 = Double.valueOf(object7.toString());
						Double erpNewPerformance2 = Double.valueOf(object8.toString());
						Double erpNewAllowance2 = Double.valueOf(object9.toString());
						Double erpNewTelFarePerquisite2 = Double.valueOf(object10.toString());

						if (erpAdjBecause != erpAdjBecause1 || erpAdjIndex != erpAdjIndex1
								|| erpNewBaseWage1 != erpNewBaseWage2 || erpNewPostWage2 != erpNewPostWage1
								|| erpNewPerformance2 != erpNewPerformance1 || erpNewAllowance2 != erpNewAllowance1
								|| erpNewTelFarePerquisite2 != erpNewTelFarePerquisite1) {
							Map<String, Object> map3 = new HashMap<>();
							map3.put("erpBaseWage", erpBaseWage);
							map3.put("erpPostWage", erpPostWage);
							map3.put("erpPerformance", erpPerformance);
							map3.put("erpAllowance", erpAllowance);
							map3.put("erpTelFarePerquisite", erpTelFarePerquisite);
							map3.put("erpAdjBecause", erpAdjBecause);
							map3.put("erpAdjIndex", erpAdjIndex);
							map3.put("erpNewBaseWage", erpNewBaseWage);
							map3.put("erpNewPostWage", erpNewPostWage);
							map3.put("erpNewPerformance", erpNewPerformance);
							map3.put("erpNewAllowance", erpNewAllowance);
							map3.put("erpNewTelFarePerquisite", erpNewTelFarePerquisite);
							map3.put("approverId", approverId);
							map3.put("employeeId", employeeId);
							map3.put("adjust_status", 0);
							boolean salaryMess = erpSalaryAdjustMapper.updateSalaryMess(map3);
							if (salaryMess == true) {
								logger.info("薪资调整计划修改成功...");
							} else {
								logger.info("薪资调整计划修改失败...");
							}
						}
					} else {

						Map<String, Object> map2 = new HashMap<>();
						map2.put("erpBaseWage", erpBaseWage);
						map2.put("erpPostWage", erpPostWage);
						map2.put("erpPerformance", erpPerformance);
						map2.put("erpAllowance", erpAllowance);
						map2.put("erpTelFarePerquisite", erpTelFarePerquisite);
						map2.put("employeeId", employeeId);
						map2.put("adjust_status", 0);
						map2.put("erpAdjBecause", erpAdjBecause);
						map2.put("erpAdjIndex", erpAdjIndex);
						map2.put("erpNewBaseWage", erpNewBaseWage);
						map2.put("erpNewPostWage", erpNewPostWage);
						map2.put("erpNewPerformance", erpNewPerformance);
						map2.put("erpNewAllowance", erpNewAllowance);
						map2.put("erpNewTelFarePerquisite", erpNewTelFarePerquisite);
						map2.put("approverId", approverId);

						try {
							boolean salaryMess = erpSalaryAdjustMapper.insertSalaryMess(map2);
							if (salaryMess == true) {
								logger.info("薪资调整计划保存成功...");
							} else {
								logger.info("薪资调整计划保存失败...");
							}
						} catch (Exception e) {
							logger.error("insertSalaryMess方法出现异常：" + e.getMessage(), e);
							return RestUtils.returnFailure("insertSalaryMess方法出现异常：" + e.getMessage());
						}
					}
				}
			}
		}
		return RestUtils.returnSuccess("提交成功");
	}

	public RestResponse findSalaryApproval(String token, Integer year) {
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
		List<Map<String, Object>> resultMap = new ArrayList<>();
		// 调用ERP-人力资源 工程 的操作层服务接口-获取员工名字及一级部门信息
		String url = protocolType + "nantian-erp-hr/nantian-erp/erp/department/findDepartmentBySLeaderId?userId="
				+ erpUser.getUserId();
		String body = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add("token", token);
		HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
		if (200 != response.getStatusCodeValue()) {
			return RestUtils.returnFailure("调用人力资源工程失败！");
		}

		/* 解析请求的结果 */
		Map<String, Object> responseBody = response.getBody();

		if (!"200".equals(responseBody.get("status"))) {
			return RestUtils.returnFailure("人力资源工程发生异常！");
		}
		List<Map<String, Object>> list = (List<Map<String, Object>>) responseBody.get("data");
		for (Map<String, Object> map : list) {
			Integer oneDepPeopTotel = 0;
			Integer oneDepAdjPeopTotel = 0;
			Double oneDepAdjSarTolel = 0.0;
			Integer oneConfirmAdjPeopTotel = 0;
			Double oneConfirmAdjSarTotel = 0.0;
			List<Map<String, Object>> SecondDep = (List<Map<String, Object>>) map.get("SecondDep");
			for (Map<String, Object> map2 : SecondDep) {
				List<Map<String, Object>> object = (List<Map<String, Object>>) map2.get("employeeMess");
				Integer twoDepPeopTotel = object.size();
				oneDepPeopTotel += twoDepPeopTotel;
				Integer twoDepAdjPeopTotel = 0;
				Double twoDepAdjSarTolel = 0.0;
				Integer twoConfirmAdjPeopTotel = 0;
				Double twoConfirmAdjSarTotel = 0.0;
				for (Map<String, Object> map3 : object) {
					Integer employeeId = (Integer) map3.get("employeeId");
					Object str = map3.get("status");
					if ("0".equals(str)) {
						map3.put("status", "实习生");
					} else if ("1".equals(str)) {
						map3.put("status", "试用期员工");
					} else if ("2".equals(str)) {
						map3.put("status", "正式员工");
					} else if ("3".equals(str)) {
						map3.put("status", "离职中");
					} else {
						map3.put("status", "已离职");
					}
					// 查看调薪记录模块
					List<Map<String, Object>> salAdjRec = this.erpSalaryAdjustMapper.selectSalAdjRecByEId(employeeId);
					if (salAdjRec != null) {
						String url2 = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmpNameByEIdMap";
						Map<String, Object> listMap = new HashMap<>();
						listMap.put("listMap", salAdjRec);
						HttpEntity<Map<String, Object>> requestFir2 = new HttpEntity<>(listMap, headers);
						ResponseEntity<Map> response3 = this.restTemplate.postForEntity(url2, requestFir2, Map.class);
						if (200 != response3.getStatusCodeValue()) {
							return RestUtils.returnFailure("调用HR工程失败！");
						}
						/*
						 * 解析请求的结果
						 */
						Map<String, Object> responseBody3 = response3.getBody();
						if (!"200".equals(responseBody.get("status"))) {
							return RestUtils.returnFailure("HR工程工程发生异常！");
						}
						List<Map<String, Object>> backSalAdjRec = (List<Map<String, Object>>) responseBody3.get("data");

						for (Map<String, Object> backmap : backSalAdjRec) {
							Double former_base_wage = Double
									.valueOf(AesUtils.decrypt((String) backmap.get("former_base_wage")));
							Double former_post_wage = Double
									.valueOf(AesUtils.decrypt((String) backmap.get("former_post_wage")));
							Double former_performance = Double
									.valueOf(AesUtils.decrypt((String) backmap.get("former_performance")));
							Double former_allowance = Double
									.valueOf(AesUtils.decrypt((String) backmap.get("former_allowance")));
							Double former_tel_fare_perquisite = Double
									.valueOf(AesUtils.decrypt((String) backmap.get("former_tel_fare_perquisite")));
							Double adjust_base_wage = Double
									.valueOf(AesUtils.decrypt((String) backmap.get("adjust_base_wage")));
							Double adjust_post_wage = Double
									.valueOf(AesUtils.decrypt((String) backmap.get("adjust_post_wage")));
							Double adjust_performance = Double
									.valueOf(AesUtils.decrypt((String) backmap.get("adjust_performance")));
							Double adjust_allowance = Double
									.valueOf(AesUtils.decrypt((String) backmap.get("adjust_allowance")));
							Double adjust_tel_fare_perquisite = Double
									.valueOf(AesUtils.decrypt((String) backmap.get("adjust_tel_fare_perquisite")));
							Double adjust_salary = adjust_base_wage + adjust_post_wage + adjust_performance
									+ adjust_allowance + adjust_tel_fare_perquisite;
							Double former_salary = former_base_wage + former_post_wage + former_performance
									+ former_allowance + former_tel_fare_perquisite;
							backmap.put("adjust_salary", adjust_salary);
							backmap.put("former_salary", former_salary);
						}
						map3.put("adjust_salary_record", backSalAdjRec);
					}

					ErpBasePayroll erpBasePayroll = this.erpBasePayrollMapper.findBasePayrollDetailByEmpId(employeeId);
					if (erpBasePayroll != null) {
						// 将数据库中加密后的薪酬信息解密
						Map<String, Double> decryptedExcelData = erpBasePayrollService
								.decryptExcelDataAes(erpBasePayroll);
						Double erpBaseWage = decryptedExcelData.get("erpBaseWage");// 基本工资
						Double erpPostWage = decryptedExcelData.get("erpPostWage");// 岗位工资
						Double erpPerformance = decryptedExcelData.get("erpPerformance");// 月度绩效
						Double erpAllowance = decryptedExcelData.get("erpAllowance");// 月度项目津贴
						Double erpSocialSecurityBase = decryptedExcelData.get("erpSocialSecurityBase");// 社保基数
						Double erpAccumulationFundBase = decryptedExcelData.get("erpAccumulationFundBase");// 公积金基数
						Double erpTelFarePerquisite = decryptedExcelData.get("erpTelFarePerquisite");// 话费补助
						map3.put("erpBaseWage", erpBaseWage);
						map3.put("erpPostWage", erpPostWage);
						map3.put("erpPerformance", erpPerformance);
						map3.put("erpAllowance", erpAllowance);
						map3.put("erpSocialSecurityBase", erpSocialSecurityBase);
						map3.put("erpAccumulationFundBase", erpAccumulationFundBase);
						map3.put("erpTelFarePerquisite", erpTelFarePerquisite);
						map3.put("wageTotel",
								erpBaseWage + erpPostWage + erpPerformance + erpAllowance + erpTelFarePerquisite);
					} else {
						map3.put("erpBaseWage", 0.0);
						map3.put("erpPostWage", 0.0);
						map3.put("erpPerformance", 0.0);
						map3.put("erpAllowance", 0.0);
						map3.put("erpSocialSecurityBase", 0.0);
						map3.put("erpAccumulationFundBase", 0.0);
						map3.put("erpTelFarePerquisite", 0.0);
						map3.put("wageTotel", 0.0);
					}
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
					String formatDate = sdf.format(new Date());
					Integer forYear = Integer.valueOf(formatDate);
					Map<String, Object> adjByEId = null;
					Boolean flag = false;
					if (year.intValue() == forYear.intValue()) {
						adjByEId = erpSalaryAdjustMapper.selectSarAdj2ByEId(employeeId, year + "%");
					} else {
						adjByEId = erpSalaryAdjustMapper.selectSarAdj3ByEId(employeeId, year + "%");
						flag = true;
					}
					map3.put("flag", flag);
					if (adjByEId == null) {
						map3.put("erpAdjwage", null);
						map3.put("newWageTotel", null);
						map3.put("erpAdjBecause", null);
						map3.put("erpAdjIndex", null);
						map3.put("erpNewBaseWage", null);
						map3.put("erpNewPostWage", null);
						map3.put("erpNewPerformance", null);
						map3.put("erpNewAllowance", null);
						map3.put("erpNewTelFarePerquisite", null);
						map3.put("newAndOldCha", "填写");
						map3.put("adjust_status", "0");
					} else {
						Double wageTotel = Double.valueOf(map3.get("wageTotel").toString());
						Double erpNewBaseWage = Double
								.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_base_wage")));
						Double erpNewPostWage = Double
								.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_post_wage")));
						Double erpNewPerformance = Double
								.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_performance")));
						Double erpNewAllowance = Double
								.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_allowance")));
						Double erpNewTelFarePerquisite = Double
								.valueOf(AesUtils.decrypt((String) adjByEId.get("adjust_tel_fare_perquisite")));
						Object erpAdjBecause = adjByEId.get("adjust_reason");
						Object erpAdjIndex = adjByEId.get("adjust_batch");
						Object adjust_status = adjByEId.get("adjust_status");
						Double newAndOldCha = erpNewBaseWage + erpNewPostWage + erpNewPerformance + erpNewAllowance
								+ erpNewTelFarePerquisite - wageTotel;
						twoDepAdjPeopTotel++;
						twoDepAdjSarTolel += newAndOldCha;
						map3.put("erpAdjBecause", erpAdjBecause);
						map3.put("erpAdjIndex", erpAdjIndex);
						map3.put("erpNewBaseWage", erpNewBaseWage);
						map3.put("erpNewPostWage", erpNewPostWage);
						map3.put("erpNewPerformance", erpNewPerformance);
						map3.put("erpNewAllowance", erpNewAllowance);
						map3.put("erpNewTelFarePerquisite", erpNewTelFarePerquisite);
						map3.put("newAndOldCha", newAndOldCha);
						map3.put("adjust_status", adjust_status);
						if(flag == true) {
							twoConfirmAdjPeopTotel++;
							twoConfirmAdjSarTotel += newAndOldCha ;
						}
					}
				}
				map2.put("twoDepAdjPeopTotel", twoDepAdjPeopTotel);
				map2.put("twoDepAdjSarTolel", twoDepAdjSarTolel);
				map2.put("twoDepPeopTotel", twoDepPeopTotel);
				map2.put("twoConfirmAdjPeopTotel",twoConfirmAdjPeopTotel);
				map2.put("twoConfirmAdjSarTotel",twoConfirmAdjSarTotel);
				oneDepAdjPeopTotel += twoDepAdjPeopTotel;
				oneDepAdjSarTolel += twoDepAdjSarTolel;
				oneConfirmAdjPeopTotel += twoConfirmAdjPeopTotel;
				oneConfirmAdjSarTotel += twoConfirmAdjSarTotel;
			}
			map.put("oneDepAdjPeopTotel", oneDepAdjPeopTotel);
			map.put("oneDepAdjSarTolel", oneDepAdjSarTolel);
			map.put("oneDepPeopTotel", oneDepPeopTotel);
			map.put("oneConfirmAdjPeopTotel",oneConfirmAdjPeopTotel);
			map.put("oneConfirmAdjSarTotel",oneConfirmAdjSarTotel);
		}
		return RestUtils.returnSuccess(list);
	}

	public RestResponse backDepMess2Salary(List<Map<String, Object>> list) {
		for (Map<String, Object> map : list) {
			Integer oneDepPeopTotel = 0;
			Integer oneDepAdjPeopTotel = 0;
			Double oneDepAdjSarTolel = 0.0;
			Integer oneConfirmAdjPeopTotel = 0;
			Double oneConfirmAdjSarTotel = 0.0;
			List<Map<String, Object>> SecondDep = (List<Map<String, Object>>) map.get("SecondDep");
			if (SecondDep != null) {
				for (Map<String, Object> map2 : SecondDep) {
					List<Map<String, Object>> employeeMess = (List<Map<String, Object>>) map2.get("employeeMess");
					Integer twoDepPeopTotel = 0;
					Integer twoDepAdjPeopTotel = 0;
					Double twoDepAdjSarTolel = 0.0;
					Integer twoConfirmAdjPeopTotel = 0;
					Double twoConfirmAdjSarTotel = 0.0;
					if (employeeMess != null) {
						for (Map<String, Object> map3 : employeeMess) {
							twoDepPeopTotel++;
							Object erpNewBaseWage = map3.get("erpNewBaseWage");
							Object erpNewPostWage = map3.get("erpNewPostWage");
							Object erpNewPerformance = map3.get("erpNewPerformance");
							Object erpNewAllowance = map3.get("erpNewAllowance");
							Object erpNewTelFarePerquisite = map3.get("erpNewTelFarePerquisite");
							Double wageTotel = Double.valueOf(map3.get("wageTotel").toString());
							Double IerpNewBaseWage;
							Double IerpNewPostWage;
							Double IerpNewPerformance;
							Double IerpNewAllowance;
							Double IerpNewTelFarePerquisite;
							Double newWageTotel = 0.0;
							if (erpNewBaseWage != null || erpNewPostWage != null || erpNewPerformance != null
									|| erpNewAllowance != null || erpNewTelFarePerquisite != null) {
								if (erpNewBaseWage == null) {
									IerpNewBaseWage = 0.0;
								} else {
									IerpNewBaseWage = Double.valueOf(erpNewBaseWage.toString());
								}

								if (erpNewPostWage == null) {
									IerpNewPostWage = 0.0;
								} else {
									IerpNewPostWage = Double.valueOf(erpNewPostWage.toString());
								}

								if (erpNewPerformance == null) {
									IerpNewPerformance = 0.0;
								} else {
									IerpNewPerformance = Double.valueOf(erpNewPerformance.toString());
								}

								if (erpNewAllowance == null) {
									IerpNewAllowance = 0.0;
								} else {
									IerpNewAllowance = Double.valueOf(erpNewAllowance.toString());
								}

								if (erpNewTelFarePerquisite == null) {
									IerpNewTelFarePerquisite = 0.0;
								} else {
									IerpNewTelFarePerquisite = Double.valueOf(erpNewTelFarePerquisite.toString());
								}

								newWageTotel = IerpNewBaseWage + IerpNewPostWage + IerpNewPerformance + IerpNewAllowance
										+ IerpNewTelFarePerquisite;
								map3.put("newWageTotel", newWageTotel);
								Double newAndOldCha = newWageTotel - wageTotel;
								map3.put("newAndOldCha", newAndOldCha);
								twoDepAdjPeopTotel++;
								twoDepAdjSarTolel += newAndOldCha;
							} else {
								map3.put("newAndOldCha", "填写");
							}
							if (map3.get("adjust_status") == "1") {
								twoConfirmAdjPeopTotel++;
								twoConfirmAdjSarTotel += Double.valueOf(String.valueOf(map3.get("newAndOldCha")));
							}
						}
					}
					map2.put("twoDepPeopTotel", twoDepPeopTotel);
					map2.put("twoDepAdjPeopTotel", twoDepAdjPeopTotel);
					map2.put("twoDepAdjSarTolel", twoDepAdjSarTolel);
					map2.put("twoConfirmAdjPeopTotel", twoConfirmAdjPeopTotel);
					map2.put("twoConfirmAdjSarTotel", twoConfirmAdjSarTotel);
					oneDepPeopTotel += twoDepPeopTotel;
					oneDepAdjPeopTotel += twoDepAdjPeopTotel;
					oneDepAdjSarTolel += twoDepAdjSarTolel;
					oneConfirmAdjPeopTotel += twoConfirmAdjPeopTotel;
					oneConfirmAdjSarTotel += twoConfirmAdjSarTotel;
				}
			}
			map.put("oneDepPeopTotel", oneDepPeopTotel);
			map.put("oneDepAdjPeopTotel", oneDepAdjPeopTotel);
			map.put("oneDepAdjSarTolel", oneDepAdjSarTolel);
			map.put("oneConfirmAdjPeopTotel", oneConfirmAdjPeopTotel);
			map.put("oneConfirmAdjSarTotel", oneConfirmAdjSarTotel);
		}
		return RestUtils.returnSuccess(list);
	}
	
	@Transactional
	public RestResponse confirmAdjSarlay(Map<String, Object> map) {
		List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("mess");
		for (Map<String, Object> mapE : list) {
			Integer oneConfirmAdjPeopTotel = 0;
			Double oneConfirmAdjSarTotel = 0.0;
			List<Map<String, Object>> SecondDep = (List<Map<String, Object>>) mapE.get("SecondDep");
			if (SecondDep != null) {
				for (Map<String, Object> map2 : SecondDep) {
					List<Map<String, Object>> employeeMess = (List<Map<String, Object>>) map2.get("employeeMess");
					Integer twoConfirmAdjPeopTotel = 0;
					Double twoConfirmAdjSarTotel = 0.0;
					if (employeeMess != null) {
						for (Map<String, Object> map3 : employeeMess) {
							Object employeeId = map3.get("employeeId");
							if (employeeId.equals(map.get("employeeId"))) {
								twoConfirmAdjPeopTotel++;
								Double newAndOldCha = Double.valueOf(String.valueOf(map3.get("newAndOldCha")));
								twoConfirmAdjSarTotel += newAndOldCha;
								Map<String, Object> selectSarAdjByEId = erpSalaryAdjustMapper
										.selectSarAdjByEId((Integer) employeeId);
								String erpBaseWage = AesUtils.encrypt(String.valueOf(map3.get("erpBaseWage")));
								String erpPostWage = AesUtils.encrypt(String.valueOf(map3.get("erpPostWage")));
								String erpPerformance = AesUtils.encrypt(String.valueOf(map3.get("erpPerformance")));
								String erpAllowance = AesUtils.encrypt(String.valueOf(map3.get("erpAllowance")));
								String erpTelFarePerquisite = AesUtils
										.encrypt(String.valueOf(map3.get("erpTelFarePerquisite")));
								String erpNewBaseWage = AesUtils.encrypt(String.valueOf(map3.get("erpNewBaseWage")));
								String erpNewPostWage = AesUtils.encrypt(String.valueOf(map3.get("erpNewPostWage")));
								String erpNewPerformance = AesUtils
										.encrypt(String.valueOf(map3.get("erpNewPerformance")));
								String erpNewAllowance = AesUtils.encrypt(String.valueOf(map3.get("erpNewAllowance")));
								String erpNewTelFarePerquisite = AesUtils
										.encrypt(String.valueOf(map3.get("erpNewTelFarePerquisite")));
								String erpAdjBecause = String.valueOf(map3.get("erpAdjBecause"));
								String erpAdjIndex = String.valueOf(map3.get("erpAdjIndex"));
								String approverId = String.valueOf(mapE.get("approverId"));
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
								String formatDate = sdf.format(new Date());
//								
								if (selectSarAdjByEId != null) {
									try {
										Map<String, Object> map2c = new HashMap<>();
										map2c.put("erpBaseWage", erpBaseWage);
										map2c.put("erpPostWage", erpPostWage);
										map2c.put("erpPerformance", erpPerformance);
										map2c.put("erpAllowance", erpAllowance);
										map2c.put("erpTelFarePerquisite", erpTelFarePerquisite);
										map2c.put("employeeId", employeeId);
										map2c.put("adjust_status", 1);
										map2c.put("erpAdjBecause", erpAdjBecause);
										map2c.put("erpAdjIndex", erpAdjIndex);
										map2c.put("erpNewBaseWage", erpNewBaseWage);
										map2c.put("erpNewPostWage", erpNewPostWage);
										map2c.put("erpNewPerformance", erpNewPerformance);
										map2c.put("erpNewAllowance", erpNewAllowance);
										map2c.put("erpNewTelFarePerquisite", erpNewTelFarePerquisite);
										map2c.put("approverId", approverId);
										map2c.put("adjust_time", map.get("effectTime"));
										boolean statueMess = erpSalaryAdjustMapper.updateSalaryMess(map2c);
										if (statueMess == true) {
											map3.put("adjust_status", 1);
											logger.info("薪资调整计划状态修改成功...");
										} else {
											logger.info("薪资调整计划状态修改失败...");
										}
										
									} catch (Exception e) {
										logger.error("updateStatueMess方法出现异常：" + e.getMessage(), e);
										return RestUtils.returnFailure("updateStatueMess方法出现异常：" + e.getMessage());
									}
								} else {
									Map<String, Object> map2E = new HashMap<>();
									map2E.put("erpBaseWage", erpBaseWage);
									map2E.put("erpPostWage", erpPostWage);
									map2E.put("erpPerformance", erpPerformance);
									map2E.put("erpAllowance", erpAllowance);
									map2E.put("erpTelFarePerquisite", erpTelFarePerquisite);
									map2E.put("employeeId", employeeId);
									map2E.put("adjust_status", 1);
									map2E.put("erpAdjBecause", erpAdjBecause);
									map2E.put("erpAdjIndex", erpAdjIndex);
									map2E.put("erpNewBaseWage", erpNewBaseWage);
									map2E.put("erpNewPostWage", erpNewPostWage);
									map2E.put("erpNewPerformance", erpNewPerformance);
									map2E.put("erpNewAllowance", erpNewAllowance);
									map2E.put("erpNewTelFarePerquisite", erpNewTelFarePerquisite);
									map2E.put("approverId", approverId);
									map2E.put("adjust_time", map.get("effectTime"));
									try {
										boolean salaryMess = erpSalaryAdjustMapper.insertSalaryMess(map2E);
										if (salaryMess == true) {
											map3.put("adjust_status", 1);
											logger.info("薪资调整计划保存成功...");
										} else {
											logger.info("薪资调整计划保存失败...");
										}
									} catch (Exception e) {
										logger.error("insertSalaryMess方法出现异常：" + e.getMessage(), e);
										return RestUtils.returnFailure("insertSalaryMess方法出现异常：" + e.getMessage());
									}

								}
							}
						}
					}
					map2.put("twoConfirmAdjPeopTotel", twoConfirmAdjPeopTotel);
					map2.put("twoConfirmAdjSarTotel", twoConfirmAdjSarTotel);
					oneConfirmAdjPeopTotel += twoConfirmAdjPeopTotel;
					oneConfirmAdjSarTotel += twoConfirmAdjSarTotel;
				}
			}
			mapE.put("oneConfirmAdjPeopTotel", oneConfirmAdjPeopTotel);
			mapE.put("oneConfirmAdjSarTotel", oneConfirmAdjSarTotel);
		}
		return RestUtils.returnSuccess(list);
	}
	
	/**
	 * Description: 定时任务自动修改薪资调整
	 * 每日23点30分查询当日的调薪计划，并修改薪资调整
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2019年02月15日 下午18:16:13
	 */
	@Transactional
	public RestResponse automaticAdjustSalaryScheduler() {
		logger.info("进入automaticAdjustSalaryScheduler方法，无参数");
		try {
			/*
			 * 获取当前年月日，如2018-02-20
			 */
			Calendar dateObj = Calendar.getInstance();
			int year = dateObj.get(Calendar.YEAR);
			int month = dateObj.get(Calendar.MONTH)+1;
			int day = dateObj.get(Calendar.DATE);
			
			String adjustTime = null;//当日日期
			if(month<10) {
				if(day<10){
					adjustTime = year+"-0"+month+"-0"+day;
				}else{
					adjustTime = year+"-0"+month+"-"+day;
				}
			}else {
				if(day<10){
					adjustTime = year+"-"+month+"-0"+day;
				}else{
					adjustTime = year+"-"+month+"-"+day;
				}
			}
			
			List<Map<String,Object>> adjustList = erpSalaryAdjustMapper.selectSalAdjRecByAdjustTime(adjustTime);
			logger.info("即将调薪的列表"+adjustList);
			
			for (Map<String, Object> adjustMap : adjustList) {
				/*
				 * 将加密的薪酬信息，赋值给薪酬管理的PO对象
				 */
				Integer employeeId = Integer.valueOf(String.valueOf(adjustMap.get("erp_employee_id")));//员工ID
				
				ErpBasePayroll erpBasePayroll = new ErpBasePayroll();
				erpBasePayroll.setErpEmployeeId(employeeId);//员工ID
				erpBasePayroll.setErpBaseWage(adjustMap.get("adjust_base_wage") == null ? null : String.valueOf(adjustMap.get("adjust_base_wage")));//基本工资
				erpBasePayroll.setErpPostWage(adjustMap.get("adjust_post_wage") == null ? null : String.valueOf(adjustMap.get("adjust_post_wage")));//岗位工资
				erpBasePayroll.setErpPerformance(adjustMap.get("adjust_performance") == null ? null : String.valueOf(adjustMap.get("adjust_performance")));//月度绩效
				erpBasePayroll.setErpAllowance(adjustMap.get("adjust_allowance") == null ? null : String.valueOf(adjustMap.get("adjust_allowance")));//月度项目津贴
				erpBasePayroll.setErpTelFarePerquisite(adjustMap.get("adjust_tel_fare_perquisite") == null ? null : String.valueOf(adjustMap.get("adjust_tel_fare_perquisite")));//话费补助
				erpBasePayroll.setErpSocialSecurityBase(adjustMap.get("adjust_social_security_base") == null ? null : String.valueOf(adjustMap.get("adjust_social_security_base")));//社保基数
				erpBasePayroll.setErpAccumulationFundBase(adjustMap.get("adjust_accumulation_fund_base") == null ? null : String.valueOf(adjustMap.get("adjust_accumulation_fund_base")));//公积金基数
				/*
				 * 如果薪酬表中有员工，则更新；如果没有，则新增
				 * 薪酬数据允许重复导入，以最新的数据为准
				 */
				ErpBasePayroll validResult = this.erpBasePayrollMapper.findBasePayrollDetailByEmpId(employeeId);
				if(validResult==null) {
					this.erpBasePayrollMapper.insertBasePayroll(erpBasePayroll);
				}else {
					this.erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);
				}

				/*
				 * 将该员工的修改信息加入日志中
				 */
				ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
				basePayrollUpdateRecord.setEmployee(null);// 被修改的员工
				basePayrollUpdateRecord.setEmployeeId(employeeId);
				basePayrollUpdateRecord
						.setProcessor("调薪后自动更新基础薪资表");// 修改人
				basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
				basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
				this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);

			}
			
			logger.info("automaticAdjustSalaryScheduler执行成功！");
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("定时器automaticAdjustSalaryScheduler发生异常 ："+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致修改薪资调整失败！");
		}
	}

	/**
	 * 查询部门人员调薪列表
	 * @param year 年度
	 * @param departmentId 一级部门id
	 * @param departmentSalaryAdjustId 部门调薪计划id
	 * @param status 审批状态
	 * @param type 调薪类型
	 * @param employeeName 员工姓名
	 * @param token
	 * @return
	 */
	public RestResponse findDepartmentSalaryAdjustList(String year, String departmentSalaryAdjustPlan, Integer departmentId, Integer departmentSalaryAdjustId,
													   Integer status, Integer departmentStatus,Integer type, String employeeName,Boolean isAllEmployee, Boolean isInsert, String token) {
		logger.info("进入automaticAdjustSalaryScheduler方法，无参数");
		List<Map<String, Object>> returnList = new ArrayList<>();
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer employeeId = erpUser.getUserId();
			//获取部门信息
			List<Map<String, Object>> departmentList = null;
			// 调用ERP-人力资源 工程 的操作层服务接口-获取用户权限下一级部门信息
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/department/findFirstDepartmentAndSuperLeaderByUserId";
			String body = null;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.add("token", token);
			HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
			if (200 != response.getStatusCodeValue()) {
				return RestUtils.returnFailure("调用人力资源工程失败！");
			}
			/*
			 * 解析请求的结果
			 */
			Map<String, Object> responseBody = response.getBody();
			if (!"200".equals(responseBody.get("status"))) {
				return RestUtils.returnFailure("人力资源工程发生异常！");
			}
			departmentList = (List<Map<String, Object>>) responseBody.get("data");
			if(departmentList == null || departmentList.size() <= 0){
				return RestUtils.returnSuccess(new ArrayList<>());
			}
			Map<String,Object> departmentNameMap = new HashMap<>();
			for(Map<String,Object> department : departmentList){
				departmentNameMap.put(String.valueOf(department.get("departmentId")), department);
			}

			List<Integer> departmentIdList = new ArrayList<>();
			Map<String, Object> paramMap = new HashMap<>();
			//查询各一级部门总人数
			String departmentUrl = protocolType+"nantian-erp-hr/nantian-erp/erp/department/findFirstDepartmentCountMap";
			HttpHeaders requestHeadersForHr = new HttpHeaders();
			requestHeadersForHr.add("token", token);
			HttpEntity<String>	requestForHr = new HttpEntity<String>(null,requestHeadersForHr);

			ResponseEntity<RestResponse> departmentResponse = restTemplate.exchange(departmentUrl,HttpMethod.GET,requestForHr,RestResponse.class);
			//跨工程调用响应失败
			if(200 != departmentResponse.getStatusCodeValue() || !"200".equals(departmentResponse.getBody().getStatus())) {
				logger.error("调用人力资源工程发生异常，响应失败！"+departmentResponse);
				return RestUtils.returnFailure("调用人力资源工程发生异常，响应失败！");
			}
			Map<String, Object> departmentInfoMap = (Map<String, Object>) departmentResponse.getBody().getData();

			//获取员工对应详细信息
			String urlForHr = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeInfoMap?employeeName="+employeeName;

			ResponseEntity<RestResponse> responseForHr = restTemplate.exchange(urlForHr,HttpMethod.GET,requestForHr,RestResponse.class);
			//跨工程调用响应失败
			if(200 != responseForHr.getStatusCodeValue() || !"200".equals(responseForHr.getBody().getStatus())) {
				logger.error("调用人力资源工程发生异常，响应失败！"+responseForHr);
				return RestUtils.returnFailure("调用人力资源工程发生异常，响应失败！");
			}
			List<String> employeeIdList = new ArrayList<>();
			Map<String, Object> employeeInfoMap = (Map<String, Object>) responseForHr.getBody().getData();
			for(Map.Entry<String, Object> entry : employeeInfoMap.entrySet()){
				employeeIdList.add(entry.getKey());
			}

			//获取所有员工对应详细信息
			String allEmployeeUrlForHr = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeInfoMap";
			ResponseEntity<RestResponse> allEmployeeResponseForHr = restTemplate.exchange(allEmployeeUrlForHr,HttpMethod.GET,requestForHr,RestResponse.class);
			//跨工程调用响应失败
			if(200 != allEmployeeResponseForHr.getStatusCodeValue() || !"200".equals(allEmployeeResponseForHr.getBody().getStatus())) {
				logger.error("调用人力资源工程发生异常，响应失败！"+allEmployeeResponseForHr);
				return RestUtils.returnFailure("调用人力资源工程发生异常，响应失败！");
			}
			Map<String, Object> allEmployeeInfoMap = (Map<String, Object>) allEmployeeResponseForHr.getBody().getData();

			if(departmentId == null){
				//部门为空，只可能是调薪计划列表没有选部门查询条件的查询
				for(Map<String, Object> departmentMap: departmentList){
					departmentIdList.add(Integer.valueOf(String.valueOf(departmentMap.get("departmentId"))));
				}
				paramMap.put("departmentIdList", departmentIdList);
				paramMap.put("year", year);
				paramMap.put("type", type);
				paramMap.put("departmentSalaryAdjustPlan", departmentSalaryAdjustPlan);
				paramMap.put("departmentSalaryAdjustId", departmentSalaryAdjustId);
				paramMap.put("loginEmployeeId", employeeId);
				if(departmentStatus != null){
					paramMap.put("departmentStatus", departmentStatus);
				}
				//查询所有部门调薪批次列表
				List<DepartmentSalaryAdjustVO> departmentSalaryAdjustList = departmentSalaryAdjustMapper.findDepartmentSalaryAdjustList(paramMap);
				List<Map<String, Object>> departmentAndEmployeeSalaryList = returnDepartmentAndEmployeeSalary(status,employeeIdList, departmentInfoMap, departmentSalaryAdjustList,allEmployeeInfoMap,departmentNameMap, departmentStatus, employeeId);
				if(isAllEmployee != null && isAllEmployee){
					List<Map<String, Object>> departmentAndEmployeeList = returnDepartmentAndEmployee(paramMap, employeeName, departmentSalaryAdjustList,departmentInfoMap,allEmployeeInfoMap,departmentNameMap,false, token);
					return RestUtils.returnSuccess(returnDepartmentAndEmployeeAndSalaryAdjustInfoList(departmentAndEmployeeList, departmentAndEmployeeSalaryList));
				}else{
					return RestUtils.returnSuccess(departmentAndEmployeeSalaryList);
				}
			}else{
				//部门id不为空
				if(isInsert != null && isInsert){
					//是新增页面查询部门下所有员工并且没有调薪信息
					paramMap.put("departmentId", departmentId);
					paramMap.put("year", year);
					paramMap.put("type", type);
					//查询所有部门调薪批次列表
					List<Map<String, Object>> departmentAndEmployeeList = returnDepartmentAndEmployee(paramMap, employeeName, null,departmentInfoMap, allEmployeeInfoMap,departmentNameMap,isInsert,token);
					return RestUtils.returnSuccess(departmentAndEmployeeList);
				}else{
					//不是新增页面并且部门id不为空
					if(departmentSalaryAdjustId != null){
						//薪资调整ID不为空，是修改页面（或审批页面切换全员与非全员）
						//查询本部门的所有员工包含调薪信息
						paramMap.put("departmentSalaryAdjustId", departmentSalaryAdjustId);
						paramMap.put("year", year);
						paramMap.put("type", type);
						paramMap.put("loginEmployeeId", employeeId);
						if(departmentStatus != null){
							paramMap.put("departmentStatus", departmentStatus);
						}
						List<DepartmentSalaryAdjustVO> departmentSalaryAdjustList = departmentSalaryAdjustMapper.findDepartmentSalaryAdjustList(paramMap);
						List<Map<String, Object>> departmentAndEmployeeSalaryList = returnDepartmentAndEmployeeSalary(status,employeeIdList, departmentInfoMap, departmentSalaryAdjustList,allEmployeeInfoMap,departmentNameMap,departmentStatus, employeeId);
						if(isAllEmployee != null && isAllEmployee){
							List<Map<String, Object>> departmentAndEmployeeList = returnDepartmentAndEmployee(paramMap, employeeName, departmentSalaryAdjustList,departmentInfoMap,allEmployeeInfoMap,departmentNameMap,false, token);
							return RestUtils.returnSuccess(returnDepartmentAndEmployeeAndSalaryAdjustInfoList(departmentAndEmployeeList, departmentAndEmployeeSalaryList));
						}else{
							return RestUtils.returnSuccess(departmentAndEmployeeSalaryList);
						}
					}else{
						//薪资调整ID为空，是列表查询页面
						paramMap.put("departmentId", departmentId);
						paramMap.put("year", year);
						paramMap.put("type", type);
						paramMap.put("departmentSalaryAdjustPlan", departmentSalaryAdjustPlan);
						paramMap.put("departmentSalaryAdjustId", departmentSalaryAdjustId);
						paramMap.put("loginEmployeeId", employeeId);
						if(departmentStatus != null){
							paramMap.put("departmentStatus", departmentStatus);
						}
						List<DepartmentSalaryAdjustVO> departmentSalaryAdjustList = departmentSalaryAdjustMapper.findDepartmentSalaryAdjustList(paramMap);
						List<Map<String, Object>> departmentAndEmployeeSalaryList = returnDepartmentAndEmployeeSalary(status,employeeIdList, departmentInfoMap, departmentSalaryAdjustList,allEmployeeInfoMap,departmentNameMap, departmentStatus, employeeId);
						if( isAllEmployee != null && isAllEmployee){
							//查询部门下所有调薪批次与下属的员工，（包含调薪信息）
							//查询所有部门调薪批次列表
							List<Map<String, Object>> departmentAndEmployeeList = returnDepartmentAndEmployee(paramMap, employeeName, departmentSalaryAdjustList,departmentInfoMap,allEmployeeInfoMap,departmentNameMap,false, token);
							return RestUtils.returnSuccess(returnDepartmentAndEmployeeAndSalaryAdjustInfoList(departmentAndEmployeeList, departmentAndEmployeeSalaryList));
						}else{
							//查询部门下所有调薪批次与下属的员工
							return RestUtils.returnSuccess(departmentAndEmployeeSalaryList);
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error("findDepartmentSalaryAdjustList发生异常 ："+e.getMessage(),e);
			return RestUtils.returnFailure("查询部门人员调薪列表失败！");
		}
	}

	/**
	 * 获取全部员工包含调薪信息列表
	 * @param departmentAndEmployeeList
	 * @param departmentAndEmployeeSalaryList
	 * @return
	 */
	private List<Map<String, Object>> returnDepartmentAndEmployeeAndSalaryAdjustInfoList(List<Map<String, Object>>departmentAndEmployeeList, List<Map<String, Object>> departmentAndEmployeeSalaryList){
		for(Map<String, Object> departmentAndEmployeeMap : departmentAndEmployeeList){
			for(Map<String, Object> departmentAndEmployeeSalaryeMap : departmentAndEmployeeSalaryList){
				if(departmentAndEmployeeMap.get("departmentSalaryAdjustId").equals(departmentAndEmployeeSalaryeMap.get("departmentSalaryAdjustId"))) {
					//有调薪信息,给部门薪酬赋值
					//调薪ID
					departmentAndEmployeeMap.put("departmentSalaryAdjustId", departmentAndEmployeeSalaryeMap.get("departmentSalaryAdjustId"));
					//调薪计划
					departmentAndEmployeeMap.put("departmentSalaryAdjustPlan", departmentAndEmployeeSalaryeMap.get("departmentSalaryAdjustPlan"));
					//调薪状态
					departmentAndEmployeeMap.put("departmentSalaryAdjustStatus", departmentAndEmployeeSalaryeMap.get("departmentSalaryAdjustStatus"));
					//调薪类型
					departmentAndEmployeeMap.put("type", departmentAndEmployeeSalaryeMap.get("type"));
					//部门ID
					departmentAndEmployeeMap.put("departmentId", departmentAndEmployeeSalaryeMap.get("departmentId"));
					//调薪人数
					departmentAndEmployeeMap.put("salaryAdjustPeopleNumber", departmentAndEmployeeSalaryeMap.get("salaryAdjustPeopleNumber"));
					//调薪金额
					departmentAndEmployeeMap.put("salaryAdjustAmount", departmentAndEmployeeSalaryeMap.get("salaryAdjustAmount"));
					//社保人数
					departmentAndEmployeeMap.put("socialSecurityPeopleNumber", departmentAndEmployeeSalaryeMap.get("socialSecurityPeopleNumber"));
					//社保金额
					departmentAndEmployeeMap.put("socialSecurityAmount", departmentAndEmployeeSalaryeMap.get("socialSecurityAmount"));
					//公积金人数
					departmentAndEmployeeMap.put("accumulationFundPeopleNumber", departmentAndEmployeeSalaryeMap.get("accumulationFundPeopleNumber"));
					//公积金金额
					departmentAndEmployeeMap.put("accumulationFundAmount", departmentAndEmployeeSalaryeMap.get("accumulationFundAmount"));
					//总人数
					departmentAndEmployeeMap.put("totalPeopleNumber", departmentAndEmployeeSalaryeMap.get("totalPeopleNumber"));

					List<Map<String, Object>> employeeList = (List<Map<String, Object>>) departmentAndEmployeeMap.get("employeeSalaryAdjustList");
					List<Map<String, Object>> employeeSalaryAdjustList = (List<Map<String, Object>>) departmentAndEmployeeSalaryeMap.get("employeeSalaryAdjustList");
					for (Map<String, Object> employee : employeeList) {
						for (Map<String, Object> employeeSalaryAdjust : employeeSalaryAdjustList) {
							if (employee.get("employeeId").equals(employeeSalaryAdjust.get("employeeId"))) {
								//给员工调薪信息赋值
								//调薪记录id
								employee.put("salaryAdjustId", employeeSalaryAdjust.get("salaryAdjustId"));
								//员工ID
								employee.put("employeeId", employeeSalaryAdjust.get("employeeId"));
								//当前薪资
								employee.put("formerSalary", employeeSalaryAdjust.get("formerSalary"));
								//调整薪资
								employee.put("adjustSalary", employeeSalaryAdjust.get("adjustSalary"));
								//原来社保基数解密
								employee.put("formerSocialSecurityBase", employeeSalaryAdjust.get("formerSocialSecurityBase"));
								//原来公积金基数解密
								employee.put("formerAccumulationFundBase", employeeSalaryAdjust.get("formerAccumulationFundBase"));
								//调整后社保基数解密
								employee.put("adjustSocialSecurityBase", employeeSalaryAdjust.get("adjustSocialSecurityBase"));
								//调整后公积金基数解密
								employee.put("adjustAccumulationFundBase", employeeSalaryAdjust.get("adjustAccumulationFundBase"));
								//原来基本工资解密
								employee.put("formerBaseWage", employeeSalaryAdjust.get("formerBaseWage"));
								//原来岗位工资解密
								employee.put("formerPostWage", employeeSalaryAdjust.get("formerPostWage"));
								//原来月度绩效解密
								employee.put("formerPerformance", employeeSalaryAdjust.get("formerPerformance"));
								//原来项目补贴解密
								employee.put("formerAllowance", employeeSalaryAdjust.get("formerAllowance"));
								//原来话费补助解密
								employee.put("formerTelFarePerquisite", employeeSalaryAdjust.get("formerTelFarePerquisite"));
								//调整后基本工资解密
								employee.put("adjustBaseWage", employeeSalaryAdjust.get("adjustBaseWage"));
								//调整后岗位工资解密
								employee.put("adjustPostWage", employeeSalaryAdjust.get("adjustPostWage"));
								//调整后月度绩效解密
								employee.put("adjustPerformance", employeeSalaryAdjust.get("adjustPerformance"));
								//调整后项目补贴解密
								employee.put("adjustAllowance", employeeSalaryAdjust.get("adjustAllowance"));
								//调整后话费补助解密
								employee.put("adjustTelFarePerquisite", employeeSalaryAdjust.get("adjustTelFarePerquisite"));
								//调整社保金额
								employee.put("adjustSocialSecurityBaseSubtract", employeeSalaryAdjust.get("adjustSocialSecurityBaseSubtract"));
								//调整公积金金额
								employee.put("adjustAccumulationFundBaseSubtract", employeeSalaryAdjust.get("adjustAccumulationFundBaseSubtract"));
								//调整原因
								employee.put("adjustReason", employeeSalaryAdjust.get("adjustReason") == null  || String.valueOf(employeeSalaryAdjust.get("adjustReason")).equals("null")?  "":employeeSalaryAdjust.get("adjustReason"));
								//申请时间
								employee.put("applyDate", employeeSalaryAdjust.get("applyDate"));
								//生效日期
								employee.put("adjustDate", employeeSalaryAdjust.get("adjustDate"));
								//审批状态名称
								employee.put("adjustStatusName", employeeSalaryAdjust.get("adjustStatusName"));
								//审批状态
								employee.put("adjustStatus", employeeSalaryAdjust.get("adjustStatus"));
								//审批人与审批时间
								employee.put("approverAndTime", employeeSalaryAdjust.get("approverAndTime"));
								//是否为选中
								employee.put("isSelect", 1);
								//审批人
								employee.put("approverId", employeeSalaryAdjust.get("approverId"));
								//审批人名称
								employee.put("approverName", employeeSalaryAdjust.get("approverName"));
							}
						}
					}
					Collections.sort(employeeList, new Comparator<Map<String, Object>>(){
						public int compare(Map<String, Object> o1, Map<String, Object> o2) {
							Integer isSelect1 = o1.get("isSelect") == null ? 0 : Integer.valueOf(String.valueOf(o1.get("isSelect")));
							Integer isSelect2 = o2.get("isSelect") == null ? 0 : Integer.valueOf(String.valueOf(o2.get("isSelect")));
							return isSelect2.compareTo(isSelect1);
						}
					});
				}
			}
		}
		return departmentAndEmployeeList;
	}
	/**
	 * 查询一级部门下所有员工信息
	 * @param paramMap
	 * @return
	 */
	private List<Map<String, Object>> returnDepartmentAndEmployee(Map<String, Object> paramMap,String employeeName,
																  List<DepartmentSalaryAdjustVO> departmentSalaryAdjustList,Map<String, Object> departmentInfoMap,
																  Map<String, Object> allEmployeeInfoMap, Map<String, Object> departmentNameMap,Boolean isInsert,
																  String token) throws Exception{
		List<Map<String, Object>> returnList = new ArrayList<>();
		List<Integer> departmentIdList = new ArrayList<>();
		List<Integer> inserDepartmentIdList = new ArrayList<>();
		if(paramMap.get("departmentIdList") != null){
			//列表查询
			departmentIdList = (List<Integer>)paramMap.get("departmentIdList");
		}else if(paramMap.get("departmentId") != null){
			//新增页面或查询列表选部门
			departmentIdList.add(Integer.valueOf(String.valueOf(paramMap.get("departmentId"))));
		}else if(paramMap.get("departmentSalaryAdjustId") != null){
			//修改页面
			departmentIdList.add(departmentSalaryAdjustList.get(0).getDepartmentId());
		}
		if(paramMap.get("departmentId") != null){
			//新增页面或查询列表选部门
			inserDepartmentIdList.add(Integer.valueOf(String.valueOf(paramMap.get("departmentId"))));
		}

		Map<String, Object> employeeMap = new HashMap<>();
		for(Integer departmentId : departmentIdList){
			//获取各部门下员工信息
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmployeeByDeptAndUser";
			Map<String,Object> body  = new HashMap<>();
			//部门ID
			body.put("deptId",departmentId);
			//状态
			body.put("status", "0,1,2,3");
			//员工名称
			body.put("employeeName", employeeName);
			HttpHeaders headers = new HttpHeaders();
			headers.add("token", token);
			HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<>(body, headers);
			ResponseEntity<RestResponse> response = restTemplate.postForEntity(url, requestEntity, RestResponse.class);
			RestResponse restResponse = response.getBody();
			if (!"200".equals(restResponse.getStatus())) {
				return null;
			}
			// 解析请求的结果
			Map<String, Object> returnEmployeeMap = (Map)restResponse.getData();
			List<Map<String, Object>> employeeList = (List<Map<String, Object>>)returnEmployeeMap.get("employeList");
			employeeMap.put(departmentId.toString(), employeeList);
		}
		//获取各员工上次调整时间
		List<Map<String, Object>> employeeLastSalaryAdjustList = erpSalaryAdjustMapper.findEmployeeLastSalaryAdjustList();
		Map<String, Object> employeeLastSalaryAdjustInfoMap  = new HashMap<>();
		for(Map<String, Object> employeeLastSalaryAdjustMap : employeeLastSalaryAdjustList){
			employeeLastSalaryAdjustInfoMap.put(String.valueOf(employeeLastSalaryAdjustMap.get("employeeId")), employeeLastSalaryAdjustMap.get("lastAdjustTime"));
		}
		//获取一级部门下所有员工信息
		if(departmentSalaryAdjustList != null && departmentSalaryAdjustList.size() > 0){
			for (DepartmentSalaryAdjustVO departmentSalaryAdjust : departmentSalaryAdjustList){
				Map<String, Object> returnMap = new HashMap<>();
				Integer firstDepartment = departmentSalaryAdjust.getDepartmentId();
				List<Map<String, Object>> employeeList = (List<Map<String, Object>>)employeeMap.get(String.valueOf(firstDepartment));
				//部门总人数
				Integer totalPeopleNumber = departmentInfoMap.get(departmentSalaryAdjust.getDepartmentId().toString()) == null ? 0 : Integer.valueOf(String.valueOf(departmentInfoMap.get(departmentSalaryAdjust.getDepartmentId().toString())));
				//部门总人数
				returnMap.put("totalPeopleNumber", totalPeopleNumber);
				//部门ID
				returnMap.put("departmentId", firstDepartment);
				//调薪批次ID
                returnMap.put("departmentSalaryAdjustId", departmentSalaryAdjust.getId());
				//调薪类型
				returnMap.put("type", departmentSalaryAdjust.getType());
				//生效日期
				returnMap.put("adjustDate", ExDateUtils.dateToString(departmentSalaryAdjust.getAdjustDate(), "yyyy-MM-dd"));
				//申请人
				Map<String,Object> employeeInfo = new HashMap<>();
				if(departmentSalaryAdjust.getSubmitPersonId() != null){
					employeeInfo = (Map<String,Object>)(allEmployeeInfoMap.get(String.valueOf(departmentSalaryAdjust.getSubmitPersonId())));
				}else if(departmentSalaryAdjust.getModifyPersonId() != null){
					employeeInfo = (Map<String,Object>)(allEmployeeInfoMap.get(String.valueOf(departmentSalaryAdjust.getModifyPersonId())));
				}
				returnMap.put("submitPersonName", employeeInfo.get("name"));
				//部门名称
				returnMap.put("departmentName", ((Map<String, Object>)departmentNameMap.get(String.valueOf(firstDepartment))).get("departmentName"));
				//审批人
				returnMap.put("approverName", ((Map<String, Object>)departmentNameMap.get(String.valueOf(firstDepartment))).get("superLeaderName"));
				//审批人id
				returnMap.put("approverId", ((Map<String, Object>)departmentNameMap.get(String.valueOf(departmentSalaryAdjust.getDepartmentId()))).get("superLeaderId"));
				List<Map<String, Object>> employeeSalaryAdjustList = new ArrayList<>();
				for(Map<String, Object> employeeInfoMap : employeeList){
					Map<String, Object> employeeSalaryAdjustMap = new HashMap<>();
					//员工ID
					employeeSalaryAdjustMap.put("employeeId", employeeInfoMap.get("employeeId"));
					//二级部门名称
					employeeSalaryAdjustMap.put("secondDepartmentName", String.valueOf(employeeInfoMap.get("secondDeptName")));
					String idCardNumber = String.valueOf(employeeInfoMap.get("idCardNumber"));
					//员工姓名+身份证后四位
                    String employeeNameString = null;
                    if(idCardNumber.length() < 4){
                        employeeNameString = employeeInfoMap.get("name") + "("+idCardNumber+")";
                    }else{
                        employeeNameString = employeeInfoMap.get("name") + "("+idCardNumber.substring(idCardNumber.length() - 4)+")";
                    }
                    employeeSalaryAdjustMap.put("employeeName", employeeNameString);
                    //员工状态名称
					employeeSalaryAdjustMap.put("employeeStatusName", employeeInfoMap.get("statusName"));
					//员工状态
					employeeSalaryAdjustMap.put("employeeStatus", employeeInfoMap.get("status"));
					//调薪类型
					employeeSalaryAdjustMap.put("typeName", departmentSalaryAdjust.getType() == 2 ? "社保/公积金基数调整" : "调整薪资");
					//上次调整时间
					employeeSalaryAdjustMap.put("lastAdjustTime", employeeLastSalaryAdjustInfoMap.get(String.valueOf(employeeInfoMap.get("employeeId"))));
					employeeSalaryAdjustList.add(employeeSalaryAdjustMap);
				}

				returnMap.put("employeeSalaryAdjustList", employeeSalaryAdjustList);
				returnList.add(returnMap);
			}
		}else{
			//新增或列表没查询到数据
			if(isInsert != null && isInsert){
				//新增
				for (Integer departmentId : departmentIdList){
					Map<String, Object> returnMap = new HashMap<>();
					Integer firstDepartment = departmentId;
					List<Map<String, Object>> employeeList = (List<Map<String, Object>>)employeeMap.get(String.valueOf(firstDepartment));
					//部门总人数
					Integer totalPeopleNumber = departmentInfoMap.get(departmentId.toString()) == null ? 0 : Integer.valueOf(String.valueOf(departmentInfoMap.get(departmentId.toString())));
					//部门总人数
					returnMap.put("totalPeopleNumber", totalPeopleNumber);
					//部门ID
					returnMap.put("departmentId", firstDepartment);
					//调薪类型
					returnMap.put("type", Integer.valueOf(String.valueOf(paramMap.get("type"))));
					List<Map<String, Object>> employeeSalaryAdjustList = new ArrayList<>();
					for(Map<String, Object> employeeInfoMap : employeeList){
						Map<String, Object> employeeSalaryAdjustMap = new HashMap<>();
						//员工ID
						employeeSalaryAdjustMap.put("employeeId", employeeInfoMap.get("employeeId"));
						//二级部门名称
						employeeSalaryAdjustMap.put("secondDepartmentName", String.valueOf(employeeInfoMap.get("secondDeptName")));
						String idCardNumber = String.valueOf(employeeInfoMap.get("idCardNumber"));
						//员工姓名+身份证后四位
						String employeeNameString = null;
						if(idCardNumber.length() < 4){
							employeeNameString = employeeInfoMap.get("name") + "("+idCardNumber+")";
						}else{
							employeeNameString = employeeInfoMap.get("name") + "("+idCardNumber.substring(idCardNumber.length() - 4)+")";
						}
						employeeSalaryAdjustMap.put("employeeName", employeeNameString);
						//员工状态
						employeeSalaryAdjustMap.put("employeeStatusName", employeeInfoMap.get("statusName"));
						//员工状态
						employeeSalaryAdjustMap.put("employeeStatus", employeeInfoMap.get("status"));
						//调薪类型
						employeeSalaryAdjustMap.put("typeName", "2".equals(String.valueOf(paramMap.get("type")))? "社保/公积金基数调整" : "调整薪资");
						//上次调整时间
						employeeSalaryAdjustMap.put("lastAdjustTime", employeeLastSalaryAdjustInfoMap.get(String.valueOf(employeeInfoMap.get("employeeId"))));
						employeeSalaryAdjustList.add(employeeSalaryAdjustMap);
					}

					returnMap.put("employeeSalaryAdjustList", employeeSalaryAdjustList);
					returnList.add(returnMap);
				}
			}
		}

		return returnList;
	}

	/**
	 * 查询部门与人员调薪信息
	 * @param paramMap
	 * @param status
	 * @param employeeIdList
	 * @param employeeInfoMap
	 * @return
	 */
	private List<Map<String, Object>> returnDepartmentAndEmployeeSalary(Integer status,
																		List<String> employeeIdList,
																		Map<String,Object> departmentInfoMap, List<DepartmentSalaryAdjustVO> departmentSalaryAdjustList,
																		Map<String, Object> allEmployeeIdInfoMap, Map<String, Object> departmentNameMap,
																		Integer departmentStatus, Integer loginEmployeeId) throws Exception{
		List<Map<String, Object>> returnList = new ArrayList<>();
		//查询所有有调薪的部门
		for (DepartmentSalaryAdjustVO departmentSalaryAdjust : departmentSalaryAdjustList){
			//总人数
			Integer totalPeopleNumber = departmentInfoMap.get(departmentSalaryAdjust.getDepartmentId().toString()) == null ? 0 : Integer.valueOf(String.valueOf(departmentInfoMap.get(departmentSalaryAdjust.getDepartmentId().toString())));
			//调薪人数
			Integer salaryAdjustPeopleNumber = 0;
			//调薪总金额
			BigDecimal salaryAdjustAmount = new BigDecimal(0.0);
			//调社保人数
			Integer socialSecurityPeopleNumber = 0;
			//调社保总金额
			BigDecimal socialSecurityAmount = new BigDecimal(0.0);
			//调公积金人数
			Integer accumulationFundPeopleNumber = 0;
			//调公积金总金额
			BigDecimal accumulationFundAmount = new BigDecimal(0.0);
			Map<String,Object> returnMap = new HashMap<>();
			returnMap.put("departmentSalaryAdjustId", departmentSalaryAdjust.getId());
			returnMap.put("departmentSalaryAdjustPlan", departmentSalaryAdjust.getPlan());
			returnMap.put("departmentSalaryAdjustStatus", departmentSalaryAdjust.getStatus());
			returnMap.put("departmentId", departmentSalaryAdjust.getDepartmentId());
			returnMap.put("type", departmentSalaryAdjust.getType());
			//生效日期
			returnMap.put("adjustDate", ExDateUtils.dateToString(departmentSalaryAdjust.getAdjustDate(), "yyyy-MM-dd"));
			//申请人
			Map<String,Object> employeeInfo = new HashMap<>();
			if(departmentSalaryAdjust.getSubmitPersonId() != null){
				employeeInfo = (Map<String,Object>)(allEmployeeIdInfoMap.get(String.valueOf(departmentSalaryAdjust.getSubmitPersonId())));
			}else if(departmentSalaryAdjust.getModifyPersonId() != null){
				employeeInfo = (Map<String,Object>)(allEmployeeIdInfoMap.get(String.valueOf(departmentSalaryAdjust.getModifyPersonId())));
			}
			returnMap.put("submitPersonName", employeeInfo.get("name"));
			//部门名称
			returnMap.put("departmentName", ((Map<String, Object>)departmentNameMap.get(String.valueOf(departmentSalaryAdjust.getDepartmentId()))).get("departmentName"));
			//审批人
			returnMap.put("approverName", ((Map<String, Object>)departmentNameMap.get(String.valueOf(departmentSalaryAdjust.getDepartmentId()))).get("superLeaderName"));
			//审批人id
			returnMap.put("approverId", ((Map<String, Object>)departmentNameMap.get(String.valueOf(departmentSalaryAdjust.getDepartmentId()))).get("superLeaderId"));
			Map<String, Object> employeeParamMap = new HashMap<>();
			employeeParamMap.put("status", status);
			employeeParamMap.put("departmentSalaryAdjustId", departmentSalaryAdjust.getId());
			employeeParamMap.put("employeeIdList", employeeIdList);
			employeeParamMap.put("departmentStatus", departmentStatus);
			employeeParamMap.put("loginEmployeeId", loginEmployeeId);
			List<Map<String, Object>> salaryAdjustList = erpSalaryAdjustMapper.findEmployeeSalaryAdjustListByparams(employeeParamMap);
			List<Map<String, Object>> employeeSalaryAdjustList = new ArrayList<>();
			for (Map<String, Object> salaryAdjustMap : salaryAdjustList){
				Map<String,Object> employeeSalaryAdjustMap = new HashMap<>();
				String employeeIdString = String.valueOf(salaryAdjustMap.get("employeeId"));
				Map<String,Object> employeeMap = (Map<String,Object>)(allEmployeeIdInfoMap.get(employeeIdString));
				//调薪记录id
				employeeSalaryAdjustMap.put("salaryAdjustId", Integer.valueOf(String.valueOf(salaryAdjustMap.get("salaryAdjustId"))));
				//员工ID
				employeeSalaryAdjustMap.put("employeeId", Integer.valueOf(employeeIdString));
				//二级部门名称
				employeeSalaryAdjustMap.put("secondDepartmentName", String.valueOf(employeeMap.get("secondDepartmentName")));
				String idCardNumber = String.valueOf(employeeMap.get("idCardNumber"));
				//员工姓名+身份证后四位
                String employeeNameString = null;
                if(idCardNumber.length() < 4){
                    employeeNameString = employeeMap.get("name") + "("+idCardNumber+")";
                }else{
                    employeeNameString = employeeMap.get("name") + "("+idCardNumber.substring(idCardNumber.length() - 4)+")";
                }
				employeeSalaryAdjustMap.put("employeeName", employeeNameString);
				//员工状态名称
				employeeSalaryAdjustMap.put("employeeStatusName", employeeMap.get("statusName"));
				//员工状态
				employeeSalaryAdjustMap.put("employeeStatus", employeeMap.get("status"));
				//调薪类型
				employeeSalaryAdjustMap.put("typeName", departmentSalaryAdjust.getType() == 2 ? "社保/公积金基数调整" : "调整薪资");
				//审批状态
				Integer adjustStatus = Integer.valueOf(String.valueOf(salaryAdjustMap.get("adjustStatus")));
				employeeSalaryAdjustMap.put("adjustStatus", adjustStatus);
				String adjustStatusName = "";
				if(adjustStatus == 0) {
					adjustStatusName = "待审批";
				}else if(adjustStatus == 1){
					adjustStatusName = "审批通过";
				}else if(adjustStatus == 11){
					adjustStatusName = "修订通过";
				}else if(adjustStatus == 2){
					adjustStatusName = "暂存";
				}else if(adjustStatus == 4){
					adjustStatusName = "驳回";
				}else if(adjustStatus == 5){
					adjustStatusName = "待总裁审批";
				}else if(adjustStatus == 51){
					adjustStatusName = "修订待总裁审批";
				}
				employeeSalaryAdjustMap.put("adjustStatusName", adjustStatusName);
					//原来基本工资加密
					String formerBaseWage = String.valueOf(salaryAdjustMap.get("formerBaseWage"));
					//原来岗位工资加密
					String formerPostWage = String.valueOf(salaryAdjustMap.get("formerPostWage"));
					//'原来月度绩效'加密
					String formerPerformance = String.valueOf(salaryAdjustMap.get("formerPerformance"));
					//'原来项目补贴'加密
					String formerAllowance = String.valueOf(salaryAdjustMap.get("formerAllowance"));
					//'原来话费补助'加密
					String formerTelFarePerquisite = String.valueOf(salaryAdjustMap.get("formerTelFarePerquisite"));

					//原来基本工资解密
					BigDecimal formerBaseWageBigDecimal = salaryAdjustMap.get("formerBaseWage") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerBaseWage));
					employeeSalaryAdjustMap.put("formerBaseWage", formerBaseWageBigDecimal);
					//原来岗位工资解密
					BigDecimal formerPostWageBigDecimal = salaryAdjustMap.get("formerPostWage") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerPostWage));
					employeeSalaryAdjustMap.put("formerPostWage", formerPostWageBigDecimal);
					//原来月度绩效解密
					BigDecimal formerPerformanceBigDecimal = salaryAdjustMap.get("formerPerformance") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerPerformance));
					employeeSalaryAdjustMap.put("formerPerformance", formerPerformanceBigDecimal);
					//原来项目补贴解密
					BigDecimal formerAllowanceBigDecimal = salaryAdjustMap.get("formerAllowance") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerAllowance));
					employeeSalaryAdjustMap.put("formerAllowance", formerAllowanceBigDecimal);
					//原来话费补助解密
					BigDecimal formerTelFarePerquisiteBigDecimal = salaryAdjustMap.get("formerTelFarePerquisite") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerTelFarePerquisite));
					employeeSalaryAdjustMap.put("formerTelFarePerquisite", formerTelFarePerquisiteBigDecimal);
					//当前薪资
					BigDecimal formerSalary = formerBaseWageBigDecimal.add(formerPostWageBigDecimal).add(formerPerformanceBigDecimal).add(formerAllowanceBigDecimal).add(formerTelFarePerquisiteBigDecimal);
					employeeSalaryAdjustMap.put("formerSalary", formerSalary);

					//调整后基本工资加密
					String adjustBaseWage = String.valueOf(salaryAdjustMap.get("adjustBaseWage"));
					//调整后岗位工资加密
					String adjustPostWage = String.valueOf(salaryAdjustMap.get("adjustPostWage"));
					//调整后月度绩效'加密
					String adjustPerformance = String.valueOf(salaryAdjustMap.get("adjustPerformance"));
					//调整后项目补贴'加密
					String adjustAllowance = String.valueOf(salaryAdjustMap.get("adjustAllowance"));
					//'调整后话费补助'加密
					String adjustTelFarePerquisite = String.valueOf(salaryAdjustMap.get("adjustTelFarePerquisite"));

					//调整后基本工资解密
					BigDecimal adjustBaseWageBigDecimal = salaryAdjustMap.get("adjustBaseWage") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustBaseWage));
					employeeSalaryAdjustMap.put("adjustBaseWage", adjustBaseWageBigDecimal);
					//调整后岗位工资解密
					BigDecimal adjustPostWageBigDecimal = salaryAdjustMap.get("adjustPostWage") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustPostWage));
					employeeSalaryAdjustMap.put("adjustPostWage", adjustPostWageBigDecimal);
					//调整后月度绩效解密
					BigDecimal adjustPerformanceBigDecimal = salaryAdjustMap.get("adjustPerformance") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustPerformance));
					employeeSalaryAdjustMap.put("adjustPerformance", adjustPerformanceBigDecimal);
					//调整后项目补贴解密
					BigDecimal adjustAllowanceBigDecimal = salaryAdjustMap.get("adjustAllowance") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustAllowance));
					employeeSalaryAdjustMap.put("adjustAllowance", adjustAllowanceBigDecimal);
					//调整后话费补助解密
					BigDecimal adjustTelFarePerquisiteBigDecimal = salaryAdjustMap.get("adjustTelFarePerquisite") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustTelFarePerquisite));
					employeeSalaryAdjustMap.put("adjustTelFarePerquisite", adjustTelFarePerquisiteBigDecimal);
					//调整薪资
					BigDecimal adjustAfterSalary = adjustBaseWageBigDecimal.add(adjustPostWageBigDecimal).add(adjustPerformanceBigDecimal).add(adjustAllowanceBigDecimal).add(adjustTelFarePerquisiteBigDecimal);
					BigDecimal adjustSalary= adjustAfterSalary.subtract(formerSalary);
					employeeSalaryAdjustMap.put("adjustSalary", adjustSalary);
					//薪资调整
					if(adjustSalary.compareTo(new BigDecimal(0.0)) != 0 && adjustStatus != 4){
						salaryAdjustPeopleNumber++;
						salaryAdjustAmount = salaryAdjustAmount.add(adjustSalary);
					}
					//社保/公积金调整
					//'原来社保基数加密
					String formerSocialSecurityBase = String.valueOf(salaryAdjustMap.get("formerSocialSecurityBase"));
					//'原来公积金基数加密
					String formerAccumulationFundBase = String.valueOf(salaryAdjustMap.get("formerAccumulationFundBase"));
					//原来社保基数解密
					BigDecimal formerSocialSecurityBaseBigDecimal = salaryAdjustMap.get("formerSocialSecurityBase") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerSocialSecurityBase));
					employeeSalaryAdjustMap.put("formerSocialSecurityBase",formerSocialSecurityBaseBigDecimal);
					//原来公积金基数解密
					BigDecimal formerAccumulationFundBaseBigDecimal = salaryAdjustMap.get("formerAccumulationFundBase") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerAccumulationFundBase));
					employeeSalaryAdjustMap.put("formerAccumulationFundBase",formerAccumulationFundBaseBigDecimal);
					//调整后社保基数加密
					String adjustSocialSecurityBase = String.valueOf(salaryAdjustMap.get("adjustSocialSecurityBase"));
					//'调整后公积金基数加密
					String adjustAccumulationFundBase = String.valueOf(salaryAdjustMap.get("adjustAccumulationFundBase"));
					//调整后社保基数解密
					BigDecimal adjustSocialSecurityBaseBigDecimal = salaryAdjustMap.get("adjustSocialSecurityBase") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustSocialSecurityBase));
					employeeSalaryAdjustMap.put("adjustSocialSecurityBase", adjustSocialSecurityBaseBigDecimal);
					BigDecimal adjustSocialSecurityBaseSubtract = adjustSocialSecurityBaseBigDecimal.subtract(formerSocialSecurityBaseBigDecimal);
					employeeSalaryAdjustMap.put("adjustSocialSecurityBaseSubtract", adjustSocialSecurityBaseSubtract);
					if(adjustSocialSecurityBaseSubtract.compareTo(new BigDecimal(0.0)) != 0 && adjustStatus != 4){
						socialSecurityPeopleNumber++;
						socialSecurityAmount = socialSecurityAmount.add(adjustSocialSecurityBaseSubtract);
					}
					//调整后公积金基数解密
					BigDecimal adjustAccumulationFundBaseBigDecimal = salaryAdjustMap.get("adjustAccumulationFundBase") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustAccumulationFundBase));
					employeeSalaryAdjustMap.put("adjustAccumulationFundBase", adjustAccumulationFundBaseBigDecimal);
					BigDecimal adjustAccumulationFundBaseSubtract = adjustAccumulationFundBaseBigDecimal.subtract(formerAccumulationFundBaseBigDecimal);
					employeeSalaryAdjustMap.put("adjustAccumulationFundBaseSubtract", adjustAccumulationFundBaseSubtract);
					if(adjustAccumulationFundBaseSubtract.compareTo(new BigDecimal(0.0)) != 0 && adjustStatus != 4){
						accumulationFundPeopleNumber++;
						accumulationFundAmount = accumulationFundAmount.add(adjustAccumulationFundBaseSubtract);
					}

				//上次调整时间
				employeeSalaryAdjustMap.put("lastAdjustTime", salaryAdjustMap.get("lastAdjustTime"));
				//调整原因
				employeeSalaryAdjustMap.put("adjustReason", salaryAdjustMap.get("adjustReason") == null || String.valueOf(salaryAdjustMap.get("adjustReason")).equals("null")? "" : salaryAdjustMap.get("adjustReason"));
				//申请时间
				employeeSalaryAdjustMap.put("applyDate", ExDateUtils.dateToString(departmentSalaryAdjust.getSubmitTime(), "yyyy-MM-dd"));
				//生效日期
				employeeSalaryAdjustMap.put("adjustDate", String.valueOf(salaryAdjustMap.get("adjustTime")));
				//审批人与审批时间
				if(salaryAdjustMap.get("approverId") != null){
					Map<String,Object> approverMap = (Map<String,Object>)allEmployeeIdInfoMap.get(String.valueOf(salaryAdjustMap.get("approverId")));
//					String approverTime = "";
//					if(salaryAdjustMap.get("approverTime") != null){
//						approverTime = ExDateUtils.dateToString((Date)salaryAdjustMap.get("approverTime"), "yyyy-MM-dd HH:mm");
//						employeeSalaryAdjustMap.put("approverAndTime", approverMap.get("name")+"/"+ approverTime);
//					}else{
//						employeeSalaryAdjustMap.put("approverAndTime", approverMap.get("name"));
//					}
					employeeSalaryAdjustMap.put("approverAndTime", approverMap.get("name"));
					employeeSalaryAdjustMap.put("approverId", salaryAdjustMap.get("approverId"));
					employeeSalaryAdjustMap.put("approverName", approverMap.get("name"));
				}else{
					employeeSalaryAdjustMap.put("approverAndTime", "");
				}
				employeeSalaryAdjustList.add(employeeSalaryAdjustMap);
			}
			returnMap.put("employeeSalaryAdjustList", employeeSalaryAdjustList);
			//调薪人数
			returnMap.put("salaryAdjustPeopleNumber", salaryAdjustPeopleNumber);
			//调薪总金额
			returnMap.put("salaryAdjustAmount", salaryAdjustAmount);
			//调社保人数
			returnMap.put("socialSecurityPeopleNumber", socialSecurityPeopleNumber);
			//调社保总金额
			returnMap.put("socialSecurityAmount", socialSecurityAmount);
			//调公积金人数
			returnMap.put("accumulationFundPeopleNumber", accumulationFundPeopleNumber);
			//调公积金总金额
			returnMap.put("accumulationFundAmount", accumulationFundAmount);
			//部门总人数
			returnMap.put("totalPeopleNumber", totalPeopleNumber);
			returnList.add(returnMap);
		}
		return returnList;
	}

	/**
	 * 保存或提交部门调薪
	 * @param token
	 * @param departmentSalaryAdjustMap
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse saveDepartmentSalaryAdjust(String token, Map<String, Object> departmentSalaryAdjustMap) throws Exception{
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
		Integer loginEmployeeId = erpUser.getUserId();
		String employeeName = erpUser.getEmployeeName();// 员工姓名
		String username = erpUser.getUsername();// 用户名
		List<Integer> roles = erpUser.getRoles();
		Boolean isGm = false;
		Boolean isFgm = false;
		if(roles.contains(8)){
			//总裁
			isGm = true;
		}else if(roles.contains(9)){
			//副总裁
			isFgm = true;
		}
		//通过权限工程查询总裁的员工ID
		List<Map<String,Object>> list = restTemplateUtils.findAllUserByRoleId(token, 8);
		Map<String,Object> map = list.get(0);
		Integer gmPersonId = (Integer) map.get("userId");//注意：userId是员工ID
		//年度
		String year = String.valueOf(departmentSalaryAdjustMap.get("year"));
		//部门ID
		Integer departmentId = Integer.valueOf(String.valueOf(departmentSalaryAdjustMap.get("departmentId")));
		//调薪计划
		String departmentSalaryAdjustPlan = String.valueOf(departmentSalaryAdjustMap.get("departmentSalaryAdjustPlan"));
		//调薪类型（1：调整薪资、2：社保/公积金基数）
		Integer type = Integer.valueOf(String.valueOf(departmentSalaryAdjustMap.get("type")));
		//生效日期
		String adjustDate = String.valueOf(departmentSalaryAdjustMap.get("adjustDate"));
		//审批人ID
		Integer approverId = Integer.valueOf(String.valueOf(departmentSalaryAdjustMap.get("approverId")));
		//提交状态0：未提交、1：已提交、
		Integer departmentSalaryAdjustStatus = Integer.valueOf(String.valueOf(departmentSalaryAdjustMap.get("departmentSalaryAdjustStatus")));
		//一级部门调薪批次ID
		Integer departmentSalaryAdjustId = null;
		if(departmentSalaryAdjustMap.get("departmentSalaryAdjustId") != null){
			departmentSalaryAdjustId = Integer.valueOf(String.valueOf(departmentSalaryAdjustMap.get("departmentSalaryAdjustId")));
		}
		//员工列表
		List<Map<String, Object>> employeeSalaryAdjustList = (List<Map<String, Object>>)departmentSalaryAdjustMap.get("employeeSalaryAdjustList");
		//新增一级部门调薪批次表
		DepartmentSalaryAdjust departmentSalaryAdjust = new DepartmentSalaryAdjust();
		departmentSalaryAdjust.setDepartmentId(departmentId);
		departmentSalaryAdjust.setYear(year);
		departmentSalaryAdjust.setStatus(departmentSalaryAdjustStatus);
		departmentSalaryAdjust.setType(type);
		departmentSalaryAdjust.setPlan(departmentSalaryAdjustPlan);
		departmentSalaryAdjust.setAdjustDate(ExDateUtils.convertToDate(adjustDate));
		departmentSalaryAdjust.setModifyPersonId(loginEmployeeId);
		departmentSalaryAdjust.setModifiedTime(ExDateUtils.getCurrentDateTime());
		if(departmentSalaryAdjustStatus == 1){
			departmentSalaryAdjust.setSubmitPersonId(loginEmployeeId);
			departmentSalaryAdjust.setSubmitTime(ExDateUtils.getCurrentDateTime());
		}
		if(departmentSalaryAdjustId == null){
			departmentSalaryAdjust.setCreateTime(ExDateUtils.getCurrentDateTime());
			departmentSalaryAdjustMapper.insertDepartmentSalaryAdjust(departmentSalaryAdjust);
		}else{
			departmentSalaryAdjust.setId(departmentSalaryAdjustId);
			departmentSalaryAdjustMapper.updateDepartmentSalaryAdjust(departmentSalaryAdjust);
		}
		for (Map<String, Object> employeeSalaryAdjust : employeeSalaryAdjustList){
			//数据类型（1：新增，2：修改， 3：删除）
			Integer dataType = Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("dataType")));
			if(dataType == 1 || dataType == 2){
				//员工ID
				Integer employeeId = Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("employeeId")));
				//调整原因
				String adjustReason = employeeSalaryAdjust.get("adjustReason") == null ? "" : String.valueOf(employeeSalaryAdjust.get("adjustReason"));
				//原来基本工资
				BigDecimal formerBaseWage = employeeSalaryAdjust.get("formerBaseWage") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerBaseWage")));
				//'原来岗位工资'
				BigDecimal formerPostWage = employeeSalaryAdjust.get("formerPostWage") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerPostWage")));
				//'原来月度绩效'
				BigDecimal formerPerformance = employeeSalaryAdjust.get("formerPerformance") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerPerformance")));
				//'原来项目补贴'
				BigDecimal formerAllowance =  employeeSalaryAdjust.get("formerAllowance") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerAllowance")));
				//'原来话费补助'
				BigDecimal formerTelFarePerquisite = employeeSalaryAdjust.get("formerTelFarePerquisite") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerTelFarePerquisite")));
				//'原来社保基数'
				BigDecimal formerSocialSecurityBase =  employeeSalaryAdjust.get("formerSocialSecurityBase") == null ? null :  new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerSocialSecurityBase")));
				//'原来公积金基数'
				BigDecimal formerAccumulationFundBase = employeeSalaryAdjust.get("formerAccumulationFundBase") == null ? null :  new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerAccumulationFundBase")));
				//调整后基本工资
				BigDecimal adjustBaseWage = null;
				//调整后岗位工资
				BigDecimal adjustPostWage = null;
				//调整后月度绩效
				BigDecimal adjustPerformance = null;
				//调整后月度项目津贴
				BigDecimal adjustAllowance = null;
				//调整后话费补助
				BigDecimal adjustTelFarePerquisite = null;
				//调整后社保基数
				BigDecimal adjustSocialSecurityBase = null;
				//调整后公积金基数
				BigDecimal adjustAccumulationFundBase = null;
				adjustBaseWage = employeeSalaryAdjust.get("adjustBaseWage") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustBaseWage")));
				adjustPostWage = employeeSalaryAdjust.get("adjustPostWage") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustPostWage")));
				adjustPerformance = employeeSalaryAdjust.get("adjustPerformance") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustPerformance")));
				adjustAllowance = employeeSalaryAdjust.get("adjustAllowance") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustAllowance")));
				adjustTelFarePerquisite = employeeSalaryAdjust.get("adjustTelFarePerquisite") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustTelFarePerquisite")));
				adjustSocialSecurityBase = employeeSalaryAdjust.get("adjustSocialSecurityBase") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustSocialSecurityBase")));
				adjustAccumulationFundBase = employeeSalaryAdjust.get("adjustAccumulationFundBase") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustAccumulationFundBase")));
				SalaryAdjustRecord salaryAdjustRecord = new SalaryAdjustRecord();
				salaryAdjustRecord.setEmployeeId(employeeId);
				salaryAdjustRecord.setAdjustTime(String.valueOf(employeeSalaryAdjust.get("adjustDate")));
				//原来基本工资
				salaryAdjustRecord.setFormerBaseWage(formerBaseWage == null ? null : AesUtils.encrypt(String.valueOf(formerBaseWage)));
				//原来岗位工资
				salaryAdjustRecord.setFormerPostWage(formerPostWage == null ? null : AesUtils.encrypt(String.valueOf(formerPostWage)));
				//原来阅读绩效
				salaryAdjustRecord.setFormerPerformance(formerPerformance == null ? null : AesUtils.encrypt(String.valueOf(formerPerformance)));
				//原来项目补贴
				salaryAdjustRecord.setFormerAllowance(formerAllowance == null ? null : AesUtils.encrypt(String.valueOf(formerAllowance)));
				//原来话费补助
				salaryAdjustRecord.setFormerTelFarePerquisite(formerTelFarePerquisite == null ? null : AesUtils.encrypt(String.valueOf(formerTelFarePerquisite)));
				//原来社保基数
				salaryAdjustRecord.setFormerSocialSecurityBase(formerSocialSecurityBase == null ? null : AesUtils.encrypt(String.valueOf(formerSocialSecurityBase)));
				//原来公积金基数
				salaryAdjustRecord.setFormerAccumulationFundBase(formerAccumulationFundBase == null ? null : AesUtils.encrypt(String.valueOf(formerAccumulationFundBase)));
				//调整基本工资
				salaryAdjustRecord.setAdjustBaseWage(adjustBaseWage == null ? null : AesUtils.encrypt(String.valueOf(adjustBaseWage)));
				//调整岗位工资
				salaryAdjustRecord.setAdjustPostWage(adjustPostWage == null ? null : AesUtils.encrypt(String.valueOf(adjustPostWage)));
				//调整月度绩效
				salaryAdjustRecord.setAdjustPerformance(adjustPerformance == null ? null : AesUtils.encrypt(String.valueOf(adjustPerformance)));
				//调整月度项目津贴
				salaryAdjustRecord.setAdjustAllowance(adjustAllowance == null ? null : AesUtils.encrypt(String.valueOf(adjustAllowance)));
				//调整话费补助
				salaryAdjustRecord.setAdjustTelFarePerquisite(adjustTelFarePerquisite == null ? null : AesUtils.encrypt(String.valueOf(adjustTelFarePerquisite)));
				//调整社保基数
				salaryAdjustRecord.setAdjustSocialSecurityBase(adjustSocialSecurityBase == null ? null : AesUtils.encrypt(String.valueOf(adjustSocialSecurityBase)));
				//调整公积金基数
				salaryAdjustRecord.setAdjustAccumulationFundBase(adjustAccumulationFundBase == null ? null : AesUtils.encrypt(String.valueOf(adjustAccumulationFundBase)));
				//调薪原因
				salaryAdjustRecord.setAdjustReason(adjustReason);
				//调整批次ID
				salaryAdjustRecord.setAdjustBatch(departmentSalaryAdjust.getId());
				Integer adjustStatus = 0;
				//调整状态（0：已提交(待副总裁审批)，1：审批通过，11，修订通过，2：暂存，4：驳回，5：待总裁审批, 51:修订待总裁审批）
				if(departmentSalaryAdjustStatus == 0){
					//提交状态0：未提交
					salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_SAVE);
				}else if(departmentSalaryAdjustStatus == 1){
					//提交状态1：已提交
					//查询该员工是否有其他未审批的调薪，有的话置为驳回，审批人为空
					List<Integer> salaryAdjustId = erpSalaryAdjustMapper.findSalaryAdjustReCordByEmployeeIdAndNotApprover(employeeId);
					if(salaryAdjustId != null && salaryAdjustId.size() > 0){
						erpSalaryAdjustMapper.updateRejectByIds(salaryAdjustId, null);
					}
					//判断提交人是否是总裁
					if(isGm){
						//1：审批通过
						salaryAdjustRecord.setApproverid(loginEmployeeId);
						salaryAdjustRecord.setApproverTime(ExDateUtils.getCurrentDateTime());
						salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_PASS);
						//审批通过判断生效日期是否小于等于当前日期，更新实时薪资表
						if(ExDateUtils.convertToDate(String.valueOf(employeeSalaryAdjust.get("adjustDate"))).compareTo(ExDateUtils.getToday()) <= 0){
							ErpBasePayroll erpBasePayroll = new ErpBasePayroll();
							erpBasePayroll.setErpEmployeeId(employeeId);//员工ID
							erpBasePayroll.setErpBaseWage(salaryAdjustRecord.getAdjustBaseWage());//基本工资
							erpBasePayroll.setErpPostWage(salaryAdjustRecord.getAdjustPostWage());//岗位工资
							erpBasePayroll.setErpPerformance(salaryAdjustRecord.getAdjustPerformance());//月度绩效
							erpBasePayroll.setErpAllowance(salaryAdjustRecord.getAdjustAllowance());//月度项目津贴
							erpBasePayroll.setErpTelFarePerquisite(salaryAdjustRecord.getAdjustTelFarePerquisite());//话费补助
							erpBasePayroll.setErpSocialSecurityBase(salaryAdjustRecord.getAdjustSocialSecurityBase());//社保基数
							erpBasePayroll.setErpAccumulationFundBase(salaryAdjustRecord.getAdjustAccumulationFundBase());//公积金基数
							/*
							 * 如果薪酬表中有员工，则更新；如果没有，则新增
							 * 薪酬数据允许重复导入，以最新的数据为准
							 */
							ErpBasePayroll validResult = this.erpBasePayrollMapper.findBasePayrollDetailByEmpId(employeeId);
							if(validResult==null) {
								this.erpBasePayrollMapper.insertBasePayroll(erpBasePayroll);
							}else {
								this.erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);
							}
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

						}
					}else if(isFgm){
						//判断提交人是否副总裁
						//5：待总裁审批
						salaryAdjustRecord.setApproverid(gmPersonId);
						salaryAdjustRecord.setApproverTime(ExDateUtils.getCurrentDateTime());
						salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_WAIT_BOSS);

					}else{
						//0：已提交(待副总裁审批)
						salaryAdjustRecord.setApproverid(approverId);
						salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_SUBMIT);
					}
				}
				if(dataType == 1){
					//新增
					erpSalaryAdjustMapper.insertSelective(salaryAdjustRecord);
				}else if(dataType == 2){
					//修改
					salaryAdjustRecord.setId(Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("salaryAdjustId"))));
					erpSalaryAdjustMapper.updateByPrimaryKeySelective(salaryAdjustRecord);
				}
				adjustStatus = salaryAdjustRecord.getAdjustStatus();
				//新增审批记录
				DepartmentSalaryAdjustApproveRecord salaryAdjustApproveRecord = new DepartmentSalaryAdjustApproveRecord();
				salaryAdjustApproveRecord.setAdjustStatus(adjustStatus);
				salaryAdjustApproveRecord.setApproverid(loginEmployeeId);
				salaryAdjustApproveRecord.setApproverTime(ExDateUtils.getCurrentDateTime());
				salaryAdjustApproveRecord.setSalaryAdjustRecordId(salaryAdjustRecord.getId());

				salaryAdjustApproveRecord.setFormerBaseWage(salaryAdjustRecord.getFormerBaseWage());
				salaryAdjustApproveRecord.setFormerPostWage(salaryAdjustRecord.getFormerPostWage());
				salaryAdjustApproveRecord.setFormerPerformance(salaryAdjustRecord.getFormerPerformance());
				salaryAdjustApproveRecord.setFormerAllowance(salaryAdjustRecord.getFormerAllowance());
				salaryAdjustApproveRecord.setFormerTelFarePerquisite(salaryAdjustRecord.getFormerTelFarePerquisite());
				salaryAdjustApproveRecord.setFormerSocialSecurityBase(salaryAdjustRecord.getFormerSocialSecurityBase());
				salaryAdjustApproveRecord.setFormerAccumulationFundBase(salaryAdjustRecord.getFormerAccumulationFundBase());

				salaryAdjustApproveRecord.setAdjustBaseWage(salaryAdjustRecord.getAdjustBaseWage());
				salaryAdjustApproveRecord.setAdjustPostWage(salaryAdjustRecord.getAdjustPostWage());
				salaryAdjustApproveRecord.setAdjustPerformance(salaryAdjustRecord.getAdjustPerformance());
				salaryAdjustApproveRecord.setAdjustAllowance(salaryAdjustRecord.getAdjustAllowance());
				salaryAdjustApproveRecord.setAdjustTelFarePerquisite(salaryAdjustRecord.getAdjustTelFarePerquisite());
				salaryAdjustApproveRecord.setAdjustSocialSecurityBase(salaryAdjustRecord.getAdjustSocialSecurityBase());
				salaryAdjustApproveRecord.setAdjustAccumulationFundBase(salaryAdjustRecord.getAdjustAccumulationFundBase());

				departmentSalaryAdjustApproveRecordMapper.insertSelective(salaryAdjustApproveRecord);
			}else if(dataType == 3){
				//删除
				erpSalaryAdjustMapper.deleteByPrimaryKey(Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("salaryAdjustId"))));
			}
		}
		return RestUtils.returnSuccess("保存成功！");

	}

	/**
	 * 查询人员调薪详情
	 * @param salaryAdjustId
	 * @param token
	 * @return
	 */
	public RestResponse findEmployeeSalaryAdjustInfo(Integer salaryAdjustId, Integer employeeId, String token) {
		logger.info("进入方法findEmployeeSalaryAdjustInfo");
		Map<String, Object> resultMap = new HashMap<>();
		try {
			//获取员工对应详细信息
			HttpHeaders requestHeadersForHr = new HttpHeaders();
			requestHeadersForHr.add("token", token);
			HttpEntity<String>	requestForHr = new HttpEntity<String>(null,requestHeadersForHr);
			String urlForHr = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeInfoMap";
			ResponseEntity<RestResponse> responseForHr = restTemplate.exchange(urlForHr,HttpMethod.GET,requestForHr,RestResponse.class);
			//跨工程调用响应失败
			if(200 != responseForHr.getStatusCodeValue() || !"200".equals(responseForHr.getBody().getStatus())) {
				logger.error("调用人力资源工程发生异常，响应失败！"+responseForHr);
				return RestUtils.returnFailure("调用人力资源工程发生异常，响应失败！");
			}
			Map<String, Object> employeeInfoMap = (Map<String, Object>) responseForHr.getBody().getData();
			List<SalaryAdjustRecord> salaryAdjustRecordList = new ArrayList<>();
			if(salaryAdjustId != null){
				SalaryAdjustRecord salaryAdjustRecord = erpSalaryAdjustMapper.selectByPrimaryKey(salaryAdjustId);
				//员工调薪ID
				resultMap.put("salaryAdjustId", salaryAdjustRecord.getId());
				//生效时间
				resultMap.put("adjustDate", salaryAdjustRecord.getAdjustTime());
				//调整原因
				resultMap.put("adjustReason", salaryAdjustRecord.getAdjustReason());
				//原来基本工资
				String formerBaseWage = salaryAdjustRecord.getFormerBaseWage() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getFormerBaseWage());
				resultMap.put("formerBaseWage", formerBaseWage);
				//原来岗位工资
				String formerPostWage = salaryAdjustRecord.getFormerPostWage() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getFormerPostWage());
				resultMap.put("formerPostWage", formerPostWage);
				//原来月度绩效
				String formerPerformance = salaryAdjustRecord.getFormerPerformance() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getFormerPerformance());
				resultMap.put("formerPerformance", formerPerformance);
				//原来项目补贴
				String formerAllowance = salaryAdjustRecord.getFormerAllowance() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getFormerAllowance());
				resultMap.put("formerAllowance", formerAllowance);
				//原来话费补助
				String formerTelFarePerquisite = salaryAdjustRecord.getFormerTelFarePerquisite() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getFormerTelFarePerquisite());
				resultMap.put("formerTelFarePerquisite", formerTelFarePerquisite);
				//原来社保基数
				String formerSocialSecurityBase = salaryAdjustRecord.getFormerSocialSecurityBase() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getFormerSocialSecurityBase());
				resultMap.put("formerSocialSecurityBase", formerSocialSecurityBase);
				//原来公积金基数
				String formerAccumulationFundBase = salaryAdjustRecord.getFormerAccumulationFundBase() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getFormerAccumulationFundBase());
				resultMap.put("formerAccumulationFundBase", formerAccumulationFundBase);
				//调薪前月度薪资
				resultMap.put("formerSalary", new BigDecimal(formerBaseWage).add(new BigDecimal(formerPostWage))
						.add(new BigDecimal(formerPerformance)).add(new BigDecimal(formerAllowance)).add(new BigDecimal(formerTelFarePerquisite)));

				//调薪后基本工资
				String adjustBaseWage = salaryAdjustRecord.getAdjustBaseWage() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getAdjustBaseWage());
				resultMap.put("adjustBaseWage", adjustBaseWage);
				//调薪后岗位工资
				String adjustPostWage = salaryAdjustRecord.getAdjustPostWage() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getAdjustPostWage());
				resultMap.put("adjustPostWage", adjustPostWage);
				//调薪后月度绩效
				String adjustPerformance = salaryAdjustRecord.getAdjustPerformance() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getAdjustPerformance());
				resultMap.put("adjustPerformance", adjustPerformance);
				//调薪后项目补贴
				String adjustAllowance = salaryAdjustRecord.getAdjustAllowance() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getAdjustAllowance());
				resultMap.put("adjustAllowance", adjustAllowance);
				//调薪后话费补助
				String adjustTelFarePerquisite = salaryAdjustRecord.getAdjustTelFarePerquisite() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getAdjustTelFarePerquisite());
				resultMap.put("adjustTelFarePerquisite", adjustTelFarePerquisite);
				//调薪后社保基数
				String adjustSocialSecurityBase = salaryAdjustRecord.getAdjustSocialSecurityBase() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getAdjustSocialSecurityBase());
				resultMap.put("adjustSocialSecurityBase", adjustSocialSecurityBase);
				//调薪后公积金基数
				String adjustAccumulationFundBase = salaryAdjustRecord.getAdjustAccumulationFundBase() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecord.getAdjustAccumulationFundBase());
				resultMap.put("adjustAccumulationFundBase", adjustAccumulationFundBase);
				//调薪后月度薪资
				resultMap.put("adjustSalary", new BigDecimal(adjustBaseWage).add(new BigDecimal(adjustPostWage))
						.add(new BigDecimal(adjustPerformance)).add(new BigDecimal(adjustAllowance)).add(new BigDecimal(adjustTelFarePerquisite)));
				//查询调薪记录
				salaryAdjustRecordList = erpSalaryAdjustMapper.findSalaryAdjustReCordById(salaryAdjustId);
			}else{
				//员工调薪ID
				resultMap.put("salaryAdjustId", null);
				//生效时间
				resultMap.put("adjustDate", null);
				//调整原因
				resultMap.put("adjustReason", null);

				ErpBasePayroll erpBasePayroll = this.erpBasePayrollMapper
						.findBasePayrollDetailByEmpId(employeeId);
				String erpBaseWage ="0.0";
				String erpPostWage ="0.0";
				String erpPerformance ="0.0";
				String erpAllowance ="0.0";
				String erpSocialSecurityBase ="0.0";
				String erpAccumulationFundBase ="0.0";
				String erpTelFarePerquisite ="0.0";

				if (erpBasePayroll != null) {
					// 将数据库中加密后的薪酬信息解密
					erpBaseWage = erpBasePayroll.getErpBaseWage() == null ? "0.0" : AesUtils.decrypt(erpBasePayroll.getErpBaseWage());// 基本工资
					erpPostWage = erpBasePayroll.getErpPostWage() == null ? "0.0" : AesUtils.decrypt(erpBasePayroll.getErpPostWage());// 岗位工资
					erpPerformance = erpBasePayroll.getErpPerformance() == null ? "0.0" : AesUtils.decrypt(erpBasePayroll.getErpPerformance());// 月度绩效
					erpAllowance = erpBasePayroll.getErpAllowance() == null ? "0.0" : AesUtils.decrypt(erpBasePayroll.getErpAllowance());// 月度项目津贴
					erpSocialSecurityBase = erpBasePayroll.getErpSocialSecurityBase() == null ? "0.0" : AesUtils.decrypt(erpBasePayroll.getErpSocialSecurityBase());// 社保基数
					erpAccumulationFundBase = erpBasePayroll.getErpAccumulationFundBase() == null ? "0.0" : AesUtils.decrypt(erpBasePayroll.getErpAccumulationFundBase());// 公积金基数
					erpTelFarePerquisite = erpBasePayroll.getErpTelFarePerquisite() == null ? "0.0" : AesUtils.decrypt(erpBasePayroll.getErpTelFarePerquisite());// 话费补助
				}

				//原来基本工资
				resultMap.put("formerBaseWage", erpBaseWage);
				//原来岗位工资
				resultMap.put("formerPostWage", erpPostWage);
				//原来月度绩效
				resultMap.put("formerPerformance", erpPerformance);
				//原来项目补贴
				resultMap.put("formerAllowance", erpAllowance);
				//原来话费补助
				resultMap.put("formerTelFarePerquisite", erpTelFarePerquisite);
				//原来社保基数
				resultMap.put("formerSocialSecurityBase", erpSocialSecurityBase);
				//原来公积金基数
				resultMap.put("formerAccumulationFundBase", erpAccumulationFundBase);
				//调薪前月度薪资
				resultMap.put("formerSalary", new BigDecimal(erpBaseWage).add(new BigDecimal(erpPostWage))
						.add(new BigDecimal(erpPerformance)).add(new BigDecimal(erpAllowance)).add(new BigDecimal(erpTelFarePerquisite)));

				//调薪后基本工资
				resultMap.put("adjustBaseWage", null);
				//调薪后岗位工资
				resultMap.put("adjustPostWage", null);
				//调薪后月度绩效
				resultMap.put("adjustPerformance", null);
				//调薪后项目补贴
				resultMap.put("adjustAllowance", null);
				//调薪后话费补助
				resultMap.put("adjustTelFarePerquisite", null);
				//调薪后社保基数
				resultMap.put("adjustSocialSecurityBase", null);
				//调薪后公积金基数
				resultMap.put("adjustAccumulationFundBase", null);
				//调薪后月度薪资
				resultMap.put("adjustSalary", null);
				//查询调薪记录
				salaryAdjustRecordList = erpSalaryAdjustMapper.findSalaryAdjustReCordByEmployeeId(employeeId);
			}

			List<Map<String, Object>> salaryAdjustRecordMapList = new ArrayList<>();
			for(SalaryAdjustRecord salaryAdjustRecordInfo : salaryAdjustRecordList){
				Map<String, Object> salaryAdjustRecordInfoMap = new HashMap<>();
				//时间
				salaryAdjustRecordInfoMap.put("adjustDate", salaryAdjustRecordInfo.getAdjustTime());
				//申请人
				Map<String, Object> submitPersonInfo = ((Map<String, Object>)(employeeInfoMap.get(String.valueOf(salaryAdjustRecordInfo.getSubmitPersonId()))));
				salaryAdjustRecordInfoMap.put("submitPersonName", submitPersonInfo == null ?  null : submitPersonInfo.get("name"));
				//审批人
				Map<String, Object> approverInfo = ((Map<String, Object>)(employeeInfoMap.get(String.valueOf(salaryAdjustRecordInfo.getApproverid()))));
				salaryAdjustRecordInfoMap.put("approverName",   approverInfo == null ? null :approverInfo .get("name"));
				//调薪原因
				salaryAdjustRecordInfoMap.put("adjustReason", salaryAdjustRecordInfo.getAdjustReason());
				//调薪前薪资
				//原来基本工资
				String formerBaseWageRecord = salaryAdjustRecordInfo.getFormerBaseWage() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getFormerBaseWage());
				//原来岗位工资
				String formerPostWageRecord = salaryAdjustRecordInfo.getFormerPostWage() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getFormerPostWage());
				//原来月度绩效
				String formerPerformanceRecord = salaryAdjustRecordInfo.getFormerPerformance() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getFormerPerformance());
				//原来项目补贴
				String formerAllowanceRecord = salaryAdjustRecordInfo.getFormerAllowance() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getFormerAllowance());
				//原来话费补助
				String formerTelFarePerquisiteRecord = salaryAdjustRecordInfo.getFormerTelFarePerquisite() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getFormerTelFarePerquisite());
				//原来社保基数
				String formerSocialSecurityBaseRecord = salaryAdjustRecordInfo.getFormerSocialSecurityBase() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getFormerSocialSecurityBase());
				salaryAdjustRecordInfoMap.put("formerSocialSecurityBase", formerSocialSecurityBaseRecord);
				//原来公积金基数
				String formerAccumulationFundBaseRecord = salaryAdjustRecordInfo.getFormerAccumulationFundBase() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getFormerAccumulationFundBase());
				salaryAdjustRecordInfoMap.put("formerAccumulationFundBase", formerAccumulationFundBaseRecord);
				//调薪前月度薪资
				salaryAdjustRecordInfoMap.put("formerSalary", new BigDecimal(formerBaseWageRecord).add(new BigDecimal(formerPostWageRecord))
						.add(new BigDecimal(formerPerformanceRecord)).add(new BigDecimal(formerAllowanceRecord)).add(new BigDecimal(formerTelFarePerquisiteRecord)));
				//调薪后薪资
				//调薪后基本工资
				String adjustBaseWageRecord = salaryAdjustRecordInfo.getAdjustBaseWage() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getAdjustBaseWage());
				//调薪后岗位工资
				String adjustPostWageRecord = salaryAdjustRecordInfo.getAdjustPostWage() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getAdjustPostWage());
				//调薪后月度绩效
				String adjustPerformanceRecord = salaryAdjustRecordInfo.getAdjustPerformance() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getAdjustPerformance());
				//调薪后项目补贴
				String adjustAllowanceRecord = salaryAdjustRecordInfo.getAdjustAllowance() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getAdjustAllowance());
				//调薪后话费补助
				String adjustTelFarePerquisiteRecord = salaryAdjustRecordInfo.getAdjustTelFarePerquisite() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getAdjustTelFarePerquisite());
				//调薪后社保基数
				String adjustSocialSecurityBaseRecord = salaryAdjustRecordInfo.getAdjustSocialSecurityBase() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getAdjustSocialSecurityBase());
				salaryAdjustRecordInfoMap.put("adjustSocialSecurityBase", adjustSocialSecurityBaseRecord);
				//调薪后公积金基数
				String adjustAccumulationFundBaseRecord = salaryAdjustRecordInfo.getAdjustAccumulationFundBase() == null ? "0.0" : AesUtils.decrypt(salaryAdjustRecordInfo.getAdjustAccumulationFundBase());
				salaryAdjustRecordInfoMap.put("adjustAccumulationFundBase", adjustAccumulationFundBaseRecord);
				//调薪后月度薪资
				salaryAdjustRecordInfoMap.put("adjustSalary", new BigDecimal(adjustBaseWageRecord).add(new BigDecimal(adjustPostWageRecord))
						.add(new BigDecimal(adjustPerformanceRecord)).add(new BigDecimal(adjustAllowanceRecord)).add(new BigDecimal(adjustTelFarePerquisiteRecord)));
				salaryAdjustRecordMapList.add(salaryAdjustRecordInfoMap);
			}
			resultMap.put("salaryAdjustRecordList", salaryAdjustRecordMapList);
		} catch (Exception e) {
			logger.error("方法findEmployeeSalaryAdjustInfo出现异常" + e.getMessage(),e);
		}
		return RestUtils.returnSuccess(resultMap);
	}

	/**
	 * 审批页面确认修改员工调整薪资
	 * @param token
	 * @param employeeSalaryAdjust
	 * @return
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse saveEmployeeSalaryAdjust(String token, Map<String, Object> employeeSalaryAdjust) throws Exception{
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
		Integer loginEmployeeId = erpUser.getUserId();
		String employeeName = erpUser.getEmployeeName();// 员工姓名
		String username = erpUser.getUsername();// 用户名
		List<Integer> roles = erpUser.getRoles();
		Boolean isGm = false;
		Boolean isFgm = false;
		if(roles.contains(8)){
			//总裁
			isGm = true;
		}else if(roles.contains(9)){
			//副总裁
			isFgm = true;
		}
		//通过权限工程查询总裁的员工ID
		List<Map<String,Object>> list = restTemplateUtils.findAllUserByRoleId(token, 8);
		Map<String,Object> map = list.get(0);
		Integer gmPersonId = (Integer) map.get("userId");//注意：userId是员工ID
		//数据类型（1：新增，2：修改）
		Integer dataType = Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("dataType")));
		//调薪类型（1：调整薪资、2：社保/公积金基数）
		Integer type = Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("type")));
		//调薪批次ID
		Integer departmentSalaryAdjustId = Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("departmentSalaryAdjustId")));
		//生效日期
		String adjustDate = String.valueOf(employeeSalaryAdjust.get("adjustDate"));
		if(dataType == 1 || dataType == 2){
			//员工ID
			Integer employeeId = Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("employeeId")));
			//调整原因
			String adjustReason = String.valueOf(employeeSalaryAdjust.get("adjustReason"));
			//原来基本工资
			BigDecimal formerBaseWage = employeeSalaryAdjust.get("formerBaseWage") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerBaseWage")));
			//'原来岗位工资'
			BigDecimal formerPostWage = employeeSalaryAdjust.get("formerPostWage") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerPostWage")));
			//'原来月度绩效'
			BigDecimal formerPerformance = employeeSalaryAdjust.get("formerPerformance") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerPerformance")));
			//'原来项目补贴'
			BigDecimal formerAllowance =  employeeSalaryAdjust.get("formerAllowance") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerAllowance")));
			//'原来话费补助'
			BigDecimal formerTelFarePerquisite = employeeSalaryAdjust.get("formerTelFarePerquisite") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerTelFarePerquisite")));
			//'原来社保基数'
			BigDecimal formerSocialSecurityBase =  employeeSalaryAdjust.get("formerSocialSecurityBase") == null ? null :  new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerSocialSecurityBase")));
			//'原来公积金基数'
			BigDecimal formerAccumulationFundBase = employeeSalaryAdjust.get("formerAccumulationFundBase") == null ? null :  new BigDecimal(String.valueOf(employeeSalaryAdjust.get("formerAccumulationFundBase")));
			//调整后基本工资
			BigDecimal adjustBaseWage = null;
			//调整后岗位工资
			BigDecimal adjustPostWage = null;
			//调整后月度绩效
			BigDecimal adjustPerformance = null;
			//调整后月度项目津贴
			BigDecimal adjustAllowance = null;
			//调整后话费补助
			BigDecimal adjustTelFarePerquisite = null;
			//调整后社保基数
			BigDecimal adjustSocialSecurityBase = null;
			//调整后公积金基数
			BigDecimal adjustAccumulationFundBase = null;

			adjustBaseWage = employeeSalaryAdjust.get("adjustBaseWage") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustBaseWage")));
			adjustPostWage = employeeSalaryAdjust.get("adjustPostWage") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustPostWage")));
			adjustPerformance = employeeSalaryAdjust.get("adjustPerformance") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustPerformance")));
			adjustAllowance = employeeSalaryAdjust.get("adjustAllowance") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustAllowance")));
			adjustTelFarePerquisite = employeeSalaryAdjust.get("adjustTelFarePerquisite") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustTelFarePerquisite")));
			adjustSocialSecurityBase = employeeSalaryAdjust.get("adjustSocialSecurityBase") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustSocialSecurityBase")));
			adjustAccumulationFundBase = employeeSalaryAdjust.get("adjustAccumulationFundBase") == null ? null : new BigDecimal(String.valueOf(employeeSalaryAdjust.get("adjustAccumulationFundBase")));


			SalaryAdjustRecord salaryAdjustRecord = new SalaryAdjustRecord();
			salaryAdjustRecord.setEmployeeId(employeeId);
			salaryAdjustRecord.setAdjustTime(adjustDate);
			//原来基本工资
			salaryAdjustRecord.setFormerBaseWage(formerBaseWage == null ? null : AesUtils.encrypt(String.valueOf(formerBaseWage)));
			//原来岗位工资
			salaryAdjustRecord.setFormerPostWage(formerPostWage == null ? null : AesUtils.encrypt(String.valueOf(formerPostWage)));
			//原来阅读绩效
			salaryAdjustRecord.setFormerPerformance(formerPerformance == null ? null : AesUtils.encrypt(String.valueOf(formerPerformance)));
			//原来项目补贴
			salaryAdjustRecord.setFormerAllowance(formerAllowance == null ? null : AesUtils.encrypt(String.valueOf(formerAllowance)));
			//原来话费补助
			salaryAdjustRecord.setFormerTelFarePerquisite(formerTelFarePerquisite == null ? null : AesUtils.encrypt(String.valueOf(formerTelFarePerquisite)));
			//原来社保基数
			salaryAdjustRecord.setFormerSocialSecurityBase(formerSocialSecurityBase == null ? null : AesUtils.encrypt(String.valueOf(formerSocialSecurityBase)));
			//原来公积金基数
			salaryAdjustRecord.setFormerAccumulationFundBase(formerAccumulationFundBase == null ? null : AesUtils.encrypt(String.valueOf(formerAccumulationFundBase)));
			//调整基本工资
			salaryAdjustRecord.setAdjustBaseWage(adjustBaseWage == null ? null : AesUtils.encrypt(String.valueOf(adjustBaseWage)));
			//调整岗位工资
			salaryAdjustRecord.setAdjustPostWage(adjustPostWage == null ? null : AesUtils.encrypt(String.valueOf(adjustPostWage)));
			//调整月度绩效
			salaryAdjustRecord.setAdjustPerformance(adjustPerformance == null ? null : AesUtils.encrypt(String.valueOf(adjustPerformance)));
			//调整月度项目津贴
			salaryAdjustRecord.setAdjustAllowance(adjustAllowance == null ? null : AesUtils.encrypt(String.valueOf(adjustAllowance)));
			//调整话费补助
			salaryAdjustRecord.setAdjustTelFarePerquisite(adjustTelFarePerquisite == null ? null : AesUtils.encrypt(String.valueOf(adjustTelFarePerquisite)));
			//调整社保基数
			salaryAdjustRecord.setAdjustSocialSecurityBase(adjustSocialSecurityBase == null ? null : AesUtils.encrypt(String.valueOf(adjustSocialSecurityBase)));
			//调整公积金基数
			salaryAdjustRecord.setAdjustAccumulationFundBase(adjustAccumulationFundBase == null ? null : AesUtils.encrypt(String.valueOf(adjustAccumulationFundBase)));
			//调薪原因
			salaryAdjustRecord.setAdjustReason(String.valueOf(adjustReason));
			//调整批次ID
			salaryAdjustRecord.setAdjustBatch(departmentSalaryAdjustId);

			if(employeeSalaryAdjust.get("salaryAdjustId") == null){
				//新增
				//查询该员工是否有其他未审批的调薪，有的话置为驳回，审批人为空
				List<Integer> salaryAdjustId = erpSalaryAdjustMapper.findSalaryAdjustReCordByEmployeeIdAndNotApprover(employeeId);
				if(salaryAdjustId != null && salaryAdjustId.size() > 0){
					erpSalaryAdjustMapper.updateRejectByIds(salaryAdjustId, null);
				}
				//调整状态（0：已提交(待副总裁审批)，1：审批通过，2：暂存，3，修订通过，4：驳回，5：待总裁审批）
				//提交状态1：已提交
				//判断提交人是否是总裁
				if(isGm){
					//1：审批通过
					salaryAdjustRecord.setApproverid(loginEmployeeId);
					salaryAdjustRecord.setApproverTime(ExDateUtils.getCurrentDateTime());
					salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_PASS);

					//审批通过判断生效日期是否小于等于当前日期，更新实时薪资表
					if(ExDateUtils.convertToDate(salaryAdjustRecord.getAdjustTime()).compareTo(ExDateUtils.getToday()) <= 0){
						ErpBasePayroll erpBasePayroll = new ErpBasePayroll();
						erpBasePayroll.setErpEmployeeId(salaryAdjustRecord.getEmployeeId());//员工ID
						erpBasePayroll.setErpBaseWage(salaryAdjustRecord.getAdjustBaseWage());//基本工资
						erpBasePayroll.setErpPostWage(salaryAdjustRecord.getAdjustPostWage());//岗位工资
						erpBasePayroll.setErpPerformance(salaryAdjustRecord.getAdjustPerformance());//月度绩效
						erpBasePayroll.setErpAllowance(salaryAdjustRecord.getAdjustAllowance());//月度项目津贴
						erpBasePayroll.setErpTelFarePerquisite(salaryAdjustRecord.getAdjustTelFarePerquisite());//话费补助
						erpBasePayroll.setErpSocialSecurityBase(salaryAdjustRecord.getAdjustSocialSecurityBase());//社保基数
						erpBasePayroll.setErpAccumulationFundBase(salaryAdjustRecord.getAdjustAccumulationFundBase());//公积金基数
						/*
						 * 如果薪酬表中有员工，则更新；如果没有，则新增
						 * 薪酬数据允许重复导入，以最新的数据为准
						 */
						ErpBasePayroll validResult = this.erpBasePayrollMapper.findBasePayrollDetailByEmpId(salaryAdjustRecord.getEmployeeId());
						if(validResult==null) {
							this.erpBasePayrollMapper.insertBasePayroll(erpBasePayroll);
						}else {
							this.erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);
						}
						/*
						 * 将该员工的修改信息加入日志中
						 */
						ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
						basePayrollUpdateRecord.setEmployee(restTemplateUtils.findEmpNameByEmployeeId(token,salaryAdjustRecord.getEmployeeId()));// 被修改的员工
						basePayrollUpdateRecord.setEmployeeId(salaryAdjustRecord.getEmployeeId());
						basePayrollUpdateRecord
								.setProcessor(employeeName == null || "".equals(employeeName) ? username : employeeName);// 修改人
						basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
						basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
						this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);
					}

				}else if(isFgm){
					//判断提交人是否副总裁
					//5：待总裁审批
					salaryAdjustRecord.setApproverid(gmPersonId);
					salaryAdjustRecord.setApproverTime(ExDateUtils.getCurrentDateTime());
					salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_WAIT_BOSS);
				}else{
					return RestUtils.returnFailure("权限不足！");
				}
				erpSalaryAdjustMapper.insertSelective(salaryAdjustRecord);

				//新增审批记录
				DepartmentSalaryAdjustApproveRecord salaryAdjustApproveRecord = new DepartmentSalaryAdjustApproveRecord();
				salaryAdjustApproveRecord.setAdjustStatus(salaryAdjustRecord.getAdjustStatus());
				salaryAdjustApproveRecord.setApproverid(loginEmployeeId);
				salaryAdjustApproveRecord.setApproverTime(ExDateUtils.getCurrentDateTime());
				salaryAdjustApproveRecord.setSalaryAdjustRecordId(salaryAdjustRecord.getId());

				salaryAdjustApproveRecord.setFormerBaseWage(salaryAdjustRecord.getFormerBaseWage());
				salaryAdjustApproveRecord.setFormerPostWage(salaryAdjustRecord.getFormerPostWage());
				salaryAdjustApproveRecord.setFormerPerformance(salaryAdjustRecord.getFormerPerformance());
				salaryAdjustApproveRecord.setFormerAllowance(salaryAdjustRecord.getFormerAllowance());
				salaryAdjustApproveRecord.setFormerTelFarePerquisite(salaryAdjustRecord.getFormerTelFarePerquisite());
				salaryAdjustApproveRecord.setFormerSocialSecurityBase(salaryAdjustRecord.getFormerSocialSecurityBase());
				salaryAdjustApproveRecord.setFormerAccumulationFundBase(salaryAdjustRecord.getFormerAccumulationFundBase());

				salaryAdjustApproveRecord.setAdjustBaseWage(salaryAdjustRecord.getAdjustBaseWage());
				salaryAdjustApproveRecord.setAdjustPostWage(salaryAdjustRecord.getAdjustPostWage());
				salaryAdjustApproveRecord.setAdjustPerformance(salaryAdjustRecord.getAdjustPerformance());
				salaryAdjustApproveRecord.setAdjustAllowance(salaryAdjustRecord.getAdjustAllowance());
				salaryAdjustApproveRecord.setAdjustTelFarePerquisite(salaryAdjustRecord.getAdjustTelFarePerquisite());
				salaryAdjustApproveRecord.setAdjustSocialSecurityBase(salaryAdjustRecord.getAdjustSocialSecurityBase());
				salaryAdjustApproveRecord.setAdjustAccumulationFundBase(salaryAdjustRecord.getAdjustAccumulationFundBase());

				departmentSalaryAdjustApproveRecordMapper.insertSelective(salaryAdjustApproveRecord);


			}else if(employeeSalaryAdjust.get("salaryAdjustId") != null){
				//修改

				//员工调薪ID
				Integer salaryAdjustId = Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("salaryAdjustId")));
				//查询原来的调薪后信息
				SalaryAdjustRecord salaryAdjustRecordInfo = erpSalaryAdjustMapper.selectByPrimaryKey(salaryAdjustId);
				Boolean isModify = false;
				if(type == 1){
					if(salaryAdjustRecordInfo.getAdjustBaseWage().equals(salaryAdjustRecord.getAdjustBaseWage())
					&&salaryAdjustRecordInfo.getAdjustPostWage().equals(salaryAdjustRecord.getAdjustPostWage())
					&&salaryAdjustRecordInfo.getAdjustPerformance().equals(salaryAdjustRecord.getAdjustPerformance())
					&&salaryAdjustRecordInfo.getAdjustAllowance().equals(salaryAdjustRecord.getAdjustAllowance())
					&&salaryAdjustRecordInfo.getAdjustTelFarePerquisite().equals(salaryAdjustRecord.getAdjustTelFarePerquisite())
					&&salaryAdjustRecordInfo.getAdjustTime().equals(salaryAdjustRecord.getAdjustTime())){
						isModify = false;
					}else{
						isModify = true;
					}
				}else if(type == 2){
					if(salaryAdjustRecordInfo.getAdjustSocialSecurityBase().equals(salaryAdjustRecord.getAdjustSocialSecurityBase())
					&&salaryAdjustRecordInfo.getAdjustAccumulationFundBase().equals(salaryAdjustRecord.getAdjustAccumulationFundBase())
					&&salaryAdjustRecordInfo.getAdjustTime().equals(salaryAdjustRecord.getAdjustTime())){
						isModify = false;
					}else{
						isModify = true;
					}
				}
				salaryAdjustRecord.setId(salaryAdjustId);
				salaryAdjustRecord.setModified(isModify);
				erpSalaryAdjustMapper.updateByPrimaryKeySelective(salaryAdjustRecord);
			}

		}
		return RestUtils.returnSuccess("保存成功！");
	}

	/**
	 * 审批员工调整薪资
	 * @param token
	 * @param employeeSalaryAdjust
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse approveEmployeeSalaryAdjust(String token, Map<String, Object> employeeSalaryAdjust) throws Exception{
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
		Integer loginEmployeeId = erpUser.getUserId();
		String employeeName = erpUser.getEmployeeName();// 员工姓名
		String username = erpUser.getUsername();// 用户名
		List<Integer> roles = erpUser.getRoles();
		Boolean isGm = false;
		Boolean isFgm = false;
		if(roles.contains(8)){
			//总裁
			isGm = true;
		}else if(roles.contains(9)){
			//副总裁
			isFgm = true;
		}
		//通过权限工程查询总裁的员工ID
		List<Map<String,Object>> list = restTemplateUtils.findAllUserByRoleId(token, 8);
		Map<String,Object> map = list.get(0);
		Integer gmPersonId = (Integer) map.get("userId");//注意：userId是员工ID
		//审批类型（1：批准，2：驳回）
		Integer approveType = Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("approveType")));
		//员工调薪ID
		Integer salaryAdjustId = Integer.valueOf(String.valueOf(employeeSalaryAdjust.get("salaryAdjustId")));
		//是否修改过
		SalaryAdjustRecord salaryAdjustRecordInfo = erpSalaryAdjustMapper.selectByPrimaryKey(salaryAdjustId);
		Boolean isModifyed = salaryAdjustRecordInfo.getModified() == null ? false : salaryAdjustRecordInfo.getModified();
		SalaryAdjustRecord salaryAdjustRecord = new SalaryAdjustRecord();
		salaryAdjustRecord.setId(salaryAdjustId);
		//审批时间
		salaryAdjustRecord.setApproverTime(ExDateUtils.getCurrentDateTime());
		Integer adjustStatus = 0;
		if(approveType == 1){
			//批准
			if(isGm){
				//总裁
				//审批通过
				if(isModifyed){
					//修订通过
					salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_MODIFY_PASS);
				}else{
					//审批通过
					salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_PASS);
				}
				//审批人总裁
				salaryAdjustRecord.setApproverid(loginEmployeeId);

				//审批通过判断生效日期是否小于等于当前日期，更新实时薪资表
				if(ExDateUtils.convertToDate(salaryAdjustRecordInfo.getAdjustTime()).compareTo(ExDateUtils.getToday()) <= 0){
					ErpBasePayroll erpBasePayroll = new ErpBasePayroll();
					erpBasePayroll.setErpEmployeeId(salaryAdjustRecordInfo.getEmployeeId());//员工ID
					erpBasePayroll.setErpBaseWage(salaryAdjustRecordInfo.getAdjustBaseWage());//基本工资
					erpBasePayroll.setErpPostWage(salaryAdjustRecordInfo.getAdjustPostWage());//岗位工资
					erpBasePayroll.setErpPerformance(salaryAdjustRecordInfo.getAdjustPerformance());//月度绩效
					erpBasePayroll.setErpAllowance(salaryAdjustRecordInfo.getAdjustAllowance());//月度项目津贴
					erpBasePayroll.setErpTelFarePerquisite(salaryAdjustRecordInfo.getAdjustTelFarePerquisite());//话费补助
					erpBasePayroll.setErpSocialSecurityBase(salaryAdjustRecordInfo.getAdjustSocialSecurityBase());//社保基数
					erpBasePayroll.setErpAccumulationFundBase(salaryAdjustRecordInfo.getAdjustAccumulationFundBase());//公积金基数
					/*
					 * 如果薪酬表中有员工，则更新；如果没有，则新增
					 * 薪酬数据允许重复导入，以最新的数据为准
					 */
					ErpBasePayroll validResult = this.erpBasePayrollMapper.findBasePayrollDetailByEmpId(salaryAdjustRecordInfo.getEmployeeId());
					if(validResult==null) {
						this.erpBasePayrollMapper.insertBasePayroll(erpBasePayroll);
					}else {
						this.erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);
					}
					/*
					 * 将该员工的修改信息加入日志中
					 */
					ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
					basePayrollUpdateRecord.setEmployee(restTemplateUtils.findEmpNameByEmployeeId(token,salaryAdjustRecordInfo.getEmployeeId()));// 被修改的员工
					basePayrollUpdateRecord.setEmployeeId(salaryAdjustRecordInfo.getEmployeeId());
					basePayrollUpdateRecord
							.setProcessor(employeeName == null || "".equals(employeeName) ? username : employeeName);// 修改人
					basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
					basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
					this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);
				}

			}else if(isFgm){
				//副总裁
				if(isModifyed){
					//修订待总裁审批
					salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_MODIFY_WAIT_BOSS);
				}else{
					//待总裁审批
					salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_WAIT_BOSS);
				}
				//下任审批人总裁
				salaryAdjustRecord.setApproverid(gmPersonId);
			}else{
				return RestUtils.returnFailure("权限不足！");
			}
		}else if(approveType == 2){
			//驳回
			salaryAdjustRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_REJECT);
			//审批人当前登录人
			salaryAdjustRecord.setApproverid(loginEmployeeId);
		}
		erpSalaryAdjustMapper.updateByPrimaryKeySelective(salaryAdjustRecord);
		adjustStatus = salaryAdjustRecord.getAdjustStatus();
		//新增审批记录
		DepartmentSalaryAdjustApproveRecord salaryAdjustApproveRecord = new DepartmentSalaryAdjustApproveRecord();
		salaryAdjustApproveRecord.setAdjustStatus(adjustStatus);
		salaryAdjustApproveRecord.setApproverid(loginEmployeeId);
		salaryAdjustApproveRecord.setApproverTime(ExDateUtils.getCurrentDateTime());
		salaryAdjustApproveRecord.setSalaryAdjustRecordId(salaryAdjustRecord.getId());

		salaryAdjustApproveRecord.setFormerBaseWage(salaryAdjustRecord.getFormerBaseWage());
		salaryAdjustApproveRecord.setFormerPostWage(salaryAdjustRecord.getFormerPostWage());
		salaryAdjustApproveRecord.setFormerPerformance(salaryAdjustRecord.getFormerPerformance());
		salaryAdjustApproveRecord.setFormerAllowance(salaryAdjustRecord.getFormerAllowance());
		salaryAdjustApproveRecord.setFormerTelFarePerquisite(salaryAdjustRecord.getFormerTelFarePerquisite());
		salaryAdjustApproveRecord.setFormerSocialSecurityBase(salaryAdjustRecord.getFormerSocialSecurityBase());
		salaryAdjustApproveRecord.setFormerAccumulationFundBase(salaryAdjustRecord.getFormerAccumulationFundBase());

		salaryAdjustApproveRecord.setAdjustBaseWage(salaryAdjustRecord.getAdjustBaseWage());
		salaryAdjustApproveRecord.setAdjustPostWage(salaryAdjustRecord.getAdjustPostWage());
		salaryAdjustApproveRecord.setAdjustPerformance(salaryAdjustRecord.getAdjustPerformance());
		salaryAdjustApproveRecord.setAdjustAllowance(salaryAdjustRecord.getAdjustAllowance());
		salaryAdjustApproveRecord.setAdjustTelFarePerquisite(salaryAdjustRecord.getAdjustTelFarePerquisite());
		salaryAdjustApproveRecord.setAdjustSocialSecurityBase(salaryAdjustRecord.getAdjustSocialSecurityBase());
		salaryAdjustApproveRecord.setAdjustAccumulationFundBase(salaryAdjustRecord.getAdjustAccumulationFundBase());

		departmentSalaryAdjustApproveRecordMapper.insertSelective(salaryAdjustApproveRecord);
		return RestUtils.returnSuccess("审批成功！");
	}

	/**
	 * 确认并导出调薪列表
	 * @param token
	 * @param salaryAdjustMap
	 * @return
	 */
	public RestResponse confirmAndExportSalaryAdjust(String token, Map<String, Object> salaryAdjustMap) throws Exception{
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
		List<Integer> roles = erpUser.getRoles();;
		if(!roles.contains(8)){
			//不是总裁不能导出
			HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
			//返回状态为2 权限不足
			response.addHeader("status", "2");
			return RestUtils.returnSuccess("权限不足，不能导出！");
		}
		Integer loginEmployeeId = erpUser.getUserId();
		HttpHeaders requestHeadersForHr = new HttpHeaders();
		requestHeadersForHr.add("token", token);
		HttpEntity<String>	requestForHr = new HttpEntity<String>(null,requestHeadersForHr);
		//获取员工对应详细信息
		String urlForHr = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeInfoMap";
		ResponseEntity<RestResponse> responseForHr = restTemplate.exchange(urlForHr,HttpMethod.GET,requestForHr,RestResponse.class);
		//跨工程调用响应失败
		if(200 != responseForHr.getStatusCodeValue() || !"200".equals(responseForHr.getBody().getStatus())) {
			logger.error("调用人力资源工程发生异常，响应失败！"+responseForHr);
			return RestUtils.returnFailure("调用人力资源工程发生异常，响应失败！");
		}
		Map<String, Object> employeeInfoMap = (Map<String, Object>) responseForHr.getBody().getData();

		//0:确认并导出 1：导出
		Integer type = Integer.valueOf(String.valueOf(salaryAdjustMap.get("type")));
		Integer departmentSalaryAdjustId = Integer.valueOf(String.valueOf(salaryAdjustMap.get("departmentSalaryAdjustId")));
		//是否确认导出
		Boolean isConfirm = Boolean.valueOf(String.valueOf(salaryAdjustMap.get("isConfirm")));
		//查询该批次下未审批的调薪列表
		Map<String, Object> employeeWaitParamMap = new HashMap<>();
		employeeWaitParamMap.put("departmentSalaryAdjustId", departmentSalaryAdjustId);
		List<Integer> statusWaitList = new ArrayList<>();
		statusWaitList.add(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_SUBMIT);
		statusWaitList.add(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_SAVE);
		statusWaitList.add(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_WAIT_BOSS);
		statusWaitList.add(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_MODIFY_WAIT_BOSS);
		employeeWaitParamMap.put("statusList", statusWaitList);
		employeeWaitParamMap.put("isExport", true);
		List<Map<String, Object>> salaryAdjustWaitList = erpSalaryAdjustMapper.findEmployeeSalaryAdjustListByparams(employeeWaitParamMap);
		if(isConfirm == null || !isConfirm){
			if(salaryAdjustWaitList != null && salaryAdjustWaitList.size() > 0){
				HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
				//返回状态为3 还有尚未审批的调薪申请
				response.addHeader("status", "3");
				return RestUtils.returnSuccess("还有尚未审批的调薪申请！");
			}
		}else{
			if(salaryAdjustWaitList != null && salaryAdjustWaitList.size() > 0){
				List<Integer> salaryAdjustWaitIdList = new ArrayList<>();
				for(Map<String, Object> salaryAdjustWait : salaryAdjustWaitList){
					salaryAdjustWaitIdList.add(Integer.valueOf(String.valueOf(salaryAdjustWait.get("salaryAdjustId"))));

					//新增审批记录
					DepartmentSalaryAdjustApproveRecord salaryAdjustApproveRecord = new DepartmentSalaryAdjustApproveRecord();
					salaryAdjustApproveRecord.setAdjustStatus(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_REJECT);
					salaryAdjustApproveRecord.setApproverid(loginEmployeeId);
					salaryAdjustApproveRecord.setApproverTime(ExDateUtils.getCurrentDateTime());
					salaryAdjustApproveRecord.setSalaryAdjustRecordId(Integer.valueOf(String.valueOf(salaryAdjustWait.get("salaryAdjustId"))));

					salaryAdjustApproveRecord.setFormerBaseWage(String.valueOf(salaryAdjustWait.get("formerBaseWage")));
					salaryAdjustApproveRecord.setFormerPostWage(String.valueOf(salaryAdjustWait.get("formerPostWage")));
					salaryAdjustApproveRecord.setFormerPerformance(String.valueOf(salaryAdjustWait.get("formerPerformance")));
					salaryAdjustApproveRecord.setFormerAllowance(String.valueOf(salaryAdjustWait.get("formerAllowance")));
					salaryAdjustApproveRecord.setFormerTelFarePerquisite(String.valueOf(salaryAdjustWait.get("formerTelFarePerquisite")));
					salaryAdjustApproveRecord.setFormerSocialSecurityBase(String.valueOf(salaryAdjustWait.get("formerSocialSecurityBase")));
					salaryAdjustApproveRecord.setFormerAccumulationFundBase(String.valueOf(salaryAdjustWait.get("formerAccumulationFundBase")));

					salaryAdjustApproveRecord.setAdjustBaseWage(String.valueOf(salaryAdjustWait.get("adjustBaseWage")));
					salaryAdjustApproveRecord.setAdjustPostWage(String.valueOf(salaryAdjustWait.get("adjustPostWage")));
					salaryAdjustApproveRecord.setAdjustPerformance(String.valueOf(salaryAdjustWait.get("adjustPerformance")));
					salaryAdjustApproveRecord.setAdjustAllowance(String.valueOf(salaryAdjustWait.get("adjustAllowance")));
					salaryAdjustApproveRecord.setAdjustTelFarePerquisite(String.valueOf(salaryAdjustWait.get("adjustTelFarePerquisite")));
					salaryAdjustApproveRecord.setAdjustSocialSecurityBase(String.valueOf(salaryAdjustWait.get("adjustSocialSecurityBase")));
					salaryAdjustApproveRecord.setAdjustAccumulationFundBase(String.valueOf(salaryAdjustWait.get("adjustAccumulationFundBase")));

					departmentSalaryAdjustApproveRecordMapper.insertSelective(salaryAdjustApproveRecord);

				}
				//置为驳回
				erpSalaryAdjustMapper.updateRejectByIds(salaryAdjustWaitIdList, loginEmployeeId);
			}
		}
		if(type == 0){
			DepartmentSalaryAdjust departmentSalaryAdjust = new DepartmentSalaryAdjust();
			departmentSalaryAdjust.setId(departmentSalaryAdjustId);
			//已确认
			departmentSalaryAdjust.setStatus(DicConstants.DEPARTMENT_SALARY_ADJUST_STATUS_CONFIRM);
			departmentSalaryAdjust.setConfirmPersonId(loginEmployeeId);
			departmentSalaryAdjust.setConfirmTime(ExDateUtils.getCurrentDateTime());
			departmentSalaryAdjustMapper.updateDepartmentSalaryAdjust(departmentSalaryAdjust);
		}
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("departmentSalaryAdjustId", departmentSalaryAdjustId);
		List<DepartmentSalaryAdjustVO> departmentSalaryAdjustList = departmentSalaryAdjustMapper.findDepartmentSalaryAdjustList(paramMap);
		DepartmentSalaryAdjustVO departmentSalaryAdjustVO = departmentSalaryAdjustList.get(0);
		Integer departmentSalaryAdjustType = departmentSalaryAdjustVO.getType();
		//查询该批次下调薪列表
		Map<String, Object> employeeParamMap = new HashMap<>();
		employeeParamMap.put("departmentSalaryAdjustId", departmentSalaryAdjustId);
		List<Integer> statusList = new ArrayList<>();
		statusList.add(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_PASS);
		statusList.add(DicConstants.EMPLOYEE_SALARY_ADJUST_STATUS_MODIFY_PASS);
		employeeParamMap.put("statusList", statusList);
		employeeParamMap.put("isExport", true);
		List<Map<String, Object>> salaryAdjustList = erpSalaryAdjustMapper.findEmployeeSalaryAdjustListByparams(employeeParamMap);
		List<Map<String, Object>> returnList = new ArrayList<>();
		for(Map<String, Object> salaryAdjustInfoMap : salaryAdjustList){
			Map<String, Object> returnMap = new HashMap<>();
			//填写员工信息
			Map<String, Object> employeeInfo = (Map)employeeInfoMap.get(String.valueOf(salaryAdjustInfoMap.get("employeeId")));
			returnMap.put("firstDepartmentName", employeeInfo.get("firstDepartmentName"));
			returnMap.put("secondDepartmentName", employeeInfo.get("secondDepartmentName"));
			returnMap.put("name", employeeInfo.get("name"));
			returnMap.put("sex", employeeInfo.get("sex"));
			returnMap.put("idCardNumber", employeeInfo.get("idCardNumber"));
			returnMap.put("position", employeeInfo.get("position"));
			returnMap.put("rank", employeeInfo.get("rank"));
			returnMap.put("adjustDate", ExDateUtils.dateToString(departmentSalaryAdjustVO.getAdjustDate(), "yyyy-MM-dd"));
			returnMap.put("adjustReason", salaryAdjustInfoMap.get("adjustReason"));

			if(departmentSalaryAdjustType == 1){
				//原来基本工资加密
				String formerBaseWage = String.valueOf(salaryAdjustInfoMap.get("formerBaseWage"));
				//原来岗位工资加密
				String formerPostWage = String.valueOf(salaryAdjustInfoMap.get("formerPostWage"));
				//'原来月度绩效'加密
				String formerPerformance = String.valueOf(salaryAdjustInfoMap.get("formerPerformance"));
				//'原来项目补贴'加密
				String formerAllowance = String.valueOf(salaryAdjustInfoMap.get("formerAllowance"));
				//'原来话费补助'加密
				String formerTelFarePerquisite = String.valueOf(salaryAdjustInfoMap.get("formerTelFarePerquisite"));

				//原来基本工资解密
				BigDecimal formerBaseWageBigDecimal = salaryAdjustInfoMap.get("formerBaseWage") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerBaseWage));
				//原来岗位工资解密
				BigDecimal formerPostWageBigDecimal = salaryAdjustInfoMap.get("formerPostWage") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerPostWage));
				//原来月度绩效解密
				BigDecimal formerPerformanceBigDecimal = salaryAdjustInfoMap.get("formerPerformance") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerPerformance));
				//原来项目补贴解密
				BigDecimal formerAllowanceBigDecimal = salaryAdjustInfoMap.get("formerAllowance") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerAllowance));
				//原来话费补助解密
				BigDecimal formerTelFarePerquisiteBigDecimal = salaryAdjustInfoMap.get("formerTelFarePerquisite") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerTelFarePerquisite));
				//当前薪资
				BigDecimal formerSalary = formerBaseWageBigDecimal.add(formerPostWageBigDecimal).add(formerPerformanceBigDecimal).add(formerAllowanceBigDecimal).add(formerTelFarePerquisiteBigDecimal);
				returnMap.put("formerSalary", formerSalary);

				//调整后基本工资加密
				String adjustBaseWage = String.valueOf(salaryAdjustInfoMap.get("adjustBaseWage"));
				//调整后岗位工资加密
				String adjustPostWage = String.valueOf(salaryAdjustInfoMap.get("adjustPostWage"));
				//调整后月度绩效'加密
				String adjustPerformance = String.valueOf(salaryAdjustInfoMap.get("adjustPerformance"));
				//调整后项目补贴'加密
				String adjustAllowance = String.valueOf(salaryAdjustInfoMap.get("adjustAllowance"));
				//'调整后话费补助'加密
				String adjustTelFarePerquisite = String.valueOf(salaryAdjustInfoMap.get("adjustTelFarePerquisite"));

				//调整后基本工资解密
				BigDecimal adjustBaseWageBigDecimal = salaryAdjustInfoMap.get("adjustBaseWage") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustBaseWage));
				returnMap.put("adjustBaseWage", adjustBaseWageBigDecimal);
				//调整后岗位工资解密
				BigDecimal adjustPostWageBigDecimal = salaryAdjustInfoMap.get("adjustPostWage") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustPostWage));
				returnMap.put("adjustPostWage", adjustPostWageBigDecimal);
				//调整后月度绩效解密
				BigDecimal adjustPerformanceBigDecimal = salaryAdjustInfoMap.get("adjustPerformance") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustPerformance));
				returnMap.put("adjustPerformance", adjustPerformanceBigDecimal);
				//调整后项目补贴解密
				BigDecimal adjustAllowanceBigDecimal = salaryAdjustInfoMap.get("adjustAllowance") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustAllowance));
				returnMap.put("adjustAllowance", adjustAllowanceBigDecimal);
				//调整后话费补助解密
				BigDecimal adjustTelFarePerquisiteBigDecimal = salaryAdjustInfoMap.get("adjustTelFarePerquisite") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustTelFarePerquisite));
				returnMap.put("adjustTelFarePerquisite", adjustTelFarePerquisiteBigDecimal);
			}else if(departmentSalaryAdjustType == 2){
				//社保/公积金调整
				//'原来社保基数加密
				String formerSocialSecurityBase = String.valueOf(salaryAdjustInfoMap.get("formerSocialSecurityBase"));
				//'原来公积金基数加密
				String formerAccumulationFundBase = String.valueOf(salaryAdjustInfoMap.get("formerAccumulationFundBase"));
				//原来社保基数解密
				BigDecimal formerSocialSecurityBaseBigDecimal = salaryAdjustInfoMap.get("formerSocialSecurityBase") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerSocialSecurityBase));
				returnMap.put("formerSocialSecurityBase",formerSocialSecurityBaseBigDecimal);
				//原来公积金基数解密
				BigDecimal formerAccumulationFundBaseBigDecimal = salaryAdjustInfoMap.get("formerAccumulationFundBase") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(formerAccumulationFundBase));
				returnMap.put("formerAccumulationFundBase",formerAccumulationFundBaseBigDecimal);
				//调整后社保基数加密
				String adjustSocialSecurityBase = String.valueOf(salaryAdjustInfoMap.get("adjustSocialSecurityBase"));
				//'调整后公积金基数加密
				String adjustAccumulationFundBase = String.valueOf(salaryAdjustInfoMap.get("adjustAccumulationFundBase"));
				//调整后社保基数解密
				BigDecimal adjustSocialSecurityBaseBigDecimal = salaryAdjustInfoMap.get("adjustSocialSecurityBase") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustSocialSecurityBase));
				//BigDecimal adjustSocialSecurityBaseSubtract = adjustSocialSecurityBaseBigDecimal.subtract(formerSocialSecurityBaseBigDecimal);
				returnMap.put("adjustSocialSecurityBase", adjustSocialSecurityBaseBigDecimal);
				//调整后公积金基数解密
				BigDecimal adjustAccumulationFundBaseBigDecimal = salaryAdjustInfoMap.get("adjustAccumulationFundBase") == null ? new BigDecimal(0.0) : new BigDecimal(AesUtils.decrypt(adjustAccumulationFundBase));
				//BigDecimal adjustAccumulationFundBaseSubtract = adjustAccumulationFundBaseBigDecimal.subtract(formerAccumulationFundBaseBigDecimal);
				returnMap.put("adjustAccumulationFundBase", adjustAccumulationFundBaseBigDecimal);
			}
			returnList.add(returnMap);
		}
		exportDepartmentSalaryAdjustList(returnList, departmentSalaryAdjustType);
		DepartmentSalaryAdjust departmentSalaryAdjust = new DepartmentSalaryAdjust();
		departmentSalaryAdjust.setId(departmentSalaryAdjustId);
		departmentSalaryAdjust.setExportPersonId(loginEmployeeId);
		departmentSalaryAdjust.setExportTime(ExDateUtils.getCurrentDateTime());
		departmentSalaryAdjustMapper.updateDepartmentSalaryAdjust(departmentSalaryAdjust);
		return RestUtils.returnSuccess("导出成功！");
	}


	@Transactional(rollbackFor = Exception.class)
	public RestResponse exportDepartmentSalaryAdjustList (List<Map<String, Object>> returnList, Integer departmentSalaryAdjustType) throws Exception{

		String strLock="";
		XSSFWorkbook workBook = null;
		workBook = new XSSFWorkbook();
		XSSFSheet sheet = null;
		sheet = workBook.createSheet("员工薪酬-薪资调薪");

		//生成第一行
		XSSFRow firstRow = sheet.createRow(0);
		//设置第一行的颜色
		CellStyle style1 = workBook.createCellStyle();
		style1.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
		style1.setFillPattern(CellStyle.SOLID_FOREGROUND);

		firstRow.createCell(0).setCellValue("序号");
		firstRow.createCell(1).setCellValue("一级部门");
		firstRow.createCell(2).setCellValue("二级部门");
		firstRow.createCell(3).setCellValue("姓名");
		firstRow.createCell(4).setCellValue("性别");
		firstRow.createCell(5).setCellValue("身份证号");
		firstRow.createCell(6).setCellValue("岗位/职务");
		firstRow.createCell(7).setCellValue("级别");
		firstRow.createCell(8).setCellValue("调整日期");
		if(departmentSalaryAdjustType == 1){
			firstRow.createCell(9).setCellValue("调整前薪资");
			firstRow.createCell(10).setCellValue("调整后基本工资");
			firstRow.createCell(11).setCellValue("调整后岗位工资");
			firstRow.createCell(12).setCellValue("调整后月度绩效");
			firstRow.createCell(13).setCellValue("调整后月度项目津贴");
			firstRow.createCell(14).setCellValue("调整后话费补助");
			firstRow.createCell(15).setCellValue("调整原因");

		}else{
			firstRow.createCell(9).setCellValue("调整前社保基数");
			firstRow.createCell(10).setCellValue("调整前公积金基数");
			firstRow.createCell(11).setCellValue("调整后岗社保基数");
			firstRow.createCell(12).setCellValue("调整后公积金基数");
			firstRow.createCell(13).setCellValue("调整原因");
		}

		for(int j = 0;j < firstRow.getLastCellNum(); j++){
			firstRow.getCell(j).setCellStyle(style1);
		}

		//下一行
		XSSFRow nextRow = null;
		Map<String, Object> param = null;
		//设置nextRow颜色
		CellStyle style2 = workBook.createCellStyle();
		style2.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
		style2.setFillPattern(CellStyle.SOLID_FOREGROUND);
		//循环 填充表格
		for (int i = 0; i < returnList.size(); i++) {
			param = returnList.get(i);
			nextRow = sheet.createRow(i+1);

			nextRow.createCell(0).setCellValue(String.valueOf(i+1));
			nextRow.createCell(1).setCellValue(String.valueOf(param.get("firstDepartmentName")));
			nextRow.createCell(2).setCellValue(String.valueOf(param.get("secondDepartmentName")));
			nextRow.createCell(3).setCellValue(String.valueOf(param.get("name")));
			nextRow.createCell(4).setCellValue(String.valueOf(param.get("sex")));
			nextRow.createCell(5).setCellValue(String.valueOf(param.get("idCardNumber")));
			nextRow.createCell(6).setCellValue(String.valueOf(param.get("position")));
			nextRow.createCell(7).setCellValue(String.valueOf(param.get("rank")));
			nextRow.createCell(8).setCellValue(String.valueOf(param.get("adjustDate")));
			if(departmentSalaryAdjustType == 1){
				nextRow.createCell(9).setCellValue(String.valueOf(param.get("formerSalary")));
				nextRow.createCell(10).setCellValue(String.valueOf(param.get("adjustBaseWage")));
				nextRow.createCell(11).setCellValue(String.valueOf(param.get("adjustPostWage")));
				nextRow.createCell(12).setCellValue(String.valueOf(param.get("adjustPerformance")));
				nextRow.createCell(13).setCellValue(String.valueOf(param.get("adjustAllowance")));
				nextRow.createCell(14).setCellValue(String.valueOf(param.get("adjustTelFarePerquisite")));
				nextRow.createCell(15).setCellValue(String.valueOf(param.get("adjustReason")));
			}else{

				nextRow.createCell(9).setCellValue(String.valueOf(param.get("formerSocialSecurityBase")));
				nextRow.createCell(10).setCellValue(String.valueOf(param.get("formerAccumulationFundBase")));
				nextRow.createCell(11).setCellValue(String.valueOf(param.get("adjustSocialSecurityBase")));
				nextRow.createCell(12).setCellValue(String.valueOf(param.get("adjustAccumulationFundBase")));
				nextRow.createCell(13).setCellValue(String.valueOf(param.get("adjustReason")));
			}
			for(int j = 0;j < firstRow.getLastCellNum(); j++){
				nextRow.getCell(j).setCellStyle(style2);
			}
		}
		XSSFRow lastRow = null;
		int lengths = returnList.size();
		lastRow = sheet.createRow(lengths+4);
		lastRow.createCell(2).setCellValue("经手人签字：");
		lastRow.createCell(4).setCellValue("领导人签字：");
		XSSFRow endRow = null;
		endRow = sheet.createRow(lengths+6);
		endRow.createCell(2).setCellValue("信息截至至：");
		endRow.createCell(3).setCellValue("年月日");
		strLock = this.exportExcelToComputer(workBook);
		return RestUtils.returnSuccessWithString(strLock);
	}

	/*
	 * 功能：导出测试
	 * 参数：workBook
	 */
	public String exportExcelToComputer(XSSFWorkbook workBook) {
//		try {
//			FileOutputStream fos = new FileOutputStream("D:\\doc\\test.xlsx");
//			workBook.write(fos);
//			fos.flush();
//			fos.close();
//		}catch (IOException e) {
//			e.printStackTrace();
//			return "导出模板表格失败";
//		}
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
		response.addHeader("Content-Disposition","attachment;filename=adjustSalary.xlsx");
		//返回状态为1成功
		response.addHeader("status", "1");
		ServletOutputStream os;
		try {
			os = response.getOutputStream();
			workBook.write(os);
			os.flush();
			os.close();
		} catch (IOException e) {
			logger.error("exportExcelToComputer出现异常:"+e.getMessage(),e);
			return "导出模板表格失败";
		}
		return "导出模板表格成功";
	}

	/**
	 * 删除调薪计划
	 * @param token
	 * @param salaryAdjustMap
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse deleteDepartmentSalaryAdjust(String token, Map<String, Object> salaryAdjustMap) throws Exception{
		Integer departmentSalaryAdjustId = Integer.valueOf(String.valueOf(salaryAdjustMap.get("departmentSalaryAdjustId")));
		DepartmentSalaryAdjust departmentSalaryAdjust = departmentSalaryAdjustMapper.selectByPrimaryKey(departmentSalaryAdjustId);
		if(departmentSalaryAdjust.getStatus() != 0){
			return RestUtils.returnSuccess("不能删除已提交的调薪计划！");
		}
		departmentSalaryAdjustMapper.deleteByPrimaryKey(departmentSalaryAdjustId);
		return RestUtils.returnSuccess("删除成功！");
	}
}