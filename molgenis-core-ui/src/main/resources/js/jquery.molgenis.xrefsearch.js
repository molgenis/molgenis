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
			var abbr = Math.round(100 / lookupAttributeNames.length);// 100 is full width in characters (if you don't change the font size)
		
			$.each(lookupAttributeNames, function(index, attrName) {
				var attrValue = entity[attrName] == undefined ?  '' :  entity[attrName];
				items.push('<div class="span' + width + '">');
				items.push(abbreviate(attrName + ': <b>' + htmlEscape(attrValue) + '</b>', abbr));
				items.push('</div>');
			});
		}
		
		items.push('</div>');
		
		return items.join('');
	}
	
	function createQueryTypeDropdown(container, attributeMetaData, operator) {
		if (attributeMetaData.fieldType == 'MREF') {
			var element = $('<select id="mref-query-type" class="operator"><option value="OR">ANY match (OR)</option><option value="AND">ALL match (AND)</option></select>');
			container.prepend(element);
			element.val(operator);
		}
	}
	
	function createSelect2(container, attributeMetaData) {
		var lookupAttrNames = getLookupAttributeNames(attributeMetaData.refEntity.href);
		var hiddenInput = container.find('input[type=hidden]');
		
		hiddenInput.select2({
			width: 650,
			placeholder: 'filter text',
			minimumInputLength: 2,
			query: function (options){
				var query = createQuery(lookupAttrNames, options.term);
				restApi.getAsync('/api/v1/' + attributeMetaData.refEntity.name, {q: {num: 10, q: query}}, function(data) {
					 options.callback({results: data.items, more: false});
				});           
            },
			initSelection: function(element, callback) {
				//Only called when the input has a value
				var query = createQuery(lookupAttrNames, element.val());
				restApi.getAsync('/api/v1/' + attributeMetaData.refEntity.name, {q: {num: 1, q: query}}, function(data) {
					callback(data.items[0]);
				});
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
		
		var addButton = $('<a href="#" class="add-ref-query-part" title="add new"><i class="icon-plus"></i></a>');
		container.append(addButton);
		
		addButton.on('click', function() {
			addQueryPartSelect(container, attributeMetaData);
		});
	}
	
	function addQueryPartSelect(container, attributeMetaData, value) {
		var attrs = {
				'placeholder': 'filter text',
				'autofocus': 'autofocus',
			};
			
		var element = createInput(attributeMetaData.fieldType, attrs, value);
		container.parent().append(element);
		createSelect2(element, attributeMetaData, value);
			
		var removeButton = $('<a href="#" class="remove-ref-query-part" title="remove"><i class="icon-remove"></i></a>');
		container.parent().find('.add-ref-query-part:not(:last)').replaceWith(removeButton);
		
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
			createQueryTypeDropdown(container, attributeMetaData, options.operator);
			
			if (options.values && options.values.length > 0) {
				
				//Preselect values
				for (var i = 0; i < options.values.length; i++) {
					addQueryPartSelect(container, attributeMetaData, options.values[i]);
				}
				
			} else {
				addQueryPartSelect(container, attributeMetaData);
				
			}
		});
		
		return this;
	};
	
}($, window.top.molgenis = window.top.molgenis || {}));