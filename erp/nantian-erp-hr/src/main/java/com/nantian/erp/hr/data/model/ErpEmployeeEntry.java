package com.nantian.erp.hr.data.model;

/** 
 * Description: 入职流程表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpEmployeeEntry {
	
	private Integer id;
	private Integer currentPersonID;//当前处理人的用户编号
	private Integer roleID;//角色编号
	private Integer status;//审批流程状态（1：待入职 2：已入职 3：项目已分配）
	private Integer offerId;//offer编号
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getCurrentPersonID() {
		return currentPersonID;
	}
	public void setCurrentPersonID(Integer currentPersonID) {
		this.currentPersonID = currentPersonID;
	}
	public Integer getRoleID() {
		return roleID;
	}
	public void setRoleID(Integer roleID) {
		this.roleID = roleID;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getOfferId() {
		return offerId;
	}
	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
	}
	
	@Override
	public String toString() {
		return "ErpEmployeeEntry [id=" + id + ", currentPersonID=" + currentPersonID + ", roleID=" + roleID
				+ ", status=" + status + ", offerId=" + offerId + "]";
	}
	   
}
