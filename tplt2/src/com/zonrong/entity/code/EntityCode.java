package com.zonrong.entity.code;


/**
 * date: 2011-7-8
 *
 * version: 1.0
 * commonts: ......
 */
public class EntityCode implements IEntityCode {
	private String code;
	
	public EntityCode() {
	}
	
	public EntityCode(String code) {
		this.code = code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
}


