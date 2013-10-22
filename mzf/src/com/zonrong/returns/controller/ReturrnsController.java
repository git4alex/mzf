package com.zonrong.returns.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.returns.service.ReturnsService;

/**
 * date: 2010-11-25
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/returns")
public class ReturrnsController {
	private Logger logger = Logger.getLogger(this.getClass());	
		
	@Resource
	private ReturnsService returnsService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map sell(@RequestBody final Map<String, Object> sale, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Map<String, Object>> detailList = (List) MapUtils.getObject(sale, "detailList");
				sale.remove("detailList");
				int id = returnsService.createReturns(sale, detailList, this.getUser());;
				this.put("id", id);
			}			
		};
		return templete.operate();			
	}
}


