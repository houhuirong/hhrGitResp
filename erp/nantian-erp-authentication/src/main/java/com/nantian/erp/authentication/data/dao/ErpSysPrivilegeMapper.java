package com.nantian.erp.authentication.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.authentication.data.vo.ErpSysPrivilegeVo;

/** 
 * Description: 特权管理- mapper接口
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   caoxiubin         1.0        
 * </pre>
 */
public interface ErpSysPrivilegeMapper {
	
	//新增特权关系
	int insertPrivilege(ErpSysPrivilegeVo erpSysPrivilege);
	
	//删除角色对应菜单信息
	int deletePrivilegeByRoleId(Integer roleId);	

	//删除菜单对应角色关系
	int deletePrivilegeByMenuId(Integer privilegeAccess,Integer privilegeAccessValue);
		
	//删除url对应角色关系
	int deletePrivilegeByUrlID(Integer UrlID);
	
	//根据条件删除特权关系
	int deletePrivilege(ErpSysPrivilegeVo erpSysPrivilege);
	
	//删除用户对应的角色关系
	int deletePrivilegeByUserId(Integer userId);
	
	//根据UrlID查找其下角色信息
	public List<ErpSysPrivilegeVo> findPrivilegeByUrlID(Integer UrlID);
	
	//根据角色ID查询权限信息
	List<ErpSysPrivilegeVo> findPrivilegeByRoleId(Integer roleId);
	
	//根据角色ID查询权限信息
	List<ErpSysPrivilegeVo> findPrivilegeByParam(Map<String, Object> param);
		
	//通过传入的menu/button列表查询角色 
	List<ErpSysPrivilegeVo> findRolePrivilegeByMenusId(Integer privilegeAccess,Object menusId);
	
	// 根据查询URL与角色的关联关系
	ErpSysPrivilegeVo findPrivilegeByRoleUrl(Integer privilegeValue,Integer privilegeAccessValue);
	
	// 根据查询菜单按钮与角色的关联关系
	List<ErpSysPrivilegeVo> findPrivilegeByRoleMenuBtn(Integer privilegeValue,Integer privilegeAccessValue);
	//zjh
	void updatePrivilegeByRoleUrl(Integer privilegeValue,Integer privilegeAccessValue);
	
	void insertPrivilegeByRoleUrl(Integer privilegeValue,Integer privilegeAccessValue);
	
	void addRelativeNumByPrivilegeId(Integer privilegeId);
	
	void decRelativeNumByPrivilegeId(Integer privilegeId);
	
	void deletePrivilegeByPrivilegeId(Integer privilegeId);
	//...
}