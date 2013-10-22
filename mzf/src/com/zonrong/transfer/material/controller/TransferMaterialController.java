package com.zonrong.transfer.material.controller;

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
import com.zonrong.transfer.material.service.TransferMaterialService;

/**
 * date: 2010-11-24
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value="/code/transfer/material")
public class TransferMaterialController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private TransferMaterialService transferMaterialService;	
	
	@RequestMapping(value="/transfer", method = RequestMethod.POST)
	@ResponseBody
	public Map create(@RequestBody final Map<String, Object> transfer, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Map<String, Object>> list = (List)MapUtils.getObject(transfer, "materialList");
				transferMaterialService.transfer(list, transfer, this.getUser());
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
				int dispatchId = transferMaterialService.send(transferIds, dispatch, this.getUser());
				this.put("dispatchId", dispatchId);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/receive/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map receive(@PathVariable final int id,@RequestBody final Map<String, Object> receive, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				transferMaterialService.receive(id, receive, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/getPrintData/{dispatchId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintData(@PathVariable final int dispatchId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> dispatch = transferMaterialService.getPrintData(dispatchId);
				this.put("dispatch", dispatch);
			}			
		};
		return templete.operate();			
	}
	
}


