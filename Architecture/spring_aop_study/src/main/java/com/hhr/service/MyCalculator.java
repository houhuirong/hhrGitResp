package com.hhr.service;

import com.hhr.util.LogUtil;

import java.lang.reflect.Method;

/**
 * @Auther: hhr
 * @Date: 2020/10/29 - 10 - 29 - 14:06
 * @Description: com.hhr.service
 * @version: 1.0
 */
public class MyCalculator implements Calculator {
    public Integer add(Integer i, Integer j) throws NoSuchMethodException {
//        Method add = MyCalculator.class.getMethod("add", Integer.class, Integer.class);
//        LogUtil.start(add,i,j);
        Integer result=i+j;
//        LogUtil.stop(add,result);
        return result;
    }

    public Integer sub(Integer i, Integer j) throws NoSuchMethodException {
      /*  Method sub = MyCalculator.class.getMethod("sub", Integer.class, Integer.class);
        LogUtil.start(sub,i,j);*/
        Integer result=i-j;
//        LogUtil.stop(sub,result);

        return result;
    }

    public Integer mul(Integer i, Integer j) throws NoSuchMethodException {
//        Method mul = MyCalculator.class.getMethod("mul", Integer.class, Integer.class);
//        LogUtil.start(mul,i,j);
        Integer result=i*j;
//        LogUtil.stop(mul,result);

        return result;
    }

    public Integer div(Integer i, Integer j) throws NoSuchMethodException {
//        Method div = MyCalculator.class.getMethod("div", Integer.class, Integer.class);
//        LogUtil.start(div,i,j);
        Integer result=i/j;
//        LogUtil.stop(div,result);
        return result;
    }
}
