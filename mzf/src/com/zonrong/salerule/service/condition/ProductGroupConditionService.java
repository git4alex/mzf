package com.zonrong.salerule.service.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.inventory.service.ProductInventoryService;
import com.zonrong.salerule.service.expression.ExpressionProcessor;
import com.zonrong.salerule.service.expression.RuleEvaluationContext;
import com.zonrong.salerule.service.mapper.ProductMapper;

/**
 * date: 2011-10-19
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ProductGroupConditionService extends ConditonService<List<Map<String, Object>>> {
	private Logger logger = Logger.getLogger(this.getClass());
	@Resource
    private	ProductInventoryService productInventoryService;

	@Override
	String getJSONString(Map<String, Object> rule) throws BusinessException {
		return MapUtils.getString(rule, "conProductJSON");
	}

	@Override
	boolean getValue(String json, List<Map<String, Object>> products) throws BusinessException {
		Map<Integer, Double> totalDiscountMap = new HashMap<Integer, Double>();
		List<Integer> productIds = new ArrayList<Integer>();
		for (Map<String, Object> product : products) {
			Integer productId = MapUtils.getInteger(product, "targetId");

			productIds.add(productId);
			totalDiscountMap.put(productId, MapUtils.getDouble(product, "totalDiscount"));
		}

		List<Map<String, Object>> productInventorys = productInventoryService.list(productIds.toArray(new Integer[]{}), null);
		for (Map<String, Object> productInventory : productInventorys) {
			Integer productId = MapUtils.getInteger(productInventory, "id");
			productInventory.put("retailBasePrice", MapUtils.getObject(productInventory, "fixedPrice"));
			productInventory.put("totalDiscount", totalDiscountMap.get(productId));
		}

		List<String> expressions = getExpression(json);
		if(CollectionUtils.isEmpty(expressions)){
			return true;
		}

		return match(productInventorys, expressions);
	}


	private boolean match(List<Map<String, Object>> productInventorys, List<String> expressions) throws BusinessException {
		if (CollectionUtils.isEmpty(expressions)) {
			return true;
		}

		for (int i = 0; i < expressions.size(); i++) {
			String expression = expressions.get(i);

			for (int j = 0; j < productInventorys.size(); j++) {
				Map<String, Object> product = productInventorys.get(j);

				ProductMapper<String, String> productMapper = new ProductMapper<String, String>(product);

				RuleEvaluationContext context = new RuleEvaluationContext();
				context.set(productMapper);

				ExpressionProcessor processor = new ExpressionProcessor(expression);
				expression = processor.replace(productMapper).getExpression();

				ExpressionParser p = new SpelExpressionParser();
				boolean flag = p.parseExpression(expression).getValue(context,Boolean.class);

				if (flag) {
					productInventorys.remove(j);
					expressions.remove(i);
					return match(productInventorys, expressions);
				}
			}
		}

		return false;
	}

	private List<String> getExpression(String json) throws BusinessException {
		List<String> exps = new ArrayList<String>();
		try {
			if (StringUtils.isNotBlank(json)){
				List<HashMap<String, Object>> list = new ObjectMapper().readValue(json, List.class);
				for (HashMap<String, Object> map : list) {
					String exp = MapUtils.getString(map, "exp");
					if (StringUtils.isNotBlank(exp)) {
						exps.add(exp);
					}
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
		return exps;
	}
}


