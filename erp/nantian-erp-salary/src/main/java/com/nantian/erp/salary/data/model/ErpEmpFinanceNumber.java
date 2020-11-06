package com.nantian.erp.salary.data.model;

/** 
 * Description: 员工财务序号表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpEmpFinanceNumber {
	
	//员工财务序号表主键
	private Integer id;
	//员工编号
	private Integer employeeId;
	//员工财务序号
	private String empFinanceNumber;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public String getEmpFinanceNumber() {
		return empFinanceNumber;
	}
	public void setEmpFinanceNumber(String empFinanceNumber) {
		this.empFinanceNumber = empFinanceNumber;
	}
	
	@Override
	public String toString() {
		return "ErpEmpFinanceNumber [id=" + id + ", employeeId=" + employeeId + ", empFinanceNumber=" + empFinanceNumber
				+ "]";
	}
	
}
