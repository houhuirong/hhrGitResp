package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.hr.data.model.PostRequire;;;

/**
 * 岗位需求Mapper
 * @author lx
 */
public interface PostRequireMapper {
	
	//插入岗位描述
	public Integer addPostRequire(PostRequire postRequire);

	//批量插入岗位描述
	public Integer addPostRequireBatch(List<PostRequire> postRequireList);
	
	//修改岗位描述
	public Integer updatePostRequire(PostRequire postRequire);
	
	//删除岗位描述
	public Integer deleatePostRequire(Integer id);
	
	//根据岗位模板编号和类型获取岗位需求信息
	public List<PostRequire> findPostRequireByPostId(Map<String, Object> param);
	
	//根据岗位模板或岗位申请主键删除岗位需求
	public void deletePostRequireByPostId(@Param("postId")Integer postId,@Param("type") Integer type);

	//通过主键ID 查找岗位需要信息
	public PostRequire selectPostRequireById(@Param("id") Integer id);
}
