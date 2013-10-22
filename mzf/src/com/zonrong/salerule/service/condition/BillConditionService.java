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
import com.zonrong.salerule.service.mapper.BillMapper;

/**
 * date: 2011-10-19
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class BillConditionService extends ConditonService<Map<String, Object>> {
	private static Logger logger = Logger.getLogger(BillConditionService.class);
	
	@Override
	String getJSONString(Map<String, Object> rule) throws BusinessException {
		return MapUtils.getString(rule, "conBillJSON");
	}

	@Override
	boolean getValue(String json, Map<String, Object> bill) throws BusinessException {
		List<Map<String, Object>> products = (List<Map<String, Object>>) bill.get("detailList");
		bill.put("productCount", products.size());
		BillMapper<String, String> billMapper = new BillMapper<String, String>(bill);

		RuleEvaluationContext context = new RuleEvaluationContext();
		context.set(billMapper);
		
		String expression = getExpression(json);
		if (StringUtils.isBlank(expression)) {
			return true;
		}
		ExpressionProcessor processor = new ExpressionProcessor(expression);
		expression = processor.replace(billMapper).getExpression();
		
		ExpressionParser p = new SpelExpressionParser();
		return p.parseExpression(expression).getValue(context,Boolean.class);
	}
	
	private String getExpression(String json) throws BusinessException {
		   List<String> exps = new ArrayList<String>();
		try {
			HashMap<String,Object> map = new ObjectMapper().readValue(json, HashMap.class);
			String amountOpt = MapUtils.getString(map, "amountOpt");
			String totalAmount = MapUtils.getString(map, "totalAmount");
			String countOpt = MapUtils.getString(map, "countOpt");
			String totalCount = MapUtils.getString(map, "totalCount");
			if(StringUtils.isNotBlank(amountOpt) && StringUtils.isNotBlank(totalAmount)){
				exps.add(StringUtils.join(new String[]{"整单消费总额", amountOpt, totalAmount}, " "));
			}
			if(StringUtils.isNotBlank(countOpt) && StringUtils.isNotBlank(totalCount)){
				exps.add(StringUtils.join(new String[]{"整单购买件数", countOpt, totalCount}, " "));
			}
		}catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
		return StringUtils.join(exps.toArray(new String[] {}), " and ");
	}
	
	public static void main(String[] args) {
		BillConditionService bs = new BillConditionService();
		String json = "{\"amountOpt\":\">=\",\"totalAmount\":3000,\"countOpt\":\">\",\"totalCount\":2,\"remark\":\"满三千 且商品超过两件\"}";
		try {
			System.out.println(bs.getExpression(json));
			//System.out.println("[14][21][25]".indexOf("[21]"));
			Float f = null;
			Object s = f-0;
			System.out.println(s);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}
}


