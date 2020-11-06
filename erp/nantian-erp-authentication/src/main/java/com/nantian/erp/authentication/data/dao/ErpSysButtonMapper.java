package com.nantian.erp.authentication.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.authentication.data.model.ErpSysButton;

public interface ErpSysButtonMapper {
	
	
	//根据菜单ID和角色id查询其下的button列表
	List<ErpSysButton> getButtonByMenuNoRole(Map<String, Object> param);
	//根据菜单ID查询其下的button列表
	List<ErpSysButton> getAllButtonByMenuNo(String menuNo);
   
	//根据标识添加按钮
	void addButton(ErpSysButton erpSysButton);

	//删除按钮
	void delButton(Integer BtnNo);

	//通过菜单id删除按钮
	void delButtonByMenuNo(String menuNo);
	
	//修改按钮
	void updateButton(ErpSysButton erpSysButton);
	
	//菜单编号去重
	ErpSysButton checkButtonNo(String buttonNo);
	
	//判断该按钮关联子菜单下的按钮个数
	int countNum(String menuNo);
	
	//查询按钮权限信息
	List<Map<String,Object>> findButtonPrivilege(Map<String,Object> paramsMap);
	
	//根据编号找id
	Map<String,Object> findButtonIdByNo(String buttonNo);
	
	//根据id找button信息
	ErpSysButton findButtonInfoById(Integer BtnID);
}