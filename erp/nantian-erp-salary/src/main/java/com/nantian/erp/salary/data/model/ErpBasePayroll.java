package com.nantian.erp.salary.data.model;

/** 
 * Description: 薪酬管理表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpBasePayroll {
	
	//工资单编号
	private Integer erpPayrollId;
	//员工编号
	private Integer erpEmployeeId;
	
	/*
	 * 因为薪酬数据需要加密，所以将字段类型由数字修改为字符串
	 */
	//基本工资
	private String erpBaseWage;
	//岗位工资
	private String erpPostWage;
	//月度绩效
	private String erpPerformance;
	//月度项目津贴
	private String erpAllowance;
	//社保基数
	private String erpSocialSecurityBase;
	//公积金基数
	private String erpAccumulationFundBase;
	//话费补助
	private String erpTelFarePerquisite;
	
	public ErpBasePayroll() {
		super();
	}
	
	public Integer getErpPayrollId() {
		return erpPayrollId;
	}
	public void setErpPayrollId(Integer erpPayrollId) {
		this.erpPayrollId = erpPayrollId;
	}
	public Integer getErpEmployeeId() {
		return erpEmployeeId;
	}
	public void setErpEmployeeId(Integer erpEmployeeId) {
		this.erpEmployeeId = erpEmployeeId;
	}
	public String getErpBaseWage() {
		return erpBaseWage;
	}
	public void setErpBaseWage(String erpBaseWage) {
		this.erpBaseWage = erpBaseWage;
	}
	public String getErpPostWage() {
		return erpPostWage;
	}
	public void setErpPostWage(String erpPostWage) {
		this.erpPostWage = erpPostWage;
	}
	public String getErpPerformance() {
		return erpPerformance;
	}
	public void setErpPerformance(String erpPerformance) {
		this.erpPerformance = erpPerformance;
	}
	public String getErpAllowance() {
		return erpAllowance;
	}
	public void setErpAllowance(String erpAllowance) {
		this.erpAllowance = erpAllowance;
	}
	public String getErpSocialSecurityBase() {
		return erpSocialSecurityBase;
	}
	public void setErpSocialSecurityBase(String erpSocialSecurityBase) {
		this.erpSocialSecurityBase = erpSocialSecurityBase;
	}
	public String getErpAccumulationFundBase() {
		return erpAccumulationFundBase;
	}
	public void setErpAccumulationFundBase(String erpAccumulationFundBase) {
		this.erpAccumulationFundBase = erpAccumulationFundBase;
	}
	public String getErpTelFarePerquisite() {
		return erpTelFarePerquisite;
	}
	public void setErpTelFarePerquisite(String erpTelFarePerquisite) {
		this.erpTelFarePerquisite = erpTelFarePerquisite;
	}

	@Override
	public String toString() {
		return "ErpBasePayroll [erpPayrollId=" + erpPayrollId + ", erpEmployeeId=" + erpEmployeeId + ", erpBaseWage="
				+ erpBaseWage + ", erpPostWage=" + erpPostWage + ", erpPerformance=" + erpPerformance
				+ ", erpAllowance=" + erpAllowance + ", erpSocialSecurityBase=" + erpSocialSecurityBase
				+ ", erpAccumulationFundBase=" + erpAccumulationFundBase + ", erpTelFarePerquisite="
				+ erpTelFarePerquisite + "]";
	}
	
}
