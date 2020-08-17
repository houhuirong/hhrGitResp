package com.date.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: hhr
 * @Date: 2020/8/17 - 08 - 17 - 15:33
 * @Description: com.date.utils
 * @version: 1.0
 */
public class isHoliday {
    public static void main(String[] args) {
        try {
            String selectedDate="2020-08-16";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(selectedDate);		//字符串转为日期
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            Integer iMonth = calendar.get(Calendar.MONTH) + 1;
            Integer iDay = calendar.get(Calendar.DAY_OF_MONTH);

            String year = String.valueOf(calendar.get(Calendar.YEAR));
            String month = iMonth > 9 ? String.valueOf(iMonth) : "0" + String.valueOf(iMonth);
            String day = iDay > 9 ? String.valueOf(iDay) : "0" + String.valueOf(iDay);
            System.out.println(year);
            System.out.println(month);
            System.out.println(day);
            //通过年月日查询数据库
        }catch(Exception e) {
            System.out.println("出错啦");
        }
    }
}
