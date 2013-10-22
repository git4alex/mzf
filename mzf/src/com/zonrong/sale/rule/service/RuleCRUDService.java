package com.zonrong.sale.rule.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class RuleCRUDService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	//判断重复执行的时间规则
	public  boolean isInTimeRule(String month,String date, String day,String hour){
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		
		int nowMonth = now.get(Calendar.MONTH)+1;     //当前月
		int nowDate = now.get(Calendar.DATE);         //当前日
		int nowDay = now.get(Calendar.DAY_OF_WEEK)-1; //当前星期
		if(nowDay == 0){
			nowDay = 7;
		}
		int nowHour = now.get(Calendar.HOUR_OF_DAY); //当前小时
		
		boolean isInMonth = isInTime(month,nowMonth);
		boolean isInDate = isInTime(date,nowDate);
		boolean isInDay = isInTime(day,nowDay);
		boolean isInHour = isInTime(hour,nowHour);
		if(isInMonth && isInDate && isInDay && isInHour){
			return true;
		}else{
			return false;	
		}
		
	}
	//判断单次执行的时间规则
	public  boolean isInTimeRule(Date start,Date end){
		long startTime = 0;
		long endTime = 0;
		long nowTime = new Date().getTime();
		if(start != null){
			startTime = start.getTime();
		}
		if(end != null){
			endTime = end.getTime();
		}
		
		if(nowTime >= startTime && nowTime <= endTime ){
			return true;
		}else{
			return false;
		}
		
	}
	
	private  boolean isInTime(String time,int nowTime){
		if(time != null){
			List months = Arrays.asList(time.split(","));
			return months.contains(nowTime+"");
		}else{
			return true;
		}
	}
	
}
