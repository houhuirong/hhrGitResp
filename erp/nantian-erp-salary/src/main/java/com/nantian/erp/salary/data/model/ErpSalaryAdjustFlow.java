package com.nantian.erp.salary.data.model;

import java.util.Date;

import javax.persistence.Table;

@Table(name="erp_salary_adjust_flow")
public class ErpSalaryAdjustFlow {
	
	//薪资调整流程表ID
	private Integer salaryAdjustFlowId;
	//员工ID
	private Integer employeeId;
	//基本工资
	private Double baseWage;
	//岗位工资
	private Double postWage;
	//月底绩效
	private Double monthPerformanceWage;
	//月度项目绩效
	private Double monthProjectPerformanceWage;
	//申请时间
	private Date applicationTime;
	//生效时间
	private Date effectiveTime;
	//申请类型
	private String type;
	
	public ErpSalaryAdjustFlow(){
		super();
	}
	public ErpSalaryAdjustFlow(Integer salaryAdjustFlowId, Integer employeeId,
			Double baseWage, Double postWage, Double monthPerformanceWage,
			Double monthProjectPerformanceWage, Date applicationTime,
			Date effectiveTime, String type) {
		this.salaryAdjustFlowId = salaryAdjustFlowId;
		this.employeeId = employeeId;
		this.baseWage = baseWage;
		this.postWage = postWage;
		this.monthPerformanceWage = monthPerformanceWage;
		this.monthProjectPerformanceWage = monthProjectPerformanceWage;
		this.applicationTime = applicationTime;
		this.effectiveTime = effectiveTime;
		this.type = type;
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

	public Double getBaseWage() {
		return baseWage;
	}

	public void setBaseWage(Double baseWage) {
		this.baseWage = baseWage;
	}

	public Double getPostWage() {
		return postWage;
	}

	public void setPostWage(Double postWage) {
		this.postWage = postWage;
	}

	public Double getMonthPerformanceWage() {
		return monthPerformanceWage;
	}

	public void setMonthPerformanceWage(Double monthPerformanceWage) {
		this.monthPerformanceWage = monthPerformanceWage;
	}

	public Double getMonthProjectPerformanceWage() {
		return monthProjectPerformanceWage;
	}

	public void setMonthProjectPerformanceWage(Double monthProjectPerformanceWage) {
		this.monthProjectPerformanceWage = monthProjectPerformanceWage;
	}

	public Date getApplicationTime() {
		return applicationTime;
	}

	public void setApplicationTime(Date applicationTime) {
		this.applicationTime = applicationTime;
	}

	public Date getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(Date effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
