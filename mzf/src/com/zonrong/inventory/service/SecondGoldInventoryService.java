package com.zonrong.inventory.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.basics.rawmaterial.service.RawmaterialService;
import com.zonrong.basics.rawmaterial.service.RawmaterialService.RawmaterialType;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.inventory.service.InventoryService.InventoryType;
import com.zonrong.inventory.service.RawmaterialInventoryService.GoldClass;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-11-3
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class SecondGoldInventoryService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private RawmaterialInventoryService rawmaterialInventoryService;  		
	@Resource
	private RawmaterialService rawmaterialService;
	@Resource
	private InventoryService inventoryService;
	
	public void warehouse(BizType bizType, int secondGoldId, BigDecimal quantity, BigDecimal cost, String costDesc, String remark, IUser user) throws BusinessException {
		StorageType storageType = StorageType.second_secondGold;
		Map<String, Object> inventory = inventoryService.findSecondGoldInventory(secondGoldId, user.getOrgId(), storageType, user);
		Integer inventoryId = null;
		if (inventory == null) {
			inventoryId = inventoryService.createSecondGoldInventory(secondGoldId, user.getOrgId(), storageType, remark, user);
		} else {
			inventoryId = MapUtils.getInteger(inventory, "id");
		}
		
		inventoryService.addQuantity(bizType, inventoryId, quantity, cost, "成本", remark, user);
	}	

	public void warehouse(BizType bizType, GoldClass goldClass, BigDecimal quantity, BigDecimal cost, String remark, IUser user) throws BusinessException {
		Integer secondGoldId = rawmaterialService.findSecondGold(goldClass, user);
		if (secondGoldId == null) {
			Map<String, Object> rawmaterial = new HashMap<String, Object>();
			rawmaterial.put("type", RawmaterialType.secondGold);
			rawmaterial.put("goldClass", goldClass);
			rawmaterial.put("quantity", 0);
			rawmaterial.put("cost", 0);
			secondGoldId = rawmaterialService.createRawmaterial(rawmaterial, user);
		} 
		
		rawmaterialInventoryService.warehouseSecondGold(bizType, secondGoldId, quantity, cost, remark, user);
	}
	
	public List<Map<String, Object>> listSecondGoldInventory(Integer[] secondGoldIds, int orgId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SECOND_GOLD_INVENTORY_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", secondGoldIds);
		where.put("orgId", orgId);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());		
		return list;
	}
	
	public Map<String, Object> getSecondGoldInventory(int secondGoldId, int orgId, IUser user) throws BusinessException {
		List<Map<String, Object>> list = listSecondGoldInventory(new Integer[]{secondGoldId}, orgId, user);	
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("库存中未找到该旧金[" + secondGoldId + "]");
		} else if (list.size() > 1) {
			throw new BusinessException("库存中找到多件旧金[" + secondGoldId + "]");
		}
		Map<String, Object> inventory = list.get(0);
		
		return inventory;
	}
	

	public void delivery(BizType bizType, int inventoryId, BigDecimal quantity, BigDecimal cost, String costDesc, boolean isUpdateLockedQuantity, String remark, IUser user) throws BusinessException {
		inventoryService.deliveryByQuantityFlow(bizType, inventoryId, quantity, cost, costDesc, isUpdateLockedQuantity, remark, user);
	}
	
	public void delivery(BizType bizType, GoldClass goldClass, int orgId, BigDecimal quantity, BigDecimal cost, String costDesc, boolean isUpdateLockedQuantity, String remark, IUser user) throws BusinessException {
		Integer secondGoldId = rawmaterialService.findSecondGold(goldClass, user);
		if (secondGoldId == null) {
			throw new BusinessException("旧金库中无此成色的金料");
		}
		Map<String, Object> inventory =  getSecondGoldInventory(secondGoldId, orgId, user);
		Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
		
		delivery(bizType, inventoryId, quantity, cost, costDesc, isUpdateLockedQuantity, remark, user);
	}
	
	public void send(int secondGoldId, BigDecimal quantity, int sourceOrgId, String remark, IUser user) throws BusinessException {
		Map<String, Object> inventory =  getSecondGoldInventory(secondGoldId, sourceOrgId, user);
		Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
		
		inventoryService.createFlowOnQuantity(BizType.send, inventoryId, quantity, InventoryType.delivery, null, null, remark, user);
	}
	
	public void receive(int secondGoldId, BigDecimal quantity, BigDecimal actualQuantity, int sourceOrgId, int targetOrgId, String remark, IUser user) throws BusinessException {
		if (targetOrgId != user.getOrgId()) {
			throw new BusinessException("操作员所在部门非调入部门，不允许收货");
		}
		
		Map<String, Object> inventory =  getSecondGoldInventory(secondGoldId, sourceOrgId, user);
		Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
		
		inventoryService.deliveryByQuantityIgnoreFlow(BizType.receive, inventoryId, quantity, null, null, true, remark, user);
		warehouse(BizType.receive, secondGoldId, actualQuantity, null, null, remark, user);

		//记录损耗
//		if (quantity.doubleValue() != actualQuantity.doubleValue()) {
//			BigDecimal lossQuantity = quantity.subtract(actualQuantity);
//			delivery(BizType.receive, inventoryId, lossQuantity, null, null, false, "旧金调拨损耗", user);
//		}
	}	
}


