package com.zonrong.transfer.common.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.TransferTargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.User;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.entity.service.EntityService;
import com.zonrong.transfer.maintain.service.TransferMaintainProductService;
import com.zonrong.transfer.material.service.TransferMaterialService;
import com.zonrong.transfer.product.service.TransferProductService;
import com.zonrong.transfer.secondGold.service.TransferSecondGoldService;
import com.zonrong.transfer.secondProduct.service.TransferSecondProductService;

/**
 * date: 2010-11-24
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value="/code/transfer")
public class TransferController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private EntityService entityService;	
	@Resource
	private TransferProductService transferProductService;
	@Resource
	private TransferMaintainProductService transferMaintainProductService;
	@Resource
	private TransferSecondGoldService transferSecondGoldService;
	@Resource
	private TransferSecondProductService transferSecondProductService;
	@Resource
	private TransferMaterialService transferMaterialService;
	
	@RequestMapping(value = "/cancel/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map cancel(@PathVariable final int id, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> transfer = entityService.getById(MzfEntity.TRANSFER_VIEW, id, User.getSystemUser());
				TransferTargetType type = TransferTargetType.valueOf(MapUtils.getString(transfer, "targetType"));

				if (type == TransferTargetType.product) {					
					transferProductService.cancelTransfer(transfer, this.getUser());
				} else if (type == TransferTargetType.maintainProduct) {					
					transferMaintainProductService.cancelTransfer(transfer, this.getUser());
				} else if (type == TransferTargetType.secondGold) {						
					transferSecondGoldService.cancelTransfer(transfer, this.getUser());
				} else if (type == TransferTargetType.secondProduct) {					
					transferSecondProductService.cancelTransfer(transfer, this.getUser());
				} else if (type == TransferTargetType.material) {					
					transferMaterialService.cancelTransfer(transfer, this.getUser());
				}
				
			}			
		};
		return templete.operate();			
	}	
}


