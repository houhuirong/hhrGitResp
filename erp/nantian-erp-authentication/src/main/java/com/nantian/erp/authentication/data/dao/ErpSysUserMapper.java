package com.nantian.erp.authentication.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.authentication.data.vo.ErpSysUserVo;
import com.nantian.erp.common.base.Pojo.ErpUser;

public interface ErpSysUserMapper {
	
	//登录验证（根据用户名查询加密后的密码）
	public ErpUser login(ErpUser erpUser);
	
	//根据用户名查询id
	public List<Integer> findRoles(Integer userId);
	
	//查询所有用户
	ErpSysUserVo findUserByEmpId(Integer empId);
	
	//新增用户
	int insertUser(ErpSysUserVo erpSysUserVo);
	
	//修改用户
	int updateUser(ErpSysUserVo erpSysUserVo);
	
	//删除用户信息
	int deleteUser(Integer roleId);
	
	//根据用户名查询用户
	ErpSysUserVo findUserByUsername(String username);
	
	//add by hhr 20181107根据用户ID查询用户信息
	ErpSysUserVo findUserInfoByUserId(Integer userId);
	
	//查询所有用户
	List<Map<String,Object>> findUserByEmpIds(Integer[] empIds);

	List<Map<String,Object>> findEmpIdListByUserId(Integer[] curPersonIds);
	
	//add by ZhangYuWei 登录验证（根据用户名查询加密后的密码）
	ErpUser loginSecondaryPassword(ErpUser erpUser);
	
	List<Map<String,Object>> findAllTempUser();
	
	//add by ZhangYuWei 通过角色ID查询一个角色对应的所有用户信息
	List<Map<String,Object>> findAllUserByRoleId(Integer roleId);

	//add by hhr 通过手机号查询用户信息
	List<Map<String,Object>> findUserByMobile(@Param("param") List<String> param);

	public List<ErpUser> findMobileByUserList(@Param("param") List<Integer> param);

	List<Map<String, Object>> findAllUser();
}