/**************************************************************
 * This module holds all the methods to query the REST api v1 *
 *  														  *
 * @module RestClientV1										  *
 *************************************************************/
var $ = require('jquery');
var apiBaseUri = '/api/v1/';


exports.apiBaseUri = apiBaseUri;

/**
 * @memberOf RestClientV1
 */
exports.get = function(resourceUri, options) {
	return this._get(resourceUri, options, false);
};

/**
 * @memberOf RestClientV1
 */
exports.getAsync = function(resourceUri, options, callback) {
	return this._get(resourceUri, options, true, callback);
};

/**
 * @memberOf RestClientV1
 */
exports._get = function(resourceUri, options, async, callback) {
	var resource = null;

	var config = {
		'dataType' : 'json',
		'cache' : true,
		'async' : async
	};

	if (callback) {
		config['success'] = function(data) {
			callback(data);
		};
	} else if (async === false) {
		config['success'] = function(data) {
			resource = data;
		};
	}

	// tunnel get requests with options through a post,
	// because it might not fit in the URL
	if (options) {
		// backward compatibility for legacy code
		if (options.q && Object.toString.call(options.q) !== '[object Array]') {
			var obj = $.extend({}, options.q);
			delete options.q;
			for (var i = 0, keys = Object.keys(obj); i < keys.length; ++i) {
				options[keys[i]] = obj[keys[i]];
			}
		}

		var url = resourceUri;
		if (resourceUri.indexOf('?') == -1) {
			url = url + '?';
		} else {
			url = url + '&';
		}
		url = url + '_method=GET';

		$.extend(config, {
			'type' : 'POST',
			'url' : url,
			'data' : JSON.stringify(options),
			'contentType' : 'application/json'
		});
	} else {
		$.extend(config, {
			'type' : 'GET',
			'url' : resourceUri
		});
	}

	var promise = this._ajax(config);

	if (async === false)
		return resource;
	else
		return promise;
};

/**
 * @memberOf RestClientV1
 */
exports._ajax = function(config) {
	if (self.token) {
		$.extend(config, {
			headers : {
				'x-molgenis-token' : self.token
			}
		});
	}

	return $.ajax(config);
};

/**
 * @memberOf RestClientV1
 */
exports._toApiUri = function(resourceUri, options) {
	var qs = "";
	if (resourceUri.indexOf('?') != -1) {
		var uriParts = resourceUri.split('?');
		resourceUri = uriParts[0];
		qs = '?' + uriParts[1];
	}
	if (options && options.attributes && options.attributes.length > 0)
		qs += (qs.length === 0 ? '?' : '&') + 'attributes=' + encodeURIComponent(options.attributes.join(','));
	if (options && options.expand && options.expand.length > 0)
		qs += (qs.length === 0 ? '?' : '&') + 'expand=' + encodeURIComponent(options.expand.join(','));
	if (options && options.q)
		qs += (qs.length === 0 ? '?' : '&') + '_method=GET';
	return resourceUri + qs;
};

/**
 * @memberOf RestClientV1
 */
exports.getPrimaryKeyFromHref = function(href) {
	return href.substring(href.lastIndexOf('/') + 1);
};

/**
 * @memberOf RestClientV1
 */
exports.getHref = function(entityName, primaryKey) {
	return apiBaseUri + entityName + (primaryKey ? '/' + primaryKey : '');
};

/**
 * @memberOf RestClientV1
 */
exports.remove = function(href, callback) {
	return this._ajax({
		type : 'POST',
		url : href,
		data : '_method=DELETE',
		async : false,
		success : callback && callback.success ? callback.success : function() {
		},
		error : callback && callback.error ? callback.error : function() {
		}
	});
};

/**
 * @memberOf RestClientV1
 */
exports.update = function(href, entity, callback, showSpinner) {
	return this._ajax({
		type : 'POST',
		url : href + '?_method=PUT',
		contentType : 'application/json',
		data : JSON.stringify(entity),
		async : true,
		showSpinner : showSpinner,
		success : callback && callback.success ? callback.success : function() {
		},
		error : callback && callback.error ? callback.error : function() {
		}
	});
};

/**
 * @memberOf RestClientV1
 */
exports.entityExists = function(resourceUri) {
	var result = false;
	this._ajax({
		dataType : 'json',
		url : resourceUri + '/exist',
		async : false,
		success : function(exists) {
			result = exists;
		}
	});

	return result;
};

/**
 * @memberOf RestClientV1
 */
exports.login = function(username, password, callback) {
	$.ajax({
		type : 'POST',
		dataType : 'json',
		url : apiBaseUri + 'login',
		contentType : 'application/json',
		async : true,
		data : JSON.stringify({
			username : username,
			password : password
		}),
		success : function(loginResult) {
			self.token = loginResult.token;
			callback.success({
				username : loginResult.username,
				firstname : loginResult.firstname,
				lastname : loginResult.lastname
			});
		},
		error : callback.error
	});
};

/**
 * @memberOf RestClientV1
 */
exports.logout = function(callback) {
	this._ajax({
		url : apiBaseUri + 'logout',
		async : true,
		success : function() {
			self.token = null;
			callback();
		}
	});
};