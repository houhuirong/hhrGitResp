package com.hhr.proxy;

import com.hhr.service.Calculator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * @Auther: hhr
 * @Date: 2020/10/29 - 10 - 29 - 15:10
 * @Description: com.hhr.proxy
 * @version: 1.0
 */
public class CaculatorProxy {
    public static Calculator getCalculator(final Calculator calculator){
        //获取被代理对象的类加载器
        ClassLoader loader=calculator.getClass().getClassLoader();
        //被代理对象的所有接口
        Class<?>[] interfaces=calculator.getClass().getInterfaces();
        //用来执行被代理类需要执行的方法
        InvocationHandler handler=new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println(method.getName()+"方法开始执行"+"参数是:"+ Arrays.asList(args));
                //开始调用被代理类的方法
                Object result=method.invoke(calculator,args);
                System.out.println(method.getName()+"方法开始结束"+"结果是:"+ Arrays.asList(result));
                return result;
            }
        };
        Object o = Proxy.newProxyInstance(loader, interfaces, handler);
        return (Calculator)o;
    }
}
