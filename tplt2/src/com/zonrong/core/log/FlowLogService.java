package com.zonrong.core.log;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;

/**
 * date: 2010-12-7
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class FlowLogService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private EntityService entityService;	
	
	public int createLog(int transId, IEntityCode entityCode, String entityId,
			String operate, ITargetType targetType, Object targetId,
			String remark, IUser user) throws BusinessException {
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("transId", transId);
		field.put("entityCode", entityCode.getCode());
		field.put("entityId", entityId);
		field.put("operate", operate);
		field.put("remark", remark);
		field.put("cdate", null);
		field.put("cuserId", null);
		field.put("cuserName", null);
		
		if (targetType != null) {
			field.put("targetType", targetType);
			field.put("targetId", targetId);
		}
		
		String id = entityService.create(TpltEnumEntityCode.BIZ_LOG, field, user);
		return Integer.parseInt(id);
	}		
}


