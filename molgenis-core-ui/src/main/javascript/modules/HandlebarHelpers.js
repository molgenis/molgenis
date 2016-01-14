define(function(require, exports, module) {
	/**
	 * This module contains a function to help with handlebar operators
	 * 
	 * @module HandlebarHelpers
	 */

	'use strict';

	var Handlebars = require('handlebars');

	/**
	 * Helper block function container
	 */
	exports.handleBarHelperBlocks = function() {
		Handlebars.registerHelper('equal', function(lvalue, rvalue, options) {
			if (arguments.length < 3)
				throw new Error("Handlebars Helper equal needs 2 parameters");
			if (lvalue != rvalue) {
				return options.inverse(this);
			} else {
				return options.fn(this);
			}
		});

		Handlebars.registerHelper('notequal', function(lvalue, rvalue, options) {
			if (arguments.length < 3)
				throw new Error("Handlebars Helper equal needs 2 parameters");
			if (lvalue != rvalue) {
				return options.fn(this);
			} else {
				return options.inverse(this);
			}
		});

		Handlebars.registerHelper('ifCond', function(v1, operator, v2, options) {
			switch (operator) {
			case '==':
				return (v1 == v2) ? options.fn(this) : options.inverse(this);
			case '===':
				return (v1 === v2) ? options.fn(this) : options.inverse(this);
			case '<':
				return (v1 < v2) ? options.fn(this) : options.inverse(this);
			case '<=':
				return (v1 <= v2) ? options.fn(this) : options.inverse(this);
			case '>':
				return (v1 > v2) ? options.fn(this) : options.inverse(this);
			case '>=':
				return (v1 >= v2) ? options.fn(this) : options.inverse(this);
			case '&&':
				return (v1 && v2) ? options.fn(this) : options.inverse(this);
			case '||':
				return (v1 || v2) ? options.fn(this) : options.inverse(this);
			default:
				return options.inverse(this);
			}
		});
	}
});