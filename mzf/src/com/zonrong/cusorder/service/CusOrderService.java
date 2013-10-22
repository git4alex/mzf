package com.zonrong.cusorder.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.basics.customer.service.CustomerService;
import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.common.service.BillStatusService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.common.utils.MzfEnum.CusOrderStatus;
import com.zonrong.common.utils.MzfEnum.MaintainStatus;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.common.utils.MzfUtils.BillPrefix;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.cusorder.service.EarnestFlowService.OrderType;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.inventory.treasury.service.TreasuryEarnestService;
import com.zonrong.inventory.treasury.service.TreasuryService.BizType;
import com.zonrong.inventory.treasury.service.TreasuryService.MoneyStorageClass1;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.BizCodeService;
import com.zonrong.util.TpltUtils;

/**
 * date: 2010-10-10
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class CusOrderService extends BillStatusService<CusOrderStatus>{
	private static Logger logger = Logger.getLogger(CusOrderService.class);
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private ProductService productService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private TreasuryEarnestService treasuryEarnestService;
	@Resource
	private EarnestFlowService earnestFlowService;
	@Resource
	private CustomerService customerService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	public enum CusOrderType {
		normal,			//本店商品订单
		transfer,		//非本店商品订单
		demand			//定制订单
	}
	
	public int createOrder(Map<String, Object> order, IUser user) throws BusinessException {
		Map<String, Object> earnest = new HashMap<String, Object>(order);
		
		Integer productId = MapUtils.getInteger(order, "productId");
		Integer diamondId = MapUtils.getInteger(order, "diamondId");
		Integer styleId = MapUtils.getInteger(order, "styleId");
		CusOrderType type = getCusOrderType(productId, styleId, user);
		
		String num = MzfUtils.getBillNum(BillPrefix.KD, user);
		order.put("status", CusOrderStatus.New);
		order.put("type", type);
		order.put("orgId", user.getOrgId());
		order.put("orgName", user.getOrgName());			
		order.put("num", num);
		order.put("cuserId", null);
		order.put("cuserName", null);
		order.put("cdate", null);
		String id = entityService.create(MzfEntity.CUS_ORDER, order, user);
		int orderId = Integer.parseInt(id);		

		if (productId != null) {			
			Map<String, Object> inventory = productInventoryService.getInventoryForProduct(productId, null);
			Integer orgId = MapUtils.getInteger(inventory, "orgId");
			if (orgId == user.getOrgId()) {
				productService.lock(productId, "客户订单[" + num + "]预定", user);
			}
		} 
		
		if (diamondId != null) {
			Map<String, Object> inventory = productInventoryService.getInventoryForProduct(diamondId, null);
			Integer orgId = MapUtils.getInteger(inventory, "orgId");
			if (orgId == user.getOrgId()) {				
				productService.lock(diamondId, "客户订单[" + num + "]预定", user);
			}
		}
		
		//定金入库
		appendEarnest(BizType.earnest, orderId, earnest, "新建客订单", user);
		
		//记录流程
		TargetType targetType = null;
		String productIdStr = null;
		if (productId != null) {
			targetType = targetType.product;
			productIdStr = productId.toString();
		}
		int transId = transactionService.createTransId();
		logService.createLog(transId, MzfEntity.CUS_ORDER, id, "新建客户订单", targetType, productIdStr, "客订，单号为："+num, user);
		
		//建立客户与该机构业务关联关系
		Integer cusId = MapUtils.getInteger(order, "cusId");
		customerService.createOrgRel(cusId, BizType.cusOrder, orderId, user);
		//记录操作日志
		businessLogService.log("新开客订单", "客订单号为:" + num, user);
		return orderId;
	}
	
	public int appendEarnest(BizType bizType, int orderId, Map<String, Object> earnest, String remark, IUser user) throws BusinessException {
		Map<String, Object> order = entityService.getById(MzfEntity.CUS_ORDER, orderId, user.asSystem());
		String num = MapUtils.getString(order, "num");
		//记录操作日志
		businessLogService.log("客订单追加定金", "客订单号为：" + num, user);
		return earnestFlowService.appendEarnest(bizType, OrderType.cusOrder, orderId, num, earnest, remark, user);
	}
	
	public Map<String,Object> getAppendEarnestData(int orderId,IUser user)throws BusinessException{
		 Map<String,Object> appdendEarnestMap = getPrintData(orderId);  //客订单
		 OrderBy orderBy = new OrderBy(new String[]{"id"},OrderByDir.desc);
		 Map<String,Object> where = new HashMap<String,Object>();
		 where.put("targetId", orderId);
		 List<Map<String,Object>> earnestFlows = entityService.list(MzfEntity.EARNEST_FLOW, where, orderBy, user);
		 if(CollectionUtils.isNotEmpty(earnestFlows) && earnestFlows.size() > 1){
			 Map<String,Object> earnestFlow = earnestFlows.get(0);
			 float lastAppendEarnest = MapUtils.getFloat(earnestFlow, "amount");
			 float earnest = MapUtils.getFloatValue(appdendEarnestMap, "totalAmount") - lastAppendEarnest;
			 appdendEarnestMap.put("earnest", earnest); //已付定金
			 appdendEarnestMap.put("appendEarnestIsAgent", MapUtils.getBoolean(earnestFlow, "isAgent"));
			 appdendEarnestMap.put("appendEarnestIsAgentText", MapUtils.getBoolean(earnestFlow, "isAgent")?"是":"否");
			 appdendEarnestMap.put("appendEarnestRemark", MapUtils.getString(earnestFlow, "remark"));
			 appdendEarnestMap.put("appendEarnest", lastAppendEarnest);
			 String payType = MapUtils.getString(earnestFlow, "payType");
			 if(StringUtils.isNotBlank(payType) && payType.equals("cash")){
				 appdendEarnestMap.put("appendEarnestCash", MapUtils.getFloat(earnestFlow, "amount"));
				 appdendEarnestMap.put("appendEarnestCard", null);
			 }else{
				 appdendEarnestMap.put("appendEarnestCash", null);
				 appdendEarnestMap.put("appendEarnestCard", MapUtils.getFloat(earnestFlow, "amount"));
			 }
		 }else{
			 appdendEarnestMap.put("earnest", MapUtils.getFloatValue(appdendEarnestMap, "totalAmount")); //已付定金
		 }
		return appdendEarnestMap;
	}
	public Map<String,Object> getAppendEarnestData(int orderId,int earnestId,IUser user)throws BusinessException{
		 Map<String,Object> appdendEarnestMap = getPrintData(orderId);  //客订单
		 OrderBy orderBy = new OrderBy(new String[]{"id"},OrderByDir.asc);
		 Map<String,Object> where = new HashMap<String,Object>();
		 where.put("targetId", orderId);
		 List<Map<String,Object>> earnestFlows = entityService.list(MzfEntity.EARNEST_FLOW, where, orderBy, user);
		 float earnest = MapUtils.getFloatValue(appdendEarnestMap, "amount");
		 float lastAppendEarnest = 0; //最后一次追加的定金
		 if(CollectionUtils.isNotEmpty(earnestFlows) && earnestFlows.size() > 1){
			 for (int i = 1; i < earnestFlows.size(); i++) {
				 Map<String,Object> earnestFlow = earnestFlows.get(i);
				 int _earnestId = MapUtils.getIntValue(earnestFlow, "id");
				 if (_earnestId < earnestId) {
					 earnest = earnest + MapUtils.getFloatValue(earnestFlow, "amount");
				 }else if (_earnestId == earnestId) {
					 lastAppendEarnest = MapUtils.getFloatValue(earnestFlow, "amount");
					 appdendEarnestMap.put("appendEarnestIsAgent", MapUtils.getBoolean(earnestFlow, "isAgent"));
					 appdendEarnestMap.put("appendEarnestIsAgentText", MapUtils.getBoolean(earnestFlow, "isAgent")?"是":"否");
					 appdendEarnestMap.put("appendEarnestRemark", MapUtils.getString(earnestFlow, "remark"));
					 appdendEarnestMap.put("appendEarnest", lastAppendEarnest);
					 String payType = MapUtils.getString(earnestFlow, "payType");
					 if(StringUtils.isNotBlank(payType) && payType.equals("cash")){
						 appdendEarnestMap.put("appendEarnestCash", MapUtils.getFloat(earnestFlow, "amount"));
						 appdendEarnestMap.put("appendEarnestCard", null);
					 }else{
						 appdendEarnestMap.put("appendEarnestCash", null);
						 appdendEarnestMap.put("appendEarnestCard", MapUtils.getFloat(earnestFlow, "amount"));
					 }
				 }
				
			} 
			 appdendEarnestMap.put("earnest", earnest); //已付定金 
		 }else{
			 appdendEarnestMap.put("earnest", MapUtils.getFloatValue(appdendEarnestMap, "totalAmount")); //已付定金
		 }
		return appdendEarnestMap;
	}
	public void receive(int orderId, int productId, int transId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUS_ORDER);
		Map<String, Object> order = entityService.getById(metadata, orderId, user);
		CusOrderStatus status = CusOrderStatus.valueOf(MapUtils.getString(order, "status"));		
		if (status != CusOrderStatus.demanding && status != CusOrderStatus.transfering) {
			throw new BusinessException("客订单状态非\"要货中\"或\"调拨中\", 收货失败！");
		}

		//商品状态改为正常
		if (productService.getStatus(productId) == ProductStatus.free) {			
			productService.lock(productId, null, user);
		}
		
		order.clear();
		order.put("status", CusOrderStatus.received);
		order.put("productId", productId);
		entityService.updateById(metadata, Integer.toString(orderId), order, user);		
		
		logService.createLog(transId, MzfEntity.CUS_ORDER, Integer.toString(orderId), "客户订单到货", TargetType.product, Integer.toString(productId), null, user);
	}
	
	public int updateCusOrder(int orderId, Map<String, Object> order, IUser user) throws BusinessException {
		Map<String, Object> dbOrder = entityService.getById(MzfEntity.CUS_ORDER_VIEW, orderId, user.asSystem());
		CusOrderStatus status = CusOrderStatus.valueOf(MapUtils.getString(dbOrder, "status"));		
		if (status != CusOrderStatus.New) {
			throw new BusinessException("此状态不能修改！");
		}
		String num = MapUtils.getString(dbOrder, "num");
		
		Integer productId = MapUtils.getInteger(order, "productId");
		Integer dbProductId = MapUtils.getInteger(dbOrder, "productId");		
		exchangeProductOnUpdate(dbProductId, productId, num, user);
		
		Integer diamondId = MapUtils.getInteger(order, "diamondId");
		Integer dbDiamondId = MapUtils.getInteger(dbOrder, "diamondId");
		exchangeProductOnUpdate(dbDiamondId, diamondId, num, user);			
		
		Map<String, Object> earnest = new HashMap<String, Object>(order);
		
		Integer styleId = MapUtils.getInteger(order, "styleId");
		CusOrderType type = getCusOrderType(productId, styleId, user);
		order.remove("num");
		order.remove("status");
		order.remove("orgId");	
		order.put("type", type);
		order.put("muserId", null);
		order.put("muserName", null);
		order.put("mdate", null);				
		int row = entityService.updateById(MzfEntity.CUS_ORDER, Integer.toString(orderId), order, user);
		
		//上次定金出库
		deliveryEarnest(BizType.cusOrder, orderId, "修改客订单", user);
		Integer earnestFlowId = MapUtils.getInteger(dbOrder, "earnestFlowId");
		earnestFlowService.deleteEarnestFlow(earnestFlowId, user);
		
		appendEarnest(BizType.earnest, orderId, earnest, "修改客订单", user);
		
		return row;
	}	
	
	private void exchangeProductOnUpdate(Integer oldProductId, Integer newProductId, String orderNum, IUser user) throws BusinessException {
		Map<String, Object> oldInventory = null;
		Map<String, Object> newInventory = null;
		ProductStatus oldStatus = null;
		ProductStatus newStatus = null;
		Integer oldOrgId = null;
		Integer newOrgId = null;
		if (oldProductId != null) {			
			oldInventory = productInventoryService.getProductInventory(oldProductId, null);
			oldStatus = ProductStatus.valueOf(MapUtils.getString(oldInventory, "status"));
			oldOrgId = MapUtils.getInteger(oldInventory, "orgId");
		}
		if (newProductId != null) {			
			newInventory = productInventoryService.getProductInventory(newProductId, null);
			newStatus = ProductStatus.valueOf(MapUtils.getString(newInventory, "status"));
			newOrgId = MapUtils.getInteger(newInventory, "orgId");
		}
		
		if (newProductId != null && oldProductId != null) {
			if (newProductId.intValue() != oldProductId) {
				if (oldOrgId != null && oldOrgId == user.getOrgId() && oldStatus == ProductStatus.locked) {					
					productService.free(oldProductId, "客户订单[" + orderNum + "]取消预定", user);			
				}
				if (newOrgId != null && oldOrgId == user.getOrgId() && newStatus == ProductStatus.free) {					
					productService.lock(newProductId, "客户订单[" + orderNum + "]预定", user);
				}
			}
		} else if (newProductId != null && oldProductId == null) {
			if (newOrgId != null && oldOrgId == user.getOrgId() && newStatus == ProductStatus.free) {					
				productService.lock(newProductId, "客户订单[" + orderNum + "]预定", user);
			}
		} else if (newProductId == null && oldProductId != null) {
			if (oldOrgId != null && oldOrgId == user.getOrgId() && oldStatus == ProductStatus.locked) {					
				productService.free(oldProductId, "客户订单[" + orderNum + "]取消预定", user);			
			}
		}
	}
	
	public void refund(int orderId, Map<String, Object> earnest, IUser user)
			throws BusinessException {			
		Map<String, Object> order = entityService.getById(MzfEntity.CUS_ORDER, orderId, user.asSystem());
		String orderNum = MapUtils.getString(order, "num");
		
		//定金出库
		String remark = MapUtils.getString(earnest, "remark"); 
		if (MapUtils.getFloat(earnest, "amount") != null) {			
			appendEarnest(BizType.refund, orderId, earnest, remark, user);
		}
		
		freeResource(orderId, "客户订单[" + orderNum + "]被退订，解锁商品", user);
		
		//更新状态
		CusOrderStatus[] priorStatus = new CusOrderStatus[]{
				CusOrderStatus.New,
				CusOrderStatus.transfering,
				CusOrderStatus.demanding,
				CusOrderStatus.received,		
				CusOrderStatus.interrupted
		};
		updateStatus(orderId, priorStatus, CusOrderStatus.refund, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> order) throws BusinessException {
				throw new BusinessException("客户订单[" + this.getNum() + "]状态为" + this.getStatus(CusOrderStatus.class).getText() + "，此时不允许退定");
			}
			
		}, user);
		
		//记录流程		
		int transId = transactionService.findTransId(MzfEntity.CUS_ORDER, Integer.toString(orderId), user);
		logService.createLog(transId, MzfEntity.CUS_ORDER, Integer.toString(orderId), "退定", null, null, remark, user);
		//记录操作日志
		businessLogService.log("客订单追退还定金", "客订单号为：" + orderNum, user);
	}
	
//	public void cancelOrder(int orderId, Map<String, Object> cancel, IUser user) throws BusinessException {
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUS_ORDER);		
//		Map<String, Object> dbOrder = entityService.getById(metadata, orderId, user.asSystem());
//		String dbOrderNum = MapUtils.getString(dbOrder, "num");
//		updateStatus(orderId, CusOrderStatus.New, CusOrderStatus.canceled, new StatusCarrier(){
//			@Override
//			public void active(Map<String, Object> order) throws BusinessException {
//				throw new BusinessException("取消客户订单" + this.getNum() + "失败");
//			}
//			
//		}, user);
//		
//		//释放定金和锁定的商品
//		freeResource(orderId, "客户订单[" + dbOrderNum + "]被取消，解锁商品", user);
//		
//		//记录流程
//		int transId = transactionService.findTransId(MzfEntity.CUS_ORDER, Integer.toString(orderId), user);
//		logService.createLog(transId, MzfEntity.CUS_ORDER, Integer.toString(orderId), "客户订单被取消", null, null, null, user);
//	}
	
	public void interruptedOrder(int orderId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUS_ORDER);		
		Map<String, Object> order = entityService.getById(metadata, orderId, user.asSystem());
		String orderNum = MapUtils.getString(order, "num");
		
		CusOrderStatus priorStatus = null;
		updateStatus(orderId, priorStatus, CusOrderStatus.interrupted, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> order) throws BusinessException {
				throw new BusinessException("中断客户订单" + this.getNum() + "失败");
			}
			
		}, user);
		
		//释放定金和锁定的商品		
		freeResource(orderId, "客户订单[" + orderNum + "]被中断，解锁商品", user);
		
		//记录流程
		int transId = transactionService.findTransId(MzfEntity.CUS_ORDER, Integer.toString(orderId), user);
		logService.createLog(transId, MzfEntity.CUS_ORDER, Integer.toString(orderId), "客户订单被中断", null, null, null, user);
	}
	
	public void deleteOrderById(int orderId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUS_ORDER);		
		Map<String, Object> dbOrder = entityService.getById(metadata, orderId, user.asSystem());
		String dbOrderNum = MapUtils.getString(dbOrder, "num");
		
		CusOrderStatus dbStatus = CusOrderStatus.valueOf(MapUtils.getString(dbOrder, "status"));
		if (CusOrderStatus.New != dbStatus) {
			throw new BusinessException("此状态不能删除！");
		}
		
		//释放定金和锁定的商品
		freeResource(orderId, "客户订单[" + dbOrderNum + "]被删除，解锁商品", user);
		
		entityService.deleteById(metadata, Integer.toString(orderId), user);
	}

	public void finishCusOrderOnSell(int orderId, IUser user) throws BusinessException {
		CusOrderStatus[] priorStatus = new CusOrderStatus[]{CusOrderStatus.New, CusOrderStatus.received};		
		updateStatus(orderId, priorStatus, CusOrderStatus.over, new StatusCarrier() {
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				throw new BusinessException("客户订单[" + this.getNum() + "]状态为" + this.getStatus(MaintainStatus.class).getText() + "，不能完成销售");
			}
			
		}, user);
	}
	
	private void freeResource(int orderId, String remark, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUS_ORDER);
		Map<String, Object> dbOrder = entityService.getById(metadata, orderId, user.asSystem());
		
		//解锁商品
		Integer productId = MapUtils.getInteger(dbOrder, "productId");
		if (productId != null) {
			try {				
				productService.free(productId, remark, user);
			} catch (Exception e) {
				logger.info(e.getMessage());
			}
		}
		
		//解锁裸石
		Integer diamondId = MapUtils.getInteger(dbOrder, "diamondId",0);
		Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, diamondId, user);
		String proStatus = MapUtils.getString(product, "status", "");
		if (diamondId != null && proStatus.equals("locked")) {
			productService.free(diamondId, remark, user);
		}		
		
		//断开与要货申请或调拨单的联系
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("orderId", null);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orderId", orderId);
		int row1 = entityService.update(MzfEntity.DEMAND, field, where, user);
		int row2 = entityService.update(MzfEntity.TRANSFER, field, where, user);
		if (row1 > 1 || row2 > 2) {
			throw new BusinessException("更新数据库发生异常");
		}		
	}
	
	private void deliveryEarnest(BizType bizType, int orderId, String remark, IUser user) throws BusinessException {
		Map<String, Object> order = entityService.getById(MzfEntity.CUS_ORDER, orderId, user.asSystem());
		Integer orgId = MapUtils.getInteger(order, "orgId");
		
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orderId", orderId);
		List<Map<String, Object>> dbList = entityService.list(MzfEntity.EARNEST_FLOW, where, null, user);
		for (Map<String, Object> amount : dbList) {
			String payType = MapUtils.getString(amount, "payType");
			String bank = MapUtils.getString(amount, "bank");
			BigDecimal money = new BigDecimal(MapUtils.getString(amount, "amount")); 
			MoneyStorageClass1 class1 = MoneyStorageClass1.valueOf(MoneyStorageClass1.class, payType);
			boolean isAgent = MapUtils.getBooleanValue(amount, "isAgent");
			
			treasuryEarnestService.delivery(bizType, orgId, money, class1, bank, isAgent, MzfEntity.CUS_ORDER, orderId, remark, user);
		}
	}
	
	private CusOrderType getCusOrderType(Integer productId, Integer styleId, IUser user) throws BusinessException {
		CusOrderType type = null;
		if (productId != null) {
			Map<String, Object> inventory = productInventoryService.getInventoryForProduct(productId, null);
			Integer orgId = MapUtils.getInteger(inventory, "orgId");
			if (user.getOrgId() == orgId) {
				type = CusOrderType.normal;
			} else {
				type = CusOrderType.transfer;
			}	
		} else if (styleId != null) {
			type = CusOrderType.demand;
		} else {
			throw new BusinessException("必须提交商品ID或者MZF款式ID");
		}	
		
		return type;
	}
	
	public Map<String, Object> getPrintData(int id) throws BusinessException {
		Map<String, Object> cusOrder = entityService.getById(MzfEntity.CUS_ORDER_VIEW, id, User.getSystemUser());
		cusOrder.put("ptypeName", BizCodeService.getBizName("productType", MapUtils.getString(cusOrder, "ptype")));
		cusOrder.put("pkindName", BizCodeService.getBizName("productKind", MapUtils.getString(cusOrder, "pkind")));
		cusOrder.put("goldClassName", BizCodeService.getBizName("goldClass", MapUtils.getString(cusOrder, "goldClass")));
		cusOrder.put("kgoldColorName", BizCodeService.getBizName("kGoldColor", MapUtils.getString(cusOrder, "kgoldColor")));
		cusOrder.put("colorName", BizCodeService.getBizName("diamondColor", MapUtils.getString(cusOrder, "color")));
		cusOrder.put("cleanName", BizCodeService.getBizName("diamondClean", MapUtils.getString(cusOrder, "clean")));
		cusOrder.put("cutName", BizCodeService.getBizName("diamondCut", MapUtils.getString(cusOrder, "cut")));
		cusOrder.put("polishingName", BizCodeService.getBizName("polishing", MapUtils.getString(cusOrder, "polishing")));
		cusOrder.put("symmetryName", BizCodeService.getBizName("symmetry", MapUtils.getString(cusOrder, "symmetry")));
		cusOrder.put("payTypeName", BizCodeService.getBizName("cusOrderPaymentWay", MapUtils.getString(cusOrder, "payType")));
		cusOrder.put("bankName", BizCodeService.getBizName("bankType", MapUtils.getString(cusOrder, "bank")));
		cusOrder.put("ptypeName", BizCodeService.getBizName("productType", MapUtils.getString(cusOrder, "ptype")));
		cusOrder.put("isAgentText", MapUtils.getBoolean(cusOrder, "isAgent")?"是":"否");
		String amountText = "";
		if(cusOrder.get("amount") != null){
			String amount = MapUtils.getString(cusOrder, "amount");
			amountText = TpltUtils.convertDigits(amount);
		}
		cusOrder.put("amountText", amountText);
		return cusOrder;
	}

	@Override
	protected EntityMetadata getBillMetadata() throws BusinessException {
		return metadataProvider.getEntityMetadata(MzfEntity.CUS_ORDER);
	}

	@Override
	protected String getBillName() {
		return "客户订单";
	}	
}


