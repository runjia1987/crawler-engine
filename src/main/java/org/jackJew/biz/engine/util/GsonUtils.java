package org.jackJew.biz.engine.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {
	
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	
	public static <T> T fromJson(String jsonContent, Class<T> cls) {
		return GSON.fromJson(jsonContent, cls);
	}
	
	public static String toJson(Object obj) {
		return GSON.toJson(obj);
	}

}
