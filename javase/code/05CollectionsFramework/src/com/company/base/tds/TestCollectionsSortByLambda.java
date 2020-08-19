package com.company.base.tds;

import java.util.*;
class TestCollectionsSortByLambda
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
		
		Collections.sort( school, (p1,p2)->p1.age-p2.age );
		System.out.println( school );

		int index = Collections.binarySearch( 
				school, new Person1("Li",23), (p1, p2)->p1.age-p2.age );
		if( index >=0 ) 
			System.out.println( "Found:" + school.get( index ));
		else
			System.out.println( "Not Found!" );
	}
}

class Person
{
	String name;
	int age;
	public Person( String name, int age){ 
		this.name=name;
		this.age=age;
	}
	public String toString(){
		return name+":"+age;
	}
}


