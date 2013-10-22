package com.zonrong.entity.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.Page;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.FileInfo;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.core.util.UploadFileUtils;
import com.zonrong.core.util.UploadFileUtils.UploadFileFolder;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.MetadataConst;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.util.TpltUtils;
/**
 * date: 2010-7-20
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/entity")
public class EntityController {
	private static Logger logger = Logger.getLogger(EntityController.class);
	
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	
	@RequestMapping(value = "/{code}", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> list(@PathVariable EntityCode code, @RequestParam Map<String, Object> parameter, HttpServletRequest request) {
		try {			
			OrderBy orderBy = TpltUtils.refactorOrderByParams(parameter);
			
			List<Map<String,Object>> list = TpltUtils.refactorQueryParams(parameter);
//			list.addAll(refactor(request));
			if (MapUtils.getInteger(parameter, "start") != null) {
				Page page = new Page(parameter);
				if (page != null) {		
					return entityService.page(code, list, page.getOffSet(), page.getPageSize(), orderBy, SessionUtils.getUser(request));		
				}	
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, entityService.list(code, list, orderBy, SessionUtils.getUser(request)));
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	
	@RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
	public ModelAndView download(@PathVariable String id, @RequestParam final String cols, HttpServletRequest request) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("id", id);
		
		try {
			logger.debug(cols);		
			List<Map<String, String>> list = new ObjectMapper().readValue(cols, List.class);
			data.put("cols", list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return new ModelAndView("excelView", "data", data);		
	}	
	
	@RequestMapping(value = "/{code}/{id}", method = RequestMethod.GET)
	@ResponseBody	
	public Map get(@PathVariable EntityCode code, @PathVariable String id, HttpServletRequest request) {
		try {
			return entityService.getById(code, id, SessionUtils.getUser(request));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return new HashMap();
		}		
	}	
	
	@RequestMapping(value = "/{code}", method = RequestMethod.POST)
	@ResponseBody
	public Map create(@PathVariable final EntityCode code, @RequestBody final Map parameter, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(code);
				Object id = entityService.create(entiyMetadata, parameter, this.getUser());	
				String generatedKey = StringUtils.isNotEmpty(entiyMetadata.getPkCode())? entiyMetadata.getPkCode():MetadataConst.GENERATED_KEY; 
				this.put(generatedKey, id);
			}			
		};
		return templete.operate();
	}		
	
	@RequestMapping(value = "/{code}/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map update(@PathVariable final EntityCode code, @PathVariable final String id, @RequestBody final Map parameter, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {								
				EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(code);
				entityService.updateById(entiyMetadata, id, parameter, this.getUser());
			}			
		};
		return templete.operate();			
	}	
	
	@RequestMapping(value = "/{code}/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map delete(@PathVariable final EntityCode code, @PathVariable final String id, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(code);
				entityService.deleteById(entiyMetadata, id, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/{code}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map delete(@PathVariable final EntityCode code, @RequestParam final Object[] ids, HttpServletRequest request) {
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
	
	@RequestMapping(value = "/export/{code}", method = RequestMethod.GET)
	public ModelAndView export(@PathVariable EntityCode code, @RequestParam Map parameter, HttpServletRequest request) {
		Map<String, Object> data = new HashMap<String, Object>();
		try {			
			//OrderBy orderBy = refactorOrderByParams(parameter);
			//List<Map<String,Object>> list = refactorQueryParams(parameter);
			//List<Integer> idsList = (List<Integer>) MapUtils.getObject(parameter, "ids");
			String[] idsStr = MapUtils.getString(parameter, "ids") .split(",");
			String[] fieldCodes = MapUtils.getString(parameter, "fieldCodes") .split(",");  //实体属性
			Integer[]ids = new Integer[idsStr.length];
			int i = 0;
			for (String id : idsStr) {
				ids[i] = Integer.parseInt(id);
				i++;
			} 
			
			Map<String,Object> where = new HashMap<String, Object>();
			where.put("id", ids);
			
			EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
			List<Map<String,Object>> dataList = entityService.list(metadata, where, null, SessionUtils.getUser(request)); 
			data.put("dataList", dataList);
			data.put("titles", metadata.getColumnTitles(fieldCodes));
			data.put("fieldCodes", fieldCodes);
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return new ModelAndView("excelView","data",data);
	}	
	
	@RequestMapping(value = "/upload/{code}", method = RequestMethod.POST)
	public Map<String, Object> upload(@PathVariable final EntityCode code,
			@RequestParam() final MultipartFile file,
			@RequestParam final Boolean isPreview,
			@RequestParam final Boolean isCreate, 
			final HttpServletRequest request, HttpServletResponse response){

		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {												
				FileInfo fileInfo = UploadFileUtils.getFileInfo(file, UploadFileFolder.TEMP_UPLOAD_FOLDER);

				File excel = fileInfo.getFile();
				try {
					byte[] bytes = file.getBytes();			
					FileOutputStream fos = new FileOutputStream(excel);
					fos.write(bytes); //写入文件
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e.getMessage(), e);
					throw new BusinessException("文件上传失败");
				}					
				String fileId = fileInfo.getFileId();
				this.put("fileId", fileId);
				logger.debug("fileId: " + fileInfo.getFileId()); // 打印文件大小和文件名称	
				logger.debug("name: " + fileInfo.getFileName()); // 打印文件大小和文件名称
				logger.debug("size: " + file.getSize()); // 打印文件大小和文件名称
				
				EntityMetadata entityMetadata = metadataProvider.getEntityMetadata(code);
				List<Map<String, Object>> data = TpltUtils.readExcel(excel, 0, entityMetadata, true, null);		
				if (isPreview != null && isPreview) {					
					this.put(MetadataConst.ITEMS_ROOT, data);
				}
				
				if (isCreate) {
					data = new ArrayList<Map<String,Object>>();
					data = TpltUtils.readExcel(excel, 0, entityMetadata, false, null);
					entityService.batchCreate(code, data, this.getUser());
				}
			}
		};
		
		return templete.operate();
	}
	
	@RequestMapping(value = "/batch/{code}/(fileId)", method = RequestMethod.POST)
	public Map<String, Object> uploadAndPreview(@PathVariable final EntityCode code, 
			@PathVariable final String fileId, 
			final HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				File excel = UploadFileUtils.getFile(fileId, UploadFileFolder.TEMP_UPLOAD_FOLDER);
				EntityMetadata entityMetadata = metadataProvider.getEntityMetadata(code);
				List<Map<String, Object>> data = TpltUtils.readExcel(excel, 0, entityMetadata, true, null);
				//List<Map<String, Object>> data = TpltUtils.readExcel(excel, 0, fieldList, true, null);
				entityService.batchCreate(code, data, this.getUser());
			}
		};

		return templete.operate();		
	}
}


