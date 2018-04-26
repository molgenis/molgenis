import _ from "underscore";
import $ from "jquery";
import {htmlEscape} from "../utils/HtmlUtils";
import {createRsqlQuery, createRsqlAggregateQuery, encodeRsqlValue} from "./rsql";

const apiBaseUri = '/api/v2/';

function createAttrsValue(attrs) {
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
}

function createSortValue(sort) {
    return _.map(sort.orders, function (order) {
        return encodeURIComponent(order.attr) + (order.direction === 'desc' ? ':desc' : '');
    }).join(','); // do not encode comma
}

export default class RestClientV2 {

    get(resourceUri, options) {
        if (!resourceUri.startsWith('/api/')) {
            // assume that resourceUri is a entity name
            resourceUri = apiBaseUri + htmlEscape(resourceUri);
        }

        let qs;
        if (options) {
            let items = [];

            if (options.q) {
                if (options.q.length > 0) {
                    items.push('q=' + encodeRsqlValue(createRsqlQuery(options.q)));
                }
            }
            if (options.aggs) {
                items.push('aggs=' + encodeRsqlValue(createRsqlAggregateQuery(options.aggs)));
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