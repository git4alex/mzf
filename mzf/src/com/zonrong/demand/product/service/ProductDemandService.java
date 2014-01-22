package com.zonrong.demand.product.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.basics.product.service.ProductService;
import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.DemandStatus;
import com.zonrong.common.utils.MzfEnum.StoreType;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.common.utils.MzfEnum.TransferStatus;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.util.Interceptor;
import com.zonrong.cusorder.service.CusOrderService;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.ProductInventoryService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.transfer.product.service.TransferProductService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-10-12
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ProductDemandService extends ProductDemandCRUDService {
	private static Logger logger = Logger.getLogger(CusOrderService.class);
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private CusOrderService cusOrderService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private TransferProductService transferProductService;
	@Resource
	private ProductService productService;
	@Resource
	private MzfOrgService mzfOrgService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	public enum Actor {
		store("门店", "提交"),		//门店
		franchisee("加盟店经理", "审核通过"), //加盟店经理
		saleMgr("销售总监", "审核通过"),	//销售总监
		FinanceMgr("财务总监", "审核通");	//财务审核

		private String name;
		private String operate;
		private Actor(String name, String operate) {
			this.name = name;
			this.operate = operate;
		}

		public String getName() {
			return this.name;
		}
		public String getOperate() {
			return this.operate;
		}
	}

	/**
	 * 门店提交要货申请到销售经理，如果是客订单转化的，直接提交到总部
	 * @param demandIds
	 * @param user
	 * @throws BusinessException
	 */
	public void submitToManager(Integer[] demandIds, String remark, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), demandIds);
		List<Map<String, Object>> demandList = entityService.list(metadata, where, null, user.asSystem());

		List<Integer> chainDemandIds = new ArrayList<Integer>();
		List<Integer> orderDemandIds = new ArrayList<Integer>();
		List<Integer> franchiseeDemandIds = new ArrayList<Integer>();
		for (Map<String, Object> dbDemand : demandList) {
			String storeType = MapUtils.getString(dbDemand, "storeType");
			Integer dbDemandId = MapUtils.getInteger(dbDemand, metadata.getPkCode());
			String dbOrderId = MapUtils.getString(dbDemand, "orderId");

			if (StringUtils.isBlank(dbOrderId)) {
				if (StoreType.chain.getText().equals(storeType)) {
					chainDemandIds.add(dbDemandId);
				} else if (StoreType.franchisee.getText().equals(storeType)
                        || StoreType.cooperate.getText().equals(storeType)) {
					franchiseeDemandIds.add(dbDemandId);
				} else {
					throw new BusinessException("门店类型不明确，要货申请无法流转");
				}
			} else {
				orderDemandIds.add(dbDemandId);
			}
		}

		//直营店浦东要货提交至销售总监
		if (chainDemandIds.size() > 0) {
			pass(chainDemandIds.toArray(new Integer[]{}), remark, Actor.store, new DemandStatus[]{DemandStatus.New, DemandStatus.reject}, DemandStatus.waitMgrProcess, user);
		}

		//加盟店普通要货提交至直营店经理
		if (franchiseeDemandIds.size() > 0) {
			pass(franchiseeDemandIds.toArray(new Integer[]{}), remark, Actor.store, new DemandStatus[]{DemandStatus.New, DemandStatus.reject}, DemandStatus.waitFranchiseeProcess, user);
		}

		//客订直接提交至总部
		if (orderDemandIds.size() > 0) {
			pass(orderDemandIds.toArray(new Integer[]{}), remark, Actor.store, new DemandStatus[]{DemandStatus.New}, DemandStatus.waitProcess, user);

			//自动生成裸钻的调拨单
			createTransferOnSubmitDemand(demandIds, user);
		}

		//删除之前的处理记录
		where = new HashMap<String, Object>();
		where.put("demandId", demandIds);
		entityService.delete(MzfEntity.DEMAND_PROCESS, where, user);
		//记录操作日志
		StringBuffer demandIdsStr = new StringBuffer();
		for (Integer id : demandIds) {
			demandIdsStr.append(id + ",");
		}
		businessLogService.log("门店提交要货申请", "申请单号：" + demandIdsStr, user);
	}

	/**
	 * 按照指定的状态提交要货申请
	 *
	 * @param demandIds
	 * @param user
	 * @throws BusinessException
	 */
	public void pass(Integer[] demandIds, String remark, Actor actor, DemandStatus[] priorStatus, DemandStatus status, IUser user) throws BusinessException {
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", status);
		field.put("muserId", user.getId());
		field.put("muserName", user.getName());
		field.put("mdate", null);
		field.put("submitUserId", user.getId());
		field.put("submitUserName", user.getName());
		field.put("submitDate", null);

		List<String> priorStatusList = new ArrayList<String>();
		for (DemandStatus _priorStatus : priorStatus) {
			priorStatusList.add(_priorStatus.toString());
		}
		String[] priorStatusArray = priorStatusList.toArray(new String[]{});
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("status", priorStatusArray);
		where.put("id", demandIds);

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND);
		entityService.update(metadata, field, where, user);

		//记录流程
		for (Integer demandId : demandIds) {
			int transId = transactionService.findTransId(MzfEntity.DEMAND, demandId.toString(), user);
			remark = StringUtils.isNotBlank(remark)? "备注" + remark : null;
			logService.createLog(transId, MzfEntity.DEMAND, demandId.toString(), actor.getName() + actor.getOperate(), null, null, remark, user);
		}
	}

	public void reject(Integer[] demandIds, String remark, Actor actor, DemandStatus priorStatus, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), demandIds);
		where.put("status", priorStatus);

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", DemandStatus.reject);
		field.put("muserId", null);
		field.put("muserName", null);
		field.put("mdate", null);

		int row = entityService.update(metadata, field, where, user);
		if (row > demandIds.length) {
			throw new BusinessException("请选择状态为" + priorStatus.getText() + "的要货申请进行驳回");
		}

		//记录流程
		for (Integer demandId : demandIds) {
			int transId = transactionService.findTransId(MzfEntity.DEMAND, demandId.toString(), user);
			logService.createLog(transId, MzfEntity.DEMAND, demandId.toString(), actor.getName() + "驳回要货申请", null, null, "备注: " + remark, user);
		}
	}

	//自动生成裸钻的调拨单
	private void createTransferOnSubmitDemand(Integer[] demandIds, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", demandIds);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());
		Map<Integer, String> orderIdDemandNum = new HashMap<Integer, String>();
//		List<Integer> orderIds = new ArrayList<Integer>();
		for (Map<String, Object> demand : list) {
			Integer orderId = MapUtils.getInteger(demand, "orderId");
			if (orderId != null) {
				String demandNum = MapUtils.getString(demand, "num");
				orderIdDemandNum.put(orderId, demandNum);
			}
		}
		Integer[] orderIds = orderIdDemandNum.keySet().toArray(new Integer[]{});
		if (orderIds.length == 0) return;

		where.clear();
		where.put("id", orderIds);
		list = entityService.list(MzfEntity.CUS_ORDER, where, null, user.asSystem());

		Map<Integer, Integer> productIdOrderIdMap = new HashMap<Integer, Integer>();
		Map<Integer, String> productIdDemandNumMap = new HashMap<Integer, String>();
		Map<Integer, Integer> orderIdOrgIdMap = new HashMap<Integer, Integer>();
		Map<Integer, String> orderIdOrderNumMap = new HashMap<Integer, String>();
		for (Map<String, Object> order : list) {
			Integer diamondId = MapUtils.getInteger(order, "diamondId");
			if (diamondId != null) {
				Integer orderId = MapUtils.getInteger(order, "id");
				String orderNum = MapUtils.getString(order, "num");
				Integer orgId = MapUtils.getInteger(order, "orgId");

				productIdOrderIdMap.put(diamondId, orderId);
				productIdDemandNumMap.put(diamondId, orderIdDemandNum.get(orderId));
				orderIdOrgIdMap.put(orderId, orgId);
				orderIdOrderNumMap.put(orderId, orderNum);
			}
		}
		Integer[] productIds = productIdOrderIdMap.keySet().toArray(new Integer[]{});
		if (productIds.length == 0) return;

		List<Map<String, Object>> inventoryList = productInventoryService.listProductInventory(productIds, null);
		for (Map<String, Object> product : inventoryList) {
			Integer productId = MapUtils.getInteger(product, "id");
			Integer orgId = MapUtils.getInteger(product, "orgId");

			Integer orderId = productIdOrderIdMap.get(productId);
			Integer orderIdOrgId = orderIdOrgIdMap.get(orderId);
			if (!orgId.equals(orderIdOrgId)) {
				String demandNum = orderIdDemandNum.get(orderId);
				String orderNum = orderIdOrderNumMap.get(orderId);
				String productNum = MapUtils.getString(product, "num");
				throw new BusinessException("要活申请[" + demandNum + "]关联的客订单[" + orderNum + "]中的裸钻商品[" + productNum + "]不在本部门，不能调拨出库");
			}
		}

		List<Integer> transferIds = new ArrayList<Integer>();
		Integer targetOrgId = mzfOrgService.getHQOrgId();
		for (int i = 0; i < inventoryList.size(); i++) {
			Map<String, Object> product = inventoryList.get(i);
			Integer productId = MapUtils.getInteger(product, "id");
			Integer orderId = productIdOrderIdMap.get(productId);
			String demandNum = productIdDemandNumMap.get(productId);

			Map<String, Object> transfer = new HashMap<String, Object>();
			transfer.put("orderId", orderId);
			transfer.put("remark", "该裸钻为客定款式指定的裸钻，与要货申请[" + demandNum + "]关联");
			int transferId = transferProductService.createProductTransfer(productId, targetOrgId, TransferStatus.waitSend, transfer, Integer.toString(i + 1), new Interceptor(), user);
			transferIds.add(transferId);
		}

		if (CollectionUtils.isNotEmpty(transferIds)) {
			Map<String, Object> dispatch = new HashMap<String, Object>();
			dispatch.put("targetOrgId", targetOrgId);
			transferProductService.send(transferIds.toArray(new Integer[]{}), dispatch, user);
		}
	}

	public void recieveProduct(int demandId, int productId, IUser user) throws BusinessException {
		updateStatus(demandId, DemandStatus.machining, DemandStatus.waitSend, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> demand) throws BusinessException {
				throw new BusinessException("要货申请[" + this.getNum() + "]状态为" + this.getStatus(DemandStatus.class).getText()+ ", 不能收货");
			}

		}, user);

		//记录流程信息
		int transId = transactionService.findTransId(MzfEntity.DEMAND, Integer.toString(demandId), user);
		logService.createLog(transId, MzfEntity.DEMAND, Integer.toString(demandId), "要货申请到货", TargetType.product, Integer.toString(productId), "要货申请待发货", user);
	}

	@Override
	protected EntityMetadata getBillMetadata() throws BusinessException {
		return metadataProvider.getEntityMetadata(MzfEntity.DEMAND);
	}

	@Override
	protected String getBillName() {
		return "商品要货申请";
	}
}


