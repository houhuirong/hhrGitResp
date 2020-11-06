package com.nantian.erp.hr.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.AdminDicMapper;
import com.nantian.erp.hr.data.dao.ErpDepartmentMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeEntryMapper;
import com.nantian.erp.hr.data.dao.ErpEmployeeMapper;
import com.nantian.erp.hr.data.dao.ErpOfferMapper;
import com.nantian.erp.hr.data.dao.ErpPositionRankRelationMapper;
import com.nantian.erp.hr.data.dao.ErpPostMapper;
import com.nantian.erp.hr.data.dao.ErpResumeMapper;
import com.nantian.erp.hr.data.dao.ErpResumePostMapper;
import com.nantian.erp.hr.data.dao.ErpResumePostOrderMapper;
import com.nantian.erp.hr.data.dao.PositionApplyProgressMapper;
import com.nantian.erp.hr.data.dao.PositionOperReordMapper;
import com.nantian.erp.hr.data.dao.PostDutyMapper;
import com.nantian.erp.hr.data.dao.PostRequireMapper;
import com.nantian.erp.hr.data.dao.PostTemplateMapper;
import com.nantian.erp.hr.data.model.AdminDic;
import com.nantian.erp.hr.data.model.ErpDepartment;
import com.nantian.erp.hr.data.model.ErpPositionRankRelation;
import com.nantian.erp.hr.data.model.ErpPost;
import com.nantian.erp.hr.data.model.ErpResume;
import com.nantian.erp.hr.data.model.ErpResumePost;
import com.nantian.erp.hr.data.model.ErpResumePostOrder;
import com.nantian.erp.hr.data.model.ErpResumePostReexam;
import com.nantian.erp.hr.data.model.PositionApplyProgress;
import com.nantian.erp.hr.data.model.PositionOperRecond;
import com.nantian.erp.hr.data.model.PostDuty;
import com.nantian.erp.hr.data.model.PostRequire;
import com.nantian.erp.hr.data.model.PostTemplate;
import com.nantian.erp.hr.data.vo.ErpPositionQueryParamVO;
import com.nantian.erp.hr.data.vo.ErpPositionQueryResultVO;
import com.nantian.erp.hr.util.FileUtils;
import com.nantian.erp.hr.util.RestTemplateUtils;
import com.nantian.erp.hr.util.WordUtil;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@PropertySource(value= {"classpath:config/sftp.properties","file:${spring.profiles.path}/config/sftp.properties","classpath:config/email.properties","file:${spring.profiles.path}/config/email.properties","classpath:config/host.properties","file:${spring.profiles.path}/config/host.properties"},ignoreResourceNotFound = true)

public class ErpPostApplyService {
	@Value("${protocol.type}")
	private String protocolType;//http或https
	/*
	 * 从配置文件中获取SFTP相关属性
	 */
    @Value("${sftp.basePath}")
    private String basePath;//服务器基本路径
    @Value("${sftp.postPath}")
    private String postPath;//岗位信息文件路径
	
	/*
	 * 从配置文件中获取Email相关属性
	 */
	@Value("${email.service.host}")
	private  String emailServiceHost;//邮件服务的IP地址和端口号
	@Value("${environment.type}")
	private  String environmentType;//环境类型（根据该标识，决定邮件的发送人、抄送人、收件人）
	@Value("${test.email.frommail}")
	private String testEmailFrommail;//测试环境发件人
	@Value("${test.email.bcc}")
	private String testEmailBcc;//测试环境抄送人
	@Value("${test.email.tomail}")
	private String testEmailTomail;//测试环境收件人
	@Value("${prod.email.interview.bcc}")
	private String prodEmailInterviewBcc;//生产环境抄送人
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ErpPostMapper postMapper;
	@Autowired
	private ErpResumeMapper resumeMapper;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private RestTemplateUtils restTemplateUtils;
	@Autowired
	private ErpEmployeeMapper employeeMapper;
	@Autowired
	AdminDicMapper adminDicMapper;
	@Autowired
	PostTemplateMapper postTemplateMapper;
	@Autowired
	PostDutyMapper  postDutyMapper;
	@Autowired
	PostRequireMapper  postRequireMapper;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	PositionApplyProgressMapper  applyProgressMapper;
	@Autowired
	ErpDepartmentMapper departmentMapper;
	@Autowired
	PositionOperReordMapper operRecordMapper;
	@Autowired 
	ErpResumePostMapper  resumePostMapper;
	@Autowired
	ErpResumePostOrderMapper  resumePostOrderMapper;
	@Autowired
	ErpOfferMapper offerMapper;
	@Autowired
	ErpEmployeeEntryMapper employeeEntryMapper;
	@Autowired
	private ErpInterviewService interviewService;
	@Autowired
	private WordUtil wordUtil;
	@Autowired
	private FileUtils fileUtils;
	@Autowired
	private ErpPositionRankRelationMapper positionRankRelationMapper;
	
	/**
	 * 获取岗位申请信息
	 * @param id 用户主键
	 * @return
	 */
	public RestResponse getPostApplyInfo(Integer id,Integer firstDepid) {
		logger.info("进入getPostApplyInfo方法 参数:id = "+id);
		String firstDepName = ""; //一级部门名称
		Integer firstDepartmentId = null; //一级部门Id
		Integer secondDepartmentId = null; //一级部门Id
		String seconfDepName = ""; //二级部门名称
		String empName = "";//员工姓名
		try {
			
		
		request.getHeader("token");
		List<Map<String, Object>> approvePersonBoss = new ArrayList(); // 当申请人二级部门为“本部”时 审批人 返回魏总何总

		Map<String, Object>  employeeMap = null;
		//查找员工姓名
		if(id != null){//根据员工Id查找员工信息
			employeeMap =employeeMapper.selectByEmployeeIdForlx(id);
		}
		if(employeeMap.containsKey("firstDepartment")){
			if(null != employeeMap.get("firstDepartment"))
			 firstDepName = String.valueOf(employeeMap.get("firstDepartment"));
		}
		if(employeeMap.containsKey("firstDepartmentId")){//firstDepartmentId,e.secondDepartment as secondDepartmentId
			if(null != employeeMap.get("firstDepartmentId"))
				firstDepartmentId = Integer.valueOf(employeeMap.get("firstDepartmentId").toString());
		}
		if(employeeMap.containsKey("secondDepartment")){
			if(null != employeeMap.get("secondDepartment"))
			seconfDepName = String.valueOf(employeeMap.get("secondDepartment"));
		}
		if(employeeMap.containsKey("secondDepartmentId")){
			if(null != employeeMap.get("secondDepartmentId"))
				secondDepartmentId = Integer.valueOf(String.valueOf(employeeMap.get("secondDepartmentId")));
		}
		if(employeeMap.containsKey("name")){
			if(null != employeeMap.get("name"))
				empName = String.valueOf(employeeMap.get("name"));
		}
		//根据一级部门Id查找一级部门经理Id
		Map<String, Object>  manager = null;
		if(firstDepid != null){
			 manager = employeeMapper.selectManagerByFirstDepartment(firstDepid); //根据需求，通过传入参数一级部门ID查找一级部门经理员工id
		}else{//如果一级部门Id参数为空，则根据当前申请人查找他一级部门Id 
			 manager = employeeMapper.selectManagerByFirstDepartment(firstDepartmentId);
		}
		Integer uId = null;
		
		if(manager.get("userId") == null){
			uId = 0;
		}else{
			 uId = Integer.valueOf(manager.get("userId").toString());//一级部门经理Id
		}
		
		Integer approveId = null;
		
		if(id.equals(uId)){//当前登录人是一级部门经理
			if(manager.containsKey("superLeader")){//有上级领导
				//获得审批人 Id			
				approveId =	Integer.valueOf(String.valueOf(manager.get("superLeader"))) ; 
			}else{
				//查找字典中的审批人(魏总/何总)
				List<AdminDic> listApp = adminDicMapper.findApprove(DicConstants.APPROVE);
				approveId = Integer.valueOf(listApp.get(0).getDicCode());
			}
		}
		else
		{
			approveId = uId;
		}		
		
		Map<String, Object> ermInfoMap =  employeeMapper.selectByEmployeeIdForlx(approveId);
		
		Map<String, Object> approve = new HashMap<>();
		approve.put("approveId", approveId); //用户主键Id
		if(ermInfoMap != null && ermInfoMap.containsKey("name")){
			approve.put("approveName", String.valueOf(ermInfoMap.get("name"))); //员工姓名
		}else{
			logger.error("employeeMapper.selectByEmployeeIdForlx(approveId)方法没查到员工姓名:{approveId=}"+approveId);
		}
			
		approvePersonBoss.add(approve); //emp.name,emp.employeeId
		
		//返回前端数据
		Map<String, Object> result = new TreeMap<>();
		result.put("firstDepartmentId", firstDepartmentId);
		result.put("firstDepName", firstDepName);
		result.put("secondDepartmentId", secondDepartmentId);
		result.put("secondDepName", seconfDepName);
		result.put("approvePersonBoss", approvePersonBoss); //审批人姓名/id
		result.put("id", id); //申请人Id
		result.put("name", empName); //申请人姓名
		
		return RestUtils.returnSuccess(result);
		} catch (Exception e) {
			logger.error("getPostApplyInfo()方法异常",e.getMessage(),e);
			return RestUtils.returnFailure("getPostApplyInfo()方法异常"+e.getMessage());
		}
	}
	
	/**
	 * 从字典获取所有的岗位类别  字典类型 :POST_CATEGORY
	 * @return RestResponse
	 */
	public RestResponse findAllPositionCategory(){
		List<Map<String, Object>> list = null;
		try {//select dic_code as "categoryId",dic_name as "categoryName"
			  list = adminDicMapper.findAllCategoryFromAdminDic(DicConstants.POST_CATEGORY);
		} catch (Exception e) {
			logger.error("findAllPositionCategory方法出现异常：" + e.getMessage());
		return	RestUtils.returnFailure("findAllPositionCategory方法出现异常：" + e.getMessage());
		}
		
		return  RestUtils.returnSuccess(list);
	}
	
	/**
	 * 通过岗位类别查询岗位名称
	 * @param 岗位类别码值
	 */
	public RestResponse findPositionName(String categoryId){
		List<String>  postNames = null;
		try {//select dic_code as "categoryId",dic_name as "categoryName"
			  postNames= postTemplateMapper.findPositionName(categoryId);
		} catch (Exception e) {
			logger.error("findPositionName方法出现异常：" + e.getMessage());
		 return	RestUtils.returnFailure("findPositionName方法出现异常：" + e.getMessage());
		}
		
		return  RestUtils.returnSuccess(postNames);
	}
	
	/**
	 * 根据岗位类别和岗位名称 定位 岗位职责,岗位要求
	 * @param categoryId  职位类别
	 * @param postName  岗位名称
	 * @return
	 */
	public RestResponse findPositionDutyAndRequire(String categoryId,String postName){
		logger.info("进入findPositionDutyAndRequire()方法参数categoryId="+categoryId+" postName = "+postName);
		Map<String, Object> param = null;
		param = new HashMap<>();
		param.put("category", categoryId);
		param.put("postName", postName);
		PostTemplate  postTemplate = null;
		Map<String, Object> resultVo = new HashMap<>(); //返回前端的岗位职责和岗位要求
		List<Map<String, Object>> positionRankList = null;
		try {//select dic_code as "categoryId",dic_name as "categoryName"
			//根据岗位类别和岗位名称 查找岗位模板
			postTemplate = postTemplateMapper.findPostTemplateIdByCatPostName(categoryId,postName);
			//获取职位职级列表
			positionRankList = postTemplateMapper.findPositionRankListByCatPostName(categoryId,postName);

			if(postTemplate != null){
				resultVo.put("postduty", postTemplate.getDuty());
				resultVo.put("postRequire", postTemplate.getRequired());
				resultVo.put("salary", postTemplate.getSalaryRange());
				resultVo.put("positionRankList", positionRankList);
			}
			
		} catch (Exception e) {
			logger.error("findPositionDutyAndRequire方法出现异常：" + e);
			return  RestUtils.returnFailure("findPositionDutyAndRequire方法出现异常：" + e.getMessage());
		}
		
		return  RestUtils.returnSuccess(resultVo);
	}
	
	/**
	 * 添加岗位申请信息
	 * @param param
	 * @return
	 */
	@Transactional(readOnly=false, rollbackFor = Exception.class)
	public RestResponse addPostApplyInfo(Map<String, Object> param, String token) throws Exception{
		logger.info("-------------------进入addPostApplyInfo()参数------:param="+param);
		ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
		Integer erpUserId = null;
		if(erpUser != null){
			erpUserId = erpUser.getUserId();
		}
		Map<String, Object> result = new HashMap<>();
		if(param == null) {
			  return RestUtils.returnFailure("参数为空");
		}
		//接口人
		if(isNullValidate(param.get("principal"))) {
			  return RestUtils.returnFailure("接口人为空");
		}
		//职位职级id
		if(isNullValidate(param.get("positionRankId"))) {
			  return RestUtils.returnFailure("职级为空");
		}
		//工作地省市
		if(isNullValidate(param.get("city"))) {
			  return RestUtils.returnFailure("工作地-省市为空");
		}
		//工作地区
		if(isNullValidate(param.get("district"))) {
			  return RestUtils.returnFailure("工作地-区为空");
		}
		//工作地县
		if(isNullValidate(param.get("county"))) {
			  return RestUtils.returnFailure("工作地-县为空");
		}
		//详细地址
		if(isNullValidate(param.get("detailAddress"))) {
			  return RestUtils.returnFailure("详细地址为空");
		}
		//申请日期
		if(isNullValidate(param.get("dateSubmit"))) {
			  return RestUtils.returnFailure("申请日期为空");
		}
		//招聘原因
		if(isNullValidate(param.get("reasonRecruit"))) {
			  return RestUtils.returnFailure("招聘原因为空");
		}
		//优先级
		if(isNullValidate(param.get("levelPriority"))) {
			  return RestUtils.returnFailure("优先级为空");
		}
		String optype = "";
		for(String key:param.keySet()){
			if(key.equals("proposerId")){ //申请人 id
				if(isNullValidate(param.get(key))){
				  return	RestUtils.returnSuccess("申请人为空");
				}
				result.put("proposerId",param.get(key));
			}
			if(key.equals("firstDepartmentId")){
				if(isNullValidate(param.get(key))){
					return	RestUtils.returnSuccess("一级部门为空");
				} 
				result.put("firstDepartmentId",param.get(key));	
			}
			if(key.equals("firstDepName")){
				if(!isNullValidate(param.get(key))){
					result.put("firstDepName",param.get(key));
				} 
				
			}
			if(key.equals("secondDepName")){
				if(!isNullValidate(param.get(key))){
					result.put("secondDepName",param.get(key));
				} 
				
			}
			if(key.equals("secondDepartmentId")){
				if(isNullValidate(param.get(key))){
					return RestUtils.returnSuccess("二级部门为空");
					
				} 
				result.put("secondDepartmentId",param.get(key));
			}
			if(key.equals("approver")){//审批人IDapprovePersonBoss
				if(isNullValidate(param.get(key))){
				  return	RestUtils.returnSuccess("审批人为空");
				} 
				result.put("approvePersonBoss",param.get(key));
			}
			if(key.equals("approverName")){//审批人姓名
				if(isNullValidate(param.get(key))){
					return	RestUtils.returnSuccess("审批人为空");
				} 
				result.put("approverName",param.get(key));
			}
			if(key.equals("categoryId")){//岗位类别码值
				if(isNullValidate(param.get(key))){
					return	RestUtils.returnSuccess("岗位类别码值为空");
				} 
				result.put("categoryId",param.get(key));
			}
			if(key.equals("postName")){//岗位名称
				if(isNullValidate(param.get(key))){
					return	RestUtils.returnSuccess("岗位名称为空");
				} 
				result.put("postName",param.get(key));
			}
//			if(key.equals("post_duties")){//岗位职责
//				if(isNullValidate(param.get(key))){
//					return	RestUtils.returnSuccess("岗位职责为空");
//				} 
//				result.put("postduty",param.get(key));
//			}
//			if(key.equals("required")){//岗位要求
//				if(isNullValidate(param.get(key))){
//					return	RestUtils.returnSuccess("岗位要求为空");
//				} 
//				result.put("postRequire",param.get(key));
//			}
			if(key.equals("other_duty")){//其他职责
				if(!isNullValidate(param.get(key))){
					result.put("otherDuty",param.get(key));
				}else{
					result.put("otherDuty","");
				}
				
			}
			if(key.equals("other_required")){//其他要求
				if(!isNullValidate(param.get(key))){
					result.put("otherRequire",param.get(key));
				}else{
					result.put("otherRequire","");
				}
				
			}
			if(key.equals("numberPeople")){//人数
				if(!isNullValidate(param.get(key))){
					result.put("numberPeople",param.get(key));
				}else{
					result.put("numberPeople",0);
				}
				
			}
			if(key.equals("salary")){//薪资范围
				if(!isNullValidate(param.get(key))){
					result.put("salary",param.get(key));
				}else{
					result.put("salary",0);
				}
				
			}
			if(key.equals("type")){//操作类别       save、submit
				if(!isNullValidate(param.get(key))){
					optype = String.valueOf(param.get(key));
				}else{
					optype = "save";
				}
			}
		
		}
		//查重校验 根据岗位名称和申请人 查找岗位申请记录
//		int isRepeatNo = postMapper.findRepeatPost(ObjToInteger(result.get("proposerId")),String.valueOf(result.get("postName")));
		//放开查重校验
		/*if(isRepeatNo > 0){
		  return	RestUtils.returnSuccessWithString("新增的岗位申请已经在岗位申请表里,请重新输入!");
		}*/
		
		Map<String, Object> findTemplateMap = new HashMap<>(); //查找岗位模板参数
		findTemplateMap.put("categoryId", result.get("categoryId"));
		findTemplateMap.put("postName", result.get("postName"));
		PostTemplate template  = postTemplateMapper.findPostTemplateIdByCatPostName(String.valueOf(result.get("categoryId")),String.valueOf(result.get("postName")));
		Integer postTemplateId = template.getPostTemplateId(); //获取岗位模板主键
		ErpPost erpPost = new ErpPost(); //岗位申请对象
		erpPost.setFirstDepartment(Integer.valueOf(result.get("firstDepartmentId").toString()));
		erpPost.setSecondDepartment(Integer.valueOf(result.get("secondDepartmentId").toString()));
		erpPost.setCategory(result.get("categoryId").toString());
		erpPost.setRequired(String.valueOf(result.get("otherRequire")));  //其他要求
		erpPost.setDuty(String.valueOf(result.get("otherDuty")));  //其他职责
		erpPost.setSalaryRange(String.valueOf(result.get("salary")));  //其他职责
		if(isNullValidate(result.get("numberPeople"))){
			erpPost.setNumberPeople(0);
		}else{
			erpPost.setNumberPeople(ObjToInteger((result.get("numberPeople"))));//招聘人数
		}
		
		erpPost.setIsClosed(0); //是否关闭 0未关闭，1关闭
		erpPost.setProposerId(erpUserId);//岗位申请人Id  存在的是用户主键
		if (optype.equals("save")){
			erpPost.setStatus(5); //岗位申请状态（5：待提交）
		}else{
			erpPost.setStatus(4); //岗位申请状态（4：待审批）
		}
		erpPost.setPostName(String.valueOf(result.get("postName")));  //岗位名称
		erpPost.setPostTemplateId(postTemplateId); //岗位模板主键
		
		erpPost.setWorkAddress(String.valueOf(param.get("detailAddress")));
		erpPost.setRecruitCycle(param.get("recruitCycle")==null?"":param.get("recruitCycle").toString());
		//接口人
		erpPost.setPrincipal(Integer.valueOf(param.get("principal").toString()));
		//审批人
		erpPost.setPrincipalLeader(Integer.valueOf(param.get("approver").toString()));
		//职位职级id
		erpPost.setPositionRankId(Integer.valueOf(param.get("positionRankId").toString()));
		//工作地-省市
		erpPost.setCity(String.valueOf(param.get("city")));
		//工作地-区
		erpPost.setDistrict(String.valueOf(param.get("district")));
		//工作地-县
		erpPost.setCounty(String.valueOf(param.get("county")));
		//详细地址
		erpPost.setDetailAddress(String.valueOf(param.get("detailAddress")));
		//申请日期
		erpPost.setDateSubmit(ExDateUtils.convertToDate(String.valueOf(param.get("dateSubmit"))));
		//招聘原因
		erpPost.setReasonRecruit(Integer.valueOf(String.valueOf(param.get("reasonRecruit"))));
		//优先级
		erpPost.setLevelPriority(Integer.valueOf(String.valueOf(param.get("levelPriority"))));
		postMapper.insertPost(erpPost);
		Integer postId = erpPost.getPostId(); //返回插入的主键ID
//		 PostDuty postDuty = null;
//		 logger.info("---批量插入岗位职责---参数:");
//		 for(int i =0 ;i<jsonArrayDuty.size();i++){
//			 postDuty = new PostDuty();
//			 jsonDuty =  (JSONObject)jsonArrayDuty.get(i);
//			postDuty.setPostdutyDescribe(jsonDuty.getString("postdutyDescribe")); //postdutyDescribe
//			postDuty.setPostId(postId);
//			postDuty.setType(2); //1 是岗位模板 2是岗位申请
//			postDutyMapper.addPostDuty(postDuty);
//		 }
//		 PostRequire postRequire = null;
//		 for(int j =0 ;j<jsonArrayRequire.size();j++){
//			 postRequire = new PostRequire();
//			 jsonRequire =  (JSONObject)jsonArrayRequire.get(j);
//			 postRequire.setPostRequireDescribe(jsonRequire.getString("postRequireDescribe"));
//			 postRequire.setPostId(postId);
//			 postRequire.setType(2); //1 是岗位模板 2是岗位申请
//			 postRequireMapper.addPostRequire(postRequire);
//		 }
			/*List<PostDuty> listDuty = new ArrayList<>(); //职责list
			for(String str:jsonDutyList){//遍历职责
				postDuty = new PostDuty();
				postDuty.setPostdutyDescribe(duty);
				postDuty.setPostId(postId);
				postDuty.setType(2); //1 是岗位模板 2是岗位申请
				listDuty.add(postDuty);
				
			}
			PostRequire postRequire = null;
			List<PostRequire> listReuire = new ArrayList<>(); //要求list
			for(String strReq:jsonRequireList){//遍历要求
				postRequire = new PostRequire();
				postRequire.setPostId(postId);
				postRequire.setPostRequireDescribe(require);
				postRequire.setType(2); //1 是岗位模板 2是岗位申请
				listReuire.add(postRequire);
			}*/
			/*logger.info("---批量插入岗位职责---参数:"+listDuty);
			//批量插入岗位职责
			postDutyMapper.addPostDutyBatch(listDuty );
			//批量插入岗位要求
			postRequireMapper.addPostRequireBatch(listReuire);*/
			/*
			 * 获取审批人用户Id
			 * 
			 */
//			Map<String,Object> approvePersonBoss = (Map<String, Object>) result.get("approvePersonBoss");
			
			//插入岗位申请流程表
			PositionApplyProgress applyProgress = new PositionApplyProgress();
			applyProgress.setPostId(postId); //岗位申请表主键
//			applyProgress.setCurrentPersonID(ObjToInteger(approvePersonBoss.get("approveId"))); //当前处理人,审批人的用户Id
			if (optype.equals("save")){
				applyProgress.setCurrentPersonID(erpUserId); //当前处理人,申请人的用户Id
			}else{
				applyProgress.setCurrentPersonID(ObjToInteger(result.get("approvePersonBoss"))); //当前处理人,审批人的用户Id
			}
			
			applyProgressMapper.addApplyProgress(applyProgress);
			
			if (optype.equals("submit")){
				//插入岗位处理记录表，只有提交处理才需要保存处理记录
				PositionOperRecond operRec = new PositionOperRecond();
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				operRec.setCreateTime(format.format(date));
				operRec.setOperContext("岗位申请");
				Integer currentPersonId = null;
	//			currentPersonId = ObjToInteger(approvePersonBoss.get("approveId"));
				currentPersonId = erpUserId; //申请人的用户ID
//				Integer empId = getUserInfo(currentPersonId);
				Map<String, Object> empMap = employeeMapper.selectByEmployeeIdForlx(currentPersonId);
				String approveName =   String .valueOf(empMap.get("name"));  
				operRec.setCurrentPersonId(currentPersonId);//当前处理人,申请人的员工Id
	//			operRec.setCurrentPersonName(String.valueOf(result.get("approveName")));  //当前处理人姓名
				operRec.setCurrentPersonName(approveName);  //当前处理人姓名
				operRec.setPostId(postId);//岗位申请表表主键
				operRecordMapper.addPositionOperReord(operRec);
			}
			
		return RestUtils.returnSuccess("新增岗位成功！");
	}
	
	private Boolean isNullValidate (Object obj){
		if(obj == null){
			return  true;
		}
		if(String.valueOf(obj).equals("null")||String.valueOf(obj).equals("")){
			return true;
		}
		return false;
	}
	
	/**
	 * 根据当前登录人查询待审批的岗位申请/审批过的、关闭的等岗位信息
	 * @param id 用户主键
	 * @return
	 */
	public RestResponse getApprovalPendingByLogin(Map<String,Object> map) {
		logger.info("进入getApprovalPendingByLogin()方法");
		try {
			String	token=	request.getHeader("token");
			ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
			Integer  ErpUserId  = null; //用户表 userID
			ErpUserId = erpUser.getUserId();
			
			//获取所有岗位信息
			List<ErpPost> listPositon = null; //岗位申请list
			listPositon = postMapper.findAllPostList(map);
			
			Integer status = 0;
			status = map.get("status") != null ? Integer.valueOf(String.valueOf(map.get("status"))) : 0;
			
			List<Map<String, Object>> listVo = new ArrayList<>();//返回前端Vo
			for(ErpPost post:listPositon){
				//查询岗位是否有当前处理人的审批记录		
				Map<String, Object> paramDic = new HashMap<>();
				Integer postId = post.getPostId();
				paramDic.put("currentPersonId", ErpUserId); 
			    paramDic.put("postId", postId);
			    
			    // 审批过的岗位--岗位状态为非1的情况
			    if (!status.equals(1)) {
					List<PositionOperRecond> positionOperRecord = operRecordMapper.findOperRecordByPostIdAndCurPerId(paramDic);
					if (positionOperRecord !=null && positionOperRecord.size() > 0){
						listVo.add(getPostInfo(post));
					}
			    }

				// 待我审批的岗--岗位状态为1的情况
			    if (status.equals(1)) {
					PositionApplyProgress  postApplyPro = applyProgressMapper.findApplyProgressByPostId(postId);
					if (postApplyPro.getCurrentPersonID().equals(ErpUserId)){
						listVo.add(getPostInfo(post));
					}
			    }
			}
			return RestUtils.returnSuccess(listVo);

		} catch (Exception e) {
			logger.error("根据当前登录人查询审批过得的岗位申请异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("根据当前登录人查询待审批的岗位申请异常:"+e.getMessage());
		}
	}
	
	/**
	 * 待我审批的岗位申请中驳回
	 * 
	 *  @param id 当前处理人用户主键
	 *  @param pastId  岗位申请主键 
	 *  @param applyEmployeeId 申请人Id
	 *  Integer id,String content,Integer postId,Integer applyEmployeeId
	 * @return 
	 */
	@Transactional
	public RestResponse rebutApply(Map<String, Object> param) {
		logger.info("进入rebutApply()方法参数："+String.valueOf(param));
		/**
		 * 输入数据处理
		 */
		Integer id = 0;
		Integer postId = 0;
		Integer applyEmployeeId = 0;
		String content = "";
		try {
			String	token=	request.getHeader("token");
			ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
			id = erpUser.getUserId(); //从缓存中获取用户ID 
			for(String str:param.keySet()){
				/*if(str.equals("id")){
					id = ObjToInteger(param.get("id")) ;
				}*/
				if(str.equals("postId")){
					postId = ObjToInteger(param.get("postId")) ;
				}
				if(str.equals("applyEmployeeId")){
					applyEmployeeId = ObjToInteger(param.get("applyEmployeeId")) ;
				}
				if(str.equals("content")){
					content = String.valueOf(param.get("content")) ;
				}
			}
			//插入岗位处理记录表
			PositionOperRecond operRec = new PositionOperRecond();
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
			operRec.setCreateTime(format.format(date));
			operRec.setOperContext("驳回原因:"+content); //处理内容
			operRec.setCurrentPersonId(id);//审批人Id
//			 Integer empId = getUserInfo(id); //根据用户主键获取员工Id
			 //根据员工Id 
			Map<String, Object> employeeMap =employeeMapper.selectByEmployeeIdForlx(id); //根据员工Id 查找员工姓名
			if(employeeMap != null &&  employeeMap.containsKey("name")){
				operRec.setCurrentPersonName(String.valueOf(employeeMap.get("name")));  
			}
			operRec.setPostId(postId);//岗位申请表表主键
			operRecordMapper.addPositionOperReord(operRec);
			//修改岗位流程表
			PositionApplyProgress  applyProgress  = applyProgressMapper.findApplyProgressByPostId(postId);
			Integer postionApplyProgId = null; //岗位流程主键
			if(applyProgress != null){
				postionApplyProgId = applyProgress.getId();
			}
			//岗位流程表，岗位申请当前处理人变更为申请人Id
			PositionApplyProgress postionapplyProgress = new PositionApplyProgress();
			postionapplyProgress.setCurrentPersonID(applyEmployeeId); //驳回处理 ，当前处理人变为变更为申请人
			postionapplyProgress.setId(postionApplyProgId);
			applyProgressMapper.updateApplyProgressById(postionapplyProgress);
			//修改岗位申请表状态 为 已关闭 (根据最新需要驳回操作不应该把状态改为关闭)
			ErpPost post = new ErpPost();
			post.setPostId(postId);
			post.setStatus(5); //待提交
			postMapper.updatePost(post);
		
		} catch (Exception e) {
			logger.error("rebutApply()方法异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("rebutApply()方法异常:"+e.getMessage());
		}
		return RestUtils.returnSuccess("ok");
	}

	/**
	 * 待我审批的岗位申请中，批准操作 update by ZhangYuWei 2019/05/29 一级部门经理岗位审批通过后，直接转到上级领导审批
	 * 岗位申请状态（1：待上级领导审批 2：发布中 3：已关闭 4:待一级部门经理审批 5:待提交 6:暂停）
	 */
	@Transactional
	public RestResponse agreeApply(Integer postId) {
		String token = request.getHeader("token");
		ErpUser erpUser = (ErpUser) this.redisTemplate.opsForValue().get(token);
		Integer id = erpUser.getUserId();
		List<Integer> roles = erpUser.getRoles();

		Map<String, Object> postMap = postMapper.findByPostIdNew(postId);
		Integer status = (Integer) postMap.get("status");// 岗位审批状态
		Integer userId = (Integer) postMap.get("userId");// 一级部门经理
		Integer superLeader = (Integer) postMap.get("superLeader");// 一级部门经理的上级领导

		// 插入岗位处理记录表
		if (status == 4 && !id.equals(superLeader)) {// 需要下一轮（一级部门经理的上级领导审批）

			// 插入岗位处理记录表
			PositionOperRecond operRec = new PositionOperRecond();
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 处理时间
			operRec.setCreateTime(format.format(date));
			operRec.setOperContext("同意,待下一轮审批"); // 处理内容
			operRec.setCurrentPersonId(id); // 当前处理人 还是登录人
			// Integer empId = getUserInfo(id); //根据用户主键获取员工Id
			Map<String, Object> employeeMap = employeeMapper.selectByEmployeeIdForlx(id); // 根据员工Id 查找员工姓名
			if (employeeMap.containsKey("name")) {
				operRec.setCurrentPersonName(String.valueOf(employeeMap.get("name")));
			}
			operRec.setPostId(postId);// 岗位申请表表主键
			operRecordMapper.addPositionOperReord(operRec);

			// 岗位申请表关联Id 和当前处理人用户编号 定位一条岗位流程记录
			Map<String, Object> paramProgress = new HashMap<>();
			paramProgress.put("postId", postId);
			paramProgress.put("currentPersonID", id);
			PositionApplyProgress applyProgress = applyProgressMapper.findApplyProgressByPidCurPerId(paramProgress);
			Integer postionApplyProgId = null; // 岗位流程主键
			if (applyProgress != null) {
				postionApplyProgId = applyProgress.getId();
			}

			// 修改岗位处理流程表
			PositionApplyProgress postionapplyProgress = new PositionApplyProgress();
			postionapplyProgress.setCurrentPersonID(superLeader); // 当前处理人为 审批人
			// postionapplyProgress.setCurrentPersonID(id); //当前处理人为 审批人
			postionapplyProgress.setId(postionApplyProgId);
			applyProgressMapper.updateApplyProgressById(postionapplyProgress);

			// 修改岗位申请表状态为发布中
			ErpPost post = new ErpPost();
			post.setPostId(postId);
			post.setStatus(1); // 上级领导审批中
			postMapper.updatePost(post);
			return RestUtils.returnSuccess("OK");
		} else {// 不需要下一轮（上级领导直接审批，或者一级部门经理和上级领导是同一个人）
			//add by hhr 20200710非副总或者总裁需要总裁审批
			if(!roles.contains(9) && !roles.contains(8)){
				//通过权限工程查询总裁的员工ID
				List<Map<String,Object>> list = restTemplateUtils.findAllUserByRoleId(token, 8);
				Map<String,Object> map = list.get(0);
				Integer nextPerson = (Integer) map.get("userId");//注意：userId是员工ID
				
				// 岗位申请表关联Id 和当前处理人用户编号 定位一条岗位流程记录
				Map<String, Object> paramProgress = new HashMap<>();
				paramProgress.put("postId", postId);
				paramProgress.put("currentPersonID", id);
				PositionApplyProgress applyProgress = applyProgressMapper.findApplyProgressByPidCurPerId(paramProgress);
				Integer postionApplyProgId = null; // 岗位流程主键
				if (applyProgress != null) {
					postionApplyProgId = applyProgress.getId();
				}
				
				//待总裁offer审批
				PositionApplyProgress postionapplyProgress = new PositionApplyProgress();
				postionapplyProgress.setCurrentPersonID(nextPerson); // 总裁为 审批人
				postionapplyProgress.setId(postionApplyProgId);
				applyProgressMapper.updateApplyProgressById(postionapplyProgress);
				return RestUtils.returnSuccessWithString("OK");
			
			}
			// 修改岗位申请表状态为发布中
			ErpPost post = new ErpPost();
			post.setPostId(postId);
			post.setStatus(2); // 发布中
			postMapper.updatePost(post);

			// 修改岗位操作记录表
			PositionOperRecond operRec = new PositionOperRecond();
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 处理时间
			operRec.setCreateTime(format.format(date));
			operRec.setOperContext("同意"); // 处理内容
			operRec.setCurrentPersonId(id);// 用户主键
			// Integer empId = getUserInfo(id); //根据用户主键获取员工Id
			Map<String, Object> employeeMap = employeeMapper.selectByEmployeeIdForlx(id); // 根据员工Id 查找员工姓名
			if (employeeMap.containsKey("name")) {
				operRec.setCurrentPersonName(String.valueOf(employeeMap.get("name")));
			}
			operRec.setPostId(postId);// 岗位申请表表主键
			operRecordMapper.addPositionOperReord(operRec);

			// add by ZhangYuWei 20190509 按照word模板生成岗位信息文件，并传到web服务器上(staring)
			String postName = String.valueOf(postMap.get("postName"));// 岗位名称

			// 根据岗位名称，查找岗位模板
			PostTemplate postTemplate = postTemplateMapper.findPostTemplateByPostName(postName);

			/* 用于组装word页面需要的数据 */
			Map<String, Object> dataMap = new HashMap<>();

			/* 组装数据 */
			// 一级部门名称
			dataMap.put("firstDepartmentName",
					postMap.get("firstDepartment") == null ? "" : postMap.get("firstDepartment"));
			// 二级部门名称
			dataMap.put("secondDepartmentName",
					postMap.get("secondDepartment") == null ? "" : postMap.get("secondDepartment"));
			// 岗位名称
			dataMap.put("postName", postName);
			// 招聘人数
			dataMap.put("numberPeople", postMap.get("numberPeople") == null ? "" : postMap.get("numberPeople"));
			// 薪资范围
			dataMap.put("salaryRange", postMap.get("salaryRange") == null ? "" : postMap.get("salaryRange"));
			
			// 工作地点
//			String address = postMap.get("workAddress") == null ? "" : String.valueOf(postMap.get("workAddress"));
			String address = postMap.get("work_address_city") == null ? "" : String.valueOf(postMap.get("work_address_city"));
			address = address + (postMap.get("work_address_district") == null ? "" : String.valueOf(postMap.get("work_address_district")));
			address = address + (postMap.get("work_address_county") == null ? "" : String.valueOf(postMap.get("work_address_county")));
			address = address + (postMap.get("detail_address") == null ? "" : String.valueOf(postMap.get("detail_address")));
			dataMap.put("workAddress", address);
			
			//招聘原因
			Integer reasonId = postMap.get("reason_recruit") == null ? 0 : Integer.valueOf(String.valueOf(postMap.get("reason_recruit")));
			String first = "";
			String second = "";
			String third = "";
			if(reasonId.equals(1)) {
				first = "■";
				second = "□";
				third = "□";
			}else if(reasonId.equals(2)) {
				first = "□";
				second = "■";
				third = "□";
			}else if(reasonId.equals(3)) {
				first = "□";
				second = "□";
				third = "■";
			}
			dataMap.put("first", first);
			dataMap.put("second", second);
			dataMap.put("third", third);
			
			// 招聘周期
			// dataMap.put("recruitCycle", postMap.get("recruitCycle") == null ? "" : postMap.get("recruitCycle"));
			
			// 岗位职责
			dataMap.put("duty", postMap.get("duty") == null ? "" : postMap.get("duty"));
			
			// 岗位要求
			dataMap.put("required", postMap.get("required") == null ? "" : postMap.get("required"));
			
			// 岗位现有人数【ERP系统记录人数】
			Integer countAllEntry = employeeEntryMapper.selectCountAllEntry(postId);
			dataMap.put("numberPeopleExist", countAllEntry == null ? "" : countAllEntry);

			String fileOnlyName = postName + "_" + System.currentTimeMillis() + ".doc"; // 文件唯一名称
			wordUtil.createFileByTemplate(dataMap, "exportPostFromDoc.ftl", DicConstants.EXPORT_POST_TEMP_PATH,
					fileOnlyName);

			/* 文件的路径 */
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			int day = calendar.get(Calendar.DATE);
			// 通过时间，在服务器动态产生路径，来保存文件
			String datePath = "/" + year + "/" + month + "/" + day + "/";
			Map<String, Object> resultMap = fileUtils.uploadFileBySFTP(DicConstants.EXPORT_POST_TEMP_PATH, fileOnlyName,
					postPath + datePath);
			Boolean isSuccess = (Boolean) resultMap.get("isSuccess");
			if (!isSuccess) {
				return RestUtils.returnSuccess("审批操作已完成，文件上传到web服务器失败！");
			}
			// add by ZhangYuWei 20190509 按照word模板生成岗位信息文件，并传到web服务器上(end)

			// add by ZhangYuWei 20190428 岗位状态变为“发布中”后，给岗位申请人、HR发邮件提醒(staring)
			String filename = (String) resultMap.get("data");
			String filepath = basePath + postPath + datePath + filename;// 附件路径
			boolean sendSuccess = this.sendEmailForAgreeApply(token, postId, filename, filepath);
			if (!sendSuccess) {
				return RestUtils.returnSuccess("审批操作已完成，但是邮件发送失败！");
			}
			// add by ZhangYuWei 20190428 岗位状态变为“发布中”后，给岗位申请人、HR发邮件提醒(end)
			return RestUtils.returnSuccess("OK");
		}
	}
	
	/**
	 * map对象值转换为 int类型
	 * @param obj
	 * @return
	 */
	public Integer ObjToInteger(Object obj){
		return Integer.valueOf(String.valueOf(obj)) ;
		
	}
	
	/**
	 * 根据当前登录人查询我申请的岗位
	 * @param map 查询条件
	 * @return
	 */
	public RestResponse getMyApplyPostionByLogin(Map<String,Object> map) {
		logger.info("进入getMyApplyPostionByLogin()方法参数{id}");
		String firstDepName = ""; //一级部门名称
		Integer firstDepartmentId = null; //一级部门Id
		Integer secondDepartmentId = null; //一级部门Id
		String seconfDepName = ""; //二级部门名称
		String empName = "";//员工姓名
		String principalEmpName = "";//接口人
		String categoryName = ""; //岗位类别名称
		String postName = ""; //岗位名称
		Integer status = 0; //岗位申请状态
		Integer numberPeople = 0; //申请人数
		String otherRequire = "";// 其他要求
		String otherDuty = "";// 其他职责
		String	token=	request.getHeader("token");
		ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
		Integer ErpUserId = null;
		if(erpUser != null){
			ErpUserId = erpUser.getUserId();
		}
List<Map<String, Object>> listVo = new ArrayList<>();//返回前端Vo
		Map<String, Object> vo = null;//返回前端Vo
		ErpDepartment  department = null;
		//根据申请人的Id查找岗位申请信息
		try {
			map.put("proposerId", ErpUserId);
		List<ErpPost> listPositon   = postMapper.findPostByProposerId(map);
			String categoryCode = ""; //岗位类别码值
			Integer postId = null;
			Map<String, Object> param = null;
			Map<String, Object> paramTemplate = null;
			Map<String, Object> employeeMap = null;
			Map<String, Object> principalEmployeeMap = null;

			PostTemplate postTemplate = null; //岗位模板
			for(ErpPost post:listPositon){//遍历岗位申请信息
				vo = new HashMap<>();//返回前端Vo
				postId = post.getPostId(); //岗位申请主键
				 Integer proposerId	= post.getProposerId(); //申请人ID
				 firstDepartmentId = post.getFirstDepartment(); //一级部门ID
				 secondDepartmentId = post.getSecondDepartment(); //二级部门ID
			     categoryCode = post.getCategory();  //
			     postName = post.getPostName(); //岗位名称
			     otherRequire = post.getRequired();  //其他要求
			     otherDuty = post.getDuty();  //其他职责
			     numberPeople = post.getNumberPeople();  //需求人数
			     status =  post.getStatus(); //1：审批中 2：发布中 3：已关闭
			     Integer positionRankId = post.getPositionRankId();//职位职级表id
			     Date applyDate = post.getDateSubmit();
			     String applyDateString = ExDateUtils.dateToString(applyDate,"yyyy-MM-dd");
			     Integer rank = null;
			     param = new HashMap<>();
			     param.put("postId", postId);
			     param.put("type", 2);
//			     //岗位职责
//			    List<PostDuty> listDuty =  postDutyMapper.findPostDutyByPostId(param);
//			    //岗位要求
//			    List<PostRequire> listRequire =  postRequireMapper.findPostRequireByPostId(param);
			    //通过岗位类别和岗位名称查找岗位模板
			    paramTemplate = new HashMap<>();
			    paramTemplate.put("categoryId", categoryCode);
			    paramTemplate.put("postName", postName);
			    postTemplate =  postTemplateMapper.findPostTemplateIdByCatPostName(categoryCode,postName);
			    String salaryRange = "";
			    if(postTemplate != null){
			    	salaryRange = postTemplate.getSalaryRange(); //薪资范围
			    }
			     
//			    Integer applyEmployeeId = getUserInfo(proposerId); //申请人员工Id
			    //通过员工Id查找员工姓名
			    employeeMap =employeeMapper.selectByEmployeeIdForlx(proposerId);
			    if(employeeMap!=null && employeeMap.containsKey("name")){
					if(null != employeeMap.get("name")){
						empName = String.valueOf(employeeMap.get("name")); //申请人姓名
					}
					
				}
			    principalEmployeeMap =employeeMapper.selectByEmployeeIdForlx(post.getPrincipal());//接口人
			    if(principalEmployeeMap != null && principalEmployeeMap.containsKey("name")){
					if(null != principalEmployeeMap.get("name")){
						principalEmpName = String.valueOf(principalEmployeeMap.get("name")); //接口人姓名
					}
			    }
			    //通过一级部门Id查找一级部门名称
				department = departmentMapper.findByDepartmentId(firstDepartmentId);
				if (department != null) {
					firstDepName = department.getDepartmentName();
				}

				department = departmentMapper.findByDepartmentId(secondDepartmentId);
				if (department != null) {
					seconfDepName = department.getDepartmentName();
				}
				   
				//职级
				ErpPositionRankRelation positionRankRelation=positionRankRelationMapper.selectErpPositionRankRelationByPostionNo(positionRankId);
				if(positionRankRelation!=null){
					rank=positionRankRelation.getRank();
				}
			   //查找岗位类别名称
			   Map<String, Object> paramDic = new HashMap<>();
			   paramDic.put("POST_CATEGORY", DicConstants.POST_CATEGORY);
			   paramDic.put("dicCode", categoryCode);
			   categoryName =  adminDicMapper.findPostCategoryName(paramDic);
			   //岗位申请表主键查找 岗位申请操作记录
			   List<PositionOperRecond> positionOperRecord = operRecordMapper.findOperRecordByPostId(postId);
			   
			   //岗位申请表主键 查找岗位流程表
			   PositionApplyProgress  applyProg = applyProgressMapper.findApplyProgressByPostId(postId);
			   if(applyProg == null ){
				   continue;
			   }
			   Integer currentPersonID = applyProg.getCurrentPersonID();//当前处理人Id
			   //根据当前处理人ID 查找UserID
//			  Integer userID  =   getUserInfo(currentPersonID);
			  //根据UserId 查找 姓名
			  Map<String, Object>  paramEmp =employeeMapper.selectByEmployeeIdForlx(currentPersonID);
			  String  currentPersonName = "";
			  if(paramEmp != null && paramEmp.containsKey("name")){
				   currentPersonName = String.valueOf(paramEmp.get("name")); //当前处理人姓名
			  }
			
			 Boolean onShow = false; //可以显示修改和删除 false不显示 ，true显示
			 if(ErpUserId == currentPersonID){ //当前处理人为申请人 时，可以修改和删除 
				 onShow = true;
			  }
			 
			 Map<String, Object> look2 = new HashMap<>(); //招聘人数，offer
			   look2.put("numberPeople", numberPeople); //获取面试人数/offer人数/入职人数等信息
			   //调用玉伟方法获取面试人数/offer人数/入职人数等信息
			   Integer CountAllInterview = resumePostMapper.selectCountAllInterview(postId);
			   Integer CountAllOffer =  offerMapper.selectCountAllOffer(postId);
			  Integer  CountAllEntry =  employeeEntryMapper.selectCountAllEntry(postId);
			   look2.put("totalInterView",CountAllInterview );
			  look2.put("offerNum", CountAllOffer);// 已发offer人数
			  look2.put("entryNum", CountAllEntry);// 已入职人数
			  
			   vo.put("look", positionOperRecord); //查看
			   vo.put("look2", look2); //查看
			   vo.put("postId", postId); //岗位申请主键
			   vo.put("applyPersonName", empName); //申请人
			   vo.put("proposerId", ErpUserId); //申请人ID
//			   vo.put("ErpUserId", ErpUserId); //审批人ID
			   vo.put("firstDepartmentId", firstDepartmentId);
			   vo.put("secondDepartmentId", secondDepartmentId);
			   vo.put("firstDepartmentName", firstDepName);
			   vo.put("secondDepartmentName", seconfDepName);
			   vo.put("postCategoryName", categoryName); //岗位类别
			   vo.put("categoryId", categoryCode); //岗位类别码值
			   vo.put("postName", postName); //岗位名称
//			   vo.put("listDuty", listDuty);
//			   vo.put("listRequire", listRequire);
			   vo.put("salaryRange", salaryRange);
			   vo.put("numberPeople", numberPeople);
			   vo.put("otherRequire", otherRequire);
			   vo.put("otherDuty", otherDuty);
			   vo.put("workAddress", post.getWorkAddress());//工作地点
			   vo.put("recruitCycle", post.getRecruitCycle());//招聘周期
			   vo.put("totalInterView",CountAllInterview );//总面试数
			   vo.put("offerNum", CountAllOffer);// 已发offer人数
			   vo.put("entryNum", CountAllEntry);// 已入职人数
			   vo.put("principalName", principalEmpName);// 接口人姓名
			   //查找优先级名称
			   Map<String, Object> levelPriorityParamDic = new HashMap<>();
			   levelPriorityParamDic.put("POST_CATEGORY", DicConstants.LEVEL_PRIORITY);
			   levelPriorityParamDic.put("dicCode", post.getLevelPriority());
			   String levelPriorityName =  adminDicMapper.findPostCategoryName(levelPriorityParamDic);
			   vo.put("levelPriority", levelPriorityName);// 优先级
			
			   //hr负责人名称
			   String personChargeName = "";
			    Map<String,Object> personChargeEmployeeMap =employeeMapper.selectByEmployeeIdForlx( post.getPersonCharge());//hr负责人名称
			    if(personChargeEmployeeMap != null && personChargeEmployeeMap.containsKey("name")){
					if(null != personChargeEmployeeMap.get("name")){
						personChargeName = String.valueOf(personChargeEmployeeMap.get("name")); //接口人姓名
					}
			    }
			   vo.put("hrCharge", personChargeName);// 优先级
			   vo.put("currentPersonId", currentPersonID); //当前处理人Id
			   vo.put("onShow", onShow); //可以修改和删除 
			   vo.put("applyDate", applyDateString); //申请日期
			   vo.put("rank", rank); //职级
			   vo.put("status", status);
			   if(status == 1){//状态
				   vo.put("statusName", "审批中");
				   vo.put("currentPersonName", currentPersonName); //当前处理人
			   }else if(status == 2){
				   vo.put("statusName", "发布中");
				   vo.put("currentPersonName", ""); //当前处理人
			   }else if(status == 3){
				   vo.put("statusName", "已关闭");
				   vo.put("currentPersonName", ""); //当前处理人
			   }else if(status == 4){
				   vo.put("statusName", "待审批");
				   vo.put("currentPersonName", currentPersonName); //当前处理人
			   }else if(status == 5){
				   vo.put("statusName", "待提交");
				   vo.put("currentPersonName", currentPersonName); //当前处理人
			   }
			   else if(status == 6){
				   vo.put("statusName", "暂停");
			   }
			   
			   listVo.add(vo);
			}
		} catch (Exception e) {
			logger.error("getMyApplyPostionByLogin()方法异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("getMyApplyPostionByLogin()方法异常"+e.getMessage());
		}
			
		return RestUtils.returnSuccess(listVo);
	
	}
	/**
	 * 根据当前登录人修改我申请的岗位
	 *@param id  用户主键
	 * @param postId  岗位申请表主键
	 * @param  applyPersonId
	 * @return
	 * @throws Exception 
	 */
	@Transactional
	public RestResponse updateMyApplyPostionByLogin(Map<String, Object> param, String token) throws Exception {
		Map<String, Object> result = new HashMap<>();
		String optype = "";
//		try {
			
		ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
		Integer erpUserId = null;
		if(erpUser != null){
			erpUserId = erpUser.getUserId();
		}
		/*
		 * 输入验证
		 */
		if(param == null) {
			  return RestUtils.returnFailure("参数为空");
		}
		//接口人
		if(isNullValidate(param.get("principal"))) {
			  return RestUtils.returnFailure("接口人为空");
		}
		//职位职级id
		if(isNullValidate(param.get("positionRankId"))) {
			  return RestUtils.returnFailure("职级为空");
		}
		//工作地省市
		if(isNullValidate(param.get("city"))) {
			  return RestUtils.returnFailure("工作地-省市为空");
		}
		//工作地区
		if(isNullValidate(param.get("district"))) {
			  return RestUtils.returnFailure("工作地-区为空");
		}
		//工作地县
		if(isNullValidate(param.get("county"))) {
			  return RestUtils.returnFailure("工作地-县为空");
		}
		//详细地址
		if(isNullValidate(param.get("detailAddress"))) {
			  return RestUtils.returnFailure("详细地址为空");
		}
		//申请日期
		if(isNullValidate(param.get("dateSubmit"))) {
			  return RestUtils.returnFailure("申请日期为空");
		}
		//招聘原因
		if(isNullValidate(param.get("reasonRecruit"))) {
			  return RestUtils.returnFailure("招聘原因为空");
		}
		//优先级
		if(isNullValidate(param.get("levelPriority"))) {
			  return RestUtils.returnFailure("优先级为空");
		}
		
		for(String key:param.keySet()){
			if(key.equals("postId")){ //岗位申请表主键
				if(isNullValidate(param.get(key))){
					return RestUtils.returnSuccess("岗位申请表主键为空");
				} 
				result.put("postId",param.get(key));	
			}
			if(key.equals("proposerId")){ //申请人 id
				if(isNullValidate(param.get(key))){
					return RestUtils.returnSuccess("申请人为空");
				} 
				result.put("proposerId",param.get(key));	
			}
			if(key.equals("firstDepartmentId")){
				if(isNullValidate(param.get(key))){
					return	RestUtils.returnSuccess("一级部门为空");
				} 
				result.put("firstDepartmentId",param.get(key));	
			}
			if(key.equals("firstDepName")){
				if(!isNullValidate(param.get(key))){
						result.put("firstDepName",param.get(key));
				} 
				
			}
			if(key.equals("seconfDepName")){
				if(!isNullValidate(param.get(key))){
						result.put("seconfDepName",param.get(key));
				} 
				
			}
			if(key.equals("secondDepartmentId")){
				if(isNullValidate(param.get(key))){
					return RestUtils.returnSuccess("二级部门为空");
					
				} 
				result.put("secondDepartmentId",param.get(key));
			}
			if(key.equals("approvePersonBoss")){//审批人
				if(isNullValidate(param.get(key))){
				  return	RestUtils.returnSuccess("审批人为空");
				} 
				result.put("approvePersonBoss",param.get(key));
			}
			if(key.equals("categoryId")){//岗位类别码值为空
				if(isNullValidate(param.get(key))){
					return	RestUtils.returnSuccess("岗位类别码值为空");
				} 
				result.put("categoryId",param.get(key));
			}
			if(key.equals("postName")){//岗位名称
				if(isNullValidate(param.get(key))){
					return	RestUtils.returnSuccess("岗位名称为空");
				} 
				result.put("postName",param.get(key));
			}
			if(key.equals("postduty")){//岗位职责
				if(isNullValidate(param.get(key))){
					return	RestUtils.returnSuccess("岗位职责为空");
				} 
				result.put("postduty",param.get(key));
			}
			if(key.equals("postRequire")){//岗位要求
				if(isNullValidate(param.get(key))){
					return	RestUtils.returnSuccess("岗位要求为空");
				} 
				result.put("postRequire",param.get(key));
			}
			if(key.equals("other_required")){//其他要求
				if(!isNullValidate(param.get(key))){
					result.put("other_required",param.get(key));
				}else{
					result.put("other_required","");
				}
				
			}
			if(key.equals("other_duty")){//其他职责
				if(!isNullValidate(param.get(key))){
					result.put("other_duty",param.get(key));
				}else{
					result.put("other_duty","");
				}
				
			}
			if(key.equals("numberPeople")){//人数
				if(!isNullValidate(param.get(key))){
					result.put("numberPeople",param.get(key));
				}else{
					result.put("numberPeople",0);
				}
				
			}
			if(key.equals("type")){//操作类别       save、submit
				if(!isNullValidate(param.get(key))){
					optype = String.valueOf(param.get(key));
				}else{
					optype = "save";
				}
			}
			if(key.equals("salary")){//薪资范围
				if(!isNullValidate(param.get(key))){
					result.put("salary",param.get(key));
				}else{
					result.put("salary","");
				}
			}
			if(key.equals("workAddress")){//工作地点
				if(!isNullValidate(param.get(key))){
					result.put("workAddress",param.get(key));
				}else{
					result.put("workAddress","");
				}
			}
			if(key.equals("recruitCycle")){//招聘周期
				if(!isNullValidate(param.get(key))){
					result.put("recruitCycle",param.get(key));
				}else{
					result.put("recruitCycle","");
				}
			}
			
		}
		//岗位为审批中、发布中或已关闭状态不可以修改
		List<Integer>  postIds = new ArrayList<>();
		postIds.add(ObjToInteger(result.get("postId")));
		List<ErpPost>  erpPostList = postMapper.findPostByPostIds(postIds);
		 Integer status = erpPostList.get(0).getStatus();
		 if(status == 1 ||status == 2 ||status == 3 ){
			return RestUtils.returnSuccessWithString("岗位发布或关闭中不可执行修改操作");
		 }
		
		/*
		 * 组装数据
		 */
		Map<String, Object> findTemplateMap = new HashMap<>(); //查找岗位模板参数
		findTemplateMap.put("categoryId", result.get("categoryId"));
		findTemplateMap.put("postName", result.get("postName"));
		PostTemplate template  = postTemplateMapper.findPostTemplateIdByCatPostName(String.valueOf(result.get("categoryId")),String.valueOf(result.get("postName")));
		Integer postTemplateId = template.getPostTemplateId(); //获取岗位模板主键
		ErpPost erpPost = new ErpPost(); //岗位申请对象
		erpPost.setFirstDepartment(Integer.valueOf(result.get("firstDepartmentId").toString()));
		erpPost.setSecondDepartment(Integer.valueOf(result.get("secondDepartmentId").toString()));
		erpPost.setCategory(result.get("categoryId").toString());
		erpPost.setRequired(result.get("other_required").toString());  //其他要求
		erpPost.setDuty(result.get("other_duty").toString());  //其他要求
		erpPost.setSalaryRange(result.get("salary").toString());  //薪资范围
		erpPost.setWorkAddress(String.valueOf(param.get("detailAddress")));//工作地点
		erpPost.setRecruitCycle(result.get("recruitCycle").toString());//招聘周期
		erpPost.setNumberPeople(Integer.valueOf(result.get("numberPeople").toString()));//招聘人数
		erpPost.setIsClosed(0); //是否关闭 0未关闭，1关闭
		erpPost.setProposerId(erpUserId);//岗位申请人Id  存在的是用户主键
		if (optype.equals("save")){
			erpPost.setStatus(5); //岗位申请状态（5：待提交）
		}else{
			erpPost.setStatus(4); //岗位申请状态（4：待审批）
		}
		erpPost.setPostName(result.get("postName").toString());  //岗位名称
		erpPost.setPostTemplateId(postTemplateId); //岗位模板主键
		//接口人
		erpPost.setPrincipal(Integer.valueOf(param.get("principal").toString()));
		//审批人
		erpPost.setPrincipalLeader(Integer.valueOf(param.get("approver").toString()));
		//职位职级id
		erpPost.setPositionRankId(Integer.valueOf(param.get("positionRankId").toString()));
		//工作地-省市
		erpPost.setCity(String.valueOf(param.get("city")));
		//工作地-区
		erpPost.setDistrict(String.valueOf(param.get("district")));
		//工作地-县
		erpPost.setCounty(String.valueOf(param.get("county")));
		//详细地址
		erpPost.setDetailAddress(String.valueOf(param.get("detailAddress")));
		//申请日期
		erpPost.setDateSubmit(ExDateUtils.convertToDate(String.valueOf(param.get("dateSubmit"))));
		//招聘原因
		erpPost.setReasonRecruit(Integer.valueOf(String.valueOf(param.get("reasonRecruit"))));
		//优先级
		erpPost.setLevelPriority(Integer.valueOf(String.valueOf(param.get("levelPriority"))));
		Integer approveId = ObjToInteger(result.get("approvePersonBoss")) ; //审批人Id
//		Integer empApproveId = getUserInfo(approveId);
		//通过员工ID查找员工姓名
		Map<String, Object> empMap = employeeMapper.selectByEmployeeIdForlx(approveId);
		String.valueOf(empMap.get("name"));
		Integer postId = ObjToInteger(result.get("postId")); //岗位申请表主键
		erpPost.setPostId(postId);
//		//调用修改方法
//		template.setPostTemplateId(template.getPostTemplateId());
//		postTemplateMapper.updatePostTemplate(template);
		
		/*
		 * 修改岗位申请信息
		 */
		try {
			 postMapper.updatePost(erpPost);
		} catch (Exception e) {
			logger.error("修改岗位申请报错:"+e.getMessage(),e);
			RestUtils.returnSuccess("修改岗位申请报错:"+e.getMessage());
		}
		/*
		 * 修改岗位职责和岗位要求
		 */
//		JSONObject jsonDuty = null;
//		List<String> jsonDutyList = new ArrayList<>(); //存储岗位职责list
//		List<Integer> jsonDutyIds = new ArrayList<>(); //存储岗位职责主键
//		String duty = "";
//		if(result.containsKey("postduty")){//岗位职责
//			if(null != result.get("postduty")){
//				duty = JSON.toJSONString(result.get("postduty")) ;
//				JSONArray jsonArray =	JSONObject.parseArray(duty);
//				PostDuty  postDuty2 = null;
//				for(int i=0;i<jsonArray.size();i++){
//					if(jsonArray.get(i) != null){//非空判断
//						jsonDuty = JSONObject.parseObject(String.valueOf(jsonArray.get(i)))  ;
////						jsonDutyList.add(jsonDuty.getString("postdutyDescribe")) ;
//						if(jsonDuty.containsKey("id")){//空 判断 插入时候没有Id，修改时候有Id
//							postDuty2 = new PostDuty();
//							postDuty2.setPostdutyDescribe(jsonDuty.getString("postdutyDescribe"));
//							postDuty2.setPostId(postId);
//							postDuty2.setType(2); //1 是岗位模板 2是岗位申请
//							postDuty2.setId(jsonDuty.getInteger("id"));
//							postDutyMapper.updatePostDuty(postDuty2); 
////							jsonDutyIds.add(jsonDuty.getInteger("id"));
//						}else{
//							postDuty2 = new PostDuty();
//							postDuty2.setPostdutyDescribe(jsonDuty.getString("postdutyDescribe"));
//							postDuty2.setPostId(postId);
//							postDuty2.setType(2); //1 是岗位模板 2是岗位申请
//							postDutyMapper.addPostDuty(postDuty2);
//						}
//						
//					}
//					
//				}
//			}
//			
//		}
//		
//		JSONObject jsonRequire = null;
//		List<String> jsonRequireList = new ArrayList<>(); //存储岗位要求list
//		List<Integer> jsonRequireIds = new ArrayList<>(); //存储岗位要求主键
//		String require = "";
//		if(result.containsKey("postRequire")){//岗位要求
//			PostRequire postRequire2 = null;
//			if(null != result.get("postRequire")){
//				require = JSON.toJSONString( result.get("postRequire")) ;
//				JSONArray jsonArray =	JSONObject.parseArray(require);
//				for(int j=0;j<jsonArray.size();j++){
//					if(jsonArray.get(j) != null){//非空判断
//						 jsonRequire = JSONObject.parseObject(String.valueOf(jsonArray.get(j)))  ;
////						jsonRequireList.add(jsonRequire.getString("postRequireDescribe")) ;
//						if(jsonRequire.containsKey("id")){ //空 判断 插入时候没有Id，修改时候有Id
////							jsonRequireIds.add(jsonRequire.getInteger("id"));
//							postRequire2 = new PostRequire();
//							postRequire2.setPostId(postId);
//							postRequire2.setPostRequireDescribe(jsonRequire.getString("postRequireDescribe"));
//							postRequire2.setType(2); //1 是岗位模板 2是岗位申请
//							postRequire2.setId(jsonRequire.getInteger("id")); //设置主键 用于修改数据
//							postRequireMapper.updatePostRequire(postRequire2); //批量修改岗位要求
//							
//						}else{
//							postRequire2 = new PostRequire();
//							postRequire2.setPostId(postId);
//							postRequire2.setPostRequireDescribe(jsonRequire.getString("postRequireDescribe"));
//							postRequire2.setType(2); //1 是岗位模板 2是岗位申请
//							postRequireMapper.addPostRequire(postRequire2); //批量修改岗位要求
//						}
//						
//					}
//					
//				}
//			}
//			
//		}

		if (optype.equals("submit")){
		/**
		 * 	修改岗位申请流程表
		 */
		    //通过postId查找岗位申请流程表
			PositionApplyProgress positionApplyProgress  = applyProgressMapper.findApplyProgressByPostId(postId);
			Integer applyProgressId = positionApplyProgress.getId();
			PositionApplyProgress applyProgress = new PositionApplyProgress();
			applyProgress.setPostId(postId); //岗位申请表主键
//			applyProgress.setCurrentPersonID(ObjToInteger(result.get("proposerId"))); //当前处理人,申请人的用户Id
			applyProgress.setCurrentPersonID(approveId); //当前处理人,审批人id
			applyProgress.setId(applyProgressId); //设置主键
			applyProgressMapper.updateApplyProgressById(applyProgress);
			//插入岗位处理记录表
			PositionOperRecond operRec = new PositionOperRecond();
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			operRec.setCreateTime(format.format(date));
			operRec.setOperContext("修改岗位申请");
			Integer currentPersonId = null;
			currentPersonId = erpUserId;
			operRec.setCurrentPersonId(currentPersonId);//当前处理人,申请人的用户Id
			 
//			 Integer empId = getUserInfo(currentPersonId); //当前处理人的员工Id
			Map<String, Object> employeeMap =employeeMapper.selectByEmployeeIdForlx(currentPersonId); //根据当前处理人id 查找当前处理人姓名
			if(employeeMap != null && employeeMap.containsKey("name")){
				operRec.setCurrentPersonName(String.valueOf(employeeMap.get("name")));  //当前处理人姓名
			}
			operRec.setPostId(postId);//岗位申请表表主键
			operRecordMapper.addPositionOperReord(operRec);
		}
//		} catch (Exception e) {
//			logger.error("更新岗位申请异常:"+e.getMessage());
//			RestUtils.returnFailure("更新岗位申请异常:"+e.getMessage());
//		}	
		return RestUtils.returnSuccess("修改成功！");
		
	}
	
	/**
	 * 根据岗位申请表主键获取当前岗位申请信息
	 * @param id  用户主键
	 * @param postId  岗位申请表主键
	 * @param  applyPersonId
	 * @return
	 */
	public RestResponse getMyPostionApplyByPostId(Integer id, Integer postId) {
		logger.info("进入getMyPostionApplyByPostId参数:postId= "+postId+"---id=="+id);
		List<Integer>  postIds = new ArrayList<>();
		postIds.add(postId);
		List<ErpPost> list = null;
		try {
			 list = postMapper.findPostByPostIds(postIds);
			ErpPost post = null;
			post = list.get(0);
			 Integer proposerId	= post.getProposerId(); //申请人ID
			 Integer firstDepartmentId = post.getFirstDepartment(); //一级部门ID
			 String firstDepartmentName = ""; //一级部门名称
			 Integer secondDepartmentId = post.getSecondDepartment(); //二级部门ID
			 String secondDepartmentName = "";
			 ErpDepartment  department  = null;
			 String categoryCode = ""; //岗位类别码值
			 String categoryName = ""; //岗位类别名称
			 String postName = ""; //岗位名称
			 String otherRequire = "";// 其他要求
			 String otherDuty = "";// 其他职责
			 Integer numberPeople = post.getNumberPeople(); //招聘人数
			 String salaryRange = ""; //薪资范围
			 post.getPostTemplateId();
			 categoryCode = post.getCategory();
			 postName = post.getPostName();
			 otherRequire = post.getRequired();
			 otherDuty = post.getDuty();
			 salaryRange = post.getSalaryRange();
			 //根据岗位类别码值查找岗位类别名称
			 Map<String, Object> param = new HashMap<>();
			 param.put("POST_CATEGORY", DicConstants.POST_CATEGORY);
			 param.put("dicCode", categoryCode);
			 categoryName = adminDicMapper.findPostCategoryName(param);
	//		 PostTemplate  postTemplate = null; //岗位模板
	//		 if(postTemplateId != null){
	//			 postTemplate =  postTemplateMapper.findtPostTemplateById(postTemplateId);
	//			 if(postTemplate != null){
	//				 salaryRange = postTemplate.getSalaryRange();
	//			 }
	//
	//		 }

			 //调用权限工程申请人ID ，获取userID
	//		Integer  usrId =  getUserInfo(proposerId);
			 //通过userID 查找员工姓名 (申请人姓名)
			 Map<String, Object>  paramEmp = employeeMapper.selectByEmployeeIdForlx(proposerId);
			String  proposerName = String.valueOf(paramEmp.get("name")); //申请人姓名
			 //通过一级部门Id查找部门名称
			 department =  departmentMapper.findByDepartmentId(firstDepartmentId);
			firstDepartmentName = department.getDepartmentName();
			secondDepartmentName =  departmentMapper.findByDepartmentId(secondDepartmentId).getDepartmentName();
			//根据一级部门Id查找一级部门经理Id
			Map<String, Object>  manager = employeeMapper.selectManagerByFirstDepartment(firstDepartmentId);
			Integer uId = null;

			if(manager.get("userId") == null){
				uId = 0;
			}else{
				 uId = Integer.valueOf(manager.get("userId").toString());//一级部门经理Id
			}

			Integer approveId = null;

			if(id.equals(uId)){//当前登录人是一级部门经理
				if(manager.containsKey("superLeader")){//有上级领导
					//获得审批人 Id
					approveId =	Integer.valueOf(String.valueOf(manager.get("superLeader"))) ;
				}else{
					//查找字典中的审批人(魏总/何总)
					List<AdminDic> listApp = adminDicMapper.findApprove(DicConstants.APPROVE);
					approveId = Integer.valueOf(listApp.get(0).getDicCode());
				}
			}
			else
			{
				approveId = uId;
			}

			//根据审批人用户Id查找 员工Id
	//		Integer empId = getUserInfo(approveId);
			//通过员工ID查找员工姓名
			Map<String, Object> ermInfoMap =  employeeMapper.selectByEmployeeIdForlx(approveId);

			Map<String, Object> approve = new HashMap<>();
			approve.put("approveId", approveId); //用户主键Id
			approve.put("approveName", String.valueOf(ermInfoMap.get("name"))); //员工姓名

			//通过岗位申请主键查找岗位职责和 岗位要求
			param.clear();
			param.put("postId", postId);
			param.put("type", 2);
	//		List<PostDuty> listDuty = postDutyMapper.findPostDutyByPostId(param);
	//		//根据岗位模板主键查找岗位要求
	//		List<PostRequire>  listReq = postRequireMapper.findPostRequireByPostId(param);
			Map<String, Object> vo = new HashMap<>(); //返回前端数据
			vo.put("postId", postId); //岗位申请表主键
		   vo.put("applyPerson", proposerName); //申请人
		   vo.put("applyId", proposerId); //申请人ID
		   vo.put("firstDepName", firstDepartmentName);
		   vo.put("firstDepartmentId", firstDepartmentId);
		   vo.put("seconfDepName", secondDepartmentName);
		   vo.put("secondDepartmentId", secondDepartmentId);
		   vo.put("categoryName", categoryName); //岗位类别名称
		   vo.put("categoryId", categoryCode); //岗位类别码值
		   vo.put("postName", postName); //岗位名称
		   vo.put("approvePersonBoss", approve) ; //审批人
//		   vo.put("postduty", listDuty);//岗位职责
//		   vo.put("postRequire", listReq);//岗位需求
		   vo.put("numberPeople", numberPeople);//招聘人数
		   vo.put("salaryRange", salaryRange);//招聘人数
		   vo.put("otherRequire", otherRequire);//其他要求
		   vo.put("otherDuty", otherDuty);//其他职责
		   vo.put("workAddress", post.getWorkAddress());//工作地点
		   vo.put("recruitCycle", post.getRecruitCycle());//招聘周期
			return RestUtils.returnSuccess(vo);
		} catch (Exception e) {
			logger.error("getMyPostionApplyByPostId方法查询异常"+e.getMessage(), e);
			return RestUtils.returnFailure("getMyPostionApplyByPostId方法查询异常"+e.getMessage());
		}
	}
	/**
	 * 申请人执行撤回操作
	 * @param id  用户主键
	 * @param postId 申请表主键
	 * @param content 撤回原因
	 * @return
	 */
	@Transactional
	public RestResponse rebackOperation(Map<String, Object> param) {
		logger.info("进入rebackOperation()方法参数:"+param);
		/**
		 * 数据处理
		 */
		Integer id = null;
		String content = "";
		Integer postId = null;
		
		String	token=	request.getHeader("token");
		ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
		id = erpUser.getUserId();
		int status = 0;
		for(String key:param.keySet()){
			if("postId".equals(key)){
				postId = ObjToInteger(param.get(key));
			}
			if("content".equals(key)){
				content = String.valueOf(param.get(key));
			}
		}
		
		//根据postId 查找岗位申请状态 status
	    Map<String, Object> postMap = postMapper.findByPostId(postId);
	    if(postMap.containsKey("status")) {
	    	status = ObjToInteger(postMap.get("status")) ;
	    }
	    if(status == 1){
			return RestUtils.returnSuccess("处于审批中岗位,不可撤回");
		}
		if(status == 2){
			return RestUtils.returnSuccess("处于发布中岗位,不可撤回");
		}
		if(status == 3){
			return RestUtils.returnSuccess("已经关闭岗位,不可撤回");
		}
	    if(status == 4){//处于待审批可执行撤回
			/**
			 * 插入岗位处理记录表
			 */
			PositionOperRecond operRec = new PositionOperRecond();
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
			operRec.setCreateTime(format.format(date));
			operRec.setOperContext(content); //处理内容
			operRec.setCurrentPersonId(id);//用户主键
//			 Integer empId = getUserInfo(id); //根据用户主键获取员工Id
			Map<String, Object> employeeMap =employeeMapper.selectByEmployeeIdForlx(id); //根据员工Id 查找员工姓名
			if(employeeMap.containsKey("name")){
				operRec.setCurrentPersonName(String.valueOf(employeeMap.get("name")));  
			}
			
			operRec.setPostId(postId);//岗位申请表表主键
			operRecordMapper.addPositionOperReord(operRec);
			/**
			 * 修改岗位流程表
			 * 
			 */
			//岗位申请表关联Id 和当前处理人用户编号 定位一条岗位流程记录
			PositionApplyProgress  applyProgress  = applyProgressMapper.findApplyProgressByPostId(postId);
			Integer postionApplyProgId = null; //岗位流程主键
			if(applyProgress != null){
				postionApplyProgId = applyProgress.getId();
			}
			//岗位流程表，岗位申请当前处理人变更为申请人Id
			PositionApplyProgress postionapplyProgress = new PositionApplyProgress();
			postionapplyProgress.setCurrentPersonID(id);
			postionapplyProgress.setId(postionApplyProgId);
			applyProgressMapper.updateApplyProgressById(postionapplyProgress);
			//修改岗位申请表状态 为 待提交
			ErpPost post = new ErpPost();
			post.setPostId(postId);
			post.setStatus(5); //待提交
			postMapper.updatePost(post);
	    }
		return RestUtils.returnSuccess("撤回成功！");
	}
	
	/**
	 * 申请人执行删除操作
	 * @param id  用户主键
	 * @param postId  申请表主键
	 * @return
	 */
	@Transactional
	public RestResponse deleteMyApplyOperation(Integer postId) {
		logger.info("进入deleteMyApplyOperation---");
		String	token=	request.getHeader("token");
		ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
		Integer id = erpUser.getUserId();
		try {
			//添加验证岗位发布或关闭中不可以删除
			List<Integer>  postIds  = new ArrayList<>();
			postIds.add(postId);
			List<ErpPost>  erpPostList = postMapper.findPostByPostIds(postIds);
			 Integer status = erpPostList.get(0).getStatus();
			 if(status == 1 ||status == 2 ||status == 3 ){
				return RestUtils.returnSuccessWithString("岗位发布或关闭中不可执行删除操作");
			 }
//			postDutyMapper.deletePostDutyByPostId(postId,2); //删除岗位职责
//			
//			postRequireMapper.deletePostRequireByPostId(postId,2);//删除岗位要求
			postMapper.deleteById(postId); //删除岗位申请表
			//往岗位申请记录表中插入数据
			PositionOperRecond operRec = new PositionOperRecond();
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
			operRec.setCreateTime(format.format(date));
			operRec.setOperContext("删除操作"); //处理内容
			operRec.setCurrentPersonId(id);//用户主键
//			 Integer empId = getUserInfo(id); //根据用户主键获取员工Id
			Map<String, Object> employeeMap =employeeMapper.selectByEmployeeIdForlx(id); //根据员工Id 查找员工姓名
			if(employeeMap != null && employeeMap.containsKey("name")){
				operRec.setCurrentPersonName(String.valueOf(employeeMap.get("name")));  
			}
			
			operRec.setPostId(postId);//岗位申请表表主键
			operRecordMapper.addPositionOperReord(operRec);
			//修改岗位流程表
			PositionApplyProgress  applyProgress  = applyProgressMapper.findApplyProgressByPostId(postId);
			Integer postionApplyProgId = null; //岗位流程主键
			if(applyProgress != null){
				postionApplyProgId = applyProgress.getId();
			}
			PositionApplyProgress postionapplyProgress = new PositionApplyProgress();
			postionapplyProgress.setCurrentPersonID(id);
			postionapplyProgress.setId(postionApplyProgId);
			postionapplyProgress.setPostId(postId);
			applyProgressMapper.updateApplyProgressById(postionapplyProgress);
		} catch (Exception e) {
			logger.error("deleteMyApplyOperation()异常"+e.getMessage());
			RestUtils.returnFailure("deleteMyApplyOperation()异常删除失败!"+e.getMessage());
		}
		return RestUtils.returnSuccess("删除成功");
	}
	/**
	 * 查询所有发布中的岗位
	 * @return
	 */
	public RestResponse findAllPublishPostionApplyn(String token) {
		logger.info("进入findAllPublishPostionApplyn()查询所有发布中的岗位");
		 Integer firstDepartmentId = null; //一级部门ID
		 String firstDepartmentName = ""; //一级部门名称
		 Integer secondDepartmentId = null; //二级部门ID
		 String secondDepartmentName = "";
		 ErpDepartment  department  = null;
		 String categoryCode = ""; //岗位类别码值
		 String categoryName = ""; //岗位类别名称
		 String postName = ""; //岗位名称
		 Integer proposerId = null; //申请人用户主键
		 String  proposerName = "" ;// 申请人姓名
		 Integer numberPeople = 0; //招聘人数
		 Integer postId = null; //岗位主键
		 String otherRequire = ""; //其他要求
		 String otherDuty = ""; //其他职责
		 String workAddress = "";//工作地点
		 String recruitCycle = "";//招聘周期
		 List<Map<String, Object>> listVo = null;
		try {
			//查询发布中的岗位
			Map<String, Object> param = new HashMap<>();
			param.put("status", 2);  //2发布中
			
			//根据角色过滤数据
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);// 从缓存中获取登录用户信息
			Integer id = erpUser.getUserId();// 从用户信息中获取角色信息
			List<Integer> roles = erpUser.getRoles();// 角色列表

			if (roles.contains(8) || roles.contains(1)|| roles.contains(10)) {// 总经理、hr
				// 查询所有的员工
			} else if (roles.contains(9)) { // 副总经理
				param.put("superLeaderId", id);
			} else if (roles.contains(2)) {// 一级部门经理角色
				param.put("leaderId", id);
			} else {
//				return RestUtils.returnFailure("无可查看的岗位"); 
				return RestUtils.returnSuccess(new ArrayList(), "无可查看的岗位");
			}				
			
			List<ErpPost> listPost = postMapper.findPostByStatus(param);
			if(listPost == null || listPost.size()< 1){
			  return	RestUtils.returnSuccess(listPost);
			}
			 listVo = new ArrayList<>();//返回前端Vo
		
			//根据申请人的Id查找岗位申请信息
				Map<String, Object> paramTemplate = null;
				Map<String, Object> employeeMap = null;
				Map<String, Object> look = null;  //查看招聘记录
				PostTemplate postTemplate = null; //岗位模板
				for(ErpPost post:listPost){//遍历岗位申请信息
					Map<String, Object> vo = new HashMap<>();//返回前端Vo
					postId = post.getPostId(); //岗位申请主键
					numberPeople = post.getNumberPeople();
					  proposerId	= post.getProposerId(); //申请人ID
					 firstDepartmentId = post.getFirstDepartment(); //一级部门ID
					 secondDepartmentId = post.getSecondDepartment(); //二级部门ID
				     categoryCode = post.getCategory();  //
				     postName = post.getPostName(); //岗位名称
				     otherRequire = post.getRequired(); //其他要求
				     otherDuty = post.getDuty(); //其他职责
				     workAddress = post.getWorkAddress();//工作地点
				     recruitCycle = post.getRecruitCycle();//招聘周期
				     param = new HashMap<>();
				     param.put("postId", postId);
				     param.put("type", 2);
//				     //岗位职责
//				    List<PostDuty> listDuty =  postDutyMapper.findPostDutyByPostId(param);
//				    //岗位要求
//				    List<PostRequire> listRequire =  postRequireMapper.findPostRequireByPostId(param);
				    //通过岗位类别和岗位名称查找岗位模板
				    paramTemplate = new HashMap<>();
				    paramTemplate.put("categoryId", categoryCode);
				    paramTemplate.put("postName", postName);
				    postTemplate =  postTemplateMapper.findPostTemplateIdByCatPostName(categoryCode,postName);
				    String salaryRange = "";
				    if(postTemplate != null){
				    	 salaryRange = postTemplate.getSalaryRange(); //薪资范围
				    }
				    
//				    Integer applyEmployeeId = getUserInfo(proposerId); //申请人员工Id
				    //通过员工Id查找员工姓名
				    employeeMap =employeeMapper.selectByEmployeeIdForlx(proposerId);
				    if (employeeMap == null){
				    	continue;
				    }
				    if(employeeMap.containsKey("name")){
						if(null != employeeMap.get("name")){
							proposerName = String.valueOf(employeeMap.get("name")); //申请人姓名
						}
						
					}
				    //通过一级部门Id查找一级部门名称
				    department =   departmentMapper.findByDepartmentId(firstDepartmentId);
				    if(department!=null) {
				    	firstDepartmentName =  department.getDepartmentName();
				    }
				   
				   department =   departmentMapper.findByDepartmentId(secondDepartmentId);
				   if(department!=null) {
					   	secondDepartmentName =  department.getDepartmentName();
				   }
				  
				   //查找岗位类别名称
				   Map<String, Object> paramDic = new HashMap<>();
				   paramDic.put("POST_CATEGORY", DicConstants.POST_CATEGORY);
				   paramDic.put("dicCode", categoryCode);
				   categoryName =  adminDicMapper.findPostCategoryName(paramDic);
				   look = new HashMap<>();
				   look.put("numberPeople", numberPeople); //招聘人数
				   //调用玉伟方法获取面试人数/offer人数/入职人数等信息
				   Integer CountAllInterview = resumePostMapper.selectCountAllInterview(postId);
				   Integer CountAllOffer =  offerMapper.selectCountAllOffer(postId);
				  Integer  CountAllEntry =  employeeEntryMapper.selectCountAllEntry(postId);
				   look.put("totalInterView",CountAllInterview );
				  look.put("offerNum", CountAllOffer);// 已发offer人数
				  look.put("entryNum", CountAllEntry);// 已入职人数
				  List<PositionOperRecond> positionOperRecord = null;	
				   positionOperRecord = operRecordMapper.findOperRecordByPostId(postId);
				   
				   vo.put("record", positionOperRecord); //审批记录
				   vo.put("look", look); //查看
				   vo.put("postId", postId); //岗位申请主键
				   vo.put("applyPerson", proposerName); //申请人
				   vo.put("applyId", proposerId); //申请人ID
				   vo.put("firstDepName", firstDepartmentName);
				   vo.put("seconfDepName", secondDepartmentName);
				   vo.put("categoryName", categoryName); //岗位类别
				   vo.put("postName", postName); //岗位名称
				   vo.put("workAddress", workAddress);//工作地点
				   vo.put("recruitCycle", recruitCycle);//招聘周期
				   vo.put("salaryRange", salaryRange);
				   vo.put("otherRequire", otherRequire); //其他要求
				   vo.put("otherDuty", otherDuty); //其他职责
				   vo.put("numberPeople", numberPeople); //招聘人数
				   
				   listVo.add(vo);
				}
			
		} catch (Exception e) {
			logger.error("findAllPublishPostionApplyn()方法报错"+e.getMessage(),e);
			return  RestUtils.returnFailure("findAllPublishPostionApplyn()方法报错"+e.getMessage());
			
		}
		return RestUtils.returnSuccess(listVo);
	}
	
	/**
	 * 推荐简历
	 * @param postId  岗位ID
	 * @param resumeId  简历Id
	 * @param isTrainee  是否是实习生
	 * @return
	 */
	@Transactional
	public RestResponse recommendedResume(String token, Integer postId, Integer resumeId,Boolean isTrainee) {
		try {
			List<Integer>  postIds = new ArrayList<>();
			postIds.add(postId);
			List<ErpPost> erpPostList = postMapper.findPostByPostIds(postIds);
			Integer proposerId = erpPostList.get(0).getProposerId(); //岗位申请人Id
			//插入岗位简历关联表
			ErpResumePost resumePost = new ErpResumePost();
			resumePost.setIsValid(true);//流程是否有效
			resumePost.setPersonId(proposerId);  //处理人为岗位申请人
//			resumePost.setPersonId(0);  //处理人为岗位申请人
			resumePost.setPostId(postId);
			resumePost.setResumeId(resumeId);
			resumePost.setStatus(DicConstants.INTERVIEW_STATUS_RESUME_SCREENING);
			if(isTrainee) {
				resumePost.setSegment(DicConstants.INTERVIEW_SEGMENT_TRAINEE_INTERVIEW);//实习生
			}else {
				resumePost.setSegment(DicConstants.INTERVIEW_SEGMENT_SOCIAL_FIRST);//初试
			}
			resumePostMapper.insertResumePost(resumePost);
			//修改简历表状态为面试中
			ErpResume resume = new ErpResume();
			resume.setResumeId(resumeId);
			resume.setStatus(DicConstants.RESUME_STATUS_IN_THE_INTERVIEW);
			resumeMapper.updateResume(resume);
			
    		//在岗位记录表中插入记录
			Map<String, Object> resumeInfo = resumeMapper.selectResumeDetail(resumeId);
			ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
    		PositionOperRecond operRec = new PositionOperRecond();
    		Date date = new Date();
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
    		operRec.setCreateTime(format.format(date));
    		operRec.setOperContext("推荐简历:"+resumeInfo.get("name")); //处理内容
    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
    		operRec.setCurrentPersonName(erpUser.getEmployeeName());//当前处理人Id
    		operRec.setPostId(postId);
    		
    		operRecordMapper.addPositionOperReord(operRec);

		} catch (Exception e) {
			logger.error("推荐简历失败"+e.getMessage());
			return RestUtils.returnFailure("推荐简历失败"+e.getMessage());
		}
		return RestUtils.returnSuccess("success");
	}
	
	/**
	 * 发起面试
	 * @param postId  岗位ID
	 * @param resumeId  简历Id
	 * @return
	 */
	@Transactional
	public RestResponse startInterview(String token, Map<String,Object> params) {
		/**
		 * 接收前端数据
		 */
		String method = String.valueOf(params.get("method"));//面试方式
		String time = null;//面试时间
		if(params.get("time")!=null) {
			time = String.valueOf(params.get("time"));
		}
		String contactId = null;//联系人
		if(params.get("contactId")!=null) {
			contactId = String.valueOf(params.get("contactId"));
		}
		String placeId = null;//面试地点
		if(params.get("placeId")!=null) {
			placeId = String.valueOf(params.get("placeId"));
		}
		Integer resumeId = Integer.valueOf(String.valueOf(params.get("resumeId")));//简历编号
		Boolean isTrainee = Boolean.valueOf(String.valueOf(params.get("isTrainee")));//是否是实习生
		Integer postId = Integer.valueOf(String.valueOf(params.get("postId"))); //岗位申请主键
		Integer proposerId=Integer.valueOf(String.valueOf(params.get("Interviewer")));//当前处理人应该是前端选择的面试官
		Boolean carryPostInfo = params.get("carryPostInfo") == null ? false
				: Boolean.valueOf(String.valueOf(params.get("carryPostInfo")));// 是否携带岗位信息
		Boolean isNext = params.get("isNext") == null ? false
				: Boolean.valueOf(String.valueOf(params.get("isNext")));// true表示邮件发送失败不影响面试流程。
		ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);// Redis中获取用户信息
		
		//通过岗位申请 ID查找岗位申请人
		/*List<Integer> postIds = new ArrayList<>();
		if(postId != null){
			postIds.add(postId);
		}
		List<ErpPost> erpPostList = postMapper.findPostByPostIds(postIds);
		Integer proposerId = erpPostList.get(0).getProposerId(); //岗位申请人Id
*/		
		try {
			
			//插入岗位简历关联表
			ErpResumePost resumePost = new ErpResumePost();
			resumePost.setIsValid(true);//流程是否有效
			resumePost.setPersonId(proposerId); //当前处理人用户编号 为岗位申请人Id
			resumePost.setPostId(postId);
			resumePost.setResumeId(resumeId);
			resumePost.setStatus(DicConstants.INTERVIEW_STATUS_ORDER_INTERVIEW); //状态为待预约
			if(isTrainee) {
				resumePost.setSegment(DicConstants.INTERVIEW_SEGMENT_TRAINEE_INTERVIEW);//实习生
			}else {
				resumePost.setSegment(DicConstants.INTERVIEW_SEGMENT_SOCIAL_FIRST);//初试
			}
			resumePostMapper.insertResumePost(resumePost);
			Integer interviewId = resumePost.getId();  //插入表返回 面试流程表主键
			
			//插入面试预约记录表
			ErpResumePostOrder resumePostOrder = new ErpResumePostOrder();
			resumePostOrder.setInterviewId(interviewId);//面试流程表编号
			resumePostOrder.setContactId(contactId); //联系人
			resumePostOrder.setMethod(method); //面试方式
			resumePostOrder.setPlaceId(placeId); //面试地点
			resumePostOrder.setTime(time); //面试时间
			resumePostOrderMapper.insertResumePostOrder(resumePostOrder);//插入表返回主键
			
			//修改简历表状态为面试中
			ErpResume resume = new ErpResume();
			resume.setResumeId(resumeId);
			resume.setStatus(DicConstants.RESUME_STATUS_IN_THE_INTERVIEW);
			resumeMapper.updateResume(resume);
			
			//在岗位记录表中插入记录
			Map<String, Object> resumeInfo = resumeMapper.selectResumeDetail(resumeId);
    		PositionOperRecond operRec = new PositionOperRecond();
    		Date date = new Date();
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
    		operRec.setCreateTime(format.format(date));
    		operRec.setOperContext("发起面试:"+resumeInfo.get("name")); //处理内容
    		operRec.setCurrentPersonId(erpUser.getUserId());//当前处理人Id
    		operRec.setCurrentPersonName(erpUser.getEmployeeName());//当前处理人Id
    		operRec.setPostId(postId);
    		
    		/*
    		 * add by ZhangYuWei
			 * 系统自动发送面试邀请邮件，发送人为登录人邮箱，接收人为简历中候选人邮箱，抄送岗位申请人邮箱
			 */
    		if(!isNext) {
    			String frommail = erpUser.getUsername();//发件人
    			if(!frommail.contains("@")) {
    				frommail += "@nantian.com.cn";
    			}
    			
    			String bcc = interviewService.getBccForSendEmail(proposerId,postId,token);//抄送人
    			if("error".equals(bcc)) {
    				return RestUtils.returnFailure("通过用户Id未获取到用户邮箱！");
    			}
    			bcc += ","+prodEmailInterviewBcc;//抄送人增加HR
    			String subject = "北京南天软件有限公司面试邀请";//主题
    			Map<String,Object> employeeMap = employeeMapper.findEmployeeDetail(erpUser.getUserId());//获取员工姓名
    			String employeeName = String.valueOf(employeeMap.get("name"));
    			//如果用户选了携带岗位职责、要求，那么把岗位信息加到邮件中。
    			String required = null;//岗位要求
    			String duty = null;//岗位职责
    			if(carryPostInfo) {
    				List<Integer> postIds = new ArrayList<>();
    				postIds.add(postId);
    				List<ErpPost> postList = postMapper.findPostByPostIds(postIds);
    				ErpPost post = postList.get(0);
    				required = post.getRequired();//岗位要求
    				duty = post.getDuty();//岗位职责
    			}
    			String text = interviewService.getTextForSendEmail(interviewId, method, frommail, employeeName,
    					erpUser.getUserPhone(), required, duty);// 邮件内容
    			String tomail = interviewService.getTomailForSendEmail(resumeId);//收件人（简历中的邮箱）
    			if("error".equals(tomail)) {
    				return RestUtils.returnFailure("通过简历Id未获取到候选人邮箱！");
    			}
    			boolean sendSuccess = restTemplateUtils.sendEmail(frommail, bcc, subject, text, tomail,DicConstants.INTERVIEW_INVITATION_EMAIL_TYPE,null);
    			if(!sendSuccess) {
    				return RestUtils.returnSuccessWithString("邮件发送失败！请到“面试预约”模块查询这条面试信息！");
    			}else {
    				//邮件发送成功后，修改面试状态为“面试中”
    				resumePost.setId(interviewId);
    				resumePost.setStatus(DicConstants.INTERVIEW_STATUS_IN_THE_INTERVIEW);
    				resumePostMapper.updateResumePost(resumePost);
    			}
    		}
			
		} catch (Exception e) {
			logger.info("发起面试失败"+e.getMessage());
			return RestUtils.returnFailure("发起面试失败"+e.getMessage());
		}
		return RestUtils.returnSuccessWithString("OK");
	}

	public RestResponse findAllClosedPosition() {
		 Integer firstDepartmentId = null; //一级部门ID
		 String firstDepartmentName = ""; //一级部门名称
		 Integer secondDepartmentId = null; //二级部门ID
		 String secondDepartmentName = "";
		 ErpDepartment  department  = null;
		 String categoryCode = ""; //岗位类别码值
		 String categoryName = ""; //岗位类别名称
		 String postName = ""; //岗位名称
		 Integer proposerId = null; //申请人用户主键
		 String  proposerName = "" ;// 申请人姓名
		 Integer numberPeople = 0; //招聘人数
		 Integer postId = null; //岗位主键
		 String closedReason = ""; //关闭原因
		 String otherRequire = ""; //其他要求
		 String otherDuty = ""; //其他职责
		 List<Map<String, Object>> listVo = null;
		try {
			//查询发布中的岗位
			Map<String, Object> param = new HashMap<>();
			param.put("status", 3);  //3已关闭
			List<ErpPost> listPost = postMapper.findPostByStatus(param);
			if(listPost == null || listPost.size()< 1){
			  return	RestUtils.returnSuccess(listPost);
			}
			 listVo = new ArrayList<>();//返回前端Vo
			Map<String, Object> vo = null; //返回前端Vo
			//根据申请人的Id查找岗位申请信息
			Map<String, Object> paramTemplate = null;
			Map<String, Object> employeeMap = null;
			Map<String, Object> look = null;
			PostTemplate postTemplate = null; //岗位模板
			for(ErpPost post:listPost){//遍历岗位申请信息
				vo = new HashMap<>();
				postId = post.getPostId(); //岗位申请主键
				numberPeople = post.getNumberPeople();
				proposerId	= post.getProposerId(); //申请人ID
				firstDepartmentId = post.getFirstDepartment(); //一级部门ID
				secondDepartmentId = post.getSecondDepartment(); //二级部门ID
				closedReason = operRecordMapper.findCloseRease(postId).getOperContext();  //关闭原因
				categoryCode = post.getCategory();  //
				postName = post.getPostName(); //岗位名称
				otherRequire = post.getRequired();
				otherDuty = post.getDuty();
				param = new HashMap<>();
				param.put("postId", postId);
				param.put("type", 2);
//				//岗位职责
//			    List<PostDuty> listDuty =  postDutyMapper.findPostDutyByPostId(param);
//			    //岗位要求
//			    List<PostRequire> listRequire =  postRequireMapper.findPostRequireByPostId(param);
			    //通过岗位类别和岗位名称查找岗位模板
			    paramTemplate = new HashMap<>();
			    paramTemplate.put("categoryId", categoryCode);
			    paramTemplate.put("postName", postName);
			    postTemplate =  postTemplateMapper.findPostTemplateIdByCatPostName(categoryCode,postName);
			    String salaryRange = "";
			    if(postTemplate != null){
			    	salaryRange = postTemplate.getSalaryRange(); //薪资范围
			    }
			    
			//				    Integer applyEmployeeId = getUserInfo(proposerId); //申请人员工Id
			    //通过员工Id查找员工姓名
			    employeeMap =employeeMapper.selectByEmployeeIdForlx(proposerId);
			    if(employeeMap == null){
			    	continue;
			    }
			    if(employeeMap.containsKey("name")){
					if(null != employeeMap.get("name")){
						proposerName = String.valueOf(employeeMap.get("name")); //申请人姓名
					}
					
				}
			    //通过一级部门Id查找一级部门名称
			   department =   departmentMapper.findByDepartmentId(firstDepartmentId);
			   firstDepartmentName =  department.getDepartmentName();
			   department =   departmentMapper.findByDepartmentId(secondDepartmentId);
			   secondDepartmentName =  department.getDepartmentName();
			   //查找岗位类别名称
			   Map<String, Object> paramDic = new HashMap<>();
			   paramDic.put("POST_CATEGORY", DicConstants.POST_CATEGORY);
			   paramDic.put("dicCode", categoryCode);
			   categoryName =  adminDicMapper.findPostCategoryName(paramDic);
			   look = new HashMap<>();
			   look.put("numberPeople", numberPeople); //招聘人数
			   //调用玉伟方法获取面试人数/offer人数/入职人数等信息
			   Integer CountAllInterview = resumePostMapper.selectCountAllInterview(postId);
			   Integer CountAllOffer =  offerMapper.selectCountAllOffer(postId);
			   Integer  CountAllEntry =  employeeEntryMapper.selectCountAllEntry(postId);
			   look.put("totalInterView",CountAllInterview );
			   look.put("offerNum", CountAllOffer);// 已发offer人数
			   look.put("entryNum", CountAllEntry);// 已入职人数
			   
			   List<PositionOperRecond> positionOperRecord = null;	
			   positionOperRecord = operRecordMapper.findOperRecordByPostId(postId);
			   
			   vo.put("record", positionOperRecord); //审批记录
			   vo.put("look", look); //查看
			   vo.put("postId", postId); //岗位申请主键
			   vo.put("applyPerson", proposerName); //申请人姓名
			   vo.put("applyId", proposerId); //申请人ID
			   vo.put("firstDepName", firstDepartmentName);
			   vo.put("seconfDepName", secondDepartmentName);
			   vo.put("categoryName", categoryName); //岗位类别
			   vo.put("postName", postName); //岗位名称
//			   vo.put("listDuty", listDuty);
//			   vo.put("listRequire", listRequire);
			   vo.put("salaryRange", salaryRange);
			   vo.put("closedReason", closedReason); //关闭原因
			   vo.put("otherRequire", otherRequire); //其他要求
			   vo.put("otherDuty", otherDuty); //其他职责
			   
			   listVo.add(vo);
			}
			
		} catch (Exception e) {
			logger.error("findAllPublishPostionApplyn()方法报错"+e.getMessage(),e);
			return  RestUtils.returnFailure("findAllPublishPostionApplyn()方法报错"+e.getMessage());
			
		}
		return RestUtils.returnSuccess(listVo);
	}

	/**
	 * 查询审批人总裁
	 * @return   查询审批人总裁
	 */
	public RestResponse findApprovePersonInDic() {
		List<AdminDic> listApp = null;
		try {
			 listApp = adminDicMapper.findApprove(DicConstants.APPROVE);
		} catch (Exception e) {
			logger.error("findApprovePersonInDic()方法异常"+e.getMessage(),e);
		}
		
		
		return RestUtils.returnSuccess(listApp);
	}
	
	/**
	 * 删除岗位职责或要求
	 * @param  id,
	 *   @param flag
	 * @return  
	 */
	public RestResponse deleteRequireOrDuty(Integer id,String flag) {
		logger.info("执行deleteRequireOrDuty()方法参数{id}",id);
		 Integer postId = null; //岗位申请Id
		 Integer status = null; //状态 1,2,3
		 List<Integer> postIds = new ArrayList<>();
		 try {
			 	
				 if(flag.equals("post_duties")){
					 //通过岗位职责ID查找岗位申请Id
					 PostDuty duty =  postDutyMapper.selectPostDutyById(id);
					 postId = duty.getPostId();
					 postIds.add(postId);
					 List<ErpPost> list =  postMapper.findPostByPostIds(postIds);
					 if(list != null && list.size() > 0){
						 status = list.get(0).getStatus();
						 if(status > 1){
							 return RestUtils.returnSuccess("岗位处于发布或关闭状态不可删除!");
						 }
					 }
					 logger.info("执行删除岗位职责操作参数:id="+id);
						postDutyMapper.deleatePostDuty(id);
					  
					}
			
			if(flag.equals("required")){
				 //通过岗位需求ID查找岗位申请Id
				 PostRequire req =  postRequireMapper.selectPostRequireById(id);
				 postId = req.getPostId();
				 postIds.add(postId);
				 List<ErpPost> list =  postMapper.findPostByPostIds(postIds);
				 if(list != null && list.size() > 0){
					 status = list.get(0).getStatus();
					 if(status > 1){
						 return RestUtils.returnSuccess("岗位处于发布或关闭状态不可删除!");
					 }
				 }
				//执行删除需求
				 logger.info("执行删除岗位要求操作参数:id="+id);
				postRequireMapper.deleatePostRequire(id);
			}
		
		 } catch (Exception e) {
				logger.error("deleteRequireOrDuty()异常"+e.getMessage(),e);
				return RestUtils.returnFailure("error");
			}
		
		return RestUtils.returnSuccess("success");
	}
   /**
    * 查询总面试 简历信息
    * @param postId
    * @return
    */
	public List<Map<String, Object>> findInterviewerById(Integer postId) {
		// TODO Auto-generated method stub
		List<Map<String, Object>> list = null;
		try {
			list = postMapper.findInterviewerResumeByPostId(postId);
		} catch (Exception e) {
			logger.error("查询总面试 简历信息错误:"+e.getMessage(),e);
		}
		
		return list;
	}
/**
 *  查询已发offer 简历信息
 * @param postId
 * @return
 */
public List<Map<String, Object>> findOfferedById(Integer postId) {
	List<Map<String, Object>> list = null;
	try {
		list = postMapper.findOfferedResumeByPostId(postId);
	} catch (Exception e) {
		logger.error("查询已发offer信息错误:"+e.getMessage(),e);
	}
	
	return list;
}
/**
 * 查询已入职
 * @param postId
 * @return
 */
public List<Map<String, Object>> findEntriedById(Integer postId) {
	List<Map<String, Object>> list = null;
	try {
		list = postMapper.findEntriedResumeByPostId(postId);
	} catch (Exception e) {
		logger.error("查询已发offer信息错误:"+e.getMessage(),e);
	
	}
	
	return list;
}
/**
 *根据一级部门ID查询所有二级部门
 * @param firstDepId
 * @return
 */
public List<Map<String,Object>> findAllSecondDepartmentByFirDepId(Integer firstDepId) {
	List<Map<String,Object>> list = null;
	try {
		list = departmentMapper.findAllSecondDepartmentBySupperId(firstDepId);
	} catch (Exception e) {
		logger.error("根据一级部门ID查询所有二级部门错误:"+e.getMessage(),e);
		
	}
	
	return list;
}

/**
 * 关闭岗位
 * @param postId  岗位ID
 * @param reason  关闭原因
 * @param id  当前登录人id
 * @return
 */
public String changeStatus(String operType, Integer postId, String reason,Integer id) {
 logger.info("进入changeStatus方法参数:operType="+operType+" postId="+postId+" reason="+reason+" id = "+id);
 	
 	Integer status = 0;
 	
 	if (operType.equals("close")){
 		//关闭
 		status = 3;
 	}else if (operType.equals("pause")){
 		//暂停
 		status = 6;
 	}else if (operType.equals("activate")){
 		//激活
 		status = 2;
 	}
	
	ErpPost post = new ErpPost();
	post.setPostId(postId);
	post.setStatus(status); 
	try {
		postMapper.updatePost(post);
	} catch (Exception e) {
		logger.error("关闭岗位方法中更新岗位表操作报错："+e.getMessage(),e);
		return  "关闭岗位方法中更新岗位表操作报错："+e.getMessage();
	}
	
	String	token=	request.getHeader("token");
	ErpUser erpUser=(ErpUser)this.redisTemplate.opsForValue().get(token);
	String	employeeName = erpUser.getEmployeeName();
	
	//2.将原因和操作时间插入岗位申请操作记录表  
	PositionOperRecond operRec = new PositionOperRecond();
	Date date = new Date();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //处理时间
	operRec.setCreateTime(format.format(date));
	operRec.setOperContext(reason); //处理内容
	operRec.setCurrentPersonId(id);//当前处理人Id
	operRec.setCurrentPersonName(employeeName);//当前处理人Id
	operRec.setPostId(postId);
	try {
		operRecordMapper.addPositionOperReord(operRec);
	} catch (Exception e) {
		logger.error("关闭岗位方法中插入岗位申请操作记录表报错：",e,e.getMessage());
		return  "关闭岗位方法中插入岗位申请操作记录表报错："+e.getMessage();
	}
	
	return "success";
}
	
/**
 * 根据岗位信息组装数据
 * @param ErpUserId 当前登录人员工ID
 * @return
 */
public Map<String, Object> getPostInfo(ErpPost erpPost)throws Exception{
	logger.info("进入getPostInfo方法");
	Map<String, Object> vo = new HashMap<>();;

	Integer postId = erpPost.getPostId(); //岗位申请主键
	Integer proposerId	= erpPost.getProposerId(); //申请人ID
	Integer firstDepartmentId = erpPost.getFirstDepartment(); //一级部门ID
	Integer secondDepartmentId = erpPost.getSecondDepartment(); //二级部门ID
	String  categoryCode = erpPost.getCategory();  //
	String  postName = erpPost.getPostName(); //岗位名称
	String otherRequire = erpPost.getRequired(); //其他要求
	String otherDuty = erpPost.getDuty(); //其他要求
	Integer numberPeople = erpPost.getNumberPeople(); //招聘人数
	Integer status = erpPost.getStatus(); //岗位状态
	String workAddress = erpPost.getWorkAddress();//工作地点
	String recruitCycle = erpPost.getRecruitCycle();//招聘周期
	Date applyDate=erpPost.getDateSubmit();//申请日期
	Integer positionRankId=erpPost.getPositionRankId();
	Integer rank=null;
	
	Map<String,Object> param = new HashMap<>();
	param.put("postId", postId);
	param.put("type", 2);
//	//岗位职责
//	List<PostDuty> listDuty =  postDutyMapper.findPostDutyByPostId(param);
//	//岗位要求
//	List<PostRequire> listRequire =  postRequireMapper.findPostRequireByPostId(param);
	//职级
	ErpPositionRankRelation positionRankRelation=positionRankRelationMapper.selectErpPositionRankRelationByPostionNo(positionRankId);
	if(positionRankRelation!=null){
	rank=positionRankRelation.getRank();
	}
	//通过岗位类别和岗位名称查找岗位模板
	PostTemplate   postTemplate =  postTemplateMapper.findPostTemplateIdByCatPostName(categoryCode,postName);
	String  salaryRange = "";
	if(postTemplate != null){
		salaryRange = postTemplate.getSalaryRange(); //薪资范围
	}

	//通过员工Id查找员工姓名
	Map<String,Object>  proposer =employeeMapper.selectByEmployeeIdForlx(proposerId);
	String proposerName = String.valueOf(proposer.get("name")); //申请人姓名
	
	//通过一级部门Id查找一级部门名称
	ErpDepartment department =   departmentMapper.findByDepartmentId(firstDepartmentId);
	String firstDepName =  department==null?"":department.getDepartmentName();
	department =   departmentMapper.findByDepartmentId(secondDepartmentId);
	String seconfDepName =  department==null?"":department.getDepartmentName();
	
	//查找岗位类别名称
	Map<String, Object> paramDic = new HashMap<>();
	paramDic.put("POST_CATEGORY", DicConstants.POST_CATEGORY);
	paramDic.put("dicCode", categoryCode);
	String categoryName =  adminDicMapper.findPostCategoryName(paramDic);
	
	if(status == 3){
		//关闭
		String closedReason = "";
		closedReason = operRecordMapper.findCloseRease(postId).getOperContext();  //关闭原因
		vo.put("closedReason", closedReason); //关闭原因
	}	
	
	//审批中、已审批、待提交
	List<PositionOperRecond> positionOperRecord = null;	
	positionOperRecord = operRecordMapper.findOperRecordByPostId(postId);
	vo.put("record", positionOperRecord); //审批记录
	
	if((status == 1) || (status == 4) || (status == 5)){

		//获取当前处理人
		PositionApplyProgress  applyProgress  = applyProgressMapper.findApplyProgressByPostId(postId);
		//通过员工Id查找员工姓名
		Map<String,Object>  currenPerson =employeeMapper.selectByEmployeeIdForlx(applyProgress.getCurrentPersonID());
		logger.info("postId:"+postId+",applyProgress:"+applyProgress+",currenPerson:"+currenPerson);
		
		vo.put("approveId",applyProgress.getCurrentPersonID() ); //当前处理人ID
	    vo.put("approveName",currenPerson==null?"":String.valueOf(currenPerson.get("name"))); //当前处理人姓名
	}
	
	if ((status == 2) || (status == 3) || (status == 6)){
		//发布中、已关闭、暂停
		Map<String, Object> interviewData = new HashMap<>();
		interviewData.put("numberPeople", numberPeople); //招聘人数
		//调用玉伟方法获取面试人数/offer人数/入职人数等信息
		Integer CountAllInterview = resumePostMapper.selectCountAllInterview(postId);
		Integer CountAllOffer =  offerMapper.selectCountAllOffer(postId);
		Integer  CountAllEntry =  employeeEntryMapper.selectCountAllEntry(postId);
		interviewData.put("totalInterView",CountAllInterview );
		interviewData.put("offerNum", CountAllOffer);// 已发offer人数
		interviewData.put("entryNum", CountAllEntry);// 已入职人数
		
		vo.put("interviewData", interviewData); //查看
	}
	 
	vo.put("postId", postId); //岗位申请主键
	vo.put("applyPersonName", proposerName); //申请人
	vo.put("proposerId", proposerId); //申请人ID
	vo.put("firstDepartmentName", firstDepName);
	vo.put("secondDepartmentName", seconfDepName);
	vo.put("postCategoryName", categoryName); //岗位类别
	vo.put("postName", postName); //岗位名称
//	vo.put("listDuty", listDuty);
//	vo.put("listRequire", listRequire);
	vo.put("salaryRange", salaryRange);
	vo.put("otherRequire", otherRequire); //其他要求
	vo.put("otherDuty", otherDuty); //其他职责
	vo.put("numberPeople", numberPeople); //招聘人数	
	vo.put("status", status);
	if(status == 1){//状态
		vo.put("statusName", "审批中");
	}else if(status == 2){
		vo.put("statusName", "发布中");
	}else if(status == 3){
		vo.put("statusName", "已关闭");
	}else if(status == 4){
		vo.put("statusName", "待审批");
	}else if(status == 5){
		vo.put("statusName", "待提交");
	}
	else if(status == 6){
		vo.put("statusName", "暂停");
	}
	vo.put("workAddress", workAddress);
	vo.put("recruitCycle", recruitCycle);
	vo.put("rank", rank);
	vo.put("applyDate", ExDateUtils.dateToString(applyDate,"yyyy-MM-dd"));
	//调用玉伟方法获取面试人数/offer人数/入职人数等信息
    Integer CountAllInterview = resumePostMapper.selectCountAllInterview(postId);
    Integer CountAllOffer =  offerMapper.selectCountAllOffer(postId);
   Integer  CountAllEntry =  employeeEntryMapper.selectCountAllEntry(postId);
   vo.put("totalInterView",CountAllInterview );//总面试数
   vo.put("offerNum", CountAllOffer);// 已发offer人数
   vo.put("entryNum", CountAllEntry);// 已入职人数
				
	return vo;
	
}

	/**
	 * Description: 岗位申请审批通过，状态变为“发布中”后，给岗位申请人、HR发邮件提醒
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年04月29日
	 */
	public boolean sendEmailForAgreeApply(String token,Integer postId,String filename,String filepath) {
		logger.info("sendEmailForAgreeApply方法开始执行，参数是：token={},postId={},filename={},filepath={}",
				token,postId,filename,filepath);
		try {
			//从Redis获取用户信息
			ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);
			String frommail = erpUser.getUsername();//发件人（当前登录人）;
			String bcc = prodEmailInterviewBcc;//抄送HR
			String subject = "岗位申请审批通过";
			//岗位申请表主键查找 岗位申请操作记录
			List<PositionOperRecond> positionOperRecordList = operRecordMapper.findOperRecordByPostId(postId);
			
			StringBuilder text = new StringBuilder();
			text.append("<div id=\"write-custom-write\" tabindex=\"0\" style=\"font-size: 14px; font-family: 宋体; outline: none;\">\r\n" + 
					"    <p>\r\n" + 
					"        您申请的岗位已经审批通过，请在“我申请的岗位”或者“发布中”查看！\r\n" + 
					"    </p>\r\n" + 
					"    <p>\r\n" + 
					"        <br/>\r\n" + 
					"    </p>\r\n" + 
					"    <table class=\"customTableClassName\">\r\n" + 
					"        <tbody>\r\n" + 
					"            <tr class=\"firstRow\">\r\n" + 
					"                <td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">\r\n" + 
					"                    时间\r\n" + 
					"                </td>\r\n" + 
					"                <td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">\r\n" + 
					"                    处理人\r\n" + 
					"                </td>\r\n" + 
					"                <td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">\r\n" + 
					"                    处理记录\r\n" + 
					"                </td>\r\n" + 
					"            </tr>");
			
			for (PositionOperRecond positionOperRecond : positionOperRecordList) {
				text.append("<tr>\r\n" + 
						"                <td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">\r\n" + 
						positionOperRecond.getCreateTime() + 
						"                </td>\r\n" + 
						"                <td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">\r\n" + 
						positionOperRecond.getCurrentPersonName() + 
						"                </td>\r\n" + 
						"                <td width=\"327\" valign=\"top\" style=\"word-break: break-all;\">\r\n" + 
						positionOperRecond.getOperContext() + 
						"                </td>\r\n" + 
						"            </tr>");
			}
			
			text.append("</tbody>\r\n" + 
					"    </table>\r\n" + 
					"    <p>\r\n" + 
					"        <br/>\r\n" + 
					"    </p>\r\n" + 
					"</div>");
			Map<String, Object> postMap = postMapper.findByPostId(postId);
			Integer proposerIdForEmail = Integer.valueOf(String.valueOf(postMap.get("proposerId")));//岗位申请人员工ID
			String tomail = restTemplateUtils.findUsernameByEmployeeId(token, proposerIdForEmail);//岗位申请人员工邮箱
			return restTemplateUtils.sendEmailWithAttachment(frommail, bcc, subject, text.toString(), tomail,
					filename, filepath,DicConstants.POSITION_APPLICATION_APPROVED_EMAIL_TYPE,null);
		} catch (Exception e) {
			logger.info("sendEmailForAgreeApply方法出现异常：{}",e.getMessage(),e);
			return false;
		}
	}

	/**
	 * 查询所有岗位列表
	 * @param erpPositionQueryParamVO 查询参数
	 * @param page 页码
	 * @param rows 每页行数
	 * @return
	 */
	public RestResponse findAllPositionList(ErpPositionQueryParamVO erpPositionQueryParamVO) {
		List<ErpPositionQueryResultVO> list = null;
//		Integer totalNumber = 0;
		try {
//			erpPositionQueryParamVO.setBeginNumber((page - 1) * rows);
//			erpPositionQueryParamVO.setEndNumber(rows);
			list = postMapper.findAllPositionList(erpPositionQueryParamVO);
//			totalNumber = postMapper.countPositionList(erpPositionQueryParamVO);
		} catch (Exception e) {
			logger.error("查询所有岗位列表错误:{}",e.getMessage(),e);
		}
//		return RestUtils.returnSuccess(new Page<>(totalNumber, list));
		return RestUtils.returnSuccess(list);
	}

	/**
	 * 查询岗位详情
	 * @param postId 岗位id
	 * @return
	 */
	public RestResponse findPositionDetail(Integer postId) {
		Map<String, Object> returnVO = null;
		//返回前端Vo
		try {
			returnVO = postMapper.findPositionDetailByPostId(postId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("查询岗位详情错误:{}",e.getMessage(),e);
		}
		return RestUtils.returnSuccess(returnVO);
	}
	
	/**
	 * 查询优先级列表
	 * @author xujianhao
	 * @return
	 */
	public RestResponse findLevelPriorityList() {
		List<Map<String, Object>> list = null;
		try {
			list = adminDicMapper.findAllJobCategoryInt(DicConstants.LEVEL_PRIORITY);
		} catch (Exception e) {
			logger.error("查询优先级列表错误:{}",e.getMessage(),e);
		}
		return RestUtils.returnSuccess(list);
	}

	/**
	 * 查询招聘原因列表
	 * @author xujianhao
	 * @return
	 */
	public RestResponse findReasonRecruitList() {
		List<Map<String, Object>> list = null;
		try {
			list = adminDicMapper.findAllJobCategoryInt(DicConstants.REASON_RECRUIT);
		} catch (Exception e) {
			logger.error("查询招聘原因列表错误:{}",e.getMessage(),e);
		}
		return RestUtils.returnSuccess(list);
	}

	/**
	 * 修改HR负责人
	 * @param erpPositionQueryParamVO postId 岗位ID hrChargeId HR负责人ID
	 * @return
	 */
	public RestResponse updateHrCharge(ErpPositionQueryParamVO erpPositionQueryParamVO) {
		try {
			postMapper.updateHrChargeById(erpPositionQueryParamVO.getPostId(), erpPositionQueryParamVO.getHrChargeId());
		} catch (Exception e) {
			logger.error("修改HR负责人错误:{}",e.getMessage(),e);
		}
		return RestUtils.returnSuccess("修改成功!");
	}

	/**
	 * 导出所有岗位列表
	 * @param erpPositionQueryParamVO
	 * @return
	 */
    public RestResponse exportAllPositionList(ErpPositionQueryParamVO erpPositionQueryParamVO) {
		logger.info("exportAllPositionList方法开始执行");
		// 定义工作簿
		XSSFWorkbook workBook = null;
		try {
			List<ErpPositionQueryResultVO> list = postMapper.findAllExportPositionList(erpPositionQueryParamVO);
			workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet("岗位申请");
			// 生成第一行（表头行）
			XSSFRow firstRow = sheet.createRow(0);
			int number = 0;
			firstRow.createCell(number++).setCellValue("二级部门");
			firstRow.createCell(number++).setCellValue("三级部门");
			firstRow.createCell(number++).setCellValue("岗位");
			firstRow.createCell(number++).setCellValue("招聘人数");
			firstRow.createCell(number++).setCellValue("接口人");
			firstRow.createCell(number++).setCellValue("招聘原因");
			firstRow.createCell(number++).setCellValue("负责人");
			firstRow.createCell(number++).setCellValue("优先级");
			firstRow.createCell(number++).setCellValue("薪资范围");
			firstRow.createCell(number++).setCellValue("申请时间");
			// 第二行数据行
			XSSFRow nextRow = null;
			ErpPositionQueryResultVO erpPositionQueryResultVO = null;
			// 循环 填充表格
			for (int dataRowNumber = 0; dataRowNumber < list.size(); dataRowNumber++) {
				erpPositionQueryResultVO = list.get(dataRowNumber);
				nextRow = sheet.createRow(dataRowNumber + 1);
				int cellNumber = 0;
				nextRow.createCell(cellNumber++).setCellValue(erpPositionQueryResultVO.getFirstDepartmentName() == null ? "" : erpPositionQueryResultVO.getFirstDepartmentName());
				nextRow.createCell(cellNumber++).setCellValue(erpPositionQueryResultVO.getSecondDepartmentName() == null ? "" : erpPositionQueryResultVO.getSecondDepartmentName());
				nextRow.createCell(cellNumber++).setCellValue(erpPositionQueryResultVO.getPostName() == null ? "" : erpPositionQueryResultVO.getPostName());
				nextRow.createCell(cellNumber++).setCellValue(erpPositionQueryResultVO.getNumberPeople() == null ? "" : String.valueOf(erpPositionQueryResultVO.getNumberPeople()));
				nextRow.createCell(cellNumber++).setCellValue(erpPositionQueryResultVO.getPrincipalName() == null ? "" : erpPositionQueryResultVO.getPrincipalName());
				nextRow.createCell(cellNumber++).setCellValue(erpPositionQueryResultVO.getReasonRecruit() == null ? "" : erpPositionQueryResultVO.getReasonRecruit());
				nextRow.createCell(cellNumber++).setCellValue(erpPositionQueryResultVO.getHrCharge() == null ? "" : erpPositionQueryResultVO.getHrCharge());
				nextRow.createCell(cellNumber++).setCellValue(erpPositionQueryResultVO.getLevelPriorityId() == null ? "" : String.valueOf(erpPositionQueryResultVO.getLevelPriorityId()));
				nextRow.createCell(cellNumber++).setCellValue(erpPositionQueryResultVO.getSalaryRange() == null ? "" : erpPositionQueryResultVO.getSalaryRange());
				nextRow.createCell(cellNumber++).setCellValue(erpPositionQueryResultVO.getApplyDate() == null ? "" : erpPositionQueryResultVO.getApplyDate());
			}
			this.exportExcelToComputer(workBook, "岗位申请.xlsx");
			return RestUtils.returnSuccessWithString("导出成功！");
		} catch (Exception e) {
			logger.error("exportAllPositionList方法出现异常："+e.getMessage(),e);
			return RestUtils.returnFailure("方法出现异常，导致导出失败！");
		}
    }

	public void exportExcelToComputer(XSSFWorkbook workBook, String fileName) throws IOException {
		logger.info("exportExcelToComputer方法开始执行，参数是：fileName="+fileName);
		// 本地测试导出文件
//		FileOutputStream fos = new FileOutputStream("C:\\doc\\"+fileName);
//		workBook.write(fos);
//		fos.flush();
//		fos.close();

		// 与前端联调导出文件
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
		response.addHeader("Content-Disposition", "attachment;filename="+fileName);
		ServletOutputStream os = response.getOutputStream();
		workBook.write(os);
		os.flush();
		os.close();
	}
}
