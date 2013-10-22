package com.zonrong.entity.service;

import com.zonrong.core.dao.*;
import com.zonrong.core.dao.dialect.SqlPlaceHolder;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.util.UploadFileUtils.UploadFileFolder;
import com.zonrong.entity.acl.AclException;
import com.zonrong.entity.acl.AclService;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.FieldMetadata;
import com.zonrong.metadata.MetadataConst.DataType;
import com.zonrong.metadata.service.MetadataProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;

/**
 * date: 2010-7-20
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class EntityService {
	private static Logger logger = Logger.getLogger(EntityService.class);
	public static final String FIELD_CODE_KEY = "field";
	public static final String OPERATOR_KEY = "operate";
	public static final String VALUE_KEY = "value";

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private AclService aclService;
	@Resource
	private Dao dao;

	public void setDao(Dao dao) {
		this.dao = dao;
	}

	/**
	 *
	 *
	 * @param code
	 * @param field {entityField:value, ... }
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public String create(IEntityCode code, Map<String, Object> field, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return create(metadata, field, user);
	}

	public String create(EntityMetadata metadata, Map<String, Object> field, IUser user) throws BusinessException {
		if (metadata == null || field == null || field.size() == 0) {
			throw new BusinessException("EntityMetadata or Map cannot be null or empty");
		}

		List<FieldMetadata> fieldList = metadata.getFieldList();
		for (FieldMetadata fm : fieldList) {
			if (DataType.DATATYPE_IMAGE.toString().equalsIgnoreCase(fm.getDataType())) {
				String id = MapUtils.getString(field, fm.getCode());
				createImage(id, user);
			}
		}

		InsertParam ip=new InsertParam();
		String tableName = metadata.getTableName();
		ip.setTableName(tableName);
		Map<String, Object> map = metadata.getDbColumnMapForUpdate(field, user);
		for(Iterator<String> it=map.keySet().iterator();it.hasNext();){
			String colName=it.next();
			ip.addColumnValue(colName, map.get(colName),0);
		}

		return ObjectUtils.toString(dao.insert(ip));
	}

	public void createImage(String id, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.UPLOAD);

		if(StringUtils.isNotEmpty(id)) {
			Map imgRow = getById(metadata,id,user);
			if(imgRow != null){
				return;
			}

			StringBuffer fullPath=null;
			try {
				File file = new File(this.getClass().getClassLoader().getResource("").toURI().getPath());

				file = file.getParentFile().getParentFile();
				fullPath = new StringBuffer(file.getPath());
				fullPath.append("/").append(UploadFileFolder.UPLOAD_FOLDER).append("/");
				String[] folder = id.split("-");
				for (int i = 0; i < folder.length - 1; i++) {
					fullPath.append(folder[i]).append("/");
				}
				fullPath.append(id.replaceAll("[|]", "."));

				file = new File(fullPath.toString());
				Map<String, Object> parameter = new HashMap<String, Object>();
				parameter.put(metadata.getPkCode(), id);
				parameter.put("content", file);
				parameter.put("cdate", null);
				Map<String, Object> map = metadata.getDbColumnMapForUpdate(parameter, user);

				InsertParam ip=new InsertParam();
				String tableName = metadata.getTableName();
				ip.setTableName(tableName);
				for(Iterator<String> it=map.keySet().iterator();it.hasNext();){
					String colName=it.next();
					ip.addColumnValue(colName, map.get(colName),0);
				}

				dao.insert(ip);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				throw new BusinessException("File path error:"+fullPath.toString());
			} catch (DuplicateKeyException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void batchCreate(IEntityCode code, List<Map<String,Object>> fields, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		batchCreate(metadata, fields, user);
	}

	public void batchCreate(EntityMetadata metadata, List<Map<String,Object>> fields, IUser user) throws BusinessException {
		if (metadata == null || CollectionUtils.isEmpty(fields)) {
			throw new BusinessException("EntityMetadata or List<Map<String,Object>> cannot be null or empty");
		}

		List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
		for (Map<String,Object> parameter : fields) {
			Map<String, Object> map = metadata.getDbColumnMapForUpdate(parameter, user);
			newList.add(map);
		}

		InsertParam ip=new InsertParam();
		String tableName = metadata.getTableName();
		ip.setTableName(tableName);
		for(int i=0;i<newList.size();i++){
			Map<String,Object> map=newList.get(i);
			for(Iterator<String> it=map.keySet().iterator();it.hasNext();){
				String colName=it.next();
				ip.addColumnValue(colName, map.get(colName),i);
			}
		}

		dao.batchUpdate(ip);
	}

	public int update(IEntityCode code, Map<String,Object> field, Map<String,Object> where, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return update(metadata, field, where, user);
	}

	public int update(EntityMetadata metadata, Map<String,Object> field, Map<String,Object> where, IUser user) throws BusinessException {
		if (metadata == null || field == null || field.size() == 0) {
			throw new BusinessException("EntityMetadata or Map cannot be null or empty");
		}

		if (where == null || where.size() == 0) {
			logger.warn("no filter on update");
		}

		List<FieldMetadata> fieldList = metadata.getFieldList();
		for (FieldMetadata fieldMetadata : fieldList) {
			if (DataType.DATATYPE_IMAGE.toString().equalsIgnoreCase(fieldMetadata.getDataType())) {
				String id = MapUtils.getString(field, fieldMetadata.getCode());
				createImage(id, user);
			}
		}

		Filter filter = null;
		Map<String,Object> where1 = metadata.getDbColumnMapForQuery(where);

		Map<String,Object> filterValueMap = new HashMap<String, Object>();
		String hk = "_p";
		int i=0;

		Iterator<String> it = where1.keySet().iterator();
		while(it.hasNext()) {
			String key = (String)it.next();
			Object value = MapUtils.getObject(where1, key);
			Filter ft = null;
			if (value != null && value.getClass().isArray()) {
				ft = Filter.field(key).in(new SqlPlaceHolder(hk+i));
			} else {
				ft = Filter.field(key).eq(new SqlPlaceHolder(hk+i));
			}

			if (filter == null) {
				filter = ft;
			} else {
				filter.and(ft);
			}

			filterValueMap.put(hk+i, value);
			i++;
		}
		Map<String,Object> field1 = metadata.getDbColumnMapForUpdate(field, user);

		UpdateParam up=new UpdateParam();
		String tableName = metadata.getTableName();
		up.setTableName(tableName);

		for(it=field1.keySet().iterator();it.hasNext();){
			String colName=it.next();
			up.addColumnValue(colName, field1.get(colName));
		}

		for(it=filterValueMap.keySet().iterator();it.hasNext();){
			String p=it.next();
			filter.setValue(p, filterValueMap.get(p));
		}

		up.setFilter(filter);


		return dao.update(up);
	}

	public int updateById(IEntityCode code, String id, Map<String,Object> field, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return updateById(metadata, id, field, user);
	}

	public int updateById(EntityMetadata metadata, String id, Map<String,Object> field, IUser user) throws BusinessException {
		Map<String,Object> where = new HashMap<String,Object>();
        if (metadata.getPkCode() == null){
            throw new BusinessException("entity pk code is null.");
        }
		where.put(metadata.getPkCode(), id);
		field.remove(metadata.getPkCode());
		return update(metadata, field, where, user);
	}

	/**
	 * create or update by pk
	 *
	 * @param metadata
	 * @param params
	 * @param user
	 * @throws BusinessException
	 */
	public void batchSave(EntityMetadata metadata, List<Map<String,Object>> params, IUser user) throws BusinessException {
		if (metadata == null || params == null) {
			throw new BusinessException("EntityMetadata or Map cannot be null or empty");
		}

		List<Map<String,Object>> newList = new ArrayList();
		Map<String, Map> updateMap = new HashMap();
		for (Map map : params) {
			String id = MapUtils.getString(map, metadata.getPkCode());
			if (id == null) {
				newList.add(map);
			} else {
				map.remove(metadata.getPkCode());
				updateMap.put(id, map);
			}
		}

		batchCreate(metadata, newList, user);

		Iterator<Entry<String, Map>> it = updateMap.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, Map> entry = it.next();
			updateById(metadata, entry.getKey(), entry.getValue(), user);
		}
	}

//	public int[] batchUpdate(EntityMetadata metadata, List<Map<String,Object>> datas, List<Map<String,Object>> where,IUser user){
//		if (metadata == null || datas == null || datas.size() == 0) {
//			throw new BusinessException("EntityMetadata or Map cannot be null or empty");
//		}
//
//		if (where == null || where.size() == 0) {
//			logger.warn("no filter on update");
//		}
//
//		List<FieldMetadata> fieldList = metadata.getFieldList();
//		for (FieldMetadata fieldMetadata : fieldList) {
//			if (MetadataConst.DATATYPE_IMAGE.equalsIgnoreCase(fieldMetadata.getDataType())) {
//				String id = MapUtils.getString(field, fieldMetadata.getCode());
//				createImage(id, user);
//			}
//		}
//
//		Filter filter = null;
//		Map<String,Object> where1 = metadata.getDbColumnMapForQuery(where.);
//
//		Map<String,Object> filterValueMap = new HashMap<String, Object>();
//		String hk = "_p";
//		int i=0;
//
//		Iterator<String> it = where1.keySet().iterator();
//		while(it.hasNext()) {
//			String key = (String)it.next();
//			Object value = MapUtils.getObject(where1, key);
//			Filter ft = null;
//			if (value != null && value.getClass().isArray()) {
//				ft = Filter.field(key).in(new SqlPlaceHolder(hk+i));
//			} else {
//				ft = Filter.field(key).eq(new SqlPlaceHolder(hk+i));
//			}
//
//			if (filter == null) {
//				filter = ft;
//			} else {
//				filter.and(ft);
//			}
//
//			filterValueMap.put(hk+i, value);
//			i++;
//		}
//		Map<String,Object> field1 = metadata.getDbColumnMapForUpdate(field, user);
//
//		UpdateParam up=new UpdateParam();
//		String tableName = metadata.getTableName();
//		up.setTableName(tableName);
//
//		for(it=field1.keySet().iterator();it.hasNext();){
//			String colName=it.next();
//			up.addColumnValue(colName, field1.get(colName));
//		}
//
//		for(it=filterValueMap.keySet().iterator();it.hasNext();){
//			String p=it.next();
//			filter.setValue(p, filterValueMap.get(p));
//		}
//
//		up.setFilter(filter);
//
//
//		return dao.batchUpdate(up);
//	}

	public int deleteById(IEntityCode code, String id, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return deleteById(metadata, id, user);
	}

	public int deleteById(EntityMetadata metadata, String id, IUser user) throws BusinessException {
		if (StringUtils.isEmpty(metadata.getPkCode())) {
			throw new BusinessException("primary key undefined");
		}
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), id);

		return delete(metadata, where, user);
	}

	public int delete(IEntityCode code, Map<String, Object> where, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return delete(metadata, where, user);
	}

	public int delete(EntityMetadata metadata, Map<String, Object> where, IUser user) throws BusinessException {
		if (metadata == null) {
			throw new BusinessException("EntityMetadata cannot be null or empty");
		}
		if (where == null || where.size() == 0) {
			logger.warn("no filter on delete");
		}

		Filter filter = null;
		Map<String, Object> map = metadata.getDbColumnMapForQuery(where);

		Map<String,Object> filterValueMap=new HashMap<String, Object>();
		String hk = "_p";
		int i=0;

		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()) {
			String key = (String)it.next();
			Object value = MapUtils.getObject(map, key);
			Filter ft = null;
			if (value != null && value.getClass().isArray()) {
				ft = Filter.field(key).in(new SqlPlaceHolder(hk+i));
			} else {
				ft = Filter.field(key).eq(new SqlPlaceHolder(hk+i));
			}

			if (filter == null) {
				filter = ft;
			} else {
				filter.and(ft);
			}

			filterValueMap.put(hk+i, value);
			i++;
		}

		DeleteParam dp=new DeleteParam();
		dp.setTableName(metadata.getTableName());

		for(it=filterValueMap.keySet().iterator();it.hasNext();){
			String p=it.next();
			filter.setValue(p, filterValueMap.get(p));
		}

		dp.setFilter(filter);

		try {
			return dao.delete(dp);
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw new BusinessException("被其它对象引用，不能删除");
		}
	}

	public Map<String, Object> getById(IEntityCode code, Object id, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return getById(metadata, id, user);
	}

	public Map<String, Object> getById(EntityMetadata metadata, Object id, IUser user) throws BusinessException {
		if (metadata == null) {
			throw new BusinessException("EntityMetadata cannot be null or empty");
		}

		if (id == null || StringUtils.isEmpty(id.toString())) {
			throw new BusinessException("id is mull or empty");
		}
		String columnName = metadata.getColumnName(metadata.getPkCode());
		Filter filter = Filter.field(columnName).eq(new SqlPlaceHolder(columnName));
		String deletedField = metadata.getColumnName(metadata.getDeletedCode());
		if (StringUtils.isNotEmpty(deletedField)) {
			Filter f = Filter.field(deletedField).isNull().or(Filter.field(deletedField).eq(0));
			filter.and(f);
		}

		QueryParam qp=new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.addAllColumn(metadata);

		filter.setValue(columnName, id);
		qp.setFilter(filter);

		return dao.get(qp);
	}

	public List<Map<String,Object>> list(IEntityCode code, Map<String,Object> where,  OrderBy orderBy, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return list(metadata, where, orderBy, user);
	}

	public List<Map<String,Object>> list(EntityMetadata netadata, Map<String,Object> where, OrderBy orderBy, IUser user) throws BusinessException {
		if (netadata == null) {
			throw new BusinessException("EntityMetadata cannot be null or empty");
		}

		List<Map<String, Object>> where1 = factor(where);
		return list(netadata, where1, orderBy, user);
	}

	public List<Map<String,Object>> list(IEntityCode code, List<Map<String,Object>> where, OrderBy orderBy, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return list(metadata, where, orderBy, user);
	}

	public List<Map<String,Object>> list(EntityMetadata metadata, List<Map<String,Object>> where, OrderBy orderBy, IUser user) throws BusinessException {
		if (metadata == null) {
			throw new BusinessException("EntityMetadata cannot be null or empty");
		}
		Filter filter = createFilter(metadata, where);

		return list(metadata, filter, orderBy, user);
	}

	public List<Map<String,Object>> list(IEntityCode code, Filter filter,  OrderBy orderBy, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return list(metadata, filter, orderBy, user);
	}

	public List<Map<String,Object>> list(EntityMetadata metadata, Filter filter, OrderBy orderBy, IUser user) throws BusinessException {
		if (metadata == null) {
			throw new BusinessException("EntityMetadata cannot be null or empty");
		}

		QueryParam qp=new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.addAllColumn(metadata);
		if(filter !=null){
			qp.setFilter(filter);
		}

		if (orderBy != null && StringUtils.isNotBlank(orderBy.toString(metadata))) {
			qp.orderBy(orderBy.toString(metadata));
		} else {
			String id = metadata.getColumnName(metadata.getPkCode());
			if (StringUtils.isNotBlank(id)) {
				qp.orderBy(new String[]{id}).desc();
			}
		}

		try {
			String accessFilter = aclService.getAccessFilter(user, metadata.getCode());
			if(filter == null){
				filter = Filter.emptyFilter();
				qp.setFilter(filter);
			}

			filter.setExtendFilterStr(accessFilter);

		} catch (AclException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return dao.list(qp);
	}

	public Page page(IEntityCode code, Map<String, Object> where, final int offset, final int pageSize, OrderBy orderBy, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return page(metadata, where, offset, pageSize, orderBy, user);
	}

	public Page page(EntityMetadata metadata, Map<String, Object> where, int start, int limit, OrderBy orderBy, IUser user) throws BusinessException {
		if (metadata == null) {
			throw new BusinessException("EntityMetadata cannot be null or empty");
		}
		List<Map<String, Object>> where1 = factor(where);
		return page(metadata, where1, start, limit, orderBy, user);
	}

	public Page page(IEntityCode code, List<Map<String,Object>> where, final int offset, final int pageSize, OrderBy orderBy, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return page(metadata, where, offset, pageSize, orderBy, user);
	}

	public Page page(EntityMetadata metadata, List<Map<String,Object>> where, int start, int limit, OrderBy orderBy, IUser user) throws BusinessException {
		if (metadata == null) {
			throw new BusinessException("EntityMetadata cannot be null or empty");
		}
		Filter filter = createFilter(metadata, where);

		return page(metadata, filter, start, limit, orderBy, user);
	}

	public Page page(IEntityCode code, Filter filter, final int offset, final int pageSize, OrderBy orderBy, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		return page(metadata, filter, offset, pageSize, orderBy, user);
	}

	public Page page(EntityMetadata metadata, Filter filter, int start, int limit, OrderBy orderBy, IUser user) throws BusinessException {
		QueryParam qp=new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.addAllColumn(metadata);
		qp.setFilter(filter);

		if (orderBy != null && StringUtils.isNotBlank(orderBy.toString(metadata))) {
			qp.orderBy(orderBy.toString(metadata));
		} else {
//			String id = metadata.getColumnName(metadata.getPkCode());
			String id = metadata.getPkCode();
			if (StringUtils.isNotBlank(id)) {
				qp.orderBy(new String[]{id}).desc();
			}
		}

		try {
			String accessFilter = aclService.getAccessFilter(user, metadata.getCode());
			if(filter == null){
				filter = Filter.emptyFilter();
				qp.setFilter(filter);
			}

			filter.setExtendFilterStr(accessFilter);

		} catch (AclException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return dao.page(qp,start,limit);
	}

	public static Filter createFilter(EntityMetadata metadata, List<Map<String,Object>> where) throws BusinessException {
		if (where == null) return null;
		Filter filter = null;

		//去掉重复项
		Map<String, Map<String, Object>> tempWhere = new HashMap<String, Map<String,Object>>();
		for (Map<String,Object> filterMap : where) {
			tempWhere.put(filterMap.toString(), filterMap);
		}

		Map<String,Object> filterValueMap = new HashMap<String, Object>();
		String hk = "_p";
		int i=0;

		for (Map<String,Object> filterMap : tempWhere.values()) {
			String fieldCode = MapUtils.getString(filterMap, FIELD_CODE_KEY);
			String operate = MapUtils.getString(filterMap, OPERATOR_KEY);
			Object value = MapUtils.getObject(filterMap, VALUE_KEY);

			String fieldName = metadata.getColumnName(fieldCode);
			if (StringUtils.isEmpty(fieldName) || StringUtils.isEmpty(operate))
				continue;

			if (value != null && value.getClass().isEnum()) {
				 value = value.toString();
			}

			if (value != null && value.getClass().isArray()) {
				Object[] vs = (Object[]) value;
				List<String> v1 = new ArrayList<String>();
				for (Object v : vs) {
					v1.add(v.toString());
				}
				value = v1.toArray(new String[]{});
			}

			if (logger.isDebugEnabled()) {
				logger.debug("fieldCode: " + fieldCode);
				logger.debug("fieldName: " + fieldName);
				logger.debug("operate: " + operate);
				logger.debug("value: " + value);
			}

			operate = operate.trim();
			Filter ft = Filter.field(fieldName);
			if (Filter.EQ.equalsIgnoreCase(operate)) {
				ft.eq(new SqlPlaceHolder(hk+i));
				filterValueMap.put(hk+i, value);
				i++;
			} else if (Filter.GT.equalsIgnoreCase(operate)) {
				ft.gt(new SqlPlaceHolder(hk+i));
				filterValueMap.put(hk+i, value);
				i++;
			} else if (Filter.LT.equalsIgnoreCase(operate)) {
				ft.lt(new SqlPlaceHolder(hk+i));
				filterValueMap.put(hk+i, value);
				i++;
			} else if (Filter.GTEQ.equalsIgnoreCase(operate)) {
				ft.ge(new SqlPlaceHolder(hk+i));
				filterValueMap.put(hk+i, value);
				i++;
			} else if (Filter.LTEQ.equalsIgnoreCase(operate)) {
				ft.le(new SqlPlaceHolder(hk+i));
				filterValueMap.put(hk+i, value);
				i++;
			} else if (Filter.NOT_EQ1.equalsIgnoreCase(operate)
					|| Filter.NOT_EQ2.equalsIgnoreCase(operate)) {
				ft.ne(new SqlPlaceHolder(hk+i));
				filterValueMap.put(hk+i, value);
				i++;
			} else if (Filter.LIKE.equalsIgnoreCase(operate)) {
				ft.like(new SqlPlaceHolder(hk+i));
				filterValueMap.put(hk+i, value);
				i++;
			} else if (Filter.N_LIKE.equalsIgnoreCase(operate)) {
				if (value == null)
					continue;

				String[] values = StringUtils.split(value.toString(), ",");
				List<String> list = new ArrayList<String>();
				for (String v : values) {
					if (StringUtils.isNotBlank(v)) {
						list.add(v);
					}
				}

				if (list.size() >= 1) {
					ft.like(new SqlPlaceHolder(hk+i));
					filterValueMap.put(hk+i, "%" + list.get(0) + "%");
					i++;
				}

				for (int j = 1; j < list.size(); j++) {
					Filter f = Filter.field(fieldName).like(new SqlPlaceHolder(hk+i));
					filterValueMap.put(hk+i, "%" + list.get(j) + "%");
					i++;
					ft.and(f);
				}
			} else if (Filter.IN.equalsIgnoreCase(operate)) {
				ft.in(new SqlPlaceHolder(hk+i));
				filterValueMap.put(hk+i, value);
				i++;
			} else if (Filter.NOT_IN.equalsIgnoreCase(operate)) {
				ft.notIn(new SqlPlaceHolder(hk+i));
				filterValueMap.put(hk+i, value);
				i++;
			} else if (Filter.NULL.equalsIgnoreCase(operate)) {
				ft.isNull();
			} else if (Filter.NOT_NULL.equalsIgnoreCase(operate)) {
				ft.isNotNull();
			}

			if (ft != null) {
				if (filter == null) {
					filter = ft;
				} else {
					filter.and(ft);
				}
			}
		}

		String deletedField = metadata.getColumnName(metadata.getDeletedCode());
		if (StringUtils.isNotEmpty(deletedField)) {
			Filter f = Filter.field(deletedField).isNull().or(Filter.field(deletedField).eq(0));
			filter = filter != null? filter.and(f):f;
		}

		for(Iterator<String> it=filterValueMap.keySet().iterator();it.hasNext();){
			String p=it.next();
			filter.setValue(p, filterValueMap.get(p));
		}

		return filter;
	}

	protected List<Map<String, Object>> factor(Map<String, Object> where) throws BusinessException {
		if (where == null || where.size() == 0) {
			return null;
		}

		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

		for (Iterator<Entry<String, Object>> it = where.entrySet().iterator(); it.hasNext(); ) {
			Entry<String, Object> entry = it.next();
			String fieldName = entry.getKey();
			Object value = entry.getValue();

			Map<String,Object> map=new HashMap<String, Object>();
			map.put(FIELD_CODE_KEY, fieldName);
			if (value != null && value.getClass().isArray()) {
				map.put(OPERATOR_KEY, Filter.IN);
			}else{
				map.put(OPERATOR_KEY, Filter.EQ);
			}
			map.put(VALUE_KEY, value);
			list.add(map);
		}

		return list;
	}
}


