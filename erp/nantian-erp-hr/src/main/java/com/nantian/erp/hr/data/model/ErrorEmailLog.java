package com.nantian.erp.hr.data.model;

import java.util.Date;

public class ErrorEmailLog {
    private Integer id;

    /**
     * 发件人
     */
    private String sender;

    /**
     * 收件人
     */
    private String recipient;

    /**
     * 抄送人
     */
    private String bcc;

    /**
     * 邮件内容
     */
    private String emailMessage;

    /**
     * 附件路径
     */
    private String attachmentPath;

    /**
     * 错误日志
     */
    private String errorLog;

    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifiedTime;

    /**
     * 是否发送成功
     */
    private Integer type;

    /**
     * 邮件主题
     */
    private String subject;
    /**
     * 邮件服务类型 admin_dic中(EMAIL_SERVICE_TYPE)
     */
    private Integer emailServiceType;

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender == null ? null : sender.trim();
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient == null ? null : recipient.trim();
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc == null ? null : bcc.trim();
    }

    public String getEmailMessage() {
        return emailMessage;
    }

    public void setEmailMessage(String emailMessage) {
        this.emailMessage = emailMessage == null ? null : emailMessage.trim();
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath == null ? null : attachmentPath.trim();
    }

    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog == null ? null : errorLog.trim();
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
    public Integer getEmailServiceType() {
		return emailServiceType;
	}

	public void setEmailServiceType(Integer emailServiceType) {
		this.emailServiceType = emailServiceType;
	}

}