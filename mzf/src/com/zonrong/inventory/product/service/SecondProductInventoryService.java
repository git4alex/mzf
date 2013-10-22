package com.zonrong.inventory.product.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.InventoryStatus;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.InventoryService;
import com.zonrong.inventory.service.SecondGoldInventoryService;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.inventory.service.InventoryService.InventoryType;
import com.zonrong.inventory.service.RawmaterialInventoryService.GoldClass;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.OrgService;

/**
 * date: 2010-11-3
 *
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
	
	public int warehouse(BizType bizType, int secondProductId, int targetOrgId, Integer ownerId, int sourceOrgId, String remark, IUser user) throws BusinessException {
		Map<String, Object> secondProcuct = entityService.getById(MzfEntity.SECOND_PRODUCT, secondProductId, User.getSystemUser());
		String num = MapUtils.getString(secondProcuct, "num");
		if (StringUtils.isNotBlank(num)) {			
			List<Map<String, Object>> inventorys = listSecondProductInventoryByNum(new String[]{num}, null);
			if (CollectionUtils.isNotEmpty(inventorys)) {
				List<String> orgNames = new ArrayList<String>();
				for (Map<String, Object> inventory : inventorys) {
					int orgId = MapUtils.getInteger(inventory, "orgId");
					orgNames.add(orgService.getOrgName(orgId));								
				}
				throw new BusinessException("该旧饰[" + num + "]已在" + orgNames + "库存中");
			}
		}
		
		StorageType storageType = StorageType.second_secondProduct; 
		
		//入库
		Map<String, Object> inventory = new HashMap<String, Object>();
		inventory.put("targetType", TargetType.secondProduct);
		inventory.put("targetId", secondProductId);
		inventory.put("ownerId", ownerId);
		int inventoryId = inventoryService.createInventory(inventory, targetOrgId, new BigDecimal(1), storageType, sourceOrgId, remark, user);
		
		inventoryService.createFlow(bizType, targetOrgId, new BigDecimal(1), InventoryType.warehouse, storageType, TargetType.secondProduct, Integer.toString(secondProductId), null, remark, user);
		return inventoryId;
	}
	
	public void send(int productId, int targetOrgId, IUser user) throws BusinessException {		
		String remark = "商品已发往" + orgService.getOrgName(targetOrgId) + ", 等待对方收货";
		Map<String, Object> inventory = getInventoryForSecondProduct(productId, null);
		Integer orgId = MapUtils.getInteger(inventory, "orgId");
		if (orgId.intValue() != user.getOrgId()) {
			throw new BusinessException("非本部门商品，不允许发货");
		}
		
		Integer inventoryId = MapUtils.getInteger(inventory, "id");
		inventoryService.updateStatus(new Integer[]{inventoryId}, InventoryStatus.onStorage, InventoryStatus.onPassage, "发货失败", remark, user);
		
		StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));		
		inventoryService.createFlow(BizType.send, orgId, new BigDecimal(1), InventoryType.delivery, storageType, TargetType.secondProduct, Integer.toString(productId), null, remark, user);
	}
	
	public void receive(int secondProductId, int targetOrgId, int sourceOrgId, String remark, IUser user) throws BusinessException {
		if (targetOrgId != user.getOrgId()) {
			throw new BusinessException("操作员所在部门非调入部门，不允许收货");
		}
		deliveryBySecondProductId(BizType.receive, secondProductId, remark, InventoryStatus.onPassage, false, user);	
		warehouse(BizType.receive, secondProductId, targetOrgId, user.getId(), sourceOrgId, remark, user);
	}	
	
	public void receiveKGold(int secondProductId, int targetOrgId, int sourceOrgId, BigDecimal quantity, BigDecimal cost, String remark, IUser user) throws BusinessException {
		if (targetOrgId != user.getOrgId()) {
			throw new BusinessException("操作员所在部门非调入部门，不允许收货");
		}
		deliveryBySecondProductId(BizType.receive, secondProductId, remark, InventoryStatus.onPassage, false, user);
		secondGoldInventoryService.warehouse(BizType.receive, GoldClass.k750, quantity, cost, remark, user);
	}
	
	private void deliveryBySecondProductId(BizType bizType, int secondProductId, String remark, InventoryStatus priorStatus, boolean isFlow, IUser user) throws BusinessException {				
		//删除库存
		Map<String, Object> inventory = getInventoryForSecondProduct(secondProductId, null);
		InventoryStatus status = InventoryStatus.valueOf(MapUtils.getString(inventory, "status"));
		if (status != priorStatus) {
			throw new BusinessException("商品（旧饰）库存状态为" + status.getText() + ", 不能进行收货操作");
		}		

		Integer inventoryId = MapUtils.getInteger(inventory, "id");
		int row = entityService.deleteById(inventoryService.getEntityMetadataOfInventory(), inventoryId.toString(), user);
		
		//记录库存流水
		if (isFlow) {			
			Integer orgId = MapUtils.getInteger(inventory, "orgId");
			StorageType storageType = StorageType.valueOf(MapUtils.getString(inventory, "storageType"));		
			inventoryService.createFlow(bizType, orgId, new BigDecimal(1), InventoryType.delivery, storageType, TargetType.secondProduct, Integer.toString(secondProductId), null, remark, user);
		}
	}
	
	public void deliveryBySecondProductId(BizType bizType, int secondProductId, String remark, InventoryStatus priorStatus, IUser user) throws BusinessException {				
		deliveryBySecondProductId(bizType, secondProductId, remark, priorStatus, true, user);
	}
	
	public List<Map<String, Object>> listSecondProductInventory(Integer[] productId, Integer orgId) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SECOND_PRODUCT_INVENTORY_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", productId);
		if (orgId != null) {
			where.put("orgId", orgId);
		}
		List<Map<String, Object>> list = entityService.list(metadata, where, null, User.getSystemUser());		
		return list;
	}
	
	public List<Map<String, Object>> listSecondProductInventoryByNum(String[] nums, Integer orgId) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SECOND_PRODUCT_INVENTORY_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		if (!ArrayUtils.isEmpty(nums)) {			
			where.put("num", nums);
		}
		if (orgId != null) {
			where.put("orgId", orgId);
		}
		List<Map<String, Object>> list = entityService.list(metadata, where, null, User.getSystemUser());		
		return list;
	}	
	
	public List<Map<String, Object>> listInventoryForSecondProduct(Integer[] productIds, Integer orgId) throws BusinessException {
		EntityMetadata metadata = inventoryService.getEntityMetadataOfInventory();
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("targetType", TargetType.secondProduct);
		where.put("targetId", productIds);	
		if (orgId != null) {
			where.put("orgId", orgId);	
		}		
		List<Map<String, Object>> dbInventoryList = entityService.list(metadata, where, null, User.getSystemUser());
		return dbInventoryList;
	}
	
	public Map<String, Object> getInventoryForSecondProduct(int productId, Integer orgId) throws BusinessException {
		List<Map<String, Object>> dbInventoryList = listInventoryForSecondProduct(new Integer[]{productId}, orgId);
		if (CollectionUtils.isEmpty(dbInventoryList)) {
			throw new BusinessException("库存中未找到该商品[" + productId + "]（旧饰）");
		} else if (dbInventoryList.size() > 1) {
			throw new BusinessException("库存中找到多件商品[" + productId + "]（旧饰）");
		}
		Map<String, Object> dbInventory = dbInventoryList.get(0);
		
		return dbInventory;
	}	
	

}


