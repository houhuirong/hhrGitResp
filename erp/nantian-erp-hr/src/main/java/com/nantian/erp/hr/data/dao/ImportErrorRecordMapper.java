package com.nantian.erp.hr.data.dao;

import java.util.List;

import com.nantian.erp.hr.data.model.ImportErrorRecord;

/**
 * 导入exce表格错误记录Mapper
 * @author 
 */
public interface ImportErrorRecordMapper {
	
	// 插入错误记录
	public int insertErrorRecord(ImportErrorRecord errorRecord);
	
	public void insertErrorRecordByBatch(List<ImportErrorRecord> errorRecordList);

	
}
