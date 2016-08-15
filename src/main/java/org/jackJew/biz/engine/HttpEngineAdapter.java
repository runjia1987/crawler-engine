package org.jackJew.biz.engine;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.jackJew.biz.engine.util.BaseUtils;
import org.jackJew.biz.engine.util.PropertyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http adapter (safe singleton for concurrency usage), based on HttpClient 4.4.1 library.
 * 
 */
public class HttpEngineAdapter {

	private final static Logger logger = LoggerFactory.getLogger(HttpEngineAdapter.class);
	
	private static final int max_retry_times = 3;
	
	private final PoolingHttpClientConnectionManager connectionManager;
	
	public final static String CONFIG_KEY_USER_AGENT = "User-Agent";
	public static final String DEFAULT_USER_AGENT = PropertyReader.getProperty(CONFIG_KEY_USER_AGENT);	
	public final static int DEFAULT_TIMEOUT = 30000;
	public final static int LONG_TIMEOUT = 50000;
	
	public final static String CONFIG_KEY_PROXY_HOST = "proxyHost";
	public final static String CONFIG_KEY_PROXY_PORT = "proxyPort";	
	
	public final static String CONFIG_HEADER_CHARSET = "charset";
	public final static String CONFIG_BIZ_TYPE = "bizType";
	
	private final List<String> LONG_TIME_BIZ_TYPES = new ArrayList<String>();
	
	
	/**
	 * return singleton instance.
	 */
	public static HttpEngineAdapter getInstance() {
		return HttpAdapterHolder.HTTP_ENGINE_ADAPTER;
	}
	
	private static class HttpAdapterHolder {
		private static final HttpEngineAdapter HTTP_ENGINE_ADAPTER = new HttpEngineAdapter();
	}
	
	private HttpEngineAdapter() {
		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(500);
		connectionManager.setDefaultMaxPerRoute(500);
		
		String value = PropertyReader.getProperty("Long-Time-BizTypes");
		if(!BaseUtils.isEmpty(value)) {
			LONG_TIME_BIZ_TYPES.addAll(Arrays.asList(value.split(",")));
		}
	}

	private CloseableHttpClient createClient(Map<String, String> config) {
		if(config == null) {
			config = new HashMap<>();
		}		
		
		String bizType = config.remove(CONFIG_BIZ_TYPE);;
		int timeout = DEFAULT_TIMEOUT;
		if(!BaseUtils.isEmpty(bizType) && LONG_TIME_BIZ_TYPES.contains(bizType)) {
			timeout = LONG_TIMEOUT;
		}	
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.setConnectTimeout(timeout).build();

		String proxyHost = config.remove(CONFIG_KEY_PROXY_HOST), proxyPort = config.remove(CONFIG_KEY_PROXY_PORT);		
		String userAgent = config.get(CONFIG_KEY_USER_AGENT);
		if(BaseUtils.isEmpty(userAgent)) {
			userAgent = DEFAULT_USER_AGENT;
		}
		config.put(CONFIG_KEY_USER_AGENT, userAgent);
		
		HttpClientBuilder builder = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.setConnectionManagerShared(true)
				.addInterceptorFirst(new CustomHttpRequestInterceptor(config))
				.setDefaultRequestConfig(requestConfig)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
		
		if(!BaseUtils.isEmpty(proxyHost)) {
			builder.setProxy(new HttpHost(proxyHost, Integer.valueOf(proxyPort)));			
		}
		CloseableHttpClient httpClient = builder.build();
		return httpClient;
	}

	public ResponseConverter get(String url) throws Exception {
		return get(url, null, null);
	}

	public ResponseConverter get(String url, Map<String, String> config, Map<String, String> headers)
			throws Exception {
		if (BaseUtils.isEmpty(url)) {
			return null;
		}
		HttpGet get = new HttpGet(url);
		if (headers != null) {
			for (Entry<String, String> entry : headers.entrySet()) {
				get.addHeader(entry.getKey(), entry.getValue());
			}
		}
		Exception lastException = null;
		for (int i = 0; i < max_retry_times; i++) {
			try (CloseableHttpClient httpClient = createClient(config);
				 CloseableHttpResponse response = httpClient.execute(get);
				) {
				return convertResponse(response, config == null ? null : config.get(CONFIG_HEADER_CHARSET));
			} catch (Exception e) {
				lastException = e;
				logger.error("", e);				
				if (i == max_retry_times - 1) {
					logger.info("Retry failed.");
				} else {
					logger.info("Retrying request.");
				}
			}
		}
		if (lastException != null && lastException instanceof HttpHostConnectException) {
			throw lastException; // server connection fail
		}
		throw new HttpException("request fail " + url);
	}

	public ResponseConverter post(String url, Map<String, String> params) throws Exception {
		return post(url, null, null, params);
	}

	public ResponseConverter post(String url, Map<String, String> config,
			Map<String, String> headers, Map<String, String> params) throws Exception {
		if (BaseUtils.isEmpty(url)) {
			return null;
		}
		HttpPost post = new HttpPost(url);
		if (headers != null) {
			for (Entry<String, String> entry : headers.entrySet()) {
				post.addHeader(entry.getKey(), entry.getValue());
			}
		}
		// application/x-www-form-urlencoded data
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		if (params != null) {
			for (Entry<String, String> entry : params.entrySet()) {
				values.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		try {
			post.setEntity(new UrlEncodedFormEntity(values));
		} catch (UnsupportedEncodingException e) {
			// swallow
		}
		Exception lastException = null;
		for (int i = 0; i < max_retry_times; i++) {
			try (CloseableHttpClient httpClient = createClient(config);
				 CloseableHttpResponse response = httpClient.execute(post);
				) {
				return convertResponse(response, config == null ? null : config.get(CONFIG_HEADER_CHARSET));
			} catch (Exception e) {
				lastException = e;
				logger.error("", e);
				if (i == max_retry_times - 1) {
					logger.info("Retry failed.");
				} else {
					logger.info("Retrying request.");
				}
			}
		}
		if (lastException != null && lastException instanceof HttpHostConnectException) {
			throw lastException; // server connection fail
		}
		throw new HttpException("request fail " + url);
	}	
	
	private ResponseConverter convertResponse(CloseableHttpResponse response, String charset) {
		ResponseConverter responseConverter = new ResponseConverter();
		responseConverter.setCharset(charset);		
		try {
			final StatusLine status = response.getStatusLine();
			responseConverter.setStatusCode(status.getStatusCode());
			
			Header[] headers = response.getAllHeaders();
			if(headers != null) {
				Map<String, String> headersMap = new HashMap<String, String>(headers.length, 1);
				for (Header h : headers) {
					headersMap.put(h.getName(), h.getValue());
				}
				responseConverter.setHeaders(headersMap);
			}
			responseConverter.setBytes(BaseUtils.getBytes(response));
		} catch(Exception e) {
			// swallow
		}		
		return responseConverter;
	}

	/**
	 * custom HttpRequestInterceptor
	 * 
	 * @author jack.zhu
	 * 
	 */
	private final static class CustomHttpRequestInterceptor implements HttpRequestInterceptor {

		private Map<String, String> config;
		
		public CustomHttpRequestInterceptor(Map<String, String> config) {
			this.config = config;
		}

		@Override
		public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
			if (config == null)
				return;
			Iterator<Map.Entry<String, String>> itr = config.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, String> entry = itr.next();
				request.setHeader(entry.getKey(), entry.getValue());
			}
		}
	}
}
