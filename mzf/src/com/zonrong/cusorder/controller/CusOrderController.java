package com.zonrong.cusorder.controller;

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

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.cusorder.service.CusOrderService;
import com.zonrong.inventory.treasury.service.TreasuryService.BizType;

/**
 * date: 2010-10-10
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/cusOrder")
public class CusOrderController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private CusOrderService cusOrderService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createOrder(@RequestBody final Map<String, Object> order, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
			  int cusOrderId = cusOrderService.createOrder(order, this.getUser());
			  this.put("id", cusOrderId);
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/appendEarnest/{orderId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map appendEarnest(@PathVariable final int orderId, @RequestBody final Map<String, Object> earnest, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				String remark = MapUtils.getString(earnest, "remark");
				cusOrderService.appendEarnest(BizType.appendEarnest, orderId, earnest, remark, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map update(@PathVariable final int id, @RequestBody final Map<String, Object> order, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {												
				cusOrderService.updateCusOrder(id, order, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
//	@RequestMapping(value = "/cancel/{id}", method = RequestMethod.PUT)
//	@ResponseBody
//	public Map cancel(@PathVariable final int id, @RequestBody final Map<String, Object> cancel, HttpServletRequest request) {
//		OperateTemplete templete = new HttpTemplete(request) {
//			protected void doSomething() throws BusinessException {												
//				cusOrderService.cancelOrder(id, cancel, this.getUser());
//			}			
//		};
//		return templete.operate();			
//	}
	
	@RequestMapping(value = "/refund/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map refund(@PathVariable final int id, @RequestBody final Map<String, Object> refund, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				cusOrderService.refund(id, refund, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map delete(@PathVariable final int id, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				cusOrderService.deleteOrderById(id, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/getPrintData/{orderId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintData(@PathVariable final int orderId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> cusOrder = cusOrderService.getPrintData(orderId);
				this.put("cusOrder", cusOrder);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/lastAppendEarnest/{orderId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintAppendEarnestLastData(@PathVariable final int orderId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> cusOrder = cusOrderService.getAppendEarnestData(orderId, this.getUser());
				this.put("cusOrder", cusOrder);
			}			
		};
		return templete.operate();			
	}
	@RequestMapping(value = "/appendEarnest/{orderId}/{earnestId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintAppendEarnestData(@PathVariable final int orderId,@PathVariable final int  earnestId,  HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> cusOrder = cusOrderService.getAppendEarnestData(orderId,earnestId, this.getUser());
				this.put("cusOrder", cusOrder);
			}			
		};
		return templete.operate();			
	}
}


