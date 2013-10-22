package com.zonrong.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

/**
 * date: 2010-11-15
 * 
 * version: 1.0 commonts: ......
 */
public class TreeConfig {
	private TreeConfig() {

	};

	public static String PID_CODE = "pidCode";

	public static String INDEX_CODE = "indexCode";

	public static String PATH_CODE = "pathCode";

	private Map<String, String> config = new HashMap<String, String>();

	private void put(String key, String value) {
		config.put(key, value);
	}

	public static TreeConfig getTreeConfig(Map parameter) {
		TreeConfig config = new TreeConfig();
		if (MapUtils.isEmpty(parameter)) {
			return config;
		}
		
		if (parameter.containsKey(PID_CODE)) {
			config.put(PID_CODE, MapUtils.getString(parameter, PID_CODE));
			parameter.remove(PID_CODE);
		}
		if (parameter.containsKey(INDEX_CODE)) {
			config.put(INDEX_CODE, MapUtils.getString(parameter, INDEX_CODE));
			parameter.remove(INDEX_CODE);
		}
		if (parameter.containsKey(PATH_CODE)) {
			config.put(PATH_CODE, MapUtils.getString(parameter, PATH_CODE));
			parameter.remove(PATH_CODE);
		}

		return config;
	}

	public String getPidCode() {
		return config.get(PID_CODE);
	}

	public String getIndexCode() {
		return config.get(INDEX_CODE);
	}

	public String getPathCode() {
		return config.get(PATH_CODE);
	}
}
