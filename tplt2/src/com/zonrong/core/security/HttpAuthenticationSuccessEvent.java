package com.zonrong.core.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.Authentication;

public class HttpAuthenticationSuccessEvent extends ApplicationEvent{

	private static final long serialVersionUID = 1739319991170935354L;
	
	private HttpServletRequest request;
	private HttpServletResponse response;

	public HttpAuthenticationSuccessEvent(Authentication authentication,HttpServletRequest request,HttpServletResponse response) {
		super(authentication);
		this.request = request;
		this.response = response;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
}
