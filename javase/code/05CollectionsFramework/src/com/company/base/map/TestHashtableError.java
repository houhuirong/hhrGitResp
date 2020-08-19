package com.company.base.map;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */
public class TestHashtableError
{
	public static void main(String[] args) 
	{
		Hashtable ht = new Hashtable();
		ht.put(new A(60000) , "Struts2权威指南");
		ht.put(new A(87563) , "轻量级J2EE企业应用实战");
		Iterator it = ht.keySet().iterator();
		A first = (A)it.next();
		first.count = 87563;
		System.out.println(ht);
		ht.remove(new A(87563));
		System.out.println(ht);
		//无法获取剩下的value，下面两行代码都将输出null。
		System.out.println(ht.get(new A(87563)));
		System.out.println(ht.get(new A(60000)));
	}
}
