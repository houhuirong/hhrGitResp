package com.nantian.erp.salary.data.dao;

import java.util.List;

import com.nantian.erp.salary.data.model.ErpBasePayroll;

/** 
 * Description: 薪酬管理mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年10月22日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpBasePayrollMapper {
	
	//新增
	void insertBasePayroll(ErpBasePayroll erpBasePayroll);
	
	//通过员工ID修改薪酬信息
	void updateBasePayroll(ErpBasePayroll erpBasePayroll);
	
	//主键查询
	ErpBasePayroll findBasePayrollDetail(Integer erpPayrollId);
	
	//通过员工ID查询薪资信息
	ErpBasePayroll findBasePayrollDetailByEmpId(Integer erpEmployeeId);
	
	//查询全部
	List<ErpBasePayroll> findBasePayrollAll();
	
}