package com.nantian.erp.salary.data.dao;

import java.util.List;

import com.nantian.erp.salary.data.model.ErpPeriodRecord;

/** 
 * Description: 入职-上岗工资单操作记录mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年01月28日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpPeriodRecordMapper {
	
	//根据员工ID查询生成试用期上岗工资单的记录
	List<ErpPeriodRecord> selectPeriodRecord(Integer employeeId);
	
	//新增记录
	void insertPeriodRecord(ErpPeriodRecord erpPeriodRecord);

}