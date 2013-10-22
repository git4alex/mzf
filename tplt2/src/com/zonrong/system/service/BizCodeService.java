package com.zonrong.system.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.QueryParam;
import com.zonrong.core.dao.UpdateParam;
import com.zonrong.core.dao.dialect.SqlPlaceHolder;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.core.util.TreeProcess;
import com.zonrong.core.util.TreeProcessHelper;
import com.zonrong.entity.TreeConfig;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.entity.service.TreeEntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-7-27
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class BizCodeService {
	private static Logger logger = Logger.getLogger(BizCodeService.class);
	
	private static Map<String, List<Map>> bizTypeMap = new HashMap<String, List<Map>>();
	private static Map<String, Map> bizCodeMap = new HashMap<String, Map>();

	private String code_key = "code";
	private String typeCode_key = "typeCode";
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private TreeEntityService treeEntityService;
	@Resource
	private EntityService entityService;
	@Resource
	private Dao dao;

	@PostConstruct
	public void load() {
		try {
			EntityMetadata bizTypeMetadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.BIZ_TYPE);
			EntityMetadata bizCodeMetadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.BIZ_CODE);
			
			QueryParam qp=new QueryParam();
			qp.setTableName(bizTypeMetadata.getTableName());
			qp.addAllColumn(bizTypeMetadata);
			qp.orderBy("id").asc();
			
			List<Map<String,Object>> typeList=dao.list(qp);
			
			qp=new QueryParam();
			qp.setTableName(bizCodeMetadata.getTableName());
			qp.addAllColumn(bizCodeMetadata);
			qp.orderBy("order_by").asc();
			
			
			List<Map<String,Object>> codeList=dao.list(qp);
			
			bizTypeMap.clear();
			bizCodeMap.clear();
			
			for (Map type : typeList) {
				String code = MapUtils.getString(type, code_key);
				for (Map map : codeList) {
					List<Map> bizCodeList = (List) bizTypeMap.get(code);
					if (null == bizCodeList) {
						bizCodeList = new ArrayList();
						bizTypeMap.put(code, bizCodeList);
					}
					String typeCode = MapUtils.getString(map, typeCode_key);
					if (StringUtils.isNotEmpty(typeCode) && typeCode.equals(code)) {						
						bizCodeList.add(map);
						
						bizCodeMap.put(MapUtils.getString(map, bizCodeMetadata.getPkCode()), map);
						
//						if (logger.isDebugEnabled()) {
//							logger.debug(map);
//						}
					}
				}
			}
			
			Iterator<Entry<String, List<Map>>> it = bizTypeMap.entrySet().iterator();
			while (it.hasNext()) {
				List<Map> list = it.next().getValue();
				List<Map> treeList = TreeProcess.buildTree(list, -1, new TreeProcessHelper("id", "pid"));
				list = treeList;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}
	
	public static Map getBizCodeById(int id) {		
		return (Map)bizCodeMap.get(Integer.toString(id));
	}	
	
	public static Map<String, List<Map>> getBizTypeMap() {
		return bizTypeMap;
	}
	
	public static String[] getBizVslues(String code) {
		List<Map> list = bizTypeMap.get(code);
		if (CollectionUtils.isEmpty(list)) 
			return null;

		List<String> res = new ArrayList<String>();
		for (Map map : list) {
			String dbValue = MapUtils.getString(map, "value");
			res.add(dbValue);
		}
		return res.toArray(new String[]{});
	}
	
	public static String getBizName(String code, String value) {		
		List<Map> list = bizTypeMap.get(code);
		if (CollectionUtils.isEmpty(list)) 
			return null;

		List<String> res = new ArrayList<String>();
		for (Map map : list) {
			String dbValue = MapUtils.getString(map, "value");
			if (dbValue != null && dbValue.equals(value)) {
				return MapUtils.getString(map, "text");
			}
		}
		return null;
	}	
	
	public void move(int id, int step, String typeCode, int pid, IUser user) throws BusinessException{
		if (step == 0) return;
		
		Map<String, Object> treeConfig = new HashMap<String, Object>();
		treeConfig.put(TreeConfig.PID_CODE, "pid");
		treeConfig.put(TreeConfig.INDEX_CODE, "orderBy");
		TreeConfig config = TreeConfig.getTreeConfig(treeConfig);
		
		EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.BIZ_CODE);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("typeCode", typeCode);
		where.put(config.getPidCode(), pid);
		List<Map<String, Object>> list = treeEntityService.list(metadata, config, where, User.getSystemUser());
		
		Map<String, Object> curr = null;
		Integer currOrderBy = null;
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> dbBizCode = list.get(i);
			Integer dbId = MapUtils.getInteger(dbBizCode, metadata.getPkCode());
			if (dbId != null && dbId.intValue() == id) {
				curr = dbBizCode;
				currOrderBy = i;
				break;
			}			
		}
		
		if (curr == null) {
			throw new BusinessException("未找到当前节点");
		}
		
		currOrderBy = currOrderBy + step;
		if (currOrderBy >= list.size() || currOrderBy < 0) {
			return;
		}
		
		list.remove(curr);
		list.add(currOrderBy, curr);
		
		UpdateParam up=new UpdateParam();
		up.setTableName(metadata.getTableName());
		Filter f=Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(new SqlPlaceHolder("id"));
		
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> dbBizCode = list.get(i);
			Integer dbId = MapUtils.getInteger(dbBizCode, metadata.getPkCode());
			up.addColumnValue(metadata.getColumnName(config.getIndexCode()), i, i);
			f.setValue("id", dbId);			
		}
		up.setFilter(f);
		dao.batchUpdate(up);
	}
	
	public void updateBizType(int bizTypeId, Map<String, Object> bizType, IUser user) throws BusinessException {
		EntityMetadata bizTypeMetadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.BIZ_TYPE);
		EntityMetadata bizCodeMetadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.BIZ_CODE);
		
		String code = MapUtils.getString(bizType, "code");
		if (StringUtils.isEmpty(code)) {
			throw new BusinessException("请填写编码值");
		}
		
		Map<String, Object> dbBizType = entityService.getById(bizTypeMetadata, bizTypeId, user.asSystem());
		String dbCode = MapUtils.getString(dbBizType, "code");
		
		if (!code.equals(dbCode)) {			
			Map<String, Object> field = new HashMap<String, Object>();
			field.put("typeCode", code);
			Map<String, Object> where = new HashMap<String, Object>();
			where.put("typeCode", dbCode);
			entityService.update(bizCodeMetadata, field, where, user);
		}
		
		entityService.updateById(bizTypeMetadata, Integer.toString(bizTypeId), bizType, user);
	}
}


