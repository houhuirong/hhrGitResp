package com.company.work;

import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateTest {
	public static void main(String[] args){
		//???????????????????????·????????轫?????????????????????У?
		boolean exportAll = false;
		String yearMonth="2020-06";
		Date today = ExDateUtils.getCurrentDateTime();
		Date yesterday= ExDateUtils.add(today, Calendar.DAY_OF_MONTH, -1);
		//???????????????
		String currentStringyearMonth = ExDateUtils.dateToString(yesterday,"yyyy-MM" );
		//?????????????????
		String currentStringyearMonthDate =   ExDateUtils.dateToString(yesterday,"yyyy-MM-dd" );
		if(currentStringyearMonth.compareTo(yearMonth) != 0){
			exportAll = true;
		}
		String[] monthStr = yearMonth.split("-");
		String year = monthStr[0];
		String month = monthStr[1];
		Calendar calendar = Calendar.getInstance();
	    calendar.set(Calendar.YEAR, Integer.valueOf(year));// ????????????????
	    calendar.set(Calendar.MONTH, Integer.valueOf(month) - 1);// ?????·?
	    System.out.println(calendar.getTime());
	    int daySizeAll = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	    String begeinTime = month +"."+ "01";//???
	    String endTime = month +"."+ String.valueOf(daySizeAll);//???
	}

}
