package com.zonrong.core.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;

/**
 * date: 2011-12-11
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class BusinessLogService implements Runnable {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	
	private int size = 100;
	private int batchCount = size/5;
	private SyncQueue<Map<String, Object>> queue = new SyncQueue<Map<String, Object>>(size);
	
	public void log(String operate, String remark, IUser user) {
		Map<String, Object> log = new HashMap<String, Object>();
		log.put("operate", operate);
		log.put("remark", remark);
		log.put("userId", user.getId());
		log.put("cdate", new Date());
		queue.put(log);
	}
	
	@PostConstruct
	private void batchCreate() {
		new Thread(this).start(); 
	}

	public void run() {		
		while (true) {
			List<Map<String, Object>> fields = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < batchCount; i++) {
				fields.add(queue.get());
			}
			
			logger.debug("处理了" + batchCount + "个" + new Date());
			try {
				entityService.batchCreate(TpltEnumEntityCode.LOG, fields, User.getSystemUser());
			} catch (BusinessException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}			
		}
	}
}


