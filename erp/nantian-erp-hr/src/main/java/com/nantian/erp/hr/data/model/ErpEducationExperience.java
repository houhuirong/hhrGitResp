package com.nantian.erp.hr.data.model;

public class ErpEducationExperience {
	
	private Integer id;
	
	private Integer employeeId;
	
	private String startTime;
	
	private String endTime;
	
	private String school;
	
	private String major;
	
	private String degree;
	
	private String filename;
	
	private String filename1;

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

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getFilename1() {
		return filename1;
	}

	public void setFilename1(String filename1) {
		this.filename1 = filename1;
	}

	@Override
	public String toString() {
		return "ErpEducationExperience [id=" + id + ", employeeId=" + employeeId + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", school=" + school + ", major=" + major + ", degree=" + degree
				+ ", filename=" + filename + ", filename1=" + filename1 + "]";
	}

}
