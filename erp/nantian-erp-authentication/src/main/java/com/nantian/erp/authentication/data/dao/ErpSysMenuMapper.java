package com.nantian.erp.authentication.data.dao;

import java.util.List;
import java.util.Map;
import com.nantian.erp.authentication.data.model.ErpSysMenu;

/**
 * Description: 菜单管理-service 接口
 * 
 * @author caoxiubin
 * @version 1.0
 * 
 *          <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月8日                   caoxiubin         1.0
 *          </pre>
 */
public interface ErpSysMenuMapper {

	// 根据角色ID查询其所带菜单信息
	List<ErpSysMenu> findAllMenuByRoleId(Integer roleId);

	// 查询角色列表信息
	List<Map<String, Object>> findAllRole();

	// 查询菜单与按钮列表
	List<ErpSysMenu> findAllMenuAndBtn();

	// 根据角色ID查询其所带菜单信息
	List<ErpSysMenu> findAllMenu();

	// 根据Id查询要删除的菜单
	ErpSysMenu findMenuById(Integer menuId);

	// 根据Id删除菜单
	void delMenu(Integer menuId);

	// 添加菜单资源
	void addMenu(ErpSysMenu erpSysMenu);

	// 修改菜单资源
	void updateMenu(ErpSysMenu erpSysMenu);

	void updateIsLeafByMenuId(ErpSysMenu erpSysMenuTemp);
	
	void updateIsButtonByMenuId(ErpSysMenu erpSysMenuTemp);
	
	//菜单编号去重
	ErpSysMenu checkMenuNo(String menuNo);
	
	//查询该菜单的上级菜单的子菜单个数
	int countNum(String MenuparentNo);	

	// 根据菜单ID查询下一级菜单
	List<ErpSysMenu> findLeafMenu(String MenuNo);
	//-----
	// 根据菜单编号查询菜单表信息 
	Map<String,Object> findMenuIdByNo(String menuNo);
	
}