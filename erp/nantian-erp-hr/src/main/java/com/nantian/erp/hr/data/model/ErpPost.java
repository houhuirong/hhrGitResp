package com.nantian.erp.hr.data.model;

import java.util.Date;

/** 
 * Description: 岗位申请表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpPost {
	//岗位Id
	private Integer postId;
	//一级部门
	private Integer firstDepartment;
	//二级部门
	private Integer secondDepartment;
	//岗位类别
	private String category;
	//岗位要求
	private String required;
	//岗位职责
	private String duty;
	//岗位薪资删除的字段
    //	private Integer salary;
	//招聘人数
	private Integer numberPeople;
	//是否关闭
	private Integer isClosed;
	//关闭岗位原因
	private String closedReason;
	//岗位申请人Id
	private Integer proposerId;
	//岗位申请状态（1：审批中 2：发布中 3：已关闭）
	private Integer status;
	//岗位名称
	private String postName;
	//薪资范围
	private String salaryRange;
	//岗位申请主键
	private Integer postTemplateId; 
	
	//工作地点（北京、其他地区）
	private String workAddress;
	//招聘周期（默认2个月，可改）
	private String recruitCycle;
	/**
	 * 接口人
	 */
	private Integer principal;
	/**
	 * 审批人
	 */
	private Integer principalLeader;
	/**
	 * 职位职级id
	 */
	private Integer positionRankId;
	/**
	 * 工作地-省市
	 */
	private String city;
	/**
	 * 工作地-区
	 */
	private String district;
	/**
	 * 工作地-县
	 */
	private String county;
	/**
	 * 详细地址
	 */
	private String detailAddress;
	/**
	 * 申请日期
	 */
	private Date dateSubmit;
	/**
	 * 招聘原因
	 */
	private Integer reasonRecruit;
	/**
	 * 优先级
	 */
	private Integer levelPriority;

	/**
	 * hr负责人
	 */
	private Integer personCharge;
	
	public String getSalaryRange() {
		return salaryRange;
	}
	public void setSalaryRange(String salaryRange) {
		this.salaryRange = salaryRange;
	}
	public Integer getPostId() {
		return postId;
	}
	public void setPostId(Integer postId) {
		this.postId = postId;
	}
	public Integer getFirstDepartment() {
		return firstDepartment;
	}
	public void setFirstDepartment(Integer firstDepartment) {
		this.firstDepartment = firstDepartment;
	}
	public Integer getSecondDepartment() {
		return secondDepartment;
	}
	public void setSecondDepartment(Integer secondDepartment) {
		this.secondDepartment = secondDepartment;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getRequired() {
		return required;
	}
	public void setRequired(String required) {
		this.required = required;
	}
	public Integer getNumberPeople() {
		return numberPeople;
	}
	public void setNumberPeople(Integer numberPeople) {
		this.numberPeople = numberPeople;
	}
	public String getClosedReason() {
		return closedReason;
	}
	public void setClosedReason(String closedReason) {
		this.closedReason = closedReason;
	}
	public Integer getProposerId() {
		return proposerId;
	}
	public void setProposerId(Integer proposerId) {
		this.proposerId = proposerId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getPostName() {
		return postName;
	}
	public void setPostName(String postName) {
		this.postName = postName;
	}
	public Integer getPostTemplateId() {
		return postTemplateId;
	}
	public void setPostTemplateId(Integer postTemplateId) {
		this.postTemplateId = postTemplateId;
	}
	
	
	public Integer getIsClosed() {
		return isClosed;
	}
	public void setIsClosed(Integer isClosed) {
		this.isClosed = isClosed;
	}
	
	public String getDuty() {
		return duty;
	}
	public void setDuty(String duty) {
		this.duty = duty;
	}
	public String getWorkAddress() {
		return workAddress;
	}
	public void setWorkAddress(String workAddress) {
		this.workAddress = workAddress;
	}
	public String getRecruitCycle() {
		return recruitCycle;
	}
	public void setRecruitCycle(String recruitCycle) {
		this.recruitCycle = recruitCycle;
	}
	public Integer getPrincipal() {
		return principal;
	}
	public void setPrincipal(Integer principal) {
		this.principal = principal;
	}
	
	public Integer getPrincipalLeader() {
		return principalLeader;
	}
	public void setPrincipalLeader(Integer principalLeader) {
		this.principalLeader = principalLeader;
	}

	public Integer getPositionRankId() {
		return positionRankId;
	}
	public void setPositionRankId(Integer positionRankId) {
		this.positionRankId = positionRankId;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public String getDetailAddress() {
		return detailAddress;
	}
	public void setDetailAddress(String detailAddress) {
		this.detailAddress = detailAddress;
	}
	
	public Date getDateSubmit() {
		return dateSubmit;
	}
	public void setDateSubmit(Date dateSubmit) {
		this.dateSubmit = dateSubmit;
	}
	public Integer getReasonRecruit() {
		return reasonRecruit;
	}
	public void setReasonRecruit(Integer reasonRecruit) {
		this.reasonRecruit = reasonRecruit;
	}
	public Integer getLevelPriority() {
		return levelPriority;
	}
	public void setLevelPriority(Integer levelPriority) {
		this.levelPriority = levelPriority;
	}
	
	
	public Integer getPersonCharge() {
		return personCharge;
	}
	public void setPersonCharge(Integer personCharge) {
		this.personCharge = personCharge;
	}
	@Override
	public String toString() {
		return "ErpPost [postId=" + postId + ", firstDepartment=" + firstDepartment + ", secondDepartment="
				+ secondDepartment + ", category=" + category + ", required=" + required + ", duty=" + duty
				+ ", numberPeople=" + numberPeople + ", isClosed=" + isClosed + ", closedReason=" + closedReason
				+ ", proposerId=" + proposerId + ", status=" + status + ", postName=" + postName + ", salaryRange="
				+ salaryRange + ", postTemplateId=" + postTemplateId + ", workAddress=" + workAddress
				+ ", recruitCycle=" + recruitCycle + ", principal=" + principal + ", principalLeader=" + principalLeader
				+ ", positionRankId=" + positionRankId + ", city=" + city + ", district=" + district + ", county="
				+ county + ", detailAddress=" + detailAddress + ", dateSubmit=" + dateSubmit + ", reasonRecruit="
				+ reasonRecruit + ", levelPriority=" + levelPriority + ", personCharge=" + personCharge + "]";
	}


}
