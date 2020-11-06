package com.nantian.erp.hr.data.model;

import java.io.Serializable;
import java.util.Date;

/**
 * t_email_service_config
 * @author 
 */
public class TEmailServiceConfig implements Serializable {
    private Integer id;

    /**
     * 邮件服务类型 admin_dic中(EMAIL_SERVICE_TYPE)
     */
    private Integer type;

    /**
     * 收件人
     */
    private String recipient;

    /**
     * 抄送人
     */
    private String bcc;

    /**
     * 邮件是否发送
     */
    private Integer send;

    /**
     * 创建人
     */
    private Integer createdPersonId;

    /**
     * 修改人
     */
    private Integer modifiedPersonId;

    /**
     * 创建时间
     */
    private Date gmtTime;

    /**
     * 修改时间
     */
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public Integer getSend() {
        return send;
    }

    public void setSend(Integer send) {
        this.send = send;
    }

    public Integer getCreatedPersonId() {
        return createdPersonId;
    }

    public void setCreatedPersonId(Integer createdPersonId) {
        this.createdPersonId = createdPersonId;
    }

    public Integer getModifiedPersonId() {
        return modifiedPersonId;
    }

    public void setModifiedPersonId(Integer modifiedPersonId) {
        this.modifiedPersonId = modifiedPersonId;
    }

    public Date getGmtTime() {
        return gmtTime;
    }

    public void setGmtTime(Date gmtTime) {
        this.gmtTime = gmtTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}