package com.nantian.erp.salary.data.model;

import javax.persistence.Table;

@Table(name="erp_trainee_salary")
public class ErpPayRollFlow {

	//上岗工资单流程审批表ID
	private Integer id;		
	//上岗工资单流程状态
	private Integer status;
	//入职人员的用户编号
	private Integer userId;
	//试用期是否锁定
	private Integer periodIsLock;	
	//当前处理人的用户编号
	private Integer currentPersonID;
	//转正是否锁定
	private Integer positiveIsLock;	
	//上岗工资单提交月份
	private String commitMonth;
	//转正工资单提交月份
	private String positiveMonth;
	//社保基数提交月份
	private String socialSecMonth;
	//状态名称
	private String statusName;
    /**
     * 是否被确认
     */
    private Boolean isConfirmed;
	
	public String getPositiveMonth() {
		return positiveMonth;
	}
	public void setPositiveMonth(String positiveMonth) {
		this.positiveMonth = positiveMonth;
	}
	public String getCommitMonth() {
		return commitMonth;
	}
	public void setCommitMonth(String commitMonth) {
		this.commitMonth = commitMonth;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getCurrentPersonID() {
		return currentPersonID;
	}
	public void setCurrentPersonID(Integer currentPersonID) {
		this.currentPersonID = currentPersonID;
	}

	public Integer getPeriodIsLock() {
		return periodIsLock;
	}
	public void setPeriodIsLock(Integer periodIsLock) {
		this.periodIsLock = periodIsLock;
	}
	public Integer getPositiveIsLock() {
		return positiveIsLock;
	}
	public void setPositiveIsLock(Integer positiveIsLock) {
		this.positiveIsLock = positiveIsLock;
	}
	public String getSocialSecMonth() {
		return socialSecMonth;
	}
	public void setSocialSecMonth(String socialSecMonth) {
		this.socialSecMonth = socialSecMonth;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	public Boolean getIsConfirmed() {
		return isConfirmed;
	}
	public void setIsConfirmed(Boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
	}
	@Override
	public String toString() {
		return "ErpPayRollFlow{" +
				"id=" + id +
				", status=" + status +
				", userId=" + userId +
				", periodIsLock=" + periodIsLock +
				", currentPersonID=" + currentPersonID +
				", positiveIsLock=" + positiveIsLock +
				", commitMonth='" + commitMonth + '\'' +
				", positiveMonth='" + positiveMonth + '\'' +
				", socialSecMonth='" + socialSecMonth + '\'' +
				", statusName='" + statusName + '\'' +
				'}';
	}
}
