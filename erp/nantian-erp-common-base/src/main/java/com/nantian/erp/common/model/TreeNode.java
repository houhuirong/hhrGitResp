/**
 * Copyright (c) 2015, China Construction Bank Co., Ltd. All rights reserved.
 * 南天软件版权所有.
 *
 * 审核人：
 */
package com.nantian.erp.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 通用的树形节点实体类，封装树形节点数据。
 * <p>
 * 
 * @author nantian.co
 * @version 1.0 2015年7月15日
 * @see
 */
public class TreeNode implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2881760877820261948L;

	private String id;// 节点ID
	private String upId;// 上级节点ID
	private String icon;// 节点图标
	private String iconSkin;// 节点样式
	private String name;// 名称
	private boolean isParent = false;// 是否是父节点（true：表示有子节点，false：表示没有子节点）
	private boolean checked = false;// 当前节点是否选中
	private boolean open = false;// 是否展开

	/** 子节点集合 */
	private List<TreeNode> children = null;
	/** 节点参数 */
	private Map<String, String> params = Maps.newHashMap();

	/**
	 * 添加子节点
	 * 
	 * @param childNode
	 */
	public void addChildNode(TreeNode childNode) {

		if (this.children == null)
			this.children = new ArrayList<TreeNode>();

		this.children.add(childNode);
		childNode.setUpId(this.id);
	}

	/**
	 * 添加子节点
	 * 
	 * @param childNode
	 */
	public void addChildNode(List<TreeNode> childNodeList) {

		if (null == this.children )
			this.children = new ArrayList<TreeNode>();

		if (null != childNodeList) {
			
			for (TreeNode treeNode : childNodeList)
				this.addChildNode(treeNode);
		}

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUpId() {
		return upId;
	}

	public void setUpId(String upId) {
		this.upId = upId;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * @return the open
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * @param open
	 *            the open to set
	 */
	public void setOpen(boolean open) {
		this.open = open;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the checked
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * @param checked
	 *            the checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	/**
	 * @return the isParent
	 */
	public boolean getIsParent() {
		return isParent;
	}

	/**
	 * @param isParent
	 *            the isParent to set
	 */
	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	public String getIconSkin() {
		return iconSkin;
	}

	public void setIconSkin(String iconSkin) {
		this.iconSkin = iconSkin;
	}
}
