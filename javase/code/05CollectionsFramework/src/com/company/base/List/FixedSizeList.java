package com.company.base.List;

import java.util.Arrays;
import java.util.List;

/**
 * 只能遍历，不可增加和删除
 */

public class FixedSizeList
{
	public static void main(String[] args) 
	{
		List fixedList = Arrays.asList("Struts2权威指南" , "ROR敏捷开发最佳实践");
		//获取fixedList的实现类，将输出Arrays$ArrayList
		System.out.println(fixedList.getClass());
		//遍历fixedList的集合元素
/*		for (int i = 0; i < fixedList.size() ; i++)
		{
			System.out.println(fixedList.get(i));
		}*/
		fixedList.forEach(System.out::println);
		//试图增加、删除元素都将引发UnsupportedOperationException异常
		fixedList.add("ROR敏捷开发最佳实践");
		fixedList.remove("Struts2权威指南");
	}
}
