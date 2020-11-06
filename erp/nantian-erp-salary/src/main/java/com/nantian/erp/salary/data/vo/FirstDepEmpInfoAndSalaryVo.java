package com.nantian.erp.salary.data.vo;

import java.util.List;

/** 
 * Description: 一级部门下所有员工的信息和薪酬情况
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
public class FirstDepEmpInfoAndSalaryVo {
	
	//一级部门Id
	private Integer firstDepartmentId;
	//一级部门名称
	private String firstDepartmentName;
	//一级部门月度收入合计
	private Double firstDepMonthIncome;
	//一级部门社保合计
	private Double firstDepSocialSecurity;
	//二级部门员工信息列表
	private List<SecondDepEmpInfoAndSalaryVo> secondDepEmpInfoAndSalaryVoList;
	
	public Integer getFirstDepartmentId() {
		return firstDepartmentId;
	}
	public void setFirstDepartmentId(Integer firstDepartmentId) {
		this.firstDepartmentId = firstDepartmentId;
	}
	public String getFirstDepartmentName() {
		return firstDepartmentName;
	}
	public void setFirstDepartmentName(String firstDepartmentName) {
		this.firstDepartmentName = firstDepartmentName;
	}
	public Double getFirstDepMonthIncome() {
		return firstDepMonthIncome;
	}
	public void setFirstDepMonthIncome(Double firstDepMonthIncome) {
		this.firstDepMonthIncome = firstDepMonthIncome;
	}
	public Double getFirstDepSocialSecurity() {
		return firstDepSocialSecurity;
	}
	public void setFirstDepSocialSecurity(Double firstDepSocialSecurity) {
		this.firstDepSocialSecurity = firstDepSocialSecurity;
	}
	public List<SecondDepEmpInfoAndSalaryVo> getSecondDepEmpInfoAndSalaryVoList() {
		return secondDepEmpInfoAndSalaryVoList;
	}
	public void setSecondDepEmpInfoAndSalaryVoList(List<SecondDepEmpInfoAndSalaryVo> secondDepEmpInfoAndSalaryVoList) {
		this.secondDepEmpInfoAndSalaryVoList = secondDepEmpInfoAndSalaryVoList;
	}

}
