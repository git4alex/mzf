package com.zonrong.inventory.service;

import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.basics.rawmaterial.service.RawmaterialService;
import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.InventoryStatus;
import com.zonrong.common.utils.MzfEnum.ProductType;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.core.util.Carrier;
import com.zonrong.entity.service.EntityService;
import com.zonrong.common.utils.MzfEnum.BizType;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.showcase.service.ShowcaseCheckService;
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
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ProductInventoryService {
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
	private MzfOrgService mzfOrgService;
	@Resource
	private ShowcaseCheckService showcaseCheckService;
	@Resource
	private RawmaterialService rawmaterialService;
	@Resource
	private RawmaterialInventoryService rawmaterialInventoryService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;
	@Resource
	private OrgService orgService;

	public enum DeliveryTemporaryReason {
		QC,
		CID
	}

    /**
     * 入库
     *
     * @param bizType 业务类型
     * @param productId 商品ID
     * @param targetOrgId 部门ID
     * @param storageType 仓库类型
     * @param sourceOrgId 来源部门ID
     * @param remark 备注
     * @return 库存记录ID
     * @throws BusinessException
     */
	public int warehouse(BizType bizType, int productId, int targetOrgId, StorageType storageType, int sourceOrgId, String remark, IUser user) throws BusinessException {
		if (storageType == null) {
			throw new BusinessException("未指定仓库");
		}

		List<Map<String, Object>> inventorys = listInventoryForProduct(new Integer[]{productId}, null);
		if (CollectionUtils.isNotEmpty(inventorys)) {
			List<String> orgNames = new ArrayList<String>();
			for (Map<String, Object> inventory : inventorys) {
				int orgId = MapUtils.getInteger(inventory, "orgId");
				orgNames.add(orgService.getOrgName(orgId));
			}
			throw new BusinessException("该商品已在" + orgNames + "库存中");
		}

		//入库
		Map<String, Object> inventory = new HashMap<String, Object>();
		inventory.put("targetType", TargetType.product);
		inventory.put("targetId", productId);

		int inventoryId = inventoryService.createInventory(inventory, targetOrgId, new BigDecimal(1), storageType, sourceOrgId, remark, user);

		inventoryService.createFlow(bizType, targetOrgId, new BigDecimal(1), MzfEnum.InventoryType.warehouse,
                storageType, TargetType.product, Integer.toString(productId), null, remark, user);

		boolean isStore = mzfOrgService.isStore(targetOrgId);
		if (isStore) {
			showcaseCheckService.checkInDefault(productId, targetOrgId, remark, user);
		}
		return inventoryId;
	}

	public void sendIgnoreProductStatus(int productId, int targetOrgId,String transferNum, IUser user) throws BusinessException {
		String remark = "调拨单号：["+transferNum+"],收货部门：["+orgService.getOrgName(targetOrgId)+"]";
		Map<String, Object> inventory = getInventoryForProduct(productId, null);
		Integer orgId = MapUtils.getInteger(inventory, "orgId");
		if (orgId != user.getOrgId()) {
			throw new BusinessException("非本部门商品，不允许发货");
		}

		Integer inventoryId = MapUtils.getInteger(inventory, "id");
		inventoryService.updateStatus(new Integer[]{inventoryId}, InventoryStatus.onStorage, InventoryStatus.onPassage, "发货失败", remark, user);

		StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
		inventoryService.createFlow(MzfEnum.BizType.send, orgId, new BigDecimal(1), MzfEnum.InventoryType.delivery,
                storageType, TargetType.product, Integer.toString(productId), null, remark, user);
	}

	public void send(int productId, int targetOrgId,String transferNum, IUser user) throws BusinessException {
		ProductStatus[] status = new ProductStatus[]{ProductStatus.locked};
		productService.check(new Integer[]{productId}, status, new Carrier() {
			@Override
			public void active(Map<String, Object> product) throws BusinessException {
				String productNum = MapUtils.getString(product, "num");
				ProductStatus status = ProductStatus.valueOf(MapUtils.getString(product, "status"));
				throw new BusinessException("商品[" + productNum + "]状态为" + status.getText() + "，不能发货");
			}

		});

		sendIgnoreProductStatus(productId, targetOrgId,transferNum, user);
	}

	public Map<String, Object> getProductInventoryByProductNumOnSale(String productNum, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("num", productNum);
		where.put("inventoryStatus", InventoryStatus.onStorage);
		where.put("storageKind", "product");
		where.put("orgId", user.getOrgId());
		List<Map<String, Object>> list = entityService.list(MzfEntity.PRODUCT_INVENTORY_VIEW, where, null, User.getSystemUser());
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("未找到该商品[" + productNum + "]");
		} else if (list.size() > 1) {
			throw new BusinessException("库存中找到多件该商品[" + productNum + "]");
		}
        Map<String, Object> product = list.get(0);
        String showcaseName = showcaseCheckService.getShowcaseName(MapUtils.getIntValue(product, "id"), user);
        product.put("showcaseName", showcaseName);
		return product;
	}


	public Map<String, Object> getProductInventoryByProductNum(String productNum, int orgId) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("num", productNum);
		where.put("inventoryStatus", InventoryStatus.onStorage);
		where.put("orgId", orgId);
		List<Map<String, Object>> list = entityService.list(MzfEntity.PRODUCT_INVENTORY_VIEW, where, null, User.getSystemUser());
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("未找到该商品[" + productNum + "]");
		} else if (list.size() > 1) {
			throw new BusinessException("库存中找到多件该商品[" + productNum + "]");
		}

		return list.get(0);
	}

	public void receive(int productId, int targetOrgId, StorageType storageType, int sourceOrgId, String remark, boolean isCusNakedDiamond, IUser user) throws BusinessException {
		if (storageType == null) {
			throw new BusinessException("未指定仓库");
		}
		if (targetOrgId != user.getOrgId()) {
			throw new BusinessException("操作员所在部门非调入部门，不允许收货");
		}
//		deliveryByProductId(BizType.receive, productId, remark, InventoryStatus.onPassage, false, user);
        //发货仓库出库，业务类型为调拨出库
        deliveryByProductId(MzfEnum.BizType.send, productId, remark, InventoryStatus.onPassage, false, user);
		if (isCusNakedDiamond) {
			int rawmaterialId = rawmaterialService.createNakedDiamondFromProduct(productId, user);
			rawmaterialInventoryService.warehouseDiamond(MzfEnum.BizType.receive, rawmaterialId, user.getOrgId(), remark, user);
		} else {
            //收货入库
			warehouse(MzfEnum.BizType.receive, productId, targetOrgId, storageType, sourceOrgId, remark, user);
		}
	}

	private void deliveryByProductId(BizType bizType, int productId, String remark, InventoryStatus priorStatus, boolean isFlow, IUser user) throws BusinessException {
		//删除库存
		Map<String, Object> inventory = getInventoryForProduct(productId, null);
		InventoryStatus status = InventoryStatus.valueOf(MapUtils.getString(inventory, "status"));
		if (status != priorStatus) {
			throw new BusinessException("商品库存状态为" + status.getText() + ", 不能进行出库操作");
		}

		Integer inventoryId = MapUtils.getInteger(inventory, "id");
		entityService.deleteById(inventoryService.getEntityMetadataOfInventory(), inventoryId.toString(), user);

		Integer orgId = MapUtils.getInteger(inventory, "orgId");

		//记录库存流水
		if (isFlow) {
			StorageType storageType  = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));
			inventoryService.createFlow(bizType, orgId, new BigDecimal(1), MzfEnum.InventoryType.delivery,
                    storageType, TargetType.product, Integer.toString(productId), null, remark, user);
		}
		remark ="商品强制出库备注：" +remark;
		boolean isStore = mzfOrgService.isStore(orgId);
		if (isStore) {
			showcaseCheckService.checkOut(productId, remark, user);
		}
	}

	public void deliveryByProductId(BizType bizType, int productId, String remark, InventoryStatus priorStatus, IUser user) throws BusinessException {
		deliveryByProductId(bizType, productId, remark, priorStatus, true, user);
		if(bizType.equals(MzfEnum.BizType.delivery)){
			//记录操作日志
			businessLogService.log("商品强制出库(商品库存)", "商品编号：" + productId, user);
		}
	}

	public List<Map<String, Object>> listInventoryForProduct(Integer[] productIds, Integer orgId) throws BusinessException {
		EntityMetadata metadata = inventoryService.getEntityMetadataOfInventory();
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("targetType", TargetType.product);
		where.put("targetId", productIds);
		if (orgId != null) {
			where.put("orgId", orgId);
		}
        return entityService.list(metadata, where, null, User.getSystemUser());
	}

    /**
     * 查询商品库存记录
     *
     * @param productId
     * @param orgId
     * @return
     * @throws BusinessException
     */
	public Map<String, Object> getInventoryForProduct(int productId, Integer orgId) throws BusinessException {
		List<Map<String, Object>> dbInventoryList = listInventoryForProduct(new Integer[]{productId}, orgId);
		if (CollectionUtils.isEmpty(dbInventoryList)) {
			throw new BusinessException("库存中未找到该商品[" + productId + "]");
		} else if (dbInventoryList.size() > 1) {
			throw new BusinessException("库存中找到多件商品[" + productId + "]");
		}

        return dbInventoryList.get(0);
	}

	public List<Map<String, Object>> listProductInventory(Integer[] productIds, Integer orgId) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT_INVENTORY_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		if (!ArrayUtils.isEmpty(productIds)) {
			where.put("id", productIds);
		}
		if (orgId != null) {
			where.put("orgId", orgId);
		}
        return entityService.list(metadata, where, null, User.getSystemUser());
	}
	public List<Map<String, Object>> listProductInventoryByNum(String[] nums, Integer orgId) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT_INVENTORY_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		if (!ArrayUtils.isEmpty(nums)) {
			where.put("num", nums);
		}
		if (orgId != null) {
			where.put("orgId", orgId);
		}
        return entityService.list(metadata, where, null, User.getSystemUser());
	}

	public Map<String, Object> getProductInventory(int productId, Integer orgId) throws BusinessException {
		List<Map<String, Object>> list = listProductInventory(new Integer[]{productId}, orgId);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("库存中未找到该商品[" + productId + "]");
		} else if (list.size() > 1) {
			throw new BusinessException("库存中找到多件商品[" + productId + "]");
		}

        return list.get(0);
	}

	public void transferToTemporary(Integer[] productIds, String remark, IUser user) throws BusinessException {
		List<Map<String, Object>> inventoryList = listInventoryForProduct(productIds, null);

		StorageType target = StorageType.product_temporary;
		String remark1 = "备注：" + (StringUtils.isBlank(remark)? "" : remark);
		remark1 = "从商品库调往临时库。" + remark1;
		List<Integer> inventoryIds = new ArrayList<Integer>();
		for (Map<String, Object> inventory : inventoryList) {
			inventoryIds.add(MapUtils.getInteger(inventory, "id"));
			String productId = MapUtils.getString(inventory, "targetId");
			StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));

			Integer orgId = MapUtils.getInteger(inventory, "orgId");
			inventoryService.createFlow(MzfEnum.BizType.transferToTemporary, orgId, new BigDecimal(1), MzfEnum.InventoryType.delivery, storageType, TargetType.product, productId, null, remark1, user);

			inventoryService.createFlow(MzfEnum.BizType.transferToTemporary, orgId, new BigDecimal(1), MzfEnum.InventoryType.warehouse, target, TargetType.product, productId, null, remark1, user);
		}

		EntityMetadata metadata = inventoryService.getEntityMetadataOfInventory();
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("storageType", target);
		field.put("cdate", null);
		field.put("cuserId", null);

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", inventoryIds.toArray(new Integer[]{}));
		entityService.update(metadata, field, where, user);

		//记录流程
		for (Integer productId : productIds) {
			int transId = transactionService.findTransId(MzfEntity.PRODUCT, Integer.toString(productId), user);
			logService.createLog(transId, MzfEntity.PRODUCT, Integer.toString(productId), "从商品库调往临时库", TargetType.product, Integer.toString(productId), remark1, user);
		}
	}

	public void check(Integer[] productId, ProductStatus status, String message) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", productId);
		List<Map<String, Object>> list = entityService.list(MzfEntity.PRODUCT, where, null, User.getSystemUser());
		for (Map<String, Object> product : list) {
			ProductStatus dbStatus = ProductStatus.valueOf(MapUtils.getString(product, "status"));
			if (status != dbStatus) {
				throw new BusinessException(message);
			}
		}
	}

	public void updateSplit(Integer[] inventoryIds, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(inventoryIds)) {
			throw new BusinessException("未指定要更新的库存记录");
		}

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT_INVENTORY_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("inventoryId", inventoryIds);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user);
		if (list.size() != inventoryIds.length) {
			throw new BusinessException("更新拆旧状态发生异常");
		}

		for (Map<String, Object> inventory : list) {
			Integer id = MapUtils.getInteger(inventory, "inventoryId");
			boolean isSplit = MapUtils.getBooleanValue(inventory, "isSplit");

			Map<String, Object> field = new HashMap<String, Object>();
			field.put("isSplit", Boolean.toString(true));
			if (isSplit) {
				field.put("isSplit", Boolean.toString(false));
			}
			entityService.updateById(MzfEntity.INVENTORY, id.toString(), field, user);
			//记录操作日志
			businessLogService.log("拆旧(商品库存)", null, user);
		}
	}

	public StorageType getDefaultStorageType(int productId) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT);
		Map<String, Object> dbProduct = entityService.getById(metadata, productId, User.getSystemUser());
		ProductType ptype = ProductType.valueOf(MapUtils.getString(dbProduct, "ptype"));
		return ptype.getStorageType();
	}
}


