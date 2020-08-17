package com.date.utils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Auther: hhr
 * @Date: 2020/8/17 - 08 - 17 - 15:01
 * @Description: com.date.utils
 * @version: 1.0
 */
public class CompanyAgeStatic {
    public static void main(String[] args) {
        //司龄维度的数据，按照入职时间统计，0-3年，3-5年，5-10年，10年以上
        Date date = new Date();  //获取当前时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateNowStr = format.format(date);
        long tempDay = 1000*3600*24;
        long tempYear = tempDay*365;

        long zero=date.getTime()/(tempDay)*(tempDay)- TimeZone.getDefault().getRawOffset();//今天零点零分零秒的毫秒数
        date = new Date(zero);

        Date date3year = new Date(date.getTime()-tempYear*3);//3年
        String date3yearStr = format.format(date3year);

        Date date5year = new Date(date.getTime()-tempYear*5);//5年
        String date5yearStr = format.format(date5year);

        Date date10year = new Date(date.getTime()-tempYear*10);//10年
        String date10yearStr = format.format(date10year);

        String[] name = {"0-3年", "3-5年", "5-10年", "10年以上"};
        String[] startDate = {date3yearStr, date5yearStr, date10yearStr, "0"};
        String[] endDate = {dateNowStr, date3yearStr, date5yearStr, date10yearStr};
        for (int i=0; i<4; i++){
            System.out.println(startDate[i]);

        }
        System.out.println("00000");
        for (int i = 0; i < 4; i++) {
            System.out.println(endDate[i]);
        }
    }
}
