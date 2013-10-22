package com.zonrong.core.security;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.system.service.AppConfigService;

@Component
public class HttpAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	@Autowired
	private HttpAuthenticationSuccessEventPublisher httpAuthenticationSuccessEventPublisher;
	@Autowired
	private AppConfigService appConfigService;
	@Resource
	private BusinessLogService businessLogService;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		try {
			UserDetails uds = (UserDetails)authentication.getPrincipal();
			IUser user = appConfigService.getUser(uds);
			SessionUtils.setUser(request, user);
			
			Map<String, Object> config = appConfigService.getAppConfig(user);
			SessionUtils.setAppContextConfig(request, config);	
			
//			Map<String, Object> field = new HashMap<String, Object>();
//			field.put("userId", user.getId());
//			field.put("operate", "登陆");
//			field.put("cdate", null);
//			entityService.create(TpltEnumEntityCode.LOG, field, user);
			businessLogService.log("登录", null, user);
		} catch (BusinessException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}		
		
		if(this.httpAuthenticationSuccessEventPublisher!=null){
			HttpAuthenticationSuccessEvent event=new HttpAuthenticationSuccessEvent(authentication,request,response);
			httpAuthenticationSuccessEventPublisher.publishAuthenticationSuccess(event);
		}
		
		super.onAuthenticationSuccess(request, response, authentication);
	}
	public HttpAuthenticationSuccessEventPublisher getHttpAuthenticationSuccessEventPublisher() {
		return httpAuthenticationSuccessEventPublisher;
	}
	public void setHttpAuthenticationSuccessEventPublisher(
			HttpAuthenticationSuccessEventPublisher httpAuthenticationSuccessEventPublisher) {
		this.httpAuthenticationSuccessEventPublisher = httpAuthenticationSuccessEventPublisher;
	}
	
	@Override
	protected boolean isAlwaysUseDefaultTargetUrl() {
		return true;
	}
}
