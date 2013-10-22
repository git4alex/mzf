package com.zonrong.core.dao;

import java.util.List;
import java.util.Map;

import com.zonrong.core.dao.dialect.Dialect;

public interface SqlParam {
	public String getSql(Dialect dialect);
	public List<Map<String, Object>> getValues(Dialect dialect);
}
