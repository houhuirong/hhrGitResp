package com.company.array;

public class BuyChicken{

	public static void main(String[] args){

		for(int i = 0;i<=20;i++){
			for(int j = 0;j<=34;j++){
				for(int k = 0;k<=300;k++){
					if(((i+j+k)==100) && ((5*i+3*j+ k/3)==100) && (k%3==0)){
						System.out.println("公鸡："+i+"\t母鸡："+j+"\t小鸡："+k);
					}
				}
			}
		}
	}
}