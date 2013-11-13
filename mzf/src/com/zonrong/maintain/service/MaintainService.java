package com.zonrong.maintain.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.basics.customer.service.CustomerService;
import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.common.service.BillStatusService;
import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.*;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.cusorder.service.EarnestFlowService;
import com.zonrong.cusorder.service.EarnestFlowService.OrderType;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.inventory.treasury.service.TreasuryEarnestService;
import com.zonrong.inventory.treasury.service.TreasurySaleService;
import com.zonrong.inventory.treasury.service.TreasuryService.MoneyStorageClass1;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.settlement.service.SettlementService;
import com.zonrong.system.service.BizCodeService;
import com.zonrong.transfer.maintain.service.TransferMaintainProductService;
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
 * date: 2011-3-9
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class MaintainService extends BillStatusService<MaintainStatus>{
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private EarnestFlowService earnestFlowService;
	@Resource
	private TreasuryEarnestService treasuryEarnestService;
	@Resource
	private TreasurySaleService treaaurySaleService;
	@Resource
	private ProductService productService;
	@Resource
	private TransferMaintainProductService transferMaintainProductService;
	@Resource
	private CustomerService customerService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private SettlementService settlementService;
	@Resource
	private MzfOrgService mzfOrgService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	public enum ProductSource {
		selled,			//已售
		onStorage		//在库
	}

	public Map<String, Object> findByProductNum(String productNum, IUser user) throws BusinessException {
		Map<String, Object> product = productService.findByProductNum(productNum, user);
		Integer productId = MapUtils.getInteger(product, "id");
		Map<String,Object> customer = findCustomerByProductId(productId);
		ProductStatus status = ProductStatus.valueOf(MapUtils.getString(product, "status"));
		if (status == ProductStatus.selled) {
			product.put("productSource", ProductSource.selled);
			if(customer != null){
				product.put("customer", customer);
			}
		} else {
			Map<String, Object> inventory = productInventoryService.getInventoryForProduct(productId, user.getOrgId());
			InventoryStatus iStatus = InventoryStatus.valueOf(MapUtils.getString(inventory, "status"));
			if (iStatus == InventoryStatus.onStorage) {
				product.put("productSource", ProductSource.onStorage);
			} else {
				throw new BusinessException("商品[" + productNum + "]库存状态为" + iStatus.getText() + "，不能维修");
			}
		}
		return product;
	}

	private Map<String,Object> findCustomerByProductId(int productId)throws BusinessException{
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("type", SaleDetailType.product);
		where.put("targetId", productId);

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SALE_DETAIL);
		OrderBy orderBy = new OrderBy(new String[]{metadata.getPkCode()}, OrderByDir.desc);
		List<Map<String, Object>> list = entityService.list(metadata, where, orderBy, User.getSystemUser());
		if(list.size() > 0){
			Map<String,Object> saleDetail = list.get(0);
			int saleId = Integer.parseInt(saleDetail.get("saleId").toString());
			Map<String,Object> sale = entityService.getById(MzfEntity.SALE, saleId, User.getSystemUser());
			if(sale != null){
				Integer cusId = MapUtils.getInteger(sale, "cusId");
				if (cusId != null) {
					Map<String,Object> customer = entityService.getById(MzfEntity.CUSTOMER_VIEW, cusId, User.getSystemUser());
					if(customer != null){
						return customer;
					}
				}
			}
		}

		return null;
	}

	public int createMaintain(Map<String, Object> maintain, IUser user) throws BusinessException {
        String maintainCode = MapUtils.getString(maintain,"maintainCode");
        if(StringUtils.isBlank(maintainCode)){
            throw new BusinessException("维修条码不能为空");
        }

        //使用维修条码作为维修单号
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("num", maintainCode);
        List<Map<String, Object>> list = entityService.list(MzfEntity.MAINTAIN, where, null, User.getSystemUser());
        if(list.size() >= 1){
            throw new BusinessException("维修单号：["+ maintainCode +"]重复" );
        }

		String productNum = MapUtils.getString(maintain, "productNum");
        Integer productId;
        String productSource;
        if(StringUtils.isBlank(productNum)){ //商品条码为空
            maintain.put("num",maintainCode); //维修条码作为新建商品的商品条码
            productId = productService.supplyProduct(maintain,new ArrayList(),new ArrayList(),ProductStatus.selled,"维修商品",user);
            productSource = ProductSource.selled.toString();
        }else{
            Map<String, Object> product = findByProductNum(productNum, user);
            productId = MapUtils.getInteger(product, "id");
            productSource = MapUtils.getString(product, "productSource");
            if (ProductStatus.selled.toString().equals(productSource)) {
                //nothing
            } else if (InventoryStatus.onStorage.toString().equals(productSource)) {
                //商品出库
                productInventoryService.deliveryByProductId(BizType.maintain, productId, "维修出库", InventoryStatus.onStorage, user);
            } else {
                throw new BusinessException("未知商品，无法继续");
            }
        }

		//商品入维修库
		StorageType storageType = StorageType.product_maintain;
		int targetOrgId = user.getOrgId();
		int ownerId = user.getId();

        //String num = MzfUtils.getBillNum(BillPrefix.WX, user);

		productInventoryService.warehouse(BizType.maintain, productId, targetOrgId, storageType, ownerId, user.getOrgId(), "维修单号：["+ maintainCode +"]", user);

		Map<String, Object> earnestFlow = new HashMap<String, Object>(maintain);

		//新增维修单
        maintain.put("productId",productId);
		maintain.put("num", maintainCode);
		maintain.put("status", MaintainStatus.New);
		maintain.put("productSource", productSource);
		maintain.put("cuserId", user.getId());
		maintain.put("cuserName", user.getName());
		maintain.put("cdate", null);

		String id = entityService.create(MzfEntity.MAINTAIN, maintain, user);
		Integer maintainId = Integer.parseInt(id);

		Object payType = MapUtils.getObject(earnestFlow, "payType");
		if (payType != null) {
			earnestFlowService.appendEarnest(com.zonrong.inventory.treasury.service.TreasuryService.BizType.earnest, OrderType.maintain, maintainId, maintainCode, earnestFlow, "新建维修单", user);
		}

		int transId = transactionService.createTransId();
		logService.createLog(transId, MzfEntity.MAINTAIN, id, "新建维修单", TargetType.product, productId, "维修，维修单号为："+ maintainCode, user);
		//记录操作日志
		businessLogService.log("新开商品维修单", "维修单号为：" + maintainCode, user);

		//建立客户与该机构业务关联关系
		Integer cusId = MapUtils.getInteger(maintain, "cusId");
		if(cusId != null){
			customerService.createOrgRel(cusId, com.zonrong.inventory.treasury.service.TreasuryService.BizType.maintain, maintainId, user);
		}

		return maintainId;
	}

	public void over(int maintainId, Map<String, Object> over, IUser user) throws BusinessException {
		String price = MapUtils.getString(over, "price");
		if (StringUtils.isBlank(price)) {
			throw new BusinessException("请输入费用");
		}
		Map<String, Object> maintain = entityService.getById(MzfEntity.MAINTAIN, maintainId, user.asSystem());
		Integer maintainOrgId = MapUtils.getInteger(maintain, "orgId");
		if (user.getOrgId() != maintainOrgId && user.getOrgId() != mzfOrgService.getHQOrgId()) {
			throw new BusinessException("维修结算人既非开单门店又非总部， 不允许结算");
		}

		//查找库存
		Integer productId = MapUtils.getInteger(maintain, "productId");
		Map<String, Object> inventory = productInventoryService.getProductInventory(productId, null);
		Integer detailId = MapUtils.getInteger(inventory, "detailId");
		if (detailId != null) {
			throw new BusinessException("尚未核销原料");
		}

		//维护结算价
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("price", price);
		entityService.updateById(MzfEntity.MAINTAIN, Integer.toString(maintainId), field, user);

		//记录流程信息
		String remark = MapUtils.getString(over, "remark");
		int transId = transactionService.findTransId(MzfEntity.MAINTAIN, Integer.toString(maintainId), user);
		logService.createLog(transId, MzfEntity.MAINTAIN, Integer.toString(maintainId), "维修结算", TargetType.product, Integer.toString(productId), remark, user);

		//记录操作日志
		businessLogService.log("商品维修单结算", "维修单号为:" + MapUtils.getString(maintain, "num"), user);

		//选择入库
		Integer sourceOrgId = MapUtils.getInteger(inventory, "orgid");
        ProductSource productSource = ProductSource.valueOf(MapUtils.getString(maintain, "productSource"));

		  //如调到总部维修则门店不能结算
		if(user.getOrgId() == maintainOrgId && sourceOrgId.intValue() != maintainOrgId){
			throw new BusinessException("总部维修商品门店不能结算");
		}
		if (sourceOrgId.intValue() == maintainOrgId) {
			MaintainStatus targetStatus = null;
			//如果是在库商品，直接入商品库
			if (productSource == ProductSource.onStorage) {
				//出维修库
				productInventoryService.deliveryByProductId(BizType.maintainOver, productId, "修复出库", InventoryStatus.onStorage, user);

				//入商品库
				StorageType storageType = productInventoryService.getDefaultStorageType(productId);
				productInventoryService.warehouse(BizType.maintainOver, productId, maintainOrgId, storageType, user.getId(), sourceOrgId, "修复入库", user);

				//如果是在库商品，目标状态为完成
				targetStatus = MaintainStatus.over;
			} else if (productSource == ProductSource.selled) {
				//如果是已售商品，直接出库
				//productInventoryService.deliveryByProductId(BizType.maintainOver, productId, "修复出库", InventoryStatus.onStorage, user);

				//如果是已售商品，目标状态为待销售
				targetStatus = MaintainStatus.received;
			}
			MaintainStatus[] priorStatus = new MaintainStatus[]{MaintainStatus.New, MaintainStatus.maintaining};
			updateStatus(maintainId, priorStatus, targetStatus, new StatusCarrier(){
				@Override
				public void active(Map<String, Object> carrier) throws BusinessException {
					throw new BusinessException("维修单[" + this.getNum() + "]状态为" + this.getStatus(MaintainStatus.class).getText() + "，不允许该操作");
				}

			}, user);
		} else {
			//自动发起调拨流程
			Map<String, Object> transfer = new HashMap<String, Object>();
			transfer.put("maintainId", maintainId);
			transfer.put("targetOrgId", maintainOrgId);
			transferMaintainProductService.transfer(new Integer[]{productId}, transfer, user);
		}

		//生成结算单
		Integer receptOrgId,payOrgId;
		if (mzfOrgService.getHQOrgId() == user.getOrgId()) {
			receptOrgId = mzfOrgService.getHQOrgId();
			payOrgId = maintainOrgId;
		} else {
			receptOrgId = maintainOrgId;
			payOrgId = mzfOrgService.getHQOrgId();
		}
		settlementService.createForMaintain(receptOrgId, payOrgId, maintainId, new BigDecimal(price), remark, user);
	}

	public void refund(int maintainId, Map<String, Object> refund, IUser user)
			throws BusinessException {
		//更新维修单状态
		MaintainStatus[] priorStatus = new MaintainStatus[]{MaintainStatus.New};
		updateStatus(maintainId, priorStatus, MaintainStatus.refund, new StatusCarrier() {
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				throw new BusinessException("维修单[" + this.getNum() + "]状态为" + this.getStatus(MaintainStatus.class).getText() + "，此时不允许退定");
			}

		}, user);

		Map<String, Object> maintain = entityService.getById(MzfEntity.MAINTAIN_VIEW, maintainId, user.asSystem());
		Integer orgId = MapUtils.getInteger(maintain, "orgId");
		Integer productId = MapUtils.getInteger(maintain, "productId");

		//退钱
		String moneyStr = MapUtils.getString(refund, "amount");
		if (StringUtils.isBlank(moneyStr)) {
			throw new BusinessException("未指定金额，无法继续");
		}
		BigDecimal money = new BigDecimal(moneyStr);
		String remark = MapUtils.getString(refund, "remark");
		if (money.doubleValue() != 0) {
			MoneyStorageClass1 class1 = MoneyStorageClass1.valueOf(MapUtils.getString(refund, "payType"));
			String bank = MapUtils.getString(refund, "bank");
			treasuryEarnestService.delivery(com.zonrong.inventory.treasury.service.TreasuryService.BizType.refund, orgId, money, class1, bank, false, MzfEntity.MAINTAIN, maintainId, remark, user);
		}


		Map<String, Object> inventory = productInventoryService.getInventoryForProduct(productId, null);
		//商品出维修库
		productInventoryService.deliveryByProductId(BizType.maintainOver, productId, remark, InventoryStatus.onStorage, user);
		ProductSource productSource = ProductSource.valueOf(MapUtils.getString(maintain, "productSource"));
		if (productSource == ProductSource.onStorage) {
			//入商品库
			Integer sourceOrgId = MapUtils.getInteger(inventory, "sourceOrgId");
			StorageType storageType = productInventoryService.getDefaultStorageType(productId);
			productInventoryService.warehouse(BizType.maintainOver, productId, orgId, storageType, user.getId(), sourceOrgId, remark, user);
		}

		//记录流程
		int transId = transactionService.findTransId(MzfEntity.MAINTAIN, Integer.toString(maintainId), user);
		logService.createLog(transId, MzfEntity.MAINTAIN, Integer.toString(maintainId), "退定", null, null, remark, user);
		//记录操作日志
		businessLogService.log("商品维修单退还定金", "维修单号为:" + MapUtils.getString(maintain, "num"), user);
	}

	public void createSale(int maintainId, Map<String, Object> sale, IUser user)
			throws BusinessException {
		MaintainStatus[] priorStatus = new MaintainStatus[]{MaintainStatus.received};
		updateStatus(maintainId, priorStatus, MaintainStatus.over, new StatusCarrier() {
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				throw new BusinessException("维修单[" + this.getNum() + "]状态为" + this.getStatus(MaintainStatus.class).getText() + "，不能销售");
			}

		}, user);

		Map<String, Object> maintain = entityService.getById(MzfEntity.MAINTAIN, maintainId, user.asSystem());
		Integer orgId = MapUtils.getInteger(maintain, "orgId");
		Integer productId = MapUtils.getInteger(maintain, "productId");

		//销售金额入库
		String moneyStr = MapUtils.getString(sale, "amount");
		if (StringUtils.isBlank(moneyStr)) {
			throw new BusinessException("未指定金额，无法继续");
		}
		BigDecimal money = new BigDecimal(moneyStr);
		String remark = MapUtils.getString(sale, "remark");
		if (money.doubleValue() != 0) {
			MoneyStorageClass1 class1 = MoneyStorageClass1.valueOf(MapUtils.getString(sale, "payType"));
			String bank = MapUtils.getString(sale, "bank");
			treaaurySaleService.warehouse(com.zonrong.inventory.treasury.service.TreasuryService.BizType.sell, orgId, money, class1, bank, false, MzfEntity.MAINTAIN, maintainId, remark, user);
		}
		//销售后修改维修单属性
		Map<String,Object> field = new HashMap<String,Object>();
		field.put("saleRemark",remark);
		field.put("saleAmount",money);
		field.put("saleBank",MapUtils.getString(sale, "bank"));
		field.put("saleIsAgent",MapUtils.getString(sale, "isAgent"));
		field.put("salePayType",MapUtils.getString(sale, "payType"));
		entityService.updateById(MzfEntity.MAINTAIN, maintainId+"", field, user);

		//商品出库
		productInventoryService.deliveryByProductId(BizType.maintailSell, productId, "维修单号：["+MapUtils.getString(maintain,"num")+"]", InventoryStatus.onStorage, user);

		//记录流程
		int transId = transactionService.findTransId(MzfEntity.MAINTAIN, Integer.toString(maintainId), user);
		logService.createLog(transId, MzfEntity.MAINTAIN, Integer.toString(maintainId), "销售", null, null, remark, user);
		//记录操作日志
		businessLogService.log("维修商品销售", "商品编号为:" + productId, user);
	}

	public Map<String, Object> getMaintainByProductId(int productId, IUser user) throws BusinessException {
		Map<String, Object> where  = new HashMap<String, Object>();
		where.put("productId", productId);

		List<Map<String, Object>> list = entityService.list(MzfEntity.MAINTAIN_VIEW, where, null, user);
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		//一件商品可能多次维修，取最后一次
		return list.get(0);
	}

	public void recieveProduct(int maintainId, IUser user) throws BusinessException {
		updateStatus(maintainId, MaintainStatus.maintaining, MaintainStatus.received, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> maintain) throws BusinessException {
				throw new BusinessException("维修单[" + this.getNum() + "]状态为" + this.getStatus(MaintainStatus.class).getText() + ", 不能收货");
			}
		}, user);

		Map<String, Object> maintain = entityService.getById(MzfEntity.MAINTAIN_VIEW, maintainId, User.getSystemUser());
		Integer productId = MapUtils.getInteger(maintain, "productId");
		//记录流程信息
		int transId = transactionService.findTransId(MzfEntity.MAINTAIN, Integer.toString(maintainId), user);
		logService.createLog(transId, MzfEntity.MAINTAIN, Integer.toString(maintainId), "维修单到货", TargetType.product, Integer.toString(productId), "维修单已到货", user);
	}

	 //获取维修单打印数据
	public Map<String,Object> getPrintMaintainData(int maintainId,IUser user)throws BusinessException{
		Map<String,Object> maintainData = new HashMap<String,Object>();
		Map<String,Object> maintain = entityService.getById(MzfEntity.MAINTAIN_VIEW, maintainId, user);


		Integer productId = MapUtils.getInteger(maintain, "productId");
		Map<String,Object> product = entityService.getById(MzfEntity.VIEW_PRODUCT, productId, user);
		Iterator productKeys = product.keySet().iterator();
		while(productKeys.hasNext()){
			String key = productKeys.next().toString();
			if(key.equals("num")){
				maintainData.put("productNum", product.get(key));
			}else if(key.toUpperCase().equals("PTYPE")){
				String type = BizCodeService.getBizName("productType", product.get(key)+"");
				maintainData.put(key+"Text", type);
				maintainData.put(key, product.get(key));
			}else if(key.toUpperCase().equals("PKIND")){
				String productKind = BizCodeService.getBizName("productKind", product.get(key)+"");
				maintainData.put(key+"Text", productKind);
				maintainData.put(key, product.get(key));
			}else if(key.toUpperCase().equals("GOLDCLASS")){
				String goldClass = BizCodeService.getBizName("goldClass", product.get(key)+""); //金料成色
				maintainData.put(key+"Text", goldClass);
				maintainData.put(key, product.get(key));
			}else if(key.toUpperCase().equals("KGOLDCOLOR")){
				String kGoldColor = BizCodeService.getBizName("kGoldColor", product.get(key)+""); //k金颜色
				maintainData.put(key+"Text", kGoldColor);
				maintainData.put(key, product.get(key));
			}else if(key.toUpperCase().equals("DIAMONDCOLOR")){
				String diamondColor = BizCodeService.getBizName("diamondColor", product.get(key)+""); //主石颜色
				maintainData.put(key+"Text", diamondColor);
				maintainData.put(key, product.get(key));
			}else if(key.toUpperCase().equals("DIAMONDCLEAN")){
				String diamondClean = BizCodeService.getBizName("diamondClean", product.get(key)+""); //主石净度
				maintainData.put(key+"Text", diamondClean);
				maintainData.put(key, product.get(key));
			}else if(key.toUpperCase().equals("DIAMONDCUT")){
				String diamondClean = BizCodeService.getBizName("diamondCut", product.get(key)+""); //主石切工
				maintainData.put(key+"Text", diamondClean);
				maintainData.put(key, product.get(key));
			}else if(key.toUpperCase().equals("DIAMONDSHAPE")){
				String diamondShape = BizCodeService.getBizName("diamondShape", product.get(key)+""); //主石形状
				maintainData.put(key+"Text", diamondShape);
				maintainData.put(key, product.get(key));
			}else{
				maintainData.put(key, product.get(key));
			}
		}

        for (String key : maintain.keySet()) {
            if (key.toUpperCase().equals("PAYTYPE")) {
                String paymentWay = BizCodeService.getBizName("paymentWay", maintain.get(key) + "");
                maintainData.put(key + "Text", paymentWay);
                maintainData.put(key, maintain.get(key));
            } else if (key.toUpperCase().equals("CUSGRADE")) {
                String customerCardType = BizCodeService.getBizName("customerCardType", maintain.get(key) + "");
                maintainData.put(key + "Text", customerCardType);
                maintainData.put(key, maintain.get(key));
            } else {
                maintainData.put(key, maintain.get(key));
            }

        }

		return maintainData;
	}
	//维修销售单打印
	public Map<String,Object> getPrintMaintainSaleData(int maintainId,IUser user)throws BusinessException{
		Map<String,Object> maintain = entityService.getById(MzfEntity.MAINTAIN_VIEW, maintainId, user);
		if(maintain.get("payType") != null){
			String payTypeText = maintain.get("payType").toString();
			String payTypeName = BizCodeService.getBizName("paymentWay", payTypeText);
			maintain.put("payTypeText",payTypeName);
		}
		String saleAmountText = "";
		if(maintain.get("saleAmount") != null){
			String saleAmount = MapUtils.getString(maintain, "saleAmount");
			saleAmountText = TpltUtils.convertDigits(saleAmount);
		}

		String cusGradeText = BizCodeService.getBizName("customerCardType", maintain.get("cusGrade")+"");
		maintain.put("cusGradeText", cusGradeText);
		maintain.put("saleAmountText", saleAmountText);
		return maintain;
	}

	@Override
	protected EntityMetadata getBillMetadata() throws BusinessException {
		return metadataProvider.getEntityMetadata(MzfEntity.MAINTAIN);
	}

	@Override
	protected String getBillName() {
		return "维修单";
	}
}


