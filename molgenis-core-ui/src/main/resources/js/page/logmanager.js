define(function(require, exports, module) {
	/**
	 * @page logmanager
	 */
	"use strict";

	var $ = require('jquery');
	var molgenis = require('molgenis');

	var contextUrl = molgenis.contextUrl;

	function renderLoggerTable() {
		window.location = contextUrl;
	}

	$(function() {
		var container = $("#plugin-container");

		$('.log-level-select', container).select2();

		$(container).on('change', '.log-level-select', function(e) {
			var logger = $(this).closest('tr').data('logger');
			var level = $(this).val();
			$.post(contextUrl + '/logger/' + logger + '/' + level);
		})

		$(container).on('click', '#create-logger-btn', function() {
			$.post(contextUrl + '/loggers/reset', function() {
				window.location = contextUrl;
			});
		});

		$(container).on('click', '#reset-loggers-btn', function() {
			$.post(contextUrl + '/loggers/reset').success(function() {
				renderLoggerTable();
			});
		});

		$('form[name="create-logger-form"]').submit(function(e) {
			e.preventDefault();
			if ($(this).valid()) {
				var name = $('#logger-name').val();
				$.post(contextUrl + '/logger/' + name + '/DEBUG').success(function() {
					renderLoggerTable();
				});
			}
		});
	});
});