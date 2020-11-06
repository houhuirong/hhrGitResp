package com.nantian.erp.salary.data.model;

/** 
 * Description: 导入新出数据错误日志记录表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpBasePayrollRecord {
	
	//错误日志表主键
	private Integer recordId;
	//员工姓名
	private String empName;
	//员工身份证号
	private String empIdCardNum;
	//错误内容
	private String errorContent;
	//出现错误的时间
	private String errorTime;
	
	public ErpBasePayrollRecord() {
		super();
	}

	public Integer getRecordId() {
		return recordId;
	}

	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}

	public String getEmpName() {
		return empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	public String getEmpIdCardNum() {
		return empIdCardNum;
	}

	public void setEmpIdCardNum(String empIdCardNum) {
		this.empIdCardNum = empIdCardNum;
	}

	public String getErrorContent() {
		return errorContent;
	}

	public void setErrorContent(String errorContent) {
		this.errorContent = errorContent;
	}

	public String getErrorTime() {
		return errorTime;
	}

	public void setErrorTime(String errorTime) {
		this.errorTime = errorTime;
	}
	
}
