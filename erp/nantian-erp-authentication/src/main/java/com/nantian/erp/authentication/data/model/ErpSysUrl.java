package com.nantian.erp.authentication.data.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "Sys_Url")
public class ErpSysUrl implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "UrlID")
	private Integer UrlID;
	
	@Column(name = "urlPath")
	private String urlPath;
	
	@Column(name = "urlDesc")
	private String urlDesc;

	public Integer getUrlID() {
		return UrlID;
	}

	public void setUrlID(Integer urlID) {
		UrlID = urlID;
	}

	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}

	public String getUrlDesc() {
		return urlDesc;
	}

	public void setUrlDesc(String urlDesc) {
		this.urlDesc = urlDesc;
	}

	@Override
	public String toString() {
		return "ErpSysUrl [UrlID=" + UrlID + ", urlPath=" + urlPath + ", urlDesc=" + urlDesc + "]";
	}
	
}
