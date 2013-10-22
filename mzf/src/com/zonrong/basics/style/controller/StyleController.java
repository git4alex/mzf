package com.zonrong.basics.style.controller;

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

import com.zonrong.basics.style.service.StyleService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;

/**
 * date: 2010-10-18
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/style")
public class StyleController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private StyleService styleService;
	
	@RequestMapping(value = "/byAllVendorStyle")
	@ResponseBody
	public Map generateStyle(HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				styleService.createStyleByAllVendorStyle(this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/byVendorStyle/{id}")
	@ResponseBody
	public Map generateStyle(@PathVariable final int id, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				styleService.createStyleByVenderStyle(id, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createStyle(@RequestBody final Map style, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Map<String, Object>> diamondList = (List) MapUtils.getObject(style, "diamondList");
				style.remove("diamondList");
				
				styleService.createStyle(style, diamondList, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateVendorStyle(@PathVariable final int id, @RequestBody final Map style, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Map<String, Object>> diamondList = (List) MapUtils.getObject(style, "diamondList");
				style.remove("diamondList");

				styleService.updateStyle(id, style, diamondList, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	
	@RequestMapping(value = "/updateDated", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateDated(@RequestBody final Integer[] styleIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				styleService.updateDated(styleIds, this.getUser());
			}			
		};
		return templete.operate();			
	}
}


