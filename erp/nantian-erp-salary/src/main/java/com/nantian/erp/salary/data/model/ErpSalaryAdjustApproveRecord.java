package com.nantian.erp.salary.data.model;

import java.util.Date;

import javax.persistence.Table;

@Table(name="erp_salary_adjust_approve_record")
public class ErpSalaryAdjustApproveRecord {
	//审批记录ID
	private Integer salaryAdjustApproveRecordId;
	//流程表主键ID
	private Integer salaryAdjustFlowId;	
	//流程表ID
	private Integer employeeId;	
	//审批时间
	private Date time;
	//审批内容
	private String content;
	//审批人
	private String processor;
	
	public ErpSalaryAdjustApproveRecord(){
			super();
	}
	public ErpSalaryAdjustApproveRecord(Integer salaryAdjustApproveRecordId,
			Integer salaryAdjustFlowId,Integer employeeId, Date time, String content,
			String processor) {
		this.salaryAdjustApproveRecordId = salaryAdjustApproveRecordId;
		this.salaryAdjustFlowId=salaryAdjustFlowId;
		this.employeeId = employeeId;
		this.time = time;
		this.content = content;
		this.processor = processor;
	}
	
	public Integer getSalaryAdjustApproveRecordId() {
		return salaryAdjustApproveRecordId;
	}
	public void setSalaryAdjustApproveRecordId(Integer salaryAdjustApproveRecordId) {
		this.salaryAdjustApproveRecordId = salaryAdjustApproveRecordId;
	}
	public Integer getSalaryAdjustFlowId() {
		return salaryAdjustFlowId;
	}
	public void setSalaryAdjustFlowId(Integer salaryAdjustFlowId) {
		this.salaryAdjustFlowId = salaryAdjustFlowId;
	}
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getProcessor() {
		return processor;
	}
	public void setProcessor(String processor) {
		this.processor = processor;
	}
	
}
