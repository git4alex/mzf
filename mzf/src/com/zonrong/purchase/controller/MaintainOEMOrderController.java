package com.zonrong.purchase.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zonrong.common.utils.MzfEnum.SettlementType;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;

/**
 * date: 2011-3-13
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/maintainOEMOrder")
public class MaintainOEMOrderController extends OEMOrderController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public VendorOrderType getVendorOrderType() {
		return VendorOrderType.maintainOEM;
	}
	
	@Override
	public SettlementType getSettlementType() {
		return SettlementType.vendorOrderMaintainOEM;
	}
}


