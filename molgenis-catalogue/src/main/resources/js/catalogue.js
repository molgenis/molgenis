(function($, molgenis) {	
	"use strict";
	
	var restApi = new molgenis.RestClient();
	var selectedPackage;
	var countTemplate;
	var modelTemplate;
	
	function createPackageTree(selectedPackage) {	
		$('#attribute-selection').fancytree({
			source:{
				url: molgenis.getContextUrl() + "/getTreeData?package=" + selectedPackage.name		
			},
			'click' : function(event, data) {
				if (data.targetType === 'title' || data.targetType === 'icon') {
					switch(data.node.data.type) {
					case 'package' :
						// TODO add package data to rest controller
						break;
					case 'entity' :
						restApi.getAsync(data.node.data.href, null, function(entity) {
							createEntityMetadataTable(entity);
						});
						break;
					case 'attribute' :
						restApi.getAsync(data.node.data.href, null, function(attribute) {
							createAttributeMetadataTable(attribute);
						});
						break;
					default:
						throw 'Unknown type';
					}
				}
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
	
	function createEntityMetadataTable(entityMetadata) {
		$('#attributes-table').entityMetadataTable({
			entityMetadata: entityMetadata
		});
	}
	
	function createHeader(entityMetaData) {
		$('#entity-class-name').html(entityMetaData.name);
		
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
	
	function load(selectedPackageName) {
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
		$('form[name=search-form]').submit(function(e) {
			e.preventDefault();
			$.ajax({
				type : $(this).attr('method'),
				url : $(this).attr('action'),
				data : $(this).serialize(),
				success : function(data) {
					$('#package-search-results').empty();
					$('#package-search-results').append(countTemplate({'count': data.total}));
					console.log(data.packages);
					for(var i = 0; i < data.packages.length; ++i){
						$('#package-search-results').append(modelTemplate({'package': data.packages[i] }));
					}
				}
			});
		});
		
		$(document).on('click', '.details-btn', function() {
			var id = $(this).closest('.package').data('id');
			window.location.href='http://localhost:8080/menu/main/catalogue/package-details-explorer?package=' + id;
			

		});
		
		$(document).on('click', '.dataexplorer-btn', function() {
			var id = $(this).closest('.package').data('id');
			//TODO link id to data explorer
			window.location.href='http://localhost:8080/menu/main/dataexplorer';
		});
		
		$(document).on('click', '.import-btn', function() {
			window.location.href='http://localhost:8080/menu/main/importwizard';
		});

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
		if($("#count-template").length > 0)
			countTemplate = Handlebars.compile($("#count-template").html());
		if($("#model-template").length > 0)
			modelTemplate = Handlebars.compile($("#model-template").html());
		
		if(window.selectedPackageName) {
			$.get(molgenis.getContextUrl() + '/getPackage?package=' + selectedPackageName, function(selectedPackage){
				createHeader(selectedPackage);
				createPackageTree(selectedPackage);
			});
		} else {
			$('form[name=search-form]').submit();
		}
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));
