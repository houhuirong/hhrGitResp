/**
 * Copyright (c) 2015, China Construction Bank Co., Ltd. All rights reserved.
 * 南天软件版权所有.
 *
 * 审核人：
 */
package com.nantian.erp.common.constants;

/**
 * 异常代码常量类，定义异常代码
 * <p>
 * 
 * @author nantian.co
 * @version 1.0 2015年7月15日 
 * @see
 */
public class ExceptionConstants {
	
	public static String EX_INNER_ERROR = "999";//内部未预料异常
	public static String EX_INNER_ERROR_MSG = "系统内部异常,请联系管理员!";//内部未预料异常
	
	public static String EX_SESSION_TIMEOUT = "998";//未登陆或者session超时异常
	public static String EX_SESSION_TIMEOUT_MSG = "您未登录或者Session超时,请重新登录!";//
	
	public static String EX_NO_PERMISSION = "997";//无权限访问
	public static String EX_NO_PERMISSION_MSG = "您没有该功能的操作权限，请联系管理员!";//
	
	public static String EX_CONFIG_FILE_INIT_ERROR = "990";//参数文件初始化异常
	
	
	public static String EX_SYS_NOT_EXIST = "996";//系统不存在
	public static String EX_SYS_NOT_EXIST_MSG = "系统配置错误，无法跳转访问!";//系统不存在
	
	public static String EX_DATA_EXEPTION = "001";//数据为空，或者数据非法
	public static String EX_DATA_EXEPTION_MSG = "数据为空，或者数据非法!";//数据为空，或者数据非法
	
	public static String EX_BUSINESS_EXEPTION = "002";//业务逻辑异常
	public static String EX_BUSINESS_EXEPTION_MSG = "业务逻辑异常!";
	
	//public static String EX_DATA_EXEPTION = "009";//系统不存在
	

}
