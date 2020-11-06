package com.nantian.erp.hr.data.dao;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * mapper 层
 * 功能：用于其他服务关于员工-部门信息的接口调用
 * @author caoxb
 * @date 2018年09月08日
 */
public interface ErpEmpDepartmentInfoMapper {
	
	//根据部门一级部门ID查询一级部门名字
	Map<String, Object> getFirstDepartment(@Param(value = "departmentId") Integer departmentId);
	
	//根据员工名字查询其名字及所属二级部门
	Map<String, Object> getErpEmpAndSecondDepart(@Param(value = "erpEmployeeId") Integer erpEmployeeId);
	
	//根据简历ID查询岗位相关信息
	Map<String, Object> findPostInfo(@Param(value = "offerId") Integer offerId);
	
	//试用期-员工及其部门信息
	List<Map<String, Object>> selectAllHasHiredByUserName(@Param(value = "userName") String userName);
	
	//转正-员工及其部门信息
	List<Map<String, Object>> selectAllPositiveByUserName(@Param(value = "userName") String userName);
	
	//根据员工ID查询员工及其部门-岗位信息
	Map<String, Object> selectEmpInfo(@Param(value = "employeeId") Integer employeeId);
	
	
}
