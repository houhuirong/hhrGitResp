/**
 * Copyright (c) 2015, China Construction Bank Co., Ltd. All rights reserved.
 * 南天软件版权所有.
 *
 * 审核人：
 */
package com.nantian.erp.common.base.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Web处理相关的工具类
 * <p>
 * 
 * @author nantian.co
 * @version 1.0 2015年8月11日
 * @see
 */
public class WebUtils {

	/**
	 * 获取http request对象
	 * 
	 * @return
	 */
	public static HttpServletRequest getRequest() {
		// 获取http请求参数
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes();
		HttpServletRequest request = attr.getRequest();

		return request;
	}
	
	
	/**
	 * 获取http response对象
	 * 
	 * @return
	 */
	public static HttpServletResponse getResponse() {
		// 获取http请求参数
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes();
		
		HttpServletResponse response = attr.getResponse();

		return response;
	}

	/**
	 * 根据请求参数名获取参数值
	 * 
	 * @param name
	 * @return
	 */
	public static String getRequestParameter(String name) {

		HttpServletRequest request = WebUtils.getRequest();

		return request.getParameter(name);
	}

	/**
	 * 获取客户端的IP地址
	 * 
	 * @return
	 */
	public static String getClientIp(HttpServletRequest request) {

		String ip = request.getHeader("X-Forwarded-For");
		if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = ip.indexOf(",");
			if (index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		}
		ip = request.getHeader("X-Real-IP");
		if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}
		return request.getRemoteAddr();
	}

}
