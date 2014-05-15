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
	
	function createQuery(lookupAttributeNames, terms, operator) {
		var q = [];
		
		$.each(lookupAttributeNames, function(index, attrName) {
			if (q.length > 0) {
				q.push({operator: 'OR'});
			}
            if (terms.length > 0) {
                $.each(terms, function(index) {
                    if(index > 0){
                        q.push({operator: 'OR'});
                    }
                    q.push({
                        field: attrName,
                        operator: operator,
                        value: terms[index]
                    });
                });
            }
		});
			
		return q;
	}
	
	function getLookupAttributeNames(entityMetaData) {
		var attributeNames = [];
		$.each(entityMetaData.attributes, function(attrName, attr) {
			if (attr.lookupAttribute === true) {
				attributeNames.push(attr.name);
			}
		});
		return attributeNames;
	}
	
	function formatResult(entity, entityMetaData, lookupAttributeNames) {
		var items = [];
		items.push('<div class="row-fluid">');
		
		if (lookupAttributeNames.length > 0) {
			var width = Math.round(12 / lookupAttributeNames.length);// 12 is full width in px
		
			$.each(lookupAttributeNames, function(index, attrName) {
				var attrLabel = entityMetaData.attributes[attrName].label || attrName;
				var attrValue = entity[attrName] == undefined ?  '' :  entity[attrName];
				items.push('<div class="span' + width + '">');
				items.push(attrLabel + ': <b>' + htmlEscape(attrValue) + '</b>');
				items.push('</div>');
			});
		}
		
		items.push('</div>');
		
		return items.join('');
	}

	function createSelect2(container, attributeMetaData, options) {
		var refEntityMetaData = restApi.get(attributeMetaData.refEntity.href, {expand: ['attributes']});
		var lookupAttrNames = getLookupAttributeNames(refEntityMetaData);
		var hiddenInput = container.find('input[type=hidden]');
	
		hiddenInput.select2({
			width: '75%',
			minimumInputLength: 2,
            multiple: (attributeMetaData.fieldType === 'MREF'),
			query: function (options){
				var query = createQuery(lookupAttrNames, [options.term],'LIKE');
				restApi.getAsync('/api/v1/' + refEntityMetaData.name, {q: {num: 10, q: query}}, function(data) {
					options.callback({results: data.items, more: false});
				});           
            },
			initSelection: function(element, callback) {
				//Only called when the input has a value
				var query = createQuery(lookupAttrNames, element.val().split(','), 'EQUALS');
				restApi.getAsync('/api/v1/' + refEntityMetaData.name, {q: {q: query}}, function(data) {
					callback(data.items);
				});
			},
			formatResult: function(entity) {
				return formatResult(entity, refEntityMetaData, lookupAttrNames);
			},
			formatSelection: function(entity) {
				return entity[refEntityMetaData.labelAttribute];
			},
			id: function(entity) {
				return entity[refEntityMetaData.labelAttribute];
			},
            separator: ',',
			dropdownCssClass: 'molgenis-xrefsearch'
		});
		
		if(!lookupAttrNames.length){
			container.append($("<label>lookup attribute is not defined.</label>"));
		}
	}

	function addQueryPartSelect(container, attributeMetaData, options) {
		var attrs = {
				'autofocus': 'autofocus'
			};

        if (attributeMetaData.fieldType === 'MREF') {
            var dropdown = $('<select class="operator"><option value="OR">OR</option><option value="AND">AND</option></select>');
            dropdown.val(options.operator);
            dropdown.width(70);
            container.prepend(dropdown);
        }
		
		var element = createInput(attributeMetaData.fieldType, attrs, options.values);
		container.prepend(element);
		createSelect2(container, attributeMetaData, options);
	}
	
	/**
	 * Creates a mref or xref select2 component
	 * 
	 * options = 	The options to create the filter 
	 * 				{
	 * 					attribute 	 ==> the meta data of the attribute
	 * 					operator ==> AND or OR or undefined
	 * 					values 	 ==> the values that are concatenate with a operator
	 *						1. example one: a AND b AND c
	 *						2. example two: a OR b OR c
	 * 					
	 * 				}
	 */
	$.fn.xrefsearch = function(options) {
		var container = this;
		var attributeUri = options.attributeUri ? options.attributeUri : options.attribute.href;
		
		restApi.getAsync(attributeUri, {attributes:['refEntity', 'fieldType'], expand:['refEntity']}, function(attributeMetaData) {
			    addQueryPartSelect(container, attributeMetaData, options);
		});
		
		return container;
	};
	
}($, window.top.molgenis = window.top.molgenis || {}));