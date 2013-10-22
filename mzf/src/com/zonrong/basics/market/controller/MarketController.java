package com.zonrong.basics.market.controller;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.market.service.MarketService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;

/**
 * date: 2010-8-23
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/market")
public class MarketController {	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private MarketService marketService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map create(@RequestBody final Map market, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {		
				List<Map<String, Object>> linkmanList = (List) MapUtils.getObject(market, "linkmanList");
				market.remove("linkmanList");
				
				int id = marketService.createMarket(market, linkmanList, this.getUser()); 
				this.put("id", id);
			}			
		};
		return templete.operate();
	}		
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map update(@PathVariable final int id, @RequestBody final Map market, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				List<Map<String, Object>> linkmanList = (List) MapUtils.getObject(market, "linkmanList");
				market.remove("linkmanList");
				marketService.updateMarket(id, market, linkmanList, this.getUser());
			}			
		};
		return templete.operate();			
	}	
}


