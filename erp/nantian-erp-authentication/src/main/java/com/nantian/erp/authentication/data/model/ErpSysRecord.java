package com.nantian.erp.authentication.data.model;

/** 
 * Description: 面试记录表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpSysRecord {
	//系统记录编号
	private Integer id;
	//处理时间
	private String time;
	//处理内容
	private String opRecord;
	//当前处理人姓名
	private String processor;
	//处理类型
	private Integer opType;
	//处理Id
	private Integer opId;
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
	public String getOpRecord() {
		return opRecord;
	}
	public void setOpRecord(String opRecord) {
		this.opRecord = opRecord;
	}
	public String getProcessor() {
		return processor;
	}
	public void setProcessor(String processor) {
		this.processor = processor;
	}
	public Integer getOpType() {
		return opType;
	}
	public void setOpType(Integer opType) {
		this.opType = opType;
	}
	public Integer getOpId() {
		return opId;
	}
	public void setOpId(Integer opId) {
		this.opId = opId;
	}
	@Override
	public String toString() {
		return "ErpSysRecord [id=" + id + ", time=" + time + ", opRecord=" + opRecord + ", processor=" + processor
				+ ", opType=" + opType + ", opId=" + opId + "]";
	}

}
