package com.zonrong.shiftwork.controller;

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
import com.zonrong.shiftwork.service.ShiftWorkService;

/**
 * date: 2010-12-30
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/shiftWork")
public class ShiftWorkController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private ShiftWorkService shiftWorkService;
	
	@RequestMapping(value = "/shiftWork", method = RequestMethod.POST)
	@ResponseBody
	public Map shiftWork(@RequestBody final Map<String, Object> shiftWork, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Map<String, Object>> detailList = (List<Map<String, Object>>) MapUtils.getObject(shiftWork, "detailList");
				shiftWork.remove("detailList");
				int id = shiftWorkService.handOver(shiftWork, detailList, this.getUser()); 
				this.put("id", id);
			}			
		};
		return templete.operate();
	}		
	
	@RequestMapping(value = "/takeOver", method = RequestMethod.PUT)
	@ResponseBody
	public Map takeOver(@RequestBody final Map<String, Object> shiftWork, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				shiftWorkService.takeOver(shiftWork, this.getUser());
			}			
		};
		return templete.operate();
	}
	
	@RequestMapping(value = "/isNeedShiftWork", method = RequestMethod.GET)
	@ResponseBody
	public Map isNeedShiftWork(HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				boolean isNeedShiftWork = shiftWorkService.isNeedShiftWork(this.getUser());
				this.put("isNeedShiftWork", isNeedShiftWork);
			}			
		};
		return templete.operate();			
	}	
}


