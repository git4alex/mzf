package com.zonrong.inventory.service;

import com.zonrong.basics.product.service.ProductService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.*;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.demand.product.service.ProductDemandService;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.purchase.dosing.service.DosingService.DosingStatus;
import com.zonrong.purchase.service.DetailCRUDService.VendorOrderDetailStatus;
import com.zonrong.system.service.BizCodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * date: 2010-11-3
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class TemporaryInventoryService extends ProductInventoryService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private ProductService productService;
	@Resource
	private InventoryService inventoryService;
	@Resource
	private ProductDemandService demandService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	public void transferToProductStorage(Integer[] productIds, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(productIds)) {
			return;
		}

		List<Map<String, Object>> dbProductInventoryList = list(productIds, null);
		List<String> tempList = new ArrayList<String>();
		List<String> isQcList = new ArrayList<String>();
		List<String> isCidList = new ArrayList<String>();
		List<String> detailIdList = new ArrayList<String>();
		List<String> productTempNumList = new ArrayList<String>();
		for (Map<String, Object> productInventory : dbProductInventoryList) {
			ProductType ptype = ProductType.valueOf(MapUtils.getString(productInventory, "ptype"));
			Float costPrice = MapUtils.getFloat(productInventory, "costPrice");
			Float retailBasePrice = MapUtils.getFloat(productInventory, "retailBasePrice");
			String productTempNum = MapUtils.getString(productInventory, "productTempNum");

			if (costPrice == null) {
				tempList.add(productTempNum);
			} else if ((ptype.isDiamond() || ptype == ProductType.kGold) && retailBasePrice == null) {
				tempList.add(productTempNum);
			}
			if (!productService.isQc(productInventory)) {
				isQcList.add(productTempNum);
			}
			if (!productService.isCid(productInventory)) {
				isCidList.add(productTempNum);
			}
//			String detailId = MapUtils.getString(productInventory, "detailId");
//			if (StringUtils.isNotBlank(detailId)) {
//				detailIdList.add(productTempNum);
//			}
			String productNum = MapUtils.getString(productInventory, "num");
			if (StringUtils.isBlank(productNum)) {
				productTempNumList.add(productTempNum);
			}

		}
		if (isQcList.size() > 0) {
			throw new BusinessException("临时条码为" + isQcList +"的商品尚未QC，不能调入商品库");
		}
//		if (isCidList.size() > 0) {
//			throw new BusinessException("临时条码为" + isCidList +"的商品尚未做证书，不能调入商品库");
//		}
		if (tempList.size() > 0) {
			throw new BusinessException("临时条码为" + tempList +"的商品价格信息不全（成本价、一口价），不能调入商品库");
		}
//		if (detailIdList.size() > 0) {
//			throw new BusinessException("临时条码为" + detailIdList +"的商品尚未核销原料，不能调入商品库");
//		}
		if (productTempNumList.size() > 0) {
			throw new BusinessException("临时条码为" + productTempNumList +"的商品尚未有商品条码，不能调入商品库");
		}

		for (Map<String, Object> productInventory : dbProductInventoryList) {
			transferToProductStorage(productInventory, user);
			//记录操作日志
			businessLogService.log("临时库调入商品库", "商品编号：[" + MapUtils.getInteger(productInventory, "id")+"]", user);
		}

		//更新对应的要货申请状态
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("productId", productIds);
		List<Map<String, Object>> list = entityService.list(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, where, null, user.asSystem());
		for (Map<String, Object> detail : list) {
			Integer productId = MapUtils.getInteger(detail, "productId");
			Integer demandId = MapUtils.getInteger(detail, "demandId");
			if (demandId != null) {
				DemandStatus status = demandService.getStatus(demandId, DemandStatus.class, user);
				if (status == DemandStatus.machining) {
					demandService.recieveProduct(demandId, productId, user);
				}
			}
		}
	}

	private void checkIsQC(Integer[] productIds, IUser user)throws BusinessException{
		if (ArrayUtils.isEmpty(productIds)) {
			return;
		}

		List<Map<String, Object>> dbProductInventoryList = list(productIds, null);
		List<String> tempList = new ArrayList<String>();
		List<String> isQcList = new ArrayList<String>();
		List<String> isCidList = new ArrayList<String>();
		List<String> detailIdList = new ArrayList<String>();
		List<String> productTempNumList = new ArrayList<String>();
		for (Map<String, Object> productInventory : dbProductInventoryList) {
			ProductType ptype = ProductType.valueOf(MapUtils.getString(productInventory, "ptype"));
			Float costPrice = MapUtils.getFloat(productInventory, "costPrice");
			Float retailBasePrice = MapUtils.getFloat(productInventory, "retailBasePrice");
			String productTempNum = MapUtils.getString(productInventory, "productTempNum");

			if (costPrice == null) {
				tempList.add(productTempNum);
			} else if ((ptype.isDiamond() || ptype == ProductType.kGold) && retailBasePrice == null) {
				tempList.add(productTempNum);
			}
			if (!productService.isQc(productInventory)) {
				isQcList.add(productTempNum);
			}
			if (!productService.isCid(productInventory)) {
				isCidList.add(productTempNum);
			}
//			String detailId = MapUtils.getString(productInventory, "detailId");
//			if (StringUtils.isNotBlank(detailId)) {
//				detailIdList.add(productTempNum);
//			}
			String productNum = MapUtils.getString(productInventory, "num");
			if (StringUtils.isBlank(productNum)) {
				productTempNumList.add(productTempNum);
			}

		}
		if (isQcList.size() > 0) {
			throw new BusinessException("临时条码为" + isQcList +"的商品尚未QC，不能打印入库单");
		}
//		if (isCidList.size() > 0) {
//			throw new BusinessException("临时条码为" + isCidList +"的商品尚未做证书，不能调入商品库");
//		}
		if (tempList.size() > 0) {
			throw new BusinessException("临时条码为" + tempList +"的商品价格信息不全（成本价、一口价），不能打印入库单");
		}
//		if (detailIdList.size() > 0) {
//			throw new BusinessException("临时条码为" + detailIdList +"的商品尚未核销原料，不能调入商品库");
//		}
		if (productTempNumList.size() > 0) {
			throw new BusinessException("临时条码为" + productTempNumList +"的商品尚未有商品条码，不能打印入库单");
		}
	}

	//入商品库打印
	public Map<String, Object> getPrintData(Integer[] ids, IUser user)throws BusinessException{
		checkIsQC(ids, user);

		Map<String, Object> data = new HashMap<String, Object>();
		List<Map<String, Object>> products = new ArrayList<Map<String, Object>>();
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", ids);
		where.put("storageType", "product_temporary");
		String ptype = "";
		String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		List<Map<String, Object>> dataList = entityService.list(MzfEntity.PRODUCT_INVENTORY_VIEW, where, null, user);
		for (Map<String, Object> map : dataList) {
			Map<String, Object> product = productService.get(MapUtils.getIntValue(map, "id", 0), user);
			product.put("tempNum", MapUtils.getString(map, "productTempNum", ""));
			String sourceId = MapUtils.getString(map, "sourceId", "");
			if (!"".equals(ptype) && !ptype.equals(MapUtils.getString(map, "pType", ""))) {
				throw new BusinessException("打印单中有不同的商品类型");
			}
			ptype = MapUtils.getString(map, "pType", "");

			if (MapUtils.getString(map, "source", "").equals("renovate")) {
				Map<String, Object> filter = new HashMap<String, Object>();
				filter.put("oldProductNum", sourceId);
				List<Map<String, Object>> list = entityService.list(MzfEntity.RENOVATE, filter, null, user);
				if (CollectionUtils.isNotEmpty(list)) {
					product.put("orderNum", MapUtils.getString(list.get(0),"num", ""));
				} else {
					product.put("orderNum", sourceId);
				}
			}
			products.add(product);

		}
		data.put("nowDate", nowDate);
		data.put("ptypeText", BizCodeService.getBizName("productType", ptype));
		data.put("dataList", products);
		return data;
	}
	//修改库存打印状态
	public void updatePrintStatus(Integer[] ids, IUser user)throws BusinessException{
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("targetId", ids);
		where.put("targetType", "product");
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("isPrint", "true");
		entityService.update(MzfEntity.INVENTORY, field, where, user);
	}
	private void transferToProductStorage(Map<String, Object> productInventory, IUser user) throws BusinessException {
		Integer productId = MapUtils.getInteger(productInventory, "id");

		ProductType ptype = ProductType.valueOf(MapUtils.getString(productInventory, "ptype"));
		StorageType target = ptype.getStorageType();

        String vendorOrderNum = MapUtils.getString(productInventory,"sourceId","unknown");
		String remark = "委外/采购订单编号：["+vendorOrderNum+"]";

		StorageType storageType = StorageType.valueOf(MapUtils.getString(productInventory, "storageType"));
		Integer orgId = MapUtils.getInteger(productInventory, "orgId");
		//出临时库流水
		inventoryService.createFlow(MzfEnum.BizType.transferToProductStorage, orgId,
                new BigDecimal(1), MzfEnum.InventoryType.delivery,
                storageType, TargetType.product, Integer.toString(productId), null, remark, user);
		//入商品库流水
		inventoryService.createFlow(MzfEnum.BizType.transferToProductStorage, orgId,
                new BigDecimal(1), MzfEnum.InventoryType.warehouse,
                target, TargetType.product, Integer.toString(productId), null, remark, user);

		EntityMetadata metadata = inventoryService.getEntityMetadataOfInventory();
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("storageType", target);
		field.put("cdate", null);
		field.put("cuserId", null);

		Integer inventoryId = MapUtils.getInteger(productInventory, "inventoryId");
		entityService.updateById(metadata, inventoryId.toString(), field, user);
	}

	public void deliveryFromTemporary(Integer[] productIds, String deliveryTemporaryReason, String remark, IUser user) throws BusinessException {
		List<Map<String, Object>> inventoryList = list(productIds, null);
		List<Integer> inventoryIds = new ArrayList<Integer>();
		for (Map<String, Object> inventory : inventoryList) {
			Integer dbInventoryId = MapUtils.getInteger(inventory, "inventoryId");
			inventoryIds.add(dbInventoryId);

			String deliveryReasonText = BizCodeService.getBizName("deliveryTemporaryReason", deliveryTemporaryReason);
			//记录库存流水
			StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
			Integer porductId = MapUtils.getInteger(inventory, "id");

			Integer orgId = MapUtils.getInteger(inventory, "orgId");
			inventoryService.createFlow(MzfEnum.BizType.deliveryFromTemporary, orgId,new BigDecimal(1),
                    MzfEnum.InventoryType.delivery, storageType,TargetType.product, Integer.toString(porductId),
                    null, deliveryReasonText, user);

			//记录流程
			int transId = transactionService.findTransId(MzfEntity.PRODUCT, Integer.toString(porductId), user);
			logService.createLog(transId, MzfEntity.PRODUCT, Integer.toString(porductId), "临时出库",
					TargetType.product,	Integer.toString(porductId), deliveryReasonText + "	" + remark, user);
			//记录操作日志
			businessLogService.log("临时库出库", "商品编号：" + porductId, user);
		}

		//更新库存状态
		inventoryService.updateStatus(inventoryIds.toArray(new Integer[]{}),
                InventoryStatus.onStorage,
                InventoryStatus.deliveryTemporary, "出库失败", remark, user);
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.INVENTORY);
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("deliveryTemporaryReason", deliveryTemporaryReason);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), inventoryIds.toArray(new Integer[]{}));
		entityService.update(metadata, field, where, user);
	}

	public void warehouseToTemporary(int productId, IUser user) throws BusinessException {
		//更新库存状态
		Map<String, Object> inventory = getInventory(productId, null);
		Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
		inventoryService.updateStatus(new Integer[]{inventoryId}, InventoryStatus.deliveryTemporary, InventoryStatus.onStorage, "入库失败", null, user);

		//更新商品QC和CID状态
		updateProductAndInventory(productId, user);

		//记录库存流水
        String deliveryTemporaryReason = MapUtils.getString(inventory, "deliveryTemporaryReason");
        DeliveryTemporaryReason reason = null;
        try {
            reason = MzfEnum.DeliveryTemporaryReason.valueOf(deliveryTemporaryReason);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }

        String remark = "入临时库";
        if(reason == MzfEnum.DeliveryTemporaryReason.CID){
            remark = "做证书入库";
        }else if(reason== MzfEnum.DeliveryTemporaryReason.QC){
            remark = "QC入库";
        }

		StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
		Integer orgId = MapUtils.getInteger(inventory, "orgId");
		inventoryService.createFlow(MzfEnum.BizType.warehouseToTemporary, orgId,
                new BigDecimal(1), MzfEnum.InventoryType.warehouse, storageType, TargetType.product, Integer.toString(productId), null, remark, user);

		//记录流程
		int transId = transactionService.findTransId(MzfEntity.PRODUCT, Integer.toString(productId), user);
		logService.createLog(transId, MzfEntity.PRODUCT, Integer.toString(productId), remark,
				TargetType.product, Integer.toString(productId), null, user);
		//记录操作日志
		businessLogService.log("临时库入库", "商品编号: " + productId, user);
	}

	private void updateProductAndInventory(int productId, IUser user) throws BusinessException {
		Map<String, Object> productInventory = getInventory(productId, null);
//		ProductType ptype = ProductType.valueOf(MapUtils.getString(productInventory, "ptype"));
//		String productNum = MapUtils.getString(productInventory, "num");
//		if (ptype.isDiamond() && StringUtils.isNotBlank(productNum)) {
//			return;
//		}

		String deliveryTemporaryReason = MapUtils.getString(productInventory, "deliveryTemporaryReason");
		DeliveryTemporaryReason reason = null;
		try {
			reason = MzfEnum.DeliveryTemporaryReason.valueOf(deliveryTemporaryReason);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		Map<String, Object> field = new HashMap<String, Object>();
		if (reason == MzfEnum.DeliveryTemporaryReason.QC) {
			field.put("isQc", Boolean.toString(true));
		} else if (reason == MzfEnum.DeliveryTemporaryReason.CID) {
			field.put("isCid", Boolean.toString(true));
		}
		if (MapUtils.isNotEmpty(field)) {
			entityService.updateById(MzfEntity.PRODUCT, Integer.toString(productId), field, user);

			Integer inventoryId = MapUtils.getInteger(productInventory, "inventoryId");
			field = new HashMap<String, Object>();
			field.put("deliveryTemporaryReason", null);
			entityService.updateById(MzfEntity.INVENTORY, inventoryId.toString(), field, user);
		}
	}

	/**
	 * 从临时库删除商品
     *
	 * @param productId
	 * @param user
	 * @throws BusinessException
	 */
	public void dropProduct(int productId, String remark, IUser user) throws BusinessException {
		Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, productId, user.asSystem());
		String productNum = MapUtils.getString(product, "num");
		if (StringUtils.isNotBlank(productNum)) {
			throw new BusinessException("该商品已经生成商品条码，不能返厂");
		}

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("productId", productId);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("未找到该商品对应的商品采购/委外加工订单明细");
		}

		if (list.size() > 1) {
			throw new BusinessException("同一件商品对应多个商品采购/委外订单的明细");
		}

		Map<String, Object> detail = list.get(0);
		Integer detailId = MapUtils.getInteger(detail, "id");
		Integer orderId = MapUtils.getInteger(detail, "orderId");
		VendorOrderDetailStatus status = VendorOrderDetailStatus.valueOf(MapUtils.getString(detail, "status"));

		where.clear();
		where.put("detailId", detailId);
		where.put("status", DosingStatus.canceled);
		List<Map<String, Object>> list1 = entityService.list(MzfEntity.DOSING, where, null, user.asSystem());
		if (CollectionUtils.isNotEmpty(list1)) {
			throw new BusinessException("已经核销原料，不能返厂");
		}

		//更新采购/委外订单明细状态，使其可以重新收货
		updateProductOrderDetail(detailId, orderId, status, user);

		//出库
		deliveryByProductId(MzfEnum.BizType.dropProduct, productId, "返厂出库", InventoryStatus.onStorage, user);

		//删除商品相关信息
		productService.deleteById(productId, user);

		//记录流程信息
		int transId = transactionService.findTransId(MzfEntity.VENDOR_ORDER, Integer.toString(orderId), user);
		logService.createLog(transId, MzfEntity.VENDOR_ORDER, Integer.toString(orderId), "返厂", null, null, remark, user);

		//将本次记录写入供应商日志
		Map<String, Object> order = entityService.getById(MzfEntity.VENDOR_ORDER, orderId, user.asSystem());
		Integer vendorId = MapUtils.getInteger(order, "vendorId");
		createVendorMeno(vendorId, "返厂", remark, user);
	}

	private void createVendorMeno(int vendorId, String name, String remark, IUser user) throws BusinessException {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        String occurrenceTime = dateformat.format(new Date());

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("vendorId", vendorId);
		field.put("name", "退货");
		field.put("occurrenceTime", occurrenceTime);
		field.put("description", remark);
		field.put("type", "drop");
		field.put("status", "finished");
		field.put("description", remark);
		field.put("cdate", null);
		field.put("cuserId", null);

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_MEMO);
		entityService.create(metadata, field, user);
	}

	private void updateProductOrderDetail(Integer detailId, Integer orderId, VendorOrderDetailStatus status, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL);
		if (VendorOrderDetailStatus.received != status) {
			throw new BusinessException("ID为["+ detailId +"]的订单明细非核销状态");
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("productId", null);
		field.put("status", VendorOrderDetailStatus.waitReceive);
		entityService.updateById(metadata, Integer.toString(detailId), field, user);


		//更新订单状态(如果全部收货，状态为订单完成，否则为收获中)
		field.clear();
		field.put("status", VendorOrderStatus.receiving);
		EntityMetadata orderMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER);
		entityService.updateById(orderMetadata, Integer.toString(orderId), field, user);
	}
}


