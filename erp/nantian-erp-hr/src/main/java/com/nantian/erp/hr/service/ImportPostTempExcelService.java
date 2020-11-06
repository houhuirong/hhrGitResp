package com.nantian.erp.hr.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.hr.data.dao.AdminDicMapper;
import com.nantian.erp.hr.data.dao.ErpDepartmentMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeRecordMapper;
import com.nantian.erp.hr.data.dao.ErpPositionRankRelationMapper;
import com.nantian.erp.hr.data.dao.ErpPostMapper;
import com.nantian.erp.hr.data.dao.PostDutyMapper;
import com.nantian.erp.hr.data.dao.PostRequireMapper;
import com.nantian.erp.hr.data.dao.PostTemplateMapper;
import com.nantian.erp.hr.data.model.ErpCertificate;
import com.nantian.erp.hr.data.model.ErpEducationExperience;
import com.nantian.erp.hr.data.model.ErpEmployee;
import com.nantian.erp.hr.data.model.ErpEmployeeRecord;
import com.nantian.erp.hr.data.model.ErpPositionRankRelation;
import com.nantian.erp.hr.data.model.ErpProjectExperience;
import com.nantian.erp.hr.data.model.ErpTechnicaExpertise;
import com.nantian.erp.hr.data.model.ErpWorkExperience;
import com.nantian.erp.hr.data.model.ImportErrorRecord;
import com.nantian.erp.hr.data.model.PostDuty;
import com.nantian.erp.hr.data.model.PostRequire;
import com.nantian.erp.hr.data.model.PostTemplate;
import com.nantian.erp.hr.util.XSSFDateUtil;

@Service
public class ImportPostTempExcelService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private PostTemplateMapper postTemplateMapper;
	
	@Autowired
	private ErpPostMapper postMapper;
	
	@Autowired
	private AdminDicMapper adminDicMapper;
	
	@Autowired
	private PostDutyMapper postDutyMapper;
	
	@Autowired
	private PostRequireMapper postRequireMapper;
	
	@Autowired
	private ErpEmployeeMapper employeeMapper;
	@Autowired
	private ErpDepartmentMapper erpDepartmentMapper;
	@Autowired
	private ErpEmployeeRecordMapper employeeRecordMapper;
	@Autowired
	private ErpPositionRankRelationMapper erpPositionRankRelationMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	
	/**
	 * 添加岗位模板
	 * @param result
	 */
	@Transactional
	public  void addPostTemplate (ArrayList<Map<String,String>> result){
		PostTemplate template = new PostTemplate(); //创建模板对象
		/*String  categoryId= null;
		String  jobId= null;
		String  postName= null;
		String  salaryRange= null;
		String  childId= null;
		String  familyId= null;
		String  postDuty= null;
		String  postRequire= null;*/
		for(Map<String, String> map :result){//遍历每一行数据
			String categoryId = map.get("categoryId").split("\\.")[0];
			String jobId = map.get("jobId");
		 	
		   String[] job = jobId.split("\\.");
		   jobId = job[0];
		   String postName = map.get("postName");
		   String salaryRange = map.get("salaryRange");
		   String childId = map.get("childId").split("\\.")[0]; //职位子类
		   String familyId = map.get("familyId");
		   String postDuty = map.get("postDuty"); //岗位职责
		   String postRequire = map.get("postRequire"); //岗位需求
			String[] dutyArray = postDuty.split("\\d+[、]|\\d+[.]"); //split("\\d+{3、}")
			String[] requireArray = postRequire.split("\\d+[、]|\\d+[.]");
			template.setCategory(categoryId);//岗位类别码值
			template.setJobCategory(jobId);//职位类别码值
			template.setPostName(postName); 
			template.setSalaryRange(salaryRange);
			template.setPositionChildType(childId);//职位类别子类码值
			template.setFamilyId(familyId);//职位职级族码值
			 Integer n	= postTemplateMapper.addPostTemplate(template);
			Integer postTemplateId =  template.getPostTemplateId(); //返回插入数据的主键
			logger.info("-------获取插入后返回的主键---postTemplateId------------="+postTemplateId);
			List<PostDuty> listDuty = new ArrayList<>();
			 List<PostRequire> listReuire = new ArrayList<>();
			//插入岗位职责和岗位要求
			for(int i=0;i<dutyArray.length;i++){
				if(StringUtils.isAllBlank(dutyArray[i])){
					continue;
				}
				PostDuty positionduty = new PostDuty();
				positionduty.setPostdutyDescribe(dutyArray[i]);
				positionduty.setPostId(postTemplateId);
				positionduty.setType(1); //1 是岗位模板 2是岗位申请
				listDuty.add(positionduty);
			}
			
			for(int j=0;j<requireArray.length;j++){
				if(StringUtils.isAllBlank(requireArray[j])){
					continue;
				}
				PostRequire postRequire2 = new PostRequire();
				postRequire2.setPostId(postTemplateId);
				postRequire2.setPostRequireDescribe(requireArray[j]);
				postRequire2.setType(1); //1 是岗位模板 2是岗位申请
				listReuire.add(postRequire2);
			}
			//批量插入岗位职责
			postDutyMapper.addPostDutyBatch(listDuty);
			//批量插入岗位要求
			postRequireMapper.addPostRequireBatch(listReuire);
			
		}
	
		
	}
	/**
     * 读取excel数据
     * @param path
     */
    public ArrayList<Map<String,String>> readExcelToObj(MultipartFile file) {

        Workbook wb = null;
        ArrayList<Map<String,String>> result = null;
        try {
        	wb = new XSSFWorkbook(file.getInputStream());
        	int sheetNu = wb.getNumberOfSheets(); //获取sheet标签个数
        	
        	for(int sheetIndex=0;sheetIndex<sheetNu;sheetIndex++ ){
        		Sheet sheet = wb.getSheetAt(sheetIndex);
      	        String sheetName = sheet.getSheetName();
      	        logger.info("");
      	        result = new ArrayList<Map<String,String>>();
        		result = readExcel(wb, sheetIndex, 1, 0); //读取1个sheet页
        		if(sheetName.contains("教育经历")){
        			result.remove(result.size()-1);
        		}else if(sheetName.contains("证书")&&result.size()>=2){
        			result.remove(result.size()-1);
        		}        
//              result = readExcel(wb, 0, 3, 0);
//              addPostTemplate (result);
//              addEmpWorkExper(result,sheetName); //增加员工的工作经历，教育经历等信息
        	}
        	 
        } catch (IOException e) {
            logger.error("readExcelToObj()方法插入或更新表报错:{}",e.getMessage(),e);
        }
        return result;
    }
    /**
     * 增加员工的工作经历等信息
     * @param result 一个sheet页
     * @param sheetName sheet页名称
     */
    @Transactional 
    public List<ImportErrorRecord>  addEmpWorkExper(ArrayList<Map<String, String>> result) {
    	String name = ""; //姓名
    	String personalId = ""; //身份证ID 
    	List<ImportErrorRecord> errorRecordList = new ArrayList<>();
		//根据sheet标签名分别调用不同的方法
    	try {
			 for(int i=0;i<result.size();i++){
				 if(result.get(i) == null){//空判断
					continue;
				 }else{
					 if(result.get(i).containsKey("name")){
						 name =  String.valueOf(result.get(i).get("name"));
					 }else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("员工姓名为空");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
					 if(result.get(i).containsKey("personalId")){
						 personalId =  String.valueOf(result.get(i).get("personalId"));
					 }else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("员工身份证号为空");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
					 //通过员工 姓名和身份证查找员工ID
					 Map<String, Object> param = new HashMap<>();
					 param.put("idCardNumber", personalId);
					 param.put("name", name);
					 logger.info("通过员工 姓名和身份证查找员工ID参数name={},idCardNumber={}",name,personalId);
					 Integer empId =  employeeMapper.findEmpIdByIdCardNumAndName(param);
					 logger.info("通过员工 姓名和身份证查找员工ID={}",empId);
					if(empId != null){
						ErpWorkExperience  workExperience = new ErpWorkExperience();
						workExperience.setEmployeeId(empId);
						workExperience.setStartTime(result.get(i).get("startTime"));
						workExperience.setEndTime(result.get(i).get("endTime"));
						workExperience.setCompany(result.get(i).get("company"));
						workExperience.setPosition(result.get(i).get("position"));
						 //通过员工Id
						 List<Map<String, Object>> listWork =  employeeMapper.findAllWorkExperienceByEmp(empId);
						 if(listWork.isEmpty()|| listWork==null){//增加数据
							 employeeMapper.addWorkExperienceByEmp(workExperience);
						 }else{//开始时间、结束时间进行数据重复校验  ,进行更新
						  Integer n	= employeeMapper.findWorkByEmpIdStartEnd(empId,result.get(i).get("startTime"),result.get(i).get("endTime"));
						  if(n != null){
							  workExperience.setId(n);
							  employeeMapper.updateWorkExperienceByEmp(workExperience); //更新已经存在的工作经历
						  }else{//增加工作经历
							  employeeMapper.addWorkExperienceByEmp(workExperience);
						  }
						 }
					}else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("通过姓名和身份证号查询不到员工");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
				 }
			 }		 
    	} catch (Exception e) {
			logger.error("插入或更新员工工作经历报错:",e.getMessage(),e);
		}
    	return errorRecordList;
	}
    /**
     * 员工部门信息更新
     * @param result 一个sheet页
     * @param sheetName sheet页名称
     */
    @Transactional 
    public List<ImportErrorRecord>  updateDepartmentInfo(ArrayList<Map<String, String>> result,String token) {
    	String name = ""; //姓名
    	String personalId = ""; //身份证ID 
    	List<ImportErrorRecord> errorRecordList = new ArrayList<>();
    	//根据sheet标签名分别调用不同的方法
    	try {
    		for(int i=0;i<result.size();i++){
    			if(result.get(i) == null){//空判断
    				continue;
    			}else{
    				if(result.get(i).containsKey("name")){
    					name =  String.valueOf(result.get(i).get("name"));
    				}else{
    					ImportErrorRecord errorRecord = new ImportErrorRecord();
    					errorRecord.setLineNo(result.get(i).get("lineNo"));
    					errorRecord.setErrorContent("员工姓名为空");
    					errorRecordList.add(errorRecord);				 
    					continue;
    				}
    				if(result.get(i).containsKey("personalId")){
    					personalId =  String.valueOf(result.get(i).get("personalId"));
    				}else{
    					ImportErrorRecord errorRecord = new ImportErrorRecord();
    					errorRecord.setLineNo(result.get(i).get("lineNo"));
    					errorRecord.setErrorContent("员工身份证号为空");
    					errorRecordList.add(errorRecord);				 
    					continue;
    				}
    				//通过员工 姓名和身份证查找员工ID
    				Map<String, Object> param = new HashMap<>();
    				param.put("idCardNumber", personalId);
    				param.put("name", name);
    				logger.info("通过员工 姓名和身份证查找员工ID参数name={},idCardNumber={}",name,personalId);
    				Integer empId =  employeeMapper.findEmpIdByIdCardNumAndName(param);
    				logger.info("通过员工 姓名和身份证查找员工ID={}",empId);
    				if(empId != null){
    					ErpEmployee erpEmployee = new ErpEmployee(); //员工对象
    					erpEmployee.setEmployeeId(empId);
    					String firstDepartmentName = result.get(i).get("firstDepartmentName");
    					String secondDepartmentName = result.get(i).get("secondDepartmentName");
    					String time = result.get(i).get("time");
    					Integer firstDepartment = 0;
    					Integer secondDepartment = 0;
    					String str = "";
    					
    					//部门信息解析
    					if(StringUtils.isNotBlank(firstDepartmentName) && StringUtils.isNotBlank(secondDepartmentName)){
    						firstDepartment = this.erpDepartmentMapper.selectIdByFirstDepartmentName(firstDepartmentName);
    						if(firstDepartment != null){
    							Map<String, Object> paramDepartName = new HashMap<>();
        						paramDepartName.put("firstDepartmentName", firstDepartmentName);
        						paramDepartName.put("secondDepartmentName", secondDepartmentName);
        						//根据一级部门名称，和二级部门名称确定 定义唯一一组二级部门ID
        						secondDepartment = this.erpDepartmentMapper.selectIdBySecondDepartmentName(paramDepartName);
        						if(secondDepartment != null){
        							erpEmployee.setFirstDepartment(firstDepartment);
        							erpEmployee.setSecondDepartment(secondDepartment);
        							str = str+"部门信息,";
        							
        							/*更新部门与员工的归属关系*/      
        							//查询员工当前部门归属
        							Map<String, Object> key = new HashMap<>();
        							key.put("employeeId",empId);
        							key.put("currentDepartment",true);
        							List<Map<String, Object>> relationList = this.erpDepartmentMapper.getEmpDepRelation(key);
        							if (relationList.size()>1){
        								logger.error("员工存在多个当前部门关联："+empId);
        							}
        							for(Map<String, Object> relation : relationList){
        								if(!relation.get("departmentId").equals(firstDepartment)){
        									SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        									Date startNew = format.parse(time);
        									Date startCurrent = format.parse(String.valueOf(relation.get("startTime")));
        									
        									if (startNew.compareTo(startCurrent) < 0){
        										//当前部门归属的开始时间大于导入的生效时间，直接修改部门id
        										relation.put("departmentId", firstDepartment);
            									this.erpDepartmentMapper.updateEmpDepRelation(relation);
        									}
        									else{
	        									Date last = new Date(startNew.getTime()-1000*3600*24);
	        									String lastDay = format.format(last);	
	        									relation.put("endTime", lastDay);
	        									this.erpDepartmentMapper.updateEmpDepRelation(relation);
	        									
	        									Map<String, Object> newRelation = new HashMap<>();
	        									newRelation.put("employeeId",empId);
	        									newRelation.put("departmentId",firstDepartment);
	        									newRelation.put("startTime",time);
	        									this.erpDepartmentMapper.insertEmpDepRelation(newRelation);
        									}
        								}
        							}
        						}else{
        							logger.error("二级部门不存在："+secondDepartmentName);
        							
        							ImportErrorRecord errorRecord = new ImportErrorRecord();
        	    					errorRecord.setLineNo(result.get(i).get("lineNo"));
        	    					errorRecord.setErrorContent("二级部门错误");
        	    					errorRecordList.add(errorRecord);
        						}
    						}
    						else{
    							logger.error("一级部门不存在："+firstDepartmentName);
    							
    							ImportErrorRecord errorRecord = new ImportErrorRecord();
    	    					errorRecord.setLineNo(result.get(i).get("lineNo"));
    	    					errorRecord.setErrorContent("一级部门错误");
    	    					errorRecordList.add(errorRecord);
    						}
    					}else{
    						logger.info("导入部门存在空，一级部门："+firstDepartmentName+"，二级部门："+secondDepartmentName);
    					}
    					 
    					//获得职位
    					String positionName = result.get(i).get("positionName");
    					logger.info("--查找职位级别关系表 获得职位编号 参数--positionName-开始---="+positionName);
    					if(StringUtils.isNotBlank(positionName)){
	    					erpEmployee.setPosition(positionName); //职位名称
	    					//查找职位级别关系表 获得职位编号
	    					ErpPositionRankRelation positionRankRelation = null;
	    					positionRankRelation = erpPositionRankRelationMapper.selectPostionNoByPostNameForSaveBug(positionName);
	    					if(positionRankRelation == null){
	    						ImportErrorRecord errorRecord = new ImportErrorRecord();
    	    					errorRecord.setLineNo(result.get(i).get("lineNo"));
    	    					errorRecord.setErrorContent("职位信息错误");
    	    					errorRecordList.add(errorRecord);
	    					}else{
	    						Integer rank = Integer.valueOf(result.get(i).get("rank"));
	    						if(rank.equals(positionRankRelation.getRank())){
	    							erpEmployee.setPosition(positionName);
	    							erpEmployee.setPositionId(positionRankRelation.getPositionNo());
	    							erpEmployee.setRank(rank);
	    							
	    							str = str+"职位职级信息";
	    						}
	    						else{
	    							ImportErrorRecord errorRecord = new ImportErrorRecord();
	    	    					errorRecord.setLineNo(result.get(i).get("lineNo"));
	    	    					errorRecord.setErrorContent("职级信息错误");
	    	    					errorRecordList.add(errorRecord);
	    						}
	    					}
    					}
    					if (str!=""){
        					this.employeeMapper.updateEmployee(erpEmployee); //修改员工表
        					
        					//增加员工在职记录表
        					ErpUser user = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
        					ErpEmployeeRecord employeeRecord = new ErpEmployeeRecord();
        					employeeRecord.setEmployeeId(empId); 
        					employeeRecord.setTime(ExDateUtils.getCurrentStringDateTime());
        					employeeRecord.setContent("修改员工："+str+",生效时间："+time);
        					employeeRecord.setProcessoer(user.getEmployeeName());
        					employeeRecordMapper.insertEmployeeRecord(employeeRecord);    						
    					}
    				}else{
    					ImportErrorRecord errorRecord = new ImportErrorRecord();
    					errorRecord.setLineNo(result.get(i).get("lineNo"));
    					errorRecord.setErrorContent("通过姓名和身份证号查询不到员工");
    					errorRecordList.add(errorRecord);				 
    					continue;
    				}
    			}
    		}		 
    	} catch (Exception e) {
    		logger.error("插入或更新员工工作经历报错:",e.getMessage(),e);
    	}
    	return errorRecordList;
    }
    /**
     * 增加员工的项目经历
     * @param result 一个sheet页
     * @param sheetName sheet页名称
     */
    @Transactional 
    public List<ImportErrorRecord>  addEmpProjectExper(ArrayList<Map<String, String>> result) {
    	String name = ""; //姓名
    	String personalId = ""; //身份证ID 
    	List<ImportErrorRecord> errorRecordList = new ArrayList<>();
		//根据sheet标签名分别调用不同的方法
    	try {
			 for(int i=0;i<result.size();i++){
				 if(result.get(i) == null){//空判断
					continue;
				 }else{
					 if(result.get(i).containsKey("name")){
						 name =  String.valueOf(result.get(i).get("name"));
					 }else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("姓名为空");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
					 if(result.get(i).containsKey("personalId")){
						 personalId =  String.valueOf(result.get(i).get("personalId"));
					 }else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("身份证号为空");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
					 //通过员工 姓名和身份证查找员工ID
					 Map<String, Object> param = new HashMap<>();
					 param.put("idCardNumber", personalId);
					 param.put("name", name);
					 logger.info("项目经验---通过员工 姓名和身份证查找员工ID参数name={},idCardNumber={}",name,personalId);
					Integer empId =  employeeMapper.findEmpIdByIdCardNumAndName(param);
					 logger.info("项目经验-----通过员工 姓名和身份证查找员工ID={}",empId);
					if(empId != null){
						ErpProjectExperience  projExperience = new ErpProjectExperience();
						projExperience.setEmployeeId(empId);
						projExperience.setStartTime(result.get(i).get("startTime"));
						projExperience.setEndTime(result.get(i).get("endTime"));
						projExperience.setProjectName(result.get(i).get("projectName")); //项目名称
						projExperience.setPost(result.get(i).get("post")); //项目中职务
						projExperience.setDescription(result.get(i).get("description")); //项目描述
						projExperience.setResponsibility(result.get(i).get("responsibility")); //责任职责
						 //通过员工Id查找所有项目经历
						 List<Map<String, Object>> listProjExp =  employeeMapper.findAllProjectExperienceByEmp(empId);
						 if(listProjExp.isEmpty()|| listProjExp==null){//增加数据
							 employeeMapper.addProjectExperienceByEmp(projExperience);
						 }else{//开始时间、结束时间进行数据重复校验  ,进行更新
						  Integer n	= employeeMapper.findProjExperByEmpIdStartEnd(empId,result.get(i).get("startTime"),result.get(i).get("endTime"));
						  if(n != null){
							  projExperience.setId(n);
							  employeeMapper.updateProjectExperienceByEmp(projExperience); //更新已经存在的项目经历
						  }else{//增加项目经历
							  employeeMapper.addProjectExperienceByEmp(projExperience);
						  }
						 } 
					}else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("通过姓名和身份证号查询不到员工");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
				 }
			 }	 
    	} catch (Exception e) {
			logger.error("插入或更新员工项目经历:",e.getMessage(),e);
		}
    	return errorRecordList;
	}
    /**
     * 增加员工的技术特长
     * @param result 一个sheet页
     * @param sheetName sheet页名称
     */
    @Transactional 
    public List<ImportErrorRecord>  addEmpTechnical(ArrayList<Map<String, String>> result) {
    	String name = ""; //姓名
    	String personalId = ""; //身份证ID 
    	List<ImportErrorRecord> errorRecordList = new ArrayList<>();
		//根据sheet标签名分别调用不同的方法
    	try {
			 for(int i=0;i<result.size();i++){
				 if(result.get(i) == null){//空判断
					continue;
				 }else{
					 if(result.get(i).containsKey("name")){
						 name =  String.valueOf(result.get(i).get("name"));
					 }else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("姓名为空");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
					 if(result.get(i).containsKey("personalId")){
						 personalId =  String.valueOf(result.get(i).get("personalId"));
					 }else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("身份证号为空");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
					 //通过员工 姓名和身份证查找员工ID
					 Map<String, Object> param = new HashMap<>();
					 param.put("idCardNumber", personalId);
					 param.put("name", name);
					 logger.info("技术特长---通过员工 姓名和身份证查找员工ID参数name={},idCardNumber={}",name,personalId);
					 Integer empId =  employeeMapper.findEmpIdByIdCardNumAndName(param);
					 logger.info("技术特长-----通过员工 姓名和身份证查找员工ID={}",empId);
					if(empId != null){
						ErpTechnicaExpertise  technicaExpertise = new ErpTechnicaExpertise();
						technicaExpertise.setEmployeeId(empId);
						technicaExpertise.setTechnicalName(result.get(i).get("technicalName")); //技能名称
						technicaExpertise.setQualification(result.get(i).get("qualification")); //熟练程度
						 //通过员工Id查找所有技术特长
						 List<Map<String, Object>> listProjExp =  employeeMapper.findAllTechnicaExpertiseByEmp(empId);
						 if(listProjExp.isEmpty()|| listProjExp==null){//增加数据
							 employeeMapper.addTechnicaExpertiseByEmp(technicaExpertise);
						 }else{//技能名称进行数据重复校验  ,进行更新
						  Integer n	= employeeMapper.findTechnicaExperByEmpIdTechnicalName(empId,result.get(i).get("technicalName"));
						  if(n != null){
							  technicaExpertise.setId(n);
							  employeeMapper.updateTechnicaExpertiseByEmp(technicaExpertise); //更新已经存在的技术特长
						  }else{//增加技术特长
							  employeeMapper.addTechnicaExpertiseByEmp(technicaExpertise);
						  }
						 }
					}else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("通过姓名和身份证号查询不到员工");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }				
				 }
			 }	 
    	} catch (Exception e) {
			logger.error("插入或更新员工技术特长报错:",e.getMessage(),e);
		}
    	return errorRecordList;
	}
    /**
     * 增加员工的证书
     * @param result 一个sheet页
     * @param sheetName sheet页名称
     */
    @Transactional 
    public List<ImportErrorRecord>  addEmpCertificate(ArrayList<Map<String, String>> result) {
    	String name = ""; //姓名
    	String personalId = ""; //身份证ID 
    	List<ImportErrorRecord> errorRecordList = new ArrayList<>();
		//根据sheet标签名分别调用不同的方法
    	try {
			 for(int i=0;i<result.size();i++){
				 if(result.get(i) == null){//空判断
					continue;
				 }else{
					 if(result.get(i).containsKey("name")){
						 name =  String.valueOf(result.get(i).get("name"));
					 }else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("姓名为空");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
					 if(result.get(i).containsKey("personalId")){
						 personalId =  String.valueOf(result.get(i).get("personalId"));
					 }else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("身份证号为空");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
					 //通过员工 姓名和身份证查找员工ID
					 Map<String, Object> param = new HashMap<>();
					 param.put("idCardNumber", personalId);
					 param.put("name", name);
					 logger.info("证书---通过员工 姓名和身份证查找员工ID参数name={},idCardNumber={}",name,personalId);
					Integer empId =  employeeMapper.findEmpIdByIdCardNumAndName(param);
					 logger.info("证书-----通过员工 姓名和身份证查找员工ID={}",empId);
					if(empId != null){
						ErpCertificate erpCertificate = new ErpCertificate();
						erpCertificate.setEmployeeId(empId);
						erpCertificate.setTime(result.get(i).get("time")); //技获得日期
						erpCertificate.setCertificateName(result.get(i).get("certificateName")); //证书名称
						erpCertificate.setLevel(result.get(i).get("level")); //等级
						erpCertificate.setOrganization(result.get(i).get("organization"));
						erpCertificate.setDescription(result.get(i).get("description"));
						erpCertificate.setCategory(result.get(i).get("category"));
						erpCertificate.setFilename(result.get(i).get("filename"));
						 //通过员工Id查找所有证书
						 List<Map<String, Object>> listCertificate =  employeeMapper.findAllCertificateByEmp(empId);
						 if(listCertificate.isEmpty()|| listCertificate==null){//增加数据
							 employeeMapper.addCertificateByEmp(erpCertificate);
						 }else{//证书名称和发证时间进行数据重复校验  ,进行更新
							 logger.info("更新证书empId={},证书名称={},发证日期={}",empId,result.get(i).get("certificateName"),result.get(i).get("time"));
							 Integer n	= employeeMapper.findRepeatCertificateByEmpIdCertifName(empId,result.get(i).get("certificateName"),result.get(i).get("time"));
							 if(n != null){
								 erpCertificate.setId(n);
								 employeeMapper.updateCertificateByEmp(erpCertificate); //更新已经存在的证书
							 }else{//增加证书
								 employeeMapper.addCertificateByEmp(erpCertificate);
							 }
						 }
					}else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("通过姓名和身份证号查询不到员工");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }					
				 }
			 }		 
    	} catch (Exception e) {
			logger.error("插入或更新员工证书报错:",e.getMessage(),e);
		}
    	return errorRecordList;
	}
    
    /**
     * 增加员工的教育经历
     * @param result 一个sheet页
     * @param sheetName sheet页名称
     */
    @Transactional 
    public List<ImportErrorRecord> addEmpEduExper(ArrayList<Map<String, String>> result) {
    	String name = ""; //姓名
    	String personalId = ""; //身份证ID 
    	List<ImportErrorRecord> errorRecordList = new ArrayList<>();
		//根据sheet标签名分别调用不同的方法
    	try {
			 for(int i=0;i<result.size();i++){
				 if(result.get(i) == null){//空判断
					continue;
				 }else{
					 if(result.get(i).containsKey("name")){
						 name =  String.valueOf(result.get(i).get("name"));
					 }else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("姓名为空");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
					 if(result.get(i).containsKey("personalId")){
						 personalId =  String.valueOf(result.get(i).get("personalId"));
					 }else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("身份证号为空");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
					 //通过员工 姓名和身份证查找员工ID
					 Map<String, Object> param = new HashMap<>();
					 param.put("idCardNumber", personalId);
					 param.put("name", name);
					Integer empId =  employeeMapper.findEmpIdByIdCardNumAndName(param);
					if(empId != null){
						 ErpEducationExperience educationExperience = new ErpEducationExperience();
						 educationExperience.setEmployeeId(empId);
						 educationExperience.setStartTime(result.get(i).get("startTime"));
						 educationExperience.setEndTime(result.get(i).get("endTime"));
						 educationExperience.setDegree(result.get(i).get("degree"));
						 educationExperience.setSchool(result.get(i).get("school"));
						 educationExperience.setMajor(result.get(i).get("major"));
						 //通过员工Id和
						 List<Map<String, Object>> listEdu =  employeeMapper.findAllEducationByEmp(empId);
						 if(listEdu.isEmpty()|| listEdu==null){//增加数据
							 employeeMapper.addEducationByEmp(educationExperience);
						 }else{//开始时间、结束时间进行数据重复校验  ,进行更新
							 logger.info("员工Id={},教育开始时间={},教育结束时间={}",empId,result.get(i).get("startTime"),result.get(i).get("endTime"));
						  Integer n	= employeeMapper.findEduByEmpIdStartEnd(empId,result.get(i).get("startTime"),result.get(i).get("endTime"));
						  if(n != null){
							  educationExperience.setId(n);
							  employeeMapper.updateEducationByEmp(educationExperience); //更新已经存在的教育经历
						  }else{//增加教育经历
							  employeeMapper.addEducationByEmp(educationExperience);
						  }
						 }	 
					}else{
						 ImportErrorRecord errorRecord = new ImportErrorRecord();
						 errorRecord.setLineNo(result.get(i).get("lineNo"));
						 errorRecord.setErrorContent("通过姓名和身份证号查询不到员工");
						 errorRecordList.add(errorRecord);				 
						 continue;
					 }
				 }
			 }
    	} catch (Exception e) {
			logger.error("插入或更新员工教育经验报错:",e.getMessage(),e);
		}
    	return errorRecordList;
	}
 
	/**
     * 读取excel文件
     * @param wb
     * @param sheetIndex sheet页下标：从0开始
     * @param startReadLine 开始读取的行:从0开始
     * @param tailLine 去除最后读取的行
     */
    public ArrayList<Map<String,String>> readExcel(Workbook wb,int sheetIndex, int startReadLine, int tailLine) {
    
        Sheet sheet = wb.getSheetAt(sheetIndex);
        String sheetName = sheet.getSheetName();
    	logger.info("进入readExcel方法读取第{}个sheet页,sheet页的名称为{}",sheetIndex,sheet.getSheetName());
        Row row = null;
        ArrayList<Map<String,String>> result = new ArrayList<Map<String,String>>();
        
        Integer rowNum = sheet.getLastRowNum();
        for(int i=startReadLine; i<sheet.getLastRowNum()-tailLine+1; i++) {

            row = sheet.getRow(i);
            if(row != null && row.getPhysicalNumberOfCells() > 0){
            	 Map<String,String> map = new HashMap<String,String>();
                for(Cell c : row) {
                	if(c.getCellType()==Cell.CELL_TYPE_BLANK || c== null){
                		continue;
                		}
                    String returnStr = "";
                    //判断指定的单元格是否是合并单元格 参数(行下标,列下标)
                    boolean isMerge = isMergedRegion(sheet, i, c.getColumnIndex());
                    //判断是否具有合并单元格
                    if(isMerge) {//获取合并单元格的值
                        String rs = getMergedRegionValue(sheet, row.getRowNum(), c.getColumnIndex());
                        returnStr = rs;
                    }else {                   	
                        returnStr = getCellValue(c);
                    }
                    int n = c.getColumnIndex();
                    logger.info("读取第{}行,第{}列的单元格:",i,n);
   
                    map.put("lineNo", String.valueOf(i));
                    if(sheetName.contains("教育经历")){
                    	 if(c.getColumnIndex()==0){
                          	  map.put("name",returnStr); //姓名
                            }else if(c.getColumnIndex()==1){
                          	  map.put("personalId",returnStr);//身份证号码
                            }else if(c.getColumnIndex()==2){
                          	  map.put("startTime",returnStr); //开始时间
                            }else if(c.getColumnIndex()==3){
                          	  map.put("endTime",returnStr); //结束时间
                            }else if(c.getColumnIndex()==4){
                          	  map.put("school",returnStr); //学校
                            }else if(c.getColumnIndex()==5){
                          	  map.put("major",returnStr); //专业
                            }else if(c.getColumnIndex()==6){
                            	  map.put("degree",returnStr); //学历
                              }
                    }else if(sheetName.contains("工作经历")){//读取工作经历
                    	 if(c.getColumnIndex()==0){
                       	  map.put("name",returnStr); //姓名
                         }else if(c.getColumnIndex()==1){
                       	  map.put("personalId",returnStr);//身份证号码
                         }else if(c.getColumnIndex()==2){
                       	  map.put("startTime",returnStr); //开始时间
                         }else if(c.getColumnIndex()==3){
                       	  map.put("endTime",returnStr); //结束时间
                         }else if(c.getColumnIndex()==4){
                       	  map.put("company",returnStr); //公司
                         }else if(c.getColumnIndex()==5){
                       	  map.put("position",returnStr); //职务
                         }
                    }else if(sheetName.contains("项目经历")){//读取项目经验
                    	 if(c.getColumnIndex()==0){
                          	  map.put("name",returnStr); //姓名
                            }else if(c.getColumnIndex()==1){
                          	  map.put("personalId",returnStr);//身份证号码
                            }else if(c.getColumnIndex()==2){
                          	  map.put("startTime",returnStr); //开始时间
                            }else if(c.getColumnIndex()==3){
                          	  map.put("endTime",returnStr); //结束时间
                            }else if(c.getColumnIndex()==4){
                          	  map.put("projectName",returnStr); //项目名称
                            }else if(c.getColumnIndex()==5){
                          	  map.put("post",returnStr); //项目中的职务
                            }else if(c.getColumnIndex()==6){
                            	  map.put("description",returnStr); //项目描述
                              }else if(c.getColumnIndex()==7){
                            	  map.put("responsibility",returnStr); //责任描述
                              }          
                    }else if(sheetName.contains("技术特长")){//读取技术特长
                    	 if(c.getColumnIndex()==0){
                         	  map.put("name",returnStr); //姓名
                           }else if(c.getColumnIndex()==1){
                         	  map.put("personalId",returnStr);//身份证号码
                           }else if(c.getColumnIndex()==2){
                         	  map.put("technicalName",returnStr); //技能名称
                           }else if(c.getColumnIndex()==3){
                         	  map.put("qualification",returnStr); //熟练程度
                           }
                    }else if(sheetName.contains("证书")){//读取证书
                    	 if(c.getColumnIndex()==0){
                        	  map.put("name",returnStr); //姓名
                          }else if(c.getColumnIndex()==1){
                        	  map.put("personalId",returnStr);//身份证号码
                          }else if(c.getColumnIndex()==2){
                        	  map.put("time",returnStr); //获得日期
                          }else if(c.getColumnIndex()==3){
                        	  map.put("certificateName",returnStr); //证书名称
                          }else if(c.getColumnIndex()==4){
                        	  map.put("level",returnStr); //证书等级
                          }else if(c.getColumnIndex()==5){
                        	  map.put("organization",returnStr); //发证机构
                          }else if(c.getColumnIndex()==6){
                        	  map.put("description",returnStr); //证书描述
                          }else if(c.getColumnIndex()==7){
                        	  map.put("category",returnStr); //证书分类
                          }else if(c.getColumnIndex()==8){
                        	  map.put("filename",returnStr); //证书文件名
                          }
                    }else if(sheetName.contains("部门职位调整")){//读取工作经历
                   	 if(c.getColumnIndex()==0){
                      	  map.put("name",returnStr); //姓名
                        }else if(c.getColumnIndex()==1){
                      	  map.put("personalId",returnStr);//身份证号码
                        }else if(c.getColumnIndex()==2){
                      	  map.put("firstDepartmentName",returnStr); //一级部门
                        }else if(c.getColumnIndex()==3){
                      	  map.put("secondDepartmentName",returnStr); //二级部门
                        }else if(c.getColumnIndex()==4){
                          map.put("positionName",returnStr); //职位名称
                        }else if(c.getColumnIndex()==5){
                          map.put("rank",returnStr); //职级
                        }else if(c.getColumnIndex()==6){
                          map.put("time",returnStr); //生效时间
	                    }
                   }else {//读取员工信息
                         if(c.getColumnIndex()==0){
                       	  map.put("name",returnStr);//姓名
                         }else if(c.getColumnIndex()==1){
                       	  map.put("socialSecurity",returnStr); //社保地
                         }else if(c.getColumnIndex()==2){
                       	  map.put("sex",returnStr); //性别
                         }else if(c.getColumnIndex()==3){
                       	  map.put("firstDepartmentName",returnStr); //一级部门名称
                         }else if(c.getColumnIndex()==4){
                       	  map.put("secondDepartmentName",returnStr); //二级部门名称
                         }else if(c.getColumnIndex()==5){
                       	  map.put("positionName",returnStr); //职位名称
                         }else if(c.getColumnIndex()==6){
                       	  map.put("rank",returnStr); //职级
                         }else if(c.getColumnIndex()==7){
                          map.put("status",returnStr); //员工状态
                         }else if(c.getColumnIndex()==8){
	                    	map.put("entryTime",returnStr); //入职时间
	                    }else if(c.getColumnIndex()==9){
	                    	map.put("contractBeginTime",returnStr); //合同开始时间
	                    }else if(c.getColumnIndex()==10){
	                    	map.put("probationEndTime",returnStr); //试用期结束时间
	                    }else if(c.getColumnIndex()==11){
	                    	map.put("endTime",returnStr); //合同结束时间 
	                    }else if(c.getColumnIndex()==12){
	                    	map.put("salaryCardNumber",returnStr); //薪资卡号
	                    }else if(c.getColumnIndex()==13){
	                    	map.put("phone",returnStr); //手机号
	                    }else if(c.getColumnIndex()==14){
	                    	map.put("username",returnStr); //公司邮箱
	                    }else if(c.getColumnIndex()==15){
	                    	map.put("personalEmail",returnStr); //个人邮箱
	                    }else if(c.getColumnIndex()==16){
	                    	map.put("IdCardNumber",returnStr); //身份证号
	                    }else if(c.getColumnIndex()==17){
	                    	map.put("takeJobTime",returnStr); //首次参加工作时间
	                    }else if(c.getColumnIndex()==18){
	                    	map.put("school",returnStr); //毕业院校
	                    }else if(c.getColumnIndex()==19){
	                    	map.put("major",returnStr); //专业
	                    }else if(c.getColumnIndex()==20){
	                    	map.put("education",returnStr); //最高学历
	                    }else if(c.getColumnIndex()==21){
	                    	map.put("leaveDate",returnStr); //最后发薪日
	                    }else if(c.getColumnIndex()==22){
	                    	map.put("dealDate",returnStr); //办理手续时间
	                    }else if(c.getColumnIndex()==23){
	                    	map.put("leaveReason",returnStr); //离职原因
	                    }
                    }                 
                }
                
                if (map.size() != 0){
                	result.add(map);
                }
                
            }
 
           
//            System.out.println();

        }
        return result;

    }

    /**
     * 获取合并单元格的值
     * @param sheet
     * @param row  行下标
     * @param column  列下标
     * @return
     */
    public String getMergedRegionValue(Sheet sheet ,int row , int column){
        int sheetMergeCount = sheet.getNumMergedRegions();

        for(int i = 0 ; i < sheetMergeCount ; i++){
        	CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();

            if(row >= firstRow && row <= lastRow){

                if(column >= firstColumn && column <= lastColumn){
                    Row fRow = sheet.getRow(firstRow);
                    Cell fCell = fRow.getCell(firstColumn);
                    return getCellValue(fCell) ;
                }
            }
        }

        return null ;
    }

    /**
     * 判断合并了行
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    private boolean isMergedRow(Sheet sheet,int row ,int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if(row == firstRow && row == lastRow){
                if(column >= firstColumn && column <= lastColumn){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断指定的单元格是否是合并单元格
     * @param sheet
     * @param row 行下标
     * @param column 列下标
     * @return
     */
    private boolean isMergedRegion(Sheet sheet,int row ,int column) {
        int sheetMergeCount  = sheet.getNumMergedRegions();//获取sheet中合并的单元格数量
        for (int i = 0; i < sheetMergeCount; i++) {
        	//合并单元格
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if(row >= firstRow && row <= lastRow){
                if(column >= firstColumn && column <= lastColumn){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断sheet页中是否含有合并单元格
     * @param sheet
     * @return
     */
    private boolean hasMerged(Sheet sheet) {
        return sheet.getNumMergedRegions() > 0 ? true : false;
    }

    /**
     * 合并单元格
     * @param sheet
     * @param firstRow 开始行
     * @param lastRow 结束行
     * @param firstCol 开始列
     * @param lastCol 结束列
     */
    private void mergeRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

    /**
     * 获取单元格的值
     * @param cell
     * @return
     */
    public String getCellValue(Cell cell){

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
    /**
     * 从excel读取内容
     */
    public void readContent(String fileName)  {
        boolean isE2007 = false;    //判断是否是excel2007格式
        if(fileName.endsWith("xlsx"))
            isE2007 = true;
        try {
            InputStream input = new FileInputStream(fileName);  //建立输入流
            Workbook wb  = null;
            //根据文件格式(2003或者2007)来初始化
            if(isE2007)
                wb = new XSSFWorkbook(input);
            else
                wb = new HSSFWorkbook(input);
            Sheet sheet = wb.getSheetAt(0);     //获得第一个表单
            Iterator<Row> rows = sheet.rowIterator(); //获得第一个表单的迭代器
            while (rows.hasNext()) {
                Row row = rows.next();  //获得行数据
                System.out.println("Row #" + row.getRowNum());  //获得行号从0开始
                Iterator<Cell> cells = row.cellIterator();    //获得第一行的迭代器
                while (cells.hasNext()) {
                    Cell cell = cells.next();
                    System.out.println("Cell #" + cell.getColumnIndex());
                    switch (cell.getCellType()) {   //根据cell中的类型来输出数据
                        case HSSFCell.CELL_TYPE_NUMERIC:
                            System.out.println(cell.getNumericCellValue());
                            break;
                        case HSSFCell.CELL_TYPE_STRING:
                            System.out.println(cell.getStringCellValue());
                            break;
                        case HSSFCell.CELL_TYPE_BOOLEAN:
                            System.out.println(cell.getBooleanCellValue());
                            break;
                        case HSSFCell.CELL_TYPE_FORMULA:
                            System.out.println(cell.getCellFormula());
                            break;
                        default:
                            System.out.println("unsuported sell type======="+cell.getCellType());
                            break;
                    }
                }
            }
        } catch (IOException ex) {
        	logger.error("readContent出现异常："+ex.getMessage(),ex);
        }
    }		
	
}

