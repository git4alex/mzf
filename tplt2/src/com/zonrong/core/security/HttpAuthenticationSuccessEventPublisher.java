package com.zonrong.core.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Component
public class HttpAuthenticationSuccessEventPublisher implements ApplicationEventPublisherAware {
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
	
	public void publishAuthenticationSuccess(HttpAuthenticationSuccessEvent event){
        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(event);
        }
	}
}
