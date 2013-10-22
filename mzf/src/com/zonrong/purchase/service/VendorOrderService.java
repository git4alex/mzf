package com.zonrong.purchase.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;

import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.SettlementType;
import com.zonrong.common.utils.MzfEnum.VendorOrderStatus;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.purchase.service.DetailCRUDService.VendorOrderDetailStatus;
import com.zonrong.settlement.service.SettlementService;

/**
 * date: 2010-10-15
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class VendorOrderService{
	private static Logger logger = Logger.getLogger(VendorOrderService.class);
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private OrderCRUDService orderCRUDService;	
	@Resource
	private SettlementService settlementService;
	@Resource
	private MzfOrgService mzfOrgService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;	
	@Resource
	private BusinessLogService businessLogService;
	
	public abstract DetailCRUDService getDetailCRUDService() throws BusinessException;
	
	public Map<String, Object> getPrintData(int vendorOrderId, IUser user) throws BusinessException {
		Map<String, Object> order = entityService.getById(MzfEntity.VENDOR_ORDER_VIEW, Integer.toString(vendorOrderId), user);
		VendorOrderType type = VendorOrderType.valueOf(MapUtils.getString(order, "type"));		
		order.put("typeText", type.getText());
		
		return order;
	}
	
	public int createOrder(Map<String, Object> order, List<Map<String, Object>> detailList, VendorOrderDetailStatus detailStatus, VendorOrderType type, IUser user) throws BusinessException {
		if (CollectionUtils.isEmpty(detailList)) {
			throw new BusinessException("订单明细为空");
		}
		
		Integer orderId = orderCRUDService.createOrder(order, type, user);
		for (Map<String, Object> detail : detailList) {
			detail.put("status", detailStatus);
		}
		getDetailCRUDService().saveDetail(orderId, detailList, user);

		return orderId;		
	}	
	
	public void updateOrder(int orderId, Map<String, Object> order, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
		if (VendorOrderStatus.New != status) {
			throw new BusinessException("当前不允许修改");
		}
		orderCRUDService.updateOrder(orderId, order, user);
		getDetailCRUDService().saveDetail(orderId, detailList, user);
	}	
	
	public void submitOrder(Integer[] orderIds, IUser user) throws BusinessException {
		getDetailCRUDService().updateDetailStatusByOrderIds(orderIds, VendorOrderDetailStatus.waitReceive, user);
		
		EntityMetadata orderMetadata = getOrderMetadata();
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("muserId", null);
		field.put("muserName", null);
		field.put("mdate", null);		
		field.put("status", VendorOrderStatus.submit);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(orderMetadata.getPkCode(), orderIds);
		where.put("status", VendorOrderStatus.New);
		entityService.update(orderMetadata, field, where, user);
		
		//记录流程信息
		 
		for (Integer orderId : orderIds) {
			int transId = transactionService.findTransId(MzfEntity.VENDOR_ORDER, orderId.toString(), user);
			logService.createLog(transId, MzfEntity.VENDOR_ORDER, orderId.toString(), "提交供应商订单", null, null, null, user);
			//记录操作日志
			businessLogService.log("提交供应商订单", "订单号：" + orderId, user);
		}
		
	}
	
	public void deleteOrder(int orderId, IUser user) throws BusinessException {	
		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
		if (VendorOrderStatus.New != status) {
			throw new BusinessException("当前不允许删除");
		}
		
		//删除订单明细
		getDetailCRUDService().deleteDetailByOrderId(orderId, user);
		
		//删除订单
		entityService.deleteById(getOrderMetadata(), Integer.toString(orderId), user);
	}
	
	public int createSettlement(SettlementType type, int orderId, BigDecimal price, String remark, IUser user) throws BusinessException {
		//校验状态，只有收货完成才能生成结算单
		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
		VendorOrderStatus targetStatus = VendorOrderStatus.finished;
		if (status != targetStatus) {
			throw new BusinessException("订单状态为" + targetStatus.getText() + "时才允许生成结算单");
		}
		
		Map<String, Object> order = orderCRUDService.get(orderId, user);
		Integer settlementId = MapUtils.getInteger(order, "settlementId");
		if (settlementId != null) {
			throw new BusinessException("已经生成结算单");
		}
		
		Integer vendorId = MapUtils.getInteger(order, "vendorId");
		Integer payOrgId = mzfOrgService.getHQOrgId();
		//记录操作日志
		businessLogService.log("结算供应商订单", "订单号:" + orderId, user);
		//生成结算单
		int id = settlementService.createForVendorOrder(type, vendorId, payOrgId, orderId, price, remark, user);
		return id;
	}
	
	private EntityMetadata getOrderMetadata() throws BusinessException {
		return metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER);
	}
}


