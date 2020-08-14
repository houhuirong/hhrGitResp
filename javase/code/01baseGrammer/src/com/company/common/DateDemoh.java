package com.company.common;

import java.text.ParseException;
import java.util.Date;

/**
 * @author: 马士兵教育
 * @create: 2019-09-07 20:14
 */
public class DateDemoh {
    public static void main(String[] args) throws ParseException {
        Date date = new Date();
        System.out.println(date);
        System.out.println(date.getTime());

    }
}
