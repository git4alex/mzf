package com.zonrong.settlement.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.settlement.service.SettlementService;

/**
 * date: 2011-8-17
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/settlement")
public class SettlementController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private SettlementService settlementService;
	
	@RequestMapping(value = "/settle", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> settle(@RequestBody final Integer[] settlementIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				settlementService.settle(settlementIds, this.getUser());
			}			
		};

		return templete.operate();			
	}
}


