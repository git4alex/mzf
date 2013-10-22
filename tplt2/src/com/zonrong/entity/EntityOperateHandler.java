package com.zonrong.entity;

import org.aopalliance.intercept.MethodInvocation;

public abstract class EntityOperateHandler {
	public abstract void beforeOperate(MethodInvocation invocation);
	public abstract void afterOperate(MethodInvocation invocation);
	public abstract String getEntityCode();
	public Object process(MethodInvocation invocation) throws Throwable{
		Object ret=null;
		this.beforeOperate(invocation);
		ret=invocation.proceed();
		this.afterOperate(invocation);
		
		return ret;
	};
}
