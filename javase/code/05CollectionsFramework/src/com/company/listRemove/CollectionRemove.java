package com.company.listRemove;

import org.omg.CORBA.OBJ_ADAPTER;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @Auther: hhr
 * @Date: 2020/8/18 - 08 - 18 - 15:18
 * @Description: com.company.listRemove
 * @version: 1.0
 */
/*
* foreach 和Iterator循环迭代访问Collection集合，都不是集合元素本身，系统只是依次把集合元素的值赋值给
* 迭代变量，因此在循环中修改迭代变量的值没有任何实际意义。
* */
public class CollectionRemove {
    public static void main(String[] args) {
        Collection c = new HashSet();
        c.add("JavaSe");
        c.add("Java");
        c.add("JavaWeb");
        Iterator iterator=c.iterator();
        while (iterator.hasNext()){
            //it.next()获取到object，故要强制转换
            String str= (String) iterator.next();
            System.out.println(str);
            if (str.equals("JavaWeb1")){
               /* //1、从集合中删除上一次next（）方法返回的元素
                iterator.remove();*/

                //2、使用Iterator迭代过程中，不可修改集合元素,下面代码引发异常
                /*
                * 2.1
                * Iterator迭代器采用的是快速失败机制，一旦在迭代过程中农检测到该集合已经被修改
                * （通常是程序中的其他线程修改，）程序立即引发ConcurrentModificationException异常，而不是显示修改后的
                * 结果，这样可以避免共享资源而引发的潜在问题；
                * 2.2
                * 另外，删除JavaWeb不会发生异常，也不可心存侥幸。这是由集合类的实现代码决定的，程序员
                * 不应该这么做。
                * 2.3
                * 注意！！！迭代时删除集合中元素都会导致异常，只有在删除集合中某个特定元素时才不会抛出异常。
                * */
                c.remove(str);
            }
           // str="JAVA";
        }
        //System.out.println(c);
        /*
        *
        * */
        for (Object obj:c){
            String str1=(String) obj;
            System.out.println(str1);
            if (str1.equals("Java")){
                //下面代码会引发ConcurrentModificationException异常
                c.remove(str1);
            }
        }
        System.out.println(c);
    }
}
