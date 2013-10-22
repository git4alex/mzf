package com.zonrong.summary.controller;

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
import com.zonrong.summary.service.SummaryService;

/**
 * date: 2011-3-1
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/summary")
public class SummaryController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private SummaryService summaryService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map doSummary(@RequestBody final Map summary, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {		
				int id = summaryService.doSummary(summary, this.getUser()); 
				this.put("id", id);
			}			
		};
		return templete.operate();
	}
	
	@RequestMapping(value = "/isNeedSummary", method = RequestMethod.GET)
	@ResponseBody
	public Map isNeedSummary(HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				boolean isNeedSummary = summaryService.isNeedSummary(this.getUser());
				this.put("isNeedSummary", isNeedSummary);
			}			
		};
		return templete.operate();			
	}	
}


