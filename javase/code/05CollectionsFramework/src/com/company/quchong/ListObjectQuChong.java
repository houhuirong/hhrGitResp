package com.company.quchong;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Auther: hhr
 * @Date: 2020/8/20 - 08 - 20 - 10:23
 * @Description: com.company.quchong
 * @version: 1.0
 */
public class ListObjectQuChong {
    public static void main(String[] args) {
        List<Person> personList = new ArrayList<Person>();
        personList.add(new Person("����", "111111"));
        personList.add(new Person("����", "222222"));
        personList.add(new Person("����", "333333"));
        personList.add(new Person("����", "111111"));

        Set<Person> setData = new HashSet<Person>();
        setData.addAll(personList);
        System.out.println("list"+personList.toString());
        System.out.println("set"+setData.toString());
    }
}
