package com.zonrong.common.service;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.HttpAuthenticationSuccessEvent;
import com.zonrong.core.security.IUser;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.shiftwork.service.ShiftWorkService;
import com.zonrong.system.service.AppConfigService;

/**
 * date: 2011-3-14
 *
 * version: 1.0
 * commonts: ......
 */
@Component
public class AuthenticationSuccessListener implements ApplicationListener<HttpAuthenticationSuccessEvent> {
	private static final Logger logger = Logger.getLogger(AuthenticationSuccessListener.class);
	
	@Resource
	private AppConfigService appConfigService;
	@Resource
	private ShiftWorkService shiftWorkService;
	public void onApplicationEvent(HttpAuthenticationSuccessEvent event) {
		HttpServletRequest request = event.getRequest();
		Authentication authentication = (Authentication)event.getSource();
		UserDetails uds = (UserDetails)authentication.getPrincipal();
		
		try {
			IUser user = appConfigService.getUser(uds);
			if(logger.isDebugEnabled()){
				logger.debug("user: " + user.getName());
			}
			
			Map<String, Object> shiftWork = shiftWorkService.getShiftWorkCode(user);
			shiftWorkService.reset(shiftWork, user);		

			Map<String, Object> config = SessionUtils.getAppContextConfig(request);
			config.put("shiftWork", shiftWork);	
		} catch (BusinessException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage());
			}
		}
	}

}


