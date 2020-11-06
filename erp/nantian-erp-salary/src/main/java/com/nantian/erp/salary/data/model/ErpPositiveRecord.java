package com.nantian.erp.salary.data.model;

/** 
 * Description: 转正-上岗工资单日志记录表PO
 *
 * @author ZhangYuWei
 * @version 1.0
 */
public class ErpPositiveRecord {
	
	//试用期-上岗工资单记录表编号
	private Integer id;
	//员工Id
	private Integer erpEmployeeId;
	//处理人-生成转正上岗工资单的人
	private String erpPositiveHandler;
	//生成时间
	private String positiveTime;
	//内容
	private String content;
	
	public ErpPositiveRecord() {
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

	public String getErpPositiveHandler() {
		return erpPositiveHandler;
	}

	public void setErpPositiveHandler(String erpPositiveHandler) {
		this.erpPositiveHandler = erpPositiveHandler;
	}

	public String getPositiveTime() {
		return positiveTime;
	}

	public void setPositiveTime(String positiveTime) {
		this.positiveTime = positiveTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "ErpPositiveRecord [id=" + id + ", erpEmployeeId=" + erpEmployeeId + ", erpPositiveHandler="
				+ erpPositiveHandler + ", positiveTime=" + positiveTime + ", content=" + content + "]";
	}

}