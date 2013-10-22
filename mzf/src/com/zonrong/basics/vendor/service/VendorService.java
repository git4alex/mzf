package com.zonrong.basics.vendor.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.Page;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.templete.SaveTemplete;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-9-28
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class VendorService {
	private Logger logger = Logger.getLogger(this.getClass());
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	
	public Page page(List<Map<String,Object>> where, int start, int limit, OrderBy orderBy, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR);
		String type = null;
		for (Map<String, Object> w : where) {
			String fieldCode = MapUtils.getString(w, EntityService.FIELD_CODE_KEY);
			if ("type".equals(fieldCode)) {
				type = MapUtils.getString(w, EntityService.VALUE_KEY);
				where.remove(w);
				break;
			}
		}
		
		Filter filter = EntityService.createFilter(metadata, where);
		if (StringUtils.isNotBlank(type)) {
			String[] types = type.split("[,]");
			String colName = metadata.getColumnName("type");
			Filter f = null;
			for (String s : types) {
				if (f == null) {					
					f = Filter.field(colName).like("%" + s + "%");
				} else {
					f = f.or(Filter.field(colName).like("%" + s + "%"));
				}
			}
			
			filter = f.and(filter);
//			filter.and(f);
		}
		return entityService.page(metadata, filter, start, limit, orderBy, user);
	}
	
	public int createVendor(Map<String, Object> vendor, List<Map<String, Object>> linkmanList, List<Map<String, Object>> accountList, IUser user) throws BusinessException {
		EntityMetadata vendorMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR);
		String id = entityService.create(vendorMetadata, vendor, user);
		int vendorId = Integer.parseInt(id);
		
		saveLinkman(vendorId, linkmanList, user);
		saveAccoun(vendorId, accountList, user);

		return vendorId;
	}
	
	public void updateVendor(int vendorId, Map<String, Object> vendor, List<Map<String, Object>> linkmanList, List<Map<String, Object>> accountList, IUser user) throws BusinessException {
		EntityMetadata vendorMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR);
		int row = entityService.updateById(vendorMetadata, Integer.toString(vendorId), vendor, user);
		if (row == 0) {
			throw new BusinessException("未能修改供应商资料");
		}
		
		saveLinkman(vendorId, linkmanList, user);
		saveAccoun(vendorId, accountList, user);		
	}
	
	private void saveLinkman(final int vendorId, List<Map<String, Object>> linkmanList, IUser user) throws BusinessException {
		SaveTemplete templete = new SaveTemplete(entityService) {
			protected EntityMetadata getEntityMetadata() throws BusinessException {
				return metadataProvider.getEntityMetadata(MzfEntity.LINKMAN);
			}
			protected void setForeignKey(Map<String, Object> map) throws BusinessException {
				map.put("vendorId", vendorId);
			}			
		};
		
		templete.save(linkmanList, user);
	}
	
	private void saveAccoun(final int vendorId, List<Map<String, Object>> accountList, IUser user) throws BusinessException {
		SaveTemplete templete = new SaveTemplete(entityService) {
			protected EntityMetadata getEntityMetadata() throws BusinessException {
				return metadataProvider.getEntityMetadata(MzfEntity.ACCOUNT);
			}
			protected void setForeignKey(Map<String, Object> map) throws BusinessException {
				map.put("vendorId", vendorId);
				map.remove("marketId");
			}			
		};
		
		templete.save(accountList, user);
	}	
}


