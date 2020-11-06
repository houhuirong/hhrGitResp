package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Table(name="employee_postive")
public class ErpEmployeePostive {
	   @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	   @Column(name = "id")
	private Integer id;
	   @Column(name = "currentPersonID")
	private Integer currentPersonID;	  
	@Column(name = "employeeId")
	private Integer employeeId;
	@Column(name="status")
	private Integer status;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getCurrentPersonID() {
			return currentPersonID;
		}
	public void setCurrentPersonID(Integer currentPersonID) {
			this.currentPersonID = currentPersonID;
		}

	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "ErpEmployeePostive [id=" + id + ", currentPersonID=" + currentPersonID + ", employeeId=" + employeeId +", status=" + status+ "]";
	}
	
	
}
