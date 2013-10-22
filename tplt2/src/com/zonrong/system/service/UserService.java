package com.zonrong.system.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-7-26
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class UserService{
	private static Logger logger = Logger.getLogger(UserService.class);
	
	@Resource
	private MetadataProvider metadataProvider;	
	@Resource
	private EntityService entityService;	
	
	public void createUser(Map<String, Object> parameters, IUser user) throws BusinessException {
		String loginName = MapUtils.getString(parameters, "loginName");
		if (!exists(loginName, user)) {			
			entityService.create(TpltEnumEntityCode.USER, parameters, user);
		} else {			
			throw new BusinessException(loginName + " is exists");
		}
	}
	
	public boolean exists(String loginName, IUser user) {
		try {			
			EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.USER);
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("loginName", loginName);
			List list = entityService.list(metadata, parameterMap, null, user.asSystem());
			if (list.size() > 0) 
				return true;
		} catch (BusinessException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return false;
	}
	
	public void updatePassword(IUser user, String oldPassword, String newPassword) throws BusinessException {
		if (StringUtils.isBlank(oldPassword)) {
			throw new BusinessException("旧密码不能为空");
		}
		if (StringUtils.isBlank(newPassword)) {
			throw new BusinessException("新密码不能为空");
		}
		
		EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.USER);
		
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("password", newPassword);
		
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", user.getId());
		where.put("password", oldPassword);
		
		int row = entityService.update(metadata, field, where, user);
		if (row != 1) {
			throw new BusinessException("修改密码失败");
		}
	}
}


