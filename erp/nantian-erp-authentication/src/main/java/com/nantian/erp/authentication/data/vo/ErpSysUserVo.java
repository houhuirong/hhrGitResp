package com.nantian.erp.authentication.data.vo;

import java.util.List;
import java.util.Map;

import com.nantian.erp.authentication.data.model.ErpRole;

public class ErpSysUserVo {
	
	//主键
    private Integer id;
    //用户名
    private String username;
    //密码
    private String password;
    //用户类型
    private Integer userType;
    //用户编号
    private Integer userId;
    //用户电话
    private String userPhone;
    
    //二级密码
    private String secondaryPassword;
    
    //该用户所包含的角色对象
    private List<ErpRole> roleList;
    
    //该用户拥有的角色信息
    private Integer[] roles;
    
    //员工对应员工信息
    private Map<String, Object> empInfo;

    public ErpSysUserVo(Integer id, String username, String password, Integer userType, Integer userId, String userPhone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.userId = userId;
        this.userPhone = userPhone;
    }

    public ErpSysUserVo() {
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
        this.username = username == null ? null : username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
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

	public Integer[] getRoles() {
		return roles;
	}

	public void setRoles(Integer[] roles) {
		this.roles = roles;
	}

	public List<ErpRole> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<ErpRole> roleList) {
		this.roleList = roleList;
	}

	public Map<String, Object> getEmpInfo() {
		return empInfo;
	}

	public void setEmpInfo(Map<String, Object> empInfo) {
		this.empInfo = empInfo;
	}

	public String getSecondaryPassword() {
		return secondaryPassword;
	}

	public void setSecondaryPassword(String secondaryPassword) {
		this.secondaryPassword = secondaryPassword;
	}

}