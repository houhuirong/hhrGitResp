package com.date.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Auther: hhr
 * @Date: 2020/8/17 - 08 - 17 - 15:42
 * @Description: com.date.utils
 * @version: 1.0
 */
public class getYearAndMonth {
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

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;

            String iMonth = month > 9 ? String.valueOf(month) : "0" + String.valueOf(month);
            System.out.println(year);
            System.out.println(iMonth);
        }catch(Exception e) {
            System.out.println(-1);
        }
    }
}
