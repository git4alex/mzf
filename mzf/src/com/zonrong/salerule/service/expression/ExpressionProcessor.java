package com.zonrong.salerule.service.expression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zonrong.salerule.service.mapper.Mapper;
import com.zonrong.salerule.service.mapper.Mapper.KeywordsMapper;


/**
 * date: 2011-10-21
 *
 * version: 1.0
 * commonts: ......
 */
public class ExpressionProcessor<T extends Mapper<String, String>> {
	private Logger log = Logger.getLogger(this.getClass());
	
	private String expression;
	
	public String getExpression() {
		return this.expression;
	}
	
	public ExpressionProcessor(String expression) {
		this.expression = expression;
		log.debug("expression is: " + this.expression);
	}
	
	public ExpressionProcessor replace(T... mappers) {
		KeywordsMapper<String, String> keywordsMapper = new KeywordsMapper<String, String>();
		List<Mapper<String, String>> list = new ArrayList<Mapper<String, String>>();
		list.add(keywordsMapper);
		
		for (T mapper : mappers) {			
			list.add(mapper);			
		}
		
		for (Mapper<String, String> mapper : list) {			
			this.expression = replace(this.expression, mapper);			
		}
		return this;
	}
	
	private String replace(String expression, Mapper<String, String> mapper) {
		if (StringUtils.isNotBlank(expression)) {
			Iterator<String> it = mapper.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				
				expression = expression.replaceAll(key, mapper.get(key));
			}
		}
		log.debug("expression is: " + expression);
		return expression;
	}
}

