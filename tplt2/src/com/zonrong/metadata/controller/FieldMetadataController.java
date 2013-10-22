package com.zonrong.metadata.controller;

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

import com.zonrong.core.dao.Page;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.MetadataConst;
import com.zonrong.metadata.MetadataConst.FieldCodeOfField;
import com.zonrong.metadata.service.MetadataCRUDService;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-7-20
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/fieldMetadata")
public class FieldMetadataController {
	private Logger logger = Logger.getLogger(FieldMetadataController.class);
	
	@Resource
	private MetadataCRUDService metadataCRUDService;
	@Resource
	private MetadataProvider metadataProvider;
	
	@RequestMapping(value = "/page", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> queryFieldForPage(@RequestParam Map parameters) {
		Page page = new Page(parameters);
		try {			
			return metadataCRUDService.pageField(parameters, page.getOffSet(), page.getPageSize());
		} catch (BusinessException e) {
			// TODO: handle exception
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return new HashMap<String, Object>();
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Map queryFieldForList(@RequestParam Map<String, Object> parameter) {		
		try {
			List<Map<String, Object>> list = metadataCRUDService.listField(parameter);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;	
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return new HashMap();
	}
	
	@RequestMapping(value = "/listByEntityId/{entityId}", method = RequestMethod.GET)
	@ResponseBody
	public Map queryFieldForList(@PathVariable String entityId) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(FieldCodeOfField.entityId.toString(), entityId);
				
		try {
			List list = metadataCRUDService.listField(parameters);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;			
		} catch (BusinessException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return new HashMap();	
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map saveField(@RequestBody final Map parameters, HttpServletRequest request) {				
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.FIELD);
				Object id = metadataCRUDService.createField(parameters, this.getUser());
				String generatedKey = StringUtils.isNotEmpty(metadata.getPkCode())? metadata.getPkCode():MetadataConst.GENERATED_KEY;
				this.put(generatedKey, id);
			}			
		};
		return templete.operate();
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateField(@PathVariable final int id, @RequestBody final Map parameter, HttpServletRequest request) {	
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				//Integer id = MapUtils.getInteger(parameter, FieldCodeOfEntity.id.toString());
				metadataCRUDService.updateFieldById(id, parameter, this.getUser());
			}			
		};
		return templete.operate();		
	}	
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Map getField(@PathVariable int id) throws Exception {
		Map fieldMetadata = metadataCRUDService.getFieldById(id);
		return fieldMetadata;
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map deleteField(@PathVariable final int id) {		
		OperateTemplete templete = new OperateTemplete() {
			protected void doSomething() throws BusinessException {
				metadataCRUDService.deleteFieldById(id);	
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/deleteByEntityId/{entityId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map deleteFieldByEntityId(@PathVariable final int entityId) {
		OperateTemplete templete = new OperateTemplete() {
			protected void doSomething() throws BusinessException {
				metadataCRUDService.deleteFieldByEntityId(entityId);
			}			
		};
		return templete.operate();		
	}
	
	@RequestMapping(value = "/listColumnsByTableName/{tableName}", method = RequestMethod.GET)
	@ResponseBody
	public Map queryListColumsByEntityId(@PathVariable String tableName) {
		try {
			List<Map> list = metadataProvider.loadColumsFromDbMetadata(tableName);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;
		} catch (BusinessException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		
		return new HashMap();
	}
	
	@RequestMapping(value = "/move/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map move(@PathVariable final int id, @RequestParam final int step, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {		
				metadataCRUDService.move(id, step, this.getUser());
			}			
		};
		return templete.operate();			
	}	
}


