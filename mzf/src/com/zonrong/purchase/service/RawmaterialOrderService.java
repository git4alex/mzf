package com.zonrong.purchase.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.zonrong.common.utils.MzfEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEnum.RawmaterialType;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.purchase.service.DetailCRUDService.VendorOrderDetailStatus;
import com.zonrong.system.service.BizCodeService;

/**
 * date: 2010-10-15
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class RawmaterialOrderService extends VendorOrderService{
	private static Logger logger = Logger.getLogger(RawmaterialOrderService.class);

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private DetailCRUDService detailCRUDService;

	@PostConstruct
	public DetailCRUDService getDetailCRUDService() throws BusinessException {
		EntityMetadata entityMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER_RAWMATERIAL_ORDER_DETAIL);
		detailCRUDService.setEntityMetadata(entityMetadata);
		return detailCRUDService;
	}

	@Override
	public int createOrder(Map<String, Object> order, List<Map<String, Object>> detailList, VendorOrderDetailStatus detailStatus, VendorOrderType type, IUser user) throws BusinessException {
		if (CollectionUtils.isEmpty(detailList)) {
			throw new BusinessException("订单明细为空");
		}

		List<Map<String, Object>> newDetailList = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> detail : detailList) {
			RawmaterialType rawmaterialType = MzfEnum.RawmaterialType.valueOf(MapUtils.getString(detail, "type"));
			if (rawmaterialType == MzfEnum.RawmaterialType.nakedDiamond) {
				int quantity = MapUtils.getIntValue(detail, "quantity", 1);
				detail.put("quantity", 1);
				for (int i = 0; i < quantity; i++) {
					newDetailList.add(detail);
				}
			} else {
				newDetailList.add(detail);
			}

		}

		return super.createOrder(order, newDetailList, detailStatus, type, user);
	}

	@Override
	public Map<String, Object> getPrintData(int vendorOrderId, IUser user) throws BusinessException {
		Map<String, Object> order = super.getPrintData(vendorOrderId, user);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orderId", vendorOrderId);
		OrderBy orderBy = new OrderBy(new String[]{"type"}, OrderByDir.asc);
		List<Map<String, Object>> list = entityService.list(MzfEntity.VENDOR_ORDER_RAWMATERIAL_ORDER_DETAIL, where, orderBy, user);
		for (Map<String, Object> map : list) {
			String type = MapUtils.getString(map, "type");
			map.put("typeText", BizCodeService.getBizName("rowmaterialType", type));

			String goldClass = MapUtils.getString(map, "goldClass");
			map.put("goldClassText", BizCodeService.getBizName("goldClass", goldClass));

			String partsType = MapUtils.getString(map, "partsType");
			map.put("partsTypeText", BizCodeService.getBizName("partsType", partsType));

			String partsStandard = MapUtils.getString(map, "partsStandard");
			map.put("partsStandardText", BizCodeService.getBizName("partsStandard", partsStandard));

			String gravelStandard = MapUtils.getString(map, "gravelStandard");
			map.put("gravelStandardText", BizCodeService.getBizName("gravelStandard", gravelStandard));

			String shape = MapUtils.getString(map, "shape");
			map.put("shapeText", BizCodeService.getBizName("diamondShape", shape));

			String clean = MapUtils.getString(map, "clean");
			map.put("cleanText", BizCodeService.getBizName("diamondClean", clean));

			String color = MapUtils.getString(map, "color");
			map.put("colorText", BizCodeService.getBizName("diamondColor", color));

			String cut = MapUtils.getString(map, "cut");
			map.put("cutText", BizCodeService.getBizName("diamondCut", cut));

		}
		order.put("detailList", list);
		return order;
	}
}


