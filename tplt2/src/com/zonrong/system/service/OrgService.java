package com.zonrong.system.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.security.User;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-7-27
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class OrgService {
	private static Logger logger = Logger.getLogger(OrgService.class);
	
	private static Map<String, Map> orgMap = null;
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;

	@PostConstruct
	public void load() {
		orgMap = new HashMap<String, Map>();
		try {
			EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.ORG);
			List<Map<String, Object>> list = entityService.list(metadata, new HashMap<String, Object>(), null, User.getSystemUser());
			for (Map<String, Object> map : list) {
				orgMap.put(MapUtils.getString(map, metadata.getPkCode()), map);				
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}
	
	public Map<String, Object> getOrg(int id) {		
		if (orgMap == null) {
			load();
		}
		return orgMap.get(Integer.toString(id));
	}	
	
	public String getOrgName(int id) {
		Map<String, Object> org = getOrg(id);
		return MapUtils.getString(org, "fullName");
	}
}


