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
/**
 * 必须要有接口，如果没有接口，不能使用，这种方式是用jdk提供的reflect包下的类
 * 但是在生产环境中我不能保证每个类都有实现的接口，所以有第二种方式cglib
 * cglib在实现的时候有没有接口都无所谓
 */
public class CaculatorProxy {
    public static Object getProxy(final Calculator object){
        //获取被代理对象的类加载器
        ClassLoader loader=object.getClass().getClassLoader();
        //被代理对象的所有接口
        Class<?>[] interfaces=object.getClass().getInterfaces();
        //用来执行被代理类需要执行的方法
        InvocationHandler handler=new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                //开始调用被代理类的方法
                Object result=null;
               try{
                   //LogUtil.start(method,args);
                   result=method.invoke(object,args);
                  // LogUtil.stop(method,result);
               }catch(Exception e){
                   //LogUtil.logException(method,e);
                   e.printStackTrace();
               }finally {
                  // LogUtil.logFinally(method);
               }


                return result;
            }
        };
        Object o = Proxy.newProxyInstance(loader, interfaces, handler);
        return (Calculator)o;
    }
}
