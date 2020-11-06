package com.nantian.erp.common.base.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nantian.erp.common.base.exception.BizException;
import com.nantian.erp.common.constants.PubConstants;

import java.math.BigDecimal;
import java.sql.Clob;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理类
 * @author 32399893
 *
 */
public class StringUtil {

	private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);
	
	
	/**
     * 按照字节来截取字符串
     * @param str
     * @param len
     * @return
     */
    public static String subStringByByte(String str,int len){
		String a1 = "";
		if(str != null){
			byte[] a = null;
			try {
				a = str.getBytes(PubConstants.JAVA_ENCODE);
			} catch (UnsupportedEncodingException e) {
				logger.error("截取字符串异常,str:"+str,e);
				throw new BizException("截取字符串异常:",e);
			}
			if(len > a.length){//如果传入参数小于字符长度则返回原字符
				return str;
			}
			
			byte[] st = new byte[len];
			System.arraycopy(a, 0, st, 0, len);
			
			try {
				a1 = new String(st,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("截取字符串异常,str:"+str,e);
				throw new BizException("截取字符串异常:",e);
			}
			/**
			 * 处理最后一个字符为中文，截取后出现乱码的问题
			 */
			int length = a1.length();
			if(str.charAt(length-1) != a1.charAt(length - 1)){//比较最后一个字符是否相等，如果不相等那说明在a1中最后一个是乱码
				if(length < 2){	//如果只有一个乱码的情况，就给a1赋""(比如截取长度为2，但是第一个字是中文，那么这里length就等于1(因为乱码占一个字节))
					a1 = "";
				}else{
					a1 = a1.substring(0, length-1);
				}
			}
			
		}
		return a1;
	}
	
    /**
     * 计算字符串长度,是否超长
     * true 表示正常
     * false表示超长
     * @param str
     * @param len
     * @return
     */
    public static boolean checkStringByByte(String str,int len){
    	boolean tn = true;
		if(str != null){
			byte[] a = null;
			try {
				a = str.getBytes(PubConstants.JAVA_ENCODE);
			} catch (UnsupportedEncodingException e) {
				logger.error("截取字符串异常,str:"+str,e);
				tn = false;
			}
			if(len < a.length){
				tn = false;
			}
		}else{
			tn = false;
		}
		return tn;
	}
    
    /**
     * 判断传入字符串是否为正整数
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){ 
       if(StringUtils.isBlank(str)){
    	   return false;
       }
	   Pattern pattern = Pattern.compile("[0-9]*"); 
	   Matcher isNum = pattern.matcher(str);
	   if( !isNum.matches() ){
	       return false; 
	   } 
	   return true; 
   }

	/**
	 * 判断是否为整数小数负数
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str) {
		Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
		String bigStr;
		try {
			bigStr = new BigDecimal(str).toString();
		} catch (Exception e) {
			return false;//异常 说明包含非数字。
		}
		Matcher isNum = pattern.matcher(bigStr); // matcher是全匹配
		if (!isNum.matches()) {
			return false;
		}
		return true;
    }
    
 
    /**
	 * 获取23位随机数
	 * @return
	 */
    public static String getDateStr(){
		SimpleDateFormat fm = new SimpleDateFormat("YYYYMMDDHHmmssSSS");
		String dateStr = fm.format(new Date());
		return dateStr + getFixLenthString(6);
	}
	
    /* 
	 * 返回长度为【strLength】的随机数，在前面补0 
	 */  
    public static String getFixLenthString(int strLength) {  
	    Random rm = new Random();  
	    // 获得随机数  
	    double pross = (1 + rm.nextDouble()) * Math.pow(10, strLength);  
	    // 将获得的获得随机数转化为字符串  
	    String fixLenthString = String.valueOf(pross);  
	    // 返回固定的长度的随机数  
	    return fixLenthString.substring(1, strLength + 1);  
	}
    
    /**
	 * clob的数据转化成String类型
	 * @param clob
	 * @return
	 */
	public static String clobToString(Clob clob) {
		if (null == clob) {
			return "";
		}
		Reader is = null;
		try {
			is = clob.getCharacterStream();
			BufferedReader br = new BufferedReader(is);
			String s = br.readLine();
			StringBuffer sb = new StringBuffer();
			while (s != null) {
				sb.append(s);
				s = br.readLine();
			}
			return sb.toString();
		} catch (Exception e) {
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
		}
		return "";
	}
	
    /**
	 * Object的数据转化成String类型
	 * @param clob
	 * @return
	 */
	public static String objectToString(Object object) {
		return object == null ? "" : String.valueOf(object);
	}
}
