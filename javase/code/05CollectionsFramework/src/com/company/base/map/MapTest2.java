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
        map.put("���Java����",109);
        map.put("���IOS����",10);
        map.put("���aJAX����",79);
        //value�����ظ�
        map.put("���Andiroid����",99);
        //�����滻keyΪ�����xml���塱��value������ԭmap��û�ж�Ӧ��key��
        //���mapû�иı䣬��������µ�key-value��
        map.replace("���xml����",66);
        System.out.println(map);
        //ʹ��ԭvalue�봫�������������Ľ������ԭ�е�value
        map.merge("���IOS����",10,(oldVal,param)->(Integer)oldVal+(Integer)param);
        System.out.println(map);
        //��keyΪjava��Ӧ��valueΪnull�򲻴���ʱ��ʹ�ü���Ľ����Ϊ��value
        map.computeIfAbsent("Java",(key)->((String)key).length());
        //��keyΪjava��Ӧ��value����ʱ��ʹ�ü���Ľ����Ϊ��value
        map.computeIfPresent("Java",(key,value)->(Integer) value*(Integer)value);
        System.out.println(map);
    for (Object key:map.keySet()){
            System.out.println(key+"-->"+map.get(key));
        }
        map.remove("���aJAX����");
    }
}
