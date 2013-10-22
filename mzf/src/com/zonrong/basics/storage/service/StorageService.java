package com.zonrong.basics.storage.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.QueryParam;
import com.zonrong.core.dao.dialect.SqlPlaceHolder;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.entity.TreeConfig;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-11-3
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class StorageService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;	
	@Resource
	private Dao dao;
	
	/**
	 * 组织机构类型
	 */
//	public enum OrgType {
//		HQ,			//总部
//		shop		//门店
//	}
	//@PostConstruct
	public void initStorage() throws BusinessException {
//		EntityMetadata storageMetadata = metadataProvider.getEntityMetadata(EntityCodeManager.STORAGE);
//		List<Map<String,Object>> storageList = entityService.list(storageMetadata, new HashMap());
//		if (storageList.size() > 0) return; 
//		
//		EntityMetadata orgMetadata = metadataProvider.getEntityMetadata(EntityCodeManager.ORG);	
//		QueryParam qp = new QueryParam();
//		qp.setTableName(orgMetadata.getTableName());
//		qp.addAllColumn(orgMetadata);
//		qp.orderBy(orgMetadata.getPkCode()).asc();
//		List<Map<String, Object>> orgList = dao.list(qp);		
//		List<Map<String, Object>> orgAndStorageList = new ArrayList();
//		for (Map map : orgList) {
//			Integer id = MapUtils.getInteger(map, orgMetadata.getPkCode());
//			String type = MapUtils.getString(map, "type");
//			if (OrgType.HQ.toString().equalsIgnoreCase(type)) {
//				orgAndStorageList.addAll(new HQStorageList(id));
//			}
//			if (OrgType.shop.toString().equalsIgnoreCase(type)){
//				orgAndStorageList.addAll(new ShopStoreageList(id));
//			}
//		}
//		
//		if (storageList.size() == 0) {
//			logger.debug("初始化仓库实体...");
//			entityService.batchCreate(storageMetadata, orgAndStorageList, null);
//			logger.debug("初始化仓库实体完成");
//		}
		
	}

	public List<Map<String, Object>> list(TreeConfig config, String kind) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.ORG_STORAGE);
		if (metadata == null) {
			throw new BusinessException("EntityMetadata cannot be null or empty");
		}	
		
		String tableName = metadata.getTableName();
		
		final String pidCode = config.getPidCode();
		final String indexCode = config.getIndexCode();
		
		QueryParam qp = new QueryParam();
		qp.setTableName(tableName);
		qp.addAllColumn(metadata);
		
		List<String> tmp=new ArrayList<String>();
		if(StringUtils.isNotBlank(pidCode)){
			tmp.add(pidCode);
		}
		
		if(StringUtils.isNotBlank(indexCode)){
			tmp.add(indexCode);
		}
		
		if(tmp.size()>0){
			qp.orderBy(tmp.toArray(new String[]{}));
		}	
		
		List<Map<String, Object>> where = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(EntityService.FIELD_CODE_KEY, "storekind");
		map.put(EntityService.OPERATOR_KEY, Filter.IN);
		map.put(EntityService.VALUE_KEY, new String[]{"org", kind});
		where.add(map);
		
		Filter filter = filter = Filter.field("storekind").eq("org").or(Filter.field("storekind").eq(new SqlPlaceHolder("storekind")));
		filter.setValue("storekind", kind);
		qp.setFilter(filter);
		return dao.list(qp);
	} 
}


