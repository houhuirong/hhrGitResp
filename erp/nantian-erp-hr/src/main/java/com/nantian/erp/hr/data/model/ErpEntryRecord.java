package com.nantian.erp.hr.data.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Table(name="entry_record")
public class ErpEntryRecord {
	   @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	   @Column(name = "id")
	private Integer id;
	   @Column(name = "offerId")
	private Integer offerId;
	private String time;
	private String content;
	private String processoer;
	
	private Integer processoerID;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getOfferId() {
		return offerId;
	}
	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getProcessoer() {
		return processoer;
	}
	public void setProcessoer(String processoer) {
		this.processoer = processoer;
	}
	public Integer getProcessoerID() {
		return processoerID;
	}
	public void setProcessoerID(Integer processoerID) {
		this.processoerID = processoerID;
	}
	@Override
	public String toString() {
		return "ErpEntryRecord [id=" + id + ", offerId=" + offerId + ", time=" + time + ", content=" + content
				+ ", processoer=" + processoer +", processoerID="+processoerID+ "]";
	}
	
}
