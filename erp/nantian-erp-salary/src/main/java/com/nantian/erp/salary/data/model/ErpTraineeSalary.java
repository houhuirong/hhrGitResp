package com.nantian.erp.salary.data.model;

import javax.persistence.Table;

@Table(name="erp_trainee_salary")
public class ErpTraineeSalary {
	//实习生薪资主键
	private Integer traineeId;
	//基本工资
	private String baseWage;
	//月度项目津贴
	private String monthAllowance;
	//员工编号
	private Integer employeeId;
	public Integer getTraineeId() {
		return traineeId;
	}
	public void setTraineeId(Integer traineeId) {
		this.traineeId = traineeId;
	}
	public String getBaseWage() {
		return baseWage;
	}
	public void setBaseWage(String baseWage) {
		this.baseWage = baseWage;
	}
	public String getMonthAllowance() {
		return monthAllowance;
	}
	public void setMonthAllowance(String monthAllowance) {
		this.monthAllowance = monthAllowance;
	}
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	@Override
	public String toString() {
		return "ErpTraineeSalary [traineeId=" + traineeId + ", baseWage="
				+ baseWage + ", monthAllowance=" + monthAllowance
				+ ", employeeId=" + employeeId + "]";
	}
	
	
}
