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

import com.zonrong.common.utils.MzfEnum.LogicOperator;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.User;
import com.zonrong.core.sql.service.SimpleSqlService;
import com.zonrong.salerule.service.expression.RuleEvaluationContext;

/**
 * date: 2011-10-19
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class CustomerConditionService extends ConditonService<Integer> {
	private Logger logger = Logger.getLogger(this.getClass());
	@Resource
	private SimpleSqlService simpleSqlService;
	
	@Override
	String getJSONString(Map<String, Object> rule) throws BusinessException {
		return MapUtils.getString(rule, "conCustomerJSON");
	}
	
	@Override
	boolean getValue(String json, Integer customerId) throws BusinessException {
		Map<String, Object> map = new HashMap<String,Object>();
		try {
			map = new ObjectMapper().readValue(json, HashMap.class);
			if (map.size() == 0) {
				return true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
		Map<String, Object> data = getCustomerParam(map);
		data.put("customerId", customerId.toString());
		List<Map<String, Object>> list = simpleSqlService.list("salerule", "customer", data, User.getSystemUser());
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("未找到该客户");
		} else if (list.size() > 1) {
			throw new BusinessException("找到多条客户记录");
		}
		
		Map<String, Object> customer = list.get(0);		
		
		RuleEvaluationContext context = new RuleEvaluationContext();		
		String expression = getExpression(json, customer);	
		if (StringUtils.isBlank(expression)) {
			return true;
		}
		ExpressionParser p = new SpelExpressionParser();
		return p.parseExpression(expression).getValue(context,Boolean.class);
	}
	
	private Map<String, Object> getCustomerParam(Map<String,Object> map) throws BusinessException {
		Map<String, Object> queryParam = new HashMap<String, Object>();
		queryParam.put("xTotalAmountPtype",  MapUtils.getString(map, "xTotalAmountPtype"));
		queryParam.put("xTotalAmountPkind", MapUtils.getString(map, "xTotalAmountPkind"));
		queryParam.put("xTotalAmountDate1", MapUtils.getString(map, "xTotalAmountDate1"));
		queryParam.put("xTotalAmountDate2", MapUtils.getString(map, "xTotalAmountDate2"));
		queryParam.put("xTotalSaleCountPtype",  MapUtils.getString(map, "xTotalSaleCountPtype"));
		queryParam.put("xTotalSaleCountPkind", MapUtils.getString(map, "xTotalSaleCountPkind"));
		queryParam.put("xTotalSaleCountDate1", MapUtils.getString(map, "xTotalSaleCountDate1"));
		queryParam.put("xTotalSaleCountDate2", MapUtils.getString(map, "xTotalSaleCountDate2"));
		
		return queryParam;		
	}
	
	private String getExpression(String json, Map<String, Object> customer) throws BusinessException {
		List<String> exps = new ArrayList<String>();
		List<String> expsArray = new ArrayList<String>();
		Map<String,Object> map = null;
		try {
			map = new ObjectMapper().readValue(json, HashMap.class);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
		exps.add(getSubExp(MapUtils.getString(customer, "type"), LogicOperator.EQ, MapUtils.getString(map, "type"), false));
		exps.add(getSubExp(MapUtils.getString(customer, "grade"), LogicOperator.EQ, MapUtils.getString(map, "grade"), false));

		String grantDate = MapUtils.getString(customer, "grantDate");
		exps.add(getSubExp(grantDate, LogicOperator.GT_EQ, MapUtils.getString(map, "grantDate1"), false));
		exps.add(getSubExp(grantDate, LogicOperator.LT_EQ, MapUtils.getString(map, "grantDate2"), false));

		String age = MapUtils.getString(customer, "age");
		exps.add(getSubExp(age, LogicOperator.GT_EQ, MapUtils.getString(map, "age1"), true));
		exps.add(getSubExp(age, LogicOperator.LT_EQ, MapUtils.getString(map, "age2"), true));
		
		String totalPoints = MapUtils.getString(customer, "totalPoints");
		exps.add(getSubExp(totalPoints, LogicOperator.GT_EQ, MapUtils.getString(map, "totalPoints1"), true));
		exps.add(getSubExp(totalPoints, LogicOperator.LT_EQ, MapUtils.getString(map, "totalPoints2"), true));
		
		String totalAmount = MapUtils.getString(customer, "totalAmount");
		exps.add(getSubExp(totalAmount, LogicOperator.GT_EQ, MapUtils.getString(map, "totalAmount1"), true));
		exps.add(getSubExp(totalAmount, LogicOperator.LT_EQ, MapUtils.getString(map, "totalAmount2"), true));
		
		String totalSaleCount = MapUtils.getString(customer, "totalSaleCount");
		exps.add(getSubExp(totalSaleCount, LogicOperator.GT_EQ, MapUtils.getString(map, "totalSaleCount1"), true));
		exps.add(getSubExp(totalSaleCount, LogicOperator.LT_EQ, MapUtils.getString(map, "totalSaleCount2"), true));
		
		String xTotalAmount = MapUtils.getString(customer, "xTotalAmount");
		exps.add(getSubExp(xTotalAmount, LogicOperator.GT_EQ, MapUtils.getString(map, "xTotalAmount1"), true));
		exps.add(getSubExp(xTotalAmount, LogicOperator.LT_EQ, MapUtils.getString(map, "xTotalAmount2"), true));
		
		String xTotalSaleCount = MapUtils.getString(customer, "xTotalSaleCount");
		exps.add(getSubExp(xTotalSaleCount, LogicOperator.GT_EQ, MapUtils.getString(map, "xTotalSaleCount1"), true));
		exps.add(getSubExp(xTotalSaleCount, LogicOperator.LT_EQ, MapUtils.getString(map, "xTotalSaleCount2"), true));
		for (String exp : exps) {
			if(StringUtils.isNotBlank(exp)){
				expsArray.add(exp);
			}
		}
		return StringUtils.join(expsArray.toArray(new String[]{}), " and ");
	}

	private String getSubExp(String v1, LogicOperator operator, String v2, boolean isNumber) {
		String s = "";
		if (!isNumber) {
			s = "'";
		}
		if (StringUtils.isNotBlank(v2)) {
			return s + v1 + s + " " + operator + " " + s + v2 + s;
		}
		
		return StringUtils.EMPTY;
	}	
}


