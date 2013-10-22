package com.zonrong.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;

import com.zonrong.metadata.EntityMetadata;

@Component
public class EntityServiceInterceptor implements MethodInterceptor {
	
	@Resource
	private List<EntityOperateHandler> handlers=new ArrayList<EntityOperateHandler>();

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		for(EntityOperateHandler handler:handlers){
			if(isEnable(invocation,handler)){
				return handler.process(invocation);
			}
		}

		return invocation.proceed();
	}
	
	public boolean isEnable(MethodInvocation invocation,EntityOperateHandler handler) {
		String entityCode=handler.getEntityCode();
		
		Object[] args=invocation.getArguments();
		String ret="";
		if(args.length>0){
			if(args[0] instanceof EntityMetadata){
				EntityMetadata em=(EntityMetadata)args[0];
				ret=em.getCode();
			}else if(args[0] instanceof String){
				ret=(String)args[0];
			}
		}
		
		return ret.equalsIgnoreCase(entityCode);
	}
}
