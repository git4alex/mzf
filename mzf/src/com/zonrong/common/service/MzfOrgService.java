package com.zonrong.common.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;
import com.zonrong.system.service.OrgService;

/**
 * date: 2011-3-7
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class MzfOrgService extends OrgService {
	public enum OrgType {
		HQ,				//总部
		store,			//门店
		subdept			//区域
	}
	
	@Resource
	private EntityService entityService;
	
	public Map<String, Object> getHQOrg() throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("type", OrgType.HQ);
		
		List<Map<String, Object>> list = entityService.list(MzfEntity.ORG, where, null, User.getSystemUser());
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("未知道类型为总部的部门");
		} else if (list.size() > 1) {
			throw new BusinessException("找到多个类型为总部的部门");
		}
		
		return list.get(0);
	}
	
	public int getHQOrgId() throws BusinessException {
		Map<String, Object> org = getHQOrg();
		return MapUtils.getInteger(org, "id");
	}
	
	public boolean isHq(int orgId) throws BusinessException {
		Map<String, Object> org = entityService.getById(MzfEntity.ORG, orgId, User.getSystemUser());
		OrgType orgType = OrgType.valueOf(MapUtils.getString(org, "type"));
		
		if (orgType == OrgType.HQ) {
			return true;
		}
		return false;
	}
	
	public boolean isSubdept(int orgId) throws BusinessException {
		Map<String, Object> org = entityService.getById(MzfEntity.ORG, orgId, User.getSystemUser());
		OrgType orgType = OrgType.valueOf(MapUtils.getString(org, "type"));
		
		if (orgType == OrgType.subdept) {
			return true;
		}
		return false;
	}
	
	public boolean isStore(int orgId) throws BusinessException {
		Map<String, Object> org = entityService.getById(MzfEntity.ORG, orgId, User.getSystemUser());
		OrgType orgType = OrgType.valueOf(MapUtils.getString(org, "type"));
		
		if (orgType == OrgType.store) {
			return true;
		}
		return false;
	}
}


