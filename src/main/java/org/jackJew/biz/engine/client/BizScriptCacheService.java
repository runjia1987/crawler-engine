package org.jackJew.biz.engine.client;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jackJew.biz.engine.HttpEngineAdapter;
import org.jackJew.biz.engine.client.EngineClient.BizScriptConnectionHolder;
import org.jackJew.biz.engine.util.BaseUtils;
import org.jackJew.biz.engine.util.PropertyReader;
import org.jackJew.biz.task.BizScript;
import org.jackJew.biz.task.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * local cache of scripts
 * @author Jack
 * 
 */
public class BizScriptCacheService {
	
	private final static Logger logger = LoggerFactory.getLogger(BizScriptCacheService.class);	
	
	private ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
	
	private final static String script_exchange = PropertyReader.getProperty("script_exchange");
	public final static String BIZ_SCRIPT_URL = PropertyReader.getProperty("biz_script_url");
	
	/**
	 * shared httpclient since the route is never changed.
	 */
	private final static CloseableHttpClient httpclient = HttpClients.custom()
				.setUserAgent(HttpEngineAdapter.DEFAULT_USER_AGENT).build();
	
	private final static String DEPRECATED = "000";
	
	private final static BizScriptCacheService instance = new BizScriptCacheService();
	
	private BizScriptCacheService() {
		try {
			BizScriptConnectionHolder.conn = EngineClient.connectionFactory.newConnection();
			Channel channel = BizScriptConnectionHolder.conn.createChannel();
			channel.exchangeDeclarePassive(script_exchange);
			// topic sub
			String queue = channel.queueDeclare().getQueue();
			Consumer consumer = new DefaultConsumer(channel) {
				@Override
		         public void handleDelivery(String consumerTag, Envelope envelope,
		                 AMQP.BasicProperties properties, byte[] body)  throws IOException {
		        	 logger.info("script update-msg received." );
		        	 try {
		        		 BizScript bizScript = BaseUtils.GSON.fromJson(new String(body, Constants.CHARSET), BizScript.class);
		        		 
			             final String bizType = bizScript.getBizType();
			             if(!BaseUtils.isEmpty(bizType)) {
			            	 if(bizScript.isDeleted()) {
				            	 cache.put(bizType, DEPRECATED);  // put flag to avoid retry getByHttp
				             } else if(!BaseUtils.isEmpty(bizScript.getScript())) {
				            	 cache.put(bizType, bizScript.getScript());
				 			}
			             }
		        	 } catch(Exception e) {
		        		 logger.error("", e);
		        	 }
		         }

				 @Override
				 public void handleCancel(String consumerTag) throws IOException {
				 	logger.error(EngineClient.CLIENT_NAME + " consumer on queue " + queue + " get unexpected cancel signal.");
				 }
			};
			
			channel.basicConsume(queue, true, consumer);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public static BizScriptCacheService getInstance() {
		return instance;
	}
	
	public String getScript(String bizType) {
		String script = cache.get(bizType);
		if(DEPRECATED.equals(script)) {
			return null;  // the bizType is deprecated.
		}
		if(BaseUtils.isEmpty(script)) {
			script = getByHttp(bizType);
			if(!BaseUtils.isEmpty(script)) {
				cache.put(bizType, script);
			}
		}
		return script;
	}
	
	/**
	 * get from remote http
	 */
	private String getByHttp(String bizType) {
		if(!BaseUtils.isEmpty(bizType)) {
			HttpGet get = new HttpGet(BIZ_SCRIPT_URL + bizType);
			try (CloseableHttpResponse response = httpclient.execute(get);){
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					byte[] bytes = BaseUtils.getBytes(response, 1 << 8);
					return new String(bytes, Constants.CHARSET);
				}
			} catch (Exception e) {
				logger.error(EngineClient.CLIENT_NAME, e);
			}
		}		
		return null;
	}

}
