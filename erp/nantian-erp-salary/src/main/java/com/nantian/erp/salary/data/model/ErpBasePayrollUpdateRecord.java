package com.nantian.erp.salary.data.model;

/** 
 * Description: 导入新出数据操作日志记录表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpBasePayrollUpdateRecord {
	
	//基本薪资表操作记录ID
	private Integer id;
	//被修改员工的姓名
	private String employee;
	//被修改员工Id
	private Integer employeeId;
	//处理人
	private String processor;
	//处理时间
	private String time;
	//处理内容
	private String content;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getEmployee() {
		return employee;
	}
	public void setEmployee(String employee) {
		this.employee = employee;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public String getProcessor() {
		return processor;
	}
	public void setProcessor(String processor) {
		this.processor = processor;
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
	
}
