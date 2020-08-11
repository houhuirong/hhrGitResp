package com.hhr.array;

import java.util.Scanner;
public class AgeRate{
	
	public static void main(String[] args){
		
		//����Scanner����
		Scanner sc = new Scanner(System.in);
		//�洢����30�������
		int ageUp = 0;
		//�洢С��30�������
		int ageDown = 0;
		
		for(int i = 0;i<10;i++){
			System.out.println("�������"+i+"λ�˿͵����䣺");
			int age = sc.nextInt();
			if(age>30){
				ageUp++;
			}else{
				ageDown++;
			}
		}
		
		System.out.println("����30������������ǣ�"+(ageUp/10.0*100)+"%");
		System.out.println("С��30������������ǣ�"+(ageDown/10.0*100)+"%");
	}
	
}