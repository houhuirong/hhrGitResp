package com.nantian.erp.common.model;

import java.util.List;

public class Page<E> implements java.io.Serializable{

	private static final long serialVersionUID = 27317864540481902L;
	
	/**
	 * 总条数
	 */
	private Integer totalNumber;
	
	private List<E> list;

	public Page(Integer totalNumber, List<E> list) {
		super();
		this.totalNumber = totalNumber;
		this.list = list;
	}
	public List<E> getList() {
		return list;
	}
	public void setList(List<E> list) {
		this.list = list;
	}
	public Integer getTotalNumber() {
		return totalNumber;
	}
	public void setTotalNumber(Integer totalNumber) {
		this.totalNumber = totalNumber;
	}
	

}
