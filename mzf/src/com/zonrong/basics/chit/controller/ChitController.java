package com.zonrong.basics.chit.controller;

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

import com.zonrong.basics.chit.service.ChitService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.UploadFileUtils;
import com.zonrong.core.util.UploadFileUtils.UploadFileFolder;
import com.zonrong.metadata.MetadataConst;

@Controller
@RequestMapping(value = "/code/chit")
public class ChitController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private ChitService chitService;
	
	@RequestMapping(value = "/loadData", method = RequestMethod.GET)
	@ResponseBody
	public Map loadData(@RequestParam final String fileId, HttpServletRequest request) {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			File file = UploadFileUtils.getFile(fileId, UploadFileFolder.UPLOAD_FOLDER);
			list = chitService.loadChitData(file, false);
			map.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage(), e);
			map.put("msg",e.getMessage());
		}

		
		map.put(MetadataConst.ITEMS_ROOT, list);
		return map;		
	}
	
	@RequestMapping(value = "/batchCreate", method = RequestMethod.POST)
	@ResponseBody
	public Map batchCreate(@RequestBody final List<Map<String, Object>> data, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				chitService.batchCreate(data, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/updateChitOrg/{orgId}", method = RequestMethod.POST)
	@ResponseBody
	public Map updateChitOrg(@RequestBody final Integer[] chitIds,@PathVariable final int orgId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				chitService.updateChitOrg(orgId, chitIds, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	
	@RequestMapping(value="/batchSellChit",method = RequestMethod.POST)
	@ResponseBody
	public Map  batchSellChit(@RequestBody final Map<String,Object> param,HttpServletRequest request){
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				List<Integer> list = (List)param.get("ids");
				Integer[] chitIds = list.toArray(new Integer[]{}); 
				String remark = param.get("remark").toString();
				chitService.batchSellChit(chitIds, this.getUser(),remark);
			}			
		};
		return templete.operate();
	}
	@RequestMapping(value = "/lockChit/{chitId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map lockChit(@PathVariable final int chitId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				chitService.lockChit(chitId, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/obsoleteChit/{chitId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map obsoleteChit(@PathVariable final int chitId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				chitService.obsoleteChit(chitId, this.getUser());
			}			
		};
		return templete.operate();			
	}
}
