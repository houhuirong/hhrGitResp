package com.nantian.erp.hr.data.dao;

import java.util.List;

import com.nantian.erp.hr.data.model.ErpRecord;

/**
 * 招聘记录表信息mapper
 * @author ZhangYuWei
 */
public interface ErpRecordMapper {
	
	//插入简历失效和生效记录
	public void insertRecord(ErpRecord record)throws Exception;
	
	//根据ID查询全部的记录
	public List<ErpRecord> selectRecordById(Integer resumeId);
	
}
