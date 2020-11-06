package com.nantian.erp.hr.data.dao;

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
public interface ErpTodoListForHrMapper {
	
	//待我审批的岗位申请-待办事项数量
	Long countPostApplyTodo(Integer personId);
	
	//待我处理的面试-待办事项数量
	Long countInterviewerTodo(Map<String,Object> params);
	
	//待我处理的offer
	Long countOfferTodo(String status);
	
	//待我处理的入职
	Long countEntryTodo(Map<String,Object> params);
	
	//待我处理的转正
	Long countPositiveTodo(Map<String,Object> params);
	
	//待我审批的部门调动申请
	Long countDepTransfTodo(Integer processor);
	
	Long countDimissionTodo(Integer processor);
	
}
