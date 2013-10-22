
package com.zonrong.entity.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
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
import com.zonrong.core.security.User;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.core.util.TreeBuilder;
import com.zonrong.entity.TreeConfig;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.entity.service.TreeEntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.MetadataConst;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.util.TpltUtils;

/**
 * project: metadataApp
 * date: 2010-5-25
 * author: wangliang
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/entity/tree")
public class TreeEntityController{
	private Logger logger = Logger.getLogger(this.getClass());
	@Resource
	private TreeEntityService treeEntityService;
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	
	@RequestMapping(value = "/{code}", method = RequestMethod.GET)
	@ResponseBody
	public List list(@PathVariable EntityCode code, @RequestParam Map<String, Object> parameter, HttpServletRequest request) {
		try {		
			TreeConfig treeConfig = TreeConfig.getTreeConfig(parameter);			
			String rootId = MapUtils.getString(parameter, "node", "-1");
			EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(code);	
			final String idField = entiyMetadata.getPkCode();
			final String pidField = treeConfig.getPidCode();
			
			List<Map<String,Object>> paramslist = TpltUtils.refactorQueryParams(parameter);			
			List list = treeEntityService.list(entiyMetadata, treeConfig, paramslist, SessionUtils.getUser(request));
			TreeBuilder b=new TreeBuilder(list){
				public String getPid(Object item){
					Map<String, Object> map = (Map) item;
					if (StringUtils.isNotBlank(pidField)) {
						String s = MapUtils.getString(map, pidField);
						return s;
					} else {
						return "-1";
					}
				}
				
				public String getId(Object item){
					Map map = (Map) item;
					return MapUtils.getString(map, idField);
				}
			};
			return b.getTree(rootId);	
		} catch (BusinessException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return new ArrayList();
	}	
	
	@RequestMapping(value = "/{code}/{id}", method = RequestMethod.GET)
	@ResponseBody	
	public Map get(@PathVariable EntityCode code, @PathVariable String id) {
		try {
			return (Map)entityService.getById(code, id, User.getSystemUser());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return new HashMap();
		}		
	}
	
	@RequestMapping(value = "/{code}", method = RequestMethod.POST)
	@ResponseBody
	public Map createNode(@PathVariable final EntityCode code, @RequestParam final Map<String, String> parameter, @RequestBody final Map<String, Object> data, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(code);
				Object id = treeEntityService.createNode(code, TreeConfig.getTreeConfig(parameter), data, this.getUser());	
				String generatedKey = StringUtils.isNotEmpty(entiyMetadata.getPkCode())? entiyMetadata.getPkCode():MetadataConst.GENERATED_KEY;
				this.put(generatedKey, id);
			}			
		};
		return templete.operate();				
	}
	
	@RequestMapping(value = "/{code}/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map update(@PathVariable final EntityCode code, @PathVariable final String id, @RequestBody final Map<String, Object> parameter, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				Map whereMap = new HashMap();	
				EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(code);
				whereMap.put(entiyMetadata.getPkCode(), id);
				parameter.remove(entiyMetadata.getPkCode());
				entityService.updateById(code, id, parameter, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/{code}/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map deleteNode(@PathVariable final EntityCode code, @PathVariable final String id, @RequestParam final Map<String, String> parameter) {
		OperateTemplete templete = new OperateTemplete() {
			protected void doSomething() throws BusinessException {	
				treeEntityService.deleteNode(code, TreeConfig.getTreeConfig(parameter), id);
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/{code}/{id}/{pid}/{index}", method = RequestMethod.PUT)
	@ResponseBody	
	public Map moveNode(@PathVariable final EntityCode code,
			@PathVariable final String id,
			@PathVariable final String pid,
			@PathVariable final int index,
			@RequestBody final Map<String, String> parameter) {
		OperateTemplete templete = new OperateTemplete() {
			protected void doSomething() throws BusinessException {
				treeEntityService.moveNode(code, TreeConfig.getTreeConfig(parameter), id, pid, index);
			}			
		};
		return templete.operate();	
	}
}


