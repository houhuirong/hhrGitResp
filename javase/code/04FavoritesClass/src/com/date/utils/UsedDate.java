package com.date.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *java中Date日期类型的大小比较
 * 1、可以直接调用Date的compareTo()方法来比较大小
 *compareTo()方法的返回值，date1小于date2返回-1，date1大于date2返回1，相等返回0
 * 2、通过Date自带的before()或者after()方法比较
 * 3、通过调用Date的getTime()方法获取到毫秒数来进行比较
 *
 */
public class UsedDate {
    public static void main(String[] args) {
        String beginTime = "2018-07-28 14:42:32";
        String endTime = "2018-07-29 12:26:32";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date date1 = format.parse(beginTime);
            Date date2 = format.parse(endTime);
            //1、
           // int compareTo = date1.compareTo(date2); 1、
           // System.out.println(compareTo);
            //2、
//            boolean before = date1.before(date2);
//            System.out.println(before);
            //3、
            long beginMillisecond = date1.getTime();
            long endMillisecond = date2.getTime();

            System.out.println(beginMillisecond > endMillisecond);
            /*
            * // 文件的路径
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;月从0开始，
		int day = calendar.get(Calendar.DATE);
            *
            * */

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
