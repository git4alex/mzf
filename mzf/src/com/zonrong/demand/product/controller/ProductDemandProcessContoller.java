package com.zonrong.demand.product.controller;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.demand.product.service.ProductDemandProcessService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * date: 2010-10-12
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/demandProcess")
public class ProductDemandProcessContoller {
	private Logger logger = Logger.getLogger(ProductDemandProcessContoller.class);

	@Resource
	private ProductDemandProcessService productDemandProcessService;

	@RequestMapping(value = "/getDemandProcess/{demandId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getDemandProcess(@PathVariable final int demandId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> demandProcess = productDemandProcessService.getById(demandId, this.getUser());
				this.put("demandProcess", demandProcess);
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/allocate/{demandId}", method = RequestMethod.POST)
	@ResponseBody
	public Map allocate(@PathVariable final int demandId, @RequestBody final Map<String, Object> process, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productDemandProcessService.allocate(demandId, process, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/replaceAllocate/{demandId}", method = RequestMethod.POST)
	@ResponseBody
	public Map replaceAllocate(@PathVariable final int demandId, @RequestBody final Map<String, Object> process, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productDemandProcessService.replaceAllocate(demandId, process, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/purchase/{demandId}", method = RequestMethod.POST)
	@ResponseBody
	public Map purchase(@PathVariable final int demandId, @RequestBody final Map<String, Object> process, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productDemandProcessService.purchase(demandId, process, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/OEM/{demandId}", method = RequestMethod.POST)
	@ResponseBody
	public Map oem(@PathVariable final int demandId, @RequestBody final Map<String, Object> process, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productDemandProcessService.oem(demandId, process, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/reject/{demandId}", method = RequestMethod.POST)
	@ResponseBody
	public Map reject(@PathVariable final int demandId, @RequestBody final Map<String, Object> process, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productDemandProcessService.reject(demandId, process, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/sendProduct", method = RequestMethod.POST)
	@ResponseBody
	public Map sendProduct(@RequestBody final Integer[] demandIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productDemandProcessService.sendProduct(demandIds, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/cancel/{demandId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map cancel(@PathVariable final int demandId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productDemandProcessService.cancel(demandId, this.getUser());
			}
		};

		return templete.operate();
	}
}


