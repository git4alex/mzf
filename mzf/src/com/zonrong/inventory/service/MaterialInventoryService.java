package com.zonrong.inventory.service;

import com.zonrong.basics.material.service.MaterialService;
import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.inventory.service.InventoryService.InventoryType;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-11-3
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class MaterialInventoryService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private InventoryService inventoryService;
	@Resource
	private MaterialService materialService;
	@Resource
	private MzfOrgService mzfOrgService;

	public void warehouse(BizType bizType, int materialId, BigDecimal quantity, BigDecimal cost, String costDesc, String remark, IUser user) throws BusinessException {
		int orgId = user.getOrgId();
		warehouse( bizType, orgId, materialId, quantity, cost, costDesc, remark, user);
	}

	public void warehouse(BizType bizType,int orgId, int materialId, BigDecimal quantity, BigDecimal cost, String costDesc, String remark, IUser user) throws BusinessException {
		Map<String, Object> inventory = inventoryService.findMaterialInventory(materialId, orgId, user);
		Integer inventoryId = null;
		if (inventory == null) {
			inventoryId = inventoryService.createMaterialInventory(materialId, orgId, "物料采购收货入库", user);
		} else {
			inventoryId = MapUtils.getInteger(inventory, "id");
		}

		inventoryService.addQuantity(bizType, inventoryId, quantity, cost, costDesc, remark, user);
	}


	public void delivery(BizType bizType, int inventoryId, BigDecimal quantity, BigDecimal cost, String costDesc, boolean isUpdateLockedQuantity, String remark, IUser user) throws BusinessException {
		inventoryService.deliveryByQuantityFlow(bizType, inventoryId, quantity, cost, costDesc, isUpdateLockedQuantity, remark, user);
	}

	public void deliveryByQuantity(BizType bizType, int materialId, BigDecimal quantity, BigDecimal cost, String costDesc, int orgId, boolean isUpdateLockedQuantity, String remark, IUser user) throws BusinessException {
		Map<String, Object> inventory = getMaterialInventory(materialId, orgId, user);
		Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");

		delivery(bizType, inventoryId, quantity, cost, costDesc, isUpdateLockedQuantity, remark, user);
	}

	public List<Map<String, Object>> listMaterialInventory(Integer[] materialIds, int orgId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.MATERIAL_INVENTORY_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		if (!ArrayUtils.isEmpty(materialIds)) {
			where.put("id", materialIds);
		}
		where.put("orgId", orgId);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());
		return list;
	}

	public Map<String, Object> getMaterialInventory(int materialId, int orgId, IUser user) throws BusinessException {
		List<Map<String, Object>> list = listMaterialInventory(new Integer[]{materialId}, orgId, user);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("库存中未找到该物料[" + materialId + "]");
		} else if (list.size() > 1) {
			throw new BusinessException("库存中找到多件物料[" + materialId + "]");
		}
		Map<String, Object> inventory = list.get(0);

		return inventory;
	}

//	public void lockByQuantityByInventoryId(Map<Integer, BigDecimal> map, String remark, IUser user) throws BusinessException {
//		if (MapUtils.isEmpty(map)) {
//			return;
//		}
//
//		for (Iterator<Entry<Integer, BigDecimal>> it = map.entrySet().iterator(); it.hasNext();) {
//			Entry<Integer, BigDecimal> en = it.next();
//			int inventoryId = en.getKey();
//			BigDecimal lockedQuantity = en.getValue();
//			inventoryService.addLockedQuantity(inventoryId, lockedQuantity, user);
//		}
//	}
//
//	public void lockByQuantity(Map<Integer, BigDecimal> map, String remark, IUser user) throws BusinessException {
//		if (MapUtils.isEmpty(map)) {
//			return;
//		}
//
//		Map<Integer, BigDecimal> newMap = convert(map, user);
//		for (Iterator<Entry<Integer, BigDecimal>> it = newMap.entrySet().iterator(); it.hasNext();) {
//			Entry<Integer, BigDecimal> en = it.next();
//			int inventoryId = en.getKey();
//			BigDecimal lockedQuantity = en.getValue();
//			inventoryService.addLockedQuantity(inventoryId, lockedQuantity, user);
//		}
//	}

//	public void freeByQuantity(Map<Integer, BigDecimal> map, String remark, IUser user) throws BusinessException {
//		if (MapUtils.isEmpty(map)) {
//			return;
//		}
//
//		Map<Integer, BigDecimal> newMap = convert(map, user);
//		for (Iterator<Entry<Integer, BigDecimal>> it = newMap.entrySet().iterator(); it.hasNext();) {
//			Entry<Integer, BigDecimal> en = it.next();
//			int inventoryId = en.getKey();
//			BigDecimal lockedQuantity = en.getValue();
//			lockedQuantity = lockedQuantity.multiply(new BigDecimal(-1));
//			inventoryService.addLockedQuantity(inventoryId, lockedQuantity, user);
//		}
//	}

	public void send(int materialId, BigDecimal quantity, int sourceOrgId, String remark, IUser user) throws BusinessException {
		Map<String, Object> inventory =  getMaterialInventory(materialId, sourceOrgId, user);
		Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
		BigDecimal cost = new BigDecimal(MapUtils.getString(inventory, "unitCost","0")).multiply(quantity,MathContext.DECIMAL32);
		inventoryService.createFlowOnQuantity(BizType.send, inventoryId, quantity, InventoryType.delivery, cost.doubleValue(), null, remark, user);
	}

	public void receive(int materialId, BigDecimal quantity, BigDecimal cost, int sourceOrgId, int targetOrgId, String remark, IUser user) throws BusinessException {
		if (targetOrgId != user.getOrgId()) {
			throw new BusinessException("操作员所在部门非调入部门，不允许收货");
		}
		if (sourceOrgId == targetOrgId) {
			throw new BusinessException("调出和调入部门相同，不允许收货");
		}

		BigDecimal warehouseCost = cost;
		if (mzfOrgService.isHq(sourceOrgId) || mzfOrgService.isHq(targetOrgId)) {
			//更新总部物料成本
			Map<String, Object> inventoryHQ =  getMaterialInventory(materialId, mzfOrgService.getHQOrgId(), user);
			BigDecimal costHQ = new BigDecimal(MapUtils.getString(inventoryHQ, "cost", Integer.toString(0)));
			BigDecimal quantityHQ = new BigDecimal(MapUtils.getString(inventoryHQ, "quantity", Integer.toString(0)));

			BigDecimal newCostHQ = null;
			if (quantityHQ.doubleValue() != 0) {
				if (mzfOrgService.isHq(sourceOrgId)) {
					newCostHQ = (quantityHQ.subtract(quantity)).divide(quantityHQ, 2, BigDecimal.ROUND_HALF_EVEN).multiply(costHQ);
				}
				if (mzfOrgService.isHq(targetOrgId)) {
					newCostHQ = (quantityHQ.add(quantity)).divide(quantityHQ, 2, BigDecimal.ROUND_HALF_EVEN).multiply(costHQ);
				}
				warehouseCost = newCostHQ.subtract(costHQ);
			} else {
				newCostHQ = cost.abs();
			}

			Map<String, Object> field = new HashMap<String, Object>();
			field.put("cost", newCostHQ);
			entityService.updateById(MzfEntity.MATERIAL, Integer.toString(materialId), field, user);
		} else {
			throw new BusinessException("只允许总部与门店之间调拨");
		}


		Map<String, Object> inventory =  getMaterialInventory(materialId, sourceOrgId, user);
		Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
		String costDesc = "批发价: " + cost;
//		delivery(BizType.receive, inventoryId, quantity, cost, costDesc, true, remark, user);

		inventoryService.deliveryByQuantityIgnoreFlow(BizType.receive, inventoryId, quantity, null, null, true, remark, user);
		warehouse(BizType.receive, materialId, quantity, warehouseCost.abs(), costDesc, remark, user);

//		materialService.addCost(materialId, cost.abs().multiply(new BigDecimal(-1)), sourceOrgId, user);
//		materialService.addCost(materialId, cost.abs(), targetOrgId, user);
	}

//	/**
//	 * 传入的参数中的key值为原料Id，将其对应成库存Id
//	 * @param map	key:存放materialId
//	 * @param user
//	 * @return		key:存放inventoryId
//	 * @throws BusinessException
//	 */
//	private Map<Integer, BigDecimal> convert(Map<Integer, BigDecimal> map, IUser user) throws BusinessException {
//		Integer[] materialId = new ArrayList<Integer>(map.keySet()).toArray(new Integer[]{});
//		Map<String, Object> where = new HashMap<String, Object>();
//		where.put("orgId", user.getOrgId());
//		where.put("targetType", TargetType.material);
//		where.put("targetId", materialId);
//		EntityMetadata metadata = inventoryService.getEntityMetadataOfInventory();
//		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());
//		Map<Integer, BigDecimal> newMap = new HashMap<Integer, BigDecimal>();
//		for (Map<String, Object> dbInventory : list) {
//			Integer inventoryId = MapUtils.getInteger(dbInventory, metadata.getPkCode());
//			Integer materialIdTemp = MapUtils.getInteger(dbInventory, "targetId");
//			BigDecimal lockedQuantity = map.get(materialIdTemp);
//			if (inventoryId == null || lockedQuantity == null) {
//				throw new BusinessException("获取参数错误");
//			}
//
//			newMap.put(inventoryId, lockedQuantity);
//		}
//
//		return newMap;
//	}
}


