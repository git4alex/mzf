package com.zonrong.core.exception;

public class DataAccessException extends org.springframework.dao.DataAccessException {
	private static final long serialVersionUID = 170160191789359544L;

	public DataAccessException(String msg) {
		super(msg);
	}

	public DataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
