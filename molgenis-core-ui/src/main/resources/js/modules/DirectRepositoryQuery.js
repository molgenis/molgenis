define(function(require, exports, module) {
	/**
	 * @module DirectRepositoryQuery
	 */
	
	"use strict";
	var $ = require('jquery');

	/**
	 * 
	 */
	exports.prototype.setSearchboxClickHandler = function(searchbox, button, dataset, dataexplorer) {
		$(function() {
			$(button).on('click', function() {
				var queryValue = $(searchbox).val();
				window.location = dataexplorer + "?" + $.param({
					entity : dataset,
					searchTerm : queryValue
				});
			});
		});
	}
});
