package com.nantian.erp.common.base.Pojo;

import java.io.Serializable;
import java.util.List;

public class ErpUser implements Serializable {
	
	private static final long serialVersionUID = -1672970955045193907L;

    private Integer id;//用户编号
	
	private String username;//用户名
	
	private String password;
	
	private Integer userType;
	
	private Integer userId;//员工编号
	
	private String userPhone;//手机号码
	
	private String oldPassword;
	
	private String employeeName;//员工姓名
	
	private String secondaryPassword;//二级密码
	
	//该用户拥有的角色ID 一对多  一个用户存在多个角色
	private List<Integer> roles;

	public ErpUser(Integer id, String username, String password, Integer userType, Integer userId, String userPhone,
			String oldPassword, String employeeName, List<Integer> roles) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.userType = userType;
		this.userId = userId;
		this.userPhone = userPhone;
		this.oldPassword = oldPassword;
		this.employeeName = employeeName;
		this.roles = roles;
	}

	public ErpUser() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public List<Integer> getRoles() {
		return roles;
	}

	public void setRoles(List<Integer> roles) {
		this.roles = roles;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getSecondaryPassword() {
		return secondaryPassword;
	}

	public void setSecondaryPassword(String secondaryPassword) {
		this.secondaryPassword = secondaryPassword;
	}

	@Override
	public String toString() {
		return "ErpUser [id=" + id + ", username=" + username + ", password=" + password + ", userType=" + userType
				+ ", userId=" + userId + ", userPhone=" + userPhone + ", oldPassword=" + oldPassword + ", employeeName="
				+ employeeName + ", secondaryPassword=" + secondaryPassword + ", roles=" + roles + "]";
	}
	
}
