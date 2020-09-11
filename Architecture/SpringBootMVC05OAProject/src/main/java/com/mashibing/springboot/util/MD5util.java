package com.mashibing.springboot.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Auther: hhr
 * @Date: 2020/9/11 - 09 - 11 - 11:16
 * @Description: com.mashibing.springboot.util
 * @version: 1.0
 */
public class MD5util {
    /**
     * MD5加密类
     *
     * @param str 要加密的字符串
     * @return 加密后的字符串
     */
    public static String code(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");// 获得MD5摘要算法的 MessageDigest 对象
            md.update(str.getBytes());// 使用指定的字节更新摘要
            byte[] byteDigest = md.digest();// 获得密文
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < byteDigest.length; offset++) {
                i = byteDigest[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
            // 16位的加密
            //return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }
}
