package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 工作调整申请实体类
 * @author gaolp
 * 2019年3月15日
 * @version 1.0  
 */
public class DepartmentTransfApply {

	private Integer id;
	private Integer employeeId;
	private Integer newFirstDepartment;
	private Integer newSecDepartment;
	private Integer oldFirstDepartment;
	private Integer oldSecDepartment;
	private String startTime;
	private String status;
	private Integer processor;
	//private Integer oldPositionId;
	//private Integer newPositionId;
	private String reason;
	private Integer applyPersonId;
	private Date applyTime;
	private Date createTime;
	private Date modifiedTime;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public Integer getNewFirstDepartment() {
		return newFirstDepartment;
	}
	public void setNewFirstDepartment(Integer newFirstDepartment) {
		this.newFirstDepartment = newFirstDepartment;
	}
	public Integer getNewSecDepartment() {
		return newSecDepartment;
	}
	public void setNewSecDepartment(Integer newSecDepartment) {
		this.newSecDepartment = newSecDepartment;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getProcessor() {
		return processor;
	}
	public void setProcessor(Integer processor) {
		this.processor = processor;
	}
	public Integer getOldFirstDepartment() {
		return oldFirstDepartment;
	}
	public void setOldFirstDepartment(Integer oldFirstDepartment) {
		this.oldFirstDepartment = oldFirstDepartment;
	}
	public Integer getOldSecDepartment() {
		return oldSecDepartment;
	}
	public void setOldSecDepartment(Integer oldSecDepartment) {
		this.oldSecDepartment = oldSecDepartment;
	}

//	public Integer getOldPositionId() {
//		return oldPositionId;
//	}
//
//	public void setOldPositionId(Integer oldPositionId) {
//		this.oldPositionId = oldPositionId;
//	}
//
//	public Integer getNewPositionId() {
//		return newPositionId;
//	}
//
//	public void setNewPositionId(Integer newPositionId) {
//		this.newPositionId = newPositionId;
//	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getApplyPersonId() {
		return applyPersonId;
	}

	public void setApplyPersonId(Integer applyPersonId) {
		this.applyPersonId = applyPersonId;
	}

	public Date getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(Date applyTime) {
		this.applyTime = applyTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	@Override
	public String toString() {
		return "DepartmentTransfApply{" +
				"id=" + id +
				", employeeId=" + employeeId +
				", newFirstDepartment=" + newFirstDepartment +
				", newSecDepartment=" + newSecDepartment +
				", oldFirstDepartment=" + oldFirstDepartment +
				", oldSecDepartment=" + oldSecDepartment +
				", startTime='" + startTime + '\'' +
				", status='" + status + '\'' +
				", processor=" + processor +
				", reason='" + reason + '\'' +
				", applyPersonId=" + applyPersonId +
				", applyTime=" + applyTime +
				", createTime=" + createTime +
				", modifiedTime=" + modifiedTime +
				'}';
	}
}
