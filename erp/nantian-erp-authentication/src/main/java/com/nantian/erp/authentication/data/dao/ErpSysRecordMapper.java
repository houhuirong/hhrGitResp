package com.nantian.erp.authentication.data.dao;

import java.util.List;

import com.nantian.erp.authentication.data.model.ErpSysRecord;

/**
 * 系统权限记录表信息mapper
 * @author ZhangQian
 */
public interface ErpSysRecordMapper {
	
	//插入简历失效和生效记录
	public void insertRecord(ErpSysRecord record);
	
	//根据ID查询全部的记录
	public List<ErpSysRecord> selectRecordById(Integer opType, Integer opId);
	
}
