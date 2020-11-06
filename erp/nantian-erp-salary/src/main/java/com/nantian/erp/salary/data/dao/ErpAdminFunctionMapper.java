package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.model.ErpAdminDic;

/** 
 * Description: 字典表mapper
 *
 * @author songxiugong
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2019年11月11日      		songxiugong          1.0       
 * </pre>
 */
public interface ErpAdminFunctionMapper {
	
	//查询payrollflow union basesalary
	public List<Map<String,Object>> selectPayRollFlowUnionBaseSalary();	
	
	//查询payrollflow union periodParoll
	public List<Map<String,Object>> selectPayRollFlowUnionperiodParoll();	
	
	//查询payrollflow union positivesalary
	public List<Map<String,Object>> selectPayRollFlowUnionPositivesalary();	
	
	//查询payrollflow union positivepeyroll
	public List<Map<String,Object>> selectPayRollFlowUnionPositivepeyroll();	
}
