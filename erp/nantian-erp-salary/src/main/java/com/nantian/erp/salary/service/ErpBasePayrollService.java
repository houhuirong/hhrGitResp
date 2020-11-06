package com.nantian.erp.salary.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.nantian.erp.common.base.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.salary.data.dao.ErpActualInsuranceFundBaseMapper;
import com.nantian.erp.salary.data.dao.ErpBasePayrollMapper;
import com.nantian.erp.salary.data.dao.ErpBasePayrollRecordMapper;
import com.nantian.erp.salary.data.dao.ErpBasePayrollUpdateRecordMapper;
import com.nantian.erp.salary.data.dao.ErpDepartmentCostMonthMapper;
import com.nantian.erp.salary.data.dao.ErpPeriodPayrollMapper;
import com.nantian.erp.salary.data.dao.ErpPositivePayrollMapper;
import com.nantian.erp.salary.data.dao.ErpPositiveSalaryMapper;
import com.nantian.erp.salary.data.dao.ErpSalaryAdjustMapper;
import com.nantian.erp.salary.data.dao.ErpSalaryMonthPerformanceMapper;
import com.nantian.erp.salary.data.dao.ErpSocialSecurityMapper;
import com.nantian.erp.salary.data.dao.ErpTalkSalaryMapper;
import com.nantian.erp.salary.data.dao.ErpTraineeSalaryMapper;
import com.nantian.erp.salary.data.model.ErpActualInsuranceFundBase;
import com.nantian.erp.salary.data.model.ErpBasePayroll;
import com.nantian.erp.salary.data.model.ErpBasePayrollRecord;
import com.nantian.erp.salary.data.model.ErpBasePayrollUpdateRecord;
import com.nantian.erp.salary.data.model.ErpDepartmentCostMonth;
import com.nantian.erp.salary.data.model.ErpPeriodPayroll;
import com.nantian.erp.salary.data.model.ErpPositivePayroll;
import com.nantian.erp.salary.data.model.ErpPositiveSalary;
import com.nantian.erp.salary.data.model.ErpSalaryMonthPerformance;
import com.nantian.erp.salary.data.model.ErpSocialSecurity;
import com.nantian.erp.salary.data.model.ErpTalkSalary;
import com.nantian.erp.salary.data.model.ErpTraineeSalary;
import com.nantian.erp.salary.data.vo.FirstDepEmpInfoAndSalaryVo;
import com.nantian.erp.salary.data.vo.SecondDepEmpInfoAndSalaryVo;
import com.nantian.erp.salary.util.AesUtils;

/**
 * Description: 薪酬管理service
 *
 * @author ZhangYuWei
 * @version 1.0
 * 
 *          <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年10月22日      		ZhangYuWei          1.0
 *          </pre>
 */
@Service
//@PropertySource("classpath:config/host.properties")
@PropertySource(value = { "classpath:config/host.properties",
		"file:${spring.profiles.path}/config/host.properties" }, ignoreResourceNotFound = true)
public class ErpBasePayrollService {

	/*
	 * 从配置文件中获取主机相关属性
	 */
	@Value("${protocol.type}")
	private String protocolType;// http或https

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ErpBasePayrollMapper erpBasePayrollMapper;
	@Autowired
	private ErpBasePayrollRecordMapper erpBasePayrollRecordMapper;
	@Autowired
	private ErpBasePayrollUpdateRecordMapper erpBasePayrollUpdateRecordMapper;
	@Autowired
	private ErpDepartmentCostMonthMapper erpDepartmentCostMonthMapper;
	@Autowired
	private ErpSalaryMonthPerformanceMapper erpMonthPerformanceMapper;
	@Autowired
	private ErpSalaryMonthPerformanceService erpMonthPerformanceService;
	@Autowired
	private ErpSocialSecurityMapper erpSocialSecurityMapper;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;
	@Autowired
	private ErpTalkSalaryMapper erpTalkSalaryMapper;
	@Autowired
	private ErpPeriodPayrollMapper erpPeriodPayrollMapper;
	@Autowired
	private ErpPositiveSalaryMapper erpPositiveSalaryMapper;
	@Autowired
	private ErpTraineeSalaryMapper erpTraineeSalaryMapper;
	@Autowired
	private ErpPositivePayrollMapper erpPositivePayrollMapper;
	@Autowired
	private ErpSalaryAdjustMapper erpSalaryAdjustMapper;
	@Autowired
	private ErpActualInsuranceFundBaseMapper erpActualInsuranceFundBaseMapper;
	@Autowired
	private ErpActualInsuranceFundBaseService erpActualFundBaseService;


	/**
	 * Description: 下载导入模板
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月22日 下午16:29:38
	 */
	public RestResponse downloadBasePayroll() {
		logger.info("downloadBasePayroll方法开始执行，传递参数：无参数");

		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("员工薪资");

			// 设置身份证号和财务号列为文本格式
			CellStyle style = workBook.createCellStyle();
			XSSFDataFormat format = workBook.createDataFormat();
			style.setDataFormat(format.getFormat("@"));
			sheet.setDefaultColumnStyle(1, style);

			// 生成表头（第一行）
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("身份证号");
			firstRow.createCell(2).setCellValue("基本工资");
			firstRow.createCell(3).setCellValue("岗位工资");
			firstRow.createCell(4).setCellValue("月度绩效");
			firstRow.createCell(5).setCellValue("月度项目津贴");
			firstRow.createCell(6).setCellValue("社保基数");
			firstRow.createCell(7).setCellValue("公积金基数");
			firstRow.createCell(8).setCellValue("话费补助");
			// 导出文件到客户端
			this.exportExcelToComputer(workBook, "员工薪酬导入模板.xlsx");
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("downloadBasePayroll方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnSuccessWithString("downloadBasePayroll方法出现异常：" + e.getMessage());
		}
	}

	/**
	 * Description: 员工薪酬信息导入
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月23日 上午9:45:48
	 */
	@Transactional
	public RestResponse importBasePayroll(MultipartFile file, String token) {
		logger.info("importBasePayroll方法开始执行，传递参数：MultipartFile类型文件,token=" + token);

		XSSFWorkbook workBook = null;
		try {
			if (file == null) {
				return RestUtils.returnSuccessWithString("文件为空！");
			}
			if (!file.getOriginalFilename().endsWith("xlsx")) {
				return RestUtils.returnSuccessWithString("请选择合适的文件导入 ！目前仅支持xlsx文件");
			}
			workBook = new XSSFWorkbook(file.getInputStream());
			// sheet
			XSSFSheet sheet = workBook.getSheetAt(0);
			int firstRowNum = sheet.getFirstRowNum();// 第一行
			int lastRowNum = sheet.getLastRowNum();// 最后一行
			int rows = lastRowNum - firstRowNum + 1;

			// 定义错误日志记录信息
			List<ErpBasePayrollRecord> ErpBasePayrollRecordList = new ArrayList<>();
			// 从缓存中获取登录用户信息
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			String employeeName = erpUser.getEmployeeName();//// 从用户信息中获取员工名
			String username = erpUser.getUsername();// 从用户信息中获取用户名

			/*
			 * 循环总行数 将每一行的数据进行入库操作，从Excel中获取所有信息
			 */
			for (int i = 1; i < rows; i++) {
				Map<String, String> excelDataMap = new HashMap<>();// 定义Excel的一行数据
				try {
					/*
					 * if(i==1) { throw new Exception("第一次手动抛异常！"); }
					 */
					/*
					 * 获取到sheet中每行的数据
					 */
					Row row = sheet.getRow(i);
					/*
					 * 将导入的Excel表格数据解析成Map格式的数据
					 */
					excelDataMap = this.convertRowToMap(row);

					/*
					 * if(i==2) { throw new Exception("第二次手动抛异常！"); }
					 */
					/*
					 * 对导入的Excel进行逐行数据内容、格式校验
					 */
					Map<String, String> validateResultMap = this.validateExcelData(excelDataMap);
					String isError = validateResultMap.get("isError");
					if ("Y".equals(isError)) {
						ErpBasePayrollRecord erpBasePayrollRecord = new ErpBasePayrollRecord();
						erpBasePayrollRecord.setEmpName(validateResultMap.get("name"));
						erpBasePayrollRecord.setEmpIdCardNum(validateResultMap.get("idCardNumber"));
						erpBasePayrollRecord.setErrorContent(validateResultMap.get("errorContent"));
						erpBasePayrollRecord.setErrorTime(ExDateUtils.getCurrentStringDateTime());
						ErpBasePayrollRecordList.add(erpBasePayrollRecord);
						continue;
					}
					/*
					 * 将薪酬数据加密
					 */
					Map<String, String> encryptedExcelData = this.encryptExcelDataAes(excelDataMap);

					/*
					 * 调用HR人力资源工程获取员工ID
					 */
					String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmpIdByIdCardNumAndName";
					// String url =
					// "https://nantian-erp-hr/nantian-erp/erp/employee/findEmpIdByIdCardNumAndName";
					MultiValueMap<String, String> erpEmployeeParam = new LinkedMultiValueMap<String, String>();
					erpEmployeeParam.add("name", excelDataMap.get("name"));
					erpEmployeeParam.add("idCardNumber", excelDataMap.get("idCardNumber"));
					HttpHeaders requestHeaders = new HttpHeaders();
					requestHeaders.add("token", token);// 将token放到请求头中

					HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
							erpEmployeeParam, requestHeaders);
					ResponseEntity<JSONObject> response = this.restTemplate.postForEntity(url, request,
							JSONObject.class);
					JSONObject responseBody = response.getBody();

					if (response.getStatusCodeValue() != 200 || responseBody == null
							|| responseBody.getInteger("data") == null) {
						logger.info("通过姓名和身份证号没有查到用户信息！");
						/*
						 * 通过姓名和身份证号没有查到用户信息时，将错误日志信息记录下来
						 */
						ErpBasePayrollRecord erpBasePayrollRecord = new ErpBasePayrollRecord();
						erpBasePayrollRecord.setEmpName(excelDataMap.get("name"));
						erpBasePayrollRecord.setEmpIdCardNum(excelDataMap.get("idCardNumber"));
						erpBasePayrollRecord.setErrorContent("通过姓名和身份证号没有查到用户信息！");
						erpBasePayrollRecord.setErrorTime(ExDateUtils.getCurrentStringDateTime());
						ErpBasePayrollRecordList.add(erpBasePayrollRecord);
						continue;
					}

					Integer erpEmployeeId = responseBody.getInteger("data");// 解析获取的数据

					/*
					 * 根据员工ID校验薪酬表中是否有记录，当导入重复员工时，将错误日志信息记录下来 update by ZhangYuWei 20181102
					 * 需求变更：允许导入重复员工，将旧数据更新
					 */
					/*
					 * ErpBasePayroll validResult =
					 * this.erpBasePayrollMapper.validEmployee(erpEmployeeId); if(validResult!=null)
					 * { ErpBasePayrollRecord erpBasePayrollRecord = new ErpBasePayrollRecord();
					 * erpBasePayrollRecord.setEmpName(excelDataMap.get("name"));
					 * erpBasePayrollRecord.setEmpIdCardNum(excelDataMap.get("idCardNumber"));
					 * erpBasePayrollRecord.setErrorContent("该员工的薪酬数据已经存在，本次没有被导入！");
					 * erpBasePayrollRecord.setErrorTime(ErpDateUtils.getCurrentStringDateTime());
					 * ErpBasePayrollRecordList.add(erpBasePayrollRecord); continue; }
					 */

					/*
					 * 将薪酬信息加密后，赋值给薪酬管理的PO对象
					 */
					ErpBasePayroll erpBasePayroll = new ErpBasePayroll();
					erpBasePayroll.setErpEmployeeId(erpEmployeeId);// 员工ID
					erpBasePayroll.setErpBaseWage(encryptedExcelData.get("erpBaseWage"));// 基本工资
					erpBasePayroll.setErpPostWage(encryptedExcelData.get("erpPostWage"));// 岗位工资
					erpBasePayroll.setErpPerformance(encryptedExcelData.get("erpPerformance"));// 月度绩效
					erpBasePayroll.setErpAllowance(encryptedExcelData.get("erpAllowance"));// 月度项目津贴
					erpBasePayroll.setErpSocialSecurityBase(encryptedExcelData.get("erpSocialSecurityBase"));// 社保基数
					erpBasePayroll.setErpAccumulationFundBase(encryptedExcelData.get("erpAccumulationFundBase"));// 公积金基数
					erpBasePayroll.setErpTelFarePerquisite(encryptedExcelData.get("erpTelFarePerquisite"));// 话费补助

					/*
					 * 如果薪酬表中有员工，则更新；如果没有，则新增 薪酬数据允许重复导入，以最新的数据为准
					 */
					ErpBasePayroll validResult = this.erpBasePayrollMapper.findBasePayrollDetailByEmpId(erpEmployeeId);
					if (validResult == null) {
						this.erpBasePayrollMapper.insertBasePayroll(erpBasePayroll);
					} else {
						this.erpBasePayrollMapper.updateBasePayroll(erpBasePayroll);
					}

					/*
					 * 将该员工的修改信息加入日志中
					 */
					ErpBasePayrollUpdateRecord basePayrollUpdateRecord = new ErpBasePayrollUpdateRecord();
					basePayrollUpdateRecord.setEmployee(excelDataMap.get("name"));// 被修改的员工
					basePayrollUpdateRecord.setEmployeeId(erpEmployeeId);
					basePayrollUpdateRecord
							.setProcessor(employeeName == null || "".equals(employeeName) ? username : employeeName);// 修改人
					basePayrollUpdateRecord.setTime(ExDateUtils.getCurrentStringDateTime());// 修改时间
					basePayrollUpdateRecord.setContent(erpBasePayroll.toString());// 修改内容
					this.erpBasePayrollUpdateRecordMapper.insertBasePayrollUpdateRecord(basePayrollUpdateRecord);

					workBook.close();// 关闭IO资源
				} catch (Exception e) {
					logger.error("循环获取excel每行表格数据时，发生异常：" + e.getMessage(), e);
					ErpBasePayrollRecord erpBasePayrollRecord = new ErpBasePayrollRecord();
					erpBasePayrollRecord.setEmpName(excelDataMap.get("name"));
					erpBasePayrollRecord.setEmpIdCardNum(excelDataMap.get("idCardNumber"));
					// 从异常信息中获取到250之内的字符，加入到错误日志中
					String message = e.getMessage();
					if (message.length() > 250) {
						message = message.substring(0, 250);
					}
					erpBasePayrollRecord.setErrorContent("循环获取excel每行表格数据时，发生异常：" + message);
					erpBasePayrollRecord.setErrorTime(ExDateUtils.getCurrentStringDateTime());
					ErpBasePayrollRecordList.add(erpBasePayrollRecord);
				}
			}

			/*
			 * 将错误日志信息，插入到错误日志记录表中，同时返回给前端显示
			 */
			if (ErpBasePayrollRecordList.size() > 0) {
				for (ErpBasePayrollRecord erpBasePayrollRecord : ErpBasePayrollRecordList) {
					this.erpBasePayrollRecordMapper.insertBasePayrollRecord(erpBasePayrollRecord);
				}
				if (ErpBasePayrollRecordList.size() == rows - 1) {
					return RestUtils.returnSuccess(ErpBasePayrollRecordList, "全部失败！");
				} else {
					return RestUtils.returnSuccess(ErpBasePayrollRecordList, "部分失败！");
				}
			}
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("导入Excel文件时发生异常，导致失败：" + e.getMessage(), e);
			return RestUtils.returnSuccessWithString("导入Excel文件时发生异常，导致失败！");
		}
	}

	/**
	 * Description: 员工薪酬信息导出
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月23日 上午10:23:01
	 */
	public RestResponse exportBasePayroll(Integer firstDepartmentId, String queryMode, String token) {
		logger.info("exportBasePayroll方法开始执行，传递参数：firstDepartmentId=" + firstDepartmentId);

		try {
			// 查询出需要导出的数据（员工信息+薪酬信息）
			List<Map<String, Object>> employeeAndBasePayrollList = this
					.findEmployeeAndBasePayrollList(firstDepartmentId, queryMode, token);
			// 当员工列表为空，不允许导出（前端不能弹出这个提示框）
//			if(employeeAndBasePayrollList.size()==0) {
//				return RestUtils.returnSuccessWithString("暂无员工信息！无法导出！");
//			}

			// 通过数据创建Excel文件
			XSSFWorkbook workBook = this.createExcel(employeeAndBasePayrollList);

			// 导出表格到客户端
			this.exportExcelToComputer(workBook, "员工薪酬导出.xlsx");
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("exportBasePayroll方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("exportBasePayroll方法出现异常：" + e.getMessage());
		}
	}

	/**
	 * Description: 导出权限内所有一级部门的员工信息和薪酬情况
	 *
	 * @Author ZhangYuWei
	 * @Create Date: 2019年7月3日
	 */
	@SuppressWarnings("unchecked")
	public RestResponse exportFirDepEmpInfoByPowerParams(String token, String departmentType, String queryMode,
			String keyword) {
		logger.info("进入exportFirDepEmpInfoByPowerParams方法 。参数是：token={},departmentType={},queryMode={},keyword={}",
				token, departmentType, queryMode, keyword);
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
		Integer id = erpUser.getUserId();// 用户Id
		String username = erpUser.getUsername();// 从用户信息中获取用户名
		List<Integer> roles = erpUser.getRoles();// 从用户信息中获取角色信息
		logger.info("id=" + id + ",username=" + username + ",roles=" + roles);

		// 调用ERP-人力资源 工程 的操作层服务接口-获取一级部门下面所有二级部门的员工的详细信息
		String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoOfAllFirDepByParams";
		Map<String, Object> departmentParams = new HashMap<>();
		departmentParams.put("departmentType", departmentType);
		departmentParams.put("queryMode", queryMode);// 查询模式（0：查询在职员工+离职员工，1：查询在职员工）
		departmentParams.put("keyword", keyword);// 关键字查询（目前仅查询员工姓名）

		/*
		 * 判断当前登录用户的角色查看数据的权限
		 */
		if (roles.contains(8)) {// 总经理
			departmentParams.put("userId", null);
			departmentParams.put("superLeader", null);
		} else if (roles.contains(9)) {// 副总经理
			departmentParams.put("userId", null);
			departmentParams.put("superLeader", id);
		} else {// 一级部门经理、其他角色
			departmentParams.put("userId", id);
			departmentParams.put("superLeader", null);
		}

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("token", token);// 将token放到请求头中
		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(departmentParams, requestHeaders);
		ResponseEntity<RestResponse> response = this.restTemplate.postForEntity(url, requestEntity, RestResponse.class);
		if (200 != response.getStatusCodeValue() || !"200".equals(response.getBody().getStatus())) {
			return RestUtils.returnFailure("调用人力资源工程失败！");
		}

		List<Map<String, Object>> employeeAndBasePayrollList = new ArrayList<>();// 需要导出的员工信息列表
		List<List<List<Map<String, Object>>>> firDepEmpInfoList = (List<List<List<Map<String, Object>>>>) response
				.getBody().getData();
		// 权限内所有一级部门员工的信息和薪酬情况
		for (List<List<Map<String, Object>>> secDepEmpInfoList : firDepEmpInfoList) {
			// 一级部门下所有二级部门员工的信息和薪酬情况
			for (List<Map<String, Object>> empInfoList : secDepEmpInfoList) {
				// 将不同部门员工的数组加在一起
				employeeAndBasePayrollList.addAll(empInfoList);
				// 一个二级部门下所有员工的信息和薪酬情况
				for (Map<String, Object> empInfo : empInfoList) {
					Integer erpEmployeeId = (Integer) empInfo.get("employeeId");
					ErpBasePayroll erpBasePayroll = erpBasePayrollMapper.findBasePayrollDetailByEmpId(erpEmployeeId);
					if (erpBasePayroll != null) {
						/*
						 * 将数据库中加密后的薪酬信息解密
						 */
						Map<String, Double> decryptedExcelData = this.decryptExcelDataAes(erpBasePayroll);
						Double erpBaseWage = decryptedExcelData.get("erpBaseWage");// 基本工资
						Double erpPostWage = decryptedExcelData.get("erpPostWage");// 岗位工资
						Double erpPerformance = decryptedExcelData.get("erpPerformance");// 月度绩效
						Double erpAllowance = decryptedExcelData.get("erpAllowance");// 月度项目津贴
						Double erpSocialSecurityBase = decryptedExcelData.get("erpSocialSecurityBase");// 社保基数
						Double erpAccumulationFundBase = decryptedExcelData.get("erpAccumulationFundBase");// 公积金基数
						Double erpTelFarePerquisite = decryptedExcelData.get("erpTelFarePerquisite");// 话费补助

						empInfo.put("erpBaseWage", erpBaseWage);
						empInfo.put("erpPostWage", erpPostWage);
						empInfo.put("erpPerformance", erpPerformance);
						empInfo.put("erpAllowance", erpAllowance);
						empInfo.put("erpSocialSecurityBase", erpSocialSecurityBase);
						empInfo.put("erpAccumulationFundBase", erpAccumulationFundBase);
						empInfo.put("erpTelFarePerquisite", erpTelFarePerquisite);
					}
				}
			}
		}
		XSSFWorkbook workbook = this.createExcel(employeeAndBasePayrollList);
		try {
			this.exportExcelToComputer(workbook, "员工薪酬导出.xlsx");
		} catch (IOException e) {
			return RestUtils.returnFailure("导出失败");
		}
		return RestUtils.returnSuccessWithString("导出成功");
	}

	public XSSFWorkbook createExcel(List<Map<String, Object>> employeeAndBasePayrollList) {
		// 定义工作簿
		XSSFWorkbook workBook = new XSSFWorkbook();
		XSSFSheet sheet = workBook.createSheet("员工薪资");
		// 生成第一行
		XSSFRow firstRow = sheet.createRow(0);
		firstRow.createCell(0).setCellValue("姓名");
		firstRow.createCell(1).setCellValue("性别");
		firstRow.createCell(2).setCellValue("一级部门");
		firstRow.createCell(3).setCellValue("二级部门");
		firstRow.createCell(4).setCellValue("职位");
		firstRow.createCell(5).setCellValue("职级");
		firstRow.createCell(6).setCellValue("身份证号码");

		firstRow.createCell(7).setCellValue("基本工资");
		firstRow.createCell(8).setCellValue("岗位工资");
		firstRow.createCell(9).setCellValue("月度绩效");
		firstRow.createCell(10).setCellValue("月度项目津贴");
		firstRow.createCell(11).setCellValue("社保基数");
		firstRow.createCell(12).setCellValue("公积金基数");
		firstRow.createCell(13).setCellValue("话费补助");
		// 下一行
		XSSFRow nextRow = null;
		Map<String, Object> employeeAndBasePayroll = null;
		// 循环 填充表格
		for (int i = 0; i < employeeAndBasePayrollList.size(); i++) {
			employeeAndBasePayroll = employeeAndBasePayrollList.get(i);
			nextRow = sheet.createRow(i + 1);

			if (employeeAndBasePayroll.get("name") != null) {
				nextRow.createCell(0).setCellValue(employeeAndBasePayroll.get("name").toString());
			}
			if (employeeAndBasePayroll.get("sex") != null) {
				nextRow.createCell(1).setCellValue(employeeAndBasePayroll.get("sex").toString());
			}
			if (employeeAndBasePayroll.get("firstDepartmentName") != null) {
				nextRow.createCell(2).setCellValue(employeeAndBasePayroll.get("firstDepartmentName").toString());
			}
			if (employeeAndBasePayroll.get("secondDepartmentName") != null) {
				nextRow.createCell(3).setCellValue(employeeAndBasePayroll.get("secondDepartmentName").toString());
			}
			if (employeeAndBasePayroll.get("position") != null) {
				nextRow.createCell(4).setCellValue(employeeAndBasePayroll.get("position").toString());
			}
			if (employeeAndBasePayroll.get("rank") != null) {
				nextRow.createCell(5).setCellValue(employeeAndBasePayroll.get("rank").toString());
			}
			if (employeeAndBasePayroll.get("idCardNumber") != null) {
				nextRow.createCell(6).setCellValue(employeeAndBasePayroll.get("idCardNumber").toString());
			}
			if (employeeAndBasePayroll.get("erpBaseWage") != null) {
				nextRow.createCell(7).setCellValue(employeeAndBasePayroll.get("erpBaseWage").toString());
			}
			if (employeeAndBasePayroll.get("erpPostWage") != null) {
				nextRow.createCell(8).setCellValue(employeeAndBasePayroll.get("erpPostWage").toString());
			}
			if (employeeAndBasePayroll.get("erpPerformance") != null) {
				nextRow.createCell(9).setCellValue(employeeAndBasePayroll.get("erpPerformance").toString());
			}
			if (employeeAndBasePayroll.get("erpAllowance") != null) {
				nextRow.createCell(10).setCellValue(employeeAndBasePayroll.get("erpAllowance").toString());
			}
			if (employeeAndBasePayroll.get("erpSocialSecurityBase") != null) {
				nextRow.createCell(11).setCellValue(employeeAndBasePayroll.get("erpSocialSecurityBase").toString());
			}
			if (employeeAndBasePayroll.get("erpAccumulationFundBase") != null) {
				nextRow.createCell(12).setCellValue(employeeAndBasePayroll.get("erpAccumulationFundBase").toString());
			}
			if (employeeAndBasePayroll.get("erpTelFarePerquisite") != null) {
				nextRow.createCell(13).setCellValue(employeeAndBasePayroll.get("erpTelFarePerquisite").toString());
			}
		}
		return workBook;
	}

	/**
	 * Description: 导出Excel文件workBook到客户端
	 *
	 * @return
	 * @throws IOException
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月23日 上午10:55:50
	 */
	public String exportExcelToComputer(XSSFWorkbook workBook, String fileName) throws IOException {
		logger.info("exportExcelToComputer方法开始执行，传递参数:fileName=" + fileName);

		// 本地测试导出文件，与前端联调、或上线前须注释掉
	
		/* FileOutputStream fos = new
		 FileOutputStream("D:"+fileName); 
		 workBook.write(fos);
		 fos.flush(); 
		 fos.close();*/
	
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		response.addHeader("Content-Disposition", "attachment;filename=ModelExcel.xlsx");
		response.addHeader("status", "1");
		response.addHeader("statusName", stringToAscii("导出成功!"));
		ServletOutputStream os;
		try {
			os = response.getOutputStream();
			workBook.write(os);
			os.flush();
			os.close();
		} catch (IOException e) {
			return "导出模板表格失败";
		}
		return "导出成功!";
	}

	/**
	 * Description: 首页-查询所有一级部门
	 * 
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月23日 上午11:26:27
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RestResponse findAllFirstDepartment(String token) {
		logger.info("进入findAllFirstDepartment方法，参数是：token=" + token);
		try {
			/*
			 * 调用ERP-人力资源 工程 的操作层服务接口-获取所有一级部门
			 */
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/department/findDepartmentBySelectableId";
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
			List<Map<String, Object>> firstDepartmentList = (List<Map<String, Object>>) responseBody.get("data");
			return RestUtils.returnSuccess(firstDepartmentList);
		} catch (Exception e) {
			logger.error("findAllFirstDepartment方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
	}

	/**
	 * Description: 根据一级部门ID查询所有二级部门的员工信息、员工基本薪资
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月23日 下午15:39:57
	 * @Update Date: 2019年05月08日 下午11:22:52
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String, Object>> findEmployeeAndBasePayrollList(Integer firstDepartmentId, String queryMode, String token) {
		logger.info("进入根据一级部门ID查询所有二级部门的员工信息、员工基本薪资 方法 。参数是：firstDepartmentId=" + firstDepartmentId);
		List<Map<String, Object>> employeeAndBasePayrollList = new ArrayList<>();
		try {
			// 调用ERP-人力资源 工程 的操作层服务接口-获取一级部门下面所有二级部门的员工的详细信息
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmployeeTable";
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("firstDepartmentId", firstDepartmentId);// 将一级部门ID放到请求体中
			//查询模式（0或者不传参数：查询在职员工+离职员工，1：查询在职员工）
			if(queryMode == null){
				queryMode = "0";
			}
			requestBody.put("queryMode", queryMode);
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);// 将token放到请求头中
			HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, requestHeaders);

			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, request, Map.class);
			if (response.getStatusCodeValue() != 200 && !"200".equals(response.getBody().get("status"))) {
				return employeeAndBasePayrollList;
			}

			// 解析获取的数据
			employeeAndBasePayrollList = (List<Map<String, Object>>) response.getBody().get("data");
			logger.info("一级部门下的员工总数：" + employeeAndBasePayrollList.size());

			/*
			 * 增加薪酬信息
			 */
			for (Map<String, Object> employeeAndBasePayroll : employeeAndBasePayrollList) {
				Integer erpEmployeeId = Integer.valueOf(String.valueOf(employeeAndBasePayroll.get("employeeId")));
				ErpBasePayroll erpBasePayroll = this.erpBasePayrollMapper.findBasePayrollDetailByEmpId(erpEmployeeId);
				if (erpBasePayroll != null) {
					/*
					 * 将数据库中加密后的薪酬信息解密
					 */
					Map<String, Double> decryptedExcelData = this.decryptExcelDataAes(erpBasePayroll);
					Double erpBaseWage = decryptedExcelData.get("erpBaseWage");// 基本工资
					Double erpPostWage = decryptedExcelData.get("erpPostWage");// 岗位工资
					Double erpPerformance = decryptedExcelData.get("erpPerformance");// 月度绩效
					Double erpAllowance = decryptedExcelData.get("erpAllowance");// 月度项目津贴
					Double erpSocialSecurityBase = decryptedExcelData.get("erpSocialSecurityBase");// 社保基数
					Double erpAccumulationFundBase = decryptedExcelData.get("erpAccumulationFundBase");// 公积金基数
					Double erpTelFarePerquisite = decryptedExcelData.get("erpTelFarePerquisite");// 话费补助

					employeeAndBasePayroll.put("erpBaseWage", erpBaseWage);
					employeeAndBasePayroll.put("erpPostWage", erpPostWage);
					employeeAndBasePayroll.put("erpPerformance", erpPerformance);
					employeeAndBasePayroll.put("erpAllowance", erpAllowance);
					employeeAndBasePayroll.put("erpSocialSecurityBase", erpSocialSecurityBase);
					employeeAndBasePayroll.put("erpAccumulationFundBase", erpAccumulationFundBase);
					employeeAndBasePayroll.put("erpTelFarePerquisite", erpTelFarePerquisite);
				}
			}
		} catch (Exception e) {
			logger.error("查询所有一级部门 方法findAllFirstDepartment出现异常：" + e.getMessage(), e);
		}
		return employeeAndBasePayrollList;
	}

	/**
	 * Description: 查询一级部门下全部二级部门的员工信息和薪酬情况 与前端约定返回的数据格式
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月24日 上午10:20:01
	 */
	@SuppressWarnings("unchecked")
	public RestResponse findSecDepEmpInfoByFirDepId(Integer firstDepartmentId, String token) {
		logger.info("进入findSecDepEmpInfoByFirDepId方法 。参数是：firstDepartmentId=" + firstDepartmentId + ",token=" + token);
		try {
			// 调用ERP-人力资源 工程 的操作层服务接口-获取一级部门下面所有二级部门的员工的详细信息
			String url = protocolType
					+ "nantian-erp-hr/nantian-erp/erp/employee/findSecDepEmpInfoByFirDepId?firstDepartmentId="
					+ firstDepartmentId;
			// String url =
			// hrServerHost+"/nantian-erp/erp/employee/findSecDepEmpInfoByFirDepId?firstDepartmentId="+firstDepartmentId;
			String body = null;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.add("token", token);
			HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
			ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					JSONObject.class);

			if (response.getStatusCodeValue() != 200 || response.getBody() == null) {
				return RestUtils.returnSuccessWithString("没有从人力资源工程获取到数据！");
			}

			// 解析获取的数据
			JSONObject responseBody = response.getBody();
			List<List<Map<String, Object>>> firstEmployeeAndBasePayrollList = (List<List<Map<String, Object>>>) responseBody
					.get("data");
			logger.info("一级部门下的二级部门总数：" + firstEmployeeAndBasePayrollList.size());

			FirstDepEmpInfoAndSalaryVo firstDepEmpInfoAndSalaryVo = new FirstDepEmpInfoAndSalaryVo();// 前端显示
			List<SecondDepEmpInfoAndSalaryVo> secondDepEmpInfoAndSalaryVoList = new ArrayList<>();// 二级部门员工信息列表
			Double firstDepMonthIncome = 0.0;// 一级部门月度收入合计

			// 一级部门下所有二级部门员工的信息和薪酬情况
			for (List<Map<String, Object>> secondEmployeeAndBasePayrollList : firstEmployeeAndBasePayrollList) {
				logger.info("二级部门下的员工总数：" + secondEmployeeAndBasePayrollList.size());
				SecondDepEmpInfoAndSalaryVo secondDepEmpInfoAndSalaryVo = new SecondDepEmpInfoAndSalaryVo();
				List<Map<String, Object>> employeeInfoList = new ArrayList<>();// 员工信息列表
				Double secondDepMonthIncome = 0.0;// 二级部门月度收入合计
				String secondDepartmentName = "";// 二级部门名称

				// 一个二级部门下所有员工的信息和薪酬情况
				for (Map<String, Object> secondEmployeeAndBasePayroll : secondEmployeeAndBasePayrollList) {
					secondDepartmentName = String.valueOf(secondEmployeeAndBasePayroll.get("secondDepartmentName"));
					Integer erpEmployeeId = Integer
							.valueOf(String.valueOf(secondEmployeeAndBasePayroll.get("employeeId")));
					ErpBasePayroll erpBasePayroll = this.erpBasePayrollMapper
							.findBasePayrollDetailByEmpId(erpEmployeeId);
					// 如果在薪酬表中没有查询到该员工的信息，那么就只显示员工基本信息；否则将显示员工基本信息+薪酬信息
					if (erpBasePayroll == null) {
						// 将二级部门下的每个员工信息增加进list中
						employeeInfoList.add(secondEmployeeAndBasePayroll);
						continue;
					}
					/*
					 * 将数据库中加密后的薪酬信息解密
					 */
					Map<String, Double> decryptedExcelData = this.decryptExcelDataAes(erpBasePayroll);
					logger.info("decryptedExcelData" + decryptedExcelData);
					Double erpBaseWage = decryptedExcelData.get("erpBaseWage");// 基本工资
					Double erpPostWage = decryptedExcelData.get("erpPostWage");// 岗位工资
					Double erpPerformance = decryptedExcelData.get("erpPerformance");// 月度绩效
					Double erpAllowance = decryptedExcelData.get("erpAllowance");// 月度项目津贴
					Double erpSocialSecurityBase = decryptedExcelData.get("erpSocialSecurityBase");// 社保基数
					Double erpAccumulationFundBase = decryptedExcelData.get("erpAccumulationFundBase");// 公积金基数
					Double erpTelFarePerquisite = decryptedExcelData.get("erpTelFarePerquisite");// 话费补助
					// 计算二级部门月度收入合计
					secondDepMonthIncome += erpBaseWage + erpPostWage + erpPerformance + erpAllowance;

					secondEmployeeAndBasePayroll.put("erpBaseWage", erpBaseWage);
					secondEmployeeAndBasePayroll.put("erpPostWage", erpPostWage);
					secondEmployeeAndBasePayroll.put("erpPerformance", erpPerformance);
					secondEmployeeAndBasePayroll.put("erpAllowance", erpAllowance);
					secondEmployeeAndBasePayroll.put("erpSocialSecurityBase", erpSocialSecurityBase);
					secondEmployeeAndBasePayroll.put("erpAccumulationFundBase", erpAccumulationFundBase);
					secondEmployeeAndBasePayroll.put("erpTelFarePerquisite", erpTelFarePerquisite);
					// 将二级部门下的每个员工信息增加进list中
					employeeInfoList.add(secondEmployeeAndBasePayroll);
				}
				// 将员工信息列表和月度收入合计两部分信息，加进二级部门VO对象供前端展现
				secondDepEmpInfoAndSalaryVo.setEmployeeInfoList(employeeInfoList);
				secondDepEmpInfoAndSalaryVo.setSecondDepMonthIncome(secondDepMonthIncome);
				secondDepEmpInfoAndSalaryVo.setSecondDepartmentName(secondDepartmentName);
				// 将所有的二级部门的月度收入合计累加求和，作为一级部门的月度收入合计
				firstDepMonthIncome += secondDepMonthIncome;
				// 将一个二级部门下全部员工信息加入数组中
				secondDepEmpInfoAndSalaryVoList.add(secondDepEmpInfoAndSalaryVo);
			}
			// 将员工信息列表和月度收入合计两部分信息，加进一级部门VO对象供前端展现
			firstDepEmpInfoAndSalaryVo.setSecondDepEmpInfoAndSalaryVoList(secondDepEmpInfoAndSalaryVoList);
			firstDepEmpInfoAndSalaryVo.setFirstDepMonthIncome(firstDepMonthIncome);
			return RestUtils.returnSuccess(firstDepEmpInfoAndSalaryVo);
		} catch (Exception e) {
			logger.error("findSecDepEmpInfoByFirDepId出现异常：" + e.getMessage(), e);
			return RestUtils.returnSuccessWithString("findSecDepEmpInfoByFirDepId出现异常：" + e.getMessage());
		}
	}

	/**
	 * Description: 条件查询新增员工薪酬信息失败的日志记录信息
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月25日 上午10:59:47
	 * @Update Date: 2018年10月29日 下午16:00:38
	 */
	public RestResponse findBasePayrollRecord(Map<String, Object> params) {
		logger.info("进入findBasePayrollRecord方法，参数是:params=" + params);
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Integer page = Integer.valueOf(String.valueOf(params.get("page")));
			Integer rows = Integer.valueOf(String.valueOf(params.get("rows")));

			Map<String, Object> paramsMap = new HashMap<>();
			paramsMap.put("limit", rows);
			paramsMap.put("offset", rows * (page - 1));
			paramsMap.put("startTime", params.get("startTime"));
			paramsMap.put("endTime", params.get("endTime"));
			List<ErpBasePayrollRecord> list = this.erpBasePayrollRecordMapper.findBasePayrollRecord(paramsMap);
			resultMap.put("list", list);
			Long totalCount = this.erpBasePayrollRecordMapper.findTotalBasePayrollRecord(paramsMap);
			resultMap.put("total", totalCount);
			return RestUtils.returnSuccess(resultMap);
		} catch (Exception e) {
			logger.error("查询全部员工薪资信息findBasePayrollRecord 出现异常" + e.getMessage(), e);
			return RestUtils.returnSuccessWithString("查询失败 ！");
		}
	}

	/**
	 * Description: 查询权限内所有一级部门的员工信息和薪酬情况
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月25日 上午11:53:29
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RestResponse findFirDepEmpInfoByPowerParams(String token, String departmentType, String queryMode,
			String keyword) {
		logger.info("进入findFirDepEmpInfoByPowerParams方法 。参数是：token={},departmentType={},queryMode={},keyword={}", token,
				departmentType, queryMode, keyword);
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();// 用户Id
			String username = erpUser.getUsername();// 从用户信息中获取用户名
			List<Integer> roles = erpUser.getRoles();// 从用户信息中获取角色信息
			logger.info("id=" + id + ",username=" + username + ",roles=" + roles);

			// 调用ERP-人力资源 工程 的操作层服务接口-获取一级部门下面所有二级部门的员工的详细信息
			String url = protocolType + "nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoOfAllFirDepByParams";
			Map<String, Object> departmentParams = new HashMap<>();
			departmentParams.put("departmentType", departmentType);
			departmentParams.put("queryMode", queryMode);// 查询模式（0：查询在职员工+离职员工，1：查询在职员工）
			departmentParams.put("keyword", keyword);// 关键字查询（目前仅查询员工姓名）

			/*
			 * 判断当前登录用户的角色查看数据的权限
			 */
			if (roles.contains(8)) {// 总经理
				departmentParams.put("userId", null);
				departmentParams.put("superLeader", null);
			} else if (roles.contains(9)) {// 副总经理
				departmentParams.put("userId", null);
				departmentParams.put("superLeader", id);
			} else {// 一级部门经理、其他角色
				departmentParams.put("userId", id);
				departmentParams.put("superLeader", null);
			}

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);// 将token放到请求头中
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(departmentParams, requestHeaders);

			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, requestEntity, Map.class);
			if (200 != response.getStatusCodeValue()) {
				return RestUtils.returnFailure("调用人力资源工程失败！");
			}
			// 解析获取的数据
			Map<String, Object> responseBody = response.getBody();
			if (!"200".equals(responseBody.get("status"))) {
				return RestUtils.returnFailure("人力资源工程发生异常！");
			}

			/*
			 * 查询社保缴纳比例、社保缴纳基数上下限
			 */
			ErpSocialSecurity lastSocialSecurity = erpSocialSecurityMapper.selectSocialSecurityLastOne();
			Double endowmentInsuranceCompanyRatio = lastSocialSecurity.getEndowmentInsuranceCompanyRatio() / 100;// 养老保险-公司缴纳比例
			Double endowmentInsuranceBaseUpper = lastSocialSecurity.getEndowmentInsuranceBaseUpper();// 养老保险-缴纳基数上限
			Double endowmentInsuranceBaseLower = lastSocialSecurity.getEndowmentInsuranceBaseLower();// 养老保险-缴纳基数下限
			Double unemploymentInsuranceCompanyRatio = lastSocialSecurity.getUnemploymentInsuranceCompanyRatio() / 100;// 失业保险-公司缴纳比例
			Double unemploymentInsuranceBaseUpper = lastSocialSecurity.getUnemploymentInsuranceBaseUpper();// 失业保险-缴纳基数上限
			Double unemploymentInsuranceBaseLower = lastSocialSecurity.getUnemploymentInsuranceBaseLower();// 失业保险-缴纳基数下限
			Double maternityInsuranceCompanyRatio = lastSocialSecurity.getMaternityInsuranceCompanyRatio() / 100;// 生育保险-公司缴纳比例
			Double maternityInsuranceBaseUpper = lastSocialSecurity.getMaternityInsuranceBaseUpper();// 生育保险-缴纳基数上限
			Double maternityInsuranceBaseLower = lastSocialSecurity.getMaternityInsuranceBaseLower();// 生育保险-缴纳基数下限
			Double medicalInsuranceCompanyRatio = lastSocialSecurity.getMedicalInsuranceCompanyRatio() / 100;// 医疗保险-公司缴纳比例
			Double medicalInsuranceBaseUpper = lastSocialSecurity.getMedicalInsuranceBaseUpper();// 医疗保险-缴纳基数上限
			Double medicalInsuranceBaseLower = lastSocialSecurity.getMedicalInsuranceBaseLower();// 医疗保险-缴纳基数下限
			Double injuryInsuranceCompanyRatio = lastSocialSecurity.getInjuryInsuranceCompanyRatio() / 100;// 工伤保险-公司缴纳比例
			Double injuryInsuranceBaseUpper = lastSocialSecurity.getInjuryInsuranceBaseUpper();// 工伤保险-缴纳基数上限
			Double injuryInsuranceBaseLower = lastSocialSecurity.getInjuryInsuranceBaseLower();// 工伤保险-缴纳基数下限
			Double accumulationFundCompanyRatio = lastSocialSecurity.getAccumulationFundCompanyRatio() / 100;// 公积金-公司缴纳比例
			Double accumulationFundBaseUpper = lastSocialSecurity.getAccumulationFundBaseUpper();// 公积金-缴纳基数上限
			Double accumulationFundBaseLower = lastSocialSecurity.getAccumulationFundBaseLower();// 公积金-缴纳基数下限

			// 将薪酬数据精确到小数点后两位
			DecimalFormat df = new DecimalFormat("#.00");

			List<FirstDepEmpInfoAndSalaryVo> firstDepEmpInfoAndSalaryVoList = new ArrayList<>();
			List<List<List<Map<String, Object>>>> firDepEmpInfoList = (List<List<List<Map<String, Object>>>>) responseBody
					.get("data");
			logger.info("权限内的一级部门总数：" + firDepEmpInfoList.size());
			// 权限内所有一级部门员工的信息和薪酬情况
			for (List<List<Map<String, Object>>> secDepEmpInfoList : firDepEmpInfoList) {
				logger.info("一级部门下的二级部门总数：" + secDepEmpInfoList.size());
				FirstDepEmpInfoAndSalaryVo firstDepEmpInfoAndSalaryVo = new FirstDepEmpInfoAndSalaryVo();// 前端显示
				List<SecondDepEmpInfoAndSalaryVo> secondDepEmpInfoAndSalaryVoList = new ArrayList<>();// 二级部门员工信息列表
				Integer firstDepartmentId = 0;// 一级部门编号
				String firstDepartmentName = "";// 一级部门名称
				Double firstDepMonthIncome = 0.0;// 一级部门月度收入合计
				Double firstDepSocialSecurity = 0.0;// 一级部门社保合计

				// 一级部门下所有二级部门员工的信息和薪酬情况
				for (List<Map<String, Object>> empInfoList : secDepEmpInfoList) {
					logger.info("二级部门下的员工总数：" + empInfoList.size());
					SecondDepEmpInfoAndSalaryVo secondDepEmpInfoAndSalaryVo = new SecondDepEmpInfoAndSalaryVo();
					List<Map<String, Object>> employeeInfoList = new ArrayList<>();// 员工信息列表
					String secondDepartmentName = "";// 二级部门名称
					Double secondDepMonthIncome = 0.0;// 二级部门月度收入合计
					Double secondDepSocialSecurity = 0.0;// 二级部门社保合计

					// 一个二级部门下所有员工的信息和薪酬情况
					for (Map<String, Object> empInfo : empInfoList) {
						firstDepartmentId = Integer.valueOf(String.valueOf(empInfo.get("firstDepartment")));
						firstDepartmentName = String.valueOf(empInfo.get("firstDepartmentName"));
						secondDepartmentName = String.valueOf(empInfo.get("secondDepartmentName"));
						Integer erpEmployeeId = Integer.valueOf(String.valueOf(empInfo.get("employeeId")));
						ErpBasePayroll erpBasePayroll = erpBasePayrollMapper
								.findBasePayrollDetailByEmpId(erpEmployeeId);
						// 如果在薪酬表中没有查询到该员工的信息，那么就只显示员工基本信息；否则将显示员工基本信息+薪酬信息
						if (erpBasePayroll == null) {
							// 将二级部门下的每个员工信息增加进list中
							employeeInfoList.add(empInfo);
							continue;
						}
						/*
						 * 将数据库中加密后的薪酬信息通过AES解密
						 */
						logger.info("员工：" + erpEmployeeId + "将进行薪酬查询！");
						Map<String, Double> decryptedExcelData = this.decryptExcelDataAes(erpBasePayroll);
						Double erpBaseWage = decryptedExcelData.get("erpBaseWage");// 基本工资
						Double erpPostWage = decryptedExcelData.get("erpPostWage");// 岗位工资
						Double erpPerformance = decryptedExcelData.get("erpPerformance");// 月度绩效
						Double erpAllowance = decryptedExcelData.get("erpAllowance");// 月度项目津贴
						Double erpSocialSecurityBase = decryptedExcelData.get("erpSocialSecurityBase");// 社保基数
						Double erpAccumulationFundBase = decryptedExcelData.get("erpAccumulationFundBase");// 公积金基数
						Double erpTelFarePerquisite = decryptedExcelData.get("erpTelFarePerquisite");// 话费补助
						// 将二级部门下所有员工的月度收入合计累加求和，作为二级部门的月度收入合计
						secondDepMonthIncome += erpBaseWage + erpPostWage + erpPerformance + erpAllowance;
						/*
						 * 一个员工的五险一金基数
						 */
						Double endowmentInsuranceBase = 0.0;// 养老保险基数
						if (erpSocialSecurityBase < endowmentInsuranceBaseLower && erpSocialSecurityBase != 0) {
							endowmentInsuranceBase = endowmentInsuranceBaseLower;
						} else if (erpSocialSecurityBase > endowmentInsuranceBaseUpper) {
							endowmentInsuranceBase = endowmentInsuranceBaseUpper;
						} else {
							endowmentInsuranceBase = erpSocialSecurityBase;
						}
						Double unemploymentInsuranceBase = 0.0;// 失业保险基数
						if (erpSocialSecurityBase < unemploymentInsuranceBaseLower && erpSocialSecurityBase != 0) {
							unemploymentInsuranceBase = unemploymentInsuranceBaseLower;
						} else if (erpSocialSecurityBase > unemploymentInsuranceBaseUpper) {
							unemploymentInsuranceBase = unemploymentInsuranceBaseUpper;
						} else {
							unemploymentInsuranceBase = erpSocialSecurityBase;
						}
						Double maternityInsuranceBase = 0.0;// 生育保险基数
						if (erpSocialSecurityBase < maternityInsuranceBaseLower && erpSocialSecurityBase != 0) {
							maternityInsuranceBase = maternityInsuranceBaseLower;
						} else if (erpSocialSecurityBase > maternityInsuranceBaseUpper) {
							maternityInsuranceBase = maternityInsuranceBaseUpper;
						} else {
							maternityInsuranceBase = erpSocialSecurityBase;
						}
						Double medicalInsuranceBase = 0.0;// 医疗保险基数
						if (erpSocialSecurityBase < medicalInsuranceBaseLower && erpSocialSecurityBase != 0) {
							medicalInsuranceBase = medicalInsuranceBaseLower;
						} else if (erpSocialSecurityBase > medicalInsuranceBaseUpper) {
							medicalInsuranceBase = medicalInsuranceBaseUpper;
						} else {
							medicalInsuranceBase = erpSocialSecurityBase;
						}
						Double injuryInsuranceBase = 0.0;// 工伤保险基数
						if (erpSocialSecurityBase < injuryInsuranceBaseLower && erpSocialSecurityBase != 0) {
							injuryInsuranceBase = injuryInsuranceBaseLower;
						} else if (erpSocialSecurityBase > injuryInsuranceBaseUpper) {
							injuryInsuranceBase = injuryInsuranceBaseUpper;
						} else {
							injuryInsuranceBase = erpSocialSecurityBase;
						}
						Double accumulationFundBase = 0.0;// 住房公积金基数
						if (erpAccumulationFundBase < accumulationFundBaseLower && erpAccumulationFundBase != 0) {
							accumulationFundBase = accumulationFundBaseLower;
						} else if (erpAccumulationFundBase > accumulationFundBaseUpper) {
							accumulationFundBase = accumulationFundBaseUpper;
						} else {
							accumulationFundBase = erpAccumulationFundBase;
						}
						/*
						 * 将二级部门下所有员工的五险一金（公司缴纳部分）合计累加求和，作为二级部门的社保合计
						 */
						secondDepSocialSecurity += endowmentInsuranceBase * endowmentInsuranceCompanyRatio
								+ unemploymentInsuranceBase * unemploymentInsuranceCompanyRatio
								+ maternityInsuranceBase * maternityInsuranceCompanyRatio
								+ medicalInsuranceBase * medicalInsuranceCompanyRatio
								+ injuryInsuranceBase * injuryInsuranceCompanyRatio
								+ accumulationFundBase * accumulationFundCompanyRatio;

						empInfo.put("erpBaseWage", erpBaseWage);
						empInfo.put("erpPostWage", erpPostWage);
						empInfo.put("erpPerformance", erpPerformance);
						empInfo.put("erpAllowance", erpAllowance);
						empInfo.put("erpSocialSecurityBase", erpSocialSecurityBase);
						empInfo.put("erpAccumulationFundBase", erpAccumulationFundBase);
						empInfo.put("erpTelFarePerquisite", erpTelFarePerquisite);
						// 将二级部门下的每个员工信息增加进list中
						employeeInfoList.add(empInfo);
					}
					/*
					 * 将员工信息列表和月度收入合计两部分信息，加进二级部门VO对象供前端展现 将工资统计、社保合计精确到小数点后两位
					 */
					secondDepEmpInfoAndSalaryVo.setEmployeeInfoList(employeeInfoList);
					secondDepEmpInfoAndSalaryVo
							.setSecondDepSocialSecurity(Double.valueOf(df.format(secondDepSocialSecurity)));
					secondDepEmpInfoAndSalaryVo
							.setSecondDepMonthIncome(Double.valueOf(df.format(secondDepMonthIncome)));
					secondDepEmpInfoAndSalaryVo.setSecondDepartmentName(secondDepartmentName);
					// 将所有的二级部门的月度收入合计累加求和，作为一级部门的月度收入合计
					firstDepMonthIncome += secondDepMonthIncome;
					// 将所有的二级部门的社保合计累加求和，作为一级部门的社保合计
					firstDepSocialSecurity += secondDepSocialSecurity;
					// 将一个二级部门下全部员工信息加入数组中
					secondDepEmpInfoAndSalaryVoList.add(secondDepEmpInfoAndSalaryVo);
				}
				/*
				 * 将员工信息列表和月度收入合计两部分信息，加进一级部门VO对象供前端展现 将工资统计、社保合计精确到小数点后两位
				 */
				firstDepEmpInfoAndSalaryVo.setSecondDepEmpInfoAndSalaryVoList(secondDepEmpInfoAndSalaryVoList);
				firstDepEmpInfoAndSalaryVo.setFirstDepSocialSecurity(Double.valueOf(df.format(firstDepSocialSecurity)));
				firstDepEmpInfoAndSalaryVo.setFirstDepMonthIncome(Double.valueOf(df.format(firstDepMonthIncome)));
				firstDepEmpInfoAndSalaryVo.setFirstDepartmentName(firstDepartmentName);
				firstDepEmpInfoAndSalaryVo.setFirstDepartmentId(firstDepartmentId);
				firstDepEmpInfoAndSalaryVoList.add(firstDepEmpInfoAndSalaryVo);
			}
			return RestUtils.returnSuccess(firstDepEmpInfoAndSalaryVoList);
		} catch (Exception e) {
			logger.error("findFirDepEmpInfoByPowerParams出现异常：" + e.getMessage(), e);
			return RestUtils.returnSuccessWithString("方法出现异常，导致查询失败！");
		}
	}

	/**
	 * Description: 定时任务自动创建部门人员费用合计 每月5日0点新建部门人员费用合计
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月06日 上午10:02:56
	 * @Update Date: 2018年12月24日 下午14:41:15
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Transactional
	public RestResponse automaticCreateDepartmentCostMonthScheduler() {
		logger.info("进入automaticCreateDepartmentCostMonthScheduler方法，无参数");
		try {
			// 调用ERP-人力资源 工程 的操作层服务接口-获取一级部门下面所有二级部门的员工的详细信息
			String url = protocolType
					+ "nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoOfAllFirDepByParamsScheduler";
			Map<String, Object> departmentParams = new HashMap<>();
			departmentParams.put("userId", null);
			departmentParams.put("superLeader", null);
			HttpHeaders requestHeaders = new HttpHeaders();
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(departmentParams, requestHeaders);
			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, requestEntity, Map.class);
			if (200 != response.getStatusCodeValue()) {
				return RestUtils.returnFailure("调用人力资源工程失败！");
			}
			// 解析获取的数据
			Map<String, Object> responseBody = response.getBody();
			if (!"200".equals(responseBody.get("status"))) {
				return RestUtils.returnFailure("人力资源工程发生异常！");
			}

			/*
			 * 查询社保缴纳比例、社保缴纳基数上下限
			 */
			ErpSocialSecurity lastSocialSecurity = erpSocialSecurityMapper.selectSocialSecurityLastOne();
			Double endowmentInsuranceCompanyRatio = lastSocialSecurity.getEndowmentInsuranceCompanyRatio() / 100;// 养老保险-公司缴纳比例
			Double endowmentInsuranceBaseUpper = lastSocialSecurity.getEndowmentInsuranceBaseUpper();// 养老保险-缴纳基数上限
			Double endowmentInsuranceBaseLower = lastSocialSecurity.getEndowmentInsuranceBaseLower();// 养老保险-缴纳基数下限
			Double unemploymentInsuranceCompanyRatio = lastSocialSecurity.getUnemploymentInsuranceCompanyRatio() / 100;// 失业保险-公司缴纳比例
			Double unemploymentInsuranceBaseUpper = lastSocialSecurity.getUnemploymentInsuranceBaseUpper();// 失业保险-缴纳基数上限
			Double unemploymentInsuranceBaseLower = lastSocialSecurity.getUnemploymentInsuranceBaseLower();// 失业保险-缴纳基数下限
			Double maternityInsuranceCompanyRatio = lastSocialSecurity.getMaternityInsuranceCompanyRatio() / 100;// 生育保险-公司缴纳比例
			Double maternityInsuranceBaseUpper = lastSocialSecurity.getMaternityInsuranceBaseUpper();// 生育保险-缴纳基数上限
			Double maternityInsuranceBaseLower = lastSocialSecurity.getMaternityInsuranceBaseLower();// 生育保险-缴纳基数下限
			Double medicalInsuranceCompanyRatio = lastSocialSecurity.getMedicalInsuranceCompanyRatio() / 100;// 医疗保险-公司缴纳比例
			Double medicalInsuranceBaseUpper = lastSocialSecurity.getMedicalInsuranceBaseUpper();// 医疗保险-缴纳基数上限
			Double medicalInsuranceBaseLower = lastSocialSecurity.getMedicalInsuranceBaseLower();// 医疗保险-缴纳基数下限
			Double injuryInsuranceCompanyRatio = lastSocialSecurity.getInjuryInsuranceCompanyRatio() / 100;// 工伤保险-公司缴纳比例
			Double injuryInsuranceBaseUpper = lastSocialSecurity.getInjuryInsuranceBaseUpper();// 工伤保险-缴纳基数上限
			Double injuryInsuranceBaseLower = lastSocialSecurity.getInjuryInsuranceBaseLower();// 工伤保险-缴纳基数下限
			Double accumulationFundCompanyRatio = lastSocialSecurity.getAccumulationFundCompanyRatio() / 100;// 公积金-公司缴纳比例
			Double accumulationFundBaseUpper = lastSocialSecurity.getAccumulationFundBaseUpper();// 公积金-缴纳基数上限
			Double accumulationFundBaseLower = lastSocialSecurity.getAccumulationFundBaseLower();// 公积金-缴纳基数下限

			// 将薪酬数据精确到小数点后两位
			DecimalFormat df = new DecimalFormat("#.00");

			// 获取当月月度，如2018-12
			Calendar dateObj = Calendar.getInstance();
			int year = dateObj.get(Calendar.YEAR);
			int month = dateObj.get(Calendar.MONTH) + 1;
			String erpMonthNum = null;// 月度
			if (month < 10) {
				erpMonthNum = year + "-0" + month;
			} else {
				erpMonthNum = year + "-" + month;
			}

			List<List<List<Map<String, Object>>>> firDepEmpInfoList = (List<List<List<Map<String, Object>>>>) responseBody
					.get("data");
			logger.info("权限内的一级部门总数：" + firDepEmpInfoList.size());
			// 所有一级部门员工的信息和薪酬情况
			for (List<List<Map<String, Object>>> secDepEmpInfoList : firDepEmpInfoList) {
				logger.info("一级部门下的二级部门总数：" + secDepEmpInfoList.size());
				/*
				 * 一级部门的费用、信息统计
				 */
				Integer firstDepartmentId = null;// 一级部门Id
				Double firDepWageCost = 0.0;// 工资费用
				Double firDepSubsidyCost = 0.0;// 补助费用
				Double firDepPerformanceCost = 0.0;// 绩效费用
				Double firDepSocialSecurityCost = 0.0;// 社保费用
				Double firDepAccumulationFundCost = 0.0;// 公积金费用
				Integer firDepEmployeeNum = 0;// 员工人数

				// 一级部门下所有二级部门员工的信息和薪酬情况
				for (List<Map<String, Object>> empInfoList : secDepEmpInfoList) {
					logger.info("二级部门下的员工总数：" + empInfoList.size());
					/*
					 * 二级部门的费用、信息统计
					 */
					Double secDepWageCost = 0.0;// 工资费用
					Double secDepSubsidyCost = 0.0;// 补助费用
					Double secDepPerformanceCost = 0.0;// 绩效费用
					Double secDepSocialSecurityCost = 0.0;// 社保费用
					Double secDepAccumulationFundCost = 0.0;// 公积金费用
					Integer secDepEmployeeNum = empInfoList.size();// 员工人数

					// 一个二级部门下所有员工的信息和薪酬情况
					for (Map<String, Object> empInfo : empInfoList) {
						firstDepartmentId = Integer.valueOf(String.valueOf(empInfo.get("firstDepartment")));// 一级部门Id
						Integer erpEmployeeId = Integer.valueOf(String.valueOf(empInfo.get("employeeId")));// 员工Id
						ErpBasePayroll erpBasePayroll = erpBasePayrollMapper
								.findBasePayrollDetailByEmpId(erpEmployeeId);
						// 如果在薪酬表中没有查询到该员工的信息，那么跳过本次循环，不做统计；否则将该员工的薪酬信息统计到二级部门下
						if (erpBasePayroll == null) {
							continue;
						}
						/*
						 * 解密基本薪资表数据
						 */
						Map<String, Double> decryptedExcelData = this.decryptExcelDataAes(erpBasePayroll);
						logger.info("decryptedExcelData=" + decryptedExcelData);
						Double erpBaseWage = decryptedExcelData.get("erpBaseWage");// 基本工资
						Double erpPostWage = decryptedExcelData.get("erpPostWage");// 岗位工资
						// Double erpPerformance = decryptedExcelData.get("erpPerformance");//月度绩效
						Double erpAllowance = decryptedExcelData.get("erpAllowance");// 月度项目津贴
						Double erpSocialSecurityBase = decryptedExcelData.get("erpSocialSecurityBase");// 社保基数
						Double erpAccumulationFundBase = decryptedExcelData.get("erpAccumulationFundBase");// 公积金基数
						Double erpTelFarePerquisite = decryptedExcelData.get("erpTelFarePerquisite");// 话费补助
						/*
						 * 根据员工ID查询其月度绩效记录，解密月度绩效表数据
						 */
						Double erpMonthMeritPay = 0.0;// 工资绩效
						Double erpMonthProjectPay = 0.0;// 项目绩效
						Map<String, Object> param = new HashMap<>();
						param.put("erpMonthEmpId", erpEmployeeId);
						param.put("erpMonthNum", erpMonthNum);
						ErpSalaryMonthPerformance erpSalaryMonthPerformance = erpMonthPerformanceMapper
								.findEmpMonthPerformanceDetail(param);
						if (null != erpSalaryMonthPerformance) {
							Map<String, String> encryptedPerformanceData = new HashMap<>();
							encryptedPerformanceData.put("erpMonthMeritPay",
									erpSalaryMonthPerformance.getErpMonthMeritPay());
							encryptedPerformanceData.put("erpMonthProjectPay",
									erpSalaryMonthPerformance.getErpMonthProjectPay());
							encryptedPerformanceData.put("erpMonthDPay", erpSalaryMonthPerformance.getErpMonthDPay());
							Map<String, Double> decryptedPerformanceData = erpMonthPerformanceService
									.decryptPerformanceDataAes(encryptedPerformanceData);
							erpMonthMeritPay = decryptedPerformanceData.get("erpMonthMeritPay");// 工资绩效
							erpMonthProjectPay = decryptedPerformanceData.get("erpMonthProjectPay");// 项目绩效
						}
						/*
						 * 一个员工的五险一金基数
						 */
						Double endowmentInsuranceBase = 0.0;// 养老保险基数
						if (erpSocialSecurityBase < endowmentInsuranceBaseLower) {
							endowmentInsuranceBase = endowmentInsuranceBaseLower;
						} else if (erpSocialSecurityBase > endowmentInsuranceBaseUpper) {
							endowmentInsuranceBase = endowmentInsuranceBaseUpper;
						} else {
							endowmentInsuranceBase = erpSocialSecurityBase;
						}
						Double unemploymentInsuranceBase = 0.0;// 失业保险基数
						if (erpSocialSecurityBase < unemploymentInsuranceBaseLower) {
							unemploymentInsuranceBase = unemploymentInsuranceBaseLower;
						} else if (erpSocialSecurityBase > unemploymentInsuranceBaseUpper) {
							unemploymentInsuranceBase = unemploymentInsuranceBaseUpper;
						} else {
							unemploymentInsuranceBase = erpSocialSecurityBase;
						}
						Double maternityInsuranceBase = 0.0;// 生育保险基数
						if (erpSocialSecurityBase < maternityInsuranceBaseLower) {
							maternityInsuranceBase = maternityInsuranceBaseLower;
						} else if (erpSocialSecurityBase > maternityInsuranceBaseUpper) {
							maternityInsuranceBase = maternityInsuranceBaseUpper;
						} else {
							maternityInsuranceBase = erpSocialSecurityBase;
						}
						Double medicalInsuranceBase = 0.0;// 医疗保险基数
						if (erpSocialSecurityBase < medicalInsuranceBaseLower) {
							medicalInsuranceBase = medicalInsuranceBaseLower;
						} else if (erpSocialSecurityBase > medicalInsuranceBaseUpper) {
							medicalInsuranceBase = medicalInsuranceBaseUpper;
						} else {
							medicalInsuranceBase = erpSocialSecurityBase;
						}
						Double injuryInsuranceBase = 0.0;// 工伤保险基数
						if (erpSocialSecurityBase < injuryInsuranceBaseLower) {
							injuryInsuranceBase = injuryInsuranceBaseLower;
						} else if (erpSocialSecurityBase > injuryInsuranceBaseUpper) {
							injuryInsuranceBase = injuryInsuranceBaseUpper;
						} else {
							injuryInsuranceBase = erpSocialSecurityBase;
						}
						Double accumulationFundBase = 0.0;// 住房公积金基数
						if (erpAccumulationFundBase < accumulationFundBaseLower) {
							accumulationFundBase = accumulationFundBaseLower;
						} else if (erpAccumulationFundBase > accumulationFundBaseUpper) {
							accumulationFundBase = accumulationFundBaseUpper;
						} else {
							accumulationFundBase = erpAccumulationFundBase;
						}
						/*
						 * 将二级部门下所有员工的五险一金（公司缴纳部分）合计累加求和，作为二级部门的社保合计
						 */
						secDepWageCost += erpBaseWage + erpPostWage + erpAllowance;// 工资费用
						secDepSubsidyCost += erpTelFarePerquisite;// 补助费用
						// secDepPerformanceCost += erpPerformance;//绩效费用
						secDepPerformanceCost += erpMonthMeritPay + erpMonthProjectPay;// 绩效费用
						secDepSocialSecurityCost += endowmentInsuranceBase * endowmentInsuranceCompanyRatio
								+ unemploymentInsuranceBase * unemploymentInsuranceCompanyRatio
								+ maternityInsuranceBase * maternityInsuranceCompanyRatio
								+ medicalInsuranceBase * medicalInsuranceCompanyRatio
								+ injuryInsuranceBase * injuryInsuranceCompanyRatio;// 社保费用
						secDepAccumulationFundCost += accumulationFundBase * accumulationFundCompanyRatio;// 公积金费用
					}
					/*
					 * 一级部门的费用、信息统计
					 */
					firDepWageCost += secDepWageCost;// 工资费用
					firDepSubsidyCost += secDepSubsidyCost;// 补助费用
					firDepPerformanceCost += secDepPerformanceCost;// 绩效费用
					firDepSocialSecurityCost += secDepSocialSecurityCost;// 社保费用
					firDepAccumulationFundCost += secDepAccumulationFundCost;// 公积金费用
					firDepEmployeeNum += secDepEmployeeNum;// 员工人数
				}
				/*
				 * 薪酬数据加密
				 */
				Map<String, String> departmentCostData = new HashMap<>();
				departmentCostData.put("firDepWageCost", df.format(firDepWageCost));
				departmentCostData.put("firDepSubsidyCost", df.format(firDepSubsidyCost));
				departmentCostData.put("firDepPerformanceCost", df.format(firDepPerformanceCost));
				departmentCostData.put("firDepSocialSecurityCost", df.format(firDepSocialSecurityCost));
				departmentCostData.put("firDepAccumulationFundCost", df.format(firDepAccumulationFundCost));
				Map<String, String> encryptedDepartmentCostData = this.encryptDepartmentCostDataAes(departmentCostData);
				/*
				 * 给PO对象赋值，并数据入库
				 */
				ErpDepartmentCostMonth departmentCostMonth = new ErpDepartmentCostMonth();
				departmentCostMonth.setMonth(erpMonthNum);
				departmentCostMonth.setFirstDepartmentId(firstDepartmentId);
				departmentCostMonth.setWageCost(encryptedDepartmentCostData.get("firDepWageCost"));
				departmentCostMonth.setSubsidyCost(encryptedDepartmentCostData.get("firDepSubsidyCost"));
				departmentCostMonth.setPerformanceCost(encryptedDepartmentCostData.get("firDepPerformanceCost"));
				departmentCostMonth.setSocialSecurityCost(encryptedDepartmentCostData.get("firDepSocialSecurityCost"));
				departmentCostMonth
						.setAccumulationFundCost(encryptedDepartmentCostData.get("firDepAccumulationFundCost"));
				departmentCostMonth.setEmployeeNum(firDepEmployeeNum);
				/*
				 * 通过一级部门Id和月份查询部门费用统计表有没有一条记录，来决定是新增？还是更新？
				 */
				Map<String, Object> params = new HashMap<>();
				params.put("firstDepartmentId", firstDepartmentId);
				params.put("month", erpMonthNum);
				ErpDepartmentCostMonth validDepartmentCostMonth = erpDepartmentCostMonthMapper
						.findDepartmentCostMonthDetail(params);
				/*
				 * 如果该一级部门在本月有统计记录，则更新；如果没有，则新增
				 */
				if (null == validDepartmentCostMonth) {
					erpDepartmentCostMonthMapper.insertDepartmentCostMonth(departmentCostMonth);
				} else {
					Integer departmentCostMonthId = validDepartmentCostMonth.getId();// 部门人员费用统计Id
					departmentCostMonth.setId(departmentCostMonthId);
					erpDepartmentCostMonthMapper.updateDepartmentCostMonth(departmentCostMonth);
				}
			}
			logger.info("automaticCreateDepartmentCostMonthScheduler执行成功！");
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("automaticCreateDepartmentCostMonthScheduler发生异常 ：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法发生异常，导致初始化部门人员费用统计失败！");
		}
	}

	/**
	 * Description: 查询所有部门的人员费用合计
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月11日 上午16:58:05
	 */
	@SuppressWarnings("unchecked")
	public RestResponse findAllDepartmentCostMonth(String startTime, String endTime, String token) {
		logger.info("进入findAllDepartmentCostMonth方法，参数是：startTime=" + startTime + ",endTime=" + endTime + ",token="
				+ token);
		try {
			/*
			 * 判断当前登录用户的角色查看数据的权限
			 */
			List<Map<String, Object>> firstDepartmentList = (List<Map<String, Object>>) erpMonthPerformanceService
					.findAllFirstDepartmentByPowerParams(token).getData();

			/*
			 * 通过起止月份查询权限内所有一级部门的费用统计
			 */
			List<ErpDepartmentCostMonth> departmentCostMonthListByPower = new ArrayList<>();
			for (Map<String, Object> firstDepartment : firstDepartmentList) {
				Object firstDepartmentId = firstDepartment.get("departmentId");
				Map<String, Object> params = new HashMap<>();
				params.put("startTime", startTime);
				params.put("endTime", endTime);
				params.put("firstDepartmentId", firstDepartmentId);
				List<ErpDepartmentCostMonth> departmentCostMonthList = erpDepartmentCostMonthMapper
						.findDepartmentCostMonthMore(params);
				departmentCostMonthListByPower.addAll(departmentCostMonthList);
			}

			for (ErpDepartmentCostMonth departmentCostMonth : departmentCostMonthListByPower) {
				/*
				 * 将薪酬数据解密
				 */
				Map<String, Double> decryptedDepartmentCostData = this
						.decryptDepartmentCostDataAes(departmentCostMonth);
				String firDepWageCost = String.valueOf(decryptedDepartmentCostData.get("firDepWageCost"));// 工资费用
				String firDepSubsidyCost = String.valueOf(decryptedDepartmentCostData.get("firDepSubsidyCost"));// 补助费用
				String firDepPerformanceCost = String.valueOf(decryptedDepartmentCostData.get("firDepPerformanceCost"));// 社保费用
				String firDepSocialSecurityCost = String
						.valueOf(decryptedDepartmentCostData.get("firDepSocialSecurityCost"));// 公积金费用
				String firDepAccumulationFundCost = String
						.valueOf(decryptedDepartmentCostData.get("firDepAccumulationFundCost"));// 社保基数
				/*
				 * 将解密后的薪酬数据覆盖掉原有数据，并返回给前端展现
				 */
				departmentCostMonth.setWageCost(firDepWageCost);
				departmentCostMonth.setSubsidyCost(firDepSubsidyCost);
				departmentCostMonth.setPerformanceCost(firDepPerformanceCost);
				departmentCostMonth.setSocialSecurityCost(firDepSocialSecurityCost);
				departmentCostMonth.setAccumulationFundCost(firDepAccumulationFundCost);
			}
			return RestUtils.returnSuccess(departmentCostMonthListByPower);
		} catch (Exception e) {
			logger.error("findAllDepartmentCostMonth发生异常 ：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法异常，导致查询失败！");
		}
	}

	/**
	 * Description: 查询指定部门的人员费用合计
	 * 
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月06日 下午14:38:27
	 */
	public RestResponse findOneDepartmentCostMonth(Integer firstDepartmentId) {
		logger.info("进入findOneDepartmentCostMonth方法，参数是：firstDepartmentId=" + firstDepartmentId);
		try {
			// 获取当前月度，如2018-12；获取过去月度，如2016-12
			Calendar dateObj = Calendar.getInstance();
			int year = dateObj.get(Calendar.YEAR);
			int month = dateObj.get(Calendar.MONTH) + 1;
			String nowMonth = null;// 当前的月度
			String pastMonth = null;// 24个月之前的月度
			if (month < 10) {
				nowMonth = year + "-0" + month;
				pastMonth = year - 2 + "-0" + month;
			} else {
				nowMonth = year + "-" + month;
				pastMonth = year - 2 + "-" + month;
			}

			/*
			 * 通过一级部门Id查询该部门当年所有的费用统计
			 */
			Map<String, Object> params = new HashMap<>();
			params.put("firstDepartmentId", firstDepartmentId);
			params.put("startTime", pastMonth);
			params.put("endTime", nowMonth);
			List<ErpDepartmentCostMonth> departmentCostMonthList = erpDepartmentCostMonthMapper
					.findDepartmentCostMonthMore(params);
			for (ErpDepartmentCostMonth departmentCostMonth : departmentCostMonthList) {
				/*
				 * 将薪酬数据解密
				 */
				Map<String, Double> decryptedDepartmentCostData = this
						.decryptDepartmentCostDataAes(departmentCostMonth);
				String firDepWageCost = String.valueOf(decryptedDepartmentCostData.get("firDepWageCost"));// 工资费用
				String firDepSubsidyCost = String.valueOf(decryptedDepartmentCostData.get("firDepSubsidyCost"));// 补助费用
				String firDepPerformanceCost = String.valueOf(decryptedDepartmentCostData.get("firDepPerformanceCost"));// 社保费用
				String firDepSocialSecurityCost = String
						.valueOf(decryptedDepartmentCostData.get("firDepSocialSecurityCost"));// 公积金费用
				String firDepAccumulationFundCost = String
						.valueOf(decryptedDepartmentCostData.get("firDepAccumulationFundCost"));// 社保基数
				/*
				 * 将解密后的薪酬数据覆盖掉原有数据，并返回给前端展现
				 */
				departmentCostMonth.setWageCost(firDepWageCost);
				departmentCostMonth.setSubsidyCost(firDepSubsidyCost);
				departmentCostMonth.setPerformanceCost(firDepPerformanceCost);
				departmentCostMonth.setSocialSecurityCost(firDepSocialSecurityCost);
				departmentCostMonth.setAccumulationFundCost(firDepAccumulationFundCost);
			}
			return RestUtils.returnSuccess(departmentCostMonthList);
		} catch (Exception e) {
			logger.error("findOneDepartmentCostMonth发生异常 ：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法异常，导致查询失败！");
		}
	}

	/**
	 * Description: 新增社保缴纳比例、社保缴纳基数上下限
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月26日 下午16:23:25
	 */
	public RestResponse insertSocialSecurity(ErpSocialSecurity socialSecurity) {
		logger.info("insertSocialSecurity方法开始执行，传递参数：" + socialSecurity);
		try {
			// 倒叙查询全部的记录，取第一条作为当前最新的数据，修改这条数据的失效时间
			ErpSocialSecurity lastSocialSecurity = erpSocialSecurityMapper.selectSocialSecurityLastOne();
			if (lastSocialSecurity != null) {
				lastSocialSecurity.setEndTime(socialSecurity.getStartTime());
				erpSocialSecurityMapper.updateSocialSecurity(lastSocialSecurity);
			}
			erpSocialSecurityMapper.insertSocialSecurity(socialSecurity);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("insertSocialSecurity方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}

	/**
	 * Description: 修改社保缴纳比例、社保缴纳基数上下限
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年11月26日 下午16:23:25
	 */
	public RestResponse updateSocialSecurity(ErpSocialSecurity socialSecurity) {
		logger.info("updateSocialSecurity方法开始执行，传递参数：" + socialSecurity);
		try {
			erpSocialSecurityMapper.updateSocialSecurity(socialSecurity);
			return RestUtils.returnSuccessWithString("修改成功！");
		} catch (Exception e) {
			logger.error("updateSocialSecurity方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常！导致操作失败！");
		}
	}

	/**
	 * Description: 查询社保缴纳比例、社保缴纳基数上下限
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月21日 上午10:54:59
	 */
	public RestResponse findSocialSecurity() {
		logger.info("findSocialSecurity方法开始执行，传递参数：无参数");
		try {
			List<ErpSocialSecurity> socialSecurityList = erpSocialSecurityMapper.selectSocialSecurityAll();
			return RestUtils.returnSuccess(socialSecurityList);
		} catch (Exception e) {
			logger.error("findSocialSecurity方法出现异常：" + e.getMessage(), e);
			return RestUtils.returnFailure("方法出现异常！导致查询失败！");
		}
	}

	/*
	 * ******************************************* 封装的工具方法
	 * *******************************************
	 */

	/**
	 * Description: 对导入的Excel进行逐行数据内容、格式校验 1、对于姓名、身份证号进行空值判断。如果为空，这行数据无效，不能入库
	 * 2、对于其他字段进行数据格式校验。如果不是数字类型的，这行数据无效，不能入库 3、当此方法抛出异常的时候，应该判定为“未知异常”
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月26日 上午11:12:40
	 */
	public Map<String, String> validateExcelData(Map<String, String> excelDataMap) {
		Map<String, String> validateResultMap = new HashMap<>();

		/*
		 * 获取表格一行的所有数据
		 */
		String name = excelDataMap.get("name");// 姓名
		String idCardNumber = excelDataMap.get("idCardNumber");// 身份证号码
		String erpBaseWage = excelDataMap.get("erpBaseWage");// 基本工资
		String erpPostWage = excelDataMap.get("erpPostWage");// 岗位工资
		String erpPerformance = excelDataMap.get("erpPerformance");// 月度绩效
		String erpAllowance = excelDataMap.get("erpAllowance");// 月度项目津贴
		String erpSocialSecurityBase = excelDataMap.get("erpSocialSecurityBase");// 社保基数
		String erpAccumulationFundBase = excelDataMap.get("erpAccumulationFundBase");// 公积金基数
		String erpTelFarePerquisite = excelDataMap.get("erpTelFarePerquisite");// 话费补助

		if (name == null) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的员工姓名为空！");
			return validateResultMap;
		}

		if (idCardNumber == null) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的员工身份证号码为空！");
			return validateResultMap;
		}

		/*
		 * 对于薪酬数据的校验：当不为空值的时候，采用正则表达式，来判断是否为数字
		 */
//		String regex = "[0-9]*(\\.?)[0-9]*";
		String regex = "^(-?\\d+)(\\.\\d+)?$"; // 2019-09-29

		if (erpBaseWage != null && !Pattern.matches(regex, erpBaseWage)) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的基本工资不是数字格式！");
			validateResultMap.put("name", name);
			validateResultMap.put("idCardNumber", idCardNumber);
			return validateResultMap;
		}

		if (erpPostWage != null && !Pattern.matches(regex, erpPostWage)) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的岗位工资不是数字格式！");
			validateResultMap.put("name", name);
			validateResultMap.put("idCardNumber", idCardNumber);
			return validateResultMap;
		}

		if (erpPerformance != null && !Pattern.matches(regex, erpPerformance)) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的月度绩效不是数字格式！");
			validateResultMap.put("name", name);
			validateResultMap.put("idCardNumber", idCardNumber);
			return validateResultMap;
		}

		if (erpAllowance != null && !Pattern.matches(regex, erpAllowance)) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的月度项目津贴不是数字格式！");
			validateResultMap.put("name", name);
			validateResultMap.put("idCardNumber", idCardNumber);
			return validateResultMap;
		}

		if (erpSocialSecurityBase != null && !Pattern.matches(regex, erpSocialSecurityBase)) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的社保基数不是数字格式！");
			validateResultMap.put("name", name);
			validateResultMap.put("idCardNumber", idCardNumber);
			return validateResultMap;
		}

		if (erpAccumulationFundBase != null && !Pattern.matches(regex, erpAccumulationFundBase)) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的公积金基数不是数字格式！");
			validateResultMap.put("name", name);
			validateResultMap.put("idCardNumber", idCardNumber);
			return validateResultMap;
		}

		if (erpTelFarePerquisite != null && !Pattern.matches(regex, erpTelFarePerquisite)) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的话费补助不是数字格式！");
			validateResultMap.put("name", name);
			validateResultMap.put("idCardNumber", idCardNumber);
			return validateResultMap;
		}

		/*
		 * 所有的数据全部校验成功
		 */
		validateResultMap.put("isError", "N");
		return validateResultMap;
	}

	/**
	 * Description: 将导入的Excel表格数据解析成Map格式的数据
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月26日 下午15:03:18
	 */
	public Map<String, String> convertRowToMap(Row row) {
		Map<String, String> excelDataMap = new HashMap<>();

		/*
		 * 获取表格一行的所有数据
		 */
		Cell name = row.getCell(0);// 姓名
		Cell idCardNumber = row.getCell(1);// 身份证号码
		Cell erpBaseWage = row.getCell(2);// 基本工资
		Cell erpPostWage = row.getCell(3);// 岗位工资
		Cell erpPerformance = row.getCell(4);// 月度绩效
		Cell erpAllowance = row.getCell(5);// 月度项目津贴
		Cell erpSocialSecurityBase = row.getCell(6);// 社保基数
		Cell erpAccumulationFundBase = row.getCell(7);// 公积金基数
		Cell erpTelFarePerquisite = row.getCell(8);// 话费补助

		if (name == null || "".equals(String.valueOf(name).trim())) {
			excelDataMap.put("name", null);
		} else {
			excelDataMap.put("name", String.valueOf(name).trim());
		}

		if (idCardNumber == null || "".equals(String.valueOf(idCardNumber).trim())) {
			excelDataMap.put("idCardNumber", null);
		} else {
			excelDataMap.put("idCardNumber", String.valueOf(idCardNumber).trim());
		}

		if (erpBaseWage == null || "".equals(String.valueOf(erpBaseWage).trim())) {
			excelDataMap.put("erpBaseWage", null);
		} else {
			excelDataMap.put("erpBaseWage", String.valueOf(erpBaseWage).trim());
		}

		if (erpPostWage == null || "".equals(String.valueOf(erpPostWage).trim())) {
			excelDataMap.put("erpPostWage", null);
		} else {
			excelDataMap.put("erpPostWage", String.valueOf(erpPostWage).trim());
		}

		if (erpPerformance == null || "".equals(String.valueOf(erpPerformance).trim())) {
			excelDataMap.put("erpPerformance", null);
		} else {
			excelDataMap.put("erpPerformance", String.valueOf(erpPerformance).trim());
		}

		if (erpAllowance == null || "".equals(String.valueOf(erpAllowance).trim())) {
			excelDataMap.put("erpAllowance", null);
		} else {
			excelDataMap.put("erpAllowance", String.valueOf(erpAllowance).trim());
		}

		if (erpSocialSecurityBase == null || "".equals(String.valueOf(erpSocialSecurityBase).trim())) {
			excelDataMap.put("erpSocialSecurityBase", null);
		} else {
			excelDataMap.put("erpSocialSecurityBase", String.valueOf(erpSocialSecurityBase).trim());
		}

		if (erpAccumulationFundBase == null || "".equals(String.valueOf(erpAccumulationFundBase).trim())) {
			excelDataMap.put("erpAccumulationFundBase", null);
		} else {
			excelDataMap.put("erpAccumulationFundBase", String.valueOf(erpAccumulationFundBase).trim());
		}

		if (erpTelFarePerquisite == null || "".equals(String.valueOf(erpTelFarePerquisite).trim())) {
			excelDataMap.put("erpTelFarePerquisite", null);
		} else {
			excelDataMap.put("erpTelFarePerquisite", String.valueOf(erpTelFarePerquisite).trim());
		}

		return excelDataMap;
	}

	/**
	 * Description: AES薪酬数据加密
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月13日 下午16:17:47
	 */
	public Map<String, String> encryptExcelDataAes(Map<String, String> excelDataMap) {
		// logger.info("进入加密方法"+excelDataMap);
		/*
		 * 获取到所有需要加密的薪酬数据
		 */
		String erpBaseWage = excelDataMap.get("erpBaseWage");// 基本工资
		String erpPostWage = excelDataMap.get("erpPostWage");// 岗位工资
		String erpPerformance = excelDataMap.get("erpPerformance");// 月度绩效
		String erpAllowance = excelDataMap.get("erpAllowance");// 月度项目津贴
		String erpSocialSecurityBase = excelDataMap.get("erpSocialSecurityBase");// 社保基数
		String erpAccumulationFundBase = excelDataMap.get("erpAccumulationFundBase");// 公积金基数
		String erpTelFarePerquisite = excelDataMap.get("erpTelFarePerquisite");// 话费补助
		/*
		 * 将薪酬加密后，赋值给Map 如果薪酬为空，那么赋值为0.0
		 */
		Map<String, String> encryptedExcelData = new HashMap<>();
		String defaultSalaryValue = AesUtils.encrypt(String.valueOf(0.0));

		if (erpBaseWage == null) {
			encryptedExcelData.put("erpBaseWage", defaultSalaryValue);
		} else {
			encryptedExcelData.put("erpBaseWage", AesUtils.encrypt(erpBaseWage));
		}

		if (erpPostWage == null) {
			encryptedExcelData.put("erpPostWage", defaultSalaryValue);
		} else {
			encryptedExcelData.put("erpPostWage", AesUtils.encrypt(erpPostWage));
		}

		if (erpPerformance == null) {
			encryptedExcelData.put("erpPerformance", defaultSalaryValue);
		} else {
			encryptedExcelData.put("erpPerformance", AesUtils.encrypt(erpPerformance));
		}

		if (erpAllowance == null) {
			encryptedExcelData.put("erpAllowance", defaultSalaryValue);
		} else {
			encryptedExcelData.put("erpAllowance", AesUtils.encrypt(erpAllowance));
		}

		if (erpSocialSecurityBase == null) {
			encryptedExcelData.put("erpSocialSecurityBase", defaultSalaryValue);
		} else {
			encryptedExcelData.put("erpSocialSecurityBase", AesUtils.encrypt(erpSocialSecurityBase));
		}

		if (erpAccumulationFundBase == null) {
			encryptedExcelData.put("erpAccumulationFundBase", defaultSalaryValue);
		} else {
			encryptedExcelData.put("erpAccumulationFundBase", AesUtils.encrypt(erpAccumulationFundBase));
		}

		if (erpTelFarePerquisite == null) {
			encryptedExcelData.put("erpTelFarePerquisite", defaultSalaryValue);
		} else {
			encryptedExcelData.put("erpTelFarePerquisite", AesUtils.encrypt(erpTelFarePerquisite));
		}
		return encryptedExcelData;
	}

	/**
	 * Description: AES薪酬数据解密
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月13日 下午17:29:18
	 */
	public Map<String, Double> decryptExcelDataAes(ErpBasePayroll encryptedExcelData) {
		// logger.info("进入解密方法"+encryptedExcelData);
		/*
		 * 获取到所有需要解密的薪酬数据
		 */
		String erpBaseWage = encryptedExcelData.getErpBaseWage();// 基本工资
		String erpPostWage = encryptedExcelData.getErpPostWage();// 岗位工资
		String erpPerformance = encryptedExcelData.getErpPerformance();// 月度绩效
		String erpAllowance = encryptedExcelData.getErpAllowance();// 月度项目津贴
		String erpSocialSecurityBase = encryptedExcelData.getErpSocialSecurityBase();// 社保基数
		String erpAccumulationFundBase = encryptedExcelData.getErpAccumulationFundBase();// 公积金基数
		String erpTelFarePerquisite = encryptedExcelData.getErpTelFarePerquisite();// 话费补助
		/*
		 * 将薪酬解密，将字符串类型转换为浮点型，赋值给Map 如果数据库该字段为空，那么赋值为0.0，为了合计薪酬使用
		 */
		Map<String, Double> decryptedExcelData = new HashMap<>();
		if (erpBaseWage == null || !StringUtil.isNumber(AesUtils.decrypt(erpBaseWage))) {
			decryptedExcelData.put("erpBaseWage", 0.0);
		} else {
			decryptedExcelData.put("erpBaseWage", Double.valueOf(AesUtils.decrypt(erpBaseWage)));
		}
		if (erpPostWage == null || !StringUtil.isNumber(AesUtils.decrypt(erpPostWage))) {
			decryptedExcelData.put("erpPostWage", 0.0);
		} else {
			decryptedExcelData.put("erpPostWage", Double.valueOf(AesUtils.decrypt(erpPostWage)));
		}
		if (erpPerformance == null || !StringUtil.isNumber(AesUtils.decrypt(erpPerformance))) {
			decryptedExcelData.put("erpPerformance", 0.0);
		} else {
			decryptedExcelData.put("erpPerformance", Double.valueOf(AesUtils.decrypt(erpPerformance)));
		}
		if (erpAllowance == null || !StringUtil.isNumber(AesUtils.decrypt(erpAllowance))) {
			decryptedExcelData.put("erpAllowance", 0.0);
		} else {
			decryptedExcelData.put("erpAllowance", Double.valueOf(AesUtils.decrypt(erpAllowance)));
		}
		if (erpSocialSecurityBase == null || !StringUtil.isNumber(AesUtils.decrypt(erpSocialSecurityBase))) {
			decryptedExcelData.put("erpSocialSecurityBase", 0.0);
		} else {
			decryptedExcelData.put("erpSocialSecurityBase", Double.valueOf(AesUtils.decrypt(erpSocialSecurityBase)));
		}
		if (erpAccumulationFundBase == null || !StringUtil.isNumber(AesUtils.decrypt(erpAccumulationFundBase))) {
			decryptedExcelData.put("erpAccumulationFundBase", 0.0);
		} else {
			decryptedExcelData.put("erpAccumulationFundBase",
					Double.valueOf(AesUtils.decrypt(erpAccumulationFundBase)));
		}
		if (erpTelFarePerquisite == null|| !StringUtil.isNumber(AesUtils.decrypt(erpTelFarePerquisite))) {
			decryptedExcelData.put("erpTelFarePerquisite", 0.0);
		} else {
			decryptedExcelData.put("erpTelFarePerquisite", Double.valueOf(AesUtils.decrypt(erpTelFarePerquisite)));
		}
		return decryptedExcelData;
	}

	/**
	 * Description: 部门费用统计中的薪酬数据加密
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月11日 下午16:27:31
	 */
	public Map<String, String> encryptDepartmentCostDataAes(Map<String, String> departmentCostData) {
		// logger.info("进入加密方法"+departmentCostData);
		/*
		 * 获取到所有需要加密的薪酬数据
		 */
		String firDepWageCost = departmentCostData.get("firDepWageCost");// 工资费用
		String firDepSubsidyCost = departmentCostData.get("firDepSubsidyCost");// 补助费用
		String firDepPerformanceCost = departmentCostData.get("firDepPerformanceCost");// 社保费用
		String firDepSocialSecurityCost = departmentCostData.get("firDepSocialSecurityCost");// 公积金费用
		String firDepAccumulationFundCost = departmentCostData.get("firDepAccumulationFundCost");// 社保基数
		/*
		 * 将薪酬加密后，赋值给Map 如果薪酬为空，那么赋值为0.0
		 */
		Map<String, String> encryptedDepartmentCostData = new HashMap<>();
		String defaultSalaryValue = AesUtils.encrypt(String.valueOf(0.0));

		if (firDepWageCost == null) {
			encryptedDepartmentCostData.put("firDepWageCost", defaultSalaryValue);
		} else {
			encryptedDepartmentCostData.put("firDepWageCost", AesUtils.encrypt(firDepWageCost));
		}
		if (firDepSubsidyCost == null) {
			encryptedDepartmentCostData.put("firDepSubsidyCost", defaultSalaryValue);
		} else {
			encryptedDepartmentCostData.put("firDepSubsidyCost", AesUtils.encrypt(firDepSubsidyCost));
		}
		if (firDepPerformanceCost == null) {
			encryptedDepartmentCostData.put("firDepPerformanceCost", defaultSalaryValue);
		} else {
			encryptedDepartmentCostData.put("firDepPerformanceCost", AesUtils.encrypt(firDepPerformanceCost));
		}
		if (firDepSocialSecurityCost == null) {
			encryptedDepartmentCostData.put("firDepSocialSecurityCost", defaultSalaryValue);
		} else {
			encryptedDepartmentCostData.put("firDepSocialSecurityCost", AesUtils.encrypt(firDepSocialSecurityCost));
		}
		if (firDepAccumulationFundCost == null) {
			encryptedDepartmentCostData.put("firDepAccumulationFundCost", defaultSalaryValue);
		} else {
			encryptedDepartmentCostData.put("firDepAccumulationFundCost", AesUtils.encrypt(firDepAccumulationFundCost));
		}
		return encryptedDepartmentCostData;
	}

	/**
	 * Description: 部门费用统计中的薪酬数据解密
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月11日 下午16:44:41
	 */
	public Map<String, Double> decryptDepartmentCostDataAes(ErpDepartmentCostMonth encryptedDepartmentCostData) {
		// logger.info("进入解密方法"+encryptedDepartmentCostData);
		/*
		 * 获取到所有需要解密的薪酬数据
		 */
		String firDepWageCost = encryptedDepartmentCostData.getWageCost();// 工资费用
		String firDepSubsidyCost = encryptedDepartmentCostData.getSubsidyCost();// 补助费用
		String firDepPerformanceCost = encryptedDepartmentCostData.getPerformanceCost();// 社保费用
		String firDepSocialSecurityCost = encryptedDepartmentCostData.getSocialSecurityCost();// 公积金费用
		String firDepAccumulationFundCost = encryptedDepartmentCostData.getAccumulationFundCost();// 社保基数

		/*
		 * 将薪酬解密，将字符串类型转换为浮点型，赋值给Map 如果数据库该字段为空，那么赋值为0.0，为了合计薪酬使用
		 */
		Map<String, Double> decryptedDepartmentCostData = new HashMap<>();
		if (firDepWageCost == null) {
			decryptedDepartmentCostData.put("firDepWageCost", 0.0);
		} else {
			decryptedDepartmentCostData.put("firDepWageCost", Double.valueOf(AesUtils.decrypt(firDepWageCost)));
		}
		if (firDepSubsidyCost == null) {
			decryptedDepartmentCostData.put("firDepSubsidyCost", 0.0);
		} else {
			decryptedDepartmentCostData.put("firDepSubsidyCost", Double.valueOf(AesUtils.decrypt(firDepSubsidyCost)));
		}
		if (firDepPerformanceCost == null) {
			decryptedDepartmentCostData.put("firDepPerformanceCost", 0.0);
		} else {
			decryptedDepartmentCostData.put("firDepPerformanceCost",
					Double.valueOf(AesUtils.decrypt(firDepPerformanceCost)));
		}
		if (firDepSocialSecurityCost == null) {
			decryptedDepartmentCostData.put("firDepSocialSecurityCost", 0.0);
		} else {
			decryptedDepartmentCostData.put("firDepSocialSecurityCost",
					Double.valueOf(AesUtils.decrypt(firDepSocialSecurityCost)));
		}
		if (firDepAccumulationFundCost == null) {
			decryptedDepartmentCostData.put("firDepAccumulationFundCost", 0.0);
		} else {
			decryptedDepartmentCostData.put("firDepAccumulationFundCost",
					Double.valueOf(AesUtils.decrypt(firDepAccumulationFundCost)));
		}
		return decryptedDepartmentCostData;
	}

	/**
	 * Description: 员工薪酬中薪酬管理每个员工薪资数据
	 *
	 * @return
	 * @Author HouHuiRong
	 * @Create Date: 2019年1月10日 下午10:30:40
	 */
	public RestResponse findEmpAllSalaryDetail(Integer offerId, Integer employeeId) {
		logger.info("findEmpAllSalaryDetail方法开始执行,参数1 offerId:" + offerId + "参数2 employeeId:" + employeeId);
		Map<String, Object> salaryDetailMap = new HashMap<String, Object>();
		try {
			ErpTalkSalary erpTalkSalary = this.erpTalkSalaryMapper.findOneByOfferId(offerId);
			if (null == erpTalkSalary) {// 参考招聘谈薪
				logger.info("通过offerId:" + offerId + "查询招聘谈薪返回结果为空");
				salaryDetailMap.put("erpTalkSalary", null);
			} else {
				erpTalkSalary.setAccumulationFundBase(AesUtils.decrypt(erpTalkSalary.getAccumulationFundBase()));
				erpTalkSalary.setMonthIncome(AesUtils.decrypt(erpTalkSalary.getMonthIncome()));
				erpTalkSalary.setSocialSecurityBase(AesUtils.decrypt(erpTalkSalary.getSocialSecurityBase()));
				erpTalkSalary.setBaseWage(AesUtils.decrypt(erpTalkSalary.getBaseWage()));
				erpTalkSalary.setMonthAllowance(AesUtils.decrypt(erpTalkSalary.getMonthAllowance()));
				salaryDetailMap.put("erpTalkSalary", erpTalkSalary);
			}
			ErpBasePayroll erpBasePayroll = this.erpBasePayrollMapper.findBasePayrollDetailByEmpId(employeeId);
			if (null == erpBasePayroll) {// 基本薪资,社保和公积金
				logger.info("通过employeeId:" + employeeId + "查询基本薪资返回结果为空");
				salaryDetailMap.put("erpBasePayroll", null);
			} else {
				erpBasePayroll.setErpBaseWage(AesUtils.decrypt(erpBasePayroll.getErpBaseWage()));
				erpBasePayroll.setErpPostWage(AesUtils.decrypt(erpBasePayroll.getErpPostWage()));
				erpBasePayroll.setErpPerformance(AesUtils.decrypt(erpBasePayroll.getErpPerformance()));
				erpBasePayroll.setErpAllowance(AesUtils.decrypt(erpBasePayroll.getErpAllowance()));
				erpBasePayroll.setErpSocialSecurityBase(AesUtils.decrypt(erpBasePayroll.getErpSocialSecurityBase()));
				erpBasePayroll
						.setErpAccumulationFundBase(AesUtils.decrypt(erpBasePayroll.getErpAccumulationFundBase()));
				erpBasePayroll.setErpTelFarePerquisite(AesUtils.decrypt(erpBasePayroll.getErpTelFarePerquisite()));
				salaryDetailMap.put("erpBasePayroll", erpBasePayroll);
			}
			ErpTraineeSalary erpTraineeSalary = this.erpTraineeSalaryMapper.selectOneTraineeSalary(employeeId);
			if (null == erpTraineeSalary) {// 实习生薪资
				logger.info("通过employeeId:" + employeeId + "查询实习生薪资返回结果为空");
				salaryDetailMap.put("erpTraineeSalary", null);
			} else {
				erpTraineeSalary.setBaseWage(AesUtils.decrypt(erpTraineeSalary.getBaseWage()));
				erpTraineeSalary.setMonthAllowance(AesUtils.decrypt(erpTraineeSalary.getMonthAllowance()));
				salaryDetailMap.put("erpTraineeSalary", erpTraineeSalary);
			}
			ErpPeriodPayroll erpPeriodPayroll = this.erpPeriodPayrollMapper.findPeriodSalary(employeeId);
			if (null == erpPeriodPayroll) {// 试用期薪资
				logger.info("通过employeeId:" + employeeId + "查询试用期薪资返回结果为空");
				salaryDetailMap.put("erpPeriodPayroll", null);
			} else {
				erpPeriodPayroll.setErpPeriodBaseWage(AesUtils.decrypt(erpPeriodPayroll.getErpPeriodBaseWage()));
				erpPeriodPayroll.setErpPeriodPostWage(AesUtils.decrypt(erpPeriodPayroll.getErpPeriodPostWage()));
				erpPeriodPayroll.setErpPeriodPerformance(AesUtils.decrypt(erpPeriodPayroll.getErpPeriodPerformance()));
				erpPeriodPayroll.setErpPeriodIncome(AesUtils.decrypt(erpPeriodPayroll.getErpPeriodIncome()));
				erpPeriodPayroll.setErpPeriodAllowance(AesUtils.decrypt(erpPeriodPayroll.getErpPeriodAllowance()));
				erpPeriodPayroll.setErpTelFarePerquisite(AesUtils.decrypt(erpPeriodPayroll.getErpTelFarePerquisite()));
				salaryDetailMap.put("erpPeriodPayroll", erpPeriodPayroll);
			}
			ErpPositiveSalary erpPositiveSalary = this.erpPositiveSalaryMapper.findPositiveSalaryByEmpId(employeeId);
			if (null == erpPositiveSalary) {// 转正薪资
				logger.info("通过employeeId:" + employeeId + "查询上岗工资单转正薪资返回结果为空");
				salaryDetailMap.put("erpPositiveSalary", null);
			} else {
				erpPositiveSalary.setErpPositiveBaseWage(AesUtils.decrypt(erpPositiveSalary.getErpPositiveBaseWage()));
				erpPositiveSalary.setErpPositivePostWage(AesUtils.decrypt(erpPositiveSalary.getErpPositivePostWage()));
				erpPositiveSalary
						.setErpPositivePerformance(AesUtils.decrypt(erpPositiveSalary.getErpPositivePerformance()));
				erpPositiveSalary
						.setErpPositiveAllowance(AesUtils.decrypt(erpPositiveSalary.getErpPositiveAllowance()));
				erpPositiveSalary.setErpPositiveIncome(AesUtils.decrypt(erpPositiveSalary.getErpPositiveIncome()));
				erpPositiveSalary
						.setErpTelFarePerquisite(AesUtils.decrypt(erpPositiveSalary.getErpTelFarePerquisite()));
				salaryDetailMap.put("erpPositiveSalary", erpPositiveSalary);
			}
			ErpPositivePayroll erpPositivePayroll = this.erpPositivePayrollMapper.selectOnePositivePayroll(employeeId);
			if (null == erpPositivePayroll) {// 转正工资单
				logger.info("通过employeeId:" + employeeId + "查询转正工资单返回结果为空");
				salaryDetailMap.put("erpPositivePayroll", null);
			} else {
				erpPositivePayroll
						.setErpPositiveBaseWage(AesUtils.decrypt(erpPositivePayroll.getErpPositiveBaseWage()));
				erpPositivePayroll
						.setErpPositivePostWage(AesUtils.decrypt(erpPositivePayroll.getErpPositivePostWage()));
				erpPositivePayroll
						.setErpPositivePerformance(AesUtils.decrypt(erpPositivePayroll.getErpPositivePerformance()));
				erpPositivePayroll
						.setErpPositiveAllowance(AesUtils.decrypt(erpPositivePayroll.getErpPositiveAllowance()));
				erpPositivePayroll.setErpPositiveIncome(AesUtils.decrypt(erpPositivePayroll.getErpPositiveIncome()));
				erpPositivePayroll
						.setErpTelFarePerquisite(AesUtils.decrypt(erpPositivePayroll.getErpTelFarePerquisite()));
				salaryDetailMap.put("erpPositivePayroll", erpPositivePayroll);
			}
			List<Map<String, Object>> salaryRecordList = this.erpSalaryAdjustMapper.selectSalAdjRecByEId(employeeId);
			if (salaryRecordList.size() == 0 || salaryRecordList == null) {// 薪资调整
				logger.info("通过employeeId:" + employeeId + "查询薪资调整记录返回结果为空");
				salaryDetailMap.put("salaryRecordList", null);
			} else {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> map : salaryRecordList) {
					Map<String, Object> tempMap = new HashMap<String, Object>();
					tempMap.put("former_base_wage", AesUtils.decrypt(String.valueOf(map.get("former_base_wage"))));
					tempMap.put("former_post_wage", AesUtils.decrypt(String.valueOf(map.get("former_post_wage"))));
					tempMap.put("former_performance", AesUtils.decrypt(String.valueOf(map.get("former_performance"))));
					tempMap.put("former_allowance", AesUtils.decrypt(String.valueOf(map.get("former_allowance"))));
					tempMap.put("former_tel_fare_perquisite",
							AesUtils.decrypt(String.valueOf(map.get("former_tel_fare_perquisite"))));
					tempMap.put("adjust_base_wage", AesUtils.decrypt(String.valueOf(map.get("adjust_base_wage"))));
					tempMap.put("adjust_post_wage", AesUtils.decrypt(String.valueOf(map.get("adjust_post_wage"))));
					tempMap.put("adjust_performance", AesUtils.decrypt(String.valueOf(map.get("adjust_performance"))));
					tempMap.put("adjust_allowance", AesUtils.decrypt(String.valueOf(map.get("adjust_allowance"))));
					tempMap.put("adjust_tel_fare_perquisite",
							AesUtils.decrypt(String.valueOf(map.get("adjust_tel_fare_perquisite"))));
					tempMap.put("adjust_time", String.valueOf(map.get("adjust_time")));
					list.add(tempMap);
				}
				salaryDetailMap.put("salaryRecordList", list);
			}
		} catch (Exception e) {
			logger.error("findEmpAllSalaryDetail方法出现异常:" + e.getMessage(), e);
		}
		return RestUtils.returnSuccess(salaryDetailMap);
	}

	/**
	 * Description: 员工薪酬>薪酬管理>薪酬分析：导出薪酬明细和汇总各类形式数据
	 *
	 * @return 所有一级部门下员工的各类形式的薪酬数据
	 * @Author HouHuiRong
	 * @Create Date: 2020年2月18日 下午10:30:40
	 */
	
	
	@SuppressWarnings({ "unchecked", "null" })
	public List<List<String>> findEmployeeSalarySummary(String startTime,
			String endTime,List<Integer> firDepartmentIdList,String token) {
		logger.info("findEmployeeSalarySummary方法开始执行,参数：startTime："
			+startTime+"endTime:"+endTime+"firDepartmentIdList:"+firDepartmentIdList);
		List<List<List<Map<String, Object>>>> findFirDepAllEmpList=null;//查询时间段内所有一级部门员工薪酬明细
		List<List<String>> allEmpList=new ArrayList<>();//查询时间段内所有一级部门员工的薪酬明细最后返回结果
		try{
			/*
			 * 跨工程调用一级部门下所有员工信息
			 */
			findFirDepAllEmpList=findFirDepAllEmp(firDepartmentIdList,token);
			if(findFirDepAllEmpList==null||findFirDepAllEmpList.size()==0){
				return allEmpList;
			}
			/*
			 * 将查询时间段填充完整
			 */	
			String[] strs=endTime.split("-");
			int year=Integer.valueOf(strs[0]);
			int month=Integer.valueOf(strs[1]);
			String endTimeNew="";
			if(month==12){
				endTimeNew=year+1+"-01";
			}else if(month<12){
				if(month<10){
					endTimeNew=year+"-0"+(month+1);
				}else{
					endTimeNew=year+"-"+(month+1);
				}					
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		    String nowTime=format.format(new Date());//获取当前系统时间
			Date date1 = format.parse(startTime);
			Date date2 = format.parse(endTimeNew);
			Calendar cal =Calendar.getInstance();
			cal.setTime(date1);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			while(cal.getTime().before(date2)){
				String queryTime = format.format(cal.getTime());//查询时间			
				cal.add(Calendar.MONTH,1);
				cal.set(Calendar.DAY_OF_MONTH,day);
				
				String startMonth=queryTime+"-31";//查询月月底
				String endMonth=queryTime+"-01";//查询月月初
				/*
				 * 返回每个查询月所有一级部门员工的薪酬明细/汇总
				 * */				
				List<List<String>> data1=new ArrayList<>();//每个查询月所有一级部门员工的薪酬明细
				List<List<String>> data2=new ArrayList<>();//每个查询所有一级部门各类薪资总和
				if(queryTime.equals(nowTime)){
					//系统当月的一级部门月度绩效状态
					List<String> statusList=erpMonthPerformanceMapper.findFirstDepartmentMonthPerStatusList(firDepartmentIdList,nowTime,nowTime);
					//如果月度绩效状态未归档,则返回空sheet
					if(statusList.size()==0||statusList.size()>1||(statusList.size()==1&&!"4".equals(statusList.get(0)))){
						break;
					}
				}
				/*
				 * 所有一级部门的费用、信息统计
				 */
				Double allFirDepWageCost = 0.0;// 工资费用
				Double allFirDepSubsidyCost = 0.0;// 补助费用
				Double allFirDepPerformanceCost = 0.0;// 绩效费用
				Double allFirDepSocialSecurityCost = 0.0;// 社保费用
				Double allFirDepAccumulationFundCost = 0.0;// 公积金费用
				for (List<List<Map<String, Object>>> secDepEmpInfoList : findFirDepAllEmpList) {
					logger.info("一级部门下的二级部门总数：" + secDepEmpInfoList.size());
					/*
					 * 一级部门的费用、信息统计
					 */
					Double firDepWageCost = 0.0;// 工资费用
					Double firDepSubsidyCost = 0.0;// 补助费用
					Double firDepPerformanceCost = 0.0;// 绩效费用
					Double firDepSocialSecurityCost = 0.0;// 社保费用
					Double firDepAccumulationFundCost = 0.0;// 公积金费用
					
					// 一级部门下所有二级部门员工的信息和薪酬情况
					for (List<Map<String, Object>> empInfoList : secDepEmpInfoList) {
						logger.info("二级部门下的员工总数：" + empInfoList.size());
						/*
						 * 二级部门的费用、信息统计
						 */
						Double secDepWageCost = 0.0;// 工资费用
						Double secDepSubsidyCost = 0.0;// 补助费用
						Double secDepPerformanceCost = 0.0;// 绩效费用
						Double secDepSocialSecurityCost = 0.0;// 社保费用
						Double secDepAccumulationFundCost = 0.0;// 公积金费用						
	
						// 一个二级部门下所有员工的信息和薪酬情况						
						for (Map<String, Object> empInfo : empInfoList) {
							/*
							 * 获取员工入职时间和离职时间
							 */	
							Integer erpEmployeeId = Integer.valueOf(String.valueOf(empInfo.get("employeeId")));// 员工Id
							String entryTime=String.valueOf(empInfo.get("entryTime"));
							String probationEndTime=String.valueOf(empInfo.get("probationEndTime"));
							String dimissionTime=String.valueOf(empInfo.get("dimissionTime"));	
							String status=String.valueOf(empInfo.get("status"));
							int entryFlag = entryTime.compareTo(startMonth);
							if(dimissionTime!=null){
								if(dimissionTime.compareTo(endMonth)<0&&"3".equals(status)){//离职中,但离职期已过
									dimissionTime=endMonth;
								}
							}
							if("null".equals(dimissionTime)&&!"4".equals(status)){//未离职的
								dimissionTime=endMonth;
							}

							if("null".equals(dimissionTime)){//status=4,已离职但离职时间为null，认定为二次入职,且还未入职
								continue;
							}

							int dimissionFlag=dimissionTime.compareTo(endMonth);
							if(entryFlag<=0&&dimissionFlag>=0){

								List<Double> monthCostList=findEmpMonthSalaryByTime(erpEmployeeId,queryTime,entryTime,probationEndTime,status);
								if(monthCostList.size()==0){
									continue;
								}
								Double empWageCost = monthCostList.get(0);// 工资费用
								Double empSocialSecurityCost = monthCostList.get(1);// 社保费用
								Double empPerformanceCost = monthCostList.get(2);// 绩效费用
								Double empAccumulationFundCost = monthCostList.get(3);// 公积金费用
								Double empSubsidyCost = monthCostList.get(4);// 补助费用

								List<String> rowData = new ArrayList<String>();
								rowData.add(queryTime);
								rowData.add(objectToString(empInfo.get("firstDepartmentName")));
								rowData.add(objectToString(empInfo.get("secondDepartmentName")));
								//rowData.add(objectToString(empInfo.get("employeeId")));
								rowData.add(objectToString(empInfo.get("name")));
								rowData.add(objectToString(empInfo.get("idCardNumber")));
								rowData.add(objectToString(empInfo.get("entryTime")));
								rowData.add(objectToString(empInfo.get("dimissionTime")));
								rowData.add(new DecimalFormat("0.00").format(empWageCost));
								rowData.add(new DecimalFormat("0.00").format(empSocialSecurityCost));
								rowData.add(new DecimalFormat("0.00").format(empPerformanceCost));
								rowData.add(new DecimalFormat("0.00").format(empAccumulationFundCost));
								rowData.add(new DecimalFormat("0.00").format(empSubsidyCost));
								data1.add(rowData);
								/*
								 * 将二级部门下所有员工的五险一金（公司缴纳部分）合计累加求和，作为二级部门的社保合计
								 */
								secDepWageCost += empWageCost;// 工资费用
								secDepSocialSecurityCost += empSocialSecurityCost;// 社保费用
								secDepPerformanceCost += empPerformanceCost;// 绩效费用
								secDepAccumulationFundCost+=empAccumulationFundCost;//公积金费用
								secDepSubsidyCost += empSubsidyCost;// 补助费用								
							}
						}
						/*
						 * 一级部门的费用、信息统计
						 */
						firDepWageCost += secDepWageCost;// 工资费用
						firDepSocialSecurityCost += secDepSocialSecurityCost;// 社保费用
						firDepPerformanceCost += secDepPerformanceCost;// 绩效费用
						firDepAccumulationFundCost += secDepAccumulationFundCost;// 公积金费用
						firDepSubsidyCost += secDepSubsidyCost;// 补助费用	
					}
					allFirDepWageCost += firDepWageCost;// 工资费用					
					allFirDepSocialSecurityCost +=firDepSocialSecurityCost;// 社保费用					
					allFirDepPerformanceCost += firDepPerformanceCost;// 绩效费用
					allFirDepAccumulationFundCost += firDepAccumulationFundCost;// 公积金费用
					allFirDepSubsidyCost += firDepSubsidyCost;// 补助费用							
				}
				List<String> rowData2 = new ArrayList<String>();
				rowData2.add(objectToString(data1.size()));
				rowData2.add(new DecimalFormat("0.00").format(allFirDepWageCost));
				rowData2.add(new DecimalFormat("0.00").format(allFirDepSocialSecurityCost));
				rowData2.add(new DecimalFormat("0.00").format(allFirDepPerformanceCost));
				rowData2.add(new DecimalFormat("0.00").format(allFirDepAccumulationFundCost));
				rowData2.add(new DecimalFormat("0.00").format(allFirDepSubsidyCost));				
				data2.add(rowData2);		
				allEmpList.addAll(data1);//所有一级部门员工明细数据
			}
		}catch(Exception e){
			logger.error("findEmployeeSalarySummary方法出现异常:"+e.getMessage());
		}
		return allEmpList;
	}
	
	/**
	 * Description: 根据月份计算员工社保费用和公积金费用
	 *
	 * @return 社保费用、公积金费用
	 * @param erpSocialSecurityBase:社保基数，erpAccumulationFundBase:公积金基数
	 * @Author houhuirong
	 * @Create Date: 2020年2月18日 上午10:54:59
	 */
	public List<Double> findEmpSocialSecurityByTime(Double erpSocialSecurityBase,Double erpAccumulationFundBase,String queryTime) {
		logger.info("findSocialSecurityByTime方法开始执行，传递参数：无参数");
		Double secDepSocialSecurityCost=0.0;
		Double secDepAccumulationFundCost=0.0;
		List<Double> resultList=new ArrayList<>();
		try {
			/*
			 * 查询社保缴纳比例、社保缴纳基数上下限
			 */
			ErpSocialSecurity lastSocialSecurity= erpSocialSecurityMapper.selectSocialSecurityByTime(queryTime);				
			Double endowmentInsuranceCompanyRatio = lastSocialSecurity.getEndowmentInsuranceCompanyRatio() / 100;// 养老保险-公司缴纳比例
			Double endowmentInsuranceBaseUpper = lastSocialSecurity.getEndowmentInsuranceBaseUpper();// 养老保险-缴纳基数上限
			Double endowmentInsuranceBaseLower = lastSocialSecurity.getEndowmentInsuranceBaseLower();// 养老保险-缴纳基数下限
			Double unemploymentInsuranceCompanyRatio = lastSocialSecurity.getUnemploymentInsuranceCompanyRatio() / 100;// 失业保险-公司缴纳比例
			Double unemploymentInsuranceBaseUpper = lastSocialSecurity.getUnemploymentInsuranceBaseUpper();// 失业保险-缴纳基数上限
			Double unemploymentInsuranceBaseLower = lastSocialSecurity.getUnemploymentInsuranceBaseLower();// 失业保险-缴纳基数下限
			Double maternityInsuranceCompanyRatio = lastSocialSecurity.getMaternityInsuranceCompanyRatio() / 100;// 生育保险-公司缴纳比例
			Double maternityInsuranceBaseUpper = lastSocialSecurity.getMaternityInsuranceBaseUpper();// 生育保险-缴纳基数上限
			Double maternityInsuranceBaseLower = lastSocialSecurity.getMaternityInsuranceBaseLower();// 生育保险-缴纳基数下限
			Double medicalInsuranceCompanyRatio = lastSocialSecurity.getMedicalInsuranceCompanyRatio() / 100;// 医疗保险-公司缴纳比例
			Double medicalInsuranceBaseUpper = lastSocialSecurity.getMedicalInsuranceBaseUpper();// 医疗保险-缴纳基数上限
			Double medicalInsuranceBaseLower = lastSocialSecurity.getMedicalInsuranceBaseLower();// 医疗保险-缴纳基数下限
			Double injuryInsuranceCompanyRatio = lastSocialSecurity.getInjuryInsuranceCompanyRatio() / 100;// 工伤保险-公司缴纳比例
			Double injuryInsuranceBaseUpper = lastSocialSecurity.getInjuryInsuranceBaseUpper();// 工伤保险-缴纳基数上限
			Double injuryInsuranceBaseLower = lastSocialSecurity.getInjuryInsuranceBaseLower();// 工伤保险-缴纳基数下限
			Double accumulationFundCompanyRatio = lastSocialSecurity.getAccumulationFundCompanyRatio() / 100;// 公积金-公司缴纳比例
			Double accumulationFundBaseUpper = lastSocialSecurity.getAccumulationFundBaseUpper();// 公积金-缴纳基数上限
			Double accumulationFundBaseLower = lastSocialSecurity.getAccumulationFundBaseLower();// 公积金-缴纳基数下限
			
			/*
			 * 一个员工的五险一金基数
			 */
			Double endowmentInsuranceBase = 0.0;// 养老保险基数
			if (erpSocialSecurityBase < endowmentInsuranceBaseLower) {
				endowmentInsuranceBase = endowmentInsuranceBaseLower;
			} else if (erpSocialSecurityBase > endowmentInsuranceBaseUpper) {
				endowmentInsuranceBase = endowmentInsuranceBaseUpper;
			} else {
				endowmentInsuranceBase = erpSocialSecurityBase;
			}
			Double unemploymentInsuranceBase = 0.0;// 失业保险基数
			if (erpSocialSecurityBase < unemploymentInsuranceBaseLower) {
				unemploymentInsuranceBase = unemploymentInsuranceBaseLower;
			} else if (erpSocialSecurityBase > unemploymentInsuranceBaseUpper) {
				unemploymentInsuranceBase = unemploymentInsuranceBaseUpper;
			} else {
				unemploymentInsuranceBase = erpSocialSecurityBase;
			}
			Double maternityInsuranceBase = 0.0;// 生育保险基数
			if (erpSocialSecurityBase < maternityInsuranceBaseLower) {
				maternityInsuranceBase = maternityInsuranceBaseLower;
			} else if (erpSocialSecurityBase > maternityInsuranceBaseUpper) {
				maternityInsuranceBase = maternityInsuranceBaseUpper;
			} else {
				maternityInsuranceBase = erpSocialSecurityBase;
			}
			Double medicalInsuranceBase = 0.0;// 医疗保险基数
			if (erpSocialSecurityBase < medicalInsuranceBaseLower) {
				medicalInsuranceBase = medicalInsuranceBaseLower;
			} else if (erpSocialSecurityBase > medicalInsuranceBaseUpper) {
				medicalInsuranceBase = medicalInsuranceBaseUpper;
			} else {
				medicalInsuranceBase = erpSocialSecurityBase;
			}
			Double injuryInsuranceBase = 0.0;// 工伤保险基数
			if (erpSocialSecurityBase < injuryInsuranceBaseLower) {
				injuryInsuranceBase = injuryInsuranceBaseLower;
			} else if (erpSocialSecurityBase > injuryInsuranceBaseUpper) {
				injuryInsuranceBase = injuryInsuranceBaseUpper;
			} else {
				injuryInsuranceBase = erpSocialSecurityBase;
			}
			Double accumulationFundBase = 0.0;// 住房公积金基数
			if (erpAccumulationFundBase < accumulationFundBaseLower) {
				accumulationFundBase = accumulationFundBaseLower;
			} else if (erpAccumulationFundBase > accumulationFundBaseUpper) {
				accumulationFundBase = accumulationFundBaseUpper;
			} else {
				accumulationFundBase = erpAccumulationFundBase;
			}
			/*
			 * 员工的社保和公积金费用
			 */
			secDepSocialSecurityCost = endowmentInsuranceBase * endowmentInsuranceCompanyRatio
					+ unemploymentInsuranceBase * unemploymentInsuranceCompanyRatio
					+ maternityInsuranceBase * maternityInsuranceCompanyRatio
					+ medicalInsuranceBase * medicalInsuranceCompanyRatio
					+ injuryInsuranceBase * injuryInsuranceCompanyRatio;// 社保费用
			secDepAccumulationFundCost = accumulationFundBase * accumulationFundCompanyRatio;// 公积金费用
			resultList.add(secDepSocialSecurityCost);
			resultList.add(secDepAccumulationFundCost);
		} catch (Exception e) {
			logger.error("findSocialSecurityByTime方法出现异常：" + e.getMessage(), e);
		}
		return resultList;
	}
	
	/**
	 * Description: 根据查询月份计算一个员工工资、绩效和补助
	 *
	 * @return
	 * @Author houhuirong
	 * @Create Date: 2020年2月18日 上午10:54:59
	 */
	public List<Double> findEmpMonthSalaryByTime(Integer erpEmployeeId,String queryTime,String entryTime,String probationEndTime,String status) {
		logger.info("findEmpMonthSalaryByTime方法开始执行，传递参数：参数erpEmployeeId"+erpEmployeeId+"参数queryTime:"+queryTime);
		List<Double> returnResultList=new ArrayList<>();
		
		/*
		 * 二级部门的费用、信息统计
		 */
		Double secDepWageCost = 0.0;// 工资费用
		Double secDepSocialSecurityCost = 0.0;// 社保费用
		Double secDepPerformanceCost = 0.0;// 绩效费用
		Double secDepAccumulationFundCost = 0.0;// 公积金费用
		Double secDepSubsidyCost = 0.0;// 补助费用

		try {
			/*
			 * 根据员工ID查询其月度绩效记录，解密月度绩效表数据
			 */
			Map<String, Object> param = new HashMap<>();
			param.put("erpMonthEmpId", erpEmployeeId);
			param.put("erpMonthNum", queryTime);
			ErpSalaryMonthPerformance erpSalaryMonthPerformance = erpMonthPerformanceMapper
					.findEmpMonthPerformanceDetail(param);
			if (null != erpSalaryMonthPerformance) {
				String erpBaseWage=erpSalaryMonthPerformance.getErpMonthBaseWage();
				String erpPostWage=erpSalaryMonthPerformance.getErpMonthPostWage();
				Map<String, String> encryptedPerformanceData = new HashMap<>();
				if(erpBaseWage==null||erpPostWage==null){
					/*
					 * 根据员工ID查询当前月基本工资和岗位工资
					 */
					ErpBasePayroll erpBasePayroll = erpBasePayrollMapper
							.findBasePayrollDetailByEmpId(erpEmployeeId);
					if(erpBasePayroll!=null){
						String erpBaseWageNew=erpBasePayroll.getErpBaseWage();
						String erpPostWageNew=erpBasePayroll.getErpPostWage();
						encryptedPerformanceData.put("erpMonthBaseWage",erpBaseWageNew);
						encryptedPerformanceData.put("erpMonthPostWage",erpPostWageNew);
						}
				}else{
					encryptedPerformanceData.put("erpMonthBaseWage",erpBaseWage);
					encryptedPerformanceData.put("erpMonthPostWage",erpPostWage);
				}
				encryptedPerformanceData.put("erpMonthMeritPay",erpSalaryMonthPerformance.getErpMonthMeritPay());//工资绩效
				encryptedPerformanceData.put("erpMonthProjectPay",erpSalaryMonthPerformance.getErpMonthProjectPay());//项目绩效
				encryptedPerformanceData.put("erpMonthMealSubsidy", erpSalaryMonthPerformance.getErpMonthMealSubsidy());//餐交补
				encryptedPerformanceData.put("erpMonthTelSubsidy", erpSalaryMonthPerformance.getErpMonthTelSubsidy());//话费补
				// 解密月度 绩效数据
				Map<String, Double> decryptedPerformanceData = erpMonthPerformanceService
						.decryptPerformanceDataAes(encryptedPerformanceData);
				Double erpMonthBaseWage=decryptedPerformanceData.get("erpMonthBaseWage");//基本工资
				Double erpMonthPostWage=decryptedPerformanceData.get("erpMonthPostWage");//岗位工资
				Double erpMonthMeritPay = decryptedPerformanceData.get("erpMonthMeritPay");// 工资绩效
				Double erpMonthProjectPay = decryptedPerformanceData.get("erpMonthProjectPay");// 项目绩效
				Double erpMonthMealSubsidy=decryptedPerformanceData.get("erpMonthMealSubsidy");//餐交补
				Double erpMonthTelSubsidy=decryptedPerformanceData.get("erpMonthTelSubsidy");//话费补助
				/*
				 * 员工的工资、绩效和补助
				 */
				if("0".equals(status)){
					secDepWageCost=erpMonthBaseWage;//实习生工资构成
				}else{
					secDepWageCost = erpMonthBaseWage + erpMonthPostWage;// 非实习生工资费用
				}
				secDepPerformanceCost = erpMonthMeritPay + erpMonthProjectPay;// 绩效费用
				secDepSubsidyCost = erpMonthTelSubsidy+erpMonthMealSubsidy;// 补助费用
			}
			secDepWageCost = (double) Math.round(secDepWageCost * 100) / 100;	
			secDepPerformanceCost = (double) Math.round(secDepPerformanceCost * 100) / 100;			
			secDepSubsidyCost = (double) Math.round(secDepSubsidyCost * 100) / 100;			
			
			/*
			 * 根据员工ID和查询月份，查询其社保和公积金费用
			 */
			if(!"0".equals(status)){//实习生不缴纳社保和公积金
				Double erpDepSocialSecurityCost=0.0;//公司社保费用
				Double erpDepAccumulationFundCost=0.0;//公司公积金公积金
				ErpActualInsuranceFundBase erpActualSocFundBase=erpActualInsuranceFundBaseMapper.findActualSocialFundByIdAndMonth(param);
				if(erpActualSocFundBase!=null){
					ErpActualInsuranceFundBase decryptedActualSocFundBase=erpActualFundBaseService.decryptDataRsaObject(erpActualSocFundBase);
					Double endowmentInsurancebase=Double.valueOf(objectToString(decryptedActualSocFundBase.getEndowmentInsuranceBase()));
					Double unemploymentInsurancebase=Double.valueOf(objectToString(decryptedActualSocFundBase.getUnemploymentInsuranceBase()));
					Double maternityInsurancebase=Double.valueOf(objectToString(decryptedActualSocFundBase.getMaternityInsuranceBase()));
					Double medicalInsurancebase=Double.valueOf(objectToString(decryptedActualSocFundBase.getMedicalInsuranceBase()));
					Double injuryInsurancebase=Double.valueOf(objectToString(decryptedActualSocFundBase.getInjuryInsuranceBase()));
					Double accumulationFundBase=Double.valueOf(objectToString(decryptedActualSocFundBase.getAccumulationFundBase()));
					
					/*
					 * 查询社保缴纳比例、社保缴纳基数上下限
					 */
					ErpSocialSecurity lastSocialSecurity= erpSocialSecurityMapper.selectSocialSecurityByTime(queryTime);
					Double endowmentInsuranceCompanyRatio = lastSocialSecurity.getEndowmentInsuranceCompanyRatio() / 100;// 养老保险-公司缴纳比例
					Double unemploymentInsuranceCompanyRatio = lastSocialSecurity.getUnemploymentInsuranceCompanyRatio() / 100;// 失业保险-公司缴纳比例
					Double maternityInsuranceCompanyRatio = lastSocialSecurity.getMaternityInsuranceCompanyRatio() / 100;// 生育保险-公司缴纳比例
					Double medicalInsuranceCompanyRatio = lastSocialSecurity.getMedicalInsuranceCompanyRatio() / 100;// 医疗保险-公司缴纳比例
					Double injuryInsuranceCompanyRatio = lastSocialSecurity.getInjuryInsuranceCompanyRatio() / 100;// 工伤保险-公司缴纳比例
					Double accumulationFundCompanyRatio = lastSocialSecurity.getAccumulationFundCompanyRatio() / 100;// 公积金-公司缴纳比例
					/*员工的五险一金合计
					 * */
					erpDepSocialSecurityCost=endowmentInsurancebase*endowmentInsuranceCompanyRatio+
							unemploymentInsurancebase*unemploymentInsuranceCompanyRatio+
							maternityInsurancebase*maternityInsuranceCompanyRatio+
							medicalInsurancebase*medicalInsuranceCompanyRatio+
							injuryInsurancebase*injuryInsuranceCompanyRatio;//月度社保费用
					erpDepAccumulationFundCost=accumulationFundBase*accumulationFundCompanyRatio;//月度公积金费用
				}else{
					// 根据员工ID查询当前月社保基数和公积金基数
					 
					ErpBasePayroll erpBasePayroll = erpBasePayrollMapper
							.findBasePayrollDetailByEmpId(erpEmployeeId);
					Map<String, Double> decryptedExcelData=new HashMap<>();
					if(erpBasePayroll!=null){
						decryptedExcelData = this.decryptExcelDataAes(erpBasePayroll);
						logger.info("decryptedExcelData=" + decryptedExcelData);
						Double erpSocialSecurityBase = decryptedExcelData.get("erpSocialSecurityBase");// 社保基数
						Double erpAccumulationFundBase = decryptedExcelData.get("erpAccumulationFundBase");// 公积金基数
						List<Double> resultList=findEmpSocialSecurityByTime(erpSocialSecurityBase,erpAccumulationFundBase,queryTime);
						erpDepSocialSecurityCost=resultList.get(0);
						erpDepAccumulationFundCost=resultList.get(1);
					}
				}
				if(!"null".equals(probationEndTime)&&!"".equals(probationEndTime)){
					String[] strs=entryTime.split("-");
					int entryDays=Integer.valueOf(strs[2]);//15号之后入职无法缴纳社保
					entryTime=new SimpleDateFormat("yyyy-MM").format(ExDateUtils.convertToDate(entryTime));//入职月份
					//转正月份
					Date probationEndDate=ExDateUtils.addDays(ExDateUtils.convertToDate(probationEndTime),1);
					String probationEndTimeNew=ExDateUtils.dateToString(probationEndDate, "yyyy-MM");
					//转正次月
					String startFundCostDate=ExDateUtils.dateToString(ExDateUtils.addMonths(probationEndDate, 1),"yyyy-MM");
					
					int entryFlag=entryTime.compareTo(queryTime);//<0查询月之前入职,=0查询本月入职,>0为异常数据
					int positionFlag=probationEndTimeNew.compareTo(queryTime);//<0已转正,=0查询月转正,>0未转正
					int startFundCostFlag=queryTime.compareTo(startFundCostDate);//>0=缴纳公积金,<0不缴纳公积金
					//查询月 等于或者大于 转正次月
					if(startFundCostFlag>=0){
						secDepSocialSecurityCost=erpDepSocialSecurityCost;//社保费用
						secDepAccumulationFundCost=erpDepAccumulationFundCost;//公积金费用
					}else{
						if((entryFlag<0&&positionFlag>=0||(entryFlag==0&&entryDays<=15))){
							// 本月转正也是次月开始缴纳 公积金							 
							secDepSocialSecurityCost=erpDepSocialSecurityCost;//社保费用
						}
					}			
				}else{ //系统上线之前合同表中无老员工数据，默认赋值查询月的 社保和公积金
					secDepSocialSecurityCost=erpDepSocialSecurityCost;//社保费用
					secDepAccumulationFundCost=erpDepAccumulationFundCost;//公积金费用
				}

			}
			secDepSocialSecurityCost = (double) Math.round(secDepSocialSecurityCost * 100) / 100;	
			secDepAccumulationFundCost = (double) Math.round(secDepAccumulationFundCost * 100) / 100;	
			returnResultList.add(secDepWageCost);
			returnResultList.add(secDepSocialSecurityCost);
			returnResultList.add(secDepPerformanceCost);
			returnResultList.add(secDepAccumulationFundCost);
			returnResultList.add(secDepSubsidyCost);
		} catch (Exception e) {
			logger.error("findEmpMonthSalaryByTime方法出现异常：" + e.getMessage(), e);
		}
		return returnResultList;
	}
	
	/**
	 * Description: 根据条件查询一级部门所有员工信息
	 *
	 * @return
	 * @Author houhuirong
	 * @Create Date: 2020年2月18日 上午10:54:59
	 */
	@SuppressWarnings("unchecked")
	public List<List<List<Map<String, Object>>>> findFirDepAllEmp(List<Integer> firDepartmentIdList,String token) {
		logger.info("findFirDepAllEmp方法开始执行，传递参数：firDepartmentIdList"+firDepartmentIdList);
		List<List<List<Map<String, Object>>>> firDepEmpInfoList=new ArrayList<>();		
		try {
			if(firDepartmentIdList.size()>1){
				
				// 调用ERP-人力资源 工程 的操作层服务接口-获取一级部门下面所有二级部门的员工的详细信息
				ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
				Integer id = erpUser.getUserId();//用户Id	
				List<Integer> roles = erpUser.getRoles();//从用户信息中获取角色信息
				String url = protocolType
						+ "nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoOfAllFirDepByParams";
				Map<String, Object> departmentParams = new HashMap<>();
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
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.add("token", token);// 将token放到请求头中
				HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(departmentParams, requestHeaders);
				ResponseEntity<Map> response = this.restTemplate.postForEntity(url, requestEntity, Map.class);
				if (200 != response.getStatusCodeValue()) {
					logger.error("findFirDepAllEmp调用人力资源工程失败！");
					return firDepEmpInfoList;
				}
				// 解析获取的数据
				Map<String, Object> responseBody = response.getBody();
				if (!"200".equals(responseBody.get("status"))) {
					logger.error("findFirDepAllEmp调用人力资源工程失败！");
					return firDepEmpInfoList;
					}
				
				firDepEmpInfoList = (List<List<List<Map<String, Object>>>>) responseBody.get("data");
				logger.info("权限内的一级部门总数：" + firDepEmpInfoList.size());
			}else if(firDepartmentIdList.size()==1){
				// 调用ERP-人力资源 工程 的操作层服务接口-获取一级部门下面所有二级部门的员工的详细信息
				Integer firstDepartmentId=firDepartmentIdList.get(0);
				String url = protocolType
						+ "nantian-erp-hr/nantian-erp/erp/employee/findSecDepEmpInfoByFirDepId?firstDepartmentId="
						+ firstDepartmentId;
				String body = null;
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
				headers.add("token", token);
				HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
				ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
						JSONObject.class);

				if (response.getStatusCodeValue() != 200 || response.getBody() == null) {
					logger.error("findFirDepAllEmp调用人力资源工程失败！");	
					return firDepEmpInfoList;
					}

				// 解析获取的数据
				JSONObject responseBody = response.getBody();
				List<List<Map<String, Object>>> firstEmployeeAndBasePayrollList = (List<List<Map<String, Object>>>) responseBody.get("data");
				logger.info("一级部门下的二级部门总数：" + firstEmployeeAndBasePayrollList.size());
				firDepEmpInfoList.add(firstEmployeeAndBasePayrollList);
			}
		} catch (Exception e) {
			logger.error("findFirDepAllEmp方法出现异常：" + e.getMessage(), e);
		}
		return firDepEmpInfoList;
	}

	@SuppressWarnings("unused")
	public XSSFWorkbook createEmpSummaryExcel(List<List<Map<String, Object>>>  findFirDepAllEmpList) {
		logger.info("createEmpSummaryExcel方法开始执行，传递参数:findFirDepAllEmpList");
		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFCellStyle cellStyle=workBook.createCellStyle();
			XSSFDataFormat format=workBook.createDataFormat();
			cellStyle.setDataFormat(format.getFormat("@"));
			XSSFSheet sheet = workBook.createSheet("员工薪酬-员工薪资汇总信息");
			// 生成表头（第一行）
			int titleNumber = 0;
			XSSFRow firstRow = sheet.createRow(titleNumber);
			firstRow.createCell(titleNumber++).setCellValue("日期范围");
			firstRow.createCell(titleNumber++).setCellValue("一级部门");
			firstRow.createCell(titleNumber++).setCellValue("二级部门");
			firstRow.createCell(titleNumber++).setCellValue("员工编号");
			firstRow.createCell(titleNumber++).setCellValue("员工姓名");
			firstRow.createCell(titleNumber++).setCellValue("身份证号码");
			firstRow.createCell(titleNumber++).setCellValue("人力成本合计");
			firstRow.createCell(titleNumber++).setCellValue("工资");
			firstRow.createCell(titleNumber++).setCellValue("社保");
			firstRow.createCell(titleNumber++).setCellValue("绩效");
			firstRow.createCell(titleNumber++).setCellValue("公积金");
			firstRow.createCell(titleNumber++).setCellValue("补贴");
			/*
			 * 所有一级部门总合计*/
			Double firDepWageCostTotal =0.0; // 工资费用
			Double firDepSocialSecurityCostTotal = 0.0;// 社保费用			
			Double firDepPerformanceCostTotal = 0.0;// 绩效费用			
			Double firDepAccumulationFundCostTotal = 0.0;// 公积金费用
			Double firDepSubsidyCostTotal = 0.0;// 补助费用
			Double firDepManpowerCostTotal2=0.0;;//人力成本费用合计
			Integer firDepEmployeeNumTotal=0;//员工总数
			
			// 下一行
			XSSFRow nextRow = null;			
	
			List<Map<String,Object>> list=findFirDepAllEmpList.get(1);
			for(int i=0;i<list.size();i++){
				Map<String,Object> empSalary=list.get(i);
				nextRow = sheet.createRow(i + 1);
				int valueNumber = 0;
				//日期范围
				nextRow.createCell(valueNumber++).setCellValue(objectToString(empSalary.get("queryTimePeriod")));
				//一级部门
				nextRow.createCell(valueNumber++).setCellValue(objectToString(empSalary.get("firstDepartmentName")));
				//二级部门
				nextRow.createCell(valueNumber++).setCellValue(objectToString(empSalary.get("secondDepartmentName")));
				//员工编号
				nextRow.createCell(valueNumber++).setCellValue(objectToString(empSalary.get("employeeId")));
				//员工姓名
				nextRow.createCell(valueNumber++).setCellValue(objectToString(empSalary.get("name")));
				//身份证号码
				nextRow.createCell(valueNumber++).setCellValue(objectToString(empSalary.get("idCardNumber")));
				/*
				 * 获取每位员工月度累计费用
				 */
				Double empMonthmanpowerCostTotal=Double.valueOf(objectToString(empSalary.get("empMonthmanpowerCostTotal")));
				Double empMonthWageCost=Double.valueOf(objectToString(empSalary.get("empWageCost")));
				Double empMonthSocialSecurityCost=Double.valueOf(objectToString(empSalary.get("empSocialSecurityCost")));
				Double empMonthPerformanceCost=Double.valueOf(objectToString(empSalary.get("empPerformanceCost")));
				Double empMonthAccumulationFundCost=Double.valueOf(objectToString(empSalary.get("empAccumulationFundCost")));
				Double empMonthSubsidyCost=Double.valueOf(objectToString(empSalary.get("empSubsidyCost")));
		
				//人力成本合计
				nextRow.createCell(valueNumber++).setCellValue(new DecimalFormat("0.00").format(empMonthmanpowerCostTotal));
				//工资
				nextRow.createCell(valueNumber++).setCellValue(new DecimalFormat("0.00").format(empMonthWageCost));
				//社保
				nextRow.createCell(valueNumber++).setCellValue(new DecimalFormat("0.00").format(empMonthSocialSecurityCost));
				//绩效
				nextRow.createCell(valueNumber++).setCellValue(new DecimalFormat("0.00").format(empMonthPerformanceCost));
				//公积金
				nextRow.createCell(valueNumber++).setCellValue(new DecimalFormat("0.00").format(empMonthAccumulationFundCost));
				//补助
				nextRow.createCell(valueNumber++).setCellValue(new DecimalFormat("0.00").format(empMonthSubsidyCost));
				/*
				 * 获取部门月度累计费用
				 */
				firDepWageCostTotal += empMonthWageCost;// 工资费用
				firDepSubsidyCostTotal += empMonthSubsidyCost;// 补助费用
				firDepPerformanceCostTotal += empMonthPerformanceCost;// 绩效费用
				firDepSocialSecurityCostTotal += empMonthSocialSecurityCost;// 社保费用
				firDepAccumulationFundCostTotal += empMonthAccumulationFundCost;// 公积金费用
				firDepManpowerCostTotal2+=empMonthmanpowerCostTotal;//人力成本费用合计
			}

		XSSFRow lastRow = null;
		firDepEmployeeNumTotal=list.size();
		lastRow = sheet.createRow(firDepEmployeeNumTotal+4);
		lastRow.createCell(3).setCellValue("人数合计：");
		lastRow.createCell(4).setCellValue(String.valueOf(firDepEmployeeNumTotal));
		lastRow.createCell(5).setCellValue("成本合计：");
		lastRow.createCell(6).setCellValue(new DecimalFormat("0.00").format(firDepManpowerCostTotal2));
		lastRow.createCell(7).setCellValue(new DecimalFormat("0.00").format(firDepWageCostTotal));
		lastRow.createCell(8).setCellValue(new DecimalFormat("0.00").format(firDepSocialSecurityCostTotal));
		lastRow.createCell(9).setCellValue(new DecimalFormat("0.00").format(firDepPerformanceCostTotal));
		lastRow.createCell(10).setCellValue(new DecimalFormat("0.00").format(firDepAccumulationFundCostTotal));
		lastRow.createCell(11).setCellValue(new DecimalFormat("0.00").format(firDepSubsidyCostTotal));
		if(list.size()==0){
		XSSFRow lastRow1 = null;
		lastRow1 = sheet.createRow(8);
		lastRow1.createCell(4).setCellValue("本月度绩效数据未归档,存在无效数据,无法导出！");
		}
		} catch (Exception e) {	
			logger.info("createEmpSummaryExcel方法出现异常：" + e.getMessage());
		}
		return workBook;
	}

	@SuppressWarnings({ "unchecked", "null" })
	public RestResponse exportEmpSalarySummary(String startTime,
			String endTime, List<Integer> firDepartmentIdList, String token,String flag,HttpServletResponse response) {
	
		logger.info("exportEmpSalarySummary方法开始执行,参数：startTime："
				+startTime+"endTime:"+endTime+"firstDepartmentId:"+firDepartmentIdList);
		HttpServletResponse httpResponse = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();

		try {
			if(startTime==null||startTime.equals("")||endTime==null||endTime.equals("")){
				httpResponse.addHeader("status", "0");
				httpResponse.addHeader("statusName", stringToAscii("请输入查询起止时间！"));
				return RestUtils.returnSuccess("请输入查询起止时间！");
			}else{

				String workStartTime="2020-02";//2020-01
				String endTimeBefore="";
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");//设置日期格式  
			    String nowTime=df.format(new Date());//获取当前系统时间 			    
			    int queryflag=endTime.compareTo(startTime);//查询起止间隔是否大于1个月，大于0为true
			    int startFlag = startTime.compareTo(workStartTime);	//查询开始时间不得早于该功能上线时间,小于0为true
				int endFlag=endTime.compareTo(nowTime);//查询结束时间不得晚于系统时间,大于0为true
		
				if(startFlag<0||endFlag>0){
					httpResponse.addHeader("status", "0");
					httpResponse.addHeader("statusName", stringToAscii("查询开始时间超出本功能上线时间:2020-02"));
					return RestUtils.returnSuccess("查询开始时间超出本功能上线时间:"+workStartTime);
				}
				/*
				 * 对于查询间隔超出1个月,不考虑当月月度绩效状态
				 */
				if(endTime.equals(nowTime)&&queryflag>0){
					Calendar c = Calendar.getInstance();
					c.setTime(df.parse(endTime));
					c.add(Calendar.MONTH, -1);
					Date m = c.getTime();
					// 前一个月的日期
					endTimeBefore= df.format(m);
				}else{
					endTimeBefore=endTime;
				}
				//查询一级部门月度绩效状态
				List<String> statusList=erpMonthPerformanceMapper.findFirstDepartmentMonthPerStatusList(firDepartmentIdList,startTime,endTimeBefore);
				if(statusList!=null&&statusList.size()!=0){
					if(statusList.size()>1){
						httpResponse.addHeader("status", "0");
						httpResponse.addHeader("statusName", stringToAscii("查询月度存在未归档数据,无法导出！"));
						return RestUtils.returnSuccess("查询月度存在未归档数据,无法导出！");
					}
					if(!"4".equals(statusList.get(0))){
						httpResponse.addHeader("status", "0");
						httpResponse.addHeader("statusName", stringToAscii("查询月度存在未归档数据,无法导出！"));
						return RestUtils.returnSuccess("查询月度存在未归档数据,无法导出！");
					}else{
						// 查询出需要导出的数据（员工信息+薪酬信息）
						List<List<String>> employeeAndBasePayrollList = this
								.findEmployeeSalarySummary(startTime,endTime,firDepartmentIdList, token);
						if(employeeAndBasePayrollList==null||employeeAndBasePayrollList.size()==0){
							httpResponse.addHeader("status", "0");
							httpResponse.addHeader("statusName", stringToAscii("调用人力资源工程失败!"));
							return RestUtils.returnSuccess("调用人力资源工程失败,请联系管理员!");
						}
						if(flag.contains("details")){
							// 通过数据创建Excel文件
							this.createEmpDetailsExcel(employeeAndBasePayrollList,startTime,endTime,response);				
						}
					}	
				}else{
					httpResponse.addHeader("status", "0");
					httpResponse.addHeader("statusName", stringToAscii("未生成月度绩效数据!"));
					return RestUtils.returnSuccess("未生成月度绩效数据!");
				}		
			}
		} catch (Exception e) {
			logger.error("exportEmpSalarySummary方法出现异常：" + e.getMessage(), e);
			httpResponse.addHeader("status", "0");
			httpResponse.addHeader("statusName", stringToAscii("方法出现异常,导出失败!"));
			return RestUtils.returnFailure("exportEmpSalarySummary方法出现异常：" + e.getMessage());
		}
		return RestUtils.returnSuccess("导出成功!");
	}
	
	/**
	 * 员工导入模板导出
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String createEmpDetailsExcel(List<List<String>>  findFirDepAllEmpList,String startTime,String endTime,HttpServletResponse response) {
		logger.info("createEmpDetailsExcel方法开始执行，传递参数:findFirDepAllEmpList"+findFirDepAllEmpList+"查询开始时间startTime:"+startTime+"截止时间:"+endTime);		

		// 定义工作簿
		XSSFWorkbook workBook = null;
		String str="";
		String fileName="导出薪资明细";
		try {
//			File f= new File("d:" + File.separator+ "test.xls") ;
//			OutputStream out = new FileOutputStream(f);//导出本地
			response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
			response.addHeader("status", "1");
			response.addHeader("statusName", stringToAscii("导出成功!"));
			ServletOutputStream out = response.getOutputStream(); //输出客户端

			workBook = new XSSFWorkbook ();
			String[] headers1 = { "月份","一级部门","二级部门", "员工姓名","员工身份证号","入职时间","离职时间",
					"工资","社保","绩效","公积金","补贴"};
			List<List<String>> data2=null;
			String sheetTitle="";//sheet名称
			if(startTime.equals(endTime)){
				sheetTitle=startTime;
			}else{
				sheetTitle=startTime+"-"+endTime;
			}
			this.exportExcel(workBook, 0, sheetTitle, headers1, findFirDepAllEmpList,data2, out);
			workBook.write(out);
			out.flush();
			out.close();
			str="导出成功!";
		} catch (Exception e) {
			logger.error("createEmpDetailsExcel方法出现异常：",e.getMessage(),e);
			response.addHeader("status", "0");
			response.addHeader("statusName", stringToAscii("导出失败!"));
			str="导出失败";
		}
		return str;
	}
	
	@SuppressWarnings("deprecation")
	public void  exportExcel(XSSFWorkbook  workbook, int sheetNum,
	String sheetTitle, String[] headers, List<List<String>> result,
	List<List<String>> data2,OutputStream out) throws Exception {
		// 生成一个表格
		XSSFSheet  sheet = workbook.createSheet();
		workbook.setSheetName(sheetNum, sheetTitle);
		// 设置表格默认列宽度为20个字节
		sheet.setDefaultColumnWidth((short) 15);
		// 生成一个样式
		XSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
		
		style.setFillBackgroundColor(HSSFColor.WHITE.index);
		style.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);
		
		// 生成一个字体
		XSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		// 把字体应用到当前的样式
		style.setFont(font);
		
		// 指定当单元格内容显示不下时自动换行
		style.setWrapText(true);
		
		// 产生表格标题行
		XSSFRow  row = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
		XSSFCell  cell = row.createCell((short) i);
		
		cell.setCellStyle(style);
		HSSFRichTextString text = new HSSFRichTextString(headers[i]);
		cell.setCellValue(text.toString());
		}
		// 遍历集合数据，产生数据行
		if (result.size()>0) {
		int index = 1;
		for (List<String> m : result) {
		row = sheet.createRow(index);
		int cellIndex = 0;
		for (String str : m) {
		XSSFCell  cell = row.createCell((short) cellIndex);
		cell.setCellValue(str.toString());
		cellIndex++;
		}
		index++;
		}
	}else{
		XSSFRow lastRow1 = null;
		lastRow1 = sheet.createRow(5);
		lastRow1.createCell(4).setCellValue("本月度绩效数据未归档,存在无效数据,无法导出！");
	}
}
	/*
	 * Object的数据转化成String类型
	 * @param clob
	 * @return
	 */
	public static String objectToString(Object object) {
		return object == null ? "" : String.valueOf(object);
	}
	
	public static String stringToAscii(String value)  
	{  
	    StringBuffer sbu = new StringBuffer();  
	    char[] chars = value.toCharArray();   
	    for (int i = 0; i < chars.length; i++) {  
	        if(i != chars.length - 1)  
	        {  
	            sbu.append((int)chars[i]).append(",");  
	        }  
	        else {  
	            sbu.append((int)chars[i]);  
	        }  
	    }  
	    return sbu.toString();  
	}  
}
