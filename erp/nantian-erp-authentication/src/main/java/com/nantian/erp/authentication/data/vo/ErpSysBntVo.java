package com.nantian.erp.authentication.data.vo;

/**
 * Description: 将按钮对象封装给前端需要的格式-菜单格式
 * 
 * @author caoxiubin
 * @version 1.0
 * 
 *          <pre>
 * Modification History: 
 * Date                   Author           Version     
 * ------------------------------------------------
 * 2018年10月17日                   caoxiubin         1.0
 *          </pre>
 */
public class ErpSysBntVo {
	
	
	
	//按钮id
    private String MenuNo;
    //按钮名字
    private String MenuName;
    //按钮编号
    private Integer BtnNo;
    //按钮样式
    private String BtnClass;
    //按钮图标
    private String BtnIcon;
    //按钮脚本
    private String BtnScript;
    //关联的菜单编号
    private String MenuparentNo;
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
	public String getMenuNo() {
		return MenuNo;
	}
	public void setMenuNo(String menuNo) {
		MenuNo = menuNo;
	}
	
	public String getMenuName() {
		return MenuName;
	}
	public void setMenuName(String menuName) {
		MenuName = menuName;
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
	public String getMenuparentNo() {
		return MenuparentNo;
	}
	public void setMenuparentNo(String menuparentNo) {
		MenuparentNo = menuparentNo;
	}
	public Integer getInitStatus() {
		return InitStatus;
	}
	public void setInitStatus(Integer initStatus) {
		InitStatus = initStatus;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	

	
   
}