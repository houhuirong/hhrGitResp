package com.nantian.erp.authentication.data.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.authentication.data.model.ErpRole;
import com.nantian.erp.common.base.Pojo.ErpUser;

/**
 * 用户Mapper
 * @author ZhangYuWei
 */
public interface ErpUserMapper {
	
	public List<ErpRole> findRoleByUserName(String username);
	
	//根据用户名查询表记录数
	public Integer countByUserName(ErpUser erpUser);
	
	//插入一条用户信息
	public void insertErpUser(ErpUser erpUser);
	
	//删除用户
	public void deleteErpUser(String username);	

	//根据用户名查询id
	public Integer findIdByName(String username);
	
	//根据用户名查询id
	public String findRoleId(String username);
	
	//hr工程入职用，插入一条用户信息
	public Integer insertErpUserForHr(Map<String,Object> map);
	
	//给hr工程调用，查询所有的用户信息
	public List<Map<String,Object>> findAllErpUser();
	
	//给hr工程调用，修改用户信息
	public int updateErpUserforForHr(Map<String, Object> map);
	
	//给hr工程调用，通过UserId查找用户信息
	public Map<String, Object> findErpUserByUserId(Map<String, Object> map);
	
	//给hr工程调用，验证手机唯一性
	public Integer volidateErpUserPhone(Map<String, Object> map);
	
	//提供给Hr调用通过Id查找用户信息
	public ErpUser getErpUserForHr(Map<String, Object> map);

	public List<Map<String, Object>> getErpUserForHrList(Integer[] curPersonIds);
	
	//给hr工程调用，通过多个UserId查找用户信息
	public List<Map<String, Object>> findErpUserByUserIdArray(@Param("userId") String userId);
}
