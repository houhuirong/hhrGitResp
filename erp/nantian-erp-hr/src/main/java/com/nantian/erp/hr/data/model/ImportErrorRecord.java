package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "importErrorRecord")
public class ImportErrorRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "lineNo")
	private String lineNo; //错误数据的行号

	@Column(name = "error_content")
	private String errorContent;//错误内容

	public String getLineNo() {
		return lineNo;
	}

	public void setLineNo(String lineNo) {
		this.lineNo = lineNo;
	}
	
	public String getErrorContent() {
		return errorContent;
	}

	public void setErrorContent(String errorContent) {
		this.errorContent = errorContent;
	}

	@Override
	public String toString() {
		return "ImportErrorRecord [lineNo=" + lineNo + ", errorContent=" + errorContent + "]";
	}
	
	

	
	
}
