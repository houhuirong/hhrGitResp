package com.nantian.erp.hr.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.AdminDicMapper;
import com.nantian.erp.hr.data.dao.ErpDepartmentMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeDimissionMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeMapper;
import com.nantian.erp.hr.data.model.AdminDic;
import com.nantian.erp.hr.data.model.ErpDepartment;

/** 
 * Description: 人力数据统计service
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月10日      		ZhangYuWei          1.0       
 * </pre>
 */
@Service
public class ErpStatisticsService {
	@Value("${protocol.type}")
	private String protocolType;//http或https
	@Autowired
	private ErpEmployeeMapper employeeMapper;
	@Autowired
	private ErpDepartmentMapper departmentMapper;
	@Autowired
	private AdminDicMapper adminDicMapper;
	@Autowired
	private ErpEmployeeDimissionMapper employeeDimissionMapper;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@SuppressWarnings({ "rawtypes" })
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	HttpServletRequest request;
	@Autowired
	RestTemplate  restTemplate;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Description: 统计员工信息（根据年份，返回每个月的统计信息）
	 *
	 * @param year
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月10日 下午13:29:38
	 */
	public List<Map<String,Object>> info(String token, String year) {
		List<Map<String,Object>> resultMapList = new ArrayList<>();
		for(int i=1;i<=12;i++) {
			String yearAndMonth = "";
			if(i<10) {
				yearAndMonth = year+"-0"+i;
			}else {
				yearAndMonth = year+"-"+i;
			}
			
			Map<String, Object> queryMap=new HashMap<String, Object>();
			queryMap.put("yearAndMonth", yearAndMonth+"%");
			
			Map<String, Object> resultMap = new HashMap<String,Object>();
			
			ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
			List<Integer> roles=erpUser.getRoles();	//当前登录人角色列表
			
			if(roles.contains(8) || roles.contains(1)){//总经理和hr可以看到所有部门的待入职
			}else if(roles.contains(9)){	//副总经理
				queryMap.put("superLeaderId", erpUser.getUserId());
			}
			else if(roles.contains(2)){//一级部门经理角色
				queryMap.put("leaderId", erpUser.getUserId());
			}
			else{
				return resultMapList;
			}

			//可查看所有待入职,即status=1,2							
			int employeeTotal = employeeMapper.findCountByEntryTime(queryMap);
			int dimissionTotal = employeeDimissionMapper.findCountByDimissionTime(queryMap);
			resultMap.put("employeeTotal", employeeTotal);
			resultMap.put("dimissionTotal", dimissionTotal);
			resultMapList.add(resultMap);
		}
		return resultMapList;
	}
	
	/**
	 * Description: 导出首页入职统计的报表
	 *
	 * @param startDate
	 * @param endDate
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月10日 下午13:55:04
	 */
	public RestResponse emportXlsxEntry(String startDate, String endDate) {
		logger.info("emportXlsxEntry方法开始执行，传递参数1：startDate：" + startDate + "参数2：endDate" + endDate);
		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			// 通过起止日期参数，查询数据库中的员工数据信息列表
			List<Map<String, Object>> entryList = employeeMapper.findAllEntry(startDate, endDate);
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("入职统计");
			// 生成第一行
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("性别");
			firstRow.createCell(2).setCellValue("一级部门");
			firstRow.createCell(3).setCellValue("二级部门");
			//firstRow.createCell(2).setCellValue("电话");
			//firstRow.createCell(4).setCellValue("员工邮箱");
			firstRow.createCell(4).setCellValue("职位");
			firstRow.createCell(5).setCellValue("职级");
			firstRow.createCell(6).setCellValue("入职时间");
			firstRow.createCell(7).setCellValue("合同开始时间");
			firstRow.createCell(8).setCellValue("合同结束时间");
			firstRow.createCell(9).setCellValue("转正时间");
			firstRow.createCell(10).setCellValue("个人邮箱");
			firstRow.createCell(11).setCellValue("手机号");
			// 下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			// 循环 填充表格
			for (int i = 0; i < entryList.size(); i++) {
				param = entryList.get(i);
				nextRow = sheet.createRow(i + 1);

				nextRow.createCell(0).setCellValue(param.get("name")==null?"":param.get("name").toString());
				nextRow.createCell(1).setCellValue(param.get("sex")==null?"":param.get("sex").toString());
				nextRow.createCell(2).setCellValue(param.get("firstDepartmentName")==null?"":param.get("firstDepartmentName").toString());
				nextRow.createCell(3).setCellValue(param.get("secondDepartmentName")==null?"":param.get("secondDepartmentName").toString());
				//nextRow.createCell(2).setCellValue(param.get("phone").toString());
				//nextRow.createCell(4).setCellValue(param.get("personalEmail").toString());
				nextRow.createCell(4).setCellValue(param.get("position")==null?"":param.get("position").toString());
				nextRow.createCell(5).setCellValue(param.get("rank")==null?"":param.get("rank").toString());
				nextRow.createCell(6).setCellValue(param.get("entryTime")==null?"":param.get("entryTime").toString());
				nextRow.createCell(7).setCellValue(param.get("contractBeginTime")==null?"":param.get("contractBeginTime").toString());
				nextRow.createCell(8).setCellValue(param.get("contractEndTime")==null?"":param.get("contractEndTime").toString());
				nextRow.createCell(9).setCellValue(param.get("probationEndTime")==null?"":param.get("probationEndTime").toString());
				nextRow.createCell(10).setCellValue(param.get("personalEmail")==null?"":param.get("personalEmail").toString());
				//调用权限工程获取用户的手机号
				 Integer employeeId = Integer.valueOf(String.valueOf(param.get("employeeId"))); 
				 RestResponse  response  = getUserInfoByEmpId( employeeId);
				 Object obj = response.getData();
				 Map<String, Object> user = null;
				 String phone = "";
				 String userName = "";
				 
				 if(StringUtils.isNoneEmpty(param.get("personalEmail").toString())) {
					 phone = String.valueOf(param.get("phone"));
				 }else {
					 if(obj != null && StringUtils.isNoneEmpty(obj.toString())) {
						 user = (Map<String, Object>) obj;
						  phone = String.valueOf(user.get("userPhone")); //个人电话
						  //userName = String.valueOf(user.get("username")); //公司邮箱
					 }
				 }
		
				nextRow.createCell(11).setCellValue(phone);

			}
			this.exportExcelToComputer(workBook, "入职统计.xlsx");
			return RestUtils.returnSuccessWithString("导出成功！");
		} catch (Exception e) {
			logger.error("emportXlsxEntry方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致导出失败！");
		}
	}
	
	/**
	 * Description: 导出首页离职统计的报表
	 *
	 * @param startDate
	 * @param endDate
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月10日 下午14:13:11
	 */
	public RestResponse emportXlsxDimission(String startDate, String endDate) {
		logger.info("emportXlsxDimission方法开始执行，传递参数1：startDate：" + startDate + "参数2：endDate" + endDate);
		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			List<Map<String, Object>> resultList = employeeMapper.findAllDimission(startDate, endDate);
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("离职统计");
			// 生成第一行
			XSSFRow firstRow = sheet.createRow(0);
			firstRow.createCell(0).setCellValue("姓名");
			firstRow.createCell(1).setCellValue("性别");
			firstRow.createCell(2).setCellValue("一级部门");
			firstRow.createCell(3).setCellValue("二级部门");
			firstRow.createCell(4).setCellValue("职位");
			firstRow.createCell(5).setCellValue("职级");
			firstRow.createCell(6).setCellValue("项目名");
			firstRow.createCell(7).setCellValue("项目经理");
			firstRow.createCell(8).setCellValue("离职时间");
			firstRow.createCell(9).setCellValue("离职原因");
			firstRow.createCell(10).setCellValue("离职去向");
			firstRow.createCell(11).setCellValue("首次参加工作时间");
			firstRow.createCell(12).setCellValue("办理手续时间");
			firstRow.createCell(13).setCellValue("入职时间");
			firstRow.createCell(14).setCellValue("个人邮箱");
			firstRow.createCell(15).setCellValue("手机号");
			// 下一行
			XSSFRow nextRow = null;
			Map<String, Object> param = null;
			// 循环 填充表格
			for (int i = 0; i < resultList.size(); i++) {
				param = resultList.get(i);
				nextRow = sheet.createRow(i + 1);
				
				nextRow.createCell(0).setCellValue(param.get("name")==null?"":param.get("name").toString());
				nextRow.createCell(1).setCellValue(param.get("sex")==null?"":param.get("sex").toString());
				nextRow.createCell(2).setCellValue(param.get("firstDepartmentName")==null?"":param.get("firstDepartmentName").toString());
				nextRow.createCell(3).setCellValue(param.get("secondDepartmentName")==null?"":param.get("secondDepartmentName").toString());
				nextRow.createCell(4).setCellValue(param.get("position")==null?"":param.get("position").toString());
				nextRow.createCell(5).setCellValue(param.get("rank")==null?"":param.get("rank").toString());
				nextRow.createCell(6).setCellValue(param.get("projectName")==null?"":param.get("projectName").toString());
				nextRow.createCell(7).setCellValue(param.get("manager")==null?"":param.get("manager").toString());
				nextRow.createCell(8).setCellValue(param.get("dimissionTime")==null?"":param.get("dimissionTime").toString());
				nextRow.createCell(9).setCellValue(param.get("dimissionReason")==null?"":param.get("dimissionReason").toString());
				nextRow.createCell(10).setCellValue(param.get("dimissionDirection")==null?"":param.get("dimissionDirection").toString());
				nextRow.createCell(11).setCellValue(param.get("takeJobTime")==null?"":param.get("takeJobTime").toString());
				nextRow.createCell(12).setCellValue(param.get("dealWithTime")==null?"":param.get("dealWithTime").toString());
				nextRow.createCell(13).setCellValue(param.get("entryTime")==null?"":param.get("entryTime").toString());
				nextRow.createCell(14).setCellValue(param.get("personalEmail")==null?"":param.get("personalEmail").toString());
				nextRow.createCell(15).setCellValue(param.get("userPhone")==null?"":param.get("userPhone").toString());
			}
			this.exportExcelToComputer(workBook, "离职统计.xlsx");
			return RestUtils.returnSuccessWithString("导出成功！");
		} catch (Exception e) {
			logger.error("emportXlsxDimission方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致导出失败！");
		}
	}
	
	/**
	 * Description: 实现导出文件到电脑
	 *
	 * @param workBook
	 * @param fileName
	 * @throws IOException
	 * @Author ZhangYuWei
	 * @Create Date: 2018年12月10日 下午14:56:20
	 */
	public void exportExcelToComputer(XSSFWorkbook workBook, String fileName) throws IOException {
		logger.info("exportExcelToComputer方法开始执行，参数是：fileName="+fileName);
		// 本地测试导出文件
		/*FileOutputStream fos = new FileOutputStream("D:\\java\\"+fileName);
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
	}
	
	/**
	 * Description: 统计集团的人力数据信息
	 * @param 
	 * @return
	 * @Author ZhangQian
	 * @Create Date: 2019年03月06日 下午13:29:38
	 */
	public RestResponse queryGroupData(String token) {
		logger.info("查询集团人力数据信息，queryGroupData");
		List<Map<String,Object>> resultMapList = new ArrayList<>();
		try{
			//查询所有部门类别
			List<AdminDic> depTypelist = adminDicMapper.findApprove(DicConstants.DEPARTMENT_TYPE);
			
			//查询所有一级部门
			List<Map<String,Object>> firstDepList = departmentMapper.findAllFirstDepartment();
			
			for(AdminDic departmentType : depTypelist){
				//按部门类别过滤
				Map<String,Object> depTypeInfo = new HashMap<>();
				List<Map<String,Object>> departmentList = new ArrayList<>();
				depTypeInfo.put("title", departmentType.getDicName());
				
				//过滤一级部门
				for(Map<String,Object> firstDepartment : firstDepList){
					if (firstDepartment.get("departmentTypeName").equals(departmentType.getDicName())){
						Map<String,Object> departmentInfo = new HashMap<>();
						//部门名
						departmentInfo.put("name", firstDepartment.get("departmentName"));
						departmentInfo.put("id", firstDepartment.get("departmentId"));
						//部门人员总数
						List<Map<String,Object>> employeeList = departmentMapper.findEmployeeList((Integer) firstDepartment.get("departmentId"));
						departmentInfo.put("number", employeeList.size());
						departmentList.add(departmentInfo);
					}
				}
				depTypeInfo.put("list", departmentList);
				resultMapList.add(depTypeInfo);
			}
			return RestUtils.returnSuccess(resultMapList);
		} catch (Exception e) {
			logger.error("queryGroupData方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致导出失败！");
		}
	}	
	/**
	 * Description: 统计一级部门的人力数据信息
	 * @param 
	 * @return
	 * @Author ZhangQian
	 * @Create Date: 2019年03月06日 下午13:29:38
	 */
	public RestResponse queryFirstDepartmentData(String token, Integer firstDepartmentId) {
		logger.info("查询一级部门人力数据信息，firstDepartmentId："+firstDepartmentId);
		try{
			Map<String,Object> resultMap = new HashMap<>();
			List<Map<String,Object>> dataList = new ArrayList<>();
			
			//权限判断
			ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
			List<Integer> roles=erpUser.getRoles();	//当前登录人角色列表			
			
			ErpDepartment departmentInfo = departmentMapper.findByDepartmentId(firstDepartmentId);
			
			if(roles.contains(8) || roles.contains(1)){//总经理和hr可以看到所有部门的待入职

			}else if(roles.contains(9)){	//副总经理
				if(!departmentInfo.getSuperLeader().equals(erpUser.getUserId())){
					return RestUtils.returnSuccess("无权限访问该一级部门的详细数据！");
				}
			}
			else if(roles.contains(2)){//一级部门经理角色
				if(!departmentInfo.getUserId().equals(erpUser.getUserId())){
					return RestUtils.returnSuccess("无权限访问该一级部门的详细数据！");
				}
			}
			else{
				return RestUtils.returnSuccess("无权限访问该一级部门的详细数据！");
			}
			
			//二级部门维度数据
			dataList.add(this.querySecDepInfo(firstDepartmentId));
			
			//职级维度数据
			dataList.add(this.queryRankInfo(firstDepartmentId));
			
			//入司时间维度数据
			dataList.add(this.queryEntryInfo(firstDepartmentId));
			
			//年龄维度数据
			dataList.add(this.queryAgeInfo(firstDepartmentId));
			
			resultMap.put("dataList", dataList);
			
			//查询部门入离职统计
			List<Map<String,Object>> statisticList = this.queryStatistics(firstDepartmentId);
			resultMap.put("statistic", statisticList);
			
			return RestUtils.returnSuccess(resultMap);
		} catch (Exception e) {
			logger.error("queryGroupData方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致导出失败！");
		}
	}	
	/**
	 * Description: 统计一级部门二级部门数据
	 * @param 
	 * @return
	 * @Author ZhangQian
	 * @Create Date: 2019年03月06日 下午13:29:38
	 */
	public Map<String,Object> querySecDepInfo(Integer firstDepartmentId) {
		logger.info("统计一级部门二级部门数据，firstDepartmentId："+firstDepartmentId);
		Map<String,Object> data = new HashMap<>();
		try{
			List<Map<String,Object>> secDepList = departmentMapper.findAllSecondDepartmentByFirDepId(firstDepartmentId);
			data.put("title", "二级部门");
			List<Map<String,Object>> departmentList = new ArrayList<>();
			for(Map<String,Object> secDepartment : secDepList){
				Map<String,Object> departmentInfo = new HashMap<>();
				//部门名
				departmentInfo.put("name", secDepartment.get("departmentName"));
				//部门人员总数
				List<Map<String,Object>> employeeList = departmentMapper.findEmployeeList((Integer) secDepartment.get("departmentId"));
				departmentInfo.put("number", employeeList.size());
				departmentList.add(departmentInfo);
			}
			data.put("list", departmentList);
		} catch (Exception e) {
			logger.error("querySecDepInfo方法出现异常："+e.getMessage(),e);
		}
		return data;
	}	
	/**
	 * Description: 统计一级部门下职级数据
	 * @param 
	 * @return
	 * @Author ZhangQian
	 * @Create Date: 2019年03月06日 下午13:29:38
	 */
	public Map<String,Object> queryRankInfo(Integer firstDepartmentId) {
		logger.info("统计一级部门下职级数据，firstDepartmentId："+firstDepartmentId);
		Map<String,Object> data = new HashMap<>();
		try{
			List<Map<String,Object>> rankList = new ArrayList<>();
			for (int i=2; i<16; i++){
				//2级到16级
				Map<String,Object> info = new HashMap<>();
				Map<String,Object> key = new HashMap<>();
				key.put("departmentId", firstDepartmentId);
				key.put("rank", i);
				Integer number = departmentMapper.countEmployeeNumByParams(key);
				if (!number.equals(0)){
					info.put("name", i+"级");
					info.put("number", number);
					rankList.add(info);
				}				
			}
			data.put("title", "职级");
			data.put("list", rankList);
		} catch (Exception e) {
			logger.error("queryRankInfo方法出现异常："+e.getMessage(),e);
		}
		return data;
	}	
	/**
	 * Description: 统计一级部门的司龄数据信息
	 * @param 
	 * @return
	 * @Author ZhangQian
	 * @Create Date: 2019年03月06日 下午13:29:38
	 */
	public Map<String,Object> queryEntryInfo(Integer firstDepartmentId) {
		logger.info("查询一级部门的司龄数据信息，firstDepartmentId："+firstDepartmentId);
		Map<String,Object> data = new HashMap<>();
		try{			
			//司龄维度的数据，按照入职时间统计，0-3年，3-5年，5-10年，10年以上
			List<Map<String,Object>> entryList = new ArrayList<>();
			
			Date date = new Date();  //获取当前时间
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
			String dateNowStr = format.format(date);
			
			long tempDay = 1000*3600*24;
			long tempYear = tempDay*365;
			
			long zero=date.getTime()/(tempDay)*(tempDay)-TimeZone.getDefault().getRawOffset();//今天零点零分零秒的毫秒数
			date = new Date(zero);			
			
			Date date3year = new Date(date.getTime()-tempYear*3);//3年
			String date3yearStr = format.format(date3year);
			
			Date date5year = new Date(date.getTime()-tempYear*5);//5年
			String date5yearStr = format.format(date5year);
			
			Date date10year = new Date(date.getTime()-tempYear*10);//10年
			String date10yearStr = format.format(date10year);
			
			String[] name = {"0-3年", "3-5年", "5-10年", "10年以上"};
			String[] startDate = {date3yearStr, date5yearStr, date10yearStr, "0"};
			String[] endDate = {dateNowStr, date3yearStr, date5yearStr, date10yearStr};
			
			for (int i=0; i<4; i++){
				Map<String,Object> info = new HashMap<>();
				Map<String,Object> key = new HashMap<>();
				key.put("departmentId", firstDepartmentId);
				key.put("startDate", startDate[i]);
				key.put("endDate", endDate[i]);				
				Integer number = departmentMapper.countEmployeeNumByParams(key);
				
				info.put("name", name[i]);
				info.put("number", number);
				entryList.add(info);
			}
			
			data.put("title", "入司年限");
			data.put("list", entryList);
		} catch (Exception e) {
			logger.error("queryGroupData方法出现异常："+e.getMessage(),e);
		}
		return data;
	}	
	/**
	 * Description: 统计一级部门的员工年龄数据信息
	 * @param 
	 * @return
	 * @Author ZhangQian
	 * @Create Date: 2019年03月06日 下午13:29:38
	 */
	public Map<String,Object> queryAgeInfo(Integer firstDepartmentId) {
		logger.info("查询一级部门的员工年龄数据信息，firstDepartmentId："+firstDepartmentId);
		Map<String,Object> data = new HashMap<>();
		try{
			List<Map<String,Object>> ageList = new ArrayList<>();
			
			Date date = new Date();  //获取当前时间
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
			
			long tempDay = 1000*3600*24;
			long tempYear = tempDay*365;
			
			long zero=date.getTime()/(tempDay)*(tempDay)-TimeZone.getDefault().getRawOffset();//今天零点零分零秒的毫秒数
			date = new Date(zero);
			
			Date date20year = new Date(date.getTime()-tempYear*20);//20岁
			String date20yearStr = format.format(date20year);
			
			Date date30year = new Date(date.getTime()-tempYear*30);//30岁
			String date30yearStr = format.format(date30year);
			
			Date date40year = new Date(date.getTime()-tempYear*40);//40岁
			String date40yearStr = format.format(date40year);
			
			Date date50year = new Date(date.getTime()-tempYear*50);//50岁
			String date50yearStr = format.format(date50year);
			
			String[] nameAge = {"20-30岁", "30-40岁", "40-50岁", "50岁以上"};
			String[] startAge = {date30yearStr, date40yearStr, date50yearStr, "0"};
			String[] endAge = {date20yearStr, date30yearStr, date40yearStr, date50yearStr};
			
			for (int i=0; i<4; i++){
				Map<String,Object> info = new HashMap<>();
				Map<String,Object> key = new HashMap<>();
				key.put("departmentId", firstDepartmentId);
				key.put("startAge", startAge[i]);
				key.put("endAge", endAge[i]);	
				Integer number = departmentMapper.countEmployeeNumByParams(key);
				
				info.put("name", nameAge[i]);
				info.put("number", number);
				ageList.add(info);
			}
			
			data.put("title", "员工年龄");
			data.put("list", ageList);
		} catch (Exception e) {
			logger.error("queryGroupData方法出现异常："+e.getMessage(),e);
		}
		return data;
	}	
	/**
	 * Description: 查询一级部门人员数据
	 * @param 
	 * @return
	 * @Author ZhangQian
	 * @Create Date: 2019年03月06日 下午13:29:38
	 */
	public List<Map<String,Object>> queryStatistics(Integer firstDepartmentId) {
		logger.info("查询一级部门人员数据，firstDepartmentId："+firstDepartmentId);
		List<Map<String,Object>> dataList = new ArrayList<>();
		try{
			Map<String,Object> key = new HashMap<>();			
			key.put("departmentId", firstDepartmentId);
			
			//查询部门开始时间
			String startTime = departmentMapper.getDepartmentStartTime(key);
			String startMonth = startTime.substring(0, 7);
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
			Date startdate = format.parse(startMonth);
			
			Map<String,Object> startMonthData = this.queryMonthData(firstDepartmentId, startMonth);
			dataList.add(startMonthData);
			
			//获取当前时间，截止时间
			Date dateNow = new Date();
			String endMonth = format.format(dateNow);
			
			long tempDay = 1000*3600*24;
			
			if (!startMonth.equals(endMonth)){
				//从开始时间逐月向后遍历
				Date next = new Date(startdate.getTime()+tempDay*31);
				String nextMonth = format.format(next);			
				while(!endMonth.equals(nextMonth)){
					Date nextTemp = next;
					Map<String,Object> monthData = this.queryMonthData(firstDepartmentId, nextMonth);
					dataList.add(monthData);		
					
					next = new Date(nextTemp.getTime()+tempDay*31);
					nextMonth = format.format(next);			
				}
				
				//最后一个月
				Map<String,Object> endMonthData = this.queryMonthData(firstDepartmentId, endMonth);
				dataList.add(endMonthData);	
			}
		
		} catch (Exception e) {
			logger.error("queryGroupData方法出现异常："+e.getMessage(),e);
		}
		return dataList;
	}
	
	public Map<String,Object> queryMonthData(Integer firstDepartmentId, String month) 
	{
		logger.info("查询一级部门数据，queryMonthData："+firstDepartmentId+"month:"+month);
		Map<String,Object> resultData = new HashMap<>();
		try{
			Map<String,Object> queryData = new HashMap<>();
			
			queryData.put("month", month);		
			queryData.put("departmentId", firstDepartmentId);	
			Integer employeeNum = departmentMapper.coutEmployeeNumByMonth(queryData);
			queryData.put("month", month+"%");	
			Integer entryNum = departmentMapper.coutEntryNumByMonth(queryData);
			Integer dimissionNum = departmentMapper.coutDimissionNumByMonth(queryData);
			Integer changeInNum = departmentMapper.coutChangeInNumByMonth(queryData);
			Integer changeOutNum = departmentMapper.coutChangeOutNumByMonth(queryData);
			
			resultData.put("month", month);
			resultData.put("employeeNum", employeeNum);
			resultData.put("entryNum", entryNum);
			resultData.put("dimissionNum", dimissionNum);
			resultData.put("changeInNum", changeInNum);
			resultData.put("changeOutNum", changeOutNum);
		} catch (Exception e) {
			logger.error("queryMonthData方法出现异常："+e.getMessage(),e);
		}
		return resultData;
	}
	
	/**
	 * 通过员工id查找用户信息
	 * @param empId 员工主键
	 * @return  
	 */
	private RestResponse getUserInfoByEmpId(Integer empId){
		String	token=	request.getHeader("token");
		//根据用户Id调用权限工程获取 userId 
		MultiValueMap<String, Object> erpUser = new  LinkedMultiValueMap<String, Object>(); //用户对象 map
		erpUser.add("userId", empId); //参数
		HttpHeaders requestHeaders=new HttpHeaders();
		requestHeaders.add("token",token);//封装token
		HttpEntity<MultiValueMap<String,Object >> request = new HttpEntity<MultiValueMap<String, Object>>(erpUser, requestHeaders); 
		String url = protocolType+"nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId"; 
		ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request, RestResponse.class);
		RestResponse response = responseEntity.getBody();
		if(!"200".equals(response.getStatus())){
			logger.error("调用权限工程获取用户信息失败"+response.getMsg());
			return RestUtils.returnFailure("调用权限工程获取用户信息失败"+response.getMsg());
		} 
		RestResponse ResponseUser = responseEntity.getBody();
		return  ResponseUser;
	}
}
