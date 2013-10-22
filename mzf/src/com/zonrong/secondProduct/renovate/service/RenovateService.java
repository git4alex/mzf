package com.zonrong.secondProduct.renovate.service;

import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.SplitStatus;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.common.utils.MzfUtils.BillPrefix;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.inventory.product.service.SecondProductInventoryService;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.secondProduct.split.service.SplitService.SplitProductSource;
import com.zonrong.settlement.service.SettlementService;
import com.zonrong.system.service.BizCodeService;
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
public class RenovateService {
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
	private SecondProductInventoryService secondProductInventoryService;
	@Resource
	private SettlementService settlementService;

	private int createRenovate(Map<String, Object> renovate, int secondProductId, int newProductId, IUser user) throws BusinessException {
		String num = MzfUtils.getBillNum(BillPrefix.FX, user);
		renovate.put("num", num);

		renovate.put("productSource", SplitProductSource.secondProduct);
		renovate.put("productId", secondProductId);
		renovate.put("newProductId", newProductId);
		renovate.put("cuserId", user.getId());
		renovate.put("cuserName", user.getName());
		renovate.put("cdate", null);
		renovate.put("status", SplitStatus.New);
		String id = entityService.create(MzfEntity.RENOVATE, renovate, user);
		int splitId = Integer.parseInt(id);

		return splitId;
	}

	public void createRenovate(int secondProductId, Map<String, Object> renovate, IUser user) throws BusinessException {
		String priceStr = MapUtils.getString(renovate, "settlementPrice");
		if (StringUtils.isBlank(priceStr)) {
			throw new BusinessException("结算价为空");
		}

		BigDecimal price = new BigDecimal(priceStr);
		String remark = MapUtils.getString(renovate, "remark");
		Map<String, Object> inventory = secondProductInventoryService.getInventoryForSecondProduct(secondProductId, user.getOrgId());
		if (MapUtils.isEmpty(inventory)) {
			throw new BusinessException("非本部门旧饰， 不能翻新");
		}

		//入临时库
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.SECOND_PRODUCT);
		Map<String, Object> secondProduct = entityService.getById(metadata, secondProductId, user);
		String num = MapUtils.getString(secondProduct, "num");
		ProductStatus status = ProductStatus.free;
		String statusRemark = "旧饰翻新，旧饰条码[" + num + "]";
		secondProduct.remove("num");
		secondProduct.remove("imageId");
		secondProduct.remove("id");
		Map<String, Object> product = new HashMap<String, Object>(secondProduct);
		product.put("source", "renovate");
		product.put("sourceId", num);
		int newProductId = productService.createProduct(product, null, null, status, statusRemark, user);

        //旧饰出库
        product = entityService.getById(MzfEntity.PRODUCT,newProductId,user);
        secondProductInventoryService.deliveryBySecondProductId(BizType.renovate, secondProductId,
                "新商品条码：["+MapUtils.getString(product,"num")+"]", MzfEnum.InventoryStatus.onStorage, user);

		//商品入库
		StorageType storageType = StorageType.product_temporary;
		productInventoryService.warehouse(BizType.renovate, newProductId, user.getOrgId(), storageType, user.getId(), user.getOrgId(), "旧饰条码：[" + num + "]", user);

		//记录翻新操作
		int renvoteId = createRenovate(renovate, secondProductId, newProductId, user);

		//生成结算单
 		Integer sourceOrgId = MapUtils.getInteger(inventory, "sourceOrgId");

		//生成结算单
		if (sourceOrgId != null) {
			settlementService.createForRenvoteSecondProduct(sourceOrgId.intValue(), user.getOrgId(), renvoteId, price, remark, user);
 		} else {
 			throw new BusinessException("来源部门为空，不能生成结算单");
 		}
	}

	public Map<String, Object> getPrintData(Map<String, Object> param, IUser user)throws BusinessException{
		String startDate = MapUtils.getString(param, "startDate", "");
		String endDate = MapUtils.getString(param, "endDate", "");
		String productSource = MapUtils.getString(param, "productSource", "");

		Map<String, Object> data = new HashMap<String, Object>();
		List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RENOVATE);
		 Filter filter = Filter.field("convert(varchar(64),cdate,23)").ge(startDate);
		 filter.and(Filter.field("convert(varchar(64),cdate,23)").le(endDate));
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
			product.put("renvoateNum", MapUtils.getString(map, "num"));
			product.put("productSourceText", proSourceText);
			dataList.add(product);
		}
		data.put("startDate", startDate);
		data.put("endDate", endDate);
		data.put("dataList", dataList);
		return data;

	}



}


