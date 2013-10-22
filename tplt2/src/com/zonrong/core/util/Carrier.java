package com.zonrong.core.util;

import java.util.Map;

import com.zonrong.core.exception.BusinessException;

/**
 * date: 2011-3-7
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class Carrier {
	public abstract void active(Map<String, Object> carrier) throws BusinessException;
}


