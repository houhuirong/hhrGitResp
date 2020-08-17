package com.date.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * @Auther: hhr
 * @Date: 2020/8/17 - 08 - 17 - 15:16
 * @Description: com.date.utils
 * @version: 1.0
 */
public class EmpAgeRange {
    public static void main(String[] args) {
        Date date = new Date();  //获取当前时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        long tempDay = 1000*3600*24;
        long tempYear = tempDay*365;

        long zero=date.getTime()/(tempDay)*(tempDay)- TimeZone.getDefault().getRawOffset();//今天零点零分零秒的毫秒数
        date = new Date(zero);

        Date date20year = new Date(date.getTime()-tempYear*20);//20岁
        String date20yearStr = format.format(date20year);

        Date date30year = new Date(date.getTime()-tempYear*30);//30岁
        String date30yearStr = format.format(date30year);

        Date date40year = new Date(date.getTime()-tempYear*40);//40岁
        String date40yearStr = format.format(date40year);

        Date date50year = new Date(date.getTime()-tempYear*50);//50岁
        String date50yearStr = format.format(date50year);

        String[] nameAge = {"20-30岁", "30-40岁", "40-50岁", "50岁以上"};
        String[] startAge = {date30yearStr, date40yearStr, date50yearStr, "0"};
        String[] endAge = {date20yearStr, date30yearStr, date40yearStr, date50yearStr};

        for (int i=0; i<4; i++){
            System.out.println(startAge[i]);
        }
        for (int i = 0; i < 4; i++) {
            System.out.println(endAge[i]);
        }
    }
}
