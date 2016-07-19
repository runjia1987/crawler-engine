package org.jackJew.biz.engine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jackJew.biz.engine.util.BaseUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JS script engine, based on Mozilla Rhino.
 * @author Jack
 * 
 */
public class JsEngineRhino implements JsEngine {
	
	private final static Logger logger = LoggerFactory.getLogger(JsEngineRhino.class);

	private final static Set<String> classWhiteList = new HashSet<String>();
	private final static Set<String> packageWhiteList = new HashSet<String>();
	
	private final static ContextFactory globalContextFactory;
	private final ScriptableObject sharedScriptObject;
	
	private final static String config_package = "org/jackJew/biz/engine/config/rhino/";
	
	public static JsEngineRhino getInstance() {
		return JsEngineRhinoHolder.JS_ENGINE_RHINO;
	}
	
	private static class JsEngineRhinoHolder {
		private final static JsEngineRhino JS_ENGINE_RHINO = new JsEngineRhino();
	}
	
	static {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String line;
		try(InputStream inputStream = cl.getResourceAsStream(config_package + "classWhiteList");
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			) {
			while ((line = br.readLine()) != null) {
				String className = line.trim();
				logger.info("class: " + className);
				if (!className.isEmpty()) {
					classWhiteList.add(className);
				}
			}
		} catch (Exception ex) {
			logger.error("", ex);
		}
		try(InputStream inputStream = cl.getResourceAsStream(config_package + "packageWhiteList");
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			) {
			while ((line = br.readLine()) != null) {
				String packageName = line.trim();
				logger.info("package: " + packageName);
				if (!packageName.isEmpty()) {
					packageWhiteList.add(packageName);
				}
			}
		} catch (Exception ex) {
			logger.error("", ex);
		}

		globalContextFactory = new ContextFactory() {
			@Override
			protected Context makeContext() {
				Context context = new Context();
				context.setClassShutter((className) -> {
					if (classWhiteList.contains(className)) {
						return true;
					}
					for (String packageName : packageWhiteList) {
						if(className.startsWith(packageName)) {
							return true;
						}
					}
					return false;
				});
				context.setOptimizationLevel(5);
				// make Rhino runtime to call observeInstructionCount each 20K bytecode instructions
				context.setInstructionObserverThreshold(20000);
				return context;
			}
		};
		ContextFactory.initGlobal(globalContextFactory);
	}

	private JsEngineRhino() {
		sharedScriptObject = globalContextFactory.enterContext().initStandardObjects();
		try {	
			ScriptableObject.putConstProperty(sharedScriptObject, "$$http", HttpEngineAdapter.getInstance());
			ScriptableObject.putConstProperty(sharedScriptObject, "$$system", SystemUtil.getInstance());
			ScriptableObject.putConstProperty(sharedScriptObject, "log", LoggerUtil.getInstance());
			
			final ClassLoader cl = Thread.currentThread().getContextClassLoader();
			InputStream inputStream = cl.getResourceAsStream(config_package + "json_util.js");
			runScript(sharedScriptObject, IOUtils.toString(inputStream, DEFAULT_CHARSET));
			inputStream.close();
			
			inputStream = cl.getResourceAsStream(config_package + "core.js");
			runScript(sharedScriptObject, IOUtils.toString(inputStream, DEFAULT_CHARSET));
			inputStream.close();
			
		} catch (Exception ex) {
			logger.error("", ex);
		} finally {
			Context.exit();
		}
	}

	private Object runScript(Scriptable scriptObject, String script) throws Exception {
		if (BaseUtils.isEmpty(script)) {
			return null;
		}
		Context context = globalContextFactory.enterContext();
		try {
			return context.evaluateString(scriptObject, script, "<cmd>", 0, null);
		} catch (Exception ex) {
			throw ex;
		} finally {
			Context.exit();
		}		
	}

	@Override
	public String runScript2JSON(String script) throws Exception {
		try {
			Object rawResult = runScript(script);
			return BaseUtils.GSON.toJson(rawResult);
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public Object runScript(String script) throws Exception {
		return runScript(sharedScriptObject, script);
	}
}
