(function() {
	var rs = {rs: [], status: []};
    log.info(rs.rs.length)
    
    rs.rs.push('abc')
    rs.rs.push('def')
    rs.rs.push('ghi')
    
    rs.status.push('SUCCESS')
    
    return rs;
})();