package com.zonrong.core.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.zonrong.core.dao.dialect.Dialect;
import com.zonrong.core.exception.DataAccessException;


@Repository("dao")
public class Dao extends SimpleJdbcDaoSupport {
	@Resource
	private Dialect dialect;
	
	@Resource
	public void setDs(DataSource ds) {
		super.setDataSource(ds);
	}

	public Map<String,Object> get(QueryParam param){		
		List<Map<String, Object>> list = list(param);

		if (list == null || list.size() < 1) {
			return null;
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			throw new DataAccessException("Incorrect result size: expected 1 actual " + list.size());
		}

	}	
	
	public List<Map<String, Object>> list(QueryParam param){
		String sql = param.getSql(dialect);
		List<Map<String, Object>> tempList = param.getValues(dialect);
		Map<String, Object> value = null;
		if (CollectionUtils.isNotEmpty(tempList)) {			
			value = tempList.get(0);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("sql:" + sql);
			logger.debug("param:" + value);
		}
		
		return this.getSimpleJdbcTemplate().queryForList(sql, value);
	}
	
	public Page page(QueryParam param,int start, int limit){
		String sql = param.getSql(dialect);
		int totalCount = count(param);
		String limitSql = dialect.getLimitString(sql, start, limit);
		List<Map<String, Object>> tempList = param.getValues(dialect);
		Map<String, Object> value = null;
		if (CollectionUtils.isNotEmpty(tempList)) {			
			value = tempList.get(0);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("sql:" + limitSql);
			logger.debug("param:" + value);
		}	
		
		List items = this.getSimpleJdbcTemplate().queryForList(limitSql, value);
		Page ret = new Page(start,limit);
		ret.setItems(items);
		ret.setTotalCount(totalCount);
		
		return ret;
	}
	
	public int count(QueryParam param){		
		String sql = param.getCountSql();
		List<Map<String, Object>> tempList = param.getValues(dialect);
		Map<String, Object> value = null;
		if (CollectionUtils.isNotEmpty(tempList)) {			
			value = tempList.get(0);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("sql:" + sql);
			logger.debug("param:" + value);
		}
		
		return this.getSimpleJdbcTemplate().queryForInt(sql, value);
	}
	
	public Number insert(InsertParam param){		
		final String sql=param.getSql(dialect);
		List<Map<String, Object>> tempList = param.getValues(dialect);
		final Map<String, Object> value = new HashMap<String, Object>();
		if (CollectionUtils.isNotEmpty(tempList)) {			
			value.putAll(tempList.get(0));
		}			
				
		SqlParameterSource paramSource = new MapSqlParameterSource(value);
		final String parsedSql = NamedParameterUtils.parseSqlStatementIntoString(sql);
		ParsedSql psSql = NamedParameterUtils.parseSqlStatement(sql);
		final Object[] vs = NamedParameterUtils.buildValueArray(psSql, paramSource, null);
		
		if (logger.isDebugEnabled()) {
			logger.debug("sql:" + parsedSql);
			logger.debug("params:" + Arrays.asList(vs));
		}
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
		this.getJdbcTemplate().update(new PreparedStatementCreator() {
	        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
	            PreparedStatement ps = connection.prepareStatement(parsedSql, Statement.RETURN_GENERATED_KEYS);
				for (int i = 0; i < vs.length; i++) {
					Object obj = vs[i];
					if (obj instanceof File) {
						try {
							File file = (File) obj;
							InputStream stream;
							stream = new FileInputStream(file);
							ps.setBinaryStream(i + 1, stream, (int) file.length());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							logger.error(e.getMessage(), e);
							throw new RuntimeException(e.getMessage());
						}
					} else {
						ps.setObject(i + 1, vs[i]);
					}
				}
	            return ps;
	        }
	    },keyHolder);
		
		try {			
			return keyHolder.getKey();	
		} catch (DataRetrievalFailureException e) {
			return null;
		}
	}
	
	public int update(UpdateParam param){
		String sql=param.getSql(dialect);
		List<Map<String, Object>> tempList = param.getValues(dialect);
		Map<String, Object> value = null;
		if (CollectionUtils.isNotEmpty(tempList)) {			
			value = tempList.get(0);
		}		
		
		if (logger.isDebugEnabled()) {
			logger.debug("sql:" + sql);
			logger.debug("params:" + value);
		}
		return this.getSimpleJdbcTemplate().update(sql, value);
	}
	
	public int[] batchUpdate(SqlParam param){
		String sql=param.getSql(dialect);
		List<Map<String, Object>> vList = param.getValues(dialect);
		Map[] vs = vList.toArray(new HashMap[]{});
		
		if (logger.isDebugEnabled()) {
			logger.debug("sql:" + sql);
			logger.debug("params:" + vList);
		}		

		return this.getSimpleJdbcTemplate().batchUpdate(sql, vs);
	}	
	
	public int delete(DeleteParam param){
		String sql = param.getSql(dialect);
		
		List<Map<String, Object>> batchValues = param.getValues(dialect);		
		if (logger.isDebugEnabled()) {
			logger.debug("sql:" + sql);
		}
				
		if (CollectionUtils.isNotEmpty(batchValues)) {
			Map<String, Object> value = batchValues.get(0);
			if (logger.isDebugEnabled()) {				
				logger.debug("params:" + value);
			}
			return this.getSimpleJdbcTemplate().update(sql, value);
		} else {
			return this.getSimpleJdbcTemplate().update(sql); 
		}
	}
	
	public int[] batchDelete(DeleteParam param){
		String sql = param.getSql(dialect);
		
		List<Map<String, Object>> batchValues = param.getValues(dialect);		
		if (CollectionUtils.isNotEmpty(batchValues)) {
			if (logger.isDebugEnabled()) {
				logger.debug("sql:" + sql);
				logger.debug("params:" + batchValues);
			}
			
			Map<String, ?>[] maps = batchValues.toArray(new HashMap[]{});
			return this.getSimpleJdbcTemplate().batchUpdate(sql, maps);
		} 
		
		return null;
	}
		

	
//	public Object getMsxValue(String fieldName) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("SELECT MAX(").append(fieldName).append(") as maxValue FROM ").append(tableName);
//		if (this.filter != null) {
//			sb.append(" WHERE ").append(this.filter.toString());
//		}
//		if (logger.isDebugEnabled()) {
//			logger.debug("sql:" + sb.toString());
//		}			
//		return this.getSimpleJdbcTemplate().queryForMap(sb.toString()).get("maxValue");
//	}
//	
//	public Object getMinValue(String fieldName) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("SELECT MIN(").append(fieldName).append(") as minValue FROM ").append(tableName);
//		if (this.filter != null) {
//			sb.append(" WHERE ").append(this.filter.toString());
//		}
//		if (logger.isDebugEnabled()) {
//			logger.debug("sql:" + sb.toString());
//		}			
//		return this.getSimpleJdbcTemplate().queryForMap(sb.toString()).get("minValue");
//	}
}

class MapSqlParameterSource implements SqlParameterSource {
	private Map<String, Object> value = new HashMap<String, Object>();
	public MapSqlParameterSource(Map<String, Object> value) {
		this.value = value;
	}
	
	public boolean hasValue(String paramName) {
		return value.containsKey(paramName);
	}
	
	public Object getValue(String paramName) throws IllegalArgumentException {
		return value.get(paramName);
	}
	
	public String getTypeName(String paramName) {
		return null;
	}
	
	public int getSqlType(String paramName) {
		return Types.OTHER;
	}
}
