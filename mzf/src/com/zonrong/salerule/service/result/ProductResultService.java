package com.zonrong.salerule.service.result;

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
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.salerule.service.expression.ExpressionProcessor;
import com.zonrong.salerule.service.expression.RuleEvaluationContext;
import com.zonrong.salerule.service.mapper.ProductMapper;

/**
 * date: 2011-11-15
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ProductResultService extends ResultService<List<Map<String, Object>>> {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private ProductInventoryService productInventoryService;
	
	@Override
	public String getJSONString(Map<String, Object> result) throws BusinessException {
		return MapUtils.getString(result, "resultProductJSON");
	}

	@Override
	public List<Map<String, Object>> getResult(List<Map<String, Object>> results, int orgId) throws BusinessException {
//		3. 赠送商品
//		   eg:[{"count":3,"countOpt":"<=","price":5000,"priceOpt":"<=","exp":"一口价 <= 2000","remark":""},{"count":1,"countOpt":"<=","price":1000,"priceOpt":"<=","exp":"商品类型 == 'diamond'","remark":""}]
//		   count:商品数量； countOpt:数量操作符； price:商品价格； priceOpt:价格操作符；exp:商品表达式； remark:备注
		
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		
		List<Map<String, Object>> productInventorys = productInventoryService.listProductInventory(null, orgId);
		try {
			int groupFlag = 0;
			for (Map<String, Object> result : results) {				
				String json = getJSONString(result);
				List<HashMap<String, Object>> list = new ObjectMapper().readValue(json, List.class);
				List<Map<String, Object>> products = new ArrayList<Map<String,Object>>();
				for (HashMap<String, Object> map : list) {
					String exp = MapUtils.getString(map, "exp");
					if (StringUtils.isNotBlank(exp)) {
						products.addAll(matchProduct(productInventorys, exp, map, groupFlag));
					}
					groupFlag ++;
				}
				
				Map<String, Object> leaf = new HashMap<String, Object>();
				if(CollectionUtils.isNotEmpty(products)){ 
					leaf.put("text", getResultName(result));
					leaf.put("leaf", true);
					//leaf.put("products", list);
					leaf.put("products", products);
					resultList.add(leaf);
				}
				
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
		
		return resultList;
	}
	
	private List<Map<String, Object>> matchProduct(List<Map<String, Object>> products, String expression,Map<String,Object> rule, int groupFlag) throws BusinessException {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		
		for (Map<String, Object> product0 : products) {
			Map<String, Object> product = new HashMap<String, Object>(product0);
			ProductMapper<String, String> productMapper = new ProductMapper<String, String>(product);
            product.put("ruleCount", MapUtils.getObject(rule, "count"));
            product.put("ruleCountOpt", MapUtils.getObject(rule, "countOpt"));
            product.put("rulePrice", MapUtils.getObject(rule, "price"));
            product.put("rulePriceOpt", MapUtils.getObject(rule, "priceOpt"));
            product.put("groupFlag", groupFlag);
            product.put("checked", false);
			RuleEvaluationContext context = new RuleEvaluationContext();
			context.set(productMapper);
			
			if (StringUtils.isBlank(expression)) {
				throw new BusinessException("赠送商品表达式为空");
			}
			ExpressionProcessor processor = new ExpressionProcessor(expression);		
			expression = processor.replace(productMapper).getExpression();
			
			ExpressionParser p = new SpelExpressionParser();
			if (p.parseExpression(expression).getValue(context,Boolean.class)) {
				list.add(product);
			}
		}
		
		return list;
	}
}


