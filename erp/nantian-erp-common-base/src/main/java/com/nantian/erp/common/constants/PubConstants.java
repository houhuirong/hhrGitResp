/**
 * Copyright (c) 2013, China Construction Bank Co., Ltd. All rights reserved.
 * 南天软件版权所有.
 *
 * 审核人：
 */
package com.nantian.erp.common.constants;

/**
 * 自动化全局全局常量类
 * <p>
 * 
 * @author nantian.co
 * @version 1.0 2013-5-14 
 * @see
 */
public class PubConstants {

	
	//分隔符常量
	public static final String SEPARATOR_COMMAS = ",";// 逗号分隔符
	public static final String SEPARATOR_SEMICOLON = ";";// 分号分隔符
	public static final String SEPARATOR_COLON = ":";// 冒号分隔符
	public static final String SEPARATOR_VERTICAL = "\\|";// 竖线分隔符
	public static final String SEPARATOR_SPACE = " ";// 空格分隔符
	public static final String SINGLE_QUOTE_MARK = "'";
	public static final String DOUBLE_QUOTE_MARK = "\"";
	
	public static final String EXEC_RESULT_SUCC = "0000";//组件执行成功
	public static final String EXEC_RESULT_FAIL = "9999";//组件执行失败
	
	public static final String JAVA_ENCODE  = "UTF-8";//用于JAVA字符转换
	
	
	public static final String SUPER_USER = "root";//超级用户名
	
	
	public static final String PASSWORD_KEY="CCBCLOUD";
	
	public final static String FILE_SEPARATOR = System.getProperty("file.separator");
	public final static String BR = System.getProperty("line.separator");
	public final static String JAVA_IO_TMPDIR = System.getProperty("java.io.tmpdir");
	
	//liufei 2015-10-28 调用som接口调用者标识，iomp表示云平台
	public final static String SOM_PROVIDER_IOMP = "iomp";
	
	/**
	 * 操作类型 用于写日志用
	 */
	public final static String SDN_OPERATION_TYPE_INSERT = "insert";
	public final static String SDN_OPERATION_TYPE_UPDATE = "update";
	public final static String SDN_OPERATION_TYPE_DELETE = "delete";
	
	public final static String SDN_OPERATION_LOG_PO = "sdnoperationlogpo";
	
	
}
