
import java.util.*;

/**
 * Description:
 * <br/>Copyright (C), 2005-2008, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */

//Z类，重写了equals方法，总是返回false，
//重写了compareTo(Object obj)方法，总是返回正整数
class Z implements Comparable
{
	int age;
	public Z(int age)
	{
		this.age = age;
	}
	public boolean equals(Object obj)
	{
		return false;
	}
	public int compareTo(Object obj)
	{
		return 1;
	}
}
public class TestTreeSet
{
	public static void main(String[] args)
	{
		TreeSet set = new TreeSet();
		Z z1 = new Z(6);
		set.add(z1);
		System.out.println(set.add(z1));
		//下面输出set集合，将看到有2个元素
		System.out.println(set);
		//修改set集合的第一个元素的age属性
		((Z)(set.first())).age = 9;
		//输出set集合的最后一个元素的age属性，将看到也变成了9
		System.out.println(((Z)(set.last())).age);
	}
}
