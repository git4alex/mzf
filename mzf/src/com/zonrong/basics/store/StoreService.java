package com.zonrong.basics.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;

 /**
  * 门店
  * @author Administrator
  *
  */
@Service
public class StoreService {
	@Resource
	private EntityService entityService;
	
	public int createStore(Map<String,Object> store,IUser user)throws BusinessException{
		OrderBy orderBy = new OrderBy(new String[]{"num"},OrderByDir.desc);
		List<Map<String,Object>> stores = entityService.list(MzfEntity.STORE, new HashMap(), orderBy, user);
		int num = 0;
		if(stores.size() > 0){
		  Map<String,Object> map = stores.get(0);
		  num = MapUtils.getInteger(map,"num") + 1;	
		}
		store.put("num", num);
		String result = entityService.create(MzfEntity.STORE, store, user);
		return Integer.parseInt(result);
	}
}
