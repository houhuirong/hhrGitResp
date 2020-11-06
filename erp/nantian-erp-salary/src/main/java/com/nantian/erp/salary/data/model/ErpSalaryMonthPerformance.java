package com.nantian.erp.salary.data.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class ErpSalaryMonthPerformance {
	
	//月度绩效ID
    private Integer erpMonthId;
    //月度
    private String erpMonthNum;
    //一级部门ID
    private Integer erpMonthFirstDepartmentId;
    //二级部门ID
    private Integer erpMonthSecondDepartmentId;
    //员工ID
    private Integer erpMonthEmpId;
    //比例
    private Double erpMonthBeliel;
    //工资绩效
    private String erpMonthMeritPay;
    //项目绩效
    private String erpMonthProjectPay;
    //项目绩效说明
    private String erpMonthProjectPayContent;
    //D类工资
    private String erpMonthDPay;
    //其他备注
    private String erpMonthRemark;
	//月度补贴
	private String erpMonthAllowance;
	//月度基本工资
    private String erpMonthBaseWage;
	//月度岗位工资
	private String erpMonthPostWage;
    
	private String erpMonthMealSubsidy;	//餐交补
	private String erpMonthTelSubsidy;	//手机话费补助
	private BigDecimal erpMonthShouldWorkDays;	//当月工作日
	private BigDecimal erpMonthActualWorkDays;	//实际工作天数
	private String erpMonthActualMeritPay;		//本月实际应发工资套改绩效
	private String erpMonthMeritSum;			//当月发放绩效合计
	

	public Integer getErpMonthId() {
		return erpMonthId;
	}
	public void setErpMonthId(Integer erpMonthId) {
		this.erpMonthId = erpMonthId;
	}
	public String getErpMonthNum() {
		return erpMonthNum;
	}
	public void setErpMonthNum(String erpMonthNum) {
		this.erpMonthNum = erpMonthNum;
	}
	public Integer getErpMonthFirstDepartmentId() {
		return erpMonthFirstDepartmentId;
	}
	public void setErpMonthFirstDepartmentId(Integer erpMonthFirstDepartmentId) {
		this.erpMonthFirstDepartmentId = erpMonthFirstDepartmentId;
	}
	public Integer getErpMonthSecondDepartmentId() {
		return erpMonthSecondDepartmentId;
	}
	public void setErpMonthSecondDepartmentId(Integer erpMonthSecondDepartmentId) {
		this.erpMonthSecondDepartmentId = erpMonthSecondDepartmentId;
	}
	public Integer getErpMonthEmpId() {
		return erpMonthEmpId;
	}
	public void setErpMonthEmpId(Integer erpMonthEmpId) {
		this.erpMonthEmpId = erpMonthEmpId;
	}
	public Double getErpMonthBeliel() {
		return erpMonthBeliel;
	}
	public void setErpMonthBeliel(Double erpMonthBeliel) {
		this.erpMonthBeliel = erpMonthBeliel;
	}
	public String getErpMonthMeritPay() {
		return erpMonthMeritPay;
	}
	public void setErpMonthMeritPay(String erpMonthMeritPay) {
		this.erpMonthMeritPay = erpMonthMeritPay;
	}
	public String getErpMonthProjectPay() {
		return erpMonthProjectPay;
	}
	public void setErpMonthProjectPay(String erpMonthProjectPay) {
		this.erpMonthProjectPay = erpMonthProjectPay;
	}
	public String getErpMonthProjectPayContent() {
		return erpMonthProjectPayContent;
	}
	public void setErpMonthProjectPayContent(String erpMonthProjectPayContent) {
		this.erpMonthProjectPayContent = erpMonthProjectPayContent;
	}
	public String getErpMonthDPay() {
		return erpMonthDPay;
	}
	public void setErpMonthDPay(String erpMonthDPay) {
		this.erpMonthDPay = erpMonthDPay;
	}
	public String getErpMonthRemark() {
		return erpMonthRemark;
	}
	public void setErpMonthRemark(String erpMonthRemark) {
		this.erpMonthRemark = erpMonthRemark;
	}
	public String getErpMonthAllowance() {
		return erpMonthAllowance;
	}
	public void setErpMonthAllowance(String erpMonthAllowance) {
		this.erpMonthAllowance = erpMonthAllowance;
	}
	public String getErpMonthBaseWage() {
		return erpMonthBaseWage;
	}
	public void setErpMonthBaseWage(String erpMonthBaseWage) {
		this.erpMonthBaseWage = erpMonthBaseWage;
	}
	public String getErpMonthPostWage() {
		return erpMonthPostWage;
	}
	public void setErpMonthPostWage(String erpMonthPostWage) {
		this.erpMonthPostWage = erpMonthPostWage;
	}
	public String getErpMonthMealSubsidy() {
		return erpMonthMealSubsidy;
	}
	public void setErpMonthMealSubsidy(String erpMonthMealSubsidy) {
		this.erpMonthMealSubsidy = erpMonthMealSubsidy;
	}
	public String getErpMonthTelSubsidy() {
		return erpMonthTelSubsidy;
	}
	public void setErpMonthTelSubsidy(String erpMonthTelSubsidy) {
		this.erpMonthTelSubsidy = erpMonthTelSubsidy;
	}
	public BigDecimal getErpMonthShouldWorkDays() {
		return erpMonthShouldWorkDays;
	}
	public void setErpMonthShouldWorkDays(BigDecimal erpMonthShouldWorkDays) {
		this.erpMonthShouldWorkDays = erpMonthShouldWorkDays;
	}
	public BigDecimal getErpMonthActualWorkDays() {
		return erpMonthActualWorkDays;
	}
	public void setErpMonthActualWorkDays(BigDecimal erpMonthActualWorkDays) {
		this.erpMonthActualWorkDays = erpMonthActualWorkDays;
	}
	public String getErpMonthActualMeritPay() {
		return erpMonthActualMeritPay;
	}
	public void setErpMonthActualMeritPay(String erpMonthActualMeritPay) {
		this.erpMonthActualMeritPay = erpMonthActualMeritPay;
	}
	public String getErpMonthMeritSum() {
		return erpMonthMeritSum;
	}
	public void setErpMonthMeritSum(String erpMonthMeritSum) {
		this.erpMonthMeritSum = erpMonthMeritSum;
	}
	
	@Override
	public String toString() {
		return "ErpSalaryMonthPerformance [erpMonthId=" + erpMonthId + ", erpMonthNum=" + erpMonthNum
				+ ", erpMonthFirstDepartmentId=" + erpMonthFirstDepartmentId + ", erpMonthSecondDepartmentId="
				+ erpMonthSecondDepartmentId + ", erpMonthEmpId=" + erpMonthEmpId + ", erpMonthBeliel=" + erpMonthBeliel
				+ ", erpMonthMeritPay=" + erpMonthMeritPay + ", erpMonthProjectPay=" + erpMonthProjectPay
				+ ", erpMonthProjectPayContent=" + erpMonthProjectPayContent + ", erpMonthDPay=" + erpMonthDPay
				+ ", erpMonthRemark=" + erpMonthRemark + ", erpMonthAllowance=" + erpMonthAllowance
				+ ", erpMonthBaseWage=" + erpMonthBaseWage + ", erpMonthPostWage=" + erpMonthPostWage
				+ ", erpMonthMealSubsidy=" + erpMonthMealSubsidy + ", erpMonthTelSubsidy=" + erpMonthTelSubsidy
				+ ", erpMonthShouldWorkDays=" + erpMonthShouldWorkDays + ", erpMonthActualWorkDays="
				+ erpMonthActualWorkDays + ", erpMonthActualMeritPay=" + erpMonthActualMeritPay + ", erpMonthMeritSum="
				+ erpMonthMeritSum + "]";
	}
}