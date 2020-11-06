package com.nantian.erp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Map;

import com.google.common.collect.Maps;

public class JWTHelper {

	// 签名秘钥
	private final static String secret = "nantian_erp_token_secret_jwt";
	private static JwtTokenHeader header = new JwtTokenHeader();
	private static  JwtTokenPayload payload;
	private static String sign;// 生产的签名

	public static String createJWT() {

		Map<String, Object> map = Maps.newHashMap();
		map.put("userName", "张三");
		JwtBuilder builder = Jwts.builder().setHeaderParam("typ", header.getTyp()).setClaims(map).signWith(SignatureAlgorithm.HS256, secret);
		return builder.compact();
	}
	
	public static Claims paraseJWT(String jsonWebToken){
		
		try{
			Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(jsonWebToken).getBody();
			return claims;
		}catch(Exception e){
			return null;
		}
		
	}

}
