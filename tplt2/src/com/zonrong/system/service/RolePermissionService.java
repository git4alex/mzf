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
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-7-26
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class RolePermissionService {
	private static Logger logger = Logger.getLogger(RolePermissionService.class);
	
	@Resource
	private MetadataProvider metadataProvider;	
	@Resource
	private EntityService entityService;

//	public List<?> queryListPermission(String code, Object roleId) throws BusinessException {		
//		Map parameterMap = new HashMap();
//		parameterMap.put("roleId", roleId);
//		return super.queryListEntityData(code, parameterMap, null);
//	}
	
	public void allocatePermission(IEntityCode code, Object roleId, Object[] permissionId, IUser user) throws BusinessException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("roleId", roleId);
		entityService.delete(code, parameterMap, user);
		
		List<Map<String, Object>> list = new ArrayList();
		for (Object object : permissionId) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("roleId", roleId);
			parameters.put("permissionId", object);
			parameters.put("cdate", null);
			list.add(parameters);
		}
		entityService.batchCreate(code, list, user);
	}
	
//	public void deletePermission(String code, Object[] id) throws BusinessException {		
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
//		Map filterMap = new HashMap();
//		filterMap.put(metadata.getIdField(), id);
//		super.delete(metadata, filterMap);
//	}	
}