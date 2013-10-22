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

/**
 * date: 2010-11-9
 *
 * version: 1.0
 * commonts: ......
 */
public class InsertParam implements SqlParam{
	private String tableName;
	private Map<String,List<Object>> columnValues = new HashMap<String,List<Object>>();
	
	public String getSql(Dialect dialect){
		if(columnValues.size()<1){
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO ").append(tableName);
	
		List<String> fList=new ArrayList<String>();
		List<Object> vList=new ArrayList<Object>();
		for(String item:columnValues.keySet()){
			List<Object> values=columnValues.get(item);
			if((values.size())>0){
				fList.add(item);
				if(values.get(0) instanceof DBFun){
					vList.add(dialect.getDBFun((DBFun)values.get(0)));
				}else if(values.get(0) instanceof DBFunction){
					vList.add(((DBFunction)values.get(0)).toString());
				}else{
					vList.add(new SqlPlaceHolder(item));
				}
			}
		}
		
		String fieldsStr = StringUtils.join(fList.iterator(),",");
		sb.append("(").append(fieldsStr).append(")");
		String valuesStr=StringUtils.join(vList.iterator(), ",");
		sb.append(" VALUES (").append(valuesStr).append(")");

		
		return sb.toString();
	}
	
	/**
	 * dependent on getSql()
	 * 
	 * @param dialect
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
				Object value=null;
				
				if(vlist.size() > i){
					value=vlist.get(i);
				}
				
				if(value instanceof DBFun){
					continue;
				}else if(value instanceof DBFunction){
					continue;
				}else{
					row.put(entry.getKey(), value);
				}
			}	
			list.add(row);
		}
		
		return list;
	};
	
	public InsertParam addColumnValue(String colName,Object value,int rowIndex){
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
		
		vs.add(value);
		return this;
	}
	
	public Map<String, List<Object>> getColumnValues() {
		return columnValues;
	}
	
	public void setColumnValues(Map<String, List<Object>> columnValues) {
		this.columnValues = columnValues;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
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
		
//		Map<String,Object> item3=new HashMap<String,Object>();
//		item3.put("field1", "value1");
//		item3.put("field3", "value3");
//		item3.put("field4", "value4");
//		item3.put("field2", DBFunction.DBFun);
//		param.add(item3);
		
		InsertParam p=new InsertParam();
		p.setTableName("test");
		for(Map<String,Object> i:param){
			for(Iterator<String> it=i.keySet().iterator();it.hasNext();){
				String key=it.next();
				p.addColumnValue(key, i.get(key),0);
			}
		}
		
//		p.getSql(new SqlServer2005Dialect());
		
		
//		InsertParam p=new InsertParam();
//		p.setTableName("test");
//		
//		p.addFieldValue("field1", "Value1");
//		p.addFieldValue("sys", DBFunction.currentDate);
		
		System.out.println(p.getSql(new SqlServer2005Dialect()));
		System.out.println(p.getValues(new SqlServer2005Dialect()));
		
	}
}


