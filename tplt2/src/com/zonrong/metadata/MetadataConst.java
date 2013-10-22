package com.zonrong.metadata;



/**
 * date: 2010-7-20
 *
 * version: 1.0
 * commonts: ......
 */
public class MetadataConst { 
	public static final String ID = "ID";
	public static final String ENTITY_ID = "ENTITY_ID";
	public static final String ENTITY_METADATA_ID = "id";

	
	public static final String ENTITY_METADATA_TREE_ID = "id";
	public static final String ENTITY_METADATA_TREE_PID = "pid";
	public static final String ENTITY_METADATA_TREE_ORDERBY = "orderBy";
	
	public static final String ENTITY_METADATA_TREE_ID_FIELD = "idField";
	public static final String ENTITY_METADATA_TREE_PID_FIELD = "pidField";
	public static final String ENTITY_METADATA_TREE_ORDERBY_FIELD = "orderByField";
	public static final String ENTITY_METADATA_TREE_PATH_FIELD = "pathField";
	public static final String ENTITY_METADATA_TREE_DEPTH_FIELD = "depthField";

	/**
	 * 实体[entity]对应的字段code
	 */
	public enum FieldCodeOfEntity{
		id,
		name,
		code,
		aliasCode,
		tableName,
		entityType,
		entityAttribute,
		delField
	}
	
	/**
	 * 实体[field]对应的字段code 
	 */
	public enum FieldCodeOfField{
		id,
		entityId,
		name,	//中文名称
		code, //英文名称
		columnName, //字段名
		dataType,
		length,
		precision,
		primaryKey,
		mandatory, //可否空
		orderBy
	}
	
	public static final String ENTITY_FIELD_LIST = "FIELD_LIST";
	public static final String ENTITY_DATA_TYPE = "DATA_TYPE";
	
	public static final String MOVENODE_ID = "ID";
	public static final String MOVENODE_TARGET_PID = "TARGET_PID";
	public static final String MOVENODE_TARGET_ORDERBY = "TARGET_ORDERBY";
	
	public static final String UPDATE_SET_FIELD = "setField";
	public static final String UPDATE_WHERE_FIELD = "whereField";
	
	public static final String TABLE_NAME_PATTERN = "tableNamePattern";	
	
	public static final String ITEMS_ROOT = "root";
	
	public static final String GENERATED_KEY = "generatedKey";		
	
	public enum DataType {
		DATATYPE_FLOAT("float"),
		DATATYPE_INTEGER("integer"),
		DATATYPE_BOOLEAN("boolean"),
		DATATYPE_STRING("string"),
		DATATYPE_IMAGE("image"),
		DATATYPE_DBDEFAULT("dbdefault"),
		DATATYPE_TIMESTAMP("timestamp"),
		DATATYPE_UID("uid"),
		DATATYPE_UNAME("uname"),
		DATATYPE_ORG_ID("orgId"),
		DATATYPE_ORG_NAME("orgName");
		
		private String dataType;
		private DataType(String dataType) {
			this.dataType = dataType;
		}
		public String toString() {
			return this.dataType;
		}
	}
	
	public static final int ENTITY_TYPE_SIMPLE = 1;
	public static final int ENTITY_TYPE_TREE = 2;
}


