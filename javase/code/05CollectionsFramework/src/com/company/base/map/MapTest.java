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
        map.put("���Java����",109);
        map.put("���IOS����",10);
        map.put("���aJAX����",79);
        //value�����ظ�
        map.put("���Andiroid����",99);
        //�����ظ���keyʱ���µ�value�Ḳ��ԭ�е�value
        //����µ�value������ԭ�е�value���÷������ر����ǵ�value
        System.out.println(map.put("���IOS����",99));//���10
        System.out.println(map);
        System.out.println("�Ƿ����ֵΪ ���ios���� key:"+map.containsKey("���IOS����"));
        System.out.println("�Ƿ����ֵΪ 99 value:"+map.containsKey("���IOS����"));
        for (Object key:map.keySet()){
            System.out.println(key+"-->"+map.get(key));
        }
        map.remove("���aJAX����");
    }
}
