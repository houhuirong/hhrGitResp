package com.nantian.erp.salary.data.vo;

import com.nantian.erp.salary.data.model.ErpBasePayroll;
import com.nantian.erp.salary.data.model.ErpPeriodPayroll;
import com.nantian.erp.salary.data.model.ErpPeriodRecord;
import com.nantian.erp.salary.data.model.ErpPositiveSalary;
import com.nantian.erp.salary.data.model.ErpTraineeSalary;

/**
 * 接受前端传来的两个对象参数
 * @author 曹秀斌
 * @date 2018-09-17
 */
public class ParamSalaryPeriodVo {
	
	//试用期-薪资对象
	private ErpPeriodPayroll erpPeriodPayroll;
	
	//转正-薪资对象
	//private ErpPositivePayroll erpPositivePayroll;
	
	//试用期-上岗工资单-记录对象
	private ErpPeriodRecord erpPeriodRecord;
	
	//转正-上岗工资单-记录对象
	//private ErpPositiveRecord erpPositiveRecord;
	
	//员工薪资表
	private ErpBasePayroll erpBasePayroll;
	
	//转正薪资表
	private ErpPositiveSalary erpPositiveSalary;
	//转正薪资表
	private ErpTraineeSalary erpTraineeSalary;		

	public ParamSalaryPeriodVo(ErpPeriodPayroll erpPeriodPayroll,
			ErpPeriodRecord erpPeriodRecord, ErpBasePayroll erpBasePayroll,
			ErpPositiveSalary erpPositiveSalary,
			ErpTraineeSalary erpTraineeSalary) {
		super();
		this.erpPeriodPayroll = erpPeriodPayroll;
		this.erpPeriodRecord = erpPeriodRecord;
		this.erpBasePayroll = erpBasePayroll;
		this.erpPositiveSalary = erpPositiveSalary;
		this.erpTraineeSalary = erpTraineeSalary;
	}

	public ParamSalaryPeriodVo() {
		super();
	}

	public ErpPeriodPayroll getErpPeriodPayroll() {
		return erpPeriodPayroll;
	}

	public void setErpPeriodPayroll(ErpPeriodPayroll erpPeriodPayroll) {
		this.erpPeriodPayroll = erpPeriodPayroll;
	}

	public ErpPeriodRecord getErpPeriodRecord() {
		return erpPeriodRecord;
	}

	public void setErpPeriodRecord(ErpPeriodRecord erpPeriodRecord) {
		this.erpPeriodRecord = erpPeriodRecord;
	}
	
	public ErpBasePayroll getErpBasePayroll() {
		return erpBasePayroll;
	}

	public void setErpBasePayroll(ErpBasePayroll erpBasePayroll) {
		this.erpBasePayroll = erpBasePayroll;
	}

	public ErpPositiveSalary getErpPositiveSalary() {
		return erpPositiveSalary;
	}

	public void setErpPositiveSalary(ErpPositiveSalary erpPositiveSalary) {
		this.erpPositiveSalary = erpPositiveSalary;
	}
	public ErpTraineeSalary getErpTraineeSalary() {
		return erpTraineeSalary;
	}

	public void setErpTraineeSalary(ErpTraineeSalary erpTraineeSalary) {
		this.erpTraineeSalary = erpTraineeSalary;
	}

	@Override
	public String toString() {
		return "ParamSalaryPeriodVo [erpPeriodPayroll=" + erpPeriodPayroll + ", erpPeriodRecord=" + erpPeriodRecord
				+ ", erpBasePayroll=" + erpBasePayroll + ", erpPositiveSalary=" + erpPositiveSalary
				+ ", erpTraineeSalary=" + erpTraineeSalary + "]";
	}
	
}
