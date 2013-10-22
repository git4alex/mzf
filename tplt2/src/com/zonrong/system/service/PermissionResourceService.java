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
public class PermissionResourceService {
	private static Logger logger = Logger.getLogger(PermissionResourceService.class);
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;

//	public List<?> queryListResource(String code, Object permissionId) throws BusinessException {		
//		Map parameterMap = new HashMap();
//		parameterMap.put("permissionId", permissionId);
//		return super.queryListEntityData(code, parameterMap, null);
//	}
	
	public void allocateResource(IEntityCode code, Object permissionId, Object[] resource, IUser user) throws BusinessException {
//		Map parameterMap = new HashMap();
//		parameterMap.put("PERMISSION_ID", permissionId);
//		super.delete(code, parameterMap);
		
		List<Map<String, Object>> list = new ArrayList();
		for (Object object : resource) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("permissionId", permissionId);
			parameters.put("resourceId", object);
			parameters.put("cdate", null);
			list.add(parameters);
		}
		entityService.batchCreate(code, list, user);
	}
	
	public void deleteResource(IEntityCode code, Object[] id, IUser user) throws BusinessException {		
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put(metadata.getPkCode(), id);
		entityService.delete(metadata, filterMap, user);
	}	
}