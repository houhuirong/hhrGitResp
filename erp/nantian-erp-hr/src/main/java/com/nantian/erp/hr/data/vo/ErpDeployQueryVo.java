package com.nantian.erp.hr.data.vo;

public class ErpDeployQueryVo {

	private Integer employeeId;
	private String content;
	//@DateTimeFormat(pattern = "yyyy-MM-dd")
	private String time;
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	@Override
	public String toString() {
		return "ErpDeployQueryVo [employeeId=" + employeeId + ", content=" + content + ", time=" + time + "]";
	}
	
}
