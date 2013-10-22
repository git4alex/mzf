package com.zonrong.settlement.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.service.BillStatusService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.SettlementStatus;
import com.zonrong.common.utils.MzfEnum.SettlementType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2011-8-17
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class SettlementService extends BillStatusService<SettlementStatus>{
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private TransactionService transactionService;	
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;
	
	public int createForTransfer(SettlementType type, int receptOrgId, int payOrgId, int transferId, BigDecimal price, String remark, IUser user) throws BusinessException {
		return create(receptOrgId, payOrgId, type, transferId, price, remark, user);
	}
	
	public int createForMaintain(int receptOrgId, int payOrgId, int maintainId, BigDecimal price, String remark, IUser user) throws BusinessException {
		return create(receptOrgId, payOrgId, SettlementType.maintainProduct, maintainId, price, remark, user);
	}
	
	public int createForSplitSecondProduct(int receptOrgId, int payOrgId, int splitId, BigDecimal price, String remark, IUser user) throws BusinessException {
		return create(receptOrgId, payOrgId, SettlementType.splitSecondProduct, splitId, price, remark, user);
	}
	
	public int createForRenvoteSecondProduct(int receptOrgId, int payOrgId, int renvoteId, BigDecimal price, String remark, IUser user) throws BusinessException {
		return create(receptOrgId, payOrgId, SettlementType.renovateSecondProduct, renvoteId, price, remark, user);
	}
	
	public int createForVendorOrder(SettlementType type, int vendorId, int payOrgId, int vendorOrderId, BigDecimal price, String remark, IUser user) throws BusinessException {
		return create(vendorId, payOrgId, type, vendorOrderId, price, remark, user);
	}
	
	private int create(int receptOrgId, int payOrgId, SettlementType type, Integer targetId, BigDecimal price, String remark, IUser user) throws BusinessException {
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("receptOrgId", receptOrgId);
		field.put("payOrgId", payOrgId);
		field.put("type", type);
		field.put("status", SettlementStatus.New);
		field.put("targetId", targetId);
		field.put("price", price);
		field.put("remark", remark);
		field.put("cuserId", null);
		field.put("cdate", null);
		String id = entityService.create(getBillMetadata(), field, user);
		
		int transId = transactionService.createTransId();
		logService.createLog(transId, MzfEntity.SETTLEMENT, id, "新建结算单", null, null, null, user);
		return Integer.parseInt(id);
	} 
	public void settle(Integer[] settlementIds, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(settlementIds)) {
			throw new BusinessException("未指定结算单");
		}
		
		EntityMetadata metadata = getBillMetadata();
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", settlementIds);
		where.put("status", SettlementStatus.New);
		
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", SettlementStatus.over);
		field.put("sdate", null);
		field.put("suserId",null);
		int row = entityService.update(metadata, field, where, user);
		
		if (row != settlementIds.length) {
			throw new BusinessException("请指定未结算的单据");
		}
		
		for (Integer settlementId : settlementIds) {
			int transId = transactionService.createTransId();
			try {				
				transId = transactionService.findTransId(MzfEntity.SETTLEMENT, settlementId.toString(), user);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}
			logService.createLog(transId, MzfEntity.SETTLEMENT, settlementId.toString(), "完成结算", null, null, null, user);
			//记录操作日志
			businessLogService.log("结算","结算单号：" + settlementId, user);
		}
	}

	@Override
	protected EntityMetadata getBillMetadata() throws BusinessException {
		return metadataProvider.getEntityMetadata(MzfEntity.SETTLEMENT);
	}

	@Override
	protected String getBillName() {
		return "结算单";
	}
	

}


