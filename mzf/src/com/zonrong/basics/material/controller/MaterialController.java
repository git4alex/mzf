package com.zonrong.basics.material.controller;

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

import com.zonrong.basics.material.service.MaterialService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.entity.service.EntityService;

/**
 * date: 2010-10-12
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/material")
public class MaterialController {
	private Logger logger = Logger.getLogger(MaterialController.class);
	
	@Resource
	private EntityService entityService;	
	@Resource
	private MaterialService materialService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createDemand(@RequestBody final Map<String, Object> material, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				materialService.addMaterial(material, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/{materialId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map createDemanda(@PathVariable final int materialId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				materialService.deleteMaterial(materialId, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/compelDeleteMaterial/{materialId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map compelDeleteMaterial(@PathVariable final int materialId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				materialService.compelDeleteMaterial(materialId, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/recover", method = RequestMethod.PUT)
	@ResponseBody
	public Map recoverMaterial(@RequestBody final Integer[] ids, HttpServletRequest request){
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				materialService.recoverMaterial(ids, this.getUser());
			}			
		};

		return templete.operate();
	}
}


