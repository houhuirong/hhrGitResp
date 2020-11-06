package com.nantian.erp.salary.data.model;

/** 
 * Description: 社保表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpSocialSecurity {
	private Integer id;//主键
	private Double endowmentInsuranceCompanyRatio;//养老保险-公司缴纳比例
	private Double endowmentInsurancePersonRatio;//养老保险-个人缴纳比例
	private Double endowmentInsuranceBaseUpper;//养老保险-缴纳基数上限
	private Double endowmentInsuranceBaseLower;//养老保险-缴纳基数下限
	private Double unemploymentInsuranceCompanyRatio;//失业保险-公司缴纳比例
	private Double unemploymentInsurancePersonRatio;//失业保险-个人缴纳比例
	private Double unemploymentInsuranceBaseUpper;//失业保险-缴纳基数上限
	private Double unemploymentInsuranceBaseLower;//失业保险-缴纳基数下限
	private Double maternityInsuranceCompanyRatio;//生育保险-公司缴纳比例
	private Double maternityInsurancePersonRatio;//生育保险-个人缴纳比例
	private Double maternityInsuranceBaseUpper;//生育保险-缴纳基数上限
	private Double maternityInsuranceBaseLower;//生育保险-缴纳基数下限
	private Double medicalInsuranceCompanyRatio;//医疗保险-公司缴纳比例
	private Double medicalInsurancePersonRatio;//医疗保险-个人缴纳比例
	private Double medicalInsuranceBaseUpper;//医疗保险-缴纳基数上限
	private Double medicalInsuranceBaseLower;//医疗保险-缴纳基数下限
	private Double injuryInsuranceCompanyRatio;//工伤保险-公司缴纳比例
	private Double injuryInsurancePersonRatio;//工伤保险-个人缴纳比例
	private Double injuryInsuranceBaseUpper;//工伤保险-缴纳基数上限
	private Double injuryInsuranceBaseLower;//工伤保险-缴纳基数下限
	private Double accumulationFundCompanyRatio;//公积金-公司缴纳比例
	private Double accumulationFundPersonRatio;//公积金-个人缴纳比例
	private Double accumulationFundBaseUpper;//公积金-缴纳基数上限
	private Double accumulationFundBaseLower;//公积金-缴纳基数下限
	private String startTime;//生效时间
	private String endTime;//失效时间
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Double getEndowmentInsuranceCompanyRatio() {
		return endowmentInsuranceCompanyRatio;
	}
	public void setEndowmentInsuranceCompanyRatio(Double endowmentInsuranceCompanyRatio) {
		this.endowmentInsuranceCompanyRatio = endowmentInsuranceCompanyRatio;
	}
	public Double getEndowmentInsurancePersonRatio() {
		return endowmentInsurancePersonRatio;
	}
	public void setEndowmentInsurancePersonRatio(Double endowmentInsurancePersonRatio) {
		this.endowmentInsurancePersonRatio = endowmentInsurancePersonRatio;
	}
	public Double getEndowmentInsuranceBaseUpper() {
		return endowmentInsuranceBaseUpper;
	}
	public void setEndowmentInsuranceBaseUpper(Double endowmentInsuranceBaseUpper) {
		this.endowmentInsuranceBaseUpper = endowmentInsuranceBaseUpper;
	}
	public Double getEndowmentInsuranceBaseLower() {
		return endowmentInsuranceBaseLower;
	}
	public void setEndowmentInsuranceBaseLower(Double endowmentInsuranceBaseLower) {
		this.endowmentInsuranceBaseLower = endowmentInsuranceBaseLower;
	}
	public Double getUnemploymentInsuranceCompanyRatio() {
		return unemploymentInsuranceCompanyRatio;
	}
	public void setUnemploymentInsuranceCompanyRatio(Double unemploymentInsuranceCompanyRatio) {
		this.unemploymentInsuranceCompanyRatio = unemploymentInsuranceCompanyRatio;
	}
	public Double getUnemploymentInsurancePersonRatio() {
		return unemploymentInsurancePersonRatio;
	}
	public void setUnemploymentInsurancePersonRatio(Double unemploymentInsurancePersonRatio) {
		this.unemploymentInsurancePersonRatio = unemploymentInsurancePersonRatio;
	}
	public Double getUnemploymentInsuranceBaseUpper() {
		return unemploymentInsuranceBaseUpper;
	}
	public void setUnemploymentInsuranceBaseUpper(Double unemploymentInsuranceBaseUpper) {
		this.unemploymentInsuranceBaseUpper = unemploymentInsuranceBaseUpper;
	}
	public Double getUnemploymentInsuranceBaseLower() {
		return unemploymentInsuranceBaseLower;
	}
	public void setUnemploymentInsuranceBaseLower(Double unemploymentInsuranceBaseLower) {
		this.unemploymentInsuranceBaseLower = unemploymentInsuranceBaseLower;
	}
	public Double getMaternityInsuranceCompanyRatio() {
		return maternityInsuranceCompanyRatio;
	}
	public void setMaternityInsuranceCompanyRatio(Double maternityInsuranceCompanyRatio) {
		this.maternityInsuranceCompanyRatio = maternityInsuranceCompanyRatio;
	}
	public Double getMaternityInsurancePersonRatio() {
		return maternityInsurancePersonRatio;
	}
	public void setMaternityInsurancePersonRatio(Double maternityInsurancePersonRatio) {
		this.maternityInsurancePersonRatio = maternityInsurancePersonRatio;
	}
	public Double getMaternityInsuranceBaseUpper() {
		return maternityInsuranceBaseUpper;
	}
	public void setMaternityInsuranceBaseUpper(Double maternityInsuranceBaseUpper) {
		this.maternityInsuranceBaseUpper = maternityInsuranceBaseUpper;
	}
	public Double getMaternityInsuranceBaseLower() {
		return maternityInsuranceBaseLower;
	}
	public void setMaternityInsuranceBaseLower(Double maternityInsuranceBaseLower) {
		this.maternityInsuranceBaseLower = maternityInsuranceBaseLower;
	}
	public Double getMedicalInsuranceCompanyRatio() {
		return medicalInsuranceCompanyRatio;
	}
	public void setMedicalInsuranceCompanyRatio(Double medicalInsuranceCompanyRatio) {
		this.medicalInsuranceCompanyRatio = medicalInsuranceCompanyRatio;
	}
	public Double getMedicalInsurancePersonRatio() {
		return medicalInsurancePersonRatio;
	}
	public void setMedicalInsurancePersonRatio(Double medicalInsurancePersonRatio) {
		this.medicalInsurancePersonRatio = medicalInsurancePersonRatio;
	}
	public Double getMedicalInsuranceBaseUpper() {
		return medicalInsuranceBaseUpper;
	}
	public void setMedicalInsuranceBaseUpper(Double medicalInsuranceBaseUpper) {
		this.medicalInsuranceBaseUpper = medicalInsuranceBaseUpper;
	}
	public Double getMedicalInsuranceBaseLower() {
		return medicalInsuranceBaseLower;
	}
	public void setMedicalInsuranceBaseLower(Double medicalInsuranceBaseLower) {
		this.medicalInsuranceBaseLower = medicalInsuranceBaseLower;
	}
	public Double getInjuryInsuranceCompanyRatio() {
		return injuryInsuranceCompanyRatio;
	}
	public void setInjuryInsuranceCompanyRatio(Double injuryInsuranceCompanyRatio) {
		this.injuryInsuranceCompanyRatio = injuryInsuranceCompanyRatio;
	}
	public Double getInjuryInsurancePersonRatio() {
		return injuryInsurancePersonRatio;
	}
	public void setInjuryInsurancePersonRatio(Double injuryInsurancePersonRatio) {
		this.injuryInsurancePersonRatio = injuryInsurancePersonRatio;
	}
	public Double getInjuryInsuranceBaseUpper() {
		return injuryInsuranceBaseUpper;
	}
	public void setInjuryInsuranceBaseUpper(Double injuryInsuranceBaseUpper) {
		this.injuryInsuranceBaseUpper = injuryInsuranceBaseUpper;
	}
	public Double getInjuryInsuranceBaseLower() {
		return injuryInsuranceBaseLower;
	}
	public void setInjuryInsuranceBaseLower(Double injuryInsuranceBaseLower) {
		this.injuryInsuranceBaseLower = injuryInsuranceBaseLower;
	}
	public Double getAccumulationFundCompanyRatio() {
		return accumulationFundCompanyRatio;
	}
	public void setAccumulationFundCompanyRatio(Double accumulationFundCompanyRatio) {
		this.accumulationFundCompanyRatio = accumulationFundCompanyRatio;
	}
	public Double getAccumulationFundPersonRatio() {
		return accumulationFundPersonRatio;
	}
	public void setAccumulationFundPersonRatio(Double accumulationFundPersonRatio) {
		this.accumulationFundPersonRatio = accumulationFundPersonRatio;
	}
	public Double getAccumulationFundBaseUpper() {
		return accumulationFundBaseUpper;
	}
	public void setAccumulationFundBaseUpper(Double accumulationFundBaseUpper) {
		this.accumulationFundBaseUpper = accumulationFundBaseUpper;
	}
	public Double getAccumulationFundBaseLower() {
		return accumulationFundBaseLower;
	}
	public void setAccumulationFundBaseLower(Double accumulationFundBaseLower) {
		this.accumulationFundBaseLower = accumulationFundBaseLower;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	@Override
	public String toString() {
		return "ErpSocialSecurity [id=" + id + ", endowmentInsuranceCompanyRatio=" + endowmentInsuranceCompanyRatio
				+ ", endowmentInsurancePersonRatio=" + endowmentInsurancePersonRatio + ", endowmentInsuranceBaseUpper="
				+ endowmentInsuranceBaseUpper + ", endowmentInsuranceBaseLower=" + endowmentInsuranceBaseLower
				+ ", unemploymentInsuranceCompanyRatio=" + unemploymentInsuranceCompanyRatio
				+ ", unemploymentInsurancePersonRatio=" + unemploymentInsurancePersonRatio
				+ ", unemploymentInsuranceBaseUpper=" + unemploymentInsuranceBaseUpper
				+ ", unemploymentInsuranceBaseLower=" + unemploymentInsuranceBaseLower
				+ ", maternityInsuranceCompanyRatio=" + maternityInsuranceCompanyRatio
				+ ", maternityInsurancePersonRatio=" + maternityInsurancePersonRatio + ", maternityInsuranceBaseUpper="
				+ maternityInsuranceBaseUpper + ", maternityInsuranceBaseLower=" + maternityInsuranceBaseLower
				+ ", medicalInsuranceCompanyRatio=" + medicalInsuranceCompanyRatio + ", medicalInsurancePersonRatio="
				+ medicalInsurancePersonRatio + ", medicalInsuranceBaseUpper=" + medicalInsuranceBaseUpper
				+ ", medicalInsuranceBaseLower=" + medicalInsuranceBaseLower + ", injuryInsuranceCompanyRatio="
				+ injuryInsuranceCompanyRatio + ", injuryInsurancePersonRatio=" + injuryInsurancePersonRatio
				+ ", injuryInsuranceBaseUpper=" + injuryInsuranceBaseUpper + ", injuryInsuranceBaseLower="
				+ injuryInsuranceBaseLower + ", accumulationFundCompanyRatio=" + accumulationFundCompanyRatio
				+ ", accumulationFundPersonRatio=" + accumulationFundPersonRatio + ", accumulationFundBaseUpper="
				+ accumulationFundBaseUpper + ", accumulationFundBaseLower=" + accumulationFundBaseLower
				+ ", startTime=" + startTime + ", endTime=" + endTime + "]";
	}
	
}
