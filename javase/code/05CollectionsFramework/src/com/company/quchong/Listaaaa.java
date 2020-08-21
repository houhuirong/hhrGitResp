package com.company.quchong;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Auther: hhr
 * @Date: 2020/8/20 - 08 - 20 - 10:17
 * @Description: com.collection.utils
 * @version: 1.0
 */
public class Listaaaa {
    public static void main(String[] args) {
        List list=new ArrayList();
        list.add(11);
        list.add(12);
        list.add(13);
        list.add(14);
        list.add(15);
        list.add(11);
        System.out.println(list);

        Set set = new HashSet();
        List newList = new ArrayList();
        set.addAll(list);
        newList.addAll(set);
        System.out.println(newList);
    }
}
