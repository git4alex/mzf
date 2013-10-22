package com.zonrong.salerule.service.result;

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

import com.zonrong.basics.chit.service.ChitService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.ChitStatus;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;
import com.zonrong.salerule.service.SaleruleService;
import com.zonrong.salerule.service.expression.ExpressionProcessor;
import com.zonrong.salerule.service.expression.RuleEvaluationContext;
import com.zonrong.salerule.service.mapper.BillMapper;
import com.zonrong.salerule.service.mapper.Mapper;
import com.zonrong.salerule.service.mapper.ProductMapper;

/**
 * date: 2011-11-15
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class GrantChitResultService extends ResultService<List<Map<String, Object>>> {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private ChitService chitService;
	@Resource
	private EntityService entityService;
	@Resource
	private SaleruleService saleruleService;
	
	private Map<String, Object> bill;
	public void setContext(Map<String, Object> bill) {
		this.bill = bill;
	}

	 
	
	@Override
	public String getJSONString(Map<String, Object> result) throws BusinessException {
		return MapUtils.getString(result, "resultChitJSON");
	}

	@Override
	public List<Map<String, Object>> getResult(List<Map<String, Object>> results, int orgId) throws BusinessException {
//		5. 送券
//		  eg:{"totalCountOpt":"<=","totalCount":4,"totalFaceValueOpt":"<","totalFaceValue":200,"rateOpt":"<=","rate":0.3,"remark":"收券备注","{"list":"[{\"type\":\"100元K金券\",\"countExp\":\"整单消费总额/50\",\"remark\":\"\"}]"}"}
//		  totalCountOpt:总数量操作符； totalCount：总数量；totalFaceValueOpt：总面值操作符；totalFaceValue：总面值；rateOpt：比率操作符；rate:比率；remark：备注
//		  list:代金券集合
//		      type:代金券类型；countExp:数量表达式； remark:备注
		
		
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId", orgId);
		where.put("status", ChitStatus.normal);
		OrderBy orderBy = new OrderBy(new String[]{"name", "num"}, OrderByDir.asc);
		List<Map<String, Object>> chitInventory = entityService.list(MzfEntity.CHIT_VIEW, where, orderBy, User.getSystemUser());
		try {
			int groupFlag = 0;
			for (Map<String, Object> result : results) {				
				String json = getJSONString(result); 
				Integer ruleId = MapUtils.getInteger(result, "ruleId");
				Map<String, Object> jsonMap = new ObjectMapper().readValue(json, Map.class);
				List<HashMap<String, Object>> list = new ObjectMapper().readValue(MapUtils.getString(jsonMap, "list"), List.class);
				List<Map<String, Object>> chits = new ArrayList<Map<String,Object>>();
				for (HashMap<String, Object> map : list) {
					String name = MapUtils.getString(map, "type");
					String countExp = MapUtils.getString(map, "countExp");
					//Integer count = MapUtils.getInteger(map, "count"); 
					Integer count = getChitCount(countExp,ruleId);
					if (StringUtils.isNotBlank(name) && count != null) {
						chits.addAll(matchChits(chitInventory, name, count, jsonMap, groupFlag));
					}
					groupFlag ++;
				}
				
				Map<String, Object> leaf = new HashMap<String, Object>();
				if(CollectionUtils.isNotEmpty(chits)){ 
					leaf.put("text", getResultName(result));
					leaf.put("leaf", true);
					//leaf.put("products", list);
					leaf.put("chits", chits);
					resultList.add(leaf);
				}
				
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
		return resultList;
	}
	 //获取代金券的数量
	private int getChitCount(String expression, int ruleId) throws BusinessException{ 
		Map<String, Object> saleRule = saleruleService.getSaleruleById(ruleId, User.getSystemUser());
		String type = MapUtils.getString(saleRule, "type");
		ExpressionProcessor processor = new ExpressionProcessor(expression);		
		ExpressionParser p = new SpelExpressionParser();
		RuleEvaluationContext context = new RuleEvaluationContext();
		Mapper<String, String>  mapper = null;
		int count = 0;
		if(type.equals("single")){
			List<Map<String, Object>> detailList = (List<Map<String, Object>>) MapUtils.getObject(bill, "detailList");
			for (Map<String, Object> detail : detailList) {
				if("product".equals(MapUtils.getString(detail, "type"))){
					detail.put("retailBasePrice", MapUtils.getFloat(detail, "salerulePrice"));
					detail.put("fixedPrice", MapUtils.getFloat(detail, "salerulePrice"));
					ProductMapper<String, String> productMapper = new ProductMapper<String, String>(detail);				
					context.set(productMapper);
					expression = processor.replace(productMapper).getExpression();
					count += p.parseExpression(expression).getValue(context, Integer.class);
				}
				
			}
		}else{
			bill.put("amount", MapUtils.getFloatValue(bill, "billAmount"));
			BillMapper<String, String> billMapper = new BillMapper<String, String>(bill);
			context.set(billMapper);
			expression = processor.replace(billMapper).getExpression();
			count = p.parseExpression(expression).getValue(context, Integer.class);
		}
		return count;
	}
	private List<Map<String, Object>> matchChits(List<Map<String, Object>> chitInventory, String name, int count, Map<String, Object> jsonMap, int groupFlag) {		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		if (CollectionUtils.isEmpty(chitInventory)) {
			return list;
		}
		
		List<Map<String, Object>> tempChitInventory = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < chitInventory.size(); i++) {
			Map<String, Object> chitO = chitInventory.get(i);
			Map<String, Object> chit = new HashMap<String, Object>(chitO);
			String chitName = MapUtils.getString(chit, "name");
			if (name.equals(chitName) && list.size() < count) {
				chit.put("totalCountOpt", MapUtils.getString(jsonMap, "totalCountOpt"));
				chit.put("totalCount", MapUtils.getString(jsonMap, "totalCount"));
				chit.put("totalFaceValueOpt", MapUtils.getString(jsonMap, "totalFaceValueOpt"));
				chit.put("totalFaceValue", MapUtils.getString(jsonMap, "totalFaceValue"));
				chit.put("rateOpt", MapUtils.getString(jsonMap, "rateOpt"));
				chit.put("rate", MapUtils.getString(jsonMap, "rate"));
				chit.put("checked", false);
				chit.put("groupFlag", groupFlag);
				list.add(chit);
			} else {
				tempChitInventory.add(chit);
			}
		}
		
		chitInventory = tempChitInventory;
		return list;
	} 

}


