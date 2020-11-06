package com.nantian.erp.salary.data.dao;

import com.nantian.erp.salary.data.model.ErpPositiveSalary;

/** 
 * Description: 转正薪资表mapper
 *
 * @author HouHuiRong
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月08日      		HouHuiRong          1.0       
 * </pre>
 */
public interface ErpPositiveSalaryMapper {

	//新增转正薪资数据
	void insertPositiveSalary(ErpPositiveSalary erpPosistive);
	//通过员工ID查询薪资信息
	ErpPositiveSalary findPositiveSalaryByEmpId(Integer erpEmployeeId);
	//更新转正薪资表
	void updatePositiveSalaryByEmpId(ErpPositiveSalary erpPositiveSalary);
}
