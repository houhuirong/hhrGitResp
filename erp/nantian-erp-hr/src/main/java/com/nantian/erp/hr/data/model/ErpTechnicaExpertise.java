package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="technicaExpertise")
public class ErpTechnicaExpertise {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;
	
	@Column(name="employeeId")
	private Integer employeeId;
	
	@Column(name="technicalName")
	private String technicalName;

	@Column(name="qualification")
	private String qualification;	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public String getTechnicalName() {
		return technicalName;
	}

	public void setTechnicalName(String technicalName) {
		this.technicalName = technicalName;
	}

	public String getQualification() {
		return qualification;
	}

	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

	@Override
	public String toString() {
		return "ErpProjectExperience [id=" + id + ", employeeId=" + employeeId
				+ ", technicalName=" + technicalName + ", qualification=" + qualification + "]";
	}
	
}
