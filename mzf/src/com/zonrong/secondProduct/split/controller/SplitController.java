package com.zonrong.secondProduct.split.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.secondProduct.split.service.SplitService;

/**
 * date: 2011-3-17
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/split")
public class SplitController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private SplitService splitService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createSplit(@RequestBody final Map<String, Object> split, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				List<Map<String, Object>> detailList = (List) MapUtils.getObject(split, "detailList");
				
				int id = splitService.createSplit(split, detailList, this.getUser()); 
				this.put("id", id);
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/{splitId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateSplit(@PathVariable final int splitId, @RequestBody final Map<String, Object> split, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				List<Map<String, Object>> detailList = (List) MapUtils.getObject(split, "detailList");
				split.remove("detailList");
				splitService.updateSplit(splitId, split, detailList, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/delete/{splitId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map deleteSplit(@PathVariable final int splitId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				splitService.deleteSplit(splitId, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/confirm/{splitId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map confirmSplit(@PathVariable final int splitId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				splitService.confirmSplit(splitId, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/createSplitRawmaterialOrder", method = RequestMethod.POST)
	@ResponseBody
	public Map createSplitRawmaterialOrder(@RequestBody final Integer[] splitIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				splitService.createSplitRawmaterialOrder(splitIds, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/summary", method = RequestMethod.GET)
	@ResponseBody
	public Map summary(@RequestParam final Integer[] splitId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Map<String, Object>> summary = splitService.summary(splitId);
				this.put("summary", summary);
			}			
		};

		return templete.operate();			
	}
	@RequestMapping(value = "/getPrintData", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintData(@RequestParam final Map<String, Object> param, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> data = splitService.getPrintData(param, this.getUser());
				this.put("data", data);
			}			
		};

		return templete.operate();			
	}
}


