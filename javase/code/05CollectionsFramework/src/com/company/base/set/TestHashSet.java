package com.company.base.set;

import java.util.HashSet;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */

//类A的equals方法总是返回true,但没有重写其hashCode()方法
class A1
{
	public boolean equals(Object obj)
	{
		return true;
	}
}

//类B的hashCode()方法总是返回1,但没有重写其equals()方法
class B1
{
	public int hashCode()
	{
		return 1;
	}
}

//类C的hashCode()方法总是返回2,但没有重写其equals()方法
class C1
{
	public int hashCode()
	{
		return 2;
	}
	public boolean equals(Object obj)
	{
		return true;
	}
}

public class TestHashSet
{
	public static void main(String[] args) 
	{
		HashSet books = new HashSet();
		//分别向books集合中添加2个A对象，2个B对象，2个C对象
		books.add(new A1());
		books.add(new A1());
		books.add(new B1());
		books.add(new B1());
		books.add(new C1());
		books.add(new C1());
		System.out.println(books);
	}
}