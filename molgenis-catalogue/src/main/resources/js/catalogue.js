(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	function createEntityMetaTree(entityMetaData, attributes) {
		var container = $('#attribute-selection');
		container.tree({
			entityMetaData: entityMetaData,
			selectedAttributes: attributes,
			onAttributesSelect: function(selects) {
			},
			onAttributeClick: function(attribute) {
			}
		});
	}
	
	$(function() {
		$('#entity-select').on('change', function() {
			var entityUri = $(this).val();
			restApi.getAsync(entityUri + '/meta', {'expand': ['attributes']}, function(entityMetaData) {
				var selectedAttributes = $.map(entityMetaData.attributes, function(attribute) {
					return attribute.fieldType !== 'COMPOUND' ? attribute : null;
				});
				
				createEntityMetaTree(entityMetaData, selectedAttributes);
			});
		});
		
		$('#entity-select').change();
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));
