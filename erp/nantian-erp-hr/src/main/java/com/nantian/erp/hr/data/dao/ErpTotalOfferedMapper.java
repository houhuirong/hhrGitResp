package com.nantian.erp.hr.data.dao;

import com.nantian.erp.hr.data.model.ErpTotalOffered;

/**
 * offer总记录数Mapper
 * @author ZhangYuWei
 */
public interface ErpTotalOfferedMapper {
	
	//插入一条已发offer记录
	public void insertTotalOffered(ErpTotalOffered totalOffered);
	
	//查询Offer记录总数
	public int countOffered(Integer postId);
	
}
