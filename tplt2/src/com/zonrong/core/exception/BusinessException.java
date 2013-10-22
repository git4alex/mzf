package com.zonrong.core.exception;

public class BusinessException extends Exception {
	private static final long serialVersionUID = 170160191789359544L;

	public BusinessException(String msg) {
		super(msg);
	}

	public BusinessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
