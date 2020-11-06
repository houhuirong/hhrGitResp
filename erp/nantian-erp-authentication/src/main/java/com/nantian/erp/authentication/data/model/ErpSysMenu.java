package com.nantian.erp.authentication.data.model;

import java.util.List;

import com.nantian.erp.authentication.data.vo.ErpSysBntVo;

public class ErpSysMenu {
	
	//主键
    private Integer MenuID;
    //菜单编号
    private String MenuNo;
    //上级菜单编号
    private String MenuparentNo;
    //菜单序号
    private Integer MenuOrder;
    //菜单名字
    private String MenuName;
    //菜单路径
    private String MenuUrl;
    //是否有叶子节点
    private Integer IsLeaf;
    
    //是否有按钮节点
    private Integer IsButton;
    
    //子菜单
    private List<ErpSysMenu> childrenList;
    
    //末级菜单下挂的button列表
    private List<ErpSysBntVo> buttonList;
    //登陆查询末级菜单下挂的button列表
    private List<ErpSysButton> buttonListLogin;
    
    private Integer[] urlList;
    
    public Integer[] getUrlList() {
		return urlList;
	}

	public void setUrlList(Integer[] urlList) {
		this.urlList = urlList;
	}

	public Integer getIsButton() {
		return IsButton;
	}

	public void setIsButton(Integer isButton) {
		IsButton = isButton;
	}

	public List<ErpSysButton> getButtonListLogin() {
		return buttonListLogin;
	}

	public void setButtonListLogin(List<ErpSysButton> buttonListLogin) {
		this.buttonListLogin = buttonListLogin;
	}

	//判断末节点的类型
	private String type;
	
	//以字段属性返回前端页面
	private ErpSysButton sysButton;
    
	//id用来保证按钮唯一性
	private String btnId;
	
	public ErpSysMenu() {
		super();
	}
	
	public ErpSysMenu(Integer menuID, String menuNo, String menuparentNo, Integer menuOrder, String menuName, String menuUrl,
			Integer isLeaf, Integer isButton) {
		super();
		MenuID = menuID;
		MenuNo = menuNo;
		MenuparentNo = menuparentNo;
		MenuOrder = menuOrder;
		MenuName = menuName;
		MenuUrl = menuUrl;
		IsLeaf = isLeaf;
		IsButton = isButton;
	}

	public Integer getMenuID() {
		return MenuID;
	}

	public void setMenuID(Integer integer) {
		MenuID = integer;
	}

	public String getMenuNo() {
		return MenuNo;
	}

	public void setMenuNo(String menuNo) {
		MenuNo = menuNo;
	}

	public String getMenuparentNo() {
		return MenuparentNo;
	}

	public void setMenuparentNo(String menuparentNo) {
		MenuparentNo = menuparentNo;
	}

	public Integer getMenuOrder() {
		return MenuOrder;
	}

	public void setMenuOrder(Integer menuOrder) {
		MenuOrder = menuOrder;
	}

	public String getMenuName() {
		return MenuName;
	}

	public void setMenuName(String menuName) {
		MenuName = menuName;
	}

	public String getMenuUrl() {
		return MenuUrl;
	}

	public void setMenuUrl(String menuUrl) {
		MenuUrl = menuUrl;
	}

	public Integer getIsLeaf() {
		return IsLeaf;
	}

	public void setIsLeaf(Integer isLeaf) {
		IsLeaf = isLeaf;
	}


	public List<ErpSysMenu> getChildrenList() {
		return childrenList;
	}

	public void setChildrenList(List<ErpSysMenu> childrenList) {
		this.childrenList = childrenList;
	}

	public List<ErpSysBntVo> getButtonList() {
		return buttonList;
	}

	public void setButtonList(List<ErpSysBntVo> buttonList) {
		this.buttonList = buttonList;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ErpSysButton getSysButton() {
		return sysButton;
	}

	public void setSysButton(ErpSysButton sysButton) {
		this.sysButton = sysButton;
	}

	public String getBtnId() {
		return btnId;
	}

	public void setBtnId(String btnId) {
		this.btnId = btnId;
	}
   
}