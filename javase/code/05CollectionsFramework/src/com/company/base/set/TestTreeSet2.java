package com.company.base.set;
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
class R implements Comparable
{
	int count;
	public R(int count)
	{
		this.count = count;
	}
	public String toString()
	{
		return "R(count属性:" + count + ")";
	}
	public boolean equals(Object obj)
	{
		if (obj instanceof R)
		{
			R r = (R)obj;
			if (r.count == this.count)
			{
				return true;
			}
		}
		return false;
	}
	public int compareTo(Object obj)
	{
		R r = (R)obj;
		if (this.count > r.count)
		{
			return 1;
		}
		else if (this.count == r.count)
		{
			return 0;
		}
		else
		{
			return -1;
		}
	}
}
public class TestTreeSet2
{
	public static void main(String[] args)
	{
		TreeSet ts = new TreeSet();
		ts.add(new R(5));
		ts.add(new R(-3));
		ts.add(new R(9));
		ts.add(new R(-2));
		//打印TreeSet集合，集合元素是有序排列的
		System.out.println(ts);
		//取出第一个元素
		R first = (R)ts.first();
		//为第一个元素的count属性赋值
		first.count = 20;
		//取出最后一个元素
		R last = (R)ts.last();
		//为最后一个元素的count属性赋值，与倒数第二个元素count属性相同
		last.count = -2;
		//再次输出count将看到TreeSet里的元素处于无序状态，且有重复元素
		System.out.println(ts);
		//删除属性被改变的元素，删除失败
		ts.remove(new R(-2));
		System.out.println(ts);
		//删除属性没有改变的元素，删除成功
		ts.remove(new R(5));
		System.out.println(ts);
	}
}
