package org.jackJew.biz.engine.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.jackJew.biz.task.Constants;

/**
 * property reader
 * @author Jack
 *
 */
public class PropertyReader {
	
	private final static Properties props = new Properties();
	
	static {
		props.putAll(resolve("config.properties"));
	}

	private PropertyReader() {
	}
	
	public static String getProperty(String key) {
		return props.getProperty(key);
	}

	/**
	 * get all key:value mappings from the specified file
	 * 
	 * @param fileName
	 * @return
	 */
	private static Properties resolve(String fileName) {		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try(InputStream ins = cl.getResourceAsStream(fileName);
			InputStreamReader insr = new InputStreamReader(ins, Constants.CHARSET);) {
			Properties props = new Properties();
			props.load(insr);
			return props;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
