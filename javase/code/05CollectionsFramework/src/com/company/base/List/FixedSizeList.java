package com.company.base.List;

import java.util.Arrays;
import java.util.List;

/**
 * ֻ�ܱ������������Ӻ�ɾ��
 */

public class FixedSizeList
{
	public static void main(String[] args) 
	{
		List fixedList = Arrays.asList("Struts2Ȩ��ָ��" , "ROR���ݿ������ʵ��");
		//��ȡfixedList��ʵ���࣬�����Arrays$ArrayList
		System.out.println(fixedList.getClass());
		//����fixedList�ļ���Ԫ��
/*		for (int i = 0; i < fixedList.size() ; i++)
		{
			System.out.println(fixedList.get(i));
		}*/
		fixedList.forEach(System.out::println);
		//��ͼ���ӡ�ɾ��Ԫ�ض�������UnsupportedOperationException�쳣
		fixedList.add("ROR���ݿ������ʵ��");
		fixedList.remove("Struts2Ȩ��ָ��");
	}
}
