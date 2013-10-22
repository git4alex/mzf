package com.zonrong.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.zonrong.entity.code.IEntityCode;


/**
 * version: 1.0
 * commonts: ......
 */
@Repository
public class XmlMetadataProvider {
	private static Logger logger = Logger.getLogger(XmlMetadataProvider.class);
		
	private static Map<String, EntityMetadata> xmlMetadataMap;
	
	@Resource
	public void setList(Map<String, EntityMetadata> configMetadataManager) {
		logger.info("ConfigMetadataProvider init...");
		XmlMetadataProvider.xmlMetadataMap = configMetadataManager;
		
		if (configMetadataManager != null) {			
			Iterator<String> it = configMetadataManager.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				EntityMetadata metadata = configMetadataManager.get(key);
				metadata.setCode(key);
				logger.info("entity code: " + key);
			}
		}
		logger.info("ConfigMetadataProvider finished");
	}
	
	public EntityMetadata getMetadataByEntityCode(IEntityCode code) {	
		return xmlMetadataMap.get(code.getCode());
	}
	
	public List<String> listColumnName(IEntityCode code) {
		EntityMetadata metadata = getMetadataByEntityCode(code);
		if (metadata != null) {
			List<String> field = new ArrayList();
			for (FieldMetadata fieldMetadata : metadata.getFieldList()) {
				field.add(fieldMetadata.getColumnName());
			}
			return field;
		}
		return null;
	}
	
	public List<String> listFieldCode(IEntityCode code) {
		EntityMetadata metadata = getMetadataByEntityCode(code);
		if (metadata != null) {
			List<String> field = new ArrayList();
			for (FieldMetadata fieldMetadata : metadata.getFieldList()) {
				field.add(fieldMetadata.getCode());
			}
			return field;
		}
		return null;		
	}
	
	public String getTableName(IEntityCode code) {
		EntityMetadata metadata = getMetadataByEntityCode(code);
		return metadata != null? metadata.getTableName():null;
	}
} 

