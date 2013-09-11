// Add endsWith function to the string class
if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
}

$(function() {
	// disable all ajax request caching
	$.ajaxSetup({
	  cache: false
	});
	// async load bootstrap modal and display
	$(document).on('click', 'a.modal-href', function(e) {
		e.preventDefault();
		e.stopPropagation();
		if(!$(this).hasClass('disabled')) {
			var container = $('#' + $(this).data('target')); 
			if(container.is(':empty')) {
				container.load($(this).attr('href'), function() {
					$('.modal:first', container).modal('show');
				});
			} else {
				$('.modal:first', container).modal('show');
			}
		}
	});
});

// molgenis entity REST API client
(function($, w) {
	"use strict";

	var molgenis = w.molgenis = w.molgenis || {};

	molgenis.RestClient = function RestClient(cache) {
		this.cache = cache === false ? null : [];
	};

	molgenis.RestClient.prototype.get = function(resourceUri, expands, q) {
		var apiUri = this._toApiUri(resourceUri, expands, q);
		var cachedResource = this.cache && this.cache[apiUri];
		if (!cachedResource) {
			var _this = this;
			if(q) {
				$.ajax({
					type : 'POST',
					dataType : 'json',
					url : apiUri,
					cache: true,
					data : JSON.stringify(q),
					contentType : 'application/json',
					async : false,
					success : function(resource) {
						if (_this.cache) _this._cachePut(resourceUri, resource, expands);
						cachedResource = resource;	
					}
				});
			} else {
				$.ajax({
					dataType : 'json',
					url : apiUri,
					cache: true,
					async : false,
					success : function(resource) {
						if (_this.cache) _this._cachePut(resourceUri, resource, expands);
						cachedResource = resource;
					}
				});
			}
		}
		return cachedResource;
	};

	molgenis.RestClient.prototype.getAsync = function(resourceUri, expands, q, callback) {
		var apiUri = this._toApiUri(resourceUri, expands, q);
		var cachedResource = this._cacheGet[apiUri];
		if (cachedResource) {
			callback(cachedResource);
		} else {
			var _this = this;
			if(q) {
				$.ajax({
					type : 'POST',
					dataType : 'json',
					url : apiUri,
					cache: true,
					data : JSON.stringify(q),
					contentType : 'application/json',
					async : true,
					success : function(resource) {
						_this._cachePut(resourceUri, resource, expands);
						callback(resource);	
					}
				});
			} else {
				$.ajax({
					dataType : 'json',
					url : apiUri,
					cache: true,
					async : true,
					success : function(resource) {
						_this._cachePut(resourceUri, resource, expands);
						callback(resource);
					}
				});
			}
		}
	};

	molgenis.RestClient.prototype._cacheGet = function(resourceUri) {
		return this.cache !== null ? this.cache[resourceUri] : null;
	};

	molgenis.RestClient.prototype._cachePut = function(resourceUri, resource, expands) {
		var apiUri = this._toApiUri(resourceUri, expands);
		this.cache[apiUri] = resource;
		if (resource.items) {
			for ( var i = 0; i < resource.items.length; i++) {
				var nestedResource = resource.items[i];
				this.cache[nestedResource.href] = nestedResource;
			}
		}
		if (expands) {
			this.cache[resourceUri] = resource;
			for ( var i = 0; i < expands.length; i++) {
				var expand = resource[expands[i]];
				if(expand){
					this.cache[expand.href] = expand;
					if (expand.items) {
						for ( var j = 0; j < expand.items.length; j++) {
							var expandedResource = expand.items[j];
							this.cache[expandedResource.href] = expandedResource;
						}
					}
				}
			}
		}
	};

	molgenis.RestClient.prototype._toApiUri = function(resourceUri, expands, q) {
		var qs = "";
		if (resourceUri.indexOf('?') != -1) {
			var uriParts = resourceUri.split('?');
			resourceUri = uriParts[0];
			qs = '?' + uriParts[1];
		}
		if(expands) qs += (qs.length == 0 ? '?' : '&') + 'expand=' + expands.join(',');
		if(q) qs += (qs.length == 0 ? '?' : '&') + '_method=GET';
		return resourceUri + qs;
	};
	
	molgenis.RestClient.prototype.getPrimaryKeyFromHref = function(href) {
		var uriParts = href.split("/");
		return uriParts[uriParts.length - 1];
	};
	
	molgenis.RestClient.prototype.remove = function(href, callback) {
		$.ajax({
			type: 'POST',
			url: href,
			data: '_method=DELETE',
			async: false,
			success: callback.success,
			error:callback.error
		});
	};
	
}($, window.top));

// molgenis search API client
(function($, w) {
	"use strict";

	var molgenis = w.molgenis = w.molgenis || {};

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
			}
		});
	};
}($, window.top));

function toggleDiv(div, image)
{
	if (document.getElementById(div).style.display == "block")
	{
		document.getElementById(image).src = "res/img/open.png";
		document.getElementById(div).style.display = "none";
	}
	else
	{
		document.getElementById(image).src = "res/img/close.png";
		document.getElementById(div).style.display = "block";
	}
}

function moveDivHorizontal()
{
	x = 0;
	w = "100%";
	if ( typeof(window.pageXOffset) == 'number'){x = window.pageXOffset;}
	else if (typeof(document.body.scrollLeft) == 'number'){x = document.body.scrollLeft; w = "auto";}
	else if (typeof(document.documentElement.scrollLeft) == 'number'){x = document.documentElement.scrollLeft;}
		
	for (i = 0; i < headersArray.length; i++)
	{
		document.getElementById(headersArray[i]).style.marginLeft = x;
		document.getElementById(headersArray[i]).style.width = w;
	}
}


//check form input
function validateForm(form, fields) 
{
	alertstring = "";
	
	for (i = 0; i < fields.length; i++) {
		if (fields[i].value == "")
		{
			alertstring += fields[i].name + "\n";
		}
	}
	if (alertstring == "")
	{
		return true;
	}
	else
	{
		alert("Fields marked with * are required. Please provide: \n" + alertstring);
		return false;
	}
}

//alter form input
function setInput(form, targetv, actionv, __targetv, __actionv, __showv)
{
	document.getElementById(form).target = targetv;
	document.getElementById(form).action = actionv;
	document.getElementById(form).__target.value = __targetv;
	document.getElementById(form).__action.value = __actionv;
	document.getElementById(form).__show.value = __showv;
}

function checkAll(formname, inputname)
{
	forminputs = document.getElementById(formname).getElementsByTagName('input');
	for (i = 0; i < forminputs.length; i++) 
	{
		if (forminputs[i].name == inputname && !forminputs[i].disabled) 
		{
			forminputs[i].checked = document.getElementById(formname).checkall.checked;
		}
	}
}

function toggleCssClass(cssClass) 
{
	var cssRules = new Array();
	var ff = true;
	if (document.styleSheets[0].cssRules) 
	{
		cssRules = document.styleSheets[0].cssRules;
	} 
	else if (document.styleSheets[0].rules) 
	{
		ff = false;
		cssRules = document.styleSheets[0].rules;
	}
	
	missing = true;

	for (i=0; i< cssRules.length; i++)
	{
		if(cssRules[i].selectorText.toLowerCase() == "."+cssClass.toLowerCase())
		{ 

			if(document.styleSheets[0].deleteRule)
				document.styleSheets[0].deleteRule(i);
			else 
				document.styleSheets[0].removeRule(i);
			missing = false;
			break;
		}
	}
	if(missing)
	{
		if(ff) document.styleSheets[0].insertRule("."+cssClass+"{display: none} ", cssRules.length - 1);
		else  document.styleSheets[0].addRule("."+cssClass,"display: none", cssRules.length - 1);
	}	
}

function showSpinner() 
{
	$('#spinner').modal('show');
}

function hideSpinner() 
{
	$('#spinner').modal('hide');
}

/*
 * Natural Sort algorithm for Javascript - Version 0.7 - Released under MIT license
 * Author: Jim Palmer (based on chunking idea from Dave Koelle)
 * 
 * https://github.com/overset/javascript-natural-sort
 */
 function naturalSort (a, b) {
    var re = /(^-?[0-9]+(\.?[0-9]*)[df]?e?[0-9]?$|^0x[0-9a-f]+$|[0-9]+)/gi,
        sre = /(^[ ]*|[ ]*$)/g,
        dre = /(^([\w ]+,?[\w ]+)?[\w ]+,?[\w ]+\d+:\d+(:\d+)?[\w ]?|^\d{1,4}[\/\-]\d{1,4}[\/\-]\d{1,4}|^\w+, \w+ \d+, \d{4})/,
        hre = /^0x[0-9a-f]+$/i,
        ore = /^0/,
        i = function(s) { return naturalSort.insensitive && (''+s).toLowerCase() || ''+s },
        // convert all to strings strip whitespace
        x = i(a).replace(sre, '') || '',
        y = i(b).replace(sre, '') || '',
        // chunk/tokenize
        xN = x.replace(re, '\0$1\0').replace(/\0$/,'').replace(/^\0/,'').split('\0'),
        yN = y.replace(re, '\0$1\0').replace(/\0$/,'').replace(/^\0/,'').split('\0'),
        // numeric, hex or date detection
        xD = parseInt(x.match(hre)) || (xN.length != 1 && x.match(dre) && Date.parse(x)),
        yD = parseInt(y.match(hre)) || xD && y.match(dre) && Date.parse(y) || null,
        oFxNcL, oFyNcL;
    // first try and sort Hex codes or Dates
    if (yD)
        if ( xD < yD ) return -1;
        else if ( xD > yD ) return 1;
    // natural sorting through split numeric strings and default strings
    for(var cLoc=0, numS=Math.max(xN.length, yN.length); cLoc < numS; cLoc++) {
        // find floats not starting with '0', string or 0 if not defined (Clint Priest)
        oFxNcL = !(xN[cLoc] || '').match(ore) && parseFloat(xN[cLoc]) || xN[cLoc] || 0;
        oFyNcL = !(yN[cLoc] || '').match(ore) && parseFloat(yN[cLoc]) || yN[cLoc] || 0;
        // handle numeric vs string comparison - number < string - (Kyle Adams)
        if (isNaN(oFxNcL) !== isNaN(oFyNcL)) { return (isNaN(oFxNcL)) ? 1 : -1; }
        // rely on string comparison if different types - i.e. '02' < 2 != '02' < '2'
        else if (typeof oFxNcL !== typeof oFyNcL) {
            oFxNcL += '';
            oFyNcL += '';
        }
        if (oFxNcL < oFyNcL) return -1;
        if (oFxNcL > oFyNcL) return 1;
    }
    return 0;
}
 $(function() {
	$(document).on('molgenis-login', function(e, msg) {
		window.location.href=window.location.href;
	});
 });

 $(function() {
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
			 inputs.push('<input type="hidden" name="' + pair[0] + '" value="' + pair[1] + '" />'); 
		 });
			
		 //send request and remove form from dom
		 $('<form action="' + url +'" method="' + method + '">').html(inputs.join('')).appendTo('body').submit().remove();
	 }; 
 });