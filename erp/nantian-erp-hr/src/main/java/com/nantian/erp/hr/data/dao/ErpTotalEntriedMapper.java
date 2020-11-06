package com.nantian.erp.hr.data.dao;

import com.nantian.erp.hr.data.model.ErpTotalEntried;

/**
 * 员工入职总记录Mapper
 * @author ZhangYuWei
 */
public interface ErpTotalEntriedMapper {
	
	//查询入职记录总数
	public int countEntried(Integer postId);
	
	//新增一条入职统计信息
	public void insertTotalEntried(ErpTotalEntried erpTotalEntried);
	
}
