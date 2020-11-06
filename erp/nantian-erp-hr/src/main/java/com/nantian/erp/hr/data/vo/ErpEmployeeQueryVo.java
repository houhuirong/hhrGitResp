package com.nantian.erp.hr.data.vo;

import java.util.List;

public class ErpEmployeeQueryVo {
	
	private String name;
	private String sex;
	private String phone;
	private String personalEmail;	
	private String position;
	private Integer positionNo;	
	
	private Integer rank;
	private Integer offerId;

	private Integer firstDepartmentId;
	private Integer secondDepartmentId;
	private String dm;
	
	private String entryTime;
	private String probationEndTime;
	private String contractBeginTime;
	public String getContractBeginTime() {
		return contractBeginTime;
	}
	public void setContractBeginTime(String contractBeginTime) {
		this.contractBeginTime = contractBeginTime;
	}
	private String contractEndTime;
	
	private String username;
    private List<Integer> right;
	
	private Integer resumeId;
	private Integer postId;
	private String status;
	private String socialSecurityPlace;
	//政治面貌
    private Integer politicalStatus;
    //民族
    private Integer groups;
	
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
	
	public String getEntryTime() {
		return entryTime;
	}
	public void setEntryTime(String entryTime) {
		this.entryTime = entryTime;
	}
	public String getProbationEndTime() {
		return probationEndTime;
	}
	public void setProbationEndTime(String probationEndTime) {
		this.probationEndTime = probationEndTime;
	}
	public String getContractEndTime() {
		return contractEndTime;
	}
	public void setContractEndTime(String contractEndTime) {
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

	public List<Integer> getRight() {
		return right;
	}
	public void setRight(List<Integer> right) {
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
	public String getPersonalEmail() {
		return personalEmail;
	}
	public void setPersonalEmail(String personalEmail) {
		this.personalEmail = personalEmail;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public Integer getPositionNo() {
		return positionNo;
	}
	public void setPositionNo(Integer positionNo) {
		this.positionNo = positionNo;
	}
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getSocialSecurityPlace() {
		return socialSecurityPlace;
	}
	public void setSocialSecurityPlace(String socialSecurityPlace) {
		this.socialSecurityPlace = socialSecurityPlace;
	}
	public Integer getPoliticalStatus() {
		return politicalStatus;
	}
	public void setPoliticalStatus(Integer politicalStatus) {
		this.politicalStatus = politicalStatus;
	}
	public Integer getGroups() {
		return groups;
	}
	public void setGroups(Integer groups) {
		this.groups = groups;
	}
	@Override
	public String toString() {
		return "ErpEmployeeQueryVo [name=" + name + ", sex=" + sex + ", phone=" + phone + ", personalEmail="
				+ personalEmail + ", position=" + position + ", positionNo=" + positionNo + ", rank=" + rank
				+ ", offerId=" + offerId + ", firstDepartmentId=" + firstDepartmentId + ", secondDepartmentId="
				+ secondDepartmentId + ", dm=" + dm + ", entryTime=" + entryTime + ", probationEndTime="
				+ probationEndTime + ", contractBeginTime=" + contractBeginTime + ", contractEndTime=" + contractEndTime
				+ ", username=" + username + ", right=" + right + ", resumeId=" + resumeId + ", postId=" + postId
				+ ", status=" + status + ", socialSecurityPlace=" + socialSecurityPlace + "]";
	}

}
