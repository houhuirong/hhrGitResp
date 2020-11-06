package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class ErpPositionRankRelation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "positionNo")
	private Integer positionNo;
	
	@Column(name = "positionType")
	private String positionType;
	
	@Column(name = "positionName")
	private String positionName;
	
	@Column(name = "rank")
	private Integer rank;

	public Integer getPositionNo() {
		return positionNo;
	}

	public void setPositionNo(Integer positionNo) {
		this.positionNo = positionNo;
	}

	public String getPositionType() {
		return positionType;
	}

	public void setPositionType(String positionType) {
		this.positionType = positionType;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	@Override
	public String toString() {
		return "ErpPositionRankRelation [positionNo=" + positionNo + ", positionType=" + positionType
				+ ", positionName=" + positionName + ", rank=" + rank + "]";
	}

	
	
	
	

	
}
