package com.zonrong.entity.acl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.zonrong.core.security.IUser;

public class AclRule {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private Map<String,Object> valueMap=new HashMap<String,Object>();
	
	public AclRule(Map<String,Object> value){
		this.valueMap=value;
	}
	
	public String getEntityCode(){
		return MapUtils.getString(valueMap, "entity_code");
	}
	
	public String getUserExp(){
		return MapUtils.getString(valueMap, "user_exp");
	}
	
	public int getPriority(){
		return MapUtils.getInteger(valueMap, "priority");
	}
	
	public String getFilterExp(){
		return MapUtils.getString(valueMap, "filter_exp");
	}
	
	public boolean enable(IUser user){
		if(user.isSystem()){
			return false;
		}
		
		ExpressionParser parser=new SpelExpressionParser();
		return parser.parseExpression(this.getUserExp()).getValue(getDefaultEvaluationContext(user), Boolean.class);
	}
	
	public boolean support(String entityCode){
		return StringUtils.equalsIgnoreCase(this.getEntityCode(), entityCode);
	}
	
	public boolean isValidValue(Map<String,Object> value){
		
		return true;
	}
	
	public String getFilterString(IUser user){
		ExpressionParser parser=new SpelExpressionParser();
		return parser.parseExpression(this.getFilterExp()).getValue(getDefaultEvaluationContext(user), String.class);
	}
	
	private EvaluationContext getDefaultEvaluationContext(IUser user){
		StandardEvaluationContext context=new StandardEvaluationContext();
		context.setVariable("user", user);
		
		try {
			context.registerFunction("allUser", AclRule.class.getDeclaredMethod("allUser", new Class[]{}));
		} catch (SecurityException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		
		return context;
	}
	
	public static Boolean allUser(){
		return true;
	}
}
