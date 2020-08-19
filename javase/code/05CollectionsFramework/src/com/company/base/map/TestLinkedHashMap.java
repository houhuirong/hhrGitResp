package com.company.base.map;

import java.util.LinkedHashMap;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */
public class TestLinkedHashMap
{
	public static void main(String[] args) 
	{
		LinkedHashMap scores = new LinkedHashMap();
		scores.put("语文" , 80);
		scores.put("数学" , 76);
		scores.put("英文" , 76);
		//遍历scores里的所有的key-value对
		for (Object key : scores.keySet())
		{
			System.out.print(key + "------>");
			System.out.print(scores.get(key) + "\n");
		}
	}
}
