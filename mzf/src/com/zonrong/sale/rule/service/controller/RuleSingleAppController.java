package com.zonrong.sale.rule.service.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.sale.rule.service.RuleSingleAppService;

/**
 * date: 2011-9-27
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/saleRule/single")
public class RuleSingleAppController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private RuleSingleAppService ruleSingleAppService;
	@Resource
	private MetadataProvider metadataProvider;
	
	@RequestMapping(value = "/matchRule", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> matchRule(@RequestParam final String productNum, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Object obj = ruleSingleAppService.matchRule(productNum, this.getUser());
				if (obj instanceof Map) {
					this.put("productInfo", obj);				
				} else if (obj instanceof List) {
					this.put("ruleList", obj);				
				} else {
					throw new BusinessException("匹配规则有误");
				}
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/appRule", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> appRule(@RequestParam final int productId, @RequestParam final int ruleId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> productInfo = ruleSingleAppService.appRule(productId, ruleId, this.getUser());
				this.put("productInfo", productInfo);
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/getGiveProduct", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> getGiveProduct(@RequestParam final int productId, @RequestParam final int ruleId,@RequestParam final boolean isGroup, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Map<String, Object>> giveProductList = ruleSingleAppService.getGiveProduct(ruleId, productId, isGroup,this.getUser());
				this.put("productInfo", giveProductList);
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/getGiveMaterial", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> getGiveMaterial(@RequestParam final int productId, @RequestParam final int ruleId,@RequestParam final boolean isGroup, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Map<String, Object>> giveMaterialList = ruleSingleAppService.getGiveMaterial(ruleId, productId, isGroup,this.getUser());
				this.put("materialInfo", giveMaterialList);
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/getGiveChit", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> getGiveChit(@RequestParam final int productId, @RequestParam final int ruleId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Map<String, Object>> giveChitList = ruleSingleAppService.getGiveChit(ruleId, productId,this.getUser());
				this.put("chitInfo", giveChitList);
			}			
		};
		return templete.operate();		
	}
	@RequestMapping(value = "/getPointAndDiscount", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> getPointAndDiscount(@RequestParam final int productId, @RequestParam final int ruleId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> pointsAndDiscount = ruleSingleAppService.getPointAndDiscount(ruleId, productId,this.getUser());
				this.put("pointsAndDiscount", pointsAndDiscount);
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/getGiveTree", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> getGiveTree(@RequestParam final int productId, @RequestParam final int ruleId,HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Map<String, Object>> treeList = ruleSingleAppService.getGiftTree(productId,ruleId,this.getUser());
				this.put("treeList", treeList);
			}			
		};
		return templete.operate();		
	}
}


