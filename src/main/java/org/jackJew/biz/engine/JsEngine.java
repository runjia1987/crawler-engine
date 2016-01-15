package org.jackJew.biz.engine;

/**
 * interface for JsEngine
 * @author Jack
 *
 */
public interface JsEngine {
	
	String DEFAULT_CHARSET = "UTF-8";
	
	/**
	 * @param script JavaScript closure style
	 */
	public Object runScript(String script) throws Exception;

	/**
	 * @param script JavaScript closure style
	 */
	public String runScript2JSON(String script) throws Exception;	
	
}
