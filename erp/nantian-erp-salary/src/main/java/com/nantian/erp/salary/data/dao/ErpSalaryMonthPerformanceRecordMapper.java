package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.model.ErpSalaryMonthPerformanceRecord;

/**
 * 月度绩效操作记录表信息mapper
 * @author ZhangYuWei
 */
public interface ErpSalaryMonthPerformanceRecordMapper {
	
	//插入月度绩效操作记录
	public void insertRecord(ErpSalaryMonthPerformanceRecord salaryMonthPerformanceRecord);
	
	//根据一级部门ID查询全部的记录
	public List<ErpSalaryMonthPerformanceRecord> selectRecordById(Map<String,Object> param);
	
}
