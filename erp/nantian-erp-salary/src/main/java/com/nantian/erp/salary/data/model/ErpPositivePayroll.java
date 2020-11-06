package com.nantian.erp.salary.data.model;

public class ErpPositivePayroll {
	
	//主键
	private Integer erpPositiveId;
    //转正基本工资
    private String erpPositiveBaseWage;
    //转正岗位工资
    private String erpPositivePostWage;
    //转正月度绩效
    private String erpPositivePerformance;
    //关联员工id
    private Integer erpEmployeeId;
    //转正基本工资-项目津贴
    private String erpPositiveAllowance;
    //转正基本工资-月度收入
    private String erpPositiveIncome;
    //社保基数
    private String erpSocialSecurityIndex;
    //公积金基数
    private String erpAccumulationFundIndex;    
  //话费补助
    private String erpTelFarePerquisite; 
    
	public ErpPositivePayroll(){
    	super();
    }
	
	public ErpPositivePayroll(Integer erpPositiveId,
			String erpPositiveBaseWage, String erpPositivePostWage,
			String erpPositivePerformance, Integer erpEmployeeId,
			String erpPositiveAllowance, String erpPositiveIncome,
			String erpSocialSecurityIndex, String erpAccumulationFundIndex,
			String erpTelFarePerquisite) {
		super();
		this.erpPositiveId = erpPositiveId;
		this.erpPositiveBaseWage = erpPositiveBaseWage;
		this.erpPositivePostWage = erpPositivePostWage;
		this.erpPositivePerformance = erpPositivePerformance;
		this.erpEmployeeId = erpEmployeeId;
		this.erpPositiveAllowance = erpPositiveAllowance;
		this.erpPositiveIncome = erpPositiveIncome;
		this.erpSocialSecurityIndex = erpSocialSecurityIndex;
		this.erpAccumulationFundIndex = erpAccumulationFundIndex;
		this.erpTelFarePerquisite = erpTelFarePerquisite;
	}

	public Integer getErpPositiveId() {
		return erpPositiveId;
	}
	public void setErpPositiveId(Integer erpPositiveId) {
		this.erpPositiveId = erpPositiveId;
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
	public Integer getErpEmployeeId() {
		return erpEmployeeId;
	}
	public void setErpEmployeeId(Integer erpEmployeeId) {
		this.erpEmployeeId = erpEmployeeId;
	}
	public String getErpPositiveAllowance() {
		return erpPositiveAllowance;
	}
	public void setErpPositiveAllowance(String erpPositiveAllowance) {
		this.erpPositiveAllowance = erpPositiveAllowance;
	}
	public String getErpPositiveIncome() {
		return erpPositiveIncome;
	}
	public void setErpPositiveIncome(String erpPositiveIncome) {
		this.erpPositiveIncome = erpPositiveIncome;
	}
	public String getErpSocialSecurityIndex() {
		return erpSocialSecurityIndex;
	}
	public void setErpSocialSecurityIndex(String erpSocialSecurityIndex) {
		this.erpSocialSecurityIndex = erpSocialSecurityIndex;
	}
	public String getErpAccumulationFundIndex() {
		return erpAccumulationFundIndex;
	}
	public void setErpAccumulationFundIndex(String erpAccumulationFundIndex) {
		this.erpAccumulationFundIndex = erpAccumulationFundIndex;
	}
	public String getErpTelFarePerquisite() {
		return erpTelFarePerquisite;
	}
	public void setErpTelFarePerquisite(String erpTelFarePerquisite) {
		this.erpTelFarePerquisite = erpTelFarePerquisite;
	}
}