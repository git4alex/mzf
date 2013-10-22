package com.zonrong.basics.customer.controller;

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

import com.zonrong.basics.customer.service.CustomerService;
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
@RequestMapping(value = "/code/customer")
public class CustomerController {	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private CustomerService customerService;	 
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map create(@RequestBody final Map<String, Object> customer, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				int id = customerService.create(customer, this.getUser());	 
				this.put("id", id);
			}			
		};
		return templete.operate();
	}		

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map update(@PathVariable final int id, @RequestBody final Map<String, Object> customer, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				customerService.update(id, customer, this.getUser());	 
			}			
		};
		return templete.operate();
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Map update(@PathVariable final int id, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				Map<String, Object> customer = customerService.getCustomerById(id, this.getUser());
				this.put("customer", customer);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/grant/{cusId}", method = RequestMethod.POST)
	@ResponseBody
	public Map update(@PathVariable final int cusId, @RequestParam final int cardId, @RequestParam final String remark, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				int id = customerService.grantCard(cusId, cardId, remark, this.getUser());
				this.put("id", id);
			}			
		};
		return templete.operate();			
	}
	@RequestMapping(value = "/upPoints/{cusId}", method = RequestMethod.POST)
	@ResponseBody
	public Map upPoints(@PathVariable final int cusId,@RequestParam final int points,@RequestParam final int historyPoints,@RequestParam final int exchangePoints,@RequestParam final String remark, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				customerService.upPoints(cusId, points, historyPoints, exchangePoints, remark, this.getUser());
			}			
		};
		return templete.operate();			
	}
	@RequestMapping(value = "/lockedPoints/{cusId}", method = RequestMethod.POST)
	@ResponseBody
	public Map lockedPoints(@PathVariable final int cusId,@RequestParam final int lockedPoints, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				customerService.lockedPoints(cusId, lockedPoints, this.getUser());
			}			
		};
		return templete.operate();			
	}
	@RequestMapping(value = "/unlockedPoints/{cusId}", method = RequestMethod.POST)
	@ResponseBody
	public Map unlockedPoints(@PathVariable final int cusId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				customerService.unlockedPoints(cusId, this.getUser());
			}			
		};
		return templete.operate();			
	}
}


