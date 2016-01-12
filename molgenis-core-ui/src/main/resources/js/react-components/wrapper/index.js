/**
 * @module wrapper
 */

var Ace = require('./Ace');
var DateTimePicker = require('./DateTimePicker');
var JQRangeSlider = require('./JQRangeSlider');
var JQueryForm = require('./JQueryForm');
var Select2 = require('./Select2');
var TinyMce = require('./TinyMce');

var wrapper = {
	'Ace' : Ace,
	'DateTimePicker' : DateTimePicker,
	'JQRangeSlider' : JQRangeSlider,
	'JQueryForm' : JQueryForm,
	'Select2' : Select2,
	'TinyMce' : TinyMce
};

export default wrapper;
