package com.zonrong.salerule.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.SaleruleStatus;
import com.zonrong.common.utils.MzfEnum.SaleruleType;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;

/**
 * date: 2011-10-19
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class SaleruleService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private EntityService entityService;
	
	private static List<Map<String, Object>> singleEnableRules = new ArrayList<Map<String,Object>>();
	private static List<Map<String, Object>> billEnableRules = new ArrayList<Map<String,Object>>();
	private static List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
	
	public static List<Map<String, Object>> getBillEnableRules() {
		return billEnableRules;
	}

	public static List<Map<String, Object>> getSingleEnableRules() {
		return singleEnableRules;
	}

	@PostConstruct
	public void load() throws BusinessException {
		//先查询结果
		OrderBy orderBy = new OrderBy(new String[]{"id"}, OrderByDir.asc);
		results = entityService.list(MzfEntity.SALERULE_RESULT, new ArrayList<Map<String, Object>>(), orderBy, User.getSystemUser());
		
		//再封装规则
		singleEnableRules = listEnableRule(SaleruleType.single, User.getSystemUser());		
		billEnableRules = listEnableRule(SaleruleType.bill, User.getSystemUser());
	}
	
	public List<Map<String, Object>> listResult(Integer[] resultIds) throws BusinessException {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		for (Integer resultId : resultIds) {			
			for (Map<String, Object> result : results) {
				if (resultId.equals(MapUtils.getInteger(result, "id"))) {
					list.add(result);
				}
			}
		}
		
		return list;
	}
	
	public List<Map<String, Object>> listEnableRule(SaleruleType type, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("type", type);
		where.put("status", SaleruleStatus.enable);
		List<Map<String, Object>> rules =  entityService.list(MzfEntity.SALERULE, where, null, user.asSystem());

		//生效结果
		
		for (Map<String, Object> rule : rules) {
			int id = MapUtils.getIntValue(rule, "id");
			
			for (Map<String, Object> result : results) {
				int ruleId = MapUtils.getIntValue(result, "ruleId");
				if (id == ruleId) {
					List<Map<String, Object>> ruleResults = (List<Map<String, Object>>) MapUtils.getObject(rule, "results");
					if (CollectionUtils.isEmpty(ruleResults)) {
						ruleResults = new ArrayList<Map<String, Object>>();						
					}
					ruleResults.add(result);
					rule.put("results", ruleResults);
				}
			}
		}
		
		return rules;
	}
	
	/**
	 * 启用规则
	 * 
	 * @param ruleId
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public void launch(int ruleId, IUser user) throws BusinessException {
		//修改状态
	}
	
	/**
	 * 停用规则
	 * 
	 * @param ruleId
	 * @param user
	 * @throws BusinessException
	 */
	public void stop(int ruleId, IUser user) throws BusinessException {
		//修改状态
	}
	public void create(Map<String,Object> saleRule,List<Map<String,Object>> saleruleResult,IUser user) throws BusinessException{
		if (CollectionUtils.isEmpty(saleruleResult)) {
			return;
		}
		String ruleId = entityService.create(MzfEntity.SALERULE, saleRule, user);
		for (Map<String, Object> map : saleruleResult) {
			map.put("ruleId", ruleId);
		}
		entityService.batchCreate(MzfEntity.SALERULE_RESULT, saleruleResult, user);
	}
	
	public Map<String, Object> getSaleruleById(int ruleId, IUser user) throws BusinessException{
		Map<String, Object> salerule = entityService.getById(MzfEntity.SALERULE, ruleId, user);
		List<Map<String, Object>> results = getSaleRulesByRuleId(ruleId, user);
		salerule.put("results", results);
		return salerule;
		
	}
	/**
	 * 查询规则的所有结果
	 * @param ruleId
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public List<Map<String,Object>> getSaleRulesByRuleId(int ruleId,IUser user) throws BusinessException{
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("ruleId", ruleId);
		OrderBy orderBy = new OrderBy(new String[] { "id" }, OrderByDir.asc);
		List<Map<String, Object>> results = entityService.list(MzfEntity.SALERULE_RESULT, where, orderBy, user);
		return results;
	}
	public void updateSalerule(Map<String,Object> saleRule,List<Map<String,Object>> saleruleResult,IUser user) throws BusinessException{
		int ruleId = MapUtils.getIntValue(saleRule, "id");
		List<Map<String, Object>> allResult = getSaleRulesByRuleId(ruleId, user); // 原来所有的结果
		List<Integer> resultIds = new ArrayList<Integer>(); // 现有的结果id集合
		for (Map<String, Object> map : saleruleResult) {
			int id = MapUtils.getIntValue(map, "id");
			if (id != 0) {
				resultIds.add(id);
			}
		}
		for (Map<String, Object> map : allResult) {
			int id = MapUtils.getIntValue(map, "id");
			if (!resultIds.contains(id)) {
				entityService.deleteById(MzfEntity.SALERULE_RESULT, id + "", user);
			}
		}

		int result = entityService.updateById(MzfEntity.SALERULE, ruleId + "", saleRule, user);
		if (result >= 0) {
			for (Map<String, Object> map : saleruleResult) {
				int resultId = MapUtils.getIntValue(map, "id");
				boolean isExists = isExistsSaleruleResult(resultId, user);
				if (isExists) {
					String id = MapUtils.getString(map, "id");
					entityService.updateById(MzfEntity.SALERULE_RESULT, id, map, user);
				} else {
					map.put("ruleId", ruleId);
					entityService.create(MzfEntity.SALERULE_RESULT, map, user);
				}
			}
		} 
			 
		 
	}
	public void deleteSalerule(int ruleId, IUser user) throws BusinessException {
		int result = entityService.deleteById(MzfEntity.SALERULE, ruleId + "", user);
		if (result >= 0) {
			Map<String, Object> where = new HashMap<String, Object>();
			where.put("ruleId", ruleId);
			entityService.delete(MzfEntity.SALERULE_RESULT, where, user);
		}
	}

	
	/**
	 * 判断促销结果是否存在
	 * @return
	 * @throws BusinessException
	 */
	private boolean isExistsSaleruleResult(int saleruleResultId,IUser user)throws BusinessException{
		Map<String, Object> result = entityService.getById(MzfEntity.SALERULE_RESULT, saleruleResultId, user);
		if (MapUtils.isNotEmpty(result)) {
			return true;
		}
		return false;
	}
}


