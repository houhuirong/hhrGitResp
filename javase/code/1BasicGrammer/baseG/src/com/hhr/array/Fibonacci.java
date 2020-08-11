package com.hhr.array;

import java.util.Scanner;
/*
쳲��������У�����ѡ���ӡ���ٸ�ֵ
*/
public class Fibonacci{
	
	public static void main(String[] args){
		/*
		Scanner sc = new Scanner(System.in);
		System.out.println("������Ҫ��ӡ��쳲��������еĸ���");
		int count = sc.nextInt();
		int x = 1;
		int y = 1;
		int z = 0;
		//ǰ��λ��1
		for(int i = 1;i<=count;i++){
			if(i==1 || i==2){
				System.out.print(1+"\t");
			}else{
				z=x+y;
				x=y;
				y=z;
				System.out.print(z+"\t");
			}
		}
		*/
		for(int i = 1;i<=10;i++){
			System.out.print(getNumber(i)+"\t");
		}
	}
	
	/*
	�ݹ麯����
		�ٳ������й����У���ʱ��Ҫ���ó�������ʱ����ʹ�õݹ�
		ע�⣺
			�ٳ����У��ܲ�ʹ�õݹ�Ͳ�Ҫʹ�õݹ�
				ʹ�õݹ��ʱ���Ӵ���Դ������
				����ݹ�Ĳ�αȽ�������ջ�����
			�����ʹ�õݹ��޷��������Ļ����ͱ���Ҫʹ�õݹ�
				���磺���ĳ������Ŀ¼�µ������ļ�����
	
	*/
	
	public static int getNumber(int number){
		if(number==1||number==2){
			return 1;
		}else{
			return getNumber(number-1)+getNumber(number-2);
		}
	}
	
}