package com.blocktyper.spoileralert;

import org.bukkit.World;

public class SpoilerAlertCalendar {
	
	public static final Integer TICKS_IN_A_DAY = 24000;
	public static final Integer DAYS_PER_WEEK = 7;
	public static final Integer WEEKS_PER_MONTH = 4;
	public static final Integer MONTHS_PER_YEAR = 12;
	public static final Integer DAYS_PER_MONTH = (DAYS_PER_WEEK*WEEKS_PER_MONTH);
	
	
	private Long day;
	private Long week;
	private Long month;
	private int year;
	private int monthOfYear;
	private int dayOfMonth;
	private int dayOfWeek;
	private long fullTime;
	
	public SpoilerAlertCalendar(){
		calc(0L);
	}
	
	public SpoilerAlertCalendar(World world){
		this(world.getFullTime());
	}

	public SpoilerAlertCalendar(long fullTime){
		calc(fullTime);
	}
	
	public void calc(long fullTime){
		this.fullTime = fullTime;
		Double preciseDay = fullTime / (SpoilerAlertCalendar.TICKS_IN_A_DAY.doubleValue());
		day = preciseDay.longValue() + 1;
		week = (day / DAYS_PER_WEEK) + (day % DAYS_PER_WEEK > 0 ? 1 : 0);
		month = (week / WEEKS_PER_MONTH) + (week % WEEKS_PER_MONTH > 0 ? 1 : 0);
		year = (month.intValue() / MONTHS_PER_YEAR) + (month.intValue() % MONTHS_PER_YEAR > 0 ? 1 : 0);
		monthOfYear = month.intValue() % MONTHS_PER_YEAR + (month.intValue() % MONTHS_PER_YEAR == 0 ? MONTHS_PER_YEAR : 0);
		dayOfMonth = day.intValue() % DAYS_PER_MONTH + (day.intValue() % DAYS_PER_MONTH == 0 ? DAYS_PER_MONTH : 0);
		dayOfWeek = dayOfMonth % DAYS_PER_WEEK + (dayOfMonth % DAYS_PER_WEEK == 0 ? DAYS_PER_WEEK : 0);
	}
	
	public void addDays(int days) {
		calc(fullTime + days*TICKS_IN_A_DAY);
	}

	public Long getDay() {
		return day;
	}

	public Long getWeek() {
		return week;
	}

	public Long getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public int getMonthOfYear() {
		return monthOfYear;
	}

	public int getDayOfMonth() {
		return dayOfMonth;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}
	
	public String getDisplayDate(){
		return monthOfYear + "/" + dayOfMonth + "/" + year;
	}
	
	public long getFullTime() {
		return fullTime;
	}

	public void setFullTime(long fullTime) {
		this.fullTime = fullTime;
	}

	public static SpoilerAlertCalendar getSpoilersCalendarFromDateString(String dateString){
		String monthString = dateString.substring(0, dateString.indexOf("/"));
		String dayString = dateString.substring(dateString.indexOf("/") + 1, dateString.lastIndexOf("/"));
		String yearString = dateString.substring(dateString.lastIndexOf("/") + 1);
		
		long month = Long.valueOf(monthString)-1;
		long day = Long.valueOf(dayString);
		long year = Long.valueOf(yearString)-1;
		
		long fullTime = (year*MONTHS_PER_YEAR*DAYS_PER_MONTH*TICKS_IN_A_DAY) + (month*DAYS_PER_MONTH*TICKS_IN_A_DAY) + (day*TICKS_IN_A_DAY);
		
		SpoilerAlertCalendar spoilersCalendar = new SpoilerAlertCalendar(fullTime);
		
		return spoilersCalendar;
	}

}
