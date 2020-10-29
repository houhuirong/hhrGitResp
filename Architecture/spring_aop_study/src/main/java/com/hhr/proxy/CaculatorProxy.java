package com.hhr.proxy;

import com.hhr.service.Calculator;
import com.hhr.util.LogUtil;

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

                //开始调用被代理类的方法
                Object result=null;
               try{
                   LogUtil.start(method,args);
                   result=method.invoke(calculator,args);
                   LogUtil.stop(method,result);
               }catch(Exception e){
                   LogUtil.logException(method,e);
                   e.printStackTrace();
               }finally {
                   LogUtil.logFinally(method);
               }


                return result;
            }
        };
        Object o = Proxy.newProxyInstance(loader, interfaces, handler);
        return (Calculator)o;
    }
}
