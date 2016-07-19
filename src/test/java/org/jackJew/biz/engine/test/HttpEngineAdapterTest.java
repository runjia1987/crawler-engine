package org.jackJew.biz.engine.test;

import org.jackJew.biz.engine.HttpEngineAdapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class HttpEngineAdapterTest {

	@Test
	public void testGet() throws Exception {
		String url = "http://item.jd.com/1603975392.html";
		HttpEngineAdapter httpEngineAdapter = HttpEngineAdapter.getInstance();
		String content = httpEngineAdapter.get(url, null, null).getText();
		
		Document document = Jsoup.parse(content);
		String text = document.select("div#itemInfo div#name h1").get(0).html();
		System.out.println(text);
	}

}
