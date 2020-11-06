package com.nantian.erp.authentication.data.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Table(name="user_role")
public class ErpUserRole implements Serializable{
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	   @Column(name = "id")
	private Integer id;
	   @Column(name = "uId")
	private Integer uId;
	   @Column(name = "rId")
	private Integer rId;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getuId() {
		return uId;
	}
	public void setuId(Integer uId) {
		this.uId = uId;
	}
	public Integer getrId() {
		return rId;
	}
	public void setrId(Integer rId) {
		this.rId = rId;
	}
	@Override
	public String toString() {
		return "ErpUserRole [id=" + id + ", uId=" + uId + ", rId=" + rId + "]";
	}
	    
}
