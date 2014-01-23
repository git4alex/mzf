package com.zonrong.common.utils;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.ITargetType;
import com.zonrong.system.service.BizCodeService;

/**
 * date: 2011-3-14
 *
 * version: 1.0
 * commonts: ......
 */
public interface MzfEnum {
	public String getText() throws BusinessException;

	public enum OrgRelType {
		transfer,
		nakedDiamond
	}

	public enum ProductType implements MzfEnum{
		diamond(StorageType.product_diamond),		        //钻石
		nakedDiamond(StorageType.product_nakedDiamond), 	//裸钻
		pt(StorageType.product_pt),				            //铂金
		kGold(StorageType.product_kGold),			        //k金
		gemstone(StorageType.product_gemstone), 		    //宝石
		pearl(StorageType.product_pearl), 			        //珍珠
		jade(StorageType.product_jade),			            //翡翠
		zircon(StorageType.product_other),			        //锆石
		pd(StorageType.product_other),				        //钯金
//		gold(StorageType.product_other);			        //黄金
        silver(StorageType.product_silver),				    //银饰
        gold(StorageType.product_gold);			            //黄金

		public boolean isDiamond() {
			switch (this) {
			case pt:
			case kGold:
			case pd:
			case gold:
            case silver:
				return false;
			default:
				return true;
			}
		}

		public String getText() throws BusinessException {
			return BizCodeService.getBizName("productType", this.toString());
		}

		private StorageType storageType;
		private ProductType(StorageType storageType) {
			this.storageType = storageType;
		}

		public StorageType getStorageType() throws BusinessException {
			return this.storageType;
		}
	}

	public enum CusOrderStatus implements MzfEnum{
		New,			//新增
		transfering,	//调拨中
		demanding,		//要货中
		received,		//已到货
		over,			//完成
		refund,			//已退定
		interrupted;	//已中断

		public String getText() {
			return BizCodeService.getBizName("cusOrderStatus", this.toString());
		}
	}

	public enum StoreType implements MzfEnum {
        cooperate("合作"),
		chain("直营"),
		franchisee("加盟");

		public String type;
		private StoreType(String type) {
			this.type = type;
		}

		public String getText() {
			return this.type;
		}
	}

	public enum DemandStatus implements MzfEnum {
		New,			//新增
		waitFranchiseeProcess, //待加盟店经理审核
		waitMgrProcess,	//待销售总监审核
		waitFinanceProcess,	//待财务审核
		waitProcess,	//待处理
		waitPurchase,	//待采购
		waitOEM,		//待委外
		machining,		//生产中
		waitSend,		//待发货
		transfering,	//调拨中
		over,			//完成(收货)
		reject,			//驳回
		canceled;		//取消

		public String getText() {
			return BizCodeService.getBizName("demandStatus", this.toString());
		}
	}

	public enum MaterialDemandStatus implements MzfEnum {
		New,			        //新建
	    waitManagerProcess,     //待区域经历审核
	    waitMgrProcess,         //待销售总监审核
		waitProcess,	        //待配货
		waitSend,		        //待发货
		transfering,	        //调拨中
		over,			        //完成
		reject,			        //驳回
		canceled;		        //取消

		public String getText() {
			return BizCodeService.getBizName("materialDemandStatus", this.toString());
		}
	}

	public enum SettlementStatus implements MzfEnum{
		New,
		over;
		public String getText() {
			return BizCodeService.getBizName("settlementStatus", this.toString());
		}
	}

	public enum SettlementType implements MzfEnum{
		transferProduct,		    //商品调拨
		transferMaterial,		    //物料调拨
		transferSecondGold,		    //旧金调拨
		transferSecondProductToSecondGold,		//旧饰调拨转旧金入库
		maintainProduct,		    //商品维修
		splitSecondProduct,		    //旧饰拆旧
		renovateSecondProduct,	    //旧饰翻新

		vendorOrderPurchase,	    //商品采购
		vendorOrderOEM,			    //商品委外
		vendorOrderMaintainOEM,	    //委外维修
		vendorOrderRawmaterial,	    //原料采购
		vendorOrderMaterial;		//物料采购

		public String getText() {
			return BizCodeService.getBizName("settlementType", this.toString());
		}
	}

	public enum MaintainStatus implements MzfEnum{
		New,				//新建
		maintaining,		//维修中
		refund,				//退定
		received,			//待销售
		over;				//完成

		public String getText() {
			return BizCodeService.getBizName("maintainStatus", this.toString());
		}
	}

	public enum TransferStatus implements MzfEnum{
		waitConfirm,
		waitApprove,
		waitSend,
		waitReceive,
		canceled,
		reject,
		over;

		public String getText() {
			return BizCodeService.getBizName("transferStatus", this.toString());
		}
	}

	public enum TransferTargetType implements MzfEnum{
		product,
		maintainProduct,
		secondProduct,
		material,
		secondGold;

		public String getText() {
			switch (this) {
			case product:
				return "商品";
			case maintainProduct:
				return "维修商品";
			case secondProduct:
				return "旧饰";
			case material:
				return "物料";
			case secondGold:
				return "旧金";
			default:
				return null;
			}
		}

		public TargetType getBizTargetType() {
			switch (this) {
			case product:
			case maintainProduct:
			case secondProduct:
				return TargetType.product;
			case material:
				return TargetType.material;
			case secondGold:
				return TargetType.secondGold;
			default:
				return null;
			}
		}
	}

	public enum InventoryStatus implements MzfEnum{
		onStorage,			//在库
		onPassage,			//在途
		deliveryTemporary,	//临时出库
		deliveryMaintain;	//委外维修出库

		public String getText() {
			return BizCodeService.getBizName("inventoryStatus", this.toString());
		}
	}


	public enum SplitStatus implements MzfEnum{
		New,	//待确认
		pass,	//待合并原料
		over;	//完成

		public String getText() {
			return BizCodeService.getBizName("splitStatus", this.toString());
		}
	}

	/**
	 * 供应商订单状态
	 */
	public enum VendorOrderStatus implements MzfEnum{
		New,		//新建
		submit,		//提交
		receiving,	//收货中
		finished,	//完成
		canceledRawmaterial;	//核销原料

		public String getText() {
			return BizCodeService.getBizName("vendorOrderStatus", this.toString());
		}
	}


	public enum VendorOrderType implements MzfEnum{
		purchase("商品采购订单", "SPCG"),		//商品采购
		OEM("委外加工订单", "WWJG"),			//商品委外
		maintainOEM("委外维修订单", "WWWX"),	//委外维修
		rawmaterial("原料采购订单", "YLCG"),	//原料采购
		splitRawmaterial("拆旧原料入库单", "CJYL"),	//拆旧原料
		material("物料采购订单", "WLCG");	//物料采购

		private String text;
		private String orderPrefix;
		private VendorOrderType(String text, String orderPrefix) {
			this.text = text;
			this.orderPrefix = orderPrefix;
		}
		public String getText() {
			return this.text;
		}
		public String getOrderPrefix() {
			return this.orderPrefix;
		}
	}

	public enum SaleType implements MzfEnum {
		sale,			//商品销售
		returns;		//退货

		public String getText() {
			switch (this) {
			case sale:
				return "商品销售";
			case returns:
				return "退货";
			default:
				return null;
			}
		}
	}

	public enum SaleDetailType implements MzfEnum {
		product,
		material,
		secondGold,
		secondJewel,
		genChit,
		returnsChit,
		present_product,
		present_material,
		present_chit;

		public String getText() {
			return BizCodeService.getBizName("saleDetailType", this.toString());
		}
	}

	public enum CustomerType implements MzfEnum {
		potential,
		normal,
		vip;

		public String getText() {
			return BizCodeService.getBizName("cusType", this.toString());
		}
	}

	public enum CustomerCardStatus implements MzfEnum {
		free,
		lock,
		obsolete;

		public String getText() {
			return BizCodeService.getBizName("customerCardStatus", this.toString());
		}
	}

	public enum CustomerLogType implements MzfEnum {
		grant,				    //发卡
		frozen,				    //冻结会员卡
		unfrozen,			    //解冻会员卡
		upgrade,			    //升级
		downgrade,              //降级
		upPoints, 			    //修改积分
		upgradeWarn,            //升级提醒
		downgradeWarn,          //降级提醒
		obsolete;			    //作废会员卡

		public String getText() {
			return BizCodeService.getBizName("CustomerLogType", this.toString());
		}
	}
	public enum CustomerPointsOperateType implements MzfEnum{
		add,    //增加积分
		lessen;  //减少积分
		public String getText() throws BusinessException {
			return BizCodeService.getBizName("CustomerPointsOperateType", this.toString());
		}
	}

	public enum CustomerPointsType implements MzfEnum{
        payPoints,          //积分抵现
		exchangePoints,     //兑换积分
		historyPoints,      //历史积分
		points,             //剩余积分
		addPoints,          //增加积分
		lessPoints,         //减少积分
		lockedPoints,       //锁定积分
		unlockedPoints;     //解锁积分
		public String getText() throws BusinessException {
			return BizCodeService.getBizName("CustomerPointsType", this.toString());
		}
	}

	public enum ChitStatus implements MzfEnum {
		normal,  //未发放
		expired, //过期
		activate, //激活
		returns, //回收
		invalid,  //作废
		freeze;   //冻结

		public String getText(){
			return BizCodeService.getBizName("chitStatus", this.toString());
		}
	}

	public enum SaleruleType implements MzfEnum {
		single,
		bill;

		public String getText(){
			return BizCodeService.getBizName("saleruleType", this.toString());
		}
	}

	public enum SaleruleStatus implements MzfEnum {
		enable,
		disenable;

		public String getText(){
			return BizCodeService.getBizName("saleruleStatus", this.toString());
		}
	}

	public enum LogicOperator {
		EQ("=="),
		GT(">"),
		LT("<"),
		GT_EQ(">="),
		LT_EQ("<=");

		String operator;
		private LogicOperator(String operator) {
			this.operator = operator;
		}

		public String toString() {
			return this.operator;
		}
	}

	public enum TargetType implements ITargetType {
		product,		//商品
		rawmaterial,	//原料
		material,		//物料
		maintainProduct, //维修收货
		secondProduct,
		secondGold,
		chit,
        oemRawmaterialReturn    //委外原料退库
	}

    public enum lendStatus implements MzfEnum{
        lend,       //借出
        sold,       //已售
        returned;   //已还

        @Override
        public String getText() throws BusinessException {
            return BizCodeService.getBizName("lendStatus", this.toString());
        }
    }

	public enum StorageType implements MzfEnum {
		rawmaterial_nakedDiamond("裸石原料库", "forRawmaterial"),
		rawmaterial_gold("金料库", "forRawmaterial"),
		rawmaterial_parts("配件库", "forRawmaterial"),
		rawmaterial_gravel("碎石", "forRawmaterial"),

		product_diamond("钻石库", "forProduct"),
		product_nakedDiamond("裸钻库（商品）", "forProduct"),
		product_pt("铂金库", "forProduct"),
		product_kGold("K金库", "forProduct"),
		product_gemstone("宝石库", "forProduct"),
		product_pearl("珍珠库", "forProduct"),
		product_jade("翡翠库", "forProduct"),
        product_gold("黄金库", "forProduct"),
        product_silver("银饰库","forProduct"),
		product_other("其它", "forProduct"),

		second_secondGold("旧金库", "forSecondGold"),
		second_secondProduct("旧饰库", "forSecondProduct"),

		product_maintain("维修库", "forProduct"),
		product_borrow("借货库"),
		product_temporary("临时库", "forTemporaryProduct"),

		material("物料库", "forMaterial"),

		Sale("现金库"),
		Earnest("定金库");

		private StorageType(String text){
			this.text = text;
		}

		private StorageType(String text, String inventoryCheckSql){
			this.text = text;
			this.inventoryCheckSql = inventoryCheckSql;
		}

		private String text;
		private String inventoryCheckSql;
		public String getText(){
			return this.text;
		}

		public String getInventoryCheckSql() {
			return inventoryCheckSql;
		}
	}

    enum InventoryType {
        warehouse,        //入库
        delivery        //出库
    }

    enum BizType {
        addMaterial,    //新增物料信息
        register,        //收货登记
        oemReturn,      //委外原料退库
        send,            //发货
        receive,        //从其它部门收货
        returned,        //退货
        renovate,        //翻新
        transferToTemporary,        //调拨如临时库
        transferToProductStorage,    //调入商品库
        dropProduct,                //返厂
        deliveryFromTemporary,        //临时出库
        deliveryFromMaintain,        //委外维修出库
        warehouseToTemporary,        //临时库入库
        warehouseToMaintain,        //维修库入库
        warehouseOnSplit,            //拆旧入库
        maintainOver,                //维修完成
        OEM,
        sell,                        //销售
        maintailSell,                //维修销售入库
        buySecondGold,                //旧金回收
        buySecondProduct,            //旧饰回收
        maintain,                    //维修
        translateToProduct,            //原料裸石转化为商品
        translateToRawmaterial,        //商品裸钻转化为原料裸石
        delivery,                    //强制出库
        vendorSell                  //供应商销售
    }

    enum RawmaterialType {
        nakedDiamond("裸石", "RD"),			//裸石
        gold("金料", ""),				//金料
        parts("配件", "RP"),				//配件
        gravel("碎石", "RSD"),				//碎石
        secondGold("旧金", "");			//旧金（旧金和原料一同记录在原料表中）

        private String name;
        private String prefix;

        RawmaterialType(String name, String prefix) {
            this.name = name;
            this.prefix = prefix;
        }
        public String getName() {
            return name;
        };

        public String getPrefix() {
            return prefix;
        };
    }

    enum RawmaterialStatus {
        free,			//正常
        locked,			//锁定
        canedled,		//核销
sold            //已售
    }

    enum GoldClass {
        pt900,			//铂900
        pt950,			//铂950
        k750,			//金750
        pd950,			//钯
        silver,			//银
        gold;			//黄金

        public String getText() {
            return BizCodeService.getBizName("goldClass", this.toString());
        }
    }

    enum DeliveryTemporaryReason {
        QC,
        CID
    }

    /**
     * 要货单处理方式
     */
    enum DemandProcessType {
        allocate,	//库存调拨
        replaceAllocate,  //替代调拨
        purchase,	//采购
        OEM,		//委外
        reject		//驳回
    }
}


