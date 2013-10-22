package com.zonrong.core.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.UploadFileUtils.UploadFileFolder;

@Controller
public class UploadController {
	private Logger logger = Logger.getLogger(UploadController.class);

	@RequestMapping("/upload")
	public void processImageUpload(@RequestParam final MultipartFile file, final HttpServletRequest request, HttpServletResponse response){
		String appRootPath = request.getSession().getServletContext().getRealPath("/");
		final StringBuffer fullPath = new StringBuffer(appRootPath + UploadFileFolder.UPLOAD_FOLDER);
		final StringBuffer fileName = new StringBuffer();

		OperateTemplete templete = new OperateTemplete() {
			protected void doSomething() throws BusinessException {
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

				try {
					byte[] bytes = file.getBytes();
					FileOutputStream fos = new FileOutputStream(dir + "/" + fileName.toString());
					fos.write(bytes); //写入文件
				} catch (IOException e) {
					e.printStackTrace();
					logger.error(e.getMessage(), e);
					throw new BusinessException("文件上传失败");
				}
				String fileId = fileName.toString().replace(".", "|");
				this.put("fileId", fileId);
				logger.debug("fileId: " + fileId); // 打印文件大小和文件名称
				logger.debug("name: " + fileName); // 打印文件大小和文件名称
				logger.debug("size: " + file.getSize()); // 打印文件大小和文件名称
			}
		};

		Map<String, Object> map = templete.operate();
		StringBuffer sb = new StringBuffer();
		sb.append("{success:" + map.get("success") + ",").append("fileId:'" + map.get("fileId") + "'}");
		try {
			response.setContentType("text/html");
			response.getWriter().write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}
}