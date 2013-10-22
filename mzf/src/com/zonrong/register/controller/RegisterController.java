package com.zonrong.register.controller;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.register.service.*;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * date: 2010-11-2
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code")
public class RegisterController {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private RegisterService registerService;
	@Resource
	private RegisterProductService registerProductService;
	@Resource
	private RegisterMaintainProductService registerMaintainProductService;
	@Resource
	private RegisterRawmaterialService registerRawmaterialService;
	@Resource
	private RegisterMaterialService registerMaterialService;

    @RequestMapping(value = "/returnRawmaterial/{dosingId}", method = RequestMethod.POST)
    @ResponseBody
    public Map returnRawmaterial(@PathVariable final String dosingId, @RequestBody final Map<String, Object> rawmaterial, HttpServletRequest request) {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                registerRawmaterialService.returnRawmaterialFromOem(dosingId,rawmaterial,this.getUser());
            }
        };
        return templete.operate();
    }

	@RequestMapping(value = "/registerProductFormMaintainOEM", method = RequestMethod.POST)
	@ResponseBody
	public Map registerProductFormOEM(@RequestBody final Map<String, Object> register, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int orderDetailId = MapUtils.getInteger(register, "orderDetailId");
				Map<String, Object> product = MapUtils.getMap(register, "product");
				int id = registerMaintainProductService.register(orderDetailId, product, register, this.getUser());
				this.put("id", id);
				this.put("targetNum", registerService.getTargetNum(id, this.getUser()));
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/registerProductFormOEM", method = RequestMethod.POST)
	@ResponseBody
	public Map registerProductFormMaintainOEM(@RequestBody final Map<String, Object> register, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int orderDetailId = MapUtils.getInteger(register, "orderDetailId");
				Map<String, Object> product = MapUtils.getMap(register, "product");
				int id = registerProductService.register(orderDetailId, product, register, this.getUser());
				this.put("id", id);
				this.put("targetNum", registerService.getTargetNum(id, this.getUser()));
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/registerProductFormPurchase", method = RequestMethod.POST)
	@ResponseBody
	public Map registerProductFormPurchase(@RequestBody final Map<String, Object> register, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int orderDetailId = MapUtils.getInteger(register, "orderDetailId");
				Map<String, Object> product = MapUtils.getMap(register, "product");

				int id = registerProductService.register(orderDetailId, product, register, this.getUser());
				this.put("id", id);
				this.put("targetNum", registerService.getTargetNum(id, this.getUser()));
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/registerRawmaterial", method = RequestMethod.POST)
	@ResponseBody
	public Map registerRawmaterial(@RequestBody final Map register, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int orderDetailId = MapUtils.getInteger(register, "orderDetailId");
				Map rawmaterial = MapUtils.getMap(register, "rawmaterial");

				int id = registerRawmaterialService.register(orderDetailId, rawmaterial, register, this.getUser());
				this.put("id", id);
				this.put("targetNum", registerService.getTargetNum(id, this.getUser()));
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/registerMaterial", method = RequestMethod.POST)
	@ResponseBody
	public Map registerMaterial(@RequestBody final Map register, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int orderDetailId = MapUtils.getInteger(register, "orderDetailId");
				Map material = MapUtils.getMap(register, "material");

				int id = registerMaterialService.register(orderDetailId, material, register, this.getUser());
				this.put("id", id);
				this.put("targetNum", registerService.getTargetNum(id, this.getUser()));
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/register/printMaterial", method = RequestMethod.GET)
	@ResponseBody
	public Map printMaterial(@RequestParam final Integer[] ids, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> data = registerMaterialService.getPrintData(ids, this.getUser());
				this.put("data", data);
			}
		};
		return templete.operate();
	}
	@RequestMapping(value = "/register/printProduct", method = RequestMethod.GET)
	@ResponseBody
	public Map printProduct(@RequestParam final Integer[] ids, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> data = registerProductService.getPrintData(ids, this.getUser());
				this.put("data", data);
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/register/printRawmaterial", method = RequestMethod.GET)
	@ResponseBody
	public Map printRawmaterial(@RequestParam final Integer[] ids, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> data = registerRawmaterialService.getPrintData(ids, this.getUser());
				this.put("data", data);
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/updateRegisterStatus", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateRegisterStatus(@RequestBody final Integer[] ids, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				registerService.updateStatus(ids, this.getUser());
			}
		};
		return templete.operate();
	}
}


