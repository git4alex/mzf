package com.zonrong.entity.code;


/**
 * date: 2011-7-8
 *
 * version: 1.0
 * commonts: ......
 */
public enum TpltEnumEntityCode implements IEntityCode {
	/**
	 * 实体
	 */
	ENTITY("entity"),
	/**
	 * 实体字段
	 */
	FIELD("field"),
	
	/**
	 * 用户
	 */
	USER("user"),
	
	/**
	 * 系统提醒
	 */
	NOTICE("notice"),
	
	/**
	 * 组织机构
	 */
	ORG("org"),
	
	
	/**
	 * 业务流程日志
	 */
	BIZ_LOG("bizLog"),
	
	/**
	 * 文件
	 */
	UPLOAD("upload"),

	/**
	 * 业务编码类型
	 */
	BIZ_TYPE("bizType"),
	
	/**
	 * 业务编码
	 */
	BIZ_CODE("bizCode"),

	/**
	 * 日志
	 */
	LOG("log"),
	
	/**
	 * 模块
	 */
	MODULE("module"),
	
	/**
	 * 模块历史
	 */
	MODULE_HISTORY("moduleHistory");
	
	TpltEnumEntityCode(String code) {
		this.code = code;
	}
	private String code;
	public String getCode() {
		return this.code;
	}

}


