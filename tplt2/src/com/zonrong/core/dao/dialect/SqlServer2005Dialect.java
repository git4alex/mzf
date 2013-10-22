package com.zonrong.core.dao.dialect;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * date: 2010-7-20
 *
 * version: 1.0
 * commonts: ......
 */
public class SqlServer2005Dialect implements Dialect {
	private static Logger logger = Logger.getLogger(SqlServer2005Dialect.class);
	
	protected static final String SQL_END_DELIMETER = ";";

	private static final String SQL_ROWNUM_FUNCTION = "ROW_NUMBER()";

	private static final String TMP_TABLE_1 = "TMP_TABLE_1";

	private static final String TMP_TABLE_2 = "TMP_TABLE_2";
	
	public boolean supportsLimit() {
		return true;
	}
	
	public String getCountSqlString(String sql) {
		sql = trimSuffix(sql);
		sql = getLineSql(sql);
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isNotBlank(sql)) {
			int orderIndex = getLastOrderInsertPoint(sql);
			if (orderIndex > 0) {
				sql = sql.substring(0, orderIndex);
			}
			sb.append("SELECT COUNT(1) AS ").append(RS_COUNT).append(" FROM (").append(sql).append(
					" ) TEMP_ ");
		}
		return sb.toString();
	}

	public String getLimitString(String sql, boolean hasOffset) {
		sql = trimSuffix(sql);
		StringBuffer sqlBuf = new StringBuffer(getPrefixSql(sql));	
		
		if (hasOffset) {
			sqlBuf.append(" WHERE ")
			.append(TMP_TABLE_2)
			.append(".")
			.append(SQL_ROWNUM_ALIAS)
			.append(" >= ? ")
			.append(" AND ")
			.append(TMP_TABLE_2)
			.append(".")
			.append(SQL_ROWNUM_ALIAS)
			.append(" < ? ");
		} else {
			sqlBuf.append(" WHERE ")
			.append(TMP_TABLE_2)
			.append(".")
			.append(SQL_ROWNUM_ALIAS)
			.append(" <= ? ");
		}

		return sqlBuf.toString();
	}

	public String getLimitString(String sql, int offset, int limit) {
		sql = trimSuffix(sql);
		StringBuffer sqlBuf = new StringBuffer(getPrefixSql(sql));
		
		if (offset > 0) {
			sqlBuf.append(" WHERE ")
			.append(TMP_TABLE_2)
			.append(".")
			.append(SQL_ROWNUM_ALIAS)
			.append(" > ")
			.append(offset)
			.append(" AND ")
			.append(TMP_TABLE_2)
			.append(".")
			.append(SQL_ROWNUM_ALIAS)
			.append(" <= ")
			.append(offset + limit);
		} else {
			sqlBuf.append(" WHERE ")
			.append(TMP_TABLE_2)
			.append(".")
			.append(SQL_ROWNUM_ALIAS)
			.append(" <= ")
			.append(limit);
		}
		return sqlBuf.toString();
	}
	
	private static String getPrefixSql(String sql) {
		sql	= getLineSql(sql);
		int orderIndex = getLastOrderInsertPoint(sql);
		if (orderIndex == -1) {
			String orderByColName;
			try{						
				String colList = sql.substring(sql.toUpperCase().indexOf("SELECT") + 6, sql.toUpperCase().indexOf("FROM"));
				String[] cols = colList.split(",");
				String[] name = cols[0].split(" ");
				
				orderByColName = name[name.length - 1];
				if (orderByColName.indexOf("*") >= 0)
					throw new Exception("没有找到默认的排序字段");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				orderByColName = "id";
			}
			sql += "order by " + orderByColName;
			orderIndex = getLastOrderInsertPoint(sql);
		}
		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append("SELECT * FROM (SELECT ")
		.append(SQL_ROWNUM_FUNCTION)
		.append(" OVER (" + sql.substring(orderIndex).replaceAll("[^\\s,]+\\.", "") + ") ")
		.append(SQL_ROWNUM_ALIAS)
		.append(" ,")
		.append(TMP_TABLE_1)
		.append(".* ")		
		.append("FROM (" + sql.substring(0, orderIndex)).append(") ")
		.append(TMP_TABLE_1)
		.append(") ")
		.append(TMP_TABLE_2);
		return sqlBuf.toString();		
	}	
	
	/**
	 * 得到最后一个Order By的插入点位置
	 * @return 返回最后一个Order By插入点的位置
	 */
	private static int getLastOrderInsertPoint(String sql){
		int orderIndex = sql.toLowerCase().lastIndexOf("order by");
		return orderIndex;
	}
	
	private static int getLastGroupInsertPoint(String sql){
		int orderIndex = sql.toLowerCase().lastIndexOf("group by");
		return orderIndex;
	}


	/**
	 * 将SQL语句变成一条语句，并且每个单词的间隔都是1个空格
	 * 
	 * @param sql SQL语句
	 * @return 如果sql是NULL返回空，否则返回转化后的SQL
	 */
	private static String getLineSql(String sql) {
		return sql.replaceAll("[\r\n]", " ").replaceAll("\\s{2,}", " ");
	}
	
	private static String trimSuffix(String sql) {
		if (sql != null) {
			sql = sql.trim();
			if (sql.endsWith(SQL_END_DELIMETER)) {
				sql = sql.substring(0, sql.length() - SQL_END_DELIMETER.length());
			}
		}
		return sql;
	}
	
	public String getDBFun(DBFun funtion) {
        switch (funtion) {    
        case currentDate:    
        	return "getDate()";
        case defaultValue:    
        	return "default";
        }
        return null;
	}
	
	public String getDBFun(DBFun funtion, String fieldName, Object startDate, Object endDate) {
        switch (funtion) {  
        case date:
        case currentDate:    
    		StringBuffer sb = new StringBuffer();
    		if (startDate instanceof String && StringUtils.isNotEmpty((String)startDate)) {
    			sb.append("dateDiff(dd, '" + startDate + "', " + fieldName + ") >= 0");
    		}
    		if (endDate instanceof String && StringUtils.isNotEmpty((String)endDate)) {
    			if (sb.length() > 0) sb.append(" and ");
    			sb.append("dateDiff(dd, '" + endDate + "', " + fieldName + ") <= 0");
    		} 
    		return sb.toString();        	
        }
        return null;
	}	
	
	public static void main(String[] args) {
		SqlServer2005Dialect dialect = new SqlServer2005Dialect();
		System.out.println(dialect.getLimitString("select * from  md_entity_info", 0, 10));
	}	
}


