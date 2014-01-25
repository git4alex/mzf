package com.zonrong.inventory.service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-11-3
 * <p/>
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

    public int createRawmaterialInventory(int rawmaterialId, int orgId, StorageType storageType, String remark, IUser user) throws BusinessException {
        Map<String, Object> inventory = new HashMap<String, Object>();
        inventory.put("targetType", TargetType.rawmaterial);
        inventory.put("targetId", rawmaterialId);
        return createInventory(inventory, orgId, new BigDecimal(0), storageType, orgId, remark, user);
    }

    public int createMaterialInventory(int materialId, int orgId, String remark, IUser user) throws BusinessException {
        Map<String, Object> inventory = new HashMap<String, Object>();
        inventory.put("targetType", TargetType.material);
        inventory.put("targetId", materialId);
        return createInventory(inventory, orgId, new BigDecimal(0), StorageType.material, orgId, remark, user);
    }

    public int createInventory(Map<String, Object> inventory, int orgId,
                               BigDecimal quantity, StorageType storageType, int sourceOrgId, String remark,
                               IUser user) throws BusinessException {
        inventory.put("orgId", orgId);
        inventory.put("storageType", storageType);
        inventory.put("quantity", quantity);
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
     * @param bizType        业务类型
     * @param orgId          部门ID
     * @param quantity       数量
     * @param type           出入库类型
     * @param storageType    库存类型
     * @param targetType     对象类型
     * @param targetId       对象ID
     * @param remark         备注
     * @param user           操作员
     * @throws BusinessException
     */
    public void createFlow(MzfEnum.BizType bizType, int orgId,
                           BigDecimal quantity, MzfEnum.InventoryType type, StorageType storageType,
                           TargetType targetType, String targetId,
                           Double cost, String remark, IUser user) throws BusinessException {
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
        flow.put("remark", remark);
        flow.put("quantity", quantity);
        flow.put("cuserId", user.getId());
        flow.put("cdate", null);
        if (targetType == TargetType.product) {//商品出入库
            Map<String, Object> target = entityService.getById(MzfEntity.PRODUCT, targetId, user);
            flow.put("cost", MapUtils.getFloat(target, "costPrice"));
            flow.put("price", MapUtils.getFloat(target, "retailBasePrice"));
            flow.put("wholesalePrice", MapUtils.getFloat(target, "wholesalePrice"));
            //发生金额为商品成本
        } else if (targetType == TargetType.rawmaterial) {//原料出入库
            //发生金额为原料成本
            if (storageType == StorageType.rawmaterial_gold || storageType == StorageType.rawmaterial_gravel) {
                //金料的发生金额为本次入库的成本，参见：createFlowOnQuantity
                flow.put("cost", cost);

                if (storageType == StorageType.rawmaterial_gravel) {
                    int idx = remark.lastIndexOf('|');
                    String wStr = remark.substring(idx + 1);
                    remark = remark.substring(0, idx);
                    flow.put("remark", remark);
                    flow.put("weight", new BigDecimal(wStr).round(new MathContext(3)).toString());
                } else {
                    flow.put("weight", quantity);
                }
            } else {
                Map<String, Object> target = entityService.getById(MzfEntity.RAWMATERIAL, targetId, user);
                flow.put("cost", MapUtils.getFloat(target, "cost"));
                if (storageType == StorageType.rawmaterial_nakedDiamond) {
                    flow.put("weight", MapUtils.getFloat(target, "spec"));
                }
            }
        } else if (targetType == TargetType.secondProduct) {//旧饰出入库
            flow.put("cost", cost);
        } else if (targetType == TargetType.secondGold) {//旧金出入库
            flow.put("cost",cost);
        } else {
            //TODO:其他类型的发生金额
        }
        entityService.create(MzfEntity.INVENTORY_FLOW, flow, user);
    }

    /**
     * 锁定库存
     *
     * @param inventoryId 库存记录ID
     * @param lockedQuantity 锁定数量
     *
     * @throws BusinessException
     */
    public void lock(int inventoryId, double lockedQuantity, IUser user) throws BusinessException {
        EntityMetadata metadata = getEntityMetadataOfInventory();
        Map<String, Object> dbInventory = entityService.getById(metadata, inventoryId, user.asSystem());

        double q = MapUtils.getDoubleValue(dbInventory, "quantity", 0);
        double lq = MapUtils.getDoubleValue(dbInventory, "lockedQuantity", 0);
        //锁定时仅增加 锁定数量；出库时要同时减 库存数量 和 锁定数量
        if (lockedQuantity > q-lq) {
            throw new BusinessException("库存量不足");
        }
        lq = lq+lockedQuantity;
        Map<String, Object> field = new HashMap<String, Object>();
        field.put("lockedQuantity", lq);
        entityService.updateById(metadata, Integer.toString(inventoryId), field, user);
    }

    public void lock(int inventoryId,IUser user) throws BusinessException {
        lock(inventoryId,1,user);
    }

    /**
     * 单品入库
     *
     * @param bizType 业务类型
     * @param orgId 部门Id
     * @param targetId 目标ID
     * @param storageType 仓库类型
     * @param cost 发生成本
     * @param costDesc 成本描述
     * @param remark 备注
     */
    public void warehouse(MzfEnum.BizType bizType,int orgId,int targetId,StorageType storageType, BigDecimal cost,String costDesc,String remark,IUser user){

    }

    /**
     * 按数量入库
     *
     * @param bizType 业务类型
     * @param inventoryId 库存记录ID
     * @param quantity 入库数量
     * @param cost 发生金额
     * @param remark 备注
     *
     * @throws BusinessException
     */
    public void warehouse(MzfEnum.BizType bizType, int inventoryId, BigDecimal quantity, BigDecimal cost, String remark, IUser user) throws BusinessException {
        if (quantity == null || quantity.doubleValue() == 0) return;

        EntityMetadata metadata = getEntityMetadataOfInventory();
        Map<String, Object> inventory = entityService.getById(metadata, inventoryId, user.asSystem());

        if (inventory == null) {
            throw new BusinessException("无库存记录，ID:[" + inventoryId + "]");
        }

        //当前库存
        BigDecimal dbQuantity = new BigDecimal(MapUtils.getString(inventory, "quantity"));
        BigDecimal dbCost = new BigDecimal(MapUtils.getFloatValue(inventory, "cost"));
        dbQuantity = dbQuantity.add(quantity);
        dbCost = dbCost.add(cost);

        Map<String, Object> field = new HashMap<String, Object>();
        field.put("quantity", dbQuantity);
        field.put("cost", dbCost);
        field.put("lastQuantity", quantity);
        entityService.updateById(metadata, Integer.toString(inventoryId), field, user);

        if (cost == null) {
            cost = new BigDecimal(0);
        }

        createFlowOnQuantity(bizType, inventoryId, quantity, MzfEnum.InventoryType.warehouse, cost.doubleValue(), remark, user);
    }

    /**
     * 出库
     *
     * @param bizType 业务类型
     * @param inventoryId 库存记录ID
     * @param quantity 出库数量
     * @param cost 发生成本
     * @param remark 备注
     *
     * @throws BusinessException
     */
    public void delivery(MzfEnum.BizType bizType, int inventoryId, BigDecimal quantity, BigDecimal cost, String remark, IUser user) throws BusinessException {
        if (quantity == null || quantity.doubleValue() == 0) return;

        EntityMetadata metadata = getEntityMetadataOfInventory();
        Map<String, Object> inventory = entityService.getById(metadata, inventoryId, user.asSystem());

        BigDecimal dbQuentity = new BigDecimal(MapUtils.getString(inventory, "quantity"));
        Map<String, Object> field = new HashMap<String, Object>();

        BigDecimal newQuentity = dbQuentity.subtract(quantity);
        if (newQuentity.doubleValue() >= 0) {
            field.put("quantity", newQuentity);
        } else {
            logger.debug("出库时库存量不足，库存总数量为："+dbQuentity+",本次数量为："+quantity);
            throw new BusinessException("库存量不足");
        }

        if (cost == null) {
            cost = new BigDecimal(0);
        }

        BigDecimal dbCost = new BigDecimal(MapUtils.getString(inventory, "cost","0"));
        BigDecimal newCost = dbCost.subtract(cost);
        if (newCost.doubleValue() >= 0) {
            field.put("cost", newCost);
        } else {
            logger.debug("出库时发生成本错误，库存总成本为："+dbCost+",本次发生成本为："+cost);
            throw new BusinessException("库存成本错误");
        }

        entityService.updateById(metadata, Integer.toString(inventoryId), field, user);

        //记录出库流水
        createFlowOnQuantity(bizType, inventoryId, quantity, MzfEnum.InventoryType.delivery, cost.doubleValue(), remark, user);
    }

    public void deliveryLocked(MzfEnum.BizType bizType, int inventoryId, BigDecimal quantity, BigDecimal cost, String remark, IUser user) throws BusinessException {
        if (quantity == null || quantity.doubleValue() == 0) return;

        delivery(bizType,inventoryId,quantity,cost,remark,user);

        EntityMetadata metadata = getEntityMetadataOfInventory();
        Map<String, Object> inventory = entityService.getById(metadata, inventoryId, user.asSystem());

        Map<String, Object> field = new HashMap<String, Object>();
        BigDecimal lockedQuantity = new BigDecimal(MapUtils.getString(inventory, "lockedQuantity", Integer.toString(0)));
        lockedQuantity = lockedQuantity.subtract(quantity);
        if (lockedQuantity.doubleValue() >= 0) {
            field.put("lockedQuantity", lockedQuantity);
        } else {
            field.put("lockedQuantity", 0);
            throw new BusinessException("库存锁定量不足");
        }
        entityService.updateById(metadata, Integer.toString(inventoryId), field, user);
    }

    public void createFlowOnQuantity(MzfEnum.BizType bizType, int inventoryId, BigDecimal quantity, MzfEnum.InventoryType type, Double cost, String remark, IUser user) throws BusinessException {
        EntityMetadata metadata = getEntityMetadataOfInventory();
        Map<String, Object> inventory = entityService.getById(metadata, inventoryId, user.asSystem());
        Integer orgId = MapUtils.getInteger(inventory, "orgId");
        StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
        TargetType targetType = TargetType.valueOf(MapUtils.getString(inventory, "targetType"));
        String targetId = MapUtils.getString(inventory, "targetId");

        createFlow(bizType, orgId, quantity, type, storageType, targetType, targetId, cost, remark, user);
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

        entityService.updateById(getEntityMetadataOfInventory(), Integer.toString(inventoryId), field, user);
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
}


