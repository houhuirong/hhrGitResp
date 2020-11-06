package com.nantian.erp.authentication.constants;

/**
 * @author 张玉伟
 * ERP人力资源相关字典表常量类
 * 2018-09-11
 */
public class DicConstants {
	
	//================================= 短信验证常量 -开始   ================================
	public static final Integer APPID = 1400185166;//短信应用SDK AppID
	public static final String APPKEY = "80ba8c0f8c1118e84152bc3b759c2ad1";//短信应用SDK AppKey
	public static final Integer TEMPLATE_ID = 280134;//短信模板ID，需要在短信应用中申请
	public static final String SMSSIGN = "薪酬管理";//签名
	//================================= 短信验证常量 -结束   ================================
	
	//================================= 菜单与按钮标识常量 -开始   ================================
	public static final String MENU_TYPE = "1";//菜单
	public static final String BUTTON_TYPE = "2";//按钮
	//================================= 菜单与按钮标识常量 -结束   ================================
	
	//=================== Redis的key值前缀 =============================================
	public static final String REDIS_PREFIX_ERRORCOUNT = "errorCount_"; //用户连续登录失败次数
	public static final String REDIS_PREFIX_CHECKCODE = "checkCode_"; //验证码
	public static final String REDIS_PREFIX_USER = "user_employee_"; //用户信息(key为employeeId，v为邮箱手机号)

	//=================== Redis的key值前缀 =============================================
	
}
