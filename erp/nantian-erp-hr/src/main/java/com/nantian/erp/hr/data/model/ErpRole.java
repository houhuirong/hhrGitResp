package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Table(name="role")
public class ErpRole {
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	   @Column(name = "roleId")
	private Integer roleId;
	private String name;
	private String keyword;
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
	@Override
	public String toString() {
		return "ErpRole [roleId=" + roleId + ", name=" + name + ", keyword=" + keyword + "]";
	}
	
}
