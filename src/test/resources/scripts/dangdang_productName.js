"use strict";

var content = args.content;
    var rs = {rs:[], status:[]};
    var url = args.url;
    var content = http.get(url, args)

    var doc = jsoup.parse(content)    
    
    var element = doc.select("div.show_info div.sale_box div.name_info h1")
    var product_name = element.attr('title');
    
    rs.rs.push(product_name)
	log.info("rs.rs length: " + rs.rs.length)
    rs.status.push("SUCCESS")
				
    return rs;