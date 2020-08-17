package com.date.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Auther: hhr
 * @Date: 2020/8/17 - 08 - 17 - 15:39
 * @Description: com.date.utils 根据传入的参数，获取月份的总天数
 * @version: 1.0
 */
public class GetDaysOfMonth {
    public static void main(String[] args) {
        try {
            String yearMonth="2020-08-16";
            SimpleDateFormat simpleDateFormat;
            if(yearMonth.split("-").length == 3) {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            }else {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM");
            }

            Date date = simpleDateFormat.parse(yearMonth);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            System.out.println(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        }catch(Exception e) {
            System.out.println(-1);
        }
    }
}
