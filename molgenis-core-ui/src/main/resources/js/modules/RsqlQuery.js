define(function(require, exports, module) {
	/**
	 * This module holds all the methods to create Rsql queries
	 * 
	 * @module RestClientV1
	 */

	"use strict";
	var $ = require('jquery');
	var _ = require('underscore');
	
	/**
	 * 
	 */
	exports.prototype.createAttrsValue = function(attrs) {
		var items = [];
		for ( var key in attrs) {
			if (attrs.hasOwnProperty(key)) {
				if (attrs[key]) {
					if (attrs[key] === '*') {
						// Do not encode wildcard and parenthesis
						items.push(encodeURIComponent(key) + '(*)');
					} else {
						// Do not encode parenthesis
						items.push(encodeURIComponent(key) + '(' + createAttrsValue(attrs[key]) + ')');
					}
				} else {
					items.push(encodeURIComponent(key));
				}
			}
		}
		// Do not encode comma
		return items.join(',');
	};

	/**
	 * 
	 */
	exports.prototype.toRsqlValue = function(value) {
		var rsqlValue;
		if (_.isString(value) === false
				|| (value.indexOf('"') !== -1 || value.indexOf('\'') !== -1 || value.indexOf('(') !== -1 || value.indexOf(')') !== -1 || value.indexOf(';') !== -1
						|| value.indexOf(',') !== -1 || value.indexOf('=') !== -1 || value.indexOf('!') !== -1 || value.indexOf('~') !== -1 || value.indexOf('<') !== -1
						|| value.indexOf('>') !== -1 || value.indexOf(' ') !== -1)) {
			rsqlValue = '"' + encodeURIComponent(value) + '"';
		} else {
			rsqlValue = encodeURIComponent(value);
		}
		return rsqlValue;
	};

	/**
	 * 
	 */
	exports.prototype.createRsqlAggregateQuery = function(aggs) {
		var rsql = '';
		if (aggs.x) {
			rsql += 'x==' + toRsqlValue(aggs.x);
		}
		if (aggs.y) {
			if (rsql.length > 0) {
				rsql += ';';
			}
			rsql += 'y==' + toRsqlValue(aggs.y);
		}
		if (aggs.distinct) {
			if (rsql.length > 0) {
				rsql += ';';
			}
			rsql += 'distinct==' + toRsqlValue(aggs.distinct);
		}
		return rsql;
	};

	/**
	 * 
	 */
	exports.prototype.createRsqlQuery = function(rules) {
		var rsql = '';

		// simplify query
		while (rules.length === 1 && rules[0].operator === 'NESTED') {
			rules = rules[0].nestedRules;
		}

		for (var i = 0; i < rules.length; ++i) {
			var rule = rules[i];
			switch (rule.operator) {
			case 'SEARCH':
				var field = rule.field !== undefined ? rule.field : '*';
				rsql += encodeURIComponent(field) + '=q=' + toRsqlValue(rule.value);
				break;
			case 'EQUALS':
				rsql += encodeURIComponent(rule.field) + '==' + toRsqlValue(rule.value);
				break;
			case 'IN':
				rsql += encodeURIComponent(rule.field) + '=in=' + '(' + $.map(rule.value, function(value) {
					return toRsqlValue(value);
				}).join(',') + ')';
				break;
			case 'LESS':
				rsql += encodeURIComponent(rule.field) + '=lt=' + toRsqlValue(rule.value);
				break;
			case 'LESS_EQUAL':
				rsql += encodeURIComponent(rule.field) + '=le=' + toRsqlValue(rule.value);
				break;
			case 'GREATER':
				rsql += encodeURIComponent(rule.field) + '=gt=' + toRsqlValue(rule.value);
				break;
			case 'GREATER_EQUAL':
				rsql += encodeURIComponent(rule.field) + '=ge=' + toRsqlValue(rule.value);
				break;
			case 'RANGE':
				rsql += encodeURIComponent(rule.field) + '=rng=' + '(' + toRsqlValue(rule.value[0]) + ',' + toRsqlValue(rule.value[1]) + ')';
				break;
			case 'LIKE':
				rsql += encodeURIComponent(rule.field) + '=like=' + toRsqlValue(rule.value);
				break;
			case 'NOT':
				rsql += encodeURIComponent(rule.field) + '!=' + toRsqlValue(rule.value);
				break;
			case 'AND':
				// ignore dangling AND rule
				if (i > 0 && i < rules.length - 1) {
					rsql += ';';
				}
				break;
			case 'OR':
				// ignore dangling OR rule
				if (i > 0 && i < rules.length - 1) {
					rsql += ',';
				}
				break;
			case 'NESTED':
				// do not nest in case of only one nested rule
				if (rule.nestedRules.length > 1) {
					rsql += '(';
				}
				// ignore rule without nested rules
				if (rule.nestedRules.length > 0) {
					rsql += createRsqlQuery(rule.nestedRules);
				}
				if (rule.nestedRules.length > 1) {
					rsql += ')';
				}
				break;
			case 'SHOULD':
				throw 'unsupported query operator [' + rule.operator + ']';
			case 'DIS_MAX':
				throw 'unsupported query operator [' + rule.operator + ']';
			case 'FUZZY_MATCH':
				throw 'unsupported query operator [' + rule.operator + ']';
			default:
				throw 'unknown query operator [' + rule.operator + ']';
			}
		}
		return rsql;
	};

	/**
	 * 
	 */
	exports.prototype.createSortValue = function(sort) {
		var qs = _.map(sort.orders, function(order) {
			return encodeURIComponent(order.attr) + (order.direction === 'desc' ? ':desc' : '');
		}).join(','); // do not encode comma
		return qs;
	};
});