package com.collection.utils;

/**
 * @Auther: hhr
 * @Date: 2020/8/13 - 08 - 13 - 16:48
 * @Description: com.collection.utils
 * @version: 1.0
 */
public class CollectionPanNull {
    /**
     * CollectionUtils.isEmpty(list)    源码
     *
     *  public static boolean isEmpty(Collection coll) {
     *         return (coll == null || coll.isEmpty());
     *     }
     * */
    /**
     *List<String> list=new ArrayList<String>();
     * CollectionUtils.isNotEmpty(list);
     * 或
     * list != null && !list.isEmpty()//判断size()效率低
     * System.out.println(list.isEmpty()); //true
     * System.out.println(list.size()); //0
     *
     * */

    /**
     *
     * Map<String, String> map=new HashMap<String, String>();
     * map.isEmpty()&&map.size()>0
     * System.out.println(map.isEmpty()); //true
     * System.out.println(map.size()); //0
     * */
}
