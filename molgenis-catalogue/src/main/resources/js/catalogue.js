(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	function createEntityMetaTree(entityMetaData) {
		$('#attribute-selection').tree({
			entityMetaData: entityMetaData,
			onAttributesSelect: function(selects) {
				refreshShoppingCart();
			},
			onAttributeClick: function(attribute) {
				createAttributeMetadataTable(attribute);
			}
		});
	}
	
	function refreshShoppingCart() {
		var attributes = $('#attribute-selection').tree('getSelectedAttributes');
		console.log(JSON.stringify(attributes));
	}
	
	function createAttributeMetadataTable(attributeMetadata) {
		$('#attributes-table').attributeMetadataTable({
			attributeMetadata: attributeMetadata
		});
	}
	
	function createHeader(entityMetaData) {
		$('#entity-class-name').html(entityMetaData.label);
		
		if (entityMetaData.description) {
			var description = $('<span data-placement="bottom"></span>');
			description.html(abbreviate(entityMetaData.description, 180));
			description.attr('data-title', entityMetaData.description);
			$('#entity-class-description').html(description.tooltip());
		} else {
			$('#entity-class-description').html('');
		}
	}
	
	function getFirstAttribute(entityMetaData) {
		for (var name in entityMetaData.attributes)
			return entityMetaData.attributes[name];
	}
	
	$(function() {
		$('#entity-select').on('change', function() {
			var entityUri = $(this).val();
			restApi.getAsync(entityUri + '/meta', {'expand': ['attributes']}, function(entityMetaData) {
				createHeader(entityMetaData);
				createEntityMetaTree(entityMetaData);
					
				var firstAttr = getFirstAttribute(entityMetaData);
				if (firstAttr.fieldType !== 'COMPOUND') {
					$('#attributes-table').attributeMetadataTable({
						attributeMetadata: firstAttr 
					});
				} else {
					$('#attributes-table').html('');
				}
			});
		});
		
		$('#entity-select').change();
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));
