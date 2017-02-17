package org.jackJew.biz.engine.test;

import org.jackJew.biz.engine.HttpEngineAdapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class HttpEngineAdapterTest {

	@Test
	public void testGet() throws Exception {
		String url = "https://item.jd.com/1595652727.html";
		HttpEngineAdapter httpEngineAdapter = HttpEngineAdapter.getInstance();
		String content = httpEngineAdapter.get(url, null, null).getText();
		
		Document document = Jsoup.parse(content);
		String text = document.select("div.itemInfo-wrap div.sku-name").get(0).text();
		System.out.println(text);
	}

}
