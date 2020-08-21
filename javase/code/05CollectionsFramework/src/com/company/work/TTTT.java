package com.company.work;

import java.util.Calendar;

public class TTTT {

	@SuppressWarnings("null")
	public static void main(String[] args) {
		String text="";
		String pal="/home/erpftpu"+"/"+"a.txt";
		//System.out.println(pal);
		for(int i=0;i<3;i++){
			text+=pal+"\n\n";
		}
		System.out.println(text);

		String s="send_offer";
		System.out.println(s.toUpperCase());
		
		String basePath="/home/erpftpu";
		String filePath="/home/erpftpu/department/2020/5/22/���ŵ����ʼ�����_1590138131446.xlsx";
		System.out.println(filePath.startsWith(basePath));
		filePath=filePath.substring(basePath.length());
		System.out.println(filePath);
		
		Calendar nowTime=Calendar.getInstance();
		nowTime.add(Calendar.MINUTE, 6);
		System.out.println(nowTime.getTime());
		Integer send=null;
		Integer type=0;
		if(type!=1){
			System.out.println("00");
		}
	}

}
