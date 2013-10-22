package com.zonrong.basics.rawmaterial.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.rawmaterial.service.RawmaterialService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.inventory.service.RawmaterialInventoryService;
import com.zonrong.inventory.service.InventoryService.BizType;

/**
 * date: 2010-8-23
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/rawmaterial")
public class RawmaterialController {	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private RawmaterialService rawmaterialService;
	@Resource
	private RawmaterialInventoryService rawmaterialInventoryService; 
	
	@RequestMapping(value = "/translateToProduct/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map translateToProduct(@PathVariable final int id, @RequestBody final Map<String, Object> product, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				rawmaterialService.translateToProduct(id, product, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	//原料出库（非裸石）
	@RequestMapping(value = "/deliveryRawmaterial/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map deliveryRawmaterial(@PathVariable final int id, @RequestBody final Map<String, Object> param, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				String quantity = MapUtils.getString(param, "quantity");
				String weight  = MapUtils.getString(param, "weight");
				String remark = MapUtils.getString(param, "remark");
				rawmaterialInventoryService.deliveryRawmaterialById(BizType.delivery,new BigDecimal(quantity),new BigDecimal(weight),id,remark,this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	//裸石出库（原料）
	@RequestMapping(value = "/deliveryDiamond/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map deliveryDiamondByRawmaterialId(@PathVariable final int id, @RequestBody final Map<String, Object> param, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				String remark = MapUtils.getString(param, "remark");
				rawmaterialInventoryService.deliveryDiamondByRawmaterialId(BizType.delivery,id,remark,this.getUser());
			}			
		};
		return templete.operate();			
	}
}


