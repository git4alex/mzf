package com.zonrong.core.templete;

import javax.servlet.http.HttpServletRequest;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.util.SessionUtils;

/**
 * date: 2010-7-20
 * author: wangliang
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class HttpTemplete extends OperateTemplete{
//	public static final String USER_KEY = "CURRENT_LOGIN_USER";
	
	private HttpServletRequest request;
	public HttpTemplete(HttpServletRequest request) {
		this.request = request;
	}
	
	public final IUser getUser() throws BusinessException{		
		IUser user = SessionUtils.getUser(request);
		if (user == null) {
			throw new BusinessException("Session 没有找到用户信息");
		}
		return user;
	}
}


