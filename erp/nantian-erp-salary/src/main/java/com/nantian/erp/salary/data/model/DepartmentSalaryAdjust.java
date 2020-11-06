package com.nantian.erp.salary.data.model;

import java.util.Date;

/**
 * 一级部门调薪表
 */
public class DepartmentSalaryAdjust {
    /**
     * id
     */
    private Integer id;
    /**
     * '一级部门id'
     */
    private Integer departmentId;

    /**
     * '年度'
     */
    private String year;
    /**
     * 审批状态（0：未提交、1：已提交、2：已确认）
     */
    private Integer status;
    /**
     * 调薪类型（1：调整薪资、2：社保/公积金基数）
     */
    private Integer type;

    /**
     * '调薪计划'
     */
    private String plan;

    /**
     * '生效日期'
     */
    private Date adjustDate;

    /**
     * 修改人id
     */
    private Integer modifyPersonId;
    /**
     * 提交人id
     */
    private Integer submitPersonId;
    /**
     * '确认人id'
     */
    private Integer confirmPersonId;
    /**
     * '导出人id'
     */
    private Integer exportPersonId;

    /**
     * '修改时间'
     */
    private Date modifiedTime;

    /**
     * '创建时间'
     */
    private Date createTime;

    /**
     * '提交时间'
     */
    private Date submitTime;
    /**
     * '确认时间'
     */
    private Date confirmTime;
    /**
     * '导出时间'
     */
    private Date exportTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public Date getAdjustDate() {
        return adjustDate;
    }

    public void setAdjustDate(Date adjustDate) {
        this.adjustDate = adjustDate;
    }

    public Integer getModifyPersonId() {
        return modifyPersonId;
    }

    public void setModifyPersonId(Integer modifyPersonId) {
        this.modifyPersonId = modifyPersonId;
    }

    public Integer getSubmitPersonId() {
        return submitPersonId;
    }

    public void setSubmitPersonId(Integer submitPersonId) {
        this.submitPersonId = submitPersonId;
    }

    public Integer getConfirmPersonId() {
        return confirmPersonId;
    }

    public void setConfirmPersonId(Integer confirmPersonId) {
        this.confirmPersonId = confirmPersonId;
    }

    public Integer getExportPersonId() {
        return exportPersonId;
    }

    public void setExportPersonId(Integer exportPersonId) {
        this.exportPersonId = exportPersonId;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(Date confirmTime) {
        this.confirmTime = confirmTime;
    }

    public Date getExportTime() {
        return exportTime;
    }

    public void setExportTime(Date exportTime) {
        this.exportTime = exportTime;
    }
}
