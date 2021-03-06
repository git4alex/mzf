package com.zonrong.salerule.service;

import com.zonrong.basics.chit.service.ChitService;
import com.zonrong.basics.customer.service.CustomerService;
import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.*;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.common.utils.MzfUtils.BillPrefix;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.cusorder.service.CusOrderService;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.inventory.product.service.SecondProductInventoryService;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.inventory.service.MaterialInventoryService;
import com.zonrong.inventory.service.RawmaterialInventoryService.GoldClass;
import com.zonrong.inventory.service.SecondGoldInventoryService;
import com.zonrong.inventory.treasury.service.TreasuryEarnestService;
import com.zonrong.inventory.treasury.service.TreasurySaleService;
import com.zonrong.inventory.treasury.service.TreasuryService.MoneyStorageClass1;
import com.zonrong.maintain.service.MaintainService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.BizCodeService;
import com.zonrong.util.TpltUtils;
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
@Service("saleService1")
public class SaleService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private TreasurySaleService treaaurySaleService;
	@Resource
	private CusOrderService cusOrderService;
	@Resource
	private ProductService productService;
	@Resource
	private CustomerService customerService;
	@Resource
	private MaterialInventoryService materialInventoryService;
	@Resource
	private TreasuryEarnestService treasuryEarnestService;
	@Resource
	private SecondGoldInventoryService secondGoldInventoryService;
	@Resource
	private SecondProductInventoryService secondProductInventoryService;
	@Resource
	private MaintainService maintainService;
	@Resource
	private ChitService chitService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	public enum SaleOrderStatus {
		waitPay,
		over
	}

	private void check(Map<String, Object> sale, List<Map<String, Object>> detailList, IUser user) throws BusinessException{
		List<SaleDetailType> list = new ArrayList<SaleDetailType>();
		Map<SaleDetailType, Object> map = new HashMap<SaleDetailType, Object>();
//		for (Map<String, Object> detail : detailList) {
//			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
//			map.put(type, null);
//		}

		String error = "实收金额必须大于0";
		try {
			Float amount = MapUtils.getFloat(sale, "amount");
			if (amount < 0) {
				throw new BusinessException(error);
			}
		} catch (Exception e) {
			throw new BusinessException(error);
		}
//		if (map.containsKey(SaleDetailType.secondGold)) {
//			if (!map.containsKey(SaleDetailType.product)) {
//				throw new BusinessException("旧金回收必须和商品销售一起进行");
//			}
//		}
//		if (map.containsKey(SaleDetailType.secondJewel)) {
//			if (!map.containsKey(SaleDetailType.product)) {
//				throw new BusinessException("旧饰回收必须和商品销售一起进行");
//			}
//		}

		Set<String> numSet = new HashSet<String>();
		int x = 0;
		for (Map<String, Object> detail : detailList) {
			String num = MapUtils.getString(detail, "targetNum");
			if (StringUtils.isNotBlank(num)) {
				numSet.add(num);
				x++;
			}
		}
		if (x > numSet.size()) {
			throw new BusinessException("销售列表中有重复的商品");
		}
		//验证积分
		int cusId = MapUtils.getIntValue(sale, "cusId",0);
		Map<String, Object> customer = customerService.getCustomerById(cusId, user);
		float saleExchangePoints = MapUtils.getFloatValue(sale, "exchangePoints",0);
		if(customer != null && saleExchangePoints > 0){
			float points = MapUtils.getFloatValue(customer, "points",0);
			//float exchangePoints = MapUtils.getFloatValue(customer, "exchangePoints",0);
			if(points < saleExchangePoints){
				throw new BusinessException("客户积分不足不能兑换物料");
			}
		}


	}

	public int createSale(Map<String, Object> sale, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		check(sale, detailList, user);
		//新建销售单
		sale.put("type", SaleType.sale);
		 //销售单中是否有铂金
//		boolean isPtProduct = false;
//		for (Map<String, Object> detail : detailList) {
//			String ptype = MapUtils.getString(detail, "ptype");
//			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
//			if(type == SaleDetailType.product && ProductType.pt.toString().equals(ptype)){
//				isPtProduct = true;
//				break;
//			}
//		}
//		//自动积分
//		if(!isPtProduct){
//			calcPoints(sale, detailList);
//		}


		int saleId = createBill(sale, detailList, user);
		String num = MapUtils.getString(sale, "num");

		Integer cusId = MapUtils.getInteger(sale, "cusId");

		int transId = transactionService.createTransId();

		//商品出库
		for (Map<String, Object> detail : detailList) {
			    SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			    Integer targetId = MapUtils.getInteger(detail, "targetId");
				String ptype = MapUtils.getString(detail, "ptype");

			if(ProductType.pt.toString().equals(ptype) && type == SaleDetailType.product){
				BigDecimal weight = new BigDecimal(MapUtils.getString(detail,  "weight", MapUtils.getString(detail, "goldWeight")));
				BigDecimal goldPrice = new BigDecimal(MapUtils.getString(detail, "goldPrice"));
				BigDecimal ptPrice = weight.multiply(goldPrice);
				productService.updatePTProductPrice(targetId, ptPrice, user);

			}

			if (type == SaleDetailType.product) {
				sellProduct(targetId, detail, saleId, transId,num, user);
			} else if (type == SaleDetailType.material) {
				int exchangePoints = MapUtils.getIntValue(detail, "exchangePoints", 0);
				 //物料兑换记录
				if(exchangePoints > 0){
					String remark = "销售单号：" + num + ";物料条码：" + MapUtils.getString(detail,"targetNum");
					customerService.createPointLog(cusId, CustomerPointsType.exchangePoints, exchangePoints,remark,user);
				}
				sellMaterial(num, targetId, detail, user);
			} else if (type == SaleDetailType.secondGold) {
				sellSecondGold(num, targetId, detail, user);
			} else if (type == SaleDetailType.secondJewel) {
				//商品标记为回收
				Map<String, Object> field = new HashMap<String, Object>();
				field.put("isReturn", 1);
				Map<String, Object> where = new HashMap<String, Object>();
				where.put("num", MapUtils.getString(detail, "targetNum"));
				entityService.update(MzfEntity.PRODUCT, field, where, user);
				sellSecondProduct(targetId, detail, user);
			} else if (type == SaleDetailType.genChit) {
				sellChit(targetId, detail, user);
			} else if (type == SaleDetailType.returnsChit) {
				returnsChit(targetId, saleId, detail, user);
			} else {
				throw new BusinessException("未指定销售类型");
			}
		}

		//货款入库
		warehouseMoney(com.zonrong.inventory.treasury.service.TreasuryService.BizType.sell, saleId, sale, false, user);

		//更新客户积分
		//String points = MapUtils.getString(sale, "points","0");
		int points = MapUtils.getIntValue(sale, "points", 0);
		int exchangePoints = MapUtils.getIntValue(sale, "exchangePoints", 0);
        int payPoints = MapUtils.getIntValue(sale,"payPoints",0);//抵现积分
		if(exchangePoints>0 || points>0 || payPoints>0){
			customerService.updatePoints(cusId, exchangePoints, points, payPoints,user, num);
		}
		//BigDecimal exchangePoints = new BigDecimal(exchangePointsStr);

		//建立客户与该机构业务关联关系
		customerService.createOrgRel(cusId, com.zonrong.inventory.treasury.service.TreasuryService.BizType.sell, saleId, user);
		//记录操作日志
		businessLogService.log("新开销售单", "销售单号为:" + num, user);
		return saleId;
	}

	private void sellProduct(Integer productId, Map<String, Object> detail, int saleId, int transId, String saleNum,IUser user) throws BusinessException {
		productInventoryService.deliveryByProductId(BizType.sell, productId, "商品销售", InventoryStatus.onStorage, user);
		productService.updateStatus(productId, ProductStatus.selled, "商品销售", null, user);

		//记录商品最终一口价和最终销售价
		BigDecimal finalPrice = new BigDecimal(MapUtils.getString(detail, "price"));
		BigDecimal discount = new BigDecimal(MapUtils.getString(detail, "totalDiscount", new Integer(0).toString()));
		BigDecimal finalSelledPrice = finalPrice.subtract(discount);
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("finalPrice", finalPrice);
		field.put("finalSelledPrice", finalSelledPrice);
		entityService.updateById(MzfEntity.PRODUCT, productId.toString(), field, user);

		Integer orderId = MapUtils.getInteger(detail, "orderId");
		if (orderId != null) {
			cusOrderService.finishCusOrderOnSell(orderId, user);
		}

		//记录流程
		logService.createLog(transId, MzfEntity.SALE, Integer.toString(saleId), "新建销售单", TargetType.product, productId, "销售,销售单号为： "+saleNum, user);
	}
	private void sellMaterial(String saleNum, Integer materialId, Map<String, Object> detail, IUser user) throws BusinessException {
		BigDecimal quantity = new BigDecimal(MapUtils.getString(detail, "quantity"));
		String remark = "商品销售， 销售单号：" + saleNum;
		BigDecimal cost = new BigDecimal(MapUtils.getString(detail, "price"));
		materialInventoryService.deliveryByQuantity(BizType.sell, materialId, quantity, cost, "价格", user.getOrgId(), false, remark, user);
	}
	private void sellSecondGold(String saleNum, Integer targetId, Map<String, Object> detail, IUser user) throws BusinessException {
		GoldClass goldClass = GoldClass.valueOf(MapUtils.getString(detail, "goldClass"));
		BigDecimal quantity = new BigDecimal(MapUtils.getString(detail, "goldWeight"));
		BigDecimal cost = new BigDecimal(MapUtils.getString(detail, "price"));
		String goldPrice = MapUtils.getString(detail, "goldPrice");
		String remark = "旧金回收，金价：" + goldPrice + "， 销售单号：" + saleNum;
		secondGoldInventoryService.warehouse(BizType.buySecondGold, goldClass, quantity, cost, remark, user);
	}
	private void sellSecondProduct(Integer secondProductId, Map<String, Object> detail, IUser user) throws BusinessException {
		int orgId = user.getOrgId();
		secondProductInventoryService.warehouse(BizType.buySecondProduct, secondProductId, orgId, null, orgId, "旧饰回收", user);
	}

	private void sellChit(Integer chitId, Map<String, Object> detail, IUser user) throws BusinessException {
		chitService.sellChit(chitId,user);
	}

	private void returnsChit(Integer chitId, Integer saleId, Map<String, Object> detail, IUser user) throws BusinessException {
		chitService.returnsChit(chitId, saleId, user);
	}

	private int createBill(Map<String, Object> sale, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		int cusId = MapUtils.getIntValue(sale, "cusId");
		String num = MzfUtils.getBillNum(BillPrefix.XS, user);
		sale.put("num", num);
		sale.put("orgId", user.getOrgId());
		sale.put("orgName", user.getOrgName());
		sale.put("isNewCus", isNewCustomer(cusId).toString());
		sale.put("cuserId", null);
		sale.put("cuserName", null);
		sale.put("cdate", null);
		String id = entityService.create(MzfEntity.SALE, sale, user);
		int saleId = Integer.parseInt(id);

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SALE_DETAIL);
		for (Map<String, Object> detail : detailList) {
			detail.remove(metadata.getPkCode());
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (SaleDetailType.secondJewel == type) {
				Integer productId = MapUtils.getInteger(detail, "targetId");
				String productNum = MapUtils.getString(detail, "targetNum");
				String productName = MapUtils.getString(detail, "targetName");

				  //记录旧饰回收日志
				if(productId != null){
					int transId = transactionService.createTransId();
					logService.createLog(transId, MzfEntity.PRODUCT, productId+"", "旧饰回收", TargetType.product, productId, "销售单号：" + num , user);
				}
				//如果是旧饰回收，要记下旧饰信息
				Map<String, Object> field = new HashMap<String, Object>(detail);
				if (productId != null) {
					Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, productId, user);
					product.remove("id");
					field = new HashMap<String, Object>(product);
				} else {
					field.put("num", productNum);
					field.put("name", productName);
				}
				field.put("buyPrice", MapUtils.getObject(detail, "price"));
				field.put("status", ProductStatus.free);

				EntityMetadata spMetadata = metadataProvider.getEntityMetadata(MzfEntity.SECOND_PRODUCT);
				String secondProductId = null;
				if (productId != null) {
					Map<String, Object> w = new HashMap<String, Object>();
					w.put("num", productNum);
					OrderBy orderBy = new OrderBy(new String[]{spMetadata.getPkCode()}, OrderByDir.asc);
					List<Map<String, Object>> spList = entityService.list(spMetadata, w, orderBy, user.asSystem());
					if (CollectionUtils.isEmpty(spList)) {
						secondProductId = entityService.create(spMetadata, field, user);
					} else {
						Map<String, Object> sp = spList.get(0);
						secondProductId = MapUtils.getString(sp, spMetadata.getPkCode());

						entityService.updateById(spMetadata, secondProductId, field, user);
					}
				} else {
					secondProductId = entityService.create(spMetadata, field, user);
					//重新生成条码
					String spNum = productService.generateProductNum(spMetadata, Integer.parseInt(secondProductId), "J");
					field = new HashMap<String, Object>();
					field.put("num", spNum);
					entityService.updateById(spMetadata, secondProductId, field, user);
				}
				detail.put("targetId", secondProductId);
			}
			detail.put("saleId", saleId);
			entityService.create(metadata, detail, user);
		}

		return saleId;
	}

	public void warehouseMoney(com.zonrong.inventory.treasury.service.TreasuryService.BizType bizType, int targetId, Map<String, Object> sale, boolean isReturns, IUser user) throws BusinessException {
		IEntityCode targetCode = MzfEntity.SALE;

		String remark = bizType.getName() + " 单号:" + MapUtils.getString(sale, "num");
        Integer orgId = MapUtils.getInteger(sale, "orgId", 0);
		for (int i = 1; i <= 3; i++) {
			warehouseMoneyForDetail(bizType, sale, MoneyStorageClass1.bankCard,
					"bankCard" + i, MoneyStorageClass1.bankCard.getName() + "付款金额", "bankCardBank" + i, MoneyStorageClass1.bankCard.getName() + "的支付银行", targetCode, targetId, remark, isReturns, user);

			warehouseMoneyForDetail(bizType, sale, MoneyStorageClass1.valueCard,
					"valueCard" + i, MoneyStorageClass1.valueCard.getName() + "付款金额", "valueCardType" + i, MoneyStorageClass1.valueCard.getName() + "的支付类型", targetCode, targetId, remark, isReturns, user);

			warehouseMoneyForDetail(bizType, sale, MoneyStorageClass1.coBrandedCard,
					"coBrandedCard" + i, MoneyStorageClass1.coBrandedCard.getName() + "付款金额", "coBrandedCardBank" + i, MoneyStorageClass1.coBrandedCard.getName() + "的支付银行", targetCode, targetId, remark, isReturns, user);

			warehouseMoneyForDetail(bizType, sale, MoneyStorageClass1.foreignCard,
					"foreignCard" + i, MoneyStorageClass1.foreignCard.getName() + "付款金额", "foreignCardType" + i, MoneyStorageClass1.foreignCard.getName() + "的支付类型", targetCode, targetId, remark, isReturns, user);
		}

		String cash = MapUtils.getString(sale, "cash");
		if (StringUtils.isNotBlank(cash)) {
			BigDecimal money = new BigDecimal(cash);
			if (isReturns) {
				treaaurySaleService.delivery(bizType, orgId, money, MoneyStorageClass1.cash, null, false, targetCode, targetId, remark, user);
			} else {
				treaaurySaleService.warehouse(bizType, orgId, money, MoneyStorageClass1.cash, null, false, targetCode, targetId, remark, user);
			}
		}
		String transfer = MapUtils.getString(sale, "transfer");
		if (StringUtils.isNotBlank(transfer)) {
			BigDecimal money = new BigDecimal(transfer);
			if (isReturns) {
				treaaurySaleService.delivery(bizType, orgId, money, MoneyStorageClass1.transfer, null, false, targetCode, targetId, remark, user);
			} else {
				treaaurySaleService.warehouse(bizType, orgId, money, MoneyStorageClass1.transfer, null, false, targetCode, targetId, remark, user);
			}
		}
		String chit = MapUtils.getString(sale, "chit");
		if (StringUtils.isNotBlank(chit)) {
			BigDecimal money = new BigDecimal(chit);
			if (isReturns) {
				treaaurySaleService.delivery(bizType, orgId, money, MoneyStorageClass1.chit, null, false, targetCode, targetId, remark, user);
			} else {
				treaaurySaleService.warehouse(bizType, orgId, money, MoneyStorageClass1.chit, null, false, targetCode, targetId, remark, user);
			}
		}
		String other = MapUtils.getString(sale, "other");
		if (StringUtils.isNotBlank(other)) {
			BigDecimal money = new BigDecimal(other);
			if (isReturns) {
				treaaurySaleService.delivery(bizType, orgId, money, MoneyStorageClass1.other, null, false, targetCode, targetId, remark, user);
			} else {
				treaaurySaleService.warehouse(bizType, orgId, money, MoneyStorageClass1.other, null, false, targetCode, targetId, remark, user);
			}
		}
	}

	private void warehouseMoneyForDetail(
			com.zonrong.inventory.treasury.service.TreasuryService.BizType bizType, Map<String, Object> sale,
			MoneyStorageClass1 class1,
			String amountKey, String amountDesc,
			String typekey, String typeDesc,
			IEntityCode targetCode, int targetId,
			String remark,  boolean isReturns,
			IUser user)
			throws BusinessException {
		String bankCard = MapUtils.getString(sale, amountKey);
		String class2 = MapUtils.getString(sale, typekey);
        Integer orgId = MapUtils.getInteger(sale, "orgId", 0);
		if (StringUtils.isNotBlank(bankCard) && StringUtils.isNotBlank(class2)) {
			BigDecimal money = new BigDecimal(bankCard);
			if (isReturns) {
				treaaurySaleService.delivery(bizType, orgId, money, class1, class2, false, targetCode, targetId, remark, user);
			} else {
				treaaurySaleService.warehouse(bizType, orgId, money, class1, class2, false, targetCode, targetId, remark, user);
			}
		} else if (StringUtils.isNotBlank(bankCard) && StringUtils.isBlank(class2)) {
			throw new BusinessException("指定了" + amountDesc + "，但未指定" + typeDesc);
		} else if (StringUtils.isBlank(bankCard) && StringUtils.isNotBlank(class2)) {
			throw new BusinessException("指定了" + typeDesc + "，但未指定" + amountDesc );
		} else {
			return;
		}
	}

	public int saveMarketProxy(int saleId, Map<String, Object> marketProxy, IUser user) throws BusinessException {
		return 0;
	}

	//审核销售单
	public void approveSale(int saleId, Map<String, Object> approve, IUser user) throws BusinessException{
		entityService.updateById(MzfEntity.SALE, Integer.toString(saleId), approve, user);
		String approveRemark = MapUtils.getString(approve, "approveRemark");
		//记录流程
		int transId = transactionService.findTransId(MzfEntity.SALE, Integer.toString(saleId), user);
		logService.createLog(transId, MzfEntity.SALE, Integer.toString(saleId), "审核销售单", null, null, approveRemark, user);
	}

	public Map<String, Object> getPrintData(int saleId, IUser user) throws BusinessException {
		Map<String, Object> sale = entityService.getById(MzfEntity.SALE_VIEW, saleId, user);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("saleId", saleId);
		List<Map<String, Object>> detailList = entityService.list(MzfEntity.SALE_DETAIL_VIEW, where, null, user);
		Map<String,Object> cusOrder = new HashMap<String,Object>(); //订单

		for (Map<String, Object> detail : detailList) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (type == SaleDetailType.product) {
//				Integer productId = MapUtils.getInteger(detail, "targetId");
//				Map<String, Object> product = productService.get(productId, user);
//				String remark = MapUtils.getString(detail, "remark");
//				detail.putAll(product);
//				detail.put("remark", remark);
				 //获得促销类型的text
				String onsaleTypeValue = MapUtils.getString(detail, "onsaleType");
				detail.put("onsaleTypeText", BizCodeService.getBizName("onsaleType", onsaleTypeValue));

				//根据订单编号获取订单
				cusOrder = getCusOrderById(MapUtils.getString(detail, "orderId"));
			}

			if (type == SaleDetailType.secondJewel) {
				//针对旧饰回收做特殊处理
				try {
					String productNum = MapUtils.getString(detail, "targetNum");
					if (StringUtils.isNotBlank(productNum)) {
						Map<String, Object> product = productService.findAllByProductNum(productNum, user);
						ProductType productType = ProductType.valueOf(MapUtils.getString(product, "ptype"));
						if (productType == ProductType.diamond) {
							float retailBasePrice = MapUtils.getFloat(product, "retailBasePrice");
							float price = MapUtils.getFloat(detail, "price");
							detail.put("price", retailBasePrice);
							detail.put("totalDiscount", retailBasePrice - price);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage(), e);
				}
			}

            detail.put("typeText", type.getText());
            String goldClass = MapUtils.getString(detail, "goldClass");
			detail.put("goldClassText", null);
			if (StringUtils.isNotBlank(goldClass)) {
				GoldClass clazz = GoldClass.valueOf(goldClass);
				detail.put("goldClassText", clazz.getText());
			}
		}

		List<Map<String, Object>> tempDetailList = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> detail : detailList) {
			String certificateCode = MapUtils.getString(detail, "certificateCode");
			String onsaleTypeText = MapUtils.getString(detail, "onsaleTypeText");
			String remark = MapUtils.getString(detail, "remark");

			if (StringUtils.isNotBlank(certificateCode) ||
					StringUtils.isNotBlank(onsaleTypeText) ||
					StringUtils.isNotBlank(remark)) {
				Map<String, Object> d = new HashMap<String, Object>();
				d.put("targetNum", MapUtils.getString(detail, "targetNum"));
				d.put("certificateCode", certificateCode);
				d.put("onsaleTypeText", onsaleTypeText);
				d.put("remark", remark);
				tempDetailList.add(d);
			}
		}

		List<Map<String, Object>> remarkDetailList = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < tempDetailList.size(); i = i + 2) {
			Map<String, Object> detail1 = tempDetailList.get(i);
			Map<String, Object> d = new HashMap<String, Object>();
			d.put("targetNum1", MapUtils.getString(detail1, "targetNum"));
			d.put("certificateCode1", MapUtils.getString(detail1, "certificateCode"));
			d.put("onsaleTypeText1", MapUtils.getString(detail1, "onsaleTypeText"));
			d.put("remark1", MapUtils.getString(detail1, "remark"));
			if (i + 1 < tempDetailList.size()) {
				Map<String, Object> detail2 = tempDetailList.get(i + 1);
				d.put("targetNum2", MapUtils.getString(detail2, "targetNum"));
				d.put("certificateCode2", MapUtils.getString(detail2, "certificateCode"));
				d.put("onsaleTypeText2", MapUtils.getString(detail2, "onsaleTypeText"));
				d.put("remark2", MapUtils.getString(detail2, "remark"));
			}
			remarkDetailList.add(d);
		}

		Integer customerId = MapUtils.getInteger(sale, "cusId");
		if (customerId != null) {
			String grade = customerService.findCustomerGrade(customerId); //客户级别
			grade = BizCodeService.getBizName("cusGrade", grade);
			sale.put("gradeText", grade);
		}
		if(cusOrder != null){
			sale.put("orderCode", cusOrder.get("num"));
			sale.put("orderDate", cusOrder.get("cdate"));
		}else{
			sale.put("orderCode", null);
			sale.put("orderDate", null);
		}
        String amountText = "";
        if(sale.get("amount") != null){
        	 String amount = MapUtils.getString(sale, "amount");
        	 amountText = TpltUtils.convertDigits(amount);
        }
        //商场代收
        sale.put("cash", MapUtils.getFloatValue(sale, "cash", 0) + MapUtils.getFloatValue(sale, "marketProxyCash", 0));
        sale.put("bankCard", MapUtils.getFloatValue(sale, "bankCard", 0) + MapUtils.getFloatValue(sale, "marketProxyBankCard", 0));
        sale.put("valueCard", MapUtils.getFloatValue(sale, "valueCard", 0) + MapUtils.getFloatValue(sale, "marketProxyValueCard", 0));
        sale.put("chit", MapUtils.getFloatValue(sale, "chit", 0) + MapUtils.getFloatValue(sale, "marketProxyChit", 0));
        sale.put("other", MapUtils.getFloatValue(sale, "other", 0) + MapUtils.getFloatValue(sale, "marketProxyOther", 0));

        sale.put("amountText", amountText);
		sale.put("detailList", detailList);
		sale.put("remarkDetailList", remarkDetailList);
		return sale;
	}

	//根据商品编号获得销售单号
	public int getSaleIdByProductId(int productId, IUser user) throws BusinessException{
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("targetId", productId);
		where.put("type", "product");
		List<Map<String, Object>> saleDetails = entityService.list(MzfEntity.SALE_DETAIL, where, null, user);
		List<Integer> saleIds = new ArrayList<Integer>();
		for (Map detail : saleDetails) {
			saleIds.add(MapUtils.getInteger(detail, "saleId"));
		}

		if(saleIds.size() > 0){
			Map<String, Object> filter = new HashMap<String, Object>();
			filter.put("id", saleIds.toArray(new Integer[]{}));
			OrderBy orderBy = new OrderBy(new String[]{"id"}, OrderByDir.desc);
			List<Map<String, Object>> sales = entityService.list(MzfEntity.SALE_VIEW, filter, orderBy, user);
			if(CollectionUtils.isNotEmpty(sales)){
				return MapUtils.getIntValue(sales.get(0), "id");
			}
		}else{
			throw new BusinessException("该旧饰无对应的销售单");
		}
		return 0;
	}
	private Map<String,Object> getCusOrderById(String orderId) throws BusinessException {
		if(orderId != null){
			Map<String,Object> cusOrder = entityService.getById(MzfEntity.CUS_ORDER, Integer.parseInt(orderId), User.getSystemUser());
			return cusOrder;
		}
		return null;
	}

	private Boolean isNewCustomer(int cusId) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("cusId", cusId);
		List<Map<String, Object>> list = entityService.list(MzfEntity.SALE_VIEW, where, null, User.getSystemUser());
		if (CollectionUtils.isNotEmpty(list)) {
			return false;
		}
		return true;
	}

	//按照实收计算总积分, 为每一件商品计算积分
	private void calcPoints(Map<String, Object> bill, List<Map<String, Object>> details) throws BusinessException {
		//实收 = 应收 - 定金支付 - 旧金支付 - 旧饰支付 - 折扣
		BigDecimal totalAmount = new BigDecimal(MapUtils.getDoubleValue(bill, "totalAmount", 0.0));
		BigDecimal frontMoney = new BigDecimal(MapUtils.getDoubleValue(bill, "frontMoney", 0.0));
		BigDecimal goldPay = new BigDecimal(MapUtils.getDoubleValue(bill, "goldPay", 0.0));
		BigDecimal productPay = new BigDecimal(MapUtils.getDoubleValue(bill, "productPay", 0.0));
		BigDecimal discount = new BigDecimal(MapUtils.getDoubleValue(bill, "discount", 0.0));
		BigDecimal pointsAmount = totalAmount.subtract(frontMoney).subtract(goldPay).subtract(productPay).subtract(discount);

		BigDecimal totalWeightPrice = new BigDecimal(0);
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (type == SaleDetailType.product) {
				Integer productId = MapUtils.getInteger(detail, "targetId");
				boolean isBargains = productService.isBargains(productId);
				boolean isAuthorityDiscount = productService.isAuthorityDiscount(detail);
				if (isBargains || isAuthorityDiscount) {
					BigDecimal price = new BigDecimal(MapUtils.getString(detail, "price", Integer.toString(0)));
					BigDecimal totalDiscount = new BigDecimal(MapUtils.getString(detail, "totalDiscount", Integer.toString(0)));

					totalWeightPrice = totalWeightPrice.add(price).subtract(totalDiscount);
				}
				detail.put("isBargains", isBargains);
				detail.put("isAuthorityDiscount", isAuthorityDiscount);
			}
		}
		pointsAmount = pointsAmount.subtract(totalWeightPrice);

		//计算总积分
		int totalPoints = pointsAmount.divide(new BigDecimal(500), 0, BigDecimal.ROUND_FLOOR).intValue();

		//为每一件商品加权平均积分， 每件商品的权重 = 一口价 - 折扣
		BigDecimal total = new BigDecimal(0);
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (type == SaleDetailType.product) {
				if (!MapUtils.getBooleanValue(detail, "isBargains") && !MapUtils.getBooleanValue(detail, "isAuthorityDiscount")) {
					BigDecimal price = new BigDecimal(MapUtils.getString(detail, "price", Integer.toString(0)));
					BigDecimal totalDiscount = new BigDecimal(MapUtils.getString(detail, "totalDiscount", Integer.toString(0)));

					BigDecimal weightPrice = price.subtract(totalDiscount);
					total = total.add(weightPrice);
				}
			}
		}
		if(total.intValue() == 0){
			return;
		}
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (type == SaleDetailType.product) {
				if (!MapUtils.getBooleanValue(detail, "isBargains") && !MapUtils.getBooleanValue(detail, "isAuthorityDiscount")) {
					BigDecimal price = new BigDecimal(MapUtils.getString(detail, "price", Integer.toString(0)));
					BigDecimal totalDiscount = new BigDecimal(MapUtils.getString(detail, "totalDiscount", Integer.toString(0)));

					BigDecimal weightPrice = price.subtract(totalDiscount);
					BigDecimal percent = weightPrice.divide(total, 2, BigDecimal.ROUND_HALF_EVEN);
					int points = percent.multiply(new BigDecimal(totalPoints)).intValue();
					detail.put("points", points);
				}
			}
		}
		bill.put("points", totalPoints);
	}
}


