(function($, molgenis) {	
	"use strict";
	
	var restApi = new molgenis.RestClient();
	var selectedPackage;
	var countTemplate;
	var modelTemplate;
	var nrResultsPerPage = 3;
	var query;
	var pageIndex = 0;
	
	function createUml() {
		var graph = new joint.dia.Graph;
		var uml = joint.shapes.uml;
		var paper = new joint.dia.Paper({
		    el: $('#paper'),
		    width: 1200,
		    height: 600,
		    gridSize: 1,
		    model: graph
		});


		graph.addCell(new uml.Class({
	        position: { x:200  , y: 500 },
	        size: { width: 180, height: 50 },
	        name: selectedPackage.name
	    }));
		
	}
	
	function createPackageTree() {
		$('#attribute-selection').fancytree({
				source:{
					url: molgenis.getContextUrl() + "/getTreeData?package=" + selectedPackage.name		
				},
				'click' : function(event, data) {
					if (data.targetType === 'title' || data.targetType === 'icon') {
						switch(data.node.data.type) {
						case 'package' :
							// no operation
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
	
	function createHeader() {
		if(selectedPackage.name != undefined){
			$('#entity-class-name').html(selectedPackage.name);
			
			if (selectedPackage.description) {
				var description = $('<span data-placement="bottom"></span>');
				description.html(abbreviate(selectedPackage.description, 180));
				description.attr('data-title', selectedPackage.description);
				$('#entity-class-description').html(description.tooltip());
			} else {
				$('#entity-class-description').html('');
			}
		}
	}
	
	function renderSearchResults(searchResults, container) {
		container.empty();
		for(var i = 0; i < searchResults.packages.length; ++i){			
			container.append(modelTemplate({'package': searchResults.packages[i], 'entities' : searchResults.packages[i].entitiesInPackage}));
		}
		container.append(countTemplate({'count': searchResults.total}));
	}
	
	function showDetails(packageName) {
		$('#standards-registry-details').load(molgenis.getContextUrl() + '/details?package=' + packageName, function() {
			$.get(molgenis.getContextUrl() + '/getPackage?package=' + packageName, function(p){
				selectedPackage = p;
				createHeader();
				createPackageTree();
			});
		});
		
		$('#standards-registry-search').removeClass('show').addClass('hidden');
		$('#standards-registry-details').removeClass('hidden').addClass('show');
	}
	
	$(function() {
		var searchResultsContainer = $('#package-search-results');
		
		$('form[name=search-form]').submit(function(e) {
			e.preventDefault();
			var q = $('#package-search').val();
			
			if (q != query) {
				//New search reset pageIndex
				pageIndex = 0;
				query = q;
			}
			
			$.ajax({
				type : $(this).attr('method'),
				url : $(this).attr('action'),
				data : JSON.stringify({
					query: q,
					offset: pageIndex * nrResultsPerPage,
					num: nrResultsPerPage
				}),
				contentType: 'application/json',
				success : function(data) {
					renderSearchResults(data, searchResultsContainer);
					
					if (data.total > nrResultsPerPage) {
						$('#package-search-results-pager').show();
					
						$('#package-search-results-pager').pager({
							'nrItems' : data.total,
							'nrItemsPerPage' : nrResultsPerPage,
							'page' : pageIndex + 1,
							'onPageChange' : function(pager) {
								pageIndex = pager.page - 1;
								$('form[name=search-form]').submit();					
							}
						});
					} else {
						$('#package-search-results-pager').hide();
					}
				}
			});
		});
		
		$(document).on('click', '#search-clear-button', function() {
			$('#package-search').val('');
			$('form[name=search-form]').submit();	
		});
		
		$(document).on('click', '.details-btn', function() {
			var id = $(this).closest('.package').data('id');
			showDetails(id);
		});
		
		$(document).on('click', '#search-results-back-btn', function(){
			$('#standards-registry-search').removeClass('hidden').addClass('show');
			$('#standards-registry-details').removeClass('show').addClass('hidden');		
		});
		
		$(document).on('click', '.dataexplorer-btn', function() {
			var selectedEntity = $(this).siblings('.entity-select-dropdown').val();
			// FIXME do not hardcode URL
			window.location.href= '/menu/main/dataexplorer?entity=' + selectedEntity;
		});
		
		$(document).on('click', '#uml-tab', function() {
			$.getScript(molgenis.getContextUrl() + '/uml?package=' + selectedPackage.name);
		});
		
		countTemplate = Handlebars.compile($("#count-template").html());
		modelTemplate = Handlebars.compile($("#model-template").html());
		
		if (selectedPackageName) {
			showDetails(selectedPackageName);
		} else {
			// initially search for all models
			$('form[name=search-form]').submit();
		}
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));
