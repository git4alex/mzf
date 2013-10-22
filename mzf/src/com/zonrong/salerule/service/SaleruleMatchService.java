
package com.zonrong.salerule.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.salerule.service.condition.BillConditionService;
import com.zonrong.salerule.service.condition.CustomerConditionService;
import com.zonrong.salerule.service.condition.DateConditionService;
import com.zonrong.salerule.service.condition.OrgConditionService;
import com.zonrong.salerule.service.condition.ProductConditionService;
import com.zonrong.salerule.service.condition.ProductGroupConditionService;

@Service
public class SaleruleMatchService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private DateConditionService dateConditionService;
	@Resource
	private OrgConditionService orgConditionService;
	@Resource
	private ProductConditionService productConditionService;	
	@Resource
	private ProductGroupConditionService productGroupConditionService;
	@Resource
	private CustomerConditionService customerConditionService;
	@Resource
	private BillConditionService billConditionService;	
	
	public List<Map<String, Object>> matchSaleruleForSingle(Map<String, Object> productInventory, int customerId, Date date) throws BusinessException {
		int orgId = MapUtils.getIntValue(productInventory, "orgId");
		
		List<Map<String, Object>> rules = SaleruleService.getSingleEnableRules();
		
		if (CollectionUtils.isNotEmpty(rules)) {			
			rules = dateConditionService.fitler(rules, date);
		}
		if (CollectionUtils.isNotEmpty(rules)) {			
			rules = orgConditionService.fitler(rules, orgId);
		}
		if (CollectionUtils.isNotEmpty(rules)) {			
			rules = productConditionService.fitler(rules, productInventory);
		}
		if (CollectionUtils.isNotEmpty(rules)) {			
			rules = customerConditionService.fitler(rules, customerId);
		}
		
		return rules;
	}
	
	public List<Map<String, Object>> matchSaleruleForBill(Map<String, Object> bill, List<Map<String, Object>> products, Date date) throws BusinessException {
		int orgId = MapUtils.getIntValue(bill, "orgId");
		int cusId = MapUtils.getIntValue(bill, "cusId");
		
		List<Map<String, Object>> rules = SaleruleService.getBillEnableRules();
		
		if (CollectionUtils.isNotEmpty(rules)) {			
			rules = dateConditionService.fitler(rules, new Date());
		}
		if (CollectionUtils.isNotEmpty(rules)) {			
			rules = orgConditionService.fitler(rules, orgId);
		}
		if (CollectionUtils.isNotEmpty(rules)) {			
			rules = productGroupConditionService.fitler(rules, products);
		}
		if (CollectionUtils.isNotEmpty(rules)) {			
			rules = customerConditionService.fitler(rules, cusId);
		}
		if (CollectionUtils.isNotEmpty(rules)) {			
			rules = billConditionService.fitler(rules, bill);
		}
		
		return rules;
	}	
}
