package com.zonrong.core.export;

import java.util.HashMap;
import java.util.Map;

/**
 * date: 2011-11-10
 *
 * version: 1.0
 * commonts: ......
 */
public class ExportCatch {
	private static Map<String, Object> map = new HashMap<String, Object>();
	
	public static void put(String key, Object value) {
		map.put(key, value);
	}
	
	public static Object getAndRemove(String key) {
		return map.get(key);
	}
}


