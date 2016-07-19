package org.jackJew.biz.engine;

/**
 * interface for JsEngine,
 * <br />
 * For the sake of performance, scriptContext of all implementations are shared, 
 * so gloabl variables will be visible to all.
 * <br/>
 * Ensure that follow "var v = x;" style in "use scrict" mode.
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
