package com.nantian.erp.hr.data.model;
/**
 * 岗位申请处理记录表
 * @author Administrator
 *
 */
public class PositionOperRecond {

	private Integer id;   //主键
	private String createTime;  //处理时间
	private String operContext;  //处理内容
	private Integer currentPersonId;  //当前处理人Id
	private String currentPersonName;  //当前处理人姓名
	private Integer postId;  //岗位申请表主键
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getOperContext() {
		return operContext;
	}
	public void setOperContext(String operContext) {
		this.operContext = operContext;
	}
	public Integer getCurrentPersonId() {
		return currentPersonId;
	}
	public void setCurrentPersonId(Integer currentPersonId) {
		this.currentPersonId = currentPersonId;
	}
	public String getCurrentPersonName() {
		return currentPersonName;
	}
	public void setCurrentPersonName(String currentPersonName) {
		this.currentPersonName = currentPersonName;
	}
	public Integer getPostId() {
		return postId;
	}
	public void setPostId(Integer postId) {
		this.postId = postId;
	}
	@Override
	public String toString() {
		return "PositionOperRecond [id=" + id + ", createTime=" + createTime + ", operContext=" + operContext
				+ ", currentPersonId=" + currentPersonId + ", currentPersonName=" + currentPersonName + ", postId="
				+ postId + "]";
	}
	
	
}
