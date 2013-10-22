package com.zonrong.system.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.User;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

@Service
public class ModuleService {
	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(ModuleService.class);
	
	@Resource
	EntityService entityService;
	@Resource
	MetadataProvider metadataProvider;
	
	public String createModule(Map<String,Object> module) throws BusinessException{
		String dbId = entityService.create(TpltEnumEntityCode.MODULE, module, User.getSystemUser());
		module.put("autoSave", "false");
		module.put("dbId", dbId);
		return entityService.create(TpltEnumEntityCode.MODULE_HISTORY, module, User.getSystemUser());
	}
	
	public void updateModule(String id,Map<String,Object> module) throws BusinessException{
		module.remove("id");
		entityService.updateById(TpltEnumEntityCode.MODULE, id, module, User.getSystemUser());
		module.put("dbId", id);
		this.createModuleVersion(id, module);
	}
	
	private void createModuleVersion(String id,Map<String,Object> module) throws BusinessException{
		module.put("autoSave", "false");
		module.remove("id");
		module.put("dbId", id);
		
		EntityMetadata metadata = getMetadataForModuleHistory();
		Filter f = Filter.field(metadata.getColumnName("autoSave")).ne("true").and(Filter.field(metadata.getColumnName("moduleId")).eq(MapUtils.getString(module, "moduleId")));
		OrderBy ob= new OrderBy(new String[]{metadata.getColumnName("updateTime")},OrderByDir.asc);
		List<Map<String,Object>> histories = entityService.list(TpltEnumEntityCode.MODULE_HISTORY, f, ob, User.getSystemUser());
		if(histories != null && histories.size() >= 10){
			String hisId = MapUtils.getString(histories.get(0), "id");
			entityService.deleteById(TpltEnumEntityCode.MODULE_HISTORY, hisId, User.getSystemUser());
		}
		
		entityService.create(TpltEnumEntityCode.MODULE_HISTORY, module, User.getSystemUser());
	}
	
	public void doAutoSave(String id,Map<String,Object> module) throws BusinessException{
		EntityMetadata metadata = getMetadataForModuleHistory();
		module.put("autoSave", "true");
		module.remove("id");
		module.put("dbId", id);
		Filter f = Filter.field(metadata.getColumnName("autoSave")).eq("true");
		OrderBy ob= new OrderBy(new String[]{"updateTime"},OrderByDir.desc);
		List<Map<String,Object>> instance = entityService.list(metadata, f, ob, User.getSystemUser());
		if(instance!=null && instance.size()>0){
			String hisId = MapUtils.getString(instance.get(0), "id");
			entityService.updateById(metadata, hisId, module, User.getSystemUser());
		}else{
			entityService.create(metadata, module, User.getSystemUser());
		}
	}
	
	public void deleteModule(String id) throws BusinessException{
		entityService.deleteById(TpltEnumEntityCode.MODULE, id, User.getSystemUser());
		
		EntityMetadata metadata = getMetadataForModuleHistory();
		Map<String,Object> where = new HashMap<String,Object>();
		where.put(metadata.getColumnName("dbId"), id);
		entityService.delete(metadata, where, User.getSystemUser());
		
	}
	
	private EntityMetadata getMetadataForModuleHistory() throws BusinessException {
		return metadataProvider.getEntityMetadata(TpltEnumEntityCode.MODULE_HISTORY);
	}
}
