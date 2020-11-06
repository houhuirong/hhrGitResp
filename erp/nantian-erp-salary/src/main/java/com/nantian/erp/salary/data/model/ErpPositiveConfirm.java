package com.nantian.erp.salary.data.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author gaolp
 * @date 2019年10月14日
 * <p>description: 转正名单确认实体类</p>
 */
@Entity
@Table(name="erp_positive_confirm")
public class ErpPositiveConfirm {

	private Integer id;
	private String yearMonth;		//年月
	private Integer isConfirm;  	//是否确认；1：确认，0：
	private String exceptionMsg;	//异常信息
	private String creatTime;		//创建时间
	private String operator;		//操作人
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getYearMonth() {
		return yearMonth;
	}
	public void setYearMonth(String yearMonth) {
		this.yearMonth = yearMonth;
	}
	public Integer getIsConfirm() {
		return isConfirm;
	}
	public void setIsConfirm(Integer isConfirm) {
		this.isConfirm = isConfirm;
	}
	public String getExceptionMsg() {
		return exceptionMsg;
	}
	public void setExceptionMsg(String exceptionMsg) {
		this.exceptionMsg = exceptionMsg;
	}
	public String getCreatTime() {
		return creatTime;
	}
	public void setCreatTime(String creatTime) {
		this.creatTime = creatTime;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
}
