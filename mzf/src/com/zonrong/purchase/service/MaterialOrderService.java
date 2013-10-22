package com.zonrong.purchase.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-10-15
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class MaterialOrderService extends VendorOrderService{
	private static Logger logger = Logger.getLogger(MaterialOrderService.class);
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private DetailCRUDService detailCRUDService;		
	
	@PostConstruct
	public DetailCRUDService getDetailCRUDService() throws BusinessException {
		EntityMetadata entityMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER_MATERIAL_ORDER_DETAIL);
		detailCRUDService.setEntityMetadata(entityMetadata);
		return detailCRUDService;
	}
	
	@Override
	public Map<String, Object> getPrintData(int vendorOrderId, IUser user) throws BusinessException {
		Map<String, Object> order = super.getPrintData(vendorOrderId, user);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orderId", vendorOrderId);
		List<Map<String, Object>> list = entityService.list(MzfEntity.VENDOR_ORDER_MATERIAL_ORDER_DETAIL_VIEW, where, null, user);
		for (Map<String, Object> map : list) {
			BigDecimal quantity = new BigDecimal(MapUtils.getString(map,"quantity","0"));
			BigDecimal cost = new BigDecimal(MapUtils.getString(map,"cost","0"));
			if(quantity.intValue() > 0){
				map.put("price", cost.divide(quantity));
			}
		}
		order.put("detailList", list);
		return order;
	}	
}


