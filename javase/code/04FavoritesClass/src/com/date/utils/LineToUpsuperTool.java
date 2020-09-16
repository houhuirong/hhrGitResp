package com.date.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: hhr
 * @Date: 2020/9/16 - 09 - 16 - 10:14
 * @Description: 驼峰名称和下划线名称的互相转换
 * @version: 1.0
 */
public class LineToUpsuperTool {

    private static Pattern linePattern=Pattern.compile("_(\\w)");
    /*
     * 驼峰名称和下划线名称的相互转换
     */
    public static String lineToHump(String str){
        str=str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()){
            matcher.appendReplacement(sb,matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();

    }

    private static Pattern humpPattern = Pattern.compile("[A-Z]");
    /** 驼峰转下划线,效率比上面高 */
    public static String humpToLine(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static void main(String[] args) {
        String tableName="tableName";
        String realTableName = LineToUpsuperTool.humpToLine(tableName).replaceAll("_entity", "").substring(0);
        System.out.println(realTableName);

        String lineToTF="h_j_k";
        String s = LineToUpsuperTool.lineToHump(lineToTF).replaceAll("_entity", "").substring(0);
        System.out.println(s);
    }
}
