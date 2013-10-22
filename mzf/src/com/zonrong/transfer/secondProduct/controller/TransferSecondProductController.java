package com.zonrong.transfer.secondProduct.controller;

import java.util.HashMap;
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

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.transfer.secondProduct.service.TransferSecondProductService;

/**
 * date: 2010-11-24
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value="/code/transfer/secondProduct")
public class TransferSecondProductController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private TransferSecondProductService transferSecondProductService;	
	
	@RequestMapping(value="/transfer", method = RequestMethod.POST)
	@ResponseBody
	public Map create(@RequestBody final Map<String, Object> transfer, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Integer> list = (List)MapUtils.getObject(transfer, "productIds");
				Integer[] productIds = list.toArray(new Integer[]{});
				transferSecondProductService.transfer(productIds, transfer, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/send", method = RequestMethod.PUT)
	@ResponseBody
	public Map send(@RequestBody final Integer[] transferIds, HttpServletRequest request) {
		final Map<String, Object> dispatch = new HashMap<String, Object>();
		dispatch.put("targetOrgId", request.getParameter("targetOrgId"));
		dispatch.put("remark", request.getParameter("remark"));
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int dispatchId = transferSecondProductService.send(transferIds, dispatch, this.getUser());
				this.put("dispatchId", dispatchId);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/receive/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map receive(@PathVariable final int id, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				transferSecondProductService.receive(id, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/receiveKGold/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map receiveKGold(@PathVariable final int id, @RequestBody final Map<String, Object> receive, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				transferSecondProductService.receiveKGold(id, receive, this.getUser());
			}			
		};
		return templete.operate();			
	}		
}


