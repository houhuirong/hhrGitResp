package com.hhr.array;

/*
��100~150֮�䲻�ܱ�3�������������

	continue:��������ѭ��
*/
public class ContinueDemo{
	
	public static void main(String[] args){
		
		for(int i = 100;i<150;i++){
			if(i%3==0){
				continue;
			}
			System.out.println(i);
		}
	}
}