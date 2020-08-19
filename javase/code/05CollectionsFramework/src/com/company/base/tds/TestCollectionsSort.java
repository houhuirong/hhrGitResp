package com.company.base.tds;

import java.util.*;
class TestCollectionsSort 
{
	public static void main(String[] args) 
	{
		List<Person1> school = new ArrayList<>();
		school.add( new Person1("Li",23));
		school.add( new Person1("Wang",28));
		school.add( new Person1("Zhang",21));
		school.add( new Person1("Tang",19));
		school.add( new Person1("Chen",22));
		school.add( new Person1("Zhao",22));
		System.out.println( school );
		
		Collections.sort( school, new PersonComparator() );
		System.out.println( school );

		int index = Collections.binarySearch( 
				school, new Person1("Li",23), new PersonComparator() );
		if( index >=0 ) 
			System.out.println( "Found:" + school.get( index ));
		else
			System.out.println( "Not Found!" );
	}
}

class Person1
{
	String name;
	int age;
	public Person1(String name, int age){
		this.name=name;
		this.age=age;
	}
	@Override
	public String toString(){
		return name+":"+age;
	}
}

class PersonComparator implements Comparator
{
	public int compare( Object obj1, Object obj2 ){
		Person1 p1 = (Person1)obj1;
		Person1 p2 = (Person1)obj2;
		if( p1.age > p2.age ) return 1;
		else if(p1.age<p2.age) return -1;
		return p1.name.compareTo( p2.name );
	}
}

