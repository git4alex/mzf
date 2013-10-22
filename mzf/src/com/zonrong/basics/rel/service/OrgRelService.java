package com.zonrong.basics.rel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.OrgRelType;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.Page;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-11-22
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class OrgRelService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;

	public void updateRel(int orgId, Integer[] orgIds, OrgRelType type, IUser user) throws BusinessException {		
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.ORG_REL);
		
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId1", orgId);
		where.put("type", type);
		entityService.delete(metadata, where, user);
		
		where.clear();
		where.put("orgId2", orgId);
		where.put("type", type);
		entityService.delete(metadata, where, user);
		
		if (ArrayUtils.isEmpty(orgIds)) {
			return;
		}
		
		List<Map<String, Object>> fields = new ArrayList<Map<String,Object>>();		
		for (Integer orgId2 : orgIds) {
			Map<String, Object> field = new HashMap<String, Object>();
			field.put("type", type);
			field.put("orgId1", orgId);
			field.put("orgId2", orgId2);
			field.put("cuserId", null);
			field.put("cdate", null);
			
			fields.add(field);
		}
		entityService.batchCreate(metadata, fields, user);
	}
	
	public boolean isRequireApprove(int orgId1, int orgId2) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.ORG);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), new Integer[]{orgId1, orgId2});
		where.put("type", "HQ");
		List<Map<String, Object>> list = entityService.list(metadata, where, null, User.getSystemUser());		
		if (CollectionUtils.isNotEmpty(list)) {
			return false;
		}
		
		metadata = metadataProvider.getEntityMetadata(MzfEntity.ORG_REL);
		where.clear();
		where.put("orgId1", orgId1);
		where.put("orgId2", orgId2);
		list = entityService.list(metadata, where, null, User.getSystemUser());
		if (CollectionUtils.isNotEmpty(list)) {
			return false;
		}
		
		where.clear();
		where.put("orgId1", orgId2);
		where.put("orgId2", orgId1);
		where.put("type", OrgRelType.transfer);
		list = entityService.list(metadata, where, null, User.getSystemUser());
		if (CollectionUtils.isNotEmpty(list)) {
			return false;
		}
		
		return true;
	}
	
	public Page pageCusOrderSource(List<Map<String, Object>> list, final int offset, final int pageSize, OrderBy orderBy, IUser user) throws BusinessException {
		List<Integer> orgIds = new ArrayList<Integer>();
		orgIds.add(user.getOrgId());
		Map<String, Object> w = new HashMap<String, Object>();
		w.put("orgId1", user.getOrgId());
		w.put("type", OrgRelType.nakedDiamond);
		List<Map<String, Object>> lst = entityService.list(MzfEntity.ORG_REL, w, null, user.asSystem());
		for (Map<String, Object> rel : lst) {
			Integer orgId = MapUtils.getInteger(rel, "orgId2");
			orgIds.add(orgId);
		}
		
		w.clear();
		w.put("orgId2", user.getOrgId());
		w.put("type", OrgRelType.nakedDiamond);
		lst = entityService.list(MzfEntity.ORG_REL, w, null, user.asSystem());
		for (Map<String, Object> rel : lst) {
			Integer orgId = MapUtils.getInteger(rel, "orgId1");
			orgIds.add(orgId);
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(EntityService.FIELD_CODE_KEY, "orgId");
		map.put(EntityService.OPERATOR_KEY, Filter.IN);
		map.put(EntityService.VALUE_KEY, orgIds.toArray(new Integer[]{}));
		
		list.add(map);
		
		return entityService.page(MzfEntity.CUS_ORDER_PRODUCT_SOURCE, list, offset, pageSize, orderBy, user);
	}
}