package com.zonrong.entity.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.DeleteParam;
import com.zonrong.core.dao.QueryParam;
import com.zonrong.core.dao.UpdateParam;
import com.zonrong.core.dao.dialect.DBFunction;
import com.zonrong.core.dao.dialect.SqlPlaceHolder;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.util.TreeBuilder;
import com.zonrong.entity.TreeConfig;
import com.zonrong.entity.acl.AclException;
import com.zonrong.entity.acl.AclService;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * project: metadataApp
 * date: 2010-7-5
 * author: wangliang
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class TreeEntityService{
	private static Logger logger = Logger.getLogger(TreeEntityService.class);
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private AclService aclService;	
	
	@Resource 
	private Dao dao;
	
	/**
	 * Create tree node
	 * 
	 * @param code entityCode
	 * @param treeConfig {pid:'parentId',index:'idx',path:'code'}
	 * @param node {text:'test',idx:'3',parentId:'10', ...}
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public String createNode(IEntityCode code, TreeConfig config,Map<String,Object> node,IUser user) throws BusinessException{
		if (code == null || config == null) {
			throw new BusinessException("String or Map cannot be null");
		}
		
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		String pidCode = config.getPidCode();
		String indexCode = config.getIndexCode();
		
		String pid=MapUtils.getString(node, pidCode);
		Integer index=null;
		if(StringUtils.isNotBlank(indexCode)){
			index=MapUtils.getInteger(node, indexCode);
			node.remove(indexCode);
		}
		node.remove(pidCode);
		
		String nodeId=entityService.create(metadata, node, user);
		
		appendChild(metadata,config,nodeId,pid,index);
		
		return nodeId;
	}
	
	private void removeChild(EntityMetadata metadata,TreeConfig config,String nodeId,String pid) throws BusinessException{
		String pidCode = config.getPidCode();
		String indexCode = config.getIndexCode();
		
		UpdateParam up=new UpdateParam();
		up.setTableName(metadata.getTableName());
		up.addColumnValue(metadata.getColumnName(pidCode),null);
		up.setFilter(Filter.field(metadata.getPkCode()).eq(nodeId));
		dao.update(up);
		
		if(StringUtils.isBlank(indexCode) || StringUtils.isBlank(pid)){
			return;	
		}
		
		//get target node
		QueryParam qp=new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.setFilter(Filter.field(metadata.getPkCode()).eq(nodeId));
		qp.addAllColumn(metadata);
		Map<String,Object> node = dao.get(qp);
		
		Integer idx=MapUtils.getInteger(node, indexCode);
		
		if(idx!=null){
			qp=new QueryParam();
			qp.setTableName(metadata.getTableName());
			qp.setFilter(Filter.field(metadata.getColumnName(pidCode)).eq(pid)
					.and(Filter.field(metadata.getColumnName(indexCode)).gt(idx))
					.and(Filter.field(metadata.getColumnName(metadata.getPkCode())).ne(nodeId)));
			qp.addColumn(metadata.getColumnName(metadata.getPkCode()),"id");
			qp.orderBy(new String[]{metadata.getColumnName(pidCode),metadata.getColumnName(indexCode)});
			
			List<Map<String,Object>> siblings=dao.list(qp);
			
			if(CollectionUtils.isEmpty(siblings)){//last child
				return;
			}
			
			int i=idx.intValue();
			
			up=new UpdateParam();
			up.setTableName(metadata.getTableName());
			Filter f=Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(new SqlPlaceHolder("itemId"));
			int c=0;
			for(Map<String,Object> item:siblings){
				String itemId=ObjectUtils.toString(item.get("id"));
				up.addColumnValue(metadata.getColumnName(indexCode), i++,c);
				f.setValue("itemId", itemId);
				
				c++;
			}
			up.setFilter(f);
			dao.batchUpdate(up);
		}
	}
	
	private void appendChild(EntityMetadata metadata,TreeConfig config,String nodeId,String pid,Integer idx){
		String pidCode = config.getPidCode();
		String indexCode = config.getIndexCode();
		
		if(StringUtils.isBlank(indexCode)){
			UpdateParam up=new UpdateParam();
			up.setTableName(metadata.getTableName());
			
			up.addColumnValue(metadata.getColumnName(pidCode), pid);
			up.setFilter(Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(nodeId));
			dao.update(up);
			
			return;	
		}
		
		if(idx!=null){
			QueryParam qp=new QueryParam();
			qp.setTableName(metadata.getTableName());
			qp.setFilter(Filter.field(metadata.getColumnName(pidCode)).eq(pid)
					.and(Filter.field(metadata.getColumnName(indexCode)).ge(idx))
					.and(Filter.field(metadata.getColumnName(metadata.getPkCode())).ne(nodeId)));
			qp.addColumn(metadata.getColumnName(metadata.getPkCode()),"id");
			qp.orderBy(new String[]{metadata.getColumnName(pidCode),metadata.getColumnName(indexCode),metadata.getColumnName(metadata.getPkCode())});
			
			List<Map<String,Object>> siblings=dao.list(qp);
			
			int i=idx.intValue()+1;
			
			if(CollectionUtils.isNotEmpty(siblings)){
				UpdateParam up=new UpdateParam();
				up.setTableName(metadata.getTableName());
				Filter f=Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(new SqlPlaceHolder("itemId"));
				int c=0;
				for(Map<String,Object> item:siblings){
					String itemId=ObjectUtils.toString(item.get("id"));
					up.addColumnValue(metadata.getColumnName(indexCode), i++,c);
					f.setValue("itemId", itemId);
					
					c++;
				}
				up.setFilter(f);
				dao.batchUpdate(up);
			}
		}else{
			QueryParam qp=new QueryParam();
			qp.setTableName(metadata.getTableName());
			qp.setFilter(Filter.field(pidCode).eq(pid));
			qp.addColumn("MAX("+metadata.getColumnName(indexCode)+")","maxIdx");
			
			Map<String,Object> tmp=dao.get(qp);
			Integer maxIdx=MapUtils.getInteger(tmp, "maxIdx",0);
			idx=maxIdx+1;
		}
		
		UpdateParam up=new UpdateParam();
		up.setTableName(metadata.getTableName());
		
		up.addColumnValue(metadata.getColumnName(indexCode), idx);
		up.addColumnValue(metadata.getColumnName(pidCode), pid);
		up.setFilter(Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(nodeId));
		dao.update(up);
		
		String pathCode = config.getPathCode();
		if(StringUtils.isNotBlank(pathCode)){
			updatePath(metadata,pid,pathCode,nodeId);
		}
	}
	
	/**
	 * update path of node,include all children
	 * 
	 * @param metadata
	 * @param pid
	 * @param pathCode
	 * @param nodeId
	 */
	private void updatePath(EntityMetadata metadata,String pid,String pathCode,String nodeId){
		//get target node
		QueryParam qp=new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.setFilter(Filter.field(metadata.getPkCode()).eq(nodeId));
		qp.addAllColumn(metadata);
		Map<String,Object> node = dao.get(qp);
		
		//get parent node
		qp=new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.setFilter(Filter.field(metadata.getPkCode()).eq(pid));
		qp.addAllColumn(metadata);
		Map<String,Object> parent = dao.get(qp);
		
		String path=MapUtils.getString(node, pathCode);
		
		//target node has path already,get all children use "like"
		if(StringUtils.isNotBlank(path)){
			UpdateParam up=new UpdateParam();
			up.setTableName(metadata.getTableName());
			
			String pPath=MapUtils.getString(parent, pathCode,"-");
			String newPath=pPath+nodeId+"-";
			
			String pathCol=metadata.getColumnName(pathCode);
			up.addColumnValue(pathCol,new DBFunction("replace("+pathCol+",'"+path+"','"+newPath+"')"));

			up.setFilter(Filter.field(metadata.getColumnName(pathCode)).like(path+"%"));
			dao.update(up);
		}else{//target node has no path,suppose target node is new created
			UpdateParam up=new UpdateParam();
			up.setTableName(metadata.getTableName());
						
			String pPath=MapUtils.getString(parent, pathCode,"-");
			up.addColumnValue(metadata.getColumnName(pathCode),pPath+nodeId+"-");

			up.setFilter(Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(nodeId));
			dao.update(up);
		}
	}
	
	public int deleteNode(IEntityCode code, TreeConfig config, String nodeId) throws BusinessException {
		if (code == null || nodeId == null) {
			throw new BusinessException("String or Object cannot be null");
		}
		
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		String tableName = metadata.getTableName();
		
		final String pkCode = metadata.getPkCode();
		final String pidCode = config.getPidCode();
		final String pathCode = config.getPathCode();
		
		QueryParam qp = new QueryParam();
		qp.setTableName(tableName);
		qp.setFilter(Filter.field(metadata.getColumnName(metadata.getPkCode())).eq(nodeId));
		Map node = dao.get(qp);
		
		if (StringUtils.isNotBlank(pidCode)) {
			removeChild(metadata, config, nodeId, MapUtils.getString(node, pidCode));
			
			if (StringUtils.isNotBlank(pathCode)) {
				String path = MapUtils.getString(node, pathCode);
				
				DeleteParam dp = new DeleteParam();
				dp.setTableName(tableName);
				dp.setFilter(Filter.field(metadata.getColumnName(pathCode)).like(path + "%"));
				return dao.delete(dp);
			}
			
			qp = new QueryParam();
			qp.setTableName(tableName);
			List list = dao.list(qp);
			TreeBuilder tb = new TreeBuilder(list) {				
				public String getPid(Object item){
					Map map = (Map) item;
					if (StringUtils.isNotBlank(pidCode)) {
						String s = MapUtils.getString(map, pidCode);
						return s;
					} else {
						return "-1";
					}
				}
				
				public String getId(Object item){
					Map map = (Map) item;
					return MapUtils.getString(map, pkCode);
				}
			};

			List<Map> nodeList = (List<Map>)tb.getNodeList(nodeId);
			List<String> ids = new ArrayList<String>();
			ids.add(nodeId);
			for (Map map : nodeList) {
				ids.add(MapUtils.getString(map, pkCode));
			}
			DeleteParam dp = new DeleteParam();
			dp.setTableName(tableName);
			dp.setFilter(Filter.field(metadata.getColumnName(pkCode)).in(ids.toArray()));
			return dao.delete(dp);
		} 
		
		throw new BusinessException("expected pid");
	}	
	
	public List<Map<String, Object>> list(IEntityCode code, TreeConfig config, Map<String, Object> where, IUser user) throws BusinessException {
		EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(code);
		return list(entiyMetadata, config, where, user);
	}	
	
	public List<Map<String, Object>> list(EntityMetadata metadata, TreeConfig config, Map<String, Object> where, IUser user) throws BusinessException {
		if (metadata == null) {
			throw new BusinessException("EntityMetadata cannot be null or empty");
		}	
		List<Map<String, Object>> where1 = entityService.factor(where);
		return list(metadata, config, where1, user);
	}
	
	public List<Map<String, Object>> list(IEntityCode code, TreeConfig config, List<Map<String, Object>> where, IUser user) throws BusinessException {
		EntityMetadata entiyMetadata = metadataProvider.getEntityMetadata(code);
		return list(entiyMetadata, config, where, user);
	}	
	
	public List<Map<String, Object>> list(EntityMetadata metadata, TreeConfig config, List<Map<String, Object>> where, IUser user) throws BusinessException {
		if (metadata == null) {
			throw new BusinessException("EntityMetadata cannot be null or empty");
		}	
		
		String tableName = metadata.getTableName();
		
		final String pidCode = config.getPidCode();
		final String indexCode = config.getIndexCode();
		
		QueryParam qp = new QueryParam();
		qp.setTableName(tableName);
		qp.addAllColumn(metadata);
		
		List<String> tmp = new ArrayList<String>();
		if(StringUtils.isNotBlank(pidCode)){
			String colName = metadata.getColumnName(pidCode);
			if(StringUtils.isNotBlank(colName)){
				tmp.add(colName);
			}
		}
		
		if(StringUtils.isNotBlank(indexCode)){
			String colName = metadata.getColumnName(indexCode);
			if(StringUtils.isNotBlank(colName)){
				tmp.add(colName);
			}
		}
		
		if(tmp.size()>0){
			qp.orderBy(tmp.toArray(new String[]{}));
		}	
		
		Filter filter = EntityService.createFilter(metadata, where);
		qp.setFilter(filter);
		
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
	
	/**
	 * 
	 * 
	 * @param code
	 * @param treeConfig {pid:'parentId',index:'idx',level:'level',path:'code'}
	 * @param nodeId
	 * @param targetPid
	 * @param index
	 * @throws BusinessException
	 */
	public void moveNode(IEntityCode code, TreeConfig config, String nodeId, String pid, Integer index) throws BusinessException {
		final String pidCode = config.getPidCode();
		
		if (code == null || nodeId == null || pid == null || index == null || pidCode == null) {
			throw new BusinessException("some parameter is null");
		}
		
		EntityMetadata metadata = metadataProvider.getEntityMetadata(code);
		
		//get target node
		QueryParam qp=new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.setFilter(Filter.field(metadata.getPkCode()).eq(nodeId));
		qp.addAllColumn(metadata);
		Map<String,Object> node = dao.get(qp);
		
		String oldPid=MapUtils.getString(node, pidCode);

		removeChild(metadata, config, nodeId, oldPid);
		appendChild(metadata, config, nodeId, pid, index);
	}	
}


