(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	var selectedEntity;
	
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
		var request = {
				entityName: selectedEntity.name,
				attributeNames: []
		}
		
		$.each(attributes, function(){
			request.attributeNames.push(this.name);
		});
		
		$.ajax({
			type: "POST",
			url: '/plugin/catalogue/shoppingcart',
			data: JSON.stringify(request),
			contentType: 'application/json'
		});
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
				selectedEntity = entityMetaData;
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
		
		$('#cart-button').click(function(){
			$('#cart-contents').load(
				'catalogue/shoppingcart/show', 
				{entityName: selectedEntity.name},
				function(){
					$('#cart-modal').modal('show');
				}
			);
		});
		
		$('#cart-modal').on('click', '.remove-attribute', function(){
			var attributeName = $(this).data('attribute-name');
			var $this = $(this);
			console.log('remove ' + attributeName);
			$.get('catalogue/shoppingcart/remove',
					{entityName: selectedEntity.name, attributeName: attributeName}, 
					function(){
						console.log('callback!');
						$this.closest('tr').hide();	
					});
		});
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));
