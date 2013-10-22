package com.zonrong.core.util;

import java.io.File;

/**
 * date: 2012-3-16
 *
 * version: 1.0
 * commonts: ......
 */
public class FileInfo {
	private String dir;
	private String fileId;
	private String fileName;
	
	public FileInfo(String dir, String fileId, String fileName) {
		super();
		this.dir = dir;
		this.fileId = fileId;
		this.fileName = fileName;
	}
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public File getFile() {
		return new File(dir + "/" + fileName);
	}
}


