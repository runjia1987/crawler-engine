"use strict";

var $$stringify = function(obj) {
    return JSON.stringify(obj);
};
var sleep = function(millis) {
    $$system.sleep(millis);
};

var vEx = org.jackJew.biz.engine.util.ValidationException
var hashmap = java.util.HashMap
var jsoup = org.jsoup.Jsoup

var setProxy = function(args, proxyHost, proxyPort) {
	args.proxyHost = proxyHost;
	args.proxyPort = proxyPort;
};

var http = {
	get: function(url, args) {
	    return this.request(url, args).getText();
	},
	post: function(url, args, params) {
	    return this.request(url, args, params).getText();
	},
    request: function(url, args, params) {
        var config = new hashmap();
        if (args) {
            for (var c in args) {
                if (c == "headers") continue;
                config.put(c, args[c].toString());
            }
        }
        var headers = new hashmap();
        if (args && args.headers) {
            for (var h in args.headers) {
                headers.put(h, args.headers[h].toString());
            }
        }
        if (params) {
            var body = new hashmap();
            if (params) {
                for (var arg in params) {
                	body.put(arg, params[arg].toString());
                }
            }
            return $$http.post(url, config, headers, body);
        } else {
            return $$http.get(url, config, headers);
        }
    }    
}