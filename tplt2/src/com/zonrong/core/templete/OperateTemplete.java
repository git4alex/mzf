package com.zonrong.core.templete;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.zonrong.core.exception.BusinessException;

/**
 * date: 2010-7-20
 * author: wangliang
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class OperateTemplete {
	private Logger logger = Logger.getLogger(this.getClass());
	private Map<String, Object> map = new HashMap<String, Object>();

	public void put(String key, Object value) {
		map.put(key, value);
	}

	public Map<String, Object> operate() {
		try {
			doSomething();	
			map.put("success", true);
		} catch (BusinessException e) {
			e.printStackTrace();
			map.put("success", false);
			map.put("msg", e.getMessage());
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			map.put("success", false);
			map.put("msg", e.getMessage() == null? new Date().toString() + ": " + e.getClass() : e.getMessage());
			logger.error(e.getMessage(), e);
		}
		return map;		
	}
	
	protected abstract void doSomething() throws BusinessException;
}


