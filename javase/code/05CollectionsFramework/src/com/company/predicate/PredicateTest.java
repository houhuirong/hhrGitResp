package com.company.predicate;

import java.util.Collection;
import java.util.HashSet;

/**
 * @Auther: hhr
 * @Date: 2020/8/18 - 08 - 18 - 16:17
 * @Description: com.company.predicate
 * @version: 1.0
 */
public class PredicateTest {
    public static void main(String[] args) {
        Collection<String> books = new HashSet<String>();
        books.add(new String("轻量级Java EE企业应用实战"));
        books.add(new String("疯狂Java讲义"));
        books.add(new String("疯狂ios讲义"));
        books.add(new String("疯狂Ajax讲义"));
        books.add(new String("疯狂Android讲义"));
        //使用Lambda表达式(目标类型是Predicate)过滤集合
        books.removeIf(ele->((String)ele).length()<10);
        System.out.println(books);
    }
}
