package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Table(name="employee_record")
public class ErpEmployeeRecord {
	   @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	   @Column(name = "id")
	private Integer id;
	   @Column(name = "employeeId")
	private Integer employeeId;
	private String time;
	private String content;
	private String processoer;

	public String getProcessoer() {
		return processoer;
	}
	public void setProcessoer(String processoer) {
		this.processoer = processoer;
	}
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
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return "ErpEmployeeRecord [id=" + id + ", employeeId=" + employeeId + ", time=" + time + ", content=" + content
				+ ", processoer=" + processoer + "]";
	}

	
}
