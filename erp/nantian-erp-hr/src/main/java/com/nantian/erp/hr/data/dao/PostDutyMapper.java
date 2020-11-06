package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.hr.data.model.PostDuty;;

/**
 * 岗位模板Mapper
 * @author lx
 */
public interface PostDutyMapper {
	
	//插入岗位职责
	public Integer addPostDuty(PostDuty postDuty);
	//插入岗位职责
	public Integer addPostDutyBatch(List<PostDuty> postDutyList);
	
	//修改岗位职责
	public Integer updatePostDuty(PostDuty postDuty);
	
	//删除岗位职责
	public Integer deleatePostDuty(Integer id);
	
	//根据岗位模板编号和类型获取岗位职责信息
	public List<PostDuty> findPostDutyByPostId(Map<String, Object> param);
	
	//根据岗位模板或者岗位申请表主键删除相关联的岗位职责
	public  void deletePostDutyByPostId(@Param("postId")Integer postId,@Param("type") Integer type);
	
	//通过主键查找岗位职责
	public  PostDuty selectPostDutyById(@Param("id")Integer id);
}
