package com.nantian.erp.authentication.data.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ErpRole implements Serializable{
	
	//角色ID
	private Integer roleId;
	//角色名字
	private String name;
	//角色关键字
	private String keyword;
	//角色类别
	private Integer roleType;
	//是否可以分配子角色
	private Integer childRoleRight;
	//父角色id
	private Integer fatherRoleId;
	//子角色所有人
	private Integer childRoleOwner;
	
	private String chlidRoleOwnerName;
	
	private String fatherRoleName;
	
	public String getChlidRoleOwnerName() {
		return chlidRoleOwnerName;
	}
	public void setChlidRoleOwnerName(String chlidRoleOwnerName) {
		this.chlidRoleOwnerName = chlidRoleOwnerName;
	}
	public String getFatherRoleName() {
		return fatherRoleName;
	}
	public void setFatherRoleName(String fatherRoleName) {
		this.fatherRoleName = fatherRoleName;
	}
	public Integer getFatherRoleId() {
		return fatherRoleId;
	}
	public void setFatherRoleId(Integer fatherRoleId) {
		this.fatherRoleId = fatherRoleId;
	}
	public Integer getChildRoleOwner() {
		return childRoleOwner;
	}
	public void setChildRoleOwner(Integer childRoleOwner) {
		this.childRoleOwner = childRoleOwner;
	}
	//该角色下的菜单列表
	private String[] menus;
	
	public ErpRole(Integer roleId, String name, String keyword, Integer childRoleRight) {
		super();
		this.roleId = roleId;
		this.name = name;
		this.keyword = keyword;
		this.childRoleRight = childRoleRight;
	}
	public ErpRole() {
		super();
	}
	public Integer getRoleId() {
		return roleId;
	}
	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String[] getMenus() {
		return menus;
	}
	public void setMenus(String[] menus) {
		this.menus = menus;
	}
	public Integer getRoleType() {
		return roleType;
	}
	public void setRoleType(Integer roleType) {
		this.roleType = roleType;
	}
	public Integer getChildRoleRight() {
		return childRoleRight;
	}
	public void setChildRoleRight(Integer childRoleRight) {
		this.childRoleRight = childRoleRight;
	}
	
}
