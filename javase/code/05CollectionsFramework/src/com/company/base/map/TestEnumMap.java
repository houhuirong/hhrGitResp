package com.company.base.map;

import java.util.EnumMap;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */
enum Season1
{
	SPRING,SUMMER,FALL,WINTER
}

public class TestEnumMap
{
	public static void main(String[] args) 
	{
		//����һ��EnumMap���󣬸�EnumMap������key������Seasonö�����ö��ֵ
		EnumMap enumMap = new EnumMap(Season1.class);
		enumMap.put(Season1.SUMMER , "��������");
		enumMap.put(Season1.SPRING , "��ů����");

		System.out.println(enumMap);
	}
} 