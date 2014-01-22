package com.zonrong.purchase.service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.SettlementType;
import com.zonrong.common.utils.MzfEnum.VendorOrderStatus;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.sql.service.SimpleSqlService;
import com.zonrong.demand.product.service.ProductDemandProcessService.DemandProcessType;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.MaintainInventoryService;
import com.zonrong.inventory.service.RawmaterialInventoryService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.purchase.dosing.service.DosingService;
import com.zonrong.purchase.service.DetailCRUDService.VendorOrderDetailStatus;
import com.zonrong.system.service.BizCodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-12-9
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class OEMOrderService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private OrderCRUDService orderCRUDService;
	@Resource
	private PurchaseOrderService purchaseOrderService;
	@Resource
	private RawmaterialInventoryService rawmaterialInventoryService;
	@Resource
	private MaintainInventoryService maintainInventoryService;
	@Resource
	private DosingService dosingService;
	@Resource
	private SimpleSqlService simpleSqlService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;

    public Integer createOrderByDemand(Map<String, Object> order, Integer[] demandIds, IUser user) throws BusinessException {
		return purchaseOrderService.createOrderByDemand(order, demandIds, DemandProcessType.OEM, user);
	}

	public int createOrder(Map<String, Object> order, VendorOrderType type, IUser user) throws BusinessException {
		return orderCRUDService.createOrder(order, type, user);
	}

	public void updateOrder(int orderId, Map<String, Object> order, IUser user) throws BusinessException {
		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
		if (VendorOrderStatus.New != status) {
			throw new BusinessException("当前不允许修改");
		}
		orderCRUDService.updateOrder(orderId, order, user);
	}

//	private boolean hasBom(int orderId, RawmaterialType type) throws BusinessException {
//		Map<String, Object> where = new HashMap<String, Object>();
//		where.put("orderId", orderId);
//		List<Map<String, Object>> bomDosingList = entityService.list(MzfEntity.BOM_DOSING_VIEW, where, null, User.getSystemUser());
//		for (Map<String, Object> bomDosing : bomDosingList) {
//			Integer dosingId = MapUtils.getInteger(bomDosing, "dosingId");
//			if (dosingId == null) {
//				String typeStr = MapUtils.getString(bomDosing, "rawmaterialType");
//				String loverTypeStr = typeStr.toLowerCase();
//				if (loverTypeStr.endsWith("diamond")) {
//					if (type == RawmaterialType.nakedDiamond || type == RawmaterialType.gravel) {
//						return true;
//					}
//				} else {
//					RawmaterialType dbType = RawmaterialType.valueOf(typeStr);
//					if (type == dbType) {
//						return true;
//					}
//				}
//
//			}
//		}
//
//		return false;
//	}

    public void submitOemOrder(Integer orderId,Map<String,Object> params,IUser user) throws BusinessException{
        EntityMetadata orderMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER);
        Map<String, Object> field = new HashMap<String, Object>();
        String factoryOrderNum = MapUtils.getString(params,"factoryOrderNum");
        if(StringUtils.isBlank(factoryOrderNum)){
            throw new BusinessException("厂家单号不能为空");
        }
        field.put("factoryOrderNum",factoryOrderNum);
        Map<String, Object> where = new HashMap<String, Object>();
        where.put(orderMetadata.getPkCode(), orderId);
        entityService.update(orderMetadata, field, where, user);
        purchaseOrderService.submitOrder(new Integer[]{orderId}, user);
    }

    /**
     * 提交订单
     *
     * @param orderIds 订单Id
     * @param vendorOrderType 订单类型
     * @param user 当前用户
     *
     * @throws BusinessException
     */
	public void submitOrder(Integer[] orderIds, VendorOrderType vendorOrderType, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", orderIds);
		List<Map<String, Object>> list = entityService.list(MzfEntity.VENDOR_ORDER_VIEW, where, null, user);
		List<String> numList = new ArrayList<String>();
		for (Map<String, Object> order : list) {
//			boolean isDosingGold = MapUtils.getBooleanValue(order, "isDosingGold");
//			boolean isDosingDiamond = MapUtils.getBooleanValue(order, "isDosingDiamond");
//			boolean isDosingParts = MapUtils.getBooleanValue(order, "isDosingParts");

//			Integer orderId = MapUtils.getInteger(order, "id");
//			String num = MapUtils.getString(order, "num");
//			if (vendorOrderType == VendorOrderType.OEM) {
//				if ((hasBom(orderId, RawmaterialType.nakedDiamond) || hasBom(
//						orderId, RawmaterialType.gravel))
//						&& !isDosingDiamond) {
//					throw new BusinessException("委外加工订单[" + num + "]裸（碎）石配料尚未确认");
//				}
////				if ((hasBom(orderId, RawmaterialType.gold)) && !isDosingGold) {
////					throw new BusinessException("委外加工订单[" + num + "]金料配料尚未确认");
////				}
//				if ((hasBom(orderId, RawmaterialType.parts)) && !isDosingParts) {
//					throw new BusinessException("委外加工订单[" + num + "]配件配料尚未确认");
//				}
//			}
		}

//		if (vendorOrderType == VendorOrderType.OEM) {
//			//委外维修不做限制
//			if (numList.size() > 0) {
//				throw new BusinessException("委外加工订单" + numList + "配料尚未确认，不能提交!");
//			}
//		}

		purchaseOrderService.submitOrder(orderIds, user);

//		Map<Integer, DosingBom> rawmaterialQuantityMap = new HashMap<Integer, DosingBom>();
//		String remark = vendorOrderType.getText() + "配料出库";
//		//原料出库
//		where = new HashMap<String, Object>();
//		where.put("orderId", orderIds);
//
//		List<Map<String, Object>>  dosingList = entityService.list(MzfEntity.DOSING_VIEW, where, null, user.asSystem());
//		for (Map<String, Object> dosing : dosingList) {
//			Integer rawmaterialId = MapUtils.getInteger(dosing, "rawmaterialId");
//
//			if (rawmaterialId != null) {
//				RawmaterialType rawmaterialType = RawmaterialType.valueOf(MapUtils.getString(dosing, "rawmaterialType"));
//				if (rawmaterialType != RawmaterialType.nakedDiamond) {
//					DosingBom dosingBom = new DosingBom();
//					dosingBom.setRawmaterialId(rawmaterialId);
//					dosingBom.setQuantity(new BigDecimal(MapUtils.getString(dosing, "dosingQuantity")));
//					try {
//						dosingBom.setWeight(new BigDecimal(MapUtils.getString(dosing, "dosingWeight")));
//					} catch (Exception e) {
//					}
//					rawmaterialQuantityMap.put(rawmaterialId, dosingBom);
//				} else {
//					rawmaterialInventoryService.deliveryDiamondByRawmaterialId(BizType.OEM, rawmaterialId, remark, user);
//				}
//			}
//		}
//
//		if (MapUtils.isNotEmpty(rawmaterialQuantityMap)) {
//			rawmaterialInventoryService.deliveryOnOem(BizType.OEM, rawmaterialQuantityMap, user.getOrgId(), remark, user);
//		}

		//提交委外维修订单时将商品出库
		if (vendorOrderType == VendorOrderType.maintainOEM) {
			submitOrderOfMaintain(orderIds, user);
		}
	}

    /**
     * 委外维修订单提交
     *
     * @param orderIds 订单id
     * @param user 用户
     * @throws BusinessException
     */
	private void submitOrderOfMaintain(Integer[] orderIds, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orderId", orderIds);
		List<Map<String, Object>> detailList = entityService.list(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, where, null, user.asSystem());
		List<Integer> productIds = new ArrayList<Integer>();
		for (Map<String, Object> detail : detailList) {
			Integer productId = MapUtils.getInteger(detail, "productId");
			if (productId == null) {
				throw new BusinessException("缺少参数[productId]");
			}
			productIds.add(productId);
		}
		if (productIds.size() > 0) {
			maintainInventoryService.deliveryFromMaintain(productIds.toArray(new Integer[]{}), "提交委外维修订单，商品出库", user);
		}
	}

    /**
     * 删除订单
     *
     * @param orderId 订单Id
     * @param user 用户
     * @throws BusinessException
     */
	public void deleteOrder(int orderId, IUser user) throws BusinessException {
		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
		if (VendorOrderStatus.New != status) {
			throw new BusinessException("当前不允许删除");
		}
		//解锁原料
		dosingService.freeRawmaterialInventory(orderId, user);

		//删除配料信息
		dosingService.deleteDosingByOrderId(orderId, user);

		//删除订单明细
		dosingService.deleteDetailByOrderId(orderId, user);

		//删除订单
		entityService.deleteById(MzfEntity.VENDOR_ORDER, Integer.toString(orderId), user);
	}

    /**
     * 新建委外加工订单明细
     *
     * @param orderId  订单Id
     * @param detail 明细数据
     * @param user 用户
     *
     * @return 明细Id
     *
     * @throws BusinessException
     */
	public int createDetail(int orderId, Map<String, Object> detail, IUser user) throws BusinessException {
		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
		if (VendorOrderStatus.New != status) {
			throw new BusinessException("当前不允许新增明细");
		}
		//新建订单明细
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL);
		detail.put("status", VendorOrderDetailStatus.New);
		String id = entityService.create(metadata, detail, user);

		return Integer.parseInt(id);
	}

	public void updateDetail(int detailId, Map<String, Object> detail, IUser user) throws BusinessException {
		Integer orderId = purchaseOrderService.findOrderIdByDetailId(detailId, user);
		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
		if (VendorOrderStatus.New != status) {
			throw new BusinessException("当前不允许修改明细");
		}

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL);
		entityService.updateById(metadata, Integer.toString(detailId), detail, user);
	}

	public void deleteDetail(int detailId, IUser user) throws BusinessException {
		Integer orderId = purchaseOrderService.findOrderIdByDetailId(detailId, user);
		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
		if (VendorOrderStatus.New != status) {
			throw new BusinessException("当前不允许删除明细");
		}
		//解锁原料
		dosingService.freeRawmaterialInventoryByDetailId(detailId, user);

		//删除配料信息
		dosingService.deleteDosingByDetailId(detailId, user);

		//删除订单明细
		entityService.deleteById(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, Integer.toString(detailId), user);
	}

    public void cancelDetail(String detailId,IUser user) throws BusinessException{
        //检查明细状态，如果为“已收货”或者“已取消”，则不能进行取消操作
        Map<String,Object> detail = entityService.getById(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL,detailId,user);
        if(detail == null){
            throw new BusinessException("未找到该订单明细的数据");
        }
        VendorOrderDetailStatus status = VendorOrderDetailStatus.valueOf(MapUtils.getString(detail,"status"));
        if(status == VendorOrderDetailStatus.canceled || status == VendorOrderDetailStatus.received){
            throw new BusinessException("状态为“已收货”或“已取消”时不能再进行取消操作");
        }

        if(status == VendorOrderDetailStatus.New){
            this.deleteDetail(Integer.parseInt(detailId),user);
            return;
        }

        //查询订单明细的配料信息
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("detailId", detailId);
        List<Map<String,Object>> dosings = entityService.list(MzfEntity.DOSING,where,null,user);

//        //如果没有配料，可以直接取消，否则提示用户
//        if(dosings!= null && dosings.size()>0){
//            throw new BusinessException("该商品包含自配的原料，不能直接取消，请先将原料退库，再进行取消操作。");
//        }

        //如果没有配料，可以直接取消，否则将配料状态改为“待退库”，并取消订单明细
        if(!CollectionUtils.isEmpty(dosings)){
            for(Map<String,Object> dosing:dosings){
                dosing.put("status",DosingService.DosingStatus.unReturn);
                dosing.put("bomId",null);
                entityService.updateById(MzfEntity.DOSING,MapUtils.getString(dosing,"id"),dosing,user);
            }
        }

        where.clear();
        where.put("id",detailId);

        Map<String,Object> field = new HashMap<String,Object>();
        field.put("status",VendorOrderDetailStatus.canceled);
        field.put("demandId",null);
        field.put("demandNum",null);
        field.put("demandOrgId",null);
        field.put("demandOrgName",null);

        //更新订单明细的状态为“取消”
        entityService.updateById(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL,detailId,field,user);

        int orderId = purchaseOrderService.findOrderIdByDetailId(Integer.parseInt(detailId),user);

        //记录操作日志
        int transId = transactionService.findTransId(MzfEntity.VENDOR_ORDER, orderId+"", user);
        logService.createLog(transId, MzfEntity.VENDOR_ORDER, orderId+"", "取消订单明细", null, detailId, null, user);

        //如果其他所有的商品已收货或者取消，更新订单状态为“已完成”
        where.clear();
        where.put("orderId",orderId);
        List<Map<String,Object>> details = entityService.list(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL,where,null,user);
        boolean c = true;
        for(Map<String,Object> d:details){
            VendorOrderDetailStatus s =  VendorOrderDetailStatus.valueOf(MapUtils.getString(d,"status"));
            if(s == VendorOrderDetailStatus.New || s == VendorOrderDetailStatus.waitReceive){
                c = false;
                break;
            }
        }

        if(c){
            Map<String,Object> f = new HashMap<String, Object>();
            VendorOrderStatus os = VendorOrderStatus.finished;
            f.put("status",os);
            entityService.updateById(MzfEntity.VENDOR_ORDER,orderId+"",f,user);
            transId = transactionService.findTransId(MzfEntity.VENDOR_ORDER, orderId+"", user);
            logService.createLog(transId, MzfEntity.VENDOR_ORDER, orderId+"", "订单完成", null, null, null, user);
        }

        //如果明细与要货申请相关，更新要货申请的状态为“待总部处理”
        String demandId = MapUtils.getString(detail, "demandId");
        if(StringUtils.isNotBlank(demandId)){
            where.clear();
            where.put("demandId",demandId);
            entityService.delete(MzfEntity.DEMAND_PROCESS,where,user);

            Map<String,Object> f = new HashMap<String, Object>();
            f.put("status", MzfEnum.DemandStatus.waitProcess);
            f.put("orderId",null);
            entityService.updateById(MzfEntity.DEMAND,demandId,f,user);
            transId = transactionService.findTransId(MzfEntity.DEMAND, demandId, user);
            logService.createLog(transId, MzfEntity.DEMAND, demandId, "取消委外加工，等待重新处理", null, null, null, user);
        }
    }

	public void cancelRawmaterial(Integer orderId, String remark, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", orderId);
		where.put("status", VendorOrderStatus.finished);

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("canceledRawmaterialRemark", remark);
		field.put("status", VendorOrderStatus.canceledRawmaterial);

		int row = entityService.update(MzfEntity.VENDOR_ORDER, field, where, user);
		if (row != 1) {
			throw new BusinessException("核销原料异常");
		}

		int transId = transactionService.findTransId(MzfEntity.VENDOR_ORDER, orderId.toString(), user);
		logService.createLog(transId, MzfEntity.VENDOR_ORDER, orderId.toString(), "核销原料", null, null, null, user);
	}

	public int createSettlement(SettlementType type, int orderId, BigDecimal price, String remark, IUser user) throws BusinessException {
		return purchaseOrderService.createSettlement(type, orderId, price, remark, user);
	}

	public Map<String, Object> getPrintData(int vendorOrderId, IUser user) throws BusinessException {
		Map<String, Object> order = purchaseOrderService.getPrintData(vendorOrderId, user);

		//配料明细
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orderId", vendorOrderId);
		OrderBy orderBy = new OrderBy(new String[]{"detailId"}, OrderByDir.desc);
		List<Map<String, Object>> dosing = entityService.list(MzfEntity.DOSING_VIEW, where, orderBy, user);
		dosing = getBizNameForDosing(dosing);

		//配料汇总
		where = new HashMap<String, Object>();
		where.put("orderId", vendorOrderId);
		List<Map<String, Object>> dosingSummary = entityService.list(MzfEntity.DOSING_SUMMARY_VIEW, where, orderBy, user);
		dosingSummary = getBizNameForDosing(dosingSummary);

		order.put("dosing", dosing);
		order.put("dosingSummary", dosingSummary);
		return order;
	}

	public Map<String, Object> getSummaryPrintData(Integer[] vendorOrderIds, IUser user) throws BusinessException {
		Map<String, Object> order = purchaseOrderService.getSummaryPrintData(vendorOrderIds, user);

		//配料明细
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orderId", vendorOrderIds);
		OrderBy orderBy = new OrderBy(new String[]{"detailId"}, OrderByDir.desc);
		List<Map<String, Object>> dosing = entityService.list(MzfEntity.DOSING_VIEW, where, orderBy, user);
		dosing = getBizNameForDosing(dosing);

		//配料汇总
		where = new HashMap<String, Object>();
		where.put("orderIds", StringUtils.join(vendorOrderIds, ", "));
		List<Map<String, Object>> dosingSummary = simpleSqlService.list("vendorOrder", "summaryDosing", where, user);
		dosingSummary = getBizNameForDosing(dosingSummary);

		order.put("dosing", dosing);
		order.put("dosingSummary", dosingSummary);
		return order;
	}

	private List<Map<String, Object>> getBizNameForDosing(List<Map<String, Object>> dosingSummarys) {
		for (Map<String, Object> dosingSummary : dosingSummarys) {
			String goldClass = MapUtils.getString(dosingSummary, "goldClass");
			dosingSummary.put("goldClassText", BizCodeService.getBizName("goldClass", goldClass));

			String rawmaterialType = MapUtils.getString(dosingSummary, "rawmaterialType");
			dosingSummary.put("rawmaterialTypeText", BizCodeService.getBizName("rowmaterialType", rawmaterialType));

			String diamondShape = MapUtils.getString(dosingSummary, "shape");
			dosingSummary.put("shapeText", BizCodeService.getBizName("diamondShape", diamondShape));

			String partsType = MapUtils.getString(dosingSummary, "partsType");
			dosingSummary.put("partsTypeText", BizCodeService.getBizName("partsType", partsType));

			String partsStandard = MapUtils.getString(dosingSummary, "partsStandard");
			dosingSummary.put("partsStandardText", BizCodeService.getBizName("partsStandard", partsStandard));

			String gravelStandard = MapUtils.getString(dosingSummary, "gravelStandard");
			dosingSummary.put("gravelStandardText", BizCodeService.getBizName("gravelStandard", gravelStandard));
		}

		return dosingSummarys;
	}
}


