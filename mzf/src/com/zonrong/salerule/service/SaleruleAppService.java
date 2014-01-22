package com.zonrong.salerule.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEnum.SaleDetailType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.inventory.service.ProductInventoryService;
import com.zonrong.salerule.service.expression.ExpressionProcessor;
import com.zonrong.salerule.service.expression.RuleEvaluationContext;
import com.zonrong.salerule.service.mapper.PresentMapper;
import com.zonrong.salerule.service.result.DiscountResultService;
import com.zonrong.salerule.service.result.GrantChitResultService;
import com.zonrong.salerule.service.result.MaterialResultService;
import com.zonrong.salerule.service.result.PointsResultService;
import com.zonrong.salerule.service.result.ProductResultService;

@Service
public class SaleruleAppService {
	private Logger logger = Logger.getLogger(this.getClass());

	private static String SALERULE_RESULTIDS = "saleruleResultIds";
	private static String SALERULE_PRICE = "salerulePrice";
	@Resource
	private PointsResultService pointsResultService;
	@Resource
	private DiscountResultService discountResultService;
	@Resource
	private ProductResultService productResultService;
	@Resource
	private MaterialResultService materialResultService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private GrantChitResultService grantChitResultService;

	@Resource
	private PointsruleService pointsruleService;
	@Resource
	private SaleruleService saleruleService;
	@Resource
	private SaleruleMatchService saleruleMatchService;
	@Resource(name = "saleService1")
	private SaleService saleService;

	public void init(Map<String, Object> bill) throws BusinessException {
		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");
		for (Map<String, Object> detail : details) {
			detail.put(SALERULE_PRICE, MapUtils.getObject(detail, "price"));
		}

		calcAndPoints(bill);
	}

	public boolean isNext(Map<String, Object> bill) throws BusinessException {
		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");
		for (Map<String, Object> detail : details) {
			if (MapUtils.getFloat(detail, "authorithDiscount", 0f) != 0) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 匹配整单的单品促销规则
	 *
	 * @param bill
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, List<Map<String, Object>>> matchSaleruleForSingle(Map<String, Object> bill, IUser user) throws BusinessException {
		Map<String, List<Map<String, Object>>> productIdRules = new HashMap<String, List<Map<String,Object>>>();

		List<Integer> productIds = new ArrayList<Integer>();
		Integer customerId = MapUtils.getInteger(bill, "cusId");
		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			float authorithDiscount = MapUtils.getFloatValue(detail, "authorithDiscount"); //权限折扣
			if (type == SaleDetailType.product && authorithDiscount == 0) {
				Integer productId = MapUtils.getInteger(detail, "targetId");
				productIds.add(productId);
			}
		}

		if (CollectionUtils.isEmpty(productIds)) {
			return productIdRules;
		}

		List<Map<String, Object>> productInventorys = productInventoryService.listProductInventory(productIds.toArray(new Integer[]{}), user.getOrgId());
		for (Map<String, Object> productInventory : productInventorys) {
//			Integer productId = MapUtils.getInteger(productInventory, "id");
			String num = MapUtils.getString(productInventory, "num");

			List<Map<String, Object>> rules = saleruleMatchService.matchSaleruleForSingle(productInventory, customerId, new Date());
			if(CollectionUtils.isNotEmpty(rules)){
//				productIdRules.put(productId, rules);
				productIdRules.put(num, rules);
			}
		}

		return productIdRules;
	}

	/**
	 * 应用单品促销规则，只处理折扣和积分
	 * @param bill
	 * @param productIdRuleResults
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> appSingleRulesOfDiscountAndPoints(Map<String, Object> bill, Map<String, List<Integer>> productIdRuleResults, IUser user) throws BusinessException {
		Map<Integer, Map<String, Object>> productIdDiscountAndPoints = getSingleRulesOfDiscountAndPoints(productIdRuleResults, user);

		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");
		float amount = 0;
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (type == SaleDetailType.product) {
				Integer productId = MapUtils.getInteger(detail, "targetId");
				Map<String, Object> discountAndPoints = productIdDiscountAndPoints.get(productId);
				detail.putAll(discountAndPoints);

				//处理折扣
				BigDecimal price = new BigDecimal(MapUtils.getString(detail, "price"));
				BigDecimal salerulePrice = (BigDecimal) MapUtils.getObject(discountAndPoints, SALERULE_PRICE);
				detail.put("saleDiscount", price.subtract(salerulePrice));
				amount += MapUtils.getFloatValue(discountAndPoints, SALERULE_PRICE);
			}
		}
//		bill.put("amount", amount);
		return bill;
	}

	/**
	 * 匹配整单规则
	 *
	 * @param bill
	 * @param user
	 * @return	规则列表
	 * @throws BusinessException
	 */
	public List<Map<String, Object>> matchSaleruleForBill(Map<String, Object> bill, IUser user) throws BusinessException {
		//bill.put("xxx", 2);
		List<Map<String, Object>> products = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (type == SaleDetailType.product) {
				products.add(detail);
			}
		}
		List<Map<String, Object>> rules = saleruleMatchService.matchSaleruleForBill(bill, products, new Date());
		return rules;
	}

	/**
	 * 应用整单促销规则，只处理折扣和积分
	 *
	 * @param bill
	 * @param saleruleResultIds
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> appBillRulesOfDiscountAndPoints(Map<String, Object> bill, List<Integer> saleruleResultIds, IUser user) throws BusinessException {
		int orgId = user.getOrgId();
		Integer[] resultIds = saleruleResultIds.toArray(new Integer[]{});

		List<Map<String, Object>> results = saleruleService.listResult(resultIds);
		//积分
		pointsResultService.setContext(null, bill);
		int presentPoints = pointsResultService.getResult(results, orgId);
		//折扣
		discountResultService.setContext(null, bill);
		BigDecimal price = discountResultService.getResult(results, orgId);
		if (price == null) {
			price = new BigDecimal(0);
		}
		BigDecimal totalAmount = new BigDecimal(MapUtils.getString(bill, "totalAmount"));
		//往每件商品上 分摊整单折扣
		int shareBillDiscount = 0;
		if (price.floatValue() > 0) {
			shareBillDiscount = totalAmount.subtract(price).intValue();
			shareBillDiscount(bill, shareBillDiscount);
		}

		bill.put("points", presentPoints);
		bill.put("shareBillDiscount", shareBillDiscount);
		bill.put(SALERULE_RESULTIDS, StringUtils.join(resultIds, ","));

		return bill;
	}

	/**
	 * 赠品列表
	 * 1.可选择的商品
	 * 2.可选择的物料
	 * 3.代金券有用户自行录入
	 *
	 * @param bill
	 * @param orgId
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> getPresent(Map<String, Object> bill, IUser user) throws BusinessException {
		int orgId = user.getOrgId();
		List<Integer> saleruleResultIds = findSaleruleResultIdsFromBill(bill, orgId);
		Map<String, Object> present = new HashMap<String, Object>();
		List<Map<String, Object>> results = saleruleService.listResult(saleruleResultIds.toArray(new Integer[]{}));

		//物料
		List<Map<String, Object>> materials = materialResultService.getResult(results, orgId);
		//商品
		List<Map<String, Object>> products = productResultService.getResult(results, orgId);

		grantChitResultService.setContext(bill);
		//赠送代金券
		List<Map<String, Object>> grantChits = grantChitResultService.getResult(results, orgId);

		if (CollectionUtils.isNotEmpty(materials)) {
			present.put("materials", materials);
		}
		if (CollectionUtils.isNotEmpty(products)) {
			present.put("products", products);
		}
		if(CollectionUtils.isNotEmpty(grantChits)){
			present.put("chits", grantChits);
		}

		return present;
	}

	/**
	 * 将选择的赠品加入销售明细
	 *
	 * @param bill
	 * @param present
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> addPresent(Map<String, Object> bill, Map<String, Object> present) throws BusinessException {
		checkGrantChit(null, null);
		//checkChitPay(null, null);
		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");

		//物料
		List<Map<String, Object>> materials = (List<Map<String, Object>>) MapUtils.getObject(present, "materials");
		if (CollectionUtils.isNotEmpty(materials)) {
			for (Map<String, Object> material : materials) {
				if (MapUtils.getBoolean(material, "checked")) {
					material.put("type", SaleDetailType.present_material);
					material.put("quantity", MapUtils.getInteger(material, "checkedQuantity", 1));
					material.put("targetId", MapUtils.getString(material, "id"));
					material.put("targetNum", MapUtils.getString(material, "num"));
					material.put("targetName", MapUtils.getString(material, "name"));
					material.put("price", MapUtils.getFloatValue(material, "price"));
					material.put(SALERULE_PRICE, MapUtils.getFloatValue(material, SALERULE_PRICE));
					details.add(material);

				}
			}
		}
		 if(!validatePresent(materials, SaleDetailType.present_material)){
			 throw new BusinessException("选择的物料不符合赠送条件");
		 }

		//商品
		List<Map<String, Object>> products = (List<Map<String, Object>>) MapUtils.getObject(present, "products");
		if (CollectionUtils.isNotEmpty(products)) {
			for (Map<String, Object> product : products) {
				if (MapUtils.getBoolean(product, "checked")) {
					product.put("type", SaleDetailType.present_product);
					product.put("quantity", 1);
					product.put("targetId", MapUtils.getString(product, "id"));
					product.put("targetNum", MapUtils.getString(product, "num"));
					product.put("targetName", MapUtils.getString(product, "name"));
					product.put("price", 0);
					product.put(SALERULE_PRICE, 0);
					details.add(product);
				}
			}
		}
		if(!validatePresent(products, SaleDetailType.present_product)){
			 throw new BusinessException("选择的商品不符合赠送条件");
		 }
		//代金券
		List<Map<String, Object>> chits = (List<Map<String, Object>>) MapUtils.getObject(present, "chits");
		if (CollectionUtils.isNotEmpty(chits)) {
			for (Map<String, Object> chit : chits) {
				if (MapUtils.getBoolean(chit, "checked")) {
					chit.put("type", SaleDetailType.present_chit);
					chit.put("quantity", 1);
					chit.put("targetId", MapUtils.getString(chit, "id"));
					chit.put("targetNum", MapUtils.getString(chit, "num"));
					chit.put("targetName", MapUtils.getString(chit, "name"));
					chit.put("price", 0);
					chit.put(SALERULE_PRICE, 0);
					details.add(chit);
				}
			}
		}
	    if(!validateChit(chits, MapUtils.getFloatValue(bill, "amount"))){
	    	throw new BusinessException("选择的代金券不符合赠送条件");
	    }
		return bill;
	}

	/**
	 * 销售开单确认
	 *
	 * @param bill
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public int sale(Map<String, Object> bill, IUser user) throws BusinessException {
		List<Map<String, Object>> detailList = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");

		return saleService.createSale(bill, detailList, user);
	}

	/**
	 * 合并支付代金券
	 */
	public Map<String, Object> mergePayChit(Map<String, Object> bill, List<Map<String, Object>> chits, IUser user) throws BusinessException{
		int orgId = user.getOrgId();
		List<Integer> resultsId = findSaleruleResultIdsFromBill(bill, orgId);
		float chit = 0;
		List<Map<String, Object>> detailList = (List<Map<String, Object>>)bill.get("detailList");
		List<Map<String, Object>> newDetailList = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> detail : detailList) {
			if(!String.valueOf(SaleDetailType.returnsChit).equals(MapUtils.getString(detail, "type"))){
				newDetailList.add(detail);
			}
		}
		//合并重复代金券
		Map<String, Object> newChits = new HashMap<String, Object>();
		for (Map<String, Object> map : chits) {
			 String name = MapUtils.getString(map, "name");
			 float actualValue = MapUtils.getFloatValue(map, "actualValue");
		     if(newChits.containsKey(name)){
		    	 Map<String, Object> chitMap = (Map<String, Object>)newChits.get(name);
		    	 chitMap.put("actualValue", MapUtils.getFloatValue(chitMap, "actualValue") + actualValue);
		    	 chitMap.put("checkedTotalCount", MapUtils.getFloatValue(chitMap, "checkedTotalCount") + 1);
		    	 newChits.put(name, chitMap);
		     }else{
		    	 map.put("checkedTotalCount", 1);
		    	 newChits.put(name, map);
		     }

		}

		chits = new ArrayList<Map<String,Object>>();
		Set<String> keys = newChits.keySet();
		int totalPoints = 0;
		for (String key : keys) {
			Map<String, Object> payChit = (Map<String, Object>)newChits.get(key);
			float actualValue = MapUtils.getFloat(payChit, "actualValue");
			BigDecimal price = new BigDecimal(actualValue);
			int points = getLostPoints(price, null);
			totalPoints += points;
			payChit.put("price", actualValue);
			payChit.put("targetName", MapUtils.getString(payChit, "name"));
			payChit.put("targetId", MapUtils.getString(payChit, "id"));
			payChit.put("type", SaleDetailType.returnsChit);
			payChit.put("points", points);
			chit += MapUtils.getFloatValue(payChit, "actualValue");
			chits.add(payChit);
		}
		//验证代金券支付
		if(!checkChitPay(resultsId.toArray(new Integer[]{}),chits, MapUtils.getFloatValue(bill, "totalAmount"))){
			throw new BusinessException("代金券支付不符合促销规则");
		}
		newDetailList.addAll(chits);
		bill.put("detailList", newDetailList);
		bill.put("chit", chit);
		bill.put("points", MapUtils.getInteger(bill, "points") + totalPoints);
		return bill;
	}
	/**
	 * 获取打印数据
	 *
	 * @param saleId
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> getPrintData(int saleId, IUser user) throws BusinessException {
		return saleService.getPrintData(saleId, user);
	}

	/**
	 * 验证商品和物料
	 * @param presents
	 * @param type
	 * @return
	 */
	private boolean validatePresent(List<Map<String, Object>> presents,SaleDetailType type){
		Map<String, Object> validateCondition = new HashMap<String, Object>();
		//组合赠品验证条件
		if(type.equals(SaleDetailType.present_product)){
			for (Map<String, Object> product : presents) {
				String groupFlag = MapUtils.getString(product, "groupFlag");
				if (MapUtils.getBoolean(product, "checked")) {
					if(validateCondition.containsKey(groupFlag)){
						Map<String, Object> groupCondition = (Map<String, Object>)validateCondition.get(groupFlag);
						groupCondition.put("checkedTotalCount", MapUtils.getInteger(groupCondition, "checkedTotalCount") + 1);
						groupCondition.put("checkedTotalPrice", MapUtils.getInteger(groupCondition, "checkedTotalPrice") + MapUtils.getIntValue(product, "retailBasePrice"));
					}else{
						Map<String, Object> groupCondition = new HashMap<String, Object>();
						groupCondition.put("checkedTotalCount", 1);
						groupCondition.put("checkedTotalPrice", MapUtils.getIntValue(product, "retailBasePrice"));
						groupCondition.put("totalCountCon", MapUtils.getIntValue(product, "ruleCount"));
						groupCondition.put("totalPriceCon", MapUtils.getIntValue(product, "rulePrice"));
						groupCondition.put("countOpt", MapUtils.getString(product, "ruleCountOpt"));
						groupCondition.put("priceOpt", MapUtils.getString(product, "rulePriceOpt"));
						validateCondition.put(groupFlag, groupCondition);
					}
				}
			}
		}else if(type.equals(SaleDetailType.present_material)){
              for (Map<String, Object> material : presents) {
					String groupFlag = MapUtils.getString(material, "groupFlag");
					if (MapUtils.getBoolean(material, "checked")) {
						if(validateCondition.containsKey(groupFlag)){
							Map<String, Object> groupCondition = (Map<String, Object>)validateCondition.get(groupFlag);
							groupCondition.put("checkedTotalCount", MapUtils.getInteger(groupCondition, "checkedTotalCount") + MapUtils.getIntValue(material, "checkedQuantity"));
							groupCondition.put("checkedTotalPrice", MapUtils.getInteger(groupCondition, "checkedTotalPrice") + MapUtils.getIntValue(material, "retailPrice"));
						}else{
							Map<String, Object> groupCondition = new HashMap<String, Object>();
							groupCondition.put("checkedTotalCount", MapUtils.getIntValue(material, "checkedQuantity"));
							groupCondition.put("checkedTotalPrice", MapUtils.getIntValue(material, "retailPrice") * MapUtils.getIntValue(material, "checkedQuantity"));
							groupCondition.put("totalCountCon", MapUtils.getIntValue(material, "ruleCount"));
							groupCondition.put("totalPriceCon", MapUtils.getIntValue(material, "rulePrice"));
							groupCondition.put("countOpt", MapUtils.getString(material, "ruleCountOpt"));
							groupCondition.put("priceOpt", MapUtils.getString(material, "rulePriceOpt"));
							validateCondition.put(groupFlag, groupCondition);
						}
					}
			}
		}
		Set<String> keys = validateCondition.keySet();
		for (String key : keys) {
			Map<String, Object> condition = (Map<String, Object>)validateCondition.get(key);
			String countOpt = MapUtils.getString(condition, "countOpt");
			String priceOpt = MapUtils.getString(condition, "priceOpt");
			String priceExp = "checkedTotalPrice "+priceOpt+" totalPriceCon";
			String countExp = "checkedTotalCount "+countOpt+" totalCountCon";
			boolean isValidatePrice = getPresentExp(priceExp, condition);
			boolean isValidateCount = getPresentExp(countExp, condition);
			if(!isValidatePrice || !isValidateCount){
				return false;
			}

	  }
		return true;
	}

	 /**
	  * 验证代金券
	  * @param chits
	  * @param amount
	  * @return
	  */
	private boolean validateChit(List<Map<String, Object>> chits, float amount){
		Map<String, Object> validateCondition = new HashMap<String, Object>();
		 for (Map<String, Object> chit : chits) {
				String groupFlag = MapUtils.getString(chit, "groupFlag");
				if (MapUtils.getBoolean(chit, "checked")) {
					if(validateCondition.containsKey(groupFlag)){
						Map<String, Object> groupCondition = (Map<String, Object>)validateCondition.get(groupFlag);
						groupCondition.put("checkedTotalCount", MapUtils.getInteger(groupCondition, "checkedTotalCount") + 1);
						groupCondition.put("checkedTotalPrice", MapUtils.getInteger(groupCondition, "checkedTotalPrice") + MapUtils.getFloat(chit, "faceValue"));
					}else{
						Map<String, Object> groupCondition = new HashMap<String, Object>();
						groupCondition.put("checkedTotalCount", 1);
						groupCondition.put("checkedTotalPrice", MapUtils.getIntValue(chit, "faceValue"));
						groupCondition.put("totalCountCon", MapUtils.getIntValue(chit, "totalCount"));
						groupCondition.put("totalPriceCon", MapUtils.getIntValue(chit, "totalFaceValue"));
						groupCondition.put("countOpt", MapUtils.getString(chit, "totalCountOpt"));
						groupCondition.put("priceOpt", MapUtils.getString(chit, "totalFaceValueOpt"));
						groupCondition.put("rateCon", MapUtils.getObject(chit, "rate"));
						groupCondition.put("rateOpt", MapUtils.getObject(chit, "rateOpt"));
						validateCondition.put(groupFlag, groupCondition);
					}
				}
		}

		 Set<String> keys = validateCondition.keySet();
			for (String key : keys) {
				Map<String, Object> condition = (Map<String, Object>)validateCondition.get(key);
				BigDecimal checkedTotalPrice = new BigDecimal(MapUtils.getFloatValue(condition,"checkedTotalPrice"));
				BigDecimal rate = checkedTotalPrice.divide(new BigDecimal(amount),3,BigDecimal.ROUND_CEILING);
				condition.put("rate", rate);

				String countOpt = MapUtils.getString(condition, "countOpt");
				String priceOpt = MapUtils.getString(condition, "priceOpt");
				String rateOpt = MapUtils.getString(condition, "rateOpt");
				String priceExp = "checkedTotalPrice "+priceOpt+" totalPriceCon";
				String countExp = "checkedTotalCount "+countOpt+" totalCountCon";
			    String rateExp = "rate" + rateOpt + "rateCon";
				boolean isValidatePrice = getPresentExp(priceExp, condition);
				boolean isValidateCount = getPresentExp(countExp, condition);
				boolean isValidateRate = getPresentExp(rateExp, condition);
				if(!isValidatePrice || !isValidateCount || !isValidateRate){
					return false;
				}

		  }
			return true;

	}

	private void shareBillDiscount(Map<String, Object> bill, int discount) throws BusinessException {
		BigDecimal totalSalerulePrice = new BigDecimal(0);
		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");
		List<Map<String, Object>> products = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (type == SaleDetailType.product) {
				BigDecimal salerulePrice = new BigDecimal(MapUtils.getString(detail, SALERULE_PRICE));
				totalSalerulePrice = totalSalerulePrice.add(salerulePrice);
				products.add(detail);
			}
		}

		int sharedBillDiscount = 0;
		for (int i = 0; i < products.size(); i++) {
			int shareBillDiscount = 0;
			Map<String, Object> detail = products.get(i);

			if (i < products.size() - 1) {
				BigDecimal salerulePrice = new BigDecimal(MapUtils.getString(detail, SALERULE_PRICE));
				BigDecimal percent = salerulePrice.divide(totalSalerulePrice, 2, BigDecimal.ROUND_HALF_EVEN);
				shareBillDiscount = percent.multiply(new BigDecimal(discount)).intValue();
				sharedBillDiscount += shareBillDiscount;
			} else {
				shareBillDiscount = discount - sharedBillDiscount;
			}

			detail.put("shareBillDiscount", shareBillDiscount);
		}
	}

	private boolean getPresentExp(String exp, Map<String, Object> condition){
		PresentMapper<String, String> presentMapper = new PresentMapper<String, String>(condition);

		RuleEvaluationContext context = new RuleEvaluationContext();
		context.set(presentMapper);
		ExpressionProcessor processor = new ExpressionProcessor(exp);
		exp = processor.replace(presentMapper).getExpression();
		ExpressionParser p = new SpelExpressionParser();
		return p.parseExpression(exp).getValue(context,Boolean.class);
	}
	private void checkGrantChit(Integer[] resultIds, List<Map<String, Object>> chits) throws BusinessException {

	}

	private boolean checkChitPay(Integer[] resultIds, List<Map<String, Object>> chits, float amount) throws BusinessException {
		List<Map<String, Object>> results = saleruleService.listResult(resultIds);
		//验证代金券类型
		if(!isChitTypeInRule(results, chits)){
			return false;
		}
		//验证代金券数量
		if(!isChitCountInRule(results, chits)){
			return false;
		}

		try {
			//验证代金券总数量，总金额，使用比率
			int countFlag = 0;
			for (Map<String, Object> rule : results) {
				Map<String, Object> condition = new  HashMap<String, Object>();
				int checkedTotalCount = 0;
				float checkedTotalPrice = 0;
				for (Map<String, Object> chit : chits) {
					checkedTotalCount += MapUtils.getIntValue(chit, "checkedTotalCount");
					checkedTotalPrice += MapUtils.getFloatValue(chit, "actualValue");
				}
				String json = MapUtils.getString(rule, "resultChitPayJson");
				Map<String, Object> result = new ObjectMapper().readValue(json, Map.class);

				//组合验证条件
				condition.put("rateCon", MapUtils.getString(result, "rate"));
				condition.put("totalCountCon", MapUtils.getString(result, "totalCount"));
				condition.put("totalPriceCon", MapUtils.getString(result, "totalFaceValue"));
				condition.put("checkedTotalCount", checkedTotalCount);
				condition.put("checkedTotalPrice", checkedTotalPrice);
				//计算代金券使用比率
				BigDecimal checkedTotalPrice_o = new BigDecimal(checkedTotalPrice);
				BigDecimal rate = checkedTotalPrice_o.divide(new BigDecimal(amount),3,BigDecimal.ROUND_CEILING);
				condition.put("rate", rate);

				String countOpt = MapUtils.getString(result, "totalCountOpt");
				String priceOpt = MapUtils.getString(result, "totalFaceValueOpt");
				String rateOpt = MapUtils.getString(result, "rateOpt");
				String priceExp = "checkedTotalPrice "+priceOpt+" totalPriceCon";
				String countExp = "checkedTotalCount "+countOpt+" totalCountCon";
			    String rateExp = "rate" + rateOpt + "rateCon";
				boolean isValidatePrice = getPresentExp(priceExp, condition);
				boolean isValidateCount = getPresentExp(countExp, condition);
				boolean isValidateRate = getPresentExp(rateExp, condition);
				if(isValidatePrice && isValidateCount && isValidateRate){
					countFlag ++;
				}

			}
			if(countFlag > 0){
				return true;
			}else{
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}

	}
	/**
	 * 判断代金券类型是否符合促销规则
	 * @return
	 */
   private boolean isChitTypeInRule(List<Map<String, Object>> saleRules, List<Map<String, Object>> chits){
		try {
			int countFlag = 0;
			for (Map<String, Object> result : saleRules) {
				String json = MapUtils.getString(result, "resultChitPayJson");
				Map<String, Object> jsonMap = new ObjectMapper().readValue(json, Map.class);
				List<HashMap<String, Object>> list = new ObjectMapper().readValue(MapUtils.getString(jsonMap, "list"), List.class);
				List<Map<String, Object>> chitsRules = new ArrayList<Map<String,Object>>();
				for (HashMap<String, Object> map : list) {
					 String type = MapUtils.getString(map, "type");
					 for (Map<String, Object> chit : chits) {
						String name = MapUtils.getString(chit, "name");
						 if(type.equals(name)){
							 countFlag ++;
						 }
					}
				}
			}
			if(countFlag < chits.size()){
				return false;
			}else{
				return true;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			new BusinessException("代金券的类型不符合促销规则");
			return false;
		}

   }

   /**
    * 判断代金券的数量是否符合促销规则
    * @param saleRules
    * @param chits
    * @return
    */
   private boolean isChitCountInRule(List<Map<String, Object>> saleRules, List<Map<String, Object>> chits){
		try {
			int countFlag = 0;
			for (Map<String, Object> result : saleRules) {
				String json = MapUtils.getString(result, "resultChitPayJson");
				Map<String, Object> jsonMap = new ObjectMapper().readValue(json, Map.class);
				List<HashMap<String, Object>> list = new ObjectMapper().readValue(MapUtils.getString(jsonMap, "list"), List.class);
				List<Map<String, Object>> chitsRules = new ArrayList<Map<String,Object>>();
				for (HashMap<String, Object> map : list) {
					Map<String, Object> condition = new HashMap<String, Object>();
					 condition.put("totalCountCon", MapUtils.getIntValue(map, "count"));
					 String countOpt = MapUtils.getString(map, "countOpt");
					 for (Map<String, Object> chit : chits) {
						condition.put("checkedTotalCount", MapUtils.getIntValue(chit, "checkedTotalCount"));
						String countExp = "checkedTotalCount" + countOpt+ "totalCountCon";
						boolean validateCount = getPresentExp(countExp, condition);
						if(validateCount){
							countFlag ++;
						}
					}
				}
			}
			if(countFlag < chits.size()){
				return false;
			}else{
				return true;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			new BusinessException("代金券的数量不符合促销规则");
			return false;
		}

  }

	private List<Integer> findSaleruleResultIdsFromBill(Map<String, Object> bill, int orgId) throws BusinessException {
		List<String> resultIdStrs = new ArrayList<String>();

		List<Integer> ruleResultIds = new ArrayList<Integer>();
		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (type == SaleDetailType.product) {
				String saleruleResultIds = MapUtils.getString(detail, SALERULE_RESULTIDS);
				if (StringUtils.isNotBlank(saleruleResultIds)) {
					resultIdStrs.add(saleruleResultIds);
				}
			}
		}

		String saleruleResultIds = MapUtils.getString(bill, SALERULE_RESULTIDS);
		if (StringUtils.isNotBlank(saleruleResultIds)) {
			resultIdStrs.add(saleruleResultIds);
		}

		for (String str : resultIdStrs) {
			String[] resultIds = StringUtils.split(str, ",");
			for (String resultId : resultIds) {
				ruleResultIds.add(Integer.parseInt(StringUtils.trim(resultId)));
			}
		}

		return ruleResultIds;
	}

	private Map<Integer, Map<String, Object>> getSingleRulesOfDiscountAndPoints(Map<String, List<Integer>> productIdRuleResultIds, IUser user) throws BusinessException {
		int orgId = user.getOrgId();
		Map<String, Map<String, Object>> productMap = new HashMap<String, Map<String,Object>>();
		String[] nums = productIdRuleResultIds.keySet().toArray(new String[]{});
		List<Map<String, Object>> productInventorys = productInventoryService.listProductInventoryByNum(nums, orgId);
		for (Map<String, Object> product : productInventorys) {
			String productNum = MapUtils.getString(product, "num");
			productMap.put(productNum, product);
		}

		Map<Integer, Map<String, Object>> map = new HashMap<Integer, Map<String,Object>>();

		Iterator<String> it = productIdRuleResultIds.keySet().iterator();
		while (it.hasNext()) {
			String productNum = it.next();
			Integer[] resultIds = productIdRuleResultIds.get(productNum).toArray(new Integer[]{});

			List<Map<String, Object>> results = saleruleService.listResult(resultIds);
			Map<String, Object> product = productMap.get(productNum);
			int productId = MapUtils.getInteger(product, "id");
			//积分
			pointsResultService.setContext(product, null);
			int points = pointsResultService.getResult(results, orgId);
			//折扣
			discountResultService.setContext(product, null);
			BigDecimal price = discountResultService.getResult(results, orgId);

			Map<String, Object> result = new HashMap<String, Object>();
			result.put("points", points);
			result.put(SALERULE_PRICE, price);
			result.put(SALERULE_RESULTIDS, StringUtils.join(resultIds, ","));

			map.put(productId, result);
		}

		return map;
	}

	/**
	 * 将抹零折扣分摊到第一件商品上
	 * @param bill
	 * @throws BusinessException
	 */
	public void addClearDiscount(Map<String, Object> bill, int clearDiscount) throws BusinessException {
		if (clearDiscount < 0) {
			throw new BusinessException("抹零折扣必须大于0");
		}
		int oldClearDiscount = MapUtils.getIntValue(bill, "clearDiscount", 0);
		int subtract = clearDiscount - oldClearDiscount;
		if (subtract == 0) {
			return;
		}
		bill.put("clearDiscount", clearDiscount);
		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (type == SaleDetailType.product) {
				BigDecimal saleDiscount = new BigDecimal(MapUtils.getString(detail, "saleDiscount", Integer.toString(0)));
				detail.put("saleDiscount", saleDiscount.add(new BigDecimal(subtract)));
				break;
			}
		}

		calcAndPoints(bill);
	}

	public void calcAndPoints(Map<String, Object> bill) throws BusinessException {
		calc(bill);
		//points(bill);
	}

	private void calc(Map<String, Object> bill) {
		BigDecimal totalAmount = new BigDecimal(0);
		BigDecimal discount = new BigDecimal(0);
		BigDecimal frontMoney = new BigDecimal(0);
		BigDecimal goldPay = new BigDecimal(0);
		BigDecimal productPay = new BigDecimal(0);

		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));

			//应收金额
			if (type == SaleDetailType.product) {
				BigDecimal price = new BigDecimal(MapUtils.getString(detail, "price", Integer.toString(0)));
				totalAmount = totalAmount.add(price);
			}

			//折扣金额
			if (type == SaleDetailType.product || type == SaleDetailType.material) {
				System.out.println(detail);
				BigDecimal authorithDiscount = new BigDecimal(MapUtils.getString(detail, "authorithDiscount", Integer.toString(0)));
				BigDecimal saleDiscount = new BigDecimal(MapUtils.getString(detail, "saleDiscount", Integer.toString(0)));
				BigDecimal shareBillDiscount = new BigDecimal(MapUtils.getString(detail, "shareBillDiscount", Integer.toString(0)));

				BigDecimal totalDiscount = authorithDiscount.add(saleDiscount).add(shareBillDiscount);
				totalDiscount = new BigDecimal(Math.ceil(totalDiscount.doubleValue()));
				detail.put("totalDiscount", totalDiscount);
				discount = discount.add(totalDiscount);
			}

			//预售定金
			if (type == SaleDetailType.product) {
				String str = MapUtils.getString(detail, "frontMoney", Integer.toString(0));
				frontMoney = frontMoney.add(new BigDecimal(str));
			}

			//旧金支付
			if (type == SaleDetailType.secondGold) {
				String str = MapUtils.getString(detail, "price", Integer.toString(0));
				goldPay = goldPay.add(new BigDecimal(str));
			}
			//旧饰支付
			if (type == SaleDetailType.secondJewel) {
				String str = MapUtils.getString(detail, "price", Integer.toString(0));
				productPay = productPay.add(new BigDecimal(str));
			}
		}

		totalAmount = new BigDecimal(Math.floor(totalAmount.doubleValue()));
		discount = new BigDecimal(Math.ceil(discount.doubleValue()));

		bill.put("totalAmount", totalAmount);
		bill.put("discount", discount);
		bill.put("frontMoney", frontMoney);
		bill.put("goldPay", goldPay);
		bill.put("productPay", productPay);
		BigDecimal amount = frontMoney.add(goldPay).add(productPay);
		amount = new BigDecimal(Math.ceil(amount.doubleValue()));
		bill.put("amount", amount);
	}

	private void points(Map<String, Object> bill) throws BusinessException {
		int orgId = MapUtils.getInteger(bill, "orgId");
		List<Map<String, Object>> details = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");

		int totalPoints = 0;
		for (Map<String, Object> detail : details) {
			SaleDetailType type = SaleDetailType.valueOf(MapUtils.getString(detail, "type"));
			if (type == SaleDetailType.product) {
				Integer productId = MapUtils.getInteger(detail, "targetId");
				BigDecimal salerulePrice = new BigDecimal(MapUtils.getString(detail, SALERULE_PRICE, Integer.toString(0)));
				BigDecimal otherCharges = new BigDecimal(MapUtils.getString(detail, "otherCharges", Integer.toString(0)));
				BigDecimal totalDiscount = new BigDecimal(MapUtils.getString(detail, "totalDiscount", Integer.toString(0)));
//				int points = MapUtils.getIntValue(detail, "points", 0);
//				points += pointsruleService.getPoints(productId, salerulePrice, otherCharges, totalDiscount, orgId);
				//int points = pointsruleService.getPoints(productId, salerulePrice, otherCharges, totalDiscount, orgId);
				int points = MapUtils.getIntValue(detail, "points");
				detail.put("points", points);

				totalPoints += points;
			}

			//旧金支付
			if (type == SaleDetailType.secondGold) {
				BigDecimal goldPay = new BigDecimal(MapUtils.getString(detail, "price", Integer.toString(0)));
				int points = getLostPoints(goldPay, null);
				detail.put("points", points);

				totalPoints += points;
			}
			//旧饰支付
			if (type == SaleDetailType.secondJewel) {
				BigDecimal productPay = new BigDecimal(MapUtils.getString(detail, "price", Integer.toString(0)));
				int points = getLostPoints(productPay, null);
				detail.put("points", points);

				totalPoints += points;
			}
		}
		int presentPoints = MapUtils.getInteger(bill, "presentPoints", 0);
		bill.put("points", totalPoints);
	}

	private int getLostPoints(BigDecimal price, String config) throws BusinessException {
		if (price != null) {
			BigDecimal lostPoints = price.divide(new BigDecimal(500), 0, BigDecimal.ROUND_FLOOR);
			return lostPoints.intValue() * -1;
		}
		return 0;
	}
}

