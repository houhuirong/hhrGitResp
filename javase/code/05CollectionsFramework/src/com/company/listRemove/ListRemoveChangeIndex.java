package com.company.listRemove;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Auther: hhr
 * @Date: 2020/7/29 - 07 - 29 - 11:11
 * @Description: com.company.listRemove
 * @version: 1.0
 */
public class ListRemoveChangeIndex {
    public static void main(String[] args) {
        int type=1;//0--ArrayList,1--CopyOnWriteArrayList
        ListRemoveChangeIndex lrc=new ListRemoveChangeIndex();
        lrc.listChangeIndex(type);
        //lrc.listRemoveForEach(type);
        lrc.listIterator(type);
    }
    private List<Integer> initList(int type){
        List<Integer> list=null;
        if(type==0){
            list = new ArrayList<>();
        }else if(type==1){
            list = new CopyOnWriteArrayList<>();
        }

        for (int i=0;i<5;i++){
            list.add(i);
        }//list {0, 1, 2, 3, 4}
        return list;
    }
    public void listChangeIndex( int type){
        List<Integer> list=this.initList(type);
        for (int i = 0; i < list.size(); i++) {
            //index and number
            System.out.print(i+" "+list.size());
            if (list.get(i)%2==0){
                list.remove(list.get(i));
                System.out.print(" delete");
                i--;//索引改变！
            }
            System.out.println();
        }

    }

    public void listRemoveForEach( int type){
        List<Integer> list=this.initList(type);
        for (Integer num : list) {
            // index and number
            System.out.print(num);
            if (num % 2 == 0) {
                list.remove(num);
                System.out.print(" delete");
            }
            System.out.println();
        }
    }

    public void listIterator( int type){
        List<Integer> list=this.initList(type);
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) {
            // index and number
            int num = it.next();
            System.out.print(num);
            if (num % 2 == 0) {
                it.remove();
                System.out.print(" delete");
            }
            System.out.println();
        }

    }
}
