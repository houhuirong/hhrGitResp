/**
 * Copyright (c) 2013, China Construction Bank Co., Ltd. All rights reserved.
 * 南天软件版权所有.
 *
 * 审核人：
 */
package com.company.work;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;



/**
 * 
 * 日期处理工具类
 * <p>
 * 
 * @author nantian.co
 * @version 1.0 2013-3-19
 * @see
 */
public class ExDateUtils {

	/* 日期格式 yyyy-MM-dd */
	public static final String PATTREN_DATE = "yyyy-MM-dd";

	/* 时间格式 HH:mm:ss */
	public static final String PATTREN_TIME = "HH:mm:ss";

	/* 日期时间格式类 yyyy-MM-dd HH:mm:ss */
	public static final String PATTREN_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

	/* 日期时间格式类 yyyy/MM/dd HH:mm:ss */
	public static final String PATTREN_DATE_TIME_EN = "yyyy/MM/dd HH:mm:ss";
	
	/* 日期时间格式类 yyyy-MM-dd HH */
	public static final String PATTREN_YYYYMMDDHH = "yyyy-MM-dd HH";

	/* 日期时间格式类 yyyy/MM/dd HH */
	public static final String PATTREN_YYYYMMDDHH_EN = "yyyy/MM/dd HH";

	/**
	 * 返回yyyy-MM-dd HH:mm:ss格式的字符串
	 * 
	 * @return
	 */
	public static String getCurrentStringDateTime() {

		return new SimpleDateFormat(PATTREN_DATE_TIME, java.util.Locale.CHINA)
				.format(getCurrentDateTime());
	}

	/**
	 * @return 取当前日期时间 [Date类型]
	 */
	public static Date getCurrentDateTime() {
		return Calendar.getInstance().getTime();
	}

	/**
	 * @return 取当前日期时间长整数 [long]毫秒表示的时间
	 */
	public static long getCurrentLongDateTime() {
		return Calendar.getInstance().getTimeInMillis();
	}



	/**
	 * Calendar 转 Date
	 * 
	 * @param cldDate
	 *            Calendar类型的日期
	 * @return Date类型日期
	 */
	public static Date calendarToDate(Calendar cldDate) {
		return cldDate.getTime();
	}

	/**
	 * Calendar 转 long (毫秒)
	 * 
	 * @param cldDate
	 *            Calendar类型的日期
	 * @return 毫秒数
	 */
	public static long calendarToMilisNum(Calendar cldDate) {
		return cldDate.getTimeInMillis();
	}

	/**
	 * Calendar 转 long (秒)
	 * 
	 * @param cldDate
	 *            Calendar类型的日期
	 * @return 秒数
	 */
	public static long calendarToSecondNum(Calendar cldDate) {
		return cldDate.getTimeInMillis() / 1000;
	}

	/**
	 * long(毫秒) 转 Calendar
	 * 
	 * @param milisNum
	 *            以毫秒表示的时间
	 * @return Calendar日期
	 */
	public static Calendar milisNumToCalendar(long milisNum) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(milisNum);
		return ca;
	}

	/**
	 * long(秒) 转 Calendar
	 * 
	 * @param secNum
	 *            以秒表示的时间
	 * @return Calendar日期
	 */
	public static Calendar secondNumToCalendar(long secNum) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(secNum * 1000);
		return ca;
	}

	/**
	 * Date 转 Calendar
	 * 
	 * @param dt
	 *            Date类型日期
	 * @return Calendar日期
	 */
	public static Calendar dateToCalendar(Date dt) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(dt);
		return ca;
	}

	/**
	 * Date 转 long (毫秒)
	 * 
	 * @param dt
	 *            Date类型日期
	 * @return long毫秒数
	 */
	public static long dateToMilisNum(Date dt) {
		return dt.getTime();
		// 或者 retrun System.currentTimeMillis();
	}

	/**
	 * Date 转 long (秒)
	 * 
	 * @param dt
	 *            Date类型日期
	 * @return long秒数
	 */
	public static long dateToSecondNum(Date dt) {
		return dt.getTime() / 1000;
	}

	/**
	 * long(秒) 转 Date
	 * 
	 * @param secNum
	 *            long秒数
	 * @return Date类型日期
	 */
	public static Date secondNumToDate(long secNum) {
		Date d = new Date();
		d.setTime(secNum * 1000);
		// 或： d = new Date(secNum * 1000);
		return d;
	}

	/**
	 * long(毫秒) 转 Date
	 * 
	 * @param secNum
	 *            long毫秒数
	 * @return Date类型日期
	 */
	public static Date milisNumToDate(long millisNum) {
		Date d = new Date();
		d.setTime(millisNum);
		// 或： d = new Date(millisNum);
		return d;
	}

	/**
	 * 将字符串转换为日期（不包括时间）
	 * 
	 * @param dateString
	 *            "yyyy-MM-dd"格式的日期字符串
	 * @return 日期
	 */
	public static Date convertToDate(String dateString) throws ParseException{
			return new SimpleDateFormat(PATTREN_DATE, java.util.Locale.CHINA)
					.parse(dateString);
	}

	/**
	 * 检查字符串是否为日期格式yyyy-MM-dd
	 * 
	 * @param dateString
	 * @return true=是；false=否
	 */
	public static boolean checkDateString(String dateString) throws ParseException{
		return (convertToDate(dateString) != null);
	}

	/**
	 * 将字符串转换为日期（包括时间）
	 * 
	 * @param dateString
	 *            "yyyy-MM-dd HH:mm:ss"格式的日期字符串
	 * @return 日期时间
	 */
	public static Date convertToDateTime(String dateTimeString) throws ParseException{
			return new SimpleDateFormat(PATTREN_DATE_TIME,
					java.util.Locale.CHINA).parse(dateTimeString);
	}

	/**
	 * 检查字符串是否为日期时间格式yyyy-MM-dd HH:mm:ss
	 * 
	 * @param dateString
	 * @return true=是；false=否
	 */
	public static boolean checkDateTimeString(String dateTimeString) throws ParseException{
		return (convertToDateTime(dateTimeString) != null);
	}

	/**
	 * 获取月底
	 * 
	 * @param year
	 *            年 4位年度
	 * @param month
	 *            月 1~12
	 * @return 月底的Date对象。例如：2006-3-31 23:59:59.999
	 */
	public static Date getMonthEnd(int year, int month) throws ParseException{
		StringBuffer sb = new StringBuffer(10);
		Date date;
		if (month < 12) {
			sb.append(Integer.toString(year));
			sb.append("-");
			sb.append(month + 1);
			sb.append("-1");
			date = convertToDate(sb.toString());
		} else {
			sb.append(Integer.toString(year + 1));
			sb.append("-1-1");
			date = convertToDate(sb.toString());
		}
		date.setTime(date.getTime() - 1);
		return date;
	}



	/**
	 * 从Date 类型 根据模式 转换成 String 类型
	 * 
	 * @param dt
	 * @param format
	 * @return
	 */
	public static String dateToString(Date dt, String format) {
		if (null == dt)
			return "";
		SimpleDateFormat tempSim = new SimpleDateFormat(format);
		return tempSim.format(dt);
	}

	public static String getTodayString() {
		SimpleDateFormat tempSim = new SimpleDateFormat("yyyy-MM-dd");
		return tempSim.format(new Date());
	}


	/**
	 * 日期加法
	 * 
	 * @param when
	 *            被计算的日期
	 * @param field
	 *            the time field. 在Calendar中定义的常数，例如Calendar.DATE
	 * @param amount
	 *            加数
	 * @return 计算后的日期
	 */
	public static Date add(Date when, int field, int amount) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(when);
		calendar.add(field, amount);
		return calendar.getTime();
	}

	/**
	 * 计算给定的日期加上给定的天数。
	 * 
	 * @param when
	 *            给定的日期
	 * @param amount
	 *            给定的天数
	 * @return 计算后的日期
	 */
	public static Date addDays(Date when, int amount) {
		return add(when, Calendar.DAY_OF_YEAR, amount);
	}

	/**
	 * 计算给定的日期加上给定的月数。
	 * 
	 * @param when
	 *            给定的日期
	 * @param amount
	 *            给定的月数
	 * @return 计算后的日期
	 */
	public static Date addMonths(Date when, int amount) {
		return add(when, Calendar.MONTH, amount);
	}

	/***
	 * 取得指定日期的同一周内日期
	 * 
	 * @param Date
	 *            日期字符串 格式: yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd
	 * @param dateValue
	 *            要显示的日期(周一到周日)
	 * @param flag
	 *            标志,1-向前查找日期 2-向后查找日期
	 * @return 指定周几的日期字符串 <code>
	 *              getWeekDay("2009-5-25", Calendar.MONDAY,1) 返回: 2009-05-25 .
	 *              getWeekDay("2009-5-25", Calendar.SUNDAY,2) 返回: 2009-05-31 .
	 * <code>
	 * @throws ParseException
	 */
	public static String getWeekDay(String Date, int dateValue, int flag)
			throws ParseException {
		Calendar calObj = Calendar.getInstance();
		SimpleDateFormat sfObj = new SimpleDateFormat("yyyy-MM-dd");
		calObj.setTime(sfObj.parse(Date));
		if (dateValue != Calendar.SATURDAY) {
			if (flag == 1) {// 周一
				calObj.setFirstDayOfWeek(dateValue);
			} else { // 周日
				calObj.setFirstDayOfWeek(dateValue + 1);
			}
		}
		calObj.set(Calendar.DAY_OF_WEEK, dateValue);
		return sfObj.format(calObj.getTime());
	}

	/**
	 * 查找指定日期的月初和月未日期
	 * 
	 * @param date
	 *            格式: yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd
	 * @param flag
	 *            1:月初(1号),2:月未 <code>
	 *  getMonth("2009-02-25",1); 返回 2009-02-01
	 *  getMonth("2009-02-25",2); 返回 2009-02-28
	 * </code>
	 * @return
	 * @throws Exception
	 */
	public static Date getMonth(String date, int flag) throws Exception {
		Calendar ca = Calendar.getInstance();
		SimpleDateFormat sfObj = new SimpleDateFormat("yyyy-MM-dd");
		ca.setTime(sfObj.parse(date)); // someDate 为你要获取的那个月的时间
		Date rtDate = null;
		if (flag == 1) {// 月初
			ca.set(Calendar.DAY_OF_MONTH, 1);
			rtDate = ca.getTime();
		} else { // 月底
			ca.set(Calendar.DAY_OF_MONTH, 1);
			rtDate = ca.getTime();
			ca.add(Calendar.MONTH, 1);
			ca.add(Calendar.DAY_OF_MONTH, -1);
			rtDate = ca.getTime();
		}
		return rtDate;
	}

	/**
	 * @主要功能：查找指定日期的月初和月未日期
	 * @param date
	 *            格式: yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd
	 * @param flag
	 * @return
	 * @throws Exception
	 */
	public static String getStrMonth(String date, int flag) throws Exception {
		SimpleDateFormat sformatObj = new SimpleDateFormat("yyyy-MM-dd");
		Date returnStr = getMonth(date, flag);
		return sformatObj.format(returnStr);
	}

	/***
	 * 
	 * @主要功能：计算两个日期之间的分钟数
	 * @author: Snoopy Chen (ctfzh@yahoo.com.cn)
	 * @since： Jun 12, 2009
	 * @param date1
	 *            日期1 格式: yyyy-MM-dd HH:mm:ss 要求比日期2大
	 * @param date2
	 *            日期2 格式: yyyy-MM-dd HH:mm:ss
	 * @return 分钟差,如: 2009-06-12 09:30:00 与 2009-06-12 08:00:00 结果为90
	 * @throws Exception
	 */
	public static int calMinutes(Date date1, Date date2) throws Exception {
		int times = 0;
		times = (int) ((date1.getTime() - date2.getTime()) / (60 * 1000));
		return times;
	}

	/***
	 * 
	 * @主要功能：将指定的日期加减n天数
	 * @author: Snoopy Chen (ctfzh@yahoo.com.cn)
	 * @since： Jun 12, 2009
	 * @param date
	 * @param amount
	 *            要增加或者减少的天数 <code>
	 *              calDay(2009-06-12,2)    返回: 2009-06-14 .(例子中没有把日期参数的时间写上)
	 *              calDay(2009-06-12,-3)    返回: 2009-06-09 .(例子中没有把日期参数的时间写上)
	 *   <code>
	 * @return
	 * @throws Exception
	 */
	public static Date calDay(Date date, int amount) throws Exception {
		Calendar tempCalen = Calendar.getInstance();
		tempCalen.setTime(date);
		tempCalen.add(Calendar.DATE, amount);
		return tempCalen.getTime();
	}

	/**
	 * 解析时间间隔，并计算好相加后的时间
	 * 
	 * @param date
	 * @param additStr
	 * @return
	 */
	public static Date dateAddition(Date date, String additStr) {
		Date reDate = null;
		String[] strs = additStr.split("\\:");
		if ("m".equals(strs[0].toString())) {// 分钟
			reDate = ExDateUtils.add(date, Calendar.MINUTE,
					Integer.parseInt(strs[1].toString()));
		} else if ("h".equals(strs[0].toString())) {// 小时
			reDate = ExDateUtils.add(date, Calendar.HOUR_OF_DAY,
					Integer.parseInt(strs[1].toString()));
		} else if ("d".equals(strs[0].toString())) {// 天
			reDate = ExDateUtils.add(date, Calendar.DAY_OF_MONTH,
					Integer.parseInt(strs[1].toString()));
		}
		return reDate;
	}

	/**
	 * 获取某个时间段内星期几的日期点列表
	 * 
	 * @author cp
	 * @since 2009-6-24 上午11:21:05
	 * @param startDate
	 * @param endDate
	 * @param day
	 * @return
	 */
	public static List<Date> getDatesOnWeek(Date startDate, Date endDate,
			int day) {
		List<Date> dates = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);
		cal.setTime(startDate);
		int startday = cal.get(Calendar.DAY_OF_WEEK);
		// 如果两个都不为0时，并开始时间的星期小于day时，得向前移一个星期才有day
		if (startday != 0 && day != 0 && startday > day) {
			cal.add(Calendar.WEEK_OF_YEAR, 1);
		}
		cal.set(Calendar.DAY_OF_WEEK, day);
		while (cal.compareTo(endCal) <= 0) {
			dates.add(cal.getTime());
			cal.add(Calendar.WEEK_OF_YEAR, 1);
		}
		return dates;
	}

	/**
	 * 获取每隔两周某个时间段内星期几的日期点列表
	 * 
	 * @author cp
	 * @since 2009-6-24 下午02:16:56
	 * @param startDate
	 * @param endDate
	 * @param day
	 * @return
	 */
	public static List<Date> getDatesOnDoubleWeek(Date startDate, Date endDate,
			int day) {
		List<Date> dates = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);
		cal.setTime(startDate);
		int startday = cal.get(Calendar.DAY_OF_WEEK);
		// 如果两个都不为0时，并开始时间的星期小于day时，得向前移一个星期才有day
		if (startday != 0 && day != 0 && startday > day) {
			cal.add(Calendar.WEEK_OF_YEAR, 1);
		}
		cal.set(Calendar.DAY_OF_WEEK, day);
		while (cal.compareTo(endCal) <= 0) {
			dates.add(cal.getTime());
			cal.add(Calendar.WEEK_OF_YEAR, 2);
		}
		return dates;
	}

	/**
	 * 获取每月周某个时间段内几号的日期点列表
	 * 
	 * @author cp
	 * @since 2009-6-24 下午02:22:49
	 * @param startDate
	 * @param endDate
	 * @param day
	 * @return
	 */
	public static List<Date> getDatesOnMonth(Date startDate, Date endDate,
			int date) {
		List<Date> dates = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);
		cal.setTime(startDate);
		int startdate = cal.get(Calendar.DATE);
		// 如果开始的日期大于参数date，即往前移一个月
		if (startdate > date) {
			cal.add(Calendar.MONTH, 1);
		}
		cal.set(Calendar.DATE, date);
		while (cal.compareTo(endCal) <= 0) {
			dates.add(cal.getTime());
			cal.add(Calendar.MONTH, 1);
		}
		return dates;
	}

	/**
	 * 根据某种格式，把字符串转成日期类型
	 * 
	 * @author cp
	 * @since 2009-6-24 下午02:38:49
	 * @param date
	 * @param pattern
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String dateStr, String pattern)
			throws ParseException {
		DateFormat df = new SimpleDateFormat(pattern);
		return df.parse(dateStr);
	}

	/**
	 * 根据日期样式,格式化日期类
	 * 
	 * @param src
	 *            要格式化的日期对象
	 * @param pattern
	 *            日期格式，如“yyyy-MM-dd”,"yyyy-MM-dd HH:mm:ss"
	 * @return
	 */
	public static synchronized Date formatDate(Date src, final String pattern) throws ParseException {
		DateFormat df = new SimpleDateFormat(pattern);
		String dateStr = df.format(src);
		Date date = null;
		date = df.parse(dateStr);
		return date;
	}

	/**
	 * java里面星期几的值表示为：日：1,一：2,....六：0. 而javascript里面星期几的值表示为：日：0,一：1,....六：6
	 * 改办法是把星期几的值表示为：日：0,一：1,....六：6转成java的表示形式
	 * 
	 * @param day
	 * @return
	 */
	public static int getJavaDay(int day) {
		if (day == 6)
			return 0;
		else
			return day + 1;
	}

	/**
	 * 转换某天为星期几
	 * 
	 * @param day
	 * @return
	 */
	public static String convertDay2Week(Date day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		int iday = cal.get(Calendar.DAY_OF_WEEK);
		String dayStr = "";
		if (iday == 1) {
			dayStr = "星期日";
		} else if (iday == 2) {
			dayStr = "星期一";
		} else if (iday == 3) {
			dayStr = "星期二";
		} else if (iday == 4) {
			dayStr = "星期三";
		} else if (iday == 5) {
			dayStr = "星期四";
		} else if (iday == 6) {
			dayStr = "星期五";
		} else if (iday == 7) {
			dayStr = "星期六";
		}
		return dayStr;

	}

	/**
	 * 计算两天的天数
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 * @throws ParseException
	 */
	public static long getDateDiffNum(String d1, String d2)
			throws ParseException {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTREN_DATE,
				java.util.Locale.CHINA);
		Long startM = simpleDateFormat.parse(d1).getTime();
		Long endM = simpleDateFormat.parse(d2).getTime();
		long result = (endM - startM) / (24 * 60 * 60 * 1000);
		return result;
	}

}
