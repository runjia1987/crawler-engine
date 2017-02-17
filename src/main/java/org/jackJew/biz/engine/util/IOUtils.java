package org.jackJew.biz.engine.util;

import java.io.InputStream;

public class IOUtils {
	
	public static String toString(InputStream ins, String charset) throws Exception {
		StringBuilder stringBuilder = new StringBuilder(1 << 8);
		int read = -1;
		byte[] buff = new byte[1 << 8];
		while((read = ins.read(buff)) != -1) {
			stringBuilder.append(new String(buff, 0, read, charset));
		}
		return stringBuilder.toString();
	}
	
	public static void closeQuietly(InputStream ins) {
		try {
			ins.close();
		} catch(Exception e) {
			// swallow
		}
	}

}
