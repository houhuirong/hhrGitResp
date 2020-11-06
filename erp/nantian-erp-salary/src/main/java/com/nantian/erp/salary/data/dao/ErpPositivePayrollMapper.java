package com.nantian.erp.salary.data.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.nantian.erp.salary.data.model.ErpPositivePayroll;

/**
 * ERP-转正-工资单 mapper层
 * @author caoxb
 * @date 2018-09-13
 */
public interface ErpPositivePayrollMapper {
	
	//查询所有转正工资单
	List<ErpPositivePayroll> selectAllPositive();
	
	//新增转正工资单
	int insert(ErpPositivePayroll erpPositivePayroll);
	
	//修改转正工资单
	int updateById(ErpPositivePayroll erpPositivePayroll);
	
	//查询单条转正上岗工资条
	ErpPositivePayroll selectOnePositivePayroll(@Param(value = "employeeId") Integer employeeId);
	
}