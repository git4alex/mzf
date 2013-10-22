package com.zonrong.salerule.service.result;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.zonrong.core.exception.BusinessException;

/**
 * date: 2011-10-21
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class ResultService<T> {
	abstract String getJSONString(Map<String, Object> result) throws BusinessException;
	
	public String getResultName(Map<String, Object> result) throws BusinessException {
		return MapUtils.getString(result, "resultName");		
	}
	
	public abstract T getResult(List<Map<String, Object>> results, int orgId) throws BusinessException;
}


