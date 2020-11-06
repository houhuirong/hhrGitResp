package com.nantian.erp.salary.data.vo;

import com.nantian.erp.salary.data.model.ErpTalkSalary;
import com.nantian.erp.salary.data.model.ErpTalkSalaryRecord;

/**
 * 接受前端传来的两个对象参数
 * @author 曹秀斌
 * @date 2018-09-17
 */
public class ParamTalkSalaryVo {
	
	//面试谈薪 对象
	private ErpTalkSalary erpTalkSalary;
	
	//谈薪记录 对象
	private ErpTalkSalaryRecord erpTalkSalaryRecord;

	public ParamTalkSalaryVo(ErpTalkSalary erpTalkSalary, ErpTalkSalaryRecord erpTalkSalaryRecord) {
		super();
		this.erpTalkSalary = erpTalkSalary;
		this.erpTalkSalaryRecord = erpTalkSalaryRecord;
	}

	public ParamTalkSalaryVo() {
		super();
	}

	public ErpTalkSalary getErpTalkSalary() {
		return erpTalkSalary;
	}

	public void setErpTalkSalary(ErpTalkSalary erpTalkSalary) {
		this.erpTalkSalary = erpTalkSalary;
	}

	public ErpTalkSalaryRecord getErpTalkSalaryRecord() {
		return erpTalkSalaryRecord;
	}

	public void setErpTalkSalaryRecord(ErpTalkSalaryRecord erpTalkSalaryRecord) {
		this.erpTalkSalaryRecord = erpTalkSalaryRecord;
	}
	
	
	
	
	
	
	
	

}
