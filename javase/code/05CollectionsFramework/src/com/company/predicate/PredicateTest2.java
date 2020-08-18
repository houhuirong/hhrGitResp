package com.company.predicate;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

/**
 * @Auther: hhr
 * @Date: 2020/8/18 - 08 - 18 - 16:24
 * @Description: com.company.predicate
 * @version: 1.0
 */
public class PredicateTest2 {
    public static void main(String[] args) {
        Collection<String> books = new HashSet<String>();
        books.add(new String("轻量级Java EE企业应用实战"));
        books.add(new String("疯狂Java讲义"));
        books.add(new String("疯狂ios讲义"));
        books.add(new String("疯狂Ajax讲义"));
        books.add(new String("疯狂Android讲义"));
        System.out.println(calAll(books,ele->((String)ele).contains("疯狂")));
        System.out.println(calAll(books,ele->((String)ele).contains("Java")));
        System.out.println(calAll(books,ele->((String)ele).length()>10));
    }
    public static int calAll(Collection books, Predicate p){
        int total=0;
        for(Object obj:books){
            //使用Predicate的test()方法判断该对象是否满足Predicate制定的条件
            if (p.test(obj)){
                total++;
            }
        }
        return total;
    }
}
