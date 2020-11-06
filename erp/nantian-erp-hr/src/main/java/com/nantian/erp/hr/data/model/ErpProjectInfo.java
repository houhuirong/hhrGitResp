package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="projectinfo")
public class ErpProjectInfo  {
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	   @Column(name = "projectInfoId")
	private Integer projectInfoId;
	  @Column(name = "projectName")
	private String projectName;
	  @Column(name = "manager")
	private String manager;
	  @Column(name = "managerEmail")
	private String managerEmail;
	public Integer getProjectInfoId() {
		return projectInfoId;
	}
	public void setProjectInfoId(Integer projectInfoId) {
		this.projectInfoId = projectInfoId;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getManager() {
		return manager;
	}
	public void setManager(String manager) {
		this.manager = manager;
	}
	public String getManagerEmail() {
		return managerEmail;
	}
	public void setManagerEmail(String managerEmail) {
		this.managerEmail = managerEmail;
	}
	@Override
	public String toString() {
		return "ErpProjectInfo [projectInfoId=" + projectInfoId + ", projectName=" + projectName + ", manager="
				+ manager + ", managerEmail=" + managerEmail + "]";
	}
	
}
