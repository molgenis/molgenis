(function($, molgenis) {
	"use strict";
	
	var ns = molgenis;
	var pagination = new ns.Pagination();
	var standardModal = new ns.StandardModal();
	var restApi = new ns.RestClient();
	var ontologyTermIRI = "ontologyTermIRI"
	var searchApi = new ns.SearchClient();
	var selectedDataSetId = null;
	var sortRule = null;
	
	ns.OntologyAnnotator = function OntologyAnnotator(){
		
	};
	
	ns.OntologyAnnotator.prototype.changeDataSet = function(selectedDataSetId){
		if(selectedDataSetId !== ''){
			var dataSetEntity = restApi.get('/api/v1/dataset/' + selectedDataSetId);
			var request = {
				documentType : 'protocolTree-' + ns.hrefToId(dataSetEntity.href),
				queryRules : [{
					field : 'type',
					operator : 'EQUALS',
					value : 'observablefeature'
				}]
			};
			searchApi.search(request, function(searchResponse){
				var sortRule = null;
				$('#catalogue-name').empty().append(dataSetEntity.name);
				$('#dataitem-number').empty().append(searchResponse.totalHitCount);
				pagination.reset();
				ns.OntologyAnnotator.prototype.updateselectedDataSetId(selectedDataSetId);
				ns.OntologyAnnotator.prototype.createMatrixForDataItems();
				initSearchDataItems(dataSetEntity);
			});
		}else{
			$('#catalogue-name').empty().append('Nothing selected');
			$('#dataitem-number').empty().append('Nothing selected');
		}
		
		function initSearchDataItems (dataSet) {
			$('#search-dataitem').typeahead({
				source: function(query, process) {
					ns.OntologyAnnotator.prototype.dataItemsTypeahead('observablefeature', ns.hrefToId(dataSet.href), query, process);
				},
				minLength : 3,
				items : 20
			});
			
			$('#search-button').click(function(){
				ns.OntologyAnnotator.prototype.createMatrixForDataItems();
			});
			

			$('#search-dataitem').on('keydown', function(e){
			    if (e.which == 13) {
			    	$('#search-button').click();
			    	return false;
			    }
			});
			
			$('#search-dataitem').on('keyup', function(e){
				if($(this).val() === ''){
					$('#search-dataitem').val('');
					ns.OntologyAnnotator.prototype.createMatrixForDataItems();
			    }
			});
		};
	};
	
	ns.OntologyAnnotator.prototype.createMatrixForDataItems = function() {
		var documentType = 'protocolTree-' + getselectedDataSetId();
		var query = [{
			field : 'type',
			operator : 'SEARCH',
			value : 'observablefeature'
		}];
		
		var queryText = $('#search-dataitem').val();
		if(queryText !== ''){
			query.push({
				operator : 'AND'
			});
			query.push({
				operator : 'SEARCH',
				value : queryText
			});
			pagination.reset();
		}
		if(sortRule !== null) query.push(sortRule);
		
		searchApi.search(pagination.createSearchRequest(documentType, query), function(searchResponse) {
			
			var searchHits = searchResponse.searchHits;
			var tableObject = $('#dataitem-table');
			var tableBody = $('<tbody />');
			
			$.each(searchHits, function(){
				$(createTableRow($(this)[0]["columnValueMap"])).appendTo(tableBody);
			});
			
			tableObject.empty().append(createTableHeader()).append(tableBody);
			pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()));
			pagination.updateMatrixPagination($('.pagination ul'), ns.OntologyAnnotator.prototype.createMatrixForDataItems);
		});
		
		function createTableRow(feature){
			
			var row = $('<tr />').addClass('show-popover').data('feature', feature).click(function()
			{
				var featureId = $(this).data('feature').id;
				var restApiFeature = restApi.get('/api/v1/observablefeature/' + featureId, ["unit", "definitions"], null);
				var components = [];
				components.push(createFeatureTable(restApiFeature));
				components.push(createSearchDiv(restApiFeature));
				var modalStyle = {
					'width' : 650, 
					'margin-left' : -350,
					'margin-top' : 100
				};
				standardModal.createModal('Annotate data item', components, modalStyle);
			});
			
			var description = feature.description;
			var isPopOver = description.length < 40;
			var descriptionDiv = $('<div />').html(isPopOver ? description : description.substring(0, 40) + '...');
			if(!isPopOver){
				descriptionDiv.addClass('show-descriptionDiv');
				descriptionDiv.popover({
					content : description,
					trigger : 'hover',
					placement : 'bottom'
				});
			}
			
			var featureNameDiv = $('<div />').append(feature.name);
			var annotationDiv = $('<div />').addClass('text-info');
			var annotationText = '';
			var featureEntity = restApi.get('/api/v1/observablefeature/' + feature.id, ["unit", "definitions"]);
			if(featureEntity.definitions.items !== 0){
				var moreToShow = featureEntity.definitions.items.length;
				$.each(featureEntity.definitions.items, function(index, ontologyTerm){
					var newAnnotationText = annotationText + ontologyTerm.name + ' , ';
					if(newAnnotationText.length > 50) {
						annotationText = annotationText.substring(0, annotationText.length - 3) + ' , ';
					}else{
						moreToShow--;
						annotationText += ontologyTerm.name + ' , ';
					}
				});
				annotationDiv.append($('<span>' + annotationText.substring(0, annotationText.length - 3) + '</span>'));
				if(moreToShow !== 0){
					$('<span />').addClass('float-right').append('<a>...' + moreToShow + ' to show</a>').click(function(){
						row.click();
					}).appendTo(annotationDiv);
				}
			}
			
			$('<td />').append(featureNameDiv).appendTo(row);
			$('<td />').append(descriptionDiv).appendTo(row);
			$('<td />').append(annotationDiv).appendTo(row);
			
			return row;
		}
		
		function createTableHeader(){
			var headerRow = $('<tr />');
			var firstColumn = $('<th>Name</th>').css('width', '15%').appendTo(headerRow);
			if (sortRule) {
				if (sortRule.operator == 'SORTASC') {
					$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-s down float-right"></span>').appendTo(firstColumn);
				} else {
					$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-n up float-right"></span>').appendTo(firstColumn);
				}
			} else {
				$('<span data-value="Name" class="ui-icon ui-icon-triangle-2-n-s updown float-right"></span>').appendTo(firstColumn);
			}
			$('<th>Description</th>').css('width', '25%').appendTo(headerRow);
			$('<th>Annotation</th>').css('width', '40%').appendTo(headerRow);
			
			// Sort click
			$(firstColumn).find('.ui-icon').click(function() {
				if (sortRule && sortRule.operator == 'SORTASC') {
					sortRule = {
						value : 'name',
						operator : 'SORTDESC'
					};
				} else {
					sortRule = {
						value : 'name',
						operator : 'SORTASC'
					};
				}
				ns.OntologyAnnotator.prototype.createMatrixForDataItems();
				return false;
			});
			return $('<thead />').append(headerRow);
		}
		
		function createSearchDiv(feature){
			var searchDiv = $('<div class="row-fluid"></div>');
			var searchGroup = $('<div class="input-append span4"></div>');
			var searchField = $('<input type="text" data-provide="typeahead" />');
			var addTermButton = $('<button class="btn" type="button" id="add-ontologyterm-button">Add</button>');
			searchField.appendTo(searchGroup);
			addTermButton.appendTo(searchGroup);
			searchField.typeahead({
				source: function(query, process) {
					ns.OntologyAnnotator.prototype.ontologyTermTypeahead('ontologyTermSynonym', query, process);
				},
				minLength : 3,
				items : 20
			});
			addTermButton.click($.proxy(checkOntologyTerm, {'searchField' : searchField, 'feature' : feature}));
			return searchDiv.append(searchGroup);
		}
		
		function checkOntologyTerm(){
			var dataMap = $(document).data('dataMap');
			var ontologyTerm = this.searchField.val();
			var toCreate = true;
			if(dataMap && ontologyTerm !== '' && dataMap[ontologyTerm]){
				var uri = dataMap[ontologyTerm].ontologyTermIRI;
				var q = {
						q : [ {
							field : 'termAccession',
							operator : 'EQUALS',
							value : uri
						} ],
				};
				restApi.getAsync('/api/v1/ontologyterm/', null, q, $.proxy(function(result){
					var ontologyTermId = null;
					if(result.items.length !== 0) {
						toCreate = false;
						var href = result.items[0].href;
						ontologyTermId = href.substring(href.lastIndexOf('/') + 1);
					}
					if(toCreate) ontologyTermId = createOntologyTerm(dataMap[ontologyTerm]);
					if(ontologyTermId != null) updateAnnotation(restApi.get(this.feature.href), ontologyTermId, true);
					restApi.getAsync(this.feature.href, ["unit", "definitions"], null, function(updatedFeature){
						var components = [];
						components.push(createFeatureTable(updatedFeature));
						components.push(createSearchDiv(updatedFeature));
						var modalStyle = {
							'width' : 650, 
							'margin-left' : -350,
							'margin-top' : 100
						};
						standardModal.createModal('Annotate data item', components, modalStyle);
						ns.OntologyAnnotator.prototype.createMatrixForDataItems();
					});
				},{'feature' : this.feature}));
			}
		}

		function updateAnnotation(feature, ontologyTermId, add){
			var data = {};
			feature.description = i18nDescription(feature).en;
			$.map(feature, function(value, key){
				if(key !== 'href'){
					if(key === 'unit')
						data[key] = value.href.substring(value.href.lastIndexOf('/') + 1);
					else if(key === 'definitions'){
						data[key] = [];
						$.each(value.items, function(index, element){
							data[key].push(element.href.substring(element.href.lastIndexOf('/') + 1));
						});
					}else
						data[key] = value;
				}	
			});
			if($.inArray(ontologyTermId, data.definitions) === -1 && add) data.definitions.push(ontologyTermId);
			if($.inArray(ontologyTermId, data.definitions) !== -1 && !add) {
				var index = data.definitions.indexOf(ontologyTermId);
				data.definitions.splice(index, 1);
			}
			updateFeature(feature, data);
		}
		
		function updateFeature(feature, data){
			$.ajax({
				type : 'PUT',
				dataType : 'json',
				url : feature.href,
				cache: true,
				data : JSON.stringify(data),
				contentType : 'application/json',
				async : false,
				success : function(data, textStatus, request) {
					console.log(data);
				},
				error : function(request, textStatus, error){
					console.log(error);
				} 
			});
		}
		
		function createOntologyTerm(data){
			var ontology = createOntology(data);
			var ontologyTermId = null;
			var query = {};
			query.name =  data.ontologyLabel + ':' + data.ontologyTerm;
			query.identifier = data.ontologyTermIRI;
			query.termAccession = data.ontologyTermIRI;
			query.description = data.ontologyTermLabel;
			query.ontology = ns.hrefToId(ontology.href);
			
			$.ajax({
				type : 'POST',
				dataType : 'json',
				url : '/api/v1/ontologyterm/',
				cache: true,
				data : JSON.stringify(query),
				contentType : 'application/json',
				async : false,
				success : function(data, textStatus, request) {
					var href = request.getResponseHeader('Location');
					ontologyTermId = href.substring(href.lastIndexOf('/') + 1);
				},
				error : function(request, textStatus, error){
					console.log(error);
				} 
			});
			return ontologyTermId;
		}
		
		function createOntology(data){
			var query = {
					q : [ {
						field : 'ontologyURI',
						operator : 'EQUALS',
						value : data.ontologyIRI
					} ],
			};
			var existingOntology = restApi.get('/api/v1/ontology/', null, query);
			if(existingOntology.items.length === 0){
				var query = {};
				query.name = data.ontologyName;
				query.identifier = data.ontologyIRI;
				query.ontologyURI = data.ontologyIRI;
				
				$.ajax({
					type : 'POST',
					dataType : 'json',
					url : '/api/v1/ontology/',
					cache: true,
					data : JSON.stringify(query),
					contentType : 'application/json',
					async : false,
					success : function(data, textStatus, request) {
						
					},
					error : function(request, textStatus, error){
						console.log(error);
					} 
				});
				existingOntology = restApi.get('/api/v1/ontology/', null, query);
			}
			return existingOntology.items[0];
		}
		
		function createFeatureTable(feature){
			var table = $('<table />').addClass('table table-bordered').attr('id', 'test-table'); 
			$('<tr><th class="feature-detail-th">ID : </th><td class="feature-detail-td">' + ns.hrefToId(feature.href) + '</td></tr>').appendTo(table);
			$('<tr><th>Name : </th><td>' + feature.name + '</td></tr>').appendTo(table);
			$('<tr><th>Description : </th><td>' + i18nDescription(feature).en + '</td></tr>').appendTo(table);
			
			if(feature.definitions.items.length !== 0){
				var ontologyTermAnnotations = $('<ul />');
				$.each(feature.definitions.items, function(index, ontologyTerm){
					var ontologyTermLink = $('<a href="' + ontologyTerm.termAccession + '" target="_blank">' + ontologyTerm.name + '</a>');
					var removeIcon = $('<i class="icon-remove float-right"></i>').click($.proxy(function(){
						var ontologyTermId = this.ontologyTermHref.substring(this.ontologyTermHref.lastIndexOf('/') + 1)
						updateAnnotation(this.feature, ontologyTermId, false);
						restApi.getAsync(this.feature.href, ["unit", "definitions"], null, function(updatedFeature){
							var components = [];
							var featureTableContainer = createFeatureTable(updatedFeature);
							components.push(featureTableContainer);
							components.push(createSearchDiv(updatedFeature));
							var modalStyle = {
								'width' : 650, 
								'margin-left' : -350,
								'margin-top' : 100
							};
							standardModal.createModal('Annotate data item', components, modalStyle);
							ns.OntologyAnnotator.prototype.createMatrixForDataItems();
							featureTableContainer.scrollTop(components[0].height());
						});
					}, {'feature' : feature, 'ontologyTermHref' : ontologyTerm.href}));
					
					ns.OntologyAnnotator.prototype.searchOntologyTermByUri(ontologyTermIRI, ontologyTerm.termAccession, function(boosted){
						var selectBoostIcon = $('<i class="icon-star-empty float-right"></i>').popover({
							content : 'Select as key concept and give more weight!',
							trigger : 'hover',
							placement : 'bottom'
						});
						if(boosted) selectBoostIcon.removeClass('icon-star-empty').addClass('icon-star');
						selectBoostIcon.click(function(){
							if($(this).hasClass('icon-star-empty')){
								ns.OntologyAnnotator.prototype.updateIndex(ontologyTerm.termAccession, true);
								$(this).removeClass('icon-star-empty').addClass('icon-star');
							}
							else {
								ns.OntologyAnnotator.prototype.updateIndex(ontologyTerm.termAccession, false);
								$(this).removeClass('icon-star').addClass('icon-star-empty');
							}
						});
						$('<li />').append(ontologyTermLink).append(removeIcon).append(selectBoostIcon).appendTo(ontologyTermAnnotations);
					});
				});
				$('<tr />').append('<th>Annotation : </th>').append($('<td />').append(ontologyTermAnnotations)).appendTo(table);
			}else{
				table.append('<tr><th>Annotation : </th><td>Not available</td></tr>');
			}
			return $('<div />').css({'max-height' : 350, 'overflow' : 'auto'}).append(table);
		}
		
		function i18nDescription(feature){
			if(feature.description === undefined) feature.description = '';
			if(feature.description.indexOf('{') !== 0){
				feature.description = '{"en":"' + (feature.description === null ? '' : feature.description) +'"}';
			}
			return eval('(' + feature.description + ')');
		}
	};
	
	ns.OntologyAnnotator.prototype.searchOntologies = function (){
		searchApi.search(createSearchRequest(), function(searchResponse){
			var ontologyDiv = $('#ontology-list');
			if(ontologyDiv.find('input').length === 0){
				$.each(searchResponse.searchHits, function(index, hit){
					var ontologyInfo =hit.columnValueMap;
					var ontologyUri = ontologyInfo.url;
					var ontologyName = ontologyInfo.ontologyLabel;
				    $('<label />').addClass('checkbox').append('<input type="checkbox" value="' + ontologyUri + '" checked>' + ontologyName).click(function(){
				    	$('#selectedOntologies').val(getAllOntologyUris(ontologyDiv));
				    }).appendTo(ontologyDiv);
				});
			}
			$('#selectedOntologies').val(getAllOntologyUris(ontologyDiv));
		});
		
		function getAllOntologyUris(ontologyDiv){
			var selectedOntologies = [];
	    	$(ontologyDiv).find('input').each(function(){
	    		if($(this).attr("checked")){
	    			selectedOntologies.push($(this).val());
	    		}
	    	});
	    	return selectedOntologies;
		}
		
		function createSearchRequest() {
			var queryRules = [];
			//todo: how to unlimit the search result
			queryRules.push({
				operator : 'LIMIT',
				value : 1000000
			});
			queryRules.push({
				operator : 'SEARCH',
				value : 'indexedOntology'
			});
			
			var searchRequest = {
				documentType : null,
				queryRules : queryRules
			};
			return searchRequest;
		}
	};

	ns.OntologyAnnotator.prototype.confirmation = function(title){
		standardModal.createModalCallback(title, function(modal){
			var confirmButton = $('<button type="btn" class="btn btn-primary">Confirm</button>').click(function(){
				modal.modal('hide');
				$('#spinner').modal();
				$('#wizardForm').attr({
					'action' : molgenis.getContextURL() + '/annotate/remove',
					'method' : 'POST'
				}).submit();
			});
			modal.css({
				'margin-top' : 200
			});
			modal.find('div.modal-body:eq(0)').append('<p style="font-size:16px"><strong>Are you sure that you want to remove all annotations?</strong></p>');
			modal.find('div.modal-footer:eq(0)').prepend(confirmButton);
		});
	};
	
	ns.OntologyAnnotator.prototype.updateIndex = function(ontologyTermIRI, boost){
		var searchRequest = {
			documentType : null,
			queryRules	: [{
				field : 'ontologyTermIRI',
				operator : 'EQUALS',
				value : ontologyTermIRI
			}]
		};
		searchApi.search(searchRequest, function(searchResponse){
			var documentType = null;
			var documentIds = [];
			$.each(searchResponse.searchHits, function(index, hit){
				if(documentType === null) documentType = hit.documentType;
				documentIds.push(hit.id);
			});
			var updateRequest = {
				'documentType' : documentType,
				'documentIds' : documentIds,
				'updateScript' : 'boost=' + boost
			};
			$.ajax({
				type : 'POST',
				url : ns.getContextURL() + '/annotate/update',
				async : false,
				data : JSON.stringify(updateRequest),
				contentType : 'application/json',
			});
		});
	}
	
	ns.OntologyAnnotator.prototype.updateselectedDataSetId = function(dataSet) {
		selectedDataSetId = dataSet;
	};
	
	ns.OntologyAnnotator.prototype.dataItemsTypeahead = function (type, dataSetId, query, response){
		var queryRules = [{
			field : 'type',
			operator : 'EQUALS',
			value : type,
		},{
			operator : 'AND'
		},{
			operator : 'SEARCH',
			value : query
		},{
			operator : 'LIMIT',
			value : 20
		}];
		var searchRequest = {
			documentType : 'protocolTree-' + dataSetId,
			queryRules : queryRules
		};
		
		searchApi.search(searchRequest, function(searchReponse){
			var result = [];
			var dataMap = {};
			$.each(searchReponse.searchHits, function(index, hit){
				var value = hit.columnValueMap.ontologyTerm;
				if($.inArray(value, result) === -1){
					var name = hit.columnValueMap.name;
					result.push(name);
					dataMap[name] = hit.columnValueMap;
				}
			});
			$(document).data('dataMap', dataMap);
			response(result);
		});
	};
	
	ns.OntologyAnnotator.prototype.searchOntologyTermByUri = function(field, query, callback){
		var queryRules = [{
			field : field,
			operator : 'EQUALS',
			value : query,
		}];
		var searchRequest = {
			documentType : null,
			queryRules : queryRules
		};
		searchApi.search(searchRequest, function(searchReponse){
			var boosted = false;
			$.each(searchReponse.searchHits, function(index, hit){
				boosted = hit.columnValueMap.boost;
				return false;
			});
			callback(boosted);
		});
	};
	
	ns.OntologyAnnotator.prototype.ontologyTermTypeahead = function (field, query, response){
		var queryRules = [{
			field : field,
			operator : 'EQUALS',
			value : query,
		},{
			operator : 'LIMIT',
			value : 20
		}];
		var searchRequest = {
			documentType : null,
			queryRules : queryRules
		};
		searchApi.search(searchRequest, function(searchReponse){
			var result = [];
			var dataMap = {};
			$.each(searchReponse.searchHits, function(index, hit){
				var value = hit.columnValueMap.ontologyTerm;
				if($.inArray(value, result) === -1){
					var ontologyName = hit.columnValueMap.ontologyLabel;
					var termName = hit.columnValueMap.ontologyTerm;
					termName = ontologyName === '' ? termName : ontologyName + ':' + termName;
					result.push(termName);
					dataMap[termName] = hit.columnValueMap;
				}
			});
			$(document).data('dataMap', dataMap);
			response(result);
		});
	};
	
	function getselectedDataSetId(){
		return selectedDataSetId;
	}
}($, window.top.molgenis = window.top.molgenis || {}));