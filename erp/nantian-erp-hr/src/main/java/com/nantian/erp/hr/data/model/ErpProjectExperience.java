package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="projectExperience")
public class ErpProjectExperience {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;
	
	@Column(name="startTime")
	private String startTime;
	
	@Column(name="employeeId")
	private Integer employeeId;
	
	@Column(name="projectName")
	private String projectName;
	
	@Column(name="post")
	private String post;
	
	@Column(name="description")
	private String description;

	@Column(name="responsibility")
	private String responsibility;	
	
	@Column(name="endTime")
	private String endTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getResponsibility() {
		return responsibility;
	}

	public void setResponsibility(String responsibility) {
		this.responsibility = responsibility;
	}
	
	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "ErpProjectExperience [id=" + id + ", startTime=" + startTime + ", employeeId=" + employeeId
				+ ", projectName=" + projectName + ", post=" + post + ", description=" + description + ", "
				+ "responsibility=" + responsibility + ", endTime=" + endTime + "]";
	}
	
}
