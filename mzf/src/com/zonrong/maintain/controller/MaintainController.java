package com.zonrong.maintain.controller;

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
import com.zonrong.maintain.service.MaintainService;

/**
 * date: 2011-3-9
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/maintain")
public class MaintainController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private MaintainService maintainService;
	
	@RequestMapping(value = "/findProduct/{productNum}", method = RequestMethod.GET)
	@ResponseBody
	public Map findProduct(@PathVariable final String productNum, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				Map<String, Object> product = maintainService.findByProductNum(productNum, this.getUser());
				this.put("product", product);
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createMaintain(@RequestBody final Map<String, Object> maintain, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				int id = maintainService.createMaintain(maintain, this.getUser());
				this.put("id", id);
			}			
		};

		return templete.operate();			
	}

	@RequestMapping(value = "/refund/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map refund(@PathVariable final int id, @RequestBody final Map<String, Object> refund, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				maintainService.refund(id, refund, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/over/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map over(@PathVariable final int id, @RequestBody final Map<String, Object> over, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				maintainService.over(id, over, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/sell/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map sell(@PathVariable final int id, @RequestBody final Map<String, Object> sale, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				maintainService.createSale(id, sale, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/getMaintainByProductId/{productId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getMaintainByProductId(@PathVariable final int productId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				Map<String, Object> maintain = maintainService.getMaintainByProductId(productId, this.getUser());
				this.put("maintain", maintain);
			}			
		};

		return templete.operate();			
	}
	@RequestMapping(value = "/getMaintainPrintData/{maintainId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getMaintainPrintData(@PathVariable final int maintainId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				Map<String, Object> printData = maintainService.getPrintMaintainData(maintainId, this.getUser());
				this.put("printData", printData);
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/getMaintainSalePrintData/{maintainId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getMaintainSalePrintData(@PathVariable final int maintainId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				Map<String, Object> printData = maintainService.getPrintMaintainSaleData(maintainId, this.getUser());
				this.put("printSaleData", printData);
			}			
		};

		return templete.operate();			
	}
}


