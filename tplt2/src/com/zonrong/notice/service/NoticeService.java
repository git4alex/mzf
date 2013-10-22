package com.zonrong.notice.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;

/**
 * date: 2010-11-4
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class NoticeService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private EntityService entityService;
	
	public enum NoticeTitle {
		allocateToShop,
		tellCustomer
	}
	
	public int createNotice(NoticeTitle title, String content, String url, IUser user) throws BusinessException {		
		if (StringUtils.isEmpty(content)) {
			throw new BusinessException("create notice failure, content is null or empty");
		}		
		
		Map<String, Object> notice = new HashMap<String, Object>();
		notice.put("title", title);
		notice.put("content", content);
		notice.put("cuserId", null);
		notice.put("cdate", null);
		String id = entityService.create(TpltEnumEntityCode.NOTICE, notice, user);
		return Integer.parseInt(id);
	}
}


