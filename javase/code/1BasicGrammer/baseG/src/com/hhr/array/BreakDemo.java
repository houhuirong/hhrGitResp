package com.hhr.array;/*
����0-100�������ֱ������88Ϊֹ��ֹͣѭ����
	break:��������ѭ��,���������ѭ����ʱ��breakֻ�������ڲ�ѭ�����޷��������ѭ��
*/

public class BreakDemo{
	
	public static void main(String[] args){
		/*
		int count = 0;
		while(true){
			int i = (int)(Math.random()*101);
			
			if(i==88){
				break;
			}
			count++;
			System.out.println(count+"--"+i);
		}
		*/
		
		//���ӡ���(1,1)(1,2)(1,3)...ֱ�����(6,6)ֹͣ
		for(int i = 1;i<10;i++){
			for(int j =1;j<10;j++){
				System.out.println("("+i+","+j+")");
				if(i==6&&j==6){
					return;
				}
			}
		}
		
	}
}