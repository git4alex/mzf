package com.zonrong.basics.style.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.style.service.VendorStyleService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.metadata.MetadataConst;

/**
 * date: 2010-10-18
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/vendorStyle")
public class VendorStyleController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private VendorStyleService vendorStyleService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createStyle(@RequestBody final Map vendorStyle, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Map<String, Object>> diamondList = (List) MapUtils.getObject(vendorStyle, "diamondList");
				vendorStyle.remove("diamondList");
				
				vendorStyleService.createVendorStyle(vendorStyle, diamondList, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map createVendorStyle(@PathVariable final int id, @RequestBody final Map vendorStyle, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Map<String, Object>> diamondList = (List) MapUtils.getObject(vendorStyle, "diamondList");
				vendorStyle.remove("diamondList");
				
				vendorStyleService.updateVendorStyle(id, vendorStyle, diamondList, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	
	@RequestMapping(value = "/rawmaterial/{vendorStyleId}")
	@ResponseBody
	public Map getRawmaterial(@PathVariable final int vendorStyleId, @RequestParam final String goldClass, HttpServletRequest request) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();			
			map.put(MetadataConst.ITEMS_ROOT, vendorStyleService.getRawmaterial(vendorStyleId, goldClass, request));
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();		
	}
	
	@RequestMapping(value = "/updateDated", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateDated(@RequestBody final Integer[] styleIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				vendorStyleService.updateDated(styleIds, this.getUser());
			}			
		};
		return templete.operate();			
	}
}


