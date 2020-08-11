package com.hhr.array;/*
��ά������Գ������������
	�����ά�����ʱ��һ����Ҫע�⣬����Ҫ��������ĳ���
*/

public class TwoArray{
	
	public static void main(String[] args){
		
		int[] arr = new int[6];
		int[][] arr2 = new int[3][];
		//������ά����Ķ���
		arr2[0] = new int[5];
		arr2[1] = new int[3];
		arr2[2] = new int[4];
		//��ֵ
		arr2[0][0] = 1;
		arr2[0][1] = 2;
		arr2[0][2] = 3;
		arr2[0][3] = 4;
		arr2[0][4] = 5;
		
		arr2[1][0] = 6;
		arr2[1][1] = 7;
		arr2[1][2] = 8;
		
		arr2[2][0] = 9;
		arr2[2][1] = 10;
		arr2[2][2] = 11;
		arr2[2][3] = 12;
		
		for(int i = 0;i<arr2.length;i++){
			for(int j = 0;j<arr2[i].length;j++){
				System.out.print(arr2[i][j]+"\t");
			}
			System.out.println();
		}
		
		
	}
}