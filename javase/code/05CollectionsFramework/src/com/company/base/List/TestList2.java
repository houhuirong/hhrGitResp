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
class A11
{//List�ж������������ֻҪͨ��equals�����ȽϷ���true����
	public boolean equals(Object obj)
	{
		return true;
	}
}

public class TestList2
{
	public static void main(String[] args) 
	{
		List books = new ArrayList();
		books.add(new String("������J2EE��ҵӦ��ʵս"));
		books.add(new String("Struts2Ȩ��ָ��"));
		books.add(new String("����J2EE��Ajax����"));
		System.out.println(books);
		//ɾ��������A���󣬽����µ�һ��Ԫ�ر�ɾ��
		books.remove(new A11());
		System.out.println(books);
		//ɾ��������A�����ٴ�ɾ�������е�һ��Ԫ��
		books.remove(new A11());
		System.out.println(books);
	}
}
