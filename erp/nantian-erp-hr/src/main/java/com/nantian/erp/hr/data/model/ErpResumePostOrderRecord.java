package com.nantian.erp.hr.data.model;

/** 
 * Description: 面试预约记录表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpResumePostOrderRecord {
	//面试预约记录编号
	private Integer id;
	//面试流程表编号
	private Integer interviewId;
	//预约备注（点击再联系按钮输入预约记录，面试流程不变动）
	private String remark;
	//当前处理人
	private String processor;
	//当前处理时间
	private String time;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getInterviewId() {
		return interviewId;
	}
	public void setInterviewId(Integer interviewId) {
		this.interviewId = interviewId;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
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
	
	@Override
	public String toString() {
		return "ErpResumePostOrderRecord [id=" + id + ", interviewId=" + interviewId + ", remark=" + remark
				+ ", processor=" + processor + ", time=" + time + "]";
	}
	
}
