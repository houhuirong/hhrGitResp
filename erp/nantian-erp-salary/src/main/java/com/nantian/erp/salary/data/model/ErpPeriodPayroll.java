package com.nantian.erp.salary.data.model;

import javax.persistence.Table;

@Table(name = "nantiansalary.erp_payroll")
public class ErpPeriodPayroll {
	
	//工资单ID
    private Integer erpPayrollId;
	//试用期基本工资
    private String erpPeriodBaseWage;
    //试用期岗位工资
    private String erpPeriodPostWage;
    //试用期月度绩效
    private String erpPeriodPerformance;
    //试用期津贴-D类工资
    private String erpPeriodAllowance;
    //关联员工id
    private Integer erpEmployeeId;
    //试用期基本工资-月度收入
    private String erpPeriodIncome;   
    //话费补助
    private String erpTelFarePerquisite;      
    
	public ErpPeriodPayroll(){
    	super();
    }

	public ErpPeriodPayroll(Integer erpPayrollId, String erpPeriodBaseWage,
			String erpPeriodPostWage, String erpPeriodPerformance,
			String erpPeriodAllowance, Integer erpEmployeeId,
			String erpPeriodIncome, String erpTelFarePerquisite) {
		super();
		this.erpPayrollId = erpPayrollId;
		this.erpPeriodBaseWage = erpPeriodBaseWage;
		this.erpPeriodPostWage = erpPeriodPostWage;
		this.erpPeriodPerformance = erpPeriodPerformance;
		this.erpPeriodAllowance = erpPeriodAllowance;
		this.erpEmployeeId = erpEmployeeId;
		this.erpPeriodIncome = erpPeriodIncome;
		this.erpTelFarePerquisite = erpTelFarePerquisite;
	}

	public Integer getErpPayrollId() {
		return erpPayrollId;
	}
	public void setErpPayrollId(Integer erpPayrollId) {
		this.erpPayrollId = erpPayrollId;
	}
	public String getErpPeriodBaseWage() {
		return erpPeriodBaseWage;
	}
	public void setErpPeriodBaseWage(String erpPeriodBaseWage) {
		this.erpPeriodBaseWage = erpPeriodBaseWage;
	}
	public String getErpPeriodPostWage() {
		return erpPeriodPostWage;
	}
	public void setErpPeriodPostWage(String erpPeriodPostWage) {
		this.erpPeriodPostWage = erpPeriodPostWage;
	}
	public String getErpPeriodPerformance() {
		return erpPeriodPerformance;
	}
	public void setErpPeriodPerformance(String erpPeriodPerformance) {
		this.erpPeriodPerformance = erpPeriodPerformance;
	}
	public String getErpPeriodAllowance() {
		return erpPeriodAllowance;
	}
	public void setErpPeriodAllowance(String erpPeriodAllowance) {
		this.erpPeriodAllowance = erpPeriodAllowance;
	}
	public Integer getErpEmployeeId() {
		return erpEmployeeId;
	}
	public void setErpEmployeeId(Integer erpEmployeeId) {
		this.erpEmployeeId = erpEmployeeId;
	}
	public String getErpPeriodIncome() {
		return erpPeriodIncome;
	}
	public void setErpPeriodIncome(String erpPeriodIncome) {
		this.erpPeriodIncome = erpPeriodIncome;
	}
	public String getErpTelFarePerquisite() {
		return erpTelFarePerquisite;
	}
	public void setErpTelFarePerquisite(String erpTelFarePerquisite) {
		this.erpTelFarePerquisite = erpTelFarePerquisite;
	}
}