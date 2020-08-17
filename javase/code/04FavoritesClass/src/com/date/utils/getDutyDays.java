package com.date.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @Auther: hhr
 * @Date: 2020/8/17 - 08 - 17 - 16:03
 * @Description: com.date.utils 计算两个日期之间除去周末后的工作时间
 * @version: 1.0
 */
public class getDutyDays {
    public static void main(String[] args) {
        int result = 0;
        Date startDate=null;
        Date endDate=null;
        try {
            Calendar calendar = Calendar.getInstance();
            Date flag = startDate;
            //循环两个日期之间的所有日期
            while (flag.compareTo(endDate) <= 0) {
                calendar.setTime(flag);
                int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                if(week == 0 || week == 6){//0为周日，6为周六
                    //跳出循环进入下一个日期
                    calendar.add(Calendar.DAY_OF_MONTH, +1);
                    flag = calendar.getTime();
                    continue;
                }
                result += 1;
                calendar.add(Calendar.DAY_OF_MONTH, +1);
                flag = calendar.getTime();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }
}
