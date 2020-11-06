package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Table(name="positive_record")
public class ErpPositiveRecord {
	   @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	   @Column(name = "id")
	private Integer id;
	   @Column(name = "context")
	private String context;
	   @Column(name = "employeeId")
	private Integer employeeId;
	   
	private String time;
	private String person;
	private Integer personID;	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}


	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getPerson() {
		return person;
	}
	public void setPerson(String person) {
		this.person = person;
	}
	public Integer getPersonID() {
		return personID;
	}
	public void setPersonID(Integer personID) {
		this.personID = personID;
	}
	@Override
	public String toString() {
		return "ErpPositiveRecord [id=" + id + ", context=" + context + ", employeeId=" + employeeId + ", time=" + time
				+ ", person=" + person +", personID=" +personID+"]";
	}
	
}
