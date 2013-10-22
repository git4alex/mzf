package com.zonrong.salerule.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
import com.zonrong.salerule.service.SaleruleService;

/**
 * date: 2011-10-21
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/salerule")
public class SaleruleController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private SaleruleService saleruleService;
	
	@RequestMapping(value = "/load", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> load(HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				saleruleService.load();
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/create", method = RequestMethod.PUT)
	@ResponseBody	
	public Map<String, Object> create(@RequestBody final Map<String,Object> param,HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String,Object> saleruleCondition = (Map<String,Object>)param.get("saleruleCondition");
				List<Map<String,Object>> saleruleResult = (List<Map<String,Object>>)param.get("saleruleResult");
				saleruleService.create(saleruleCondition, saleruleResult, this.getUser());
			}			
		};
		return templete.operate();		
	}
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@ResponseBody	
	public Map<String, Object> update(@RequestBody final Map<String,Object> param,HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String,Object> saleruleCondition = (Map<String,Object>)param.get("saleruleCondition");
				List<Map<String,Object>> saleruleResult = (List<Map<String,Object>>)param.get("saleruleResult");
				saleruleService.updateSalerule(saleruleCondition, saleruleResult, this.getUser());
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/delete/{ruleId}", method = RequestMethod.DELETE)
	@ResponseBody	
	public Map<String, Object> delete(@PathVariable final int ruleId,HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				saleruleService.deleteSalerule(ruleId, this.getUser());
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/saleruleResult/{ruleId}", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> getSaleRulesByRuleId(@PathVariable final int ruleId,HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Map<String,Object>> results = saleruleService.getSaleRulesByRuleId(ruleId, this.getUser());
				this.put("results", results);
			}			
		};
		return templete.operate();		
	} 
	@RequestMapping(value = "/getSalerule/{ruleId}", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> getSaleRulesById(@PathVariable final int ruleId,HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String,Object> salerule = saleruleService.getSaleruleById(ruleId, this.getUser()); 
				this.put("salerule", salerule);
			}			
		};
		return templete.operate();		
	}
}


