package com.zonrong.basics.customer.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.customer.service.CustomerCardService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.UploadFileUtils;
import com.zonrong.core.util.UploadFileUtils.UploadFileFolder;
import com.zonrong.metadata.MetadataConst;

/**
 * date: 2011-8-25
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/customerCard")
public class CustomerCardController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private CustomerCardService customerCardService;
	
	@RequestMapping(value = "/lock/{cardId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map lock(@PathVariable final int cardId, HttpServletRequest request) {
		final String remark = request.getParameter("remark");
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				customerCardService.lock(cardId, remark, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/free/{cardId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map free(@PathVariable final int cardId, HttpServletRequest request) {
		final String remark = request.getParameter("remark");
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				customerCardService.free(cardId, remark, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/obsolete", method = RequestMethod.PUT)
	@ResponseBody
	public Map obsolete(@RequestBody final Integer[] cardIds, @RequestParam final String remark, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				customerCardService.obsolete(cardIds, remark, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/loadData", method = RequestMethod.GET)
	@ResponseBody
	public Map loadDate(@RequestParam final String fileId, HttpServletRequest request) {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		try {
			File file = UploadFileUtils.getFile(fileId, UploadFileFolder.UPLOAD_FOLDER);
			list = customerCardService.loadData(file, false);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put(MetadataConst.ITEMS_ROOT, list);
		return map;		
	}
	
	@RequestMapping(value = "/batchCreate", method = RequestMethod.POST)
	@ResponseBody
	public Map batchCreate(@RequestBody final List<Map<String, Object>> data, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				customerCardService.batchCreate(data, this.getUser());
			}			
		};
		return templete.operate();			
	}
}


