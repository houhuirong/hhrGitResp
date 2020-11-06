package com.nantian.erp.common.constants;

/**
 * 全局常量类，定义公用的常量
 * <p>
 * 
 * @author nantian
 * @version 1.0 2018年3月1日 
 * @see
 */
public class GlobalConstants {
	
	
	//是否有效标识
	public static String IS_ACTIVE_Y = "Y";
	public static String IS_ACTIVE_N = "N";
  
	//CM_CI_TYPE表中设备类型编码
	public final static String CM_CI_TYPE_SW_TYPE_SEQNO ="switch";	
	public final static String CM_CI_TYPE_FW_TYPE_SEQNO ="001010203";
	public final static String CM_CI_TYPE_SLB_TYPE_SEQNO ="001010205";
    
	//SDN用到表的SEQ名称定义(IOMP_SEQ 改成 SDN_SEQ)
	public final static String SEQ_NAME_IOMP_SEQ ="sdn_common.sdn_seq";//iomp7001.IOMP_SEQ
	//sdn系统在工单表的operateType值
	public static final String NET_DEVICE_SERVICE_APPLY = "netDeviceServiceApply";
	
	//资产接口模块服务器IP地址
	public static final String ASSETS_SERVER_IP = "128.240.235.112";
	
	public static final String ASSETS_SERVER_PORT = "9090";
	
	// 设备类型
	public final static String DEVICE_FW_TYPE = "fw";
	public final static String DEVICE_SW_TYPE = "switch";
	public final static String DEVICE_LB_TYPE = "lb";
}
