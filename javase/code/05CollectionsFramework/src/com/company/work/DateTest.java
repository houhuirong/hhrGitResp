package com.hhr.TestWork;

import com.company.work.ExDateUtils;

import java.util.Calendar;
import java.util.Date;

public class DateTest {
	public static void main(String[] args){
		//是否导出全部真实数据（导出当前月份或之后的，需将当前日期之后的考勤放入项目中）
		boolean exportAll = false;
		String yearMonth="2020-06";
		Date today = ExDateUtils.getCurrentDateTime();
		Date yesterday= ExDateUtils.add(today, Calendar.DAY_OF_MONTH, -1);
		//获取昨天时间的年月
		String currentStringyearMonth = ExDateUtils.dateToString(yesterday,"yyyy-MM" );
		//获取昨天时间的年月日
		String currentStringyearMonthDate =   ExDateUtils.dateToString(yesterday,"yyyy-MM-dd" );
		if(currentStringyearMonth.compareTo(yearMonth) != 0){
			exportAll = true;
		}
		String[] monthStr = yearMonth.split("-");
		String year = monthStr[0];
		String month = monthStr[1];
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, Integer.valueOf(year));// 不设置的话默认为当年
		calendar.set(Calendar.MONTH, Integer.valueOf(month) - 1);// 设置月份
		System.out.println(calendar.getTime());
		int daySizeAll = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		String begeinTime = month +"."+ "01";//月初
		String endTime = month +"."+ String.valueOf(daySizeAll);//月末
	}

}
