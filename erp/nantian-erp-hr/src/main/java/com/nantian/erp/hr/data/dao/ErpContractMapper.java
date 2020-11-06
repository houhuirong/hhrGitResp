package com.nantian.erp.hr.data.dao;

import org.apache.ibatis.annotations.Param;

import com.nantian.erp.hr.data.model.ErpContract;

/**
 * 合同Mapper
 * @author ZhangYuWei
 */
public interface ErpContractMapper {
	
	//add 20180917  插入一条合同记录
	public void insertContract(ErpContract contact);
	
	//add 20180917  通过员工ID修改合同终止时间
	public void updateContractByEmployeeId(ErpContract contact);
	
	//add by lx 通过员工iD查找合同记录
	public ErpContract findContractByEmpId(@Param(value = "employeeId") Integer empId);
	
	public void updateContractById(ErpContract contact);
	
	
}
