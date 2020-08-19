package com.company.base.map;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: hhr
 * @Date: 2020/8/19 - 08 - 19 - 10:49
 * @Description: com.company.base.map
 * @version: 1.0
 */
public class MapTest {
    public static void main(String[] args) {
        Map map = new HashMap<>();
        map.put("疯狂Java讲义",109);
        map.put("疯狂IOS讲义",10);
        map.put("疯狂aJAX讲义",79);
        //value可以重复
        map.put("疯狂Andiroid讲义",99);
        //放入重复的key时，新的value会覆盖原有的value
        //如果新的value覆盖了原有的value，该方法返回被覆盖的value
        System.out.println(map.put("疯狂IOS讲义",99));//输出10
        System.out.println(map);
        System.out.println("是否包含值为 疯狂ios讲义 key:"+map.containsKey("疯狂IOS讲义"));
        System.out.println("是否包含值为 99 value:"+map.containsKey("疯狂IOS讲义"));
        for (Object key:map.keySet()){
            System.out.println(key+"-->"+map.get(key));
        }
        map.remove("疯狂aJAX讲义");
    }
}
