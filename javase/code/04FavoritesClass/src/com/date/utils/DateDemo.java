package com.date.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.*;

/**
 * @author: 马士兵教育
 * @create: 2019-09-07 20:14
 */
public class DateDemo {
    public static void main(String[] args) throws ParseException {
        Date date = new Date();
        System.out.println(date);
        System.out.println(date.getTime());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //将Date类按照规范转换为字符串格式
        String str = dateFormat.format(date);
        System.out.println(str);
        //将字符串转换成对应的日期类
        Date d1 = dateFormat.parse("2010-10-10 20:20:20");
        System.out.println(d1);

        //获取的是当前系统的时间
        Calendar calendar = Calendar.getInstance();
        System.out.println(calendar);
        //设置指定时间的日历类
        calendar.setTime(d1);
        System.out.println(calendar);
        System.out.println(calendar.get(Calendar.YEAR));
        System.out.println(calendar.get(Calendar.MONTH));
        System.out.println(calendar.get(Calendar.DAY_OF_MONTH));
        System.out.println(calendar.get(Calendar.HOUR_OF_DAY));
        System.out.println(calendar.get(Calendar.MINUTE));
        System.out.println(calendar.get(Calendar.SECOND));

        //calendar还可以和date自由转换
        Calendar calendar1=Calendar.getInstance();
        Date date1=calendar1.getTime();
        System.out.println(date1);
        Calendar newCalendar=Calendar.getInstance();
        newCalendar.setTime(date1);
        System.out.println(newCalendar);
        System.out.println(calendar1.get(Calendar.MINUTE));

        Calendar c = Calendar.getInstance();
        //取出年
        System.out.println(c.get(YEAR));
        //取出月份
        System.out.println(c.get(MONTH));
        //取出日
        System.out.println(c.get(DATE));
        //分别设置年、月、日、小时、分钟、秒
        c.set(2003 , 10 , 23 , 12, 32, 23);//2003-11-23 12:32:23
        System.out.println(c.getTime());
        //将Calendar的年前推1年
        c.add(YEAR , -1); //2002-11-23 12:32:23
        System.out.println(c.getTime());
        //将Calendar的月前推8个月
        c.roll(MONTH , -8); //2002-03-23 12:32:23
        System.out.println(c.getTime());

        Calendar call =Calendar.getInstance();
        call.set(2003,7,23,0,0,0);
        System.out.println(call.getTime());
        //超出允许范围会进位
        call.add(MONTH,6);
        System.out.println(call.getTime());

        Calendar cal2=Calendar.getInstance();
        cal2.set(2003,7,31,0,0,0);
        System.out.println(cal2.getTime());
        //如果下一级字段也需要改变，那么该字段会修正到变化最小的值
        cal2.add(MONTH,6);
        System.out.println(cal2.getTime());
        /*
        * roll的上一级不会增加，下一级与add相似，会到最小
        * */
        //设置容错性，Lenient可以自动改变，non-Lenient则会报错
        Calendar cal = Calendar.getInstance();
        cal.set(MONTH , 13); //结果是YEAR字段加1，MONTH字段为1（二月）
        System.out.println(cal.getTime());
        //关闭容错性
       /* cal.setLenient(false);
        cal.set(MONTH , 13); //导致运行时异常
        System.out.println(cal.getTime());*/

        //set延时
        System.out.println("----------------------");
        Calendar cal5 = Calendar.getInstance();
        cal5.set(2003 , 7 , 31);
        cal5.set(MONTH , 8); //理论上应该是是10月1日，但实际上是9月31日（不合法的日期）
        //下面代码输出10月1日
        //System.out.println(cal5.getTime());
        //设置DATE字段为5
        cal5.set(DATE , 5);
        System.out.println(cal5.getTime());
    }
}
