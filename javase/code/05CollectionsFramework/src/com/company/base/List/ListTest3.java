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
        books.add(new String("������Java EE��ҵӦ��ʵս"));
        books.add(new String("���Java����"));
        books.add(new String("���Aroidnd����"));
        books.add(new String("���ios����"));
        //����
        books.sort((o1,o2)->((String)o1).length()-((String)o2).length());
        System.out.println(books);
        //��ÿ���ַ����ĳ����滻����Ԫ��
        books.replaceAll(ele->((String)ele).length());
        System.out.println(books);
    }
}
