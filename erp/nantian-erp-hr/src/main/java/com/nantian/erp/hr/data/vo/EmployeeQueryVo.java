package com.nantian.erp.hr.data.vo;

import java.util.Date;
import java.util.List;

public class EmployeeQueryVo {

	
	private String name;
	private String sex;
	private String phone;
	private String position;
	private Integer rank;
	private Integer offerId;

	
	
	
	
	private Integer firstDepartmentId;
	private Integer secondDepartmentId;
	private String dm;
	
	private Date entryTime;
	private Date probationEndTime;
	private Date contractEndTime;
	
	private String username;
    private List<String> right;
	
	private Integer resumeId;
	private Integer postId;

	
	
	public Integer getOfferId() {
		return offerId;
	}
	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
	}


	public Integer getFirstDepartmentId() {
		return firstDepartmentId;
	}
	public void setFirstDepartmentId(Integer firstDepartmentId) {
		this.firstDepartmentId = firstDepartmentId;
	}
	public Integer getSecondDepartmentId() {
		return secondDepartmentId;
	}
	public void setSecondDepartmentId(Integer secondDepartmentId) {
		this.secondDepartmentId = secondDepartmentId;
	}
	public String getDm() {
		return dm;
	}
	public void setDm(String dm) {
		this.dm = dm;
	}
	public Date getEntryTime() {
		return entryTime;
	}
	public void setEntryTime(Date entryTime) {
		this.entryTime = entryTime;
	}
	public Date getProbationEndTime() {
		return probationEndTime;
	}
	public void setProbationEndTime(Date probationEndTime) {
		this.probationEndTime = probationEndTime;
	}
	public Date getContractEndTime() {
		return contractEndTime;
	}
	public void setContractEndTime(Date contractEndTime) {
		this.contractEndTime = contractEndTime;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}


	public Integer getResumeId() {
		return resumeId;
	}
	public void setResumeId(Integer resumeId) {
		this.resumeId = resumeId;
	}
	public Integer getPostId() {
		return postId;
	}
	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public List<String> getRight() {
		return right;
	}
	public void setRight(List<String> right) {
		this.right = right;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	@Override
	public String toString() {
		return "EmployeeQueryVo [name=" + name + ", sex=" + sex + ", phone=" + phone + ", position=" + position
				+ ", rank=" + rank + ", offerId=" + offerId + ", firstDepartmentId=" + firstDepartmentId
				+ ", secondDepartmentId=" + secondDepartmentId + ", dm=" + dm + ", entryTime=" + entryTime
				+ ", probationEndTime=" + probationEndTime + ", contractEndTime=" + contractEndTime + ", username="
				+ username + ", right=" + right + ", resumeId=" + resumeId + ", postId=" + postId + "]";
	}
	
}
