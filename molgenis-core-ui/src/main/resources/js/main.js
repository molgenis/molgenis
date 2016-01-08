'use strict';

console.log('loading requirejs configuration...');

// We need this to get readable error messages
// Without this piece it will only show errors
// occurring in the require.js file
require.onError = function(error) {
	console.log('error:', error);
	throw error;
};

// Configuration for RequireJS
require.config({

	// Set a baseUrl to the /js/ folder, and let RequireJS know where our
	// libraries are with the paths option (compulsory when using jQuery as it
	// is a named module)
	baseUrl : '/js/',
	paths : {
		jquery : 'components/jquery/jquery-2.1.1.min',
		domready : 'components/requirejs/requirejs-domready',
		handlebars : 'components/handlebars/handlebars-v4.0.5',
		bootstrap : 'components/bootstrap/bootstrap.min',
		react : 'components/react/react-with-addons',
		underscore : 'components/underscore/underscore-min',
		validate : 'plugins/jquery.validate.min',
		molgenis : 'modules/MolgenisGlobalObject',
	},
	// Shim any scripts that do not have AMD support
	// This is common with most jQuery plugins
	shim : {
		underscore : {
			exports : '_'
		},
		bootstrap : [ 'jquery' ],
		domready : [ 'jquery' ],
		validate : [ 'jquery' ],
		'plugins/jQEditRangeSlider-min.js' : [ 'jquery' ],
		'plugins/jquery-sortable-min.js' : [ 'jquery' ],
		'plugins/jquery.bootstrap.pager.js' : [ 'jquery' ],
		'plugins/jquery.bootstrap.wizard.min.js' : [ 'jquery' ],
		'plugins/jquery.cookie-1.4.1.min.js' : [ 'jquery' ],
		'plugins/jquery.fancytree.min.js' : [ 'jquery' ],
		'plugins/jquery.form.min.js' : [ 'jquery' ],
		'plugins/select2-patched.js' : [ 'jquery' ],
		'plugins/bootstrap.file-input.js' : [ 'jquery', 'bootstrap' ],
		'plugins/bootstrap-datetimepicker.min.js' : [ 'jquery', 'bootstrap' ],
		'plugins/bootstrap-fileupload.min.js' : [ 'jquery', 'bootstrap' ]
	}
});

// Specify the Core libraries which are required, and also any modules that you
// might want to load and initialise on every page
define(function(require) {
	console.log('loading main dependencies...')
	
	// Create the molgenis object in the global scope
	// Used for backwards compatibility
	var molgenis = require('molgenis');
	var $ = require('jquery');
	var domReady = require('domready');
	var Handlebars = require('handlebars');
	var Bootstrap = require('bootstrap');
	var React = require('react');
	var _ = require('underscore');
	var validate = require('validate');

	domReady(function() {
		// Init common module code here
		// TODO common modules, like React components????
		console.log('All dependencies loaded and DOM ready!');

		/*
		 * Code that we should do something with
		 */

		// workaround for "Uncaught RangeError: Maximum call stack size
		// exceeded"
		// http://stackoverflow.com/a/19190216
//		$.fn.modal.Constructor.prototype.enforceFocus = function() {
//		};
//
//		/**
//		 * Add download functionality to JQuery. data can be string of
//		 * parameters or array/object
//		 * 
//		 * Default method is POST
//		 * 
//		 * Usage: <code>download('/localhost:8080',
//		 'param1=value1&param2=value2')</code>
//		 * Or: <code>download('/localhost:8080', {param1 : 'value1', param2 :
//		 'value2'})</code>
//		 * 
//		 */
//		$.download = function(url, data, method) {
//			if (!method) {
//				method = 'POST';
//			}
//
//			data = typeof data == 'string' ? data : $.param(data);
//
//			// split params into form inputs
//			var inputs = [];
//			$.each(data.split('&'), function() {
//				var pair = this.split('=');
//				inputs.push('<input type="hidden" name="' + pair[0] + '" value="' + pair[1] + '" />');
//			});
//
//			// send request and remove form from dom
//			$('<form action="' + url + '" method="' + method + '">').html(inputs.join('')).appendTo('body').submit().remove();
//		};
//
//		// serialize form as json object
//		$.fn.serializeObject = function() {
//			var o = {};
//			var a = this.serializeArray();
//			$.each(a, function() {
//				if (o[this.name] !== undefined) {
//					if (!o[this.name].push) {
//						o[this.name] = [ o[this.name] ];
//					}
//					o[this.name].push(this.value || '');
//				} else {
//					o[this.name] = this.value || '';
//				}
//			});
//			return o;
//		};
//
//		$(document).ajaxError(function(event, xhr, settings, e) {
//			if (xhr.status === 401) {
//				document.location = "/login";
//			}
//			try {
//				molgenis.createAlert(JSON.parse(xhr.responseText).errors);
//			} catch (e) {
//				molgenis.createAlert([ {
//					'message' : 'An error occurred. Please contact the administrator.'
//				} ], 'error');
//			}
//		});
//
//		window.onerror = function(msg, url, line) {
//			molgenis.createAlert([ {
//				'message' : 'An error occurred. Please contact the administrator.'
//			}, {
//				'message' : msg
//			} ], 'error');
//		};
//		
//		// use ajaxPrefilter instead of ajaxStart and ajaxStop
//		// to work around issue http://bugs.jquery.com/ticket/13680
//		$.ajaxPrefilter(function(options, _, jqXHR) {
//			if (options.showSpinner !== false) {
//				showSpinner();
//				jqXHR.always(hideSpinner);
//			}
//		});
//		
//		/**
//		 * Returns a promise with plugin settings
//		 */
//		molgenis.getPluginSettings = function() {
//			var api = new molgenis.RestClientV2();
//			return api.get('/api/v2/' + molgenis.getPluginSettingsId() + '/' + molgenis.getPluginId());
//		};
	});
});
