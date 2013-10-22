package com.zonrong.core.exception;

public class SqlParamException extends org.springframework.dao.DataAccessException {
	private static final long serialVersionUID = 170160191789359544L;

	public SqlParamException(String msg) {
		super(msg);
	}

	public SqlParamException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
