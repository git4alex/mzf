package com.zonrong.basics.vendor.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.vendor.service.VendorService;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.Page;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.util.TpltUtils;

/**
 * date: 2010-8-23
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/vendor")
public class VendorController {	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private VendorService vendorService;
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> list(@RequestParam Map parameter, HttpServletRequest request) {
		try {			
			OrderBy orderBy = TpltUtils.refactorOrderByParams(parameter);
			
			List<Map<String,Object>> list = TpltUtils.refactorQueryParams(parameter);
//			list.addAll(refactor(request));
			if (MapUtils.getInteger(parameter, "start") != null) {
				Page page = new Page(parameter);
				if (page != null) {		
					return vendorService.page(list, page.getOffSet(), page.getPageSize(), orderBy, SessionUtils.getUser(request));		
				}	
			} else {
				throw new BusinessException("未找到分页参数");
			}		
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}	
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map create(@RequestBody final Map<String, Object> vendor, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {		
				List<Map<String, Object>> linkmanList = (List) MapUtils.getObject(vendor, "linkmanList");
				List<Map<String, Object>> accountList = (List) MapUtils.getObject(vendor, "accountList");
				vendor.remove("linkmanList");
				vendor.remove("acountList");
				
				int id = vendorService.createVendor(vendor, linkmanList, accountList, this.getUser()); 
				this.put("id", id);
			}			
		};
		return templete.operate();
	}		
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map update(@PathVariable final int id, @RequestBody final Map<String, Object> vendor, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				List<Map<String, Object>> linkmanList = (List) MapUtils.getObject(vendor, "linkmanList");
				List<Map<String, Object>> accountList = (List) MapUtils.getObject(vendor, "accountList");
				vendor.remove("linkmanList");
				vendor.remove("accountList");
				vendorService.updateVendor(id, vendor, linkmanList, accountList, this.getUser());
			}			
		};
		return templete.operate();			
	}	
}


