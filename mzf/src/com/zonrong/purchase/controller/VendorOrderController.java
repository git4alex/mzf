package com.zonrong.purchase.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.SettlementType;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.purchase.service.VendorOrderService;
import com.zonrong.purchase.service.DetailCRUDService.VendorOrderDetailStatus;

/**
 * date: 2010-10-15
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class VendorOrderController {
	private Logger logger = Logger.getLogger(VendorOrderController.class);	
	
	private VendorOrderService vendorOrderService;
	public void setVendorOrderService(VendorOrderService orderService) {
		this.vendorOrderService = orderService;
	}
	public abstract VendorOrderType getVendorOrderType();
	
	public abstract SettlementType getSettlementType();
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody	
	public void list(@RequestParam Map parameter, HttpServletRequest request, HttpServletResponse response) {
		try {
			request.getRequestDispatcher("/entity/" + MzfEntity.VENDOR_ORDER_VIEW.getCode() + "?type=" + getVendorOrderType()).forward(request, response);			
		}  catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}	
	}		
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createOrder(@RequestBody final Map vendorOrder, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> order = MapUtils.getMap(vendorOrder, "order");
				List<Map<String, Object>> detailList = (List) MapUtils.getObject(vendorOrder, "detailList");
				int id = vendorOrderService.createOrder(order, detailList,  VendorOrderDetailStatus.New, getVendorOrderType(), this.getUser());
				this.put("id", id);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "{orderId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map update(@PathVariable final int orderId, @RequestBody final Map<String, Object> vendorOrder, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				Map<String, Object> order = MapUtils.getMap(vendorOrder, "order");
				List<Map<String, Object>> detailList = (List) MapUtils.getObject(vendorOrder, "detailList");
				order.remove("type");
				vendorOrderService.updateOrder(orderId, order, detailList, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/submit")
	@ResponseBody
	public Map submit(@RequestParam final Integer[] orderIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				vendorOrderService.submitOrder(orderIds, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/{orderId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map delete(@PathVariable final int orderId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				vendorOrderService.deleteOrder(orderId, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/settle/{orderId}", method = RequestMethod.POST)
	@ResponseBody
	public Map settle(@PathVariable final int orderId, @RequestParam final BigDecimal price, @RequestParam final String remark, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				vendorOrderService.createSettlement(getSettlementType(), orderId, price, remark, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/getPrintData/{vendorOrderId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintData(@PathVariable final int vendorOrderId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> vendorOrder = vendorOrderService.getPrintData(vendorOrderId, this.getUser());
				this.put("vendorOrder", vendorOrder);
			}			
		};
		return templete.operate();			
	}	
}


