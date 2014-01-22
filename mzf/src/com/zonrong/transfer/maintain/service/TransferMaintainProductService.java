package com.zonrong.transfer.maintain.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.*;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.ProductInventoryService;
import com.zonrong.maintain.service.MaintainService;
import com.zonrong.maintain.service.MaintainService.ProductSource;
import com.zonrong.transfer.common.service.TransferService;
import org.apache.commons.collections.MapUtils;
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
public class TransferMaintainProductService extends TransferService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private MaintainService maintainService;
	@Resource
	private MzfOrgService mzfOrgService;
	@Resource
	private BusinessLogService businessLogService;

	protected TransferTargetType getTargetType() {
		return TransferTargetType.maintainProduct;
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
			TransferStatus status = TransferStatus.waitSend;;
			Integer sourceOrgId = MapUtils.getInteger(inventory, "orgId");

			Map<String, Object> maintain = maintainService.getMaintainByProductId(productId, user);
			Integer maintainId = MapUtils.getInteger(maintain, "id");
			MaintainStatus maintainStatus = MaintainStatus.valueOf(MapUtils.getString(maintain, "status"));
			if (maintainStatus == MaintainStatus.New) {
				maintainService.updateStatus(maintainId, MaintainStatus.New, MaintainStatus.maintaining, null, user);
			}

			int transId = transactionService.findTransId(MzfEntity.MAINTAIN, maintainId.toString(), user);

			transfer.put("maintainId", maintainId);
			transfer.put("quantity", 1);
			int transferId = createTransfer(getTargetType(), productId, sourceOrgId, targetOrgId, status, transfer, Integer.toString(i + i), transId, user);

			if (targetOrgId != null && status == TransferStatus.waitSend) {
				transferIdList.add(transferId);
			}

//			Integer inventoryId = MapUtils.getInteger(inventory, "inventoryId");
//			if (suserId != null) {
//				inventoryService.updateOwnerId(inventoryId, suserId, user);
//			}
			//记录操作日志
			businessLogService.log("维修库出库", "商品编号为：" + productId, user);
		}

		//系统自动发货
		if (transferIdList.size() > 0) {
			Integer[] transferIds = transferIdList.toArray(new Integer[]{});
			Map<String, Object> dispatch = new HashMap<String, Object>();
			dispatch.put("targetOrgId", targetOrgId);

			this.send(transferIds, dispatch, user);
		}
	}

	protected void send(Map<String, Object> transfer, int targetOrgId, IUser user) throws BusinessException{
		Integer productId = MapUtils.getInteger(transfer, "targetId");
		//从仓库发货
		productInventoryService.sendIgnoreProductStatus(productId, targetOrgId,MapUtils.getString(transfer,"num"), user);
		//记录操作日志
		businessLogService.log("发货(维修调拨)", "调拨单号：" + MapUtils.getInteger(transfer, "id"), user);
	}

	public void receive(int transferId, IUser user) throws BusinessException {
		Map<String, Object> transfer = entityService.getById(MzfEntity.TRANSFER_VIEW, transferId, user.asSystem());
		//收货
		this.receive(transfer, new BigDecimal(1), null, user);
		Integer targetId = MapUtils.getInteger(transfer, "targetId");

		//库存收货
		Integer targetOrgId = MapUtils.getInteger(transfer, "targetOrgId");

		Integer maintainId = MapUtils.getInteger(transfer, "maintainId");
		Map<String, Object> maintain = entityService.getById(MzfEntity.MAINTAIN_VIEW, maintainId, User.getSystemUser());
		Integer maintainOrgId = MapUtils.getInteger(maintain, "orgId");
		ProductSource productSource = ProductSource.valueOf(MapUtils.getString(maintain, "productSource"));

		//已售商品入维修库，未售商品入商品库
		StorageType storageType = StorageType.product_maintain;
		if (targetOrgId.intValue() == maintainOrgId) {
			if (productSource == ProductSource.onStorage) {
				storageType = productInventoryService.getDefaultStorageType(targetId);
			}
		}
		Integer sourceOrgId = MapUtils.getInteger(transfer, "sourceOrgId");
		productInventoryService.receive(targetId, targetOrgId, storageType, sourceOrgId, "商品调拨", false, user);

		boolean isStore = mzfOrgService.isStore(targetOrgId);
		if (isStore && targetOrgId.intValue() == maintainOrgId) {
			//将商品放入默认柜台
			if (productSource == ProductSource.onStorage) {
				maintainService.updateStatus(maintainId, MaintainStatus.maintaining, MaintainStatus.over, new StatusCarrier(){
					@Override
					public void active(Map<String, Object> carrier) throws BusinessException {
						throw new BusinessException("维修单[" + this.getNum() + "]状态为" + this.getStatus(MaintainStatus.class).getText() + "，不能收货");
					}

				}, user);
			} else {
				//更新维修单状态
				maintainService.recieveProduct(maintainId, user);
			}
		}
		//记录操作日志
		businessLogService.log("收货(维修调拨)", "调拨单号：" + transferId, user);
	}

	@Override
	public void cancelTransfer(Map<String, Object> transfer, IUser user) throws BusinessException {
		super.cancelTransfer(transfer, user);

		Integer productId = MapUtils.getInteger(transfer, "targetId");

		Map<String, Object> inventroy = productInventoryService.getProductInventory(productId, user.getOrgId());
		Integer inventoryId = MapUtils.getInteger(inventroy, "inventoryId");
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", InventoryStatus.onStorage);
		entityService.updateById(MzfEntity.INVENTORY, inventoryId.toString(), field, user);
		//记录操作日志
		businessLogService.log("取消调拨(维修调拨)", "调拨单号：" + MapUtils.getInteger(transfer, "id"), user);
	}
}