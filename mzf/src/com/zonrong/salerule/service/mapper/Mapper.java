package com.zonrong.salerule.service.mapper;

import java.util.HashMap;
import java.util.Map;

/**
 * date: 2011-10-27
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class Mapper<K, E> extends HashMap<String, String> {
	public abstract void load(Map<String, Object> box);
	

	public static class KeywordsMapper<K,V> extends Mapper<String, String> {	
		public KeywordsMapper() {
			super();
					
			this.put("并且", " and ");
			this.put("或者", " or ");
		}
		
		public void load(Map<String, Object> box) {
			
		};
	}
}
