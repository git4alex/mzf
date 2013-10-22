package com.zonrong.purchase.controller;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zonrong.common.utils.MzfEnum.SettlementType;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;
import com.zonrong.purchase.service.MaterialOrderService;
import com.zonrong.purchase.service.VendorOrderService;

/**
 * date: 2010-10-15
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/materialOrder")
public class MaterialOrderController extends VendorOrderController {
	private Logger logger = Logger.getLogger(MaterialOrderController.class);	

	@Resource(type=MaterialOrderService.class)
	public void setVendorOrderService(VendorOrderService orderService) {
		super.setVendorOrderService(orderService);
	}
	
	@Override
	public VendorOrderType getVendorOrderType() {
		return VendorOrderType.material;
	}
	
	@Override
	public SettlementType getSettlementType() {
		return SettlementType.vendorOrderMaterial;
	}
}


