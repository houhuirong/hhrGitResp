package com.hhr.array;/*

return����������;
	1�����ط����ķ���ֵ
	2����ֹ��ǰ����
		
*/

public class ReturnDemo{
	
	public static void main(String[] args){
		
		System.out.println(get());
		for(int i = 0;i<10;i++){
			System.out.println(i);
			if(i==5){
				return;
				//System.exit(100);
			}
			System.out.println("����ִ��");
		}
		
	}
	
	public static int get(){
		return 100;
	}
}