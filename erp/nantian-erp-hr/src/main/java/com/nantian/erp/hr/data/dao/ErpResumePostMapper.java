package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.hr.data.model.ErpPositionRankRelation;
import com.nantian.erp.hr.data.model.ErpResumePost;

/** 
 * Description: 面试mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月06日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpResumePostMapper {
	
	//新增简历和岗位关联关系（增加一条面试记录）
	public void insertResumePost(ErpResumePost resumePost);
	
	//通过面试ID删除简历和岗位关联关系（增加一条面试记录）
	public void deleteByInterviewId(Integer interviewId);
	
	//通过岗位ID删除简历和岗位关联关系（增加多条面试记录）
	public void deleteByPostId(Integer postId);
	
	//修改面试流程信息
	public void updateResumePost(ErpResumePost resumePost);
	
	//条件查询面试流程表
	public List<Map<String,Object>> findResumePostInfoByParams(Map<String,Object> paramsMap);
	
	//根据面试ID查询记录（现场面试）
	public Map<String,Object> findResumePostInfoForPlace(Integer interviewId);
	
	//根据面试ID查询记录（电话面试）
	public Map<String,Object> findResumePostInfoForPhone(Integer interviewId);
	
	//根据面试ID查询记录
	public Map<String,Object> findResumePostInfoById(Integer interviewId);
	
	//根据岗位Id查询其下全部的职位职级的对应关系
	public List<Map<String,Object>> findPositionRankList(Integer postId);
	
	//查询一个岗位的总面试人数
	public Integer selectCountAllInterview(Integer postId);
	
	//根据岗位ID查询全部记录
	public List<ErpResumePost> findByPostId(Integer postId);

	public ErpPositionRankRelation findTraineePositionRankList();
	
	/**
	 * 根据简历id查询面试id列表
	 * @param resumeId 简历id
	 * @return
	 */
	public List<Integer> findIdsByResumeId(Integer resumeId)throws Exception;

	/**
	 * 根据面试id列表修改面试为失效
	 * @param resumePostIds 面试id
	 */
	public void updateValidFalseByIds(@Param("resumePostIds") List<Integer> resumePostIds)throws Exception;

	
}
