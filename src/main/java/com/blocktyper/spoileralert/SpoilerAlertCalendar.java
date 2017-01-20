package com.blocktyper.spoileralert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bukkit.World;
import org.bukkit.entity.HumanEntity;

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
	
	public long getFullTime() {
		return fullTime;
	}

	public void setFullTime(long fullTime) {
		this.fullTime = fullTime;
	}
	
	public String getNbtDateString(){
		String dateFormat = SpoilerAlertPlugin.NBT_DATE_FORMAT;
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Calendar cal = new GregorianCalendar();
		
		cal.set(Calendar.MONTH, monthOfYear - 1);
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		cal.set(Calendar.YEAR, year);
		return sdf.format(cal.getTime());
	}
	
	public String getDateString(HumanEntity player, SpoilerAlertPlugin plugin){
		
		String dateFormat = plugin.getPlayerDateFormat(player);
		
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Calendar cal = new GregorianCalendar();
		
		cal.set(Calendar.MONTH, monthOfYear - 1);
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		cal.set(Calendar.YEAR, year);
		return sdf.format(cal.getTime());
	}

	public static SpoilerAlertCalendar getSpoilersCalendarFromDateString(String dateString){
		
		if(dateString == null || dateString.isEmpty()){
			return null;
		}
		try {
			String dateFormat = SpoilerAlertPlugin.NBT_DATE_FORMAT;
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			Date date = sdf.parse(dateString);
			
			Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int year = cal.get(Calendar.YEAR) - 1;
			
			long fullTime = (year*MONTHS_PER_YEAR*DAYS_PER_MONTH*TICKS_IN_A_DAY) + (month*DAYS_PER_MONTH*TICKS_IN_A_DAY) + (day*TICKS_IN_A_DAY);
			
			SpoilerAlertCalendar spoilerAlertCalendar = new SpoilerAlertCalendar(fullTime);
			
			return spoilerAlertCalendar;
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		return null;
	}

}
