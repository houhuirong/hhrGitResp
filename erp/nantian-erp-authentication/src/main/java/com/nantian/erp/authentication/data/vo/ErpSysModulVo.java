package com.nantian.erp.authentication.data.vo;

import java.util.List;

/** 
 * Description: 模块信息封装类
 * @author caoxiubin
 * @version 1.0
 * <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月11日                   caoxiubin         1.0        
 * </pre>
 */
public class ErpSysModulVo {
    
	//模块ID
	private Integer ModulId;
	
	//模块名字
	private String ModulName;
	
	//模块描述
	private String ModulDesc;
	
	//下挂接口集合
	private List<ErpSysUrlVo> urlList;

	public Integer getModulId() {
		return ModulId;
	}

	public void setModulId(Integer modulId) {
		ModulId = modulId;
	}

	public String getModulName() {
		return ModulName;
	}

	public void setModulName(String modulName) {
		ModulName = modulName;
	}

	public String getModulDesc() {
		return ModulDesc;
	}

	public void setModulDesc(String modulDesc) {
		ModulDesc = modulDesc;
	}

	public List<ErpSysUrlVo> getUrlList() {
		return urlList;
	}

	public void setUrlList(List<ErpSysUrlVo> urlList) {
		this.urlList = urlList;
	}
	
	
	
	
	
}
