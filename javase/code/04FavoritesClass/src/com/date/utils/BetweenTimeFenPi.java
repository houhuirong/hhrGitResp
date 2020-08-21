package com.date.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Auther: hhr
 * @Date: 2020/8/21 - 08 - 21 - 10:28
 * @Description: com.date.utils
 * @version: 1.0
 */
public class BetweenTimeFenPi {
    public static void main(String[] args) throws ParseException {
        String startTime = "2020-08-21";
        String endTimeNew = "2020-10-10";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        String nowTime = format.format(new Date());//获取当前系统时间
        Date date1 = format.parse(startTime);
        Date date2 = format.parse(endTimeNew);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        while (cal.getTime().before(date2)) {
            String queryTime = format.format(cal.getTime());//查询时间
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, day);

            String startMonth = queryTime + "-31";//查询月月底
            String endMonth = queryTime + "-01";//查询月月初
            System.out.println(startMonth);
        }
    }
}
