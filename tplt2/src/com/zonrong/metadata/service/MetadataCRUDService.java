package com.zonrong.metadata.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.DeleteParam;
import com.zonrong.core.dao.InsertParam;
import com.zonrong.core.dao.Page;
import com.zonrong.core.dao.QueryParam;
import com.zonrong.core.dao.UpdateParam;
import com.zonrong.core.dao.dialect.SqlPlaceHolder;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.MetadataConst;
import com.zonrong.metadata.XmlMetadataProvider;
import com.zonrong.metadata.MetadataConst.FieldCodeOfEntity;
import com.zonrong.metadata.MetadataConst.FieldCodeOfField;

/**
 * date: 2010-7-20
 *
 * version: 1.0
 * commonts: ......
 */
@Service("metadataCRUDService")
public class MetadataCRUDService {
	private static Logger logger = Logger.getLogger(MetadataCRUDService.class);
	@Resource
	private Dao dao;
	@Resource
	private XmlMetadataProvider xmlMetadataProvider;

	public boolean isEntityExists(IEntityCode entityCode) {
		String tableName = xmlMetadataProvider.getTableName(TpltEnumEntityCode.ENTITY);
		QueryParam param = new QueryParam();
		param.setTableName(tableName);
		param.setFilter(Filter.field(FieldCodeOfEntity.code.toString()).eq(entityCode));
		int count = dao.count(param);

		//exists in DB
		if (count > 0)
			return true;

		//exists in XML config
		if (xmlMetadataProvider.getMetadataByEntityCode(entityCode) != null)
			return true;

		return false;
	}

	/**
	 * Create entity metadata
	 *
	 * @param parameter {entityField1:value1,entityField2:value2,entityField3:value3, ...}
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public Object createEntity(Map parameter, IUser user) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.ENTITY);
		String codeStr = MapUtils.getString(parameter, FieldCodeOfEntity.code.toString());
		EntityCode code = new EntityCode(codeStr);
		if (isEntityExists(code)) {
			throw new BusinessException("Entity:" + code + " is exists");
		}

		Map<String, Object> map = metadata.getDbColumnMapForUpdate(parameter, user);
		InsertParam param = new InsertParam();
		param.setTableName(metadata.getTableName());
		for(Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
			String colName = it.next();
			param.addColumnValue(colName, MapUtils.getObject(map, colName),0);
		}

		return dao.insert(param);
	}



	/**
	 *
	 *
	 * @param id
	 * @param parameter {code:value1,name:value2,tableName:value3, ...}
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public int updateEntityById(int id,Map parameter, IUser user) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.ENTITY);

		String newCodeStr = MapUtils.getString(parameter, FieldCodeOfEntity.code.toString());
		EntityCode newCode = new EntityCode(newCodeStr);
		Map entity = getEntityByCode(newCode);
		if(entity!=null){
			Integer entityId = MapUtils.getInteger(entity, MetadataConst.ENTITY_METADATA_ID);
			if(id != entityId) {
				throw new BusinessException("Code:" + newCode + " is exists");
			}
		}

		UpdateParam param=new UpdateParam();
		param.setTableName(metadata.getTableName());

		parameter.remove(metadata.getPkCode());
		Map<String,Object> map=metadata.getDbColumnMapForUpdate(parameter, user);
		for(Iterator<String> it=map.keySet().iterator();it.hasNext();){
			String colName=it.next();
			param.addColumnValue(colName,MapUtils.getObject(map, colName));
		}

		param.setFilter(Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(id));

		return dao.update(param);
	}


	/**
	 *
	 * @param code
	 * @return {id:value,code:value1,name:value2,tableName:value3, ...}
	 */
	public Map<String,Object> getEntityByCode(IEntityCode code) {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.ENTITY);

		SqlPlaceHolder holder = new SqlPlaceHolder("code");
		Filter f = Filter.field(metadata.getColumnName(FieldCodeOfEntity.code.toString())).eq(holder);
		f.setValue(holder.getName(), code.getCode());

		QueryParam param = new QueryParam();
		param.setTableName(metadata.getTableName());
		param.setFilter(f);
		param.addAllColumn(metadata);

		return dao.get(param);
	}

	/**
	 *
	 * @param code
	 * @return {id:value,code:value1,name:value2,tableName:value3, ...}
	 */
	public Map<String,Object> getEntityByAliasCode(IEntityCode code) {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.ENTITY);

		SqlPlaceHolder holder = new SqlPlaceHolder("code");
		Filter f = Filter.field(metadata.getColumnName(FieldCodeOfEntity.aliasCode.toString())).like(holder);
		f.setValue(holder.getName(), "%" + code.getCode() + "%");

		QueryParam param = new QueryParam();
		param.setTableName(metadata.getTableName());
		param.setFilter(f);
		param.addAllColumn(metadata);

		return dao.get(param);
	}

	/**
	 *
	 * @param parameter {code:value1,name:value2,tableName:value3, ...}
	 *
	 * @return [{code:value1,name:value2,tableName:value3, ...}, ...]
	 * @throws BusinessException
	 */
	public List<Map<String,Object>> listEntity(Map parameter) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.ENTITY);
		QueryParam param = getQueryParam(metadata, parameter);

		return dao.list(param);
	}

	public Page pageEntity(Map parameter, final int start, final int limit) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.ENTITY);
		QueryParam param = getQueryParam(metadata, parameter);

		return dao.page(param, start, limit);
	}

	private QueryParam getQueryParam(EntityMetadata metadata, Map<String, Object> parameter) {
		QueryParam param=new QueryParam();
		param.setTableName(metadata.getTableName());

		Map<String,Object> map = metadata.getDbColumnMapForQuery(parameter);
		Filter filter = null;
		if (map != null) {
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()) {
				String colName = (String)it.next();

				SqlPlaceHolder holder = new SqlPlaceHolder(colName);
				if (filter == null) {
					filter = Filter.field(colName).eq(holder);
				} else {
					filter.and(Filter.field(colName).eq(holder));
				}
				filter.setValue(holder.getName(), MapUtils.getObject(map, colName));
			}
		}
		param.setFilter(filter);
		param.addAllColumn(metadata);

		return param;
	}

	public Object createField(Map parameter, IUser user) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.FIELD);

		Map<String, Object> map = metadata.getDbColumnMapForUpdate(parameter, user);
		InsertParam param = new InsertParam();
		param.setTableName(metadata.getTableName());
		for(Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
			String colName = it.next();
			param.addColumnValue(colName, MapUtils.getObject(map, colName),0);
		}

		return dao.insert(param);
	}

	public int updateFieldById(Integer id, Map parameter, IUser user) throws BusinessException {
		if (id == null) {
			throw new BusinessException("Required id for update");
		}
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.FIELD);

		UpdateParam param = new UpdateParam();
		param.setTableName(metadata.getTableName());

		parameter.remove(metadata.getPkCode());
		Map<String,Object> map = metadata.getDbColumnMapForUpdate(parameter, user);
		for(Iterator<String> it = map.keySet().iterator(); it.hasNext(); ){
			String colName = it.next();
			param.addColumnValue(colName, MapUtils.getObject(map, colName));
		}

		param.setFilter(Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(id));

		return dao.update(param);
	}

	public List<Map<String, Object>> listField(Map parameter) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.FIELD);
		QueryParam param = getQueryParam(metadata, parameter);
		param.orderBy(FieldCodeOfField.orderBy.toString());
		return dao.list(param);
	}

	public Page pageField(Map parameter, final int start, final int limit) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.FIELD);
		QueryParam param = getQueryParam(metadata, parameter);
		return dao.page(param, start, limit);
	}

	public Map<String, Object> getEntityById(int id) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.ENTITY);

		SqlPlaceHolder holder = new SqlPlaceHolder("P");
		Filter f = Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(holder);
		f.setValue(holder.getName(), id);

		QueryParam param = new QueryParam();
		param.setTableName(metadata.getTableName());
		param.setFilter(f);
        param.addAllColumn(metadata);

		return dao.get(param);
	}

	public Map<String, Object> getFieldById(int id) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.FIELD);
		QueryParam param = new QueryParam();
		param.setTableName(metadata.getTableName());
		param.setFilter(Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(id));

		return dao.get(param);
	}

	public int deleteFieldById(int id) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.FIELD);
		DeleteParam param = new DeleteParam();
		param.setTableName(metadata.getTableName());
		param.setFilter(Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(id));

		return dao.delete(param);
	}

	public int deleteFieldByEntityId(int entityId) throws BusinessException {
		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.FIELD);
		DeleteParam param = new DeleteParam();
		param.setTableName(metadata.getTableName());
		param.setFilter(Filter.field(metadata.getColumnName(FieldCodeOfField.entityId.toString())).eq(entityId));

		return dao.delete(param);
	}

	public int deleteEntity(int id) throws BusinessException {
		deleteFieldByEntityId(id);

		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.ENTITY);
		DeleteParam param = new DeleteParam();
		param.setTableName(metadata.getTableName());
		param.setFilter(Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(id));

		return dao.delete(param);
	}

	public void move(int id, int step, IUser user) throws BusinessException{
		if (step == 0) return;

		EntityMetadata metadata = xmlMetadataProvider.getMetadataByEntityCode(TpltEnumEntityCode.FIELD);

		Filter filter = Filter.field(FieldCodeOfField.id.toString()).eq(id);
		QueryParam qp = new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.setFilter(filter);
		qp.addColumn(metadata.getColumnName(metadata.getPkCode()), metadata.getPkCode());
		qp.addColumn(metadata.getColumnName(FieldCodeOfField.entityId.toString()), FieldCodeOfField.entityId.toString());
		Map<String, Object> curr = dao.get(qp);

		Integer entityId = MapUtils.getInteger(curr, FieldCodeOfField.entityId.toString());
		if (entityId == null) {
			return;
		}

		filter = Filter.field(metadata.getColumnName(FieldCodeOfField.entityId.toString())).eq(entityId);
		qp = new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.setFilter(filter);
		qp.addColumn(metadata.getColumnName(metadata.getPkCode()), metadata.getPkCode());
		qp.orderBy(metadata.getColumnName(FieldCodeOfField.orderBy.toString()));
		List<Map<String, Object>> dbFieldList = dao.list(qp);

		curr = null;
		Integer currOrderBy = null;
		for (int i = 0; i < dbFieldList.size(); i++) {
			Map<String, Object> dbField = dbFieldList.get(i);
			Integer dbId = MapUtils.getInteger(dbField, metadata.getPkCode());
			if (dbId != null && dbId.intValue() == id) {
				curr = dbField;
				currOrderBy = i;
				break;
			}
		}

		if (curr == null) {
			throw new BusinessException("未找到当前节点");
		}

		currOrderBy = currOrderBy + step;
		if (currOrderBy >= dbFieldList.size() || currOrderBy < 0) {
			return;
		}

		dbFieldList.remove(curr);
		dbFieldList.add(currOrderBy, curr);

		UpdateParam up = new UpdateParam();
		up.setTableName(metadata.getTableName());
		Filter f=Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(new SqlPlaceHolder("id"));

		for (int i = 0; i < dbFieldList.size(); i++) {
			Map<String, Object> dbField = dbFieldList.get(i);
			Integer dbId = MapUtils.getInteger(dbField, metadata.getPkCode());
			up.addColumnValue(metadata.getColumnName(FieldCodeOfField.orderBy.toString()), i, i);
			f.setValue("id", dbId);
		}
		up.setFilter(f);
		dao.batchUpdate(up);
	}
}


