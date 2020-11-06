package com.nantian.erp.salary.data.model;
/** 
 * Description: 员工薪酬-员工实际的社保和公积金的基数
 * @author 宋修功
 * @date 2020年02月18日
 * @version 1.0
 */
public class ErpActualInsuranceFundBase {
	private Integer id;
	private String endowmentInsuranceBase;	//'养老保险-实际基数'
	private String unemploymentInsuranceBase;	//'失业保险-实际基数'
	private String maternityInsuranceBase;	//生育保险-实际基数
	private String medicalInsuranceBase;	//医疗保险-实际基数
	private String injuryInsuranceBase;		//工伤保险-实际基数
	private String accumulationFundBase;	//公积金-实际基数
	private String tookEffectDate;		//生效的年度月份
	private String gmtModified;			//修改时间
	private String gmtCreate;			//创建时间
	private Integer employeeId;			//员工id
	
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}


	/**
	 * @return the endowmentInsuranceBase
	 */
	public String getEndowmentInsuranceBase() {
		return endowmentInsuranceBase;
	}


	/**
	 * @param endowmentInsuranceBase the endowmentInsuranceBase to set
	 */
	public void setEndowmentInsuranceBase(String endowmentInsuranceBase) {
		this.endowmentInsuranceBase = endowmentInsuranceBase;
	}


	/**
	 * @return the unemploymentInsuranceBase
	 */
	public String getUnemploymentInsuranceBase() {
		return unemploymentInsuranceBase;
	}


	/**
	 * @param unemploymentInsuranceBase the unemploymentInsuranceBase to set
	 */
	public void setUnemploymentInsuranceBase(String unemploymentInsuranceBase) {
		this.unemploymentInsuranceBase = unemploymentInsuranceBase;
	}


	/**
	 * @return the maternityInsuranceBase
	 */
	public String getMaternityInsuranceBase() {
		return maternityInsuranceBase;
	}


	/**
	 * @param maternityInsuranceBase the maternityInsuranceBase to set
	 */
	public void setMaternityInsuranceBase(String maternityInsuranceBase) {
		this.maternityInsuranceBase = maternityInsuranceBase;
	}


	/**
	 * @return the medicalInsuranceBase
	 */
	public String getMedicalInsuranceBase() {
		return medicalInsuranceBase;
	}


	/**
	 * @param medicalInsuranceBase the medicalInsuranceBase to set
	 */
	public void setMedicalInsuranceBase(String medicalInsuranceBase) {
		this.medicalInsuranceBase = medicalInsuranceBase;
	}


	/**
	 * @return the injuryInsuranceBase
	 */
	public String getInjuryInsuranceBase() {
		return injuryInsuranceBase;
	}


	/**
	 * @param injuryInsuranceBase the injuryInsuranceBase to set
	 */
	public void setInjuryInsuranceBase(String injuryInsuranceBase) {
		this.injuryInsuranceBase = injuryInsuranceBase;
	}


	/**
	 * @return the accumulationFundBase
	 */
	public String getAccumulationFundBase() {
		return accumulationFundBase;
	}


	/**
	 * @param accumulationFundBase the accumulationFundBase to set
	 */
	public void setAccumulationFundBase(String accumulationFundBase) {
		this.accumulationFundBase = accumulationFundBase;
	}


	/**
	 * @return the tookEffectDate
	 */
	public String getTookEffectDate() {
		return tookEffectDate;
	}


	/**
	 * @param tookEffectDate the tookEffectDate to set
	 */
	public void setTookEffectDate(String tookEffectDate) {
		this.tookEffectDate = tookEffectDate;
	}


	/**
	 * @return the gmtModified
	 */
	public String getGmtModified() {
		return gmtModified;
	}


	/**
	 * @param gmtModified the gmtModified to set
	 */
	public void setGmtModified(String gmtModified) {
		this.gmtModified = gmtModified;
	}


	/**
	 * @return the gmtCreate
	 */
	public String getGmtCreate() {
		return gmtCreate;
	}


	/**
	 * @param gmtCreate the gmtCreate to set
	 */
	public void setGmtCreate(String gmtCreate) {
		this.gmtCreate = gmtCreate;
	}




	/**
	 * @return the employeeId
	 */
	public Integer getEmployeeId() {
		return employeeId;
	}


	/**
	 * @param employeeId the employeeId to set
	 */
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}


	@Override
	public String toString() {
		return "ErpActualInsuranceFundBase [id=" + id + ", endowmentInsuranceBase=" + endowmentInsuranceBase
				+ ", unemploymentInsuranceBase=" + unemploymentInsuranceBase + ", maternityInsuranceBase="
				+ maternityInsuranceBase + ", medicalInsuranceBase=" + medicalInsuranceBase + ", injuryInsuranceBase="
				+ injuryInsuranceBase + ", accumulationFundBase=" + accumulationFundBase + ", tookEffectDate="
				+ tookEffectDate + ", gmtModified=" + gmtModified + ", gmtCreate=" + gmtCreate + ", employeeId="
				+ employeeId + "]";
	}
	
	
}
