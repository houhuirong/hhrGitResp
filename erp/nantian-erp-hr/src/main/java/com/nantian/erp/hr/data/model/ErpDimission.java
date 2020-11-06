package com.nantian.erp.hr.data.model;

/** 
 * Description: 员工离职表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpDimission {
	//员工离职编号
	private Integer id;
	//员工编号
	private Integer employeeId;
	//离职时间
	private String dimissionTime;
	//离职原因
	private String dimissionReason;
	//离职去向
	private String dimissionDirection;
	//办理手续时间
	private String dealWithTime;
	
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
	public String getDimissionTime() {
		return dimissionTime;
	}
	public void setDimissionTime(String dimissionTime) {
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
	public String getDealWithTime() {
		return dealWithTime;
	}
	public void setDealWithTime(String dealWithTime) {
		this.dealWithTime = dealWithTime;
	}
	
	@Override
	public String toString() {
		return "ErpDimission [id=" + id + ", employeeId=" + employeeId + ", dimissionTime=" + dimissionTime
				+ ", dimissionReason=" + dimissionReason + ", dimissionDirection=" + dimissionDirection
				+ ", dealWithTime=" + dealWithTime + "]";
	}
	
	
}
