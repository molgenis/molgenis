$(function() {
	// async load bootstrap modal and display
	$(document).on('click', 'a.modal-href', function(e) {
		e.preventDefault();
		e.stopPropagation();
		var container = $('#' + $(this).data('target')); 
		if(container.is(':empty')) {
			container.load($(this).attr('href'), function() {
				$('.modal:first', container).modal('show');
			});
		} else {
			$('.modal:first', container).modal('show');
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

	molgenis.RestClient.prototype.get = function(resourceUri, expands) {
		var apiUri = this._toApiUri(resourceUri, expands);
		var cachedResource = this.cache && this.cache[apiUri];
		if (cachedResource) {
			console.log('retrieved ' + apiUri + ' from cache', cachedResource);
		} else {
			var _this = this;
			$.ajax({
				dataType : 'json',
				url : apiUri,
				async : false,
				success : function(resource) {
					console.log('retrieved ' + apiUri + ' from server', resource);
					_this._cachePut(resourceUri, resource, expands);
					cachedResource = resource;
				}
			});
		}
		return cachedResource;
	};

	molgenis.RestClient.prototype.getAsync = function(resourceUri, expands, callback) {
		var apiUri = this._toApiUri(resourceUri, expands);
		var cachedResource = this._cacheGet[apiUri];
		if (cachedResource) {
			console.log('retrieved ' + apiUri + ' from cache', cachedResource);
			callback(cachedResource);
		} else {
			var _this = this;
			$.ajax({
				dataType : 'json',
				url : apiUri,
				async : true,
				success : function(resource) {
					console.log('retrieved ' + apiUri + ' from server', resource);
					_this._cachePut(resourceUri, resource, expands);
					callback(resource);
				}
			});
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
				this.cache[expand.href] = expand;
				if (expand.items) {
					for ( var j = 0; j < expand.items.length; j++) {
						var expandedResource = expand.items[j];
						this.cache[expandedResource.href] = expandedResource;
					}
				}
			}
		}
	};

	molgenis.RestClient.prototype._toApiUri = function(resourceUri, expands) {
		return expands ? resourceUri + '?expand=' + expands.join(',') : resourceUri;
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
		console.log("Call SearchService json=" + jsonRequest);

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
