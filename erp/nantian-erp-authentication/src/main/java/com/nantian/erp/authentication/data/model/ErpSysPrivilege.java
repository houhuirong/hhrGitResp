package com.nantian.erp.authentication.data.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "Sys_Privilege")
public class ErpSysPrivilege implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PrivilegeID")
	private Integer PrivilegeID;
	
	@Column(name = "PrivilegeMaster")
	private Integer PrivilegeMaster;
	
	@Column(name = "PrivilegeValue")
	private Integer PrivilegeValue;
	
	@Column(name = "PrivilegeAccess")
	private Integer PrivilegeAccess;
	
	@Column(name = "privilegeAccessValue")
	private Integer privilegeAccessValue;
	
	@Column(name = "PrivilegeOperation")
	private Integer PrivilegeOperation;
	
	@Column(name = "PrivilegeOperationValue")
	private Integer PrivilegeOperationValue;

	public Integer getPrivilegeID() {
		return PrivilegeID;
	}

	public void setPrivilegeID(Integer privilegeID) {
		PrivilegeID = privilegeID;
	}

	public Integer getPrivilegeMaster() {
		return PrivilegeMaster;
	}

	public void setPrivilegeMaster(Integer privilegeMaster) {
		PrivilegeMaster = privilegeMaster;
	}

	public Integer getPrivilegeValue() {
		return PrivilegeValue;
	}

	public void setPrivilegeValue(Integer privilegeValue) {
		PrivilegeValue = privilegeValue;
	}

	public Integer getPrivilegeAccess() {
		return PrivilegeAccess;
	}

	public void setPrivilegeAccess(Integer privilegeAccess) {
		PrivilegeAccess = privilegeAccess;
	}

	public Integer getPrivilegeAccessValue() {
		return privilegeAccessValue;
	}

	public void setPrivilegeAccessValue(Integer privilegeAccessValue) {
		this.privilegeAccessValue = privilegeAccessValue;
	}

	public Integer getPrivilegeOperation() {
		return PrivilegeOperation;
	}

	public void setPrivilegeOperation(Integer privilegeOperation) {
		PrivilegeOperation = privilegeOperation;
	}

	public Integer getPrivilegeOperationValue() {
		return PrivilegeOperationValue;
	}

	public void setPrivilegeOperationValue(Integer privilegeOperationValue) {
		PrivilegeOperationValue = privilegeOperationValue;
	}

	@Override
	public String toString() {
		return "ErpSysPrivilege [PrivilegeID=" + PrivilegeID + ", PrivilegeMaster=" + PrivilegeMaster
				+ ", PrivilegeValue=" + PrivilegeValue + ", PrivilegeAccess=" + PrivilegeAccess
				+ ", privilegeAccessValue=" + privilegeAccessValue + ", PrivilegeOperation=" + PrivilegeOperation
				+ ", PrivilegeOperationValue=" + PrivilegeOperationValue + "]";
	}
	
	
	
}