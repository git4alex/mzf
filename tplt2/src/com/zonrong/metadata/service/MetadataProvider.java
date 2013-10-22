package com.zonrong.metadata.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.metadata.TableMetaDataContext;
import org.springframework.jdbc.core.metadata.TableMetaDataProvider;
import org.springframework.jdbc.core.metadata.TableMetaDataProviderFactory;
import org.springframework.jdbc.core.metadata.TableParameterMetaData;
import org.springframework.stereotype.Service;

import com.zonrong.core.dao.Dao;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.FieldMetadata;
import com.zonrong.metadata.MetadataConst;
import com.zonrong.metadata.XmlMetadataProvider;
import com.zonrong.metadata.MetadataConst.FieldCodeOfField;
import com.zonrong.util.TpltUtils;

/**
 * date: 2010-7-20
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class MetadataProvider {
	private static Logger logger = Logger.getLogger(MetadataProvider.class);
	@Resource
	private Dao dao;
	
	@Resource
	private MetadataCRUDService metadataCRUDService;
	@Resource
	private XmlMetadataProvider xmlMetadataProvider;	
	
	private Map<String, EntityMetadata> entitymetadataMap = new HashMap();
	
	public void clearMetadataCache() {
		entitymetadataMap.clear();
		logger.info("元数据缓存已经清除");
	}
	
	public EntityMetadata getEntityMetadata(IEntityCode code) throws BusinessException{
		EntityMetadata metadata = entitymetadataMap.get(code.getCode());
		if (metadata != null) {
			return metadata;
		}
		
		metadata = xmlMetadataProvider.getMetadataByEntityCode(code);
		
		if (metadata == null) {			
			metadata = getEntityMetadataFromDB(code);
		}				
		
		if (metadata == null) {
			throw new BusinessException("cannot found entity metadata by " + code.getCode());
		}
		if (metadata.getFieldList() == null || metadata.getFieldList().size() == 0) {
			logger.warn("cannot found entity fields metadata by " + code.getCode());
		}		
		
		//缓存元数据
		entitymetadataMap.put(code.getCode(), metadata);
		return metadata;
	}

	protected EntityMetadata getEntityMetadataFromDB(IEntityCode code) throws BusinessException{
		EntityMetadata metadata = null;
		Map<String,Object> entity = metadataCRUDService.getEntityByCode(code);
		
		if (MapUtils.isEmpty(entity)) {
			entity = metadataCRUDService.getEntityByAliasCode(code);
			entity.put("code", MapUtils.getString(entity, "aliasCode"));
		}
		
		if (MapUtils.isNotEmpty(entity)) {
			metadata = new EntityMetadata(entity);

			Map<String,Object> params=new HashMap<String,Object>();
			
			String entityId = MapUtils.getString(entity, MetadataConst.ENTITY_METADATA_ID);
			params.put(FieldCodeOfField.entityId.toString(), entityId);
			
			List<Map<String,Object>> fieldList=metadataCRUDService.listField(params);

			for(Map<String,Object> field:fieldList){
				FieldMetadata fm=new FieldMetadata(field);
				metadata.addField(fm);
			}

			return metadata;
		} 
		
		return null;
	}
	
	public List<Map> loadColumsFromDbMetadata(String entityCode) throws BusinessException {
		List<Map> mapList = new ArrayList();		
		try {
			List<TableParameterMetaData> list = getTableColumnsFromDB(entityCode);
			for (TableParameterMetaData m : list) {
				String columnName = m.getParameterName();
				Map map = new HashMap();				
				map.put(FieldCodeOfField.columnName.toString(), columnName);
				map.put(FieldCodeOfField.code.toString(), getFieldCode(columnName));
				map.put(FieldCodeOfField.dataType.toString(), TpltUtils.getSystemDataType(m.getSqlType()).toString());
				mapList.add(map);
			}			
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
		
		return mapList;
	}
	
	private String getFieldCode(String columnName) {
		try {
			String s = columnName.toLowerCase();
			int index = s.indexOf("_");
			String s1, s2;
			for (int i = 0; index > 0; i++) {
				if (i == 10) {					
					throw new Exception();
				}
				if (s.length() > index + 1) {					
					s1 = s.substring(index + 1, index + 2);
					s1 = s1.toUpperCase();
					s2 = s.substring(index, index + 2);
				} else {
					s1 = "";
					s2 = "_";
				}
				s = s.replace(s2, s1);
				index = s.indexOf("_");
			}
			return s;				
		} catch (Exception e) {
			// TODO: handle exception
		}
		return columnName;
	}
	
	private List<TableParameterMetaData> getTableColumnsFromDB(String tableName) throws SQLException {
		TableMetaDataContext context = new TableMetaDataContext();
		context.setAccessTableColumnMetaData(true);
		context.setTableName(tableName);
		TableMetaDataProvider metaDataProvider = TableMetaDataProviderFactory.createMetaDataProvider(dao.getDataSource(), context);
		List<TableParameterMetaData> list = metaDataProvider.getTableParameterMetaData();
		
		return list;
	}
}


