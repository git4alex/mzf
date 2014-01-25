package com.zonrong.inventory.service;

import com.zonrong.basics.rawmaterial.service.RawmaterialService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.BizType;
import com.zonrong.common.utils.MzfEnum.GoldClass;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
public class SecondGoldInventoryService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private RawmaterialService rawmaterialService;
	@Resource
	private InventoryService inventoryService;

    //入库
	public void warehouse(BizType bizType, GoldClass goldClass, BigDecimal quantity, BigDecimal cost, String remark, IUser user) throws BusinessException {
		Integer secondGoldId = rawmaterialService.getSecondGoldIdByGoldClass(goldClass, user);
		if (secondGoldId == null) {
			Map<String, Object> rawmaterial = new HashMap<String, Object>();
			rawmaterial.put("type", MzfEnum.RawmaterialType.secondGold);
			rawmaterial.put("goldClass", goldClass);
			rawmaterial.put("quantity", 0);
			rawmaterial.put("cost", 0);
			secondGoldId = rawmaterialService.createRawmaterial(rawmaterial, user);
		}

        //查找当前用户所在部门的旧金库存
        Map<String, Object> inventory = getInventory(secondGoldId, user.getOrgId(), user);
        Integer inventoryId;
        if (inventory == null) {
            inventoryId = createInventory(secondGoldId, user.getOrgId(), user);
        } else {
            inventoryId = MapUtils.getInteger(inventory, "inventoryId");
        }

        inventoryService.warehouse(bizType, inventoryId, quantity, cost, remark, user);
        logger.debug("旧金入库，数量："+quantity.round(new MathContext(2))+"，发生金额："+cost.round(new MathContext(2)));
        //rawmaterialService.addCost(rawmaterialId, cost, user);
	}

    private int createInventory(int secondGoldId, int orgId, IUser user) throws BusinessException {
        Map<String, Object> inventory = new HashMap<String, Object>();
        inventory.put("targetType", MzfEnum.TargetType.secondGold);
        inventory.put("targetId", secondGoldId);
        return inventoryService.createInventory(inventory, orgId, new BigDecimal(0), MzfEnum.StorageType.second_secondGold, orgId, null, user);
    }

    /**
     * 查找指定部门，指定类型的旧金库存记录
     * 同一种旧金在同一个部门只能存在于一个仓库中
     *
     * @param secondGoldId 旧金ID
     * @param orgId        部门ID
     * @return 库存记录
     * @throws BusinessException
     */
    public Map<String, Object> getInventory(int secondGoldId, int orgId, IUser user) throws BusinessException {
        List<Map<String, Object>> inventories = listInventory(new Integer[]{secondGoldId}, orgId, user);
        if(CollectionUtils.isEmpty(inventories)){
            throw new BusinessException("未找到指定的库存记录");
        }else if(inventories.size()>1){
            throw new BusinessException("指定的库存记录重复");
        }

        return inventories.get(0);
    }

    public void lock(int orgId,int targetId,double quantity,IUser user) throws BusinessException {
        Map<String,Object> inv = getInventory(targetId, orgId, user);
        int inventoryId = MapUtils.getIntValue(inv,"inventoryId");
        inventoryService.lock(inventoryId, quantity, user);
    }

    public void unLock(int orgId,int targetId,double quantity,IUser user) throws BusinessException {
        lock(orgId, targetId, quantity * -1, user);
    }

	public List<Map<String, Object>> listInventory(Integer[] secondGoldIds, int orgId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SECOND_GOLD_INVENTORY_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", secondGoldIds);
		where.put("orgId", orgId);
        return entityService.list(metadata, where, null, user.asSystem());
	}

	public void deliveryLocked(BizType bizType, Integer secondGoldId, int orgId, Double quantity, String remark, IUser user) throws BusinessException {
		if (secondGoldId == null) {
			throw new BusinessException("旧金库中无此成色的金料");
		}
		Map<String, Object> inventory =  getInventory(secondGoldId, orgId, user);
		Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
        Double dbQuantity = MapUtils.getDouble(inventory,"quantity");
        Double dbCost = MapUtils.getDouble(inventory,"cost");

        BigDecimal cost = new BigDecimal(dbCost * quantity/dbQuantity);

        inventoryService.deliveryLocked(bizType, inventoryId, new BigDecimal(quantity), cost, remark, user);
	}

    public void delivery(BizType bizType,Integer secondGoldId,int orgId,Double quantity,String remark,IUser user) throws BusinessException {
        if (secondGoldId == null) {
            throw new BusinessException("旧金库中无此成色的金料");
        }
        Map<String, Object> inventory =  getInventory(secondGoldId, orgId, user);
        Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
        Double dbQuantity = MapUtils.getDouble(inventory,"quantity");
        Double dbCost = MapUtils.getDouble(inventory,"cost",0d);

        BigDecimal cost = new BigDecimal(dbCost * quantity/dbQuantity);
        inventoryService.delivery(bizType, inventoryId, new BigDecimal(quantity), cost, remark, user);
    }
}


