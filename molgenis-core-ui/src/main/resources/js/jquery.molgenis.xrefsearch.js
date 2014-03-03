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
		var refEntityMetaData = restApi.get(entityUri + '?attributes=attributes&expand=attributes');
		
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
		
		$.each(lookupAttributeNames, function(index, attrName) {
			items.push('<div class="span6">');
			items.push(attrName);
			items.push(': <b>');
			items.push(entity[attrName]);
			items.push('</b>');
			items.push('</div>');
		});

		items.push('</div>');
		
		return items.join('');
	}
	
	function createSelect2(container, metaData) {
		var lookupAttrNames = getLookupAttributeNames(metaData.refEntity.href);
		
		container.select2({
			width: 570,
			placeholder: 'Search...',
			minimumInputLength: 2,
			ajax: {
				quietMillis: 200,
				dataType: 'json',
				url: '/api/v1/' + metaData.refEntity.name + '?_method=GET',
				type: 'POST',
				params : {contentType: 'application/json;charset=utf-8'},
				data: function(term, page) {
					var q = createQuery(lookupAttrNames, term);
					return JSON.stringify({num : 10, q : q});
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
				return entity[metaData.refEntity.labelAttribute];
			},
			id: function(entity) {
				return entity[metaData.refEntity.labelAttribute];
			}
		});
		
	}
	
	$.fn.xrefsearch = function(options) {
		var container = this;
	
		restApi.getAsync(options.featureUri, {attributes:['refEntity'], expand:['refEntity']}, function(metaData) {
			createSelect2(container, metaData);
		});
		
		return this;
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));