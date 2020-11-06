package com.nantian.erp.hr.data.model;

public class PostTemplate {
	private Integer  postTemplateId; //岗位模板编号
	private String category; //岗位类别
	private String postName; //岗位名称
	private String jobCategory; //职位类别
	private String salaryRange; //薪资范围
	private String positionChildType;//职位子类
	private String familyId; //职位职级族码值
	private String duty; //职责
	private String required; //要求
	public Integer getPostTemplateId() {
		return postTemplateId;
	}
	public void setPostTemplateId(Integer postTemplateId) {
		this.postTemplateId = postTemplateId;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getPostName() {
		return postName;
	}
	public void setPostName(String postName) {
		this.postName = postName;
	}
	public String getJobCategory() {
		return jobCategory;
	}
	public void setJobCategory(String jobCategory) {
		this.jobCategory = jobCategory;
	}
	public String getSalaryRange() {
		return salaryRange;
	}
	public void setSalaryRange(String salaryRange) {
		this.salaryRange = salaryRange;
	}
	
	
	
	public String getPositionChildType() {
		return positionChildType;
	}
	public void setPositionChildType(String positionChildType) {
		this.positionChildType = positionChildType;
	}
	
	public String getFamilyId() {
		return familyId;
	}
	public void setFamilyId(String familyId) {
		this.familyId = familyId;
	}
	public String getDuty() {
		return duty;
	}
	public void setDuty(String duty) {
		this.duty = duty;
	}
	public String getRequired() {
		return required;
	}
	public void setRequired(String required) {
		this.required = required;
	}
	@Override
	public String toString() {
		return "PostTemplate [postTemplateId=" + postTemplateId + ", category=" + category + ", postName=" + postName
				+ ", jobCategory=" + jobCategory + ", salaryRange=" + salaryRange + ", positionChildType="
				+ positionChildType + ", familyId=" + familyId + ", duty=" + duty + ", required=" + required + "]";
	}

	
	
	
	
	
	
	
}
