package org.jackJew.biz.engine;

import org.jackJew.biz.engine.util.BaseUtils;

public class SystemUtil {
	
	public static SystemUtil getInstance() {
		return SystemUtilHolder.SYSTEM_UTIL;
	}	
	
	private static class SystemUtilHolder {
		private static final SystemUtil SYSTEM_UTIL = new SystemUtil();
	}
	
	private SystemUtil() {
	}

	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
			LoggerUtil.getInstance().error(BaseUtils.getSimpleExMsg(e));
		}
	}
}
