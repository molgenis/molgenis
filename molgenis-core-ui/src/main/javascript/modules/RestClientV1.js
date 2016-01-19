/**************************************************************
 * This module holds all the methods to query the REST api v1 *
 *                                                            *
 * @module RestClientV1                                       *
 **************************************************************/
var $ = require('jquery');

class RestClientV1 {
    constructor() {
        this.apiBaseUri = '/api/v1/';
    }

    get(resourceUri, options) {
        return this._get(resourceUri, options, false);
    }

    getAsync(resourceUri, options, callback) {
        return this._get(resourceUri, options, true, callback);
    }

    _get(resourceUri, options, async, callback) {
        var resource = null;

        var config = {
            'dataType': 'json',
            'cache': true,
            'async': async
        };

        if (callback) {
            config['success'] = function (data) {
                callback(data);
            };
        } else if (async === false) {
            config['success'] = function (data) {
                resource = data;
            };
        }

        // tunnel get requests with options through a post,
        // because it might not fit in the URL
        if (options) {
            // backward compatibility for legacy code
            if (options.q && !Array.isArray(options.q)) {
                var obj = Object.assign({}, options.q);
                delete options.q;
                for (var i = 0, keys = Object.keys(obj); i < keys.length; ++i) {
                    options[keys[i]] = obj[keys[i]];
                }
            }

            var url = resourceUri;
            if (resourceUri.indexOf('?') == -1) {
                url = url + '?';
            } else {
                url = url + '&';
            }
            url = url + '_method=GET';

            Object.assign(config, {
                'type': 'POST',
                'url': url,
                'data': JSON.stringify(options),
                'contentType': 'application/json'
            });
        } else {
            Object.assign(config, {
                'type': 'GET',
                'url': resourceUri
            });
        }

        var promise = this._ajax(config);

        if (async === false)
            return resource;
        else
            return promise;
    }

    _ajax(config) {
        if (self.token) {
            Object.assign(config, {
                headers: {
                    'x-molgenis-token': self.token
                }
            });
        }
        return $.ajax(config);
    }

    _toApiUri(resourceUri, options) {
        var qs = "";
        if (resourceUri.indexOf('?') != -1) {
            var uriParts = resourceUri.split('?');
            resourceUri = uriParts[0];
            qs = '?' + uriParts[1];
        }
        if (options && options.attributes && options.attributes.length > 0)
            qs += (qs.length === 0 ? '?' : '&') + 'attributes=' + encodeURIComponent(options.attributes.join(','));
        if (options && options.expand && options.expand.length > 0)
            qs += (qs.length === 0 ? '?' : '&') + 'expand=' + encodeURIComponent(options.expand.join(','));
        if (options && options.q)
            qs += (qs.length === 0 ? '?' : '&') + '_method=GET';
        return resourceUri + qs;
    }

    getPrimaryKeyFromHref(href) {
        return href.substring(href.lastIndexOf('/') + 1);
    }

    getHref(entityName, primaryKey) {
        return apiBaseUri + entityName + (primaryKey ? '/' + primaryKey : '');
    }

    remove(href, callback) {
        return this._ajax({
            type: 'POST',
            url: href,
            data: '_method=DELETE',
            async: false,
            success: callback && callback.success ? callback.success : function () {
            },
            error: callback && callback.error ? callback.error : function () {
            }
        });
    }

    update(href, entity, callback, showSpinner) {
        return this._ajax({
            type: 'POST',
            url: href + '?_method=PUT',
            contentType: 'application/json',
            data: JSON.stringify(entity),
            async: true,
            showSpinner: showSpinner,
            success: callback && callback.success ? callback.success : function () {
            },
            error: callback && callback.error ? callback.error : function () {
            }
        });
    }

    entityExists(resourceUri) {
        var result = false;
        this._ajax({
            dataType: 'json',
            url: resourceUri + '/exist',
            async: false,
            success: function (exists) {
                result = exists;
            }
        });

        return result;
    }

    login(username, password, callback) {
        $.ajax({
            type: 'POST',
            dataType: 'json',
            url: this.apiBaseUri + 'login',
            contentType: 'application/json',
            async: true,
            data: JSON.stringify({
                username: username,
                password: password
            }),
            success: function (loginResult) {
                self.token = loginResult.token;
                callback.success({
                    username: loginResult.username,
                    firstname: loginResult.firstname,
                    lastname: loginResult.lastname
                });
            },
            error: callback.error
        });
    }

    logout(callback) {
        this._ajax({
            url: this.apiBaseUri + 'logout',
            async: true,
            success: function () {
                self.token = null;
                callback();
            }
        });
    }
}

export default new RestClientV1();