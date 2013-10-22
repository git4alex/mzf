package com.zonrong.salerule.service.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zonrong.core.exception.BusinessException;

/**
 * date: 2011-10-19
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class ConditonService<T> {
	public List<Map<String, Object>> fitler(List<Map<String, Object>> rules, T param) throws BusinessException {
		List<Map<String, Object>> newList = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> rule : rules) {
			String json = getJSONString(rule);
			if (getValue(json, param)) {
				newList.add(rule);
			}
		}
		return newList;
	}
	
	abstract String getJSONString(Map<String, Object> rule) throws BusinessException;
	
	abstract boolean getValue(String json, T param) throws BusinessException;
}


