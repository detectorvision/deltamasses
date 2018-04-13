package com.detectorvision.utility;

import java.util.Calendar;

public class date2milliseconds {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Calendar myCal=Calendar.getInstance();
		myCal.set(Calendar.MONTH, Calendar.JANUARY);
		myCal.set(Calendar.YEAR, 2019);
		myCal.set(Calendar.DAY_OF_MONTH, 1);
		myCal.set(Calendar.HOUR_OF_DAY,0);
		myCal.set(Calendar.MINUTE,1);
		myCal.set(Calendar.SECOND, 1);
		myCal.toString();
		System.out.println(""+get(myCal));
	}
	
	public static long get(Calendar aCalendar){
		long ms=0;
		ms=aCalendar.getTimeInMillis();
		return ms;
	}

}

