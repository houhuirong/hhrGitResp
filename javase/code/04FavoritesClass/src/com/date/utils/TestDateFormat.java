package com.date.utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import static java.text.DateFormat.*;

/**
 * ���й��ʻ��������䲻������Ϊ���������ַ������������ض��ĸ�ʽ��
 */
public class TestDateFormat
{
	public static void main(String[] args) 
	{
		//��Ҫ����ʽ��������
		Date dt = new Date();
		//��������Locale���ֱ�����й�������
		Locale[] locales = {Locale.CHINA, Locale.US};
		DateFormat[] df = new DateFormat[16];
		//Ϊ��������Locale����16��DateFormat����
		for (int i = 0 ; i < locales.length ; i++)
		{
			df[i * 8] = DateFormat.getDateInstance(SHORT, locales[i]);
			df[i * 8 + 1] = DateFormat.getDateInstance(MEDIUM, locales[i]);
			df[i * 8 + 2] = DateFormat.getDateInstance(LONG, locales[i]);
			df[i * 8 + 3] = DateFormat.getDateInstance(FULL, locales[i]);
			df[i * 8 + 4] = DateFormat.getTimeInstance(SHORT, locales[i]);
			df[i * 8 + 5] = DateFormat.getTimeInstance(MEDIUM , locales[i]);
			df[i * 8 + 6] = DateFormat.getTimeInstance(LONG , locales[i]);
			df[i * 8 + 7] = DateFormat.getTimeInstance(FULL , locales[i]);
		}
		for (int i = 0 ; i < locales.length ; i++)
		{
			switch (i)
			{
				case 0:
					System.out.println("-------�й����ڸ�ʽ--------");
					break;
				case 1:
					System.out.println("-------�������ڸ�ʽ--------");
					break;						
			}
			System.out.println("SHORT��ʽ�����ڸ�ʽ��" + df[i * 8].format(dt));
			System.out.println("MEDIUM��ʽ�����ڸ�ʽ��" + df[i * 8 + 1].format(dt));
			System.out.println("LONG��ʽ�����ڸ�ʽ��" + df[i * 8 + 2].format(dt));
			System.out.println("FULL��ʽ�����ڸ�ʽ��" + df[i * 8 + 3].format(dt));
			System.out.println("SHORT��ʽ��ʱ���ʽ��" + df[i * 8 + 4].format(dt));
			System.out.println("MEDIUM��ʽ��ʱ���ʽ��" + df[i * 8 + 5].format(dt));
			System.out.println("LONG��ʽ��ʱ���ʽ��" + df[i * 8 + 6].format(dt));
			System.out.println("FULL��ʽ��ʱ���ʽ��" + df[i * 8 + 7].format(dt));
		}
	}
}
