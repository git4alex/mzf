package com.zonrong.core.dao.dialect;


/**
 * date: 2010-11-12
 *
 * version: 1.0
 * commonts: ......
 */
public enum DBFun{
	date,
	currentDate,
	defaultValue;
	
	public static DBFun parse(Object obj) {
		if (DBFun.date.name().equals(obj.toString())) {				
			return date;
		}		
		if (DBFun.currentDate.name().equals(obj.toString())) {				
			return currentDate;
		}
		if (DBFun.defaultValue.name().equals(obj.toString())) {				
			return defaultValue;
		}	
		return null;
	}
}


