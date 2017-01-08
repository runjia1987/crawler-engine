package org.jackJew.biz.engine.client;

import java.util.Map;

import org.jackJew.biz.engine.HttpEngineAdapter;
import org.jackJew.biz.engine.JsEngine;
import org.jackJew.biz.engine.JsEngineNashorn;
import org.jackJew.biz.engine.JsEngineRhino;
import org.jackJew.biz.engine.client.EngineClient.MessagePushService;
import org.jackJew.biz.engine.util.BaseUtils;
import org.jackJew.biz.engine.util.PropertyReader;
import org.jackJew.biz.task.Constants;
import org.jackJew.biz.task.Reply;
import org.jackJew.biz.task.TaskObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * task
 * @author Jack
 *
 */
public class Task implements Runnable {
	
	private final static Logger logger = LoggerFactory.getLogger(Task.class);	
	
	private byte[] body;
	
	private final static JsEngine JS_ENGINE;
	private final static String engineType = PropertyReader.getProperty("jsEngine");
	
	static {
		if("nashorn".equalsIgnoreCase(engineType)) {
			JS_ENGINE = JsEngineNashorn.getInstance();
		} else if("rhino".equalsIgnoreCase(engineType)) {
			JS_ENGINE = JsEngineRhino.getInstance();
		} else {
			throw new RuntimeException("no correct jsEngine is provided.");
		}
	}
	
	public Task(byte[] body) {
		this.body = body;
	}

	@Override
	public void run() {
		try {
			TaskObject taskObject = BaseUtils.GSON.fromJson(
						new String(body, Constants.CHARSET), TaskObject.class);			
			String script = BizScriptCacheService.getInstance().getScript(taskObject.getBizType());
			if(BaseUtils.isEmpty(script)) {
				return;
			}
			Map<String, String> args = taskObject.getArgs();
			JsonObject argsObject = new JsonObject();
			argsObject.addProperty(HttpEngineAdapter.CONFIG_BIZ_TYPE, taskObject.getBizType());
			if(!BaseUtils.isNullOrEmpty(args)) {
				for(Map.Entry<String, String> entry : args.entrySet()) {
					argsObject.addProperty(entry.getKey(), entry.getValue());
				}
			}
			// wrap script in closure
			StringBuilder scriptsBuffer = new StringBuilder("(function(args){");
			scriptsBuffer.append(script).append("})(").append(argsObject.toString()).append(")");
			
			String result = JS_ENGINE.runScript2JSON(scriptsBuffer.toString());
			logger.info(EngineClient.CLIENT_NAME + " - " + taskObject.getTaskId());
			
			// send reply
			Reply reply = new Reply(taskObject.getTaskId(), taskObject.getBizType(), result);
			MessagePushService.getInstance().submit(reply);
			
		} catch (Exception e) {
			logger.error(EngineClient.CLIENT_NAME, e);
		}
	}
	
}