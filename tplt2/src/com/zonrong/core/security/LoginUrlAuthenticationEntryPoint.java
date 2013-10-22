package com.zonrong.core.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.AuthenticationException;

public class LoginUrlAuthenticationEntryPoint extends org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint {
	private String ajaxLoginFormUrl;

	@Override
	protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) {
		
		String xRequestedWith = request.getHeader("X-Requested-With");
		if(StringUtils.isNotBlank(xRequestedWith) && xRequestedWith.equalsIgnoreCase("XMLHttpRequest")){
			return this.getAjaxLoginFormUrl();
		}
		
		if(StringUtils.isNotBlank((String)request.getParameter("_dc"))){
			return this.getAjaxLoginFormUrl();
		}
		
		return super.determineUrlToUseForThisRequest(request, response, exception);
	}

	public String getAjaxLoginFormUrl() {
		return ajaxLoginFormUrl;
	}

	public void setAjaxLoginFormUrl(String ajaxLoginFormUrl) {
		this.ajaxLoginFormUrl = ajaxLoginFormUrl;
	}
}
