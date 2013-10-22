package com.zonrong.inventory.product.service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.InventoryStatus;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.InventoryService;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.inventory.service.InventoryService.InventoryType;
import com.zonrong.metadata.EntityMetadata;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2011-3-15
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class MaintainInventoryService extends ProductInventoryService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private InventoryService inventoryService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;

    /**
     * 委外维修商品出库（从维修库调入临时库）
     * @param productIds
     * @param remark
     * @param user
     * @throws BusinessException
     */
	public void deliveryFromMaintain(Integer[] productIds, String remark, IUser user) throws BusinessException {
		List<Map<String, Object>> inventoryList = listInventoryForProduct(productIds, null);

		StorageType target = StorageType.product_temporary;

		List<Integer> inventoryIds = new ArrayList<Integer>();
		for (Map<String, Object> inventory : inventoryList) {
			inventoryIds.add(MapUtils.getInteger(inventory, "id"));
			String productId = MapUtils.getString(inventory, "targetId");
			StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));

			Integer orgId = MapUtils.getInteger(inventory, "orgId");
            //维修库出库记录
			inventoryService.createFlow(BizType.deliveryFromMaintain, orgId,
                    new BigDecimal(1), InventoryType.delivery, storageType, TargetType.product, productId, null, remark, user);
            //临时库入库记录
			inventoryService.createFlow(BizType.deliveryFromMaintain, orgId,
                    new BigDecimal(1), InventoryType.warehouse, target, TargetType.product, productId, null, remark, user);

			//记录流程
			Integer transId = transId = transactionService.findTransId(MzfEntity.PRODUCT, productId, user);;
			logService.createLog(transId, MzfEntity.PRODUCT, productId, "委外维修出库",
					TargetType.product, productId, remark, user);
		}

		EntityMetadata metadata = inventoryService.getEntityMetadataOfInventory();
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("storageType", target);

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", inventoryIds.toArray(new Integer[]{}));
		entityService.update(metadata, field, where, user);

		//更新库存状态
		inventoryService.updateStatus(inventoryIds.toArray(new Integer[]{}), InventoryStatus.onStorage, InventoryStatus.deliveryMaintain, "出库失败", remark, user);
	}

//	public void deliveryFromMaintain(Integer[] productIds, String remark, IUser user) throws BusinessException {
//		List<Map<String, Object>> inventoryList = listInventoryForProduct(productIds, null);
//		List<Integer> inventoryIds = new ArrayList<Integer>();
//		for (Map<String, Object> inventory : inventoryList) {
//			Integer dbInventoryId = MapUtils.getInteger(inventory, "id");
//			inventoryIds.add(dbInventoryId);
//
//			//记录库存流水
//			StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
//			Integer productId = MapUtils.getInteger(inventory, "targetId");
//
//			Integer orgId = MapUtils.getInteger(inventory, "orgId");
//			inventoryService.createFlow(BizType.deliveryFromMaintain, orgId, new BigDecimal(1), InventoryType.delivery, storageType, TargetType.product, Integer.toString(productId), null, remark, user);
//
//			//记录流程
//			int transId = transactionService.findTransId(BizEntity.PRODUCT, Integer.toString(productId), user);
//			logService.createLog(transId, BizEntity.PRODUCT, Integer
//					.toString(productId), "维修出库",
//					TargetType.product,
//					Integer.toString(productId), remark, user);
//		}
//
//		//更新库存状态
//		inventoryService.updateStatus(inventoryIds.toArray(new Integer[]{}), InventoryStatus.onStorage, InventoryStatus.deliveryMaintain, "出库失败", remark, user);
//	}

    /**
     * 临时库调入维修库
     * @param productId        商品ID
     * @param remark
     * @param user
     * @throws BusinessException
     */
	public void warehouseToMaintain(int productId, String remark, IUser user) throws BusinessException {
		//更新库存状态
		Map<String, Object> inventory = getInventoryForProduct(productId, null);
		StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
		StorageType target = StorageType.product_maintain;
		Integer orgId = MapUtils.getInteger(inventory, "orgId");
        //临时库出库记录
		inventoryService.createFlow(BizType.deliveryFromMaintain, orgId,
                new BigDecimal(1), InventoryType.delivery, storageType, TargetType.product, Integer.toString(productId), null, remark, user);
        //维修库入库记录
		inventoryService.createFlow(BizType.deliveryFromMaintain, orgId,
                new BigDecimal(1), InventoryType.warehouse, target, TargetType.product, Integer.toString(productId), null, remark, user);

		Integer inventoryId = MapUtils.getInteger(inventory, "id");
		Integer[] inventoryIds = new Integer[]{inventoryId};
		EntityMetadata metadata = inventoryService.getEntityMetadataOfInventory();
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("storageType", target);

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", inventoryIds);
		entityService.update(metadata, field, where, user);

		//更新库存状态
		inventoryService.updateStatus(inventoryIds, InventoryStatus.deliveryMaintain,
                InventoryStatus.onStorage, "委外维修收货入库", remark, user);

		//记录流程
		int transId = transactionService.findTransId(MzfEntity.PRODUCT, Integer.toString(productId), user);
		logService.createLog(transId, MzfEntity.PRODUCT, Integer.toString(productId), "委外维修收货入库",
				TargetType.product, Integer.toString(productId), remark, user);
	}

//	public void warehouseToMaintain(int productId, String remark, IUser user) throws BusinessException {
//		//更新库存状态
//		Map<String, Object> inventory = getInventoryForProduct(productId, null);
//		Integer inventoryId = MapUtils.getInteger(inventory, "id");
//		inventoryService.updateStatus(new Integer[]{inventoryId}, InventoryStatus.deliveryMaintain, InventoryStatus.onStorage, "入库失败", null, user);
//
//		//记录库存流水
//		StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
//		Integer orgId = MapUtils.getInteger(inventory, "orgId");
//		inventoryService.createFlow(BizType.warehouseToMaintain, orgId, new BigDecimal(1), InventoryType.warehouse, storageType, TargetType.product, Integer.toString(productId), null, remark, user);
//
//		//记录流程
//		int transId = transactionService.findTransId(BizEntity.PRODUCT, Integer.toString(productId), user);
//		logService.createLog(transId, BizEntity.PRODUCT, Integer.toString(productId), "收获登记入维修库",
//				TargetType.product, Integer.toString(productId), remark, user);
//	}
}


