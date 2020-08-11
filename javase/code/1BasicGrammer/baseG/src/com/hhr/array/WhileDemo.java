package com.hhr.array;/*
ѭ���ṹ��
	1��while	�Ƚ����жϣ��ٽ����߼�ִ��
		��Ҫ�Ĳ������
			��ʼ���������ĳ�ʼ��
			�����жϣ�����Ҫ�󷵻�true����false��ֵ
			ѭ���壺�����Ҫִ�е��߼�����
			������������ʹ��ѭ������
	2��do while		��ִ�д����߼�����ִ���ж�
		

*/

public class WhileDemo{
	
	public static void main(String [] args){
		
		
		//whileѭ������
		/*
		int i = 1;
		while(i<=100){
			System.out.println("��"+i+"�����");
			i++;
		}
		*/
		
		//��100�ڵ�ż����
		/*
		int i = 1;
		//������յĴ洢����
		int sum = 0;
		while(i<=100){
			if(i % 2 == 0){
				sum+=i;
			}
			i++;
		}
		System.out.println("100���ڵ�ż�����ǣ�"+sum);
		*/
		
		// do while
		/*
		int i = 1;
		do{
			System.out.println("��"+i+"�����");
			i++;
		}while(i<=100);
		*/
		int i = 1;
		int sum = 0;
		do{
			if(i % 2 == 0){
				sum+=i;
			}
			i++;
		}while(i<=100);
		System.out.println("100���ڵ�ż�����ǣ�"+sum);
		
	}
}