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
			createTableHeader();
			var searchHits = searchResponse.searchHits;
			$.each(searchHits, function(){
				var feature = $(this)[0]["columnValueMap"];
				var description = feature.description;
				var isPopOver = description.length < 50;
				var popover = $('<span />');
				popover.html(isPopOver ? description : description.substring(0, 50) + '...');
				if(!isPopOver){
					popover.addClass('show-popover');
					popover.popover({
						content : description,
						trigger : 'hover',
						placement : 'bottom'
					});
				}
				var clickFeature = $('<span class="text-info show-popover">' + feature.name + '</span>');
				clickFeature.data('feature', feature);
				clickFeature.click(function(){
					var featureId = $(this).data('feature').id;
					restApi.getAsync('/api/v1/observablefeature/' + featureId, ["unit", "definition"], null, function(feature){
						var components = [];
						components.push(createFeatureTable(feature));
						components.push(createSearchDiv(feature));
						standardModal.createModal('Annotate data item', components);
					});
				});
				
				var row = $('<tr />');
				$('<td />').append(clickFeature).appendTo(row);
				$('<td />').append(popover).appendTo(row);
				var annotation = $('<td />');
				var featureEntity = restApi.get('/api/v1/observablefeature/' + feature.id, ["unit", "definition"]);
				if(featureEntity.definition.items !== 0){
					var annotationText = '';
					$.each(featureEntity.definition.items, function(index, ontologyTerm){
						annotationText += ontologyTerm.name + ' , ';
					});
					annotation.append(annotationText.substring(0, annotationText.length - 3));
				}
				
				annotation.appendTo(row);
				$(row).appendTo($('#dataitem-table'));
			});
			pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()) - 1);
			pagination.updateMatrixPagination($('.pagination ul'), ns.createMatrixForDataItems);
		});
		
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
					$('#annotation-modal').modal('hide');
					restApi.getAsync(this.feature.href, ["unit", "definition"], null, function(updatedFeature){
						var components = [];
						components.push(createFeatureTable(updatedFeature));
						components.push(createSearchDiv(updatedFeature));
						standardModal.createModal('Annotate data item', components);
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
			var table = $('<table class="table table-bordered"></table>'); 
			if(feature.description === undefined) feature.description = '';
			if(feature.description.indexOf('{') !== 0){
				feature.description = '{"en":"' + (feature.description === null ? '' : feature.description) +'"}';
			}
			var description = eval('(' + feature.description + ')');
			table.append('<tr><th>ID : </th><td>' + feature.href.substring(feature.href.lastIndexOf('/') + 1) + '</td></tr>');
			table.append('<tr><th>Name : </th><td>' + feature.name + '</td></tr>');
			table.append('<tr><th>Description : </th><td>' + (description === undefined ? '' : description.en) + '</td></tr>');
			if(feature.definition.items.length !== 0){
				var ontologyTermAnnotations = $('<ul />');
				$.each(feature.definition.items, function(index, ontologyTerm){
					var uri = ontologyTerm.termAccession;
					var linkOut = $('<a href="' + uri + '" target="_blank">' + ontologyTerm.name + '</a>');
					var removeIcon = $('<i class="icon-remove"></i>');
					$('<li />').append(linkOut).append(removeIcon).appendTo(ontologyTermAnnotations);
					removeIcon.click($.proxy(function(){
						var ontologyTermId = this.ontologyTermHref.substring(this.ontologyTermHref.lastIndexOf('/') + 1)
						updateAnnotation(this.feature, ontologyTermId, false);
						$('#annotation-modal').modal('hide');
						restApi.getAsync(this.feature.href, ["unit", "definition"], null, function(updatedFeature){
							var components = [];
							components.push(createFeatureTable(updatedFeature));
							components.push(createSearchDiv(updatedFeature));
							standardModal.createModal('Annotate data item', components);
							ns.createMatrixForDataItems();
						});
					}, {'feature' : feature, 'ontologyTermHref' : ontologyTerm.href}));
				});
				var annotationRow = $('<tr />');
				annotationRow.append('<th>Annotation : </th>');
				annotationRow.append($('<td />').append(ontologyTermAnnotations));
				table.append(annotationRow);
			}else{
				table.append('<tr><th>Annotation : </th><td>Not available</td></tr>');
			}
			return table;
		}
		
		function createTableHeader(){
			$('#dataitem-table').empty();
			$('#dataitem-table').append('<thead><tr><th>Name</th><th>Description</th><th>Annotation</th></tr></thead>');
		}
	};
	
	
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
	
	$(function() {
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
	});
}($, window.top));