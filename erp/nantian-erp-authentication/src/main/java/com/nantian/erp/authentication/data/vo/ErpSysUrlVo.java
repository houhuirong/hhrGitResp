package com.nantian.erp.authentication.data.vo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ErpSysUrlVo implements Serializable{

	//主键
	private Integer UrlID;
	//接口路径
	private String UrlPath;
	//接口描述
	private String UrlDesc;
	//模块ID
	private Integer ModulId;
	
	//接口可供几个角色访问
	private String[] menus;

	public ErpSysUrlVo(Integer urlID, String urlPath, String urlDesc, Integer modulId) {
		super();
		UrlID = urlID;
		UrlPath = urlPath;
		UrlDesc = urlDesc;
		ModulId = modulId;
	}

	public ErpSysUrlVo() {
		super();
	}

	public Integer getUrlID() {
		return UrlID;
	}

	public void setUrlID(Integer urlID) {
		UrlID = urlID;
	}

	public String getUrlPath() {
		return UrlPath;
	}

	public void setUrlPath(String urlPath) {
		UrlPath = urlPath;
	}

	public String getUrlDesc() {
		return UrlDesc;
	}

	public void setUrlDesc(String urlDesc) {
		UrlDesc = urlDesc;
	}

	public Integer getModulId() {
		return ModulId;
	}

	public void setModulId(Integer modulId) {
		ModulId = modulId;
	}

	public String[] getMenus() {
		return menus;
	}

	public void setMenus(String[] menus) {
		this.menus = menus;
	}


}
