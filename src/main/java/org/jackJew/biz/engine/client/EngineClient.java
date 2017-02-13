package org.jackJew.biz.engine.client;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jackJew.biz.engine.util.BaseUtils;
import org.jackJew.biz.engine.util.PropertyReader;
import org.jackJew.biz.task.Constants;
import org.jackJew.biz.task.Reply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;

/**
 * receive messages from rabbitMQ. run in each JVM process.
 * @author Jack
 *
 */
public class EngineClient {
	
	private final static Logger logger = LoggerFactory.getLogger(EngineClient.class);
	public static String CLIENT_NAME;
	
	private final static int threadPoolSize = Integer.valueOf(PropertyReader.getProperty("threadPoolSize"));
	public static final ThreadPoolExecutor pool = new ThreadPoolExecutor(threadPoolSize, threadPoolSize,
			0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
	
	public final static String mqFactoryUri = PropertyReader.getProperty("mqFactoryUri");	
	private final static String queueName = PropertyReader.getProperty("queueName");	
	final static ConnectionFactory connectionFactory = new ConnectionFactory();	

	public static void main(String[] args) throws Exception {
		if(args == null || args.length == 0) {
			System.err.println("clientName is required.");
			System.exit(1);
		}
		CLIENT_NAME = args[0];
		logger.info(CLIENT_NAME + " is starting... on queue " + queueName);
		
		connectionFactory.setUri(mqFactoryUri);
		connectionFactory.setAutomaticRecoveryEnabled(true);
		connectionFactory.setTopologyRecoveryEnabled(true);
		// since amqp-client v4.0, default is true. now is v3.6. setTopologyRecoveryEnabled enables exchanges/queue/bindings/consumers auto recovery
		
		Connection conn = connectionFactory.newConnection(pool);
		Channel channel = conn.createChannel();
		channel.basicQos(threadPoolSize, false);
		
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
	         public void handleDelivery(String consumerTag, Envelope envelope,
	                 AMQP.BasicProperties properties, byte[] body)  throws IOException {
	             new Task(body).process();
	         }

			 @Override
			 public void handleCancel(String consumerTag) throws IOException {
			 	logger.error(CLIENT_NAME + " consumer on queue " + queueName + " get cancel signal.");
			 }
	     };
		channel.basicConsume(queueName, true, consumer);
		// add hook when process exits or is interrupted.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        	try {
				conn.close();
				if(MessagePushService.conn != null) {
					MessagePushService.conn.close();
				}
				if(BizScriptConnectionHolder.conn != null) {
					BizScriptConnectionHolder.conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
        	pool.shutdownNow();
        }));
        logger.info(CLIENT_NAME + " is started.");
	}
	
	static class MessagePushService {
		
		private final static String exchangeName = PropertyReader.getProperty("exchangeName");
		private final static String queueNameReply = PropertyReader.getProperty("queueNameReply");
		private static Connection conn;
		private final LinkedBlockingQueue<Reply> replyQueue = new LinkedBlockingQueue<>();
		
		static class MessagePushServiceHolder {
			// avoid unnecessary initialziation until that we really need MQ connection.
			private final static MessagePushService instance = new MessagePushService();
		}		
		
		public static MessagePushService getInstance() {
			return MessagePushServiceHolder.instance;
		}
		
		private MessagePushService() {
			try {
				conn = connectionFactory.newConnection();
				Channel channel = conn.createChannel();
				Thread publishThread = new Thread(() -> {
					while(true) {
						try {
							Reply reply = replyQueue.take();
							if(reply == null) {
								Thread.sleep(2000);							
							} else {
								channel.basicPublish(exchangeName, queueNameReply, MessageProperties.BASIC,
										BaseUtils.GSON.toJson(reply).getBytes(Constants.CHARSET));
							}
						} catch(Exception e) {
						}						
					}
				}, "publishThread");
				publishThread.setDaemon(true);
				publishThread.start();
			} catch (Exception e) {
				logger.error(CLIENT_NAME, BaseUtils.getSimpleExMsg(e));
			}
		}
		
		/**
		 * submit reply message
		 */
		public void submit(Reply reply) {
			replyQueue.offer(reply);
		}
	}
	
	static class BizScriptConnectionHolder {
		static Connection conn;  // static but initialized in constructor, to avoid unnecessarily
		// initialziation except until that we really need MQ connection.
	}
	
}