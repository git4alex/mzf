package com.zonrong.metadata;

import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.zonrong.metadata.MetadataConst.FieldCodeOfField;


/**
 * date: 2010-7-30
 *
 * version: 1.0
 * commonts: ......
 */
public class FieldMetadata {
	private String name;	
	private String code; 
	private String columnName; 
	private String dataType;
	private String bizTypeCode;
	private Integer length;
	private boolean primaryKey;
	private boolean mandatory;
	
	public FieldMetadata(){
		
	}
	
	public FieldMetadata(Map<String,Object> params){
		setName(MapUtils.getString(params, FieldCodeOfField.name.toString()));
		setCode(MapUtils.getString(params, FieldCodeOfField.code.toString()));
		setColumnName(MapUtils.getString(params, FieldCodeOfField.columnName.toString()));
		setDataType(MapUtils.getString(params, FieldCodeOfField.dataType.toString()));
		setLength(MapUtils.getInteger(params, FieldCodeOfField.length.toString(), null));
		setPrimaryKey(MapUtils.getBooleanValue(params, FieldCodeOfField.primaryKey.toString()));
		setMandatory(MapUtils.getBooleanValue(params, FieldCodeOfField.mandatory.toString()));
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public boolean isMandatory() {
		return mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBizTypeCode() {
		return bizTypeCode;
	}
	public void setBizTypeCode(String bizTypeCode) {
		this.bizTypeCode = bizTypeCode;
	}
	
}


