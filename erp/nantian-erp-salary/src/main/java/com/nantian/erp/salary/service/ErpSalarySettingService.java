package com.nantian.erp.salary.service;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
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
import com.nantian.erp.salary.data.dao.ErpEmpFinanceNumberMapper;
import com.nantian.erp.salary.data.model.ErpEmpFinanceNumber;
import com.nantian.erp.salary.data.model.ErpEmpFinanceNumberRecord;

/** 
 * Description: 薪酬设置service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年01月23日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
//@PropertySource("classpath:config/host.properties")
@PropertySource(value= {"classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpSalarySettingService {
	
	/*
	 * 从配置文件中获取主机相关属性
	 */
	@Value("${protocol.type}")
    private String protocolType;//http或https
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ErpEmpFinanceNumberMapper empFinanceNumberMapper;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;

	/**
	 * Description: 下载导入模板
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月23日 上午10:23:27
	 */
	public RestResponse downloadEmpFinanceNumber() {
		logger.info("downloadEmpFinanceNumber方法开始执行，无参数");
		
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("员工财务序号");
			// 生成表头（第一行）
			XSSFRow firstRow = sheet.createRow(0);
			//设置身份证号和财务号列为文本格式
			CellStyle style = workBook.createCellStyle();
			XSSFDataFormat format = workBook.createDataFormat();
			style.setDataFormat(format.getFormat("@"));
			sheet.setDefaultColumnStyle(1,style);
			sheet.setDefaultColumnStyle(2,style);
			
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("身份证号");
			firstRow.createCell(2).setCellValue("财务序号");
			//导出文件到客户端
			this.exportExcelToComputer(workBook,"员工薪酬导入模板.xlsx");
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("downloadEmpFinanceNumber方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常导致下载失败！");
		}
	}
	
	/**
	 * Description: 员工财务序号导入
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月23日 上午11:42:56
	 */
	@Transactional
	public RestResponse importEmpFinanceNumber(MultipartFile file,String token) {
		logger.info("importBasePayroll方法开始执行，传递参数：MultipartFile类型文件,token="+token);

		XSSFWorkbook workBook = null;
		try {
			if (file==null) {
		    	return RestUtils.returnSuccessWithString("文件为空！");
			}
			if (!file.getOriginalFilename().endsWith("xlsx")) {
		    	return RestUtils.returnSuccessWithString("请选择合适的文件导入 ！目前仅支持xlsx文件");
			}
			workBook = new XSSFWorkbook(file.getInputStream());
			
			XSSFSheet sheet = workBook.getSheetAt(0);//获取第一个sheet
			int firstRowNum = sheet.getFirstRowNum();//第一行
			int lastRowNum = sheet.getLastRowNum();//最后一行
			int rows = lastRowNum - firstRowNum + 1;//该sheet中的总行数
			
			//定义错误日志记录信息
			List<ErpEmpFinanceNumberRecord> empFinanceNumberRecordList = new ArrayList<>();
			
			//循环总行数  将每一行的数据进行入库操作，从Excel中获取所有信息
			for (int i = 1; i < rows; i++) {
				Map<String,String> excelDataMap = new HashMap<>();//定义Excel的一行数据
				try {
					//获取到sheet中每行的数据
					Row row = sheet.getRow(i);
					
					//将导入的Excel表格数据解析成Map格式的数据
					excelDataMap = this.convertRowToMap(row);
					
					//对导入的Excel进行逐行数据内容、格式校验
					Map<String,String> validateResultMap = this.validateExcelData(excelDataMap);
					String isError = validateResultMap.get("isError");
					if("Y".equals(isError)) {
						ErpEmpFinanceNumberRecord empFinanceNumberRecord = new ErpEmpFinanceNumberRecord();
						empFinanceNumberRecord.setEmpName(validateResultMap.get("name"));
						empFinanceNumberRecord.setEmpIdCardNum(validateResultMap.get("idCardNumber"));
						empFinanceNumberRecord.setErrorContent(validateResultMap.get("errorContent"));
						empFinanceNumberRecord.setErrorTime(ExDateUtils.getCurrentStringDateTime());
						empFinanceNumberRecordList.add(empFinanceNumberRecord);
						continue;
					}
					
					
					//调用HR人力资源工程获取员工ID
					String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmpIdByIdCardNumAndName";
					MultiValueMap<String, String> erpEmployeeParam = new LinkedMultiValueMap<String, String>();
					erpEmployeeParam.add("name", excelDataMap.get("name"));
					erpEmployeeParam.add("idCardNumber", excelDataMap.get("idCardNumber"));
					HttpHeaders requestHeaders=new HttpHeaders();
					requestHeaders.add("token",token);//将token放到请求头中
					
					HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(erpEmployeeParam, requestHeaders); 
					ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
					JSONObject responseBody = response.getBody();
					
					if(response.getStatusCodeValue() != 200 || !"200".equals(responseBody.getString("status")) || responseBody.getInteger("data")==null){
						logger.info("通过姓名和身份证号没有查到用户信息！");
						//通过姓名和身份证号没有查到用户信息时，将错误日志信息记录下来
						ErpEmpFinanceNumberRecord empFinanceNumberRecord = new ErpEmpFinanceNumberRecord();
						empFinanceNumberRecord.setEmpName(excelDataMap.get("name"));
						empFinanceNumberRecord.setEmpIdCardNum(excelDataMap.get("idCardNumber"));
						empFinanceNumberRecord.setErrorContent("通过姓名和身份证号没有查到用户信息！");
						empFinanceNumberRecord.setErrorTime(ExDateUtils.getCurrentStringDateTime());
						empFinanceNumberRecordList.add(empFinanceNumberRecord);
						continue;
					}
					
					Integer employeeId = responseBody.getInteger("data");//解析获取的数据
					
					//组合员工财务序号PO
					ErpEmpFinanceNumber erpEmpFinanceNumber = new ErpEmpFinanceNumber();
					erpEmpFinanceNumber.setEmployeeId(employeeId);
					erpEmpFinanceNumber.setEmpFinanceNumber(excelDataMap.get("empFinanceNumber"));
					
					/*
					 * 如果薪酬数据库中的员工财务序号表中有员工，则更新；如果没有，则新增
					 */
					ErpEmpFinanceNumber validResult = empFinanceNumberMapper.findEmpFinanceNumberDetailByEmpId(employeeId);
					if(validResult==null) {
						empFinanceNumberMapper.insertEmpFinanceNumber(erpEmpFinanceNumber);
					}else {
						empFinanceNumberMapper.updateEmpFinanceNumber(erpEmpFinanceNumber);
					}
					workBook.close();//关闭IO资源
				} catch(Exception e) {
					logger.error("循环获取excel每行表格数据时，发生异常："+e.getMessage(),e);
					ErpEmpFinanceNumberRecord empFinanceNumberRecord = new ErpEmpFinanceNumberRecord();
					empFinanceNumberRecord.setEmpName(excelDataMap.get("name"));
					empFinanceNumberRecord.setEmpIdCardNum(excelDataMap.get("idCardNumber"));
					//从异常信息中获取到250之内的字符，加入到错误日志中
					String message = e.getMessage();
					if(message.length()>250) {
						message = message.substring(0,250);
					}
					empFinanceNumberRecord.setErrorContent("循环获取excel每行表格数据时，发生异常：" + message);
					empFinanceNumberRecord.setErrorTime(ExDateUtils.getCurrentStringDateTime());
					empFinanceNumberRecordList.add(empFinanceNumberRecord);
				}
			}
			
			/*
			 * 将错误日志信息，插入到错误日志记录表中，同时返回给前端显示
			 */
			if(empFinanceNumberRecordList.size()>0) {
//				for (ErpEmpFinanceNumberRecord empFinanceNumberRecord : empFinanceNumberRecordList) {
//					erpBasePayrollRecordMapper.insertBasePayrollRecord(empFinanceNumberRecord);
//				}
				if(empFinanceNumberRecordList.size() == rows-1) {
					return RestUtils.returnSuccess(empFinanceNumberRecordList,"全部失败！");
				}else {
					return RestUtils.returnSuccess(empFinanceNumberRecordList,"部分失败！");
				}
			}
			return RestUtils.returnSuccessWithString("导入财务号成功！");
		} catch (Exception e) {
			logger.error("导入Excel文件时发生异常，导致失败："+e.getMessage(),e);
			return RestUtils.returnFailure("导入Excel文件时发生异常，导致失败！");
		}
	}
	
	/**
	 * Description: 员工财务序号导出
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月24日 下午16:55:48
	 */
	public RestResponse exportEmpFinanceNumber(List<Map<String,Object>> erpEmpFinanceNumberList) {
		logger.info("exportEmpFinanceNumber方法开始执行，传递参数：erpEmpFinanceNumberList="+erpEmpFinanceNumberList);
		XSSFWorkbook workBook = null;// 定义工作簿
		try {
			//当员工财务序号信息列表为空，不允许导出
			if(erpEmpFinanceNumberList.size()==0) {
				return RestUtils.returnSuccessWithString("暂无员工财务序号信息！无法导出！");
			}
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("员工财务序号信息");
			// 生成第一行
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("财务序号");
			firstRow.createCell(1).setCellValue("员工姓名");
			firstRow.createCell(2).setCellValue("职位");
			firstRow.createCell(3).setCellValue("职级");
			firstRow.createCell(4).setCellValue("一级部门");
			firstRow.createCell(5).setCellValue("二级部门");
			firstRow.createCell(6).setCellValue("状态");
			// 下一行
			XSSFRow nextRow = null;
			// 循环 填充表格
			for (int i = 0; i < erpEmpFinanceNumberList.size(); i++) {
				Map<String,Object> erpEmpFinanceNumber = erpEmpFinanceNumberList.get(i);
				nextRow = sheet.createRow(i + 1);
				
				nextRow.createCell(0).setCellValue(erpEmpFinanceNumber.get("empFinanceNumber")==null?
						"":String.valueOf(erpEmpFinanceNumber.get("empFinanceNumber")));
				nextRow.createCell(1).setCellValue(erpEmpFinanceNumber.get("name")==null?
						"":String.valueOf(erpEmpFinanceNumber.get("name")));
				nextRow.createCell(2).setCellValue(erpEmpFinanceNumber.get("position")==null?
						"":String.valueOf(erpEmpFinanceNumber.get("position")));
				nextRow.createCell(3).setCellValue(erpEmpFinanceNumber.get("rank")==null?
						"":String.valueOf(erpEmpFinanceNumber.get("rank")));
				nextRow.createCell(4).setCellValue(erpEmpFinanceNumber.get("firstDepartmentName")==null?
						"":String.valueOf(erpEmpFinanceNumber.get("firstDepartmentName")));
				nextRow.createCell(5).setCellValue(erpEmpFinanceNumber.get("secondDepartmentName")==null?
						"":String.valueOf(erpEmpFinanceNumber.get("secondDepartmentName")));
				nextRow.createCell(6).setCellValue(erpEmpFinanceNumber.get("statusName")==null?
						"":String.valueOf(erpEmpFinanceNumber.get("statusName")));
			}
			//导出表格到客户端
			this.exportExcelToComputer(workBook,"员工财务序号信息.xlsx");
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("exportEmpFinanceNumber方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常导致导出失败！" );
		}
	}
	
	/**
	 * Description: 查询全部员工财务序号
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月23日 上午16:38:54
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RestResponse findEmpFinanceNumber(String token) {
		logger.info("findEmpFinanceNumber方法开始执行，参数是：token="+token);
		try {
			/*
			 * 调用ERP-人力资源工程-获取所有员工及部门基本信息
			 */
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmployeeAll";
			String body = null;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.add("token", token);
			HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
			if(200 != response.getStatusCodeValue() || !"200".equals(response.getBody().get("status"))) {
				logger.info("调用人力资源工程过程中发生了异常，导致查询失败！");
				return RestUtils.returnFailure("调用人力资源工程过程中发生了异常，导致查询失败！");
			}
			List<Map<String,Object>> allEmployeeList = (List<Map<String, Object>>) response.getBody().get("data");
			for (Map<String, Object> employeeMap : allEmployeeList) {
				Integer employeeId = Integer.valueOf(String.valueOf(employeeMap.get("employeeId")));
				ErpEmpFinanceNumber erpEmpFinanceNumber = empFinanceNumberMapper.findEmpFinanceNumberDetailByEmpId(employeeId);
				if(erpEmpFinanceNumber!=null) {
					employeeMap.put("empFinanceNumber", erpEmpFinanceNumber.getEmpFinanceNumber());
				}else {
					employeeMap.put("empFinanceNumber", "");
				}
			}
			return RestUtils.returnSuccess(allEmployeeList);
		} catch (Exception e) {
			logger.error("findEmpFinanceNumber方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常导致查询失败！");
		}
	}
	
	/**
	 * Description: 查询权限内的员工财务序号
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月24日 上午18:17:24
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RestResponse findEmpFinanceNumberByPower(String token) {
		logger.info("进入findEmpFinanceNumberByPower方法 。参数是：token="+token);
		try {
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();//用户Id
			String username = erpUser.getUsername();//从用户信息中获取用户名
			List<Integer> roles = erpUser.getRoles();//从用户信息中获取角色信息
			logger.info("id="+id+",username="+username+",roles="+roles);
			
			//调用ERP-人力资源 工程 的操作层服务接口-获取一级部门下面所有二级部门的员工的详细信息
			String url = protocolType+"nantian-erp-hr/nantian-erp/erp/employee/findEmpInfoOfAllFirDepByParams";
			Map<String,Object> departmentParams = new HashMap<>();
			/*
			 * 判断当前登录用户的角色查看数据的权限
			 */
			if(roles.contains(8) || roles.contains(7)) {//总经理、经管
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
			requestHeaders.add("token", token);//将token放到请求头中
			HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<>(departmentParams, requestHeaders);
			ResponseEntity<Map> response = this.restTemplate.postForEntity(url, requestEntity, Map.class);
			if(200 != response.getStatusCodeValue() || !"200".equals(response.getBody().get("status"))) {
				return RestUtils.returnFailure("调用人力资源工程过程中发生异常，导致查询员工信息失败！");
			}
			
			List<Map<String, Object>> resultMapList = new ArrayList<>();//返回的员工财务序号等信息的结果集合
			List<List<List<Map<String, Object>>>> firDepEmpInfoList = (List<List<List<Map<String, Object>>>>) response.getBody().get("data");
			logger.info("权限内的一级部门总数："+firDepEmpInfoList.size());
			//权限内所有一级部门员工的信息
			for (List<List<Map<String, Object>>> secDepEmpInfoList : firDepEmpInfoList) {
				logger.info("一级部门下的二级部门总数："+secDepEmpInfoList.size());
				//一级部门下所有二级部门员工的信息
				for (List<Map<String, Object>> empInfoList : secDepEmpInfoList) {
					logger.info("二级部门下的员工总数："+empInfoList.size());
					//一个二级部门下所有员工的信息
					for (Map<String, Object> empInfo : empInfoList) {
						Integer employeeId = Integer.valueOf(String.valueOf(empInfo.get("employeeId")));//员工Id
						ErpEmpFinanceNumber erpEmpFinanceNumber = empFinanceNumberMapper.findEmpFinanceNumberDetailByEmpId(employeeId);
						if(erpEmpFinanceNumber!=null) {
							empInfo.put("empFinanceNumber", erpEmpFinanceNumber.getEmpFinanceNumber());
						}else {
							empInfo.put("empFinanceNumber", "");
						}
						resultMapList.add(empInfo);
					}
				}
			}
			return RestUtils.returnSuccess(resultMapList);
		} catch (Exception e) {
			logger.error("findFirDepEmpInfoByPowerParams出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致查询失败！");
		}
	}
	
	/**
	 * Description: 新增员工财务序号
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月24日 下午15:39:55
	 */
	public RestResponse insertEmpFinanceNumber(ErpEmpFinanceNumber erpEmpFinanceNumber) {
		logger.info("insertEmpFinanceNumber方法开始执行，参数是：erpEmpFinanceNumber="+erpEmpFinanceNumber);
		try {
			empFinanceNumberMapper.insertEmpFinanceNumber(erpEmpFinanceNumber);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("insertEmpFinanceNumber方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常导致新增失败！");
		}
	}
	
	/**
	 * Description: 修改员工财务序号
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月24日 下午15:01:50
	 */
	public RestResponse updateEmpFinanceNumber(ErpEmpFinanceNumber erpEmpFinanceNumber) {
		logger.info("updateEmpFinanceNumber方法开始执行，参数是：erpEmpFinanceNumber="+erpEmpFinanceNumber);
		try {
			empFinanceNumberMapper.updateEmpFinanceNumber(erpEmpFinanceNumber);
			return RestUtils.returnSuccessWithString("OK");
		} catch (Exception e) {
			logger.error("updateEmpFinanceNumber方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常导致修改失败！");
		}
	}
	
/* ******************************************* 封装的工具方法  ******************************************* */
	
	/**
	 * Description: 将导入的Excel表格数据解析成Map格式的数据
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月23日 下午15:23:47
	 */
	public Map<String,String> convertRowToMap(Row row){
		Map<String,String> excelDataMap = new HashMap<>();
		/*
		 * 获取表格一行的所有数据
		 */
		Cell name = row.getCell(0);//姓名
		Cell idCardNumber = row.getCell(1);//身份证号码
		Cell empFinanceNumber = row.getCell(2);//员工财务序号
		
		if(name==null || "".equals(String.valueOf(name).trim())) {
			excelDataMap.put("name", null);
		}else {
			excelDataMap.put("name", String.valueOf(name).trim());
		}
		
		if(idCardNumber==null || "".equals(String.valueOf(idCardNumber).trim())) {
			excelDataMap.put("idCardNumber", null);
		}else {
			excelDataMap.put("idCardNumber", String.valueOf(idCardNumber).trim());
		}
		
		if(empFinanceNumber==null || "".equals(String.valueOf(empFinanceNumber).trim())) {
			excelDataMap.put("empFinanceNumber", null);
		}else {
			excelDataMap.put("empFinanceNumber", String.valueOf(empFinanceNumber).trim());
		}
		return excelDataMap;
	}
	
	/**
	 * Description: 对导入的Excel进行逐行数据内容、格式校验
	 * 1、对于姓名、身份证号进行空值判断。如果为空，这行数据无效，不能入库
	 * 2、当此方法抛出异常的时候，应该判定为“未知异常”
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年01月23日 下午15:36:34
	 */
	public Map<String,String> validateExcelData(Map<String,String> excelDataMap){
		Map<String,String> validateResultMap = new HashMap<>();
		
		/*
		 * 获取表格一行的所有数据
		 */
		String name = excelDataMap.get("name");//姓名
		String idCardNumber = excelDataMap.get("idCardNumber");//身份证号码
		String empFinanceNumber = excelDataMap.get("empFinanceNumber");//员工财务序号
		
		if(name==null) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的员工姓名为空！");
			return validateResultMap;
		}
		
		if(idCardNumber==null) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的员工身份证号码为空！");
			return validateResultMap;
		}
		
		if(empFinanceNumber==null) {
			validateResultMap.put("isError", "Y");
			validateResultMap.put("errorContent", "导入的员工财务序号为空！");
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
	 * Description: 导出Excel文件workBook到客户端
	 *
	 * @return
	 * @throws IOException 
	 * @Author ZhangYuWei
	 * @Create Date: 2018年10月23日 上午10:55:50
	 */
	public String exportExcelToComputer(XSSFWorkbook workBook,String fileName) throws IOException {
		logger.info("exportExcelToComputer方法开始执行，传递参数:fileName="+fileName);

		// 本地测试导出文件，与前端联调、或上线前须注释掉
		/*FileOutputStream fos = new FileOutputStream("C:\\Users\\张玉伟\\Desktop\\"+fileName);
		workBook.write(fos);
		fos.flush();
		fos.close();*/
		
		// 与前端联调导出文件
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
		response.addHeader("Content-Disposition", "attachment;filename="+fileName);
		ServletOutputStream os = response.getOutputStream();
		workBook.write(os);
		os.flush();
		os.close();
		return "OK";
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RestResponse findAllFinanceNumber(String token) {
		logger.info("findAllFinanceNumber方法开始执行，参数是：token="+token);
		try {
			List<Map<String, Object>> financeNumberList = empFinanceNumberMapper.findAllFinanceNumber();

			return RestUtils.returnSuccess(financeNumberList);
		} catch (Exception e) {
			logger.error("findAllFinanceNumber方法出现异常：" + e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常导致查询失败！");
		}
	}
}
