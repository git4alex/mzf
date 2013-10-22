package com.zonrong.basics.module.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zonrong.basics.module.service.ModuleCodeService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;

@Controller
@RequestMapping("/code/module")
public class ModuleCodeController {

	@Resource
	private ModuleCodeService moduleCodeService;
	
	@RequestMapping(value="/getComponent/{moduleId}/{componentId}")
	public Map getModuleStr(@PathVariable final String moduleId, @PathVariable final String componentId, HttpServletRequest request){
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				String component = moduleCodeService.getComponent(moduleId, componentId, this.getUser());
				this.put("component", component);
			}			
		};
		return templete.operate();	
	}
	
}
