package com.nantian.erp.salary.data.model;

/** 
 * Description: 入职-上岗工资单日志记录表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpPeriodRecord {
	
	//试用期-上岗工资单记录表编号
	private Integer id;
	//员工Id
    private Integer erpEmployeeId;
    //处理人-生成试用期工资单的人
    private String erpPayrollHandler;
    //生成时间
    private String payrollTime;
    //内容
    private String content;
    
    public ErpPeriodRecord() {
        super();
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getErpEmployeeId() {
		return erpEmployeeId;
	}

	public void setErpEmployeeId(Integer erpEmployeeId) {
		this.erpEmployeeId = erpEmployeeId;
	}

	public String getErpPayrollHandler() {
		return erpPayrollHandler;
	}

	public void setErpPayrollHandler(String erpPayrollHandler) {
		this.erpPayrollHandler = erpPayrollHandler;
	}

	public String getPayrollTime() {
		return payrollTime;
	}

	public void setPayrollTime(String payrollTime) {
		this.payrollTime = payrollTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "ErpPeriodRecord [id=" + id + ", erpEmployeeId=" + erpEmployeeId + ", erpPayrollHandler="
				+ erpPayrollHandler + ", payrollTime=" + payrollTime + ", content=" + content + "]";
	}

}