(function($, molgenis) {
	"use strict";

    // workaround for "Uncaught RangeError: Maximum call stack size exceeded"
    // http://stackoverflow.com/a/19190216
    $.fn.modal.Constructor.prototype.enforceFocus = function() {};
    
	molgenis.setContextUrl = function(contextUrl) {
		molgenis.contextUrl = contextUrl;
	};

	molgenis.getContextUrl = function() {
		return molgenis.contextUrl;
	};

	molgenis.createAlert = function(alerts, type, container) {
		if (type !== 'error' && type !== 'warning' && type !== 'success')
			type = 'error';
		if (container === undefined) {
			container = $('.alerts');
			container.empty();
		}

		var items = [];
		items.push('<div class="alert alert-');
		items.push(type);
		items
				.push('"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>');
		items.push(type.charAt(0).toUpperCase() + type.slice(1));
		items.push('!</strong> ');
		$.each(alerts, function(i, alert) {
			if (i > 0)
				items.push('<br/>');
			items.push('<span>' + alert.message + '</span>');
		});
		items.push('</div>');

		container.prepend(items.join(''));
	};

	molgenis.i18n = molgenis.i18n || {};
	molgenis.i18n.get = function(str, lang) {
		lang = typeof lang !== 'undefined' ? lang : 'en';
		var i18nObj;
		if (str
				&& (str.charAt(0) !== '{' || str.charAt(str.length - 1) !== '}'))
			i18nObj = {
				'en' : str
			};
		else
			i18nObj = JSON.parse(str ? str : '{}');
		return i18nObj[lang];
	};
	molgenis.i18n.getAll = function(str, lang) {
		lang = typeof lang !== 'undefined' ? lang : 'en';
		var i18nObj;
		if (str
				&& (str.charAt(0) !== '{' || str.charAt(str.length - 1) !== '}'))
			i18nObj = {
				'en' : str
			};
		else
			i18nObj = JSON.parse(str ? str : '{}');
		return i18nObj;
	};

	/*
	 * Create a datasets indexer alert when indexer is running.
	 */
	molgenis.createDatasetsindexerAlert = function() {
		$.get("/dataindexerstatus", function(response) {
			if (response && response.isRunning === true) {
				showDatasetsindexerStatusMessage();
			}
		});

		function showDatasetsindexerStatusMessage() {
			$.get("/dataindexerstatus", function(response) {
				$('.datasetsindexerAlerts').empty();
				if (response.isRunning === true) {
					setTimeout(showDatasetsindexerStatusMessage, 3000);
				}
				molgenis.createAlert([ {
					'message' : response.message
				} ], response.type, $('.datasetsindexerAlerts'));
			});
		}
	};

	/**
	 * Returns all atomic attributes. In case of compound attributes (attributes consisting of multiple atomic
	 * attributes) only the descendant atomic attributes are returned. The compound attribute itself is not returned.
	 * 
	 * @param attributes
	 * @param restClient
	 */
	molgenis.getAtomicAttributes = function(attributes, restClient) {
		var atomicAttributes = [];
		function createAtomicAttributesRec(attributes) {
			$.each(attributes, function(i, attribute) {
				if(attribute.fieldType === 'COMPOUND'){
					// FIXME improve performance by retrieving async 
					attribute = restClient.get(attribute.href, {'expand': ['attributes']});
					createAtomicAttributesRec(attribute.attributes);
				}
					else
						atomicAttributes.push(attribute);
			});	
		}
		createAtomicAttributesRec(attributes);
		return atomicAttributes;
	};

	/**
	 * Returns all compound attributes. In case of compound attributes (attributes consisting of multiple atomic
	 * attributes) only the descendant atomic attributes are returned. The compound attribute itself is not returned.
	 * 
	 * @param attributes
	 * @param restClient
	 */
	molgenis.getCompoundAttributes = function(attributes, restClient) {
		var compoundAttributes = [];
		function createAtomicAttributesRec(attributes) {
			$.each(attributes, function(i, attribute) {
				if(attribute.fieldType === 'COMPOUND'){
					// FIXME improve performance by retrieving async 
					attribute = restClient.get(attribute.href, {'expand': ['attributes']});
					compoundAttributes.push(attribute);
					createAtomicAttributesRec(attribute.attributes);
				}
			});	
		}
		createAtomicAttributesRec(attributes);
		return compoundAttributes;
	};
	
	/*
	 * Natural Sort algorithm for Javascript - Version 0.7 - Released under MIT license
	 * Author: Jim Palmer (based on chunking idea from Dave Koelle)
	 *
	 * https://github.com/overset/javascript-natural-sort
	 */
	molgenis.naturalSort = function(a, b) {
		var re = /(^-?[0-9]+(\.?[0-9]*)[df]?e?[0-9]?$|^0x[0-9a-f]+$|[0-9]+)/gi, sre = /(^[ ]*|[ ]*$)/g, dre = /(^([\w ]+,?[\w ]+)?[\w ]+,?[\w ]+\d+:\d+(:\d+)?[\w ]?|^\d{1,4}[\/\-]\d{1,4}[\/\-]\d{1,4}|^\w+, \w+ \d+, \d{4})/, hre = /^0x[0-9a-f]+$/i, ore = /^0/, i = function(
				s) {
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
		for ( var cLoc = 0, numS = Math.max(xN.length, yN.length); cLoc < numS; cLoc++) {
			// find floats not starting with '0', string or 0 if not defined (Clint Priest)
			oFxNcL = !(xN[cLoc] || '').match(ore) && parseFloat(xN[cLoc])
					|| xN[cLoc] || 0;
			oFyNcL = !(yN[cLoc] || '').match(ore) && parseFloat(yN[cLoc])
					|| yN[cLoc] || 0;
			// handle numeric vs string comparison - number < string - (Kyle Adams)
			if (isNaN(oFxNcL) !== isNaN(oFyNcL)) {
				return (isNaN(oFxNcL)) ? 1 : -1;
			}
			// rely on string comparison if different types - i.e. '02' < 2 != '02' < '2'
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
	String.prototype.endsWith = function(suffix) {
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

function htmlEscape(text) {
	return $('<div/>').text(text).html();
}

/*
 * Create a table cell to show data of a certain type
 * Is used by the dataexplorer and the forms plugin
 */
function formatTableCellValue(value, dataType) {
	if (!value) {
		return '';
	}
	
	if (dataType.toLowerCase() == "hyperlink") {
		value = '<a target="_blank" href="' + value + '">' + htmlEscape(value)
				+ '</a>';

	} else if (dataType.toLowerCase() == "email") {
		value = '<a href="mailto:' + value + '">' + htmlEscape(value) + '</a>';

	} else if (dataType.toLowerCase() == 'bool') {
		var checked = (value === true);
		value = '<input type="checkbox" disabled="disabled" ';
		if (checked) {
			value = value + 'checked ';
		}

		value = value + '/>';

	} else if (dataType.toLowerCase() != 'html') {

		if (value.length > 50) {
			var abbr = htmlEscape(abbreviate(value, 50));
			value = '<span class="show-popover"  data-content="'
					+ htmlEscape(value) + '" data-toggle="popover">' + abbr
					+ "</span>";
		} else {
			value = htmlEscape(value);
		}

	} else {
		value = htmlEscape(value);
	}

	return value;
}

/**
 * Is s is longer then maxLength cut it and add ...
 * @param s
 * @param maxLength
 */
function abbreviate(s, maxLength) {
	if (s.length <= maxLength) {
		return s;
	}
	
	return s.substr(0, maxLength-3) + '...';
}

/**
 * Create input element for a molgenis data type
 * 
 * @param dataType molgenis data type
 * @param attrs input attributes
 * @param val input value
 * @param lbl input label (for checkbox and radio inputs)
 */
function createInput(dataType, attrs, val, lbl) {
	function createBasicInput(type, attrs, val) {
		var $input = $('<input type="' + type + '">');
		if(attrs)
			$input.attr(attrs);
		if(val !== undefined)
			$input.val(val);
		return $input;
	}
	
	switch(dataType) {
		case 'BOOL':
			var label = $('<label class="radio">');
			var $input = createBasicInput('radio', attrs, val); 
			return label.append($input).append(val ? 'True' : 'False');
		case 'CATEGORICAL':
			var label = $('<label class="checkbox">');
			var $input = createBasicInput('checkbox', attrs, val); 
			return label.append($input).append(lbl);
		case 'DATE':
		case 'DATE_TIME':
			var format = dataType === 'DATE' ? 'yyyy-MM-dd' : 'yyyy-MM-dd\'T\'hh:mm:ss' + getCurrentTimezoneOffset();
			var items = [];
			items.push('<div class="input-append date">');
			items.push('<input data-format="' + format + '" data-language="en" type="text">');
			items.push('<span class="add-on">');
			items.push('<i data-time-icon="icon-time" data-date-icon="icon-calendar"></i>');
			items.push('</span>');
			items.push('</div>');
			var datepicker = $(items.join(''));
			if(attrs)
				$('input', datepicker).attr(attrs);
			if(val !== undefined)
				$('input', datepicker).val(val);
			return datepicker.datetimepicker();
		case 'DECIMAL':
		case 'INT':
		case 'LONG':
			return createBasicInput('number', attrs, val);
		case 'EMAIL':
			return createBasicInput('email', attrs, val);
		case 'HTML':
		case 'HYPERLINK':
		case 'STRING':
		case 'TEXT':
		case 'ENUM':
			return createBasicInput('text', attrs, val);
		case 'MREF':
		case 'XREF':
			return createBasicInput('hidden', attrs, val);
		case 'FILE':
		case 'IMAGE':
			throw 'Unsupported data type: ' + dataType;
		default:
			throw 'Unknown data type: ' + dataType;
	}
}

// molgenis entity REST API client
(function($, molgenis) {
	"use strict";
	var self = molgenis.RestClient = molgenis.RestClient || {};
	
	molgenis.RestClient = function RestClient() {};

	molgenis.RestClient.prototype.get = function(resourceUri, options) {
		return this._get(resourceUri, options);
	};
	
	molgenis.RestClient.prototype.getAsync = function(resourceUri, options, callback) {
		this._get(resourceUri, options, callback);
	};

	molgenis.RestClient.prototype._get = function(resourceUri, options, callback) {
		var resource = null;

		var async = callback !== undefined;
		
		var config = {
			'dataType' : 'json',
			'url' : this._toApiUri(resourceUri, options),
			'cache' : true,
			'async' : async,
			'success' : function(data) {
				if (async)
					callback(data);
				else
					resource = data;
			}
		};
		
		// tunnel get requests with query through a post,
		// because it might not fit in the URL
		if (options && options.q) {
			$.extend(config, {
				'type' : 'POST',
				'data' : JSON.stringify(options.q),
				'contentType' : 'application/json'
			});
		}
		
		if (self.token) {
			$.extend(config, {
				headers: {'x-molgenis-token': self.token}
			});
		}
		
		$.ajax(config);
		
		if (!async)
			return resource;
	};
	
	molgenis.RestClient.prototype._toApiUri = function(resourceUri, options) {
		var qs = "";
		if (resourceUri.indexOf('?') != -1) {
			var uriParts = resourceUri.split('?');
			resourceUri = uriParts[0];
			qs = '?' + uriParts[1];
		}
		if (options && options.attributes && options.attributes.length > 0)
			qs += (qs.length === 0 ? '?' : '&') + 'attributes=' + options.attributes.join(',');
		if (options && options.expand && options.expand.length > 0)
			qs += (qs.length === 0 ? '?' : '&') + 'expand=' + options.expand.join(',');
		if (options && options.q)
			qs += (qs.length === 0 ? '?' : '&') + '_method=GET';
		return resourceUri + qs;
	};

	molgenis.RestClient.prototype.getPrimaryKeyFromHref = function(href) {
		return href.substring(href.lastIndexOf('/') + 1);
	};

	molgenis.RestClient.prototype.getHref = function(entityName, primaryKey) {
		return '/api/v1/' + entityName + (primaryKey ? '/' + primaryKey : '');
	};
	
	molgenis.RestClient.prototype.remove = function(href, callback) {
		$.ajax({
			type : 'POST',
			url : href,
			data : '_method=DELETE',
			async : false,
			success : callback.success,
			error : callback.error
		});
	};
	
	molgenis.RestClient.prototype.entityExists = function(resourceUri) {
		var result = false;
		$.ajax({
			dataType : 'json',
			url : resourceUri + '/exist',
			async : false,
			success : function(exists) {
				result = exists;
			}
		});
		
		return result;
	};
	
	molgenis.RestClient.prototype.login = function(username, password, callback) {
		$.ajax({
			type: 'POST',
			dataType : 'json',
			url : '/api/v1/login',
			contentType : 'application/json',
			async : true,
			data: JSON.stringify({username: username, password: password}),
			success : function(loginResult) {
				self.token = loginResult.token;
				callback.success({
					username: loginResult.username,
					firstname: loginResult.firstname,
					lastname: loginResult.lastname
				});
			},
			error : callback.error
		});
	};
	
	molgenis.RestClient.prototype.logout = function(callback) {
		$.ajax({
			url : '/api/v1/logout',
			async : true,
			headers: {'x-molgenis-token': self.token},
			success : function() {
				self.token = null;
				callback();
			}
		});
	};
	
}($, window.top.molgenis = window.top.molgenis || {}));

// molgenis search API client
(function($, molgenis) {
	"use strict";

	molgenis.SearchClient = function SearchClient() {
	};

	molgenis.SearchClient.prototype.search = function(searchRequest, callback) {
		var jsonRequest = JSON.stringify(searchRequest);
		
		$.ajax({
			type : "POST",
			url : '/search',
			data : jsonRequest,
			contentType : 'application/json',
			success : function(searchResponse) {
				if (searchResponse.errorMessage) {
					alert(searchResponse.errorMessage);
				}
				callback(searchResponse);
			},
			error : function(xhr) {
				alert(xhr.responseText);
			}
		});
	};
}($, window.top.molgenis = window.top.molgenis || {}));

function showSpinner(callback) {
	var spinner = $('#spinner');
	if (spinner.length === 0) {
		// do not add fade effect on modal: http://stackoverflow.com/a/22101894
		var items = [];
		items.push('<div id="spinner" class="modal hide" data-backdrop="static">');
		items.push('<div class="modal-header"><h3>Loading ...</h3></div>');
		items.push('<div class="modal-body"><div class="modal-body-inner"><img src="/img/waiting-spinner.gif"></div></div>');
		items.push('</div>');
		$('body').append(items.join(''));
		spinner = $('#spinner');
		spinner.data('count', 0);
	}
	
	if (callback) {
		spinner.on('shown', function() {
			callback();
		});
	}
	
	var count = $('#spinner').data('count');
	if(count === 0) {
		var timeout = setTimeout(function(){ spinner.modal('show'); }, 500);
		$('#spinner').data('timeout', timeout);
		$('#spinner').data('count', 1);
	} else {
		$('#spinner').data('count', count + 1);
	}
}

function hideSpinner() {
	if ($('#spinner').length !== 0) {
		var count = $('#spinner').data('count');
		if(count === 1) {
			clearTimeout($('#spinner').data('timeout'));
			$('#spinner').modal('hide');
		}
		if (count > 0) {
			$('#spinner').data('count', count - 1);
		}
	}
}

$(function() {
	// disable all ajax request caching
	$.ajaxSetup({
		cache : false
	});

	// use ajaxPrefilter instead of ajaxStart and ajaxStop
	// to work around issue http://bugs.jquery.com/ticket/13680
	$.ajaxPrefilter(function( options, _, jqXHR ) {
	    showSpinner();
	    jqXHR.always( hideSpinner );
	});

	$(document).ajaxError(function(event, xhr, settings, e) {
		try {
			molgenis.createAlert(JSON.parse(xhr.responseText).errors);
		} catch(e) {
			molgenis.createAlert([{'message': 'An error occurred. Please contact the administrator.'}], 'error');
		}
	});
	
	window.onerror = function(msg, url, line) {
		molgenis.createAlert([{'message': 'An error occurred. Please contact the administrator.'}, {'message': msg}], 'error');
	};
	
	// async load bootstrap modal and display
	$(document).on('click', 'a.modal-href', function(e) {
		e.preventDefault();
		e.stopPropagation();
		if (!$(this).hasClass('disabled')) {
			var container = $('#' + $(this).data('target'));
			if (container.is(':empty')) {
				container.load($(this).attr('href'), function() {
					$('.modal:first', container).modal('show');
				});
			} else {
				$('.modal:first', container).modal('show');
			}
		}
	});
	
	/**
	 * Add download functionality to JQuery.
	 * data can be string of parameters or array/object
	 *
	 * Default method is POST
	 *
	 * Usage:
	 * <code>download('/localhost:8080', 'param1=value1&param2=value2')</code> Or:
	 * <code>download('/localhost:8080', {param1 : 'value1', param2 : 'value2'})</code>
	 *
	 */
	$.download = function(url, data, method) {
		if (!method) {
			method = 'POST';
		}

		data = typeof data == 'string' ? data : $.param(data);

		//split params into form inputs
		var inputs = [];
		$.each(data.split('&'), function() {
			var pair = this.split('=');
			inputs.push('<input type="hidden" name="' + pair[0] + '" value="'
					+ pair[1] + '" />');
		});

		//send request and remove form from dom
		$('<form action="' + url + '" method="' + method + '">').html(
				inputs.join('')).appendTo('body').submit().remove();
	};
});
