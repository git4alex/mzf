package com.zonrong.purchase.service;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.common.utils.MzfEnum.VendorOrderStatus;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-10-26
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class OrderCRUDService {
	private static Logger logger = Logger.getLogger(OrderCRUDService.class);
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	
	public Map<String, Object> get(int orderId, IUser user) throws BusinessException {				
		EntityMetadata orderMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER_VIEW);		
		
		return entityService.getById(orderMetadata, Integer.toString(orderId), user);
	}

	public int createOrder(Map<String, Object> order, VendorOrderType type, IUser user) throws BusinessException {		
		EntityMetadata orderMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER);
		String num = MzfUtils.getBillNum(type.getOrderPrefix(), user);
		order.put("type", type);
		order.put("status", VendorOrderStatus.New);
		order.put("cuserId", null);
		order.put("cuserName", null);
		order.put("cdate", null);
		order.put("num", "" + num);
		String orderId = entityService.create(orderMetadata, order, user);
				
		//记录流程信息
		String remark = "订单类型：" + type.getText();
		int transId = transactionService.createTransId();
		logService.createLog(transId, MzfEntity.VENDOR_ORDER, orderId, "新建供应商订单", null, null, remark, user);		
		
		return Integer.parseInt(orderId);
	}
	
	public int updateOrder(int orderId, Map<String, Object> order, IUser user) throws BusinessException {				
		EntityMetadata orderMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER);		
		order.remove(orderMetadata.getPkCode());
		order.put("muserId", null);
		order.put("muserName", null);
		order.put("mdate", null);
		
		return entityService.updateById(orderMetadata, Integer.toString(orderId), order, user);
	}
	
	public int deleteOrder(int orderId, IUser user) throws BusinessException {		
		EntityMetadata orderMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER);	
		return entityService.deleteById(orderMetadata, Integer.toString(orderId), user);
	}	
	
	public VendorOrderStatus getStatus(Integer orderId, IUser user) throws BusinessException {
		Map<String, Object> order = get(orderId, user);
		return VendorOrderStatus.valueOf(MapUtils.getString(order, "status"));
	}
}


