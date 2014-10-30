(function($, molgenis) {	
	"use strict";
	
	var restApi = new molgenis.RestClient();
	var selectedPackage;
	var countTemplate;
	var modelTemplate;
	
	var nrResultsPerPage = 3;
	var query;
	var pageIndex = 0;
	
	function createPackageTree(selectedPackage) {
		if(selectedPackage.name){
			$('#attribute-selection').fancytree({
				source:{
					url: molgenis.getContextUrl() + "/getTreeData?package=" + selectedPackage.name		
				},
				'click' : function(event, data) {
					if (data.targetType === 'title' || data.targetType === 'icon') {
						switch(data.node.data.type) {
						case 'package' :
							document.getElementById('package-' + data.node.key).scrollIntoView();
							break;
						case 'entity' :
							document.getElementById('entity-' + data.node.key).scrollIntoView();
							break;
						case 'attribute' :
							document.getElementById('attribute-' + data.node.parent.key + data.node.key).scrollIntoView();
							break;
						default:
							throw 'Unknown type';
						}
					}
				}
			});
		}
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
	
	function renderSearchResults(searchResults, container) {
		container.empty();
		for(var i = 0; i < searchResults.packages.length; ++i){
			container.append(modelTemplate({'package': searchResults.packages[i] }));
		}
		container.append(countTemplate({'count': searchResults.total}));
	}
	
	function search(callback) {
		
	}
	
	$(function() {
		var searchResultsContainer = $('#package-search-results');
		
		$('form[name=search-form]').submit(function(e) {
			e.preventDefault();
			$.ajax({
				type : $(this).attr('method'),
				url : $(this).attr('action'),
				data : JSON.stringify({
					query: $('#package-search').val(),
					offset: pageIndex * nrResultsPerPage,
					num: nrResultsPerPage
				}),
				contentType: 'application/json',
				success : function(data) {
					renderSearchResults(data, searchResultsContainer);
					
					$('#package-search-results-pager').pager({
						'nrItems' : data.total,
						'nrItemsPerPage' : nrResultsPerPage,
						'page' : pageIndex + 1,
						'onPageChange' : function(pager) {
							pageIndex = pager.page - 1;
							$('form[name=search-form]').submit();					
						}
					});
				}
			});
		});
			
		$(document).on('click', '#search-clear-button', function() {
			$('#package-search').val('');
			$('form[name=search-form]').submit();	
		});
		
		function showPackageDetails(id) {
			$('#standards-registry-details').load(molgenis.getContextUrl() + '/details?package=' + id, function() {
				$.get(molgenis.getContextUrl() + '/getPackage?package=' + id, function(selectedPackage){
					createPackageTree(selectedPackage);
				});
				
				$('#standards-registry-search').removeClass('show').addClass('hidden');
				$('#standards-registry-details').removeClass('hidden').addClass('show');
			});
		}
		
		$(document).on('click', '.details-btn', function() {
			var id = $(this).closest('.package').data('id');
			showPackageDetails(id);	
		});
		
		$(document).on('click', '#search-results-back-btn', function(){
			$('#standards-registry-search').removeClass('hidden').addClass('show');
			$('#standards-registry-details').removeClass('show').addClass('hidden');		
		});
		
		$(document).on('click', '.dataexplorer-btn', function() {
			var id = $(this).closest('.package').data('id');
			// TODO link id to data explorer
			// FIXME do not hardcode URL
			window.location.href= '/menu/main/dataexplorer';
		});
		
		$(document).on('click', '.import-btn', function() {
			// FIXME do not hardcode URL
			window.location.href= '/menu/main/importwizard';
		});
		
		countTemplate = Handlebars.compile($("#count-template").html());
		modelTemplate = Handlebars.compile($("#model-template").html());
		
		if(window.location.hash) {
			console.log(window.location.hash);
			showPackageDetails(window.location.hash.substring(1));
		}
		// initially search for all models
		$('form[name=search-form]').submit();
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));
