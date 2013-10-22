package com.zonrong.core.util;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.zonrong.core.exception.BusinessException;

/**
 * date: 2011-8-26
 *
 * version: 1.0
 * commonts: ......
 */
public class UploadFileUtils {
	private static Logger logger = Logger.getLogger(UploadFileUtils.class);
	
	public enum UploadFileFolder {
		UPLOAD_FOLDER("uploadFolder"),
		TEMP_UPLOAD_FOLDER("tempUploadFolder");
		
		private String folder;
		private UploadFileFolder(String folder) {
			this.folder = folder;
		}
		
		public String toString() {
			return this.folder;
		}
	};
	
	public static File getFile(String id, UploadFileFolder parentFolder) throws BusinessException {
		try {			
			StringBuffer url = new StringBuffer();
			if (StringUtils.isEmpty(id))
				return null;
			
			url.append("/").append(parentFolder).append("/");
			String[] folder = id.split("-");
			for (int i = 0; i < folder.length - 1; i++) {
				url.append(folder[i]).append("/");
			}
			url.append(id.replaceAll("[|]", "."));
			
			String appRootPath = getAppRootPath();	
			File file = new File(appRootPath);
			file = new File(file.getPath() + url.toString());
			
			return file;	
		} catch (Exception e) {
			throw new BusinessException(e.getMessage(), e);
		}
	}

	public static FileInfo getFileInfo(MultipartFile file, UploadFileFolder parentFolder) throws BusinessException {
		try {		
			String appRootPath = getAppRootPath();	
			final StringBuffer fullPath = new StringBuffer(appRootPath + parentFolder);
			final StringBuffer fileName = new StringBuffer();	
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");   
			String dateStr = dateformat.format(new Date());			
			fullPath.append("/").append(dateStr);
			fileName.append(dateStr).append("-");	     
			File dir = new File(fullPath.toString());
			if (!(dir.exists())) {
				dir.mkdirs();
			}			
			
			String originalFilename = file.getOriginalFilename();
			int index = originalFilename.lastIndexOf(".");
			String expandedName = StringUtils.EMPTY;
			if (index >= 0) {
				expandedName = originalFilename.substring(index);
			} 
			expandedName = expandedName.toLowerCase();
			String uuid = UUID.randomUUID().toString().replace("-", "");
			fileName.append(uuid).append(expandedName);			
			
			String fileId = fileName.toString().replace(".", "|");
//			return fileId;
			return new FileInfo(dir.toString(), fileId, fileName.toString());
		} catch (Exception e) {
			throw new BusinessException(e.getMessage(), e);
		}
	}
	
	private static String getAppRootPath() throws URISyntaxException {
		String path = UploadFileUtils.class.getClassLoader().getResource("").toURI().getPath();
		int index = path.indexOf("/WEB-INF");
		if (index > 0) {
			path = path.substring(0, index); 
		}
		return path;
	}
	
	public static void main(String[] args) {
		try {
			getFile("20110824-5631783465585559932|jpg", UploadFileFolder.UPLOAD_FOLDER);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}
	
}

