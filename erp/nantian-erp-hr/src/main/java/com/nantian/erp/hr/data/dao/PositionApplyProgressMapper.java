package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.hr.data.model.PositionApplyProgress;

/**
 * 岗位申请流程Mapper
 *
 */
public interface PositionApplyProgressMapper {
	
	//新增岗位类别
	public  int addApplyProgress(PositionApplyProgress applyProgress);
	
	//根据当前登录人Id查找信息
	public List<PositionApplyProgress> findApplyProgressInfoByCurPersonID(Integer erpUserId); 
	//判断当前登陆人 是处理人
	public Integer isCurLoginPersonID(Integer erpUserId);
	
	//岗位申请表关联Id 和当前处理人用户编号 定位一条岗位流程记录
	public PositionApplyProgress findApplyProgressByPidCurPerId(Map<String, Object> paramProgress);
	//修改岗位流程表
	public void updateApplyProgressById(PositionApplyProgress positionApplyProgress);
	
	//根据岗位申请表主键 查找岗位申请流程
	public  PositionApplyProgress  findApplyProgressByPostId (@Param("postId") Integer postId);

	/**
	 * 根据岗位ID更新审批人
	 * @param updateMap
	 * @throws Exception
	 */
	void updateCurrentPersonIdByPostId(Map<String, Object> updateMap)throws Exception;
}
