package com.zonrong.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * date: 2010-7-23 author: wangliang
 * 
 * version: 1.0 commonts: ......
 */
public class TreeProcess {
	public static List<Map> buildTree(List list, Object rootValue,
			TreeProcessHelper helper) {
		List<Map> targetlist = new ArrayList();
		if (StringUtils.isEmpty(helper.getIdFieldName())) {
			return list;
		}
		List childrenList = helper.getChildrenByRootValue(list, rootValue);
		for (Object row : childrenList) {
			Map tempMap = helper.rowProcess(row);
			Map targetMap = addChildrenNode(list, tempMap, helper
					.getIdValue(row), helper);
			targetlist.add(targetMap);
		}
		return targetlist;
	}

	private static Map addChildrenNode(List<Map> list, Map sourceMap,
			Object rootValue, TreeProcessHelper helper) {
		List childrenList = helper.getChildrenByRootValue(list, rootValue);
		List children = new ArrayList();
		for (Object row : childrenList) {
			Map tempMap = helper.rowProcess(row);
			children.add(tempMap);
			addChildrenNode(list, tempMap, helper.getIdValue(row), helper);
		}
		if (children.size() > 0) {
			sourceMap.put("leaf", Boolean.FALSE);
			sourceMap.put("children", children);
		} else {
			sourceMap.put("leaf", Boolean.TRUE);
		}
		return sourceMap;
	}
}

