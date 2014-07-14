package com.scampus.tools;

import java.util.Calendar;

public class DateParser {
	public DateParser() {
		
	}
	
	public Calendar parse(String dateString) {
		String[] dateAndTime = dateString.split("T");
		String[] dates = dateAndTime[0].split("-");
		String[] timeAndLocale = dateAndTime[1].split("-");
		String[] timeSplit = timeAndLocale[0].split(":");
		//TODO setear tiempo local
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), 
				Integer.parseInt(dates[2]), Integer.parseInt(timeSplit[0]),
				Integer.parseInt(timeSplit[1]), Integer.parseInt(timeSplit[2]));
		return cal;
	}
	
	public Calendar parseBirthday (String birthday, String separator) {
		// Cumpleaños viene en la forma yyyy mm dd
		String[] dates = birthday.split(separator);
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]),
				Integer.parseInt(dates[2]));
		return cal;
	}
}
