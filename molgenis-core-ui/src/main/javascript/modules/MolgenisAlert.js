define(function(require, exports, module) {
	/**
	 * @module MolgenisAlert
	 */

	'use strict';

	var $ = require('jquery');
	
	/**
	 * Generates an alert on the page
	 * 
	 * @memberOf MolgenisAlert
	 */
	exports.createAlert = function(alerts, type, container) {
		if (type !== 'error' && type !== 'warning' && type !== 'success' && type !== 'info') {
			type = 'error';
		}

		if (container === undefined) {
			container = $('.alerts');
			container.empty();
		}

		var items = [];
		items.push('<div class="alert alert-');
		// backwards compatibility
		items.push(type === 'error' ? 'danger' : type);
		items.push('"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>');
		items.push(type.charAt(0).toUpperCase() + type.slice(1));
		items.push('!</strong> ');
		$.each(alerts, function(i, alert) {
			if (i > 0) {
				items.push('<br/>');
			}
			items.push('<span>' + alert.message + '</span>');
		});
		items.push('</div>');

		container.prepend(items.join(''));
	};
});