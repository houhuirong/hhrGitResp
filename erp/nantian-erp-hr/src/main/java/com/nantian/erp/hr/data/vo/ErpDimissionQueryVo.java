package com.nantian.erp.hr.data.vo;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class ErpDimissionQueryVo {
	

	private String name;
	private String sex;
	private String firstDepartment;
	private String secondDepartment;
	private String position;
	private String rank;
	private String projectGroup;
	private String projectManager;
	  @DateTimeFormat(pattern = "yyyy-MM-dd") 
	private Date dimissionTime;
	private String dimissionReason;
	private String dimissionDirection;
	private Integer employeeId;
	  @DateTimeFormat(pattern = "yyyy-MM-dd") 
	  private Date takeJobTime;
	  @DateTimeFormat(pattern = "yyyy-MM-dd") 
	  private Date dealWithTime;

	  

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getFirstDepartment() {
		return firstDepartment;
	}
	public void setFirstDepartment(String firstDepartment) {
		this.firstDepartment = firstDepartment;
	}
	public String getSecondDepartment() {
		return secondDepartment;
	}
	public void setSecondDepartment(String secondDepartment) {
		this.secondDepartment = secondDepartment;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getRank() {
		return rank;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}
	public String getProjectGroup() {
		return projectGroup;
	}
	public void setProjectGroup(String projectGroup) {
		this.projectGroup = projectGroup;
	}
	public String getProjectManager() {
		return projectManager;
	}
	public void setProjectManager(String projectManager) {
		this.projectManager = projectManager;
	}
	public Date getDimissionTime() {
		return dimissionTime;
	}
	public void setDimissionTime(Date dimissionTime) {
		this.dimissionTime = dimissionTime;
	}
	public String getDimissionReason() {
		return dimissionReason;
	}
	public void setDimissionReason(String dimissionReason) {
		this.dimissionReason = dimissionReason;
	}
	public String getDimissionDirection() {
		return dimissionDirection;
	}
	public void setDimissionDirection(String dimissionDirection) {
		this.dimissionDirection = dimissionDirection;
	}
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public Date getTakeJobTime() {
		return takeJobTime;
	}
	public void setTakeJobTime(Date takeJobTime) {
		this.takeJobTime = takeJobTime;
	}
	public Date getDealWithTime() {
		return dealWithTime;
	}
	public void setDealWithTime(Date dealWithTime) {
		this.dealWithTime = dealWithTime;
	}
	@Override
	public String toString() {
		return "ErpDimissionQueryVo [name=" + name + ", sex=" + sex + ", firstDepartment=" + firstDepartment
				+ ", secondDepartment=" + secondDepartment + ", position=" + position + ", rank=" + rank
				+ ", projectGroup=" + projectGroup + ", projectManager=" + projectManager + ", dimissionTime="
				+ dimissionTime + ", dimissionReason=" + dimissionReason + ", dimissionDirection=" + dimissionDirection
				+ ", employeeId=" + employeeId + ", takeJobTime=" + takeJobTime + ", dealWithTime=" + dealWithTime
				+ "]";
	}
	
	
}
