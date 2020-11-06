package com.nantian.erp.hr.data.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Table(name="department")
public class ErpDepartment implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8358798331899702072L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="departmentId")
	private Integer departmentId;
	
	@Column(name="departmentName")
	private String departmentName;
	
	@Column(name="rank")
	private String rank;
	
	@Column(name="upperDepartment")
	private Integer upperDepartment;
	
	@Column(name="departmentManagerEmail")
	private String departmentManagerEmail;
	
	@Column(name="departmentDuty")
	private String departmentDuty;
	
	@Column(name="userId")
	private Integer userId;
	
	@Column(name="departmentType")
	private String departmentType;
	
	@Column(name="superLeader")
	private Integer superLeader;

	private String code;

	private Integer changeType;
    /**
     * 部门排序
     */
	@Column(name="departmentOrder")
    private String departmentOrder;
	
	public Integer getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public String getRank() {
		return rank;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}
	public Integer getUpperDepartment() {
		return upperDepartment;
	}
	public void setUpperDepartment(Integer upperDepartment) {
		this.upperDepartment = upperDepartment;
	}
	public String getDepartmentManagerEmail() {
		return departmentManagerEmail;
	}
	public void setDepartmentManagerEmail(String departmentManagerEmail) {
		this.departmentManagerEmail = departmentManagerEmail;
	}
	
	
	
	public String getDepartmentDuty() {
		return departmentDuty;
	}
	public void setDepartmentDuty(String departmentDuty) {
		this.departmentDuty = departmentDuty;
	}
	
	
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getDepartmentType() {
		return departmentType;
	}
	public void setDepartmentType(String departmentType) {
		this.departmentType = departmentType;
	}
	
	
	
	public Integer getSuperLeader() {
		return superLeader;
	}
	public void setSuperLeader(Integer superLeader) {
		this.superLeader = superLeader;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	public String getDepartmentOrder() {
		return departmentOrder;
	}
	public void setDepartmentOrder(String departmentOrder) {
		this.departmentOrder = departmentOrder;
	}

	public Integer getChangeType() {
		return changeType;
	}

	public void setChangeType(Integer changeType) {
		this.changeType = changeType;
	}

	@Override
	public String toString() {
		return "ErpDepartment [departmentId=" + departmentId
				+ ", departmentName=" + departmentName + ", rank=" + rank
				+ ", upperDepartment=" + upperDepartment
				+ ", departmentManagerEmail=" + departmentManagerEmail
				+ ", departmentDuty=" + departmentDuty + ", userId=" + userId
				+ ", departmentType=" + departmentType + ", superLeader="
				+ superLeader + ", code=" + code + ", departmentOrder="
				+ departmentOrder + "]";
	}
}
