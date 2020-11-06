package com.nantian.erp.hr.data.model;

/** 
 * Description: offer表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpOffer {
	//offer表编号
	private Integer offerId;
	//面试流程编号
	private Integer interviewId;
	//入职时间
	private String entryTime;
	//职位
	private Integer position;
	//职级
	private Integer rank;
	//社保地
	private String socialSecurityPlace;
	//试用期期限
	private Integer probationPeriod;
	//合同期限
	private Integer contractPeriod;
	//身份证号码
	private String idCardNumber;
	//毕业证书编号
	private String gradCertNumber;
	//对应招聘岗位
	private String jobPosition;
	//招聘渠道
	private String channel;
	//offer备注
	private String remark;
	//offer文件名
	private String offerFileName;
	//职业性格测验报告的文件名
	private String reportFileName;
	//offer状态（字典表中dic_type为OFFER_STATUS_TYPE）
	private String status;
	//档归原因
	private String reason;
    /**
     * 发送人Id
     */
    private Integer sendUserId;
	
	public Integer getOfferId() {
		return offerId;
	}
	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
	}
	public Integer getInterviewId() {
		return interviewId;
	}
	public void setInterviewId(Integer interviewId) {
		this.interviewId = interviewId;
	}
	public String getEntryTime() {
		return entryTime;
	}
	public void setEntryTime(String entryTime) {
		this.entryTime = entryTime;
	}
	public Integer getPosition() {
		return position;
	}
	public void setPosition(Integer position) {
		this.position = position;
	}
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	public String getSocialSecurityPlace() {
		return socialSecurityPlace;
	}
	public void setSocialSecurityPlace(String socialSecurityPlace) {
		this.socialSecurityPlace = socialSecurityPlace;
	}
	public Integer getProbationPeriod() {
		return probationPeriod;
	}
	public void setProbationPeriod(Integer probationPeriod) {
		this.probationPeriod = probationPeriod;
	}
	public Integer getContractPeriod() {
		return contractPeriod;
	}
	public void setContractPeriod(Integer contractPeriod) {
		this.contractPeriod = contractPeriod;
	}
	public String getIdCardNumber() {
		return idCardNumber;
	}
	public void setIdCardNumber(String idCardNumber) {
		this.idCardNumber = idCardNumber;
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
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getOfferFileName() {
		return offerFileName;
	}
	public void setOfferFileName(String offerFileName) {
		this.offerFileName = offerFileName;
	}
	public String getReportFileName() {
		return reportFileName;
	}
	public void setReportFileName(String reportFileName) {
		this.reportFileName = reportFileName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public Integer getSendUserId() {
		return sendUserId;
	}
	public void setSendUserId(Integer sendUserId) {
		this.sendUserId = sendUserId;
	}
	@Override
	public String toString() {
		return "ErpOffer [offerId=" + offerId + ", interviewId=" + interviewId
				+ ", entryTime=" + entryTime + ", position=" + position
				+ ", rank=" + rank + ", socialSecurityPlace="
				+ socialSecurityPlace + ", probationPeriod=" + probationPeriod
				+ ", contractPeriod=" + contractPeriod + ", idCardNumber="
				+ idCardNumber + ", gradCertNumber=" + gradCertNumber
				+ ", jobPosition=" + jobPosition + ", channel=" + channel
				+ ", remark=" + remark + ", offerFileName=" + offerFileName
				+ ", reportFileName=" + reportFileName + ", status=" + status
				+ ", reason=" + reason + ", sendUserId=" + sendUserId + "]";
	}
	
	
}
