package com.nantian.erp.hr.data.dao;

import java.util.List;

import com.nantian.erp.hr.data.model.ErpPositiveRecord;

/**
 * 转正记录表Mapper
 * @author ZhangYuWei
 */
public interface ErpPositiveRecordMapper {
	
	//add 20180917  增加一条转正记录
	public void insertPositiveRecord(ErpPositiveRecord positiveRecord);
	
	//add 20180917  通过员工编号employeeId按照时间倒序查询入职记录信息
	public List<ErpPositiveRecord> findByEmployeeId(Integer employeeId);
	
}
