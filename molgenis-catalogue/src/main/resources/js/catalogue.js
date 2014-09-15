(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	function createEntityMetaTree(entityMetaData) {
		$('#attribute-selection').tree({
			entityMetaData: entityMetaData,
			onAttributesSelect: function(selects) {
			},
			onAttributeClick: function(attribute) {
				$('#attributes-table').attributeMetadataTable({
					attributeMetadata:attribute
				});
			}
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
	
	$(function() {
		$('#entity-select').on('change', function() {
			var entityUri = $(this).val();
			restApi.getAsync(entityUri + '/meta', {'expand': ['attributes']}, function(entityMetaData) {
				createHeader(entityMetaData);
				createEntityMetaTree(entityMetaData);
			});
		});
		
		$('#entity-select').change();
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));
