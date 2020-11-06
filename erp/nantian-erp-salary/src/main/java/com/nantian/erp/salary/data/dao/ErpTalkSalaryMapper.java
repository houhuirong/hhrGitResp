package com.nantian.erp.salary.data.dao;

import com.nantian.erp.salary.data.model.ErpTalkSalary;

/** 
 * Description: 面试谈薪mapper
 *
 * @author ZhangYuWei
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                  Author           Version     
 * ------------------------------------------------
 * 2018年11月14日      		ZhangYuWei          1.0       
 * </pre>
 */
public interface ErpTalkSalaryMapper {
	
	//新增一条谈薪记录
	void insertTalkSalary(ErpTalkSalary erpTalkSalary);
	
	//修改一条谈薪记录
	void updateTalkSalary(ErpTalkSalary erpTalkSalary);
	
	//主键查询
	ErpTalkSalary findOneByOfferId(Integer offerId);
	
}