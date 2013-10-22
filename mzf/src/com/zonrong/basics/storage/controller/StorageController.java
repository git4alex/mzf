package com.zonrong.basics.storage.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.storage.service.StorageService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.util.TreeBuilder;
import com.zonrong.entity.TreeConfig;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-11-2
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value="/code/tree/storage")
public class StorageController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private StorageService storageService;
	
	@Resource
	private MetadataProvider metadataProvider;
	
	@RequestMapping(value = "/{kind}", method = RequestMethod.GET)
	@ResponseBody
	public List list(@PathVariable String kind, @RequestParam Map parameter) {
		try {		
			TreeConfig treeConfig = TreeConfig.getTreeConfig(parameter);			
			
			EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(MzfEntity.ORG_STORAGE);			
			String rootId = MapUtils.getString(parameter, "node", "-1");
			final String idField = entiyMetadata.getPkCode();
			final String pidField = treeConfig.getPidCode();
			List list = storageService.list(treeConfig, kind);
			TreeBuilder b = new TreeBuilder(list){
				public String getPid(Object item){
					Map map = (Map) item;
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
	
}


