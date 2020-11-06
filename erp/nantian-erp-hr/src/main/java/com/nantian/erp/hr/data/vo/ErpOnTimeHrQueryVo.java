package com.nantian.erp.hr.data.vo;

public class ErpOnTimeHrQueryVo {

	private Integer employeeId;
	private String content;
	private String processoer;
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
	public String getProcessoer() {
		return processoer;
	}
	public void setProcessoer(String processoer) {
		this.processoer = processoer;
	}
	@Override
	public String toString() {
		return "ErpOnTimeHrQueryVo [employeeId=" + employeeId + ", content=" + content + ", processoer=" + processoer
				+ "]";
	}
	
}
