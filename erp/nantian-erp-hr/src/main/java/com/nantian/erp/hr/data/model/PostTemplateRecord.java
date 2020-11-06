package com.nantian.erp.hr.data.model;

import java.util.Date;

public class PostTemplateRecord {
	private Integer id;
	private Integer  postTemplateId; //岗位模板编号
	private Integer modifiedUser;  //操作人ID
	private String modifiedTime;  //操作时间
	private String gmtModified;  //修改时间
	private String gmtCreate;  //创建时间
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getPostTemplateId() {
		return postTemplateId;
	}
	public void setPostTemplateId(Integer postTemplateId) {
		this.postTemplateId = postTemplateId;
	}
	public Integer getModifiedUser() {
		return modifiedUser;
	}
	public void setModifiedUser(Integer modifiedUser) {
		this.modifiedUser = modifiedUser;
	}
	public String getModifiedTime() {
		return modifiedTime;
	}
	public void setModifiedTime(String modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	public String getGmtModified() {
		return gmtModified;
	}
	public void setGmtModified(String gmtModified) {
		this.gmtModified = gmtModified;
	}
	public String getGmtCreate() {
		return gmtCreate;
	}
	public void setGmtCreate(String gmtCreate) {
		this.gmtCreate = gmtCreate;
	}
	@Override
	public String toString() {
		return "ErpPostTemplateRecord [id=" + id + ", postTemplateId="
				+ postTemplateId + ", modifiedUser=" + modifiedUser
				+ ", modifiedTime=" + modifiedTime + ", gmtModified="
				+ gmtModified + ", gmtCreate=" + gmtCreate + ", getId()="
				+ getId() + ", getPostTemplateId()=" + getPostTemplateId()
				+ ", getModifiedUser()=" + getModifiedUser()
				+ ", getModifiedTime()=" + getModifiedTime()
				+ ", getGmtModified()=" + getGmtModified()
				+ ", getGmtCreate()=" + getGmtCreate() + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
	
}
