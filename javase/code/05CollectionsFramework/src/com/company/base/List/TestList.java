package com.company.base.List;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */

public class TestList
{
	public static void main(String[] args) 
	{
		List books = new ArrayList();
		//向books集合中添加三个元素
		books.add(new String("轻量级J2EE企业应用实战"));
		books.add(new String("Struts2权威指南"));
		books.add(new String("基于J2EE的Ajax宝典"));
		System.out.println(books);
		//将新字符串对象插入在第二个位置
		books.add(1 , new String("ROR敏捷开发最佳实践"));
		for (int i = 0 ; i < books.size() ; i++ )
		{
			System.out.println(books.get(i));
		}
		//删除第三个元素
		books.remove(2);
		System.out.println(books);
		//判断指定元素在List集合中位置：输出1，表明位于第二位
		System.out.println(books.indexOf(new String("ROR敏捷开发最佳实践")));
		//将第二个元素替换成新的字符串对象
		//set不会改变List集合的长度，所有set的索引不能超过集合长度。
		books.set(1, new String("Struts2权威指南"));
		System.out.println(books);
		//将books集合的第二个元素（包括）到第三个元素（不包括）截取称子集合
		System.out.println(books.subList(1 , 2));

	}
}
