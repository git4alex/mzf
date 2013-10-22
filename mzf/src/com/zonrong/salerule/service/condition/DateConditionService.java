package com.zonrong.salerule.service.condition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.zonrong.salerule.service.mapper.DateMapper;

/**
 * date: 2011-10-19
 * 
 * version: 1.0 commonts: ......
 */
@Service
public class DateConditionService extends ConditonService<Date> {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	String getJSONString(Map<String, Object> rule) throws BusinessException {
		return MapUtils.getString(rule, "conDateJSON");
	}

	@Override
	boolean getValue(String json, Date date) throws BusinessException {
		Map<String, Object> dateMap = new HashMap<String, Object>();
		dateMap.put("paramDateTime", date);
		String expression = getExpression(json, dateMap);
		if (StringUtils.isBlank(expression)) {
			return true;
		}
		DateMapper<String, String> dateMapper = new DateMapper<String, String>(dateMap);
		
		RuleEvaluationContext context = new RuleEvaluationContext();
		context.set(dateMapper);
		
		ExpressionProcessor processor = new ExpressionProcessor(expression);		
		expression = processor.replace(dateMapper).getExpression();
		
		ExpressionParser p = new SpelExpressionParser();
		
		return p.parseExpression(expression).getValue(context, Boolean.class);
	}

	private String getExpression(String json, Map<String, Object> param)
			throws BusinessException {
		try {
			List<HashMap<String, String>> list = new ObjectMapper().readValue(json, List.class);
			
			List<String> exps = new ArrayList<String>();
			for (HashMap<String, String> map : list) {
				String startDate = MapUtils.getString(map, "startDate");
				String startTime = MapUtils.getString(map, "startTime");
				String endDate = MapUtils.getString(map, "endDate");
				String endTime = MapUtils.getString(map, "endTime");
				Date startDateTime = getDateTime(startDate, startTime);
				Date endDateTime = getDateTime(endDate, endTime);
				param.put("startDateTime", startDateTime);
				param.put("endDateTime", endDateTime);
				
				List<String> subExps = new ArrayList<String>();
				if (startDateTime != null && endDateTime != null) {					
					subExps.add("#getTime(起始时间) <= #getTime(指定时间)");
					subExps.add("#getTime(结束时间) >= #getTime(指定时间)");
				}
					
				// 重复时间规则
				boolean isRepeat = MapUtils.getBoolean(map, "isRepeat");
				if (isRepeat) {
					String months = splitString(MapUtils.getString(map, "months"));
					String days = splitString(MapUtils.getString(map, "days"));
					String weeks = splitString(MapUtils.getString(map, "weeks"));
					String hours = splitString(MapUtils.getString(map, "hours"));
					if (StringUtils.isNotBlank(months)) {
						subExps.add("'"+months+"'.indexOf('[' + #getMonth(指定时间) + ']') >= 0");
					}
				    if (StringUtils.isNotBlank(days)) {
				    	subExps.add("'"+days+"'.indexOf('[' + #getDay(指定时间) + ']') >= 0");	
				    }
				    if (StringUtils.isNotBlank(weeks)) {
				    	subExps.add("'"+weeks+"'.indexOf('[' + #getWeek(指定时间) + ']') >= 0");	
				    }
				    if (StringUtils.isNotBlank(hours)) {
				    	subExps.add("'"+hours+"'.indexOf('[' + #getHour(指定时间) + ']') >= 0");	
				    }
				}
				
				exps.add(StringUtils.join(subExps.toArray(new String[]{}), " and "));
			}
			return StringUtils.join(exps.toArray(new String[]{}), " or ");
		} catch (Exception e) {
			new BusinessException("时间表达式出现错误:" + e.getMessage());
			return "";
		}
		
	}

	private Date getDateTime(String date, String time) {
		try {
			if ((date != null && !date.equals("")) && (time != null && !time.equals(""))) {
				date = date.substring(0, 10);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd k:m");
				Date newdate = sdf.parse(date + " " + time);
				return newdate;
			}else{
				return null;
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return null;
		
	}
    private String splitString(String splitStr){
    	if(splitStr != null && !splitStr.equals("")){
    		String[] strs = splitStr.split(",");
    		StringBuffer newStrs = new StringBuffer();
    		for(int i = 0;i < strs.length;i++){
    			newStrs.append("[" + strs[i] + "]");
    		}
    		return newStrs.toString();
    	}
    	return "";
    }
    
}
