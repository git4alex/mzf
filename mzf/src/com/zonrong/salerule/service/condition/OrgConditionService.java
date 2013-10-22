package com.zonrong.salerule.service.condition;

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
import com.zonrong.salerule.service.expression.RuleEvaluationContext;

/**
 * date: 2011-10-19
 * 
 * version: 1.0 commonts: ......
 */
@Service
public class OrgConditionService extends ConditonService<Integer> {
	private Logger logger = Logger.getLogger(this.getClass());
	@Override
	String getJSONString(Map<String, Object> rule) throws BusinessException {
		return MapUtils.getString(rule, "conOrgJSON");
	}

	@Override
	boolean getValue(String json, Integer orgId) throws BusinessException {
		String expression = getExpression(json, orgId);
		if (StringUtils.isBlank(expression)) {
			return true;
		}
		RuleEvaluationContext context = new RuleEvaluationContext();
		
		ExpressionParser p = new SpelExpressionParser();
		
		return p.parseExpression(expression).getValue(context, Boolean.class);
	}

	private String getExpression(String json, Integer orgId)
			throws BusinessException {
		try {
			StringBuffer sb = new StringBuffer();
			List<HashMap<String, Object>> list = new ObjectMapper().readValue(json, List.class);
			for (HashMap map : list) {
				Integer _orgId = MapUtils.getInteger(map, "orgId");
				if (_orgId == null) {
					throw new BusinessException("部门表达式出现错误");
				}
				sb.append("[" + _orgId + "]");
			}
			
			if (StringUtils.isNotBlank(sb.toString())) {
				return "'"+sb.toString()+"'.indexOf('[" + orgId + "]') >= 0";				
			} else {
				return new Boolean(true).toString();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
	} 
	  
}
