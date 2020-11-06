package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.model.ErpEmpFinanceNumber;

/** 
 * Description: 员工财务序号管理mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年01月23日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpEmpFinanceNumberMapper {
	
	//新增员工的财务序号
	void insertEmpFinanceNumber(ErpEmpFinanceNumber erpEmpFinanceNumber);
	
	//通过员工ID修改员工的财务序号
	void updateEmpFinanceNumber(ErpEmpFinanceNumber erpEmpFinanceNumber);
	
	//通过员工ID查询财务序号信息
	ErpEmpFinanceNumber findEmpFinanceNumberDetailByEmpId(Integer employeeId);
	
	List<Map<String, Object>> findAllFinanceNumber();
	
	//通过指定参数查询财务序号信息
	List<ErpEmpFinanceNumber> findEmpFinanceNumberDetailByParams(Map<String,Object> params);
	
}