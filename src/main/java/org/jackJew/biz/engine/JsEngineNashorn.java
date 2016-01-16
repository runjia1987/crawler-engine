package org.jackJew.biz.engine;

import java.io.InputStream;
import java.util.Iterator;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.apache.commons.io.IOUtils;
import org.jackJew.biz.engine.util.BaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JS script engine, based on JDK 8 Nashorn.
 * @author Jack
 *
 */
public class JsEngineNashorn implements JsEngine {
	
	private final static Logger logger = LoggerFactory.getLogger(JsEngineNashorn.class);
	
	private final NashornScriptEngine scriptEngine;
	private final SimpleScriptContext scriptContext;
	
	private final static String config_package = "org/jackJew/biz/engine/config/nashorn/";
	
	public static JsEngineNashorn getInstance() {
		return JsEngineNashornHolder.JS_ENGINE_NASHORN;
	}
	
	private static class JsEngineNashornHolder {
		private final static JsEngineNashorn JS_ENGINE_NASHORN = new JsEngineNashorn();
	}
	
	private JsEngineNashorn() {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		scriptEngine = (NashornScriptEngine)scriptEngineManager.getEngineByName("nashorn");
		
		Bindings bindings = new SimpleBindings();
		bindings.put("$$http", HttpEngineAdapter.getInstance());
		bindings.put("$$system", SystemUtil.getInstance());
		bindings.put("log", LoggerUtil.getInstance());
		
		scriptContext = new SimpleScriptContext();
		scriptContext.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
		scriptEngine.setContext(scriptContext);
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			InputStream inputStream = cl.getResourceAsStream(config_package + "json_util.js");
			CompiledScript compiledScript = scriptEngine.compile(
					IOUtils.toString(inputStream, DEFAULT_CHARSET));
			compiledScript.eval(scriptContext);
			inputStream.close();
			
			inputStream = cl.getResourceAsStream(config_package + "core.js");
			compiledScript = scriptEngine.compile(
					IOUtils.toString(inputStream, DEFAULT_CHARSET));
			compiledScript.eval(scriptContext);
			inputStream.close();
			
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	@Override
	public Object runScript(String script) {
		try {
			Object result = scriptEngine.eval(script, scriptContext);
			return result;
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
	
	@Override
	public String runScript2JSON(String script) {
		try {
			Object result = scriptEngine.eval(script, scriptContext);			
			if(result instanceof ScriptObjectMirror) {
				// GSON does not support ScriptObjectMirror jsonSerializer well, it converts Array to Map with int-keys.
				// so need transform.
				ScriptObjectMirror objectMirror = (ScriptObjectMirror)result;
				transform(objectMirror);
				
				return BaseUtils.GSON.toJson(result);
			} else {
				Object jsonObject = scriptEngine.invokeFunction("$$stringify", result);
				return jsonObject.toString();
			}			
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
	
	private void transform(ScriptObjectMirror objectMirror) {
		Iterator<String> itr = objectMirror.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			Object value = objectMirror.get(key);			
			if(value instanceof ScriptObjectMirror) {
				ScriptObjectMirror field = (ScriptObjectMirror)value;
				if(field.isArray()) {
					objectMirror.put(key, field.values());  // return Array for GSON
				}
				transform(field); // we assume that the depth of recursion is good
			}
		}
	}
	
	/**
	 * secutiry filter by className
	 * @author Jack
	 *
	 */
	static class SecurityFilter {
		
		public void filter(String className) {
			// TODO
		}
	}

}
