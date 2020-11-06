package com.nantian.erp.hr.data.model;

/** 
 * Description: 岗位申请流程表
 *
 * @author lx
 * @version 1.0
 */
public class PositionApplyProgress {
	private Integer id;   //主键
	private Integer postId;  //岗位申请表关联Id
	private Integer currentPersonID;  //当前处理人用户编号
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getPostId() {
		return postId;
	}
	public void setPostId(Integer postId) {
		this.postId = postId;
	}
	public Integer getCurrentPersonID() {
		return currentPersonID;
	}
	public void setCurrentPersonID(Integer currentPersonID) {
		this.currentPersonID = currentPersonID;
	}
	@Override
	public String toString() {
		return "PositionApplyProgress [id=" + id + ", postId=" + postId + ", currentPersonID=" + currentPersonID + "]";
	}
	
	
	
}
