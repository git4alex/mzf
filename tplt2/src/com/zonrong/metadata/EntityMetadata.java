package com.zonrong.metadata;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.metadata.MetadataConst.DataType;
import com.zonrong.metadata.MetadataConst.FieldCodeOfEntity;

public class EntityMetadata {
	private Logger logger = Logger.getLogger(EntityMetadata.class);

	private String pkCode;
	private String name;
	private String code;
	private String tableName;
	private String deletedCode;
	private List<FieldMetadata> fieldList=new ArrayList<FieldMetadata>();

	private Map<String, FieldMetadata> fieldMap = new HashMap<String, FieldMetadata>();

	public EntityMetadata(){

	}

	public EntityMetadata(Map<String,Object> param){
		setName(MapUtils.getString(param, FieldCodeOfEntity.name.toString()));
		setCode(MapUtils.getString(param, FieldCodeOfEntity.code.toString()));
		setTableName(MapUtils.getString(param, FieldCodeOfEntity.tableName.toString()));
		setDeletedCode(MapUtils.getString(param, FieldCodeOfEntity.delField.toString()));
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<FieldMetadata> getFieldList() {
		return fieldList;
	}

	public void addField(FieldMetadata field) throws BusinessException {
		if (field.isPrimaryKey()){
			if(StringUtils.isNotBlank(pkCode)){
				throw new BusinessException("Duplicate pk field");
			}else{
				pkCode=field.getCode();
			}
		}

		fieldList.add(field);
		fieldMap.put(field.getCode(), field);
	}

	public String getPkCode() {
		return pkCode;
	}

	public void setPkCode(String pkCode) {
		this.pkCode = pkCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDeletedCode() {
		return deletedCode;
	}

	public void setDeletedCode(String deletedCode) {
		this.deletedCode = deletedCode;
	}

	public List<String> getColumnNames() {
		List<String> fields = new ArrayList();
		for (FieldMetadata metadata : fieldList) {
			fields.add(metadata.getColumnName());
		}

		return fields;
	}

	public List<String> getFieldCodes() {
		List<String> codes = new ArrayList();
		for (FieldMetadata metadata : fieldList) {
			codes.add(metadata.getCode());
		}

		return codes;
	}

	public String getColumnName(String fieldCode) {
		try {
			return fieldMap.get(fieldCode).getColumnName();
		} catch (Exception e) {
			logger.debug("not found columnName by fieldCode " + fieldCode);
		}
		return null;
	}

	public String getColumnTitle(String fieldCode) {
		try {
			return fieldMap.get(fieldCode).getName();
		} catch (Exception e) {
			logger.debug("not found columnName by fieldCode " + fieldCode);
		}
		return null;
	}

	public String[] getColumnTitles(String[] filedCodes)throws BusinessException{
		List<String> list = new ArrayList<String>();
		for (String code : filedCodes) {
			list.add(this.getColumnTitle(code));
		}
		return list.toArray(new String[]{});
	}


	public String getFieldDataType(String fieldCode) {
		try {
			return fieldMap.get(fieldCode).getDataType();
		} catch (Exception e) {
			logger.debug("not found dataType by fieldCode " + fieldCode);
		}
		return null;
	}

	public String getFieldDataTypeByFieldName(String fieldName) {
		try {
			Iterator it = fieldMap.keySet().iterator();
			while(it.hasNext()) {
				FieldMetadata metadata = fieldMap.get(it.next());
				if (metadata.getColumnName().equals(fieldName)) {
					return metadata.getDataType();
				}
			}
		} catch (Exception e) {
			logger.debug("not found dataType by fieldName " + fieldName);
		}
		return null;
	}

	/**
	 * Convert {entityField:value} to {dbColumn:value}
	 *
	 * @param parameter {entityField1:value1,entityField2:value2,entityField3:value3, ...}
	 * @param user
	 * @return  {dbColumn1:value1,dbColumn2:value2,dbColumn3:value3, ...}
	 * @throws BusinessException
	 */
	public Map<String,Object> getDbColumnMapForUpdate(Map<String, Object> parameter, IUser user) throws BusinessException {
		Map<String, Object> map = new HashMap<String, Object>();
		Iterator<String> it = parameter.keySet().iterator();
		while(it.hasNext()) {
			String fieldCode = it.next();
			Object fieldValue = parameter.get(fieldCode);

			String colName = getColumnName(fieldCode);
			String fieldDataType = getFieldDataType(fieldCode);

			if (StringUtils.isNotEmpty(colName)) {
				if (DataType.DATATYPE_FLOAT.toString().equalsIgnoreCase(fieldDataType) && "".equals(fieldValue)) {
					fieldValue = null;
				}else if(DataType.DATATYPE_INTEGER.toString().equalsIgnoreCase(fieldDataType) && "".equals(fieldValue)){
					fieldValue = null;
				}
				map.put(colName, getRealValue(fieldDataType, fieldValue, user));
			}
		}
		return map;
	}

	private Object getRealValue(String dataType, Object value, IUser user){
		if (DataType.DATATYPE_DBDEFAULT.toString().equalsIgnoreCase(dataType)) {
			//return DBFun.currentDate;
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		} else if (DataType.DATATYPE_TIMESTAMP.toString().equalsIgnoreCase(dataType)) {
			//return DBFun.currentDate;
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		} else if (DataType.DATATYPE_UID.toString().equalsIgnoreCase(dataType)) {
			return user.getId();
		} else if (DataType.DATATYPE_UNAME.toString().equalsIgnoreCase(dataType)) {
			return user.getName();
		} else if (DataType.DATATYPE_ORG_ID.toString().equalsIgnoreCase(dataType)) {
			return user.getOrgId();
		} else if (DataType.DATATYPE_ORG_NAME.toString().equalsIgnoreCase(dataType)) {
			return user.getOrgName();
		} else if (value != null && value.getClass().isEnum()) {
			return value.toString();
		} else {
			return value;
		}
	}

	public Map<String, Object> getDbColumnMapForQuery(Map<String, Object> parameter) {
		Map<String, Object> map = new HashMap<String, Object>();
		Iterator<String> it = parameter.keySet().iterator();
		while(it.hasNext()) {
			String fieldCode = it.next();
			Object fieldValue = parameter.get(fieldCode);

			String colName = getColumnName(fieldCode);
			if (StringUtils.isNotEmpty(colName)) {
				if (fieldValue != null && fieldValue.getClass().isEnum()) {
					map.put(colName, fieldValue.toString());
				} else {
					map.put(colName, fieldValue);
				}
			}
		}
		return map;
	}

	public void setFieldList(List<FieldMetadata> fieldList) {
		this.fieldList = fieldList;
		for(FieldMetadata fm:fieldList){
			fieldMap.put(fm.getCode(), fm);
		}
	}

}


