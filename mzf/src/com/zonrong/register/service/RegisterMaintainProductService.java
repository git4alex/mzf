package com.zonrong.register.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.MaintainInventoryService;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-11-2
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class RegisterMaintainProductService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private MaintainInventoryService maintainInventoryService;
	@Resource
	private RegisterService registerService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;		
	@Resource
	private BusinessLogService businessLogService;
	
	public int register(int orderDetailId, Map<String, Object> maintainInfo, Map<String, Object> register, IUser user) throws BusinessException {
		Map<String, Object> dbDetail = entityService.getById(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, orderDetailId, user.asSystem());
		Integer productId = MapUtils.getInteger(dbDetail, "productId");				
		
		//核销订单明细
		String fee = MapUtils.getString(maintainInfo, "maintainFee");
		BigDecimal maintainFee = null;
		if (StringUtils.isNotBlank(fee)) {
			maintainFee = new BigDecimal(fee);
		}		
		cancelProductOrderDetail(dbDetail, productId, maintainFee, user);
		
		//商品入库			
		maintainInventoryService.warehouseToMaintain(productId, "委外维修收货登记入维修库", user);
		
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("size", MapUtils.getString(maintainInfo, "size"));
		field.put("goldWeight", MapUtils.getString(maintainInfo, "goldWeight"));
		entityService.updateById(MzfEntity.PRODUCT, productId.toString(), field, user);
		
		//收货记录
		Integer orderId = MapUtils.getInteger(dbDetail, "orderId");		
		int receiveId = registerService.createRegister(register, TargetType.maintainProduct, orderId, orderDetailId, productId, new BigDecimal(1), user);
		//记录操作日志
		businessLogService.log("维修收货登记", "商品编号: " + productId, user);
		
		return receiveId;		
	}
	
	private void cancelProductOrderDetail(Map<String, Object> detail, final int productId, final BigDecimal maintainFee, IUser user) throws BusinessException {
		//核销明细
		CancelDetailTemplete templete = new CancelDetailTemplete(metadataProvider, entityService){
			public IEntityCode getDetailEntityCode(){
				return MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL;
			}
			public void putObjectId(Map<String, Object> field) {
				if (maintainFee != null) {					
					field.put("maintainFee", maintainFee);
				}
			}
		};
		templete.cancelDetail(detail, user);
	}
}


