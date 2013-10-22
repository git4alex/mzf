package com.zonrong.metadata.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.TreeProcess;
import com.zonrong.core.util.TreeProcessHelper;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.MetadataConst;
import com.zonrong.metadata.service.MetadataCRUDService;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-7-20
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/entityMetadata")
public class EntityMetadataController {
	private Logger logger = Logger.getLogger(EntityMetadataController.class);
	
	@Resource
	private MetadataCRUDService metadataCRUDService;
	@Resource
	private MetadataProvider metadataProvider;	
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> listEntity(@RequestParam Map parameter) {
		try {		
			List<Map<String, Object>> list = metadataCRUDService.listEntity(parameter);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;
		} catch (BusinessException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return new HashMap<String, Object>();
	}
	
	@RequestMapping(value = "/tree", method = RequestMethod.GET)
	@ResponseBody	
	public List getEntityTree(@RequestParam Map parameters) {
		try {
			List<Map<String, Object>> list = metadataCRUDService.listEntity(parameters);
			
			List<Map> treeList = TreeProcess.buildTree(list, -1, new TreeProcessHelper("id", "pid"));
			return treeList;
		} catch (BusinessException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		
		return new ArrayList();
	}	
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody	
	public Map saveEntity(@RequestBody final Map parameters, HttpServletRequest request) {					
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.ENTITY);
				Object id = metadataCRUDService.createEntity(parameters, this.getUser());
				String generatedKey = StringUtils.isNotEmpty(metadata.getPkCode())? metadata.getPkCode():MetadataConst.GENERATED_KEY;
				this.put(generatedKey, id);
			}			
		};
		return templete.operate();	
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody	
	public Map updateEntity(@PathVariable final int id, @RequestBody final Map<String, Object> parameter, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
//				Integer id = MapUtils.getInteger(parameter, FieldCodeOfEntity.id.toString());
				metadataCRUDService.updateEntityById(id, parameter, this.getUser());
			}			
		};
		return templete.operate();		
	}	
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody	
	public Map deleteEntity(@PathVariable final int id) throws IOException {				
		OperateTemplete templete = new OperateTemplete() {
			protected void doSomething() throws BusinessException {
				metadataCRUDService.deleteEntity(id);	
			}			
		};
		return templete.operate();
	}	
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody	
	public Map getEntity(@PathVariable final int id)  {
		try {
			Map entityMetadata = metadataCRUDService.getEntityById(id);
			return entityMetadata;
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		
		return new HashMap();
	}	
}


