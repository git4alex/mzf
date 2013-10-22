package com.zonrong.core.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.zonrong.metadata.EntityMetadata;

/**
 * date: 2010-12-10
 *
 * version: 1.0
 * commonts: ......
 */
public class OrderBy {	
	private String[] fields;
	
	private OrderByDir dir;
	
	public enum OrderByDir {
		asc,
		desc
	}
	
	public OrderBy(String[] fields, OrderByDir dir) {
		this.fields = fields;
		this.dir = dir;
	}
	
	public String toString(EntityMetadata metadata) {
		if (metadata == null) {
			return StringUtils.EMPTY;
		}
		
		if (ArrayUtils.isEmpty(fields)) {
			return StringUtils.EMPTY;
		}
		
		List<String> cols = new ArrayList<String>();
		for (String field : fields) {
			String col = metadata.getColumnName(field);
			if (StringUtils.isNotEmpty(col)) {
				cols.add(col);
			}
		}
		
		if (CollectionUtils.isEmpty(cols)) {
			return StringUtils.EMPTY;
		}
		
		StringBuffer sb = new StringBuffer();		
		sb.append(StringUtils.join(cols.iterator(), ", "));
		if (this.dir != null) {
			sb.append(" ").append(this.dir.toString());
		}

		return sb.toString();
	}
}


