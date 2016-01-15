"use strict";

var rs = { rs:[], status: [] };
var url = args.url;
var charset = args.charset;

// setProxy(args, "127.0.0.1", "23001");
log.info("url: " + url);

var content = http.get(url, args);

var doc = jsoup.parse(content)

rs.rs.push( doc.select("div#itemInfo div#name h1").get(0).text() )
rs.status.push('SUCCESS')

return rs;