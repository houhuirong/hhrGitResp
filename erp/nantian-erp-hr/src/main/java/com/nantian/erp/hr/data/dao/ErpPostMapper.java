package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

import com.nantian.erp.hr.data.model.ErpPost;
import com.nantian.erp.hr.data.vo.ErpPositionQueryParamVO;
import com.nantian.erp.hr.data.vo.ErpPositionQueryResultVO;

/**
 * 岗位Mapper
 * @author ZhangYuWei
 */
public interface ErpPostMapper {
	
	//add by 曹秀彬  查询 所有的岗位  根据参数isClosed 判断结果是 发布中的或是已关闭的
	public List<Map<String, Object>> findPostByIsClosed(Map<String, Object> param);
	
	//add by 曹秀彬  统计该岗位已入职人数
	public Integer getPostCountForEntry(@Param(value = "postId") Integer postId);
	
	//插入岗位信息
	public  Integer insertPost(ErpPost post); 
	
	//修改岗位信息
	public void updatePost(ErpPost post);
	
	//查询全部的岗位列表
	public List<ErpPost> findAllPostList(Map<String,Object> map);
	
	//通过岗位ID查询岗位信息
	public Map<String,Object> findByPostId(Integer postId);
	
	//通过岗位ID查询岗位信息 20200110 SXG
	public Map<String,Object> findByPostIdNew(Integer postId);
	
	//通过岗位是否关闭查询岗位信息
	public List<Map<String,Object>> findByIsClosed(Boolean isClosed);
	
	//从字典表中查询全部的岗位类别
	public List<Map<String,Object>> findAllCategoryFromAdminDic();
	
	//通过岗位ID查询全部的简历列表（面试）
	public List<Map<String,Object>> findInterviewerResumeByPostId(Integer postId);
	
	//通过岗位ID查询全部的简历列表（已发offer）
	public List<Map<String,Object>> findOfferedResumeByPostId(Integer postId);
	
	//通过岗位ID查询全部的简历列表（入职）
	public List<Map<String,Object>> findEntriedResumeByPostId(Integer postId);
	
	//通过岗位申请IdS查找岗位申请信息
	public List<ErpPost> findPostByPostIds(List<Integer> postIds); //findPostByPostIds
	//根据岗位申请人Id查询岗位申请信息
	public List<ErpPost> findPostByProposerId(Map<String,Object> map);
	
	//根据主键删除岗位申请信息
	public int deleteById(@Param("postId") Integer postId );
	
	//查询发布中的岗位
	public List<ErpPost> findPostByStatus(Map<String, Object> param);
	//判断有岗位申请的数量
	public Integer countPostStatus(@Param(value = "postTemplateId") Integer postTemplateId);
  
	//查找重复的岗位申请
	public Integer findRepeatPost(@Param("proposerId") Integer proposerId,@Param("postName") String postName);
	
	//通过主键查找岗位
	public ErpPost findPostByPostId(@Param("postId") Integer postId);

	/**
	 * 
	 * @param erpPositionQueryParamVO 查询参数
	 * @return
	 * @throws Exception
	 */
	public List<ErpPositionQueryResultVO> findAllPositionList(ErpPositionQueryParamVO erpPositionQueryParamVO) throws Exception;

	/**
	 * 查询所有岗位列表总数
	 * @param erpPositionQueryParamVO 查询参数
	 * @return
	 */
	public Integer countPositionList(ErpPositionQueryParamVO erpPositionQueryParamVO) throws Exception;

	/**
	 * 根据岗位id修改HR负责人
	 * @param postId 岗位ID
	 * @param hrChargeId HR负责人ID
	 * @throws Exception
	 */
	public void updateHrChargeById(@Param("postId") Integer postId,@Param("hrChargeId") Integer hrChargeId) throws Exception;

	/**
	 * 根据岗位id查询岗位详情
	 * @param postId 岗位id
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> findPositionDetailByPostId(Integer postId)throws Exception;

	/**
	 * 查询导出所有岗位列表
	 * @param erpPositionQueryParamVO
	 * @return
	 */
	List<ErpPositionQueryResultVO> findAllExportPositionList(ErpPositionQueryParamVO erpPositionQueryParamVO);

	/**
	 * 根据员工查询待审批的岗位
	 * @param employeeId
	 * @return
	 * @throws Exception
	 */
	List<ErpPost> findWaitPostByProposerId(Integer employeeId)throws Exception;
}
