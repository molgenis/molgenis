(function ($, molgenis) {
    "use strict";

    // workaround for "Uncaught RangeError: Maximum call stack size exceeded"
    // http://stackoverflow.com/a/19190216
    $.fn.modal.Constructor.prototype.enforceFocus = function () {
    };

    molgenis.setCookieWall = function (cookieWall) {
        molgenis.cookieWall = cookieWall;
    };

    molgenis.getCookieWall = function () {
        return molgenis.cookieWall;
    };

    molgenis.setContextUrl = function (contextUrl) {
        molgenis.contextUrl = contextUrl;
    };

    molgenis.getContextUrl = function () {
        return molgenis.contextUrl;
    };

    molgenis.setPluginId = function (pluginId) {
        molgenis.pluginId = pluginId;
    };

    molgenis.getPluginId = function () {
        return molgenis.pluginId;
    };

    /**
     * Returns the plugin settings entity id
     */
    molgenis.getPluginSettingsId = function () {
        return 'sys' + molgenis.packageSeparator + 'set' + molgenis.packageSeparator + molgenis.getPluginId();
    };

    /**
     * Returns a promise with plugin settings
     */
    molgenis.getPluginSettings = function () {
        var api = new molgenis.RestClientV2();
        return api.get('/api/v2/' + molgenis.getPluginSettingsId() + '/' + molgenis.getPluginId());
    };

    molgenis.createAlert = function (alerts, type, container) {
        if (type !== 'error' && type !== 'warning' && type !== 'success' && type !== 'info')
            type = 'error';
        if (container === undefined) {
            container = $('.alerts');
            container.empty();
        }

        var items = [];
        items.push('<div class="alert alert-');
        items.push(type === 'error' ? 'danger' : type); // backwards compatibility
        items
            .push('"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>');
        items.push(type.charAt(0).toUpperCase() + type.slice(1));
        items.push('!</strong> ');
        $.each(alerts, function (i, alert) {
            if (i > 0)
                items.push('<br/>');
            var message = '<span>' + alert.message;
            if (alert.code) message += ' (' + alert.code + ')';
            message += '</span>';
            items.push(message)
        });
        items.push('</div>');

        container.prepend(items.join(''));
    };

    molgenis.i18n = molgenis.i18n || {};
    molgenis.i18n.get = function (str, lang) {
        lang = typeof lang !== 'undefined' ? lang : 'en';
        var i18nObj;
        if (str
            && (str.charAt(0) !== '{' || str.charAt(str.length - 1) !== '}'))
            i18nObj = {
                'en': str
            };
        else
            i18nObj = JSON.parse(str ? str : '{}');
        return i18nObj[lang];
    };
    molgenis.i18n.getAll = function (str, lang) {
        lang = typeof lang !== 'undefined' ? lang : 'en';
        var i18nObj;
        if (str
            && (str.charAt(0) !== '{' || str.charAt(str.length - 1) !== '}'))
            i18nObj = {
                'en': str
            };
        else
            i18nObj = JSON.parse(str ? str : '{}');
        return i18nObj;
    };

    /*
     * Natural Sort algorithm for Javascript - Version 0.7 - Released under MIT
     * license Author: Jim Palmer (based on chunking idea from Dave Koelle)
     *
     * https://github.com/overset/javascript-natural-sort
     */
    molgenis.naturalSort = function (a, b) {
        var re = /(^-?[0-9]+(\.?[0-9]*)[df]?e?[0-9]?$|^0x[0-9a-f]+$|[0-9]+)/gi, sre = /(^[ ]*|[ ]*$)/g, dre = /(^([\w ]+,?[\w ]+)?[\w ]+,?[\w ]+\d+:\d+(:\d+)?[\w ]?|^\d{1,4}[\/\-]\d{1,4}[\/\-]\d{1,4}|^\w+, \w+ \d+, \d{4})/, hre = /^0x[0-9a-f]+$/i, ore = /^0/, i = function (s) {
                return molgenis.naturalSort.insensitive && ('' + s).toLowerCase()
                    || '' + s;
            },
            // convert all to strings strip whitespace
            x = i(a).replace(sre, '') || '', y = i(b).replace(sre, '') || '',
            // chunk/tokenize
            xN = x.replace(re, '\0$1\0').replace(/\0$/, '').replace(/^\0/, '')
                .split('\0'), yN = y.replace(re, '\0$1\0').replace(/\0$/, '')
                .replace(/^\0/, '').split('\0'),
            // numeric, hex or date detection
            xD = parseInt(x.match(hre))
                || (xN.length != 1 && x.match(dre) && Date.parse(x)), yD = parseInt(y
                    .match(hre))
                || xD && y.match(dre) && Date.parse(y) || null, oFxNcL, oFyNcL;
        // first try and sort Hex codes or Dates
        if (yD)
            if (xD < yD)
                return -1;
            else if (xD > yD)
                return 1;
        // natural sorting through split numeric strings and default strings
        for (var cLoc = 0, numS = Math.max(xN.length, yN.length); cLoc < numS; cLoc++) {
            // find floats not starting with '0', string or 0 if not defined
            // (Clint Priest)
            oFxNcL = !(xN[cLoc] || '').match(ore) && parseFloat(xN[cLoc])
                || xN[cLoc] || 0;
            oFyNcL = !(yN[cLoc] || '').match(ore) && parseFloat(yN[cLoc])
                || yN[cLoc] || 0;
            // handle numeric vs string comparison - number < string - (Kyle
            // Adams)
            if (isNaN(oFxNcL) !== isNaN(oFyNcL)) {
                return (isNaN(oFxNcL)) ? 1 : -1;
            }
            // rely on string comparison if different types - i.e. '02' < 2 !=
            // '02' < '2'
            else if (typeof oFxNcL !== typeof oFyNcL) {
                oFxNcL += '';
                oFyNcL += '';
            }
            if (oFxNcL < oFyNcL)
                return -1;
            if (oFxNcL > oFyNcL)
                return 1;
        }
        return 0;
    };
}($, window.top.molgenis = window.top.molgenis || {}));

// Add endsWith function to the string class
if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function (suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
}

function getCurrentTimezoneOffset() {
    function padNumber(number, length) {
        var str = "" + number;
        while (str.length < length) {
            str = '0' + str;
        }

        return str;
    }

    var offset = new Date().getTimezoneOffset();
    offset = ((offset < 0 ? '+' : '-')
    + padNumber(parseInt(Math.abs(offset / 60)), 2) + padNumber(Math
        .abs(offset % 60), 2));

    return offset;
}

function getInternetExplorerVersion() {
    var rv = -1;
    if (navigator.appName == 'Microsoft Internet Explorer') {
        var ua = navigator.userAgent;
        var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
        if (re.exec(ua) != null)
            rv = parseFloat(RegExp.$1);
    }
    else if (navigator.appName == 'Netscape') {
        var ua = navigator.userAgent;
        var re = new RegExp("Trident/.*rv:([0-9]{1,}[\.0-9]{0,})");
        if (re.exec(ua) != null)
            rv = parseFloat(RegExp.$1);
    }
    return rv;
}

/*
 * Create a table cell to show data of a certain type Is used by the
 * dataexplorer and the forms plugin
 */
function formatTableCellValue(rawValue, dataType, editable, nillable) {
    var htmlElement;

    if (dataType === undefined) {
        return '<span>&nbsp;</span>';
    }
    else if (dataType.toLowerCase() == 'bool') {
        htmlElement = '<input type="checkbox" ';
        if (rawValue === true) {
            htmlElement += 'checked ';
        }
        if (editable !== true) {
            htmlElement += 'disabled="disabled"';
        }

        htmlElement += '/>';

        if (dataType.toLowerCase() == 'bool' && nillable === true && (rawValue === undefined || rawValue === '')) {
            htmlElement = $(htmlElement);
            htmlElement.prop('indeterminate', true);
        }

        return htmlElement;
    }
    if (typeof rawValue === 'undefined' || rawValue === null) {
        return '<span>&nbsp;</span>';
    }

    if (dataType.toLowerCase() == "hyperlink") {
        return htmlElement = '<a target="_blank" href="' + rawValue + '">' + htmlEscape(rawValue) + '</a>';

    } else if (dataType.toLowerCase() == "email") {
        return htmlElement = '<a href="mailto:' + rawValue + '">' + htmlEscape(rawValue) + '</a>';

    } else if (dataType.toLowerCase() != 'html') {
        if (rawValue.length > 50) {
            var abbr = htmlEscape(abbreviate(rawValue, 50));
            return htmlElement = '<span class="show-popover"  data-content="'
                + htmlEscape(rawValue) + '" data-toggle="popover">' + abbr
                + "</span>";
        } else {
            return '<span>' + htmlEscape(rawValue) + '</span>';
        }

    } else {
        return '<span>' + htmlEscape(rawValue) + '</span>';
    }
}

/**
 * Is s is longer then maxLength cut it and add ...
 *
 * @param s
 * @param maxLength
 */
function abbreviate(s, maxLength) {
    if (s.length <= maxLength) {
        return s;
    }

    return s.substr(0, maxLength - 3) + '...';
}

/**
 * Create input element for a molgenis data type
 *
 * @param dataType
 *            molgenis data type
 * @param attrs
 *            input attributes
 * @param val
 *            input value
 * @param lbl
 *            input label (for checkbox and radio inputs)
 *
 * @deprecated use AttributeControl.js
 */
function createInput(attr, attrs, val, lbl) {
    function createBasicInput(type, attrs, val) {
        var $input = $('<input type="' + type + '">');
        if (attrs)
            $input.attr(attrs);
        if (val !== undefined)
            $input.val(val);
        return $input;
    }

    var dataType = attr.fieldType;
    switch (dataType) {
        case 'BOOL':
            var label = $('<label class="radio">');
            var $input = createBasicInput('radio', attrs, val);
            return label.append($input).append(val ? 'True' : 'False');
        case 'CATEGORICAL':
        case 'CATEGORICAL_MREF':
            var label = $('<label>');
            var $input = createBasicInput('checkbox', attrs, val);
            return $('<div class="checkbox">').append(label.append($input).append(lbl));
        case 'DATE':
        case 'DATE_TIME':
            var $div = $('<div>').addClass('group-append date input-group');
            var $input = createBasicInput('text', attrs, val)
                .addClass('form-control')
                .attr('data-date-format', dataType === 'DATE' ? 'YYYY-MM-DD' : 'YYYY-MM-DDTHH:mm:ssZZ')
                .appendTo($div);
            if (attr.nillable) {
                $input.addClass('nillable');
                $('<span>')
                    .addClass('input-group-addon')
                    .append($('<span>')
                        .addClass('glyphicon glyphicon-remove empty-date-input clear-date-time-btn'))
                    .appendTo($div);
            }
            $('<span>').addClass('input-group-addon datepickerbutton')
                .append($('<span>').addClass('glyphicon glyphicon-calendar'))
                .appendTo($div);
            $div.datetimepicker(dataType === 'DATE' ? {format: 'YYYY-MM-DD'} : {format: 'YYYY-MM-DDTHH:mm:ssZZ'});
            return $div;
        case 'DECIMAL':
            var input = createBasicInput('number', $.extend({}, attrs, {'step': 'any'}), val).addClass('form-control');
            if (!attr.nillable)
                input.prop('required', true);
            return input;
        case 'INT':
        case 'LONG':
            var opts = $.extend({}, attrs, {'step': '1'});
            if (attr.range) {
                if (typeof attr.range.min) opts.min = attr.range.min;
                if (typeof attr.range.max !== 'undefined') opts.max = attr.range.max;
            }
            var input = createBasicInput('number', opts, val).addClass('form-control');
            if (!attr.nillable)
                input.prop('required', true);
            return input;
        case 'EMAIL':
            return createBasicInput('email', attrs, val).addClass('form-control');
        case 'HTML':
        case 'HYPERLINK':
        case 'STRING':
        case 'TEXT':
        case 'ENUM':
        case 'SCRIPT':
            return createBasicInput('text', attrs, val).addClass('form-control');
        case 'MREF':
        case 'XREF':
        case 'FILE':
        case 'ONE_TO_MANY':
            return createBasicInput('hidden', attrs, val).addClass('form-control');
        default:
            throw 'Unknown data type: ' + dataType;
    }
}

function showSpinner(callback) {
    var spinner = $('#spinner');
    var login = $('#login-modal');

    if (spinner.length === 0) {
        // do not add fade effect on modal: http://stackoverflow.com/a/22101894
        var items = [];
        items.push('<div class="modal" id="spinner" aria-labelledby="spinner-modal-label" aria-hidden="true">');
        items.push('<div class="modal-dialog modal-sm">');
        items.push('<div class="modal-content">');
        items.push('<div class="modal-header"><h4 class="modal-title" id="spinner-modal-label">Loading ...</h4></div>');
        items.push('<div class="modal-body"><div class="modal-body-inner"><img src="/img/waiting-spinner.gif"></div></div>');
        items.push('</div>');
        items.push('</div>');

        $('body').append(items.join(''));
        spinner = $('#spinner');
        spinner.data('count', 0);
        spinner.modal({
            backdrop: 'static',
            show: false
        });
    }

    if (callback) {
        spinner.on('shown.bs.modal', function (e) {
            callback();
        });
    }

    var count = $('#spinner').data('count');
    if (count === 0) {
        var timeout = setTimeout(function () {
            spinner.modal('show');
        }, 500);
        $('#spinner').data('timeout', timeout);
        $('#spinner').data('count', 1);
    } else {
        $('#spinner').data('count', count + 1);
    }

    if (login.length > 0) {
        hideSpinner();
    }
}

function hideSpinner() {
    if ($('#spinner').length !== 0) {
        var count = $('#spinner').data('count');
        if (count === 1) {
            clearTimeout($('#spinner').data('timeout'));
            $('#spinner').modal('hide');
        }
        if (count > 0) {
            $('#spinner').data('count', count - 1);
        }
    }
}

/**
 * Helper block function container
 */
function handleBarHelperBlocks(Handlebars) {
    Handlebars.registerHelper('equal', function (lvalue, rvalue, options) {
        if (arguments.length < 3)
            throw new Error("Handlebars Helper equal needs 2 parameters");
        if (lvalue != rvalue) {
            return options.inverse(this);
        } else {
            return options.fn(this);
        }
    });

    Handlebars.registerHelper('notequal', function (lvalue, rvalue, options) {
        if (arguments.length < 3)
            throw new Error("Handlebars Helper equal needs 2 parameters");
        if (lvalue != rvalue) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    });

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

$(function () {
    var languageSelectBox = document.getElementById('language-select-box');
    if (languageSelectBox) {
        React.render(molgenis.ui.LanguageSelectBox({}), languageSelectBox);
    }

    // disable all ajax request caching
    $.ajaxSetup({
        cache: false
    });

    // use ajaxPrefilter instead of ajaxStart and ajaxStop
    // to work around issue http://bugs.jquery.com/ticket/13680
    $.ajaxPrefilter(function (options, _, jqXHR) {
        if (options.showSpinner !== false) {
            showSpinner();
            jqXHR.always(hideSpinner);
        }
    });

    $(document)
        .ajaxError(
            function (event, xhr, settings, e) {
                if (xhr.status === 401) {
                    document.location = "/login";
                }
                try {
                    molgenis
                        .createAlert(JSON.parse(xhr.responseText).errors);
                } catch (ex) {
                    molgenis
                        .createAlert(
                            [{
                                'message': 'An error occurred. Please contact the administrator.'
                            }], 'error');
                    console.log('An ajax error occurred.', event, xhr, settings, e);
                }
            });

    window.onerror = function (msg, url, line) {
        console.log('An error occurred.', msg, url, line);
        molgenis.createAlert([{
            'message': 'An error occurred. Please contact the administrator.'
        }, {
            'message': msg
        }], 'error');
    };

    // async load bootstrap modal and display
    $(document).on('click', 'a.modal-href', function (e) {
        e.preventDefault();
        e.stopPropagation();
        if (!$(this).hasClass('disabled')) {
            var container = $('#' + $(this).data('target'));
            if (container.is(':empty')) {
                container.load($(this).attr('href'), function () {
                    $('.modal:first', container).modal('show');
                });
            } else {
                $('.modal:first', container).modal('show');
            }
        }
    });

    // support overlapping bootstrap modals:
    // http://stackoverflow.com/questions/19305821/bootstrap-3-0-multiple-modals-overlay
    $(document).on('show.bs.modal', '.modal', function (event) {
        var zIndex = 1040 + (10 * $('.modal:visible').length);
        $(this).css('z-index', zIndex);
        setTimeout(function () {
            $('.modal-backdrop').not('.modal-stack').css('z-index', zIndex - 1).addClass('modal-stack');
        }, 0);
    });

    // if modal closes, check if other modal remains open, if so, reapply the modal-open class to the body
    $(document).on('hidden.bs.modal', '.modal', function (event) {
        if ($('.modal:visible').length) {
            $('body').addClass('modal-open');
        }
    });

    // focus first input on modal display
    $(document).on('shown.bs.modal', '.modal', function () {
        $(this).find('input:visible:first').focus();
    });

    // focus first input on modal display
    $(document).on('click', '.plugin-settings-btn', function () {
        React.unmountComponentAtNode($('#plugin-settings-container')[0]); // fix https://github.com/molgenis/molgenis/issues/3587
        React.render(molgenis.ui.Form({
            entity: molgenis.getPluginSettingsId(),
            entityInstance: molgenis.getPluginId(),
            mode: 'edit',
            modal: true,
            enableOptionalFilter: false,
            enableFormIndex: false,
            onSubmitSuccess: function () {
                location.reload();
            }
        }), $('#plugin-settings-container')[0]);
    });

    /**
     * Add download functionality to JQuery. data can be string of parameters or
     * array/object
     *
     * Default method is POST
     *
     * Usage:
     * <code>download('/localhost:8080', 'param1=value1&param2=value2')</code>
     * Or:
     * <code>download('/localhost:8080', {param1 : 'value1', param2 : 'value2'})</code>
     *
     */
    $.download = function (url, data, method) {
        if (!method) {
            method = 'POST';
        }

        data = typeof data == 'string' ? data : $.param(data);

        // split params into form inputs
        var inputs = [];
        $.each(data.split('&'), function () {
            var pair = this.split('=');
            inputs.push('<input type="hidden" name="' + pair[0] + '" value="'
                + pair[1] + '" />');
        });

        // send request and remove form from dom
        $('<form action="' + url + '" method="' + method + '">').html(
            inputs.join('')).appendTo('body').submit().remove();
    };

    // serialize form as json object
    $.fn.serializeObject = function () {
        var o = {};
        var a = this.serializeArray();
        $.each(a, function () {
            if (o[this.name] !== undefined) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = this.value || '';
            }
        });
        return o;
    };

    // Call handleBarHelperBlock function to set helper blocks for entire application
    handleBarHelperBlocks(Handlebars);

    // clear datetimepicker on pressing cancel button
    $(document).on('click', '.clear-date-time-btn', function (e) {
        $(this).closest('div.date').find('input').val('');
        $(this).trigger('changeDate');
    });

    if (molgenis.getCookieWall()) {
        // show cookie wall
        var cookieValue = $.cookie("permissionforcookies");

        if (undefined === cookieValue) {
            $('.navbar.navbar-default.navbar-fixed-top').prepend(
                $('<div id="accept-cookies-container" class="container-fluid">' +
                    '<div class="jumbotron">' +
                    '<p class="text-center">' + window.location.hostname + ' uses third-party analytical cookies to analyze the use of the site and improve usability. By clicking on the accept button, or by continuing to use this website, you consent to the placing of cookies.</p>' +
                    '<p class="text-center"><a id="accept-cookies" class="btn btn-primary btn-lg" href="#" role="button">Accept cookies</a></p>' +
                    '</div>' +
                    '</div>'
                ));

            $('body').css({'margin-top': $('#accept-cookies-container').height()});

            $('#accept-cookies').on('click', function () {
                $.cookie("permissionforcookies", "true", {expires: 365, path: '/', secure: false});
                $('#accept-cookies-container').fadeOut(1000);

                // Reset body margin-top default value
                setTimeout(function () {
                    $('body').css({'margin-top': 0});
                }, 1000);
            });
        }
    }
});

//jQuery Deparam - v0.1.0 - 6/14/2011
//http://benalman.com/
//Copyright (c) 2011 Ben Alman; Licensed MIT, GPL

(function ($) {
    // Creating an internal undef value is safer than using undefined, in case it
    // was ever overwritten.
    var undef;
    // A handy reference.
    var decode = decodeURIComponent;

    // Document $.deparam.
    var deparam = $.deparam = function (text, reviver) {
        // The object to be returned.
        var result = {};
        // Iterate over all key=value pairs.
        $.each(text.replace(/\+/g, ' ').split('&'), function (index, pair) {
            // The key=value pair.
            var kv = []

            // Retrieve the key
            kv.push(pair.split('=')[0])

            // Retrieve the value, use substring instead of split because pair can
            // also be RSQL format e.g. q=user==admin
            kv.push(pair.substring(pair.indexOf('=') + 1 ))

            // The key, URI-decoded.
            var key = decode(kv[0]);
            // Abort if there's no key.
            if (!key) {
                return;
            }
            // The value, URI-decoded. If value is missing, use empty string.
            var value = decode(kv[1] || '');
            // If key is more complex than 'foo', like 'a[]' or 'a[b][c]', split it
            // into its component parts.
            var keys = key.split('][');
            var last = keys.length - 1;
            // Used when key is complex.
            var i = 0;
            var current = result;

            // If the first keys part contains [ and the last ends with ], then []
            // are correctly balanced.
            if (keys[0].indexOf('[') >= 0 && /\]$/.test(keys[last])) {
                // Remove the trailing ] from the last keys part.
                keys[last] = keys[last].replace(/\]$/, '');
                // Split first keys part into two parts on the [ and add them back onto
                // the beginning of the keys array.
                keys = keys.shift().split('[').concat(keys);
                // Since a key part was added, increment last.
                last++;
            } else {
                // Basic 'foo' style key.
                last = 0;
            }

            if ($.isFunction(reviver)) {
                // If a reviver function was passed, use that function.
                value = reviver(key, value);
            } else if (reviver) {
                // If true was passed, use the built-in $.deparam.reviver function.
                value = deparam.reviver(key, value);
            }

            if (last) {
                // Complex key, like 'a[]' or 'a[b][c]'. At this point, the keys array
                // might look like ['a', ''] (array) or ['a', 'b', 'c'] (object).
                for (; i <= last; i++) {
                    // If the current key part was specified, use that value as the array
                    // index or object key. If omitted, assume an array and use the
                    // array's length (effectively an array push).
                    key = keys[i] !== '' ? keys[i] : current.length;
                    if (i < last) {
                        // If not the last key part, update the reference to the current
                        // object/array, creating it if it doesn't already exist AND there's
                        // a next key. If the next key is non-numeric and not empty string,
                        // create an object, otherwise create an array.
                        current = current[key] = current[key] || (isNaN(keys[i + 1]) ? {} : []);
                    } else {
                        // If the last key part, set the value.
                        current[key] = value;
                    }
                }
            } else {
                // Simple key.
                if ($.isArray(result[key])) {
                    // If the key already exists, and is an array, push the new value onto
                    // the array.
                    result[key].push(value);
                } else if (key in result) {
                    // If the key already exists, and is NOT an array, turn it into an
                    // array, pushing the new value onto it.
                    result[key] = [result[key], value];
                } else {
                    // Otherwise, just set the value.
                    result[key] = value;
                }
            }
        });

        return result;
    };

    // Default reviver function, used when true is passed as the second argument
    // to $.deparam. Don't like it? Pass your own!
    deparam.reviver = function (key, value) {
        var specials = {
            'true': true,
            'false': false,
            'null': null,
            'undefined': undef
        };

        return (+value + '') === value ? +value // Number
            : value in specials ? specials[value] // true, false, null, undefined
            : value; // String
    };

}(jQuery));
// IE9
if (window.history === undefined)
    window.history = {};
if (window.history.pushState === undefined)
    window.history.pushState = function () {
    };
if (window.history.replaceState === undefined)
    window.history.replaceState = function () {
    };
if (window.onpopstate === undefined)
    window.onpopstate = function () {
    };

// polyfills
Number.isInteger = Number.isInteger || function (value) {
        return typeof value === "number" &&
            isFinite(value) &&
            Math.floor(value) === value;
    };

// ECMAScript 6
if (!String.prototype.startsWith) {
    String.prototype.startsWith = function (searchString, position) {
        position = position || 0;
        return this.lastIndexOf(searchString, position) === position;
    };
}

if (!Array.prototype.find) {
    Object.defineProperty(Array.prototype, 'find', {
        value: function (predicate) {
            'use strict';
            if (this == null) {
                throw new TypeError('Array.prototype.find called on null or undefined');
            }
            if (typeof predicate !== 'function') {
                throw new TypeError('predicate must be a function');
            }
            var list = Object(this);
            var length = list.length >>> 0;
            var thisArg = arguments[1];
            var value;

            for (var i = 0; i < length; i++) {
                value = list[i];
                if (predicate.call(thisArg, value, i, list)) {
                    return value;
                }
            }
            return undefined;
        }
    });
}

Number.MAX_SAFE_INTEGER = Number.MAX_SAFE_INTEGER || 9007199254740991;
Number.MIN_SAFE_INTEGER = Number.MIN_SAFE_INTEGER || -9007199254740991;