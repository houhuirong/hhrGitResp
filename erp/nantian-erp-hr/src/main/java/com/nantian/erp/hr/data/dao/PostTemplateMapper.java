package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.hr.data.model.PostTemplate;

/**
 * 岗位模板Mapper
 * @author lx
 */
public interface PostTemplateMapper {
	
	//插入岗位模板信息
	public Integer addPostTemplate(PostTemplate postTemplate);
	
	//删除岗位模板信息
	public Integer deleatePostTemplate(Integer postTemplateId);
	
	//查询全部岗位模板
	public List<Map<String,Object>> findAllPostTemplate(@Param ("familyId") String familyId,@Param ("jobId") String jobId,@Param ("childId") String childId,
														@Param ("categoryId") String categoryId,@Param ("postName") String postName);
	//根据主键查找岗位模板
	public PostTemplate findtPostTemplateById(Integer postTemplateId);
	
	//add by ZhangYuWei 20181110  根据职位类别、职位子类、职位族类查询职位名称、职级列表
	public Map<String,Object> selectPostTemplateByPostId(Integer postId);
	
	//修改岗位模板信息
	public Integer updatePostTemplate(PostTemplate postTemplate);
	
	//通过岗位类别查询岗位名称
	public List<String> findPositionName (String categoryId);
		
	//根据岗位类别和岗位名称 查找岗位模板主键
	public PostTemplate findPostTemplateIdByCatPostName(@Param ("categoryId") String categoryId,@Param("postName") String postName);
	
	//根据岗位名称，查找岗位模板主键
	public PostTemplate findPostTemplateByPostName(String postName);

	/**
	 * 根据岗位类别，岗位名称获取职位职级列表
	 * @param categoryId 岗位类别
	 * @param postName	岗位名称
	 * @return
	 */
	public List<Map<String, Object>> findPositionRankListByCatPostName(@Param ("categoryId")String categoryId, @Param("postName") String postName);
}
