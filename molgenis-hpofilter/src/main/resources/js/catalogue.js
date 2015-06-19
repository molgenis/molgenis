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
			url: 'catalogue/shoppingcart',
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
	
	function load(entityUri) {
		$.ajax({
			url: 'catalogue/shoppingcart/clear',
			async: false
		});
		
		restApi.getAsync(entityUri + '/meta', {'expand': ['attributes']}, function(entityMetaData) {
			selectedEntity = entityMetaData;
			createHeader(entityMetaData);
			createEntityMetaTree(entityMetaData);
				
			$('#attributes-table').attributeMetadataTable({
				attributeMetadata: getFirstAttribute(entityMetaData) 
			});
		});
	}
	
	$(function() {
		$('.entity-dropdown-item').click(function() {
			var entityUri = $(this).attr('id');
			load(entityUri);
		});
		
		$('#cart-button').click(function(){
			$('#cart-modal').load(
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
			$.get('catalogue/shoppingcart/remove',
					{entityName: selectedEntity.name, attributeName: attributeName}, 
					function(){
						$this.closest('tr').remove();	
						var node = $('#attribute-selection').tree('getNodeByUri', '/api/v1/' + selectedEntity.name + '/meta/' + attributeName);
						if (node) {
							node.setSelected(false);
						}
						
						var nrOfHeaderRows = 1;
						if ($('#shoppingcart tr').size() === nrOfHeaderRows) {
							$('#cart-contents').html('<p>Cart is empty</p>');
						}
					});
		});
		
		if (selectedEntityName) {
			load('/api/v1/' + selectedEntityName);
		}
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));