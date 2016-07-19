"use strict";

	var content = args.content;
    var rs = {rs:[], status:[]};
    var doc = jsoup.parse(content)
    var elements = doc.select("a[href]")
    for(var i = 0; i < elements.size(); i++) {
    	var element = elements.get(i);
    	var url = element.attr("href").toString()
    	if(url.startsWith("http://shop.dangdang.com/")) {
    		rs.rs.push(url.replaceAll("\r|\t|\n", ""));
    	}
    }
    if(rs.rs) {
    	log.info("rs.rs length: " + rs.rs.length)
    }
    rs.status.push("SUCCESS")
				
    return rs;