package com.nantian.erp.hr.data.dao;

import java.util.List;

import com.nantian.erp.hr.data.model.ErpRole;

/**
 * 角色Mapper
 * @author ZhangYuWei
 */
public interface ErpRoleMapper {
	
	//通过角色ID查询单条记录信息
	public ErpRole findByRoleId(Integer roleId);
	
	//查询所有角色
	public List<ErpRole> findAllRole();

}
