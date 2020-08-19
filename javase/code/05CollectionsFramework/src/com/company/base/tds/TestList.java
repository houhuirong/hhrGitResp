package com.company.base.tds;

import java.util.*;
class TestList {
	public static void main(String[] args){ 
		//List<Photo> album = new ArrayList<>(); 
		List<Photo1> album = new LinkedList<>();

		album.add( new Photo1("one",new Date(), "classroom"));
		album.add( new Photo1("two",new Date(), "library"));
		album.add( new Photo1("three",new Date(), "gym"));
		album.add( new Photo1("three",new Date(), "dorm"));

		Iterator<Photo1> iterator = album.iterator();
		while(iterator.hasNext()){
			Photo1 photo = iterator.next();
			System.out.println( photo.toString() );
		}

		for( Photo1 photo : album ){
			System.out.println( photo );
		}
	}
}
class Photo1 {
	String title;
	Date date;
	String memo;
	Photo1(String title, Date date, String memo){
		this.title = title;
		this.date = date;
		this.memo = memo;
	}
	@Override
	public String toString(){
		return title + "(" + date + ")" + memo;
	}
}
