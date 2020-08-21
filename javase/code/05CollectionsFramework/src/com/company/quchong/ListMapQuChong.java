package com.company.quchong;

import java.util.*;

/**
 * @Auther: hhr
 * @Date: 2020/8/20 - 08 - 20 - 16:46
 * @Description: com.company.quchong
 * @version: 1.0
 */
public class ListMapQuChong {
    public static void main(String[] args) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("positionName","产品经理");
        map.put("rank",7);
        list.add(map);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("positionName","架构师");
        map1.put("rank",7);
        list.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("positionName","产品经理");
        map2.put("rank",7);
        list.add(map2);
        System.out.println(list);
        //Set<Map<String,Object>> returnSet=new TreeSet<Map<String,Object>>();
        Set<Map<String,Object>> returnSet=new TreeSet<Map<String,Object>>(
                Comparator.comparing(positionRank->String.valueOf(positionRank.get("positionName")))
                );
        System.out.println(returnSet);
        returnSet.addAll(new ArrayList<>(list));
        System.out.println(returnSet);

    }
}
