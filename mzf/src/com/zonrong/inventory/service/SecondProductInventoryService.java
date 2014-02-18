package com.zonrong.inventory.service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.BizType;
import com.zonrong.common.utils.MzfEnum.InventoryStatus;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.OrgService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
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
 * date: 2010-11-3
 * <p/>
 * version: 1.0
 * commonts: ......
 */
@Service
public class SecondProductInventoryService {
    private Logger logger = Logger.getLogger(this.getClass());

    @Resource
    private MetadataProvider metadataProvider;
    @Resource
    private EntityService entityService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private SecondGoldInventoryService secondGoldInventoryService;
    @Resource
    private OrgService orgService;

    public int warehouse(BizType bizType, int secondProductId, Integer targetOrgId, Integer sourceOrgId, String remark, IUser user) throws BusinessException {
        Map<String, Object> secondProcuct = entityService.getById(MzfEntity.SECOND_PRODUCT, secondProductId, User.getSystemUser());
        String num = MapUtils.getString(secondProcuct, "num");
        if (StringUtils.isNotBlank(num)) {
            List<Map<String, Object>> inventorys = listByNums(new String[]{num}, null);
            if (CollectionUtils.isNotEmpty(inventorys)) {
                List<String> orgNames = new ArrayList<String>();
                for (Map<String, Object> inventory : inventorys) {
                    int orgId = MapUtils.getInteger(inventory, "orgId");
                    orgNames.add(orgService.getOrgName(orgId));
                }
                throw new BusinessException("旧饰:[" + num + "]已在" + orgNames + "库存中");
            }
        }

        StorageType storageType = StorageType.second_secondProduct;

        //入库
        Map<String, Object> inventory = new HashMap<String, Object>();
        inventory.put("targetType", TargetType.secondProduct);
        inventory.put("targetId", secondProductId);

        int inventoryId = inventoryService.createInventory(inventory, targetOrgId, new BigDecimal(1), storageType, user);

        inventoryService.createFlow(bizType, targetOrgId, new BigDecimal(1),
                MzfEnum.InventoryType.warehouse, storageType,
                TargetType.secondProduct, Integer.toString(secondProductId),
                MapUtils.getDouble(secondProcuct, "buyPrice"), remark, user);

        return inventoryId;
    }

    public void delivery(BizType bizType, int secondProductId, String remark, InventoryStatus priorStatus, IUser user) throws BusinessException {
        Map<String, Object> inventory = get(secondProductId);
        InventoryStatus status = InventoryStatus.valueOf(MapUtils.getString(inventory, "status"));
        if (status != priorStatus) {
            throw new BusinessException("库存状态为" + status.getText() + ", 不能出库");
        }

        //删除库存
        Integer inventoryId = MapUtils.getInteger(inventory, "id");
        entityService.deleteById(inventoryService.getEntityMetadataOfInventory(), inventoryId.toString(), user);

        Map<String,Object> product = entityService.getById(MzfEntity.SECOND_PRODUCT,secondProductId,user);
        Double cost = MapUtils.getDouble(product, "buyPrice");
        Integer orgId = MapUtils.getInteger(inventory, "orgId");
        StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
        inventoryService.createFlow(bizType, orgId, new BigDecimal(1),
                MzfEnum.InventoryType.delivery, storageType, TargetType.secondProduct,
                Integer.toString(secondProductId), cost, remark, user);
    }

    /**
     * 调拨发货。只是将库存状态置为“在途”
     *
     * @throws BusinessException
     */
    public void sendOnPassage(int productId, String remark, IUser user) throws BusinessException {
        Map<String, Object> inventory = get(productId);
        Integer orgId = MapUtils.getInteger(inventory, "orgId");
        if (orgId != user.getOrgId()) {
            throw new BusinessException("非本部门商品，不允许发货");
        }

        Integer inventoryId = MapUtils.getInteger(inventory, "id");
        inventoryService.updateStatus(new Integer[]{inventoryId}, InventoryStatus.onStorage, InventoryStatus.onPassage, "发货失败", remark, user);

        //记录出库
        //不能在收货时记录，否则无法确定出库时的操作员
        Map<String, Object> product = entityService.getById(MzfEntity.SECOND_PRODUCT, productId, user);
        Double buyPrice = MapUtils.getDouble(product, "buyPrice");
        StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
        inventoryService.createFlow(BizType.send, orgId, new BigDecimal(1),
                MzfEnum.InventoryType.delivery, storageType, TargetType.secondProduct,
                Integer.toString(productId), buyPrice, remark, user);
    }

    public void cancelSend(int productId,String remark,IUser user) throws BusinessException {
        Map<String, Object> inventroy = get(productId);
        Integer inventoryId = MapUtils.getInteger(inventroy, "id");
        Map<String, Object> field = new HashMap<String, Object>();
        field.put("status", InventoryStatus.onStorage);
        entityService.updateById(MzfEntity.INVENTORY, inventoryId.toString(), field, user);

        Map<String,Object> product = entityService.getById(MzfEntity.SECOND_PRODUCT,productId,user);
        Double cost = MapUtils.getDouble(product,"buyPrice");
        inventoryService.createFlow(BizType.send, user.getOrgId(), new BigDecimal(1),
                MzfEnum.InventoryType.delivery, StorageType.second_secondProduct, TargetType.secondProduct,
                Integer.toString(productId), cost, remark, user);
    }

    public void receive(int secondProductId, int targetOrgId, int sourceOrgId, String remark, IUser user) throws BusinessException {
        if (targetOrgId != user.getOrgId()) {
            throw new BusinessException("操作员所在部门非调入部门，不允许收货");
        }

        //删除库存
        Map<String, Object> inventory = get(secondProductId);
        Integer inventoryId = MapUtils.getInteger(inventory, "id");
        entityService.deleteById(inventoryService.getEntityMetadataOfInventory(), inventoryId.toString(), user);

        warehouse(MzfEnum.BizType.receive, secondProductId, targetOrgId, sourceOrgId, remark, user);
    }

    public void receiveKGold(int secondProductId, int targetOrgId, BigDecimal quantity, BigDecimal cost, String remark, IUser user) throws BusinessException {
        if (targetOrgId != user.getOrgId()) {
            throw new BusinessException("操作员所在部门非调入部门，不允许收货");
        }
        delivery(MzfEnum.BizType.receive, secondProductId, remark, InventoryStatus.onPassage, user);
        secondGoldInventoryService.warehouse(MzfEnum.BizType.receive, MzfEnum.GoldClass.k750, quantity, cost, remark, user);
    }

    public List<Map<String, Object>> list(Integer[] productId, Integer orgId) throws BusinessException {
        EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SECOND_PRODUCT_INVENTORY_VIEW);
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("id", productId);
        if (orgId != null) {
            where.put("orgId", orgId);
        }
        return entityService.list(metadata, where, null, User.getSystemUser());
    }

    public List<Map<String, Object>> listByNums(String[] nums, Integer orgId) throws BusinessException {
        EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SECOND_PRODUCT_INVENTORY_VIEW);
        Map<String, Object> where = new HashMap<String, Object>();
        if (!ArrayUtils.isEmpty(nums)) {
            where.put("num", nums);
        }
        if (orgId != null) {
            where.put("orgId", orgId);
        }
        return entityService.list(metadata, where, null, User.getSystemUser());
    }

    public List<Map<String, Object>> listInventory(Integer[] productIds) throws BusinessException {
        EntityMetadata metadata = inventoryService.getEntityMetadataOfInventory();
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("targetType", TargetType.secondProduct);
        where.put("targetId", productIds);
        return entityService.list(metadata, where, null, User.getSystemUser());
    }

    public Map<String, Object> get(int productId) throws BusinessException {
        List<Map<String, Object>> dbInventoryList = listInventory(new Integer[]{productId});
        if (CollectionUtils.isEmpty(dbInventoryList)) {
            throw new BusinessException("库存中未找到该商品[" + productId + "]（旧饰）");
        } else if (dbInventoryList.size() > 1) {
            throw new BusinessException("库存中找到多件商品[" + productId + "]（旧饰）");
        }

        return dbInventoryList.get(0);
    }
}


