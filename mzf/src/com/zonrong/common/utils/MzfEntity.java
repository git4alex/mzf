package com.zonrong.common.utils;

import com.zonrong.entity.code.IEntityCode;

/**
 * date: 2010-10-11
 *
 * version: 1.0
 * commonts: ......
 */
public enum MzfEntity implements IEntityCode {
	
	 /**
	  * 客户积分流水
	  */
	CUSTOMER_POINTS_FLOW("customerPointsFlow"),
	 /**
	  * 客户积分流水(视图)
	  */
	VIEW_CUSTOMER_POINTS_FLOW("vCustomerPointsFlow"),
	
	/**
	 * 供应商
	 */
	VENDOR("vendor"),
	
	/**
	 * 联系人
	 */
	LINKMAN("linkman"),
	
	/**
	 * 账户
	 */
	ACCOUNT("account"),
	
	/**
	 * 商场
	 */
	MARKET("market"),
	
	/**
	 * 系统提醒
	 */
	NOTICE("notice"),
	
	/**
	 * 厂家款式
	 */
	VENDOR_STYLE("vendorStyle"),
	
	/**
	 * 厂家款式（查询）
	 */
	VENDOR_STYLE_VIEW("vVendorStyle"),
	
	/**
	 * 供应商日志
	 */
	VENDOR_MEMO("memo"),
	
	/**
	 * 厂家款式副石信息
	 */
	VENDOR_STYLE_DIAMOND("vendorStyleDiamond"),
	
	/**
	 * MZF款式
	 */
	STYLE("style"),
	
	/**
	 * MZF款式副石信息
	 */
	STYLE_DIAMOND("styleDiamond"),
	
	/**
	 * 商品
	 */
	PRODUCT("product"),
	
	/**
	 * 商品视图
	 */
	VIEW_PRODUCT("vProduct"),
	
	/**
	 * 旧饰
	 */
	SECOND_PRODUCT("secondProduct"),	
	
	/**
	 * 商品主石和副石
	 */
	PRODUCT_DIAMOND("diamond"),
	
	/**
	 * 商品证书
	 */
	PRODUCT_CERTIFICATE("certificate"),
	
	/**
	 * 图片
	 */
	UPLOAD("upload"),
	
	/**
	 * 客户订单
	 */
	CUS_ORDER("cusOrder"),
	
	/**
	 * 下客户订单所选库存
	 */
	CUS_ORDER_PRODUCT_SOURCE("cusOrderProductSource"),
	
	/**
	 * 客户订单
	 */
	CUS_ORDER_VIEW("vCusOrder"),		
	
	/**
	 * 定金收款记录（包括客订和维修单）
	 */
	EARNEST_FLOW("earnestFlow"),	
	
	/**
	 *要货单 
	 */
	DEMAND("demand"),
	
	/**
	 * 要货单处理
	 */
	DEMAND_PROCESS("demandProcess"),
	
	/**
	 * 物料要货申请
	 */
	MATERIAL_DEMAND("materialDemand"),
	
	/**
	 * 物料要货申请（查询）
	 */
	MATERIAL_DEMAND_VIEW("vMaterialDemand"),
	
	/**
	 * 要货单
	 */
	DEMAND_VIEW("vDemand"),
	
	/**
	 * 要货单处理
	 */
	DEMAND_PROCESS_VIEW("vDemandProcess"),	
	
	/**
	 * 供应商订单
	 */
	VENDOR_ORDER("vendorOrder"),
	
	/**
	 * 供应商订单（收货登记查询）
	 */
	VENDOR_ORDER_VIEW("vVendorOrder"),
	
	/**
	 * 供应商采购委外订单明细
	 */
	VENDOR_ORDER_PRODUCT_ORDER_DETAIL("vendorProductOrderDetail"),
	
	/**
	 * 供应商采购委外订单明细（查询）
	 */
	VENDOR_ORDER_PRODUCT_ORDER_DETAIL_VIEW("vVendorProductOrderDetail"),
	
	/**
	 * 原料采购订单明细
	 */
	VENDOR_ORDER_RAWMATERIAL_ORDER_DETAIL("rawmaterialOrderDetail"),
	
	/**
	 * 物料采购订单明细
	 */
	VENDOR_ORDER_MATERIAL_ORDER_DETAIL("materialOrderDetail"),
	
	/**
	 * 物料采购订单明细(查询)
	 */
	VENDOR_ORDER_MATERIAL_ORDER_DETAIL_VIEW("vMaterialOrderDetail"),
	
	/**
	 * 原料
	 */
	RAWMATERIAL("rawmaterial"),
	
	/**
	 * 物料
	 */
	MATERIAL("material"),
	
	/**
	 * 组织机构
	 */
	ORG("org"),
	/**
	 * 业务编码
	 */
	BIZ_CODE("bizCode"),
	/**
	 * 组织机构仓库树查询实体
	 */
	ORG_STORAGE("vOrgStorage"),
	
	/**
	 * 仓库
	 */
	STORAGE("storage"),
	
	/**
	 * 库存
	 */
	INVENTORY("inventory"),
	
	/**
	 * 商品库存
	 */
	PRODUCT_INVENTORY_VIEW("productInventory"),
	
	/**
	 * 旧饰库存
	 */
	SECOND_PRODUCT_INVENTORY_VIEW("secondProductInventory"),
	
	/**
	 * 库存流水
	 */
	INVENTORY_FLOW("inventoryFlow"),
	
	/**
	 * 收货登记
	 */
	REGISTER("register"),
	
	/**
	 * 收货登记（查询）
	 */
	REGISTER_VIEW("vRegister"),
	
	/**
	 * 调拨单
	 */
	TRANSFER("transfer"),
	
	/**
	 * 调拨单(查询)
	 */
	TRANSFER_VIEW("vTransfer"),
	
	/**
	 * 调拨审核
	 */
	TRANSFER_APPROVE("transferApprove"),
	
	/**
	 * 促销规则
	 */
	SALERULE("salerule"),
	/**
	 * 促销规则
	 */
	SALERULE_RESULT("saleruleResult"),
	/**
	 * 销售单
	 */
	SALE("sale"),
	
	/**
	 * 销售单
	 */
	SALE_VIEW("vSale"),
	
	/**
	 * 销售单明细
	 */
	SALE_DETAIL("saleDetail"),
	
	/**
	 * 销售单明细
	 */
	SALE_DETAIL_VIEW("vSaleDetail"),
	
	/**
	 * 客户
	 */
	CUSTOMER("customer"),
	
	/**
	 * 发卡
	 */
	GRANT_CARD("grantCard"),
	
	/**
	 * 会员卡
	 */
	CUSTOMER_CARD("customerCard"),
	
	/**
	 * 会员卡（查询）
	 */
	CUSTOMER_CARD_VIEW("vCustomerCard"),
	
	/**
	 * 会员卡操作日志
	 */
	CUSTOMER_LOG("customerLog"),
	
	/**
	 * 客户
	 */
	CUSTOMER_VIEW("vCustomer"),
	
	/**
	 * 客户与组织机构业务关系
	 */
	CUSTOMER_ORG_REL("customerOrgRel"),
	
	/**
	 * 门店
	 */
	STORE("store"),
	
	/**
	 * 门店
	 */
	STORE_VIEW("vStore"),
	
	/**
	 * 员工
	 */
	EMPLOYEE("employee"),
	
	/**
	 * 员工
	 */
	EMPLOYEE_VIEW("vEmployee"),
	
	/**
	 * 交接班
	 */
	SHIFT_WORK("shiftWork"),
	
	/**
	 * 交接班
	 */
	SHIFT_WORK_DETAIL("shiftWorkDetail"),	
	
	/**
	 * 原料库存
	 */
	RAWMATERIAL_INVENTORY_VIEW("rawmaterialInventory"),
	
	/**
	 * 物料库存
	 */
	MATERIAL_INVENTORY_VIEW("materialInventory"),
	
	/**
	 * 旧金库存
	 */
	SECOND_GOLD_INVENTORY_VIEW("secondGoldInventory"),
	
	/**
	 * 供应商款式原料需求清单
	 */
	BOM("bom"),
	
	/**
	 * 委外订单配料明细
	 */
	DOSING("dosing"),
	
	/**
	 * 委外订单配料明细
	 */
	DOSING_VIEW("vDosing"),		
	
	/**
	 * 委外订单配料明细汇总
	 */
	DOSING_SUMMARY_VIEW("vDosingSummary"),
	
	/**
	 * 配料情况对比
	 */
	BOM_DOSING_VIEW("vBomDosing"),
	
	/**
	 * 补料记录
	 */
	PATCH("patch"),
	
	/**
	 * 盘点
	 */
	INVENTORY_CHECK("inventoryCheck"),
	
	/**
	 * 盘点
	 */
	INVENTORY_CHECK_VIEW("vInventoryCheck"),
	
	/**
	 * 盘点明细
	 */
	INVENTORY_CHECK_DETAIL("inventoryCheckDetail"),
	
	/**
	 * 盘点明细备注
	 */
	INVENTORY_CHECK_DETAIL_REMARK("inventoryCheckDetailRemark"),
	
	/**
	 * 柜台
	 */
	SHOWCASE("showcase"),
	
	/**
	 * 柜台陈列商品 
	 */
	SHOWCASE_PRODUCT("showcaseProduct"),
	
	/**
	 * 柜台陈列商品（查询）
	 */
	VIEW_SHOWCASE_PRODUCT("vShowcaseProduct"),
	/**
	 * 柜台陈列商品流水
	 */
	SHOWCASE_PRODUCT_FLOW("showcaseProductFlow"),
	
	/**
	 * 柜台点数
	 */
	SHOWCASE_CHECK("showcaseCheck"),
	
	/**
	 * 柜台点数明细
	 */
	SHOWCASE_CHECK_DETAIL("showcaseCheckDetail"),
	
	/**
	 * 商品调拨关系
	 */
	ORG_REL("orgRel"),
	
	/**
	 * 金库
	 */
	TREASURY("treasury"),
	/**
	 * 金库流水
	 */
	TREASURY_FLOW("treasuryFlow"),
	/**
	 * 销售额金库
	 */
	TREASURY_SALE_VIEW("vTreasurySale"),
	/**
	 * 销售额金库流水
	 */
	TREASURY_SALE_FLOW_VIEW("vTreasurySaleFlow"),
	/**
	 * 定金金库
	 */
	TREASURY_EARNEST_VIEW("vTreasuryEarnest"),
	/**
	 * 定金金库流水
	 */
	TREASURY_EARNEST_FLOW_VIEW("vTreasuryEarnestFlow"),
	
	/**
	 * 金价
	 */
	GOLD_VALUE("goldValue"),
	/**
	 * 最新金价
	 */
	GOLD_VALUE_LAST_VIEW("vGoldValueLast"),
	
	/**
	 * 日结
	 */
	SUMMARY("summary"),
	/**
	 * 日结（查询）
	 */
	SUMMARY_VIEW("vSummary"),
	/**
	 * 日结（查询当日）
	 */
	SUMMARY_TODAY_VIEW("vSummaryToday"),
	
	/**
	 * 维修
	 */	
	MAINTAIN("maintain"),
	
	/**
	 * 维修（查询）
	 */	
	MAINTAIN_VIEW("vMaintain"),
	
	/**
	 * 拆旧
	 */
	SPLIT("split"),
	
	/**
	 * 拆旧
	 */
	SPLIT_VIEW("vSplit"),
	
	/**
	 * 拆旧明细
	 */
	SPLIT_DEETAIL("splitDetail"),

	/**
	 * 拆旧原料汇总
	 */
	SPLIT_RAWMATERIAL_SUMMARY("vSplitRawmaterialSummary"),	

	/**
	 * 翻新
	 */
	RENOVATE("renovate"),
	
	/**
	 * 发货单
	 */
	DISPATCH("dispatch"),

	/**
	 * 发货单（发货单）
	 */
	DISPATCH_VIEW("vDispatch"),
	
	/**
	 * 结算单
	 */
	SETTLEMENT("settlement"),
	
	/**
	 * 结算单（差查询）
	 */
	SETTLEMENT_VIEW("vSettlement"),
	
	/**
	 * 商品调价
	 */
	PRODUCT_ADJUST("productAjdust"),
	
	/**
	 * 商品调价（查询）
	 */
	PRODUCT_ADJUST_VIEW("vProductAjdust"),
	
	/**
	 * 退货明细
	 */	
	RETURNS_DETAIL("returnsDetail"),
	
	/**
	 * 会员升级规则
	 */
	CUSTOMER_UPGRADE_RULE("customerUpgradeRule"),
	
	/**
	 * 代金券
	 */
	CHIT("chit"),
	
	/**
	 * 代金券
	 */
	CHIT_VIEW("vChit"),
    /**
     * 代金券支付明细
     * 
     */
	CHIT_PAY("chitPay"),
	/**
	 * 
	 * 铂金类商品工费
	 *
	 */
	PROCESSING_CHARGES("processingCharges"),
	
	/**
	 * 积分规则
	 */
	POINTS_RULE("pointsRule");
	
	MzfEntity(String code) {
		this.code = code;
	}
	private String code;
	
	public String getCode() {
		return this.code;
	}
}


