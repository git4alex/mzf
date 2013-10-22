package com.zonrong.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-7-23
 *
 * version: 1.0
 * commonts: ......
 */
public class TreeProcessHelper {	
	private List<Object> idList = new ArrayList();	
	
	private String idFieldName;
	private String pidFieldName;
	
	
//	public TreeProcessHelper() {
//		this.idFieldName = Const.ENTITY_METADATA_TREE_ID;
//		this.pidFieldName = Const.ENTITY_METADATA_TREE_PID;
//	}
	
	public String getIdFieldName() {
		return idFieldName;
	}

	public void setIdFieldName(String idFieldName) {
		this.idFieldName = idFieldName;
	}

	public String getPidFieldName() {
		return pidFieldName;
	}

	public void setPidFieldName(String pidFieldName) {
		this.pidFieldName = pidFieldName;
	}

	public TreeProcessHelper(String idFieldName, String pidFieldName) {
		this.idFieldName = idFieldName;
		this.pidFieldName = pidFieldName;
	}
	
	public List getIdList() {
		return idList;
	}

	public void addId(Object id) {
		this.idList.add(id);
	}

	public Object getIdValue(Object row) {
		Map map = (Map) row;
		return map.get(this.idFieldName);
	}
	
	public Object getPidValue(Object row) {
		Map map = (Map) row;
		return map.get(this.pidFieldName);		
	}
	
	public Map rowProcess(Object row){

		return (Map) row;
	}
	
	public List getChildrenByRootValue(List list, Object rootValue) {
		if (list == null) return null;
		List treeList = new ArrayList();
		for (Object row : list) {
			Object pidValue = getPidValue(row);	
			if (pidValue == null)
				continue;
			
			if (pidValue instanceof Number) {
				int _pidValue = ((Integer) pidValue).intValue();
				int _rootValue;
				if (rootValue instanceof Number) {
					_rootValue = ((Integer) rootValue).intValue();					
				} else {
					_rootValue = Integer.parseInt(rootValue.toString());
				}
				
				if (_pidValue == _rootValue) {
					treeList.add(row);
					this.addId(getIdValue(row));
				}
			} else {
				String _pidValue = pidValue.toString();
				String _rootValue;
				if (rootValue instanceof Number) {
					_rootValue = ((Integer) rootValue).toString();					
				} else {
					_rootValue = rootValue.toString();
				}
				
				if (_pidValue.equals(_rootValue)) {
					treeList.add(row);
					this.addId(getIdValue(row));
				}
			}
		}
		return treeList;
	}	
}


