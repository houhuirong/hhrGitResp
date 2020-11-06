package com.nantian.erp.salary.data.vo;

import java.util.List;
import java.util.Map;

/** 
 * Description: 二级部门下所有员工的信息和薪酬情况
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年10月24日      		ZhangYuWei          1.0       
 * 2018年11月25日      		ZhangYuWei          1.1       
 * </pre>
 */
public class SecondDepEmpInfoAndSalaryVo {
	
	//二级部门名称
	private String secondDepartmentName;
	//二级部门月度收入合计
	private Double secondDepMonthIncome;
	//二级部门社保合计
	private Double secondDepSocialSecurity;
	//员工信息列表
	private List<Map<String,Object>> employeeInfoList;
	
	public String getSecondDepartmentName() {
		return secondDepartmentName;
	}
	public void setSecondDepartmentName(String secondDepartmentName) {
		this.secondDepartmentName = secondDepartmentName;
	}
	public Double getSecondDepMonthIncome() {
		return secondDepMonthIncome;
	}
	public void setSecondDepMonthIncome(Double secondDepMonthIncome) {
		this.secondDepMonthIncome = secondDepMonthIncome;
	}
	public Double getSecondDepSocialSecurity() {
		return secondDepSocialSecurity;
	}
	public void setSecondDepSocialSecurity(Double secondDepSocialSecurity) {
		this.secondDepSocialSecurity = secondDepSocialSecurity;
	}
	public List<Map<String, Object>> getEmployeeInfoList() {
		return employeeInfoList;
	}
	public void setEmployeeInfoList(List<Map<String, Object>> employeeInfoList) {
		this.employeeInfoList = employeeInfoList;
	}
	
}
