package com.zonrong.purchase.controller;

import com.zonrong.common.utils.MzfEnum.SettlementType;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.purchase.service.OEMOrderService;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * date: 2010-12-9
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/OEMOrder")
public class OEMOrderController {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource(type=OEMOrderService.class)
	private OEMOrderService orderService;

	public VendorOrderType getVendorOrderType() {
		return VendorOrderType.OEM;
	}

	public SettlementType getSettlementType() {
		return SettlementType.vendorOrderOEM;
	}

	@RequestMapping(value = "/byDemand", method = RequestMethod.POST)
	@ResponseBody
	public Map createOrderByDemand(@RequestParam final Integer[] demandIds, @RequestBody final Map<String, Object> order, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int id = orderService.createOrderByDemand(order, demandIds, this.getUser());
				this.put("id", id);
			}
		};
		return templete.operate();
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createOrder(@RequestBody final Map<String, Object> order, HttpServletRequest request) throws BusinessException {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int id = orderService.createOrder(order, getVendorOrderType(), this.getUser());
				this.put("id", id);
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/{orderId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateOrder(@PathVariable final int orderId, @RequestBody final Map<String, Object> order, HttpServletRequest request) throws BusinessException {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				orderService.updateOrder(orderId, order, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/submit", method = RequestMethod.POST)
	@ResponseBody
	public Map submit(@RequestParam final Integer[] orderIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				orderService.submitOrder(orderIds, getVendorOrderType(), this.getUser());
			}
		};
		return templete.operate();
	}

    @RequestMapping(value = "/submitOemOrder/{orderId}", method = RequestMethod.POST)
    @ResponseBody
    public Map submit(@RequestParam final Map<String,Object> params, @PathVariable final Integer orderId, HttpServletRequest request) {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                orderService.submitOemOrder(orderId, params, this.getUser());
            }
        };
        return templete.operate();
    }

	@RequestMapping(value = "/{orderId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map delete(@PathVariable final int orderId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				orderService.deleteOrder(orderId, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/detail", method = RequestMethod.POST)
	@ResponseBody
	public Map createDetail(@RequestBody final Map<String, Object> detail, HttpServletRequest request) throws BusinessException {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Integer orderId = MapUtils.getInteger(detail, "orderId");
				int id = orderService.createDetail(orderId, detail, this.getUser());
				this.put("id", id);
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/detail/{detailId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateDetail(@PathVariable final int detailId, @RequestBody final Map<String, Object> detail, HttpServletRequest request) throws BusinessException {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				orderService.updateDetail(detailId, detail, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/detail/{detailId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map deleteDetail(@PathVariable final int detailId, HttpServletRequest request) throws BusinessException {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				orderService.deleteDetail(detailId, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/cancelRawmaterial/{orderId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map cancelRawmaterial(@PathVariable final Integer orderId, @RequestBody final Map<String, Object> map, HttpServletRequest request) throws BusinessException {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				String remark = MapUtils.getString(map, "remark");
				orderService.cancelRawmaterial(orderId, remark, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/settle/{orderId}", method = RequestMethod.POST)
	@ResponseBody
	public Map settle(@PathVariable final int orderId, @RequestParam final BigDecimal price, @RequestParam final String remark, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				orderService.createSettlement(getSettlementType(), orderId, price, remark, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/getPrintData/{vendorOrderId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintData(@PathVariable final int vendorOrderId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> vendorOrder = orderService.getPrintData(vendorOrderId, this.getUser());
				this.put("vendorOrder", vendorOrder);
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

    @RequestMapping(value = "/cancelDetail/{detailId}", method = RequestMethod.PUT)
    @ResponseBody
    public Map cancelDetail(@PathVariable final String detailId, HttpServletRequest request) throws BusinessException {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                orderService.cancelDetail(detailId,this.getUser());
            }
        };
        return templete.operate();
    }
}


