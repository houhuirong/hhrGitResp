package com.company.work;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestM {

	public static void main(String[] args) throws Exception {
		// 获取当月第一天和现在月份
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date nowMonth=ExDateUtils.getCurrentDateTime();//系统时间
		Date monthStart=ExDateUtils.getMonth(format.format(nowMonth),1);//给定月第一天第一刻
		//起始与结束工作日最多相隔7天
		int num=nowMonth.getDate()/7;
		Date dateTo=null;
		//获取月度打卡和休假记录
		for(int i=0;i<num;i++){
			dateTo=ExDateUtils.addDays(monthStart, 7);
			if(i==num-1){
				dateTo=nowMonth;
			}
			String workDateFrom=ExDateUtils.dateToString(monthStart,"yyyy-MM-dd HH:mm:ss");
			String workDateTo=ExDateUtils.dateToString(dateTo,"yyyy-MM-dd HH:mm:ss");
			monthStart=ExDateUtils.addDays(monthStart, 8);
		}
		if(num==0){
			String workDateFrom=ExDateUtils.dateToString(monthStart,"yyyy-MM-dd HH:mm:ss");
			String workDateTo=ExDateUtils.dateToString(dateTo,"yyyy-MM-dd HH:mm:ss");
		}
	}

}
