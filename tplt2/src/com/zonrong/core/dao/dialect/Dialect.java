package com.zonrong.core.dao.dialect;



public interface Dialect {
	public static final String RS_COUNT = "rs_count";

	public static final String SQL_ROWNUM_ALIAS = "ROWNUMBER_A";
	
	public boolean supportsLimit();

	public String getLimitString(String sql, boolean hasOffset);

	public String getLimitString(String sql, int offset, int limit);

	public String getCountSqlString(String sql);	
	
	public String getDBFun(DBFun funtion);		
	
	public String getDBFun(DBFun funtion, String fieldName, Object startDate, Object endDate);	
}