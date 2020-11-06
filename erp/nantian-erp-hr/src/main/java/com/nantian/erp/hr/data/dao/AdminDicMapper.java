package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.hr.data.model.AdminDic;

/**
 * 字典Mapper
 *
 */
public interface AdminDicMapper {
	//从字典表中查询全部的岗位类别
	public List<Map<String, Object>> findAllCategoryFromAdminDic(String POST_CATEGORY);
	
	//新增岗位类别
	public  int addPositionCategory(AdminDic dic);
	
	//从字典表中查询全部的职位类别
	public List<Map<String, Object>> findAllJobCategory(String JOB_CATEGORY);
	
	//查找最后一次插入的岗位类别信息
	public  AdminDic findLastInsertPostCategory(String POST_CATEGORY);

	//根据岗位码值查找岗位名称
	public String findPostCategoryName(Map<String, Object> param);
	
	//根据职位码值查找职位名称
	public String findJobCategoryName(Map<String, Object> param);
	
	//根据职位码值查找职位子类的信息(码值，名称)
	public List<AdminDic> findtPostChildByJobId(String  jobId);
	
	//查询所有族
	public List<AdminDic> findAllFamily(String familyId);
	
	//查找审批人
	
	public List<AdminDic> findApprove(String APPROVER);
	//通用方法(通过类型和码值查找字典数据)
	public  AdminDic commonFindDicByTypeCode(Map<String, Object> param);
	
	public String findUpperUIdByDictype(String dicCode);
	
	public List<Map<String, Object>> findAllGroupsFromAdminDic();
	
	public List<Map<String, Object>> findAllPoliticalFromAdminDic();

	public List<Map<String, Object>> findAllJobCategoryInt(String JOB_CATEGORY);	
}
