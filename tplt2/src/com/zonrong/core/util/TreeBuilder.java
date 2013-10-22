package com.zonrong.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * date: 2010-11-5
 *
 * version: 1.0
 * commonts: ......
 */
public abstract class TreeBuilder {
	private List<Object> dataList;

	private List nodeList = new ArrayList();
	//private String rootId;
	
	

	public TreeBuilder(List<Object> dataList) {
		super();
		this.dataList = dataList;
	}

	public abstract String getPid(Object item);

	public abstract String getId(Object item);

	public List<Map<String, Object>> getTree(String nodeId) {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();

		List<Object> topNode = getChildrenByParentId(nodeId);
		for (Object item : topNode) {
			ret.add(createNode(item));
		}
		return ret;
	}

	private Map<String, Object> createNode(Object data) {
		try{
			Map<String,Object> ret = (Map<String,Object>)data;
			String id=getId(data);
			List<Object> children=getChildrenByParentId(id);
			ret.put("leaf", true);
			if(children.size()>0){
				List<Map<String,Object>> childNodes=new ArrayList<Map<String,Object>>();
				for (Object item : children) {					
					childNodes.add(createNode(item));
				}
				
				ret.put("children", childNodes);
				ret.put("leaf", childNodes.size()<1);
			}
			
			return ret;
			
		}catch(Exception e){
						
		}
		
		return null;
	}
	
	public List getNodeList(String nodeId) {
		getTree(nodeId);
		return nodeList;
	}

	private List<Object> getChildrenByParentId(String id) {
		List<Object> ret = new ArrayList<Object>();
		if (StringUtils.isNotBlank(id)) {
			for (Object item : dataList) {
				if(id.equals(getPid(item))){
					ret.add(item);
					nodeList.add(item);
				}
			}
		}
		return ret;
	}

	public void setDataList(List<Object> dataList) {
		this.dataList = dataList;
	}
}


