package com.company.base.List;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: hhr
 * @Date: 2020/8/19 - 08 - 19 - 9:54
 * @Description: com.company.base.List
 * @version: 1.0
 */
public class ListTest3 {
    public static void main(String[] args) {
        List books = new ArrayList();
        books.add(new String("轻量级Java EE企业应用实战"));
        books.add(new String("疯狂Java讲义"));
        books.add(new String("疯狂Aroidnd讲义"));
        books.add(new String("疯狂ios讲义"));
        //排序
        books.sort((o1,o2)->((String)o1).length()-((String)o2).length());
        System.out.println(books);
        //用每个字符串的长度替换集合元素
        books.replaceAll(ele->((String)ele).length());
        System.out.println(books);
    }
}
