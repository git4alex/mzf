package com.zonrong.transfer.common.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.common.service.BillStatusService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.common.utils.MzfEnum.TransferStatus;
import com.zonrong.common.utils.MzfEnum.TransferTargetType;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.common.utils.MzfUtils.BillPrefix;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
 * date: 2010-11-22
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public abstract class TransferService extends BillStatusService<TransferStatus> {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;

	protected abstract TransferTargetType getTargetType();

//	protected abstract void send(int targetId, int targetOrgId, IUser user) throws BusinessException;
	protected abstract void send(Map<String, Object> transfer, int targetOrgId, IUser user) throws BusinessException;

	protected void checkTransferProductBySelf(Integer targetOrgId, List<Map<String, Object>> inventoryList, IUser user)  throws BusinessException {
		if (targetOrgId == null) {
			throw new BusinessException("未指定调入部门");
		}

		for (Map<String, Object> inventory : inventoryList) {
			String productNum = MapUtils.getString(inventory, "num");
			Integer orgId = MapUtils.getInteger(inventory, "orgId");
			if (targetOrgId.equals(orgId)) {
                throw new BusinessException("商品：[" + productNum + "]已在调入部门，不能进行调拨");
			}
			if (orgId != user.getOrgId()) {
                throw new BusinessException("商品：[" + productNum + "]不在调出部门，不能进行调拨");
			}

            //检查是否为借货
            Map<String,Object> filter = new HashMap<String,Object>();
            filter.put("productNum",productNum);
            filter.put("status", MzfEnum.lendStatus.lend);

            List<Map<String,Object>> ret = entityService.list(new EntityCode("productLend"),filter,null,user);
            if(ret.size()>0){//最多只能有一条记录
                Map<String,Object> lend = ret.get(0);
                int srcOrgId = MapUtils.getIntValue(lend,"srcOrgId",-1);
                if(srcOrgId != targetOrgId){
                    throw new BusinessException("商品：[" + productNum + "]为借货，不能进行调拨");
                }
            }
		}
	}

	public int createTransfer(TransferTargetType targetType,
			int targetId, int sourceOrgId, Integer targetOrgId,
			TransferStatus status, Map<String, Object> transfer, String numSufix, Integer transId, IUser user)
			throws BusinessException {
		Map<String, Object> field = new HashMap<String, Object>(transfer);

		Double quantity = MapUtils.getDouble(field, "quantity");
		if (quantity == null) {
			throw new BusinessException("未指定调拨数量");
		}

		String num = MzfUtils.getBillNum(BillPrefix.CK, user);
		if (StringUtils.isNotBlank(numSufix)) {
			num = num + numSufix;
		}
		field.put("num", num);
		field.put("sourceOrgId", sourceOrgId);
		field.put("targetOrgId", targetOrgId);
		field.put("targetType", targetType);
		field.put("targetId", targetId);
		field.put("status", status);
		field.put("cuserId", null);
		field.put("cuserName", null);
		field.put("cdate", null);
		field.remove("id");
		String id = entityService.create(MzfEntity.TRANSFER, field, user);
		Integer transferId = Integer.parseInt(id);

		//记录流程信息
		if (transId == null) {
			transId = transactionService.createTransId();
		}
		logService.createLog(transId, MzfEntity.TRANSFER, transferId.toString(), "新建调拨记录", targetType.getBizTargetType(), targetId, status.getText(), user);

		return transferId;
	}

	public int send(Integer[] transferIds, Map<String, Object> dispatch, IUser user) throws BusinessException{
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.TRANSFER_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), transferIds);
		where.put("targetType", getTargetType());
		List<Map<String, Object>> transferList = entityService.list(metadata, where, null, user.asSystem());
		if (CollectionUtils.isEmpty(transferList)) {
			throw new BusinessException("未指定" + getTargetType().getText());
		}

		Integer targetOrgId = MapUtils.getInteger(dispatch, "targetOrgId");
		List<String> numList = new ArrayList<String>();
		for (Map<String, Object> transfer : transferList) {
			Integer targetOrgId1 = MapUtils.getInteger(transfer, "targetOrgId");
			if (targetOrgId1 != null && targetOrgId1.intValue() != targetOrgId) {
				numList.add(MapUtils.getString(transfer, "num"));
			}
		}
		if (CollectionUtils.isNotEmpty(numList)) {
			throw new BusinessException("出库单" + numList + "的调入部门与指定的调入部门不同");
		}

		Map<String, Object> firstTransfer = transferList.get(0);
		Integer firstSourceOrgId = MapUtils.getInteger(firstTransfer, "sourceOrgId");

		//创建发货单
		int dispatchId = dispatch(transferIds, dispatch, firstSourceOrgId, user);

		for (Map<String, Object> transfer : transferList) {
			Integer transferId = MapUtils.getInteger(transfer, metadata.getPkCode());
			Integer targetId = MapUtils.getInteger(transfer, "targetId");
			String sourceOrgName = MapUtils.getString(transfer, "sourceOrgName");
//			Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");

			send(transfer, targetOrgId, user);
//			send(targetId, targetOrgId, user);

			//修改调拨单状态为待收货
			updateStatus(transferId, new TransferStatus[]{TransferStatus.waitSend}, TransferStatus.waitReceive, new StatusCarrier(){
				@Override
				public void active(Map<String, Object> carrier) throws BusinessException {
					throw new BusinessException("调拨单[" + this.getNum() + "]状态为" + this.getStatus(TransferStatus.class).getText() + "， 不能进行发货操作");
				}
			}, user);

			int transId = transactionService.findTransId(MzfEntity.TRANSFER, Integer.toString(transferId), user);
			logService.createLog(transId, MzfEntity.TRANSFER, Integer.toString(transferId), "发货", getTargetType().getBizTargetType(), targetId, "调出部门：" + sourceOrgName, user);
		}

		return dispatchId;
	}

	private int dispatch(Integer[] transferIds, Map<String, Object> dispatch, Integer sourceOrgId, IUser user)throws BusinessException {
		String num = MzfUtils.getBillNum(BillPrefix.FH, user);
		dispatch.put("num", num);
		dispatch.put("sourceOrgId", sourceOrgId);
		dispatch.put("cuserId", null);
		dispatch.put("cdate", null);
		String dispatchId = entityService.create(MzfEntity.DISPATCH, dispatch, user);

		Integer targetOrgId = MapUtils.getInteger(dispatch, "targetOrgId");
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("dispatchId", dispatchId);
		field.put("targetOrgId", targetOrgId);
		field.put("sdate", null);

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.TRANSFER);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), transferIds);
		where.put("targetType", getTargetType());
		entityService.update(metadata, field, where, user);

		return Integer.parseInt(dispatchId);
	}

	protected int receive(Map<String, Object> transfer, BigDecimal actualQuantity, String diffRemark, IUser user) throws BusinessException {
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
		if (user.getOrgId() != targetOrgId) {
			throw new BusinessException("非调入部门人员不能收货");
		}

		Integer transferId = MapUtils.getInteger(transfer, "id");
		Integer targetId = MapUtils.getInteger(transfer, "targetId");

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("ruserId", user.getId());
		field.put("ruserName", user.getName());
		field.put("rdate", null);
		field.put("diffRemark", diffRemark);
		if (actualQuantity != null) {
			field.put("actualQuantity", actualQuantity);
		}
		entityService.updateById(MzfEntity.TRANSFER, Integer.toString(transferId), field, user);

		updateStatus(transferId, new TransferStatus[]{TransferStatus.waitReceive}, TransferStatus.over, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				throw new BusinessException("调拨单[" + this.getNum() + "]状态为" + this.getStatus(TransferStatus.class).getText() + "， 不能进行收货操作");
			}
		}, user);

		String targetOrgName = MapUtils.getString(transfer, "targetOrgName");
		int transId = transactionService.findTransId(MzfEntity.TRANSFER, Integer.toString(transferId), user);
		logService.createLog(transId, MzfEntity.TRANSFER, Integer.toString(transferId), "收货", getTargetType().getBizTargetType(), targetId, "调入部门：" + targetOrgName, user);

		return transId;
	}

	public void cancelTransfer(Map<String, Object> transfer, IUser user) throws BusinessException {
		int transferId = MapUtils.getInteger(transfer, "id");
		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
		if (sourceOrgId != user.getOrgId()) {
			throw new BusinessException("非本部门调拨单，不允许取消");
		}
		TransferStatus[] priorStatus = new TransferStatus[]{TransferStatus.waitApprove,
				TransferStatus.reject,
				TransferStatus.waitSend,
				TransferStatus.waitReceive};

		//更新调拨申请状态
		updateStatus(transferId, priorStatus, TransferStatus.canceled, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				throw new BusinessException("调拨单[" + this.getNum() + "]状态为" + this.getStatus(TransferStatus.class).getText() + "， 不允许取消操作");
			}
		}, user);


		TargetType targetType = getTargetType().getBizTargetType();
		Integer targetId = MapUtils.getInteger(transfer, "targetId");
		//记录流程信息
		int transId = transactionService.findTransId(MzfEntity.TRANSFER, Integer.toString(transferId), user);
		if (targetType == TargetType.product ||
				targetType == TargetType.maintainProduct ||
				targetType == TargetType.secondProduct) {
			logService.createLog(transId, MzfEntity.TRANSFER, Integer.toString(transferId), "取消调拨", targetType, targetId, null, user);
		} else {
			logService.createLog(transId, MzfEntity.TRANSFER, Integer.toString(transferId), "取消调拨", null, null, null, user);
		}
	}

	public Map<String, Object> getPrintData(int dispatchId) throws BusinessException {
		Map<String, Object> dispatch = entityService.getById(MzfEntity.DISPATCH_VIEW, dispatchId, User.getSystemUser());

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("dispatchId", dispatchId);
		List<Map<String, Object>> list = entityService.list(MzfEntity.TRANSFER_VIEW, where, null, User.getSystemUser());
		for (Map<String, Object> map : list) {
			Map<String, Object> material = entityService.getById(MzfEntity.MATERIAL, MapUtils.getString(map, "targetId","0"), User.getSystemUser());
			double promotionPrice = MapUtils.getDoubleValue(material, "promotionPrice");
			double quantity = MapUtils.getDoubleValue(map, "quantity");
			map.put("promotionPrice", promotionPrice);
			map.put("totalPrice", promotionPrice * quantity);
		}
		dispatch.put("detailList", list);

		return dispatch;
	}

	@Override
	protected EntityMetadata getBillMetadata() throws BusinessException {
		return metadataProvider.getEntityMetadata(MzfEntity.TRANSFER);
	}

	@Override
	protected String getBillName() {
		return "调拨单";
	}

}