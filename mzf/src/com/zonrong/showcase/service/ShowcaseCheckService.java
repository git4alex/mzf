package com.zonrong.showcase.service;

import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2011-1-5
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ShowcaseCheckService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private MzfOrgService mzfOrgService;
	public enum ShowcaseProudctFlowType {
		in,		//入柜
		out		//出柜
	}

	private Map<String, Object> getStore(int orgId,IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.STORE);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId",orgId);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("未找到该用户所在部门对应的门店");
		} else if (list.size() > 1) {
			throw new BusinessException("找到该用户所在部门对应多家门店");
		}

		return list.get(0);
	}

	public int checkInDefault(int productId,int orgId, String remark, IUser user) throws BusinessException {
		Map<String, Object> store = getStore(orgId, user);
		Integer storeId = MapUtils.getInteger(store, "id");
		Integer defaultShowcaseId = MapUtils.getInteger(store, "defaultShowcaseId");
		if (defaultShowcaseId == null) {
			Map<String, Object> field = new HashMap<String, Object>();
			field.put("storeId", storeId);
			field.put("name", "保险柜");
			field.put("remark", "默认柜台");
			field.put("cuserId", null);
			field.put("cuserName", null);
			field.put("cdate", null);
			String id = entityService.create(MzfEntity.SHOWCASE, field, user);
			defaultShowcaseId = new Integer(id);

			field.clear();
			field.put("defaultShowcaseId", defaultShowcaseId);
			entityService.updateById(MzfEntity.STORE, storeId.toString(), field, user);
		}
		return checkIn(defaultShowcaseId, productId, remark, user);
	}

	public void checkIn(int showcaseId, Integer[] productIds, String remark, IUser user) throws BusinessException {
		for (Integer pid : productIds) {
			  int count = 0;
			  for (Integer id : productIds) {
				 if(pid == id){
					 count++;
				 }
			}
			if (count  > 1) {
				throw new BusinessException("商品重复");
			}
		}
		for (Integer productId : productIds) {
			checkIn(showcaseId, productId, remark, user);
		}
	}

	public int checkIn(int showcaseId, int productId, String remark, IUser user) throws BusinessException {
//		Map<String, Object> store = getStore(user);
//		Integer storeId = MapUtils.getInteger(store, "id");

		Map<String, Object> showcase = entityService.getById(MzfEntity.SHOWCASE, showcaseId, user.asSystem());
//		Integer showcaseStoreId = MapUtils.getInteger(showcase, "storeId");
//		if (showcaseStoreId == null || showcaseStoreId.intValue() != storeId.intValue()) {
//			throw new BusinessException("目标柜台非本门店柜台");
//		}

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SHOWCASE_PRODUCT);
		HashMap<String,Object> where = new HashMap<String,Object>();
		where.put("showcaseId", showcaseId);
		where.put("productId", productId);
		List<Map<String,Object>> showcaseProducts = entityService.list(metadata, where, null, user);
		if(showcaseProducts.size() > 0){
			throw new BusinessException("柜台已存在该商品");
		}

		try {
			Map<String, Object> showcaseProduct = getShowcaseProduct(productId, user);
			Integer dbShowcaseId = MapUtils.getInteger(showcaseProduct, "showcaseId");
			Map<String, Object> dbShowcase = entityService.getById(MzfEntity.SHOWCASE, dbShowcaseId, user.asSystem());
			String fromShowcase = MapUtils.getString(dbShowcase, "name");
			String toShowcase = MapUtils.getString(showcase, "name");
			remark = "从" + fromShowcase + "调整到" + toShowcase;
		} catch (Exception e) {
		}

		//出柜台
		try {
			checkOut(productId, remark, user);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage());
			}
		}

		//入柜台
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("showcaseId", showcaseId);
		field.put("productId", productId);
		field.put("cuserId", null);
		field.put("cuserName", null);
		field.put("cdate", null);
		String id = entityService.create(metadata, field, user);

		createFlow(showcaseId, productId, ShowcaseProudctFlowType.in, remark, user);
		return Integer.parseInt(id);
	}

	public Map<String, Object> getShowcaseProduct(int productId, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("productId", productId);
		List<Map<String, Object>> list = entityService.list(MzfEntity.SHOWCASE_PRODUCT, where, null, user);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("商品不在柜台上");
		} else if (list.size() > 1) {
			throw new BusinessException("商品同时在多个柜台上");
		}
		return list.get(0);
	}

	public String getShowcaseName(int productId, IUser user) throws BusinessException{
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("productId", productId);
		List<Map<String, Object>> list = entityService.list(MzfEntity.VIEW_SHOWCASE_PRODUCT, where, null, user);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("商品不在柜台上");
		} else if (list.size() > 1) {
			throw new BusinessException("商品同时在多个柜台上");
		}
		Map<String, Object> showcase = list.get(0);
		return MapUtils.getString(showcase, "showcaseName");
	}

	public void checkOut(int productId, String remark, IUser user) throws BusinessException {
		Map<String, Object> showcaseProduct = getShowcaseProduct(productId, user);

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SHOWCASE_PRODUCT);

		//出柜台
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("productId", productId);
		int row = entityService.delete(metadata, where, user);
		if (row != 1) {
			throw new BusinessException("从柜台移出商品失败");
		}
		Integer showcaseId = MapUtils.getInteger(showcaseProduct, "showcaseId");
		createFlow(showcaseId, productId, ShowcaseProudctFlowType.out, remark, user);
	}

	public void checkCount(Map<String, Object> map, IUser user) throws BusinessException {
		List<Map<String, Object>> detailList = (List<Map<String, Object>>) map.get("detailList");
		if (CollectionUtils.isEmpty(detailList)) {
			throw new BusinessException("List<Map<String, Object>> is empty");
		}

		map.put("cdate", null);
		String showcaseCheckId = entityService.create(MzfEntity.SHOWCASE_CHECK, map, user);

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SHOWCASE_CHECK_DETAIL);
		for (Map<String, Object> detail : detailList) {
			detail.put("showcaseCheckId", showcaseCheckId);
			entityService.create(metadata, detail, user);
		}
	}

	private int createFlow(int showcaseId, int productId, ShowcaseProudctFlowType type, String remark, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SHOWCASE_PRODUCT_FLOW);
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("showcaseId", showcaseId);
		field.put("productId", productId);
		field.put("type", type);
		field.put("remark", remark);
		field.put("cuserId", null);
		field.put("cuserName", null);
		field.put("cdate", null);

		String id = entityService.create(metadata, field, user);
		return Integer.parseInt(id);
	}
	public int delShowcase(int showcaseId,IUser user) throws BusinessException{
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("showcaseId", showcaseId);
		List<Map<String, Object>> list = entityService.list(MzfEntity.SHOWCASE_PRODUCT, where, null, user);
		if(list.size() == 0){
	      int count = entityService.deleteById(MzfEntity.SHOWCASE, showcaseId+"", user);
	      return count;
		}else{
			throw new BusinessException("该柜台中有商品不能删除");
		}
	}
}


