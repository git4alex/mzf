package com.zonrong.system.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.IEntityCode;
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
public class UserRoleService {
	private static Logger logger = Logger.getLogger(UserRoleService.class);
	
	@Resource
	private MetadataProvider metadataProvider;	
	@Resource
	private EntityService entityService;

//	public List<?> queryListRole(String code, Object userId) throws BusinessException {		
//		Map parameterMap = new HashMap();
//		parameterMap.put("userId", userId);
//		return super.queryListEntityData(code, parameterMap, null);
//	}
	
	public void allocateRole(IEntityCode code, Object userId, Object[] roleId, IUser user) throws BusinessException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("userId", userId);
		entityService.delete(code, parameterMap, user);
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Object object : roleId) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("userId", userId);
			parameters.put("roleId", object);
			parameters.put("cdate", null);
			list.add(parameters);
		}
		entityService.batchCreate(code, list, user);
	}
	
	public void deleteRole(IEntityCode code, Object[] id, IUser user) throws BusinessException {		
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		Map filterMap = new HashMap();
		filterMap.put(metadata.getPkCode(), id);
		entityService.delete(metadata, filterMap, user);
	}	
}