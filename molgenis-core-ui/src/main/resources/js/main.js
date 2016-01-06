'use strict';

// First we’re setting a baseUrl to the scripts folder, then letting RequireJS know where our libraries are 
// with the paths option (compulsory when using jQuery as it is a named module) and then shimming any scripts 
// that do not have AMD support. This is common with most jQuery plugins.
require.config({
	baseUrl : '/src/main/resources/js',
	paths : {
		jquery : '/components/jquery/jquery-2.1.1.min',
		domready : '/components/requirejs/requirejs-domready',
		handlebars : '/components/handlebars/handlebars',
		bootstrap : '/components/bootstrap/bootstrap.min',
		react : '/components/react/react-with-addons',
		molgenis : '/modules/molgenis',
		underscore : '/components/underscore/underscore-min',
		react_button : '/component/Button'
	},
	shim : {
		handlebars : {
			exports : 'Handlebars'
		},
		'/plugins/jQEditRangeSlider-min.js' : [ 'jquery' ],
		'/plugins/jquery-sortable-min.js' : [ 'jquery' ],
		'/plugins/jquery.bootstrap.pager.js' : [ 'jquery' ],
		'/plugins/jquery.bootstrap.wizard.min.js' : [ 'jquery' ],
		'/plugins/jquery.cookie-1.4.1.min.js' : [ 'jquery' ],
		'/plugins/jquery.fancytree.min.js' : [ 'jquery' ],
		'/plugins/jquery.form.min.js' : [ 'jquery' ],
		'/plugins/jquery.molgenis.attributemetadata.table.js' : [ 'jquery' ],
		'/plugins/jquery.molgenis.table.js' : [ 'jquery' ],
		'/plugins/jquery.molgenis.tree.js' : [ 'jquery' ],
		'/plugins/jquery.molgenis.xrefmrefsearch.js' : [ 'jquery' ],
		'/plugins/jquery.validate.min.js' : [ 'jquery' ],
		'/plugins/select2-patched.js' : [ 'jquery' ],
		'plugins/bootstrap.file-input.js' : [ 'bootstrap' ],
		'plugins/bootstrap-datetimepicker.min.js' : [ 'bootstrap' ],
		'plugins/bootstrap-fileupload.min.js' : [ 'bootstrap' ]
	}
});

// Specify the Core libraries which are required, and also any modules that you
// might want to load and initialise on every page
define(function(require) {
	var $ = require('jquery');
	var domReady = require('domready');
	var Handlebars = require('handlebars');
	var Bootstrap = require('bootstrap');
	var React = require('react');
	var molgenis = require('molgenis');
	var _ = require('underscore');
	
	domReady(function() {
		// Init common module code here
		// TODO common modules, like React components????
		console.log('ready!');
	});
});
