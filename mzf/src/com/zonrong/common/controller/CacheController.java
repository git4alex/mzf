package com.zonrong.common.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.salerule.service.PointsruleService;
import com.zonrong.salerule.service.SaleruleService;

/**
 * date: 2011-11-13
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/cache")
public class CacheController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private SaleruleService saleruleService;
	@Resource
	private PointsruleService pointsruleService;
	
	@RequestMapping(value = "/loadSalerule", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> loadSalerule(HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				saleruleService.load();
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/loadPointsrule", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> loadPointsrule(HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				pointsruleService.load();
			}			
		};
		return templete.operate();		
	}
}


