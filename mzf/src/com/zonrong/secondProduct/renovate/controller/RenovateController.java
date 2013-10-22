package com.zonrong.secondProduct.renovate.controller;

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
import com.zonrong.secondProduct.renovate.service.RenovateService;

/**
 * date: 2011-3-17
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/renovate")
public class RenovateController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private RenovateService renovateService;
	
	@RequestMapping(value = "/{secondProductId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateSplit(@PathVariable final int secondProductId, @RequestBody final Map<String, Object> renovate, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				renovateService.createRenovate(secondProductId, renovate, this.getUser());
			}			
		};

		return templete.operate();
	}	
	@RequestMapping(value = "/getPrintData", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintData(@RequestParam final Map<String, Object> param, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> data = renovateService.getPrintData(param, this.getUser());
				this.put("data", data);
			}			
		};

		return templete.operate();			
	}
}


