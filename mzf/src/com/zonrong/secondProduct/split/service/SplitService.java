package com.zonrong.secondProduct.split.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.basics.rawmaterial.service.RawmaterialService.RawmaterialType;
import com.zonrong.common.service.BillStatusService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.*;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.common.utils.MzfUtils.BillPrefix;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.core.templete.SaveTemplete;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.ProductInventoryService;
import com.zonrong.inventory.service.SecondProductInventoryService;
import com.zonrong.inventory.service.RawmaterialInventoryService.GoldClass;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.purchase.service.DetailCRUDService.VendorOrderDetailStatus;
import com.zonrong.purchase.service.RawmaterialOrderService;
import com.zonrong.register.service.RegisterRawmaterialService;
import com.zonrong.settlement.service.SettlementService;
import com.zonrong.system.service.BizCodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2011-3-17
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class SplitService extends BillStatusService<SplitStatus> {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private SecondProductInventoryService	secondProductInventoryService;
	@Resource
	private RegisterRawmaterialService registerRawmaterialService;
	@Resource
	private RawmaterialOrderService rawmaterialOrderService;
	@Resource
	private SettlementService settlementService;
	@Resource
	private BusinessLogService businessLogService;

	public enum SplitProductSource {
		secondProduct,
		product,
		maintainProduct
	}

	public int createSplit(Map<String, Object> split, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		try {
			SplitProductSource.valueOf(MapUtils.getString(split, "productSource"));
		} catch (Exception e) {
			throw new BusinessException("请指定合法的拆旧商品来源");
		}
		String num = MzfUtils.getBillNum(BillPrefix.CJ, user);
		split.put("num", num);
		split.put("cuserId", user.getId());
		split.put("cuserName", user.getName());
		split.put("cdate", null);
		split.put("status", SplitStatus.New);
		String id = entityService.create(getBillMetadata(), split, user);
		int splitId = Integer.parseInt(id);

		saveDetail(splitId, detailList, user);
		//旧饰标记为拆旧
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("targetId", MapUtils.getString(split, "productId", "0"));
		where.put("targetType", "secondProduct");
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("isSplit", "true");

		entityService.update(MzfEntity.INVENTORY, field, where, user);

		//记录操作日志
		businessLogService.log("新增拆旧", "拆旧单号：" + num, user);
		return splitId;
	}

	public void updateSplit(int splitId, Map<String, Object> split, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		SplitStatus status = this.getStatus(splitId, SplitStatus.class, user);
		if (status != SplitStatus.New) {
			throw new BusinessException(this.getBillName() + "状态为" + status.getText() + ", 不允许修改操作");
		}
		split.remove("productSource");
		split.remove("productId");
		split.remove("status");
		entityService.updateById(getBillMetadata(), Integer.toString(splitId), split, user);

		saveDetail(splitId, detailList, user);
	}

	private void saveDetail(final int splitId, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		final EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SPLIT_DEETAIL);
		SaveTemplete templete = new SaveTemplete(entityService) {
			protected EntityMetadata getEntityMetadata() throws BusinessException {
				return metadata;
			}
			protected void setForeignKey(Map<String, Object> map) throws BusinessException {
				map.put("splitId", splitId);
			}
		};

		templete.save(detailList, user);
	}

	public void deleteSplit(int splitId, IUser user) throws BusinessException {
		SplitStatus status = this.getStatus(splitId, SplitStatus.class, user);
		if (status != SplitStatus.New) {
			throw new BusinessException("当前状态为" + status.getText() + "，不允许删除");
		}

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("splitId", splitId);
		entityService.delete(MzfEntity.SPLIT_DEETAIL, where, user);

		entityService.deleteById(getBillMetadata(), Integer.toString(splitId), User.getSystemUser());
	}

	public void confirmSplit(int splitId, IUser user) throws BusinessException {
		Map<String, Object> split = entityService.getById(getBillMetadata(), splitId, User.getSystemUser());
//		SplitStatus status = SplitStatus.valueOf(MapUtils.getString(split, "status"));
//		if (status != SplitStatus.New) {
//			throw new BusinessException(this.getBillName() + "状态为" + status.getText() + ", 不允许审核操作");
//		}
		final String billName = this.getBillName();
		this.updateStatus(splitId, SplitStatus.New, SplitStatus.pass, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> demand) throws BusinessException {
				throw new BusinessException(billName + "[" + this.getNum() + "]状态为" + this.getStatus(DemandStatus.class).getText() + ", 不允许确认");
			}
		}, user);

		//商品出库
		SplitProductSource splitProductSource = SplitProductSource.valueOf(MapUtils.getString(split, "productSource"));
		Integer productId = MapUtils.getInteger(split, "productId");

		String remark = "拆旧单号：["+MapUtils.getString(split, "num")+"]";
	 	if (splitProductSource == SplitProductSource.product || splitProductSource == SplitProductSource.maintainProduct) {
	 		productInventoryService.deliveryByProductId(MzfEnum.BizType.warehouseOnSplit, productId, remark, InventoryStatus.onStorage, user);
	 	} else if (splitProductSource == SplitProductSource.secondProduct) {
	 		Map<String, Object> inventory = secondProductInventoryService.getInventoryForSecondProduct(productId, null);
	 		Integer sourceOrgId = MapUtils.getInteger(inventory, "sourceOrgId");
			//生成结算单
			String price = MapUtils.getString(split, "settlementPrice");
			if (StringUtils.isBlank(price)) {
				throw new BusinessException("结算价为空");
			}
			if (sourceOrgId != null) {
				settlementService.createForSplitSecondProduct(sourceOrgId, user.getOrgId(), splitId, new BigDecimal(price), remark, user);
	 		} else {
	 			throw new BusinessException("来源部门为空，不能生成结算单");
	 		}

	 		secondProductInventoryService.deliveryBySecondProductId(MzfEnum.BizType.warehouseOnSplit, productId, remark, InventoryStatus.onStorage, user);
	 	}

	 	//记录操作日志
	 	businessLogService.log("拆旧确认", "拆旧单号：" + MapUtils.getString(split, "num"), user);
	 	//原料入库
//		Map<String, Object> where = new HashMap<String, Object>();
//		where.put("splitId", splitId);
//		List<Map<String, Object>> list = entityService.list(MzfEntity.SPLIT_DEETAIL, where, null, User.getSystemUser());
//		for (Map<String, Object> rawmaterial : list) {
//			rawmaterial.remove("id");
//			rawmaterial.put("source", "split");
//			rawmaterial.put("sourceId", productId);
//			registerRawmaterialService.registerRawmaterial(rawmaterial, user);
//		}
	}

	public int createSplitRawmaterialOrder(Integer[] splitIds, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("status", SplitStatus.pass);
		where.put("id", splitIds);
		List<Map<String, Object>> list = entityService.list(this.getBillMetadata(), where, null, user.asSystem());
		Integer sourceOrgId = null;
		if (CollectionUtils.isNotEmpty(list)) {
			sourceOrgId = MapUtils.getInteger(list.get(0), "sourceOrgId");
			for (Map<String, Object> split : list) {
				if (MapUtils.getIntValue(split, "sourceOrgId") != sourceOrgId.intValue()) {
					throw new BusinessException("来源部门不一致，不允许汇总");
				}
			}
		} else {
			throw new BusinessException("集合为空");
		}


		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", SplitStatus.over);
		field.put("muserId", null);
		field.put("muserName", null);
		field.put("mdate", null);
		int row = entityService.update(this.getBillMetadata(), field, where, user);
		if (row != splitIds.length) {
			throw new BusinessException("汇总原料操作异常");
		}

		Map<String, Object> order = new HashMap<String, Object>();
		order.put("sourceOrgId", sourceOrgId);
		List<Map<String, Object>> summary = summary(splitIds);
		int orderId = rawmaterialOrderService.createOrder(order, summary,  VendorOrderDetailStatus.waitReceive, VendorOrderType.splitRawmaterial, user);

		field = new HashMap<String, Object>();
		field.put("status", VendorOrderStatus.submit);
		entityService.updateById(MzfEntity.VENDOR_ORDER, Integer.toString(orderId), field, user);

		return orderId;
	}

	public List<Map<String, Object>> summary(Integer[] splitIds) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("splitId", splitIds);

		List<Map<String, Object>> summary = new ArrayList<Map<String,Object>>();
		Map<String, Map<String, Object>> tempMap = new HashMap<String, Map<String,Object>>();

		final String splitChar = "-";
		List<Map<String, Object>> list = entityService.list(MzfEntity.SPLIT_DEETAIL, where, null, User.getSystemUser());
		for (Map<String, Object> detail : list) {
			detail.remove("id");
			detail.remove("splitId");
			RawmaterialType type = RawmaterialType.valueOf(MapUtils.getString(detail, "type"));
			if (type == RawmaterialType.gold) {
				GoldClass goldClass = GoldClass.valueOf(MapUtils.getString(detail, "goldClass"));
				String key = type.toString() + splitChar + goldClass.toString();
				Map<String, Object> map = detail;
				if (tempMap.containsKey(key)) {
					map =  tempMap.get(key);
				} else {
					tempMap.put(key, map);
					continue;
				}
				BigDecimal preCost = (BigDecimal) MapUtils.getObject(map, "cost", new BigDecimal(0));
				BigDecimal preQuantity = (BigDecimal) MapUtils.getObject(map, "quantity", new BigDecimal(0));

				BigDecimal cost = (BigDecimal) MapUtils.getObject(detail, "cost", new BigDecimal(0));
				BigDecimal quantity = (BigDecimal) MapUtils.getObject(detail, "quantity", new BigDecimal(0));

				map.put("cost", preCost.add(cost));
				map.put("quantity", preQuantity.add(quantity));
			} else if (type == RawmaterialType.parts) {
				GoldClass goldClass = GoldClass.valueOf(MapUtils.getString(detail, "goldClass"));
				String partsType = MapUtils.getString(detail, "partsType");
				String partsStandard = MapUtils.getString(detail, "partsStandard");
				String key =  type.toString() + splitChar + goldClass.toString() + splitChar + partsType + splitChar + partsStandard;
				Map<String, Object> map = detail;
				if (tempMap.containsKey(key)) {
					map =  tempMap.get(key);
				} else {
					tempMap.put(key, map);
					continue;
				}
				BigDecimal preCost = (BigDecimal) MapUtils.getObject(map, "cost", new BigDecimal(0));
				BigDecimal preQuantity = (BigDecimal) MapUtils.getObject(map, "quantity", new BigDecimal(0));

				BigDecimal cost = (BigDecimal) MapUtils.getObject(detail, "cost", new BigDecimal(0));
				BigDecimal quantity = (BigDecimal) MapUtils.getObject(detail, "quantity", new BigDecimal(0));

				map.put("cost", preCost.add(cost));
				map.put("quantity", preQuantity.add(quantity));
			} else if (type == RawmaterialType.gravel) {
				String gravelStandard = MapUtils.getString(detail, "gravelStandard");
				String key = type.toString() + splitChar + gravelStandard;
				Map<String, Object> map = detail;
				if (tempMap.containsKey(key)) {
					map =  tempMap.get(key);
				} else {
					tempMap.put(key, map);
					continue;
				}
				BigDecimal preWeight = (BigDecimal) MapUtils.getObject(map, "weight", new BigDecimal(0));
				BigDecimal preCost = (BigDecimal) MapUtils.getObject(map, "cost", new BigDecimal(0));
				BigDecimal preQuantity = (BigDecimal) MapUtils.getObject(map, "quantity", new BigDecimal(0));

				BigDecimal weight = (BigDecimal) MapUtils.getObject(detail, "weight", new BigDecimal(0));
				BigDecimal cost = (BigDecimal) MapUtils.getObject(detail, "cost", new BigDecimal(0));
				BigDecimal quantity = (BigDecimal) MapUtils.getObject(detail, "quantity", new BigDecimal(0));

				map.put("weight", preWeight.add(weight));
				map.put("cost", preCost.add(cost));
				map.put("quantity", preQuantity.add(quantity));
			} else if (type == RawmaterialType.nakedDiamond) {
				summary.add(detail);
			}
		}

		summary.addAll(tempMap.values());

		return summary;
	}


	public Map<String, Object> getPrintData(Map<String, Object> param, IUser user)throws BusinessException{
		String startDate = MapUtils.getString(param, "startDate", "");
		String endDate = MapUtils.getString(param, "endDate", "");
		String productSource = MapUtils.getString(param, "productSource", "");

		Map<String, Object> data = new HashMap<String, Object>();
		List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SPLIT);
		 Filter filter = Filter.field("convert(varchar(64),cdate,23)").ge(startDate);
		 filter.and(Filter.field("convert(varchar(64),cdate,23)").le(endDate));
		 filter.and(Filter.field("status").eq("over"));
		 if(productSource != null && !productSource.equals("")){
			 filter.and(Filter.field("product_source").eq(productSource));
		 }
		 List<Map<String, Object>> splits = entityService.list(metadata, filter, null, user);
		 for (Map<String, Object> map : splits) {
			Map<String, Object> product = new HashMap<String, Object>();
			Integer productId = MapUtils.getInteger(map, "productId", 0);
			String proSource = MapUtils.getString(map, "productSource","");
			String proSourceText = BizCodeService.getBizName("splitProductSource", proSource);

			if(proSource.equals("secondProduct")){
				 product = entityService.getById(MzfEntity.SECOND_PRODUCT, productId, user);
			}
			if(proSource.equals("product")){
				 product = entityService.getById(MzfEntity.PRODUCT, productId, user);
			}
			product.put("wholesalePrice", MapUtils.getDoubleValue(map, "wholesalePrice"));
			product.put("settlementPrice", MapUtils.getDoubleValue(map, "settlementPrice"));
			product.put("splitNum", MapUtils.getString(map, "num"));
			product.put("productSourceText", proSourceText);
			dataList.add(product);
		}
		data.put("startDate", startDate);
		data.put("endDate", endDate);
		data.put("dataList", dataList);
		return data;

	}

	@Override
	protected EntityMetadata getBillMetadata() throws BusinessException {
		return metadataProvider.getEntityMetadata(MzfEntity.SPLIT);
	}

	@Override
	protected String getBillName() {
		return "拆旧记录";
	}
}


