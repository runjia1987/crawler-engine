package org.jackJew.biz.engine.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
	
	private final static int threadPoolSize = Integer.valueOf(PropertyReader.getProperty("threadPoolSize"));
	public final static String mqFactoryUri = PropertyReader.getProperty("mqFactoryUri");
	
	private final static String queueName = PropertyReader.getProperty("queueName");
	
	public static String CLIENT_NAME;
	
	final static ConnectionFactory connectionFactory = new ConnectionFactory();
	
	public static final ThreadPoolExecutor pool = new ThreadPoolExecutor(threadPoolSize, threadPoolSize,
			0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

	public static void main(String[] args) throws Exception {
		if(args == null || args.length == 0) {
			System.err.println("clientName is required.");
			System.exit(1);
		}
		CLIENT_NAME = args[0];
		logger.info(CLIENT_NAME + " is starting... on queue " + queueName);
		
		connectionFactory.setUri(mqFactoryUri);
		connectionFactory.setAutomaticRecoveryEnabled(true);
		Connection conn = connectionFactory.newConnection();
		Channel channel = conn.createChannel();
		channel.basicQos(threadPoolSize * 2, true);
		
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
	         public void handleDelivery(String consumerTag, Envelope envelope,
	                 AMQP.BasicProperties properties, byte[] body)  throws IOException {
	             pool.submit(new Task(body));
	         }

			 @Override
			 public void handleCancel(String consumerTag) throws IOException {
			 	logger.error(CLIENT_NAME + " consumer on queue " + queueName + " get canceled signal.");
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
		
		private final static int initialChannelsPoolSize = Integer.valueOf(PropertyReader.getProperty("initialChannelsPoolSize"));
		private final static int channelsPoolSize = Integer.valueOf(PropertyReader.getProperty("channelsPoolSize"));
		
		private final List<Channel> channelsPool = new ArrayList<>(initialChannelsPoolSize);
		private int channelsCount;
		private final Random random = new Random(System.currentTimeMillis());
		private static Connection conn;  // static but initialized in constructor, to avoid unnecessarily
		// initialziation except until that we really need MQ connection.
		
		private final static MessagePushService instance = new MessagePushService();
		
		public static MessagePushService getInstance() {
			return instance;
		}
		
		private MessagePushService() {
			try {
				conn = connectionFactory.newConnection();
			} catch (Exception e) {
				logger.error(CLIENT_NAME, BaseUtils.getSimpleExMsg(e));
			}
			if(conn != null) {
				int i = 0;
				while(i++ < initialChannelsPoolSize) {
					try {
						Channel channel = conn.createChannel();
						channelsPool.add(channel);
					} catch(Exception e) {
						logger.error(CLIENT_NAME, BaseUtils.getSimpleExMsg(e));
					}
				}
				channelsCount = channelsPool.size();
			}
		}
		
		/**
		 * publish reply message
		 */
		public void publish(Reply reply) {
			if(channelsCount == 0 || conn == null) {
				return;
			}
			if(pool.getQueue().size() > threadPoolSize && channelsCount != channelsPoolSize) {
				// expand channelsPool
				int i = 0;
				synchronized(channelsPool) {
					while(i++ < channelsPoolSize - channelsCount) {
						try {
							Channel channel = conn.createChannel();
							channelsPool.add(channel);
						} catch(Exception e) {
							logger.error(CLIENT_NAME, BaseUtils.getSimpleExMsg(e));
						}
					}
					channelsCount = channelsPool.size();
				}
			}
			int randomPos = random.nextInt(channelsCount);
			try {
				channelsPool.get(randomPos).basicPublish(exchangeName, queueNameReply, MessageProperties.BASIC,
						BaseUtils.GSON.toJson(reply).getBytes(Constants.CHARSET));
			} catch (Exception e) {
				logger.error(CLIENT_NAME, BaseUtils.getSimpleExMsg(e));
			}
		}		
	}
	
	static class BizScriptConnectionHolder {
		static Connection conn;  // static but initialized in constructor, to avoid unnecessarily
		// initialziation except until that we really need MQ connection.
	}
}