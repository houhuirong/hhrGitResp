package com.company.fenpi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestListChange {

	public static void main(String[] args) {
		//为集合初始化多条数据，根据id 合并inCome字段使用
        People p1 = new People();
        p1.setId(1001);
        p1.setName("张三");
        p1.setInCome(5000.0);
 
        People p2 = new People();
        p2.setId(1002);
        p2.setName("李四");
        p2.setInCome(8000.0);
 
        People p3 = new People();
        p3.setId(1003);
        p3.setName("王二");
        p3.setInCome(5350.0);
 
        People p4 = new People();
        p4.setId(1003);
        p4.setName("王二");
        p4.setInCome(3000);
 
        People p5 = new People();
        p5.setId(1002);
        p5.setName("李四");
        p5.setInCome(4500);
 
        List<People> list = new ArrayList();
        list.add(p1);
        list.add(p2);
        list.add(p3);
        list.add(p4);
        list.add(p5);
        
        //遍历集合中数据
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).toString());
        }
 
        System.out.println("================================================");
 
        List<People> changeList = getNewList(list);
        //合并后的数据条数
        System.out.println("合并后的数据条数:"+changeList.size());
        //遍历合并后的数据
        for (People people : changeList) {
            if(people!=null)
            System.out.println(people.toString());
        }
    }
 
 
    private static List<People> getNewList(List<People> list) {
        HashMap<Integer, People> tempMap = new HashMap<Integer, People>();
        for (People people : list) {
            int temp = people.getId();//获取编号
            //containsKey(Object key)该方法判断Map集合中是否包含指定的键名，如果包含返回true，不包含返回false
            //containsValue(Object value)该方法判断Map集合中是否包含指定的键值，如果包含返回true，不包含返回false
            if (tempMap.containsKey(temp)) {
                People p = new People();
                p.setId(temp);
                p.setName(people.getName());
                //合并相同id的inCome值
                p.setInCome(tempMap.get(temp).getInCome() + people.getInCome());
 
                //HashMap不允许key重复，当有key重复时，前面key对应的value值会被覆盖
                tempMap.put(temp, p);
            } else {
                tempMap.put(temp, people);
            }
        }
 
        //去除重复 编号id 的 list
        List<People> newList = new ArrayList<People>();
        for(Integer temp:tempMap.keySet()){
            newList.add(tempMap.get(temp));
        }
        return newList;

	}

}
