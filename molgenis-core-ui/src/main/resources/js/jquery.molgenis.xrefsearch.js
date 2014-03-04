/**
 * An autocomplete search dropdown for xref and mref values for use in the filterdialog
 * 
 * usage: 
 * 
 * $('#id_of_hidden_input)').xrefsearch({attributeUri: 'api/v1/celiacsprue/meta/Celiac_Family'});
 * 
 * or
 * 
 * $('#id_of_hidden_input)').xrefsearch({attribute: attribute});
 * 
 * Depends on select2.js and molgenis.js
 */
(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();
	
	function createQuery(lookupAttributeNames, term) {
		var q = [];
		
		$.each(lookupAttributeNames, function(index, attrName) {
			if (q.length > 0) {
				q.push({operator: 'OR'});
			}
				
			q.push({
				field: attrName,
				operator: 'LIKE',
				value: term
			});
		});
			
		return q;
	}
	
	function getLookupAttributeNames(entityUri) {
		var attributeNames = [];
		var refEntityMetaData = restApi.get(entityUri,  {attributes: ['attributes'], expand: ['attributes']});
		
		$.each(refEntityMetaData.attributes, function(attrName, attr) {
			if (attr.lookupAttribute) {
				attributeNames.push(attr.name);
			}
		});
			
		return attributeNames;
	}
	
	function formatResult(entity, lookupAttributeNames) {
		var items = [];
		items.push('<div class="row-fluid">');
		
		if (lookupAttributeNames.length > 0) {
			var width = Math.round(12 / lookupAttributeNames.length);// 12 is full width in px
			var abbr = Math.round(110 / lookupAttributeNames.length);// 110 is full width in characters (if you don't change the font size)
			
			$.each(lookupAttributeNames, function(index, attrName) {
				var attrValue = entity[attrName] == undefined ?  '' :  entity[attrName];
				
				items.push('<div class="span' + width + '">');
				items.push(abbreviate(attrName + ': <b>' + attrValue + '</b>', abbr));
				items.push('</div>');
			});
		}
		
		items.push('</div>');
		
		return items.join('');
	}
	
	function createQueryTypeDropdown(container, attributeMetaData) {
		if (attributeMetaData.fieldType == 'MREF') {
			container.prepend('<select id="mref-query-type"><option value="OR">ANY match (OR)</option><option value="AND">ALL match (AND)</option></select>');
		}
	}
	
	function createSelect2(container, attributeMetaData) {
		var lookupAttrNames = getLookupAttributeNames(attributeMetaData.refEntity.href);
		var hiddenInput = container.find('input[type=hidden]');
		
		hiddenInput.select2({
			width: 670,
			placeholder: 'filter text',
			minimumInputLength: 2,
			ajax: {
				quietMillis: 200,
				dataType: 'json',
				url: '/api/v1/' + attributeMetaData.refEntity.name + '?_method=GET',
				type: 'POST',
				params : {contentType: 'application/json;charset=utf-8'},
				data: function(term, page) {
					var q = createQuery(lookupAttrNames, term);
					return JSON.stringify({num: 10, q: q});
				},
				results: function(data, page) {
					return {
						results: data.items,
						more: false
					};
				},
			},
			formatResult: function(entity) {
				return formatResult(entity, lookupAttrNames);
			},
			formatSelection: function(entity) {
				return entity[attributeMetaData.refEntity.labelAttribute];
			},
			id: function(entity) {
				return entity[attributeMetaData.refEntity.labelAttribute];
			}
		});
		
		var addButton = $('<a href="#" class="add-ref-query-part"><img src="/img/add.png" ></a>');
		container.append(addButton);
		
		addButton.on('click', function() {
			addQueryPartSelect(container, attributeMetaData);
		});
	}
	
	function addQueryPartSelect(container, attributeMetaData) {
		var attrs = {
				'placeholder': 'filter text',
				'autofocus': 'autofocus',
			};
			
		var element = createInput(attributeMetaData.fieldType, attrs, undefined)
		container.parent().append(element);
		createSelect2(element, attributeMetaData);
			
		var removeButton = $('<a href="#" class="remove-ref-query-part"><img src="/img/cancel.png" ></a>');
		container.find('.add-ref-query-part').replaceWith(removeButton);
		
		removeButton.on('click', function() {
			removeQueryPartSelect($(this).parent());
		});
	}
	
	function removeQueryPartSelect(element) {
		element.remove();
	}
	
	$.fn.xrefsearch = function(options) {
		var container = this;
		var attributeUri = options.attributeUri ? options.attributeUri : options.attribute.href;
		
		restApi.getAsync(attributeUri, {attributes:['refEntity', 'fieldType'], expand:['refEntity']}, function(attributeMetaData) {
			createQueryTypeDropdown(container, attributeMetaData);
			createSelect2(container, attributeMetaData);
		});
		
		return this;
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));