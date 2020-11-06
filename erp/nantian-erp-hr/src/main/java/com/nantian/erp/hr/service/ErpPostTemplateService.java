package com.nantian.erp.hr.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nantian.erp.common.base.Pojo.ErpUser;
import com.nantian.erp.common.base.util.ExDateUtils;
import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;
import com.nantian.erp.hr.constants.DicConstants;
import com.nantian.erp.hr.data.dao.AdminDicMapper;
import com.nantian.erp.hr.data.dao.ErpPositionRankRelationMapper;
import com.nantian.erp.hr.data.dao.ErpPostMapper;
import com.nantian.erp.hr.data.dao.PostDutyMapper;
import com.nantian.erp.hr.data.dao.PostRequireMapper;
import com.nantian.erp.hr.data.dao.PostTemplateMapper;
import com.nantian.erp.hr.data.dao.PostTemplateRecordMapper;
import com.nantian.erp.hr.data.model.AdminDic;
import com.nantian.erp.hr.data.model.PostTemplate;
import com.nantian.erp.hr.data.model.PostTemplateRecord;


@Service
public class ErpPostTemplateService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

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
	private ErpPositionRankRelationMapper positionRankRelationMapper;
	
	@Autowired
	private PostTemplateRecordMapper postTemplateRecordMapper;
	
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;
	@Autowired
	private HttpServletRequest request;

	/**
	 * 新增岗位模板
	 * @param post
	 * @return
	 */
	@Transactional(readOnly=false)
	public RestResponse insertPostTemplate(Map<String, Object> postTemplate) {
		
		logger.info("进入insertPostTemplate方法，参数是："+postTemplate.toString());
		//select dic_code as "categoryId",dic_name as "categoryName"
		String token = request.getHeader("token");
		ErpUser erpUser = (ErpUser) redisTemplate.opsForValue().get(token);//从缓存中获取登录用户信息
		Integer id = erpUser.getUserId();// 从用户信息中获取角色信息
		String categoryId = null; //岗位类别码值
		String  postName = null; //岗位名称
		String familyId = null; //职位族码值
		String jobId = null; //职位类别码值
		String duty = null; //岗位职责
		String require = null; //岗位要求
		String salaryRange = null; //岗位要求
		String childId = null; //职位类别子类码值
		PostTemplate template = new PostTemplate(); //创建模板对象
		PostTemplateRecord templateRecord = new PostTemplateRecord();
		if(postTemplate.containsKey("categoryId")){//岗位类别码值
			if(null != postTemplate.get("categoryId")){
				categoryId = String.valueOf(postTemplate.get("categoryId"));
			}
			
		}
		if(postTemplate.containsKey("postName")){//岗位名称
			if(null != postTemplate.get("postName")){
				postName = String.valueOf(postTemplate.get("postName"));
			}
			
		}
		if(postTemplate.containsKey("familyId")){//职位族码值
			if(null != postTemplate.get("familyId")){
				familyId = String.valueOf(postTemplate.get("familyId"));
			}
			
		}
		if(postTemplate.containsKey("jobId")){//职位类别码值
			if(null != postTemplate.get("jobId")){
				jobId = String.valueOf(postTemplate.get("jobId"));
			}
			
		}
		if(postTemplate.containsKey("salaryRange")){//薪资范围
			if(null != postTemplate.get("salaryRange")){
				salaryRange = String.valueOf(postTemplate.get("salaryRange"));
			}
			
		}
		if(postTemplate.containsKey("childId")){//职位类别子类码值
			if(null != postTemplate.get("childId")){
				childId = String.valueOf(postTemplate.get("childId"));
			}
			
		}
		if(postTemplate.containsKey("postduty")){//职位类别子类名称
			if(null != postTemplate.get("postduty")){
				duty = String.valueOf(postTemplate.get("postduty"));
			}
			
		}
		if(postTemplate.containsKey("postRequire")){//职位类别子类名称
			if(null != postTemplate.get("postRequire")){
				require = String.valueOf(postTemplate.get("postRequire"));
			}
			
		}
		template.setCategory(categoryId);//岗位类别码值
		template.setJobCategory(jobId);//职位类别码值
		template.setPostName(postName); 
		template.setSalaryRange(salaryRange);
		template.setPositionChildType(childId);//职位类别子类码值
		template.setFamilyId(familyId);//职位职级族码值
		template.setDuty(duty);//职位职级族码值
		template.setRequired(require);//职位职级族码值
		//模板记录
		templateRecord.setModifiedUser(id);
		templateRecord.setGmtCreate(ExDateUtils.getCurrentStringDateTime());
		templateRecord.setModifiedTime(ExDateUtils.getCurrentStringDateTime());
		templateRecord.setGmtModified(ExDateUtils.getCurrentStringDateTime());
		
		try {
			///判断执行是插入还是修改操作postTemplateId
			if(postTemplate.get("postTemplateId") != null){
				template.setPostTemplateId((Integer) postTemplate.get("postTemplateId"));
			
				postTemplateMapper.updatePostTemplate(template);
				templateRecord.setPostTemplateId((Integer) postTemplate.get("postTemplateId"));
				postTemplateRecordMapper.addPostTemplateRecord(templateRecord);

			}
			else{//插入模板
				 postTemplateMapper.addPostTemplate(template);
				 templateRecord.setPostTemplateId(template.getPostTemplateId());
				 postTemplateRecordMapper.addPostTemplateRecord(templateRecord);
			}
		
		} catch (Exception e) {
			logger.error("insertPostTemplate方法出现异常：" + e.getMessage());
			RestUtils.returnFailure("insertPostTemplate方法出现异常：" + e.getMessage());
		}
		
		return RestUtils.returnSuccess("OK");
	}

	/**
	 * 查询所有岗位模板
	 * @param familyId 职位族id
	 * @param jobId 职位类别id
	 * @param childId 职位子类id
	 * @param categoryId 岗位类别id
	 * @param postName 岗位名称
	 * @return
	 */
	public RestResponse findAllPostTemplate(String familyId,String jobId,String childId,String categoryId,String postName) {
		logger.info("进入findAllPostTemplate方法");
		
		List<Map<String, Object>>  list = new ArrayList<>();  //返回前端的数据
		try {
			list = postTemplateMapper.findAllPostTemplate(familyId, jobId, childId, categoryId, postName);
			//遍历全部的岗位模板
			for(Map<String,Object> map:list){
				Integer postTemplateId = Integer.valueOf(String.valueOf(map.get("postTemplateId"))); //岗位模板编号
				Map<String,Object> recordMap=postTemplateRecordMapper.findPostTemplateRecordById(postTemplateId);
				if(recordMap != null){
					map.put("modifiedTime", recordMap.get("modifiedTime"));
					map.put("modifiedUserId", recordMap.get("modifiedUserId"));
					map.put("modifiedUserName", recordMap.get("modifiedUserName"));
				}else{
					map.put("modifiedTime", "");
					map.put("modifiedUserId", "");
					map.put("modifiedUserName", "");
				}
			}
			//遍历全部的岗位模板
				/*for(int i=0;i<list.size();i++){
				mapVo = new HashMap<>();
				param = new HashMap<>();
				postTemplateId = list.get(i).getPostTemplateId(); //岗位模板编号
				postName = list.get(i).getPostName(); //岗位名称
				salaryRange = list.get(i).getSalaryRange(); //薪资范围
				categoryId = list.get(i).getCategory();//获得岗位类别码值
				jobId = list.get(i).getJobCategory();//获得职位类别码值
				childId = list.get(i).getPositionChildType();//职位类别子类码值
				familyId = list.get(i).getFamilyId();// 职位职级族码值
				duty = list.get(i).getDuty();// 职位职级族码值
				require = list.get(i).getRequired();// 职位职级族码值
				 //根据岗位类别码值和字典类型查找岗位类别名称
				param.put("dicCode", categoryId);
				param.put("POST_CATEGORY", DicConstants.POST_CATEGORY);
				categoryName = adminDicMapper.findPostCategoryName(param);
				//根据族码值和定义的类型查找族的名称
				param.clear();
				param.put("dicCode", familyId);
				param.put("dicType", DicConstants.JOB_FAMILY);
				
				AdminDic dicFamily = adminDicMapper.commonFindDicByTypeCode(param);
				familyName = dicFamily.getDicName();
				 //根据职位类别码值和字典类型查找职位类别名称
				param.clear();
				param.put("dicCode", jobId);
				param.put("dicType", familyId);
				AdminDic job = adminDicMapper.commonFindDicByTypeCode(param);
				if (job!=null){
					jobName = job.getDicName();
				}
				//根据职位子类码值查找 子类名称
				param.clear();
				param.put("dicCode", childId);
				param.put("dicType", familyId+jobId);
				AdminDic job2 = adminDicMapper.commonFindDicByTypeCode(param);	
				if (job2!=null){
					jobName = job2.getDicName();
				}*/
				
				
				//查找岗位职责信息
//				param.clear();
//				param.put("postId", postTemplateId);
//				param.put("type", 1); //1岗位模板2是岗位申请
//				List<PostDuty> listDuty = postDutyMapper.findPostDutyByPostId(param);
				//查找岗位要求
//				param.clear();
//				param.put("postId", postTemplateId);
//				param.put("type", 1); //1岗位模板2是岗位申请
//				List<PostRequire> listRequire = postRequireMapper.findPostRequireByPostId(param);
				
				/*mapVo.put("postTemplateId", postTemplateId);
				mapVo.put("postName", postName);
				mapVo.put("categoryId", categoryId);
				mapVo.put("categoryName", categoryName);
				mapVo.put("jobId", jobId);
				mapVo.put("jobName", jobName);
				mapVo.put("salaryRange", salaryRange);
				mapVo.put("childId", childId);
				mapVo.put("childName", childName);
				mapVo.put("familyId", familyId);
				mapVo.put("familyName", familyName);
				mapVo.put("postduty", duty);
				mapVo.put("postRequire", require);
				mapVo.put("jobType", familyId); //拼接的子类的(字典类型)前端用于修改
				listVo.add(mapVo);*/
			
		} catch (Exception e) {
			logger.error("findAllPostTemplate方法出现异常：" + e.getMessage(),e);
		  return	RestUtils.returnFailure("findAllPostTemplate方法出现异常：" + e.getMessage());
		}
		return RestUtils.returnSuccess(list);
	}
	
	/**
	 * 从字典获取所有的岗位类别  字典类型 :POST_CATEGORY
	 * @return
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
	 * 从字典获取所有的职位类别 
	 * @return
	 */
	public RestResponse findAllJobCategory(String dicCode){
		List<Map<String, Object>>  list = null;
		List<Map<String, Object>>  listVo = new ArrayList<>();
		try {//jobId,jobName
			
			list = adminDicMapper.findAllJobCategory(dicCode); 
			
		} catch (Exception e) {
			logger.error("findAllPositionCategory方法出现异常：" + e.getMessage());
			return	RestUtils.returnFailure("findAllJobCategory方法出现异常：" + e.getMessage());
		}
		String jobId = "";
		Map<String, Object> param = null;
		for(Map<String, Object> map :list){
			 param = new HashMap<>();
			jobId = String.valueOf(map.get("jobId"));
			//将类型和码值拼接在一起返回前台用于子类的查询 = JOB_CATEGORY_TECH+1
//			param.put("jobId", dicCode+jobId); //类型+码值
			param.put("jobName",map.get("jobName"));//名称
			param.put("dicType",dicCode); //类型
			param.put("jobId",jobId); //码值
			listVo.add(param);
		}
		
		return  RestUtils.returnSuccess(listVo);
	}
	/**
	 * 从字典获取最后一次插入的岗位类别： JOB_CATEGORY 
	 * @return
	 */
	private  AdminDic findLastInsertPostCategory(){
		AdminDic  adminDic = null;
		try {
			adminDic = adminDicMapper.findLastInsertPostCategory(DicConstants.POST_CATEGORY); 
		} catch (Exception e) {
			logger.error("findLastInsertPostCategory方法出现异常：" + e.getMessage());
		}
		
		return adminDic;
	}
	
	/**
	 * 插入岗位类别到字典表
	 * @return
	 */
	@Transactional
	public  RestResponse insertPostCategory(Map<String,Object> param){
		String  categoryName = "";
		AdminDic  dic = null;
		Integer dicCode = 0;
		if(param.containsKey("categoryName")){//获得岗位类别名称
			categoryName = String.valueOf(param.get("categoryName"));
			//从字典获取最后一次插入的岗位类别信息
			dic = findLastInsertPostCategory();
			if(dic != null){
				 dicCode = new Integer(String.valueOf(dic.getDicCode()));
				dicCode = dicCode + 1;
			}
			
			dic.setDicCode(String.valueOf(dicCode)); //岗位标识
			dic.setDicName(categoryName); //岗位类别名称
			dic.setDicType(DicConstants.POST_CATEGORY); //岗位类别
			try {
				adminDicMapper.addPositionCategory(dic); 
			} catch (Exception e) {
				logger.error("insertPostCategory方法出现异常：" + e.getMessage());
				return	RestUtils.returnFailure("insertPostCategory方法出现异常：" + e.getMessage());
			}
		}else{
			RestUtils.returnSuccess("没传入数据不能插入");
		}
		
		
		return RestUtils.returnSuccess("OK");
	}
	
	/**
	 * 根据岗位模板查找岗位模板信息
	 * @param postTemplateId
	 * @return
	 */
	public RestResponse findtPostTemplateById(Integer postTemplateId) {
		logger.info("进入findtPostTemplateById方法:参数"+postTemplateId);
		String categoryId = ""; //岗位类别码值
		String categoryName = ""; //岗位类别名称
		String jobId = ""; //职位类别码值
		String jobName = ""; //职位类别名称
		String postName = ""; //岗位名称
		String salaryRange = ""; //岗位名称
		String duty = "";
		String require = "";
		Map<String,Object> param = null; //查找岗位,职位类别名称参数
		Map<String, Object> mapVo  = null; //Vo对象
//		List<Map<String, Object>>  listVo = new ArrayList<>();  //返回前端的数据
		try {
			PostTemplate list = postTemplateMapper.findtPostTemplateById(postTemplateId);
			//遍历岗位模板
				mapVo = new HashMap<>();
				param = new HashMap<>();
				postTemplateId = list.getPostTemplateId(); //岗位模板编号
				postName = list.getPostName(); //岗位名称
				salaryRange = list.getSalaryRange(); //薪资范围
				categoryId = list.getCategory();//获得岗位类别码值
				jobId = list.getJobCategory();//获得职位类别码值
				duty = list.getDuty();// 职位职级族码值
				require = list.getRequired();// 职位职级族码值
				 //根据岗位类别码值和字典类型查找岗位类别名称
				param.put("dicCode", categoryId);
				param.put("POST_CATEGORY", DicConstants.POST_CATEGORY);
				categoryName = adminDicMapper.findPostCategoryName(param);
				 //根据职位类别码值和字典类型查找职位类别名称
				param.clear();
				param.put("dicCode", jobId);
				param.put("JOB_CATEGORY", DicConstants.JOB_CATEGORY);
				jobName = adminDicMapper.findJobCategoryName(param);
				
//				//查找岗位职责信息
//				param.clear();
//				param.put("postId", postTemplateId);
//				param.put("type", 1); //1岗位模板2是岗位申请
//				List<PostDuty> listDuty = postDutyMapper.findPostDutyByPostId(param);
//				//查找岗位要求
//				param.clear();
//				param.put("postId", postTemplateId);
//				param.put("type", 1); //1岗位模板2是岗位申请
//				List<PostRequire> listRequire = postRequireMapper.findPostRequireByPostId(param);
			
				mapVo.put("postTemplateId", postTemplateId);
				mapVo.put("postName", postName);
				mapVo.put("categoryId", categoryId);
				mapVo.put("categoryName", categoryName);
				mapVo.put("jobId", jobId);
				mapVo.put("jobName", jobName);
				mapVo.put("salaryRange", salaryRange);
				mapVo.put("postduty", duty);
				mapVo.put("postRequire", require);
//				listVo.add(mapVo);
		} catch (Exception e) {
			logger.error("findtPostTemplateById方法出现异常：" + e.getMessage());
			return RestUtils.returnFailure("findtPostTemplateById方法出现异常：" + e.getMessage());
		}
		return RestUtils.returnSuccess(mapVo);
	}
	/**
	 * 根据职位类别码值查找职位子类
	 * @param jobId   职位类别码值
	 * @return  职位子类的码值和名称
	 */
	public RestResponse findtPostChildByJobId(String dicType, String dicCode) {
		Map<String, Object> param = null;
		List<Map<String, Object>> list = new ArrayList<>();
		List<AdminDic> childList = null;
		String  jobId = dicType+dicCode;
		try {
			 childList = adminDicMapper.findtPostChildByJobId(jobId);
			 for(AdminDic dic:childList){
					param = new HashMap<>();
					param.put("childId", dic.getDicCode()); // 子类码值
					param.put("childName", dic.getDicName()); //子类名称
					list.add(param);
				}
		} catch (Exception e) {
			return	RestUtils.returnFailure("findtPostChildByJobId方法出现异常：" + e.getMessage());
		}
		
		/* "childId":"职位类别子类码值",
		"childName":"职位类别名称"*/
		
		return RestUtils.returnSuccess(list);
	}
	
	/**
	 * 查询所有族
	 * @return
	 */
	public RestResponse findAllFamily(String familyId) {
		List<AdminDic> list = null;
		try {
			list = adminDicMapper.findAllFamily(familyId);
		} catch (Exception e) {
			RestUtils.returnFailure("findAllFamily方法出现异常：" + e.getMessage());
		}
		 
		Map<String, Object> map = null;
		List<Map<String, Object>> listMap = new ArrayList<>();
		for(AdminDic dic :list){
			map = new HashMap<>();//JOB_CATEGORY_TECH JOB_CATEGORY_TECH
			//返回前端标识，用于判断将技术族默认放在前边
			if(dic.getDicCode().equals(DicConstants.JOB_CATEGORY)){//字典常量
				map.put("isFlag", "1");
				
			}
			map.put("familyId", dic.getDicCode());
			map.put("familyName", dic.getDicName());
			listMap.add(map);
		}
		return RestUtils.returnSuccess(listMap);
	}
	/**
	 * 删除岗位模板及相关信息
	 * @param postTemplateId  模板主键
	 * @return
	 */
	@Transactional
	public RestResponse deletePostTemplateById(Integer postTemplateId) {
		//判断有业务数据不能删除
		Integer n = postMapper.countPostStatus(postTemplateId);	
		if(n > 0){
		   return	RestUtils.returnSuccessWithString("有岗位申请数据，不能删除!");
		}
		
		//查找岗位模板
//		PostTemplate template = postTemplateMapper.findtPostTemplateById(postTemplateId);
		//删除岗位职责
//		postDutyMapper.deletePostDutyByPostId(postTemplateId,1); //1代表岗位模板
//		//删除岗位要求
//		postRequireMapper.deletePostRequireByPostId(postTemplateId,1);
//		
		postTemplateMapper.deleatePostTemplate(postTemplateId);
		
		return RestUtils.returnSuccess();
	}
	/**
	 * 验证岗位名称唯一
	 * @param categoryId  岗位类别码值
	 * @param postName     岗位名称
	 * @return
	 */
	public RestResponse validatePostionName(String categoryId, String postName) {
		Map<String, Object> param = new HashMap<>();
		param.put("category", categoryId);
		param.put("postName", postName);
		Boolean f = false;
		PostTemplate template  = postTemplateMapper.findPostTemplateIdByCatPostName(categoryId,postName);
		if(template == null){
			f = true;
			return RestUtils.returnSuccess(f);
		}
		return RestUtils.returnSuccess(f);
	}
	
	/**
	 * Description: 职位职级的薪资范围设置
	 *
	 * @return
	 * @Author ZhangYuWei
	 * @Create Date: 2019年02月18日 下午14:42:48
	 */
	public RestResponse findPositionRankList(Map<String,Object> params) {
		logger.info("findPositionRankList方法开始执行，参数是：params="+params);
		try{
			/*
			 * 根据职位类别、职位子类、职位族类查询职位名称、职级列表
			 */
			List<Map<String,Object>> positionRankList = positionRankRelationMapper.selectPositionRankList(params);
			logger.info("positionRankList="+positionRankList);
			return RestUtils.returnSuccess(positionRankList);
		}catch(Exception e){
			logger.error("findPositionRankList方法出现异常:"+e.getMessage(),e);
			return RestUtils.returnFailure("方法发生异常，导致查询失败！");
		}
	}

	
}
