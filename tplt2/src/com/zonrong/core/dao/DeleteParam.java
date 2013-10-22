package com.zonrong.core.dao;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.zonrong.core.dao.dialect.Dialect;
import com.zonrong.core.dao.dialect.SqlPlaceHolder;
import com.zonrong.core.dao.dialect.SqlServer2005Dialect;
import com.zonrong.core.dao.filter.Filter;

/**
 * date: 2010-11-9
 *
 * version: 1.0
 * commonts: ......
 */
public class DeleteParam implements SqlParam {
	private String tableName;
	private Filter filter;
	
	public String getSql(Dialect dialect){
		StringBuffer sb=new StringBuffer();
		
		sb.append("DELETE FROM ").append(tableName);
		if(filter!=null){
			String filterStr=filter.toString();
			if(StringUtils.isNotBlank(filterStr)){
				sb.append(" WHERE ").append(filterStr);
			}
		}
		
		return sb.toString();
	}
	
	public List<Map<String, Object>> getValues(Dialect dialect){
		if (filter == null) {
			return null;
		}
		
		return filter.getValues();
	}	
	
	public Filter getFilter() {
		return filter;
	}
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public static void main(String[] args){
		DeleteParam p = new DeleteParam();
		p.setTableName("test");
		
		Filter f = Filter.field("name").eq(new SqlPlaceHolder("vname"));
		f.and(Filter.field("age").eq(new SqlPlaceHolder("vage")));
		p.setFilter(f);
		
		for(int i = 0; i < 5; i ++){
			f.setValue("vage", 23 * i);
			f.setValue("vname", "zhangsan");
		}
		
		
		System.out.println(p.getSql(new SqlServer2005Dialect()));
		System.out.println(p.getValues(new SqlServer2005Dialect()));
		
	}	
}


