package org.jackJew.biz.engine.test;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jackJew.biz.engine.HttpEngineAdapter;
import org.jackJew.biz.engine.JsEngine;
import org.jackJew.biz.engine.JsEngineNashorn;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.google.gson.JsonObject;

public class HttpEngineTest {

	@Test
	public void testGet() throws Exception {
		String url = "http://item.jd.com/1629572.html";
		HttpEngineAdapter httpClient = HttpEngineAdapter.getInstance();
		String content = httpClient.get(url, null, null).getText();
		
		Document document = Jsoup.parse(content);
		String text = document.select("div#itemInfo div#name h1").get(0).html();
		System.out.println(text);
	}
	
	/**
	 * run closure.js, closure wrapped in closure will not return any objects
	 */
	@Test
	public void testRunClosure() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try (InputStream ins = cl.getResourceAsStream("org/jackJew/biz/engine/test/config/closure_js.js");) {
			String script = IOUtils.toString(ins);			
			JsonObject config = new JsonObject();
			
			JsEngine jsEngine = JsEngineNashorn.getInstance();
			Object result = jsEngine.runScript(String.format("(function(args){%s})(%s);", script, config.toString()));
			
			if(result != null) {
				System.out.println(result.getClass());
			} else {
				System.out.println("testRunClosure result is null.");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
