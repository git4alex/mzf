
package com.zonrong.sale.rule.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;

@Service
public class RuleBillAppService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	public boolean isPassOnSale(List<Map<String, Object>> saleDetail,  List<Map<String, Object>> saleRuleList, IUser user) throws BusinessException {
		
		return true;
	}
}
