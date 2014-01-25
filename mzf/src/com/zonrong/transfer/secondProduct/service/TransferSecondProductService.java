package com.zonrong.transfer.secondProduct.service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.*;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.SecondProductInventoryService;
import com.zonrong.settlement.service.SettlementService;
import com.zonrong.system.service.OrgService;
import com.zonrong.transfer.common.service.TransferService;
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
public class TransferSecondProductService extends TransferService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private SecondProductInventoryService secondProductInventoryService;
	@Resource
	private SettlementService settlementService;
	@Resource
	private BusinessLogService businessLogService;
    @Resource
    private OrgService orgService;

	protected TransferTargetType getTargetType() {
		return TransferTargetType.secondProduct;
	}

	/**
	 * 调拨出库
	 *
	 * @param productIds
	 * @param transfer
	 * @param user
	 * @throws BusinessException
	 */
	public void transfer(Integer[] productIds, Map<String, Object> transfer, IUser user) throws BusinessException {
		List<Map<String, Object>> inventoryList = secondProductInventoryService.list(productIds, null);
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
		if (targetOrgId != null) {
			checkTransferProductBySelf(targetOrgId, inventoryList, user);
		}
//		Integer suserId = MapUtils.getInteger(transfer, "suserId");

		List<Integer> transferIdList = new ArrayList<Integer>();
		for (int i = 0; i < inventoryList.size(); i++) {
			final Map<String, Object> inventory = inventoryList.get(i);
			final Integer productId = MapUtils.getInteger(inventory, "id");
			TransferStatus status = TransferStatus.waitSend;
			Integer sourceOrgId = MapUtils.getInteger(inventory, "orgId");

			transfer.put("quantity", 1);
			int transferId = createTransfer(getTargetType(), productId, sourceOrgId, targetOrgId, status, transfer, Integer.toString(i + i), null, user);
			if (targetOrgId != null) {
				transferIdList.add(transferId);
			}

//			Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
//			if (suserId != null) {
//				inventoryService.updateOwnerId(inventoryId, suserId, user);
//			}
			//记录操作日志
			businessLogService.log("旧饰库出库", "编号为：" + productId, user);
		}
		//系统自动发货
		if (transferIdList.size() > 0) {
			Integer[] transferIds = transferIdList.toArray(new Integer[]{});
			Map<String, Object> dispatch = new HashMap<String, Object>();
			dispatch.put("targetOrgId", targetOrgId);

			send(transferIds, dispatch, user);
		}
	}

	protected void send(Map<String, Object> transfer, int targetOrgId, IUser user) throws BusinessException{
		Integer productId = MapUtils.getInteger(transfer, "targetId");
		//更新库存状态
        String num = MapUtils.getString(transfer,"num");
        String remark = "调拨单号：["+num+"],发往：["+orgService.getOrgName(targetOrgId)+"]";
		secondProductInventoryService.sendOnPassage(productId, remark, user);
		//记录操作日志
		businessLogService.log("发货(旧饰调拨)", "调拨单号：" + MapUtils.getInteger(transfer, "id"), user);
	}

	public void receive(int transferId, IUser user) throws BusinessException {
		Map<String, Object> transfer = entityService.getById(MzfEntity.TRANSFER_VIEW, transferId, user.asSystem());
		//收货
		this.receive(transfer, new BigDecimal(1), null, user);
		Integer targetId = MapUtils.getInteger(transfer, "targetId");

		//库存收货
		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
        String transNum = MapUtils.getString(transfer,"num");

		secondProductInventoryService.receive(targetId, targetOrgId, sourceOrgId, "调拨单号：["+transNum+"]", user);
		//记录操作日志
		businessLogService.log("收货(旧饰调拨)", "调拨单号：" + MapUtils.getInteger(transfer, "id"), user);
	}

	public void receiveKGold(int transferId, Map<String, Object> receive, IUser user) throws BusinessException {
		Map<String, Object> transfer = entityService.getById(MzfEntity.TRANSFER_VIEW, transferId, user.asSystem());
		String ptype = MapUtils.getString(transfer, "ptype");
		if (StringUtils.isNotBlank(ptype)) {
			if (ProductType.valueOf(ptype) != ProductType.kGold) {
				throw new BusinessException("非旧金，不能收货");
			}
		}

		String price = MapUtils.getString(receive, "settlementPrice");
		if (StringUtils.isBlank(price)) {
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
		this.receive(transfer, actualQuantity, diffRemark, user);

		Integer targetId = MapUtils.getInteger(transfer, "targetId");
		String targetNum = MapUtils.getString(transfer, "targetNum");
		//BigDecimal quantity = new BigDecimal(MapUtils.getString(transfer, "quantity"));
		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
		String num = MapUtils.getString(transfer, "num");
		secondProductInventoryService.receiveKGold(targetId, targetOrgId, actualQuantity, new BigDecimal(price), "旧饰调拨转旧金，原调拨单号【" + num + "】，原旧饰条码【" + targetNum + "】", user);

		//生成结算单
		settlementService.createForTransfer(SettlementType.transferSecondProductToSecondGold, sourceOrgId, targetOrgId, transferId, new BigDecimal(price), null, user);
		//记录操作日志
		businessLogService.log("收货(旧饰调拨)", "调拨单号：" + MapUtils.getInteger(transfer, "id"), user);
	}

	@Override
	public void cancel(Map<String, Object> transfer, IUser user) throws BusinessException {
		super.cancel(transfer, user);

		Integer productId = MapUtils.getInteger(transfer, "targetId");
        String num = MapUtils.getString(transfer,"num");
        String remark="调拨单号：["+num+"]";
        secondProductInventoryService.cancelSend(productId,remark,user);

		//记录操作日志
		businessLogService.log("取消调拨(旧饰调拨)", "调拨单号：" + MapUtils.getInteger(transfer, "id"), user);
	}
}