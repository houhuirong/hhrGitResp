package com.nantian.erp.hr.data.model;

/** 
 * Description: 面试记录表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpRecord {
	//面试记录编号
	private Integer id;
	//处理时间
	private String time;
	//面试记录内容
	private String content;
	//当前处理人姓名
	private String processor;
	//简历Id
	private Integer resumeId;
	//当前处理人ID
	private Integer processorId;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	public String getProcessor() {
		return processor;
	}
	public void setProcessor(String processor) {
		this.processor = processor;
	}
	public Integer getResumeId() {
		return resumeId;
	}
	public void setResumeId(Integer resumeId) {
		this.resumeId = resumeId;
	}

	public Integer getProcessorId() {
		return processorId;
	}

	public void setProcessorId(Integer processorId) {
		this.processorId = processorId;
	}

	@Override
	public String toString() {
		return "ErpRecord{" +
				"id=" + id +
				", time='" + time + '\'' +
				", content='" + content + '\'' +
				", processor='" + processor + '\'' +
				", resumeId=" + resumeId +
				", processorId=" + processorId +
				'}';
	}
}
