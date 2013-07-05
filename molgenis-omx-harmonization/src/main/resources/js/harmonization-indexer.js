(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var selectedDataSet = null;
	var offSet = 1;
	var currentPage = 1;
	var pager = 10;
	var totalPage = 0;
	
	ns.changeDataSet = function(selectedDataSet){
		resetVariables();
		ns.updateSelectedDataset(selectedDataSet);
		ns.createMatrixForDataItems();
		function resetVariables(){
			offSet = 1;
			currentPage = 1;
		}
	};
	
	ns.createMatrixForDataItems = function() {
		searchApi.search(ns.createSearchRequest(), function(searchResponse) {
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
						createAnnotationModal(feature);
					});
				});
				
				var row = $('<tr />');
				$('<td />').append(clickFeature).appendTo(row);
				$('<td />').append(popover).appendTo(row);
				row.append('<td></td>');
				$(row).appendTo($('#dataitem-table'));
			});
			totalPage = Math.ceil(searchResponse.totalHitCount / pager) - 1;
			ns.updateMatrixPagination();
		});
		
		function createAnnotationModal(feature) {
			
			var modal = $('<div />');
			if($('#annotation-modal').length != 0){
				modal = $('#annotation-modal');
				modal.empty();
			}else{
				$('body').append(modal);
			}
			modal.addClass('modal hide');
			modal.attr('id', 'annotation-modal');
			modal.attr('data-backdrop', false);
			
			var header = $('<div />');
			header.addClass('modal-header');
			header.append('<button type="button" name="annotation-btn-close" class="close" data-dismiss="#annotation-modal" data-backdrop="true" aria-hidden="true">&times;</button>');
			header.append("<h3>Annotate data item</h3>");
			
			var body = $('<div />');
			body.addClass('modal-body');
			body.append(featureTable(feature));
			body.append(createSearchDiv(feature));
			
			var footer = $('<div />');
			footer.addClass('modal-footer');
			footer.append('<button name="annotation-btn-close" class="btn" data-dismiss="#annotation-modal" aria-hidden="true">Close</button>');
			
			modal.append(header);
			modal.append(body);
			modal.append(footer);
			modal.modal('show');
			
			$('button[name="annotation-btn-close"]').click(function(){
				$('#annotation-modal').modal('hide');
			});
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
					ns.searchOntologyTerms(query, process);
				},
				minLength : 3,
				items : 10
			});
			addTermButton.click($.proxy(checkOntologyTerm, {'searchField' : searchField, 'feature' : feature}));
			return searchDiv.append(searchGroup);
		}
		
		function checkOntologyTerm(){
			var dataMap = $(document).data('dataMap');
			var ontologyTerm = this.searchField.val();
			var toCreate = true;
			if(dataMap && ontologyTerm !== ''){
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
					if(ontologyTermId != null) addAnnotation(restApi.get(this.feature.href), ontologyTermId);
					$('#annotation-modal').modal('hide');
					restApi.getAsync(this.feature.href, ["unit", "definition"], null, function(updatedFeature){
						createAnnotationModal(updatedFeature);
					});
				},{'feature' : this.feature}));
			}
		}

		function addAnnotation(feature, ontologyTermId){
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
			if($.inArray(ontologyTermId, data.definition) === -1) data.definition.push(ontologyTermId);
			updateOntologyTerm(feature, data);
		}
		
		function updateOntologyTerm(feature, data){
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
			query.name = data.ontologyTerm;
			query.identifier = data.ontologyTermIRI.replace(/\W/g, '');
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
		
		function featureTable(feature){
			var table = $('<table class="table table-bordered"></table>');
			table.append('<tr><th>ID : </th><td>' + feature.href.substring(feature.href.lastIndexOf('/') + 1) + '</td></tr>');
			table.append('<tr><th>Name : </th><td>' + feature.name + '</td></tr>');
			table.append('<tr><th>Description : </th><td>' + feature.description + '</td></tr>');
			if(feature.definition.items.length != 0){
				var ontologyTermAnnotations = $('<ul />');
				$.each(feature.definition.items, function(index, element){
					var uri = element.termAccession;
					var linkOut = $('<a href="' + uri + '" target="_blank">' + element.name + '</a>');
//					linkOut.popover({
//						content : uri,
//						title : uri,
//						trigger : 'hover',
//						placement : 'right'
//					});
					$('<li />').append(linkOut).appendTo(ontologyTermAnnotations);
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
	
	ns.updateMatrixPagination = function() {
		if(totalPage !== 0){
			$('.pagination ul').empty();
			$('.pagination ul').append('<li><a href="#">Prev</a></li>');
			var displayedPage = (totalPage < 10 ? totalPage : 9) + offSet; 
			for(var i = offSet; i <= displayedPage ; i++){
				var element = $('<li />');
				if(i == currentPage)
					element.addClass('active');
				element.append('<a href="/">' + i + '</a>');
				$('.pagination ul').append(element);
			}
			var lastPage = totalPage + 1 > 10 ? totalPage + 1 : 10;
			if(totalPage - offSet > 9){
				$('.pagination ul').append('<li class="active"><a href="#">...</a></li>');
				$('.pagination ul').append('<li><a href="#">' + lastPage + ' </a></li>');
			}
			$('.pagination ul').append('<li><a href="#">Next</a></li>');
			$('.pagination ul li').each(function(){
				$(this).click(function(){
					var pageNumber = $(this).find('a').html();
					if(pageNumber === "Prev"){
						if(currentPage > offSet) currentPage--;
						else if(offSet > 1) {
							offSet--;
							currentPage--;	
						}
					}else if(pageNumber === "Next"){
						if(currentPage <= totalPage) {
							currentPage++;
							if(currentPage >= offSet + 9) offSet++;
						}
					}else if(pageNumber !== "..."){
						currentPage = parseInt(pageNumber);
						if(currentPage > offSet + 9){
							offSet = currentPage - 9;
						} 
					}
					ns.createMatrixForDataItems();
					return false;
				});
			});
		}
	};
	
	ns.createSearchRequest = function() {
		var queryRules = [];
		//todo: how to unlimit the search result
		queryRules.push({
			operator : 'LIMIT',
			value : pager
		});
		queryRules.push({
			operator : 'OFFSET',
			value : (currentPage - 1) * pager
		});
		queryRules.push({
			operator : 'SEARCH',
			value : 'observablefeature'
		});
		var searchRequest = {
			documentType : 'protocolTree-' + getSelectedDataSet(),
			queryRules : queryRules
		};
		return searchRequest;
	};
	
	ns.updateSelectedDataset = function(dataSet) {
		selectedDataSet = dataSet;
	};
	
	ns.annotateDataItems = function() {
		$('input[name="__action"]').val("annotateDataItems");
		$('#harmonizationIndexer-form').submit();
	};
	
	ns.searchOntologyTerms = function (query, response){
		
		var queryRules = [{
			field : 'ontologyTermSynonym',
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
					result.push(hit.columnValueMap.ontologyTerm);
					dataMap[hit.columnValueMap.ontologyTerm] = hit.columnValueMap;
				}
			});
			$(document).data('dataMap', dataMap);
			response(result);
		});
	}
	
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
			$('#harmonizationIndexer-form').submit();
		});
		$('#annotate-dataitems').click(function(){
			ns.annotateDataItems();
		});
	});
}($, window.top));