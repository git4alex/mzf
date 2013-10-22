package com.zonrong.basics;

import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.util.Carrier;

/**
 * date: 2011-3-14
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class StatusCarrier extends Carrier {
	
	public abstract void active(Map<String, Object> carrier) throws BusinessException;
		
	private Map<String, Object> carrier;
	public void setCarrier(Map<String, Object> carrier) throws BusinessException {
		this.carrier = carrier;
	}
	
	public String getNum() throws BusinessException {
		return MapUtils.getString(carrier, "num");
	}
	public <T extends Enum> T getStatus(Class<T> enumType) throws BusinessException {
		String name = MapUtils.getString(carrier, "status");
		return (T)Enum.valueOf(enumType, name);
	}
}


