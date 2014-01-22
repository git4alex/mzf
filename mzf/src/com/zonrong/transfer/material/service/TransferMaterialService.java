package com.zonrong.transfer.material.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.MaterialDemandStatus;
import com.zonrong.common.utils.MzfEnum.SettlementType;
import com.zonrong.common.utils.MzfEnum.TransferStatus;
import com.zonrong.common.utils.MzfEnum.TransferTargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.demand.material.service.MaterialDemandService;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.MaterialInventoryService;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.settlement.service.SettlementService;
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
public class TransferMaterialService extends TransferService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private MaterialDemandService materialDemandService;
	@Resource
	private MaterialInventoryService materialInventoryService;
	@Resource
	private SettlementService settlementService;
	@Resource
	private FlowLogService logService;
	@Resource
	private MzfOrgService mzfOrgService;
	@Resource
	private BusinessLogService businessLogService;

	protected TransferTargetType getTargetType() {
		return TransferTargetType.material;
	}

	/**
	 * 待审核或者待发货是可取消
     *
	 * @param transfer 调拨记录
     *
	 * @throws BusinessException
	 */
	@Override
	public void cancelTransfer(Map<String, Object> transfer, IUser user) throws BusinessException {
		super.cancelTransfer(transfer, user);

		//解锁原料
		Integer materialId = MapUtils.getInteger(transfer, "targetId");
		double quantity = MapUtils.getDoubleValue(transfer, "quantity",0);

        Map<String,Object> inv = materialInventoryService.getInventory(materialId,user.getOrgId(),user);
		int inventoryId = MapUtils.getIntValue(inv,"id");

		materialInventoryService.unLock(inventoryId, quantity, user);
		//记录操作日志
		businessLogService.log("取消调拨(物料调拨)", "调拨单号：" + MapUtils.getInteger(transfer, "id"), user);
	}

	public void transfer(List<Map<String, Object>> materialList, Map<String, Object> transfer, IUser user) throws BusinessException {
		List<Map<String, Object>> transferList = new ArrayList<Map<String,Object>>();
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId"); //调入部门编号

		for (Map<String, Object> material : materialList) {
			Integer inventoryId = MapUtils.getInteger(material, "inventoryId");
			Map<String, Object> inventory = entityService.getById(metadataProvider.getEntityMetadata(MzfEntity.INVENTORY), inventoryId.toString(), user);
			Integer materialId = MapUtils.getInteger(inventory, "targetId");
			Integer sourceOrgId = MapUtils.getInteger(inventory, "orgId");  //调出部门编号
			if(targetOrgId !=  null){
				if(!mzfOrgService.isHq(targetOrgId) && !mzfOrgService.isHq(sourceOrgId)){
					throw new BusinessException("门店之间不能进行物料调拨");
				}
			}
			if (sourceOrgId != user.getOrgId()) {
				throw new BusinessException("非本部门物料");
			}

			Map<String, Object> transfer1 = new HashMap<String, Object>(transfer);
			transfer1.putAll(material);
			transfer1.put("sourceOrgId", sourceOrgId);
			transfer1.put("materialId", materialId);
			transferList.add(transfer1);
		}


		List<Integer> transferIdList = new ArrayList<Integer>();
		for (int i = 0; i < transferList.size(); i++) {
			Map<String, Object> transfer1 = transferList.get(i);
			Integer materialId = MapUtils.getInteger(transfer1, "materialId");
			Integer sourceOrgId = MapUtils.getInteger(transfer1, "sourceOrgId");
			int transId = transactionService.createTransId();
			int transferId = createTransfer(getTargetType(), materialId, sourceOrgId, targetOrgId, TransferStatus.waitSend, transfer1, Integer.toString(i + i), transId, user);
			if (targetOrgId != null) {
				transferIdList.add(transferId);
			}

			Integer inventoryId = MapUtils.getInteger(transfer1, "inventoryId");
			double quantity = MapUtils.getDoubleValue(transfer1, "quantity");

			materialInventoryService.lock(inventoryId, quantity, user);
			//记录操作日志
			businessLogService.log("物料库出库", "物料编号：" + materialId, user);
		}

		//系统自动发货
		if (transferIdList.size() > 0) {
			Integer[] transferIds = transferIdList.toArray(new Integer[]{});
			Map<String, Object> dispatch = new HashMap<String, Object>();
			dispatch.put("targetOrgId", targetOrgId);

			this.send(transferIds, dispatch, user);
		}
	}
//	public void transfer(Integer materialId, BigDecimal quantity, Map<String, Object> transfer, IUser user) throws BusinessException {
//		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
//		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
//		if (sourceOrgId == null) {
//			throw new BusinessException("未指定调出部门");
//		}
////		if (targetOrgId == null) {
////			throw new BusinessException("请选择调入部门");
////		}
//
//		if (sourceOrgId != user.getOrgId()) {
//			throw new BusinessException("非本部门物料，不能调拨出库");
//		}
//
//		String remark = MapUtils.getString(transfer, "remark");
//		int transferId = createMaterialTransfer(materialId, sourceOrgId, targetOrgId, TransferStatus.waitSend, transfer, user);
//
//		Map<Integer, BigDecimal> map = new HashMap<Integer, BigDecimal>();
//		map.put(materialId, quantity);
//		materialInventoryService.lockByQuantity(map, remark, user);
//
//		//系统自动发货
//		if (targetOrgId != null) {
//			Integer[] transferIds = new Integer[]{transferId};
//			Map<String, Object> dispatch = new HashMap<String, Object>();
//			dispatch.put("targetOrgId", targetOrgId);
//			this.send(transferIds, dispatch, user);
//		}
//	}

	protected void send(Map<String, Object> transfer, int targetOrgId, IUser user) throws BusinessException{
		Integer materialId = MapUtils.getInteger(transfer, "targetId");
		BigDecimal quantity = new BigDecimal(MapUtils.getFloatValue(transfer, "quantity", 0));
		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");

		//TODO从仓库发货
		materialInventoryService.send(materialId, quantity, sourceOrgId, null, user);
		//记录操作日志
		businessLogService.log("发货(物料调拨)", "调拨单号：" + MapUtils.getInteger(transfer, "id"), user);
	}

	public void receive(int transferId, Map<String, Object> receive, IUser user) throws BusinessException {
		//收货
//		Double actualQuantity = MapUtils.getDouble(receive, "actualQuantity");
//		if (actualQuantity == null) {
//			throw new BusinessException("未指定实际收货数量");
//		}

		Map<String, Object> transfer = entityService.getById(MzfEntity.TRANSFER_VIEW, transferId, user.asSystem());
		String diffRemark = MapUtils.getString(receive, "diffRemark");
		int transId = this.receive(transfer, new BigDecimal(1), diffRemark, user);

		Integer targetId = MapUtils.getInteger(transfer, "targetId");
		BigDecimal quantity = new BigDecimal(MapUtils.getString(transfer, "quantity"));
		BigDecimal cost = new BigDecimal(MapUtils.getString(transfer, "materialWholesalePrice"));
		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
		materialInventoryService.receive(targetId, quantity, cost, sourceOrgId, targetOrgId, "商品调拨", user);

		//更新物料要货申请状态
		Integer demandId = MapUtils.getInteger(transfer, "demandId");
		if (demandId != null) {
			materialDemandService.updateStatus(demandId, MaterialDemandStatus.transfering, MaterialDemandStatus.over,  new StatusCarrier() {
				@Override
				public void active(Map<String, Object> demand) throws BusinessException {
					throw new BusinessException("物料要货申请[" + this.getNum() + "]状态为" + this.getStatus(MaterialDemandStatus.class).getText()+ ", 不能收货");
				}

			}, user);

			logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, demandId.toString(), "物料要货申请完成", getTargetType().getBizTargetType(), targetId, "调拨已收货", user);
		}

		//生成结算单
		String price = MapUtils.getString(transfer, "materialWholesalePrice");
		if (StringUtils.isBlank(price)) {
			throw new BusinessException("物料批发价为空");
		}
		settlementService.createForTransfer(SettlementType.transferMaterial, sourceOrgId, targetOrgId, transferId, new BigDecimal(price), null, user);
		//记录操作日志
		businessLogService.log("收货(物料调拨)", "调拨单号：" + transferId, user);
	}

}