(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var pagination = new ns.Pagination();
	var standardModal = new ns.StandardModal();
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var selectedDataSet = null;
	var CONTEXT_URL = null;
	
	ns.setContextURL = function(CONTEXT_URL){
		this.CONTEXT_URL = CONTEXT_URL;
	};
	
	ns.getContextURL = function() {
		return this.CONTEXT_URL;
	};
	
	ns.changeDataSet = function(selectedDataSet){
		if(selectedDataSet !== ''){
			var dataSetEntity = restApi.get('/api/v1/dataset/' + selectedDataSet);
			var request = {
				documentType : 'protocolTree-' + hrefToId(dataSetEntity.href),
				queryRules : [{
					field : 'type',
					operator : 'EQUALS',
					value : 'observablefeature'
				}]
			};
			searchApi.search(request, function(searchResponse){
				$('#catalogue-name').empty().append(dataSetEntity.name);
				$('#dataitem-number').empty().append(searchResponse.totalHitCount);
				pagination.reset();
				ns.updateSelectedDataset(selectedDataSet);
				ns.createMatrixForDataItems();
				initSearchDataItems(dataSetEntity);
			});
		}else{
			$('#catalogue-name').empty().append('Nothing selected');
			$('#dataitem-number').empty().append('Nothing selected');
		}
		
		function hrefToId (href){
			return href.substring(href.lastIndexOf('/') + 1); 
		};
		
		function initSearchDataItems (dataSet) {
			$('#search-dataitem').typeahead({
				source: function(query, process) {
					ns.dataItemsTypeahead('observablefeature', hrefToId(dataSet.href), query, process);
				},
				minLength : 3,
				items : 20
			});
			
			$('#search-button').click(function(){
				ns.createMatrixForDataItems();
			});
			
			$('#clear-button').click(function(){
				$('#search-dataitem').val('');
				ns.createMatrixForDataItems();
			});
		};
	};
	
	ns.createMatrixForDataItems = function(queryRule) {
		var documentType = 'protocolTree-' + getSelectedDataSet();
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
		
		searchApi.search(pagination.createSearchRequest(documentType, query), function(searchResponse) {
			
			var searchHits = searchResponse.searchHits;
			var tableObject = $('#dataitem-table');
			var tableBody = $('<tbody />');
			
			$.each(searchHits, function(){
				$(createTableRow($(this)[0]["columnValueMap"])).appendTo(tableBody);
			});
			
			tableObject.empty().append(createTableHeader()).append(tableBody);
			pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()));
			pagination.updateMatrixPagination($('.pagination ul'), ns.createMatrixForDataItems);
		});
		
		function createTableRow(feature){
			
			var row = $('<tr />');
			
			var description = feature.description;
			var isPopOver = description.length < 40;
			var popover = $('<span />').html(isPopOver ? description : description.substring(0, 40) + '...');
			if(!isPopOver){
				popover.addClass('show-popover');
				popover.popover({
					content : description,
					trigger : 'hover',
					placement : 'bottom'
				});
			}
			
			var clickFeature = $('<span class="text-info show-popover">' + feature.name + '</span>').
				data('feature', feature).click(function()
			{
				var featureId = $(this).data('feature').id;
				restApi.getAsync('/api/v1/observablefeature/' + featureId, ["unit", "definition"], null, function(feature){
					var components = [];
					components.push(createFeatureTable(feature));
					components.push(createSearchDiv(feature));
					var modalStyle = {
						'width' : 650, 
						'margin-left' : -350,
						'margin-top' : 100
					};
					standardModal.createModal('Annotate data item', components, modalStyle);
				});
			});
			
			var annotationDiv = $('<div />');
			var annotationText = '';
			var featureEntity = restApi.get('/api/v1/observablefeature/' + feature.id, ["unit", "definition"]);
			if(featureEntity.definition.items !== 0){
				var moreToShow = featureEntity.definition.items.length;
				$.each(featureEntity.definition.items, function(index, ontologyTerm){
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
						clickFeature.click();
					}).appendTo(annotationDiv);
				}
			}
			
			$('<td />').append(clickFeature).appendTo(row);
			$('<td />').append(popover).appendTo(row);
			$('<td />').append(annotationDiv).appendTo(row);
			return row;
		}
		
		function createTableHeader(){
			var headerRow = $('<tr />');
			$('<th>Name</th>').css('width', '15%').appendTo(headerRow);
			$('<th>Description</th>').css('width', '25%').appendTo(headerRow);
			$('<th>Annotation</th>').css('width', '40%').appendTo(headerRow);
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
					ns.ontologyTermTypeahead('ontologyTermSynonym', query, process);
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
					
					restApi.getAsync(this.feature.href, ["unit", "definition"], null, function(updatedFeature){
						var components = [];
						components.push(createFeatureTable(updatedFeature));
						components.push(createSearchDiv(updatedFeature));
						var modalStyle = {
							'width' : 650, 
							'margin-left' : -350,
							'margin-top' : 100
						};
						standardModal.createModal('Annotate data item', components, modalStyle);
						ns.createMatrixForDataItems();
					});
				},{'feature' : this.feature}));
			}
		}

		function updateAnnotation(feature, ontologyTermId, add){
			var data = {};
			$.map(feature, function(value, key){
				if(key !== 'href'){
					if(key === 'unit')
						data[key] = value.href.substring(value.href.lastIndexOf('/') + 1);
					else if(key === 'definition'){
						data[key] = [];
						$.each(value.items, function(index, element){
							data[key].push(element.href.substring(element.href.lastIndexOf('/') + 1));
						});
					}else
						data[key] = value;
				}	
			});
			if($.inArray(ontologyTermId, data.definition) === -1 && add) data.definition.push(ontologyTermId);
			if($.inArray(ontologyTermId, data.definition) !== -1 && !add) {
				var index = data.definition.indexOf(ontologyTermId);
				data.definition.splice(index, 1);
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
			var ontologyTermId = null;
			var query = {};
			query.name =  data.ontologyLabel + ':' + data.ontologyTerm;
			query.identifier = data.ontologyTermIRI;
			query.termAccession = data.ontologyTermIRI;
			query.description = data.ontologyTermLabel;
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
		
		function createFeatureTable(feature){
			
			var table = $('<table />').addClass('table table-bordered').attr('id', 'test-table'); 
			$('<tr><th class="feature-detail-th">ID : </th><td class="feature-detail-td">' + ns.hrefToId(feature.href) + '</td></tr>').appendTo(table);
			$('<tr><th>Name : </th><td>' + feature.name + '</td></tr>').appendTo(table);
			$('<tr><th>Description : </th><td>' + i18nDescription(feature).en + '</td></tr>').appendTo(table);
			
			if(feature.definition.items.length !== 0){
				var ontologyTermAnnotations = $('<ul />');
				$.each(feature.definition.items, function(index, ontologyTerm){
					var ontologyTermLink = $('<a href="' + ontologyTerm.termAccession + '" target="_blank">' + ontologyTerm.name + '</a>');
					var removeIcon = $('<i class="icon-remove"></i>').click($.proxy(function(){
						var ontologyTermId = this.ontologyTermHref.substring(this.ontologyTermHref.lastIndexOf('/') + 1)
						updateAnnotation(this.feature, ontologyTermId, false);
						restApi.getAsync(this.feature.href, ["unit", "definition"], null, function(updatedFeature){
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
							ns.createMatrixForDataItems();
							featureTableContainer.scrollTop(components[0].height());
						});
					}, {'feature' : feature, 'ontologyTermHref' : ontologyTerm.href}));
					
					$('<li />').append(ontologyTermLink).append(removeIcon).appendTo(ontologyTermAnnotations);
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
	
	ns.hrefToId = function(href){
		return href.substring(href.lastIndexOf('/') + 1); 
	}
	
	ns.updateSelectedDataset = function(dataSet) {
		selectedDataSet = dataSet;
	};
	
	ns.showMessageDialog = function(message){
		$('#alert-message').hide().empty();
		var content = '<button type="button" class="close" data-dismiss="alert">&times;</button>';
		content += '<p><strong>Message : </strong> ' + message + '</p>';
		$('#alert-message').append(content).addClass('alert alert-info').show();
		w.setTimeout(function(){
			$('#alert-message').fadeOut().empty();
		}, 10000);
		$(document).scrollTop(0);	
	};
	
	ns.dataItemsTypeahead = function (type, dataSetId, query, response){
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
	
	ns.ontologyTermTypeahead = function (field, query, response){
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
	
	function getSelectedDataSet(){
		return selectedDataSet;
	}
	
	$(document).ready(function(){
		$('#index-button').click(function(){
			if($('#uploadedOntology').val() !== ''){
				$('input[name="__action"]').val("indexOntology");
				$('#harmonizationIndexer-form').submit();
			}else{
				alert('Please upload a file in OWL or OBO format!');
			}
		});
		$('#refresh-button').click(function(){
			$('#ontologyannotator-form').attr({
				'action' : ns.getContextURL(),
				'method' : 'GET'
			}).submit();
		});

		$('#annotate-dataitems').click(function(){
			$('#ontologyannotator-form').attr({
				'action' : ns.getContextURL() + '/annotate',
				'method' : 'POST'
			}).submit();
		});
		
		$('#search-dataitem').on('keydown', function(e){
		    if (e.which == 13) {
		    	$('#search-button').click();
		    	return false;
		    }
		});
		
		$('#search-dataitem').on('keyup', function(e){
			if($(this).val() === ''){
				$('#clear-button').click();
		    }
		});
	});
}($, window.top));