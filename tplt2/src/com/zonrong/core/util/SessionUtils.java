package com.zonrong.core.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.zonrong.core.security.IUser;

/**
 * date: 2010-12-6
 *
 * version: 1.0
 * commonts: ......
 */
public class SessionUtils {
	private static final String USER_KEY = "CURRENT_LOGIN_USER";
	private static final String APP_CONTEXT_CONFIG_KEY = "APP_CONTEXT_CONFIG_KEY";
	
	public final static IUser getUser(HttpServletRequest request) {
		Object user = request.getSession().getAttribute(USER_KEY);
		if (user != null) {
			return (IUser) user;
		} else {
			return null;
		}
	}
	
	public final static void setUser(HttpServletRequest request, IUser user) {
		request.getSession().setAttribute(USER_KEY, user);
	}
	
	public final static Map<String, Object> getAppContextConfig(HttpServletRequest request) {
		Object config = request.getSession().getAttribute(APP_CONTEXT_CONFIG_KEY);
		if (config != null) {
			return (Map<String, Object>) config;
		} else {
			return null;
		}
	}
	
	public final static void setAppContextConfig(HttpServletRequest request, Map<String, Object> config) {
		request.getSession().setAttribute(APP_CONTEXT_CONFIG_KEY, config);
	}	
}


