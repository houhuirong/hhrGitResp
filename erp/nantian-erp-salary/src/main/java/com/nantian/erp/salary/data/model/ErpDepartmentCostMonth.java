package com.nantian.erp.salary.data.model;

/** 
 * Description: 部门人员薪酬分析表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpDepartmentCostMonth {
	
	//部门人员费用ID
	private Integer id;
	//月份
	private String month;
	//一级部门Id
	private Integer firstDepartmentId;
	//工资费用
	private String wageCost;
	//补助费用
	private String subsidyCost;
	//绩效费用
	private String performanceCost;
	//社保费用
	private String socialSecurityCost;
	//公积金费用
	private String accumulationFundCost;
	//员工人数
	private Integer employeeNum;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public Integer getFirstDepartmentId() {
		return firstDepartmentId;
	}
	public void setFirstDepartmentId(Integer firstDepartmentId) {
		this.firstDepartmentId = firstDepartmentId;
	}
	public String getWageCost() {
		return wageCost;
	}
	public void setWageCost(String wageCost) {
		this.wageCost = wageCost;
	}
	public String getSubsidyCost() {
		return subsidyCost;
	}
	public void setSubsidyCost(String subsidyCost) {
		this.subsidyCost = subsidyCost;
	}
	public String getPerformanceCost() {
		return performanceCost;
	}
	public void setPerformanceCost(String performanceCost) {
		this.performanceCost = performanceCost;
	}
	public String getSocialSecurityCost() {
		return socialSecurityCost;
	}
	public void setSocialSecurityCost(String socialSecurityCost) {
		this.socialSecurityCost = socialSecurityCost;
	}
	public String getAccumulationFundCost() {
		return accumulationFundCost;
	}
	public void setAccumulationFundCost(String accumulationFundCost) {
		this.accumulationFundCost = accumulationFundCost;
	}
	public Integer getEmployeeNum() {
		return employeeNum;
	}
	public void setEmployeeNum(Integer employeeNum) {
		this.employeeNum = employeeNum;
	}
	
	@Override
	public String toString() {
		return "ErpDepartmentCostMonth [id=" + id + ", month=" + month + ", firstDepartmentId=" + firstDepartmentId
				+ ", wageCost=" + wageCost + ", subsidyCost=" + subsidyCost + ", performanceCost=" + performanceCost
				+ ", socialSecurityCost=" + socialSecurityCost + ", accumulationFundCost=" + accumulationFundCost
				+ ", employeeNum=" + employeeNum + "]";
	}
	
}
