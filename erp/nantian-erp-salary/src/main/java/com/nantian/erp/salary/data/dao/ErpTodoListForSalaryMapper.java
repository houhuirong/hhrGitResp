package com.nantian.erp.salary.data.dao;

import java.util.Map;

/** 
 * Description: 待办事项mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年12月28日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpTodoListForSalaryMapper {
	
	//待我处理的工资单（上岗、转正）
	Long countPayrollTodo(Map<String,Object> params);
	
}
