package com.company.quchong;

import java.util.*;

/**
 * @Auther: hhr
 * @Date: 2020/8/21 - 08 - 21 - 9:56
 * @Description: com.company.quchong
 * @version: 1.0
 */
public class ListZhuanMap {
    /**
     * 根据map中的某个key 去除List中重复的map
     * @author shijing
     * @param list
     * @param mapKey
     * @return
     */
    public static List<Map<String, Object>> removeRepeatMapByKey(List<Map<String, Object>>
                                                                         list, String mapKey){
        if (CollectionUtils.isNullOrEmpty(list)) return null;
        //把list中的数据转换成msp,去掉同一id值多余数据，保留查找到第一个id值对应的数据
        List<Map<String, Object>> listMap = new ArrayList<>();
        Map<String, Map> msp = new HashMap<>();
        for(int i = list.size()-1 ; i>=0; i--){
            Map map = list.get(i);
            String id = (String)map.get(mapKey);
            map.remove(mapKey);
            msp.put(id, map);
        }
        //把msp再转换成list,就会得到根据某一字段去掉重复的数据的List<Map>
        Set<String> mspKey = msp.keySet();
        for(String key: mspKey){
            Map newMap = msp.get(key);
            newMap.put(mapKey, key);
            listMap.add(newMap);
        }
        return listMap;
    }
    public static void main(String[] args) {
        Map<String, Map> msp = new HashMap<String, Map>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>();
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("id", "1123");
        map1.put("name", "张三");
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("id", "2");
        map2.put("name", "李四");
        Map<String, Object> map3 = new HashMap<String, Object>();
        map3.put("id", "1123");
        map3.put("name", "王五");
        Map<String, Object> map4 = new HashMap<String, Object>();
        map4.put("id", "3");
        map4.put("name", "赵六");
        list.add(map1);
        list.add(map2);
        list.add(map3);
        list.add(map4);
        System.out.println("初始数据：" + list.toString());
        System.out.println("去重之后：" + removeRepeatMapByKey(list,"id"));
    }

}
