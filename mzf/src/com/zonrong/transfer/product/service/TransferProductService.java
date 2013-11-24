package com.zonrong.transfer.product.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.basics.rel.service.OrgRelService;
import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.*;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.core.util.Interceptor;
import com.zonrong.cusorder.service.CusOrderService;
import com.zonrong.demand.product.service.ProductDemandService;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.inventory.service.InventoryService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.settlement.service.SettlementService;
import com.zonrong.showcase.service.ShowcaseCheckService;
import com.zonrong.system.service.BizCodeService;
import com.zonrong.system.service.OrgService;
import com.zonrong.transfer.common.service.TransferService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * date: 2010-11-22
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class TransferProductService extends TransferService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private ProductService productService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private ProductDemandService demandService;
	@Resource
	private CusOrderService cusOrderService;
	@Resource
	private ShowcaseCheckService showcaseCheckService;
	@Resource
	private SettlementService settlementService;
	@Resource
	private OrgRelService orgRelService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private InventoryService inventoryService;
	@Resource
	private OrgService orgService;
	@Resource
	private MzfOrgService mzfOrgService;
	@Resource
	private FlowLogService logService;
	@Resource
    private BusinessLogService businessLogService;

	protected TransferTargetType getTargetType() {
		return TransferTargetType.product;
	}

	/**
	 * 申请调拨
	 *
	 * @param productId
	 * @param targetOrgId
	 * @param transfer
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public int applyTransfer(final int productId, int targetOrgId, Map<String, Object> transfer, IUser user) throws BusinessException {
		return createProductTransfer(productId, targetOrgId, TransferStatus.waitConfirm, transfer, null, new Interceptor(){
			@Override
			public void before(Map<String, Object> transfer, IUser user)  throws BusinessException {
				Map<String, Object> inventory = productInventoryService.getProductInventory(productId, null);
				String productNum = MapUtils.getString(inventory, "num");
				Integer orgId = MapUtils.getInteger(inventory, "orgId");
				if (orgId == user.getOrgId()) {
					throw new BusinessException("商品[" + productNum + "]已在本部门，不必调拨");
				}

				ProductStatus status = ProductStatus.valueOf(MapUtils.getString(inventory, "status"));
				if (ProductStatus.locked == status) {
					String statusRemark = MapUtils.getString(inventory, "statusRemark");
					throw new BusinessException("商品[" + productNum + "]不能调拨，原因:[" + statusRemark + "]");
				}
			}

			@Override
			public void after(Map<String, Object> transfer, IUser user)  throws BusinessException {
				//锁定商品
				String transferNum = MapUtils.getString(transfer, "num");
				String statusRemark = "内部交易锁定商品, 与调拨单[" + transferNum + "]关联";
				productService.lock(productId, statusRemark, user);
			}
		}, user);
	}

	/**
	 * 客订单申请调拨
	 *
	 * @param productId
	 * @param targetOrgId
	 * @param transfer
	 * @param user
	 * @throws BusinessException
	 */
	public int applyTransferFromCusOrder(final int productId, final int targetOrgId, Map<String, Object> transfer, IUser user) throws BusinessException {
		int id = createProductTransfer(productId, targetOrgId, TransferStatus.waitConfirm, transfer, null, new Interceptor(){
			@Override
			public void before(Map<String, Object> transfer, IUser user)  throws BusinessException {
				Map<String, Object> inventory = productInventoryService.getProductInventory(productId, null);
				Integer sourceOrgId = MapUtils.getInteger(inventory, "orgId");
				Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
				if (sourceOrgId.intValue() == targetOrgId) {
					String productNum = MapUtils.getString(inventory, "num");
					throw new BusinessException("商品[" + productNum + "]已在该部门，不必申请调拨");
				}
			};
			@Override
			public void after(Map<String, Object> transfer, IUser user)  throws BusinessException {
				//锁定商品
				String transferNum = MapUtils.getString(transfer, "num");
				String statusRemark = "内部交易锁定商品, 与调拨单[" + transferNum + "]关联";
				productService.lock(productId, statusRemark, user);
			};
		}, user);

		//更新客订单状态为调拨中
		Integer orderId = MapUtils.getInteger(transfer, "orderId");
		if (orderId != null) {
			cusOrderService.updateStatus(orderId, CusOrderStatus.New, CusOrderStatus.transfering, new StatusCarrier() {
				@Override
				public void active(Map<String, Object> order) throws BusinessException {
					throw new BusinessException("调拨单与客户订单[" + this.getNum() + "]关联失败");
				}

			}, user);
		}

		return id;
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
		List<Map<String, Object>> inventoryList = productInventoryService.listProductInventory(productIds, null);
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
		if (targetOrgId != null) {
			checkTransferProductBySelf(targetOrgId, inventoryList, user);
		}
		Integer suserId = MapUtils.getInteger(transfer, "suserId");

		List<Integer> transferIdList = new ArrayList<Integer>();
		for (int i = 0; i < inventoryList.size(); i++) {
			final Map<String, Object> inventory = inventoryList.get(i);
			final Integer productId = MapUtils.getInteger(inventory, "id");

			TransferStatus status = TransferStatus.waitApprove;;
			Integer sourceOrgId = MapUtils.getInteger(inventory, "orgId");
			if (targetOrgId != null) {
				if (!orgRelService.isRequireApprove(sourceOrgId, targetOrgId)) {
					status = TransferStatus.waitSend;
				}
			} else {
				status = TransferStatus.waitSend;
				Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
				if (suserId == null) {
					throw new BusinessException("未指定持有人");
				}

				//更改库存持有人
				inventoryService.updateOwnerId(inventoryId, suserId, user);
			}

			int transferId = createProductTransfer(productId, targetOrgId, status, transfer, Integer.toString(i + i), new Interceptor(){
				@Override
				public void after(Map<String, Object> transfer, IUser user)  throws BusinessException {
					//锁定商品
					String transferNum = MapUtils.getString(transfer, "num");
					String statusRemark = "内部交易锁定商品, 与调拨单[" + transferNum + "]关联";
					productService.lock(productId, statusRemark, user);
				}
			},user);
			if (targetOrgId != null && status == TransferStatus.waitSend) {
				transferIdList.add(transferId);
			}
			//记录操作日志
			businessLogService.log("出库(商品库存)", "商品编号：" + productId, user);
		}

		//系统自动发货
		if (transferIdList.size() > 0) {
			Integer[] transferIds = transferIdList.toArray(new Integer[]{});
			Map<String, Object> dispatch = new HashMap<String, Object>();
			dispatch.put("targetOrgId", targetOrgId);

			this.send(transferIds, dispatch, user);
		}
	}

	public int createProductTransfer(int productId, Integer targetOrgId, TransferStatus status, Map<String, Object> transfer, String numSufix, Interceptor interceptor, IUser user) throws BusinessException {
		if (interceptor != null) {
			interceptor.before(transfer, user);
		}

		Integer orderId = MapUtils.getInteger(transfer, "orderId");
		if (orderId != null) {
			Map<String, Object> where = new HashMap<String, Object>();
			where.put("orderId", orderId);
			where.put("targetOrgId", targetOrgId);

			List list = entityService.list(MzfEntity.TRANSFER_VIEW, where, null, user.asSystem());
			if (CollectionUtils.isNotEmpty(list)) {
				Map<String, Object> dbOrder = entityService.getById(MzfEntity.CUS_ORDER, orderId, user.asSystem());
				String orderNum = MapUtils.getString(dbOrder, "num");
				throw new BusinessException("客订单[" + orderNum + "]已经生成调拨申请！");
			}
		}

		Map<String, Object> inventory = productInventoryService.getProductInventory(productId, null);
		Integer sourceOrgId = MapUtils.getInteger(inventory, "orgId");

		//记录流程信息
		int transId;
		Integer demandId = MapUtils.getInteger(transfer, "demandId");
		if (orderId != null) {
			transId = transactionService.findTransId(MzfEntity.CUS_ORDER, orderId.toString(), user);
		} else if (demandId != null) {
			transId = transactionService.findTransId(MzfEntity.DEMAND, demandId.toString(), user);
		} else {
			transId = transactionService.createTransId();
		}
		//商品调拨，数量为1
		transfer.put("quantity", 1);
		String wholesalePrice = MapUtils.getString(inventory, "wholesalePrice");
		if (StringUtils.isBlank(wholesalePrice)) {
			throw new BusinessException("批发价为空，不能调拨");
		}
		transfer.put("productPriorSettlementPrice", wholesalePrice);
		int transferId = createTransfer(getTargetType(), productId, sourceOrgId, targetOrgId, status, transfer, numSufix, transId, user);

		if (interceptor != null) {
			transfer.put("id", transferId);
			interceptor.after(transfer, user);
		}

		return transferId;
	}

	public void confirmTransfer(int transferId, Map<String, Object> confirm, IUser user) throws BusinessException {
		Map<String, Object> transfer = entityService.getById(MzfEntity.TRANSFER_VIEW, transferId, User.getSystemUser());

		TransferStatus resStatus = TransferStatus.canceled;
		boolean isAgree = MapUtils.getBoolean(confirm, "isAgree");
		String remark = MapUtils.getString(confirm, "remark");
		if (isAgree) {
			remark = "货主同意调拨 " + remark;
			resStatus = TransferStatus.waitApprove;
		} else {
			remark = "货主不同意调拨 " + remark;
		}
		//判断是否需要审核
		if (resStatus == TransferStatus.waitApprove) {
			Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
			Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
			if (!orgRelService.isRequireApprove(sourceOrgId, targetOrgId)) {
				resStatus = TransferStatus.waitSend;
			}
		}
		updateStatus(transferId, new TransferStatus[]{TransferStatus.waitConfirm}, resStatus, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				throw new BusinessException("调拨单[" + this.getNum() + "]状态为" + this.getStatus(TransferStatus.class).getText() + "， 不能进行确认操作");
			}

		}, user);

		Integer productId = MapUtils.getInteger(transfer, "targetId");
		if (!isAgree) {
			Integer orderId = MapUtils.getInteger(transfer, "orderId");
			if (orderId != null) {
				cusOrderService.interruptedOrder(orderId, user);
			}

			if (productId != null) {
				productService.free(productId, "货主不同意调拨该商品, 解锁商品", user);
			}
		}

		//记录流程信息
		int transId = transactionService.findTransId(MzfEntity.TRANSFER, Integer.toString(transferId), user);
		logService.createLog(transId, MzfEntity.TRANSFER, Integer.toString(transferId), "货主确认调拨", getTargetType().getBizTargetType(), productId, remark, user);
		//记录操作日志
		businessLogService.log("货主确认(商品调拨)", "商品编号：" + productId, user);
	}

	@Override
	public void cancelTransfer(Map<String, Object> transfer, IUser user) throws BusinessException {
		super.cancelTransfer(transfer, user);

		Integer orderId = MapUtils.getInteger(transfer, "orderId");
		if (orderId != null) {
			cusOrderService.interruptedOrder(orderId, user);
		}

		Integer productId = MapUtils.getInteger(transfer, "targetId");
		productService.free(productId, "取消调拨, 解锁商品", user);

		Map<String, Object> inventroy = productInventoryService.getProductInventory(productId, user.getOrgId());
		Integer inventoryId = MapUtils.getInteger(inventroy, "inventoryId");
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", InventoryStatus.onStorage);
		field.put("remark", "取消调拨");
		entityService.updateById(MzfEntity.INVENTORY, inventoryId.toString(), field, user);
		//记录操作日志
		businessLogService.log("取消调拨(商品调拨)", "商品编号：" + productId, user);
	}

	public int approveTransfer(int transferId, Map<String, Object> approve, IUser user) throws BusinessException {
		Map<String, Object> transfer = entityService.getById(MzfEntity.TRANSFER_VIEW, transferId, User.getSystemUser());
		Integer productId = MapUtils.getInteger(transfer, "targetId");
		Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, productId, User.getSystemUser());
		ProductType ptype = ProductType.valueOf(MapUtils.getString(product, "ptype"));
		BigDecimal dbRetailBasePrice = null;
		BigDecimal dbWholesalePrice = null;
		BigDecimal retailBasePrice = null;
		String wholesalePriceStr = MapUtils.getString(approve, "wholesalePrice");
		if (StringUtils.isBlank(wholesalePriceStr)) {
			throw new BusinessException("请填写批发价");
		}
		BigDecimal wholesalePrice = new BigDecimal(wholesalePriceStr);
		if (ptype != ProductType.pt && ptype != ProductType.gold) {
			dbRetailBasePrice = new BigDecimal(MapUtils.getString(product, "retailBasePrice"));	//一口价
			dbWholesalePrice = new BigDecimal(MapUtils.getString(product, "wholesalePrice"));		//批发价

			String retailBasePriceStr = MapUtils.getString(approve, "retailBasePrice");
			if (StringUtils.isBlank(retailBasePriceStr)) {
				throw new BusinessException("请填写一口价");
			}
			retailBasePrice = new BigDecimal(retailBasePriceStr);	//一口价
		}
		String remark = getRemark(retailBasePrice, dbRetailBasePrice, wholesalePrice, dbWholesalePrice, user);

		//创建审核
		approve.put("transferId", transferId);
		approve.put("remark", MapUtils.getString(approve, "remark") + " " + remark);
		approve.put("cuserId", null);
		approve.put("cuserName", null);
		approve.put("cdate", null);
		String id = entityService.create(MzfEntity.TRANSFER_APPROVE, approve, user);

		boolean res = MapUtils.getBoolean(approve, "approve");
		TransferStatus resStatus = TransferStatus.reject;
		if (res) {
			resStatus = TransferStatus.waitSend;
		}
		updateStatus(transferId, new TransferStatus[]{TransferStatus.waitApprove}, resStatus, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				throw new BusinessException("调拨单[" + this.getNum() + "]状态为" + this.getStatus(TransferStatus.class).getText() + "， 不能进行审核操作");
			}
		}, user);

		String logRemark = "驳回";
		if (resStatus == TransferStatus.waitSend) {
			//修改商品资料
			product.clear();
			if (ptype != ProductType.pt  && ptype != ProductType.gold) {
				product.put("retailBasePrice", retailBasePrice);
				String promotionPrice = MapUtils.getString(approve, "promotionPrice");
				if (StringUtils.isBlank(promotionPrice)) {
					promotionPrice = MapUtils.getString(approve, "retailBasePrice");
				}
				product.put("promotionPrice", new BigDecimal(promotionPrice));
			}
			product.put("wholesalePrice", wholesalePrice);
			entityService.updateById(MzfEntity.PRODUCT, Integer.toString(productId), product, user);
			logRemark = "同意调拨";
		} else {
			//自动取消调拨操作
			cancelTransfer(transfer, user);
		}

		//记录日志
		int transId = transactionService.findTransId(MzfEntity.TRANSFER, Integer.toString(transferId), user);
		logService.createLog(transId, MzfEntity.TRANSFER, Integer.toString(transferId), "审核调拨申请", getTargetType().getBizTargetType(), productId, logRemark, user);
		//记录操作日志
		businessLogService.log("审核(商品调拨)", "商品编号：" + productId, user);
		return Integer.parseInt(id);
	}

	private String getRemark(BigDecimal retailBasePrice0, BigDecimal retailBasePrice1,
			BigDecimal wholesalePrice0, BigDecimal wholesalePrice1, IUser user)
			throws BusinessException {
		StringBuffer sb = new StringBuffer();

		List<String> list = new ArrayList<String>();
		if (retailBasePrice0 != null && retailBasePrice1 != null) {
			if (retailBasePrice0.doubleValue() != retailBasePrice1.doubleValue()) {
				list.add("将一口价从" + retailBasePrice1 + "改为了" + retailBasePrice0);
			}
		}
		if (wholesalePrice0 != null && wholesalePrice1 != null) {
			if (wholesalePrice0.doubleValue() != wholesalePrice1.doubleValue()) {
				list.add("将批发价从" + wholesalePrice1 + "改为了" + wholesalePrice0);
			}
		}
		if (list.size() > 0) {
			sb.append(user.getName() + "[" + user.getId() + "]" + StringUtils.join(list.toArray(), ", "));
		}
		return sb.toString();
	}

	protected void send(Map<String, Object> transfer, int targetOrgId, IUser user) throws BusinessException{
		Integer productId = MapUtils.getInteger(transfer, "targetId");
		//从仓库发货
		productInventoryService.send(productId, targetOrgId,MapUtils.getString(transfer,"num"), user);
		//记录操作日志
		businessLogService.log("发货(商品调拨)", "商品编号：" + productId, user);
	}

	public Integer[] sendByStore(Integer[] transferIds, String remark, IUser user) throws BusinessException{
		TransferStatus status =TransferStatus.waitSend;
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.TRANSFER_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), transferIds);
		where.put("targetType", getTargetType());
		where.put("status", status);
		List<Map<String, Object>> transferList = entityService.list(metadata, where, null, user.asSystem());
		if (CollectionUtils.isEmpty(transferList)) {
			throw new BusinessException("请指定" + status.getText() + "的" + getTargetType().getText());
		}

		List<String> numList = new ArrayList<String>();
		for (Map<String, Object> transfer : transferList) {
			Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
			if (targetOrgId == null) {
				numList.add(MapUtils.getString(transfer, "num"));
			}
		}
		if (CollectionUtils.isNotEmpty(numList)) {
			throw new BusinessException("出库单" + numList + "的没有调入部门");
		}

		Map<Integer, List<Integer>> sends = new HashMap<Integer, List<Integer>>();
		for (Map<String, Object> transfer : transferList) {
			Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
			List<Integer> list = sends.get(targetOrgId);
			if (CollectionUtils.isEmpty(list)) {
				list = new ArrayList<Integer>();
			}
			Integer transferId = MapUtils.getInteger(transfer, metadata.getPkCode());
			list.add(transferId);

			sends.put(targetOrgId, list);
			//记录操作日志
			businessLogService.log("确认调拨(商品调拨)", "调拨单号：" + transferId, user);
		}

		List<Integer> dispatchIdList = new ArrayList<Integer>();
		Iterator<Integer> it = sends.keySet().iterator();
		while (it.hasNext()) {
			Integer targetOrgId = it.next();
			List<Integer> list = sends.get(targetOrgId);

			Map<String, Object> dispatch = new HashMap<String, Object>();
			dispatch.put("targetOrgId", targetOrgId);
			dispatch.put("remark", remark);
			Integer dispatchId = this.send(list.toArray(new Integer[]{}), dispatch, user);
			dispatchIdList.add(dispatchId);
		}

		return dispatchIdList.toArray(new Integer[]{});
	}

	public void receive(int transferId, IUser user) throws BusinessException {
		Map<String, Object> transfer = entityService.getById(MzfEntity.TRANSFER_VIEW, transferId, user.asSystem());
		//收货
		int transId = this.receive(transfer, new BigDecimal(1), null, user);
		Integer productId = MapUtils.getInteger(transfer, "targetId");

		//库存收货
		StorageType storageType = productInventoryService.getDefaultStorageType(productId);
		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
		//判断是否是客定裸石(特殊处理的业务)
		boolean isCusNakedDiamond = isCusNakedDiamond(transfer);
		String remark = "调拨单号：" + MapUtils.getString(transfer, "num") + ", 发货单号：" + MapUtils.getString(transfer, "dispatchNum");
		productInventoryService.receive(productId, targetOrgId, storageType, sourceOrgId, remark, isCusNakedDiamond, user);

		boolean isStore = mzfOrgService.isStore(targetOrgId);
		if (isStore) {
			//更新要货申请状态
			Integer demandId = MapUtils.getInteger(transfer, "demandId");
			if (demandId != null) {
				demandService.updateStatus(demandId, DemandStatus.transfering, DemandStatus.over, new StatusCarrier() {
					@Override
					public void active(Map<String, Object> demand) throws BusinessException {
						throw new BusinessException("要货申请[" + this.getNum() + "]状态为" + this.getStatus(DemandStatus.class).getText()+ ", 不能收货");
					}

				}, user);

				logService.createLog(transId, MzfEntity.DEMAND, demandId.toString(), "要货申请完成", getTargetType().getBizTargetType(), productId, "调拨已收货", user);
			}

			Integer orderId = MapUtils.getInteger(transfer, "orderId");
			if (orderId != null) {
				//更新客订单状态
				cusOrderService.receive(orderId, productId, transId, user);
			} else {
				//商品状态改为正常
				productService.free(productId, null, user);
			}
		} else {
			//商品状态改为正常
			productService.free(productId, null, user);
		}

        //处理借货
        boolean isLend = MapUtils.getBoolean(transfer,"isLend",false);
        if(isLend){
            Map<String,Object> productLend = new HashMap<String,Object>();
            productLend.put("productId",productId);
            productLend.put("productNum",MapUtils.getString(transfer,"targetNum"));
            productLend.put("productName",MapUtils.getString(transfer,"targetName"));
            productLend.put("cdate",null);
            productLend.put("cuser",null);
            productLend.put("cuserName",null);
            productLend.put("srcOrgId",MapUtils.getIntValue(transfer,"sourceOrgId"));
            productLend.put("srcOrgName",MapUtils.getString(transfer,"sourceOrgName"));
            productLend.put("tgtOrgId",MapUtils.getIntValue(transfer,"targetOrgId"));
            productLend.put("tgtOrgName",MapUtils.getString(transfer,"targetOrgName"));
            productLend.put("status",lendStatus.lend);
            //创建借货记录
            entityService.create(new EntityCode("productLend"),productLend,user);
        }

        //处理借货归还
        Map<String,Object> value = new HashMap<String,Object>();
        value.put("status",lendStatus.returned);
        value.put("edate",null);
        Map<String,Object> filter = new HashMap<String,Object>();
        filter.put("productId",productId);
        filter.put("srcOrgId",MapUtils.getIntValue(transfer,"targetOrgId"));
        filter.put("tgtOrgId",MapUtils.getIntValue(transfer,"sourceOrgId"));
        filter.put("status",lendStatus.lend);
        entityService.update(new EntityCode("productLend"),value,filter,user);

		//生成结算单
		Map<String, Object> product = productService.get(productId, user);
		String wholesalePrice = MapUtils.getString(product, "wholesalePrice");
		if (sourceOrgId != mzfOrgService.getHQOrgId() && targetOrgId != mzfOrgService.getHQOrgId()) {
			String priorSettlementPrice = MapUtils.getString(transfer, "productPriorSettlementPrice");
			if (StringUtils.isBlank(priorSettlementPrice)) {
				String msg = orgService.getOrgName(sourceOrgId) + "与" + orgService.getOrgName(mzfOrgService.getHQOrgId()) + "的结算价为空，不能生成结算价";
				throw new BusinessException(msg);
			}

			//1.发货方与总部的结算
			settlementService.createForTransfer(SettlementType.transferProduct, sourceOrgId, mzfOrgService.getHQOrgId(), transferId, new BigDecimal(priorSettlementPrice), null, user);

			if (StringUtils.isBlank(wholesalePrice)) {
				String msg = orgService.getOrgName(mzfOrgService.getHQOrgId()) + "与" + orgService.getOrgName(targetOrgId) + "的结算价为空，不能生成结算价";
				throw new BusinessException(msg);
			}
			//2.总部与收货方的的结算
			settlementService.createForTransfer(SettlementType.transferProduct, mzfOrgService.getHQOrgId(), targetOrgId, transferId, new BigDecimal(wholesalePrice), null, user);
		} else {
			if (StringUtils.isBlank(wholesalePrice)) {
				String msg = orgService.getOrgName(sourceOrgId) + "与" + orgService.getOrgName(targetOrgId) + "的结算价为空，不能生成结算价";
				throw new BusinessException(msg);
			}
			settlementService.createForTransfer(SettlementType.transferProduct, sourceOrgId, targetOrgId, transferId, new BigDecimal(wholesalePrice), null, user);
		}
		//记录操作日志
		businessLogService.log("收货(商品调拨)", "调拨单号：" + transferId, user);
	}

	public Map<String, Object> getPrintData(int dispatchId, IUser user) throws BusinessException {
		Map<String, Object> dispatch = entityService.getById(MzfEntity.DISPATCH_VIEW, dispatchId, user);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("dispatchId", dispatchId);
		where.put("ptype", ProductType.pt.toString());
		List<Map<String, Object>> ptList = entityService.list(MzfEntity.TRANSFER_VIEW, where, null, user);
		dispatch.put("ptList", ptList);

		List<Map<String, Object>> whereList = new ArrayList<Map<String,Object>>();
		where = new HashMap<String, Object>();
		where.put(EntityService.FIELD_CODE_KEY, "dispatchId");
		where.put(EntityService.OPERATOR_KEY, Filter.EQ);
		where.put(EntityService.VALUE_KEY, dispatchId);
		whereList.add(where);
		where = new HashMap<String, Object>();
		where.put(EntityService.FIELD_CODE_KEY, "ptype");
		where.put(EntityService.OPERATOR_KEY, Filter.NOT_EQ1);
		where.put(EntityService.VALUE_KEY, ProductType.pt.toString());
		whereList.add(where);
		List<Map<String, Object>> unptList = entityService.list(MzfEntity.TRANSFER_VIEW, whereList, null, user);
		dispatch.put("unptList", unptList);

		return dispatch;
	}

	//发货明细打印
	public Map<String, Object> getSendProductData(Integer[] dispatchIds, IUser user)throws BusinessException{
		Map<String, Object> data = new HashMap<String, Object>();
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", dispatchIds);
		where.put("status", new String[]{"waitReceive","over"});
		//调拨明细
		String ptype = "";
		String orgName = "";
		List<Map<String, Object>> ptList = entityService.list(MzfEntity.TRANSFER_VIEW, where, null, user);
		for (Map<String, Object> map : ptList) {
			Integer targetId = Integer.parseInt(MapUtils.getString(map, "targetId","0"));
			Map<String, Object> product = productService.get(targetId, user);
			ptype = BizCodeService.getBizName("productType", MapUtils.getString(product, "ptype"));
			orgName = MapUtils.getString(map, "targetOrgName");
		    product.put("remark", MapUtils.getString(map, "remark"));
		    product.put("sdate", MapUtils.getObject(map, "sdateStr"));
		    product.put("dispatchNum", MapUtils.getObject(map, "dispatchNum"));

			dataList.add(product);
		}

		data.put("orgName", orgName);
		data.put("pTypeText", ptype);
		data.put("productList", dataList);

		return data;
	}
	//们店回仓入库单
	public Map<String, Object> getStoreBackProductData(int dispatchId, IUser user)throws BusinessException{
		Map<String, Object> data = new HashMap<String, Object>();
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("dispatchId", dispatchId);
		where.put("status", new String[]{"over"});
		//调拨明细
		String cdate = null;
		String dispatchNum = "";
		List<Map<String, Object>> ptList = entityService.list(MzfEntity.TRANSFER_VIEW, where, null, user);
		for (Map<String, Object> map : ptList) {
			Integer targetId = Integer.parseInt(MapUtils.getString(map, "targetId","0"));
			Map<String, Object> product = productService.get(targetId, user);
			dispatchNum = MapUtils.getString(map, "dispatchNum");
			cdate = MapUtils.getString(map, "cdateStr");
			product.put("rdate", MapUtils.getString(map, "rdateStr"));
			product.put("sourceOrgName", MapUtils.getString(map, "sourceOrgName"));
			product.put("goldClassText", BizCodeService.getBizName("goldClass", MapUtils.getString(map, "goldClass","")));
			product.put("ptypeText", BizCodeService.getBizName("productType", MapUtils.getString(map, "ptype","")));
			product.put("pkindText", BizCodeService.getBizName("productKind", MapUtils.getString(map, "pkind","")));
		    product.put("remark", MapUtils.getString(map, "remark"));
		    product.put("dispatchNum", MapUtils.getObject(map, "dispatchNum"));

			dataList.add(product);
		}

		data.put("dispatchNum", dispatchNum);
		data.put("cdate", cdate);
		data.put("productList", dataList);

		return data;
	}

	public void updateTransferStatus(Map<String, Object> where, IUser user)throws BusinessException{
//		Map<String, Object> where = new HashMap<String, Object>();
//		where.put("id", ids);
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("isPrint", "true");
		entityService.update(MzfEntity.TRANSFER, field, where, user);
	}

	//判断该商品是否是客订裸钻
	private boolean isCusNakedDiamond(Map<String, Object> transfer) throws BusinessException {
		//1.是否是裸钻; 2.调拨单与客定关联; 3.是否是从门店调往总部
		Integer productId = MapUtils.getInteger(transfer, "targetId");
		boolean isNakedDiamond = productService.isNakedDiamond(productId);
		if (!isNakedDiamond) {
			return false;
		}

		Integer orderId = MapUtils.getInteger(transfer, "orderId");
		if (orderId == null) {
			return false;
		}

		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");
		if (!mzfOrgService.isStore(sourceOrgId) || !mzfOrgService.isHq(targetOrgId)) {
			return false;
		}

		return true;
	}
}