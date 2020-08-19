package com.company.base.List;

import java.util.LinkedList;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */
public class TestLinkedList
{
	public static void main(String[] args) 
	{
		LinkedList books = new LinkedList();
		//将字符串元素加入队列的尾部
		books.offer("Struts2权威指南");
		//将一个字符串元素入栈
		books.push("轻量级J2EE企业应用实战");
		//将字符串元素添加到队列的头部
		books.offerFirst("ROR敏捷开发最佳实践");
		for (int i = 0; i < books.size() ; i++ )
		{
			System.out.println(books.get(i));
		}
		//访问、并不删除队列的第一个元素
		System.out.println(books.peekFirst());
		//访问、并不删除队列的最后一个元素
		System.out.println(books.peekLast());
		//采用出栈的方式将第一个元素pop出队列
		System.out.println(books.pop());
		//下面输出将看到队列中第一个元素被删除
		System.out.println(books);
		//访问、并删除队列的最后一个元素
		System.out.println(books.pollLast());
		//下面输出将看到队列中只剩下中间一个元素：轻量级J2EE企业应用实战
		System.out.println(books);

	}
}
