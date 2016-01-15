package org.jackJew.biz.engine.test;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jackJew.biz.engine.JsEngine;
import org.jackJew.biz.engine.JsEngineRhino;
import org.jackJew.biz.engine.util.BaseUtils;
import org.junit.Test;

import com.google.gson.JsonObject;

/**
 * testcase for proxy
 * 
 * @author jack.zhu
 * 
 */
public class UseJavaProxyTest {
	
	@Test
	public void testUse() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = cl.getResourceAsStream("scripts/useProxy.js");
		String script = IOUtils.toString(inputStream);
		IOUtils.closeQuietly(inputStream);
		
		System.out.println("Executing...");
		JsEngine jsEngine = JsEngineRhino.getInstance();

		String currentUrl = "http://item.jd.com/1031411.html";
		System.out.println("url: " + currentUrl);
		JsonObject config = new JsonObject();
		config.addProperty("url", currentUrl);
		config.addProperty("charset", "GBK");

		try {
			String result = jsEngine.runScript2JSON(
					String.format("(function(args){%s})(%s);", script, config.toString()));
			System.out.println(result);
		} catch (Exception ex) {
			System.out.println("catch exception. " + BaseUtils.getSimpleExMsg(ex));
		}
	}
}