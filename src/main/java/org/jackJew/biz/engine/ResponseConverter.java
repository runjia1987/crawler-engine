package org.jackJew.biz.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.jackJew.biz.engine.util.BaseUtils;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter for http response with charset detection, based on mozilla jchardet.
 * 
 */
public class ResponseConverter {
	
	private final static Logger logger = LoggerFactory.getLogger(ResponseConverter.class);

	private String charset;
	private int statusCode;
	private Map<String, String> headers;
	private byte[] bytes;

	private String detectAndResolve(InputStream ins) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			nsDetector nsDetector = new nsDetector(nsPSMDetector.CHINESE);
			byte[] buffer = new byte[512];
			int len = 0;
			boolean found = false;
			boolean isAscii = true;			
			CharsetObserverNotify charsetObserverNotify = new CharsetObserverNotify();
			nsDetector.Init(charsetObserverNotify);
			
			while ((len = ins.read(buffer)) != -1) {
				baos.write(buffer, 0, len);

				// Check if the stream is only ascii.
				if (isAscii) {
					isAscii = nsDetector.isAscii(buffer, len);
				}

				// DoIt if non-ascii and not found yet.
				if (!isAscii && !found) {
					found = nsDetector.DoIt(buffer, len, false);
				}
			}
			nsDetector.DataEnd();
			buffer = null;
			
			if (isAscii) {
				charsetObserverNotify.found = true;
			}
			if (!charsetObserverNotify.found) {
				String probCharsets[] = nsDetector.getProbableCharsets();
				if (probCharsets.length > 0) {
					charsetObserverNotify.charset = probCharsets[0];
				}
			}
			charset = charsetObserverNotify.charset;
			if (BaseUtils.isEmpty(charset)) {
				return new String(baos.toByteArray());
			} else {				
				return new String(baos.toByteArray(), charset);
			}
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
	
	public String getText() {
		if (bytes == null) {
			return null;
		}
		String text = null;
		if (BaseUtils.isEmpty(charset)) {
			// if no charset is provided, then call jchardet.
			text = detectAndResolve(new ByteArrayInputStream(bytes));
		} else {
			try {
				text = new String(bytes, charset);
			} catch (UnsupportedEncodingException e) {
				// charset is wrong, then call jchardet.
				text = detectAndResolve(new ByteArrayInputStream(bytes));
			}
		}
		return text;
	}

	final class CharsetObserverNotify implements nsICharsetDetectionObserver {

		public boolean found = false;
		public String charset = null;

		@Override
		public void Notify(String charset) {
			this.found = true;
			this.charset = charset;
		}
	}
	
	void setCharset(String charset) {
		this.charset = charset;
	}
	
	void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	Map<String, String> getHeaders() {
		return headers;
	}

	void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	int getStatusCode() {
		return statusCode;
	}
}
