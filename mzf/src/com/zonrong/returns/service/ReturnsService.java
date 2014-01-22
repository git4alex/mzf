package com.zonrong.returns.service;

import com.zonrong.basics.chit.service.ChitService;
import com.zonrong.basics.customer.service.CustomerService;
import com.zonrong.basics.material.service.MaterialService;
import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.*;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.common.utils.MzfUtils.BillPrefix;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.ProductInventoryService;
import com.zonrong.inventory.service.SecondProductInventoryService;
import com.zonrong.inventory.service.MaterialInventoryService;
import com.zonrong.inventory.service.RawmaterialInventoryService.GoldClass;
import com.zonrong.inventory.service.SecondGoldInventoryService;
import com.zonrong.inventory.service.TreasuryService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.sale.service.SaleService;
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
 * date: 2011-3-9
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ReturnsService{
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private MaterialInventoryService materialInventoryService;
	@Resource
	private SecondGoldInventoryService secondGoldInventoryService;
	@Resource
	private SecondProductInventoryService secondProductInventoryService;
	@Resource
	private ProductService productService;
	@Resource
	private CustomerService customerService;
	@Resource
	private SaleService saleService;
	@Resource
	private MaterialService materialService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;
	@Resource
	private ChitService chitService;

	private void check(Map<String, Object> sale, List<Map<String, Object>> detailList) throws BusinessException{

	}
	public int createReturns(Map<String, Object> returns, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		check(returns, detailList);
		//新建销售单
		returns.put("type", SaleType.returns);
		int returnsId = createBill(returns, detailList, user);
		String num = MapUtils.getString(returns, "num");

		int transId = transactionService.createTransId();
		Integer orgId = MapUtils.getInteger(returns, "orgId", 0);
		//商品出库
		for (Map<String, Object> detail : detailList) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			Integer targetId = MapUtils.getInteger(detail, "targetId");

			String remark = "退货单号：[" + num+"]";
			if (type == SaleDetailType.product) {
				Map<String, Object> product = productService.get(targetId, user);
				ProductType ptype = ProductType.valueOf(MapUtils.getString(product, "ptype"));
				StorageType storageType = ptype.getStorageType();
				//更新商品状态
				productService.updateStatus(targetId, ProductStatus.free, remark, null, user);
				//商品入库
				productInventoryService.warehouse(MzfEnum.BizType.returned, targetId, orgId, storageType, orgId, remark, user);

				//记录流程
				logService.createLog(transId, MzfEntity.SALE, Integer.toString(returnsId), "新建退货单", TargetType.product, targetId, remark, user);
			} else if (type == SaleDetailType.material) {
				BigDecimal quantity = new BigDecimal(MapUtils.getString(detail, "quantity"));
				BigDecimal cost = new BigDecimal(MapUtils.getString(detail, "price"));
				materialInventoryService.warehouse(MzfEnum.BizType.returned,orgId, targetId, quantity, cost, "价格", remark, user);
				materialService.addCost(targetId, cost, orgId, user);
			} else if (type == SaleDetailType.secondGold) {
				GoldClass goldClass = GoldClass.valueOf(MapUtils.getString(detail, "goldClass"));
				BigDecimal quantity = new BigDecimal(MapUtils.getString(detail, "goldWeight"));
				BigDecimal cost = new BigDecimal(MapUtils.getString(detail, "price"));
				secondGoldInventoryService.delivery(MzfEnum.BizType.returned, goldClass, orgId, quantity, cost, "价格", remark, user);
			} else if (type == SaleDetailType.secondJewel) {
				//商品标记为未回收
				Map<String, Object> field = new HashMap<String, Object>();
				field.put("isReturn", 0);
				Map<String, Object> where = new HashMap<String, Object>();
				where.put("num", MapUtils.getString(detail, "targetNum"));
				entityService.update(MzfEntity.PRODUCT, field, where, user);
				//旧饰出库
				secondProductInventoryService.deliveryBySecondProductId(MzfEnum.BizType.returned, targetId, remark, InventoryStatus.onStorage, user);
			}else if(type == SaleDetailType.returnsChit){
				//退还回收的代金券
				chitService.returnBackChit(targetId, user);
			}else if(type == SaleDetailType.genChit){
				//退回已销售的代金券
				chitService.backSellChit(targetId, user);
			} else {
				throw new BusinessException("未指定销售类型");
			}
		}

		//货款入库
		saleService.warehouseMoney(TreasuryService.BizType.returns, returnsId, returns, true, user);

		//更新客户积分
		Integer cusId = MapUtils.getInteger(returns, "cusId");
		//String points = MapUtils.getString(returns, "points");
		int points = MapUtils.getIntValue(returns, "points", 0);
		int exchangePoints = MapUtils.getIntValue(returns, "exchangePoints",0);
	    customerService.subtractPoints(cusId, points*(-1), exchangePoints, user, num);


		//如果销售单已经全部退货，做标志
		int saleId = MapUtils.getInteger(returns, "saleId");
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("saleId", saleId);
		where.put("isReturns", Boolean.toString(false));
		List<Map<String, Object>> list = entityService.list(MzfEntity.SALE_DETAIL_VIEW, where, null, user.asSystem());
		if (CollectionUtils.isEmpty(list)) {
			Map<String, Object> field = new HashMap<String, Object>();
			field.put("isReturns", Boolean.toString(true));
			entityService.updateById(MzfEntity.SALE, Integer.toString(saleId), field, user);
		}

		//建立客户与该机构业务关联关系
		customerService.createOrgRel(cusId, TreasuryService.BizType.returns, returnsId, user);
		//记录操作日志
		businessLogService.log("新增退货单", "退货单号为:" + num, user);
		return returnsId;
	}

	private int createBill(Map<String, Object> returns, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		String num = MzfUtils.getBillNum(BillPrefix.TH, user);
		returns.put("num", num);
		//returns.put("orgId", user.getOrgId());
		//returns.put("orgName", user.getOrgName());
		returns.put("cuserId", null);
		returns.put("cuserName", null);
		returns.put("cdate", null);
		String id = entityService.create(MzfEntity.SALE, returns, user);
		int saleId = Integer.parseInt(id);

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RETURNS_DETAIL);
		for (Map<String, Object> detail : detailList) {
			Integer saleDetailId = MapUtils.getInteger(detail, "id");
			detail.put("saleDetailId", saleDetailId);
			detail.put("saleId", saleId);
			detail.remove(metadata.getPkCode());
			entityService.create(metadata, detail, user);
		}

		return saleId;
	}
}


