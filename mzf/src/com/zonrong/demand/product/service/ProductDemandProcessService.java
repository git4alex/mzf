package com.zonrong.demand.product.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.DemandStatus;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.common.utils.MzfEnum.TransferStatus;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.common.utils.MzfUtils.BillPrefix;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.cusorder.service.CusOrderService;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
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
public class ProductDemandProcessService {
	private static Logger logger = Logger.getLogger(CusOrderService.class);
	@Resource
	private EntityService entityService;
	@Resource
	private ProductDemandService demandService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private TransferProductService transferProductService;
	@Resource
	private ProductService productService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	/**
	 * 要货单处理方式
	 */
	public enum DemandProcessType {
		allocate,	//库存调拨
		replaceAllocate,  //替代调拨
		purchase,	//采购
		OEM,		//委外
		reject		//驳回
	}

	public Map<String, Object> getDemandProcess(int demandId, IUser user) throws BusinessException {
		Map<String, Object> demandProcess = entityService.getById(MzfEntity.DEMAND_PROCESS_VIEW, demandId, user);
		Integer styleId = MapUtils.getInteger(demandProcess, "styleId");
		checkProcess(demandProcess, user);

		Map<String, Object> where = new HashMap<String, Object>();
		//where.put("styleId", styleId);
		where.put("status", ProductStatus.free);
		where.put("orgId", user.getOrgId());
		where.put("pkind", MapUtils.getString(demandProcess, "pkind"));
		where.put("ptype", MapUtils.getString(demandProcess, "ptype"));
		where.put("storageKind", "product");
		List<Map<String, Object>> inventoryList = entityService.list(MzfEntity.PRODUCT_INVENTORY_VIEW, where, null, user);
		if (CollectionUtils.isNotEmpty(inventoryList)) {
			demandProcess.put("hasInventory", true);
		} else {
			demandProcess.put("hasInventory", false);
		}

		where = new HashMap<String, Object>();
		where.put("styleId", styleId);
		where.put("orgId", user.getOrgId());
		List<Map<String, Object>> vendorStyleList = entityService.list(MzfEntity.VENDOR_STYLE_VIEW, where, null, user);
		if (CollectionUtils.isNotEmpty(vendorStyleList) && vendorStyleList.size() == 1) {
			demandProcess.put("singleVendorStyle", vendorStyleList.get(0));
		}

		return demandProcess;
	}

	/**
	 * 检查要货申请是否可以重新处理
	 * @param demandProcess
	 * @param user
	 * @throws BusinessException
	 */
	private void checkProcess(Map<String, Object> demandProcess, IUser user)throws BusinessException{
		String processType = MapUtils.getString(demandProcess, "type", "");  //处理方式

		 //采购或委外加工
		if(processType.equals("OEM") || processType.equals("purchase")){
			String orderNum = MapUtils.getString(demandProcess, "vendorOrderNum", "");
			//获取采购或委外加工订单
			Map<String, Object> where = new HashMap<String, Object>();
			where.put("num", orderNum);
			List<Map<String, Object>> vendorOrders = entityService.list(MzfEntity.VENDOR_ORDER, where, null, user);
			if(CollectionUtils.isNotEmpty(vendorOrders)){
				Map<String, Object> vendorOrder = vendorOrders.get(0);
				String orderStatus = MapUtils.getString(vendorOrder, "status", "");
				boolean isDosingDiamond = MapUtils.getBooleanValue(vendorOrder, "isDosingDiamond", false);  //裸钻和碎钻是否配置
				boolean isDosingParts  = MapUtils.getBooleanValue(vendorOrder, "isDosingParts", false);   //配件是否配置
				String type = MapUtils.getString(vendorOrder, "type", "");
				if(!"New".equals(orderStatus)){
					throw new BusinessException("供应商订单为非[新建]状态，不能重新处理");
				}
				if(isDosingDiamond && type.equals("OEM")){
					throw new BusinessException("供应商订单裸石或碎石已确认配料，不能重新处理");
				}
				if(isDosingParts && type.equals("OEM")){
					throw new BusinessException("供应商订单配件已确认配料，不能重新处理");
				}
			}
		}

	}

	public void allocate(int demandId, Map<String, Object> process, IUser user) throws BusinessException {
		preProcess(demandId, user);

		String message = "当前不允许库存调拨";
		process(demandId, process, DemandStatus.waitSend, DemandProcessType.allocate, message, user);

		//锁定商品
		Map<String, Object> demand = entityService.getById(MzfEntity.DEMAND, Integer.toString(demandId), user);
		String demandNum = MapUtils.getString(demand, "num");
		Integer productId = MapUtils.getInteger(process, "productId");
		productService.lock(productId, "与要货申请[" + demandNum + "]关联", user);
		//记录操作日志
		businessLogService.log("处理要货申请(库存调拨)", "要货单号：" + demandId, user);
	}
	public void replaceAllocate(int demandId, Map<String, Object> process, IUser user) throws BusinessException {
		preProcess(demandId, user);

		String message = "当前不允许库存调拨";
		process(demandId, process, DemandStatus.waitSend, DemandProcessType.replaceAllocate, message, user);

		//修改要货申请(增加配货款式)
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("allocateStyleId", MapUtils.getString(process, "allocateStyleId"));
		field.put("allocateRemark", MapUtils.getString(process, "allocateRemark"));
		entityService.updateById(MzfEntity.DEMAND, demandId + "", field, user);

		//锁定商品
		Map<String, Object> demand = entityService.getById(MzfEntity.DEMAND, Integer.toString(demandId), user);
		String demandNum = MapUtils.getString(demand, "num");
		Integer productId = MapUtils.getInteger(process, "productId");
		productService.lock(productId, "与要货申请[" + demandNum + "]关联", user);
		//记录操作日志
		businessLogService.log("处理要货申请(替代调拨)", "要货单号：" + demandId, user);
	}

	public void purchase(int demandId, Map<String, Object> demand, IUser user) throws BusinessException {
		preProcess(demandId, user);

		String message = "当前不允许商品采购";
		process(demandId, demand, DemandStatus.waitPurchase, DemandProcessType.purchase, message, user);
		//记录操作日志
		businessLogService.log("处理要货申请(商品采购)", "要货单号：" + demandId, user);
	}

	public void oem(int demandId, Map<String, Object> demand, IUser user) throws BusinessException {
		preProcess(demandId, user);

		String message = "当前不允许委外加工";
		process(demandId, demand, DemandStatus.waitOEM, DemandProcessType.OEM, message, user);
		//记录操作日志
		businessLogService.log("处理要货申请(委外加工)", "要货单号：" + demandId, user);
	}

	/**
	 * 处理要货申请准备
	 * @param demandId
	 * @param user
	 * @throws BusinessException
	 */
	private void preProcess(int demandId, IUser user)throws BusinessException{
		Map<String, Object> demandProcess = entityService.getById(MzfEntity.DEMAND_PROCESS_VIEW, demandId, user);
		String processType = MapUtils.getString(demandProcess, "type", "");  //要货申请处理方式
		String processStatus = MapUtils.getString(demandProcess, "status", "");  //状态
		//如果上次处理方式为采购或委外加工则删除供应商订单
		if(processType.equals("OEM") || processType.equals("purchase")){
			Map<String, Object> where = new HashMap<String, Object>();
			where.put("demandId", demandId);
			List<Map<String, Object>> productOrderDetiaList = entityService.list(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, where, null, user);
			if(CollectionUtils.isNotEmpty(productOrderDetiaList)){
				String orderId = MapUtils.getString(productOrderDetiaList.get(0), "orderId", "0");
				//删除供应商订单明细
				 entityService.delete(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, where, user);
				//删除供应商订单
				 where = new HashMap<String, Object>();
				 where.put("orderId", orderId);
				 productOrderDetiaList = entityService.list(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, where, null, user);
				if(CollectionUtils.isEmpty(productOrderDetiaList)){
					entityService.deleteById(MzfEntity.VENDOR_ORDER, orderId, user);
				}

			}
			//如果处理方式为库存调拨且商品代发货，则解锁商品
		}else if(processType.equals("purchase") || processType.equals("replaceAllocate")){
			if(processStatus.equals("waitSend")){
				int productId = MapUtils.getIntValue(demandProcess, "productId", 0);
				productService.free(productId, "要货申请重新处理，解锁商品", user);
			}
		}


	}

	private void process(int demandId, Map<String, Object> process, DemandStatus status, DemandProcessType type, final String message, IUser user) throws BusinessException {
		DemandStatus[] fromStatus = new DemandStatus[]{
				DemandStatus.waitProcess,
				DemandStatus.waitSend,
				DemandStatus.waitPurchase,
				DemandStatus.waitOEM
		};
		demandService.updateStatus(demandId, fromStatus, status, new StatusCarrier() {
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				throw new BusinessException(message);
			}

		}, user);

		//记录处理结果
		process.remove("id");
		createDemandProcess(demandId, type, process, user);

		//记录流程
		createLog(demandId, type, null, user);

	}

	public void reject(int demandId, Map<String, Object> process, IUser user) throws BusinessException {
		//修改要货单状态
		DemandStatus[] fromStatus = new DemandStatus[]{
				DemandStatus.waitProcess,
				DemandStatus.waitSend,
				DemandStatus.waitPurchase,
				DemandStatus.waitOEM
		};
		demandService.updateStatus(demandId, fromStatus, DemandStatus.reject, new StatusCarrier() {
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				throw new BusinessException("要货申请[" + this.getNum() + "]状态为" + this.getStatus(DemandStatus.class).getText() + ", 不允许驳回");
			}

		}, user);

		//记录处理结果
		process.remove("id");
		createDemandProcess(demandId, DemandProcessType.reject, process, user);

		//记录流程
		String rejectRemark = MapUtils.getString(process, "rejectRemark");
		rejectRemark = "驳回原因：" + rejectRemark;
		createLog(demandId, DemandProcessType.reject, rejectRemark, user);
	}

	//记录流程
	private void createLog(int demandId, DemandProcessType type, String remark, IUser user) throws BusinessException {
		int transId = transactionService.findTransId(MzfEntity.DEMAND, Integer.toString(demandId), user);

		String operate = "处理要货申请";
		StringBuffer remark1 = new StringBuffer("处理方式：");
		if (type == DemandProcessType.allocate) {
			remark1.append("库存调拨");
		}else if(type == DemandProcessType.replaceAllocate){
			remark1.append("替代调拨");
		} else if (type == DemandProcessType.purchase) {
			remark1.append("商品采购");
		} else if (type == DemandProcessType.OEM) {
			remark1.append("委外加工");
		} else if (type == DemandProcessType.reject) {
			remark1.append("驳回");
		}

		if (StringUtils.isNotBlank(remark)) {
			remark1.append(" ").append(remark);
		}
		logService.createLog(transId, MzfEntity.DEMAND, Integer.toString(demandId), operate, null, null, remark1.toString(), user);
	}

	public void cancelDemand(int demandId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND);
		Map<String, Object> dbDemand = entityService.getById(metadata, demandId, user.asSystem());

		Integer orderId = MapUtils.getInteger(dbDemand, "orderId");
		String num = MapUtils.getString(dbDemand, "num");
		if (orderId != null) {
			String orderNum = MapUtils.getString(dbDemand, "orderNum");
			throw new BusinessException("要货申请[" + num + "]由客订单[" + orderNum + "]转化而来，不能取消");
		}

		demandService.updateStatus(demandId, DemandStatus.waitSend, DemandStatus.canceled, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> demand) throws BusinessException {
				throw new BusinessException("要货申请[" + this.getNum() + "]状态为" + this.getStatus(DemandStatus.class).getText() + ", 该操作不能继续");
			}

		}, user);
		//解锁商品
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("demandId", demandId);
		List<Map<String, Object>> demandProcesses = entityService.list(MzfEntity.DEMAND_PROCESS, where, null, user);
		if(CollectionUtils.isNotEmpty(demandProcesses)){
			Integer productId = MapUtils.getInteger(demandProcesses.get(0), "productId");
			if(productId != null){
				productService.free(productId, "要申请["+num+"]取消", user);
			}
		}

		//记录流程信息
		int transId = transactionService.findTransId(MzfEntity.DEMAND, Integer.toString(demandId), user);
		logService.createLog(transId, MzfEntity.DEMAND, Integer.toString(demandId), "取消要货申请", null, null, null, user);
		//记录操作日志
		businessLogService.log("要货申请撤销", "要货申请编号：" + demandId, user);
	}

	public void sendProduct(Integer[] demandIds, IUser user) throws BusinessException{
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND_PROCESS_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), demandIds);
		List<Map<String, Object>> dbDemandList = entityService.list(metadata, where, null, user.asSystem());

		Integer sourceOrgId = user.getOrgId();
		Integer targetOrgId = null;
		List<Integer> transferIds = new ArrayList<Integer>();
		for (int i = 0; i < dbDemandList.size(); i++) {
			Map<String, Object> dbDemand = dbDemandList.get(i);
			Integer productId = MapUtils.getInteger(dbDemand, "productId");
			if (productId == null) {
				throw new BusinessException("未找到要货单对应的商品");
			}
			String productNum = MapUtils.getString(dbDemand, "productNum");

			int transferId = sendProduct(dbDemand, productId, Integer.toString(i + 1), user);
			transferIds.add(transferId);
			if (targetOrgId == null) {
				targetOrgId = MapUtils.getInteger(dbDemand, "orgId");
			}
			//记录操作日志
			businessLogService.log("要货申请发货", "要货申请编号：" + MapUtils.getString(dbDemand, "id"), user);
		}

		//创建发货单
		Map<String, Object> dispatch = new HashMap<String, Object>();
		String num = MzfUtils.getBillNum(BillPrefix.FH, user);
		dispatch.put("num", num);
		dispatch.put("sourceOrgId", sourceOrgId);
		dispatch.put("targetOrgId", targetOrgId);
		dispatch.put("remark", null);
		dispatch.put("cuserId", null);
		dispatch.put("cdate", null);
		String dispatchId = entityService.create(MzfEntity.DISPATCH, dispatch, user);

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("dispatchId", dispatchId);
		field.put("sdate", null);
		where = new HashMap<String, Object>();
		where.put("id", transferIds.toArray(new Integer[]{}));
		entityService.update(MzfEntity.TRANSFER, field, where, user);
	}

	private int sendProduct(Map<String, Object> demand, int productId, String transferNumSufix, IUser user) throws BusinessException {
		Integer demandId = MapUtils.getInteger(demand, "id");

		//分支流程信息
		int transId = transactionService.findTransId(MzfEntity.DEMAND, Integer.toString(demandId), user);
		logService.createLog(transId, MzfEntity.DEMAND, Integer.toString(demandId), "发货", TargetType.product, Integer.toString(productId), "要货申请发货", user);

		//新建调拨单，并修改其状态为待收货
		Integer targetOrgId = MapUtils.getInteger(demand, "orgId");
		Map<String, Object> transfer = new HashMap<String, Object>();
		transfer.put("orderId", MapUtils.getInteger(demand, "orderId"));
		transfer.put("demandId", MapUtils.getString(demand, "id"));
		int transferId = transferProductService.createProductTransfer(productId, targetOrgId, TransferStatus.waitReceive, transfer, transferNumSufix, null, user);

		//从仓库发货
        transfer = entityService.getById(MzfEntity.TRANSFER,transferId,user);
		productInventoryService.send(productId, targetOrgId,MapUtils.getString(transfer,"num"), user);

		//修改要货申请状态为调拨中
		demandService.updateStatus(demandId, DemandStatus.waitSend, DemandStatus.transfering, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> demand) throws BusinessException {
				throw new BusinessException("商品要货申请[" + this.getNum() + "]状态为" + this.getStatus(DemandStatus.class).getText() + ", 不能发货");
			}

		}, user);

		return transferId;
	}

	public void updateDemandProcessByDemandId(int demandId, int productId, String productNum, String productName, IUser user) throws BusinessException {
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("productId", productId);
		field.put("productNum", productNum);
		field.put("productName", productName);

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("demandId", demandId);
		int row = entityService.update(MzfEntity.DEMAND_PROCESS, field, where, user);
		if (row == 0) {
			throw new BusinessException("未找到要货申请处理记录");
		} else if (row > 1) {
			throw new BusinessException("同一个要货申请对应了多个处理结果");
		}
	}

	private int createDemandProcess(int demandId, DemandProcessType type, Map<String, Object> process, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND_PROCESS);

		//删除上一次的处理记录（删除的记录行数有可能为0）
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("demandId", demandId);
		entityService.delete(metadata, where, user);

		process.put("demandId", demandId);
		process.put("type", type);
		process.put("cuserId", null);
		process.put("cuserName", null);
		process.put("cdate", null);
		String id = entityService.create(metadata, process, user);
		return Integer.parseInt(id);
	}

	private Map<String, Object> getProcessor(Map<String, Object> demand) throws BusinessException {
		Map<String, Object> process = new HashMap<String, Object>();

		Object obj = demand.get("vendorId");
		if (obj != null) process.put("vendorId", obj);
		obj = demand.get("vendorName");
		if (obj != null) process.put("vendorName", obj);
		obj = demand.get("vendorStyleId");
		if (obj != null) process.put("vendorStyleId", obj);
		obj = demand.get("vendorStyleCode");
		if (obj != null) process.put("vendorStyleCode", obj);
		obj = demand.get("vendorStyleName");
		if (obj != null) process.put("vendorStyleName", obj);
		obj = demand.get("productId");
		if (obj != null) process.put("productId", obj);
		obj = demand.get("productNum");
		if (obj != null) process.put("productNum", obj);
		obj = demand.get("productName");
		if (obj != null) process.put("productName", obj);

		obj = demand.get("confirmWeight");
		if (obj != null) process.put("confirmWeight", obj);
		obj = demand.get("confirmSize");
		if (obj != null) process.put("confirmSize", obj);
		obj = demand.get("confirmColor");
		if (obj != null) process.put("confirmColor", obj);
		obj = demand.get("confirmClean");
		if (obj != null) process.put("confirmClean", obj);

		return process;
	}
}


