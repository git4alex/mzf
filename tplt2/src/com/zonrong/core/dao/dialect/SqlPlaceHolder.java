package com.zonrong.core.dao.dialect;
/**
 * date: 2010-11-12
 *
 * version: 1.0
 * commonts: ......
 */
public class SqlPlaceHolder {
	private String variable;
	
	public SqlPlaceHolder(String name) {
		this.variable = name;
	}
	
	public String toString() {
		return ":" + variable;
	}
	
	public String getName() {
		return this.variable;
	}
}


