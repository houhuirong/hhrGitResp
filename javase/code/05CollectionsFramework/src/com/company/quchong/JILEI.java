package com.company.quchong;

import java.util.*;

/**
 * @Auther: hhr
 * @Date: 2020/8/21 - 08 - 21 - 10:19
 * @Description: com.company.quchong
 * @version: 1.0
 */
public class JILEI {
    public static void main(String[] args) {
        List<Map<String,Object>> collisionList=new ArrayList<Map<String,Object>>();

        List<Map<String,Object>> tmpList=new ArrayList<Map<String,Object>>();
        Set<String> keysSet = new HashSet<String>();
        for(Map<String, Object> collisionMap : collisionList){
            String keys = (String) collisionMap.get("value");
            int beforeSize = keysSet.size();
            keysSet.add(keys);
            int afterSize = keysSet.size();
            if(afterSize == beforeSize + 1){
                tmpList.add(collisionMap);
            }
        }
    }
}
