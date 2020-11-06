package com.nantian.erp.salary.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nantian.erp.common.base.util.StringUtil;
import com.nantian.erp.salary.data.dao.*;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.constants.DicConstants;
import com.nantian.erp.salary.data.model.ErpActualInsuranceFundBase;
import com.nantian.erp.salary.data.model.ErpBasePayroll;
import com.nantian.erp.salary.data.model.ErpEmpFinanceNumber;
import com.nantian.erp.salary.data.model.ErpPeriodPayroll;
import com.nantian.erp.salary.data.model.ErpPositivePayroll;
import com.nantian.erp.salary.data.model.ErpSalaryMonthPerformance;
import com.nantian.erp.salary.data.model.ErpSalaryMonthPerformanceRecord;
import com.nantian.erp.salary.util.AesUtils;

/** 
 * Description: 功能：月度绩效 service实现层
 * @author caoxiubin
 * @version 1.1
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年09月08日                       caoxiubin          1.0        
 * 2018年12月02日                      ZhangYuWei          1.1
 * 2020年02月22日                      Songxiugong          1.2
 * </pre>
 */
@Service
@PropertySource(value= {"classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpSalaryMonthPerformanceService {
	/*
	 * 从配置文件中获取主机相关属性
	 */
	@Value("${protocol.type}")
    private String protocolType;//http或https
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ErpSalaryMonthPerformanceMapper erpMonthPerformanceMapper;
	@Autowired
	private ErpSalaryMonthPerformanceMapper erpSalaryMonthPerformanceMapper;
	@Autowired
	private ErpSalaryMonthPerformanceRecordMapper salaryMonthPerformanceRecordMapper;
	@Autowired
	private ErpBasePayrollMapper erpBasePayrollMapper;
	@Autowired
	private ErpPeriodPayrollMapper erpPeriodPayrollMapper;
	@Autowired
	private ErpPositivePayrollMapper erpPositivePayrollMapper;
	@Autowired
	private ErpEmpFinanceNumberMapper empFinanceNumberMapper;
	@Autowired
	private ErpPositivePayrollService erpPositivePayrollService;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;
	
	/**
	 * Description: 查询当前登录用户权限内的所有一级部门
	 * 
	 * @param  token
	 * @UpdateAuthor ZhangYuWei
	 * @UpdateDate 2018年12月06日 下午22:01:14
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RestResponse findAllFirstDepartmentByPowerParams(String token) {
		logger.info("进入findAllFirstDepartmentByPowerParams方法，参数是：token="+token);
		List<Map<String,Object>> firstDepartmentList = new ArrayList<>();
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();//用户Id
			String username = erpUser.getUsername();//从用户信息中获取用户名
			List<Integer> roles = erpUser.getRoles();//从用户信息中获取角色信息
			logger.info("id="+id+",username="+username+",roles="+roles);
			
			//仅一级部门经理角色能看到权限内部门的月度绩效
/*			if(!roles.contains(2)) {
				return RestUtils.returnSuccess(firstDepartmentList);
			}
			
			 * 当前登录用户的角色查看数据的权限
			 
			departmentParams.put("userId", id);
			departmentParams.put("superLeader", null);*/
			
			//调用ERP-人力资源 工程 的操作层服务接口-获取一级部门下面所有二级部门的员工的详细信息
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/department/findContainSecDepAllFirstDepartment";
			Map<String,Object> departmentParams = new HashMap<>();
			/*
			 * 判断当前登录用户的角色查看数据的权限
			 */
			if(roles.contains(8)) {//总经理
				departmentParams.put("userId", null);
				departmentParams.put("superLeader", null);
			}else if(roles.contains(9)) {//副总经理
				departmentParams.put("userId", null);
				departmentParams.put("superLeader", id);
			}else {//一级部门经理、其他角色
				departmentParams.put("userId", id);
				departmentParams.put("superLeader", null);
			}
			
			HttpHeaders requestHeaders=new HttpHeaders();
			requestHeaders.add("token", token);//将token放到请求头中
			HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<>(departmentParams, requestHeaders);
			
			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, requestEntity, Map.class);
			if(200 != response.getStatusCodeValue()) {
				return RestUtils.returnSuccessWithString("调用人力资源工程失败！");
			}
			//解析获取的数据
			Map<String,Object> responseBody = response.getBody();
			if(!"200".equals(responseBody.get("status"))) {
				return RestUtils.returnSuccessWithString("人力资源工程发生异常！");
			}
			
			firstDepartmentList = (List<Map<String, Object>>) responseBody.get("data");
			return RestUtils.returnSuccess(firstDepartmentList);
		} catch (Exception e) {
			logger.error("findAllFirstDepartmentByPowerParams方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
	}
	
	/**
	 * Description: 根据当前登陆用户  生成月度绩效表单信息
	 * @param  paramTokenVo        封装对象
	 * @return RestResponse             
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月30日 下午16:24:51
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RestResponse showTableContent(HttpServletRequest request) {
		try {
			String token = request.getHeader("token");
			ErpUser erpUser = (ErpUser) this.redisTemplate.opsForValue().get(token);//从redis缓存中获取用户信息
			Integer id = erpUser.getUserId();//从用户信息中获取用户编号
			String username = erpUser.getUsername();//从用户信息中获取用户名
					    
		    /*
			 * 调用人力资源工程，查询用户的一级部门编号、一级部门名称
			 */
		    String url1 = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeDetail?employeeId="+id;
			HttpHeaders requestHeaders1=new HttpHeaders();
			requestHeaders1.add("token", token);
		    HttpEntity<String> requestEntity1=new HttpEntity<String>(null,requestHeaders1);
		    ResponseEntity<Map> response1 = restTemplate.exchange(url1,HttpMethod.GET,requestEntity1,Map.class);
		    if(200 != response1.getStatusCodeValue()) {
				return RestUtils.returnFailure("调用人力资源工程失败！");
			}
			//解析获取的数据
			Map<String,Object> responseBody1 = response1.getBody();
			if(!"200".equals(responseBody1.get("status"))) {
				return RestUtils.returnFailure("人力资源工程发生异常！");
			}
		    /*
		     * 通过用户ID获取到员工信息
		     */
		    Map<String,Object> employeeMap = (Map<String, Object>) responseBody1.get("data");
			Integer firstDepartmentId = null;//一级部门编号
			String firstDepartmentName = null;//一级部门名称
			if(null != employeeMap.get("firstDepartmentId")) {
				firstDepartmentId = Integer.valueOf(String.valueOf(employeeMap.get("firstDepartmentId")));
			}
			if(null != employeeMap.get("firstDepartmentName")) {
				firstDepartmentName = String.valueOf(employeeMap.get("firstDepartmentName"));
			}
			
			/*
			 * 向前端返回用户信息
			 */
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("empName", username);
			resultMap.put("firstDepartmentId", firstDepartmentId);
			resultMap.put("firstDepartmentName", firstDepartmentName);
			return RestUtils.returnSuccess(resultMap);
		}catch (Exception e) {
			logger.error("根据当前登陆用户 生成月度绩效表单信息 方法showTableContent 出现异常 ："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致查询失败！");
		}
	}
	
	/**
	 * Description: 根据一级部门查询二级部门月度绩效记录
	 * 
	 * @param  param
	 * @param  token
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月30日 下午17:27:25
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RestResponse findSecondErpMonthPerformance(Map<String, Object> param,String token) {
		logger.info("进入findSecondErpMonthPerformance方法，参数是：param="+param+",token="+token);
		Map<String, Object> resultList = new HashMap<>(); //返回结果
		try {
			//接收前端传递过来的参数
			Integer firstDepartmentId = Integer.valueOf(String.valueOf(param.get("departmentId")));//一级部门ID
			String erpMonthNum = String.valueOf(param.get("erpMonthNum"));//月份
			
			//查询该一级部门的月度绩效状态erpMonthNum
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("firstdepartmentId", firstDepartmentId);
			queryMap.put("month", erpMonthNum);
			String status = erpMonthPerformanceMapper.findFirstDepartmentMonthPerStatus(queryMap);
			
			List<Map<String, Object>> secondDepList = new ArrayList<>(); //返回结果
			if ("4".equals(status) || "3".equals(status) || "2".equals(status)){
				//绩效为已归档、已导出、已经提交状态，直接查询绩效表获取数据
				secondDepList = this.findSecondMonthPerformaFromTable(param, token);
			}else{
				//通过hr查询当前所有员工
				secondDepList = this.findSecondMonthPerformaFromHr(param, token);
			}
			
			if( status== null ||status.equals("") || status.equals("null")) {
				status = "1";
			}
			resultList.put("status", status);
			resultList.put("secondDepList", secondDepList);
			return RestUtils.returnSuccess(resultList);
		}catch (Exception e) {
			logger.error("根据当前登陆用户 生成月度绩效表单信息 方法showTableContent 出现异常 ："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致查询失败！");
		}
		
	}
	
	public List<Map<String, Object>> findSecondMonthPerformaFromTable(Map<String, Object> param,String token) {
		logger.info("进入findSecondMonthPerformaFromTable方法，参数是：param="+param+",token="+token);
		List<Map<String, Object>> secondDepList = new ArrayList<>(); //返回结果
		try {
			//接收前端传递过来的参数
			Integer firstDepartmentId = Integer.valueOf(String.valueOf(param.get("departmentId")));//一级部门ID
			String erpMonthNum = String.valueOf(param.get("erpMonthNum"));//月份
			
			//查询该一级部门的月度绩效状态
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("erpMonthFirstDepartmentId", firstDepartmentId);
			queryMap.put("erpMonthNum", erpMonthNum);
			
			List<ErpSalaryMonthPerformance> monthPerformanceList = erpMonthPerformanceMapper.findEmpMonthPerformanceMore(queryMap);
			for (ErpSalaryMonthPerformance monthPerformance : monthPerformanceList){
				Map<String,Object> secondDepMap = null;
				Integer departmentId = monthPerformance.getErpMonthSecondDepartmentId();
				//查看二级部门是否在列表中
				for (Map<String, Object> secondDep : secondDepList){
					if (secondDep.get("secondDepartmentId").equals(departmentId)){
						secondDepMap = secondDep;
						break;
					}
				}
				if (secondDepMap == null){
					//二级部门不在已有列表中
					secondDepMap = new HashMap<>();					
					//查询部门名称
					Map<String,Object> departmentInfo = (Map<String,Object>) redisTemplate.opsForValue().get("department_"+departmentId);
					secondDepMap.put("secondDepartmentName", (String)departmentInfo.get("departmentName"));
					secondDepMap.put("secondDepartmentId", departmentId);
					secondDepMap.put("employeeList", new ArrayList<>());
					
					secondDepMap.put("secDepMonthMeritPayTotal", 0.00);		//二级部门工资绩效总和
					secondDepMap.put("secDepMonthProjectPayTotal", 0.00);	//二级部门项目绩效总和

					secondDepList.add(secondDepMap);
				}
				List<Map<String, Object>> employeeList = (List<Map<String, Object>>) secondDepMap.get("employeeList");
				Integer employeeId = monthPerformance.getErpMonthEmpId();
				//查询员工信息
				Map<String,Object> employeeInfo = (Map<String,Object>) redisTemplate.opsForValue().get("employee_"+employeeId);
				employeeInfo.put("erpMonthNum", erpMonthNum);
				employeeInfo.put("erpMonthBeliel", monthPerformance.getErpMonthBeliel());
				employeeInfo.put("erpMonthProjectPayContent", monthPerformance.getErpMonthProjectPayContent());
				employeeInfo.put("erpMonthRemark", monthPerformance.getErpMonthRemark());
				
				//解密数据
				Map<String,String> encryptedPerformanceData = new HashMap<>();
				//解密绩效数据
				encryptedPerformanceData.put("erpMonthMeritPay", monthPerformance.getErpMonthMeritPay());
				encryptedPerformanceData.put("erpMonthProjectPay", monthPerformance.getErpMonthProjectPay());
				encryptedPerformanceData.put("erpMonthDPay", monthPerformance.getErpMonthDPay());
				encryptedPerformanceData.put("erpMonthAllowance", monthPerformance.getErpMonthAllowance());
				Map<String,Double> decryptedPerformanceData = this.decryptPerformanceDataAes(encryptedPerformanceData);
				
				DecimalFormat df = new DecimalFormat("#0.00");
				employeeInfo.put("erpMonthMeritPay", df.format(decryptedPerformanceData.get("erpMonthMeritPay")));//工资绩效
				employeeInfo.put("erpMonthProjectPay", df.format(decryptedPerformanceData.get("erpMonthProjectPay")));//项目绩效
				employeeInfo.put("erpMonthDPay", decryptedPerformanceData.get("erpMonthDPay")); //D类工资
				employeeInfo.put("erpMonthAllowance", df.format(decryptedPerformanceData.get("erpMonthAllowance")));//月度补助
			
				
				//增加显示 项目绩效说明 和 其它备注
				String temp = monthPerformance.getErpMonthProjectPayContent() == null ? "" : String.valueOf(monthPerformance.getErpMonthProjectPayContent());
				encryptedPerformanceData.put("erpMonthProjectPayContent", temp);
				
				temp = monthPerformance.getErpMonthRemark() == null ? "" : String.valueOf(monthPerformance.getErpMonthRemark());
				encryptedPerformanceData.put("erpMonthRemark", temp);
				
				secondDepMap.put("secDepMonthMeritPayTotal", Double.parseDouble(employeeInfo.get("erpMonthMeritPay").toString()) + Double.parseDouble(secondDepMap.get("secDepMonthMeritPayTotal").toString()));		//二级部门工资绩效总和
				secondDepMap.put("secDepMonthProjectPayTotal", Double.parseDouble(employeeInfo.get("erpMonthProjectPay").toString()) +  Double.parseDouble(secondDepMap.get("secDepMonthProjectPayTotal").toString()));	//二级部门项目绩效总和

				employeeList.add(employeeInfo);
			}
		}catch (Exception e) {
			logger.error("findSecondMonthPerformaFromTable出现异常 ："+e.getMessage(),e);
		}
		return secondDepList;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String, Object>> findSecondMonthPerformaFromHr(Map<String, Object> param,String token) {
		logger.info("进入findSecondMonthPerformaFromHr方法，参数是：param="+param+",token="+token);
		List<Map<String, Object>> secondDepList = new ArrayList<>(); //返回结果
		try {
			//接收前端传递过来的参数
			Integer firstDepartmentId = Integer.valueOf(String.valueOf(param.get("departmentId")));//一级部门ID
			String erpMonthNum = String.valueOf(param.get("erpMonthNum"));//月份
			
			/*
			 * 调用ERP-人力资源工程-获取当前登录人权限内的所有部门及员工基本信息
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			
			/*
			 * 封装参数，调用ERP-人力资源 工程 的操作层服务接口，根据二级部门ID获取员工部门信息
			 */
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/department/findAllEmployeeByDepartmentAndMonth";
			Map<String,Object> requestBody  = new HashMap<>();
			requestBody.put("firstDepartmentId", firstDepartmentId);
			requestBody.put("month", erpMonthNum);
			
        	HttpHeaders requestHeaders = new HttpHeaders();
        	requestHeaders.add("token",token);//将token放到请求头中
        	HttpEntity<Map<String,Object>> request = new HttpEntity<>(requestBody, requestHeaders);
        	
        	ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
        	if(response.getStatusCodeValue()!=200 || !"200".equals(response.getBody().get("status"))){
        		logger.error("调用HR工程发生异常！");
        		return secondDepList;
        	}			

			List<Map<String,Object>> secondDepEmpInfoList = (List<Map<String, Object>>) response.getBody().get("data");
			logger.info("secondDepEmpInfoList="+secondDepEmpInfoList);
        	
        	//跨工程查询当月的所有考勤数据
        	Integer workday = 0;
			String url2 = protocolType+"nantian-erp-project/nantian-erp/project/monthAttendance/getAllMonthAttendanceByMonth";
			Map<String,Object> requestBodys  = new HashMap<>();
			requestBodys.put("month", erpMonthNum);
        	HttpHeaders requestHeader = new HttpHeaders();
        	requestHeader.add("token",token);
        	HttpEntity<Map<String,Object>> requests = new HttpEntity<>(requestBodys, requestHeader);
        	ResponseEntity<Map> responses = this.restTemplate.postForEntity(url2, requests, Map.class);
        	
        	List<Map<String,Object>> empMonthWorkday = null;
        	if(responses.getStatusCodeValue()!=200 || !"200".equals(responses.getBody().get("status"))){
        		logger.error("调用PROJECT工程发生异常！");
        	}
        	else{
	        	empMonthWorkday =(List<Map<String, Object>>) responses.getBody().get("data");
			}
			
			for (Map<String,Object> secondDep : secondDepEmpInfoList) {		
				List<Map<String, Object>> employeeList = (List<Map<String, Object>>) secondDep.get("employeeList");					
				for (Map<String, Object> employee : employeeList){
					employee.put("employeeName", employee.get("name"));
					Map<String,Object> empMonthPerformanceData = this.findEmpMonthPerformanceData(employee,erpMonthNum,empMonthWorkday);
					if (empMonthPerformanceData != null){
						employee.putAll(empMonthPerformanceData);
					}
				}
			}			
			return secondDepEmpInfoList;			
		} catch (Exception e) {
			logger.error("方法findSecondErpMonthPerformance出现异常："+e.getMessage(),e);
			return null;
		}
	}
	
	/**
	 * Description: 查询员工月度绩效相关信息
	 * 
	 * @param  secondDepartmentId
	 * @param  erpMonthNum
	 * @param  token
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月02日 下午17:21:55
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String,Object> findEmpMonthPerformanceData(Map<String, Object> employee,String erpMonthNum,List<Map<String,Object>> attendanceData) {
		logger.info("进入findEmpMonthPerformanceData方法，参数是：employee="+employee+",erpMonthNum="+erpMonthNum);
		try {
				Integer employeeId = (Integer) employee.get("employeeId");
			    Map<String,Object> EmpMonthPerformanceData = new HashMap<>();
				Map<String, Object> param = new HashMap<>();
				param.put("erpMonthEmpId", employeeId);
				param.put("erpMonthNum", erpMonthNum);
				ErpSalaryMonthPerformance erpSalaryMonthPerformance = erpMonthPerformanceMapper.findEmpMonthPerformanceDetail(param);
				if(null != erpSalaryMonthPerformance) {					
					Map<String,String> encryptedPerformanceData = new HashMap<>();
					//解密绩效数据
					encryptedPerformanceData.put("erpMonthMeritPay", erpSalaryMonthPerformance.getErpMonthMeritPay());
					encryptedPerformanceData.put("erpMonthProjectPay", erpSalaryMonthPerformance.getErpMonthProjectPay());
					encryptedPerformanceData.put("erpMonthDPay", erpSalaryMonthPerformance.getErpMonthDPay());
					encryptedPerformanceData.put("erpMonthAllowance", erpSalaryMonthPerformance.getErpMonthAllowance());
					Map<String,Double> decryptedPerformanceData = this.decryptPerformanceDataAes(encryptedPerformanceData);
					
					DecimalFormat df = new DecimalFormat("#0.00");
					EmpMonthPerformanceData.put("erpMonthId", erpSalaryMonthPerformance.getErpMonthId());//月度绩效Id
					
					//SXG 2019-09-02  解决  Cannot format given Object as a Number问题
					if (erpSalaryMonthPerformance == null || erpSalaryMonthPerformance.getErpMonthBeliel() == null) {
						EmpMonthPerformanceData.put("erpMonthBeliel", "0.00");//比例
					}else {
						EmpMonthPerformanceData.put("erpMonthBeliel", df.format(erpSalaryMonthPerformance.getErpMonthBeliel()));//比例
					}
//					EmpMonthPerformanceData.put("erpMonthBeliel", df.format(erpSalaryMonthPerformance.getErpMonthBeliel()));//比例
					EmpMonthPerformanceData.put("erpMonthMeritPay", df.format(decryptedPerformanceData.get("erpMonthMeritPay")));//工资绩效
					EmpMonthPerformanceData.put("erpMonthProjectPay", df.format(decryptedPerformanceData.get("erpMonthProjectPay")));//项目绩效
					EmpMonthPerformanceData.put("erpMonthDPay", decryptedPerformanceData.get("erpMonthDPay")); //D类工资
					EmpMonthPerformanceData.put("erpMonthProjectPayContent", erpSalaryMonthPerformance.getErpMonthProjectPayContent());//项目绩效说明
					EmpMonthPerformanceData.put("erpMonthRemark", erpSalaryMonthPerformance.getErpMonthRemark());//其他备注
					EmpMonthPerformanceData.put("erpMonthAllowance", df.format(decryptedPerformanceData.get("erpMonthAllowance")));//月度补助

				}else {
					Map<String,Object> params = new HashMap<>();
					params.put("erpMonthNum", erpMonthNum);					
					params.put("employeeId", employee.get("employeeId"));
					params.put("firstDepartment", employee.get("firstDepartment"));
					params.put("secondDepartment", employee.get("secondDepartment"));
					params.put("beginTime", employee.get("entryTime"));
					params.put("probationEndTime", employee.get("probationEndTime"));
					params.put("dimissionTime", employee.get("dimissionTime"));
					
					EmpMonthPerformanceData = this.calcMonthPerformance(params, attendanceData);
				}

//			logger.info("EmpMonthPerformanceData="+EmpMonthPerformanceData);
			return EmpMonthPerformanceData;
		} catch (Exception e) {
			logger.error("findEmpMonthPerformanceData方法出现异常："+e.getMessage(),e);
			return null;
		}
	}

	/**
	 * Description: 创建月度绩效
	 * 
	 * @param firstDepartmentInfo 月度绩效记录
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月04日 上午10:14:20
	 */
	@SuppressWarnings({ "unchecked" })
	@Transactional
	public RestResponse createErpMonthPerformance(Map<String, Object> firstDepartmentInfo,String token) throws Exception{
		logger.info("进入createErpMonthPerformance方法，参数是： firstDepartmentInfo="+firstDepartmentInfo+",token="+token);
//		try {
			ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
			
			Boolean isCommit = Boolean.valueOf(String.valueOf(firstDepartmentInfo.get("submitType")));//true是提交，false是保存
			String erpMonthNum = String.valueOf(firstDepartmentInfo.get("erpMonthNum"));//月度
			Integer erpMonthFirstDepartmentId = Integer.valueOf(String.valueOf(firstDepartmentInfo.get("erpMonthFirstDepartmentId")));//一级部门Id
			
			//1-1 Plus 判断选择的日期是否为当前月份或者上一个月份
			String submitType = isCommit == true ? "提交" : "保存"; 
			Map<String, Object> alert = new HashMap<String, Object>();	//提示信息
			if(!this.isPreOrCurMonth(this.stringToDate(erpMonthNum + "-01"))) {
				alert.put("Message", "请" + submitType + "当前月份或者上个月份!");
				return RestUtils.returnSuccess(alert, "OK");
			}
			
			Map<String,Object> monthPerformanceApply = new  HashMap<>();
			monthPerformanceApply.put("firstdepartmentId", erpMonthFirstDepartmentId);
			monthPerformanceApply.put("month", erpMonthNum);
			String currentApply = erpMonthPerformanceMapper.findFirstDepartmentMonthPerStatus(monthPerformanceApply);	
			
			if(currentApply != null && currentApply.equals("3")) {
				return RestUtils.returnSuccessWithString("月度绩效已经被锁定，无法" + submitType);
			}
			if(currentApply != null && currentApply.equals("4")) {
				return RestUtils.returnSuccessWithString("月度绩效已经被导出，无法" + submitType);
			}
			
			
			//所有二级部门信息及员工绩效记录
			List<Map<String, Object>> secondDepartmentInfoList = (List<Map<String, Object>>) firstDepartmentInfo.get("secondDepartmentInfo");
			if(null == secondDepartmentInfoList || secondDepartmentInfoList.size() == 0) {
				return RestUtils.returnSuccessWithString("该一级部门下没有员工绩效记录！");
			}
			List<Integer> employeeIdList = new ArrayList<>();
			//获取本月各员工出勤信息
			Map<String, Object> employeeMonthAttendance = new HashMap<>();
			for (Map<String, Object> secondDepartmentInfo : secondDepartmentInfoList) {
				//每个二级部门下员工的月度绩效记录
				List<Map<String, Object>> employeeList = (List<Map<String, Object>>) secondDepartmentInfo.get("employeeList");
				for (Map<String, Object> employee : employeeList) {
					employeeIdList.add(Integer.valueOf(String.valueOf(employee.get("employeeId"))));
				}
			}
			if(employeeIdList.size() > 0){
				//调用project获取本月各员工考勤信息
				String url = protocolType + "nantian-erp-project/nantian-erp/project/monthAttendance/queryEmployeeMonthAttendance";
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.add("token", token);
				Map<String, Object> queryMap = new HashMap<>();
				queryMap.put("month", erpMonthNum);
				queryMap.put("employeeIdList", employeeIdList);
				HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(queryMap,
						requestHeaders);
				ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request,
						RestResponse.class);
				RestResponse response = responseEntity.getBody();
				if (!"200".equals(response.getStatus())) {
					logger.error("createErpMonthPerformance调用项目工程方法异常:" + response.getMsg());
					return RestUtils.returnFailure("调用项目工程获取各员工出勤信息失败" + response.getMsg());
				}
				// 解析返回结果
				employeeMonthAttendance = (Map<String, Object>)response.getData();
			}
			for (Map<String, Object> secondDepartmentInfo : secondDepartmentInfoList) {
				//每个二级部门下员工的月度绩效记录
				List<Map<String, Object>> employeeList = (List<Map<String, Object>>) secondDepartmentInfo.get("employeeList");
				for (Map<String, Object> employee : employeeList) {
					/*
					 * 整合一个员工的信息、月度绩效信息
					 * 通过员工Id查询有没有基本工资，如果有就将月度津贴作为工资绩效、D类工资
					 */
					Integer erpMonthSecondDepartmentId = null;//二级部门Id
					if(employee.get("secondDepartment") != null && !(String.valueOf(employee.get("secondDepartment")).equals("null"))
							&& !(String.valueOf(employee.get("secondDepartment")).equals(""))) {
						erpMonthSecondDepartmentId = Integer.valueOf(String.valueOf(employee.get("secondDepartment")));//二级部门Id
					}
					Integer erpMonthEmpId = Integer.valueOf(String.valueOf(employee.get("employeeId")));//员工Id
					

					ErpBasePayroll erpBasePayroll = erpBasePayrollMapper.findBasePayrollDetailByEmpId(erpMonthEmpId);

					String erpMonthProjectPay = employee.get("erpMonthProjectPay") == null 
							||  String.valueOf(employee.get("erpMonthProjectPay")).equals("null")
							||  String.valueOf(employee.get("erpMonthProjectPay")).equals("")?
							"" : String.valueOf(employee.get("erpMonthProjectPay"));//项目绩效
					Double erpMonthBeliel = employee.get("erpMonthBeliel") == null 
							||  String.valueOf(employee.get("erpMonthBeliel")).equals("null")
							||  String.valueOf(employee.get("erpMonthBeliel")).equals("")? 
							0.00 : Double.valueOf(String.valueOf(employee.get("erpMonthBeliel")));//比例
					String erpMonthMeritPay = employee.get("erpMonthMeritPay") == null
							||  String.valueOf(employee.get("erpMonthMeritPay")).equals("null")
							||  String.valueOf(employee.get("erpMonthMeritPay")).equals("")? 
							"" : String.valueOf(employee.get("erpMonthMeritPay"));//工资绩效
		        	String erpMonthDPay = employee.get("erpMonthDPay") == null
		        			||  String.valueOf(employee.get("erpMonthDPay")).equals("null")
							||  String.valueOf(employee.get("erpMonthDPay")).equals("")? 
		        			"" : String.valueOf(employee.get("erpMonthDPay"));//D类工资
		        	String erpMonthAllowance = employee.get("erpMonthAllowance") == null
		        			||  String.valueOf(employee.get("erpMonthAllowance")).equals("null")
							||  String.valueOf(employee.get("erpMonthAllowance")).equals("")? 
		        			"" : String.valueOf(employee.get("erpMonthAllowance"));//月度补助
					String erpMonthProjectPayContent = "";//项目津贴说明
					if(employee.get("erpMonthProjectPayContent") != null 
							&& !(String.valueOf(employee.get("erpMonthProjectPayContent")).equals("null"))) {
						erpMonthProjectPayContent = String.valueOf(employee.get("erpMonthProjectPayContent"));
					}
					String erpMonthRemark = null;//备注
					if(employee.get("erpMonthRemark")!=null 
							&& !(String.valueOf(employee.get("erpMonthRemark")).equals("null"))) {
						erpMonthRemark = String.valueOf(employee.get("erpMonthRemark"));
					}
					/*
					 * 将薪酬数据加密
					 */
					Map<String,String> preformanceDataMap = new  HashMap<>();
					preformanceDataMap.put("erpMonthMeritPay", erpMonthMeritPay);
					preformanceDataMap.put("erpMonthProjectPay", erpMonthProjectPay);
					preformanceDataMap.put("erpMonthDPay", erpMonthDPay);
					preformanceDataMap.put("erpMonthAllowance", erpMonthAllowance);
					Map<String,String> encryptedPreformanceData = this.encryptPerformanceDataAes(preformanceDataMap);
					erpMonthMeritPay = encryptedPreformanceData.get("erpMonthMeritPay");
					erpMonthProjectPay = encryptedPreformanceData.get("erpMonthProjectPay");
					erpMonthDPay = encryptedPreformanceData.get("erpMonthDPay");
					erpMonthAllowance = encryptedPreformanceData.get("erpMonthAllowance");
					/*
					 * 创建新对象，整合PO，新增数据库
					 */
					ErpSalaryMonthPerformance erpSalaryMonthPerformance = new ErpSalaryMonthPerformance();
					erpSalaryMonthPerformance.setErpMonthNum(erpMonthNum);
					erpSalaryMonthPerformance.setErpMonthFirstDepartmentId(erpMonthFirstDepartmentId);
					erpSalaryMonthPerformance.setErpMonthSecondDepartmentId(erpMonthSecondDepartmentId);
					erpSalaryMonthPerformance.setErpMonthEmpId(erpMonthEmpId);
					erpSalaryMonthPerformance.setErpMonthBeliel(erpMonthBeliel);
					erpSalaryMonthPerformance.setErpMonthMeritPay(erpMonthMeritPay);
					erpSalaryMonthPerformance.setErpMonthProjectPay(erpMonthProjectPay);
					erpSalaryMonthPerformance.setErpMonthDPay(erpMonthDPay);
					erpSalaryMonthPerformance.setErpMonthProjectPayContent(erpMonthProjectPayContent);
					erpSalaryMonthPerformance.setErpMonthRemark(erpMonthRemark);
					erpSalaryMonthPerformance.setErpMonthAllowance(erpMonthAllowance);
					if(erpBasePayroll!=null){
						erpSalaryMonthPerformance.setErpMonthBaseWage(erpBasePayroll.getErpBaseWage());
						erpSalaryMonthPerformance.setErpMonthPostWage(erpBasePayroll.getErpPostWage());
						erpSalaryMonthPerformance.setErpMonthTelSubsidy(erpBasePayroll.getErpTelFarePerquisite());
					}
					Map<String, Object> monthAttendanceMap = (Map)employeeMonthAttendance.get(String.valueOf(erpMonthEmpId));
					if(monthAttendanceMap != null && monthAttendanceMap.get("actualWorkDays") != null){
						BigDecimal actualWorkDays = new BigDecimal(String.valueOf(monthAttendanceMap.get("actualWorkDays")));
						erpSalaryMonthPerformance.setErpMonthActualWorkDays(actualWorkDays);
					}
					
					/*
					 * 通过员工Id和月份查询月度绩效表有没有一条记录，来决定是新增？还是更新？
					 */
					Map<String, Object> param = new HashMap<>();
					param.put("erpMonthEmpId", employee.get("employeeId"));
					param.put("erpMonthNum", erpMonthNum);
					ErpSalaryMonthPerformance validNullResult = this.erpMonthPerformanceMapper.findEmpMonthPerformanceDetail(param);
					/*
					 * 如果该员工在本月有月度绩效，则更新；如果没有，则新增
					 */
					if(null == validNullResult) {
						erpMonthPerformanceMapper.createErpMonthPerformance(erpSalaryMonthPerformance);
					}else {
						erpSalaryMonthPerformance.setErpMonthId(validNullResult.getErpMonthId());
						erpMonthPerformanceMapper.updateErpMonthPerformance(erpSalaryMonthPerformance);
					}
				}
			}
			
			//更新月度绩效状态表			
			if(isCommit) {
				monthPerformanceApply.put("status","2");
				monthPerformanceApply.put("submitPersonId",userInfo.getUserId());
			}else {
				monthPerformanceApply.put("status","1");
			}
			
			if (currentApply == null){
				monthPerformanceApply.put("createTime",ExDateUtils.getCurrentDateTime());
				monthPerformanceApply.put("modifiedTime",ExDateUtils.getCurrentDateTime());
				erpMonthPerformanceMapper.insertFirstDepartmentMonthPerStatus(monthPerformanceApply);
			}else{
				if (currentApply.equals("1")){
					monthPerformanceApply.put("modifiedTime",ExDateUtils.getCurrentDateTime());
					//当前是未提交状态，按照最新状态修改，如果是已提交，则不更改状态
					erpMonthPerformanceMapper.updateFirstDepartmentMonthPerStatus(monthPerformanceApply);
				}else if(currentApply.equals("2")) {
					//已经提交过->再保存，状态不改变
					monthPerformanceApply.put("status",currentApply);
					monthPerformanceApply.put("modifiedTime",ExDateUtils.getCurrentDateTime());
					erpMonthPerformanceMapper.updateFirstDepartmentMonthPerStatus(monthPerformanceApply);
				}
			}
			
			/*
			 * 增加月度绩效的新增记录
			 */
			//从前端传递过来的参数中获取一级部门Id
			Integer firstDepartmentId = Integer.valueOf(String.valueOf(firstDepartmentInfo.get("erpMonthFirstDepartmentId")));
			//通过token从redis缓存中获取用户信息
			String employeeName = userInfo.getEmployeeName();//员工姓名
			String username = userInfo.getUsername();//用户名
			String processor = employeeName==null||"".equals(employeeName)?username:employeeName;//新增月度绩效的操作人
			ErpSalaryMonthPerformanceRecord salaryMonthPerformanceRecord = new ErpSalaryMonthPerformanceRecord();
			salaryMonthPerformanceRecord.setFirstDepartmentId(firstDepartmentId);
			salaryMonthPerformanceRecord.setMonth(erpMonthNum);
			salaryMonthPerformanceRecord.setProcessor(processor);
			salaryMonthPerformanceRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			String content = "";
			if(isCommit) {
				content = "提交月度绩效";
			}else {
				content = "保存月度绩效";
			}
			salaryMonthPerformanceRecord.setContent(content);
			salaryMonthPerformanceRecordMapper.insertRecord(salaryMonthPerformanceRecord);
			
			return RestUtils.returnSuccessWithString("OK");
//		} catch (Exception e) {
//			logger.error("创建月度绩效方法 createErpMonthPerformance 出现异常 ："+e.getMessage(),e);
//			return RestUtils.returnFailure("发生异常，导致创建月度绩效失败！");
//		}
	}
	
	/**
	 * Description: 月度绩效列表导出
	 * 
	 * @param  erpMonthNum 月度
	 * @param  token
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月03日 下午14:34:25
	 * @Update Date: 2019年06月06日
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RestResponse exportMonthPerformance(String token,@RequestParam String erpMonthNum) {
		logger.info("进入exportMonthPerformance方法，参数是：token="+token+",erpMonthNum="+erpMonthNum);
		/*
		 * 调用ERP-人力资源工程-获取当前登录人权限内的所有部门及员工基本信息
		 */
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
		Integer id = erpUser.getUserId();//用户Id
		String employeeName = erpUser.getEmployeeName();//从用户信息中获取员工姓名
		String username = erpUser.getUsername();//从用户信息中获取用户名
		List<Integer> roles = erpUser.getRoles();//从用户信息中获取角色信息
		logger.info("id="+id+",username="+username+",roles="+roles);
		//调用ERP-人力资源 工程 的操作层服务接口-获取一级部门下面所有二级部门的员工的详细信息
		String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoOfAllFirDepByParams";
		Map<String,Object> departmentParams = new HashMap<>();
		/*
		 * 判断当前登录用户的角色查看数据的权限
		 */
		if(roles.contains(8)) {//总经理
			departmentParams.put("userId", null);
			departmentParams.put("superLeader", null);
		}else if(roles.contains(9)) {//副总经理
			departmentParams.put("userId", null);
			departmentParams.put("superLeader", id);
		}else {//一级部门经理、其他角色
			departmentParams.put("userId", id);
			departmentParams.put("superLeader", null);
		}
		//queryMode查询模式（0或者不传参数：查询在职员工+离职员工，1：查询在职员工，2：查询在职员工+指定时间段的离职员工）
		departmentParams.put("queryMode", "2");
		
		//获取当月月度，如2018-12
		Calendar dateObj = Calendar.getInstance();
		int year = dateObj.get(Calendar.YEAR);
		int month = dateObj.get(Calendar.MONTH)+1;
		int day = dateObj.get(Calendar.DATE);
		String currentDate = null;//月度
		if(month<10) {
			currentDate = year+"-0"+month;
		}else {
			currentDate = year+"-"+month;
		}
		if(day<10) {
			currentDate += "-0"+day;
		}else {
			currentDate += "-"+day;
		}
		//上一年的12月1号
		String historyDate = (year-1)+"-12-01";
		departmentParams.put("dimissionTimeStart", historyDate);//离职时间开始
		departmentParams.put("dimissionTimeEnd", currentDate);//离职时间结束
		
		HttpHeaders requestHeaders=new HttpHeaders();
		requestHeaders.add("token", token);//将token放到请求头中
		HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<>(departmentParams, requestHeaders);
		ResponseEntity<Map> response = this.restTemplate.postForEntity(url, requestEntity, Map.class);
		if(200 != response.getStatusCodeValue()) {
			return RestUtils.returnFailure("调用人力资源工程失败！");
		}
		//解析获取的数据
		Map<String,Object> responseBody = response.getBody();
		if(!"200".equals(responseBody.get("status"))) {
			return RestUtils.returnFailure("人力资源工程发生异常！");
		}
		List<List<List<Map<String, Object>>>> firDepEmpInfoList = (List<List<List<Map<String, Object>>>>) responseBody.get("data");
		
		List<Map<String, Object>> monthPerformanceList = new ArrayList();//最后封装结果
		List<Integer> firstDepartmentIds = new ArrayList<>();//导出的一级部门Id集合
		
		//权限内所有一级部门员工的信息和薪酬情况
		for (List<List<Map<String, Object>>> secDepEmpInfoList : firDepEmpInfoList) {
			logger.info("一级部门下的二级部门总数："+secDepEmpInfoList.size());
			//一级部门下所有二级部门员工的信息和薪酬情况
			for (List<Map<String, Object>> empInfoList : secDepEmpInfoList) {
				logger.info("二级部门下的员工总数："+empInfoList.size());
				/*
				 * 一个二级部门下所有员工的信息和薪酬情况（所有员工绩效记录和一级部门信息）
				 */
				for (Map<String, Object> employee : empInfoList) {
					Map<String, Object> monthPerformance = new HashMap<>();
					Integer erpMonthEmpId = Integer.valueOf(String.valueOf(employee.get("employeeId")));//员工ID
					Integer firstDepartmentId = Integer.valueOf(String.valueOf(employee.get("firstDepartment")));//一级部门ID
 					firstDepartmentIds.add(firstDepartmentId);//将一级部门加入到数组中，方便后续增加月度绩效的导出记录
					//查询所有的员工薪酬记录
					Map<String, Object> requestParam = new HashMap<>();
					requestParam.put("erpMonthNum", erpMonthNum);
					requestParam.put("erpMonthEmpId", erpMonthEmpId);
					ErpSalaryMonthPerformance erpSalaryMonthPerformance = erpSalaryMonthPerformanceMapper.findEmpMonthPerformanceDetail(requestParam);
					if(null !=erpSalaryMonthPerformance) {
						/*
						 * 将数据库中加密后的薪酬信息解密
						 */
						Map<String,String> encryptedPerformanceData = new HashMap<>();
						encryptedPerformanceData.put("erpMonthMeritPay", erpSalaryMonthPerformance.getErpMonthMeritPay());
						encryptedPerformanceData.put("erpMonthProjectPay", erpSalaryMonthPerformance.getErpMonthProjectPay());
						encryptedPerformanceData.put("erpMonthDPay", erpSalaryMonthPerformance.getErpMonthDPay());
						Map<String,Double> decryptedPerformanceData = this.decryptPerformanceDataAes(encryptedPerformanceData);
						Double erpMonthMeritPay = decryptedPerformanceData.get("erpMonthMeritPay");//工资绩效
						Double erpMonthProjectPay = decryptedPerformanceData.get("erpMonthProjectPay");//项目绩效
						
						monthPerformance.put("erpMonthBeliel", erpSalaryMonthPerformance.getErpMonthBeliel());//比例
						monthPerformance.put("erpMonthProjectPay", erpMonthProjectPay);//项目绩效
						monthPerformance.put("erpMonthMeritPay", erpMonthMeritPay);//工资绩效
						monthPerformance.put("erpMonthProjectPayContent", erpSalaryMonthPerformance.getErpMonthProjectPayContent()
								==null?"":erpSalaryMonthPerformance.getErpMonthProjectPayContent());//项目绩效说明
						monthPerformance.put("erpMonthRemark", erpSalaryMonthPerformance.getErpMonthRemark()
								==null?"":erpSalaryMonthPerformance.getErpMonthRemark());//其他备注
					}else {
						monthPerformance.put("erpMonthBeliel", "");//比例
						monthPerformance.put("erpMonthProjectPay", "");//项目绩效
						monthPerformance.put("erpMonthMeritPay", "");//工资绩效
						monthPerformance.put("erpMonthProjectPayContent", "");//项目绩效说明
						monthPerformance.put("erpMonthRemark", "");//其他备注
					}
					
					//查询员工财务序号
					ErpEmpFinanceNumber empFinanceNumber = empFinanceNumberMapper.findEmpFinanceNumberDetailByEmpId(erpMonthEmpId);
					monthPerformance.put("empFinanceNumber", empFinanceNumber==null||empFinanceNumber.getEmpFinanceNumber()==null?"":empFinanceNumber.getEmpFinanceNumber());//员工财务序号
					monthPerformance.put("empName", employee.get("name")==null?"":String.valueOf(employee.get("name")));//员工姓名
					monthPerformance.put("sex", employee.get("sex")==null?"":String.valueOf(employee.get("sex")));//性别
					monthPerformance.put("maxDepartmentName", "集成服务集团");//上级部门
					monthPerformance.put("firstDepartmentName", employee.get("firstDepartmentName")==null?"":String.valueOf(employee.get("firstDepartmentName")));//一级部门
					monthPerformance.put("secondDepartmentName", employee.get("secondDepartmentName")==null?"":String.valueOf(employee.get("secondDepartmentName")));//二级部门
					monthPerformance.put("takeJobTime", employee.get("takeJobTime")==null?"":String.valueOf(employee.get("takeJobTime")));//参加工作时间
					
					String status = String.valueOf(employee.get("status"));//员工状态
					switch(status) {
					case DicConstants.EMPLOYEE_STATUS_TRAINEE:
						monthPerformance.put("statusName", "实习生");break;//实习生
					case DicConstants.EMPLOYEE_STATUS_PROBATION:
						monthPerformance.put("statusName", "试用期员工");break;//试用期员工
					case DicConstants.EMPLOYEE_STATUS_FORMAL:
						monthPerformance.put("statusName", "正式员工");break;//正式员工
					case DicConstants.EMPLOYEE_STATUS_DIMISSION_ING:
						monthPerformance.put("statusName", "离职中");break;//离职中
					case DicConstants.EMPLOYEE_STATUS_DIMISSION_ED:
						monthPerformance.put("statusName", "已离职");break;//已离职
					default:
						monthPerformance.put("statusName", "");break;// 默认空值
					}
					/*
					 * 员工是否在离职中状态  如果是 查询其考勤截止时间
					 */
					monthPerformance.put("dimissionTime", employee.get("dimissionTime")==null?"":String.valueOf(employee.get("dimissionTime")));//离职时间
//					if(DicConstants.EMPLOYEE_STATUS_DIMISSION_ING.equals(status)) {
//						/*
//						 * 调用ERP-人力资源工程-获取所有员工及部门基本信息
//						 */
//						String url1 = protocolType+"nantian-erp-hr/nantian-erp/erp/dimission/findOneById?employeeId="+erpMonthEmpId;
//						String body1 = null;
//						HttpHeaders headers1 = new HttpHeaders();
//						headers1.setContentType(MediaType.APPLICATION_JSON_UTF8);
//						headers1.add("token", token);
//						HttpEntity<String> requestEntity1 = new HttpEntity<String>(body1, headers1);
//						ResponseEntity<Map> response1 = restTemplate.exchange(url1, HttpMethod.GET, requestEntity1, Map.class);
//						if(200 != response1.getStatusCodeValue()) {
//							return RestUtils.returnFailure("调用人力资源工程失败！");
//						}
//						/*
//						 * 解析请求的结果
//						 */
//						Map<String,Object> responseBody1 = response1.getBody();
//						if(!"200".equals(responseBody1.get("status"))) {
//							return RestUtils.returnFailure("人力资源工程发生异常！");
//						}
//						Map<String,Object> dimissionEmployee = (Map<String,Object>) responseBody1.get("data");
//						if(dimissionEmployee.get("dimissionTime")!=null) {
//							monthPerformance.put("dimissionTime", String.valueOf(dimissionEmployee.get("dimissionTime"))); 
//						}
//					}
					monthPerformanceList.add(monthPerformance);
				}
			}
		}
		/*
		 * 增加月度绩效的导出记录
		 */
		String processor = employeeName==null||"".equals(employeeName)?username:employeeName;//导出月度绩效的操作人
		Set<Integer> tempSet = new HashSet<>(firstDepartmentIds);
		firstDepartmentIds.clear();
		firstDepartmentIds.addAll(tempSet);//去重后的一级部门Id集合
		for(Integer firstDepartmentId : firstDepartmentIds) {
			ErpSalaryMonthPerformanceRecord salaryMonthPerformanceRecord = new ErpSalaryMonthPerformanceRecord();
			salaryMonthPerformanceRecord.setFirstDepartmentId(firstDepartmentId);
			salaryMonthPerformanceRecord.setProcessor(processor);
			salaryMonthPerformanceRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			salaryMonthPerformanceRecord.setContent("导出月度绩效");
			salaryMonthPerformanceRecordMapper.insertRecord(salaryMonthPerformanceRecord);
		}
		return this.exportForMonthPerformance(monthPerformanceList);
	}
	
	/**
	 * Description: 导出-方法
	 * 
	 * @param List<Map<String, Object>> resultList
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月03日 下午15:08:54
	 * @Update Date: 2019年06月06日
	 */
	public RestResponse exportForMonthPerformance(List<Map<String, Object>> monthPerformanceList) {
		logger.info("进入exportForMonthPerformance方法，参数是：monthPerformanceList="+monthPerformanceList);
		//InputStream resourceAsStream = this.getClass().getResourceAsStream("/template/月度绩效.xlsx");
		XSSFWorkbook workBook = null;
		try {
			//workBook = new XSSFWorkbook(resourceAsStream); //制成book
			//XSSFSheet sheet = workBook.getSheetAt(0);
			
			workBook = new XSSFWorkbook(); //制成book
			XSSFSheet sheet = workBook.createSheet("员工薪资");
			// 生成第一行
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("财务序号");
			firstRow.createCell(1).setCellValue("姓名");
			firstRow.createCell(2).setCellValue("性别");
			firstRow.createCell(3).setCellValue("一级部门");
			firstRow.createCell(4).setCellValue("二级部门");
			firstRow.createCell(5).setCellValue("三级部门");
			firstRow.createCell(6).setCellValue("比例");
			firstRow.createCell(7).setCellValue("项目绩效");
			firstRow.createCell(8).setCellValue("工资绩效");
			firstRow.createCell(9).setCellValue("项目绩效说明");
			firstRow.createCell(10).setCellValue("其他备注");
			
			firstRow.createCell(11).setCellValue("离职人员考勤截止日期");
			firstRow.createCell(12).setCellValue("离职时所在一级部门");
			firstRow.createCell(13).setCellValue("参加工作时间");
			firstRow.createCell(14).setCellValue("员工状态");
			
			//下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			//循环 填充表格
			for (int i = 0; i < monthPerformanceList.size(); i++) {
				param = monthPerformanceList.get(i);
				nextRow = sheet.createRow(i+1);//从第二行开始 - 表头占据一行
				
				nextRow.createCell(0).setCellValue(param.get("empFinanceNumber")==null?"":String.valueOf(param.get("empFinanceNumber")));//员工月度绩效财务序号
				nextRow.createCell(1).setCellValue(param.get("empName")==null?"":String.valueOf(param.get("empName")));//姓名
				nextRow.createCell(2).setCellValue(param.get("sex")==null?"":String.valueOf(param.get("sex")));//性别
				nextRow.createCell(3).setCellValue(param.get("maxDepartmentName")==null?"":String.valueOf(param.get("maxDepartmentName")));//一级部门
				nextRow.createCell(4).setCellValue(param.get("firstDepartmentName")==null?"":String.valueOf(param.get("firstDepartmentName")));//二级部门
				nextRow.createCell(5).setCellValue(param.get("secondDepartmentName")==null?"":String.valueOf(param.get("secondDepartmentName")));//三级部门
				nextRow.createCell(6).setCellValue(param.get("erpMonthBeliel")==null?"":String.valueOf(param.get("erpMonthBeliel")));//比例
				nextRow.createCell(7).setCellValue(param.get("erpMonthProjectPay")==null?"":String.valueOf(param.get("erpMonthProjectPay")));//项目绩效
				nextRow.createCell(8).setCellValue(param.get("erpMonthMeritPay")==null?"":String.valueOf(param.get("erpMonthMeritPay")));//工资绩效
				nextRow.createCell(9).setCellValue(param.get("erpMonthProjectPayContent")==null?"":String.valueOf(param.get("erpMonthProjectPayContent")));//项目绩效说明
				nextRow.createCell(10).setCellValue(param.get("erpMonthRemark")==null?"":String.valueOf(param.get("erpMonthRemark")));//其他备注
				
				nextRow.createCell(11).setCellValue(param.get("dimissionTime")==null?"":String.valueOf(param.get("dimissionTime")));//离职人员考勤截止日期
				nextRow.createCell(12).setCellValue(param.get("firstDepartmentName")==null?"":String.valueOf(param.get("firstDepartmentName")));//离职时所在一级部门
				nextRow.createCell(13).setCellValue(String.valueOf(param.get("takeJobTime")));//参加工作时间
				nextRow.createCell(14).setCellValue(String.valueOf(param.get("statusName")));//员工状态
			}
			this.exportExcelToComputer(workBook);
			return RestUtils.returnSuccessWithString("导出成功！");
		} catch (Exception e) {
			logger.error("exportForMonthPerformance发生异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致导出失败！");
		}
	}
	
	/**
	 * Description: 通过员工Id和月份计算的月度绩效
	 * 
	 * @Author zhangqian
	 * @Create Date: 2019年5月28日 上午09:59:21
	 */
	@Transactional
	public Map<String,Object> calcMonthPerformance(Map<String,Object> params, List<Map<String,Object>> attendanceData) {
		logger.info("进入calcMonthPerformance方法，参数:"+params);
		Map<String,Object> EmpMonthPerformanceData = new HashMap<>();
		try {
			Integer employeeId = Integer.valueOf(String.valueOf((params.get("employeeId"))));
			String erpMonthNum = (String) params.get("erpMonthNum");
			
			String beginTime = null;
			String probationEndTime = null;
			String dimissionTime = null;
			String erpMonthRemark = "";//备注
			Double erpMonthDPay = 0.0;
			Double erpMonthMeritPay = 0.0;
			Boolean telAllowance = true;
			
			if(params.containsKey("beginTime")){
				beginTime = (String) params.get("beginTime");
			}
			if(params.containsKey("probationEndTime")){
				probationEndTime = (String) params.get("probationEndTime");
			}
			if(params.containsKey("dimissionTime")){
				dimissionTime = (String) params.get("dimissionTime");
			}
			
			/*
			 * 整合一个员工的信息、月度绩效信息
			 * 通过员工Id查询有没有基本工资，如果有就将月度津贴作为工资绩效、D类工资
			 */
			if (dimissionTime != null){
				//判断离职时间是否为当月				
				String[] str = dimissionTime.split("-");
				String dimissionMonth = str[0] + "-" + str[1];				
				if (dimissionMonth.equals(erpMonthNum)) {
					if (!erpMonthRemark.equals("")){
						erpMonthRemark = erpMonthRemark + "\r\n";
					}
					erpMonthRemark = erpMonthRemark + "当月离职，离职日期：" + dimissionTime;
				}
			}
			
			if (beginTime != null){
				//判断入职时间是否为当月				
				String[] str = beginTime.split("-");
				String beginMonth = str[0] + "-" + str[1];
				if (beginMonth.equals(erpMonthNum)){
					if (!erpMonthRemark.equals("")){
						erpMonthRemark = erpMonthRemark + "\r\n";
					}
					erpMonthRemark = erpMonthRemark + "当月入职，入职日期：" + beginTime;
				}
			}
			
			if (probationEndTime != null && probationEndTime.toString() !="null" && probationEndTime.toString() !=""){
				//判断离职时间是否为当月
				String[] str = probationEndTime.split("-");
				String probationMonth = str[0] + "-" + str[1];				

				DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date probationDate = format.parse(probationMonth+"-01"); 
				Date erpMonthDate = format.parse(erpMonthNum+"-01"); 				
				
				if (probationDate.getTime() > erpMonthDate.getTime()){
					//处于试用期，没有话费补贴
					telAllowance = false;
				}				
				
				if (probationMonth.equals(erpMonthNum)){
					if (!erpMonthRemark.equals("")){
						erpMonthRemark = erpMonthRemark + "\r\n";
					}
					erpMonthRemark = erpMonthRemark + "当月转正，转正日期：" + probationEndTime;
					
					//按转正日期计算D类工资
					Double date = Double.valueOf(str[2]);
					String day30[] = {"04","06","09","11"};
					String day31[] = {"01","03","05","07","08","10","12"};
					
					//计算试用期比例
					Double proportion = 0.0;
					if (Arrays.asList(day30).contains(str[1])){
						proportion = date/30;
					}else if (Arrays.asList(day31).contains(str[1])){
						proportion = date/31;
					}else{
						proportion = date/28;
					}
					
					//查询试用期D类工资
					ErpPeriodPayroll periodPayroll = this.erpPeriodPayrollMapper.findPeriodSalary(employeeId);
					//查询转正工资单D类工资
					ErpPositivePayroll positivePayroll = this.erpPositivePayrollMapper.selectOnePositivePayroll(employeeId);
					
					Double period = 0.0;
					if(periodPayroll !=null) {
						String periodallowance = this.erpPositivePayrollService.decryptDataRsa(periodPayroll.getErpPeriodAllowance());
						period = Double.valueOf(String.valueOf(periodallowance));
					}

					Double positive = 0.0;
					if(positivePayroll != null) {
						String positiveallowance = this.erpPositivePayrollService.decryptDataRsa(positivePayroll.getErpPositiveAllowance());
						positive = Double.valueOf(String.valueOf(positiveallowance));
					}
					
					
					erpMonthDPay = period*proportion + positive*(1-proportion);
				}
			}
			
			ErpBasePayroll basePayroll = null;
			if (erpMonthDPay == 0.0){
				//从基础薪资表中获取D类工资
				basePayroll = erpBasePayrollMapper.findBasePayrollDetailByEmpId(employeeId);
				if(basePayroll != null) {
					String allowance = this.erpPositivePayrollService.decryptDataRsa(basePayroll.getErpAllowance());
//					erpMonthDPay = Double.valueOf(Integer.valueOf(allowance));
					erpMonthDPay = Double.valueOf(allowance);
				}
			}
			erpMonthMeritPay = erpMonthDPay;
			
			//查询员工考勤数据
			Double workday = 0.0;
			Double workProject = 0.0;
			Double workCompany = 0.0;						
			for (Map<String,Object> attendance:attendanceData){
				if (employeeId.equals(attendance.get("employeeId"))){
					//查找到员工考勤
					if (attendance.get("workDay") != null) {
						workday = Double.valueOf(String.valueOf(attendance.get("workDay")));
					}
					if (attendance.get("workProject") != null) {
						workProject = Double.valueOf(String.valueOf(attendance.get("workProject")));
					}
					if (attendance.get("workCompany") != null) {
						workCompany = Double.valueOf(String.valueOf(attendance.get("workCompany")));
					}
				}
			}
			Double erpMonthBeliel = workday/21.75;//出勤比例
			Double erpMonthAllowance = 35*workProject + 15*workCompany;//项目：餐补15+交补20，公司：餐补15	
			
			//话费补助
			if (telAllowance){
				if (basePayroll == null){
					basePayroll = erpBasePayrollMapper.findBasePayrollDetailByEmpId(employeeId);
				}
				if(basePayroll != null) {
					String telFarePerquisite = this.erpPositivePayrollService.decryptDataRsa(basePayroll.getErpTelFarePerquisite());
//					erpMonthAllowance += Double.valueOf(Integer.valueOf(telFarePerquisite));
					erpMonthAllowance += Double.valueOf(telFarePerquisite);
				}
			}
						
			DecimalFormat df = new DecimalFormat("#0.00");
			EmpMonthPerformanceData.put("erpMonthBeliel", df.format(erpMonthBeliel));//比例
			EmpMonthPerformanceData.put("erpMonthMeritPay", df.format(erpMonthMeritPay));//工资绩效
			EmpMonthPerformanceData.put("erpMonthProjectPay", 0);//项目绩效
			EmpMonthPerformanceData.put("erpMonthDPay", df.format(erpMonthDPay)); //D类工资
			EmpMonthPerformanceData.put("erpMonthProjectPayContent", "");//项目绩效说明
			EmpMonthPerformanceData.put("erpMonthRemark", erpMonthRemark);//其他备注
			EmpMonthPerformanceData.put("erpMonthAllowance", df.format(erpMonthAllowance));//月度补助
						
			logger.info("calcMonthPerformance执行成功！");
		} catch (Exception e) {
			logger.error("calcMonthPerformance发生异常 ："+e.getMessage(),e);
		}
		return EmpMonthPerformanceData;
	}
	
	/**
	 * Description: 初始化手动创建月度绩效
	 * 1、每月1日0点新建当月月度绩效表，默认比例（1）、D类工资（基本薪资表）、工资绩效（等于D类工资）、项目绩效（0），其他为空
	 * 2、页面上有“初始化月度绩效”按钮，点击之后依然执行该方法
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月12日 上午11:30:51
	 */
	@Transactional
	public RestResponse initializeCreateMonthPerformance(Map<String,Object> param,String token) {
		logger.info("进入initializeCreateMonthPerformance方法，参数是：token="+token);
		try {
			/*
			 * 调用ERP-人力资源工程-获取当前登录人权限内的所有部门及员工基本信息
			 */
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			
			//接收前端传递过来的参数
			Integer firstDepartmentId = Integer.valueOf(String.valueOf(param.get("departmentId")));//一级部门ID
			String erpMonthNum = String.valueOf(param.get("erpMonthNum"));//月份
			
			//删除当前绩效数据，并删除绩效申请
			this.erpSalaryMonthPerformanceMapper.deleteMonthPerformance(param);
			this.erpSalaryMonthPerformanceMapper.deleteMonthPerformanceApply(param);
			
			//增加操作记录
			String employeeName = erpUser.getEmployeeName();//员工姓名
			String username = erpUser.getUsername();//用户名
			String processor = employeeName==null||"".equals(employeeName)?username:employeeName;//新增月度绩效的操作人
			ErpSalaryMonthPerformanceRecord salaryMonthPerformanceRecord = new ErpSalaryMonthPerformanceRecord();
			salaryMonthPerformanceRecord.setFirstDepartmentId(firstDepartmentId);
			salaryMonthPerformanceRecord.setMonth(erpMonthNum);
			salaryMonthPerformanceRecord.setProcessor(processor);
			salaryMonthPerformanceRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			salaryMonthPerformanceRecord.setContent("初始化月度绩效");
			salaryMonthPerformanceRecordMapper.insertRecord(salaryMonthPerformanceRecord);
			/*
			 * 封装参数，调用ERP-人力资源 工程 的操作层服务接口，根据二级部门ID获取员工部门信息
			 */
//			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/department/findAllEmployeeByDepartmentAndMonth";
//			Map<String,Object> requestBody  = new HashMap<>();
//			requestBody.put("firstDepartmentId", firstDepartmentId);
//			requestBody.put("month", erpMonthNum);
//			
//        	HttpHeaders requestHeaders = new HttpHeaders();
//        	requestHeaders.add("token",token);//将token放到请求头中
//        	HttpEntity<Map<String,Object>> request = new HttpEntity<>(requestBody, requestHeaders);
//        	
//        	ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
//        	if(response.getStatusCodeValue()!=200 || !"200".equals(response.getBody().get("status"))){
//        		logger.error("调用HR工程发生异常！");
//        		return RestUtils.returnFailure("调用HR工程发生异常！");
//        	}
//			
//			/*
//			 * 解析请求的结果，解析获取的数据 -一级部门下的二级部门 名字 - 编号
//			 */
//			List<Map<String,Object>> secondDepEmpInfoList = (List<Map<String, Object>>) response.getBody().get("data");
//			logger.info("secondDepEmpInfoList="+secondDepEmpInfoList);
//			
//			List<Map<String, Object>> allEmployeeList = new ArrayList<>();
//			for (Map<String,Object> secondDep : secondDepEmpInfoList) {
//				List<Map<String, Object>> employeeList = (List<Map<String, Object>>) secondDep.get("employeeList");				
//				allEmployeeList.addAll(employeeList);				
//			}
//			String str = this.createMonthPerformance(erpMonthNum, erpUser.getEmployeeName(), allEmployeeList);
			
			logger.info("initializeCreateMonthPerformance执行成功！");
			return RestUtils.returnSuccessWithString("执行成功！");
		} catch (Exception e) {
			logger.error("initializeCreateMonthPerformance发生异常 ："+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致初始化月度绩效失败！");
		}
	}
	
	/**
	 * Description: 根据一级部门展开月度绩效记录
	 * 
	 * @param  firstDepartmentId 一级部门编号
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月22日 下午13:40:04
	 */
	public RestResponse findMonthPerformanceRecord(@RequestBody Map<String,Object> param) {
		logger.info("进入findMonthPerformanceRecord方法，参数是：param="+param);
		try {
			List<ErpSalaryMonthPerformanceRecord> recordList = salaryMonthPerformanceRecordMapper.selectRecordById(param);
			return RestUtils.returnSuccess(recordList);
		} catch (Exception e) {
			logger.error("方法findMonthPerformanceRecord出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致查询失败！");
		}
	}
	
	/**
	 * Description: 锁定
	 * 
	 * @param  firstDepartmentId 一级部门编号
	 * @Author ZhangYuWei
	 * @Create Date: 2019年06月05日
	 */
	public RestResponse changeMonthPerformanceType(Map<String,Object> param,String token) {
		logger.info("进入changeMonthPerformanceType方法，参数是：param="+param);
		try {
			//接收前端传递过来的参数
			String type = (String) param.get("type");//类型
			String monthNum = (String) param.get("monthNum");//月份
			Integer erpMonthFirstDepartmentId = (Integer) param.get("firstDepartmentId");//一级部门ID

			//更新月度绩效状态表
			Map<String,Object> monthPerformanceApply = new  HashMap<>();
			String content = "";
			//1：未提交，2：已提交，3：锁定，4：归档
			switch(type) {
				case "lock":
					monthPerformanceApply.put("status","3");
					content = "锁定月度绩效";
					break;
				case "unlock":
					monthPerformanceApply.put("status","2");
					content = "解锁月度绩效";
					break;
				case "archive":
					monthPerformanceApply.put("status","4");
					content = "归档月度绩效";
					break;
				default:
					return RestUtils.returnSuccessWithString("传入的参数不正确！");
			}
			monthPerformanceApply.put("firstdepartmentId", erpMonthFirstDepartmentId);
			monthPerformanceApply.put("month", monthNum);
			
			erpMonthPerformanceMapper.updateFirstDepartmentMonthPerStatus(monthPerformanceApply);
			
			/*
			 * 增加月度绩效的新增记录
			 */
			ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
			String employeeName = userInfo.getEmployeeName();//员工姓名
			String username = userInfo.getUsername();//用户名
			String processor = employeeName==null||"".equals(employeeName)?username:employeeName;//新增月度绩效的操作人
			ErpSalaryMonthPerformanceRecord salaryMonthPerformanceRecord = new ErpSalaryMonthPerformanceRecord();
			salaryMonthPerformanceRecord.setFirstDepartmentId(erpMonthFirstDepartmentId);
			salaryMonthPerformanceRecord.setMonth(monthNum);
			salaryMonthPerformanceRecord.setProcessor(processor);
			salaryMonthPerformanceRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			salaryMonthPerformanceRecord.setContent(content);
			salaryMonthPerformanceRecordMapper.insertRecord(salaryMonthPerformanceRecord);

			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("方法changeMonthPerformanceType出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法异常，导致操作失败！"+e.getMessage());
		}
	}
	
	
	/* *************************************** 月度绩效新增接口逻辑 Begin***************************************************** */
	/**
	 * Description: 查询月度绩效
	 * @param Map params Keys：month、deptI、employeeName、page、limit
	 * @return
	 * @Author Songxiugong
	 * @Create Date: 2018年11月24日 上午10:20:01
	 * @modify Date: 2020-02-22	SXG 全部修改，只保留方法名称
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RestResponse findAllErpMonthPerformanceByPowerParams(String token,Map<String,Object> params){

		try {
			
			//0-plus 
			Map<String, Object> alertInfo = new HashMap<String, Object>(); // 返回前端提示信息
				
			// 0.获取各个参数配置赋值给查询参数（Map）
			// 0-1 给查询HR的参数赋值
			Map<String, Object> queryMap = new HashMap<String, Object>(); // 用户对象 map
			String deptId, employeeName;
		
			//0-1-plus entryTime    dimissionTime
			String month;
			month = String.valueOf(params.get("month"));
			if (month != null) {
				if (month != "" && month != "null") {
					queryMap.put("entryTime",month + "-31");
					queryMap.put("dimissionTime",month + "-01");
				}
			}
			
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
			if (params.get("exportDepartmentIdList") != null) {
				queryMap.put("exportDepartmentIdList", ((List)params.get("exportDepartmentIdList")));

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

			//status 员工状态
			queryMap.put("status", "0,1,2,3,4");
			//1.获取指定部门内的所有员工（没有指定部门，获取所有管辖部门的员工信息，调用HR工程-接口：findEmployeeByDeptAndUser
			//	获取的数据需要进行分页
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmployeeByDeptAndUser";
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);

			HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(queryMap,
					requestHeaders);

			ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request,
					RestResponse.class);
			RestResponse response = responseEntity.getBody();
			//1-1 如果没有获取到数据

			if (!response.getStatus().equals("200")) {
				String info = "调用HR失败，请联系管理员！";
				logger.info(info);
						
				alertInfo.put("Message",info);		
				return RestUtils.returnSuccess(alertInfo, "OK");
			}
			
			if (null == response.getData() || "".equals(response.getData())) {
				logger.info("获取数据返回结果为空！");
				alertInfo.put("Message","获取数据返回结果为空！");		
				return RestUtils.returnSuccess(alertInfo, "OK");
			}

			if (!response.getMsg().equals("OK")) {
				if(response.getMsg().equals("NotAuth")) {
					return this.returnErrorInfo(response.getMsg());	
				}
				
				String info = "获取HR数据失败！";
				logger.info(info);
				alertInfo.put("Message",info);
				return RestUtils.returnSuccess(alertInfo, "OK");		
			}

			// 解析返回结果
			List<Map<String, Object>> erpUserList = new ArrayList<Map<String, Object>>();
			Map<String,Object> returnedValue = new HashMap<String, Object>();

			returnedValue = (Map<String,Object>)response.getData();
			erpUserList = (List<Map<String, Object>>) (returnedValue.get("employeList"));

			// 2.根据找的员工信息取获取员工对应的月度绩效数据，然后给前端进行展示。默认获取所管辖所有人员的月度绩效数据。
			// 2-1 查询月度绩效
			// 2-1-1 待查询的月份
			Map<String, Object> queryAttendanceMap = new HashMap<String, Object>();
			if (month != null) {
				if (month != "" && month != "null") {
					queryAttendanceMap.put("erpMonthNum", month);
				}
			}
			
			// 2-2-2 待查询的人员
			List<Integer> userList = new ArrayList<Integer>();
			int plus;
			for (plus = 0; plus < erpUserList.size(); plus++) {
				Map<String, Object> employeeInfo = erpUserList.get(plus);
				Integer temp = Integer.valueOf(String.valueOf(employeeInfo.get("employeeId")));

				//字段值为null的字段赋值为""
				for(Map.Entry<String, Object> employee: employeeInfo.entrySet()) {
					String key = employee.getKey();
					String value = String.valueOf(employee.getValue());
					if(value == null || value.equals("null")) {
						employeeInfo.put(key, "");
					}
				}
				userList.add(temp);
			}
			queryAttendanceMap.put("list", userList);
			if(userList.size() == 0) {
				return RestUtils.returnSuccess(returnedValue, "OK");	
			}
			// 2-2-3 查询月度绩效
			List<ErpSalaryMonthPerformance> monthPerformanceList = erpMonthPerformanceMapper
					.findEmpMonthPerformanceMore(queryAttendanceMap);

			// 2-2-4 查询财务编号

			List<ErpEmpFinanceNumber> erpEmpFinanceNumberList = empFinanceNumberMapper.findEmpFinanceNumberDetailByParams(queryAttendanceMap);

			// 2-2-5 合并HR和月度绩效数据
			//解密月度绩效数据
			int ifbPlus;
			for (ifbPlus = 0; ifbPlus < monthPerformanceList.size(); ifbPlus++) {
				ErpSalaryMonthPerformance monthPerformance = monthPerformanceList.get(ifbPlus);
				monthPerformance = decryptDataRsaObject(monthPerformance);
			}

			//员工信息、合并月度绩效、财务序号
			for (plus = 0; plus < erpUserList.size(); plus++) {
				Map<String, Object> employeeInfo = erpUserList.get(plus);
				Integer employeeId = Integer.valueOf(String.valueOf(employeeInfo.get("employeeId")));

				//A. 合并月度绩效------------------------------------
				int flag = 0;	//是否匹配的标识 1匹配 0 不匹配
				for (ifbPlus = 0; ifbPlus < monthPerformanceList.size(); ifbPlus++) {
					ErpSalaryMonthPerformance monthPerformance = monthPerformanceList.get(ifbPlus);

					if(monthPerformance == null) {
						break;
					}

					if(monthPerformance.getErpMonthEmpId() == null) {
						break;
					}

					Integer ifbUserId = Integer.valueOf(String.valueOf(monthPerformance.getErpMonthEmpId()));

					if(employeeId.equals(ifbUserId)) {
						//工资绩效
						employeeInfo.put("erpMonthMeritPay",(Object)(monthPerformance.getErpMonthMeritPay()));
						//项目绩效
						employeeInfo.put("erpMonthProjectPay",(Object)(monthPerformance.getErpMonthProjectPay()));
						//餐交补
						employeeInfo.put("erpMonthMealSubsidy",(Object)(monthPerformance.getErpMonthMealSubsidy()));
//						/手机话费补助
						employeeInfo.put("erpMonthTelSubsidy",(Object)(monthPerformance.getErpMonthTelSubsidy()));
						//本月实际应发工资套改绩效
						employeeInfo.put("erpMonthActualMeritPay",(Object)(monthPerformance.getErpMonthActualMeritPay()));
						//当月发放绩效合计
						employeeInfo.put("erpMonthMeritSum",(Object)(monthPerformance.getErpMonthMeritSum()));

						//当月工作日
						employeeInfo.put("erpMonthShouldWorkDays",(Object)(monthPerformance.getErpMonthShouldWorkDays()));
						//实际工作天数
						employeeInfo.put("erpMonthActualWorkDays",(Object)(monthPerformance.getErpMonthActualWorkDays()));
						flag = 1;
						break;
					}
				}
				if(flag == 0) {
					employeeInfo.put("erpMonthMeritPay","");
					employeeInfo.put("erpMonthProjectPay","");
					employeeInfo.put("erpMonthMealSubsidy","");
					employeeInfo.put("erpMonthTelSubsidy","");
					employeeInfo.put("erpMonthActualMeritPay","");
					employeeInfo.put("erpMonthMeritSum","");
					employeeInfo.put("erpMonthShouldWorkDays","");
					employeeInfo.put("erpMonthActualWorkDays","");
				}

				//B. 合并财务序号------------------------------------
				flag = 0;	//是否匹配的标识 1匹配 0 不匹配
				for (ifbPlus = 0; ifbPlus < erpEmpFinanceNumberList.size(); ifbPlus++) {
					ErpEmpFinanceNumber erpEmpFinanceNumber = erpEmpFinanceNumberList.get(ifbPlus);

					if(erpEmpFinanceNumber == null) {
						break;
					}

					if(erpEmpFinanceNumber.getEmployeeId() == null) {
						break;
					}

					Integer ifbUserId = Integer.valueOf(String.valueOf(erpEmpFinanceNumber.getEmployeeId()));

					if(employeeId.equals(ifbUserId)) {
						//财务序号
						employeeInfo.put("empFinanceNumber",(Object)(erpEmpFinanceNumber.getEmpFinanceNumber()));

						flag = 1;
						break;
					}
				}
				if(flag == 0) {
					employeeInfo.put("empFinanceNumber","");
				}
			}

			returnedValue.put("employeList", erpUserList);
			return RestUtils.returnSuccess(returnedValue, "OK") ;
		}catch(Exception e) {
			logger.error("findAllErpMonthPerformanceByPowerParams出现异常："+e.getMessage(),e);
			Map<String,Object> valueError = new HashMap<String, Object>();
			valueError.put("Message", "方法异常，导致操作失败！"+e.getMessage());
			return RestUtils.returnFailure(valueError, "Error");
		}
	}
	
	/**
	 * Description: 依据当前登陆人角色并根据选择的部门查询对应的月度考勤状态和月度绩效状态及月度绩效的初始化状态
	 *
	 * @param  month 年度月份（YYYY-MM）
	 * @Author Songxiuogong
	 * @Create Date: 2020年02月23日
	 */
	public RestResponse queryDepartMentListAndPerformanceStatus(String month, String token) {
		logger.info("进入queryDepartMentListAndPerformanceStatus方法根据查询角色查询部门及对应的月度考勤和月度绩效状态！");
		//跨工程查询自己负责的部门及其月度考勤和月度绩效

		try {
			List<Map<String,Object>> firstDepartmentList = null;	//返给前端的List

			//1. 跨project工程调用获取部门及月度考勤信息

			Map<String,Object> params = new HashMap<String, Object>();

			//没有填写，默认为当前月份
			if(month ==null || month.equals("") || month.equals("null") ) {
				Calendar calendar = Calendar.getInstance();
				Integer year = calendar.get(Calendar.YEAR);
				Integer imonth = calendar.get(Calendar.MONTH) + 1;

				SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM");
				month = formatYMD.format(formatYMD.parse(String.valueOf(year) + "-" + String.valueOf(imonth)));
			}
			params.put("month", month);

			String url = protocolType + "nantian-erp-project/nantian-erp/project/adminDic/queryDepartMentListAndAttendanceStatus?month=" + month;
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);

			HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(null,requestHeaders);

			ResponseEntity<RestResponse> response = this.restTemplate.exchange(url, HttpMethod.GET,request,RestResponse.class);
			if (!("200".equals(response.getBody().getStatus()))) {
				return this.returnErrorInfo("调用Project工程失败！");
			}

			// 1-1.解析请求的结果
			Map<String,Object>  valueError = new HashMap<String, Object>();
			
			Map<String,Object> data = (Map<String,Object>)response.getBody().getData();
			if(data.isEmpty()) {
				valueError.put("Message", "获取考勤数据为空");
				return RestUtils.returnSuccess(valueError, "OK");
			}
						
			String departmentName = String.valueOf(data.get("depts"));	//集团名称
			firstDepartmentList = (List<Map<String,Object>>)(data.get("depts"));	//集团的信息

			//2.根据月份查询一级部门的月度绩效状态
			List<Map<String,Object>> performanceStatusList= erpSalaryMonthPerformanceMapper.findMonthPerformanceApplyByParams(params);

			if(firstDepartmentList == null || firstDepartmentList.size() == 0 ) {
				RestUtils.returnSuccess(new ArrayList<String>(), "OK");
			}

			//2-1.状态码及状态中文名称组装
			Map<Integer, Integer> departmentStatusMap = new HashMap<>();
			Map<Integer, String> departmentStatusNameMap = new HashMap<>();
			Map<Integer, Integer> departmentStatusInitMap = new HashMap<>();
			for(Map<String,Object> performanceStatus : performanceStatusList){
				if(performanceStatus.get("firstdepartmentId") != null) {
					if(String.valueOf(performanceStatus.get("firstdepartmentId")) != "") {
						Integer deptID = Integer.valueOf(String.valueOf(performanceStatus.get("firstdepartmentId")));
						Integer temp = 1;			//部门月度绩效状态码
						String statuName = null;	//部门月度绩效状态中文名称

						if(performanceStatus.get("status") != null) {
							if(String.valueOf(performanceStatus.get("status")) != "") {
								temp = Integer.valueOf(String.valueOf(performanceStatus.get("status")));
						}

						switch(temp.intValue()) {
//						case 0:
//							statuName = "未提交";
//							break;
						case 1:
							statuName = "未提交";
							break;
						case 2:
							statuName = "已提交";
							break;
						case 3:
							statuName = "锁定";
							break;
						case 4:
							statuName = "归档";
							break;
						}

						departmentStatusMap.put(deptID, temp);
						departmentStatusNameMap.put(deptID, statuName);
						//初始化状态码 statusInit 0 未初始化，1已初始化
						Integer statusInitValue =  Boolean.valueOf(String.valueOf(performanceStatus.get("status_init"))) ? 1 : 0;
						Integer statusInit = performanceStatus.get("status_init") == null || String.valueOf(performanceStatus.get("status_init")).equals("")
								|| String.valueOf(performanceStatus.get("status_init")).equals("null") 
								? 0 : statusInitValue;

						departmentStatusInitMap.put(deptID, statusInit);
						}
					}
				}
			}

			// 2-2.组装各部门月度考勤状态与月度绩效状态【注意：二级部门没有月度绩效】
			
			int statusInitFlag = 1;	//只要有一个部门绩效没有被初始化就是0 ，所有有效部门初始化后才为1
			for (Map<String,Object> firstDepartment : firstDepartmentList){
				//
				Integer firstDeptID = Integer.valueOf(String.valueOf(firstDepartment.get("departmentId")));
				Integer firstStatus = departmentStatusMap.get(firstDeptID);
				String firstStatusName = departmentStatusNameMap.get(firstDeptID);
				Integer statuInit = departmentStatusInitMap.get(firstDeptID);
				
				firstDepartment.put("status", firstStatus == null ? 1 : firstStatus);//月度绩效状态码
				firstDepartment.put("statuName", firstStatusName == null ? "未提交" : firstStatusName);//度绩效状态中文名称
				firstDepartment.put("statusInit", statuInit == null ? 0 : statuInit);//初始化状态码 statusInit 0 未初始化，1已初始化
				statusInitFlag =  statuInit == null || statuInit.intValue() == 0 ? 0 : statusInitFlag;
				if(firstDepartment.get("depts") == null || ((List<Map<String, Object>>)firstDepartment.get("depts")).size() ==0 ){
					continue;
				}
				for (Map<String,Object> secondDepartment : (List<Map<String, Object>>) firstDepartment.get("depts")){
					//查询部门考勤状态
					Integer secondStatus = departmentStatusMap.get(Integer.valueOf(String.valueOf(secondDepartment.get("departmentId"))));
					secondDepartment.put("status", firstStatus == null ? 1 : firstStatus);
					secondDepartment.put("statuName", firstStatusName == null ? "未提交" : firstStatusName);
				}
			}
			
			data.put("statuInit", statusInitFlag);
			return RestUtils.returnSuccess(data);
		}catch (Exception e) {
			logger.error("queryDepartMentListAndPerformanceStatus方法出现异常:"+e.getMessage(),e);
			Map<String,Object> valueError = new HashMap<String, Object>();
			valueError.put("Message", "方法异常，导致操作失败！"+e.getMessage());
			return RestUtils.returnFailure(valueError, "Error");
		}
	}

	/**
	 * Description: 锁定、解锁、归档指定月份的月度绩效
	 *
	 * @param  Map：key="type",   value: lock、unlock、archive
	 * 				Key="month",  value:"2020-02"	YYYY-MM
	 * 				key=firstdepartmentId	value:String
	 * @Author Songxiugong
	 * @Create Date: 2020年02月23日
	 */
	@Transactional
	public RestResponse changeCurMonthPerformanceType(Map<String,Object> params,String token) throws Exception{
		logger.info("进入changeCurMonthPerformanceType方法，参数是：param="+params);
//		try {
			//1.获取当前月份的所有部门创建的月度绩效
			//1-1.如果param中没有month参数，获取当前月份
			String month = null;
			if(params.get("month") == null || String.valueOf(params.get("month")).equals("")) {
				Date dateNow = Calendar.getInstance().getTime();

				Calendar calendar = Calendar.getInstance();
				Integer iyear = calendar.get(Calendar.YEAR);
				Integer imonth = calendar.get(Calendar.MONTH) + 1;
				String myMonth = imonth > 9 ? String.valueOf(imonth) : "0" + String.valueOf(imonth);
				month = String.valueOf(iyear) + "-" + myMonth;

			}else {
				month = String.valueOf(params.get("month"));
			}
			
			//1-1 Plus 判断选择的日期是否为当前月份或者上一个月份
			Map<String, Object> alert = new HashMap<String, Object>();	//提示信息
			if(!this.isPreOrCurMonth(this.stringToDate(month + "-01"))) {
				alert.put("Message", "请选择当前月份或者上个月份!");
				return RestUtils.returnSuccess(alert, "OK");
			}
			
			//判断部门是否都已经初始化操作
			RestResponse response  = this.queryDepartMentListAndPerformanceStatus(month, token);
			if(!(response.getStatus().equals("200"))) {
				alert.put("Message", "获取绩效异常，请联系管理员");
				return RestUtils.returnSuccess(alert, "OK");
			}
			
			Map<String,Object> attendancePerformanceStatus = (Map<String,Object> ) response.getData();
			if(String.valueOf(attendancePerformanceStatus.get("statuInit")).equals("0")) {
				alert.put("Message", "部分人员的月度绩效没有初始化，请先初始化月度绩效！");
				return RestUtils.returnSuccess(alert, "OK");
			}
			
			List<Map<String,Object>> attendancePerformanceList = (List<Map<String,Object>> ) attendancePerformanceStatus.get("depts");
			
			if(attendancePerformanceList == null || attendancePerformanceList.size() == 0){
				alert.put("Message", "获取绩效异常，请联系管理效！");
				return RestUtils.returnSuccess(alert, "OK");
			}
						
			//1-2.指定查询的的参数
			Map<String,Object> paramsQuery = new HashMap<String, Object>();
			paramsQuery.put("month", month);

			//需要写日志的变量
			String content = null;	//操作内容
			String newStatus = null;	//新的状态码
			Map<String,Object> returnMessage = new HashMap<String, Object>();//返回提示信息
			if(params.get("firstdepartmentIds") != null){
				List<Integer> firstdepartmentIds=(List<Integer>) params.get("firstdepartmentIds");
				int plus = 0;
				for(Integer firdepartmentId:firstdepartmentIds){
					//查询参数赋值
					if(params.get("type") == null || String.valueOf(params.get("type")).equals("")) {
						Map<String,Object> valueError = new HashMap<String, Object>();
						valueError.put("Message", "参数的类型错误，请联系管理员!");
						return RestUtils.returnSuccess(valueError, "OK");
					}else {

						/*Integer firstDept = params.get("firstdepartmentId") == null
								|| String.valueOf(params.get("firstdepartmentId")).equals("null")
								|| String.valueOf(params.get("firstdepartmentId")).equals("")
								? null : Integer.valueOf(String.valueOf(params.get("firstdepartmentId")));*/
						Integer firstDept=firdepartmentId;
						//考勤、绩效状态获取
						Map<String,Integer> summary = this.attendanceAndPerformanceSummary(month, firstDept, token);
						//需要查询的状态
						List<String> statusList = new ArrayList<String>();

						String type = String.valueOf(params.get("type"));
						switch(type) {
						case "lock":
												
							//绩效没有提交--提示操作人员
							if(summary.get("performanceStatus").intValue() == 1) {
								alert.put("Message", "所选部门"+month+"月份考勤未导出或月度绩效未提交，不能锁定！");
								return RestUtils.returnSuccess(alert, "OK");
							}else if(summary.get("performanceStatus").intValue() == 3){
								alert.put("Message", "绩效已经锁定，无需再锁定");
								return RestUtils.returnSuccess(alert, "OK");
							}else if(summary.get("performanceStatus").intValue() == 4){
								alert.put("Message", "绩效已经归档，不能锁定!");
								return RestUtils.returnSuccess(alert, "OK");
							}
												
							statusList.add("2");
							paramsQuery.put("statusList",statusList);
							paramsQuery.put("satus","2");
							newStatus = "3";
							content = "锁定月度绩效";
							break;
						case "unlock":
							//绩效提示
							if(summary.get("performanceStatus").intValue() == 1) {
								alert.put("Message", "绩效没有锁定，不能解除锁定");
								return RestUtils.returnSuccess(alert, "OK");
							}else if(summary.get("performanceStatus").intValue() == 2){
								alert.put("Message", "绩效没有锁定，不能解除锁定");
								return RestUtils.returnSuccess(alert, "OK");
							}else if(summary.get("performanceStatus").intValue() == 4){
								alert.put("Message", "绩效已导出，不能解除锁定");
								return RestUtils.returnSuccess(alert, "OK");
							}
							
							statusList.add("3");
							paramsQuery.put("statusList",statusList);
							paramsQuery.put("satus","3");
							newStatus = "2";
							content = "解锁月度绩效";
							break;
						case "archive":
							statusList.add("3");
							paramsQuery.put("statusList",statusList);
							paramsQuery.put("satus","3");

							newStatus = "4";
							content = "归档月度绩效";
							break;
						default:
							alert.put("Message", "传入的参数不正确！");
							return RestUtils.returnSuccess(alert, "OK");
						}
					}

					//部门配置
					/*if(params.get("firstdepartmentId") != null && !String.valueOf(params.get("firstdepartmentId")).equals("")) {
						paramsQuery.put("firstdepartmentId",Integer.valueOf(String.valueOf(params.get("firstdepartmentId"))));
					}*/
					paramsQuery.put("firstdepartmentId",firdepartmentId);
				
					//1-3.获取指定月份、状态为satusList中的绩效状态
					List<Map<String,Object>> erpSalaryMonthPerformanceList = erpSalaryMonthPerformanceMapper.findMonthPerformanceApplyByParams(paramsQuery);

					//1-3-1没有月度绩效数据
					if(erpSalaryMonthPerformanceList == null || erpSalaryMonthPerformanceList.size() == 0) {
						Map<String,Object> tempReturn = new HashMap<String, Object>();
						tempReturn.put("Message", content + " " + " 0 条");
						continue;
					}

					//1-3-2有月度绩效数据进行更新操作
					Map<String,Object> paramsSave = new HashMap<String, Object>();
					paramsSave.put("status", newStatus);

					erpSalaryMonthPerformanceList = this.filterActualSalaryMonthPerformanceList(erpSalaryMonthPerformanceList, attendancePerformanceList);
					for(int index = 0; index < erpSalaryMonthPerformanceList.size(); index ++) {
						Map<String,Object> erpSalaryMonthPerformance = erpSalaryMonthPerformanceList.get(index);
						
						if(erpSalaryMonthPerformance.get("id") == null || String.valueOf(erpSalaryMonthPerformance.get("id")).equals("")) {
							break;
						}
						Integer perApplayId = Integer.valueOf(String.valueOf(erpSalaryMonthPerformance.get("id")));
										
						if(erpSalaryMonthPerformance.get("firstdepartmentId") == null || String.valueOf(erpSalaryMonthPerformance.get("firstdepartmentId")).equals("")) {
							break;
						}
						Integer firstDeptId = Integer.valueOf(String.valueOf(erpSalaryMonthPerformance.get("firstdepartmentId")));

						paramsSave.put("perApplayId", perApplayId);
						paramsSave.put("modifiedTime",ExDateUtils.getCurrentDateTime());
						if(params.get("firstdepartmentId") != null && !String.valueOf(params.get("firstdepartmentId")).equals("")) {
							paramsSave.put("firstdepartmentId",Integer.valueOf(String.valueOf(params.get("firstdepartmentId"))));
						}
						erpMonthPerformanceMapper.updateFirstDepartmentMonthPerStatus(paramsSave);
						
						//3.增加月度绩效的新增记录
						this.logPerformanceOperatation(token, month, content);
						plus++;
					}
				}
				returnMessage.put("Message", content + " " + plus + " 个部门");
			}
			return RestUtils.returnSuccess(returnMessage, "OK");
//		} catch (Exception e) {
//			logger.error("方法changeCurMonthPerformanceType出现异常："+e.getMessage(),e);
//
//			Map<String,Object> valueError = new HashMap<String, Object>();
//			valueError.put("message", "方法异常，导致操作失败！"+e.getMessage());
//			return RestUtils.returnFailure(valueError, "Error");
//		}
	}
	
	/**
	 * Description: 获取有效部门的月度绩效（标准为左侧的机构树）
	 *         
	 * @params  月度绩效：app List<Map<String,Object>> erpSalaryMonthPerformanceList 
	 * 			机构树的月度绩效和月度考勤状态：List<Map<String,Object>> attendancePerformance
	 * @return 月度绩效：app List<Map<String,Object>> erpSalaryMonthPerformanceList 
	 * @Author songxiugong
	 * @Create Date: 2020年03月04日
	 */
	public List<Map<String,Object>> filterActualSalaryMonthPerformanceList(List<Map<String,Object>> erpSalaryMonthPerformanceList,
			List<Map<String,Object>> attendancePerformanceList){
		
		List<Map<String,Object>> erpSalaryMonthPerformanceListNew = new ArrayList<Map<String,Object>>();
		try {
			
			for(int iplus = 0; iplus < erpSalaryMonthPerformanceList.size(); iplus++) {
				
				Map<String,Object> erpSalaryMonthPerformance = erpSalaryMonthPerformanceList.get(iplus);
				String deptId =  String.valueOf(erpSalaryMonthPerformance.get("firstdepartmentId"));
				
				for(int jplus = 0; jplus < attendancePerformanceList.size(); jplus++) {
					
					Map<String,Object> attendancePerformance = attendancePerformanceList.get(jplus);
					String deptIdInAttPer = String.valueOf(attendancePerformance.get("departmentId"));
					if(deptIdInAttPer.equals(deptId)) {
						erpSalaryMonthPerformanceListNew.add(erpSalaryMonthPerformance);
						break;
					}
				}
				
			}
		}catch(Exception e) {
			logger.error("filterActualSalaryMonthPerformanceList 出现异常:" + e.getMessage(), e);
		}
		
		return erpSalaryMonthPerformanceListNew;
	}
		
	/**
	 * Description: 经管初始化月度绩效--将各个一级部门经理提交的月度绩效缺失的数据补录完整
	 * 				Logic：实习生没有话费补助和餐交补
	 * @param  Map：keys: month
	 * @Author Songxiugong
	 * @Create Date: 2020年02月23日
	 */
	@Transactional
	public RestResponse completePerformance(Map<String,Object> params, String token) throws Exception{
		logger.info("completePerformance：Begin 经管初始化月度绩效！");	
		
		//0.判断是否为经管角色
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
		Integer id = erpUser.getUserId();//用户Id
		String username = erpUser.getUsername();//从用户信息中获取用户名
		List<Integer> roles = erpUser.getRoles();//从用户信息中获取角色信息
		logger.info("id="+id+",username="+username+",roles="+roles);
		
		//仅经管角色能在"所有月度绩效"中初始化月度绩效
		if(!roles.contains(7)) {
			Map<String,Object> returnMessageForRole = new HashMap<String, Object>();
			returnMessageForRole.put("Message", "您无权限执行操作");
			return RestUtils.returnSuccess(returnMessageForRole, "OK");
		}
		
		//1.没有填写，默认为当前月份
		String month = params.get("month") == null ? "" : String.valueOf(params.get("month"));
				
		if(month ==null || month.equals("") || month.equals("null") ) {
			Calendar calendar = Calendar.getInstance();
			Integer year = calendar.get(Calendar.YEAR);
			Integer imonth = calendar.get(Calendar.MONTH) + 1;

			SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM");
			month = formatYMD.format(formatYMD.parse(String.valueOf(year) + "-" + String.valueOf(imonth)));
		}
		
		//1-1 Plus 判断选择的日期是否为当前月份或者上一个月份
		Map<String, Object> alert = new HashMap<String, Object>();	//提示信息
		if(!this.isPreOrCurMonth(this.stringToDate(month + "-01"))) {
			alert.put("Message", "请选择当前月份或者上个月份!");
			return RestUtils.returnSuccess(alert, "OK");
		}
		
		//2.是否可以进行初始化操作提示
		int flag = this.canInitMonthPerformance(month, token);
//			flag = 4;			
		Map<String,Object> returnMessage = new HashMap<String, Object>();
//		if(flag == 0 ) {
//			returnMessage.put("Message", "考勤没有导出，无法初始化");
//		}else if(flag == 1) {
//			returnMessage.put("Message", "部分月度绩效没有提交，无法初始化");
//		}else
		if(flag == 3) {
			returnMessage.put("Message", "月度绩效已锁定，无法初始化");
		}else if(flag == 4) {
			returnMessage.put("Message", "月度绩效已导出，无法初始化");
		}else if(flag == -1){
			returnMessage.put("Message", "数据异常，请联系管理员");
		}
//		if(flag != 2) {
//			return RestUtils.returnSuccess(returnMessage, "OK");
//		}
		
		//2.获取人员信息列表，判断是否为实习生，实习生没有各种补助
		
		Map<String, Object> queryMap = new HashMap<String, Object>();
		//status 员工状态
		queryMap.put("status", "0,1,2,3,4");
		//1.获取指定部门内的所有员工（没有指定部门，获取所有管辖部门的员工信息，调用HR工程-接口：findEmployeeByDeptAndUser
		//	获取的数据需要进行分页
		String urlForHR = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmployeeByDeptAndUser";
		HttpHeaders requestHeadersForHR = new HttpHeaders();
		requestHeadersForHR.add("token", token);

		HttpEntity<Map<String, Object>> requestForHR = new HttpEntity<Map<String, Object>>(queryMap,
				requestHeadersForHR);

		ResponseEntity<RestResponse> responseEntityForHR = this.restTemplate.postForEntity(urlForHR, requestForHR,
				RestResponse.class);
		RestResponse responseForHR = responseEntityForHR.getBody();
		//1-1 如果没有获取到数据

		String info;
		if (!responseForHR.getStatus().equals("200")) {
			info = "调用HR失败，请联系管理员！";
			logger.info(info);
					
			alert.put("Message",info);		
			return RestUtils.returnSuccess(alert, "OK");
		}
		
		if (null == responseForHR.getData() || "".equals(responseForHR.getData())) {
			logger.info("获取数据返回结果为空！");
			alert.put("Message","获取数据返回结果为空！");		
			return RestUtils.returnSuccess(alert, "OK");
		}

		if (!responseForHR.getMsg().equals("OK")) {
			if(responseForHR.getMsg().equals("NotAuth")) {
				return this.returnErrorInfo(responseForHR.getMsg());	
			}
			
			info = "获取HR数据失败！";
			logger.info(info);
			alert.put("Message",info);
			return RestUtils.returnSuccess(alert, "OK");		
		}

		// 解析返回结果
		List<Map<String, Object>> erpUserList = new ArrayList<Map<String, Object>>();
		Map<String,Object> returnedValue = new HashMap<String, Object>();

		returnedValue = (Map<String,Object>)responseForHR.getData();
		erpUserList = (List<Map<String, Object>>) (returnedValue.get("employeList"));
			
		//3.初始化月度绩效
		Map<String, Object> paramsQuery = new HashMap<String, Object>();
		paramsQuery.put("month", month);
		
		//3-0.跨project调用，获取所有指定月份的节假日---------------------------------------------------------------------------	
		Double configShouldWorkDays;	//从节假日中获取并通过计算得到的工作日天数
		
		String urlHoliday = protocolType + "nantian-erp-project/nantian-erp/project/holiday/findSpecifiedMothHoliday?selectedDate=" + month;
		HttpHeaders requestHeadersHoliday = new HttpHeaders();
		requestHeadersHoliday.add("token", token);
		
		HttpEntity<Map<String, Object>> requestHoliday = new HttpEntity<Map<String, Object>>(null,requestHeadersHoliday);
		ResponseEntity<RestResponse> responseHoliday = this.restTemplate.exchange(urlHoliday, HttpMethod.GET, requestHoliday, RestResponse.class);
		
		Map<String,Object> valueErrorHoliday = new HashMap<String, Object>();	//错误Map容器
		
		if (!("200".equals(responseHoliday.getBody().getStatus()))) {
			valueErrorHoliday.put("Message", "获取工作日失败，请联系管理员！");
			return RestUtils.returnSuccess(valueErrorHoliday, "OK");
		}
		
		//解析请求的结果
		Map<String,Object> daysAndHolidaysInMonth = (Map<String,Object>) responseHoliday.getBody().getData();

		if(daysAndHolidaysInMonth == null || daysAndHolidaysInMonth.size() == 0 ) {
			valueErrorHoliday.put("Message", "获取工作日失败，请联系管理员！");
			return RestUtils.returnSuccess(valueErrorHoliday, "OK");
		}
		
		//获取工作日
		String temp = daysAndHolidaysInMonth.get("workdays") != null ? String.valueOf(daysAndHolidaysInMonth.get("workdays")) : null;
		if(temp == null) {
			valueErrorHoliday.put("Message", "获取工作日异常，请联系管理员！");
			return RestUtils.returnSuccess(valueErrorHoliday, "OK");
		}
		configShouldWorkDays = Double.valueOf(temp);
		
		//3-1.跨project调用，获取所有指定月份的月度考勤---------------------------------------------------------------------------
		
		String url = protocolType + "nantian-erp-project/nantian-erp/project/monthAttendance/getAllMonthAttendanceByMonth";
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("token", token);
		
		HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(paramsQuery,requestHeaders);
		ResponseEntity<RestResponse> response = this.restTemplate.exchange(url, HttpMethod.POST,request,RestResponse.class);
		
		Map<String,Object> valueError = new HashMap<String, Object>();	//错误Map容器
		
		if (!("200".equals(response.getBody().getStatus()))) {
			valueError.put("Message", "获取月度考勤失败，请联系管理员！");
			return RestUtils.returnSuccess(valueError, "OK");
		}
		
		// 3-2.解析请求的结果
		List<Map<String,Object>> erpAttendanceList = (List<Map<String,Object>>)response.getBody().getData();

		if(erpAttendanceList == null || erpAttendanceList.size() == 0 ) {
			valueError.put("Message", "月度考勤数据空，无法进行初始化操作！");
			return RestUtils.returnSuccess(valueError, "OK");
		}

		// 3-3.获取所有指定月份的月度绩效---------------------------------------------------------------------------
		
		Set<Integer> deptsInit = new HashSet<Integer>();
		
		erpAttendanceList = attendanceAddEmployeeStatus(erpAttendanceList, erpUserList);
		
		Map<String,Object> qureyPerformance = new HashMap<String, Object>();
		qureyPerformance.put("erpMonthNum", month);
		List<ErpSalaryMonthPerformance> erpSalaryMonthPerformanceList = erpSalaryMonthPerformanceMapper.findEmpMonthPerformanceMore(qureyPerformance);
		for(int jPlus = 0; jPlus < erpSalaryMonthPerformanceList.size(); jPlus ++) {
			
			ErpSalaryMonthPerformance erpSalaryMonthPerformance = erpSalaryMonthPerformanceList.get(jPlus);
			
			//3-3-1.保持精度月度绩效各个字段 BigDecimal 变量定义及获取---------------------------------------------------------------------------
			String myTempValue;
			
			//手机补助
			myTempValue = erpSalaryMonthPerformance.getErpMonthTelSubsidy() == null || erpSalaryMonthPerformance.getErpMonthTelSubsidy().equals("") ? 
					"" : AesUtils.decrypt(erpSalaryMonthPerformance.getErpMonthTelSubsidy());
			
			BigDecimal telSubsidy = new BigDecimal(0) ;			//手机补助  绩效创建时获取，此处重新获取
			if(!myTempValue.equals("") && !myTempValue.equals("null")) {
				telSubsidy = telSubsidy.add(new BigDecimal(myTempValue).setScale(2,BigDecimal.ROUND_HALF_UP));
			}
			
			//项目绩效
			myTempValue = erpSalaryMonthPerformance.getErpMonthProjectPay() == null || erpSalaryMonthPerformance.getErpMonthProjectPay().equals("") ? 
					"" : AesUtils.decrypt(erpSalaryMonthPerformance.getErpMonthProjectPay());
			
			BigDecimal projectPay = new BigDecimal(0) ;			//项目绩效   绩效创建时获取，此处重新获取
			if(!myTempValue.equals("") && !myTempValue.equals("null")) {
				projectPay = projectPay.add(new BigDecimal(myTempValue).setScale(2,BigDecimal.ROUND_HALF_UP));
			}
			
			//本月工资套改绩效 定义及获取
			myTempValue = erpSalaryMonthPerformance.getErpMonthMeritPay() == null || erpSalaryMonthPerformance.getErpMonthMeritPay().equals("") ? 
					"" : AesUtils.decrypt(erpSalaryMonthPerformance.getErpMonthMeritPay());
			
			BigDecimal meritPay = new BigDecimal(0) ;			//本月工资套改绩效   绩效创建时获取，此处重新获取
			if(!myTempValue.equals("") && !myTempValue.equals("null")) {
				meritPay = meritPay.add(new BigDecimal(myTempValue).setScale(2,BigDecimal.ROUND_HALF_UP));
			}
			
			BigDecimal actualMeritPay = new BigDecimal(0) ;		//本月实际应发工资套改绩效   绩效创建时获取，此处重新获取
			BigDecimal shouldWorkDays = new BigDecimal(0) ;		//当月工作日数
			BigDecimal actualWorkDays = new BigDecimal(0) ;		//实际工作天数	初始化月度绩效时已经从月度考勤中获取了，此处不需要获取
			BigDecimal meritSum = new BigDecimal(0) ;			//当月发放绩效合计
			BigDecimal mealSubsidy = new BigDecimal(0) ;		//餐交补助
			
			Double erpMonthBeliel = null; 			//出勤比例
							
			BigDecimal attendanceActualWorkDays = new BigDecimal(0); 				//实际工作天数-考勤	已经计算好了 
			BigDecimal attendancePositiveProjectWorkDays = new BigDecimal(0); 		//转正在项目天数-考勤
			BigDecimal attendancePeriodProjectWorkDays = new BigDecimal(0); 		//试用期在项目天数-考勤
			BigDecimal attendancePositiveCompanyWorkDays = new BigDecimal(0); 		//转正在公司天数-考勤
			BigDecimal attendancePeriodCompanyWorkDays = new BigDecimal(0); 		//试用期在公司天数-考勤
			BigDecimal attendancePositiveRemoteWorkDays = new BigDecimal(0); 		//转正远程天数-考勤
			BigDecimal attendancePeriodRemoteWorkDays = new BigDecimal(0); 		//试用期远程天数-考勤



			Double workDay = null; 			//工作天数-考勤
			Double leaveDay = null; 		//请假天数-考勤
			Double workRemote = null; 		//请假天数-考勤


			//3-3-2.更新月度绩效---------------------------------------------------------------------------
			//向数据库更新数据的ErpSalaryMonthPerformance 实例
			ErpSalaryMonthPerformance erpSalaryMonthPerformanceToUpdate = new ErpSalaryMonthPerformance();
			
			int  performanceEmployeeID = (erpSalaryMonthPerformance.getErpMonthEmpId()).intValue();
			for(int iPlus = 0; iPlus < erpAttendanceList.size(); iPlus++) {
				
				//找月度考勤中对应的数据
				Map<String,Object> erpAttendance = erpAttendanceList.get(iPlus);
				int  attendanceEmployeeID = Integer.valueOf(String.valueOf(erpAttendance.get("employeeId"))).intValue();
				
				if(performanceEmployeeID == attendanceEmployeeID) {
					
					//初始化过的一级部门 ID集合
					if(erpSalaryMonthPerformance.getErpMonthFirstDepartmentId() != null) {
						deptsInit.add(erpSalaryMonthPerformance.getErpMonthFirstDepartmentId());
					}
					attendanceActualWorkDays = BigDecimal.valueOf(Double.valueOf(this.formartValue(erpAttendance.get("actual_work_days"))));
					
					attendancePositiveProjectWorkDays = BigDecimal.valueOf(Double.valueOf(this.formartValue(erpAttendance.get("positive_project_work_days"))));
					attendancePeriodProjectWorkDays = BigDecimal.valueOf(Double.valueOf(this.formartValue(erpAttendance.get("period_project_work_days"))));
					attendancePositiveCompanyWorkDays = BigDecimal.valueOf(Double.valueOf(this.formartValue(erpAttendance.get("positive_company_work_days"))));
					attendancePeriodCompanyWorkDays = BigDecimal.valueOf(Double.valueOf(this.formartValue(erpAttendance.get("period_company_work_days"))));

					attendancePositiveRemoteWorkDays = BigDecimal.valueOf(Double.valueOf(this.formartValue(erpAttendance.get("positive_remote_work_days"))));
					attendancePeriodRemoteWorkDays = BigDecimal.valueOf(Double.valueOf(this.formartValue(erpAttendance.get("period_remote_work_days"))));
					workDay = Double.valueOf(String.valueOf(erpAttendance.get("workDay")));
					leaveDay = Double.valueOf(String.valueOf(erpAttendance.get("leaveDay")));
					//A.比例计算
					if(configShouldWorkDays == 0.0) {
						erpMonthBeliel = Double.valueOf(0.0);
					}else {
						erpMonthBeliel = workDay/configShouldWorkDays;
					}
					
					BigDecimal bFormat = new BigDecimal(erpMonthBeliel);
					erpMonthBeliel = bFormat.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();	//四舍五入取两位有效小数
					erpSalaryMonthPerformanceToUpdate.setErpMonthBeliel(erpMonthBeliel);
					
					//B.计算餐交费
					//判断员工状态，计算餐费和交通费用。逻辑：通过 试用期天数和转正天数比较判断人员是否转正的， 不跨工程调用获取员工的状态，提高效率
					
					if(!(String.valueOf(erpAttendance.get("employeeStatus")).equals("0"))) {
						double periodDays = attendancePeriodProjectWorkDays.doubleValue() + attendancePeriodCompanyWorkDays.doubleValue();
						double positiveDays = attendancePositiveProjectWorkDays.doubleValue() + attendancePeriodCompanyWorkDays.doubleValue();
						double temValue = 0;
						
						temValue = workDay * 15 + attendancePeriodProjectWorkDays.doubleValue() * 10 + 
								attendancePositiveProjectWorkDays.doubleValue() * 20;
						mealSubsidy = mealSubsidy.add(new BigDecimal(temValue));
						erpSalaryMonthPerformanceToUpdate.setErpMonthMealSubsidy(AesUtils.encrypt(mealSubsidy.setScale(2,BigDecimal.ROUND_HALF_UP).toString()));
					}else {
						erpSalaryMonthPerformanceToUpdate.setErpMonthMealSubsidy(AesUtils.encrypt("0.00"));
					}
					
					//C.当月工作日
					shouldWorkDays = shouldWorkDays.add(new BigDecimal(configShouldWorkDays));
					erpSalaryMonthPerformanceToUpdate.setErpMonthShouldWorkDays(shouldWorkDays);
					
					//D.实际工作天数【再从月度考勤中更新一下】
					actualWorkDays = actualWorkDays.add(attendanceActualWorkDays);
					erpSalaryMonthPerformanceToUpdate.setErpMonthActualWorkDays(actualWorkDays);

					//F.本月实际应发工资套改绩效计算：本月工资套改绩效 * (实际工作天数/当月工作日)
					BigDecimal tempDecimal = meritPay.multiply(actualWorkDays);
						
					actualMeritPay = shouldWorkDays.doubleValue() == 0.0 ? new BigDecimal(0.0) : actualMeritPay.add(tempDecimal.divide(shouldWorkDays,2, BigDecimal.ROUND_HALF_UP));
					erpSalaryMonthPerformanceToUpdate.setErpMonthActualMeritPay(AesUtils.encrypt(actualMeritPay.setScale(2,BigDecimal.ROUND_HALF_UP).toString()));
										
					//G.当月发放绩效合计 当月发放绩效合计==项目绩效+本月应实际发方的工资套改绩效+餐交补+手机话费补助
					meritSum = meritSum.add(projectPay).add(actualMeritPay).add(mealSubsidy).add(telSubsidy);
					erpSalaryMonthPerformanceToUpdate.setErpMonthMeritSum(AesUtils.encrypt(String.valueOf(meritSum.setScale(2, BigDecimal.ROUND_HALF_UP).toString())));
					
					//月度绩效ID
					erpSalaryMonthPerformanceToUpdate.setErpMonthId(erpSalaryMonthPerformance.getErpMonthId());
					
					//3-3.完成月度绩效的更新操作
					erpMonthPerformanceMapper.updateErpMonthPerformance(erpSalaryMonthPerformanceToUpdate);
					break;
				}
			}				
		}
		
		//3-plus. 将月度绩效的初始化状态数据进行更新
		Map<String, Object> paramsPerformanceApp = new HashMap<String, Object>();
		paramsPerformanceApp.put("month", month);
		paramsPerformanceApp.put("statusInit", 1);
		for(Integer dept : deptsInit) {
			paramsPerformanceApp.put("firstdepartmentId", dept);
			erpMonthPerformanceMapper.updateFirstDepartmentMonthPerStatus(paramsPerformanceApp);
		}
		
		//4.做Log记录
		String content = "所有月度绩效-初始化月度绩效！";
		this.logPerformanceOperatation(token,month,content);
		
		logger.info("completePerformance：End 经管初始化月度绩效！");				
		returnMessage.put("Message", "月度绩效初始化完毕");
		return RestUtils.returnSuccess(returnMessage, "OK");
	}
	
	/**
	 * Description:将员工的月度考勤List中，对每一个条月度考勤，新增员工状态
	 * @return：List<Map<String,Object>> 
	 * @param  params：List<Map<String,Object>> 员工的月度考勤数据，List<Map<String, Object>> employeeList员工列表数据
	 * @Author Songxiugong
	 * @Create Date: 2020年03月04日
	 */
	public List<Map<String,Object>> attendanceAddEmployeeStatus(List<Map<String,Object>> erpAttendanceList, List<Map<String, Object>> employeeList){
		logger.info("attendanceAddEmployeeStatus Begin");
		try {
			for(int iplus = 0; iplus < erpAttendanceList.size(); iplus++) {
				
				Map<String,Object> erpAttendance =(Map<String,Object>) erpAttendanceList.get(iplus);
				Integer employeeID = Integer.valueOf(String.valueOf(erpAttendance.get("employeeId")));
				erpAttendance.put("employeeStatus", "");
				
				for(int jplus = 0; jplus < employeeList.size(); jplus++) {
					
					Map<String,Object> employee =(Map<String,Object>) employeeList.get(jplus);
					Integer employeeIDInHR = Integer.valueOf(String.valueOf(employee.get("employeeId")));
					if(employeeID.intValue() == employeeIDInHR.intValue()) {
						String status = employee.get("status") == null ? "" : String.valueOf(employee.get("status"));
						erpAttendance.put("employeeStatus", status);
						break;
					}
				}
			}
		}catch(Exception e) {
			logger.error("attendanceAddEmployeeStatus:" + e.getMessage(), e);
		}
		logger.info("attendanceAddEmployeeStatus End");
		return erpAttendanceList;
	}
	
	/**
	 * Description: 经管导出月度绩效新功能
	 * @return：Excel
	 * @param  params：Keys: month
	 * @Author Songxiugong
	 * @Create Date: 2020年02月24日
	 */
	@Transactional
	public RestResponse exportMonthPerformanceNew(String token, Map<String,Object>params) throws Exception{
		logger.info("进入exportMonthPerformanceNew方法！");
		//logger.info("测试common===="+StringUtil.isNumber("123"));
//		try {
			//导出响应状态  0：导出Excel失败，有提示信息；1：导出Excel成功。
			HttpServletResponse responseForError = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
			responseForError.addHeader("status", "0");
			
			//1.判断当前登陆人是否为经管，如果不是不允许提交
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();//用户Id
			String employeeName = erpUser.getEmployeeName();//从用户信息中获取员工姓名
			String username = erpUser.getUsername();//从用户信息中获取用户名
			List<Integer> roles = erpUser.getRoles();//从用户信息中获取角色信息
			logger.info("id="+id+",username="+username+",roles="+roles);
			
			if(!roles.contains(7)) {
				Map<String,Object> returnMessageForRole = new HashMap<String, Object>();
				returnMessageForRole.put("Message", "您无权限执行操作");
				return RestUtils.returnSuccess(returnMessageForRole, "OK");
			}
			
			//1-1 Plus 判断选择的日期是否为当前月份或者上一个月份
			String month = String.valueOf(params.get("month"));
			Map<String, Object> alert = new HashMap<String, Object>();	//提示信息
			if(!this.isPreOrCurMonth(this.stringToDate(month + "-01"))) {
				alert.put("Message", "只能导出当前月份或者上个月份的绩效!");
				return RestUtils.returnSuccess(alert, "OK");
			}
			
			//2.查看所有部门的月度绩效是否为锁定状态，如果不是锁定状态，不允许导出绩效并提示用户（2020-05-25 改为只导出:考勤状态为导出状态、初始化过的、绩效状态为锁定的数据）
			//2-1.根据月份查询一级部门的月度绩效状态，调用接口：findAllErpMonthPerformanceByPowerParams
			Map<String,Object> valueError = new HashMap<String, Object>();
			RestResponse responseOfPerformanceStatus =  this.queryDepartMentListAndPerformanceStatus(month,token);
			
			if(!("200".equals(responseOfPerformanceStatus.getStatus()))){
				valueError.put("Message", "获取月度绩效失败，请联系管理员");
				return RestUtils.returnSuccess(valueError, "OK");
				
			} 
			
			Map<String,Object> data = (Map<String,Object>)responseOfPerformanceStatus.getData();
			if(data.isEmpty()) {
				valueError.put("Message", "获取月度绩效失败，请联系管理员");
				return RestUtils.returnSuccess(valueError, "OK");
			}
			
			List<Map<String,Object>> responseOfPerformanceStatusList  =  (List<Map<String,Object>>)(data.get("depts"));
						
			if(responseOfPerformanceStatusList == null || responseOfPerformanceStatusList.size() == 0) {
				valueError.put("Message", "月度绩效没有提交，不允许导出");
				return RestUtils.returnSuccess(valueError, "OK");
			}

			List<Integer> exportDepartmentIdList = new ArrayList<>();

			//2-2.如果不是锁定状态，不允许导出绩效并提示用户;（2020-05-25 改为只导出:考勤状态为导出状态、初始化过的、绩效状态为锁定的数据）
			for(Map<String,Object> performanceStatus : responseOfPerformanceStatusList){
				
				String temp = performanceStatus.get("status") == null || String.valueOf(performanceStatus.get("status")).equals("") 
						|| String.valueOf(performanceStatus.get("status")).equals("null") ? "0" : String.valueOf(performanceStatus.get("status"));

				String statusInit = performanceStatus.get("statusInit") == null || String.valueOf(performanceStatus.get("statusInit")).equals("")
						|| String.valueOf(performanceStatus.get("statusInit")).equals("null") ? "0" : String.valueOf(performanceStatus.get("statusInit"));

				String attendanceStatus = performanceStatus.get("attendanceStatus") == null || String.valueOf(performanceStatus.get("attendanceStatus")).equals("")
						|| String.valueOf(performanceStatus.get("attendanceStatus")).equals("null") ? "0" : String.valueOf(performanceStatus.get("attendanceStatus"));

				String departmentId = performanceStatus.get("departmentId") == null || String.valueOf(performanceStatus.get("departmentId")).equals("")
						|| String.valueOf(performanceStatus.get("departmentId")).equals("null") ? "0" : String.valueOf(performanceStatus.get("departmentId"));

/*				if(Integer.valueOf(temp).intValue() != 3 && Integer.valueOf(temp).intValue() != 4 ) {
					valueError.put("Message", "月度绩效没有锁定，不允许导出");
					return RestUtils.returnSuccess(valueError, "OK");
				}*/
				//（2020-05-25 改为只导出:考勤状态为导出状态、初始化过的、绩效状态为锁定的数据）
				if((Integer.valueOf(temp).intValue() == 3 || Integer.valueOf(temp).intValue() == 4) && Integer.valueOf(statusInit).intValue() == 1  && Integer.valueOf(attendanceStatus).intValue() == 2) {
					exportDepartmentIdList.add(Integer.valueOf(departmentId));

				}
			}
			//params.put("exportDepartmentIdList", exportDepartmentIdList);
			//add by 2020-06-10 按照前端传输部门id列表进行导出
			List<Integer> firDepIds=(List<Integer>)params.get("firDepIds");//接收前端一级部门id列表
			if(exportDepartmentIdList.containsAll(firDepIds)){
				params.put("exportDepartmentIdList", firDepIds);
			}else{
				valueError.put("Message", "不符合月度绩效导出条件，请重新导出！");
				return RestUtils.returnSuccess(valueError, "OK");
			}
			
			//3.与展示所有月度绩效的页面统一   调用展示接口
			RestResponse response = this.findAllErpMonthPerformanceByPowerParams(token,params);
			if(!("200".equals(response.getStatus()))){
				valueError.put("Message", "月度绩效查询异常,请联系管理员");
				return RestUtils.returnSuccess(valueError, "OK");
			} 
						
			if(!response.getMsg().toUpperCase().equals("OK")) {
				valueError.put("Message", ((Map<String,Object>)response.getData()).get("Message"));
				return RestUtils.returnSuccess(valueError, "OK");
			}
			
			//4.解析data并导出Excel
			List<Map<String,Object>> returnList  = (List<Map<String,Object>> )(((Map<String,Object>)response.getData()).get("employeList"));
		
			//4-1.将人员的月度考勤状态更新
			Map<String,Object> updateValue = new HashMap<String, Object>();
			updateValue.put("month",month);
			updateValue.put("status","4");
			updateValue.put("modifiedTime",ExDateUtils.getCurrentDateTime());
			updateValue.put("type","archive");
			updateValue.put("firstdepartmentIds", firDepIds);
			//4-2.更新状态为4
			this.changeCurMonthPerformanceType(updateValue,token);
			
			//4-3.做Log记录
			String content = "导出月度绩效！";
			this.logPerformanceOperatation(token,month,content);
			
			//4-4.导出Excel
			return this.exportPerformanceAsExcel(returnList);
//		}catch(Exception e) {
//			logger.error("exportMonthPerformanceNew:" + e.getMessage(), e);
//			Map<String,Object> valueError = new HashMap<String, Object>();
//			valueError.put("message", e.getMessage());
//			return RestUtils.returnFailure(valueError, "Error");
//		}
	}

	/* *************************************** 月度绩效新增接口逻辑 End***************************************************** */
	
	/* *************************************** 中间方法 Begin*************************************** */
	/**
	 * Description: 获取当前月份第一天日期 ，格式YYYY-MM-01
	 *         
	 * @return 当前的日期 String ，格式YYYY-MM-01
	 * @Author songxiugong
	 * @Create Date: 2020年03月01日
	 */
	public String getCurYearMonthFirstDay(){
		
		String dateReturn = null;
		try {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			
			String monthStr = month > 9 ? String.valueOf(month) :"0" + String.valueOf(month);
			
			dateReturn = String.valueOf(year) + "-" + monthStr + "-01";
		}catch(Exception e) {
			logger.error("getCurYearMonthFirstDay 出现异常:" + e.getMessage(), e);
		}
		return dateReturn;
	}
	
	/**
	 * Description: 写入日志
	 * 
	 * @param token,yearMonth,content【日志内容】
	 * 
	 * @Author Songxiugong
	 * @Create Date: 2020年02月23日
	 */
	public void logPerformanceOperatation(String token,String yearMonth, String content) {
		try {
			//3.增加月度绩效的新增记录
			ErpUser userInfo = (ErpUser) this.redisTemplate.opsForValue().get(token);
			String employeeName = userInfo.getEmployeeName();//员工姓名
			String username = userInfo.getUsername();//用户名
			String processor = employeeName==null||"".equals(employeeName)?username:employeeName;//新增月度绩效的操作人
			ErpSalaryMonthPerformanceRecord salaryMonthPerformanceRecord = new ErpSalaryMonthPerformanceRecord();
//			salaryMonthPerformanceRecord.setFirstDepartmentId(firstDeptId);
			salaryMonthPerformanceRecord.setMonth(yearMonth);
			salaryMonthPerformanceRecord.setProcessor(processor);
			salaryMonthPerformanceRecord.setTime(ExDateUtils.getCurrentStringDateTime());
			salaryMonthPerformanceRecord.setContent(content);
			salaryMonthPerformanceRecordMapper.insertRecord(salaryMonthPerformanceRecord);
		}catch(Exception e) {
			logger.error("logPerformanceOperatation:" + e.getMessage(), e);
		}
	}	
	
	/**
	 * Description: 导出Excel
	 * 
	 * @param List<Map<String, Object>> resultList
	 * @Author Songxiugong
	 * @Create Date: 2020年02月23日
	 */
	public RestResponse exportPerformanceAsExcel(List<Map<String, Object>> monthPerformanceList) {
		logger.info("进入exportPerformanceAsExcel方法，参数是：monthPerformanceList="+monthPerformanceList);
		//InputStream resourceAsStream = this.getClass().getResourceAsStream("/template/月度绩效.xlsx");
		XSSFWorkbook workBook = null;
		try {
			//workBook = new XSSFWorkbook(resourceAsStream); //制成book
			//XSSFSheet sheet = workBook.getSheetAt(0);
			
			workBook = new XSSFWorkbook(); //制成book
			XSSFSheet sheet = workBook.createSheet("月度绩效");
			// 生成第一行
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("财务序号");
			firstRow.createCell(1).setCellValue("姓名");
			firstRow.createCell(2).setCellValue("身份证号");
			
			firstRow.createCell(3).setCellValue("二级部门");
			firstRow.createCell(4).setCellValue("三级部门");
			
			firstRow.createCell(5).setCellValue("项目绩效");
			firstRow.createCell(6).setCellValue("工资套改绩效");
			firstRow.createCell(7).setCellValue("餐交补");
			firstRow.createCell(8).setCellValue("手机话费补助");
			
			firstRow.createCell(9).setCellValue("当月工作日");
			firstRow.createCell(10).setCellValue("实际工作天数");
			
			firstRow.createCell(11).setCellValue("本月实际应发工资套改绩效");
			firstRow.createCell(12).setCellValue("当月发放绩效合计");
			firstRow.createCell(13).setCellValue("离职日期");
	
			if(monthPerformanceList == null || monthPerformanceList.size() == 0) {
				this.exportExcelToComputer(workBook);
				return RestUtils.returnSuccessWithString("导出成功！");
			}
			//下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			//循环 填充表格
			for (int i = 0; i < monthPerformanceList.size(); i++) {
				param = monthPerformanceList.get(i);
				nextRow = sheet.createRow(i+1);//从第二行开始 - 表头占据一行
				
				nextRow.createCell(0).setCellValue(param.get("empFinanceNumber") == null || String.valueOf(param.get("empFinanceNumber")).equals("null") ? "": String.valueOf(param.get("empFinanceNumber")));//员工月度绩效财务序号
				nextRow.createCell(1).setCellValue(param.get("name") == null || String.valueOf(param.get("name")).equals("null") ? "" : String.valueOf(param.get("name")));//姓名
				nextRow.createCell(2).setCellValue(param.get("idCardNumber") == null || String.valueOf(param.get("idCardNumber")).equals("null") ? "" : String.valueOf(param.get("idCardNumber")));		//身份证号

				nextRow.createCell(3).setCellValue(param.get("firstDeptName") == null || String.valueOf(param.get("firstDeptName")).equals("null") ? "" : String.valueOf(param.get("firstDeptName")));		//二级部门
				nextRow.createCell(4).setCellValue(param.get("secondDeptName") == null || String.valueOf(param.get("secondDeptName")).equals("null") ? "" : String.valueOf(param.get("secondDeptName")));	//三级部门
				
				nextRow.createCell(5).setCellValue(param.get("erpMonthProjectPay") == null || String.valueOf(param.get("erpMonthProjectPay")).equals("null") ? "" : String.valueOf(param.get("erpMonthProjectPay")));	//项目绩效
				nextRow.createCell(6).setCellValue(param.get("erpMonthMeritPay") == null || String.valueOf(param.get("erpMonthMeritPay")).equals("null") ? "" : String.valueOf(param.get("erpMonthMeritPay")));		//工资套改绩效
				nextRow.createCell(7).setCellValue(param.get("erpMonthMealSubsidy") == null || String.valueOf(param.get("erpMonthMealSubsidy")).equals("null") ? "" : String.valueOf(param.get("erpMonthMealSubsidy")));//餐交补
				nextRow.createCell(8).setCellValue(param.get("erpMonthTelSubsidy") == null || String.valueOf(param.get("erpMonthTelSubsidy")).equals("null") ? "" : String.valueOf(param.get("erpMonthTelSubsidy")));//手机话费补助
				
				nextRow.createCell(9).setCellValue(param.get("erpMonthShouldWorkDays") == null || String.valueOf(param.get("erpMonthShouldWorkDays")).equals("null") ? "" : String.valueOf(param.get("erpMonthShouldWorkDays")));//当月工作日
				nextRow.createCell(10).setCellValue(param.get("erpMonthActualWorkDays") == null || String.valueOf(param.get("erpMonthActualWorkDays")).equals("null") ? "" : String.valueOf(param.get("erpMonthActualWorkDays")));//实际工作天数
				
				nextRow.createCell(11).setCellValue(param.get("erpMonthActualMeritPay") == null || String.valueOf(param.get("erpMonthActualMeritPay")).equals("null") ? "" : String.valueOf(param.get("erpMonthActualMeritPay")));	//本月实际应发工资套改绩效
				nextRow.createCell(12).setCellValue(param.get("erpMonthMeritSum") == null || String.valueOf(param.get("erpMonthMeritSum")).equals("null") ? "" : String.valueOf(param.get("erpMonthMeritSum")));
				nextRow.createCell(13).setCellValue(param.get("dimissionTime") == null || String.valueOf(param.get("dimissionTime")).equals("null") ? "" : String.valueOf(param.get("dimissionTime")));//离职日期

			}
			this.exportExcelToComputer(workBook);
			return RestUtils.returnSuccessWithString("导出成功！");
		} catch (Exception e) {
			logger.error("exportPerformanceAsExcel发生异常："+e.getMessage(),e);
			
			HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
			response.setHeader("status", "0");
			
			Map<String,Object> valueError = new HashMap<String, Object>();
			valueError.put("Message", "导出失败");
			return RestUtils.returnFailure(valueError, "Error");
		}
	}
	
	/**
	 * Description: 进行为"" 或者 "null" 或者 null 判断并转化数据，返回类型为String
	 * 返回值：String 
	 * 	
	 * @param  Object：一个字符串
	 * @Author Songxiugong
	 * @Create Date: 2020年02月24日
	 */
	public String formartValue(Object value) {
//		logger.info("进入formartValue方法！");
		try {
			String returnValue;
			returnValue = value == null || String.valueOf(value).equals("") || String.valueOf(value).equals("null") ? "0" : String.valueOf(value);
			return returnValue;
		}catch(Exception e) {
			logger.error("formartValue:" + e.getMessage(), e);
			return "0";
		}
	}
	
	/**
	 * Description: 返回错误信息
	 * @return：RestResponse 
	 * 	
	 * @param  content：错误内容
	 * @Author Songxiugong
	 * @Create Date: 2020年02月26日
	 */
	public RestResponse returnErrorInfo(String content) throws Exception{
		Map<String,Object> valueError = new HashMap<String, Object>();
		valueError.put("Message", content);
		return RestUtils.returnFailure(valueError, "Error");
	}
	
	/**
	 * Description: A：获取有效的一级部门的指定月份的月度考勤状态和月度绩效状态
	 * Logic & 返回值		
	 * 				1.先判断所有一级部门的考勤是否已经导出，如果没有导出，则返回 0
	 * 			    2.所有一级部门的考勤已经导出，有部门未提交，则返回 1
	 *  		    3.所有一级部门的考勤已经导出，有部门已经锁定，则返回 3
	 *  		    4.所有一级部门的考勤已经导出，有部门已经导出，则返回 4
	 *  		    5.所有一级部门的考勤已经导出，所有部门已经提交，则返回 2
	 *  			6.代码错误及其它异常 -1
	 * @param  Map：key=month
	 * @Author Songxiugong
	 * @Create Date: 2020年02月24日
	 * @modified Date: 2020年03月02日
	 */
	public int canInitMonthPerformance(String month, String token) {
		
		try {
			logger.info("进入canInitMonthPerformance方法！");
							
			//一、如果A结果的考勤为部分导出（或者没有导出 或者没有提交 ），则不允许初始化月度绩效；
			//1. 跨project工程调用获取部门及月度考勤信息
			
			List<Map<String,Object>> firstDepartmentList = null;	//一级部门信息
			Map<String,Object> params = new HashMap<String, Object>();
			
			//没有填写，默认为当前月份
			if(month ==null || month.equals("") || month.equals("null")) {
				Calendar calendar = Calendar.getInstance();
				Integer year = calendar.get(Calendar.YEAR);
				Integer imonth = calendar.get(Calendar.MONTH) + 1;
				
				String myMonth = imonth > 9 ? String.valueOf(imonth) : "0" + String.valueOf(imonth);
				SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM");
				month = formatYMD.format(formatYMD.parse(String.valueOf(year) + "-" + myMonth));
			}
			params.put("month", month);
			
			RestResponse response = this.queryDepartMentListAndPerformanceStatus(month,token);
			if(!response.getStatus().equals("200")) {
				return -1;
			}
			
//			String url = protocolType + "nantian-erp-project/nantian-erp/project/adminDic/queryDepartMentListAndAttendanceStatus?month=" + month;
//			HttpHeaders requestHeaders = new HttpHeaders();
//			requestHeaders.add("token", token);
//			
//			HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(null,requestHeaders);
//
//			ResponseEntity<RestResponse> response = this.restTemplate.exchange(url, HttpMethod.GET,request,RestResponse.class);
//			if (!("200".equals(response.getBody().getStatus()))) {
//				return 1;
//			}
			
			// 1-1.解析请求的结果
			Map<String,Object> data = (Map<String,Object>)response.getData();
			if(data.isEmpty()) {
				return 0;
			}

			firstDepartmentList = (List<Map<String,Object>>)(data.get("depts"));	//集团的信息
			if(firstDepartmentList == null || firstDepartmentList.size() == 0 ) {
				return 0;
			}
			
			//1-2.判断所有有效的本部一级部门是否都已经全部导出了月度考勤，没有的话返回false
			for (Map<String,Object> firstDepartment : firstDepartmentList){
				String attendanceStatus = firstDepartment.get("attendanceStatus") == null ?
						"" : String.valueOf(firstDepartment.get("attendanceStatus"));
				String departmentId = firstDepartment.get("departmentId") == null ?
						"" : String.valueOf(firstDepartment.get("departmentId"));
				String departmentName = firstDepartment.get("departmentName") == null ? 
						"" : String.valueOf(firstDepartment.get("departmentName")) ;
				
				if(!attendanceStatus.equals("2") || attendanceStatus.equals("") || departmentId.equals("")) {
					return 0;
				}
			}
			
			//二、如果A结果的考勤为全部导出，对应的月度绩效不是全部提交的，返回提示;
			//2.根据月份查询一级部门的月度绩效状态
			//List<Map<String,Object>> performanceStatusList= erpSalaryMonthPerformanceMapper.findMonthPerformanceApplyByParams(params);
						
			//2-1.状态码 并判断所有月度绩效是否为“提交”状态
			Map<Integer, Integer> departmentStatusMap = new HashMap<>();
			for(Map<String,Object> performanceStatus : firstDepartmentList){
				
				String firstdepartmentId = performanceStatus.get("departmentId") == null ? 
						"" : String.valueOf(performanceStatus.get("departmentId"));
						
				String status = performanceStatus.get("status") == null ? 
						"" : String.valueOf(performanceStatus.get("status"));
						
				if(firstdepartmentId.equals("") || firstdepartmentId.equals("null") ||
						status.equals("") || status.equals("null")) {
					return 0;
				}
				
				if(!status.equals("2")) {
					int statusInt = Integer.valueOf(status).intValue();
					if(statusInt == 3) {
						return 3;
					}else if(statusInt == 4) {
						return 4;
					}else if(statusInt == 1 || statusInt == -1) {
						return 1;
					}
					
				}
				
				departmentStatusMap.put(Integer.valueOf(firstdepartmentId), Integer.valueOf(status));			
			}
			
//			//三、如果A结果的考勤为全部导出, 月度绩效结果为全部提交，则允许初始化月度绩效。
//			// 2-2.比较A的部门ID都在B中，则允许提交
//						
//			for (Map<String,Object> firstDepartment : firstDepartmentList){
//				//
//				Integer firstDeptIDAttendance = Integer.valueOf(String.valueOf(firstDepartment.get("departmentId")));
//				Integer firstDeptStatusPerformance = departmentStatusMap.get(firstDeptIDAttendance);
//				if(firstDeptStatusPerformance == null) {
//					return 3;
//				}
//			}
			return 2;
			
		} catch (Exception e) {
			logger.error("canInitMonthPerformance:" + e.getMessage(), e);
			return -1;
		}
	}
	
	/**
	 * Description: A：获取有效的一级部门的指定月份的月度考勤状态；B：获取有效一级部门领导在指定月份已经填写月度绩效状态
	 *   			      考勤总的状态A=0逻辑：只要有一个一部们考勤状态为0
	 *    			      考勤总的状态A=1逻辑：所部一级部门考勤状态为1
	 *    			      考勤总的状态A=2逻辑：所有一级部门考勤状态为2
	 *    			      考勤总的状态A=-1逻辑：其它及异常情况

	 *   			      绩效总的状态B=0逻辑：只要有一个一部门月度绩效状态为0	//实际没有此状态
	 *    			      绩效总的状态A=1逻辑：所部一级部门绩效状态为1
	 *    			      绩效总的状态A=2逻辑：所部一级部门绩效状态为2
	 *    			      绩效总的状态A=3逻辑：所部一级部门绩效状态为3
	 *         		      绩效总的状态A=4逻辑：所部一级部门绩效状态为4
	 *                 绩效总的状态A=4逻辑：：其它及异常情况
	 *         
	 * 返回值：Map 	key:attendanceStatus, value:   0：未提交 1：已提交 2：已导出/已锁定
	 * 		   		key:performanceStatus value:   0：未提交 1：修改中(部分修改中或者全部修改中)，2：已提交，3：锁定，4：归档
	 * @param  Map：key=month
	 * @Author Songxiugong
	 * @Create Date: 2020年02月27日
	 */
	public Map<String, Integer> attendanceAndPerformanceSummary(String month, Integer firstdepartmentId, String token) {
		
		try {
			logger.info("进入canInitMonthPerformance方法！");
			Map<String, Integer> summary = new HashMap<String, Integer>();	//返回结果
			//1. 跨project工程调用获取部门及月度考勤信息
			
			List<Map<String,Object>> firstDepartmentList = null;	//一级部门信息
			Map<String,Object> params = new HashMap<String, Object>();
			
			//没有填写，默认为当前月份
			if(month ==null || month.equals("") || month.equals("null")) {
				Calendar calendar = Calendar.getInstance();
				Integer year = calendar.get(Calendar.YEAR);
				Integer imonth = calendar.get(Calendar.MONTH) + 1;
				
				String myMonth = imonth > 9 ? String.valueOf(imonth) : "0" + String.valueOf(imonth);
				SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM");
				month = formatYMD.format(formatYMD.parse(String.valueOf(year) + "-" + myMonth));
			}
			params.put("month", month);
						
			RestResponse response = this.queryDepartMentListAndPerformanceStatus(month, token);
			// 1-1.解析请求的结果
			if(!(response.getStatus().equals("200"))) {
				summary.put("attendanceStatus", -1);
				summary.put("performanceStatus", -1);
				return summary;
			}
			
			Map<String,Object> data = (Map<String,Object>)response.getData();
			if(data.isEmpty()) {
				summary.put("attendanceStatus", -1);
				summary.put("performanceStatus", -1);
				return summary;
			}
			
			firstDepartmentList  =  (List<Map<String,Object>>)(data.get("depts"));
			if(firstDepartmentList == null || firstDepartmentList.size() == 0 ) {
				summary.put("attendanceStatus", -1);
				summary.put("performanceStatus", -1);
				return summary;
			}

			if(firstdepartmentId == null){
				// 2. 考勤总的各种状态判断
				// 考勤总的状态A=0逻辑：只要有一个一部们考勤状态为0
				for (Map<String,Object> firstDepartment : firstDepartmentList){
					String attendanceStatus = firstDepartment.get("attendanceStatus") == null ? "" : String.valueOf(firstDepartment.get("attendanceStatus"));
					String departmentId = firstDepartment.get("departmentId") == null ? "" : String.valueOf(firstDepartment.get("departmentId"));
					String departmentName = firstDepartment.get("departmentName") == null ?  "" : String.valueOf(firstDepartment.get("departmentName")) ;

					if(attendanceStatus.equals("0") || attendanceStatus.equals("") || departmentId.equals("")) {
						summary.put("attendanceStatus", 0);
						summary.put("performanceStatus", 1);
						return summary;
					}
				}

				//考勤总的状态A=1逻辑：所部一级部门考勤状态为1
				int allFirstDepts = firstDepartmentList.size();	//所有有效一级部门数目

				if(attendanceStatusCheck(firstDepartmentList,"1")) {
					summary.put("attendanceStatus", 1);
					summary.put("performanceStatus", 1);
					return summary;
				}

				//考勤总的状态A=2逻辑：所部一级部门考勤状态为2
				if(attendanceStatusCheck(firstDepartmentList,"2")) {
					summary.put("attendanceStatus", 2);
					summary.put("performanceStatus", 1);
				}

				//3. 月度绩效总的各种状态判断

				// 考勤总的状态逻辑：只要有一个一部们考勤状态为1
				for (Map<String,Object> firstDepartment : firstDepartmentList){
					String performanceStatus = firstDepartment.get("status") == null ?
							"" : String.valueOf(firstDepartment.get("status"));

					if(performanceStatus.equals("1") || performanceStatus.equals("") || performanceStatus.equals("")) {
						summary.put("attendanceStatus", 2);
						summary.put("performanceStatus", 1);
						return summary;
					}
				}

				if(performanceStatusCheck(firstDepartmentList,"2")) {
					summary.put("attendanceStatus", 2);
					summary.put("performanceStatus", 2);
					return summary;
				}

				if(performanceStatusCheck(firstDepartmentList,"3")) {
					summary.put("attendanceStatus", 2);
					summary.put("performanceStatus", 3);
					return summary;
				}

				if(performanceStatusCheck(firstDepartmentList,"4")) {
					summary.put("attendanceStatus", 2);
					summary.put("performanceStatus", 4);
					return summary;
				}
				//其它情况
				summary.put("attendanceStatus", 0);
				summary.put("performanceStatus", 1);
				return summary;
			}else{
				//查询指定部门的考勤与绩效状态
				for(Map<String,Object> firstDepartment : firstDepartmentList){
					String attendanceStatus = firstDepartment.get("attendanceStatus") == null ? "" : String.valueOf(firstDepartment.get("attendanceStatus"));
					String departmentId = firstDepartment.get("departmentId") == null ? "" : String.valueOf(firstDepartment.get("departmentId"));
					if(departmentId.equals(String.valueOf(firstdepartmentId))){
						//指定部门 考勤状态
						if(attendanceStatus.equals("0") || attendanceStatus.equals("") || departmentId.equals("")) {
							summary.put("attendanceStatus", 0);
							summary.put("performanceStatus", 1);
							return summary;
						}else if(attendanceStatus.equals("1")){
							summary.put("attendanceStatus", 1);
							summary.put("performanceStatus", 1);
							return summary;
						}else if(attendanceStatus.equals("2")){
							summary.put("attendanceStatus", 2);
						}else {
							summary.put("attendanceStatus", 0);
							summary.put("performanceStatus", 1);
						}
						//绩效状态
						String performanceStatus = firstDepartment.get("status") == null ? "" : String.valueOf(firstDepartment.get("status"));
						if(performanceStatus.equals("1") || performanceStatus.equals("") || performanceStatus.equals("")) {
							summary.put("attendanceStatus", 2);
							summary.put("performanceStatus", 1);
							return summary;
						}else if(performanceStatus.equals("2")){
							summary.put("attendanceStatus", 2);
							summary.put("performanceStatus", 2);
							return summary;
						}else if(performanceStatus.equals("3")){
							summary.put("attendanceStatus", 2);
							summary.put("performanceStatus", 3);
							return summary;
						}else if(performanceStatus.equals("4")){
							summary.put("attendanceStatus", 2);
							summary.put("performanceStatus", 4);
							return summary;
						}else{
							summary.put("attendanceStatus", 0);
							summary.put("performanceStatus", 1);
						}
						return summary;
					}
				}
			}
			summary.put("attendanceStatus", 0);
			summary.put("performanceStatus", 1);
			return summary;
		} catch (Exception e) {
			logger.error("canInitMonthPerformance:" + e.getMessage(), e);
			Map<String, Integer> valueError = new HashMap<String, Integer>();
			valueError.put("Error", -1);
			return valueError;
		}
	}
	

	/**
	 * Description: 查看所有一级部门的考勤是否全部为指定状态
	 *         
	 * @return：true 为指定状态， false 不是指定状态
	 * @param  List<Map<String,Object>> firstDepartmentList, String status
	 * @Author Songxiugong
	 * @Create Date: 2020年02月27日
	 */
	public boolean attendanceStatusCheck(List<Map<String,Object>> firstDepartmentList, String status) {
		
		try {
			//考勤总的状态A= 指定状态判断
			int allFirstDepts = firstDepartmentList.size();	//所有有效一级部门数目
			int count = 0;
			for (Map<String,Object> firstDepartment : firstDepartmentList){
				String attendanceStatus = firstDepartment.get("attendanceStatus") == null ? 
						"" : String.valueOf(firstDepartment.get("attendanceStatus"));
	
				if(attendanceStatus.equals(status) ) {
					count ++;
				}
			}
			
			if(count == allFirstDepts) {
				return true;
			}else{
				return false;
			}
		}catch(Exception e) {
			logger.error("attendanceStatusCheck:" + e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Description: 查看所有一级部门的绩效是否全部为指定状态
	 *         
	 * @return：true 为指定状态， false 不是指定状态
	 * @param  List<Map<String,Object>> firstDepartmentList, String status
	 * @Author Songxiugong
	 * @Create Date: 2020年02月27日
	 */
	public boolean performanceStatusCheck(List<Map<String,Object>> firstDepartmentList, String status) {
		
		try {
			//考勤总的状态A= 指定状态判断
			int allFirstDepts = firstDepartmentList.size();	//所有有效一级部门数目
			int count = 0;
			for (Map<String,Object> firstDepartment : firstDepartmentList){
				String performanceStatus = firstDepartment.get("status") == null ? 
						"" : String.valueOf(firstDepartment.get("status"));
				
				if(performanceStatus.equals(status) ) {
					count ++;
				}
			}
			
			if(count == allFirstDepts) {
				return true;
			}else{
				return false;
			}
		}catch(Exception e) {
			logger.error("performanceStatusCheck:" + e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Description: 获取选的指定日期的上一个月份及月份第一天
	 *         
	 * @return：获取选的指定日期的上一个月份 String  YYYY-MM-01
	 * @param  Date YYYY-MM-01
	 * @Author Songxiugong
	 * @Create Date: 2020年03月02日
	 */
	public Date getPreviousMonthDay(String yearMonthDay) {
		Date preMonth = null;
		try{			
			
			//String To Date 
			String strFormat = "yyyy-MM-dd";
			SimpleDateFormat format = new SimpleDateFormat(strFormat);
			Date date = format.parse(yearMonthDay);
			
			//Date To Calendar
			Calendar cldar = Calendar.getInstance();
			cldar.setTime(date);
			
			//Month subtraction
			cldar.add(Calendar.MONTH, -1);
			
			preMonth = cldar.getTime();
			
			
			//Calendar To String
//			int year =  cldar.get(Calendar.YEAR);
//			int month =  cldar.get(Calendar.MONTH) + 1;
//			String monthStr = month > 9 ? String.valueOf(month) :"0" + String.valueOf(month);
//			
//			preMonth = String.valueOf(year) + "-" + monthStr + "-01";
			
		}catch(Exception e) {
			logger.error("getPreviousMonth:" + e.getMessage(), e);
		}
		return preMonth;
	}
	
	/**
	 * Description: String To Date
	 *         
	 * @return：Date 
	 * @param  String YYYY-MM-DD
	 * @Author Songxiugong
	 * @Create Date: 2020年03月02日
	 */
	public Date stringToDate(String dateStr) {
		Date date = null;
		try{			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			date = formatter.parse(dateStr);
			
		}catch(Exception e) {
			logger.error("stringToDate:" + e.getMessage(), e);
		}
		return date;
	}
	
	/**
	 * Description: 判断选定的月份是否为当前月份或者上一个月份
	 *         
	 * @return：boolean
	 * @param  String YYYY-MM-DD
	 * @Author Songxiugong
	 * @Create Date: 2020年03月02日
	 */
	public boolean isPreOrCurMonth(Date now) {
		boolean flag = false;
		try{			
			Date nowMonth, previousMonth;
			nowMonth = this.stringToDate(this.getCurYearMonthFirstDay());
			previousMonth = this.getPreviousMonthDay(this.getCurYearMonthFirstDay());
			
			if(now.getTime() == nowMonth.getTime() || now.getTime() == previousMonth.getTime()) {
				flag = true;
			}
		}catch(Exception e) {
			logger.error("isPreOrCurMonth:" + e.getMessage(), e);
		}
		return flag;
	}
	
	/* *************************************** 中间方法 End  *************************************** */
	
	
	/* *************************************** 封装的工具方法  *************************************** */
	/**
	 * Description: 导出-测试
	 * 
	 * @param workBook
	 * @throws IOException 
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月03日 下午15:37:46
	 */
	public void exportExcelToComputer(XSSFWorkbook workBook) throws IOException {
		/*
		 * 本地下载
		 */
		/*
		 * FileOutputStream fos = new FileOutputStream("C:\\NanTian\\test.xlsx");
		 * workBook.write(fos); fos.flush(); fos.close();
		 */
		/*
		 * 服务器下载
		 */
		
		 HttpServletResponse response =
		 ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).
		 getResponse(); response.addHeader("Content-Disposition",
		 "attachment;filename=SalaryMonthPerformance.xlsx");
		 response.setHeader("status", "1"); ServletOutputStream os; os =
		 response.getOutputStream(); workBook.write(os); os.flush(); os.close();
		 
	}
	
	/**
	 * Description: 薪酬数据加密
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月06日 下午15:02:36
	 */
	public Map<String,String> encryptPerformanceDataAes(Map<String,String> preformanceDataMap){
		//logger.info("进入加密方法preformanceDataMap="+preformanceDataMap);
		/*
		 * 获取到所有需要加密的薪酬数据
		 */
		String erpMonthMeritPay = preformanceDataMap.get("erpMonthMeritPay");//工资绩效
		String erpMonthProjectPay = preformanceDataMap.get("erpMonthProjectPay");//项目绩效
		String erpMonthDPay = preformanceDataMap.get("erpMonthDPay");//D类工资
		String erpMonthAllowance = preformanceDataMap.get("erpMonthAllowance");//月度补助
		/*
		 * 将薪酬加密后，赋值给Map
		 * 如果薪酬为空，那么赋值为0.0
		 */
		Map<String,String> encryptedPreformanceData = new HashMap<>();
		String defaultSalaryValue = AesUtils.encrypt(String.valueOf(0.00));
		if(erpMonthMeritPay.replaceAll(" ", "").isEmpty()) {
			encryptedPreformanceData.put("erpMonthMeritPay", defaultSalaryValue);
		}else {
			encryptedPreformanceData.put("erpMonthMeritPay", AesUtils.encrypt(erpMonthMeritPay));
		}
		if(erpMonthProjectPay.replaceAll(" ", "").isEmpty()) {
			encryptedPreformanceData.put("erpMonthProjectPay", defaultSalaryValue);
		}else {
			encryptedPreformanceData.put("erpMonthProjectPay", AesUtils.encrypt(erpMonthProjectPay));
		}
		if(erpMonthDPay.replaceAll(" ", "").isEmpty()) {
			encryptedPreformanceData.put("erpMonthDPay", defaultSalaryValue);
		}else {
			encryptedPreformanceData.put("erpMonthDPay", AesUtils.encrypt(erpMonthDPay));
		}
		if(erpMonthAllowance.replaceAll(" ", "").isEmpty()) {
			encryptedPreformanceData.put("erpMonthAllowance", defaultSalaryValue);
		}else {
			encryptedPreformanceData.put("erpMonthAllowance", AesUtils.encrypt(erpMonthAllowance));
		}
		return encryptedPreformanceData;
	}
	
	/**
	 * Description: 薪酬数据解密
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月26日 下午17:29:18
	 */
	public Map<String,Double> decryptPerformanceDataAes(Map<String,String> encryptedPerformanceData){
		//logger.info("进入解密方法encryptedPerformanceData="+encryptedPerformanceData);
		/*
		 * 获取到所有需要解密的薪酬数据
		 */
		String erpMonthBaseWage = AesUtils.decrypt(encryptedPerformanceData.get("erpMonthBaseWage"));//月度基本工资
		String erpMonthPostWage = AesUtils.decrypt(encryptedPerformanceData.get("erpMonthPostWage"));//月度岗位工资
		String erpMonthMeritPay = AesUtils.decrypt(encryptedPerformanceData.get("erpMonthMeritPay"));//工资绩效
		String erpMonthProjectPay = AesUtils.decrypt(encryptedPerformanceData.get("erpMonthProjectPay"));//项目绩效
		String erpMonthDPay = AesUtils.decrypt(encryptedPerformanceData.get("erpMonthDPay"));//D类工资
		String erpMonthAllowance = AesUtils.decrypt(encryptedPerformanceData.get("erpMonthAllowance"));//补助
		String erpMonthMealSubsidy=AesUtils.decrypt(encryptedPerformanceData.get("erpMonthMealSubsidy"));//餐交补
		String erpMonthTelSubsidy=AesUtils.decrypt(encryptedPerformanceData.get("erpMonthTelSubsidy"));//餐交补
		/*
		 * 将薪酬解密，将字符串类型转换为浮点型，赋值给Map
		 * 如果数据库该字段为空，那么赋值为0.0，为了合计薪酬使用
		 */
		Map<String,Double> decryptedPerformanceData = new HashMap<>();
		//因为String.valueof和解密过来的值是空值“null”,而不是空，所以不能值判断==null
		if(erpMonthBaseWage==null || erpMonthBaseWage.equals("null")) {
			decryptedPerformanceData.put("erpMonthBaseWage", 0.0);
		}else {
			decryptedPerformanceData.put("erpMonthBaseWage", Double.valueOf(erpMonthBaseWage));
		}
		if(erpMonthPostWage==null || erpMonthPostWage.equals("null")) {
			decryptedPerformanceData.put("erpMonthPostWage", 0.0);
		}else {
			decryptedPerformanceData.put("erpMonthPostWage", Double.valueOf(erpMonthPostWage));
		}
		if(erpMonthMeritPay==null || erpMonthMeritPay.equals("null")) {
			decryptedPerformanceData.put("erpMonthMeritPay", 0.0);
		}else {
			decryptedPerformanceData.put("erpMonthMeritPay", Double.valueOf(erpMonthMeritPay));
		}
		if(erpMonthProjectPay==null || erpMonthProjectPay.equals("null")) {
			decryptedPerformanceData.put("erpMonthProjectPay", 0.0);
		}else {
			decryptedPerformanceData.put("erpMonthProjectPay", Double.valueOf(erpMonthProjectPay));
		}
		if(erpMonthDPay==null || erpMonthDPay.equals("null")) {
			decryptedPerformanceData.put("erpMonthDPay", 0.0);
		}else {
			decryptedPerformanceData.put("erpMonthDPay", Double.valueOf(erpMonthDPay));
		}
		if(erpMonthAllowance==null||erpMonthAllowance.equals("null")) {
			decryptedPerformanceData.put("erpMonthAllowance", 0.0);
		}else {
			decryptedPerformanceData.put("erpMonthAllowance", Double.valueOf(erpMonthAllowance));
		}
		if(erpMonthMealSubsidy==null||erpMonthMealSubsidy.equals("null")) {
			decryptedPerformanceData.put("erpMonthMealSubsidy", 0.0);
		}else {
			decryptedPerformanceData.put("erpMonthMealSubsidy", Double.valueOf(erpMonthMealSubsidy));
		}
		if(erpMonthTelSubsidy==null||erpMonthTelSubsidy.equals("null")) {
			decryptedPerformanceData.put("erpMonthTelSubsidy", 0.0);
		}else {
			decryptedPerformanceData.put("erpMonthTelSubsidy", Double.valueOf(erpMonthTelSubsidy));
		}


		return decryptedPerformanceData;
	}


	/**
	 * Description: 解密月度绩效的加密字段
	 *
	 * @return ErpSalaryMonthPerformance 对象
	 * @Author songxiugong
	 * @Create Date: 2020年02月23日
	 */
	public ErpSalaryMonthPerformance decryptDataRsaObject(ErpSalaryMonthPerformance erpSalaryMonthPerformance) {
		try {

			//项目绩效
			String temp = erpSalaryMonthPerformance.getErpMonthProjectPay();
			temp = AesUtils.decrypt(temp);
			if(temp == null || temp.equals("null")){
				erpSalaryMonthPerformance.setErpMonthProjectPay("");
			}else {
				erpSalaryMonthPerformance.setErpMonthProjectPay(temp);
			}

			//工资绩效（工资套改绩效）
			temp = erpSalaryMonthPerformance.getErpMonthMeritPay();
			temp = AesUtils.decrypt(temp);
			if(temp == null || temp.equals("null")){
				erpSalaryMonthPerformance.setErpMonthMeritPay("");
			}else {
				erpSalaryMonthPerformance.setErpMonthMeritPay(temp);
			}

			//餐交补
			temp = erpSalaryMonthPerformance.getErpMonthMealSubsidy();
			temp = AesUtils.decrypt(temp);
			if(temp == null || temp.equals("null")){
				erpSalaryMonthPerformance.setErpMonthMealSubsidy("");
			}else {
				erpSalaryMonthPerformance.setErpMonthMealSubsidy(temp);
			}

			//手机话费补助
			temp = erpSalaryMonthPerformance.getErpMonthTelSubsidy();
			temp = AesUtils.decrypt(temp);
			if(temp == null || temp.equals("null")){
				erpSalaryMonthPerformance.setErpMonthTelSubsidy("");
			}else {
				erpSalaryMonthPerformance.setErpMonthTelSubsidy(temp);
			}

			//本月实际应发工资套改绩效
			temp = erpSalaryMonthPerformance.getErpMonthActualMeritPay();
			temp = AesUtils.decrypt(temp);
			if(temp == null || temp.equals("null")){
				erpSalaryMonthPerformance.setErpMonthActualMeritPay("");
			}else {
				erpSalaryMonthPerformance.setErpMonthActualMeritPay(temp);
			}

			//当月发放绩效合计
			temp = erpSalaryMonthPerformance.getErpMonthMeritSum();
			temp = AesUtils.decrypt(temp);
			if(temp == null || temp.equals("null")){
				erpSalaryMonthPerformance.setErpMonthMeritSum("");
			}else {
				erpSalaryMonthPerformance.setErpMonthMeritSum(temp);
			}


			//当月工作日
			BigDecimal bTemp = erpSalaryMonthPerformance.getErpMonthShouldWorkDays();
			if(bTemp == null){
				erpSalaryMonthPerformance.setErpMonthShouldWorkDays(BigDecimal.valueOf(0.00));
			}else {
				erpSalaryMonthPerformance.setErpMonthShouldWorkDays(bTemp);
			}

			//实际工作天数
			bTemp = erpSalaryMonthPerformance.getErpMonthActualWorkDays();
			if(bTemp == null){
				erpSalaryMonthPerformance.setErpMonthActualWorkDays(BigDecimal.valueOf(0.00));
			}else {
				erpSalaryMonthPerformance.setErpMonthActualWorkDays(bTemp);
			}

			return erpSalaryMonthPerformance;
		} catch (Exception e) {
			logger.error("decryptDataRsaObject:" + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 修改月度绩效状态
	 * @param params
	 * @param token
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public RestResponse updateFirstDepartmentMonthPerformanceStatus(Map<String, Object> params, String token) throws Exception{
		logger.info("updateFirstDepartmentMonthPerformanceStatus方法开始执行,参数：params"+params);
		Integer departmentId = Integer.valueOf(String.valueOf(params.get("departmentId")));
		String month = String.valueOf(params.get("month"));
		Map<String,Object> queryMap = new HashMap<>();
		queryMap.put("firstdepartmentId", departmentId);
		queryMap.put("month", month);
		//查询月度绩效状态
		String status = erpMonthPerformanceMapper.findFirstDepartmentMonthPerStatus(queryMap);
		Map<String, Object> returnMessageMap = new HashMap<>();
		if("3".equals(status)){
			returnMessageMap.put("Message", "该月度绩效已锁定请先解锁！");
			return RestUtils.returnSuccess(returnMessageMap, "OK");
		}
		 if("4".equals(status)){
			 returnMessageMap.put("Message", "该月度绩效已归档不可修改！");
			 return RestUtils.returnSuccess(returnMessageMap, "OK");
		 }
		if("1".equals(status)){
			returnMessageMap.put("Message", "该月度绩效未提交无需修改！");
			return RestUtils.returnSuccess(returnMessageMap, "OK");
		}
		Map<String,Object> updateMap = new HashMap<>();
		updateMap.put("status", DicConstants.MONTH_PERFORMANCE_APPLY_STATUS_MODIFY);
		updateMap.put("modifiedTime", ExDateUtils.getCurrentDateTime());
		updateMap.put("firstdepartmentId", departmentId);
		updateMap.put("month", month);
		erpMonthPerformanceMapper.updateFirstDepartmentMonthPerStatus(updateMap);
		returnMessageMap.put("Message", "撤回成功！");
		return 	RestUtils.returnSuccess(returnMessageMap, "OK");		
	}

	/**
	 * 查询一级部门月度绩效状态
	 * @param param
	 * @return
	 */
    public RestResponse findMonthPerformanceStatusByDepartmentIdAndMonth(Map<String, Object> param) {
		logger.info("findMonthPerformanceStatusByDepartmentIdAndMonth方法开始执行,参数：params"+param);
		String month = String.valueOf(param.get("month"));
		Integer firstdepartmentId = Integer.valueOf(String.valueOf(param.get("firstdepartmentId")));
		String status = erpMonthPerformanceMapper.findFirstDepartmentMonthPerStatus(param);
		return RestUtils.returnSuccess(status,"OK");
	}
}
