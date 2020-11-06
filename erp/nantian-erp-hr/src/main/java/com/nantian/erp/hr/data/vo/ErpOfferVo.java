package com.nantian.erp.hr.data.vo;

/*
 * 接收前端的offer参数
 */
public class ErpOfferVo {
	/*
	 * 公共参数
	 */
	private Integer offerId;//offer表编号
	private Boolean isTrainee;//是否是实习生
	private String idCardNumber;//身份证号码
	private String entryTime;//入职时间
	
	/*
	 * 实习生参数
	 */
	private String baseWage;//基本工资
	private String monthAllowance;//月度项目津贴
	private String salaryRemark;//谈薪备注
	
	/*
	 * 社招生参数
	 */
	private String gradCertNumber;//毕业证书编号
	private String jobPosition;//对应招聘岗位
	private String channel;//招聘渠道
	private String offerRemark;//offer备注
	
	
	public Integer getOfferId() {
		return offerId;
	}
	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
	}
	public Boolean getIsTrainee() {
		return isTrainee;
	}
	public void setIsTrainee(Boolean isTrainee) {
		this.isTrainee = isTrainee;
	}
	public String getIdCardNumber() {
		return idCardNumber;
	}
	public void setIdCardNumber(String idCardNumber) {
		this.idCardNumber = idCardNumber;
	}
	public String getBaseWage() {
		return baseWage;
	}
	public void setBaseWage(String baseWage) {
		this.baseWage = baseWage;
	}
	public String getMonthAllowance() {
		return monthAllowance;
	}
	public void setMonthAllowance(String monthAllowance) {
		this.monthAllowance = monthAllowance;
	}
	public String getSalaryRemark() {
		return salaryRemark;
	}
	public void setSalaryRemark(String salaryRemark) {
		this.salaryRemark = salaryRemark;
	}
	public String getGradCertNumber() {
		return gradCertNumber;
	}
	public void setGradCertNumber(String gradCertNumber) {
		this.gradCertNumber = gradCertNumber;
	}
	public String getJobPosition() {
		return jobPosition;
	}
	public void setJobPosition(String jobPosition) {
		this.jobPosition = jobPosition;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getOfferRemark() {
		return offerRemark;
	}
	public void setOfferRemark(String offerRemark) {
		this.offerRemark = offerRemark;
	}
	public String getEntryTime() {
		return entryTime;
	}
	public void setEntryTime(String entryTime) {
		this.entryTime = entryTime;
	}
	
	@Override
	public String toString() {
		return "ErpOfferVo [offerId=" + offerId + ", isTrainee=" + isTrainee + ", idCardNumber=" + idCardNumber
				+ ", baseWage=" + baseWage + ", monthAllowance="
				+ monthAllowance + ", salaryRemark=" + salaryRemark + ", gradCertNumber=" + gradCertNumber
				+ ", jobPosition=" + jobPosition + ", channel=" + channel + ", offerRemark=" + offerRemark
				+ ", entryTime=" + entryTime + "]";
	}
	
}
