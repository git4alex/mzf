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
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.system.service.RolePermissionService;

/**
 * date: 2010-7-26
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/rolePermission")
public class RolePermissionController {
	private Logger logger = Logger.getLogger(RolePermissionController.class);
	private EntityCode code = new EntityCode("rolePermission");
	@Resource
	private RolePermissionService permissionService;
	
//	@RequestMapping(value = "/{roleId}", method = RequestMethod.GET)
//	@ResponseBody	
//	public Map list(@PathVariable Object roleId) {		
//		Map map = new HashMap();
//		try {
//			String code = "permissionAllcoate";
//			map.put(Const.ITEMS_ROOT, permissionService.queryListPermission(code, roleId));
//		} catch (BusinessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error(e.getMessage(), e);
//		}
//		return map;	
//	}
	
	@RequestMapping(value = "/{roleId}", method = RequestMethod.POST)
	@ResponseBody
	public Map allocate(@PathVariable final Object roleId, @RequestBody final List permission, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				permissionService.allocatePermission(code, roleId, permission.toArray(), this.getUser());
			}			
		};
		return templete.operate();			
	}
	
//	@RequestMapping(method = RequestMethod.DELETE)
//	@ResponseBody
//	public Map delete(@RequestParam final Object[] ids) {
//		OperateTemplete templete = new OperateTemplete() {
//			protected void doSomething() throws BusinessException {
//				permissionService.deletePermission(code, ids);
//			}			
//		};
//		return templete.operate();			
//	}	
}


