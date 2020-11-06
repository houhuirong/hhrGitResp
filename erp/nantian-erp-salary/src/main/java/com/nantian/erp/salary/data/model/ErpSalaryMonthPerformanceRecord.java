package com.nantian.erp.salary.data.model;

/** 
 * Description: 月度绩效操作记录表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpSalaryMonthPerformanceRecord {
	//月度绩效记录表编号
	private Integer id;
	//部门Id
	private Integer firstDepartmentId;
	//月份
	private String month;
	//当前操作人姓名
	private String processor;
	//操作时间
	private String time;
	//操作记录内容
	private String content;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getFirstDepartmentId() {
		return firstDepartmentId;
	}
	public void setFirstDepartmentId(Integer firstDepartmentId) {
		this.firstDepartmentId = firstDepartmentId;
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
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	@Override
	public String toString() {
		return "ErpSalaryMonthPerformanceRecord [id=" + id + ", firstDepartmentId=" + firstDepartmentId + ", month="
				+ month + ", processor=" + processor + ", time=" + time + ", content=" + content + "]";
	}

	
}
