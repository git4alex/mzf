package com.zonrong.showcase.controller;

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
import com.zonrong.showcase.service.ShowcaseCheckService;

/**
 * date: 2011-1-5
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/showcaseCheck")
public class ShowcaseCheckController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private ShowcaseCheckService showcaseCheckService;
		
	@RequestMapping(value = "/checkIn/{showcaseId}", method = RequestMethod.POST)
	@ResponseBody
	public Map checkIn(@PathVariable final int showcaseId, @RequestBody final Integer[] productIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				showcaseCheckService.checkIn(showcaseId, productIds, null, this.getUser());
			}			
		};
		return templete.operate();
	}
	
	@RequestMapping(value = "/checkIn", method = RequestMethod.POST)
	@ResponseBody
	public Map checkIn(@RequestParam final int showcaseId, @RequestParam final int productId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				int id = showcaseCheckService.checkIn(showcaseId, productId, null, this.getUser());
				this.put("id", id);
			}			
		};
		return templete.operate();
	}
	
	@RequestMapping(value = "/checkCount", method = RequestMethod.POST)
	@ResponseBody
	public Map checkCount(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				showcaseCheckService.checkCount(map, this.getUser());
			}			
		};
		return templete.operate();
	}	
	@RequestMapping(value="/deleteShowcase/{showcaseId}",method=RequestMethod.DELETE)
	@ResponseBody
	public Map delShowcase(@PathVariable final int showcaseId,HttpServletRequest request){
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				showcaseCheckService.delShowcase(showcaseId, this.getUser());
			}			
		};
		return templete.operate();
	}
}


