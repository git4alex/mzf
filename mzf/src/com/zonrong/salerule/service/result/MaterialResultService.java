package com.zonrong.salerule.service.result;

import java.math.BigDecimal;
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

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.User;
import com.zonrong.inventory.service.MaterialInventoryService;
import com.zonrong.salerule.service.expression.ExpressionProcessor;
import com.zonrong.salerule.service.expression.RuleEvaluationContext;
import com.zonrong.salerule.service.mapper.MaterialMapper;

/**
 * date: 2011-11-15
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class MaterialResultService extends ResultService<List<Map<String, Object>>> {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MaterialInventoryService materialInventoryService;
	
	@Override
	public String getJSONString(Map<String, Object> result) throws BusinessException {
		return MapUtils.getString(result, "resultMaterialJSON");
	}

	@Override
	public List<Map<String, Object>> getResult(List<Map<String, Object>> results, int orgId) throws BusinessException {
//		4. 赠送物料
//		   eg: [{"countOpt":"<=","count":2,"priceOpt":"<=","price":120,"exp":"类型 == '办公'","remark":"测试备注"},{"countOpt":"<","count":4,"priceOpt":">=","price":12,"exp":"价格 < 120","remark":"赠送物料备注"}]
//		   count:物料数量； countOpt:数量操作符； price:物料价格； priceOpt:价格操作符；exp:物料表达式； remark:备注
		
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> materialInventorys = materialInventoryService.listMaterialInventory(null, orgId, User.getSystemUser());
		try {
			int groupFlag = 0;
			for (Map<String, Object> result : results) {
				String json = getJSONString(result);
				List<Map<String, Object>> materials = new ArrayList<Map<String,Object>>();
				List<HashMap<String, Object>> list = new ObjectMapper().readValue(json, List.class);
				
				for (HashMap<String, Object> map : list) {
					String exp = MapUtils.getString(map, "exp");
					if (StringUtils.isNotBlank(exp)) {
						materials.addAll(matchMaterial(materialInventorys, exp, map, groupFlag));
					}
					groupFlag ++;
				}
				
				Map<String, Object> leaf = new HashMap<String, Object>();
				if(CollectionUtils.isNotEmpty(materials)){
					leaf.put("text", getResultName(result));
					leaf.put("leaf", true);
					//leaf.put("materials", list);
					leaf.put("materials", materials);
					resultList.add(leaf);
				}  
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
		
		return resultList;
	}
	
	private List<Map<String, Object>> matchMaterial(List<Map<String, Object>> materials, String expression, Map<String,Object> rule, int groupFlag) throws BusinessException {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> materialO : materials) {
			BigDecimal quantity = new BigDecimal(MapUtils.getString(materialO, "quantity", Integer.toString(0)));
			BigDecimal checkedQuantity = new BigDecimal(MapUtils.getString(materialO, "checkedQuantity", Integer.toString(0)));
			if (quantity.subtract(checkedQuantity).floatValue() <= 0) {
				continue;
			}
			materialO.put("quantity", quantity.subtract(checkedQuantity).floatValue());
			
			Map<String, Object> material = new HashMap<String, Object>(materialO);
			Float f = MapUtils.getFloat(material, "retailPrice");
			if (f < 30) {
				System.out.println(f);
			}
			MaterialMapper<String, String> materialMapper = new MaterialMapper<String, String>(material);
			material.put("ruleCount", MapUtils.getObject(rule, "count"));
			material.put("ruleCountOpt", MapUtils.getObject(rule, "countOpt"));
			material.put("rulePrice", MapUtils.getObject(rule, "price"));
			material.put("price", MapUtils.getFloatValue(rule, "presentPrice"));
			material.put("salerulePrice", MapUtils.getFloatValue(rule, "presentPrice"));
			material.put("rulePriceOpt", MapUtils.getObject(rule, "priceOpt"));
			material.put("groupFlag", groupFlag);
			material.put("checked", false);
			material.put("checkedQuantity", 0);
			RuleEvaluationContext context = new RuleEvaluationContext();
			context.set(materialMapper);
			
			if (StringUtils.isBlank(expression)) {
				throw new BusinessException("赠送商品表达式为空");
			}
			ExpressionProcessor processor = new ExpressionProcessor(expression);		
			expression = processor.replace(materialMapper).getExpression();
			
			ExpressionParser p = new SpelExpressionParser();
			if (p.parseExpression(expression).getValue(context,Boolean.class)) {
				list.add(material);
			}
		}
		
		return list;
	}
}


