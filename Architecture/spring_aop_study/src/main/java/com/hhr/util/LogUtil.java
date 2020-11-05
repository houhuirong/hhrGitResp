package com.hhr.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Auther: hhr
 * @Date: 2020/10/29 - 10 - 29 - 14:23
 * @Description: com.hhr.util
 * @version: 1.0
 */
/**
 * 通知注解有以下几种类型：
 *
 * @Before:前置通知，在方法执行之前完成
 * @After：后置通知，在方法执行完成之后执行
 * @AfterReturing：返回通知：在返回结果之后运行
 * @AfterThrowing：异常通知：出现异常的时候使用
 * @Around：环绕通知
 *
 * 在方法的参数列表中不要随便添加参数值，会有异常信息
 */
/*@Aspect
@Component
@Order(200)*/
public class LogUtil {
    @Pointcut("execution(public Integer com.hhr.service.MyCalculator.*(Integer,Integer))")
    public void myPointCut(){}
    @Pointcut("execution(* *(..))")
    public void myPointCut1(){

    }

    @Before(value = "myPointCut1()")
    public static void start(JoinPoint joinPoint){
        Signature signature = joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        System.out.println("log---"+signature.getName()+"方法开始执行：参数是："+Arrays.asList(args));
    }
    @AfterReturning(value = "myPointCut()",returning = "result")
    public static void stop(JoinPoint joinPoint,Object result){
        Signature signature = joinPoint.getSignature();
        System.out.println("log---"+signature.getName()+"方法开始执行结束：结果是："+result);
    }
    @AfterThrowing(value = "myPointCut()",throwing = "e")
    public static void logException(JoinPoint joinPoint,Exception e){
        Signature signature = joinPoint.getSignature();
        System.out.println("log---"+signature.getName()+"抛出。。。。异常"+e);
    }
    @After(value = "myPointCut()")
    public static void logFinally(JoinPoint joinPoint){
        Signature signature = joinPoint.getSignature();
        System.out.println("log---"+signature.getName()+"方法执行结束。。。。。。over");

    }
    @Around("myPointCut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Signature signature = pjp.getSignature();
        Object[] args = pjp.getArgs();
        Object result = null;
        try {
            System.out.println("log---环绕通知start："+signature.getName()+"方法开始执行，参数为："+ Arrays.asList(args));
            //通过反射的方式调用目标的方法，相当于执行method.invoke(),可以自己修改结果值
            result = pjp.proceed(args);
//            result=100;
            System.out.println("log---环绕通知stop"+signature.getName()+"方法执行结束");
        } catch (Throwable throwable) {
            System.out.println("log---环绕异常通知："+signature.getName()+"出现异常");
            throw throwable;
        }finally {
            System.out.println("log---环绕返回通知："+signature.getName()+"方法返回结果是："+result);
        }
        return result;
    }
}
