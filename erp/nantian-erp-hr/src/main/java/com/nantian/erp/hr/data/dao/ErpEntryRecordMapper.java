package com.nantian.erp.hr.data.dao;

import java.util.List;

import com.nantian.erp.hr.data.model.ErpEntryRecord;

/**
 * 入职记录表Mapper
 * @author ZhangYuWei
 */
public interface ErpEntryRecordMapper {
	
	//增加一条入职记录信息
	public void insertEntryRecord(ErpEntryRecord entryRecord);
	
	//通过offerId按照时间倒序查询入职记录信息
	public List<ErpEntryRecord> findByOfferId(Integer offerId);
	
}
