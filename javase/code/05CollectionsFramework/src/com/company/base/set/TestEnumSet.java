package com.company.base.set;

import java.util.Collection;
import java.util.EnumSet;
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
public class TestEnumSet
{
	public static void main(String[] args) 
	{
		Collection c = new HashSet();
		c.clear();
		c.add(Season.FALL);
		c.add(Season.SPRING);
		//复制Collection集合中所有元素来创建EnumSet集合
		EnumSet enumSet = EnumSet.copyOf(c);
		//输出[SPRING,FALL]
		System.out.println(enumSet);
		c.add("Struts2权威指南");
		c.add("轻量级J2EE企业应用实战");
		//下面代码出现异常：因为c集合里的元素不是全部都为枚举值
		enumSet = EnumSet.copyOf(c); 
	}
} 