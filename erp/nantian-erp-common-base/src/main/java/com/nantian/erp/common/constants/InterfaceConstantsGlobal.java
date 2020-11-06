package com.nantian.erp.common.constants;

/**
 * 全局常量类，定义公用的接口常量
 * <p>
 * 
 * @author sunyp
 * @version 1.0 2018年4月3日 
 * @see
 */
public class InterfaceConstantsGlobal { 
	//资产接口模块服务器IP地址
	public static final String ASSETS_SERVER_IP = "10.12.65.157";	
	public static final String ASSETS_SERVER_PORT = "8090";
	
	//资产---/assets
	public final static String INF_AM_ASSETS_OPTIONS ="/assets/options";	
	public final static String INF_AM_ASSETS_NETAREAS ="/assets/netareas";	
	public final static String INF_AM_ASSETS_DATACENTERS ="/assets/datacenters";
	public final static String INF_AM_ASSETS_BUILDINGS ="/assets/buildings";
	public final static String INF_AM_ASSETS_ROOMS ="/assets/rooms";
	public final static String INF_AM_ASSETS_CABINETS ="/assets/cabinets";
	public final static String INF_AM_ASSETS_CABINET_U ="/assets/cabinet_u";
	public final static String INF_AM_ASSETS_DEVICECONTRACTS ="/assets/devicecontracts";
	public final static String INF_AM_ASSETS_CONTRACTSECURES ="/assets/contractsecures";
	public final static String INF_AM_ASSETS_DEVICES ="/assets/devices";
	public final static String INF_AM_ASSETS_PASSWDS ="/assets/passwds";
    
	//网络---/
	public final static String INF_NW_SUBNETPOOLS ="/subnetpools";
	public final static String INF_NW_SUBNETS ="/subnets";
	public final static String INF_NW_IPS ="/ips";
}
