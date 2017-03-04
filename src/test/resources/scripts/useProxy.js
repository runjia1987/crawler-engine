"use strict";

var rs = { rs:[], status: [] };
var url = args.url;
var charset = args.charset;

// setProxy(args, "127.0.0.1", "23001");
log.info("url: " + url);

var content = http.get(url, args);
var doc = jsoup.parse(content)

var elements = doc.select("div.product-intro div.itemInfo-wrap div.sku-name")
rs.rs.push( elements.get(0).text() )
rs.status.push('SUCCESS')

return rs;