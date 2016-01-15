package org.jackJew.biz.engine.client;

import java.util.Map;

import org.jackJew.biz.engine.HttpEngineAdapter;
import org.jackJew.biz.engine.JsEngine;
import org.jackJew.biz.engine.JsEngineRhino;
import org.jackJew.biz.engine.client.EngineClient.MessagePushService;
import org.jackJew.biz.engine.util.BaseUtils;
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
			String argsStr = argsObject.toString();
			scriptsBuffer.append(script).append("})(").append(argsStr).append(")");
			
			JsEngine jsEngine = JsEngineRhino.getInstance();
			String result = jsEngine.runScript2JSON(scriptsBuffer.toString());
			logger.info(EngineClient.CLIENT_NAME + " - " + taskObject.getTaskId() + ", " + argsStr);
			
			// send reply
			Reply reply = new Reply(taskObject.getTaskId(), result, (byte) (BaseUtils.isEmpty(result) ? 0 : 1));
			MessagePushService.getInstance().publish(reply);
			
		} catch (Exception e) {
			logger.error(EngineClient.CLIENT_NAME, e);
		}
	}
	
}