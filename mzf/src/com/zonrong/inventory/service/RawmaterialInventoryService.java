package com.zonrong.inventory.service;

import com.zonrong.basics.rawmaterial.service.RawmaterialService;
import com.zonrong.basics.rawmaterial.service.RawmaterialService.RawmaterialType;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.DosingBom;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.inventory.service.InventoryService.InventoryType;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.BizCodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 * date: 2010-11-3
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class RawmaterialInventoryService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private InventoryService inventoryService;
	@Resource
	private RawmaterialService rawmaterialService;
	@Resource
	private BusinessLogService businessLogService;
    @Resource
    private FlowLogService flowLogService;
    @Resource
    private TransactionService transactionService;

	public enum GoldClass {
		pt900,			//铂900
		pt950,			//铂950
		k750,			//金750
		pd950,			//钯
		silver,			//银
		no,             //无
		gold;			//黄金

		public String getText() {
			return BizCodeService.getBizName("goldClass", this.toString());
		}
	}

	public int warehouseDiamond(BizType bizType, int rawmaterialId, int sourceOrgId, String remark, IUser user) throws BusinessException {
		StorageType storageType = StorageType.rawmaterial_nakedDiamond;

		//入库
		Map<String, Object> inventory = new HashMap<String, Object>();
		inventory.put("targetType", TargetType.rawmaterial);
		inventory.put("targetId", rawmaterialId);

		int id = inventoryService.createInventory(inventory, user.getOrgId(), new BigDecimal(1), storageType, sourceOrgId, remark, user);

		inventoryService.createFlow(bizType, user.getOrgId(),
				new BigDecimal(1), InventoryType.warehouse, storageType,
				TargetType.rawmaterial, Integer.toString(rawmaterialId), null,
				remark, user);

		return id;
	}

	public void warehouseGold(BizType bizType, int rawmaterialId,
			BigDecimal quantity, BigDecimal cost, String remark, IUser user)
			throws BusinessException {
		StorageType storageType = StorageType.rawmaterial_gold;
		warehouseByQuantity(bizType, storageType, rawmaterialId, quantity, cost, remark, user);
	}

	public void warehouseSecondGold(BizType bizType, int rawmaterialId,
			BigDecimal quantity, BigDecimal cost, String remark, IUser user)
			throws BusinessException {
		StorageType storageType = StorageType.second_secondGold;
		warehouseSecondGoldByQuantity(bizType, storageType, rawmaterialId, quantity, cost, remark, user);
	}

	public void warehouseGravel(BizType bizType, int rawmaterialId,
			BigDecimal quantity, BigDecimal cost, BigDecimal weight, String remark, IUser user)
			throws BusinessException {
		StorageType storageType = StorageType.rawmaterial_gravel;
		warehouseByQuantity(bizType, storageType, rawmaterialId, quantity, cost, remark, user);

		//更新碎石重量
		rawmaterialService.addWeight(rawmaterialId, weight, user);
	}

	public void warehouseParts(BizType bizType, int rawmaterialId,
			BigDecimal quantity, BigDecimal cost, String remark, IUser user)
			throws BusinessException {
		StorageType storageType = StorageType.rawmaterial_parts;
		warehouseByQuantity(bizType, storageType, rawmaterialId, quantity, cost, remark, user);
	}

	private void warehouseByQuantity(BizType bizType, StorageType storageType,
			int rawmaterialId, BigDecimal quantity, BigDecimal cost, String remark, IUser user)
			throws BusinessException {
		if (storageType == null) {
			throw new BusinessException("未指定仓库类型");
		}
		Map<String, Object> inventory = inventoryService.findRawmaterialInventory(rawmaterialId, user.getOrgId(), storageType, user);
		Integer inventoryId = null;
		if (inventory == null) {
			inventoryId = inventoryService.createRawmaterialInventory(rawmaterialId, user.getOrgId(), storageType, "原料采购收货入库", user);
		} else {
			inventoryId = MapUtils.getInteger(inventory, "id");
		}

		inventoryService.addQuantity(bizType, inventoryId, quantity, cost, "成本", remark, user);

		rawmaterialService.addCost(rawmaterialId, cost, user);
	}

	private void warehouseSecondGoldByQuantity(BizType bizType, StorageType storageType,
			int rawmaterialId, BigDecimal quantity, BigDecimal cost, String remark, IUser user)
			throws BusinessException {
		if (storageType == null) {
			throw new BusinessException("未指定仓库类型");
		}

		Map<String, Object> inventory = inventoryService.findSecondGoldInventory(rawmaterialId, user.getOrgId(), storageType, user);
		Integer inventoryId = null;
		if (inventory == null) {
			inventoryId = inventoryService.createSecondGoldInventory(rawmaterialId, user.getOrgId(), storageType, null, user);
		} else {
			inventoryId = MapUtils.getInteger(inventory, "id");
		}

		inventoryService.addQuantity(bizType, inventoryId, quantity, cost, null, remark, user);

		rawmaterialService.addCost(rawmaterialId, cost, user);
	}

	/**
	 * 裸石强制出库
	 * @param bizType
	 * @param rawmaterialId
	 * @param remark
	 * @param user
	 * @throws BusinessException
	 */
	public void deliveryDiamondByRawmaterialId(BizType bizType, int rawmaterialId, String remark, IUser user) throws BusinessException {
		StorageType storageType = StorageType.rawmaterial_nakedDiamond;
		Map<String, Object> dbInventory = inventoryService.findRawmaterialInventory(rawmaterialId, user.getOrgId(), storageType, user);
		Integer inventoryId = MapUtils.getInteger(dbInventory, "id");

		EntityMetadata metadata = inventoryService.getEntityMetadataOfInventory();
		int row = entityService.deleteById(metadata, Integer.toString(inventoryId), user);
		if (row == 0) {
			throw new BusinessException("原料(钻石)[" + inventoryId + "]库存出库失败");
		}

		inventoryService.createFlow(bizType, user.getOrgId(),
				new BigDecimal(1), InventoryType.delivery, storageType,
				TargetType.rawmaterial, Integer.toString(rawmaterialId), null,
				remark, user);
	}

	/**
	 * 提交委外订单时配料出库
	 *
	 * @param bizType
	 * @param rawmaterialQuantityMap	key:rawmaterialId，value:DosingBom
	 * @param orgId
	 * @param user
	 * @throws BusinessException
	 */
	public void deliveryByQuantityOnOEM(BizType bizType, Map<Integer, DosingBom> rawmaterialQuantityMap, int orgId, String remark, IUser user) throws BusinessException {
		Integer[] rawmaterialIds = rawmaterialQuantityMap.keySet().toArray(new Integer[]{});
		List<Map<String, Object>> list = listRawmaterialInventory(rawmaterialIds, orgId, user);
		for (Map<String, Object> map : list) {
			Integer rawmaterialId = MapUtils.getInteger(map, "id");
			String num = MapUtils.getString(map, "num");
			Double dbCost = MapUtils.getDouble(map, "cost");
			Double dbQuantity = MapUtils.getDouble(map, "quantity");
			Double dbLockedQuantity = MapUtils.getDouble(map, "lockedQuantity");
			if (dbCost == null || dbCost < 0) {
				throw new BusinessException("原料[" + num + "]价格为空或低于0");
			}
			if (dbQuantity == null || dbQuantity < 0) {
				throw new BusinessException("原料[" + num + "]数量为空或低于0");
			}
			Double allQuantity = dbQuantity;
			if (dbLockedQuantity != null) {
				allQuantity = allQuantity.doubleValue() + dbLockedQuantity.doubleValue();
			}


			//加权平均，更新原料价格
			DosingBom dosingBom = rawmaterialQuantityMap.get(rawmaterialId);
			BigDecimal quantity = dosingBom.getQuantity();
			BigDecimal outCost = MzfUtils.getAvgByWeighted(allQuantity, dbCost, quantity);
			outCost = outCost.multiply(new BigDecimal(-1));
			rawmaterialService.addCost(rawmaterialId, outCost, user);

			RawmaterialType type = RawmaterialType.valueOf(MapUtils.getString(map, "type"));
			if (type == RawmaterialType.gravel) {
				BigDecimal weight = dosingBom.getWeight();
				if (weight == null) {
					throw new BusinessException("未指定碎石出库重量");
				}
				weight = weight.multiply(new BigDecimal(-1));
				rawmaterialService.addWeight(rawmaterialId, weight, user);
			}

			Integer inventoryId = MapUtils.getInteger(map, "inventoryId");
			inventoryService.deliveryByQuantityFlow(bizType, inventoryId, quantity, null, "成本", true, remark, user);
		}

		//将原料成本记在商品上（待定）
	}

	 /**
	  * 原料强制出库（非裸石）
	  * @param bizType
	  * @param quantity
	  * @param rawmaterialId
	  * @param remark
	  * @param user
	  * @throws BusinessException
	  */
	public void deliveryRawmaterialById(BizType bizType,BigDecimal quantity,BigDecimal weight, int rawmaterialId, String remark, IUser user) throws BusinessException {
		List<Map<String, Object>> list = listRawmaterialInventory(new Integer[] { rawmaterialId }, 1, user);
		Map<String, Object> map = new HashMap<String, Object>();
		if (list != null && list.size() == 1) {
			map = list.get(0);
		} else {
			throw new BusinessException("原料数据有误，不能出库");
		}

		Integer id = MapUtils.getInteger(map, "id");
		String num = MapUtils.getString(map, "num");
		Double dbCost = MapUtils.getDouble(map, "cost");
		Double dbQuantity = MapUtils.getDouble(map, "quantity");
		Double dbLockedQuantity = MapUtils.getDouble(map, "lockedQuantity");
		if (dbCost == null || dbCost < 0) {
			throw new BusinessException("原料[" + num + "]价格为空或低于0");
		}
		if (dbQuantity == null || dbQuantity < 0) {
			throw new BusinessException("原料[" + num + "]数量为空或低于0");
		}
		Double allQuantity = dbQuantity;
		if (dbLockedQuantity != null) {
			allQuantity = allQuantity.doubleValue()
					+ dbLockedQuantity.doubleValue();
		}

		// 加权平均，更新原料价格
		BigDecimal outCost = MzfUtils.getAvgByWeighted(allQuantity, dbCost,quantity);
		outCost = outCost.multiply(new BigDecimal(-1));
		rawmaterialService.addCost(id, outCost, user);
		RawmaterialType type = RawmaterialType.valueOf(MapUtils.getString(map,"type"));
		if (type == RawmaterialType.gravel) {

			if (weight == null) {
				throw new BusinessException("未指定碎石出库重量");
			}
			weight = weight.multiply(new BigDecimal(-1));
			rawmaterialService.addWeight(id, weight, user);
		}

		Integer inventoryId = MapUtils.getInteger(map, "inventoryId");
		inventoryService.deliveryByQuantityFlow(bizType, inventoryId, quantity,null, "强制出库", true, remark, user);
		//记录操作日志
		businessLogService.log("原料库强制出库", "库存编号为：" + inventoryId, user);
	}

	public void lockDiamond(Integer[] rawmaterialId, String message, String remark, IUser user) throws BusinessException {
		rawmaterialService.lockDiamond(rawmaterialId, remark, user);
	}

	public void freeDiamond(Integer[] rawmaterialId, String message, String remark, IUser user) throws BusinessException {
		rawmaterialService.freeDiamond(rawmaterialId, remark, user);
	}

	public void lockByQuantity(Map<Integer, BigDecimal> rawmaterialIdQuantityMap, String remark, IUser user) throws BusinessException {
		if (MapUtils.isEmpty(rawmaterialIdQuantityMap)) {
			return;
		}

		Map<Integer, BigDecimal> inventoryQuantityMap = convertToInventoryQuantityMap(rawmaterialIdQuantityMap, user);
        for (Entry<Integer, BigDecimal> en : inventoryQuantityMap.entrySet()) {
            int inventoryId = en.getKey();
            BigDecimal lockedQuantity = en.getValue();
            inventoryService.addLockedQuantity(inventoryId, lockedQuantity, user);
        }
	}

	public void freeByQuantity(Map<Integer, BigDecimal> rawmaterialIdQuantityMap, String remark, IUser user) throws BusinessException {
		if (MapUtils.isEmpty(rawmaterialIdQuantityMap)) {
			return;
		}

		Map<Integer, BigDecimal> inventoryIdQuantityMap = convertToInventoryQuantityMap(rawmaterialIdQuantityMap, user);
		for (Iterator<Entry<Integer, BigDecimal>> it = inventoryIdQuantityMap.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, BigDecimal> en = it.next();
			int inventoryId = en.getKey();
			BigDecimal lockedQuantity = en.getValue();
			lockedQuantity = lockedQuantity.multiply(new BigDecimal(-1));
			inventoryService.addLockedQuantity(inventoryId, lockedQuantity, user);
		}
	}

	public List<Map<String, Object>> listRawmaterialInventory(Integer[] rawmaterialIds, int orgId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL_INVENTORY_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", rawmaterialIds);
		where.put("orgId", orgId);
		List<Map<String, Object>> rawmaterialInventoryList = entityService.list(metadata, where, null, user.asSystem());
		return rawmaterialInventoryList;
	}

	public Map<String, Object> getRawmaterialInventory(int rawmaterialId, int orgId, IUser user) throws BusinessException {
		List<Map<String, Object>> rawmaterialInventoryList = listRawmaterialInventory(new Integer[]{rawmaterialId}, orgId, user);
		if (CollectionUtils.isEmpty(rawmaterialInventoryList)) {
			throw new BusinessException("库存中未找到该原料[" + rawmaterialId + "]");
		} else if (rawmaterialInventoryList.size() > 1) {
			throw new BusinessException("库存中找到多件原料[" + rawmaterialId + "]");
		}
		Map<String, Object> dbInventory = rawmaterialInventoryList.get(0);

		return dbInventory;
	}

	/**
	 * 传入的参数中的key值为原料Id，将其对应成库存Id
	 * @param rawmaterialIdQuantityMap	key:存放rawmaterialId
	 * @param user
	 * @return		key:存放inventoryId
	 * @throws BusinessException
	 */
	private Map<Integer, BigDecimal> convertToInventoryQuantityMap(Map<Integer, BigDecimal> rawmaterialIdQuantityMap, IUser user) throws BusinessException {
		Integer[] rawmaterialId = new ArrayList<Integer>(rawmaterialIdQuantityMap.keySet()).toArray(new Integer[]{});
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId", user.getOrgId());
		where.put("targetType", TargetType.rawmaterial);
		where.put("targetId", rawmaterialId);
		EntityMetadata metadata = inventoryService.getEntityMetadataOfInventory();
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());
		Map<Integer, BigDecimal> inventoryQuantityMap = new HashMap<Integer, BigDecimal>();
		for (Map<String, Object> dbInventory : list) {
			Integer inventoryId = MapUtils.getInteger(dbInventory, metadata.getPkCode());
			Integer rawmaterialIdTemp = MapUtils.getInteger(dbInventory, "targetId");
			BigDecimal lockedQuantity = rawmaterialIdQuantityMap.get(rawmaterialIdTemp);
			if (inventoryId == null || lockedQuantity == null) {
				throw new BusinessException("获取参数错误");
			}

			inventoryQuantityMap.put(inventoryId, lockedQuantity);
		}

		return inventoryQuantityMap;
	}
}


