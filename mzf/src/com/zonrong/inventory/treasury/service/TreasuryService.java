package com.zonrong.inventory.treasury.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.InventoryService.InventoryType;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.OrgService;

/**
 * date: 2010-11-3
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class TreasuryService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private OrgService orgService;
	
	public enum MoneyStorageClass1 {
		cash("现金"),
		bankCard("银联卡"),
		valueCard("储值卡"),
		coBrandedCard("联名卡"),
		foreignCard("外卡"),
		transfer("转账"),
		chit("代金券"),
		other("其它");
		
		MoneyStorageClass1(String name){
			this.name = name;
		}
		
		private String name;
		
		public String getName() {
			return name;
		}
	}
	
	public enum BizType {
		sell("销售"),
		returns("退货"),
		cusOrder("客户订单"),
		maintain("维修"),
		earnest("定金"),
		appendEarnest("追加定金"),
		refund("退定"),
		summary("日结");
		
		BizType(String name){
			this.name = name;
		}
		
		private String name;
		
		public String getName() {
			return name;
		}
	}
	
	public enum TreasuryType {
		sale("现金"),
		earnest("定金");
		
		TreasuryType(String name){
			this.name = name;
		}
		
		private String name;
		
		public String getName() {
			return name;
		}
	}
	
	public abstract String getStorageName();
	
	public abstract TreasuryType getTreasuryType();
	
	public abstract void setClass2(Map<String, Object> where, MoneyStorageClass1 class1, String class2) throws BusinessException;
	
	public void doSummary(int orgId,  IEntityCode targetCode, int targetId, String remark, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.TREASURY);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId", orgId);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user);
		
		for (Map<String, Object> treasury : list) {
			Integer treasuryId = MapUtils.getInteger(treasury, metadata.getPkCode());
			BigDecimal money = new BigDecimal(MapUtils.getDoubleValue(treasury, "money"));
			boolean isAgent = MapUtils.getBoolean(treasury, "isAgent");
			
			delivery(BizType.summary, treasuryId, money, isAgent, targetCode, targetId, remark, user);
		}
	}
	
	public void warehouse(BizType bizType, int orgId, BigDecimal money, MoneyStorageClass1 class1, String class2, boolean isAgent, IEntityCode targetCode, int targetId, String remark, IUser user) throws BusinessException {
		if (money == null) return;
		
		Integer treasuryId = findTreasury(orgId, class1, class2, isAgent, user);		
		if (treasuryId == null) {
			treasuryId = createDefaultTreasury(class1, class2, isAgent, user);
		}

		addMoney(bizType, treasuryId, money, isAgent, targetCode, targetId, remark, false, user);
	}
	
	public void delivery(BizType bizType, int orgId, BigDecimal money, MoneyStorageClass1 class1, String class2, boolean isAgent, IEntityCode targetCode, int targetId, String remark, IUser user) throws BusinessException {
		if (money == null || money.doubleValue() == 0) return;
		
		Integer treasuryId = findTreasury(orgId, class1, class2, isAgent, user);
		if (treasuryId == null) {
			String orgName = orgService.getOrgName(orgId);
			throw new BusinessException("未找到" + orgName + "下的" + getStorageName() + "中的" + class1.getName() + "[" + class2 + "]账户");
		}
		delivery(bizType, treasuryId, money, isAgent, targetCode, targetId, remark, user);
	}
	
	public void delivery(BizType bizType, int treasuryId, BigDecimal money, boolean isAgent, IEntityCode targetCode, int targetId, String remark, IUser user) throws BusinessException {
		if (money == null || money.doubleValue() == 0) return;
		
		money = money.multiply(new BigDecimal(-1));		
		addMoney(bizType, treasuryId, money, isAgent, targetCode, targetId, remark, false, user);
	}
	
	private void addMoney(BizType bizType, int treasuryId, BigDecimal money, boolean isAgent, IEntityCode targetCode, int targetId, String remark, boolean isValidBalance, IUser user) throws BusinessException {
		if (money.doubleValue() == 0) {
			return;
		}
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.TREASURY);
		Map<String, Object> treasury = entityService.getById(metadata, treasuryId, user.asSystem());
		
		BigDecimal dbMoney = new BigDecimal(MapUtils.getString(treasury, "money"));
		dbMoney = dbMoney.add(money);
		if (isValidBalance) {
			if (dbMoney.doubleValue() < 0) {
				throw new BusinessException(getStorageName() + "余额不足");
			}				
		}
		
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("money", dbMoney);
		int row = entityService.updateById(metadata, Integer.toString(treasuryId), field, user);
		if (row == 0) {
			throw new BusinessException("未找到" + getStorageName() + "对应的库存，操作失败");
		}
		
		createFlow(bizType, treasuryId, money, targetCode, targetId, remark, user);
	}
	
	private int createFlow(BizType bizType, int treasuryId, BigDecimal money, IEntityCode targetCode, int targetId, String remark, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.TREASURY_FLOW);
		
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("treasuryId", treasuryId);
		field.put("bizType", bizType);
		field.put("targetCode", targetCode.getCode());
		field.put("targetId", targetId);
		field.put("money", money);
		field.put("remark", remark);
		field.put("cuserId", null);
		field.put("cuserName", null);
		field.put("cdate", null);
		
		//当 money 是负值时为现金出库出库
		if (money.doubleValue() > 0) {			
			field.put("type", InventoryType.warehouse);
		} else {
			field.put("type", InventoryType.delivery);
		}		
		String id = entityService.create(metadata, field, user);
		return Integer.parseInt(id);
	}	
	
	private int createDefaultTreasury(MoneyStorageClass1 class1, String class2, boolean isAgent, IUser user) throws BusinessException {
		Map<String, Object> inventory = new HashMap<String, Object>();
		inventory.put("type", getTreasuryType());
		inventory.put("orgId", user.getOrgId());
		inventory.put("orgName", user.getOrgName());
		inventory.put("class1", class1);
		inventory.put("class2", class2);
		inventory.put("isAgent", Boolean.toString(isAgent));
		inventory.put("money", 0);

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.TREASURY);
		String id = entityService.create(metadata, inventory, user);
		
		return Integer.parseInt(id);
	}
	
	private Integer findTreasury(int orgId, MoneyStorageClass1 class1, String class2, boolean isAgent, IUser user) throws BusinessException {
		if (class1 == null) {
			throw new BusinessException("未指定" + getStorageName() + "大类");
		}
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("type", getTreasuryType());
		where.put("orgId", orgId);
		where.put("class1", class1);
		where.put("isAgent", Boolean.toString(isAgent));
		setClass2(where, class1, class2);
		
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.TREASURY);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());
		
		if (CollectionUtils.isEmpty(list)) {
			return null;
		} else if (list.size() == 1) {
			return MapUtils.getInteger(list.get(0), metadata.getPkCode());
		} else {
			throw new BusinessException("同一部门发现多个相同的" + getStorageName());
		}		
	}	
}


