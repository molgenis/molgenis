define(function(require, exports, module) {
	/**
	 * This module contains functions to hide or show the spinner
	 * 
	 * @module Spinner
	 */

	'use strict';

	var $ = require('jquery');

	/**
	 * @memberOf Spinner
	 */
	exports.prototype.showSpinner = function(callback) {
		var spinner = $('#spinner');
		var login = $('#login-modal');

		if (spinner.length === 0) {
			// do not add fade effect on modal:
			// http://stackoverflow.com/a/22101894
			var items = [];
			items.push('<div class="modal" id="spinner" aria-labelledby="spinner-modal-label" aria-hidden="true">');
			items.push('<div class="modal-dialog modal-sm">');
			items.push('<div class="modal-content">');
			items.push('<div class="modal-header"><h4 class="modal-title" id="spinner-modal-label">Loading ...</h4></div>');
			items.push('<div class="modal-body"><div class="modal-body-inner"><img src="/img/waiting-spinner.gif"></div></div>');
			items.push('</div>');
			items.push('</div>');

			$('body').append(items.join(''));
			spinner = $('#spinner');
			spinner.data('count', 0);
			spinner.modal({
				backdrop : 'static',
				show : false
			});
		}

		if (callback) {
			spinner.on('shown.bs.modal', function(e) {
				callback();
			});
		}

		var count = $('#spinner').data('count');
		if (count === 0) {
			var timeout = setTimeout(function() {
				spinner.modal('show');
			}, 500);
			$('#spinner').data('timeout', timeout);
			$('#spinner').data('count', 1);
		} else {
			$('#spinner').data('count', count + 1);
		}

		if (login.length > 0) {
			hideSpinner();
		}
	}

	/**
	 * @memberOf Spinner
	 */
	exports.prototype.hideSpinner = function() {
		if ($('#spinner').length !== 0) {
			var count = $('#spinner').data('count');
			if (count === 1) {
				clearTimeout($('#spinner').data('timeout'));
				$('#spinner').modal('hide');
			}
			if (count > 0) {
				$('#spinner').data('count', count - 1);
			}
		}
	}
});