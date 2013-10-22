package com.zonrong.basics.goldvalue.service;

import java.math.BigDecimal;
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
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;

/**
 * date: 2011-2-24
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class GoldValueService {
	public Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private EntityService entityService;	
	
	public enum GoldValueType {
		salePt950,
		salePt900,
		buyGold,
		buyPt950,
		buyK750
	}
	
	public static Map<String, Map<String, Object>> map = new HashMap<String, Map<String,Object>>();
	
	@PostConstruct
	public void updateCache() throws BusinessException {
		List<Map<String, Object>> list = entityService.list(MzfEntity.GOLD_VALUE_LAST_VIEW, new ArrayList<Map<String, Object>>(), null, User.getSystemUser());
		if (CollectionUtils.isEmpty(list)) return;
		
		for (Map<String, Object> goldValue : list) {
			Integer orgId = MapUtils.getInteger(goldValue, "orgId");
			this.map.put(orgId.toString(), goldValue);
		}		
	}	
	
	public BigDecimal getValue(GoldValueType type, int orgId) throws BusinessException {
		if (type == null) {
			throw new BusinessException("未指定金价类型");
		}
		
		Map<String, Object> goldValue = map.get(Integer.toString(orgId));
		Float f = MapUtils.getFloat(goldValue, type.toString());
		if (f != null) {
			return new BigDecimal(f);
		}
		throw new BusinessException("没有取到相应的金价");
	}	
	
	public int create(Map<String, Object> goldValue, IUser user) throws BusinessException {
		String id = entityService.create(MzfEntity.GOLD_VALUE, goldValue, user);
		updateCache();
		
		return Integer.parseInt(id);
	}
}


