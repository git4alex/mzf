package com.zonrong.register.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.common.utils.MzfEnum.VendorOrderStatus;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.purchase.service.DetailCRUDService.VendorOrderDetailStatus;

/**
 * date: 2010-11-2
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class RegisterService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private EntityService entityService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;		

	int createRegister(Map<String, Object> register, TargetType type,
			int orderId, int orderDetailId, int targetId, BigDecimal quantity, 
			IUser user) throws BusinessException {	
		
		register.put("type", type);
		register.put("orderId", orderId);
		register.put("orderDetailId", orderDetailId);
		register.put("targetId", targetId);
		register.put("quantity", quantity);
		register.put("cuserId", null);
		register.put("cdate", null);		
		String receiveId = entityService.create(MzfEntity.REGISTER, register, user);
		
		//记录流程信息
		Map<String, Object> order = entityService.getById(MzfEntity.VENDOR_ORDER, orderId, user);
		String remark = "订单号：" + MapUtils.getString(order, "num");
		int transId = transactionService.findTransId(MzfEntity.VENDOR_ORDER, Integer.toString(orderId), user);
		logService.createLog(transId, MzfEntity.VENDOR_ORDER, Integer.toString(orderId), "收货登记", type, Integer.toString(targetId), remark, user);		
		
		return Integer.parseInt(receiveId);		
	}
	
	public String getTargetNum(int registerId, IUser user) throws BusinessException {
		Map<String, Object> map = entityService.getById(MzfEntity.REGISTER_VIEW, Integer.toString(registerId), user.asSystem());
		if (MapUtils.isNotEmpty(map)) {
			return MapUtils.getString(map, "targetNum");
		}
		
		return null;
	}
	
	public void updateStatus(Integer[] ids, IUser user) throws BusinessException{
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", ids);
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("isPrint", "true");
		entityService.update(MzfEntity.REGISTER, field, where, user);
	}
}

abstract class CancelDetailTemplete {
	public abstract IEntityCode getDetailEntityCode();
	public abstract void putObjectId(Map<String, Object> field);
	
	private MetadataProvider metadataProvider;
	private EntityService entityService;
	
	CancelDetailTemplete(MetadataProvider metadataProvider, EntityService entityService) {
		this.metadataProvider = metadataProvider;
		this.entityService = entityService;
	}
	
	void cancelDetail(Map<String, Object> detail, IUser user) throws BusinessException {		
		EntityMetadata detailMetadata = metadataProvider.getEntityMetadata(getDetailEntityCode());
		
		//更新订单明细状态
		Integer detailId = MapUtils.getInteger(detail, detailMetadata.getPkCode());
		Integer orderId = MapUtils.getInteger(detail, "orderId");
		VendorOrderDetailStatus status = VendorOrderDetailStatus.valueOf(MapUtils.getString(detail, "status"));
		if (VendorOrderDetailStatus.received == status) {
			throw new BusinessException("ID为["+ detailId +"]的订单明细已经核销");
		}
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", VendorOrderDetailStatus.received);
		putObjectId(field);
		entityService.updateById(detailMetadata, Integer.toString(detailId), field, user);
		
		//更新订单状态(如果全部收货，状态为订单完成，否则为收获中)
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("status", VendorOrderDetailStatus.waitReceive);
		parameter.put("orderId", orderId);
		List<Map<String, Object>> detailList = entityService.list(detailMetadata, parameter, null, user.asSystem());
		field.clear();
		if (detailList.size() == 0) {
			field.put("status", VendorOrderStatus.finished);
		} else {
			field.put("status", VendorOrderStatus.receiving);
		}
		EntityMetadata orderMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER);		
		entityService.updateById(orderMetadata, Integer.toString(orderId), field, user);
	}	
}


