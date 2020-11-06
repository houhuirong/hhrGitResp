package com.nantian.erp.salary.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.Maps;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.vo.ParamSalaryPeriodVo;
import com.nantian.erp.salary.util.AesUtils;

/**
 * 试用期-上岗工资单 service 层
 * 
 * @author caoxb
 * @date 2018-09-013
 */
@Service
@PropertySource(value = { "classpath:config/sftp.properties", "file:${spring.profiles.path}/config/sftp.properties",
		"classpath:config/email.properties", "file:${spring.profiles.path}/config/email.properties",
		"classpath:config/host.properties",
		"file:${spring.profiles.path}/config/host.properties" }, ignoreResourceNotFound = true)
public class ErpPeriodPayrollService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${protocol.type}")
	private String protocolType;// http或https
	@Autowired
	RestTemplate restTemplate;

	@Autowired(required = false)
	private ErpPeriodPayrollMapper erpPeriodPayrollMapper;

	@Autowired
	private ErpPositivePayrollMapper erpPositivePayrollMapper;

	@Autowired
	private ErpTalkSalaryMapper erpTalkSalaryMapper;

	@Autowired
	private ErpPeriodRecordMapper erpPeriodRecordMapper;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private RestTemplateUtils restTemplateUtils;

	@Autowired
	private ErpPositiveRecordMapper erpPositiveRecordMapper;

	@Autowired
	private ErpPayRollFlowMapper erpPayRollFlowMapper;
	@Autowired
	private ErpBasePayrollMapper erpBasePayrollMapper;
	@Autowired
	private ErpPositiveSalaryMapper erpPositiveSalaryMapper;
	@Autowired
	private ErpTraineeSalaryMapper erpTraineeSalaryMapper;
	@Autowired
	private ErpSalaryMonthPerformanceService erpSalaryMonthPerformanceService;
	@Autowired
	private ErpPositiveConfirMapper erpPositiveConfirMapper;
	@Autowired
	private ErpBasePayrollUpdateRecordMapper erpBasePayrollUpdateRecordMapper;

	/*
	 * 查询入职-待我处理上岗工资单-列表
	 * 参数：null
	 */
	@SuppressWarnings({ "unchecked" })
	public List<Map<String, Object>> findAllPayrollForMe(String token) {

		logger.info("查询入职-待我处理上岗工资单-列表  参数token " + token);
		List<Map<String, Object>> returnList = new ArrayList<>();// 定义返回结果
		Boolean flag = false;// 解密标识
		try {
			ErpUser erpUser = (ErpUser) this.redisTemplate.opsForValue().get(token);
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("currentPersonID", erpUser.getUserId());
			queryMap.put("status", 1);// 入职未处理
			List<Map<String, Object>> UserList = this.erpPayRollFlowMapper.findUserIdsByCurrentPerID(queryMap);// 当前处理人查询未审批用户IDs
			if (UserList.size() > 0) {
				returnList = allPeriodPayroll(UserList, token, flag);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("查询入职-待我处理上岗工资单-列表 findAllPayrollForMe 出现异常" + e.getMessage());
		}
		logger.info("返回的员工记录条数：" + returnList.size());
		return returnList;
	}

	/*
	 * 查询 试用期-所有上岗工资单 
	 * 参数：null
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findAllPayroll(String token, String startMonth, String endMonth) {
		logger.info("查询 试用期-所有上岗工资单  参数token=" + token);
		// 返回结果
		List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> returnTempList = new ArrayList<Map<String, Object>>();
		Boolean flag = false;// 解密标识
		try {
			ErpUser erpUser = (ErpUser) this.redisTemplate.opsForValue().get(token);
			List<Integer> roles = erpUser.getRoles();// 角色列表

			Map<String, Object> queryMap = new HashMap<String, Object>();
			Integer isManger = 0;
			if (roles.contains(8) || roles.contains(7)) {// 总经理、经管可以看到所有部门的待入职
				queryMap.put("managerId", erpUser.getUserId());
			} else if (roles.contains(9)) { // 副总经理
				queryMap.put("superLeaderId", erpUser.getUserId());
			} else if (roles.contains(2)) {// 一级部门经理角色
				queryMap.put("leaderId", erpUser.getUserId());
			} else {
				return returnList;
			}

			// 按起止月份查询
			queryMap.put("startMonth", startMonth);
			queryMap.put("endMonth", endMonth);

			List<Map<String, Object>> UserList = this.erpPayRollFlowMapper.findAllPeriodPayRoll(queryMap);
			if (UserList.size() > 0) {
				returnList = allPeriodPayroll(UserList, token, flag);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("查询 试用期-所有上岗工资单 findAllPayroll 出现异常：" + e.getMessage());
		}
		logger.info("返回的员工记录条数：" + returnList.size());
		return returnList;
	}

	/*
	 * 入职-新增上岗工资单
	 * 参数：erpPeriodPayroll
	 */
	@Transactional
	public String insertErpPayroll(ParamSalaryPeriodVo paramSalaryVo, String token) {
		logger.info("入职-新增上岗工资单 参数-封装对象 : " + paramSalaryVo.toString());
		String str = null;
		Boolean flag = true;// 加密标志
		try {
			// 通过token从redis缓存中获取用户信息
			ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
			String employeeName = userInfo.getEmployeeName();// 员工姓名
			String username = userInfo.getUsername();// 用户名
			// 第一步：新增员工薪资表
			ErpBasePayroll erpBasePayroll = paramSalaryVo.getErpBasePayroll();
			Integer empId = erpBasePayroll.getErpEmployeeId();
			ErpBasePayroll basePayRoll = this.erpBasePayrollMapper.findBasePayrollDetailByEmpId(empId);
			if (null == basePayRoll) {
				ErpBasePayroll encryptBasePayroll = encryptAndDecryptBasePayroll(flag, erpBasePayroll);
				this.erpBasePayrollMapper.insertBasePayroll(encryptBasePayroll);
				/*
				 * 将该员工的修改信息加入日志中
				 */
				ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
				basePayrollUpdateRecord.setEmployee(restTemplateUtils.findEmpNameByEmployeeId(token,empId));// 被修改的员工
				basePayrollUpdateRecord.setEmployeeId(empId);
				basePayrollUpdateRecord
						.setProcessor(employeeName == null || "".equals(employeeName) ? username : employeeName);// 修改人
				basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
				basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
				this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);
				// 第二步新增转正薪资表数据
				ErpPositiveSalary erpPositiveSalary = paramSalaryVo.getErpPositiveSalary();
				ErpPositiveSalary encryptPositiveSalary = encryptAndDecryptPositiveSalary(flag, erpPositiveSalary);
				this.erpPositiveSalaryMapper.insertPositiveSalary(encryptPositiveSalary);
				Integer isPeriod = erpPositiveSalary.getIsPeriod();
				if (isPeriod == 1) {// 有试用期
					// 第三步：新增试用期工资
					ErpPeriodPayroll erpPeriodPayroll = paramSalaryVo.getErpPeriodPayroll();
					ErpPeriodPayroll encryptPeriodPayroll = encryptAndDecryptPeriodPayroll(flag, erpPeriodPayroll);
					this.erpPeriodPayrollMapper.insert(encryptPeriodPayroll);
				}
//				String url=protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/findUserByEmpId?empId="+erpBasePayroll.getErpEmployeeId();
//				HttpHeaders requestHeaders=new HttpHeaders();
//				requestHeaders.add("token", token);
//		        HttpEntity<String> requestEntity=new HttpEntity<String>(null,requestHeaders);
//		        ResponseEntity<String> response= restTemplate.exchange(url,HttpMethod.GET,requestEntity,String.class);
//		        String strResult=response.getBody();
//		        JSONObject jsStr=JSON.parseObject(strResult);
//		    	if(null == jsStr.get("data") || "".equals(String.valueOf(jsStr.get("data")))) {
//					return null;
//				}
//		        Map<String,Object> map=(Map<String, Object>) jsStr.get("data");
				// 第五步：变更上岗工资流程审批表中状态
				ErpPayRollFlow erpPayRollFlow = new ErpPayRollFlow();
				erpPayRollFlow.setStatus(2);// 入职工资单已处理
				erpPayRollFlow.setUserId(empId);
				this.erpPayRollFlowMapper.updatePayRollFlow(erpPayRollFlow);

				// add by ZhangYuWei 20190128 新增一条试用期工资条记录
				String erpPayrollHandler = employeeName == null || "".equals(employeeName) ? username : employeeName;// 操作人

				ErpPeriodRecord erpPeriodRecord = paramSalaryVo.getErpPeriodRecord();
				erpPeriodRecord.setErpPayrollHandler(erpPayrollHandler);
				erpPeriodRecord.setPayrollTime(ExDateUtils.getCurrentStringDateTime());
				erpPeriodRecord.setContent("新增上岗工资单");
				erpPeriodRecordMapper.insertPeriodRecord(erpPeriodRecord);
			} else {
				return "该人员已录入入职上岗工资单,请勿重复录入！";
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("入职-新增上岗工资单 insertErpPayroll 出现异常 ：" + e.getMessage());
			str = "新增失败 ！";
		}
		return str;
	}

	/*
	 * 入职-修改上岗工资单 
	 * 参数：paramSalaryVo
	 */
	@Transactional
	public String updateErpPayroll(ParamSalaryPeriodVo paramSalaryVo, String token) {
		logger.info("招聘-修改上岗工资单 参数-封装对象 : " + paramSalaryVo.toString());
		String str = null;
		Boolean flag = true;// 加密标识
		try {
			// 第三步修改转正薪资
			ErpBasePayroll erpBasePayroll = paramSalaryVo.getErpBasePayroll();
			ErpBasePayroll encryptBasePayroll = encryptAndDecryptBasePayroll(flag, erpBasePayroll);
			this.erpBasePayrollMapper.updateBasePayroll(encryptBasePayroll);
			// 第四步修改员工薪资表
			ErpPositiveSalary erpPositiveSalary = paramSalaryVo.getErpPositiveSalary();
			ErpPositiveSalary encryptPositiveSalary = encryptAndDecryptPositiveSalary(flag, erpPositiveSalary);
			this.erpPositiveSalaryMapper.updatePositiveSalaryByEmpId(encryptPositiveSalary);
			Integer isPeriod = erpPositiveSalary.getIsPeriod();
			if (isPeriod == 1) {// 有试用期
				// 第一步：修改试用期-工资
				ErpPeriodPayroll erpPeriodPayroll = paramSalaryVo.getErpPeriodPayroll();
				Integer empId = erpPeriodPayroll.getErpEmployeeId();
				ErpPeriodPayroll period = this.erpPeriodPayrollMapper.findPeriodSalary(empId);
				if (period != null) {
					ErpPeriodPayroll encryptPeriodPayroll = encryptAndDecryptPeriodPayroll(flag, erpPeriodPayroll);
					this.erpPeriodPayrollMapper.updateById(encryptPeriodPayroll);
				} else {
					ErpPeriodPayroll encryptPeriodPayroll = encryptAndDecryptPeriodPayroll(flag, erpPeriodPayroll);
					this.erpPeriodPayrollMapper.insert(encryptPeriodPayroll);
				}
			}
			// add by ZhangYuWei 20190128 新增一条试用期工资记录
			// 通过token从redis缓存中获取用户信息
			ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
			String employeeName = userInfo.getEmployeeName();// 员工姓名
			String username = userInfo.getUsername();// 用户名
			String erpPayrollHandler = employeeName == null || "".equals(employeeName) ? username : employeeName;// 操作人

			ErpPeriodRecord erpPeriodRecord = paramSalaryVo.getErpPeriodRecord();
			erpPeriodRecord.setErpPayrollHandler(erpPayrollHandler);
			erpPeriodRecord.setPayrollTime(ExDateUtils.getCurrentStringDateTime());
			erpPeriodRecord.setContent("修改上岗工资单");
			erpPeriodRecordMapper.insertPeriodRecord(erpPeriodRecord);
			str = "修改成功 ！";
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("入职-修改上岗工资单 updateErpPayroll 出现异常 ：" + e.getMessage());
			str = "修改失败 ！";
		}
		return str;
	}

	@Transactional
	public String updateErpPayRollFlow(Map<String, Object> paramMap, String token) {
		logger.info("updateErpPayRollFlow方法开始执行,参数paramMap:" + paramMap + "参数token:" + token);
		String str = null;
		String opType = null;
		ErpPayRollFlow erpPayRollFlow = new ErpPayRollFlow();
		ErpPositiveRecord erpPositiveRecord = new ErpPositiveRecord();
		try {
			opType = (String) paramMap.get("opType");
			if (opType.equals("lock")) {
				List<Integer> empIdList = new ArrayList<Integer>();
				if (paramMap.get("employeeId") != null) {
					empIdList.addAll((List<Integer>) paramMap.get("employeeId"));
				}
				if (empIdList.size() > 0) {
					for (int i = 0; i < empIdList.size(); i++) {

						Integer employeeId = empIdList.get(i);
						if (employeeId != null) {

							// add by ZhangYuWei 20190128 增加该员工工资单被修改的记录
							// 通过token从redis缓存中获取用户信息

							ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
							String employeeName = userInfo.getEmployeeName();// 员工姓名
							String username = userInfo.getUsername();// 用户名
							String erpPayrollHandler = employeeName == null || "".equals(employeeName) ? username
									: employeeName;// 操作人

							Set<String> set = paramMap.keySet();
							for (String key : set) {
								if (key.contains("period")) {
									Integer isLock = Integer.valueOf(String.valueOf(paramMap.get(key)));
									erpPayRollFlow.setPeriodIsLock(isLock);

									// 上岗工资单日志的打印
									ErpPeriodRecord erpPeriodRecord = new ErpPeriodRecord();
									erpPeriodRecord.setErpEmployeeId(employeeId);
									erpPeriodRecord.setErpPayrollHandler(erpPayrollHandler);
									erpPeriodRecord.setPayrollTime(ExDateUtils.getCurrentStringDateTime());
									erpPeriodRecord.setContent("上岗工资单锁定成功");
									erpPeriodRecordMapper.insertPeriodRecord(erpPeriodRecord);
								} else if (key.contains("positive")) {
									Integer isLock = Integer.valueOf(String.valueOf(paramMap.get("positiveIsLock")));
									erpPayRollFlow.setPositiveIsLock(isLock);
									if ((String) paramMap.get("positiveMonth") != null) {
										String positiveMonth = (String) paramMap.get("positiveMonth");
										erpPayRollFlow.setPositiveMonth(positiveMonth);
									}
									// 转正工资单日志的打印
									erpPositiveRecord.setErpEmployeeId(employeeId);
									erpPositiveRecord.setErpPositiveHandler(erpPayrollHandler);
									erpPositiveRecord.setPositiveTime(ExDateUtils.getCurrentStringDateTime());
									erpPositiveRecord.setContent("转正工资单锁定成功");
								}
							}
							erpPositiveRecordMapper.insertPositiveRecord(erpPositiveRecord);
							erpPayRollFlow.setUserId(employeeId);
							this.erpPayRollFlowMapper.updatePayRollFlow(erpPayRollFlow);
						}
					}
					str = "锁定成功！";
				}
			} else if (opType.equals("changeMonth")) {
				Integer empId = (Integer) paramMap.get("employeeId");
				String commitMonth = (String) paramMap.get("commitMonth");

				erpPayRollFlow.setUserId(empId);
				erpPayRollFlow.setCommitMonth(commitMonth);
				this.erpPayRollFlowMapper.updatePayRollFlow(erpPayRollFlow);
			} else if (opType.equals("positiveMonth")) {
				Integer empId = (Integer) paramMap.get("employeeId");
				String empName="";
				String positiveMonth = (String) paramMap.get("positiveMonth");
				boolean isConfirmed=this.erpPayRollFlowMapper.findIsConfirmedByUserId(empId);
				if (isConfirmed) {
					Map<String, Object> managerInfo = (Map<String, Object>) redisTemplate.opsForValue().get("employee_" + empId);
					empName =String.valueOf(managerInfo.get("employeeName"));
					return str = "员工"+empName+"已确认，无法修改！";
					 
				}
				Map<String,Object> newConfirMap = erpPositiveConfirMapper.seleConfirmByear(positiveMonth);
				if (newConfirMap != null) {
					return str = positiveMonth+"月已确认，无法修改！";
				}
				erpPayRollFlow.setUserId(empId);
				erpPayRollFlow.setPositiveMonth(positiveMonth);
				this.erpPayRollFlowMapper.updatePayRollFlow(erpPayRollFlow);
			} else {
				logger.info("updateErpPayRollFlow操作类型错误" + opType);
				str = "操作错误";
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("updateErpPayRollFlow方法出现错误:" + e.getMessage(),e);
			str = "锁定失败 ！";
		}
		return str;
	}

	/**
	 * Description: 测试加密
	 *
	 * @return
	 * @Author HouHuiRong
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
	 * Description: 测试解密
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

	public ErpPeriodPayroll encryptAndDecryptPeriodPayroll(Boolean flag, ErpPeriodPayroll erpPeriodPayroll) {
		if (erpPeriodPayroll != null) {
			if (flag) {// 加密
				erpPeriodPayroll.setErpPeriodBaseWage(encryptDataRsa(erpPeriodPayroll.getErpPeriodBaseWage()));
				erpPeriodPayroll.setErpPeriodPostWage(encryptDataRsa(erpPeriodPayroll.getErpPeriodPostWage()));
				erpPeriodPayroll.setErpPeriodPerformance(encryptDataRsa(erpPeriodPayroll.getErpPeriodPerformance()));
				erpPeriodPayroll.setErpPeriodIncome(encryptDataRsa(erpPeriodPayroll.getErpPeriodIncome()));
				erpPeriodPayroll.setErpPeriodAllowance(encryptDataRsa(erpPeriodPayroll.getErpPeriodAllowance()));
				erpPeriodPayroll.setErpTelFarePerquisite(encryptDataRsa(erpPeriodPayroll.getErpTelFarePerquisite()));
			} else {// 解密
				erpPeriodPayroll.setErpPeriodBaseWage(decryptDataRsa(erpPeriodPayroll.getErpPeriodBaseWage()));
				erpPeriodPayroll.setErpPeriodPostWage(decryptDataRsa(erpPeriodPayroll.getErpPeriodPostWage()));
				erpPeriodPayroll.setErpPeriodPerformance(decryptDataRsa(erpPeriodPayroll.getErpPeriodPerformance()));
				erpPeriodPayroll.setErpPeriodIncome(decryptDataRsa(erpPeriodPayroll.getErpPeriodIncome()));
				erpPeriodPayroll.setErpPeriodAllowance(decryptDataRsa(erpPeriodPayroll.getErpPeriodAllowance()));
				erpPeriodPayroll.setErpTelFarePerquisite(decryptDataRsa(erpPeriodPayroll.getErpTelFarePerquisite()));
			}
		}
		return erpPeriodPayroll;
	}

	public ErpPositiveSalary encryptAndDecryptPositiveSalary(Boolean flag, ErpPositiveSalary erpPositiveSalary) {
		if (erpPositiveSalary != null) {
			if (flag) {// 加密
				erpPositiveSalary.setErpPositiveBaseWage(encryptDataRsa(erpPositiveSalary.getErpPositiveBaseWage()));
				erpPositiveSalary.setErpPositivePostWage(encryptDataRsa(erpPositiveSalary.getErpPositivePostWage()));
				erpPositiveSalary
						.setErpPositivePerformance(encryptDataRsa(erpPositiveSalary.getErpPositivePerformance()));
				erpPositiveSalary.setErpPositiveAllowance(encryptDataRsa(erpPositiveSalary.getErpPositiveAllowance()));
				erpPositiveSalary.setErpPositiveIncome(encryptDataRsa(erpPositiveSalary.getErpPositiveIncome()));
				erpPositiveSalary.setErpTelFarePerquisite(encryptDataRsa(erpPositiveSalary.getErpTelFarePerquisite()));
			} else {// 解密
				erpPositiveSalary.setErpPositiveBaseWage(decryptDataRsa(erpPositiveSalary.getErpPositiveBaseWage()));
				erpPositiveSalary.setErpPositivePostWage(decryptDataRsa(erpPositiveSalary.getErpPositivePostWage()));
				erpPositiveSalary
						.setErpPositivePerformance(decryptDataRsa(erpPositiveSalary.getErpPositivePerformance()));
				erpPositiveSalary.setErpPositiveAllowance(decryptDataRsa(erpPositiveSalary.getErpPositiveAllowance()));
				erpPositiveSalary.setErpPositiveIncome(decryptDataRsa(erpPositiveSalary.getErpPositiveIncome()));
				erpPositiveSalary.setErpTelFarePerquisite(decryptDataRsa(erpPositiveSalary.getErpTelFarePerquisite()));
			}
		}
		return erpPositiveSalary;
	}

	public ErpBasePayroll encryptAndDecryptBasePayroll(Boolean flag, ErpBasePayroll erpBasePayroll) {
		if (erpBasePayroll != null) {
			if (flag) {// 加密
				erpBasePayroll.setErpBaseWage(encryptDataRsa(erpBasePayroll.getErpBaseWage()));
				erpBasePayroll.setErpPostWage(encryptDataRsa(erpBasePayroll.getErpPostWage()));
				erpBasePayroll.setErpPerformance(encryptDataRsa(erpBasePayroll.getErpPerformance()));
				erpBasePayroll.setErpAllowance(encryptDataRsa(erpBasePayroll.getErpAllowance()));
				erpBasePayroll.setErpSocialSecurityBase(encryptDataRsa(erpBasePayroll.getErpSocialSecurityBase()));
				erpBasePayroll.setErpAccumulationFundBase(encryptDataRsa(erpBasePayroll.getErpAccumulationFundBase()));
				erpBasePayroll.setErpTelFarePerquisite(encryptDataRsa(erpBasePayroll.getErpTelFarePerquisite()));
			} else {// 解密
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

	public ErpTraineeSalary encryptAndDecryptTraineeSalary(Boolean flag, ErpTraineeSalary erpTraineeSalary) {
		if (erpTraineeSalary != null) {
			if (flag) {// 加密
				erpTraineeSalary.setBaseWage(encryptDataRsa(erpTraineeSalary.getBaseWage()));
				erpTraineeSalary.setMonthAllowance(encryptDataRsa(erpTraineeSalary.getMonthAllowance()));
			} else {// 解密
				erpTraineeSalary.setBaseWage(decryptDataRsa(erpTraineeSalary.getBaseWage()));
				erpTraineeSalary.setMonthAllowance(decryptDataRsa(erpTraineeSalary.getMonthAllowance()));
			}
		}
		return erpTraineeSalary;
	}

	/**
	 * 新增实习生薪资
	 */
	@Transactional
	public String insertErpTraineeSalary(ParamSalaryPeriodVo paramSalaryVo, String token) {
		logger.info("实习生入职-新增上岗工资单 参数-封装对象 : " + paramSalaryVo.toString());
		String str = null;
		Boolean flag = true;// 加密标志
		try {
			// 通过token从redis缓存中获取用户信息
			ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
			String employeeName = userInfo.getEmployeeName();// 员工姓名
			String username = userInfo.getUsername();// 用户名
			// 第一步：新增员工薪资表
			ErpBasePayroll erpBasePayroll = paramSalaryVo.getErpBasePayroll();
			ErpBasePayroll basePayroll = this.erpBasePayrollMapper
					.findBasePayrollDetailByEmpId(erpBasePayroll.getErpEmployeeId());
			if (basePayroll == null) {
				ErpBasePayroll encryptBasePayroll = encryptAndDecryptBasePayroll(flag, erpBasePayroll);
				this.erpBasePayrollMapper.insertBasePayroll(encryptBasePayroll);
				/*
				 * 将该员工的修改信息加入日志中
				 */
				ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
				basePayrollUpdateRecord.setEmployee(restTemplateUtils.findEmpNameByEmployeeId(token,erpBasePayroll.getErpEmployeeId()));// 被修改的员工
				basePayrollUpdateRecord.setEmployeeId(erpBasePayroll.getErpEmployeeId());
				basePayrollUpdateRecord
						.setProcessor(employeeName == null || "".equals(employeeName) ? username : employeeName);// 修改人
				basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
				basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
				this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);
				// 第二步新增转正薪资表数据
				ErpTraineeSalary erpTraineeSalary = paramSalaryVo.getErpTraineeSalary();
				ErpTraineeSalary encryptTraineeSalary = encryptAndDecryptTraineeSalary(flag, erpTraineeSalary);
				this.erpTraineeSalaryMapper.insertTraineeSalary(encryptTraineeSalary);

				// 第五步：变更上岗工资流程审批表中状态
				ErpPayRollFlow erpPayRollFlow = new ErpPayRollFlow();
				erpPayRollFlow.setStatus(3);// 入职工资单已处理
				erpPayRollFlow.setUserId(erpBasePayroll.getErpEmployeeId());
				this.erpPayRollFlowMapper.updatePayRollFlow(erpPayRollFlow);
				str = "新增成功";

				// add by ZhangYuWei 20190128 新增一条操作记录
				String erpPayrollHandler = employeeName == null || "".equals(employeeName) ? username : employeeName;// 操作人

				ErpPeriodRecord erpPeriodRecord = paramSalaryVo.getErpPeriodRecord();
				erpPeriodRecord.setErpPayrollHandler(erpPayrollHandler);
				erpPeriodRecord.setPayrollTime(ExDateUtils.getCurrentStringDateTime());
				erpPeriodRecord.setContent("新增上岗工资单（实习生）");
				erpPeriodRecordMapper.insertPeriodRecord(erpPeriodRecord);
			} else {
				return "该人员已录入入职上岗工资单,请勿重复录入！";
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("入职-新增上岗工资单 insertErpPayroll 出现异常 ：" + e.getMessage());
			str = "新增失败 ！";
		}
		return str;
	}

	@Transactional
	public String updateErpTraineeSalary(ParamSalaryPeriodVo paramSalaryVo, String token) {
		logger.info("实习生入职-修改上岗工资单 参数-封装对象 : " + paramSalaryVo.toString());
		String str = null;
		Boolean flag = true;// 加密标志
		try {
			// 第一步：新增员工薪资表
			ErpBasePayroll erpBasePayroll = paramSalaryVo.getErpBasePayroll();
			ErpBasePayroll encryptBasePayroll = encryptAndDecryptBasePayroll(flag, erpBasePayroll);
			this.erpBasePayrollMapper.updateBasePayroll(encryptBasePayroll);
			// 第二步新增转正薪资表数据
			ErpTraineeSalary erpTraineeSalary = paramSalaryVo.getErpTraineeSalary();
			ErpTraineeSalary encryptTraineeSalary = encryptAndDecryptTraineeSalary(flag, erpTraineeSalary);
			this.erpTraineeSalaryMapper.updateTraineeSalary(encryptTraineeSalary);
			str = "修改成功";

			// add by ZhangYuWei 20190128 新增一条操作记录
			// 通过token从redis缓存中获取用户信息
			ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
			String employeeName = userInfo.getEmployeeName();// 员工姓名
			String username = userInfo.getUsername();// 用户名
			String erpPayrollHandler = employeeName == null || "".equals(employeeName) ? username : employeeName;// 操作人

			ErpPeriodRecord erpPeriodRecord = paramSalaryVo.getErpPeriodRecord();
			erpPeriodRecord.setErpPayrollHandler(erpPayrollHandler);
			erpPeriodRecord.setPayrollTime(ExDateUtils.getCurrentStringDateTime());
			erpPeriodRecord.setContent("修改上岗工资单（实习生）");
			erpPeriodRecordMapper.insertPeriodRecord(erpPeriodRecord);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("入职-新增上岗工资单 insertErpPayroll 出现异常 ：" + e.getMessage());
			str = "修改失败 ！";
		}
		return str;
	}

	public List<Map<String, Object>> allPeriodPayroll(List<Map<String, Object>> list, String token, Boolean flag) {
		logger.info("allPeriodPayroll开始执行，参数为list:" + list);
		List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
		try {

			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoById";
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);// 将token放到请求头中
			HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(list, requestHeaders);

			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
			if (response.getStatusCodeValue() != 200) {
				logger.info("入职-新增上岗工资单查询员工信息，hr工程返回错误");
				return returnList;
			}

			List<Map<String, Object>> mapList = (List<Map<String, Object>>) response.getBody().get("data");

			for (int i = 0; i < mapList.size(); i++) {
				Map<String, Object> userInfoMap = mapList.get(i);

				// 根据简历ID查询员工的面试谈薪
				Integer offerId = Integer
						.valueOf(String.valueOf(userInfoMap.get("offerId") == null ? 0 : userInfoMap.get("offerId")));
				ErpTalkSalary erpTalkSalary = this.erpTalkSalaryMapper.findOneByOfferId(offerId);
				if (erpTalkSalary != null) {
					erpTalkSalary.setAccumulationFundBase(decryptDataRsa(erpTalkSalary.getAccumulationFundBase()));
					erpTalkSalary.setMonthIncome(decryptDataRsa(erpTalkSalary.getMonthIncome()));
					erpTalkSalary.setSocialSecurityBase(decryptDataRsa(erpTalkSalary.getSocialSecurityBase()));
					erpTalkSalary.setBaseWage(decryptDataRsa(erpTalkSalary.getBaseWage()));
					erpTalkSalary.setMonthAllowance(decryptDataRsa(erpTalkSalary.getMonthAllowance()));
				}
				userInfoMap.put("erpTalkSalaryInfo", erpTalkSalary);
				userInfoMap.put("entryStatus", userInfoMap.get("payRollFlowStatus"));
				userInfoMap.put("periodIsLock", userInfoMap.get("periodIsLock"));// 试用期是否锁定
				Boolean isTrainee = Boolean.valueOf(String.valueOf(userInfoMap.get("isTrainee")));// 是否是实习生
				Integer status = Integer.valueOf(String.valueOf(userInfoMap.get("payRollFlowStatus")));
				Integer employeeId = Integer.valueOf(String.valueOf(userInfoMap.get("employeeId")));
				if (status == 2 || status == 3 || status == 4) {// 入职已处理
					if (isTrainee) {// 实习生薪资
						ErpTraineeSalary erpTraineeSalary = this.erpTraineeSalaryMapper
								.selectOneTraineeSalary(employeeId);
						ErpTraineeSalary decryptTraineeSalary = encryptAndDecryptTraineeSalary(flag, erpTraineeSalary);
						userInfoMap.put("erpTraineeSalary", decryptTraineeSalary);// 试用期相关工资

					} else {// 社招生薪资
							// 员工基础薪资
						ErpBasePayroll erpBasePayroll = this.erpBasePayrollMapper
								.findBasePayrollDetailByEmpId(employeeId);
						ErpBasePayroll decryptBasePayroll = encryptAndDecryptBasePayroll(flag, erpBasePayroll);
						userInfoMap.put("erpBasePayroll", decryptBasePayroll);
						// 转正薪资
						ErpPositiveSalary erpPositiveSalary = this.erpPositiveSalaryMapper
								.findPositiveSalaryByEmpId(employeeId);
						ErpPositiveSalary decryptPositiveSalary = encryptAndDecryptPositiveSalary(flag,
								erpPositiveSalary);
						userInfoMap.put("erpPositiveSalary", decryptPositiveSalary);
						Integer isPeriod = erpPositiveSalary == null?null:erpPositiveSalary.getIsPeriod();
						if (isPeriod != null && isPeriod == 1 ) {
							// 查询填写的试用期工资单
							ErpPeriodPayroll erpPeriodPayroll = this.erpPeriodPayrollMapper
									.selectOnePeriodPayroll(employeeId);
							ErpPeriodPayroll decryptPeriodPayroll = encryptAndDecryptPeriodPayroll(flag,
									erpPeriodPayroll);
							userInfoMap.put("erpPeriodPayrollInfo", decryptPeriodPayroll);// 试用期相关工资
						} else {
							userInfoMap.put("erpPeriodPayrollInfo", "");// 试用期相关工资
						}
					}
				} else if (status == 1) {// 入职未处理
					if (isTrainee) {// 实习生
						userInfoMap.put("erpTraineeSalary", "");
					} else {// 社招生
						if ((userInfoMap.get("probationEndTime") != null) && (!userInfoMap.get("probationEndTime")
								.equals(userInfoMap.get("contractBeginTime")))) {
							userInfoMap.put("isPeriod", true);
						}
						userInfoMap.put("erpPeriodPayrollInfo", "");
						userInfoMap.put("erpBasePayroll", "");
						userInfoMap.put("erpPositiveSalary", "");
					}
				}
				returnList.add(userInfoMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("入职-查询上岗工资单 allPeriodPayroll 出现异常 ：" + e.getMessage(), e);
		}
		return returnList;
	}

	/**
	 * Description: 根据员工ID查询入职上岗工资单的操作记录
	 * 
	 * @param firstDepartmentId 员工Id
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月28日 下午16:22:13
	 */
	public RestResponse findPayrollRecord(Integer employeeId) {
		logger.info("进入findPayrollRecord方法，参数是：employeeId=" + employeeId);
		try {
			List<ErpPeriodRecord> recordList = erpPeriodRecordMapper.selectPeriodRecord(employeeId);
			return RestUtils.returnSuccess(recordList);
		} catch (Exception e) {
			logger.error("方法findPayrollRecord出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法异常，导致查询失败！");
		}
	}
}