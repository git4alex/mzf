package com.zonrong.basics.goldvalue.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.goldvalue.service.GoldValueService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;

/**
 * date: 2011-2-24
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/goldValue")
public class GoldValueController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private GoldValueService goldValueService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map create(@RequestBody final Map goldValue, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int id = goldValueService.create(goldValue, this.getUser());	 
				this.put("id", id);
			}			
		};
		return templete.operate();
	}	
}


