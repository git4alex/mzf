package com.zonrong.salerule.service.result;

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
 * date: 2011-10-21
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class PointsResultService extends ResultService<Integer> {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private Map<String, Object> product;
	private Map<String, Object> bill;
	
	public void setContext(Map<String, Object> product, Map<String, Object> bill) {
		this.product = product;
		this.bill = bill;
	}		

	@Override
	public String getJSONString(Map<String, Object> result) throws BusinessException {
		return MapUtils.getString(result, "resultPointsJSON");
	}
	
	@Override
	public Integer getResult(List<Map<String, Object>> results, int orgId) throws BusinessException {
//		2.赠送积分
//		   eg:{"calcPriority":3,"exp":"points+10","remark":"测试积分备注"}

		int points = 0;
		try {
			for (Map<String, Object> result : results) {
				String json = getJSONString(result);
//				List<HashMap<String, Object>> list = new ObjectMapper().readValue(json, List.class);
//				for (HashMap<String, Object> map : list) {
//					String exp = MapUtils.getString(map, "exp");
//					if (StringUtils.isNotBlank(exp)) {
//						points += getPoints(exp);
//					}
//				}
				Map<String, Object> map = new ObjectMapper().readValue(json, Map.class);
				String exp = MapUtils.getString(map, "exp");
				if (StringUtils.isNotBlank(exp)) {
					points += getPoints(exp);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}		
		return points;
	}
	
	public Integer getPoints(String expression) {		
		ExpressionProcessor processor = new ExpressionProcessor(expression);		
		ExpressionParser p = new SpelExpressionParser();
		RuleEvaluationContext context = new RuleEvaluationContext();
		
		if (product != null) {			
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
		return d.intValue();	
	}

}


