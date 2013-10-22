package com.zonrong.transfer.product.controller;

import java.util.HashMap;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.transfer.product.service.TransferProductService;

/**
 * date: 2010-11-24
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value="/code/transfer/product")
public class TransferProductController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private TransferProductService transferProductService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map applyTransfer(@RequestBody final Map<String, Object> transfer, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Integer productId = MapUtils.getInteger(transfer, "productId");		
				transfer.remove("productId");
				
				IUser user = this.getUser();
				transferProductService.applyTransfer(productId, user.getOrgId(), transfer, user);				
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value="/fromCusOrder", method = RequestMethod.POST)
	@ResponseBody
	public Map applyTransferFromCusOrder(@RequestBody final Map transfer, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Integer productId = MapUtils.getInteger(transfer, "productId");		
				transfer.remove("productId");
				
				IUser user = this.getUser();
				transferProductService.applyTransferFromCusOrder(productId, user.getOrgId(), transfer, user);				
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/confirm/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map confirm(@PathVariable final int id, @RequestBody final Map<String, Object> approve, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				transferProductService.confirmTransfer(id, approve, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/approve/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map approve(@PathVariable final int id, @RequestBody final Map<String, Object> approve, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				transferProductService.approveTransfer(id, approve, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value="/transfer", method = RequestMethod.POST)
	@ResponseBody
	public Map create(@RequestBody final Map<String, Object> transfer, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Integer> list = (List)MapUtils.getObject(transfer, "productIds");
				Integer[] productIds = list.toArray(new Integer[]{});
				transferProductService.transfer(productIds, transfer, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/send", method = RequestMethod.PUT)
	@ResponseBody
	public Map send(@RequestBody final Integer[] transferIds, HttpServletRequest request) {
		final Map<String, Object> dispatch = new HashMap<String, Object>();
		dispatch.put("targetOrgId", request.getParameter("targetOrgId"));
		dispatch.put("remark", request.getParameter("remark"));
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int dispatchId = transferProductService.send(transferIds, dispatch, this.getUser());
				this.put("dispatchId", dispatchId);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/sendByStore", method = RequestMethod.PUT)
	@ResponseBody
	public Map confirmSend(@RequestBody final Integer[] transferIds, HttpServletRequest request) {
		final String remark = request.getParameter("remark");
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Integer[] dispatchIds = transferProductService.sendByStore(transferIds, remark, this.getUser());
				this.put("dispatchId", dispatchIds);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/receive/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map receive(@PathVariable final int id, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				transferProductService.receive(id, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/getPrintData/{dispatchId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintData(@PathVariable final int dispatchId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> dispatch = transferProductService.getPrintData(dispatchId, this.getUser());
				this.put("dispatch", dispatch);
			}			
		};
		return templete.operate();			
	}
	@RequestMapping(value = "/getSendProductData", method = RequestMethod.PUT)
	@ResponseBody
	public Map getSendProductData(@RequestBody final Integer[] dispatchIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> data = transferProductService.getSendProductData(dispatchIds, this.getUser());
				this.put("data", data);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/getStoreBackProductData", method = RequestMethod.PUT)
	@ResponseBody
	public Map getStoreBackProductData(@RequestBody final int dispatchId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> data = transferProductService.getStoreBackProductData(dispatchId, this.getUser());
				this.put("data", data);
			}			
		};
		return templete.operate();			
	}
	@RequestMapping(value = "/updateTransferStatusByDisNum", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateTransferStatusByDisNum(@RequestBody final int dispatchId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> where = new HashMap<String, Object>();
				where.put("dispatchId", dispatchId);
				transferProductService.updateTransferStatus(where, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/updateTransferStatus", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateTransferStatus(@RequestBody final Integer[] dispatchIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> where = new HashMap<String, Object>();
				where.put("id", dispatchIds);
				transferProductService.updateTransferStatus(where, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	
}


