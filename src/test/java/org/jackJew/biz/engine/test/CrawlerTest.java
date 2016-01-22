package org.jackJew.biz.engine.test;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jackJew.biz.engine.JsEngine;
import org.jackJew.biz.engine.JsEngineRhino;
import org.junit.Test;

import com.google.gson.JsonObject;

public class CrawlerTest {

	@Test
	public void testCrawl() {
		long start = System.currentTimeMillis();
		String scriptFile = "org/jackJew/biz/engine/test/config/dd_shop.js";
		String url = "http://category.dangdang.com/cid4001867.html";
		
		try (InputStream ins =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(scriptFile);){
			String script = IOUtils.toString(ins, "UTF-8");
			JsonObject config = new JsonObject();
			config.addProperty("url", url);
			
			String runningScript = 
					String.format("(function(args){%s})(%s);", script, config.toString());
			
			JsEngine jsEngine = JsEngineRhino.getInstance();
			String result = jsEngine.runScript2JSON(runningScript);				
			System.out.println(result);
			System.out.println("total time cost: " + (System.currentTimeMillis() - start) + " ms.");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
