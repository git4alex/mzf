package com.zonrong.basics.charges.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.charges.service.ChargesService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.metadata.MetadataConst;

/**
 * 2011-08-23
 * @author Administrator
 * 铂金类商品工费控制器
 */
@Controller
@RequestMapping("/code/charges")
public class ChargesController {
	private Logger logger = Logger.getLogger(ChargesController.class);
	
	@Resource
	private ChargesService chargesService;
	 
	@RequestMapping(value="/update",method=RequestMethod.PUT)
	@ResponseBody
	public Map updateCharges(@RequestBody final Map<String,Object>  charges,HttpServletRequest request){
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				chargesService.updateCharges(charges, this.getUser());
			}
		};

		return templete.operate();		
	}
	 
	@RequestMapping(value="/insert",method=RequestMethod.PUT)
	@ResponseBody
	public Map insertCharges(@RequestBody final Map<String,Object>  charges,HttpServletRequest request){
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				chargesService.createCharges(charges, this.getUser());
			}
		};
		return templete.operate();
	}
	 
	@RequestMapping(value="/view",method=RequestMethod.GET)
	@ResponseBody
	public Map<String,Object> getView(HttpServletRequest request){
		try {
			List<Map> list = chargesService.getChargeViewData();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.debug(e.getMessage(), e);
			return new HashMap();
		}
	}
}
