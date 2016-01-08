define(function(require, exports, module) {
	/**
	 * This module holds all the methods to query the REST api v2
	 * 
	 * @module RestClientV2
	 */

	'use strict';
	var $ = require('jquery')
	var rsqlQuery = require('/modules/RsqlQuery');

	var apiBaseUri = '/api/v2/';

	/**
	 * @memberOf RestClientV2
	 */
	exports.RestClientV2.prototype.get = function(resourceUri, options) {
		if (!resourceUri.startsWith('/api/')) {
			// assume that resourceUri is a entity name
			resourceUri = apiBaseUri + htmlEscape(resourceUri);
		}

		var qs;
		if (options) {
			var items = [];

			if (options.q) {
				if (options.q.length > 0) {
					items.push('q=' + rsqlQuery.createRsqlQuery(options.q));
				}
			}
			if (options.aggs) {
				items.push('aggs=' + rsqlQuery.createRsqlAggregateQuery(options.aggs));
			}
			if (options.attrs) {
				items.push('attrs=' + rsqlQuery.createAttrsValue(options.attrs));
			}
			if (options.sort) {
				items.push('sort=' + rsqlQuery.createSortValue(options.sort));
			}
			if (options.start !== undefined) {
				items.push('start=' + options.start);
			}
			if (options.num !== undefined) {
				items.push('num=' + options.num);
			}
			qs = items.join('&');
		} else {
			qs = null;
		}

		if ((qs ? resourceUri + '?' + qs : resourceUri).length < 2048) {
			return $.ajax({
				method : 'GET',
				url : qs ? resourceUri + '?' + qs : resourceUri,
				dataType : 'json',
				cache : true
			});
		} else {
			// keep URLs under 2048 chars: http://stackoverflow.com/a/417184
			// tunnel GET request through POST
			return $.ajax({
				method : 'POST',
				url : resourceUri + '?_method=GET',
				dataType : 'json',
				contentType : 'application/x-www-form-urlencoded',
				data : qs,
				cache : true
			});
		}
	};

	/**
	 * @memberOf RestClientV2
	 */
	exports.RestClientV2.prototype.remove = function(name, id) {
		return $.ajax({
			type : 'DELETE',
			url : apiBaseUri + encodeURI(name) + '/' + encodeURI(id)
		});
	};

	exports.module = apiBaseUri;
});