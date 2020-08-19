package com.company.base.map;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */
public class TestProperties
{
	public static void main(String[] args) throws Exception
	{
		Properties props = new Properties();
		//向Properties中增加属性
		props.setProperty("username" , "yeeku");
		props.setProperty("password" , "123456");
		//将Properties中的属性保存到a.ini文件中
		props.store(new FileOutputStream("a.ini") , "comment line");
		//新建一个Properties对象
		Properties props2 = new Properties();
		//向Properties中增加属性
		props2.setProperty("gender" , "male");
		//将a.ini文件中的属性名-属性值追加到props2中
		props2.load(new FileInputStream("a.ini") );
		System.out.println(props2);
	}
}
