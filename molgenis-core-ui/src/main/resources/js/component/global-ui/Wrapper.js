define(function(require, exports, module) {
	/**
	 * @module wrapper
	 */

	'use strict';

	var Ace = require('component/wrapper/Ace');
	var DateTimePicker = require('component/wrapper/DateTimePicker');
	var JQRangeSlider = require('component/wrapper/JQRangeSlider');
	var JQueryForm = require('component/wrapper/JQueryForm');
	var Select2 = require('component/wrapper/Select2');
	var TinyMce = require('component/wrapper/TinyMce');

	var wrapper = {
		'Ace' : Ace,
		'DateTimePicker' : DateTimePicker,
		'JQRangeSlider' : JQRangeSlider,
		'JQueryForm' : JQueryForm,
		'Select2' : Select2,
		'TinyMce' : TinyMce
	};

	module.exports = wrapper;
});