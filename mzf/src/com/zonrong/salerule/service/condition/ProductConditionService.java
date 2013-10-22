package com.zonrong.salerule.service.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
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
public class ProductConditionService extends ConditonService<Map<String, Object>> {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	String getJSONString(Map<String, Object> rule) throws BusinessException {
		return MapUtils.getString(rule, "conProductJSON");
	}

	@Override
	boolean getValue(String json, Map<String, Object> product) throws BusinessException {
//		Map<String, Object> product = new HashMap<String, Object>();
//		product.put("ptype", "pt");
//		product.put("pkind", "001");
//		product.put("retailBasePrice", 10005);
		product.put("retailBasePrice", MapUtils.getObject(product, "fixedPrice"));
		ProductMapper<String, String> productMapper = new ProductMapper<String, String>(product);

		RuleEvaluationContext context = new RuleEvaluationContext();
		context.set(productMapper);
		
		String expression = getExpression(json);
		if (StringUtils.isBlank(expression)) {
			return true;
		}
		ExpressionProcessor processor = new ExpressionProcessor(expression);		
		expression = processor.replace(productMapper).getExpression();
		
		ExpressionParser p = new SpelExpressionParser();
		return p.parseExpression(expression).getValue(context,Boolean.class);
	}
	
	private String getExpression(String json) throws BusinessException {
		List<String> exps = new ArrayList<String>();
		try {
			List<HashMap<String, Object>> list = new ObjectMapper().readValue(json, List.class);
			for (HashMap<String, Object> map : list) {
				String exp = MapUtils.getString(map, "exp");
				if (StringUtils.isNotBlank(exp)) {
					exps.add(exp);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
		return StringUtils.join(exps.toArray(new String[] {}), " or ");
	}
}


