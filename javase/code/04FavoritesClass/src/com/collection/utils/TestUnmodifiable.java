package com.collection.utils;

import java.util.*;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */
public class TestUnmodifiable
{
	public static void main(String[] args)
	{
		//创建一个空的、不可改变的List对象
		List unmodifiableList = Collections.emptyList();
		//创建一个只有一个元素，且不可改变的Set对象
		Set unmodifiableSet = Collections.singleton("Struts2权威指南");
		//创建一个普通Map对象
		Map scores = new HashMap();
		scores.put("语文" , 80);
		scores.put("Java" , 82);
		//返回普通Map对象对应的不可变版本
		Map unmodifiableMap = Collections.unmodifiableMap(scores);
		//下面任意一行代码都将引发UnsupportedOperationException异常
		unmodifiableList.add("测试元素");
		unmodifiableSet.add("测试元素");
		unmodifiableMap.put("语文" , 90);
	}
}
