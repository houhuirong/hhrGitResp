package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.model.ErpBasePayrollRecord;

/** 
 * Description: 薪酬管理错误日志记录mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年10月24日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpBasePayrollRecordMapper {
	
	//新增一条错误日志记录
	void insertBasePayrollRecord(ErpBasePayrollRecord erpBasePayrollRecord);
	
	//查询全部错误日志记录
	public List<ErpBasePayrollRecord> findBasePayrollRecord(Map<String, Object> paramsMap);
	
	//查询全部错误日志记录的总条数
	public Long findTotalBasePayrollRecord(Map<String,Object> params);
	
}