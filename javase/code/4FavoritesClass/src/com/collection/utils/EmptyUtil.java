package com.collection.utils;

import java.util.Collection;
import java.util.Map;

/**
 * 判空工具类
 * Created by foxsand on 2017/9/27.
 */
public class EmptyUtil {
    //Suppress default constructor for noninstantiability
    private EmptyUtil(){
        throw new AssertionError();
    }

    public static boolean isEmpty(Object object){
        if (object == null){
            return true;
        }
        if (object instanceof CharSequence && ((CharSequence) object).length() == 0){
            return true;
        }
        if (object instanceof Collection && ((Collection) object).isEmpty()){
            return true;
        }
        if (object instanceof Map && ((Map) object).isEmpty()){
            return true;
        }
        return false;
    }
    public static boolean isNoEmpty(Object object){
        return !isEmpty(object);
    }
}
