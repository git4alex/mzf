package com.zonrong.core.dao.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.zonrong.core.dao.dialect.DBFun;
import com.zonrong.core.dao.dialect.DBFunction;
import com.zonrong.core.dao.dialect.Dialect;
import com.zonrong.core.dao.dialect.SqlPlaceHolder;
import com.zonrong.core.dao.dialect.SqlServer2005Dialect;

public class Filter {
	public static final String EQ = "=";
	public static final String GT = ">";
	public static final String LT = "<";
	public static final String GTEQ = ">=";
	public static final String LTEQ = "<=";
	public static final String NOT_EQ1 = "<>";
	public static final String NOT_EQ2 = "!=";
	public static final String LIKE = "LIKE";
	public static final String N_LIKE = "nLIKE";
	public static final String IN = "IN";
	public static final String NOT_IN = "NOT IN";
	public static final String NULL = "IS NULL";
	public static final String NOT_NULL = "IS NOT NULL";
	public static final String BETWEEN = "BETWEEN";
	
	Dialect dialect = new SqlServer2005Dialect();
	
	private String field;
	private String op;
	private Object value;
	
	private String extendFilterStr;
	
	private List<Sibling> siblings=new ArrayList<Sibling>();
	
	private Filter(){
		
	}
	
	private Filter(String field){
		this.field=field;
	}
	
	public static Filter emptyFilter(){
		return new Filter();
	}
	
	public static Filter field(String field){
		return new Filter(field);
	}
	
	public static Filter group(Filter filter){
		Filter f=new Filter();
		f.siblings.add(f.new Sibling("AND", filter));
		
		return f;
	}
	
	public Filter eq(Object value) {
		this.op=EQ;
		this.value = wrap(value);
		return this;
	}
	
	public Filter lt(Object value) {
		this.op=LT;
		this.value = wrap(value);
		return this;
	}
	
	public Filter gt(Object value) {
		this.op=GT;
		this.value = wrap(value);
		return this;
	}
	
	public Filter le(Object value) {
		this.op=LTEQ;
		this.value = wrap(value);
		return this;
	}
	
	public Filter ge(Object value) {
		this.op=GTEQ;
		this.value = wrap(value);
		return this;
	}	
	
	public Filter ne(Object value) {
		this.op=NOT_EQ1;
		this.value = wrap(value);
		return this;
	}
	
	public Filter like(Object value) {
		this.op=LIKE;
		this.value= wrap(value);
		return this;
	}
	
	public Filter bt(Object start, Object end) {
		boolean value1Exists = false, value2Exists = false;
		if (start != null && (start + "").length() > 0)
			value1Exists = true;
		if (end != null && (end + "").length() > 0)
			value2Exists = true;

		if (value1Exists && value2Exists) {
			this.op=BETWEEN;
			this.value = new String[]{wrap(start),wrap(end)};
			return this;
		} else if (value1Exists) {
			return ge(start);
		} else if (value2Exists) {
			return le(end);
		}		
		return this;
	}
	
	public Filter dataBt(Object start, Object end) {
		this.op="DATE_BETWEEN";
		this.value = new String[]{wrap(start),wrap(end)};
		return this;
	}
	
	public Filter in(Object obj) {
		this.op=IN;
		this.value = obj;
		return this;
	}
	
	public Filter notIn(Object obj) {
		this.op=NOT_IN;
		this.value = obj;
		return this;
	}
	
	public Filter isNull() {
		this.op = NULL;
		return this;
	}
	
	public Filter isNotNull() {
		this.op = NOT_NULL;
		return this;
	}	
	
	public Filter and(Filter f){
		this.siblings.add(new Sibling("AND", f));
		return this;
	}
	
	public Filter or(Filter f){
		this.siblings.add(new Sibling("OR", f));
		return this;
	}
	
	public String toString(){
		List<String> tmp=new ArrayList<String>();
		if(null != this.field && null !=this.value){			
			if(this.op.equals(IN) || this.op.equals(NOT_IN)){
				tmp.add(this.field);				
				tmp.add(this.op);
				
				List<Object> valueList = new ArrayList<Object>();
				if (this.value instanceof SqlPlaceHolder) {
					SqlPlaceHolder holder = (SqlPlaceHolder) this.value;
					String holderName = holder.getName();
					Object sqlValue = ((List)this.values.get(holderName)).get(0);
					List list = new ArrayList();
					if (sqlValue instanceof Collection<?>) {
						Collection c = (Collection) sqlValue;
						list.addAll(c);
					} else if (sqlValue.getClass().isArray()) {
						Object[] obj = (Object[]) sqlValue;
						list.addAll(Arrays.asList(obj));
					} else {
						
					}
					
					if (list.size() > 0) {
						for (int i = 0; i < list.size(); i++) {
							String newHolderName = holderName + i;
							valueList.add(new SqlPlaceHolder(newHolderName));
							this.setValue(newHolderName, list.get(i));
						}		
						this.values.remove(holderName);
						this.value = valueList;
					}
				} else {					
					if(this.value instanceof Collection<?>){
						this.value=((Collection<?>)this.value).toArray();
					}else if(!this.value.getClass().isArray()){
						this.value=new Object[]{this.value};
					}
					
					Object[] vals = (Object[])this.value;

					for (Object val:vals) {
						valueList.add(wrap(val));
					}
				}
				tmp.add("(");
				tmp.add(StringUtils.join(valueList.iterator(),","));
				tmp.add(")");
			} else{
				tmp.add(this.field);				
				tmp.add(this.op);				
				tmp.add(ObjectUtils.toString(this.value));	
			}
		} else if(null != this.field && null ==this.value){
			tmp.add(this.field);
			tmp.add(this.op);
		}
		
		if(this.siblings.size()>0){
			if(tmp.size()>0){
				tmp.add(0, "(");
				
				for(Sibling s:this.siblings){
					tmp.add(s.getOp());
					tmp.add(s.getFilter().setValues(values).toString());
				}
				
				tmp.add(")");
			}else{
				Sibling fs=this.siblings.get(0);
				tmp.add(fs.getFilter().setValues(values).toString());
				
				if(this.siblings.size()>1){
					for(int i=1;i<this.siblings.size();i++){
						Sibling s=this.siblings.get(i);
						tmp.add(s.getOp());
						tmp.add(s.getFilter().setValues(values).toString());
					}					
				}
			}
		}
		
		String ret=StringUtils.join(tmp.iterator()," ");
		
		if(StringUtils.isNotBlank(extendFilterStr)){
			if(StringUtils.isNotBlank(ret)){
				ret = "(" + ret + ") " + extendFilterStr;
			}else{
				ret = "1=1 " + extendFilterStr;
			}
		}
		
		return ret;
	}
    
	private String wrap(Object obj) {
		if(obj == null){
			return null;
		}
		if (obj instanceof Number) {
			return ObjectUtils.toString(obj);
		} else if (obj instanceof String && obj != null && obj.toString().startsWith(":")) {
			return ObjectUtils.toString(obj);
		} else if (obj instanceof DBFun) {
			return ObjectUtils.toString(obj);
		} else if (obj instanceof DBFunction) {
			return ObjectUtils.toString(obj);
		} else if (obj instanceof SqlPlaceHolder) {
			return ObjectUtils.toString(obj);
		}  else {
			return "'" + ObjectUtils.toString(obj) + "'";
		}			
	}	
	
	private Map<String, List<Object>> values = new HashMap<String, List<Object>>();
	
	private Filter setValues(Map<String, List<Object>> values) {
		this.values = values;
		return this;
	}
	
	public void setValue(String name, Object value) {
		List<Object> list = values.get(name);
		if (list == null) {
			list = new ArrayList<Object>();
			values.put(name, list);
		}
		
		list.add(value);
	}
	
	public List<Map<String, Object>> getValues() {
		if (values.size() < 1) {
			return null;
		}

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int size = values.values().iterator().next().size();
		
		for (int i = 0; i < size; i++) {
			Map<String, Object> row = new HashMap<String, Object>();
			for(Iterator<Entry<String, List<Object>>> it = values.entrySet().iterator(); it.hasNext(); ) {
				Entry<String, List<Object>> entry = it.next();
				List<Object> vlist = entry.getValue();
				
				row.put(entry.getKey(), vlist.get(i));
			}	
			list.add(row);
		}
		
		return list;
	}
	
	private class Sibling{
		private String op;
		private Filter f;
		
		public Sibling(String op,Filter f){
			this.op=op;
			this.f=f;
		}

		public String getOp() {
			return op;
		}

		public Filter getFilter() {
			return f;
		}
		
		
	}


	public String getField() {
		return field;
	}

	public String getOp() {
		return op;
	}

	public Object getValue() {
		return value;
	}
	
	public static void main(String[] args){
		Filter filter = Filter.group(Filter.field("f3").ge("v3").or(Filter.field("f4").in(new SqlPlaceHolder("f4")))).and(Filter.field("field2").gt("value2")).and(Filter.field("field4").isNull());
//		filter.and(Filter.field("field3").dataBt("2000-01-01", "2010-10-01"));
//		filter.and(Filter.field("field3").isNotNull());
		filter.and(Filter.field("name").eq(new SqlPlaceHolder("vname")).or(Filter.field("age").eq(new SqlPlaceHolder("vage"))));
		
		System.out.println(filter.toString());
	}

	public String getExtendFilterStr() {
		return extendFilterStr;
	}

	public void setExtendFilterStr(String extendFilterStr) {
		this.extendFilterStr = extendFilterStr;
	}
}
