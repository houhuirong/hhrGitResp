package com.company.quchong;

import java.util.*;
import net.sf.json.JSONArray;


/**
 * @Auther: hhr
 * @Date: 2020/8/20 - 08 - 20 - 17:18
 * @Description: com.company.quchong
 * @version: 1.0
 */
public class Lislt {
    public static void main(String[] args) {

//需求：根据List<Map<String,Object>> 的map中name 属性相同去重
/////////////////////////////////////////////以下为造数据/////////////////////////////////////////////////////////////////////////////////////////
        //创建数据,对根据姓名 name 去重
        List<Map<String,Object>> allList = new ArrayList<>();
        List<Map<String,Object>>  smallList = new ArrayList<>();

        Map<String,Object> map1 = new HashMap<>();

        map1.put("id",2019);
        map1.put("name","小明");
        map1.put("id",2017);
        map1.put("name","小王");


        Map<String,Object> map2 = new HashMap<>();
        map2.put("id",2020);
        map2.put("name","小明");

        allList.add(map1);
        allList.add(map2);
        smallList.add(map2);

/////////////////////////////////////////////以上为造数据/////////////////////////////////////////////////////////////////////////////////////////

        /*将list map 转list pojo，必须要转成对象，因为要去重某一个属性值，而不是去重整个对象，所以必须重写 这个属性值的hashcode 和equals 值*/
        List<U> list1 = jsonToList(JSONArray.fromObject(allList).toString(),U.class);
        List<U> list2 = jsonToList(JSONArray.fromObject(smallList).toString(),U.class);

        Set<U> set = new HashSet<>();
        set.addAll(list1);
        set.addAll(list2);

        for (U u :set){
            System.out.println(u.getName());
        }
    }

    /**
     * 将json数据转换成pojo对象list
     */
    public static <T>List<T> jsonToList(String jsonData, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            List<T> list = MAPPER.readValue(jsonData, javaType);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
