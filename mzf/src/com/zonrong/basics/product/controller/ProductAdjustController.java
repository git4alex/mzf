package com.zonrong.basics.product.controller;

import com.zonrong.basics.product.service.ProductAdjustService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.util.TpltUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-8-23
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/adjust")
public class ProductAdjustController {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private ProductAdjustService productAdjustService;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createAdust(@RequestBody final Map<String,Object> params, HttpServletRequest request) throws BusinessException {
        final Float pm = MapUtils.getFloat(params,"pm",1f);
        params.remove("pm");
        final Float pi = MapUtils.getFloat(params,"pi",0f);
        params.remove("pi");
        final Float pt = MapUtils.getFloat(params,"pt");
        params.remove("pt");
        final String remark = MapUtils.getString(params,"changeRemark");
        params.remove("changeRemark");
        final List<Map<String,Object>> queryParams = TpltUtils.refactorQueryParams(params);
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productAdjustService.createAdjust(queryParams, pm,pi,pt,remark,this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/cancelAdjust", method = RequestMethod.PUT)
	@ResponseBody
	public Map cancelAdjust(@RequestBody final Integer[] adjustIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productAdjustService.cancelAdjust(adjustIds, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/adjustPrice", method = RequestMethod.PUT)
	@ResponseBody
	public Map adjustPrice(@RequestBody final List<Map<String, Object>> list, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productAdjustService.adjustPrice(list, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/confirm", method = RequestMethod.PUT)
	@ResponseBody
	public Map confirm(@RequestBody final Integer[] adjustIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productAdjustService.confirm(adjustIds, this.getUser());
			}
		};
		return templete.operate();
	}
}


