package com.nantian.erp.hr.data.model;

public class PostRequire {

	private Integer id;  //主键
	private String postRequireDescribe; //岗位要求描述
	private Integer postId ; //对应岗位编号（tpye:1 岗位模板编号 type：2 岗位申请编号
	private Integer  type; //1岗位模板2是岗位申请
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	
	public String getPostRequireDescribe() {
		return postRequireDescribe;
	}
	public void setPostRequireDescribe(String postRequireDescribe) {
		this.postRequireDescribe = postRequireDescribe;
	}
	public Integer getPostId() {
		return postId;
	}
	public void setPostId(Integer postId) {
		this.postId = postId;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "PostRequire [id=" + id + ", postRequireDescribe=" + postRequireDescribe + ", postId=" + postId
				+ ", type=" + type + "]";
	}
	
	
	
	
	
}
