package com.zonrong.core.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.security.User;
import com.zonrong.core.util.UploadFileUtils.UploadFileFolder;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;

/**
 * date: 2010-9-29
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
public class ImageController {
	private Logger logger = Logger.getLogger(ImageController.class);
	@Resource
	private EntityService entityService;
	
	@RequestMapping(value = "/image/{id}", method = RequestMethod.GET)
	public void getImage(final @PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
		FileOutputStream fos = null;
		StringBuffer url = new StringBuffer();
		boolean hasImage = false;
		try {
			if(StringUtils.isNotEmpty(id)) {
				url.append("/").append(UploadFileFolder.UPLOAD_FOLDER).append("/");
				String[] folder = id.split("-");
				for (int i = 0; i < folder.length - 1; i++) {
					url.append(folder[i]).append("/");
				}
				url.append(id.replaceAll("[|]", "."));
				
				File file = new File(this.getClass().getClassLoader().getResource("").toURI().getPath());
				file = file.getParentFile().getParentFile();
				file = new File(file.getPath() + url.toString());
				if (!file.exists()) {
					Map<String, Object> image = entityService.getById(TpltEnumEntityCode.UPLOAD, id, User.getSystemUser());
					if (MapUtils.isNotEmpty(image)) {
						File dir = new File(file.getParent());
						if (!(dir.exists())) {
							dir.mkdirs();
						}						
						Object obj = image.get("content");
						byte[] bytes = (byte[])obj;
						fos = new FileOutputStream(file.getPath(),true);   
			            fos.write(bytes);   
			            fos.flush(); 	
			            hasImage = true;
					} 					
				} else {
				    hasImage = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} 
		
		finally{							
			try{   
				if (fos != null) {
					fos.close();   					
				}
				
				if (hasImage) {
					request.getRequestDispatcher(url.toString()).forward(request, response);
				} else {
					request.getRequestDispatcher("/images/noImage.jpg").forward(request, response);					
				}
            }   
            catch(Exception e){
            	e.printStackTrace();
            	logger.error(e.getMessage(), e);
            }  
		}		
	}
	
	@RequestMapping(value = "/image/{code}/{id}", method = RequestMethod.GET)
	@ResponseBody
	public void getImage(final @PathVariable EntityCode code, final @PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
		try {
			Map entity = entityService.getById(code, id, User.getSystemUser());
			String imageId = MapUtils.getString(entity, "imageId");
			if (StringUtils.isNotEmpty(imageId)) {
				request.getRequestDispatcher("/image/" + imageId).forward(request, response);
				return;
			}
			request.getRequestDispatcher("/images/noImage.jpg").forward(request, response);		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}
	
	@RequestMapping(value = "/image", method = RequestMethod.GET)
	@ResponseBody
	public void getNoImage(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.getRequestDispatcher("/images/noImage.jpg").forward(request, response);		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}	
}


