package com.zonrong.salerule.controller;

import java.util.Date;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.salerule.service.SaleruleMatchService;
import com.zonrong.util.TpltUtils;

/**
 * date: 2011-10-21
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/salerule/test")
public class SaleruleAppTestController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private SaleruleMatchService saleruleMatchService;
	@Resource
	private ProductInventoryService productInventoryService;	
	
	
	@RequestMapping(value = "/matchBillRule", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> testMatch(@RequestBody final Map<String, Object> bill,@RequestParam final String billDate,
										HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Map<String, Object>> products = (List<Map<String, Object>>) bill.get("detailList");
				Date date = TpltUtils.getDate(billDate, "yyyy-MM-dd hh:mm");
				
				List<Map<String, Object>> rules = saleruleMatchService.matchSaleruleForBill(bill, products, date);
				this.put("rules", rules);
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/matchSingleRule/{productNum}", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> testMatch(@PathVariable final String productNum,
										@RequestParam final int orgId,
										@RequestParam final int customerId, 										
										@RequestParam final double totalDiscount,
										@RequestParam final String billDate,
										HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> productInventory = productInventoryService.getProductInventoryByProductNum(productNum, orgId);
				
				Date date = TpltUtils.getDate(billDate, "yyyy-MM-dd hh:mm");
				List<Map<String, Object>> rules = saleruleMatchService.matchSaleruleForSingle(productInventory, customerId, date);
				this.put("rules", rules);
			}			
		};
		return templete.operate();		
	}	
}


