package com.nantian.erp.salary.data.model;

/** 
 * Description: 员工薪酬-员工实际的社保和公积金的基数生成Log记录
 * @author 宋修功
 * @date 2020年02月18日
 * @version 1.0
 */
public class ErpActualInsuranceFundBaseRecord {
	private String id;
	private Integer actualInsuranceFundBaseId;	//实际基数ID
	private Integer submitEmployeeId;	//提交人员的员工ID
	private String meno;				//更新操作说明
	private String gmtModified;			//修改时间
	private String gmtCreate;			//创建时间
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the actualInsuranceFundBaseId
	 */
	public Integer getActualInsuranceFundBaseId() {
		return actualInsuranceFundBaseId;
	}
	/**
	 * @param actualInsuranceFundBaseId the actualInsuranceFundBaseId to set
	 */
	public void setActualInsuranceFundBaseId(Integer actualInsuranceFundBaseId) {
		this.actualInsuranceFundBaseId = actualInsuranceFundBaseId;
	}
	/**
	 * @return the submitEmployeeId
	 */
	public Integer getSubmitEmployeeId() {
		return submitEmployeeId;
	}
	/**
	 * @param submitEmployeeId the submitEmployeeId to set
	 */
	public void setSubmitEmployeeId(Integer submitEmployeeId) {
		this.submitEmployeeId = submitEmployeeId;
	}
	/**
	 * @return the meno
	 */
	public String getMeno() {
		return meno;
	}
	/**
	 * @param meno the meno to set
	 */
	public void setMeno(String meno) {
		this.meno = meno;
	}
	/**
	 * @return the gmtModified
	 */
	public String getGmtModified() {
		return gmtModified;
	}
	/**
	 * @param gmtModified the gmtModified to set
	 */
	public void setGmtModified(String gmtModified) {
		this.gmtModified = gmtModified;
	}
	/**
	 * @return the gmtCreate
	 */
	public String getGmtCreate() {
		return gmtCreate;
	}
	/**
	 * @param gmtCreate the gmtCreate to set
	 */
	public void setGmtCreate(String gmtCreate) {
		this.gmtCreate = gmtCreate;
	}
	
	@Override
	public String toString() {
		return "ErpActualInsuranceFundBaseRecord [id=" + id + ", actualInsuranceFundBaseId=" + actualInsuranceFundBaseId
				+ ", submitEmployeeId=" + submitEmployeeId + ", meno=" + meno + ", gmtModified=" + gmtModified
				+ ", gmtCreate=" + gmtCreate + "]";
	}
	
}
