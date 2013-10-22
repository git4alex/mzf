package com.zonrong.entity.controller;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-7-20
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/entity/batch")
public class EntityBatchController {
	private Logger logger = Logger.getLogger(EntityBatchController.class);
	
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	
	@RequestMapping(value = "/{code}", method = RequestMethod.POST)
	@ResponseBody
	public Map create(@PathVariable final EntityCode code, @RequestBody final List<Map<String,Object>> list, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(code);
				entityService.batchSave(entiyMetadata, list, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/{code}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map delete(@PathVariable final EntityCode code, @RequestBody final Object[] ids, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> parameters = new HashMap<String, Object>();
				EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(code);
				parameters.put(entiyMetadata.getPkCode(), ids);
				entityService.delete(code, parameters, this.getUser());
			}			
		};

		return templete.operate();
	}	
}


