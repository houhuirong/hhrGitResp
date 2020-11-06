package com.nantian.erp.authentication.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.authentication.data.model.ErpRole;
import com.nantian.erp.authentication.data.vo.ErpSysUserVo;
import org.apache.ibatis.annotations.Param;

/** 
 * Description: 角色管理- mapper接口
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   caoxiubin         1.0        
 * </pre>
 */
public interface ErpRoleMapper {
	
	//查询所有角色
	public List<Map<String, Object>> findAllChildRole(Integer ownerId);
	
	//通过角色ID查询单条记录信息
	public ErpRole findByRoleId(Integer roleId);
	
	//查询所有角色
	public List<ErpSysUserVo> findAllUser();
	
	//给用户新增角色信息
	int insertUserRole(Map<String, Object> param);
	
	//给用户新增角色信息
	int insertRole(ErpRole erpRole);
	
	//给用户新增角色信息
	int updateRole(ErpRole erpRole);
	
	//删除角色信息
	int deleteRole(Integer roleId);
	
	//判断名字是否存在
	ErpRole checkName(Map<String, Object> param);
	
	//判断keyword是否存在
	ErpRole checkKeyword(Map<String, Object> param);
	
	//根据员工ID查询角色信息
	List<ErpRole> findRoleByUserId(Integer userId);

	//add 20181015 查询所有基础角色
	List<Map<String,Object>> findAllBaseRole();
	
	//add 20181026 根据角色ID查询角色信息
	public ErpRole findRoleInfoByRoleId(Integer roleId);
	
	//根据基础角色id查询所有子角色
	public List<ErpRole> findAllChildRoleByFatherId(Integer roleId);
	
	//根据employeeId查询员工所具有的角色信息
	public List<Map<String,Object>> findRoleListByEmpId(Integer employeeId);
	
	//根据基础角色查询授权列表
	public List<Map<String,Object>> findAuthListByRoleId(Integer roleId);
	
	//根据基础角色查询授权列表
	public List<Map<String,Object>> findRoleByAuth(Map<String, Object> param);
	
	//增加基础角色授权
	void insertRoleAuth(Map<String, Object> param);
	
	//删除角色授权
	void deleteRoleAuth(Map<String, Object> param);

	/**
	 * 获取角色ID及子角色ID列表
	 * @param roleIdList
	 * @return
	 */
    List<Integer> findAllChildRoleByFatherIds(@Param("roleIdList") List<Integer> roleIdList);

	List<Integer> findRoleIdListByEmpId(Integer userId);
}
