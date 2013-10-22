package com.zonrong.system;

import javax.annotation.Resource;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.zonrong.core.dao.Dao;
import com.zonrong.entity.EntityOperateHandler;
import com.zonrong.metadata.service.MetadataProvider;

@Component
public class UserOperateHandler extends EntityOperateHandler {
	private Logger logger=Logger.getLogger(this.getClass());
	
	@Resource
	private Dao dao;
	
	@Resource
	private MetadataProvider metadataprovider;
	
	@Override
	public void beforeOperate(MethodInvocation invocation) {

	}

	@Override
	public void afterOperate(MethodInvocation invocation) {
		logger.debug("after user operate");
	}
	
	@Override
	public String getEntityCode(){
		return "user";
	}

}
