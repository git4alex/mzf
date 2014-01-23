package com.zonrong.sale.controller;

import java.math.BigDecimal;
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

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.inventory.service.ProductInventoryService;
import com.zonrong.sale.service.SaleService;
import com.zonrong.salerule.service.PointsruleService;

/**
 * date: 2010-11-25
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/sale")
public class SaleController {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private SaleService saleService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private PointsruleService pointsruleService;

	@RequestMapping(value = "/findByProductNum/{productNum}", method = RequestMethod.GET)
	@ResponseBody
	public Map findByProductNum(@PathVariable final String productNum, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> product = productInventoryService.getInventoryByNumOnSale(productNum, this.getUser());
				this.put("product", product);
			}
		};
		return templete.operate();
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map sell(@RequestBody final Map<String, Object> sale, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Map<String, Object>> detailList = (List) MapUtils.getObject(sale, "detailList");
				sale.remove("detailList");
				int id = saleService.createSale(sale, detailList, this.getUser());

				this.put("id", id);
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/confirm/{saleId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map confirm(@PathVariable final int saleId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
//				saleService.confirm(saleId, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/cancel/{saleId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map cancel(@PathVariable final int saleId,HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
//				saleService.cancel(saleId, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/marketProxy/{saleId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map proxy(@PathVariable final int saleId, @RequestBody final Map<String, Object> marketProxy, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				saleService.saveMarketProxy(saleId, marketProxy, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/getPrintData/{saleId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintData(@PathVariable final int saleId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> sale = saleService.getPrintData(saleId, this.getUser());
				this.put("sale", sale);
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/getPoints/{productId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPoints(@PathVariable final int productId, @RequestParam final String otherCharges, @RequestParam final String saleDiscount, @RequestParam final String authorityDiscount, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				BigDecimal charges = new BigDecimal(otherCharges);
				BigDecimal discount1 = new BigDecimal(saleDiscount);
				BigDecimal discount2 = new BigDecimal(authorityDiscount);
				int points = pointsruleService.getPoints(productId, null, charges, discount1.add(discount2), this.getUser().getOrgId());
				this.put("points", points);
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/saleApprove/{saleId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map saleApprove(@PathVariable final int saleId, @RequestBody final Map<String, Object> approve, HttpServletRequest request){
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				saleService.approveSale(saleId, approve, this.getUser());
			}
		};
		return templete.operate();
	}
	@RequestMapping(value = "/getSaleIdByProductId/{productId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getSaleIdByProductId(@PathVariable final int productId, HttpServletRequest request){
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int saleId = saleService.getSaleIdByProductId(productId, this.getUser());
				this.put("saleId", saleId);
			}
		};
		return templete.operate();
	}
}


