(function($, molgenis) {	
	"use strict";
	
	/**
	 * Helper block for numerous logical operators
	 */
	function logicOperators() {
		Handlebars.registerHelper('ifCond', function (v1, operator, v2, options) {
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
	
	/**
	 * Helper block for error reporting equals statement
	 */
	function equals() {
		Handlebars.registerHelper('equal', function(lvalue, rvalue, options) {
		    if (arguments.length < 3)
		        throw new Error("Handlebars Helper equal needs 2 parameters");
		    if (lvalue != rvalue) {
		        return options.inverse(this);
		    } else {
		        return options.fn(this);
		    }
		});
	}
	
	/**
	 * Helper block for error reporting non equals statement
	 */
	function notEquals() {
		Handlebars.registerHelper('notequal', function(lvalue, rvalue, options) {
		    if (arguments.length < 3)
		        throw new Error("Handlebars Helper equal needs 2 parameters");
		    if (lvalue != rvalue) {
		    	 return options.fn(this);
		    } else {
		    	 return options.inverse(this);
		    }
		});
	}
	
	/**
	 * Creates helper blocks to support logic in handlebar templates
	 */
	$(function() {
		logicOperators();
		notEquals();
		equals();
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));