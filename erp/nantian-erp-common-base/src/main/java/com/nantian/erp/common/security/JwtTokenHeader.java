package com.nantian.erp.common.security;


/**
 * JWT头信息
 * @author test
 *
 */
public class JwtTokenHeader {
	
	private String typ = "JWT";//类型
	private String alg = "HS256";//签名所用的算法
	
	
	public String getTyp() {
		return typ;
	}
	public void setTyp(String typ) {
		this.typ = typ;
	}
	public String getAlg() {
		return alg;
	}
	public void setAlg(String alg) {
		this.alg = alg;
	}
	
	

}
