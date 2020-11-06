package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 工作调动申请审批记录
 * @author gaolp
 * 2019年3月20日
 * @version 1.0  
 */
public class DepartmentTransfRecod {

	private Integer id;
	private Integer transferApplyID;
	private Integer processor;
	private String content;
	private String rcTime;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getProcessor() {
		return processor;
	}
	public void setProcessor(Integer processor) {
		this.processor = processor;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getRcTime() {
		return rcTime;
	}
	public void setRcTime(String rcTime) {
		this.rcTime = rcTime;
	}
	public Integer getTransferApplyID() {
		return transferApplyID;
	}
	public void setTransferApplyID(Integer transferApplyID) {
		this.transferApplyID = transferApplyID;
	}
	@Override
	public String toString() {
		return "GroupTransfRecod [id=" + id + ", transferApplyID=" + transferApplyID + ", processor=" + processor
				+ ", content=" + content + ", rcTime=" + rcTime + "]";
	}
	
}
