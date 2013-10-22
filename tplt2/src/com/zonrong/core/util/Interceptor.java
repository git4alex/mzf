package com.zonrong.core.util;

import java.util.Map;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;

/**
 * date: 2011-3-7
 *
 * version: 1.0
 * commonts: ......
 */
public class Interceptor {
	public void before(Map<String, Object> map, IUser user)  throws BusinessException {
		
	};
	
	public void after(Map<String, Object> map, IUser user)  throws BusinessException {
		
	};
}


