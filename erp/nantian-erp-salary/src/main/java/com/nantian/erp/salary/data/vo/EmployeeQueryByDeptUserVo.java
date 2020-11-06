package com.nantian.erp.salary.data.vo;

/**
 * 根据部门ID、用户名、superLeader、leader等参数查询到的员工返回的VO
 * 
 * @author Songxiugong
 *
 */
public class EmployeeQueryByDeptUserVo {
	private String name;
	private Integer employeeId;
	private String sex;
	private String entryTime;
	private String firstDeptName;
	private String secondDeptName;
	private Integer rank;
	private String positionName;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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

	/**
	 * @return the sex
	 */
	public String getSex() {
		return sex;
	}

	/**
	 * @param sex the sex to set
	 */
	public void setSex(String sex) {
		this.sex = sex;
	}

	/**
	 * @return the entryTime
	 */
	public String getEntryTime() {
		return entryTime;
	}

	/**
	 * @param entryTime the entryTime to set
	 */
	public void setEntryTime(String entryTime) {
		this.entryTime = entryTime;
	}

	/**
	 * @return the firstDeptName
	 */
	public String getFirstDeptName() {
		return firstDeptName;
	}

	/**
	 * @param firstDeptName the firstDeptName to set
	 */
	public void setFirstDeptName(String firstDeptName) {
		this.firstDeptName = firstDeptName;
	}

	/**
	 * @return the secondDeptName
	 */
	public String getSecondDeptName() {
		return secondDeptName;
	}

	/**
	 * @param secondDeptName the secondDeptName to set
	 */
	public void setSecondDeptName(String secondDeptName) {
		this.secondDeptName = secondDeptName;
	}

	/**
	 * @return the rank
	 */
	public Integer getRank() {
		return rank;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank(Integer rank) {
		this.rank = rank;
	}

	/**
	 * @return the positionName
	 */
	public String getPositionName() {
		return positionName;
	}

	/**
	 * @param positionName the positionName to set
	 */
	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	@Override
	public String toString() {
		return "EmployeeQueryByDeptUserVo [name=" + name + ", employeeId=" + employeeId + ", sex=" + sex
				+ ", entryTime=" + entryTime + ", firstDeptName=" + firstDeptName + ", secondDeptName=" + secondDeptName
				+ ", rank=" + rank + ", positionName=" + positionName + "]";
	}

}
