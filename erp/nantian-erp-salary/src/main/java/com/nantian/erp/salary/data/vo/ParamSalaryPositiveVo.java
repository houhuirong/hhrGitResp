package com.nantian.erp.salary.data.vo;

import com.nantian.erp.salary.data.model.ErpBasePayroll;
import com.nantian.erp.salary.data.model.ErpPositivePayroll;
import com.nantian.erp.salary.data.model.ErpPositiveRecord;

/**
 * 接受前端传来的两个对象参数
 * @author 曹秀斌
 * @date 2018-09-17
 */
public class ParamSalaryPositiveVo {
	
	private String probationEndTime;
	//转正-薪资对象
	private ErpPositivePayroll erpPositivePayroll;
	
	//转正-上岗工资单-记录对象
	private ErpPositiveRecord erpPositiveRecord;
	
	//员工薪资表
	private ErpBasePayroll erpBasePayroll;


	public ParamSalaryPositiveVo() {
		super();
	}

	public ParamSalaryPositiveVo(ErpPositivePayroll erpPositivePayroll,
			ErpPositiveRecord erpPositiveRecord, ErpBasePayroll erpBasePayroll) {
		super();
		this.erpPositivePayroll = erpPositivePayroll;
		this.erpPositiveRecord = erpPositiveRecord;
		this.erpBasePayroll = erpBasePayroll;
	}


	public ErpPositivePayroll getErpPositivePayroll() {
		return erpPositivePayroll;
	}

	public void setErpPositivePayroll(ErpPositivePayroll erpPositivePayroll) {
		this.erpPositivePayroll = erpPositivePayroll;
	}

	public ErpPositiveRecord getErpPositiveRecord() {
		return erpPositiveRecord;
	}

	public void setErpPositiveRecord(ErpPositiveRecord erpPositiveRecord) {
		this.erpPositiveRecord = erpPositiveRecord;
	}
	public ErpBasePayroll getErpBasePayroll() {
		return erpBasePayroll;
	}

	public void setErpBasePayroll(ErpBasePayroll erpBasePayroll) {
		this.erpBasePayroll = erpBasePayroll;
	}

	public String getProbationEndTime() {
		return probationEndTime;
	}

	public void setProbationEndTime(String probationEndTime) {
		this.probationEndTime = probationEndTime;
	}

	@Override
	public String toString() {
		return "ParamSalaryPositiveVo [probationEndTime=" + probationEndTime + ", erpPositivePayroll="
				+ erpPositivePayroll + ", erpPositiveRecord=" + erpPositiveRecord + ", erpBasePayroll=" + erpBasePayroll
				+ "]";
	}

	
	
}
