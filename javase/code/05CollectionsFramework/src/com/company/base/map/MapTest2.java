package com.company.base.map;

import javax.management.ValueExp;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: hhr
 * @Date: 2020/8/19 - 08 - 19 - 10:49
 * @Description: com.company.base.map
 * @version: 1.0
 */
public class MapTest2 {
    public static void main(String[] args) {
        Map map = new HashMap<>();
        map.put("疯狂Java讲义",109);
        map.put("疯狂IOS讲义",10);
        map.put("疯狂aJAX讲义",79);
        //value可以重复
        map.put("疯狂Andiroid讲义",99);
        //尝试替换key为“疯狂xml讲义”的value，由于原map中没有对应的key，
        //因此map没有改变，不会添加新的key-value对
        map.replace("疯狂xml讲义",66);
        System.out.println(map);
        //使用原value与传入参数计算出来的结果覆盖原有的value
        map.merge("疯狂IOS讲义",10,(oldVal,param)->(Integer)oldVal+(Integer)param);
        System.out.println(map);
        //当key为java对应的value为null或不存在时，使用计算的结果作为新value
        map.computeIfAbsent("Java",(key)->((String)key).length());
        //当key为java对应的value存在时，使用计算的结果作为新value
        map.computeIfPresent("Java",(key,value)->(Integer) value*(Integer)value);
        System.out.println(map);
    for (Object key:map.keySet()){
            System.out.println(key+"-->"+map.get(key));
        }
        map.remove("疯狂aJAX讲义");
    }
}
