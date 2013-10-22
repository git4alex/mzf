package com.zonrong.test;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

public class TestInterceptor implements MethodInterceptor {
	private Logger logger=Logger.getLogger(this.getClass());

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		logger.debug("------before invocation.proceed");
		try{
			invocation.proceed();
			Object[] o = invocation.getArguments();
		}catch(Exception e){
			
		}
		
		logger.debug("------after invocation.proceed");
		return null;
	}

}
