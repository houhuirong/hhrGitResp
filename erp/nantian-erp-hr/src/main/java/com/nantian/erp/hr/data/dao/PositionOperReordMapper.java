package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.hr.data.model.PositionOperRecond;

/**
 * 岗位申请操作记录Mapper
 *
 */
public interface PositionOperReordMapper {
	
	//新增岗位申请操作记录 PositionOperReordMapper
	public  int addPositionOperReord(PositionOperRecond operRec);
	
	//通过当亲处理人Id和岗位申请表主键 查找岗位申请记录
	public List<PositionOperRecond> findOperRecordByPostIdAndCurPerId(Map<String, Object> paramDic);
	
	//通过岗位申请表主键查找岗位申请记录
	public List<PositionOperRecond> findOperRecordByPostId(@Param("postId")  Integer postId);
	
	//查询关闭原因
	public PositionOperRecond findCloseRease(@Param(value = "postId") Integer postId); 
	
	//根据当前登录人ID，查找他曾经审批过得岗位记录
	public List<PositionOperRecond> findCurrentIdApprovedOperRecor(@Param(value = "currentPersonId") Integer currentPersonId);
	//根据当前登录人ID，查找他曾经审批过处于发布中/已关闭的岗位
	public List<PositionOperRecond> findCurrentIdPublishOperRecor(@Param(value = "currentPersonId") Integer currentPersonId,@Param(value = "status") Integer status);
	
}
