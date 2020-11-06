package com.nantian.erp.authentication.data.model;

public class ErpSysButton {
	
	//主键
    private Integer BtnID;
    //按钮名字
    private String BtnName;
    //按钮编号
    private Integer BtnNo;
    //按钮样式
    private String BtnClass;
    //按钮图标
    private String BtnIcon;
    //按钮脚本
    private String BtnScript;
    //菜单编号
    private String MenuNo;
    //按钮状态 文本框 - 按钮
    private Integer InitStatus;
    //判断末节点的类型
  	private String type;
  	
  	private Integer[] urlList;
  	
	public Integer[] getUrlList() {
		return urlList;
	}

	public void setUrlList(Integer[] urlList) {
		this.urlList = urlList;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ErpSysButton(Integer btnID, String btnName, Integer btnNo, String btnClass, String btnIcon, String btnScript,
			String menuNo, Integer initStatus) {
		super();
		BtnID = btnID;
		BtnName = btnName;
		BtnNo = btnNo;
		BtnClass = btnClass;
		BtnIcon = btnIcon;
		BtnScript = btnScript;
		MenuNo = menuNo;
		InitStatus = initStatus;
	}

	public ErpSysButton() {
		super();
	}

	public Integer getBtnID() {
		return BtnID;
	}

	public void setBtnID(Integer btnID) {
		BtnID = btnID;
	}

	public String getBtnName() {
		return BtnName;
	}

	public void setBtnName(String btnName) {
		BtnName = btnName;
	}

	public Integer getBtnNo() {
		return BtnNo;
	}

	public void setBtnNo(Integer btnNo) {
		BtnNo = btnNo;
	}

	public String getBtnClass() {
		return BtnClass;
	}

	public void setBtnClass(String btnClass) {
		BtnClass = btnClass;
	}

	public String getBtnIcon() {
		return BtnIcon;
	}

	public void setBtnIcon(String btnIcon) {
		BtnIcon = btnIcon;
	}

	public String getBtnScript() {
		return BtnScript;
	}

	public void setBtnScript(String btnScript) {
		BtnScript = btnScript;
	}

	public String getMenuNo() {
		return MenuNo;
	}

	public void setMenuNo(String menuNo) {
		MenuNo = menuNo;
	}

	public Integer getInitStatus() {
		return InitStatus;
	}

	public void setInitStatus(Integer initStatus) {
		InitStatus = initStatus;
	}

   
}