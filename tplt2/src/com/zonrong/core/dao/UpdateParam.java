package com.zonrong.core.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.zonrong.core.dao.dialect.DBFun;
import com.zonrong.core.dao.dialect.DBFunction;
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
public class UpdateParam implements SqlParam{
	private String tableName;
	private Map<String,List<Object>> columnValues = new HashMap<String,List<Object>>();
	private Filter filter;
	
	public String getSql(Dialect dialect){
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE ").append(tableName).append(" SET");
	
		List<String> tmp=new ArrayList<String>();
		for(String item:columnValues.keySet()){
			List<Object> values=columnValues.get(item);
			if((values.size())>0){
				if(values.get(0) instanceof DBFun){
					tmp.add(item + "=" + dialect.getDBFun((DBFun)values.get(0)));
				}else if(values.get(0) instanceof DBFunction){
					tmp.add(item + "=" + ((DBFunction)values.get(0)).toString());
				}else {
					tmp.add(item + "=" + new SqlPlaceHolder(item));
				}
			}
		}
		
		String fieldsStr = StringUtils.join(tmp.iterator(),", ");
		sb.append(" ").append(fieldsStr);

		if(filter!=null){
			String filterStr=filter.toString();
			if(StringUtils.isNotBlank(filterStr)){
				sb.append(" WHERE ").append(filterStr);
			}
		}
			
		return sb.toString();
	}
	
	/**
	 * dependent on getSql()
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getValues(Dialect dialect){
		if (columnValues.size() < 1) {
			return null;
		}

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int size = columnValues.values().iterator().next().size();
		
		for (int i = 0; i < size; i++) {
			Map<String, Object> row = new HashMap<String, Object>();
			for(Iterator<Entry<String, List<Object>>> it = columnValues.entrySet().iterator(); it.hasNext(); ) {
				Entry<String, List<Object>> entry = it.next();
				List<Object> vlist = entry.getValue();
				Object value=vlist.get(i);
				if(value instanceof DBFun){
					continue;
				}else if(value instanceof DBFunction){
					continue;
				}else if(value ==  null || StringUtils.isBlank(value.toString())){
					row.put(entry.getKey(), null);
				}else {
					row.put(entry.getKey(), value);
				}
			}
			
			if(filter != null){
				List<Map<String, Object>> filterValueList = filter.getValues();
				
				if((filterValueList!=null) && filterValueList.size()>=i){
					row.putAll(filterValueList.get(i));
				}
			}
			
			list.add(row);
		}
		return list;
	}
	
	public UpdateParam addColumnValue(String colName,Object value){
		return addColumnValue(colName,value,0);
	}
	
	public UpdateParam addColumnValue(String colName,Object value,int rowIndex){
		List<Object> vs=columnValues.get(colName);
		if(vs==null){
			vs=new ArrayList<Object>();
			columnValues.put(colName, vs);
		}
		
		if(vs.size()<rowIndex){
			for(int i=vs.size();i<rowIndex;i++){
				vs.add(null);
			}
		}
		
		vs.add(rowIndex,value);
		
		return this;
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
	
	public Map<String, List<Object>> getColumnValues() {
		return columnValues;
	}

	public void setColumnValues(Map<String, List<Object>> columnValues) {
		this.columnValues = columnValues;
	}
	
	public static void main(String[] args){
		List<Map<String,Object>> param=new ArrayList<Map<String,Object>>();
		Map<String,Object> item=new HashMap<String,Object>();
		item.put("field1", "value1");
		item.put("field2", new SqlServer2005Dialect().getDBFun(DBFun.currentDate));
		item.put("field3", "value3");
		item.put("field4", "value4");
		param.add(item);
		
		Map<String,Object> item1=new HashMap<String,Object>();
		item1.put("field4", "value4");
		item1.put("field2", new DBFunction("replace(a, b, c)"));
		item1.put("field3", "value3");
		item1.put("field1", "value1");
		param.add(item1);

		UpdateParam p = new UpdateParam();
		p.setTableName("test");
		
		Filter f = Filter.field("name").eq(new SqlPlaceHolder("vname"));
		f.and(Filter.field("age").eq(new SqlPlaceHolder("vage")));
		p.setFilter(f);
		
		for(Map<String,Object> i:param){
			for(Iterator<String> it=i.keySet().iterator();it.hasNext();){
				String key=it.next();
				p.addColumnValue(key, i.get(key),0);
				f.setValue("vage", 23);
				f.setValue("vname", "zhangsan");
			}
		}
		
		
		System.out.println(p.getSql(new SqlServer2005Dialect()));
		System.out.println(p.getValues(new SqlServer2005Dialect()));
		
	}
}


