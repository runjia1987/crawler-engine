"use strict";

var rs = { rs:[], status:[] };
	var url = args.url;
	args.charset = "GB2312";
	args.headers = {};
	args.headers["Referer"] = url;
	
	log.info(url + "," + args)
	var shtml = http.get(url, args);
	
	var baseUri = getBaseUri(url);
	log.info("baseUri: " + baseUri);
	
	var doc = jsoup.parse(shtml);
	var href_list = doc.select("a[href]");
	var index = 0, count = 0
	for (;index < href_list.size(); index++) {
		var href = href_list.get(index).attr("href").toString()
		if(href.startsWith('http://shop.dangdang.com/')) {
			rs.rs.push(href.replaceAll("\n|\r||\t", ""));
			count++
			if(count >= 50) {
				break;
			}
		}
	}
	rs.status.push('SUCCESS');
	return rs;
	
	function getBaseUri(url) {		
		var lastIndex = url.indexOf("dangdang.com");
		return url.substring(0, lastIndex) + "dangdang.com";
	}