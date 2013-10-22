package com.zonrong.system.controller;

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
import com.zonrong.system.service.BizCodeService;

/**
 * date: 2010-12-22
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/biz")
public class BizCodeController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private BizCodeService bizCodeService;
	
	@RequestMapping(value = "/bizCode/{typeCode}/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map move(@PathVariable final String typeCode, @PathVariable final int id, @RequestParam final int step, @RequestParam final int pid, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				bizCodeService.move(id, step, typeCode, pid, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/bizType//{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map move(@PathVariable final int id, @RequestBody final Map<String, Object> bizType, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {			
				bizCodeService.updateBizType(id, bizType, this.getUser());
			}			
		};
		return templete.operate();			
	}
}


