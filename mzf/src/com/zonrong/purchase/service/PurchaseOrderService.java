package com.zonrong.purchase.service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.DemandStatus;
import com.zonrong.common.utils.MzfEnum.ProductType;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.demand.product.service.ProductDemandProcessService.DemandProcessType;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.purchase.service.DetailCRUDService.VendorOrderDetailStatus;
import com.zonrong.system.service.BizCodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-10-15
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class PurchaseOrderService extends VendorOrderService{
	private static Logger logger = Logger.getLogger(PurchaseOrderService.class);
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private DetailCRUDService detailCRUDService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	@PostConstruct
	public DetailCRUDService getDetailCRUDService() throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL);
		detailCRUDService.setEntityMetadata(metadata);
		return detailCRUDService;
	}

	public Integer createOrderByDemand(Map<String, Object> order, Integer[] demandIds, DemandProcessType type, IUser user) throws BusinessException {
		Integer vendorId = MapUtils.getInteger(order, "vendorId");

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND_PROCESS_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), demandIds);
		List<Map<String, Object>> detailList = entityService.list(metadata, where, null, user.asSystem());

		//校验数据
		check(vendorId, detailList, type);

		Boolean isFromOrder = true;
		String orderId = MapUtils.getString(detailList.get(0), "orderId");
		if (StringUtils.isBlank(orderId)) {
			isFromOrder = false;
		}

		ProductType ptype = ProductType.valueOf(MapUtils.getString(detailList.get(0), "ptype"));
		Boolean isDiamond = ptype.isDiamond();

		for (Map<String, Object> dbDemand : detailList) {
			Integer demandId = MapUtils.getInteger(dbDemand, metadata.getPkCode());
			dbDemand.remove(metadata.getPkCode());
			dbDemand.put("demandId", demandId);
			dbDemand.put("weight", MapUtils.getString(dbDemand, "confirmWeight"));
			dbDemand.put("size", MapUtils.getString(dbDemand, "confirmSize"));
			dbDemand.put("color", MapUtils.getString(dbDemand, "confirmColor"));
			dbDemand.put("clean", MapUtils.getString(dbDemand, "confirmClean"));
		}

		order.put("type", type);
		order.put("isCusOrder", isFromOrder.toString());
		order.put("isDiamond", isDiamond.toString());
		VendorOrderType vendorOrderType = VendorOrderType.valueOf(type.toString());
		Integer dbOrderId = createOrder(order, detailList,  VendorOrderDetailStatus.New, vendorOrderType, user);
		//记录操作日志
		businessLogService.log("要货申请生成订单", "订单编号：" + dbOrderId, user);

		return dbOrderId;
	}



	@Override
	public void submitOrder(Integer[] orderIds, IUser user) throws BusinessException {
		super.submitOrder(orderIds, user);

		//如果是商品采购订单或者委外加工订单， 并且其中的商品明细是从要货申请生成而来，则要修改要活申请的状态为"加工中"
		Map<String, Object> where = new HashMap<String, Object>();
		where.clear();
		where.put("orderId", orderIds);
		List<Map<String, Object>> detailList = entityService.list(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, where, null, user.asSystem());
		List<Integer> demandIdsList = new ArrayList<Integer>();
		for (Map<String, Object> detail : detailList) {
			Integer demandId = MapUtils.getInteger(detail, "demandId");
			if (demandId != null) {
				demandIdsList.add(demandId);

				Integer detailId = MapUtils.getInteger(detail, "id");

				int transId = transactionService.findTransId(MzfEntity.DEMAND, demandId.toString(), user);
				logService.createLog(transId, MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, Integer.toString(detailId), "提交供应商订单", null, null, null, user);
			}
		}

		Map<String, Object> field = new HashMap<String, Object>();
		if (demandIdsList.size() > 0) {
			field.clear();
			field.put("status", DemandStatus.machining);

			where.clear();
			where.put("id", demandIdsList.toArray());
			where.put("status", new String[]{DemandStatus.waitPurchase.toString(), DemandStatus.waitOEM.toString()});
			int rowNum = entityService.update(MzfEntity.DEMAND, field, where, user);
//			if (rowNum < demandIdsList.size()) {
//				throw new BusinessException("提交失败");
//			}
		}
	}

	public int findOrderIdByDetailId(int detailId, IUser user) throws BusinessException {
		Map<String, Object> dbDetail = entityService.getById(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, detailId, user.asSystem());

		return MapUtils.getInteger(dbDetail, "orderId");
	}

	public Map<String, Object> findOrderByDetailId(int detailId, IUser user) throws BusinessException {
		int orderId = findOrderIdByDetailId(detailId, user);
		return entityService.getById(MzfEntity.VENDOR_ORDER, Integer.toString(orderId), user);
	}

	private void check(Integer vendorId, List<Map<String, Object>> detailList, DemandProcessType type) throws BusinessException {
		if (vendorId == null) {
			throw new BusinessException("未指定供应商");
		}

		if (CollectionUtils.isEmpty(detailList)) {
			throw new BusinessException("为指定要货申请");
		}

		List<Integer> invalid1 = new ArrayList<Integer>();
		List<Integer> invalid2 = new ArrayList<Integer>();
		List<Integer> invalid3 = new ArrayList<Integer>();
		for (Map dbDemand : detailList) {
			Integer demandVendorId = MapUtils.getInteger(dbDemand, "vendorId");
			DemandProcessType demandType = DemandProcessType.valueOf(MapUtils.getString(dbDemand, "type"));
			if (vendorId.intValue() != demandVendorId) {
				invalid1.add(demandVendorId);
			}

			if (type != demandType) {
				invalid2.add(demandVendorId);
			}

			String isGenerated = MapUtils.getString(dbDemand, "isGenerated");
			if ("true".equalsIgnoreCase(isGenerated)) {
				invalid3.add(demandVendorId);
			}
		}

		if (invalid1.size() > 0) {
			throw new BusinessException("编号为" + invalid1.toString() + "的要货申请和订单的供应商不符");
		}

		if (invalid2.size() > 0) {
			throw new BusinessException("编号为" + invalid1.toString() + "的要货申请的处理方式不能生成订单");
		}

		if (invalid3.size() > 0) {
			throw new BusinessException("编号为" + invalid3.toString() + "的要货申请已经生成订单");
		}
	}

	@Override
	public Map<String, Object> getPrintData(int vendorOrderId, IUser user) throws BusinessException {
		Map<String, Object> order = super.getPrintData(vendorOrderId, user);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orderId", vendorOrderId);
		OrderBy orderBy = new OrderBy(new String[]{"ptype", "pkind"}, OrderByDir.asc);
		List<Map<String, Object>> list = entityService.list(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, where, orderBy, user);
		for (Map<String, Object> detail : list) {
			ProductType ptype = ProductType.valueOf(MapUtils.getString(detail, "ptype"));
			detail.put("ptypeText", ptype.getText());

			String pkind = MapUtils.getString(detail, "pkind");
			detail.put("pkindText", BizCodeService.getBizName("productKind", pkind));

			String goldClass = MapUtils.getString(detail, "goldClass");
			detail.put("goldClassText", BizCodeService.getBizName("goldClass", goldClass));
		}
		order.put("detailList", list);
		return order;
	}

	public Map<String, Object> getSummaryPrintData(Integer[] vendorOrderIds, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", vendorOrderIds);
		List<Map<String, Object>> orders = entityService.list(MzfEntity.VENDOR_ORDER_VIEW, where, null, user);
		if (CollectionUtils.isEmpty(orders)) {
			throw new BusinessException("请指定订单");
		}

		Map<String, Object> firstOrder = orders.get(0);
		Integer vendorId = MapUtils.getInteger(firstOrder, "vendorId");
		for (Map<String, Object> order : orders) {
			if (vendorId.intValue() != MapUtils.getInteger(order, "vendorId")) {
				throw new BusinessException("非同一家供应商的订单，不能合并");
			}
		}

		where = new HashMap<String, Object>();
		where.put("orderId", vendorOrderIds);
		OrderBy orderBy = new OrderBy(new String[]{"ptype", "pkind"}, OrderByDir.asc);
		List<Map<String, Object>> list = entityService.list(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, where, orderBy, user);

		for (Map<String, Object> detail : list) {
			int orderId = MapUtils.getInteger(detail, "orderId");
			Map<String, Object> order = findById(orders, orderId);
			detail.put("orderNum", MapUtils.getString(order, "num"));
            detail.put("factoryOrderNum", MapUtils.getString(order, "factoryOrderNum"));
			detail.put("orderCdate", MapUtils.getObject(order, "cdate"));
			detail.put("orderExpectDate", MapUtils.getObject(order, "expectDate"));
			detail.put("orderCuserName", MapUtils.getObject(order, "cuserName"));


			ProductType ptype = ProductType.valueOf(MapUtils.getString(detail, "ptype"));
			detail.put("ptypeText", ptype.getText());

			String pkind = MapUtils.getString(detail, "pkind");
			detail.put("pkindText", BizCodeService.getBizName("productKind", pkind));

			String goldClass = MapUtils.getString(detail, "goldClass");
			detail.put("goldClassText", BizCodeService.getBizName("goldClass", goldClass));
		}

		Map<String, Object> order = new HashMap<String, Object>();
		order.put("vendorName", MapUtils.getString(firstOrder, "vendorName"));
		order.put("detailList", list);
		return order;
	}

	private Map<String, Object> findById(List<Map<String, Object>> orders, int orderId) throws BusinessException {
		for (Map<String, Object> order : orders) {
			if (orderId == MapUtils.getInteger(order, "id")) {
				return order;
			}
		}

		throw new BusinessException("未找到订单");
	}
}


