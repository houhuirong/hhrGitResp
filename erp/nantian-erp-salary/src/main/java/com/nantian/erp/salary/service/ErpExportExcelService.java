package com.nantian.erp.salary.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.dao.ErpPayRollFlowMapper;
import com.nantian.erp.salary.data.dao.ErpPeriodPayrollMapper;
import com.nantian.erp.salary.data.dao.ErpPositiveConfirMapper;
import com.nantian.erp.salary.data.dao.ErpPositivePayrollMapper;
import com.nantian.erp.salary.data.dao.ErpSalaryMonthPerformanceMapper;
import com.nantian.erp.salary.data.dao.ErpTalkSalaryMapper;
import com.nantian.erp.salary.data.dao.ErpTraineeSalaryMapper;
import com.nantian.erp.salary.data.model.ErpPeriodPayroll;
import com.nantian.erp.salary.data.model.ErpPositivePayroll;
import com.nantian.erp.salary.data.model.ErpTalkSalary;
import com.nantian.erp.salary.data.model.ErpTraineeSalary;
import com.nantian.erp.salary.util.AesUtils;

/**
 * serviceImpl 接口实现层
 * 员工薪酬 - 列表导出
 * @author 曹秀斌
 * @date 2018-09-16
 */
@Service
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties","classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties","classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpExportExcelService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${protocol.type}")
    private String protocolType;//http或https
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	private ErpTalkSalaryMapper erpTalkSalaryMapper;
	
	@Autowired
	private ErpPeriodPayrollMapper erpPeriodPayroll;
	@Autowired
	private ErpPositiveConfirMapper erpPositiveConfirMapper;
	
	@Autowired
	private ErpSalaryMonthPerformanceMapper erpSalaryMonthPerformanceMapper;
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate; 
	@Autowired
	private ErpPayRollFlowMapper erpPayRollFlowMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private ErpTraineeSalaryMapper erpTraineeSalaryMapper;
	@Autowired 
	private ErpPositivePayrollMapper erpPositivePayrollMapper;
	@Autowired
	private ErpPeriodPayrollService erpPayrollService;

	/*--所有待入职列表导出  start --------------------------------------------------------------------------------------------*/
	/**
	 * 员工薪酬-所有待入职列表导出
	 * 
	 */
	@SuppressWarnings("unchecked")
	public RestResponse exportToBeHiredEmp() {
		
		List<Map<String, Object>> returnList = new ArrayList<>();
		
		//调用ERP-人力资源 工程 的操作层服务接口-获取所有待入职员工及部门基本信息
		String url = protocolType+"nantian-erp-hr/nantian-erp/erp/entry/findall";
		JSONObject js = this.restTemplate.getForObject(url, JSONObject.class);
		if(null == js.get("data") || "".equals(String.valueOf(js.get("data")))) {
			return RestUtils.returnSuccessWithString("没有获取到员工基本信息，请检查 ！");
		}
		
		//解析获取的数据
		List<Object> result = js.getJSONArray("data");
		Map<String, Object> param = null;
		for (Object object : result) {
			param = (Map<String, Object>) object;
			//待入职人员简历ID
			Integer offerId = Integer.valueOf(String.valueOf(param.get("offerId")));
			Integer resumeId = Integer.valueOf(String.valueOf(param.get("resumeId")));
			//面试谈薪
			ErpTalkSalary erpTalkSalary = this.erpTalkSalaryMapper.findOneByOfferId(offerId);
			if(null != erpTalkSalary) {
				param.put("erpTalkIncome", erpTalkSalary.getMonthIncome());//面试谈薪-收入
			}else {
				param.put("erpTalkIncome", 0);//面试谈薪-收入
			}
			//岗位信息
			Map<String, Object> postInfo = this.findPostInfo(offerId);
			param.put("postName", postInfo.get("postName"));              //岗位信息
			
			returnList.add(param);
		}
		return this.exportToBeHiredEmp(returnList);
	}
	
	/*
	 * 根据简历ID查询岗位信息
	 * 参数：resumeId
	 */
	private Map<String, Object> findPostInfo(Integer offerId) {
		logger.info("查询所有一级部门 参数 : " + offerId);
		Map<String, Object> returnMap = new HashMap<>();
		
		//调用ERP-人力资源 工程 的操作层服务接口-获取所有待入职员工及部门基本信息
		String url = protocolType+"nantian-erp-hr/nantian-erp/erp/empDepartment/Info/findPostInfo?offerId="+offerId;
		logger.info("调用ERP-人力资源 - url : " + url);
		JSONObject js = this.restTemplate.getForObject(url, JSONObject.class);
		if(null == js.get("data") || "".equals(String.valueOf(js.get("data")))) {
			return returnMap;
		}
		
		//解析获取的数据
		if(null == js.get("data") || "".equals(js.get("data"))) {
			return returnMap;
		}else {
			returnMap = (Map<String, Object>) js.getJSONObject("data"); 
		}
		return returnMap;
	}
	
	/*
	 * 功能：导出
	 * 参数：List<Map<String, Object>> resultList  
	 */
	public RestResponse exportToBeHiredEmp (List<Map<String, Object>> resultList) {
		
		//定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("员工薪酬-招聘-所有待入职");
			
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("性别");
			firstRow.createCell(2).setCellValue("一级部门");
			firstRow.createCell(3).setCellValue("二级部门");
			firstRow.createCell(4).setCellValue("岗位");
			firstRow.createCell(5).setCellValue("职位");
			firstRow.createCell(6).setCellValue("职级");
			firstRow.createCell(7).setCellValue("薪资");
			
			//下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			//循环 填充表格
			for (int i = 0; i < resultList.size(); i++) {
				param = (Map<String, Object>) resultList.get(i);
				nextRow = sheet.createRow(i+1);
				nextRow.createCell(0).setCellValue(String.valueOf(param.get("name")));
				nextRow.createCell(1).setCellValue(String.valueOf(param.get("sex")));
				nextRow.createCell(2).setCellValue(String.valueOf(param.get("firstDepartment")));
				nextRow.createCell(3).setCellValue(String.valueOf(param.get("secondDepartment")));
				nextRow.createCell(4).setCellValue(String.valueOf(param.get("postName")));
				nextRow.createCell(5).setCellValue(String.valueOf(param.get("position")));
				nextRow.createCell(6).setCellValue(String.valueOf(param.get("rank")));
				nextRow.createCell(7).setCellValue(String.valueOf(param.get("erpTalkIncome")));
			}
		} catch (Exception e) {
			logger.error("exportToBeHiredEmp出现异常："+e.getMessage(),e);
		}
		String str = this.exportExcelToComputer(workBook);
		return RestUtils.returnSuccessWithString(str);
	}
	/*--所有待入职列表导出  end --------------------------------------------------------------------------------------------*/
	
	/*--入职-试用期工资单员工信息 列表导出  start --------------------------------------------------------------------------------------------*/
	/**
	 * 员工薪酬-试用期工资单员工信息列表导出
	 * flag 1=导出待我处理  0=所有
	 */
	@SuppressWarnings("unchecked")
	public RestResponse exportPeriodEmp(String token) {
		
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		String userName = this.stringRedisTemplate.opsForValue().get(token);//用户名
		logger.info("当前登陆人："+userName);
		
		//调用ERP-人力资源 工程 的操作层服务接口-获取所有员工及部门基本信息
		String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/postOrPositivePayroll";
		Map<String, Object> tempParam = Maps.newHashMap();
		tempParam.put("payrollType", "post");//试用期
		tempParam.put("username", userName);
		RestResponse response = this.restTemplate.postForObject(url, tempParam, RestResponse.class);
		
		if(null == response.getData() || "".equals(String.valueOf(response.getData()))) {
			return RestUtils.returnSuccessWithString("没有获取到员工薪酬-试用期工资单员工信息 ！");
		}
		
		//解析获取的数据
		List<Object> result = (List<Object>) response.getData();
		Map<String, Object> map = null;
		for (Object object : result) {
			map = (Map<String, Object>) object;
			resultList.add(map);
		}
		return this.exportPeriodEmp(resultList, token);
	}
	
	/*
	 * 功能：导出
	 * 参数：List<Map<String, Object>> resultList  针对每个列表差异字段处理 共用工具类
	 */
	public RestResponse exportPeriodEmp (List<Map<String, Object>> resultList,String token) {
		
		//定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = null;
			if(null != token && !"".equals(token)) {
				sheet = workBook.createSheet("员工薪酬-入职-待我处理的上岗工资单");
			}else {
				sheet = workBook.createSheet("员工薪酬-入职-所有上岗工资单");
			}
			//生成第一行  当为归档offer时 得添加归档理由
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("性别");
			firstRow.createCell(2).setCellValue("一级部门");
			firstRow.createCell(3).setCellValue("二级部门");
			firstRow.createCell(4).setCellValue("岗位");
			firstRow.createCell(5).setCellValue("职位");
			firstRow.createCell(6).setCellValue("职级");
			
			//下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			//循环 填充表格
			for (int i = 0; i < resultList.size(); i++) {
				param = resultList.get(i);
				nextRow = sheet.createRow(i+1);
				nextRow.createCell(0).setCellValue(String.valueOf(param.get("name")));
				nextRow.createCell(1).setCellValue(String.valueOf(param.get("sex")));
				nextRow.createCell(2).setCellValue(String.valueOf(param.get("firstDepartmentName")));
				nextRow.createCell(3).setCellValue(String.valueOf(param.get("secondDepartmentName")));
				nextRow.createCell(4).setCellValue(String.valueOf(param.get("postName")));
				nextRow.createCell(5).setCellValue(String.valueOf(param.get("position")));
				nextRow.createCell(6).setCellValue(String.valueOf(param.get("rank")));
			}
		} catch (Exception e) {
			logger.error("exportPeriodEmp出现异常："+e.getMessage(),e);
		}
		String str = this.exportExcelToComputer(workBook);
		return RestUtils.returnSuccessWithString(str);
	}
	/*--入职 试用期工资单员工信息 列表导出  end --------------------------------------------------------------------------------------------*/
	
	/*--转正-转正期工资单员工信息 列表导出  start --------------------------------------------------------------------------------------------*/
	/**
	 * 员工薪酬-转正期工资单员工信息列表导出
	 * 
	 */
	@SuppressWarnings("unchecked")
	public RestResponse exportPositiveEmp(String token,String positiveMonth) {

        List<Map<String, Object>> returnList = new ArrayList<Map<String,Object>>();
		Boolean flag=false;
		Map<String,Object> lockMap=new HashMap<String, Object>();
		Map<String,Object> returnMap=new HashMap<String, Object>();
		try {
			ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
			List<Integer> roles=erpUser.getRoles();//角色列表
			//查询该月的转正名单hr是否已确认
			Map<String,Object> confirMap = erpPositiveConfirMapper.seleConfirmByear(positiveMonth);
			if(confirMap == null || confirMap.get("isConfirm") == null){
				HttpServletResponse response1 = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
				//response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8"));
				//response.setCharacterEncoding("UTF-8");
				response1.setHeader("status","0");
				return RestUtils.returnSuccess("hr未确认，不能导出！");
			}
			Integer isConfirm = (Integer) confirMap.get("isConfirm");
			if (isConfirm.equals(0)) {
				HttpServletResponse response1 = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
				//response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8"));
				//response.setCharacterEncoding("UTF-8");
				response1.setHeader("status","0");
				return RestUtils.returnSuccess("存在转正异常数据，不能导出！");
			}
			if(roles.contains(8)){//总裁
				Map<String,Object> query=new HashMap<String,Object>();//多重角色可看到所有未处理和已处理的
				query.put("positiveMonth", positiveMonth);
				List<Map<String,Object>> UserList=this.erpPayRollFlowMapper.findAllConfirmPositivePayRoll(query);
				//如果存在未处理的则不能导出
				for (Map<String, Object> map : UserList) {
					Integer status = (Integer) map.get("status");
					if(status.equals(2)){
						HttpServletResponse response1 = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
						//response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8"));
						//response.setCharacterEncoding("UTF-8");
						response1.setHeader("status","0");
						return RestUtils.returnSuccess("存在未处理，不能导出！");
					}
				}
				returnMap=positivePayroll(UserList,token,positiveMonth,flag);
				returnList=(List<Map<String, Object>>) returnMap.get("returnList");
				List<Integer> list=(List<Integer>) returnMap.get("employeeId");
				lockMap.put("employeeId", list);
			}else if(roles.contains(9)){//副总裁
				//查询转正流程工资单状态为2，3，4的所有人员
				Map<String,Object> queryMap1=new HashMap<String,Object>();
				queryMap1.put("positiveMonth", positiveMonth);
				List<Map<String,Object>> List=this.erpPayRollFlowMapper.findAllConfirmPositivePayRoll(queryMap1);//多重角色查看所有未处理和已处理的
				//如果存在未处理的则不能导出
				for (Map<String, Object> map : List) {
					Integer status = (Integer) map.get("status");
					if(status.equals(2)){
						HttpServletResponse response2 = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
						//response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8"));
						//response.setCharacterEncoding("UTF-8");
						response2.setHeader("status","0");
						return RestUtils.returnSuccess("存在未处理，不能导出！");
					}
				}
				//跨工程查询superLeaderId为当前登陆人的部门里面所有员工
				String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoById";
				HttpHeaders requestHeaders=new HttpHeaders();
				requestHeaders.add("token",token);//将token放到请求头中
				HttpEntity<List<Map<String,Object>>> request = new HttpEntity<>(List, requestHeaders);
				ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
				if(response.getStatusCodeValue() != 200){
					logger.info("转正工资单查询员工信息，hr工程返回错误");
				}
				List<Map<String,Object>> mapList = (List<Map<String, Object>>) response.getBody().get("data");
				returnMap=positivePayroll(mapList,token,positiveMonth,flag);
				returnList=(List<Map<String, Object>>) returnMap.get("returnList");	
				List<Integer> list=(List<Integer>) returnMap.get("employeeId");
				lockMap.put("employeeId", list);
			}else if(roles.contains(2)){//一级部门经理角色
				Map<String,Object> queryMap=new HashMap<String,Object>();
				queryMap.put("positiveMonth", positiveMonth);
				queryMap.put("currentPersonID", erpUser.getUserId());//一级部门经理仅能看到自己部门未处理和已处理的
				List<Map<String,Object>> UserInfoList=this.erpPayRollFlowMapper.findAllConfirmPositivePayRoll(queryMap);
				//如果存在未处理的则不能导出
				for (Map<String, Object> map : UserInfoList) {
					Integer status = (Integer) map.get("status");
					if(status.equals(2)){
						HttpServletResponse response3 = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
						//response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8"));
						//response.setCharacterEncoding("UTF-8");
						response3.setHeader("status","0");
						return RestUtils.returnSuccess("存在未处理，不能导出！");
					}
				}
				returnMap=positivePayroll(UserInfoList,token,positiveMonth,flag);
				returnList=(List<Map<String, Object>>) returnMap.get("returnList");
				List<Integer> list=(List<Integer>) returnMap.get("employeeId");
				lockMap.put("employeeId", list);
			}
			lockMap.put("positiveIsLock", 1);
			lockMap.put("positiveMonth", positiveMonth);
			lockMap.put("opType", "lock");
		} catch (Exception e) {
			logger.error("查询 试用期-所有上岗工资单 findAllPayroll 出现异常：" + e.getMessage(),e);
		}
		if(returnList != null){
			logger.info("返回的员工记录条数："+returnList.size());
		}else{
			logger.info("返回的员工记录条数：0");
		}
		try{
			this.exportPositiveEmp(returnList, token,lockMap);
		}catch (Exception e) {
			logger.error("转正工资单导出出现异常：" + e.getMessage(),e);
		}
		return RestUtils.returnSuccess("导出成功！");
	}
	
	/*
	 * 功能：导出
	 * 参数：List<Map<String, Object>> resultList  针对每个列表差异字段处理 共用工具类
	 */
	@Transactional
	public RestResponse exportPositiveEmp (List<Map<String, Object>> returnList,String token,Map<String,Object> lockMap) {
		
		String strLock="";
		XSSFWorkbook workBook = null;
		try {
			
				workBook = new XSSFWorkbook();
				XSSFSheet sheet = null;
			    sheet = workBook.createSheet("员工薪酬-转正-转正工资单");
			    
				//生成第一行  当为归档offer时 得添加归档理由
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
				firstRow.createCell(8).setCellValue("转正日期");
				firstRow.createCell(9).setCellValue("转正前工资");
				firstRow.createCell(10).setCellValue("基本工资");
				firstRow.createCell(11).setCellValue("岗位工资");
				firstRow.createCell(12).setCellValue("月度绩效");
				firstRow.createCell(13).setCellValue("备注");
				
				for(int j = 0;j < 14; j++){
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
					nextRow.createCell(8).setCellValue(String.valueOf(param.get("probationEndTime")));
					//转正前工资
					if(String.valueOf(param.get("periodIncome")) != null){
						nextRow.createCell(9).setCellValue(String.valueOf(param.get("periodIncome")));
					}
					//转正工资
					ErpPositivePayroll positivePayroll=(ErpPositivePayroll) param.get("erpPositivePayrollInfo");
					if(positivePayroll != null ){
						nextRow.createCell(10).setCellValue(String.valueOf(positivePayroll.getErpPositiveBaseWage())==null?"":
							String.valueOf(positivePayroll.getErpPositiveBaseWage()));
						nextRow.createCell(11).setCellValue(String.valueOf(positivePayroll.getErpPositivePostWage())==null?"":
							String.valueOf(positivePayroll.getErpPositivePostWage()));
						nextRow.createCell(12).setCellValue(String.valueOf(positivePayroll.getErpPositivePerformance())==null?"":
							String.valueOf(positivePayroll.getErpPositivePerformance()));
					}else{
						nextRow.createCell(10).setCellValue("");
						nextRow.createCell(11).setCellValue("");
						nextRow.createCell(12).setCellValue("");
					}
					
					//nextRow.createCell(7).setCellValue(String.valueOf(positivePayroll.getErpPositiveAllowance()));
					//nextRow.createCell(8).setCellValue(String.valueOf(positivePayroll.getErpTelFarePerquisite()));
					nextRow.createCell(13).setCellValue("");
					for(int j = 0;j < 14; j++){
						nextRow.getCell(j).setCellStyle(style2);
					}
				}
				XSSFRow lastRow = null;
				int lengths = returnList.size();
				lastRow = sheet.createRow(lengths+4);
				lastRow.createCell(2).setCellValue("经手人签字：");
				lastRow.createCell(4).setCellValue("领导人签字：");
				XSSFRow endRow = null;
				String positiveMonth = (String) lockMap.get("positiveMonth"+"-30");
				endRow = sheet.createRow(lengths+6);
				endRow.createCell(2).setCellValue("信息截至至：");
				endRow.createCell(3).setCellValue("年月日");
		
		} catch (Exception e) {
			logger.error("exportPositiveEmp出现异常："+e.getMessage(),e);
		}
		String str = this.exportExcelToComputer(workBook);
		if(str.contains("成功")){
			try{
				strLock=this.erpPayrollService.updateErpPayRollFlow(lockMap,token);
			}catch (Exception e) {
				logger.error("锁定转正工资单出现异常："+e.getMessage(),e);
			}
		}
		return RestUtils.returnSuccessWithString(strLock);
	}
	/*--转正-转正期工资单员工信息 列表导出  end --------------------------------------------------------------------------------------------*/
	
	/*
	 * 功能：导出测试
	 * 参数：workBook
	 */
	public String exportExcelToComputer(XSSFWorkbook workBook) {
//		try {
//			FileOutputStream fos = new FileOutputStream("E:\\java\\test.xlsx");
//			workBook.write(fos);
//			fos.flush();
//			fos.close();
//		}catch (IOException e) {
//			e.printStackTrace();
//			return "导出模板表格失败";
//		}
		HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
		response.addHeader("Content-Disposition","attachment;filename=salary.xlsx");
		//返回状态为1是为了让前端在做转正工资单的导出状态进行判断
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
	 * Description: 测试加密
	 *
	 * @return
	 * @Author HouHuiRong
	 * @Create Date: 2018年11月12日 下午10:31:01
	 */
	public String encryptDataRsa(String salary) {
		logger.info("进入encryptDataRsa方法，参数是:salary="+salary);
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
	/*
	 * 转正工资单加密或解密
	 */
	public ErpPositivePayroll encryptAndDecryptPositivePayroll(Boolean flag,ErpPositivePayroll erpPositivePayroll){
		if(flag){//加密
			erpPositivePayroll.setErpPositiveBaseWage(encryptDataRsa(erpPositivePayroll.getErpPositiveBaseWage()));//转正基本工资
			erpPositivePayroll.setErpPositivePostWage(encryptDataRsa(erpPositivePayroll.getErpPositivePostWage()));//转正岗位工资
			erpPositivePayroll.setErpPositivePerformance(encryptDataRsa(erpPositivePayroll.getErpPositivePerformance()));//转正月度绩效
			erpPositivePayroll.setErpPositiveAllowance(encryptDataRsa(erpPositivePayroll.getErpPositiveAllowance()));//项目津贴
			erpPositivePayroll.setErpPositiveIncome(encryptDataRsa(erpPositivePayroll.getErpPositiveIncome()));//月度收入
			erpPositivePayroll.setErpTelFarePerquisite(encryptDataRsa(erpPositivePayroll.getErpTelFarePerquisite()));//花费补助
		}else{//解密
			erpPositivePayroll.setErpPositiveBaseWage(decryptDataRsa(erpPositivePayroll.getErpPositiveBaseWage()));
			erpPositivePayroll.setErpPositivePostWage(decryptDataRsa(erpPositivePayroll.getErpPositivePostWage()));
			erpPositivePayroll.setErpPositivePerformance(decryptDataRsa(erpPositivePayroll.getErpPositivePerformance()));
			erpPositivePayroll.setErpPositiveAllowance(decryptDataRsa(erpPositivePayroll.getErpPositiveAllowance()));
			erpPositivePayroll.setErpPositiveIncome(decryptDataRsa(erpPositivePayroll.getErpPositiveIncome()));
			erpPositivePayroll.setErpTelFarePerquisite(decryptDataRsa(erpPositivePayroll.getErpTelFarePerquisite()));

		}
		return erpPositivePayroll;
	}
	
	
	public Map<String,Object> positivePayroll(List<Map<String,Object>> UserInfoList,String token,String positiveMonth,Boolean flag){
		
		List<Map<String,Object>> empList= new ArrayList<Map<String,Object>>();
		List<Integer> empIdList=new ArrayList<Integer>();
		Map<String,Object> lockMap=new HashMap<String, Object>();
		try{
			if(UserInfoList.size()>0){
				HttpHeaders requestHeaders=new HttpHeaders();
				requestHeaders.add("token", token);
		        HttpEntity<String> requestEntity=new HttpEntity<String>(null,requestHeaders);			   
		        
		        //调用ERP-人力资源工程的操作层服务接口-查询员工基本信息
		       /* String url2=protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeByPositiveMonth?probationEndTime="+positiveMonth;
		        ResponseEntity<String> res= restTemplate.exchange(url2,HttpMethod.GET,requestEntity,String.class);
		        String strRes=res.getBody();			        
		        JSONObject empStr=JSON.parseObject(strRes);
		        empList=(List<Map<String,Object>>) empStr.get("data");
		        if(null==empList){//通过员工ID未获取到员工信息！
		        	logger.info("访问hr工程获取员工信息失败！");
		        	return new HashMap<String, Object>();
		        }*/
				
				for(int i=0;i<UserInfoList.size();i++){
					//调用ERP-权限 工程 的操作层服务接口-查询员工ID
					Map<String,Object> userInfoMap=UserInfoList.get(i);
					Integer status=Integer.valueOf(String.valueOf(userInfoMap.get("status")));
					Integer empId=Integer.valueOf(String.valueOf(userInfoMap.get("userId")));   
			        // for(int j=0;j<empList.size();j++){
			        	// Map<String,Object> empInfoMap=empList.get(j);
						// Integer monthEmpId=Integer.valueOf(String.valueOf(empInfoMap.get("employeeId")));
						// if(empId.intValue()==monthEmpId.intValue()){
						//	userInfoMap.putAll(empInfoMap);
							empIdList.add(empId);
					//调用ERP-人力资源工程的操作层服务接口-查询员工基本信息
							String url1 = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeDetail?employeeId="+empId;
							HttpHeaders requestHeaders1=new HttpHeaders();
							requestHeaders1.add("token", token);
						    HttpEntity<String> requestEntity1=new HttpEntity<String>(null,requestHeaders1);
						    ResponseEntity<Map> response1 = restTemplate.exchange(url1,HttpMethod.GET,requestEntity1,Map.class);
							//解析获取的数据
							Map<String,Object> responseBody1 = response1.getBody();
							if(!"200".equals(responseBody1.get("status"))) {
								logger.info("访问hr工程获取员工信息失败！");
								return new HashMap<String, Object>();
							}
						    /*
						     * 通过用户ID获取到员工信息
						     */
						    Map<String,Object> employeeMap = (Map<String, Object>) responseBody1.get("data");
						    userInfoMap.putAll(employeeMap);
							//转正工资单
							ErpPositivePayroll erpPositivePayroll= erpPositivePayrollMapper.selectOnePositivePayroll(empId);
							if(erpPositivePayroll == null){
								userInfoMap.put("erpPositivePayrollInfo", null);
							}else{
								ErpPositivePayroll decryptPositivePayroll=encryptAndDecryptPositivePayroll(flag,erpPositivePayroll);
								userInfoMap.put("erpPositivePayrollInfo", decryptPositivePayroll);
							}
							//转正前工资——判断是否实习生
							Boolean isTrainee = Boolean.valueOf(String.valueOf(employeeMap.get("isTrainee")));//是否是实习生
							if(isTrainee){//实习生查询实习工资单
								ErpTraineeSalary erpTraineeSalary=this.erpTraineeSalaryMapper.selectOneTraineeSalary(empId);
								if(erpTraineeSalary == null){
									userInfoMap.put("periodIncome", "");
								}else{
									ErpTraineeSalary decryptTraineeSalary=encryptAndDecryptTraineeSalary(flag,erpTraineeSalary);
									userInfoMap.put("periodIncome", decryptTraineeSalary.getBaseWage());
									}	
								}else{//非实习生则查询上岗工资单
								ErpPeriodPayroll erpPeriodPayroll = this.erpPeriodPayroll.findPeriodSalary(empId);
								if(erpPeriodPayroll == null){
									userInfoMap.put("periodIncome", "");
								}else{
									String income = decryptDataRsa(erpPeriodPayroll.getErpPeriodIncome());
									userInfoMap.put("periodIncome", income);
								}
							}
							//break;
						}
			        }
				
			logger.info("positivePayroll开始执行："+positiveMonth+"导出的人员为："+empIdList);
			lockMap.put("employeeId", empIdList);			
			lockMap.put("returnList", UserInfoList);
		}catch(Exception e){
			logger.error("positivePayroll出现异常："+e.getMessage(),e);
		}
		return lockMap;
	}
	
	public ErpTraineeSalary encryptAndDecryptTraineeSalary(Boolean flag,ErpTraineeSalary erpTraineeSalary){
		if(erpTraineeSalary!=null){
		if(flag){//加密
			erpTraineeSalary.setBaseWage(encryptDataRsa(erpTraineeSalary.getBaseWage()));
			erpTraineeSalary.setMonthAllowance(encryptDataRsa(erpTraineeSalary.getMonthAllowance()));
		}else{//解密
			erpTraineeSalary.setBaseWage(decryptDataRsa(erpTraineeSalary.getBaseWage()));
			erpTraineeSalary.setMonthAllowance(decryptDataRsa(erpTraineeSalary.getMonthAllowance()));
		}
		}
		return erpTraineeSalary;
	}
}
