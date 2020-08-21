package com.company.fenpi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: hhr
 * @Date: 2020/8/21 - 08 - 21 - 11:02
 * @Description: com.company.fenpi
 * @version: 1.0
 */
public class FenPiTest {
    public static void main(String[] args) {
        Map<String, Object> body = new HashMap<>();
        //分批算法1
        List<Map<String,Object>> userDetailList = new ArrayList();
        int num=100;// 定义批处理的数据数量（即批处理条件）
        int times=0;
        if(userDetailList.size()>num){
            // 如果大于定义的数量，按定义数量进行批处理
            times=userDetailList.size()/num;
        }
        // 遍历分批处理次数，并进行批处理
        for(int i=0;i<=times;i++) {
            //定义要进行批处理的临时集合
            List<String> tempList = new ArrayList<>();
            for (int j = i * num; j < userDetailList.size(); j++) {
                tempList.add(String.valueOf(userDetailList.get(j).get("userid")));
                if (tempList.size() == num) {
                    break;
                }
            }
            body.put("useridlist", tempList);
        }

        //分批算法2
        int offset=0;//偏移量,偏移量从0开始
        int limit=50;//分页大小，最大50
        int size=25;//员工分批参数,与offset、limit配合使用,至多有50条打卡记录
        List<String> userIds=new ArrayList<>();
        List<Map<String,Object>> userList = new ArrayList();

        for(Map<String,Object> userMap:userList){
            userIds.add(String.valueOf(userMap.get("userid")));
        }
        int number = userIds.size()/size;
        int remainder = userIds.size()%size;
        for (int i = 0; i < number; i++) {
            boolean flag = true;
            offset = 0;//偏移量,偏移量从0开始
            List<String> ownerNameList = new ArrayList<>(userIds.subList(i * size, (i + 1) * size));//每次请求25个员工
            body.put("userIdList", ownerNameList);
        }
        if(remainder > 0) {
            List<String> ownerNameList = new ArrayList<>(userIds.subList(number * size, userIds.size()));
            body.put("userIdList", ownerNameList);
            boolean flag = true;
        }
    }
}
