package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.model.ErpBasePayrollUpdateRecord;

/** 
 * Description: 薪酬管理操作日志记录mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年08月14日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpBasePayrollUpdateRecordMapper {
	
	//新增一条操作日志记录
	void insertBasePayrollUpdateRecord(ErpBasePayrollUpdateRecord erpBasePayrollRecord);
	
	//查询全部操作日志记录
	public List<ErpBasePayrollUpdateRecord> findBasePayrollUpdateRecord(Map<String, Object> paramsMap);
	
	//查询全部操作日志记录的总条数
	public Long findTotalBasePayrollUpdateRecord(Map<String,Object> params);
	
}