package com.zonrong.salerule.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.salerule.service.SaleruleAppService;

/**
 * date: 2011-10-21
 *
 * version: 1.0
 * 
 * 前后台交互情况 
 * 前台step1: 打开开打页面， 选择商品
 * step 1 process: 匹配单品规则，导航（2， 3， 5）
 *  
 * 前台step2: 选择匹配到的单品促销规则
 * step 2 process: 应用单品规则（折扣和积分）， 匹配整单规则，导航（3, 5）
 * 
 * 前台step3: 选择匹配道德整单促销规则
 * step 3 process：应用整单规则（折扣和积分），整合赠品，导航 （4， 5）
 * 
 * 前台step4：选择赠品
 * step 5 process：将赠品加入销售列表，导航到第5步
 * 
 * 前台step5：付款
 * step 5 process：开单
 */
@Controller
@RequestMapping("/salerule/app")
public class SaleruleAppController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private SaleruleAppService saleruleAppService;
	
	
	/**
	 * 匹配所有的促销规则
	 * @return
	 */
	@RequestMapping(value = "/matchAllSalerule", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> matchAllSalerule(@RequestBody final Map<String,Object> bill, HttpServletRequest request){
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				//匹配单品规则 
				Map<String, List<Map<String, Object>>> productIdRules = saleruleAppService.matchSaleruleForSingle(bill, this.getUser()); 
				//匹配整单规则
				List<Map<String, Object>> billSalerules = saleruleAppService.matchSaleruleForBill(bill, this.getUser()); 
				this.put("singleSalerules", productIdRules);
				this.put("billSalerules", billSalerules);
			 
			}			
		};
		return templete.operate();	
	}
	/**
	 * step 1 process
	 * @param bill
	 * @param request
	 * @return
	 */		
	@RequestMapping(value = "/matchSaleruleForSingle", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> matchSaleruleForBill(@RequestBody final Map<String,Object> bill, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				saleruleAppService.init(bill);				
				this.put("bill", bill);
				
				if (saleruleAppService.isNext(bill)) {
					//匹配单品规则
					Map<String, List<Map<String, Object>>> productIdRules = saleruleAppService.matchSaleruleForSingle(bill, this.getUser());				
					if (MapUtils.isNotEmpty(productIdRules)) {					
						this.put("productIdRules", productIdRules);
						this.put("step", 2);
						return;
					} 
					
					//匹配整单规则
					List<Map<String, Object>> billSalerules = saleruleAppService.matchSaleruleForBill(bill, this.getUser());
					if (CollectionUtils.isNotEmpty(billSalerules)) {					
						this.put("billSalerules", billSalerules);
						this.put("step", 3);
						return;
					}
				}
				
				//计算、积分
//				saleruleAppService.calcAndPoints(bill);
				this.put("step", 5);
			}			
		};
		return templete.operate();		
	}
	
	/**
	 * step 2 process
	 * @param bill
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/appSingleRulesOfDiscountAndPoints", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> appSingleRulesOfDiscountAndPoints(@RequestBody final Map<String,Object> bill, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				IUser user = this.getUser();
				
				//应用单品规则 
				List productResults = (List)bill.get("productIdRuleResults");
				bill.remove("productIdRuleResults");
				
				Map<String,List<Integer>> productResultsMap = new HashMap<String, List<Integer>>();
				for (int i = 0;i < productResults.size();i++) {
					Map map = (Map)productResults.get(i);  
					//Integer proId = MapUtils.getInteger(map, "proId");
					String num = MapUtils.getString(map,"proNum");
					List<Integer> resultIds = (List<Integer>)map.get("resultIds");
					productResultsMap.put(num, resultIds);
				}  
				Map<String, Object> newBill = saleruleAppService.appSingleRulesOfDiscountAndPoints(bill, productResultsMap, user);
				this.put("bill", newBill);
				
				//匹配整单规则
				List<Map<String, Object>> billSalerules = saleruleAppService.matchSaleruleForBill(newBill, user);
				if (CollectionUtils.isNotEmpty(billSalerules)) {
					this.put("billSalerules", billSalerules);				
					this.put("step", 3);
					return;
				}
				
				Map<String, Object> present = saleruleAppService.getPresent(bill, this.getUser());
				if (MapUtils.isNotEmpty(present)) {
					this.put("present", present);
					this.put("step", 4);
					return;
				}
				
				saleruleAppService.calcAndPoints(newBill);
				this.put("step", 5);
			}			
		};
		return templete.operate();		
	}
	
	/**
	 * step 3 process
	 * @param bill
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/appBillRulesOfDiscountAndPoints", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> appBillRulesOfDiscountAndPoints(@RequestBody final Map<String,Object> bill, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Integer> saleruleResultIds = (List<Integer>) MapUtils.getObject(bill, "saleruleResultIds");				
				//bill.remove("saleruleResultIds");
				
				Map<String, Object> newBill = saleruleAppService.appBillRulesOfDiscountAndPoints(bill, saleruleResultIds, this.getUser());
				this.put("bill", newBill);
				
				Map<String, Object> present = saleruleAppService.getPresent(bill, this.getUser());
				if (MapUtils.isNotEmpty(present)) {
					this.put("present", present);
					this.put("step", 4);
					return;
				}
				
				this.put("bill", newBill);
				saleruleAppService.calcAndPoints(newBill);
				this.put("step", 5);
			}			
		};
		return templete.operate();		
	}
	
	/**
	 * step 4 process
	 * @param bill
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addPresent", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> addPresent(@RequestBody final Map<String,Object> bill, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> present = MapUtils.getMap(bill, "present");				
				bill.remove("present");
				
				Map<String, Object> newBill = saleruleAppService.addPresent(bill, present);
				this.put("bill", newBill);	
				saleruleAppService.calcAndPoints(newBill);
				this.put("step", 5);
			}			
		};
		return templete.operate();		
	}	
	
	/**
	 * 修改抹零折扣
	 * @param bill
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addClearDiscount/{clearDiscount}", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> addClearDiscount(@PathVariable final int clearDiscount, @RequestBody final Map<String,Object> bill, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				saleruleAppService.addClearDiscount(bill, clearDiscount);
				this.put("bill", bill);	
				this.put("step", 5);
			}			
		};
		return templete.operate();		
	}
	
	/**
	 * step 5 process
	 * @param bill
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/sale", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> sale(@RequestBody final Map<String,Object> bill, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				bill.remove("present");
				
				int saleId = saleruleAppService.sale(bill, this.getUser());
				this.put("saleId", saleId);
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/getPrintData/{saleId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintData(@PathVariable final int saleId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> sale = saleruleAppService.getPrintData(saleId, this.getUser());
				this.put("sale", sale);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/mergePayChit", method = RequestMethod.POST)
	@ResponseBody
	public Map mergePayChit(@RequestBody final Map<String, Object> bill, HttpServletRequest request){
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Map<String, Object>> chits = (List<Map<String, Object>>)MapUtils.getObject(bill, "chits");
				Map<String, Object> newbill = saleruleAppService.mergePayChit(bill, chits, this.getUser());
				this.put("bill", newbill);
				
			}			
		};
		return templete.operate();			
	}	
	
}


