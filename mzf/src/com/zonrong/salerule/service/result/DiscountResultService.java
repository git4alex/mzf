package com.zonrong.salerule.service.result;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.zonrong.salerule.service.mapper.BillMapper;
import com.zonrong.salerule.service.mapper.ProductMapper;

/**
 * date: 2011-11-15
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class DiscountResultService extends ResultService<BigDecimal> {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private Map<String, Object> product;
	private Map<String, Object> bill;

	public void setContext(Map<String, Object> product, Map<String, Object> bill) {
		this.product = product;
		this.bill = bill;
	}
	
	@Override
	public String getJSONString(Map<String, Object> result) throws BusinessException {
		return MapUtils.getString(result, "resultDiscountJSON");
	}

	@Override
	public BigDecimal getResult(List<Map<String, Object>> results, int orgId) throws BusinessException {
//		1. 价格折扣
//		   eg: {"calcPriority":2,"exp":"totalAmount*0.9","remark":"测试备注"}
//		   calcPriority:执行优先级；exp:折扣表达式； remark:备注

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			for (Map<String, Object> result : results) {
				String json = getJSONString(result);
				Map<String, Object> map = new ObjectMapper().readValue(json, Map.class);
				list.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		
		checkForPriority(list);
		sortByPriority(list);
		
		BigDecimal price = null;
		if (product != null) {			
			price = new BigDecimal(MapUtils.getString(product, "fixedPrice"));
		}
		BigDecimal tempPrice = null;
		for (Map<String, Object> map : list) {
			String exp = MapUtils.getString(map, "exp");
			if (StringUtils.isNotBlank(exp)) {
				price = getPrice(exp, tempPrice);
				tempPrice = price;
			}
		}

		return price;
	}
	
	public BigDecimal getPrice(String expression, BigDecimal tempPrice) {		
		ExpressionProcessor processor = new ExpressionProcessor(expression);		
		ExpressionParser p = new SpelExpressionParser();
		RuleEvaluationContext context = new RuleEvaluationContext();
		
		if (product != null) {			
			product.put("tempPrice", tempPrice);
			ProductMapper<String, String> productMapper = new ProductMapper<String, String>(product);				
			context.set(productMapper);
			expression = processor.replace(productMapper).getExpression();
		}
		
		if (bill != null) {			
			BillMapper<String, String> billMapper = new BillMapper<String, String>(bill);
			context.set(billMapper);
			expression = processor.replace(billMapper).getExpression();
		}
		
		Double d = p.parseExpression(expression).getValue(context, Double.class);
		return new BigDecimal(d);	
	}	

	
	private void checkForPriority(List<Map<String, Object>> list) throws BusinessException {
		for (Map<String, Object> for1 : list) {
			Integer priority = MapUtils.getInteger(for1, "calcPriority");
			int count = 0;
			for (Map<String, Object> for2 : list) {
				if (priority == MapUtils.getInteger(for2, "calcPriority")) {
					count ++;
				}
			}
			if (count > 1) {
				throw new BusinessException("多个生效结果中的折扣计算优先级相同");
			}
		}
	}
	
	private void sortByPriority(List<Map<String, Object>> list) throws BusinessException {
		Comparator<Map<String, Object>> c = new Comparator<Map<String,Object>>() {
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Integer priority1 = MapUtils.getInteger(o1, "calcPriority");
				Integer priority2 = MapUtils.getInteger(o2, "calcPriority");
				if (priority1 > priority2) {
					return -1;
				} else if (priority1 < priority2) {
					return 1;
				} else {
					return 0;
				}
			}
		};
	}
}


