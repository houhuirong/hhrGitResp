package com.nantian.erp.hr.service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.data.dao.ErpContractMapper;
import com.nantian.erp.hr.data.dao.ErpDepartmentMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeDimissionMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeePostiveMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeRecordMapper;
import com.nantian.erp.hr.data.dao.ErpPositionRankRelationMapper;
import com.nantian.erp.hr.data.dao.ImportErrorRecordMapper;
import com.nantian.erp.hr.data.model.ErpContract;
import com.nantian.erp.hr.data.model.ErpDimission;
import com.nantian.erp.hr.data.model.ErpEmployee;
import com.nantian.erp.hr.data.model.ErpEmployeePostive;
import com.nantian.erp.hr.data.model.ErpEmployeeRecord;
import com.nantian.erp.hr.data.model.ErpPositionRankRelation;
import com.nantian.erp.hr.data.model.ImportErrorRecord;
import com.nantian.erp.hr.util.XSSFDateUtil;

/**
 * serviceImpl 接口实现层
 * 人力资源 - 列表导入
 * @author caoxb
 * @date 2018-09-27
 */
@Service
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties","classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties","classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)
public class ErpImportExcelService {
	 @Value("${protocol.type}")
	  private String protocolType;//http或https
	
	@Autowired
	private ErpDepartmentMapper erpDepartmentMapper;
	@Autowired
	private ErpEmployeeRecordMapper employeeRecordMapper;
	@Autowired
	private ErpEmployeeMapper erpEmployeeMapper;
	@Autowired
	private ErpEmployeePostiveMapper employeePostiveMapper;
	
	@Autowired
	private ErpPositionRankRelationMapper erpPositionRankRelationMapper;
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private ImportErrorRecordMapper errRecordMapper;
	
	@Autowired
	private ErpContractMapper contractMapper;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ImportPostTempExcelService importPostTempExcelService;
	
	@Autowired
	private ErpEmployeeDimissionMapper employeeDimissionMapper;
	
	@SuppressWarnings("resource")
	@Transactional
	public RestResponse importEmpExcel(MultipartFile file,String token) throws IOException {
		logger.info("importEmpExcel方法开始执行，传递参数：MultipartFile类型文件");

		Workbook work = null;
		String returnString = null;
		Map<String, List<ImportErrorRecord>> responseMap = new HashMap<>(); //返回前端的错误list
		if (!file.getOriginalFilename().endsWith("xlsx")) {
	    	return RestUtils.returnSuccess("请选择合适的文件导入 ！目前仅支持xlsx文件");
		}
		work = new XSSFWorkbook(file.getInputStream());
		 ArrayList<Map<String,String>> resultMap = null;
		//sheet
		//获取sheet标签个数(由于有隐藏的sheet不需要遍历所以减1)
		int sheetNu = work.getNumberOfSheets()-1;
		List<ImportErrorRecord> errorRecordList = new ArrayList<>();
		/**
		 * 整合员工信息导入 ，员工工作经历，教育经历证书合成一个excel导入
		 */
		for(int sheetindex=0;sheetindex<sheetNu;sheetindex++){//遍历sheet页
			Sheet sheet = work.getSheetAt(sheetindex);
  	        String sheetName = sheet.getSheetName();
  	        logger.info("获取sheet页的名称为{}",sheetName);
  	        resultMap = new ArrayList<Map<String,String>>();
  	        resultMap = importPostTempExcelService.readExcel(work, sheetindex, 1, 0); //读取1个sheet页
    		if(sheetName.contains("教育经历")){
    			errorRecordList.addAll(importPostTempExcelService.addEmpEduExper(resultMap)); //增加员工的工作经历，教育经历等信息
    		}else if(sheetName.contains("证书")){
    			errorRecordList.addAll(importPostTempExcelService.addEmpCertificate(resultMap)); 
    		}else if(sheetName.contains("项目经历")){
    			errorRecordList.addAll(importPostTempExcelService.addEmpProjectExper(resultMap)); 
    		}else if(sheetName.contains("技术特长")){
    			errorRecordList.addAll(importPostTempExcelService.addEmpTechnical(resultMap)); 
    		}else if(sheetName.contains("工作经历")){
    			errorRecordList.addAll(importPostTempExcelService.addEmpWorkExper(resultMap)); 
    		}else if(sheetName.contains("部门职位调整")){
    			errorRecordList.addAll(importPostTempExcelService.updateDepartmentInfo(resultMap,token)); 
    		}else{
    			errorRecordList.addAll(addEmployeeInfo(resultMap,token));
    		}
		}

		returnString = "OK";
		work.close(); //关闭文件
		if(errorRecordList.size()>0){
			return RestUtils.returnSuccess(errorRecordList, "部分数据导入失败"); 
		}else{
			return RestUtils.returnSuccess(errorRecordList, "全部成功");
		}
}
	public  boolean isNumeric(String str){
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}
    /**
     * 增加员工
     * @param result 一个sheet页
     * @param sheetName sheet页名称
     */
    @Transactional 
    public List<ImportErrorRecord>  addEmployeeInfo(ArrayList<Map<String, String>> result,String token){
		ErpEmployee erpEmployee = new ErpEmployee(); //员工对象
		MultiValueMap<String, Object> erpUser = new LinkedMultiValueMap<String, Object>();; //用户对象 map
		Map<String, String> empMap = null;		
		List<ImportErrorRecord> errorRecordList = new ArrayList<>();
		erpUser= new LinkedMultiValueMap<String, Object>();
		
		try{
			for(int i=0;i<result.size();i++){
				 if(result.get(i) == null){//空判断
					continue;
				 }
				 empMap = result.get(i);
				 if(empMap.containsKey("name")){
					 erpEmployee.setName(empMap.get("name"));
				 }else{
					 ImportErrorRecord errorRecord = new ImportErrorRecord();
					 errorRecord.setLineNo(empMap.get("lineNo"));
					 errorRecord.setErrorContent("员工姓名为空");
					 errorRecordList.add(errorRecord);				 
					 continue;
		        }
				if(empMap.containsKey("IdCardNumber")){
					erpEmployee.setIdCardNumber(empMap.get("IdCardNumber"));
				}else{
					ImportErrorRecord errorRecord = new ImportErrorRecord();
					errorRecord.setLineNo(empMap.get("lineNo"));
					errorRecord.setErrorContent("身份证号码为空");
					errorRecordList.add(errorRecord);				 
					continue;
				}
				if(empMap.containsKey("phone")){
					if(isNumeric(String.valueOf(empMap.get("phone")))){
						erpUser.add("userPhone", empMap.get("phone"));
					}else{
						ImportErrorRecord errorRecord = new ImportErrorRecord();
						errorRecord.setLineNo(empMap.get("lineNo"));
						errorRecord.setErrorContent("手机号必须为数字");
						errorRecordList.add(errorRecord);
						continue;
					}
				}else {
					ImportErrorRecord errorRecord = new ImportErrorRecord();
					errorRecord.setLineNo(empMap.get("lineNo"));
					errorRecord.setErrorContent("手机号为空");
					errorRecordList.add(errorRecord);				 
					continue;
				}
				erpEmployee.setSocialSecurity(empMap.get("socialSecurity"));
				erpEmployee.setSex(empMap.get("sex"));
				
				String empStatus = empMap.get("status"); //员工状态
				if("实习生".equals(empStatus)) {
					erpEmployee.setStatus("0");
				}
				if("试用期".equals(empStatus)) {
					erpEmployee.setStatus("1");
				}
				if("正式员工".equals(empStatus)) {
					erpEmployee.setStatus("2");
				}
				if("离职中".equals(empStatus)) {
					erpEmployee.setStatus("3");
				}
				if("已离职".equals(empStatus)) {
					erpEmployee.setStatus("4");
				}
				
				//部门
				String firstDepartmentName = empMap.get("firstDepartmentName");
				String secondDepartmentName = empMap.get("secondDepartmentName");
				Integer firstDepartment = 0;
				Integer secondDepartment = 0;
				
				if("已离职".equals(empStatus)){
					//已离职员工之间保存部门名称，不关联id
					erpEmployee.setFirstDepartmentName(firstDepartmentName);
					erpEmployee.setSecondDepartmentName(secondDepartmentName);
				}else{
					//根据一级部门名字获取一级部门ID
					if(StringUtils.isNotBlank(firstDepartmentName)){
						firstDepartment = this.erpDepartmentMapper.selectIdByFirstDepartmentName(firstDepartmentName);
						if(firstDepartment != null){
							erpEmployee.setFirstDepartment(firstDepartment);
						}else{
								logger.error("一级部门Id为空"+firstDepartmentName);
								erpEmployee.setFirstDepartment(null);			
						}
						
					}else{
						logger.error("一级部门Id为空"+firstDepartmentName);
						erpEmployee.setFirstDepartment(null);
					}
					 
					//根据二级部门名字获取二级部门ID
					if(StringUtils.isNotBlank(secondDepartmentName)){
						Map<String, Object> paramDepartName = new HashMap<>();
						paramDepartName.put("firstDepartmentName", firstDepartmentName);
						paramDepartName.put("secondDepartmentName", secondDepartmentName);
						//根据一级部门名称，和二级部门名称确定 定义唯一一组二级部门ID
						secondDepartment = this.erpDepartmentMapper.selectIdBySecondDepartmentName(paramDepartName);
						if(secondDepartment != null){
							erpEmployee.setSecondDepartment(secondDepartment);
						}else{
							logger.error("二级部门Id为空"+secondDepartmentName);
							erpEmployee.setSecondDepartment(null);
						}
						
					}else{
						logger.error("二级部门Id为空"+secondDepartmentName);
						erpEmployee.setSecondDepartment(null);
					}
				}
				
				//获得职位
				String positionName = empMap.get("positionName");
				logger.info("--查找职位级别关系表 获得职位编号 参数--positionName-开始---="+positionName);
				erpEmployee.setPosition(positionName); //职位名称
				//查找职位级别关系表 获得职位编号
				ErpPositionRankRelation positionRankRelation = null;
				positionRankRelation = erpPositionRankRelationMapper.selectPostionNoByPostNameForSaveBug(positionName);
				if(positionRankRelation == null){
					erpEmployee.setPositionId(0);
				}else{
					//如果职位编号大于0，填入职位编号否则填入0
					erpEmployee.setPositionId(positionRankRelation.getPositionNo());
				}
				logger.info("--查找职位级别关系表 获得职位编号 参数--positionName-结束---="+positionName);
				String rank = empMap.get("rank");
 				if("".equals(rank) || rank == null ){//职级为空 的处理
					erpEmployee.setRank(null);
				}else if(rank.length() > 0){
					String[] strs = rank.split("\\.");
					erpEmployee.setRank(Integer.valueOf(strs[0])); //职级
				}
					
				String entryTime = empMap.get("entryTime");
 				if(entryTime != null && !ExDateUtils.checkDateString(entryTime)){
					ImportErrorRecord errorRecord = new ImportErrorRecord();
					errorRecord.setLineNo(empMap.get("lineNo"));
					errorRecord.setErrorContent("入职时间格式不正确");
					errorRecordList.add(errorRecord);
					continue;
				}
				erpEmployee.setEntryTime(empMap.get("entryTime")); //入职时间
				erpEmployee.setSalaryCardNumber(empMap.get("salaryCardNumber")); //工资卡号
	
				erpUser.add("username",empMap.get("username"));//用户表里存放公司邮箱
				
				erpEmployee.setPersonalEmail(empMap.get("personalEmail"));    //个人邮箱
				if(empMap.get("takeJobTime") != null && !ExDateUtils.checkDateString(empMap.get("takeJobTime"))){
					ImportErrorRecord errorRecord = new ImportErrorRecord();
					errorRecord.setLineNo(empMap.get("lineNo"));
					errorRecord.setErrorContent("首次参加工作时间格式不正确");
					errorRecordList.add(errorRecord);
					continue;
				}
				erpEmployee.setTakeJobTime(empMap.get("takeJobTime"));   //首次参加工作时间

				erpEmployee.setSchool(empMap.get("school"));        //毕业院校
				erpEmployee.setMajor(empMap.get("major"));    	   //专业
				erpEmployee.setEducation(empMap.get("education"));     //最高学历
				
				String record = "";
				//通过员工身份证和姓名查员工ID
				 Integer empId = this.erpEmployeeMapper.findEmpIdByIdCardNum(empMap.get("IdCardNumber"));
				 if (empId != null){
					 ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
					 if (!user.getRoles().contains(4)){//管理员
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(empMap.get("lineNo"));
						 errorRecord.setErrorContent(erpEmployee.getName()+":员工已存在，重复导入请联系管理员");
						 errorRecordList.add(errorRecord);
						 continue;
				 	 }
					 
					 erpEmployee.setEmployeeId(empId);
					 this.erpEmployeeMapper.updateEmployee(erpEmployee); //修改员工
					 record = "重新导入员工信息";					 
				 }else{
					 this.erpEmployeeMapper.insertEmployee(erpEmployee); //插入员工
					//获得插入数据库的主键
					 empId = erpEmployee.getEmployeeId();
					 record = "导入员工";
					 
					//增加员工与部门的关联表
					Map<String,Object> empDepRelation =new HashMap<String, Object>();
					empDepRelation.put("employeeId", empId);
					empDepRelation.put("departmentId", erpEmployee.getFirstDepartment());
					empDepRelation.put("startTime", erpEmployee.getEntryTime());
					erpDepartmentMapper.insertEmpDepRelation(empDepRelation);
				 }
				 
				//当status为"1"时，填写转正流程表			
				if("1".equals(erpEmployee.getStatus())) {//待转正
					Integer userId = this.erpEmployeeMapper.findUserIdByDepartID(erpEmployee.getSecondDepartment()); //二级部门经理用户编号
					if(userId == null){
						//二级部门经理查找不到时由一级部门经理负责
						userId = this.erpEmployeeMapper.findUserIdByDepartID(erpEmployee.getFirstDepartment());
					}
					
					ErpEmployeePostive employeePostive = new ErpEmployeePostive();
					employeePostive.setCurrentPersonID(userId);//用户编号
					employeePostive.setEmployeeId(erpEmployee.getEmployeeId());
					employeePostive.setStatus(1);//待转正的状态标识
					employeePostiveMapper.insertEmployeePostive(employeePostive);
				}	
				 
				//增加员工在职记录表
				ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
				ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
				employeeRecord.setEmployeeId(empId); 
				employeeRecord.setTime(ExDateUtils.getCurrentStringDateTime());
				employeeRecord.setContent(record);
				employeeRecord.setProcessoer(user.getEmployeeName());
				employeeRecordMapper.insertEmployeeRecord(employeeRecord);	
					 
				 //修改合同表  将合同开始，合同结束 试用期时间更新到合同表
				 ErpContract contract = new ErpContract();
				if(empMap.get("contractBeginTime") != null && !ExDateUtils.checkDateString(empMap.get("contractBeginTime"))){
					ImportErrorRecord errorRecord = new ImportErrorRecord();
					errorRecord.setLineNo(empMap.get("lineNo"));
					errorRecord.setErrorContent("合同开始时间格式不正确");
					errorRecordList.add(errorRecord);
					continue;
				}
				 contract.setBeginTime(empMap.get("contractBeginTime")); //合同开始时间
				if(empMap.get("probationEndTime") != null && !ExDateUtils.checkDateString(empMap.get("probationEndTime"))){
					ImportErrorRecord errorRecord = new ImportErrorRecord();
					errorRecord.setLineNo(empMap.get("lineNo"));
					errorRecord.setErrorContent("试用期结束时间格式不正确");
					errorRecordList.add(errorRecord);
					continue;
				}
				 contract.setProbationEndTime(empMap.get("probationEndTime")); //试用期结束时间
				if(empMap.get("endTime") != null && !ExDateUtils.checkDateString(empMap.get("endTime"))){
					ImportErrorRecord errorRecord = new ImportErrorRecord();
					errorRecord.setLineNo(empMap.get("lineNo"));
					errorRecord.setErrorContent("合同结束时间格式不正确");
					errorRecordList.add(errorRecord);
					continue;
				}
				 contract.setEndTime(empMap.get("endTime")); //合同结束时间
				 contract.setEmployeeId(empId);
				 
				 ErpContract conact = contractMapper.findContractByEmpId(empId);
				 if(conact == null){//插入
					 contractMapper.insertContract(contract);
				 }else{//更新
					 contract.setContractId(conact.getContractId());
					 contractMapper.updateContractById(contract);
				 }
				
				if("已离职".equals(empStatus)){					
					if (empMap.containsKey("leaveDate")){
						ErpDimission dimission = new ErpDimission();
						dimission.setEmployeeId(empId);
						dimission.setDimissionTime(empMap.get("leaveDate"));
						dimission.setDealWithTime(empMap.get("dealDate"));
						dimission.setDimissionDirection("");
						dimission.setDimissionReason(empMap.get("leaveReason"));
						//插入一条员工离职记录信息
						employeeDimissionMapper.insertDimission(dimission);
					}
				}else{
					/*
					 * 调用权限工程， 插入用户信息
					 */					
					erpUser.add("userType", 1); //设置用户类型
					erpUser.add("userId", empId); //设置用户ID
					erpUser.add("username", empMap.get("username"));
					Md5Hash md5 = new Md5Hash("000000", "nantian-erp");
					erpUser.add("password", md5.toString());//md5加密后的6个0
					HttpHeaders requestHeaders=new HttpHeaders();
					requestHeaders.add("token",token);//封装token
					HttpEntity<MultiValueMap<String,Object >> request = new HttpEntity<MultiValueMap<String, Object>>(erpUser, requestHeaders); 
					String url = protocolType+"nantian-erp-authentication/nantian-erp/erp/insertErpUserForHr"; 
					logger.info("调用权限工程， 插入用户信息https://nantian-erp-authentication/nantian-erp/erp/insertErpUserForHr,参数{}",erpUser);
					 ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request, RestResponse.class);
					 RestResponse response = responseEntity.getBody();
					if(!"200".equals(response.getStatus())){
						logger.error("调用权限工程插入用户信息失败"+response.getMsg());
						RestUtils.returnFailure("调用权限工程插入用户信息失败:"+response.getMsg());
					} 
					
					Integer userMapId = (Integer) response.getData();
					
					/*
					 * 插入用户角色表
					 */
					//通过员工ID查找User 表id
					MultiValueMap<String, Object> userRole = new LinkedMultiValueMap<String, Object>();
					
//					Object obj = userMapId.get("id");
					Integer userPrikey = userMapId;
					String userRoleurl = protocolType+"nantian-erp-authentication/nantian-erp/userRole/insertUserRoleForHr"; 
					userRole = new LinkedMultiValueMap<String, Object>();
					userRole.add("rId", 3); //默认给3
					userRole.add("uId", userPrikey); //用户ID
					HttpHeaders requestHeadersR=new HttpHeaders();
					requestHeadersR.add("token",token);//封装token
					request = new HttpEntity<MultiValueMap<String, Object>>(userRole, requestHeadersR); 
					 ResponseEntity<RestResponse> responseEntityR = this.restTemplate.postForEntity(userRoleurl, request, RestResponse.class);
					 RestResponse responseR = responseEntityR.getBody();
					if(!"200".equals(responseR.getStatus())){
						logger.error("调用权限工程插入角色信息失败"+response.getMsg());
					}
				}					
			}
		}catch(Exception e) {
			logger.error("未知异常导入失败"+e.getMessage(),e);
		}
		
		return errorRecordList;
    }
	/**
	 * 字符串空验证  true 是空 ,false是非空
	 * @param strdd 传入字符串
	 * @return
	 */
	private boolean  validateIsNull(String strdd ){
		if(StringUtils.isBlank(strdd)|| "null".equals(strdd)){
			return true;
		}else{
			return false;
		}
		
	}
	
	/**
	 * 身份证验证
	 * @param s_aStr
	 * @return
	 */
	public boolean isCard(String cardcode){
		//第一代身份证正则表达式(15位)
		    String isIDCard1 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
		    //第二代身份证正则表达式(18位)
		    String isIDCard2 ="^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[A-Z])$";
		
		    //验证身份证
		    if (cardcode.matches(isIDCard1) || cardcode.matches(isIDCard2)) {
		        return true;
		    }
		    return false;
	}
	
	/**
	 * 身份证唯一性验证
	 * @param valueOf
	 * @return
	 */
	private int personIdOnlyValidate(String valueOf) {
		Integer number = this.erpEmployeeMapper.volidatePersonIdCard(valueOf);
		
		return number;
	}
	
	/**
	 * 通过员工id查找用户信息
	 * @param empId 员工主键
	 * @return  
	 */
	private RestResponse getUserInfoByEmpId(Integer empId,String token){
		logger.info("调用权限工程通过员工id查找用户信息参数empId:"+empId+"   token:"+token);
//		String	token=	request.getHeader("token");
		//根据用户Id调用权限工程获取 userId 
		MultiValueMap<String, Object> erpUser = new  LinkedMultiValueMap<String, Object>(); //用户对象 map
		erpUser.add("userId", empId); //参数
		HttpHeaders requestHeaders=new HttpHeaders();
		requestHeaders.add("token",token);//封装token
		HttpEntity<MultiValueMap<String,Object >> request = new HttpEntity<MultiValueMap<String, Object>>(erpUser, requestHeaders); 
		String url = protocolType+"nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId"; 
//		String url = "http://nantian-erp-authentication/nantian-erp/erp/findErpUserByUserId"; 
		ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request, RestResponse.class);
		RestResponse response = responseEntity.getBody();
		//{result={"status":"200","msg":"新增成功 ！","data":""}, code=200}
		if(!"200".equals(response.getStatus())){
			logger.error("调用权限工程获取用户信息失败"+response.getMsg());
			return RestUtils.returnFailure("调用权限工程获取用户信息失败"+response.getMsg());
		} 
		RestResponse ResponseUser = responseEntity.getBody();
		return  ResponseUser;
	}
	 
	/**
	 * 调用权限工程通过用户Id修改用户信息
	 * @param id  用户ID
	 * @param token  token
	 * @param username  公司邮箱
	 * @param userPhone  电话
	 * @return
	 */
	private RestResponse updateUserInfoById(Integer id,String token,String username,String userPhone){
		logger.info("调用权限工程通过id查找修改用户信息参数id="+id+"   token=:"+token+" username="+username+" userPhone="+userPhone);
//		String	token=	request.getHeader("token");
		//根据用户Id调用权限工程获取 userId 
		MultiValueMap<String, Object> erpUser = new  LinkedMultiValueMap<String, Object>(); //用户对象 map
		erpUser.add("id", id); //参数
		erpUser.add("username", username); //参数
		erpUser.add("userPhone", userPhone); //参数
		HttpHeaders requestHeaders=new HttpHeaders();
		requestHeaders.add("token",token);//封装token
		HttpEntity<MultiValueMap<String,Object >> request = new HttpEntity<MultiValueMap<String, Object>>(erpUser, requestHeaders); 
		String url = protocolType+"nantian-erp-authentication/nantian-erp/authentication/user/updateUserForHr"; 
		ResponseEntity<RestResponse> responseEntity = this.restTemplate.postForEntity(url, request, RestResponse.class);
		RestResponse response = responseEntity.getBody();
		//{result={"status":"200","msg":"新增成功 ！","data":""}, code=200}
		if(!"200".equals(response.getStatus())){
			logger.error("调用权限工程更新用户信息失败"+response.getMsg());
			return RestUtils.returnFailure("调用权限工程更新用户信息失败"+response.getMsg());
		} 
		RestResponse ResponseUser = responseEntity.getBody();
		return  ResponseUser;
	}
	
	 
	 private static String getCellValue(Cell cell) {
		  if (cell == null) {
	            return "";
	        }
	        String strCell = "";
	        switch (cell.getCellType()) {
	            case XSSFCell.CELL_TYPE_STRING:
	                strCell = cell.getStringCellValue();
	                break;
	            case XSSFCell.CELL_TYPE_NUMERIC:
	                if (XSSFDateUtil.isCellDateFormatted(cell)) {
	                    //  如果是date类型则 ，获取该cell的date值
	                    strCell = new SimpleDateFormat("yyyy-MM-dd").format(XSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
	                } else { // 纯数字
	                	DecimalFormat df = new DecimalFormat("0");
	                    strCell = df.format(cell.getNumericCellValue());
//	                    strCell = String.valueOf(cell.getNumericCellValue());
	                }
	                    break;
	            case XSSFCell.CELL_TYPE_BOOLEAN:
	                strCell = String.valueOf(cell.getBooleanCellValue());
	                break;
	            case XSSFCell.CELL_TYPE_BLANK:
	                strCell = "";
	                break;
	            default:
	                strCell = "";
	                break;
	        }
	        if (strCell.equals("") || strCell == null) {
	            return "";
	        }
	      
	        return strCell;
	    }
	 
	 public RestResponse fuzzyQueryEmp(Map<String, Object> map){
		 List li1 = null;
		 String str = (String) map.get("searchdata");
		 try {
			String str1 = "%" + str + "%";
			System.out.println("str1" + str1);
			li1 = erpEmployeeMapper.fuzzyQueryCertificate(str1);
			List li2 = erpEmployeeMapper.fuzzyQueryEducationExperience(str1);
			List li3 = erpEmployeeMapper.fuzzyQueryProjectExperience(str1);
			List li4 = erpEmployeeMapper.fuzzyQueryTechnicaExpertise(str1);
			List li5 = erpEmployeeMapper.fuzzyQueryWorkExperience(str1);
			li1.addAll(li2);
			li1.addAll(li3);
			li1.addAll(li4);
			li1.addAll(li5);
			HashSet h = new HashSet(li1);   
			li1.clear();   
			li1.addAll(h);   
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		 return RestUtils.returnSuccess(li1);	 
	 }

}
