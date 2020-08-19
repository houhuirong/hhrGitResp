package com.company.base.tds;

import java.util.*;

class GenericContravariance
{
	public static void main(String[] args) 
	{
		Comparator<Fruit1>comparator = new Comparator<Fruit1>(){
			public int compare(Fruit1 obj1, Fruit1 obj2 ){
				return obj1.weight - obj2.weight;
			}
		};

		Basket<Apple1> basket1 = new Basket<>(new Apple1(), new Apple1(), new Apple1());
		Basket<Banana1> basket2 = new Basket<>(new Banana1(), new Banana1(), new Banana1());
		basket1.sort(comparator);
		basket2.sort(comparator);
		traverse(basket1);
		traverse(basket2);
	}
	public static void traverse(Basket<? extends Fruit1> basket ){
		for( Fruit1 obj : basket.things ){
			System.out.print(obj);
		}
	}
}

class Fruit1
{	int weight = (int)(Math.random()*100);
	public String toString(){return this.getClass().getName()+weight; }
}
class Apple1 extends Fruit1
{
}
class Banana1 extends Fruit1
{
}

class Basket<T>{
	public T []things;
	public Basket(T...things){
		this.things = things;
	}
	public void sort( Comparator<? super T> comparator){
		Arrays.sort(things, comparator);
	}
}
