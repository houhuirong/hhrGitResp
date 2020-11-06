package com.nantian.erp.salary.data.model;

/** 
 * Description: 字典表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpAdminDic {
	private Integer dicId;   //主键
	private String dicCode;  //字典标识
	private String dicName;  //字典名字
	private String dicType;  //字典类型
	
	public Integer getDicId() {
		return dicId;
	}
	public void setDicId(Integer dicId) {
		this.dicId = dicId;
	}
	public String getDicCode() {
		return dicCode;
	}
	public void setDicCode(String dicCode) {
		this.dicCode = dicCode;
	}
	public String getDicName() {
		return dicName;
	}
	public void setDicName(String dicName) {
		this.dicName = dicName;
	}
	public String getDicType() {
		return dicType;
	}
	public void setDicType(String dicType) {
		this.dicType = dicType;
	}
	
	@Override
	public String toString() {
		return "AdminDic [dicId=" + dicId + ", dicCode=" + dicCode + ", dicName=" + dicName + ", dicType=" + dicType
				+ "]";
	}
	
}
