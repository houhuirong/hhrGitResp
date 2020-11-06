package com.nantian.erp.salary.data.dao;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.salary.data.model.ErpTraineeSalary;

/** 
 * Description: 实习生薪资mapper
 *
 * @author HouHuiRong
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月13日      		 HouHuiRong         1.0       
 * </pre>
 */
public interface ErpTraineeSalaryMapper {

	//新增一条实习生薪资记录
	void insertTraineeSalary(ErpTraineeSalary erpTraineeSalary);
	//更新一条实习生薪资记录
	void updateTraineeSalary(ErpTraineeSalary erpTraineeSalary);
	//查询单条实习生上岗工资条
	ErpTraineeSalary selectOneTraineeSalary(@Param(value = "employeeId") Integer employeeId);
}
