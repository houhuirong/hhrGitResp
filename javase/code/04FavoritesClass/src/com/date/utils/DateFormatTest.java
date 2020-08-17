package com.date.utils;


import java.text.DateFormat;
import java.text.ParseException;

/**
*setLenient(boolean lenient),
 * 默认采用不严格的日期语法，参数为true，
 */
public class DateFormatTest {
    public static void main(String[] args) throws ParseException {
        String str1="2014-12-12";
        String str2="2014年12月10日";
        System.out.println(DateFormat.getDateInstance().parse(str1));
        System.out.println(DateFormat.getDateInstance(DateFormat.LONG).parse(str2));
       // System.out.println(DateFormat.getDateInstance().parse(str2));
    }



}
