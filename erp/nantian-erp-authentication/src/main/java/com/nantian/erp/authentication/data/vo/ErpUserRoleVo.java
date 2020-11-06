package com.nantian.erp.authentication.data.vo;

/** 
 * Description: 用户角色信息封装类
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月9日                   caoxiubin         1.0        
 * </pre>
 */
public class ErpUserRoleVo {
    
	//用户ID
	private Integer userId;
	
	//角色-一个用户可以对应多个角色
	private Integer roleId;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}
	
	
	
	
	
}
