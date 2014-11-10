(function($, molgenis) {	
	"use strict";
	
	var restApi = new molgenis.RestClient();
	var detailsPackageName;
	var countTemplate;
	var modelTemplate;
	
	var nrResultsPerPage = 3;
	var query;
	var pageIndex = 0;
	
	/**
	 * @memberOf molgenis.standardsregistry
	 */
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
	
	/**
	 * @memberOf molgenis.standardsregistry
	 */
	function renderSearchResults(searchResults, container) {
		container.empty();
		for(var i = 0; i < searchResults.packages.length; ++i){			
			container.append(modelTemplate({'package': searchResults.packages[i], 'entities' : searchResults.packages[i].entitiesInPackage, 'tags' : searchResults.packages[i].tags}));
		}
		container.append(countTemplate({'count': searchResults.total}));
		$('.select2').select2({width: 300});
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
		
		function showPackageDetails(id) {
			detailsPackageName = id;
			
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
			var selectedEntity = $(this).parent().siblings('select.entity-select-dropdown').val();
			if(selectedEntity) {
				// FIXME do not hardcode URL
				window.location.href= '/menu/main/dataexplorer?entity=' + selectedEntity;
			}
		});
		
		$(document).on('click', '#uml-tab', function() {
			$.getScript(molgenis.getContextUrl() + '/uml?package=' + detailsPackageName);
		});
		
		$(document).on('click', '#print-btn', function() {
			var width = $('#package-doc-container').outerWidth();
			$('#package-doc-container').css('height', '100%').css('width', '21cm').css("overflow", "hidden");
			window.print();
			$('#package-doc-container').css('height', '600px').css("overflow-x", "hidden").css("overflow-y", "auto").css('width', width);
		});
		
		Handlebars.registerHelper('notequal', function(lvalue, rvalue, options) {
		    if (arguments.length < 3)
		        throw new Error("Handlebars Helper equal needs 2 parameters");
		    if (lvalue != rvalue) {
		    	 return options.fn(this);
		    } else {
		    	 return options.inverse(this);
		    }
		});
		
		Handlebars.registerHelper('equal', function(lvalue, rvalue, options) {
		    if (arguments.length < 3)
		        throw new Error("Handlebars Helper equal needs 2 parameters");
		    if (lvalue != rvalue) {
		        return options.inverse(this);
		    } else {
		        return options.fn(this);
		    }
		});
		
		countTemplate = Handlebars.compile($("#count-template").html());
		modelTemplate = Handlebars.compile($("#model-template").html());
		
		if(window.location.hash) {
			showPackageDetails(window.location.hash.substring(1));
		}
		// initially search for all models
		$('form[name=search-form]').submit();
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));
