package com.nantian.erp.authentication.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.authentication.data.model.ErpUserRole;
import com.nantian.erp.authentication.data.vo.ErpUserRoleVo;

/**
 * 用户角色关联Mapper
 * @author ZhangYuWei
 */
public interface ErpUserRoleMapper {
	
	//add 20180917  根据用户ID查询用户和角色关联表信息
	public List<ErpUserRole> selectRelByUserId(ErpUserRole userRole);
	
	//add 20180917  新增一条用户和角色的关联关系
	public void insertUserRole(ErpUserRole userRole);
	
	//add 20180918  通过用户ID删除所有关联的表记录数据
	public void deleteUserRoleByUserId(Integer userId);
	
	//add cxb  201801015  新增一条用户和角色的关联关系
	public void insert(ErpUserRoleVo erpUserRoleVo);
	
	//add cxb  201801015  根据员工ID查询关联的角色信息
	public List<ErpUserRole> findUserRoleByUserId(Integer userId);
	
	//add by lx
	public void insertUserRoleForHr(Map<String, Object> userRole);

	public void insertUserRoleForHrList(List<Map<String, Object>> userRoleList);
	
	public void deleteUserRoleByRoleId(Integer roleId);
	
	public void deleteUserRoleByUserRole(Integer userId, Integer roleId);
	
}
