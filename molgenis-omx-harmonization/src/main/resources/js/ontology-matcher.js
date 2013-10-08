(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var pagination = new ns.Pagination();
	var standardModal = new ns.StandardModal();
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var selectedDataSet = null;
	var sortRule = null;
	
	ns.addTargetDataSet = function(targetDataSetId) {
		var selectedOptions = $('#target-catalogue').data('selectedOptions') === undefined ? [] : $('#target-catalogue').data('selectedOptions');
		if(targetDataSetId !== null && targetDataSetId !== undefined){
			if($.inArray(targetDataSetId, selectedOptions) === -1){
				selectedOptions.push(targetDataSetId);
				$('#target-catalogue').data('selectedOptions', selectedOptions);
				switchOptions();
			}
		}
		$('#selectedTargetDataSets').val(selectedOptions);
		renderOptions();
		function renderOptions(){
			var targetCatalogues = $('#target-catalogue');
			var dataSetDiv = $('<div />').addClass('span10');
			targetCatalogues.css('margin-top', '20px').empty().append(dataSetDiv);
			$.each(selectedOptions, function(index, targetDataSetId){
				var dataSet = restApi.get('/api/v1/dataset/' + targetDataSetId);
				var nameDiv = $('<div />').addClass('span4').append(dataSet.name);
				var controlDiv = $('<div />').addClass('offset4 span4');
				var viewCatalogue = $('<button type="btn" class="btn btn-link">View</button>').click(function(){
					changeDataSet(targetDataSetId);
					$('#catalogue-container').show();
					return false;
				});
				var hideCatalogue = $('<button type="btn" class="btn btn-link">Hide</button>').click(function(){
					$('#catalogue-container').hide().find('table').empty();
					sortRule = null;
					return false;
				});
				var removeCatalogue = $('<button type="btn" class="btn btn-link">Remove</button>').click(function(){
					var index = selectedOptions.indexOf(targetDataSetId);
					selectedOptions.splice(index, 1);
					$('#target-catalogue').data('selectedOptions', selectedOptions);
					sortRule = null;
					renderOptions();
					$('#catalogue-container').hide().find('table').empty();
					return false;
				});
				$('<div />').addClass('btn-group').append(viewCatalogue).append(hideCatalogue).append(removeCatalogue).appendTo(controlDiv);
				$('<div />').addClass('row-fluid').append(nameDiv).append(controlDiv).appendTo(dataSetDiv);
			});
		}
		
		function switchOptions(){
			var index = 0;
			var options = $('#targetDataSets option');
			options.attr('selected',false).each(function(){
				if(targetDataSetId !== $(this).val()){
					index++;
				}else return false;
			});
			index = index === options.length - 1 ? 0 : index + 1;
			$(options[index]).attr('selected', true);
		}
	};
	
	ns.selectCatalogue = function(action){
		var selectedOptions = $('#target-catalogue').data('selectedOptions');
		var selectedSourceDataSetId = $('#sourceDataSet').val();
		var selectedDataSets = [];
		if(selectedOptions !== undefined && selectedOptions !== null){
			$.each(selectedOptions, function(index, dataSetId){
				selectedDataSets.push(dataSetId);
			});
		}
		var request = {
			'sourceDataSetId' : selectedSourceDataSetId,
			'selectedDataSetIds' : selectedDataSets
		}
		$.ajax({
			type : 'POST',
			url : ns.getContextURL() + '/ontologymatcher/' + action,
			data : JSON.stringify(request),
			contentType : 'application/json',
			async : false,
			success : function(response) {
//				if(response.isRunning){ 
//					$('#start-match').attr('disabled', 'disabled');
//					$('#confirm-match').hide();
//				} else {
//					$('#confirm-match').show();
//					$('#start-match').attr('disabled', 'disabled');
//				}
			},
			error : function(status) {
				alert('error');
			}
		});	
	};
	
	function changeDataSet (selectedDataSetId){
		if(selectedDataSetId !== ''){
			var dataSetEntity = restApi.get('/api/v1/dataset/' + selectedDataSetId);
			$('#selected-catalogue').empty().append(dataSetEntity.name);
			var request = {
				documentType : 'protocolTree-' + ns.hrefToId(dataSetEntity.href),
				queryRules : [{
					field : 'type',
					operator : 'EQUALS',
					value : 'observablefeature'
				}]
			};
			searchApi.search(request, function(searchResponse){
				pagination.reset();
				updateSelectedDataset(selectedDataSetId);
				createMatrixForDataItems();
				initSearchDataItems(dataSetEntity);
			});
		}
		
		function initSearchDataItems (dataSet) {
			$('#search-dataitem')
			.on('keydown', function(e){
			    if (e.which == 13) {
			    	$('#search-button').click();
			    	return false;
			    }
			}).on('keyup', function(e){
				if($(this).val() === ''){
					createMatrixForDataItems();
			    }
			});
			$('#search-button').click(function(){
				createMatrixForDataItems();
			});
		}
	};
	
	function createMatrixForDataItems () {
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
			pagination.updateMatrixPagination($('.pagination ul'), createMatrixForDataItems);
		});
		
		function createTableRow(feature){
			var row = $('<tr />');
			var description = feature.description;
			var isPopOver = description.length < 120;
			var descriptionSpan = $('<span />').html(isPopOver ? description : description.substring(0, 120) + '...');
			if(!isPopOver){
				descriptionSpan.addClass('show-popover');
				descriptionSpan.popover({
					content : description,
					trigger : 'hover',
					placement : 'bottom'
				});
			}
			var featureNameSpan = $('<span>' + feature.name + '</span>');
			$('<td />').append(featureNameSpan).appendTo(row);
			$('<td />').append(descriptionSpan).appendTo(row);
			return row;
		}
		
		function createTableHeader(){
			var headerRow = $('<tr />');
			var firstColumn = $('<th>Name</th>').css('width', '30%').appendTo(headerRow);
			if (sortRule) {
				if (sortRule.operator == 'SORTASC') {
					$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-s down float-right"></span>').appendTo(firstColumn);
				} else {
					$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-n up float-right"></span>').appendTo(firstColumn);
				}
			} else {
				$('<span data-value="Name" class="ui-icon ui-icon-triangle-2-n-s updown float-right"></span>').appendTo(firstColumn);
			}
			$('<th>Description</th>').css('width', '70%').appendTo(headerRow);
			
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
				createMatrixForDataItems();
				return false;
			});
			
			return $('<thead />').append(headerRow);
		}
		
		function getSelectedDataSet(){
			return selectedDataSet;
		}
	};
	
	function updateSelectedDataset (dataSet) {
		selectedDataSet = dataSet;
	}
	
	function addAllDataSets(){
		var selectedOptions = [];
		$('#targetDataSets option').each(function(){
			selectedOptions.push($(this).val());
		});
		$('#target-catalogue').data('selectedOptions', selectedOptions);
		ns.addTargetDataSet();
	}
	
	function removeAllSelectedDataSets(){
		var selectedOptions = [];
		$('#target-catalogue').data('selectedOptions', selectedOptions);
		ns.addTargetDataSet();
		$('#catalogue-container').hide().find('table').empty();
	}
	
	$(document).ready(function(){
		$('#add-target-dataset').click(function(){
			var targetDataSet = $('#targetDataSets option:selected');
			ns.addTargetDataSet(targetDataSet.val());
			return false;
		});
		
		$('#remove-target-all-datasets').click(function(){
			removeAllSelectedDataSets();
			return false;
		});
		
		$('#add-target-all-datasets').click(function(){
			addAllDataSets();
			return false;
		}).trigger('click');
		
		$('#confirm-match').click(function(){
			ns.selectCatalogue('match');
		}).hide();
		
		$('#next-button').click(function(){
			ns.selectCatalogue('check');
		});
		
		$('#start-match').click(function(){
			ns.selectCatalogue('check');
		});
	});
}($, window.top));