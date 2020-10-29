package com.hhr.util;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Auther: hhr
 * @Date: 2020/10/29 - 10 - 29 - 14:23
 * @Description: com.hhr.util
 * @version: 1.0
 */
public class LogUtil {
    public static void start(Method method,Object...objects){
        System.out.println(method.getName()+"方法开始执行：参数是："+ Arrays.asList(objects));
    }
    public static void stop(Method method,Object...objects){
        System.out.println(method.getName()+"方法开始执行结束：结果是："+ Arrays.asList(objects));
    }
    public static void logException(Method method,Exception e){
        System.out.println(method.getName()+"抛出。。。。异常"+e.getMessage());
    }
    public static void logFinally(Method method){
        System.out.println(method.getName()+"方法执行结束。。。。。。over");

    }
}
