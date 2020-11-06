package com.nantian.erp.salary.data.model;

/** 
 * Description: 面试谈薪表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpTalkSalary {
	
	//面试通过-关联的offerId
    private Integer offerId;
    //社招生-月度收入
    private String monthIncome;
    //社招生-社保基数
    private String socialSecurityBase;
    //社招生-公积金基数
    private String accumulationFundBase;
    //实习生-基本工资
    private String baseWage;
    //实习生-月度项目津贴
    private String monthAllowance;
    //备注，记录招聘谈薪过程的特殊情况
    private String remark;
    
	public Integer getOfferId() {
		return offerId;
	}
	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
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
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@Override
	public String toString() {
		return "ErpTalkSalary [offerId=" + offerId + ", monthIncome=" + monthIncome + ", socialSecurityBase="
				+ socialSecurityBase + ", accumulationFundBase=" + accumulationFundBase + ", baseWage=" + baseWage
				+ ", monthAllowance=" + monthAllowance + ", remark=" + remark + "]";
	}

}