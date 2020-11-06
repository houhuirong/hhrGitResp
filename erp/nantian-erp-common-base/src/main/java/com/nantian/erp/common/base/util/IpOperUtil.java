/**
 * 
 */
package com.nantian.erp.common.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * 子网掩码地址工具类
 * @author 71838065.nantian.co
 * 
 */
public class IpOperUtil {

	/**
	 * 子网掩码转换方法（例如：192.168.1-3.0/24转换成192.168.1.1/24、192.168.2.0/24、192.168.3.0/24）
	 * @param paramIp
	 * @return
	 */
	public static List<String> subnetMast2List(String paramIp){
		if (StringUtils.isEmpty(paramIp)) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		// 替换全角逗号
		paramIp = paramIp.replaceAll("，", ",");
		String subnetMask = null;
		String[] subnetMaskArray = paramIp.split(",");
		for (int i = 0; i < subnetMaskArray.length; i++) {
			if (subnetMaskArray[i].indexOf("-") != -1) {
				// 获取掩码，
				subnetMask = subnetMaskArray[i].substring(subnetMaskArray[i].indexOf("/"));
				// 再次根据.分割，如192.168.1-3.0, 分割后数组[192,168,1-3,0]
				String[] tempIpArr = subnetMaskArray[i].substring(0, subnetMaskArray[i].indexOf("/")).split("\\.");
				if (tempIpArr[2].indexOf("-") != -1) {
					String[] numbers = tempIpArr[2].split("-");
					int min = Integer.valueOf(numbers[0]);
					int max = Integer.valueOf(numbers[1]);
					// 循环取值，生成子网掩码地址
					for (; min <= max; min++) {
						StringBuffer sb = new StringBuffer();
						sb.append(tempIpArr[0]);
						sb.append(".");
						sb.append(tempIpArr[1]);
						sb.append(".");
						sb.append(min);
						sb.append(".");
						sb.append(tempIpArr[3]);
						sb.append(subnetMask);
						list.add(sb.toString());
					}
				}
			} else {
				list.add(subnetMaskArray[i]);
			}
		}
		return list;
	}
	
	/**
	 * 获取网关地址
	 * @param ip
	 * @return
	 */
	public static final String getGateWay(String ip) {
		if (StringUtils.isEmpty(ip)) {
			return null;
		}
		if (ip.indexOf("/") == -1) {
			return null;
		}
		String[] arr = ip.split("/");
		String[] ipArr = arr[0].split("\\.");
		if (Integer.valueOf(arr[1]) == 24) {
			return arr[0].substring(0, arr[0].lastIndexOf("\\.")+1)+"254";
		}
		int endIp = 0;
		if (Integer.valueOf(arr[1]) > 24) {
			int num = Integer.parseInt(arr[1]) - 24;
			int ipSeg = (int) (256 / (Math.pow(2, num)));
			int rst = (int) (Integer.parseInt(ipArr[3]) / ipSeg);
			endIp = (1+rst) * ipSeg -2;
			return ipArr[0]+"."+ipArr[1]+"."+ipArr[2]+"."+endIp+"/"+arr[1];
		}
		return null;
	}

	/**
	 * 校验是否为掩码．
	 * @param mask
	 * @return
	 */
	public static boolean isMask(String mask){  
		Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$");
		return pattern.matcher(mask).matches();  
	}
	
	/**
	 * 掩码转换（如24转换成255.255.255.0）
	 * @param mask
	 * @return
	 */
	public static String mask2ipMask(String mask) {
		String ipMask = null;  
		if (!isMask(mask)) {
			return null;
		} else {
			if (mask.contains(".")) {  
				ipMask = mask;
			} else {
				int inetMask = Integer.valueOf(mask);  
				int part = inetMask / 8;  
				int remainder = inetMask % 8;  
				int sum = 0;  
				for (int i = 8; i > 8 - remainder; i--) {  
					sum = sum + (int) Math.pow(2, i - 1); 
				}
				if (part == 0) { 
					ipMask = sum + ".0.0.0"; 
				} else if (part == 1) {  
					ipMask = "255." + sum + ".0.0"; 
				} else if (part == 2) {  
					ipMask = "255.255." + sum + ".0";  
				} else if (part == 3) {  
					ipMask = "255.255.255." + sum;
				}  else if (part == 4) {  
					ipMask = "255.255.255.255";  
				}
			}
			return ipMask;
		}
	}
	
	/**
	 * @param cclass
	 * @return
	 * 获取一个IP地址的前3位（a、b、c段）
	 */
	public static String getAbcclass(String cclass) {
		String[] cclassArray = cclass.split("\\.");
		StringBuffer abcclassIp = new StringBuffer();
		for(int i=0;i<cclassArray.length-1;i++) {
			abcclassIp.append(cclassArray[i]);
			abcclassIp.append(".");
		}
		String abcclass = abcclassIp.toString();
		return abcclass;
	}
	
}
