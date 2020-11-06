package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "certificate")
public class ErpCertificate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "time")
	private String time;
	
	@Column(name = "certificateName")
	private String certificateName;

	public String getCertificateName() {
		return certificateName;
	}

	public void setCertificateName(String certificateName) {
		this.certificateName = certificateName;
	}

	@Column(name = "filename")
	private String filename;

	@Column(name = "employeeId")
	private Integer employeeId;

	@Column(name = "description")
	private String description;

	@Column(name = "level")
	private String level;

	@Column(name = "organization")
	private String organization;

	@Column(name = "category")
	private String category;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLevel() {
		return level;
	}
	
	public void setLevel(String level) {
		this.level = level;
	}

	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "ErpCertificate [id=" + id + ", time=" + time + ", certificateName=" + certificateName + ", filename=" + filename
				+ ", employeeId=" + employeeId + ", description=" + description + ", level=" + level + ""
				+ ", organization=" + organization + "" + ", category=" + category + "]";
	}
	
}
