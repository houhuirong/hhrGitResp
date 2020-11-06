package com.nantian.erp.salary.data.model;

import javax.persistence.Table;

@Table(name="erp_positive_salary")
public class ErpPositiveSalary {
	
	//转正薪资表ID
	private Integer erpPstSalaryId;
	//转正基本工资-月度收入
	private String erpPositiveIncome;
	//转正基本工资
	private String erpPositiveBaseWage;
	//转正岗位工资
	private String erpPositivePostWage;
	//转正月度绩效
	private String erpPositivePerformance;
	//转正-项目津贴
	private String erpPositiveAllowance;
	//关联员工id
	private Integer erpEmployeeId;
	//试用期薪资比例
    private String erpPeriodBeliel;	
    //是否有试用期
    private Integer isPeriod;
    //话费补助
    private String erpTelFarePerquisite;    
    
	@Override
	public String toString() {
		return "ErpPositiveSalary [erpPstSalaryId=" + erpPstSalaryId
				+ ", erpPositiveIncome=" + erpPositiveIncome
				+ ", erpPositiveBaseWage=" + erpPositiveBaseWage
				+ ", erpPositivePostWage=" + erpPositivePostWage
				+ ", erpPositivePerformance=" + erpPositivePerformance
				+ ", erpPositiveAllowance=" + erpPositiveAllowance
				+ ", erpEmployeeId=" + erpEmployeeId + ", erpPeriodBeliel="
				+ erpPeriodBeliel + ", isPeriod=" + isPeriod
				+ ", erpTelFarePerquisite=" + erpTelFarePerquisite + "]";
	}
	public Integer getErpPstSalaryId() {
		return erpPstSalaryId;
	}
	public void setErpPstSalaryId(Integer erpPstSalaryId) {
		this.erpPstSalaryId = erpPstSalaryId;
	}
	public String getErpPositiveIncome() {
		return erpPositiveIncome;
	}
	public void setErpPositiveIncome(String erpPositiveIncome) {
		this.erpPositiveIncome = erpPositiveIncome;
	}
	public String getErpPositiveBaseWage() {
		return erpPositiveBaseWage;
	}
	public void setErpPositiveBaseWage(String erpPositiveBaseWage) {
		this.erpPositiveBaseWage = erpPositiveBaseWage;
	}
	public String getErpPositivePostWage() {
		return erpPositivePostWage;
	}
	public void setErpPositivePostWage(String erpPositivePostWage) {
		this.erpPositivePostWage = erpPositivePostWage;
	}
	
	public String getErpPositivePerformance() {
		return erpPositivePerformance;
	}
	public void setErpPositivePerformance(String erpPositivePerformance) {
		this.erpPositivePerformance = erpPositivePerformance;
	}
	public String getErpPositiveAllowance() {
		return erpPositiveAllowance;
	}
	public void setErpPositiveAllowance(String erpPositiveAllowance) {
		this.erpPositiveAllowance = erpPositiveAllowance;
	}
	public Integer getErpEmployeeId() {
		return erpEmployeeId;
	}
	public void setErpEmployeeId(Integer erpEmployeeId) {
		this.erpEmployeeId = erpEmployeeId;
	}
	public String getErpPeriodBeliel() {
		return erpPeriodBeliel;
	}
	public void setErpPeriodBeliel(String erpPeriodBeliel) {
		this.erpPeriodBeliel = erpPeriodBeliel;
	}
	public Integer getIsPeriod() {
		return isPeriod;
	}
	public void setIsPeriod(Integer isPeriod) {
		this.isPeriod = isPeriod;
	}
	public String getErpTelFarePerquisite() {
		return erpTelFarePerquisite;
	}
	public void setErpTelFarePerquisite(String erpTelFarePerquisite) {
		this.erpTelFarePerquisite = erpTelFarePerquisite;
	}
}
