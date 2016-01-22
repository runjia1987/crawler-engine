package org.jackJew.biz.engine;

import org.jackJew.biz.engine.client.EngineClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(LoggerUtil.class);

	public static LoggerUtil getInstance() {
		return DebugMethodsHolder.LOGGER_UTIL;
	}
	
	private static class DebugMethodsHolder {
		private final static LoggerUtil LOGGER_UTIL = new LoggerUtil();
	}	

	private LoggerUtil() {
	}

	public void debug(String log) {
		logger.debug(EngineClient.CLIENT_NAME + " - " + log);
	}

	public void info(String log) {
		logger.info(EngineClient.CLIENT_NAME + " - " + log);
	}
	
	public void warn(String log) {
		logger.warn(EngineClient.CLIENT_NAME + " - " + log);
	}

	public void error(String log) {
		logger.error(EngineClient.CLIENT_NAME + " - " + log);
	}	
}