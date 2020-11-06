package com.nantian.erp.salary.data.dao;


import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.nantian.erp.salary.data.model.ErpPeriodPayroll;

/**
 * ERP-试用期-工资单 mapper层
 * @author caoxb
 * @date 2018-09-13
 */
public interface ErpPeriodPayrollMapper {
	
	//根据员工ID查询工资单详细信息
	ErpPeriodPayroll findPeriodSalary(@Param(value = "employeeId") Integer employeeId);
	
	//新增试用期工资单
	int insert(ErpPeriodPayroll erpPeriodPayroll);
	
	//修改试用期工资单
	int updateById(ErpPeriodPayroll erpPeriodPayroll);
	
	//查询所有试用期工资单
	List<ErpPeriodPayroll> selectAllPeriod();
	
	//查询单条试用期上岗工资条
	ErpPeriodPayroll selectOnePeriodPayroll(@Param(value = "employeeId") Integer employeeId);
	
	
}