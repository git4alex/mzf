package com.zonrong.transfer.secondGold.service;

import com.zonrong.basics.rawmaterial.service.RawmaterialService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.SettlementType;
import com.zonrong.common.utils.MzfEnum.TransferStatus;
import com.zonrong.common.utils.MzfEnum.TransferTargetType;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.SecondGoldInventoryService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.settlement.service.SettlementService;
import com.zonrong.system.service.BizCodeService;
import com.zonrong.transfer.common.service.TransferService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-11-22
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class TransferSecondGoldService extends TransferService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private SecondGoldInventoryService secondGoldInventoryService;
	@Resource
	private SettlementService settlementService;
	@Resource
	private BusinessLogService businessLogService;
    @Resource
    private RawmaterialService rawmaterialService;

	protected TransferTargetType getTargetType() {
		return TransferTargetType.secondGold;
	}

	private int createSecondGoldTransfer(int secondGoldId, int sourceOrgId, int targetOrgId, TransferStatus status, Map<String, Object> transfer, IUser user) throws BusinessException {
		Double quantity = MapUtils.getDouble(transfer, "quantity");
		if (quantity == null) {
			throw new BusinessException("未指定调拨数量");
		}
		int transId = transactionService.createTransId();
		//记录操作日志
		businessLogService.log("旧金出库", "旧金编号："+secondGoldId+";源部门编号：" + sourceOrgId + "; 目标部门编号:" + targetOrgId, user);
		return createTransfer(getTargetType(), secondGoldId, sourceOrgId, targetOrgId, status, transfer, null, transId, user);
	}

	/**
	 * 待审核或者待发货是可取消
     *
	 * @throws BusinessException
	 */
	@Override
	public void cancel(Map<String, Object> transfer, IUser user) throws BusinessException {
		super.cancel(transfer, user);

		//解锁原料
		int secondGoldId = MapUtils.getIntValue(transfer, "targetId");
		double lockedQuantity = MapUtils.getDoubleValue(transfer, "quantity");
        int srcOrgId = MapUtils.getIntValue(transfer,"sourceOrgId");
		secondGoldInventoryService.unLock(srcOrgId,secondGoldId, lockedQuantity, user);

		//记录操作日志
		businessLogService.log("取消调拨(旧金调拨)", "调拨单号：" + MapUtils.getInteger(transfer, "id") , user);
	}

	public void transfer(Integer secondGoldId, Map<String, Object> transfer, IUser user) throws BusinessException {
		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
		Integer inventoryId = MapUtils.getInteger(transfer, "inventoryId");
		if (sourceOrgId == null) {
			throw new BusinessException("未指定调出部门");
		}
		if (targetOrgId == null) {
			throw new BusinessException("请选择调入部门");
		}
		if (inventoryId == null) {
			throw new BusinessException("未指定库存ID");
		}

		if (sourceOrgId != user.getOrgId()) {
			throw new BusinessException("非本部门旧金，不能调拨出库");
		}

		int transferId = createSecondGoldTransfer(secondGoldId, sourceOrgId, targetOrgId, TransferStatus.waitSend, transfer, user);

		//系统自动发货
		Map<String, Object> dispatch = new HashMap<String, Object>();
		dispatch.put("targetOrgId", targetOrgId);
		this.send(new Integer[]{transferId}, dispatch, user);
	}

	protected void send(Map<String, Object> transfer,int targetOrgId, IUser user) throws BusinessException{
		Integer secondGoldId = MapUtils.getInteger(transfer, "targetId");
		Double quantity = MapUtils.getDouble(transfer, "quantity", 0d);
		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");

		secondGoldInventoryService.lock(sourceOrgId,secondGoldId,quantity,user);

		//记录操作日志
		businessLogService.log("发货(旧金调拨)", "调拨单号：" + MapUtils.getString(transfer, "num") , user);
	}

	  //旧金收货打印
	 public Map<String,Object> getPrintData(String startDate, String endDate, IUser user)throws BusinessException{

		 EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.TRANSFER_VIEW);
		 Filter filter = Filter.field("convert(varchar(64),rdate,23)").ge(startDate);
		 filter.and(Filter.field("convert(varchar(64),rdate,23)").le(endDate));
		 filter.and(Filter.field("status").eq("over"));
		 filter.and(Filter.field("target_Type").eq("secondGold"));
		 filter.and(Filter.field("target_Org_Id").eq(1));

		 OrderBy orderBy = new OrderBy(new String[]{"targetName","sourceOrgName"},OrderByDir.desc);
		 List<Map<String,Object>> list = entityService.list(metadata, filter, orderBy, user);
		 for (Map<String, Object> map : list) {
			Integer id = MapUtils.getInteger(map, "id", 0);
			Map<String, Object> where = new HashMap<String, Object>();
			where.put("targetId", id);
			map.put("goldClassText", BizCodeService.getBizName("goldClass", MapUtils.getString(map, "targetName","")));
			map.put("settlementPrice", 0);
			List<Map<String, Object> > settlements = entityService.list(MzfEntity.SETTLEMENT, where, null, user);
			if(CollectionUtils.isNotEmpty(settlements)){
				map.put("settlementPrice", MapUtils.getDouble(settlements.get(0), "price"));
			}

		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("startDate", startDate);
		data.put("endDate", endDate);
		data.put("dataList", list);
		return data;
	 }

	public void receive(int transferId, Map<String, Object> receive, IUser user) throws BusinessException {
		Double price = MapUtils.getDouble(receive, "settlementPrice");
		if (price == null) {
			throw new BusinessException("收货时未指定结算价");
		}

		//收货
		BigDecimal actualQuantity = null;
		try {
			actualQuantity = new BigDecimal(MapUtils.getString(receive, "actualQuantity"));
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage());
			}
		}
		if (actualQuantity == null) {
			throw new BusinessException("未指定实际收货重量");
		}

		String diffRemark = MapUtils.getString(receive, "diffRemark");
		Map<String, Object> transfer = entityService.getById(MzfEntity.TRANSFER_VIEW, transferId, user.asSystem());
		super.receive(transfer, actualQuantity, diffRemark, user);

		Integer targetId = MapUtils.getInteger(transfer, "targetId");
		Double quantity = MapUtils.getDouble(transfer, "quantity");
		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
        String transNum = MapUtils.getString(transfer,"num");

        String remark = "调拨单号：["+transNum+"]";
        secondGoldInventoryService.deliveryLocked(MzfEnum.BizType.send,targetId,sourceOrgId,quantity,remark,user);
        secondGoldInventoryService.warehouse(MzfEnum.BizType.receive,rawmaterialService.getGoldClassByTargetId(targetId,user),new BigDecimal(quantity),new BigDecimal(price*quantity),remark,user);

		//生成结算单
		settlementService.createForTransfer(SettlementType.transferSecondGold, sourceOrgId, targetOrgId, transferId, new BigDecimal(price), null, user);
		//记录操作日志
		businessLogService.log("收货(旧金调拨)", "调拨单号：" + transferId, user);
	}
}