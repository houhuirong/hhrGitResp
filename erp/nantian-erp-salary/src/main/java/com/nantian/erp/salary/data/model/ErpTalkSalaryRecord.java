package com.nantian.erp.salary.data.model;

import java.util.Date;

public class ErpTalkSalaryRecord {
	
	private Integer erpRecordId;
	
    private Integer erpResumeId;

    private String erpHandler;

    private Date updateTime;

    public ErpTalkSalaryRecord(Integer erpResumeId, String erpHandler, Date updateTime, Integer erpRecordId) {
        this.erpResumeId = erpResumeId;
        this.erpHandler = erpHandler;
        this.updateTime = updateTime;
        this.erpRecordId = erpRecordId;
    }

    public ErpTalkSalaryRecord() {
        super();
    }

    public Integer getErpResumeId() {
        return erpResumeId;
    }

    public void setErpResumeId(Integer erpResumeId) {
        this.erpResumeId = erpResumeId;
    }

    public String getErpHandler() {
        return erpHandler;
    }

    public void setErpHandler(String erpHandler) {
        this.erpHandler = erpHandler == null ? null : erpHandler.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

	public Integer getErpRecordId() {
		return erpRecordId;
	}

	public void setErpRecordId(Integer erpRecordId) {
		this.erpRecordId = erpRecordId;
	}
    
    
}