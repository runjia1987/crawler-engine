package org.jackJew.biz.engine.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * utilities
 * @author Jack
 */
public class BaseUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(BaseUtils.class);
	
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	
	public static final DateTimeFormatter full_Formatter =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withLocale(Locale.getDefault())
			.withZone(ZoneId.systemDefault());
	
	public static boolean isNullOrEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}
	
	public static boolean isNullOrEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean isEmpty(String source) {
		return source == null || source.isEmpty();
	}
	
	public static String getSimpleExMsg(Exception e) {
		return e == null ? "" : (e.getClass().getName() + " - " + e.getMessage());
	}
	
	public static byte[] getBytes(CloseableHttpResponse response) {
		ByteArrayOutputStream bous = new ByteArrayOutputStream(1 << 15); // 32KB
		try(InputStream ins = response.getEntity().getContent();) {
			byte[] buffer = new byte[1 << 10];
			int len = 0;
			while ((len = ins.read(buffer)) > 0) {
				bous.write(buffer, 0, len);
			}
			buffer = null;
			return bous.toByteArray();

		} catch (Exception e) {
			logger.error("getBytes failure " + BaseUtils.getSimpleExMsg(e));
		}
		return null;
	}
	
	public static byte[] getBytes(CloseableHttpResponse response, int expectedSize) {
		ByteArrayOutputStream bous = new ByteArrayOutputStream(expectedSize);
		try(InputStream ins = response.getEntity().getContent();) {
			byte[] buffer = new byte[1 << 7];
			int len = 0;
			while ((len = ins.read(buffer)) > 0) {
				bous.write(buffer, 0, len);
			}
			buffer = null;
			return bous.toByteArray();

		} catch (Exception e) {
			logger.error("getBytes failure " + BaseUtils.getSimpleExMsg(e));
		}
		return null;
	}

	
}
