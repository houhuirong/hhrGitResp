package com.company.list;

import java.util.ArrayList;
import java.util.Iterator;

public class ArrayListDemo {

    public static void main(String[] args) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        System.out.printf("Before add:arrayList.size()=%d\n",arrayList.size());
        arrayList.add(1);
        arrayList.add(3);
        arrayList.add(5);
        arrayList.add(7);
        arrayList.add(9);
        System.out.printf("After add:arrayList.size()=%d\n",arrayList.size());
        System.out.println("Printing elements of arrayList");
        //三种遍历方式打印元素
        //第一种：通过迭代器遍历
        System.out.print("通过迭代器遍历");
        Iterator<Integer> it=arrayList.iterator();
        while (it.hasNext()){
            System.out.print(it.next()+" ");
        }
        System.out.println();
        //第二种：通过索引值遍历
        System.out.println("通过索引值遍历");
        for (int i=0;i<arrayList.size();i++){
            System.out.print(arrayList.get(i)+" ");
        }
        //第三种：for循环遍历
        System.out.print("for循环遍历：");
        for (Integer number:arrayList){
            System.out.print(number+" ");
        }
        //toArray用法
        //第一种方式（最常用）
        Integer[] integers=arrayList.toArray(new Integer[0]);
        System.out.println();

        System.out.println();
        //第二种方式（容易理解）
        Integer[] integers1 = new Integer[arrayList.size()];
        arrayList.toArray(integers1);
    }
}
