package com.nantian.erp.common.constants;

public class DicConstants {
	
	//================================ 资源池类型常量 -开始  ===================================
	public final static  String SDN_ADMIN_DIC_TYPE_CODE_RES_POOL = "POOL_TYPE";
	
	public final static  String SDN_RES_POOL_TYPE_FABRIC = "FABRIC";
	public final static  String SDN_RES_POOL_TYPE_SLB = "SLB";
	public final static  String SDN_RES_POOL_TYPE_FW = "FW";
	public final static  String SDN_RES_POOL_TYPE_DNS = "DNS";
	public final static  String SDN_RES_POOL_TYPE_SDN = "SDN";
	public final static  String ADDRESS = "ADDRESS";
	public final static  String DATACENTER_TYPE = "DATACENTER_TYPE";
	public final static  String NW_RES_POOL_TYPE = "NW_RES_POOL_TYPE";
	public final static  String IP_TYPE = "IP_TYPE";
	
	//================================ 资源池类型常量 -结束  ===================================
	
	//============================= 物理设备状态在字典表中的相关常量-开始  ==========================
	public final static  String SDN_ADMIN_DIC_TYPE_CODE_DEVICE_STATUS = "DEVICE_STATUS";
	
	public final static  String SDN_CM_DEVICE_STATUS_IN_STORE = "13";//库存中
	public final static  String SDN_CM_DEVICE_STATUS_RESERVED = "14";//已预留
	public final static  String SDN_CM_DEVICE_STATUS_ALLOCATED = "15";//已分配
	public final static  String SDN_CM_DEVICE_STATUS_PUT_AWAY = "16";//上架
	public final static  String SDN_CM_DEVICE_STATUS_POWER_ON = "17";//加电
	public final static  String SDN_CM_DEVICE_STATUS_POWER_DOWN = "18";//下电
	//============================= 物理设备状态在字典表中的相关常量-结束  ==========================
	
	//================================= 安全区相关字典常量 -开始   ===============================
		public static final String ADMIN_DIC_TYPE_CODE_SECURE_AREA = "SECURE_AREA";
		
		//广域网区
		public static final String ADMIN_DIC_CODE_SECURE_AREA_CE = "6";
		public static final String ADMIN_DIC_NAME_SECURE_AREA_CE = "广域网区";
		
		//外联区
		public static final String ADMIN_DIC_CODE_SECURE_AREA_ECN_DMZ = "2";
		public static final String ADMIN_DIC_NAME_SECURE_AREA_ECN_DMZ = "外联网DMZ服务区";
		
		//互联区
		public static final String ADMIN_DIC_CODE_SECURE_AREA_INT_DMZ = "1";
		public static final String ADMIN_DIC_NAME_SECURE_AREA_INT_DMZ = "互联网DMZ服务区";
		
		//开放区
		public static final String ADMIN_DIC_CODE_SECURE_AREA_OPN = "3";
		public static final String ADMIN_DIC_NAME_SECURE_AREA_OPN = "开放服务区";
		//================================= 安全区相关字典常量 -结束   ===============================
		
		//================================= IP子网相关字典常量 -开始   ===============================
		//C段地址类型
		public final static String CCLASS_TYPE = "CCLASS_TYPE";
		//安全区域
		public final static String SECURE_AREA = "SECURE_AREA";
		//安全层级
		public final static String SECURE_TIER = "SECURE_TIER";
		// 数据中心地址码
		public final static String DATACENTER_ADDRESS_CODE_YQ = "YQ";// 洋桥
		public final static String DATACENTER_ADDRESS_CODE_WH = "WH";// 南湖
		public final static String DATACENTER_ADDRESS_CODE_BD = "BD";// 稻香湖
		//================================= IP子网相关字典常量 -结束   ===============================
		//================================= 设备相关字典常量 -开始   ===============================
		public static final String AM_OPTIONS_DEVICE_TYPE = "device_type";//设备类型
		public static final String AM_OPTIONS_MODEL = "model";//型号
		public static final String AM_OPTIONS_SWITCH_VENDOR = "switch_vendor";//交换机厂商
		public static final String AM_OPTIONS_FW_VENDOR = "fw_vendor";//防火墙厂商
		public static final String AM_OPTIONS_DNS_VENDOR = "dns_vendor";//域名解析厂商
		public static final String AM_OPTIONS_LB_VENDOR = "lb_vendor";//负载均衡厂商
		//================================= 设备相关字典常量 -结束   ===============================
		
	//================================= IP类型常量 -结束   ===============================
	public static final String MGT_IP_CONST = "22";
}
