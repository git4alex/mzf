package com.zonrong.system.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.system.service.UserRoleService;

/**
 * date: 2010-7-26
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/userRole")
public class UserRoleController {
	private Logger logger = Logger.getLogger(UserRoleController.class);
	private EntityCode code = new EntityCode("userRole");
	@Resource
	private UserRoleService userRoleService;
	
//	@RequestMapping(value = "/{userId}/{allocated}", method = RequestMethod.GET)
//	@ResponseBody	
//	public Map list(@PathVariable Object userId, @PathVariable boolean allocated) {
//		Map map = new HashMap();
//		try {
//			String code = allocated? "roleAllocated":"roleAllocate";
//			map.put(Const.ITEMS_ROOT, userRoleService.queryListRole(code, userId));
//		} catch (BusinessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error(e.getMessage(), e);
//		}
//		return map;			
//	}
	
	@RequestMapping(value = "/{userId}", method = RequestMethod.POST)
	@ResponseBody
	public Map allocate(@PathVariable final Object userId, @RequestBody final List Role, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				userRoleService.allocateRole(code, userId, Role.toArray(), this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public Map delete(@RequestParam final Object[] ids, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				userRoleService.deleteRole(code, ids, this.getUser());
			}			
		};
		return templete.operate();			
	}	
}


