package com.company.base.List;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */
public class TestListIterator
{
	public static void main(String[] args) 
	{
		String[] books = {
			"Struts2权威指南",
			"轻量级J2EE企业应用实战"
		};
		List bookList = new ArrayList();
		for (int i = 0; i < books.length ; i++ )
		{
			bookList.add(books[i]);
		}
		ListIterator lit = bookList.listIterator();
		while (lit.hasNext())
		{
			System.out.println(lit.next());
			lit.add("-------分隔符-------");
		}
		System.out.println("==========下面开始反向迭代===========");
		while(lit.hasPrevious())
		{
			System.out.println(lit.previous());
		}
	}
}
