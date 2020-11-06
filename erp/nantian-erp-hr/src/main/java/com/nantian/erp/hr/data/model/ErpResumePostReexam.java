package com.nantian.erp.hr.data.model;

/** 
 * Description: 复试信息表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpResumePostReexam {
	//复试临时表编号
	private Integer id;
	//面试流程表编号
	private Integer interviewId;
	//入职时间
	private String entryTime;
	//职位
	private Integer position;
	//职级
	private Integer rank;
	//月度收入
	private String monthIncome;
	//社保基数
	private String socialSecurityBase;
	//公积金基数
	private String accumulationFundBase;
	//社保地
	private String socialSecurityPlace;
	//试用期期限
	private Integer probationPeriod;
	//合同期限
	private Integer contractPeriod;
	//备注（记录招聘谈薪过程的特殊情况）
	private String remark;
	//素质模型打分
	private String score;
	
	//通过理由	SXG 2019-09-19
	private String contents;
	
	//是否通过	SXG 2019-09-23
	private String pass;
	
	//是否有下一轮面试
	private Boolean isNext;
	
	//复试面试官
	private Integer personId;
	
	//是否预约复试时间
	private Boolean appointment;
	
	public Boolean getIsNext() {
		return isNext;
	}
	public void setIsNext(Boolean isNext) {
		this.isNext = isNext;
	}
	public Integer getPersonId() {
		return personId;
	}
	public void setPersonId(Integer personId) {
		this.personId = personId;
	}
	public Boolean getAppointment() {
		return appointment;
	}
	public void setAppointment(Boolean appointment) {
		this.appointment = appointment;
	}
	
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	public String getMonthIncome() {
		return monthIncome;
	}
	public void setMonthIncome(String monthIncome) {
		this.monthIncome = monthIncome;
	}
	public String getSocialSecurityBase() {
		return socialSecurityBase;
	}
	public void setSocialSecurityBase(String socialSecurityBase) {
		this.socialSecurityBase = socialSecurityBase;
	}
	public String getAccumulationFundBase() {
		return accumulationFundBase;
	}
	public void setAccumulationFundBase(String accumulationFundBase) {
		this.accumulationFundBase = accumulationFundBase;
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
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	
	@Override
	public String toString() {
		return "ErpResumePostReexam [id=" + id + ", interviewId=" + interviewId + ", entryTime=" + entryTime
				+ ", position=" + position + ", rank=" + rank + ", monthIncome=" + monthIncome + ", socialSecurityBase="
				+ socialSecurityBase + ", accumulationFundBase=" + accumulationFundBase + ", socialSecurityPlace="
				+ socialSecurityPlace + ", probationPeriod=" + probationPeriod + ", contractPeriod=" + contractPeriod
				+ ", remark=" + remark + ", score=" + score + ", contents=" + contents  + ", pass=" + pass
				+ ", isNext=" + isNext + ", personId=" + personId  + ", appointment=" + appointment + "]";
	}
	
}
