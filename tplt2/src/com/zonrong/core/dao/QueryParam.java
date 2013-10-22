package com.zonrong.core.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.zonrong.core.dao.dialect.Dialect;
import com.zonrong.core.dao.dialect.SqlPlaceHolder;
import com.zonrong.core.dao.dialect.SqlServer2005Dialect;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.FieldMetadata;

/**
 * date: 2010-11-9
 *
 * version: 1.0
 * commonts: ......
 */
public class QueryParam implements SqlParam{	
	private String tableName;
	private Filter filter;
	private String[] orderByFields;
	private String orderDir;
	private List<String[]> columns = new ArrayList<String[]>();
	
	public String getCountSql(){
		return getSql("count(1)");
	}
	
	private String getSql(String fieldsStr){
		StringBuffer sb = new StringBuffer();  
		sb.append("SELECT ");
		sb.append(fieldsStr);
		sb.append(" FROM ").append(tableName);
		if (filter != null) {
			String str=filter.toString();
			if(StringUtils.isNotBlank(str)){
				sb.append(" WHERE ").append(str);
			}
		}
		
//		StringBuffer orderBy = new StringBuffer();
//		if (!ArrayUtils.isEmpty(orderByFields)) {
//			orderBy.append(" ORDER BY " + StringUtils.join(orderByFields, ", "));
//			if ("DESC".equalsIgnoreCase(orderDir)) {
//				orderBy.append(" " + orderDir);		
//			}
//		}
//		
//		if (StringUtils.isNotEmpty(orderBy.toString())) {
//			sb.append(orderBy);		
//		}
		
		return sb.toString();
	}

	public List<Map<String, Object>> getValues(Dialect dialect){
		if (filter == null) {
			return null;
		}
		
		return filter.getValues();
	}
	
	public String getSql(Dialect dialect){		
		String sql = getSql(getFieldsStr());
		StringBuffer orderBy = new StringBuffer();
		if (!ArrayUtils.isEmpty(orderByFields)) {
			orderBy.append(" ORDER BY " + StringUtils.join(orderByFields, ", "));
			if ("DESC".equalsIgnoreCase(orderDir)) {
				orderBy.append(" " + orderDir);		
			}
		}
		
		if (StringUtils.isNotEmpty(orderBy.toString())) {
			sql += orderBy;		
		}		
		return sql;
	}
	
	private String getFieldsStr() {
		if (columns.size() < 1) {
			return "*";
		}
		StringBuffer sb;
		List<String> list = new ArrayList<String>();
		for (String[] field : columns) {
			sb = new StringBuffer();
			sb.append(field[0]);
			if (field.length == 2) {
				sb.append(" as ").append(field[1]);
			}
			list.add(sb.toString());
		}
		return StringUtils.join(list.toArray(), ", ");
	}
	
	public QueryParam addColumn(String colName, String alias) {
		String[] s = new String[] {colName, alias};
		columns.add(s);
		return this;
	}
	
	public QueryParam addColumn(String fieldName) {
		String[] s = new String[] {fieldName};
		columns.add(s);
		return this;
	}
	
	public void addAllColumn(EntityMetadata metadata) {
		List<FieldMetadata> fieldList = metadata.getFieldList();
		for (FieldMetadata fieldMetadata : fieldList) {
			addColumn(fieldMetadata.getColumnName(), fieldMetadata.getCode());
		}		
	}
	
	public Filter getFilter() {
		return filter;
	}
	public void setFilter(Filter filter) {
		this.filter = filter;
	}	
	
	public String[] getOrderByFields() {
		return orderByFields;
	}

	public QueryParam orderBy(String[] orderByFields) {
		this.orderByFields = orderByFields;
		return this;
	}

	public QueryParam orderBy(String orderByField) {
		this.orderByFields = new String[]{orderByField};
		return this;
	}
	
	public void desc(){
		orderDir="DESC";
	}
	
	public void asc(){
		orderDir="ASC";
	}

	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}	
	
	public static void main(String[] args){
		QueryParam param = new QueryParam();
		param.setTableName("sys_menu");
		param.addColumn("A");
		param.addColumn("B", "b");
		param.orderBy(new String[]{"A"});
		
		Filter f = Filter.field("name").eq(new SqlPlaceHolder("vname"));
		f.and(Filter.field("age").eq(new SqlPlaceHolder("vage")));
		param.setFilter(f);
		
		f.setValue("vage", 23);
		f.setValue("vname", "zhangsan");
		
		
		System.out.println(param.getSql(new SqlServer2005Dialect()));
		System.out.println(param.getValues(new SqlServer2005Dialect()));
		
	}	
}


