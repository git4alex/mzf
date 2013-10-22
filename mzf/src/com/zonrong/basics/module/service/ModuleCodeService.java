package com.zonrong.basics.module.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;


@Service
public class ModuleCodeService {

	@Resource
	private EntityService entityService;
	
	public String getComponent(String moduleId,String componentId, IUser user)throws BusinessException{
		try {
			Map<String,Object> where = new HashMap<String,Object>();
			where.put("moduleId", moduleId);
			
			List<Map<String,Object>> modules = entityService.list(TpltEnumEntityCode.MODULE, where, null, user.asSystem());
			if (CollectionUtils.isEmpty(modules)) {
				throw new BusinessException("not found module");
			} else if (modules.size() > 1) {
				throw new BusinessException("expect 1 actual multiple");
			} 
			
			Map<String,Object> module = module = modules.get(0);;			
			Map<String, Object> map = new ObjectMapper().configure(
					Feature.ALLOW_UNQUOTED_FIELD_NAMES, true).readValue(
					module.get("config").toString(), HashMap.class);

			List<Map<String, Object>> components = (List<Map<String, Object>>) map.get("components");
			for (Map<String, Object> component : components) {

				System.out.println(component.get("id").toString());
				if (component.get("id").toString().equals(componentId)) {
					return component.toString();
				}
			}			 
		} catch (Exception e1) {
			throw new BusinessException("");
		}
		
		return null;
	}
	
}
