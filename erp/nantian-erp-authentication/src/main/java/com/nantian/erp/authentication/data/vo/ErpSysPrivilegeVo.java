package com.nantian.erp.authentication.data.vo;

public class ErpSysPrivilegeVo {
	
	//主键
    private Integer PrivilegeID;
    //特权主类型(1：Role 2：User)
    private Integer PrivilegeMaster;
    //特权主编号(userID、roleID)
    private Integer PrivilegeValue;
    //特权访问类型(1:Url 或2:Menu或3:Button)
    private Integer PrivilegeAccess;
    //特权访问编号(urlID、MenuID、ButtonID)
    private Integer PrivilegeAccessValue;
    //特权操作类型（1：是否显示 2：是否可以访问 ）
    private Integer PrivilegeOperation;
    //特权操作值(0:不显示/不可以访问/不可以点击,1:显示/可以点击/可以访问)
    private Integer PrivilegeOperationValue;
    //关系引用计数
    private Integer relativeNum;
	
    
    
	public ErpSysPrivilegeVo(Integer privilegeID, Integer privilegeMaster, Integer privilegeValue,
			Integer privilegeAccess, Integer privilegeAccessValue, Integer privilegeOperation,
			Integer privilegeOperationValue) {
		super();
		PrivilegeID = privilegeID;
		PrivilegeMaster = privilegeMaster;
		PrivilegeValue = privilegeValue;
		PrivilegeAccess = privilegeAccess;
		PrivilegeAccessValue = privilegeAccessValue;
		PrivilegeOperation = privilegeOperation;
		PrivilegeOperationValue = privilegeOperationValue;
	}
	public ErpSysPrivilegeVo() {
		super();
	}
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
		return PrivilegeAccessValue;
	}
	public void setPrivilegeAccessValue(Integer privilegeAccessValue) {
		PrivilegeAccessValue = privilegeAccessValue;
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
	public Integer getRelativeNum() {
		return relativeNum;
	}
	public void setRelativeNum(Integer relativeNum) {
		this.relativeNum = relativeNum;
	}
    
   
}