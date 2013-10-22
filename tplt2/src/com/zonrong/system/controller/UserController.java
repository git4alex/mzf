package com.zonrong.system.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.Page;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.MetadataConst;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.UserService;
import com.zonrong.util.TpltUtils;

/**
 * date: 2010-7-26
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/user")
public class UserController {
	private Logger logger = Logger.getLogger(this.getClass());

	private final TpltEnumEntityCode code = TpltEnumEntityCode.USER;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private UserService userService;	
	@Resource
	private EntityService entityService;
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody	
	public Map list(@RequestParam Map<String, Object> where, HttpServletRequest request) {
		try{		
			OrderBy orderBy = TpltUtils.refactorOrderByParams(where);
			List<Map<String,Object>> list = TpltUtils.refactorQueryParams(where);				
			if (MapUtils.getInteger(where, "start") != null) {
				Page page = new Page(where);
				if (page != null) {		
					return entityService.page(code, list, page.getOffSet(), page.getPageSize(), orderBy, SessionUtils.getUser(request));		
				}	
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, entityService.list(code, list, orderBy, SessionUtils.getUser(request)));
			return map;	
		} catch (BusinessException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return new HashMap();
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody	
	public Map get(@PathVariable String id, HttpServletRequest request) {
		try {
			return (Map)entityService.getById(code, id, SessionUtils.getUser(request));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return new HashMap();
		}		
	}	
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createUser(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				userService.createUser(map, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value="/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map update(@PathVariable final Object id, @RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				Map<String, Object> whereMap = new HashMap<String, Object>();	
				EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
				whereMap.put(metadata.getPkCode(), id);
				map.remove(metadata.getPkCode());
				entityService.update(code, map, whereMap, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map delete(@PathVariable final Object id, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> parameters = new HashMap<String, Object>();
				EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
				parameters.put(metadata.getPkCode(), id);				
				entityService.delete(code, parameters, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/valid")
	@ResponseBody	
	public Map validLoginName(@RequestParam String loginName, HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		boolean b = userService.exists(loginName, SessionUtils.getUser(request));
		map.put("exists", b);
		
		return map;
	}
	
	@RequestMapping(value="/updatePassword", method = RequestMethod.PUT)
	@ResponseBody
	public Map updatePassword(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				String oldPassword = MapUtils.getString(map, "oldPassword");
				String newPassword = MapUtils.getString(map, "newPassword");
				userService.updatePassword(this.getUser(), oldPassword, newPassword);
			}			
		};
		return templete.operate();			
	}
}


