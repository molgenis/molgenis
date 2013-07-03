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
				var row = $('<tr />');
				row.append('<td>' + feature.name + '</td>');
				$('<td />').append(popover).appendTo(row);
				row.append('<td></td>');
				$(row).appendTo($('#dataitem-table'));
			});
			totalPage = Math.ceil(searchResponse.totalHitCount / pager) - 1;
			ns.updateMatrixPagination();
		});
		
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