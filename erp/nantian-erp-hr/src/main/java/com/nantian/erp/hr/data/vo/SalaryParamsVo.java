package com.nantian.erp.hr.data.vo;

import java.util.List;

/**
 * add by 张玉伟  20180921 薪酬工程需要的参数
 * @author ZhangYuWei
 */
public class SalaryParamsVo {
    
	private String username;//用户名
	private String payrollType;//工资单类型（post表示“上岗工资单”，positive表示“转正工资单”）
	private Integer employeeId;//员工ID
	
	private List<Integer> employeeIds;//员工ID数组（薪酬模块使用）
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPayrollType() {
		return payrollType;
	}
	public void setPayrollType(String payrollType) {
		this.payrollType = payrollType;
	}
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public List<Integer> getEmployeeIds() {
		return employeeIds;
	}
	public void setEmployeeIds(List<Integer> employeeIds) {
		this.employeeIds = employeeIds;
	}
	@Override
	public String toString() {
		return "SalaryParamsVo [username=" + username + ", payrollType=" + payrollType + ", employeeId=" + employeeId
				+ ", employeeIds=" + employeeIds + "]";
	}
	
}
