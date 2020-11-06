package com.nantian.erp.hr.data.dao;

import java.util.List;
import com.nantian.erp.hr.data.model.ErpEmployeeRecord;

/**
 * 员工在职记录表Mapper
 * @author ZhangQian
 */
public interface ErpEmployeeRecordMapper {
	
	//增加一条员工在职记录信息
	public void insertEmployeeRecord(ErpEmployeeRecord employeeRecord);
	
	//查询员工在职记录信息
	public List<ErpEmployeeRecord> findEmployeeRecord(Integer employeeId);
	
}
