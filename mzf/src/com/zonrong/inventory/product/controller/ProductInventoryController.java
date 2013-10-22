package com.zonrong.inventory.product.controller;

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

import com.zonrong.basics.product.service.ProductService;
import com.zonrong.common.utils.MzfEnum.InventoryStatus;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.inventory.product.service.TemporaryInventoryService;
import com.zonrong.inventory.service.InventoryService.BizType;

/**
 * date: 2010-12-22
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/productInventory")
public class ProductInventoryController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private TemporaryInventoryService temporaryInventoryService;
	@Resource
	private ProductService productService;
	
	@RequestMapping(value = "/findByProductNum/{productNum}", method = RequestMethod.GET)
	@ResponseBody
	public Map findByProductNum(@PathVariable final String productNum, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> product = productInventoryService.getProductInventoryByProductNum(productNum, this.getUser().getOrgId());
				this.put("product", product);
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/transferToTemporary", method = RequestMethod.PUT)
	@ResponseBody
	public Map transferToTemporary(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Integer> productIdList = (List<Integer>) MapUtils.getObject(map, "productIds");
				Integer[] productIds = productIdList.toArray(new Integer[]{});
				String remark = MapUtils.getString(map, "remark");
				productInventoryService.transferToTemporary(productIds, remark, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/transferToProductStorage", method = RequestMethod.PUT)
	@ResponseBody
	public Map transferFromTemporary(@RequestBody final Integer[] productId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {					
				temporaryInventoryService.transferToProductStorage(productId, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/deliveryFromTemporary", method = RequestMethod.PUT)
	@ResponseBody
	public Map deliveryFromTemporary(@RequestBody final Map<String, Object> delivery, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {		
				List<Integer> list = (List<Integer>)MapUtils.getObject(delivery, "productId");
				Integer[] productId = list.toArray(new Integer[]{});
				String deliveryReason = MapUtils.getString(delivery, "deliveryReason");
				String remark = MapUtils.getString(delivery, "remark");
				temporaryInventoryService.deliveryFromTemporary(productId, deliveryReason, remark, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/warehouseToTemporary/{productId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map warehouseToTemporary(@PathVariable final int productId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {					
				temporaryInventoryService.warehouseToTemporary(productId, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/free/{productId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map free(@PathVariable final int productId, @RequestBody final Map<String, Object> params, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				String remark = MapUtils.getString(params, "remark");
				productService.free(productId, remark, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/drop/{productId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map drop(@PathVariable final int productId, @RequestBody final Map<String, Object> params, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				String remark = MapUtils.getString(params, "remark");
				temporaryInventoryService.dropProduct(productId, remark, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/updateSplit", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateSplit(@RequestBody final Integer[] inventoryIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				temporaryInventoryService.updateSplit(inventoryIds, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/deliveryByProductId/{productId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map deliveryByProduct(@PathVariable final int productId,@RequestBody final Map<String, Object> params, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				String remark = MapUtils.getString(params, "remark");
		        String priorStatus = MapUtils.getString(params, "priorStatus");
				productInventoryService.deliveryByProductId(BizType.delivery, productId, remark, InventoryStatus.valueOf(priorStatus), this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/printData", method = RequestMethod.GET)
	@ResponseBody
	public Map printData(@RequestParam final Integer[] ids, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> data = temporaryInventoryService.getPrintData(ids,this.getUser());
				this.put("data", data);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/updatePrintStatus", method = RequestMethod.PUT)
	@ResponseBody
	public Map updatePrintStatus(@RequestBody final Integer[] ids, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				temporaryInventoryService.updatePrintStatus(ids, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
}


