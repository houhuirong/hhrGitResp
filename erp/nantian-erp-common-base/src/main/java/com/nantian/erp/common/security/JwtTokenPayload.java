package com.nantian.erp.common.security;


/**
 * JWT载荷（Payload）
 * @author test
 *
 */
public class JwtTokenPayload {

	//标准所定义
	private String iss; // jwt签发者
	private String sub; // jwt所面向的用户
	private String aud; // 接收jwt的一方
	private String exp; // jwt的过期时间，这个过期时间必须要大于签发时间
	//private String nbf; // 定义在什么时间之前，该jwt都是不可用的.
	private String iat; // wt的签发时间
	//private String jti; // jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击
	
	
	public String getIss() {
		return iss;
	}
	public void setIss(String iss) {
		this.iss = iss;
	}
	public String getSub() {
		return sub;
	}
	public void setSub(String sub) {
		this.sub = sub;
	}
	public String getAud() {
		return aud;
	}
	public void setAud(String aud) {
		this.aud = aud;
	}
	public String getExp() {
		return exp;
	}
	public void setExp(String exp) {
		this.exp = exp;
	}
	public String getIat() {
		return iat;
	}
	public void setIat(String iat) {
		this.iat = iat;
	}
	
	
	
	
	
	
	
	
}
