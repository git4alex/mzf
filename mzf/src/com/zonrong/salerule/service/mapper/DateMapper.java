package com.zonrong.salerule.service.mapper;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

/**
 * date: 2011-10-20
 *
 * version: 1.0
 * commonts: ......
 */
public class DateMapper<K,V> extends Mapper<String, String> {	
	public Map<String, Object> date = new HashMap<String, Object>();
	
	public Map<String, Object> getDate() {
		return date;
	}
	
	public DateMapper(Map<String, Object> date) {
		super();
		this.date = date;
		
		this.put("起始时间", "startDateTime");
		this.put("结束时间", "endDateTime");
		this.put("指定时间", "paramDateTime");
	}
	
	public void load(Map<String, Object> box) {
		box.put("startDateTime", MapUtils.getObject(date, "startDateTime"));
		box.put("endDateTime", MapUtils.getObject(date, "endDateTime"));
		box.put("paramDateTime", MapUtils.getObject(date, "paramDateTime"));
	}
	
	
	public static long getTime(Date date) {
		return date.getTime();
	}
	
	public static int getMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.MONTH); // 当前月

	}
	
	public static int getDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.DATE); // 当前日

	}
	
	public static int getWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.DAY_OF_WEEK); // 当前星期

	}
	
	public static int getHour(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.HOUR_OF_DAY); // 当前小时
	}
}


