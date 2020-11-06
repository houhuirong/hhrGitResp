package com.nantian.erp.salary.data.model;

/**
 * Description:薪资范围设置
 * @author HouHuiRong
 * @version 1.0
 * */
public class ErpSalaryRangeSet {

	//薪资范围设置ID
	private Integer erpSalaryRangeSetId;
	//职级
	private Integer erpPositionNo;
	//工资上限
	private String erpSalaryMax;
	//工资下限
	private String erpSalaryMin;
	//社保上限
	private String erpSocialSecurityMax;
	//社保下限
	private String erpSocialSecurityMin;
	
	public Integer getErpSalaryRangeSetId() {
		return erpSalaryRangeSetId;
	}
	public void setErpSalaryRangeSetId(Integer erpSalaryRangeSetId) {
		this.erpSalaryRangeSetId = erpSalaryRangeSetId;
	}
	public Integer getErpPositionNo() {
		return erpPositionNo;
	}
	public void setErpPositionNo(Integer erpPositionNo) {
		this.erpPositionNo = erpPositionNo;
	}
	public String getErpSalaryMax() {
		return erpSalaryMax;
	}
	public void setErpSalaryMax(String erpSalaryMax) {
		this.erpSalaryMax = erpSalaryMax;
	}
	public String getErpSalaryMin() {
		return erpSalaryMin;
	}
	public void setErpSalaryMin(String erpSalaryMin) {
		this.erpSalaryMin = erpSalaryMin;
	}
	public String getErpSocialSecurityMax() {
		return erpSocialSecurityMax;
	}
	public void setErpSocialSecurityMax(String erpSocialSecurityMax) {
		this.erpSocialSecurityMax = erpSocialSecurityMax;
	}
	public String getErpSocialSecurityMin() {
		return erpSocialSecurityMin;
	}
	public void setErpSocialSecurityMin(String erpSocialSecurityMin) {
		this.erpSocialSecurityMin = erpSocialSecurityMin;
	}
	
	@Override
	public String toString() {
		return "ErpSalaryRangeSet [erpSalaryRangeSetId=" + erpSalaryRangeSetId
				+ ", erpPositionNo=" + erpPositionNo + ", erpSalaryMax=" + erpSalaryMax
				+ ", erpSalaryMin=" + erpSalaryMin + ", erpSocialSecurityMax="
				+ erpSocialSecurityMax + ", erpSocialSecurityMin="
				+ erpSocialSecurityMin + "]";
	}	
}
