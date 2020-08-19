package com.company.base.set;

import java.util.HashSet;
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
class R1
{
	int count;
	public R1(int count)
	{
		this.count = count;
	}
	public String toString()
	{
		return "R(count属性:" + count + ")";
	}
	public boolean equals(Object obj)
	{
		if (obj instanceof R1)
		{
			R1 r = (R1)obj;
			if (r.count == this.count)
			{
				return true;
			}
		}
		return false;
	}
	public int hashCode()
	{
		return this.count;
	}
}

public class TestHashSet2
{
	public static void main(String[] args) 
	{
		HashSet hs = new HashSet();
		hs.add(new R1(5));
		hs.add(new R1(-3));
		hs.add(new R1(9));
		hs.add(new R1(-2));
		//打印TreeSet集合，集合元素是有序排列的
		System.out.println(hs);
		//取出第一个元素
		Iterator it = hs.iterator();
		R1 first = (R1)it.next();
		//为第一个元素的count属性赋值
		first.count = -3;
		//再次输出count将看到TreeSet里的元素处于无序状态
		System.out.println(hs);
		hs.remove(new R1(-3));
		System.out.println(hs);
		//输出false
		System.out.println("hs是否包含count为-3的R对象？" + hs.contains(new R1(-3)));
		//输出false
		System.out.println("hs是否包含count为-2的R对象？" + hs.contains(new R1(-2)));

	}
}
