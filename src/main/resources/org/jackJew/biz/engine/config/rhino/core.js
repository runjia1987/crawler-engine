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

var http = {
	get: function(url, config) {
	    return this.request(url, config).getText();
	},
	post: function(url, args, config) {
	    return this.request(url, config, args).getText();
	},
    request: function(url, config, args) {
        var _config = new hashmap();
        if (config) {
            for (var c in config) {
                if (c == "headers") continue;
                _config.put(c, config[c].toString());
            }
        }
        var headers = new hashmap();
        if (config && config.headers) {
            for (var h in config.headers) {
                headers.put(h, config.headers[h].toString());
            }
        }
        if (args) {
            var _params = new hashmap();
            if (args) {
                for (var arg in args) {
                    _params.put(arg, args[arg].toString());
                }
            }
            return $$http.post(url, _params, _config, headers);
        } else {
            return $$http.get(url, _config, headers);
        }
    }    
}

var setProxy = function(args, proxyHost, proxyPort) {
	args.proxyHost = proxyHost;
	args.proxyPort = proxyPort;
}