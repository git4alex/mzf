package com.zonrong.inventory.service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.InventoryStatus;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
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
public class InventoryService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;

	public enum InventoryType {
		warehouse,		//入库
		delivery		//出库
	}

	public enum BizType {
		addMaterial,	//新增物料信息
		register,		//收货登记
        oemReturn,      //委外原料退库
		send,			//发货
		receive,		//从其它部门收货
		returned,		//退货
		renovate,		//翻新
		transferToTemporary,		//调拨如临时库
		transferToProductStorage,	//调入商品库
		dropProduct,				//返厂
		deliveryFromTemporary,		//临时出库
		deliveryFromMaintain,		//委外维修出库
		warehouseToTemporary,		//临时库入库
		warehouseToMaintain,		//维修库入库
		warehouseOnSplit,			//拆旧入库
		maintainOver,				//维修完成
		OEM,
		sell,						//销售
		maintailSell,				//维修销售入库
		buySecondGold,				//旧金回收
		buySecondProduct,			//旧饰回收
		maintain,					//维修
		translateToProduct,			//原料裸石转化为商品
		translateToRawmaterial,		//商品裸钻转化为原料裸石
		delivery                    //强制出库
	}

	public Map<String, Object> findProductInventory(int productId, int orgId, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId", orgId);
		where.put("targetType", TargetType.product);
		where.put("targetId", productId);
		return getInventory(where, user);
	}

	public Map<String, Object> findRawmaterialInventory(int rawmaterialId, int orgId, StorageType storageType, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId", orgId);
		where.put("storageType", storageType);
		where.put("targetType", TargetType.rawmaterial);
		where.put("targetId", rawmaterialId);
		return getInventory(where, user);
	}

	public Map<String, Object> findSecondGoldInventory(int secondGoldId, int orgId, StorageType storageType, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId", orgId);
		where.put("storageType", storageType);
		where.put("targetType", TargetType.secondGold);
		where.put("targetId", secondGoldId);
		return getInventory(where, user);
	}

	public Map<String, Object> findMaterialInventory(int materialId, int orgId, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId", orgId);
		where.put("storageType", StorageType.material);
		where.put("targetType", TargetType.material);
		where.put("targetId", materialId);
		return getInventory(where, user);
	}

	public int createRawmaterialInventory(int rawmaterialId, int orgId, StorageType storageType, String remark, IUser user) throws BusinessException {
		Map<String, Object> inventory = new HashMap<String, Object>();
		inventory.put("targetType", TargetType.rawmaterial);
		inventory.put("targetId", rawmaterialId);
		return createInventory(inventory, orgId, new BigDecimal(0), storageType, orgId, remark, user);
	}

	public int createSecondGoldInventory(int secondGoldId, int orgId, StorageType storageType, String remark, IUser user) throws BusinessException {
		Map<String, Object> inventory = new HashMap<String, Object>();
		inventory.put("targetType", TargetType.secondGold);
		inventory.put("targetId", secondGoldId);
		return createInventory(inventory, orgId, new BigDecimal(0), storageType, orgId, remark, user);
	}

	public int createMaterialInventory(int materialId, int orgId, String remark, IUser user) throws BusinessException {
		Map<String, Object> inventory = new HashMap<String, Object>();
		inventory.put("targetType", TargetType.material);
		inventory.put("targetId", materialId);
		return createInventory(inventory, orgId, new BigDecimal(0), StorageType.material, orgId, remark, user);
	}

	private Map<String, Object> getInventory(Map<String, Object> where, IUser user) throws BusinessException {
		EntityMetadata metadata = getEntityMetadataOfInventory();
		List<Map<String, Object>> dbInventoryList = entityService.list(metadata, where, null, user.asSystem());
		if (CollectionUtils.isEmpty(dbInventoryList)) {
			return null;
		} else if (dbInventoryList.size() > 1) {
			throw new BusinessException("同一仓库中找到ID相同的多件货品");
		}

        return dbInventoryList.get(0);
	}

	public int createInventory(Map<String, Object> inventory, int orgId,
			BigDecimal quantity, StorageType storageType, int sourceOrgId, String remark,
			IUser user) throws BusinessException {
		inventory.put("orgId", orgId);
		inventory.put("storageType", storageType);
		inventory.put("quantity", quantity);
		inventory.put("ownerId", user.getId());
		inventory.put("remark", remark);
		inventory.put("status", InventoryStatus.onStorage);
		inventory.put("sourceOrgId", sourceOrgId);
		inventory.put("cuserId", user.getId());
		inventory.put("cdate", null);

		EntityMetadata metadata = getEntityMetadataOfInventory();
		String id = entityService.create(metadata, inventory, user);
		return Integer.parseInt(id);
	}

    /**
     * 创建出入库记录
     *
     * @param bizType       业务类型
     * @param orgId         部门ID
     * @param quantity      数量
     * @param type          出入库类型
     * @param storageType   库存类型
     * @param targetType    对象类型
     * @param targetId      对象ID
     * @param deliveryReason 出入库原因
     * @param remark        备注
     * @param user          操作员
     * @throws BusinessException
     */
	public void createFlow(BizType bizType, int orgId,
			BigDecimal quantity, InventoryType type, StorageType storageType,
			TargetType targetType, String targetId,
			String deliveryReason, String remark, IUser user) throws BusinessException {
		if (quantity == null || quantity.floatValue() == 0) {
			return;
		}

		Map<String, Object> flow = new HashMap<String, Object>();

		flow.put("bizType", bizType);
		flow.put("orgId", orgId);
		flow.put("storageType", storageType);
		flow.put("type", type);
		flow.put("targetType", targetType);
		flow.put("targetId", targetId);
		flow.put("deliveryReason", deliveryReason);
		flow.put("remark", remark);
		flow.put("quantity", quantity);
		flow.put("cuserId", user.getId());
		flow.put("cdate", null);

        if(targetType == TargetType.product){//商品出入库
            Map<String,Object> target = entityService.getById(MzfEntity.PRODUCT,targetId,user);
            flow.put("cost",MapUtils.getFloat(target,"costPrice"));
            flow.put("price",MapUtils.getFloat(target,"retailBasePrice"));
            flow.put("wholesalePrice",MapUtils.getFloat(target,"wholesalePrice"));
            //发生金额为商品成本
        }else if(targetType == TargetType.rawmaterial){//原料出入库
            //发生金额为原料成本
            if(storageType == StorageType.rawmaterial_gold || storageType == StorageType.rawmaterial_gravel){
                //金料的发生金额为本次入库的成本，参见：createFlowOnQuantity
                flow.put("cost",Float.valueOf(deliveryReason));
            }else{
                Map<String,Object> target = entityService.getById(MzfEntity.RAWMATERIAL,targetId,user);
                flow.put("cost",MapUtils.getFloat(target,"cost"));
            }
        }else if(targetType == TargetType.secondProduct){//旧饰出入库
            //发生金额为旧饰回收价格
            Map<String,Object> target = entityService.getById(MzfEntity.SECOND_PRODUCT,targetId,user);
            flow.put("cost",MapUtils.getFloat(target,"buyPrice"));
        }else if(targetType == TargetType.secondGold){//旧金出入库
            //发生金额为旧金回收价格*数量
        }else{
            //TODO:其他类型的发生金额
        }
        entityService.create(MzfEntity.INVENTORY_FLOW,flow,user);
//		EntityMetadata metadata = getEntityMetadataOfFlow();
//		String id = entityService.create(metadata, flow, user);
//		return Integer.parseInt(id);
	}

	public void addLockedQuantity(int inventoryId, BigDecimal lockedQuantity, IUser user) throws BusinessException {
		EntityMetadata metadata = getEntityMetadataOfInventory();
		Map<String, Object> dbInventory = entityService.getById(metadata, inventoryId, user.asSystem());

		BigDecimal q = new BigDecimal(MapUtils.getString(dbInventory, "quantity"));
		BigDecimal lq = new BigDecimal(MapUtils.getString(dbInventory, "lockedQuantity", "0"));
		lq = lq.add(lockedQuantity);
		if (lockedQuantity.doubleValue() > q.doubleValue()) {
			throw new BusinessException("库存量不足");
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("lockedQuantity", lq);
		entityService.updateById(metadata, Integer.toString(inventoryId), field, user);

		//TODO 记录XXX锁定多少
	}

//	public void warehouseByQuantity(int inventoryId, BigDecimal quantity, IUser user) throws BusinessException {
//		EntityMetadata metadata = getEntityMetadataOfInventory();
//		Map<String, Object> dbInventory = entityService.getById(metadata, inventoryId, user.asSystem());
//
//		BigDecimal dbQuantity = new BigDecimal(MapUtils.getString(dbInventory, "quantity"));
//		dbQuantity = dbQuantity.add(quantity);
//
//		Map<String, Object> field = new HashMap<String, Object>();
//		field.put("quantity", dbQuantity);
//		entityService.updateById(metadata, Integer.toString(inventoryId), field, user);
//
//		//TODO 记录入库流水
//	}


	public void addQuantity(BizType bizType, int inventoryId, BigDecimal quantity, BigDecimal cost, String costDesc, String remark, IUser user) throws BusinessException {
		if (quantity == null || quantity.doubleValue() == 0) return;

		EntityMetadata metadata = getEntityMetadataOfInventory();
		Map<String, Object> inventory = entityService.getById(metadata, inventoryId, user.asSystem());

		BigDecimal dbQuantity = new BigDecimal(MapUtils.getString(inventory, "quantity"));
		dbQuantity = dbQuantity.add(quantity);
		if (dbQuantity.doubleValue() < 0) {
			throw new BusinessException("库存不足");
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("quantity", dbQuantity);
		field.put("lastQuantity", quantity);
		entityService.updateById(metadata, Integer.toString(inventoryId), field, user);

		InventoryType type = null;
		if (quantity.doubleValue() > 0) {
			type = InventoryType.warehouse;
		} else {
			type = InventoryType.delivery;
		}
//		inventory.remove(metadata.getPkCode());

        if(cost == null){
            cost = new BigDecimal(0);
        }
		createFlowOnQuantity(bizType, inventoryId, quantity, type, cost.doubleValue(), costDesc, remark, user);
	}

	public void deliveryByQuantityIgnoreFlow(BizType bizType, int inventoryId, BigDecimal quantity, BigDecimal cost, String costDesc, boolean isUpdateLockedQuantity, String remark, IUser user) throws BusinessException {
		deliveryByQuantity(bizType, inventoryId, quantity, cost, costDesc, isUpdateLockedQuantity, remark, false, user);
	}

	public void deliveryByQuantityFlow(BizType bizType, int inventoryId, BigDecimal quantity, BigDecimal cost, String costDesc, boolean isUpdateLockedQuantity, String remark, IUser user) throws BusinessException {
		deliveryByQuantity(bizType, inventoryId, quantity, cost, costDesc, isUpdateLockedQuantity, remark, true, user);
	}

	private void deliveryByQuantity(BizType bizType, int inventoryId, BigDecimal quantity, BigDecimal cost, String costDesc, boolean isUpdateLockedQuantity, String remark, boolean isFlow, IUser user) throws BusinessException {
		if (quantity == null || quantity.doubleValue() == 0) return;

		EntityMetadata metadata = getEntityMetadataOfInventory();
		Map<String, Object> inventory = entityService.getById(metadata, inventoryId, user.asSystem());

		BigDecimal q = new BigDecimal(MapUtils.getString(inventory, "quantity"));
		Map<String, Object> field = new HashMap<String, Object>();

		q = q.subtract(quantity);
		if (q.doubleValue() >= 0) {
			field.put("quantity", q);
		} else {
			throw new BusinessException("库存量不足");
		}
		if (isUpdateLockedQuantity) {
			BigDecimal lq = new BigDecimal(MapUtils.getString(inventory, "lockedQuantity", Integer.toString(0)));
			lq = lq.subtract(quantity);
			if (lq.doubleValue() >= 0) {
				field.put("lockedQuantity", lq);
			} else {
				field.put("lockedQuantity", 0);
//				throw new BusinessException("库存锁定量不足");
			}
		}
		entityService.updateById(metadata, Integer.toString(inventoryId), field, user);

		//记录出库流水
		if (isFlow) {
			createFlowOnQuantity(bizType, inventoryId, quantity, InventoryType.delivery, cost.doubleValue(), costDesc, remark, user);
		}
	}

	public void createFlowOnQuantity(BizType bizType, int inventoryId, BigDecimal quantity, InventoryType type, Double cost, String costDesc, String remark, IUser user) throws BusinessException {
		EntityMetadata metadata = getEntityMetadataOfInventory();
		Map<String, Object> inventory = entityService.getById(metadata, inventoryId, user.asSystem());
		Integer orgId = MapUtils.getInteger(inventory, "orgId");
		StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
		TargetType targetType = TargetType.valueOf(MapUtils.getString(inventory, "targetType"));
		String targetId = MapUtils.getString(inventory, "targetId");

//		remark = remark == null? "":remark;
//		costDesc = costDesc == null? "":costDesc;

//		if (StringUtils.isBlank(remark)) {
//			remark = costDesc;
//		} else {
//			remark += "；" + costDesc;
//		}
//		remark += " 总成本：" + cost;
		createFlow(bizType, orgId, quantity, type, storageType, targetType, targetId, ObjectUtils.toString(cost), remark, user);
	}

	public void updateStatus(Integer[] inventoryId, InventoryStatus priorStatus, InventoryStatus nextStatus, String message, String remark, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(inventoryId)) return;

		EntityMetadata metadata = getEntityMetadataOfInventory();
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), inventoryId);
		List<Map<String, Object>> dbInventoryList = entityService.list(metadata, where, null, user.asSystem());
		for (Map<String, Object> dbInventory : dbInventoryList) {
			InventoryStatus dbStatus = InventoryStatus.valueOf(MapUtils.getString(dbInventory, "status"));
			if (priorStatus != dbStatus) {
				String s = "当前状态不允许此操作";
				if (StringUtils.isNotBlank(message)) {
					s = message;
				}
				throw new BusinessException(s);
			}
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", nextStatus);
		field.put("remark", remark);
		field.put("muserId", null);
		field.put("muserName", null);
		field.put("mdate", null);
		int row = entityService.update(metadata, field, where, user);
		if (row != inventoryId.length) {
			throw new BusinessException("更新库存状态失败。原因：未找到相应库存[" + Arrays.toString(inventoryId) + "]");
		}
	}

	public void updateOwnerId(int inventoryId, int ownerId, IUser user) throws BusinessException {
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("ownerId", ownerId);

		int rowNum = entityService.updateById(getEntityMetadataOfInventory(), Integer.toString(inventoryId), field, user);
	}

	public int getInventoryId(int targetId, TargetType type, int orgId) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId", orgId);
		where.put("targetType", type);
		where.put("targetId", targetId);
		EntityMetadata metadata = getEntityMetadataOfInventory();
		List<Map<String, Object>> list = entityService.list(metadata, where, null, User.getSystemUser());
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("未找到库存记录");
		} else if (list.size() > 1) {
			throw new BusinessException("找到多条库存记录");
		} else {
			return MapUtils.getInteger(list.get(0), "id");
		}
	}

	public EntityMetadata getEntityMetadataOfInventory() throws BusinessException {
		return metadataProvider.getEntityMetadata(MzfEntity.INVENTORY);
	}

//	public EntityMetadata getEntityMetadataOfFlow() throws BusinessException {
//		return metadataProvider.getEntityMetadata(MzfEntity.INVENTORY_FLOW);
//	}
}


