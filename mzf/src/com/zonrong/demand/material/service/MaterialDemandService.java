package com.zonrong.demand.material.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.common.service.BillStatusService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.MaterialDemandStatus;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.common.utils.MzfEnum.TransferStatus;
import com.zonrong.common.utils.MzfEnum.TransferTargetType;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.common.utils.MzfUtils.BillPrefix;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.MaterialInventoryService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.transfer.material.service.TransferMaterialService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2011-8-16
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class MaterialDemandService extends BillStatusService<MaterialDemandStatus>{
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private TransferMaterialService transferMaterialService;
	@Resource
	private MaterialInventoryService materialInventoryService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
    private BusinessLogService businessLogService;


	public int create(Map<String, Object> demand, IUser user) throws BusinessException {
		//获取申请物料在当前门店的库存
		int materialId = MapUtils.getIntValue(demand, "materialId");
		int orgId = MapUtils.getIntValue(demand, "orgId");
		Map<String, Object> inventory = materialInventoryService.getInventory(materialId, orgId, user);

		String num = MzfUtils.getBillNum(BillPrefix.WLYH, user);
		demand.put("status", MaterialDemandStatus.New);
		demand.put("num", num);
		demand.put("cuserId", null);
		demand.put("cdate", null);
		if(inventory != null){
			demand.put("orgInventoryQuantity", MapUtils.getFloat(inventory, "quantity"));
		}else{
			demand.put("orgInventoryQuantity", 0);
		}

		String materialDemandId = entityService.create(getBillMetadata(), demand, user);

		//int materialId = MapUtils.getInteger(demand, "materialId");
		int transId = transactionService.createTransId();
		logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, materialDemandId, "新建物料要货申请", TargetType.material, materialId, null, user);

		return Integer.parseInt(materialDemandId);
	}

	public void update(int demandId, Map<String, Object> demand, IUser user) throws BusinessException {
		if (this.getStatus(demandId, MaterialDemandStatus.class, user) != MaterialDemandStatus.New) {
			throw new BusinessException(this.getBillName() + "不能修改");
		}
		int materialId = MapUtils.getIntValue(demand, "materialId");
		int orgId = MapUtils.getIntValue(demand, "orgId");
		Map<String, Object> inventory = materialInventoryService.getInventory(materialId, orgId, user);
		if(inventory != null){
			demand.put("orgInventoryQuantity", MapUtils.getFloat(inventory, "quantity"));
		}else{
			demand.put("orgInventoryQuantity", 0);
		}

		Map<String, Object> field = new HashMap<String, Object>(demand);
		field.remove("status");
		field.remove("num");
		field.remove("cuserId");
		field.remove("cdate");

		entityService.updateById(getBillMetadata(), Integer.toString(demandId), field, user);
		//记录流程
		int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, Integer.toString(demandId), user);
		logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, Integer.toString(demandId), "修改物料要货申请", null, null, null, user);
	}

	public void delete(int demandId,IUser user) throws BusinessException {
		if (this.getStatus(demandId, MaterialDemandStatus.class, user) != MaterialDemandStatus.New) {
			throw new BusinessException(this.getBillName() + "不能删除");
		}
		entityService.deleteById(getBillMetadata(), Integer.toString(demandId), user.asSystem());
	}

	public void submit(Integer[] demandIds, IUser user) throws BusinessException {
		for (Integer demandId : demandIds) {
			updateStatus(demandId, new MaterialDemandStatus[]{MaterialDemandStatus.New,MaterialDemandStatus.reject}, MaterialDemandStatus.waitManagerProcess, new StatusCarrier(){
				@Override
				public void active(Map<String, Object> demand) throws BusinessException {
					throw new BusinessException("物料要货申请[" + this.getNum() + "]状态为" + this.getStatus(MaterialDemandStatus.class).getText()+ ", 不能提交");
				}

			}, user);

			//记录流程
			int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, demandId.toString(), user);
			logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, demandId.toString(), "提交物料要货申请", null, null, null, user);
			//记录操作日志
			businessLogService.log("物料要货申请提交", "申请编号：" + demandId, user);
		}
	}

    /**
     * 区域经理批量审核
     *
     * @param ids 要货申请Id
     * @param remark 备注
     * @param user
     * @throws BusinessException
     */
    public void bManagerProcess(List<Integer> ids, String remark, IUser user)throws BusinessException {
        for(Integer demandId:ids){
            Map<String,Object> demand = entityService.getById(metadataProvider.getEntityMetadata(new EntityCode("vMaterialDemand")) ,demandId,user);
            if(demand!=null){
                String type = MapUtils.getString(demand, "materialType");
                Map<String, Object> field = new HashMap<String, Object>();
                field.put("confirmQuantity", MapUtils.getFloat(demand, "expectQuantity"));
                if(type.equals("特批物料")){
                    field.put("status", MaterialDemandStatus.waitMgrProcess);
                }else{
                    field.put("status", MaterialDemandStatus.waitProcess);
                }
                entityService.updateById(getBillMetadata(), ObjectUtils.toString(demandId), field, user);

                //记录日志
                int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, String.valueOf(demandId), user);
                logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, String.valueOf(demandId), "区域经理审核物料要货申请", null, null, remark, user);
            }
        }
    }

	//区域经理审核
	public void managerProcess(int demandId, Map<String, Object> process, IUser user)throws BusinessException {
		String type = MapUtils.getString(process, "type");
		String remark = MapUtils.getString(process, "remark");
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("confirmQuantity", MapUtils.getFloat(process, "confirmQuantity"));
		if(type.equals("特批物料")){
			field.put("status", MaterialDemandStatus.waitMgrProcess);
		}else{
			field.put("status", MaterialDemandStatus.waitProcess);
		}
		entityService.updateById(getBillMetadata(), Integer.toString(demandId), field, user);

		int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, String.valueOf(demandId), user);
		logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, String.valueOf(demandId), "区域经理审核物料要货申请", null, null, remark, user);
	}

    //

    /**
     * 销售总监批量审核
     *
     * @param ids 要货申请Id
     * @param remark 备注
     * @param user
     * @throws BusinessException
     */
    public void bMgrProcess(List<Integer> ids,String remark, IUser user)throws BusinessException {
        for(Integer demandId:ids){
            Map<String, Object> field = new HashMap<String, Object>();
            field.put("status", MaterialDemandStatus.waitProcess);
            entityService.updateById(getBillMetadata(), ObjectUtils.toString(demandId), field, user);

            int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, ObjectUtils.toString(demandId), user);
            logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, ObjectUtils.toString(demandId), "销售总监审核物料要货申请", null, null, remark, user);
        }
    }

	//销售总监审核
	public void mgrProcess(int demandId, Map<String, Object> process, IUser user)throws BusinessException {
		String remark = MapUtils.getString(process, "remark");
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", MaterialDemandStatus.waitProcess);
		entityService.updateById(getBillMetadata(), Integer.toString(demandId), field, user);

		//记录日志
		int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, String.valueOf(demandId), user);
		logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, String.valueOf(demandId), "销售总监审核物料要货申请", null, null, remark, user);
	}

    public void bReject(List<Integer> ids, String remark, IUser user) throws BusinessException {
        for(Integer demandId:ids){
            Map<String, Object> field = new HashMap<String, Object>();
            field.put("remark", remark);
            field.put("status", MaterialDemandStatus.reject);
            entityService.updateById(getBillMetadata(), ObjectUtils.toString(demandId), field, user);

            int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, ObjectUtils.toString(demandId), user);
            logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, ObjectUtils.toString(demandId), "驳回要货申请", null, null, remark, user);
        }
    }

	public void reject(int demandId, Map<String, Object> reject, IUser user) throws BusinessException {
		String remark = MapUtils.getString(reject, "remark");

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("remark", remark);
		field.put("status", MaterialDemandStatus.reject);
		entityService.updateById(getBillMetadata(), Integer.toString(demandId), field, user);

		//记录日志
		int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, Integer.toString(demandId), user);
		logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, Integer.toString(demandId), "驳回要货申请", null, null, remark, user);
	}

	public void allcoate(int demandId, Map<String, Object> allocate, IUser user) throws BusinessException {
		BigDecimal allocatedQuantity = new BigDecimal(MapUtils.getString(allocate, "allocatedQuantity"));
		BigDecimal settlementPrice = new BigDecimal(MapUtils.getString(allocate, "settlementPrice"));

		//更新配料数量
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("allocatedQuantity", allocatedQuantity);
		field.put("settlementPrice", settlementPrice);
		field.put("status", MaterialDemandStatus.waitSend);
		entityService.updateById(getBillMetadata(), Integer.toString(demandId), field, user);

		//记录日志
		int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, Integer.toString(demandId), user);
		logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, Integer.toString(demandId), "处理要货申请", null, null, "配物料", user);
		//记录操作日志
		businessLogService.log("物料要货申请处理(配料)", "要货申请单号：" + demandId, user);
	}

	public void send(Integer[] demandIds, String remark, IUser user) throws BusinessException{
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.MATERIAL_DEMAND_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), demandIds);
		where.put("status", MaterialDemandStatus.waitSend);
		List<Map<String, Object>> demandList = entityService.list(metadata, where, null, user.asSystem());

		List<Integer> transferIds = new ArrayList<Integer>();
		for (int i = 0; i < demandList.size(); i++) {
			Map<String, Object> demand = demandList.get(i);
			Integer materialId = MapUtils.getInteger(demand, "materialId");
			Integer demandId = MapUtils.getInteger(demand, "id");

			//记录分支流程信息
			int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, Integer.toString(demandId), user);
			logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, Integer.toString(demandId), "发货", TargetType.material, Integer.toString(materialId), "物料要货申请发货", user);

			//修改要货申请状态为调拨中
			updateStatus(demandId, MaterialDemandStatus.waitSend, MaterialDemandStatus.transfering, new StatusCarrier(){
				@Override
				public void active(Map<String, Object> demand) throws BusinessException {
					throw new BusinessException("物料要货申请[" + this.getNum() + "]状态为" + this.getStatus(MaterialDemandStatus.class).getText() + ", 不能发货");
				}

			}, user);

			//新建调拨单，并修改其状态为待收货
			Integer targetOrgId = MapUtils.getInteger(demand, "orgId");
			Map<String, Object> transfer = new HashMap<String, Object>();
			transfer.put("demandId", MapUtils.getString(demand, "id"));

			transfer.put("quantity", MapUtils.getString(demand, "allocatedQuantity"));
			transfer.put("materialWholesalePrice", MapUtils.getString(demand, "settlementPrice"));
			int transferId = transferMaterialService.createTransfer(TransferTargetType.material, materialId, user.getOrgId(), targetOrgId, TransferStatus.waitSend, transfer, Integer.toString(i + 1), transId, user);
			transferIds.add(transferId);
			//锁定物料
			Map<String, Object> materialInventory = materialInventoryService.getInventory(materialId, 1, user);
			if(MapUtils.isNotEmpty(materialInventory)){
				int inventoryId = MapUtils.getIntValue(materialInventory, "id");
				double lockedQuantity = MapUtils.getDoubleValue(demand, "allocatedQuantity");
				materialInventoryService.lock(inventoryId, lockedQuantity, user);

			}
			//记录操作日志
			businessLogService.log("物料要货申请处理(发货)", "要货申请单号：" + demandId, user);
		}

		Integer firstTargetOrgId = MapUtils.getInteger(demandList.get(0), "orgId");
		Map<String, Object> dispatch = new HashMap<String, Object>();
		dispatch.put("targetOrgId", firstTargetOrgId);
		transferMaterialService.send(transferIds.toArray(new Integer[]{}), dispatch, user);
	}

	 //打印待发货的物料要货单
	public List getPrintData(IUser user)throws BusinessException{
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("status", "waitProcess");
		OrderBy orderBy = new OrderBy(new String[]{"materialNum","materialName"}, OrderByDir.desc);
		List<Map<String, Object>> dataList  = entityService.list(MzfEntity.MATERIAL_DEMAND_VIEW, where, orderBy, user);
		return dataList;
	}
//	public void send(Integer[] demandIds, String remark, IUser user) throws BusinessException{
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.MATERIAL_DEMAND_VIEW);
//		Map<String, Object> where = new HashMap<String, Object>();
//		where.put(metadata.getPkCode(), demandIds);
//		List<Map<String, Object>> dbDemandList = entityService.list(metadata, where, null, user.asSystem());
//
//		Integer sourceOrgId = user.getOrgId();
//		Integer targetOrgId = null;
//		List<Integer> transferIds = new ArrayList<Integer>();
//		for (int i = 0; i < dbDemandList.size(); i++) {
//			Map<String, Object> dbDemand = dbDemandList.get(i);
//			Integer materialId = MapUtils.getInteger(dbDemand, "materialId");
//
//			int transferId = send(dbDemand, materialId, Integer.toString(i + 1), user);
//			transferIds.add(transferId);
//			if (targetOrgId == null) {
//				targetOrgId = MapUtils.getInteger(dbDemand, "orgId");
//			}
//		}
//
//		//创建发货单
//		Map<String, Object> dispatch = new HashMap<String, Object>();
//		String num = MzfUtils.getBillNum(BillPrefix.FH, user);
//		dispatch.put("num", num);
//		dispatch.put("sourceOrgId", sourceOrgId);
//		dispatch.put("targetOrgId", targetOrgId);
//		dispatch.put("remark", null);
//		dispatch.put("cuserId", null);
//		dispatch.put("cdate", null);
//		String dispatchId = entityService.create(MzfEntity.DISPATCH, dispatch, user);
//
//		Map<String, Object> field = new HashMap<String, Object>();
//		field.put("dispatchId", dispatchId);
//		where = new HashMap<String, Object>();
//		where.put("id", transferIds.toArray(new Integer[]{}));
//		entityService.update(MzfEntity.TRANSFER, field, where, user);
//	}
//
//	private int send(Map<String, Object> demand, int materialId, String transferNumSufix, IUser user) throws BusinessException {
//		Integer demandId = MapUtils.getInteger(demand, "id");
//
//		//分支流程信息
//		int transId = transactionService.findTransId(MzfEntity.MATERIAL_DEMAND, Integer.toString(demandId), user);
//		logService.createLog(transId, MzfEntity.MATERIAL_DEMAND, Integer.toString(demandId), "发货", TargetType.material, Integer.toString(materialId), "物料要货申请发货", user);
//
//		//新建调拨单，并修改其状态为待收货
//		Integer targetOrgId = MapUtils.getInteger(demand, "orgId");
//		Map<String, Object> transfer = new HashMap<String, Object>();
//		transfer.put("demandId", MapUtils.getString(demand, "id"));
//
//		transfer.put("quantity", MapUtils.getString(demand, "allocatedQuantity"));
//		transfer.put("materialWholesalePrice", MapUtils.getString(demand, "settlementPrice"));
//		int transferId = transferMaterialService.createTransfer(TransferTargetType.material, materialId, user.getOrgId(), targetOrgId, TransferStatus.waitReceive, transfer, transferNumSufix, transId, user);
//
//		//修改要货申请状态为调拨中
//		updateStatus(demandId, MaterialDemandStatus.waitSend, MaterialDemandStatus.transfering, new StatusCarrier(){
//			@Override
//			public void active(Map<String, Object> demand) throws BusinessException {
//				throw new BusinessException("物料要货申请[" + this.getNum() + "]状态为" + this.getStatus(MaterialDemandStatus.class).getText() + ", 不能发货");
//			}
//
//		}, user);
//
//		return transferId;
//	}

	@Override
	protected EntityMetadata getBillMetadata() throws BusinessException {
		return metadataProvider.getEntityMetadata(MzfEntity.MATERIAL_DEMAND);
	}

	@Override
	protected String getBillName() {
		return "物料要货申请";
	}
}


