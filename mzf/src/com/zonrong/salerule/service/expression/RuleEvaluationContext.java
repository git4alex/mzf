package com.zonrong.salerule.service.expression;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.zonrong.salerule.service.mapper.DateMapper;
import com.zonrong.salerule.service.mapper.Mapper;

/**
 * date: 2011-10-20
 *
 * version: 1.0
 * commonts: ......
 */
public class RuleEvaluationContext extends StandardEvaluationContext {
	private Logger logger = Logger.getLogger(this.getClass());
	
	public RuleEvaluationContext() {
		super();
		this.setRootObject(new HashMap<String, Object>());
		this.getPropertyAccessors().add(new MapAccessor());
		
		try {
			this.registerFunction("getTime", DateMapper.class.getDeclaredMethod("getTime", new Class[]{Date.class}));
			this.registerFunction("getMonth", DateMapper.class.getDeclaredMethod("getMonth", new Class[]{Date.class}));
			this.registerFunction("getDay", DateMapper.class.getDeclaredMethod("getDay", new Class[]{Date.class}));
			this.registerFunction("getWeek", DateMapper.class.getDeclaredMethod("getWeek", new Class[]{Date.class}));
			this.registerFunction("getHour", DateMapper.class.getDeclaredMethod("getHour", new Class[]{Date.class}));
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}	

	public void set(Mapper mapper) {
		Map<String, Object> rootObject = ((HashMap<String,Object>)this.getRootObject().getValue());
		mapper.load(rootObject);
		System.out.println(rootObject);
	}
}


