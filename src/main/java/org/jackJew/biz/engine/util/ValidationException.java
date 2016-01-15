package org.jackJew.biz.engine.util;

/**
 * exception for content validation
 * 
 * @author jack.zhu
 *
 */
public class ValidationException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public ValidationException(String message) {
		super(message);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

}
