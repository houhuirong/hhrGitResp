package com.nantian.erp.hr.data.model;

public class ErpContract {
	private Integer contractId;
	private String beginTime;
	private String probationEndTime;
	private String endTime;
	private Integer employeeId ;
	private String renewalStartTime1;//第一次续签合同开始时间
	private String renewalEndTime1;//第一次续签合同结束时间
	private String renewalStartTime2;//第二次续签合同开始时间
	private String renewalEndTime2;//第二次续签合同结束时间
	  
	public Integer getContractId() {
		return contractId;
	}
	public void setContractId(Integer contractId) {
		this.contractId = contractId;
	}

	public String getProbationEndTime() {
		return probationEndTime;
	}
	public void setProbationEndTime(String probationEndTime) {
		this.probationEndTime = probationEndTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public String getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}
	public String getRenewalStartTime1() {
		return renewalStartTime1;
	}
	public void setRenewalStartTime1(String renewalStartTime1) {
		this.renewalStartTime1 = renewalStartTime1;
	}
	public String getRenewalEndTime1() {
		return renewalEndTime1;
	}
	public void setRenewalEndTime1(String renewalEndTime1) {
		this.renewalEndTime1 = renewalEndTime1;
	}
	public String getRenewalStartTime2() {
		return renewalStartTime2;
	}
	public void setRenewalStartTime2(String renewalStartTime2) {
		this.renewalStartTime2 = renewalStartTime2;
	}
	public String getRenewalEndTime2() {
		return renewalEndTime2;
	}
	public void setRenewalEndTime2(String renewalEndTime2) {
		this.renewalEndTime2 = renewalEndTime2;
	}
	
}
