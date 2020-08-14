package com.company.listRemove;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Auther: hhr
 * @Date: 2020/7/29 - 07 - 29 - 11:37
 * @Description: com.company.listRemove
 * @version: 1.0
 */
public class ListRemoveRecommend {
    /**
     * 因为foreach删除会导致快速失败问题，fori顺序遍历会导致重复元素没删除，所以正确解法如下：
     * */
    public static void main(String[] args) {

    }
    //第一种遍历，倒叙遍历删除
    public void dxListRemove(){
        List<Integer> list=this.initList();
        for(int i=list.size()-1; i>-1; i--){
            if(list.get(i).equals("jay")){
                list.remove(list.get(i));
            }
        }
    }
    //第二种，迭代器删除
    public void listIterator(){
        List<Integer> list=this.initList();
        Iterator itr = list.iterator();
        while(itr.hasNext()) {
            if(itr.next().equals("jay")){
                itr.remove();
            }
        }
    }

    private List<Integer> initList(){
        List<Integer> list=new ArrayList<>();
         for (int i=0;i<5;i++){
            list.add(i);
        }//list {0, 1, 2, 3, 4}
        return list;
    }
}
