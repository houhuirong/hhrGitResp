package com.company.base.map;

import java.util.HashMap;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */
public class NullInHashMap
{
	public static void main(String[] args) 
	{
		HashMap hm = new HashMap();
		//试图将2个key为null的key-value对放入HashMap中
		hm.put(null , null);
		hm.put(null , null);
		//将一个value为null的key-value对放入HashMap中
		hm.put("a" , null);
		//输出Map对象
		System.out.println(hm);
	}
}
