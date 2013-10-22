package com.zonrong.core.security;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

public class AccessDecisionManager implements org.springframework.security.access.AccessDecisionManager {

	private static final Logger logger = Logger.getLogger(AccessDecisionManager.class);

	/**
	 * In this method, need to compare authentication with configAttributes. 
	 * 1, A object is a URL, a filter was find permission configuration by this URL, and pass to here. 
	 * 2, Check authentication has attribute in permission configuration (configAttributes) 
	 * 3, If not match corresponding authentication, throw a AccessDeniedException.
	 */
	public void decide(Authentication authentication, Object object,Collection<ConfigAttribute> configAttributes)
			throws AccessDeniedException, InsufficientAuthenticationException {

		Assert.notNull(object, " object is must not null !");

		if (configAttributes == null) {
			return;
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Check authority " + object.toString());
			logger.debug("This Object need authority is：" + configAttributes);
			//logger.debug("User authority is：" + authentication.getAuthorities());
		}

		Iterator<ConfigAttribute> ite = configAttributes.iterator();
		
		while (ite.hasNext()) {
			ConfigAttribute ca = ite.next();
			
			String needAuthority = ((SecurityConfig) ca).getAttribute();
			for (GrantedAuthority ga : authentication.getAuthorities()) {
				if (needAuthority.equals(ga.getAuthority())) {
					return;
				}
			}
		}
		
		StringBuffer sb=new StringBuffer();
		sb.append("Access is denied on:["+object.toString()+"]\n");
		sb.append("This Object need authority is：" + configAttributes +"\n");
		sb.append("User authority is：" + authentication.getAuthorities());

		throw new AccessDeniedException(sb.toString());
	}

	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	public boolean supports(Class<?> clazz) {
		return true;
	}
}