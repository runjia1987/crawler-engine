package org.jackJew.biz.engine.test;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jackJew.biz.engine.JsEngine;
import org.jackJew.biz.engine.JsEngineNashorn;
import org.jackJew.biz.engine.JsEngineRhino;
import org.junit.Test;

import com.google.gson.JsonObject;

/**
 * when max is below 50, Rhino is better than Nashorn;
 * <br/>
 * when max is 100, Nashorn is better than Rhino.
 * @author Jack
 *
 */
public class JsEngineTest {
	
	private final int max = 20;

	@Test
	public void testNashornJS() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try (InputStream ins = cl.getResourceAsStream("site_source.html");) {
			String content = IOUtils.toString(ins);
			JsonObject config = new JsonObject();
			config.addProperty("content", content);
			
			JsEngine jsEngine = JsEngineNashorn.getInstance();
			InputStream inputStream = cl.getResourceAsStream("org/jackJew/biz/engine/test/config/test.js");			
			String script = IOUtils.toString(inputStream);
			IOUtils.closeQuietly(inputStream);
			
			long startTime = System.currentTimeMillis();
			int i = 0;
			while(i < max) {
				String result = jsEngine.runScript2JSON(
						String.format("(function(args){%s})(%s);", script, config.toString())
				);
				if(i++ == 0){
					System.out.println(result);
				}
			}			
			System.out.println("testNashornJS time cost: " + (System.currentTimeMillis() - startTime) + " ms.");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRhinoJS() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try (InputStream ins = cl.getResourceAsStream("site_source.html");) {
			String content = IOUtils.toString(ins);
			
			JsonObject config = new JsonObject();
			config.addProperty("content", content);
			
			JsEngine jsEngine = JsEngineRhino.getInstance();
			InputStream inputStream = cl.getResourceAsStream("org/jackJew/biz/engine/test/config/test.js");			
			String script = IOUtils.toString(inputStream);
			IOUtils.closeQuietly(inputStream);
			
			long startTime = System.currentTimeMillis();
			int i = 0;
			while(i < max) {
				String result = jsEngine.runScript2JSON(
						String.format("(function(args){%s})(%s);", script, config.toString()));
				if(i++ == 0){
					System.out.println(result);					
				}				
			}
			System.out.println("testRhinoJS time cost: " + (System.currentTimeMillis() - startTime) + " ms.");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
