package com.zonrong.purchase.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.common.utils.MzfEnum.SettlementType;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.demand.product.service.ProductDemandProcessService.DemandProcessType;
import com.zonrong.purchase.service.PurchaseOrderService;
import com.zonrong.purchase.service.VendorOrderService;

/**
 * date: 2010-10-15
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/purchaseOrder")
public class PurchaseOrderController extends VendorOrderController {
	private Logger logger = Logger.getLogger(PurchaseOrderController.class);	

	private PurchaseOrderService orderService;
	@Resource(type=PurchaseOrderService.class)
	public void setVendorOrderService(VendorOrderService orderService) {
		this.orderService = (PurchaseOrderService)orderService;		
		super.setVendorOrderService(orderService);
	}
	
	@Override
	public VendorOrderType getVendorOrderType() {
		return VendorOrderType.purchase;
	}
	
	@Override
	public SettlementType getSettlementType() {
		return SettlementType.vendorOrderPurchase;
	}
	
	@RequestMapping(value = "/byDemand", method = RequestMethod.POST)
	@ResponseBody
	public Map createorderByDemand(@RequestParam final Integer[] demandIds, @RequestBody final Map<String, Object> order, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {						
				order.put("type", getVendorOrderType());
				orderService.createOrderByDemand(order, demandIds, DemandProcessType.purchase, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/getSummaryPrintData", method = RequestMethod.GET)
	@ResponseBody
	public Map getSummaryPrintData(@RequestParam final Integer[] vendorOrderIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> vendorOrder = orderService.getSummaryPrintData(vendorOrderIds, this.getUser());
				this.put("vendorOrder", vendorOrder);
			}			
		};
		return templete.operate();			
	}		
}


