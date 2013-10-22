package com.zonrong.cusorder.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.treasury.service.TreasuryEarnestService;
import com.zonrong.inventory.treasury.service.TreasuryService.BizType;
import com.zonrong.inventory.treasury.service.TreasuryService.MoneyStorageClass1;

/**
 * date: 2010-10-10
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class EarnestFlowService {
	private static Logger logger = Logger.getLogger(EarnestFlowService.class);
	@Resource
	private EntityService entityService;
	@Resource
	private TreasuryEarnestService treasuryEarnestService;

	
	public enum OrderType {
		cusOrder,
		maintain
	}
	
	public int appendEarnest(BizType bizType, OrderType targetType, int targetId, String targetNum, Map<String, Object> earnest, String remark, IUser user) throws BusinessException {
		Map<String, Object> earnestFlow = new HashMap<String, Object>(earnest);
		Boolean isAgent = MapUtils.getBooleanValue(earnestFlow, "isAgent");
		remark = StringUtils.isBlank(remark)? StringUtils.EMPTY:" 备注：" + remark; 
		
		earnestFlow.put("targetType", targetType);
		earnestFlow.put("targetId", targetId);
		earnestFlow.put("isAgent", isAgent.toString());
		earnestFlow.put("remark", bizType.getName() + remark);
		earnestFlow.put("cuserId", null);
		earnestFlow.put("cuserName", null);
		earnestFlow.put("cdate", null);		
		String id = entityService.create(MzfEntity.EARNEST_FLOW, earnestFlow, user);
		Integer earnestFlowId = Integer.parseInt(id);
		
		//定金入库
		remark = bizType.getName() + " 单号:" + targetNum;		
		appendEarnest(bizType, earnestFlowId, user.getOrgId(), remark, user);
		return earnestFlowId;
	}	
	
	private void appendEarnest(BizType bizType, int earnestFlowId, int orgId, String remark, IUser user) throws BusinessException {
		Map<String, Object> earnestFlow = entityService.getById(MzfEntity.EARNEST_FLOW, Integer.toString(earnestFlowId), user);
		
		String payType = MapUtils.getString(earnestFlow, "payType");
		String bank = MapUtils.getString(earnestFlow, "bank");
		BigDecimal money = new BigDecimal(MapUtils.getString(earnestFlow, "amount")); 
		MoneyStorageClass1 class1 = MoneyStorageClass1.valueOf(MoneyStorageClass1.class, payType);
		boolean isAgent = MapUtils.getBooleanValue(earnestFlow, "isAgent");
		
		String targetType = MapUtils.getString(earnestFlow, "targetType");
		IEntityCode entityCode = new EntityCode(targetType);
		int cusOrderId = MapUtils.getInteger(earnestFlow, "targetId");
		
		if (money.doubleValue() > 0) {
			treasuryEarnestService.warehouse(bizType, orgId, money, class1, bank, isAgent, entityCode, cusOrderId, remark, user);			
		} else {
			money = money.abs();
			treasuryEarnestService.delivery(bizType, orgId, money, class1, bank, isAgent, entityCode, cusOrderId, remark, user);
		}
	}
	
	public void deleteEarnestFlow(int id, IUser user) throws BusinessException {
		entityService.deleteById(MzfEntity.EARNEST_FLOW, Integer.toString(id), user);
	}
}


