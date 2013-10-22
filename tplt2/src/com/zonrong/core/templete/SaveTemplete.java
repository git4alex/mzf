package com.zonrong.core.templete;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;

/**
 * date: 2010-11-23
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class SaveTemplete {
	private EntityService entityService;
	public SaveTemplete(EntityService entityService) {
		this.entityService = entityService;
	}
	
	protected abstract EntityMetadata getEntityMetadata() throws BusinessException;
	
	protected abstract void setForeignKey(Map<String, Object> map) throws BusinessException;
	
	public void save(List<Map<String, Object>> list, IUser user) throws BusinessException {
		EntityMetadata metadata = getEntityMetadata();
		
		Map<String, Object> where = new HashMap<String, Object>();
		setForeignKey(where);
		List<Map<String, Object>> dbList = entityService.list(metadata, where, null, user.asSystem());
		Map<String, Map<String, Object>> dbMap = new HashMap<String, Map<String,Object>>();
		for (Map<String, Object> map : dbList) {
			String id = MapUtils.getString(map, metadata.getPkCode());
			dbMap.put(id, map);
		}
		
		if (CollectionUtils.isNotEmpty(list)) {
			for (Map<String, Object> map : list) {
				Integer id = MapUtils.getInteger(map, metadata.getPkCode());
				if (id != null) {
					if (dbMap.get(id.toString()) != null) {
						setForeignKey(map);
						map.remove(metadata.getPkCode());
						entityService.updateById(metadata, Integer.toString(id), map, user);
						dbMap.remove(id.toString());
					} 
				} else {
					setForeignKey(map);
					entityService.create(metadata, map, user);
				}
			}
			
			if (dbMap.keySet().size() > 0) {
				where.clear();
				where.put(metadata.getPkCode(), dbMap.keySet().toArray(new String[]{}));
				entityService.delete(metadata, where, user);
			}
		} else {
			entityService.delete(metadata, where, user);
		}
	}		
}


