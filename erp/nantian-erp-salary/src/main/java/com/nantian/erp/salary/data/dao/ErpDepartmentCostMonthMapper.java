package com.nantian.erp.salary.data.dao;

import java.util.List;
import java.util.Map;

import com.nantian.erp.salary.data.model.ErpDepartmentCostMonth;

/** 
 * Description: 部门人员薪酬分析mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月06日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpDepartmentCostMonthMapper {
	
	//新增
	void insertDepartmentCostMonth(ErpDepartmentCostMonth departmentCostMonth);
	
	//修改
	void updateDepartmentCostMonth(ErpDepartmentCostMonth departmentCostMonth);
	
	//查询一条部门费用统计记录（条件：一级部门Id、月度）
	ErpDepartmentCostMonth findDepartmentCostMonthDetail(Map<String,Object> params);
	
	//查询多条部门费用统计记录（条件：一级部门Id、起止月度）
	List<ErpDepartmentCostMonth> findDepartmentCostMonthMore(Map<String,Object> params);
	
}