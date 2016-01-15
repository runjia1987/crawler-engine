package org.jackJew.biz.engine.test;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jackJew.biz.engine.JsEngine;
import org.jackJew.biz.engine.JsEngineNashorn;
import org.jackJew.biz.engine.JsEngineRhino;
import org.jackJew.biz.engine.util.BaseUtils;
import org.jackJew.biz.task.Constants;
import org.junit.Test;

import com.google.gson.JsonObject;

/**
 * test variable visibility of js evaluation in the same scriptContext.
 * @author Jack
 *
 */
public class PublicVarsVisibilityTest {
	
	@Test
	public void test() throws Exception {
		String scriptFile = "scripts/publicVars.js";
		InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(scriptFile);
		String script = IOUtils.toString(ins, Constants.CHARSET);
		IOUtils.closeQuietly(ins);
		
		JsEngine jsEngine = JsEngineNashorn.getInstance();
		JsonObject config = new JsonObject();
		try {
			String result = jsEngine.runScript2JSON(
					String.format("(function(args){%s})(%s);", script, config.toString()));
						
			result = jsEngine.runScript2JSON(
					String.format("(function(args){%s})(%s);", "return pub1;", config.toString()));
			System.out.println(result);  // 123, global variable will be exported.
			
		} catch (Exception ex) {
			System.out.println("catch exception. " + BaseUtils.getSimpleExMsg(ex));
		}
	}
	
	@Test
	public void testWithClosure() throws Exception {
		String scriptFile = "scripts/publicVarsInClosure.js";
		InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(scriptFile);
		String script = IOUtils.toString(ins, Constants.CHARSET);
		IOUtils.closeQuietly(ins);
		
		JsEngine jsEngine = JsEngineRhino.getInstance();
		JsonObject config = new JsonObject();
		try {
			String result = jsEngine.runScript2JSON(
					String.format("(function(args){%s})(%s);", script, config.toString()));
						
			result = jsEngine.runScript2JSON(
					String.format("(function(args){%s})(%s);", "return pub1;", config.toString()));
			System.out.println(result);  // 123, global variable will be exported.
			
		} catch (Exception ex) {
			System.out.println("catch exception. " + BaseUtils.getSimpleExMsg(ex));
		}
	}

}
