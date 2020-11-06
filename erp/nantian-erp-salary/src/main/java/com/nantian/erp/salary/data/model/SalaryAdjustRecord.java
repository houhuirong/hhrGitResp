package com.nantian.erp.salary.data.model;

import java.util.Date;

public class SalaryAdjustRecord {
    private Integer id;

    private Integer employeeId;

    private Integer approverid;

    private String adjustTime;

    private String formerBaseWage;

    private String formerPostWage;

    private String formerPerformance;

    private String formerAllowance;

    private String formerTelFarePerquisite;

    private String adjustBaseWage;

    private String adjustPostWage;

    private String adjustPerformance;

    private String adjustAllowance;

    private String adjustTelFarePerquisite;

    private String adjustReason;

    private Integer adjustBatch;

    private Integer adjustStatus;

    private Date approverTime;

    private String formerSocialSecurityBase;

    private String formerAccumulationFundBase;

    private String adjustSocialSecurityBase;

    private String adjustAccumulationFundBase;

    private Integer submitPersonId;

    private Boolean modified;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getApproverid() {
        return approverid;
    }

    public void setApproverid(Integer approverid) {
        this.approverid = approverid;
    }

    public String getAdjustTime() {
        return adjustTime;
    }

    public void setAdjustTime(String adjustTime) {
        this.adjustTime = adjustTime == null ? null : adjustTime.trim();
    }

    public String getFormerBaseWage() {
        return formerBaseWage;
    }

    public void setFormerBaseWage(String formerBaseWage) {
        this.formerBaseWage = formerBaseWage == null ? null : formerBaseWage.trim();
    }

    public String getFormerPostWage() {
        return formerPostWage;
    }

    public void setFormerPostWage(String formerPostWage) {
        this.formerPostWage = formerPostWage == null ? null : formerPostWage.trim();
    }

    public String getFormerPerformance() {
        return formerPerformance;
    }

    public void setFormerPerformance(String formerPerformance) {
        this.formerPerformance = formerPerformance == null ? null : formerPerformance.trim();
    }

    public String getFormerAllowance() {
        return formerAllowance;
    }

    public void setFormerAllowance(String formerAllowance) {
        this.formerAllowance = formerAllowance == null ? null : formerAllowance.trim();
    }

    public String getFormerTelFarePerquisite() {
        return formerTelFarePerquisite;
    }

    public void setFormerTelFarePerquisite(String formerTelFarePerquisite) {
        this.formerTelFarePerquisite = formerTelFarePerquisite == null ? null : formerTelFarePerquisite.trim();
    }

    public String getAdjustBaseWage() {
        return adjustBaseWage;
    }

    public void setAdjustBaseWage(String adjustBaseWage) {
        this.adjustBaseWage = adjustBaseWage == null ? null : adjustBaseWage.trim();
    }

    public String getAdjustPostWage() {
        return adjustPostWage;
    }

    public void setAdjustPostWage(String adjustPostWage) {
        this.adjustPostWage = adjustPostWage == null ? null : adjustPostWage.trim();
    }

    public String getAdjustPerformance() {
        return adjustPerformance;
    }

    public void setAdjustPerformance(String adjustPerformance) {
        this.adjustPerformance = adjustPerformance == null ? null : adjustPerformance.trim();
    }

    public String getAdjustAllowance() {
        return adjustAllowance;
    }

    public void setAdjustAllowance(String adjustAllowance) {
        this.adjustAllowance = adjustAllowance == null ? null : adjustAllowance.trim();
    }

    public String getAdjustTelFarePerquisite() {
        return adjustTelFarePerquisite;
    }

    public void setAdjustTelFarePerquisite(String adjustTelFarePerquisite) {
        this.adjustTelFarePerquisite = adjustTelFarePerquisite == null ? null : adjustTelFarePerquisite.trim();
    }

    public String getAdjustReason() {
        return adjustReason;
    }

    public void setAdjustReason(String adjustReason) {
        this.adjustReason = adjustReason == null ? null : adjustReason.trim();
    }

    public Integer getAdjustBatch() {
        return adjustBatch;
    }

    public void setAdjustBatch(Integer adjustBatch) {
        this.adjustBatch = adjustBatch;
    }

    public Integer getAdjustStatus() {
        return adjustStatus;
    }

    public void setAdjustStatus(Integer adjustStatus) {
        this.adjustStatus = adjustStatus;
    }

    public Date getApproverTime() {
        return approverTime;
    }

    public void setApproverTime(Date approverTime) {
        this.approverTime = approverTime;
    }

    public String getFormerSocialSecurityBase() {
        return formerSocialSecurityBase;
    }

    public void setFormerSocialSecurityBase(String formerSocialSecurityBase) {
        this.formerSocialSecurityBase = formerSocialSecurityBase == null ? null : formerSocialSecurityBase.trim();
    }

    public String getFormerAccumulationFundBase() {
        return formerAccumulationFundBase;
    }

    public void setFormerAccumulationFundBase(String formerAccumulationFundBase) {
        this.formerAccumulationFundBase = formerAccumulationFundBase == null ? null : formerAccumulationFundBase.trim();
    }

    public String getAdjustSocialSecurityBase() {
        return adjustSocialSecurityBase;
    }

    public void setAdjustSocialSecurityBase(String adjustSocialSecurityBase) {
        this.adjustSocialSecurityBase = adjustSocialSecurityBase == null ? null : adjustSocialSecurityBase.trim();
    }

    public String getAdjustAccumulationFundBase() {
        return adjustAccumulationFundBase;
    }

    public void setAdjustAccumulationFundBase(String adjustAccumulationFundBase) {
        this.adjustAccumulationFundBase = adjustAccumulationFundBase == null ? null : adjustAccumulationFundBase.trim();
    }

    public Integer getSubmitPersonId() {
        return submitPersonId;
    }

    public void setSubmitPersonId(Integer submitPersonId) {
        this.submitPersonId = submitPersonId;
    }

    public Boolean getModified() {
        return modified;
    }

    public void setModified(Boolean modified) {
        this.modified = modified;
    }
}