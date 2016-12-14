import _ from "underscore";
import $ from "jquery";
import {htmlEscape} from "../utils/HtmlUtils";

var apiBaseUri = '/api/v2/';

var createAttrsValue = function (attrs) {
    var items = [];
    for (var key in attrs) {
        if (attrs.hasOwnProperty(key)) {
            if (attrs[key]) {
                if (attrs[key] === '*') {
                    items.push(encodeURIComponent(key) + '(*)'); // do not encode wildcard and parenthesis
                } else {
                    items.push(encodeURIComponent(key) + '(' + createAttrsValue(attrs[key]) + ')'); // do not encode parenthesis
                }
            } else {
                items.push(encodeURIComponent(key));
            }
        }
    }
    return items.join(','); // do not encode comma
};

var toRsqlValue = function (value) {
    var rsqlValue;
    if (_.isString(value) === false || (value.indexOf('"') !== -1 || value.indexOf('\'') !== -1 || value.indexOf('(') !== -1 || value.indexOf(')') !== -1 || value.indexOf(';') !== -1
        || value.indexOf(',') !== -1 || value.indexOf('=') !== -1 || value.indexOf('!') !== -1 || value.indexOf('~') !== -1 || value.indexOf('<') !== -1
        || value.indexOf('>') !== -1 || value.indexOf(' ') !== -1)) {
        rsqlValue = '"' + encodeURIComponent(value) + '"';
    } else {
        rsqlValue = encodeURIComponent(value);
    }
    return rsqlValue;
};

var createRsqlAggregateQuery = function (aggs) {
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

export function createRsqlQuery(rules) {
    var rsql = '';

    for (var i = 0; i < rules.length; ++i) {
        var rule = rules[i];

        // simplify query
        while (rule.operator === 'NESTED' && rule.nestedRules.length === 1) {
            rule = rule.nestedRules[0];
        }

        switch (rule.operator) {
            case 'SEARCH':
                var field = rule.field !== undefined ? rule.field : '*';
                rsql += encodeURIComponent(field) + '=q=' + toRsqlValue(rule.value);
                break;
            case 'EQUALS':
                rsql += encodeURIComponent(rule.field) + '==' + toRsqlValue(rule.value);
                break;
            case 'IN':
                rsql += encodeURIComponent(rule.field) + '=in=' + '(' + $.map(rule.value, function (value) {
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
                // ignore rule without nested rules
                if (rule.nestedRules.length > 0) {
                    rsql += '(' + createRsqlQuery(rule.nestedRules) + ')';
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

var createSortValue = function (sort) {
    var qs = _.map(sort.orders, function (order) {
        return encodeURIComponent(order.attr) + (order.direction === 'desc' ? ':desc' : '');
    }).join(','); // do not encode comma
    return qs;
};

export default class RestClientV2 {

    get(resourceUri, options) {
        if (!resourceUri.startsWith('/api/')) {
            // assume that resourceUri is a entity name
            resourceUri = apiBaseUri + htmlEscape(resourceUri);
        }

        var qs;
        if (options) {
            var items = [];

            if (options.q) {
                if (options.q.length > 0) {
                    items.push('q=' + createRsqlQuery(options.q));
                }
            }
            if (options.aggs) {
                items.push('aggs=' + createRsqlAggregateQuery(options.aggs));
            }
            if (options.attrs) {
                items.push('attrs=' + createAttrsValue(options.attrs));
            }
            if (options.sort) {
                items.push('sort=' + createSortValue(options.sort));
            }
            if (options.start !== undefined) {
                items.push('start=' + options.start);
            }
            if (options.num !== undefined) {
                items.push('num=' + options.num);
            }
            qs = items.join('&');
        } else {
            qs = null;
        }

        if ((qs ? resourceUri + '?' + qs : resourceUri).length < 2048) {
            return $.ajax({
                method: 'GET',
                url: qs ? resourceUri + '?' + qs : resourceUri,
                dataType: 'json',
                cache: true
            });
        } else {
            // keep URLs under 2048 chars: http://stackoverflow.com/a/417184
            // tunnel GET request through POST
            return $.ajax({
                method: 'POST',
                url: resourceUri + '?_method=GET',
                dataType: 'json',
                contentType: 'application/x-www-form-urlencoded',
                data: qs,
                cache: true
            });
        }
    };

    remove(name, id) {
        return $.ajax({
            type: 'DELETE',
            url: apiBaseUri + encodeURI(name) + '/' + encodeURI(id)
        });
    };
}