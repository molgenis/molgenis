(function($, molgenis, w) {
	"use strict";
	
	var ontologyAnnotator = new molgenis.OntologyAnnotator();
	var standardModal = new molgenis.StandardModal();
	var restApi = new molgenis.RestClient();
	var selectedDataSet = null;
	var userName = null;
	var biobankDataSets = null;
	var storeMappingMappedFeature = 'store_mapping_mapped_feature';
	var observationSet = 'observationsetid';
	
	molgenis.MappingManager = function MappingManager(){};
	
	molgenis.MappingManager.prototype.changeDataSet = function(userName, selectedDataSetId, dataSetIds){
		
		//Initialize selectedDataset with default value null
		selectedDataSet = null;
		//Clear the previous messages if there are any 
		if($('#mapping-unavailable')) $('#mapping-unavailable').remove();
		//Check the nullibity of target selecteDataSetId
		if(!selectedDataSetId || selectedDataSetId === ''){
			var mappingUnavailable = $('<p />').attr('id', 'mapping-unavailable').addClass('text-align-center').append('The target catalogue cannot be empty!').css('margin-top', '50px');
			$('#container').empty().after(mappingUnavailable);
			$('#dataitem-number').empty().append('Nothing selected');
			$('#div-search').hide();
		}else{
			selectedDataSet = restApi.get('/api/v1/dataset/' + selectedDataSetId, {'expand' : ['protocolUsed']});
			//Check the nullibity of selecte biobank dataSetIds
			if(!dataSetIds || dataSetIds.length === 0){
				var mappingUnavailable = $('<p />').attr('id', 'mapping-unavailable').addClass('text-align-center').append('There are no matches produced for catalog <strong>' + selectedDataSet.Name + '</strong> . Please click on BiobankConnectWizard to produce new match sets!').css('margin-top', '50px');
				$('#container').empty().after(mappingUnavailable);
				$('#dataitem-number').empty().append('Nothing selected');
				$('#div-search').hide();
			}else{
				$('#div-search').show();
				setUserName(userName); 
				biobankDataSets = restApi.get('/api/v1/dataset/', {
					'q' : {
						'q' : [{
							'field' : 'id',
							'operator' : 'IN',
							'value' : remove(selectedDataSetId, dataSetIds)
						}]
					},
					'expand' : ['protocolUsed']
				}).items;
				$('#dataitem-number').empty().append(molgenis.getTotalNumberOfItems(selectedDataSetId));
				updateMatrix();
				initSearchDataItems();
			}
		} 
		
		function initSearchDataItems() {
			var options = {'updatePager' : true};
			$('#search-dataitem').typeahead({
				source: function(query, process) {
					molgenis.dataItemsTypeahead(molgenis.hrefToId(selectedDataSet.href), query, process);
				},
				minLength : 3,
				items : 20
			}).on('keydown', function(e){
			    if (e.which == 13) {
			    	$('#search-button').click();
			    	return false;
			    }
			}).on('keyup', function(e){
				if($(this).val() === ''){
					updateMatrix(options);
			    }
			});
			$('#search-button').click(function(){
				updateMatrix(options);
			});
		}
	};
	
	function updateMatrix(options){
		var involvedDataSetNames = [];
		involvedDataSetNames.push(selectedDataSet.Name);
		$.each(biobankDataSets, function(index, dataSet){
			involvedDataSetNames.push(dataSet.Name);
		});
		var default_options = {
			'dataSetId' : molgenis.hrefToId(selectedDataSet.href),
			'tableHeaders' : involvedDataSetNames,
			'queryText' : $('#search-dataitem').val(),
			'sortRule' : null,
			'createTableRow' : createTableRow,
			'updatePager' : false,
			'container' : $('#container')
		}
		if(options !== undefined && options !== null){
			$.extend(default_options, options);
		}
		molgenis.createMatrixForDataItems(default_options);
	}
	
	function createTableRow(featureFromIndex, table){
		var row = $('<tr />');
		var feature = restApi.get('/api/v1/observablefeature/' + featureFromIndex.id);
		var description = '<strong>' + feature.Name + '</strong> : ' + molgenis.i18nDescription(feature).en;
		var popover = makePopoverComponenet(description, Math.ceil((table.find('th:eq(0)').width() - 135)/4), molgenis.i18nDescription(feature).en);
		$('<td />').addClass('add-border show-popover').append(popover).appendTo(row).click(function(){
			var row = $(this).parents('tr:eq(0)');
			if(!$('body').data('clickedRow')) $('body').data('clickedRow', {});
			var storedRowInfo = $('body').data('clickedRow');
			storedRowInfo[molgenis.hrefToId(feature.href)] = row;
			createAnnotationModal(feature);
		});
		
		//Calculate the number of digits to show in the cell and full content is displayed in popover
		var numberOfDigit = Math.ceil(((table.width() - table.find('th:eq(0)').width()) / biobankDataSets.length - 80)/8);
		$.each(biobankDataSets, function(index, mappedDataSet){
			var removeIcon = $('<i />').addClass('icon-trash show-popover float-right');
			var editIcon = $('<i />').addClass('icon-pencil show-popover float-right');
			var cellDiv = $('<div />').addClass('row-fluid text-align-center').append(removeIcon).append(editIcon);
			$('<td />').append(cellDiv).appendTo(row);
			getMapping(createDataSetIdentifier(selectedDataSet, mappedDataSet), molgenis.hrefToId(feature.href), function(mappedFeatureNames){
				var topFeatureDivContainer = $('<div />').addClass('show-popover').appendTo(cellDiv).click(function(){
					editIcon.click();
				});
				if(mappedFeatureNames.length > 0){
					switchBackgroundIcon(editIcon, mappedFeatureNames);
					topFeatureDivContainer.append(makePopoverComponenet(mappedFeatureNames.join(','), numberOfDigit, mappedFeatureNames.join('<br />')));
				}
				//Initialize the edit click events
				editIcon.click(function(){
					createMappings(selectedDataSet, mappedDataSet, feature, function(candidateMappedFeatures){
						standardModal.createModalCallback('Candidate mappings', function(modal){
							//Customize modal
							modal.modal('show').css({
								'width' : '90%',
								'margin-left' : '-45%',
								'top' : '15%'
							});
							
							//Modal header information.
							var infoDiv = $('<div />').addClass('span4').css({
								'position' : 'absolute',
								'margin-left' : 0,
								'margin-top' : 25,
								'z-index' : 10000
							});
							$('<div />').append('<h4>' + selectedDataSet.Name + '</h4>').appendTo(infoDiv);
							$('<div />').append('<span class="info"><strong>Data item : </strong></span>').append('<span>' + feature.Name + '</span>').appendTo(infoDiv);
							$('<div />').append('<span class="info"><strong>Description : </strong></span>').append('<span>' + molgenis.i18nDescription(feature).en + '</span>').appendTo(infoDiv);
							var selectedMappings = $('<div />').append('<span class="info"><strong>Selected mappings : </strong></span>').append('<span>' + mappedFeatureNames.join(' , ') + '</span>').appendTo(infoDiv);
							modal.find('.modal-header:eq(0)').append(infoDiv);
							
							//Modal body information.
							var mappingTable = createMappingTable(feature, candidateMappedFeatures, mappedFeatureNames);
							var tableDiv = $('<div />').addClass('span7').css('margin-left', '-10px').append('<div><h4>' + mappedDataSet.Name +  '</h4></div>').append(mappingTable);
							var body = modal.find('.modal-body:eq(0)').addClass('overflow-y-auto').css({
								'min-height' : 250,
								'max-height' : 350
							});
							$('<div />').addClass('row-fluid').append('<div class="span5"></div>').append(tableDiv).appendTo(body);
							
							//Modal footer information.
							var confirmButton = $('<button class="btn btn-primary">Confirm</button>');
							modal.find('.modal-footer:eq(0)').prepend(confirmButton);
							
							//Confirm button event handler
							confirmButton.click(function(){
								var mappedFeatureMap = updateMappingInfo(feature, mappingTable, mappedDataSet, mappedFeatureNames);
								mappedFeatureNames = mappedFeatureMap.mappedFeatureNames;
								switchBackgroundIcon(editIcon, mappedFeatureNames);
								editIcon.siblings('div:eq(0)').empty().append(makePopoverComponenet(mappedFeatureNames.join(','), numberOfDigit, mappedFeatureNames.join('<br />')));
								standardModal.closeModal();
							});
							//table check box handler
							tableDiv.find('input[type="checkbox"]').click(function(){
								var dataItems = [];
								tableDiv.find('input:checked').each(function(index, checkbox){
									dataItems.push($(checkbox).data('eachMapping').mappedFeature.Name);
								});
								selectedMappings.find('span:eq(1)').html(dataItems.join(' , '));
							});
						});
					});
				});
				
				//Initialize the remove click event
				removeIcon.click(function(){
					standardModal.createModalCallback('Confirmation', function(modal){
						var confirmButton = $('<button type="btn" class="btn btn-primary">Confirm</button>');
						var confirmationMessage = $('<p />').css('font-size', '16px').addClass('text-align-center');
						confirmationMessage.append('Are you sure that you want to remove candidate mappings for ');
						confirmationMessage.append('<span style="text-decoration:underline;"><strong>' + feature.Name + '</strong></span> in dataset ');
						confirmationMessage.append('<span style="text-decoration:underline;"><strong>' + mappedDataSet.Name + '</strong></span> ?');
						modal.find('div.modal-body:eq(0)').append(confirmationMessage);
						modal.find('div.modal-footer:eq(0)').prepend(confirmButton);
						modal.modal('show').css({
							'width' : '50%',
							'margin-left' : '-25%',
							'top' : '30%'
						});
						//Initialize the event for confirm button
						confirmButton.click(function(){
							removeSelectedMappings(molgenis.hrefToId(feature.href), [molgenis.hrefToId(mappedDataSet.href)], function(data){
								molgenis.getFeatureFromIndex(feature, function(featureFromIndex){
									updateExistingRow(featureFromIndex.columnValueMap, row, [molgenis.hrefToId(mappedDataSet.href)]);
									modal.modal('hide');									
								});
							});
						});
					});
				});
			});
		});
		return row;
		
		//Helper class for createTableRow function...
		function makePopoverComponenet(text, length, content){
			var popover = $('<span />').html(text.length < length ? text : text.substring(0, length) + '..');
			if(!(text.length < length)){
				popover.addClass('show-popover').popover({
					'content' : content,
					'trigger' : 'hover',
					'placement' : 'bottom',
					'html' : true
				});
			}
			return popover;
		}
		
		function switchBackgroundIcon(editIcon, mappedFeatureNames){
			if(mappedFeatureNames.length === 0){
				editIcon.removeClass('icon-ok').addClass('icon-pencil').parents('td:eq(0)').css('background', '');
			}else{
				editIcon.removeClass('icon-pencil').addClass('icon-ok').parents('td:eq(0)').css('background-color', 'rgb(223, 235, 245)');
			}
		}	
	}
	
	function updateExistingRow(featureFromIndex, existingRow, rematchedSelectedDataSetIds){
		var biobankDataSetIds = [];
		$.each(biobankDataSets, function(index, dataSet){
			biobankDataSetIds.push(molgenis.hrefToId(dataSet.href));
		});
		$.each(rematchedSelectedDataSetIds, function(index, dataSetId){
			var index = $.inArray(dataSetId, biobankDataSetIds);
			if(index !== -1){
				var spinner = $('<img src="/img/waiting-spinner.gif">').css('height', '20px');
				existingRow.find('td:eq(' + (index + 1) + ')').empty().append(spinner).css('text-align', 'center');
			}
		});
		if(rematchedSelectedDataSetIds.length > 0){
			var columnIndex = $.inArray(rematchedSelectedDataSetIds[0], biobankDataSetIds)
			setTimeout(function(){
				var table = existingRow.parents('table:eq(0)');
				var rowIndex = $.inArray(existingRow[0], table.find('tr'));
				var updatedRow = createTableRow(featureFromIndex, table);
				var columnToUpdate = updatedRow.find('td:eq(' + (columnIndex + 1) + ')');
				table.find('tr:eq(' + --rowIndex + ')').after(updatedRow);
				existingRow.remove();
			}, 1500);
		}
	}
	
	function removeSelectedMappings(featureId, mappedDataSetIds, callback){
		var request = {
			'sourceDataSetId' : molgenis.hrefToId(selectedDataSet.href),
			'selectedDataSetIds' : mappedDataSetIds,
			'featureId' : featureId,
			'mappedFeatureIds' : []
		};
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/savescript',
			data : JSON.stringify(request),
			contentType : 'application/json',
			success : function(data){
				if(callback !== undefined && callback !== null && typeof(callback) === 'function'){
					callback(data);
				}
			}
		});
	}
	
	function updateMappingInfo(feature, mappingTable, mappedDataSet, mappedFeatureNames){
		var selectedFeatureMap = {};
		$.each(mappingTable.find('input'), function(index, checkBox){
			var eachMapping = $(checkBox).data('eachMapping');
			var changedValue = null;
			if(checkBox.checked)
			{
				selectedFeatureMap[eachMapping.mappedFeature.Name] = molgenis.hrefToId(eachMapping.mappedFeature.href);
			}
		});
		var results = {
			'mappedFeatureNames' : Object.keys(selectedFeatureMap)
		}
		if(!compareTwoArrays(selectedFeatureMap, mappedFeatureNames)){
			var mappedFeatureIds = Object.keys(selectedFeatureMap).map(function(key){
				return selectedFeatureMap[key];
			});
			var request = {
				'sourceDataSetId' : molgenis.hrefToId(selectedDataSet.href),
				'selectedDataSetIds' : [molgenis.hrefToId(mappedDataSet.href)],
				'featureId' : molgenis.hrefToId(feature.href),
				'mappedFeatureIds' : mappedFeatureIds
			};
			$.ajax({
				type : 'POST',
				url : molgenis.adaptContextUrl() + '/savescript',
				data : JSON.stringify(request),
				async : false,
				contentType : 'application/json',
				success : function(data){				
					$.extend(results, data);
				}
			});
		}
		return results;
	}
	
	function getMapping(dataSetIdentifier, featureId, callback){
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/getmapping',
			data : JSON.stringify({'dataSetIdentifier' : dataSetIdentifier , 'featureIds' : [featureId]}),
			async : false,
			contentType : 'application/json',
			success : function(data, textStatus, request){				
				var results = [];
				if(data.searchHits.length > 0){
					var hit = data.searchHits[0].columnValueMap;
					var mappedFeatureString = hit[storeMappingMappedFeature];
					var mappedFeatureIds = (mappedFeatureString.length > 2) ? mappedFeatureString.substring(1, mappedFeatureString.length - 1).split(/\s*,\s*/) : [];
					if(mappedFeatureIds.length > 0){
						var mappedFeatureItems = restApi.get('/api/v1/observablefeature/', {
							'q' : {
								'q' : [{
									'field' : 'id',
									'operator' : 'IN',
									'value' : mappedFeatureIds
								}]
							}
						});
						$.each(mappedFeatureItems.items, function(index, feature){
							if($.inArray(feature.Name, results) === -1) results.push(feature.Name);
						});
					}
				}
				callback(results)
			}
		});
	}
	
	function createMappings(selectedDataSet, mappedDataSet, feature, callback){
		var searchRequest = {
			featureId : molgenis.hrefToId(feature.href),
			sourceDataSetId : molgenis.hrefToId(selectedDataSet.href),
			selectedDataSetIds : [molgenis.hrefToId(mappedDataSet.href)]
		};
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/createmapping',
			data : JSON.stringify(searchRequest),
			contentType : 'application/json',
			success : function(data, textStatus, request) {	
				var mappedFeatues = [];
				var existingIds = [];
				$.each(data.searchHits, function(index, hit){
					if($.inArray(hit.columnValueMap.id, existingIds) === -1){
						existingIds.push(hit.columnValueMap.id);
						mappedFeatues.push(hit.columnValueMap);
					}
				});
				callback(mappedFeatues);
			},
			error : function(request, textStatus, error){
				console.log(error);
				callback([]);
			}
		});
	}
	
	function createMappingTable(feature, candidateMappedFeatures, mappedFeatureNames){
		var mappingTable = $('<table />').addClass('table table-bordered').append('<tr><th>Name</th><th>Description</th><th>Score</th><th>Select</th></tr>');
		var scores = [];
		$.each(candidateMappedFeatures, function(index, eachMapping){
			scores.push(eachMapping.score);
		});
		var highScoresIndex = [];
		if(scores.length > 2){
			var classifications = ss.jenks(scores, 2);
			var naturalBreak = classifications[1];
			$.each(scores, function(index, score){
				if(score > naturalBreak){
					highScoresIndex.push(index);
				}
			});
		} else {
			$.each(scores, function(index, score){
				highScoresIndex.push(index);
			});
		}
		var soretedMappedFeatures = sortByScoreAndLength(candidateMappedFeatures);
		$.each(soretedMappedFeatures, function(index, mappedFeatureFromIndex){
			var mappedFeatureId = mappedFeatureFromIndex.id;
			var score = mappedFeatureFromIndex.score;
			var mappedFeature = restApi.get('/api/v1/observablefeature/' + mappedFeatureId);
			if(mappedFeature){
				var row = $('<tr />');
				var checkBox = $('<input type="checkbox">');
				mappedFeatureFromIndex.mappedFeature = mappedFeature;
				checkBox.data('eachMapping', mappedFeatureFromIndex);
				checkBox.attr('checked', $.inArray(mappedFeature.Name, mappedFeatureNames) !== -1)
				row.append('<td>' + mappedFeature.Name + '</td><td>' + molgenis.i18nDescription(mappedFeature).en + '</td><td>' + score + '</td>');
				row.append($('<td />').append($('<label class="checkbox"></label>').append(checkBox))).appendTo(mappingTable);
			}
			if($.inArray(index, highScoresIndex) !== -1){
				row.addClass('info');
			}else{
				row.addClass('warning');
			}
		});
		return mappingTable;
	}
	
	function sortByScoreAndLength(mappedFeatures){
		var total = mappedFeatures.length;
		var subSetMappings = mappedFeatures.slice(0, total < 10 ? total : 10);
		var map = {};
		$.each(subSetMappings, function(index, mappedFeatureFromIndex){
			var score = mappedFeatureFromIndex.score;
			mappedFeatureFromIndex.comparedScore = score / mappedFeatureFromIndex.description.length;
			if(!map[score]){
				map[score] = [];
			}
			map[score].push(mappedFeatureFromIndex);
		});
		var topTenOrder = [];
		$.each(map, function(score, mappings){
			if(mappings.length > 1 && score > 0){
				mappings.sort(function(a,b){
					return molgenis.naturalSort(b.comparedScore, a.comparedScore);
				});
			}
			topTenOrder = topTenOrder.concat(mappings);
		});
		if(total > 10) topTenOrder = topTenOrder.concat(mappedFeatures.slice(10, total));
		return topTenOrder;
	}
	
	function createAnnotationModal(feature){
		var title = 'Rematch research variable : ';
		restApi.getAsync(feature.href, {'expand': ["unit", "definitions"]}, function(updatedFeature){
			var modal = ontologyAnnotator.createFeatureTable(title, updatedFeature, createAnnotationModal);
			var body = modal.find('div.modal-body:eq(0)').addClass('overflow-y-visible');
			var footer = modal.find('div.modal-footer:eq(0)');
			var nextButton = $('<button type="btn" class="btn btn-primary">Next</button>').click(function(){
				createRematchingModal(updatedFeature);
			});
			footer.prepend(nextButton);
			var table = $('body table:eq(0)');
		});
	}
	
	function createRematchingModal(feature){
		standardModal.createModalCallback('Rematch research variable : ' + feature.Name, function(modal){
			var divControlPanel = $('<div />').addClass('row-fluid');
			var selectTag = $('<select />');
			var rematchedSelectedDataSetIds = [];
			$.each(biobankDataSets, function(index, dataSet){
				selectTag.append('<option value="' + molgenis.hrefToId(dataSet.href) + '">' + dataSet.Name + '</option>');
				rematchedSelectedDataSetIds.push(molgenis.hrefToId(dataSet.href));
			});
			var selectButton = $('<button type="btn" class="btn btn-info">Select</button>');
			var selectAllButton = $('<button type="btn" class="btn btn-primary">Select all</button>');
			var removeAllButton = $('<button type="btn" class="btn btn">Remove all</button>');
			var matchButton = $('<button type="btn" class="btn btn-primary">Rematch</button>');
			
			$('<div />').addClass('offset1 span3').append(selectTag).appendTo(divControlPanel);
			$('<div />').addClass('offset1 span5 btn-group').append(selectButton).append(selectAllButton).append(removeAllButton).appendTo(divControlPanel);
			
			var infoContainer = $('<div />').addClass('row-fluid');
			var dataSetsContainer = $('<div />').addClass('offset1 span10 well').appendTo(infoContainer);
			$('<div />').addClass('span12').append('<legend class="legend-small">Selected catalogues : </legend>').appendTo(dataSetsContainer);
			
			modal.find('div.modal-body:eq(0)').append(divControlPanel).append(infoContainer);
			modal.find('div.modal-footer:eq(0)').prepend(matchButton);
			modal.modal('show');
			
			//Initialize all the button events
			selectButton.click(function(){
				var selectedDataSetId = selectTag.val();
				if(selectedDataSetId !== null && selectedDataSetId !== undefined){
					if($.inArray(selectedDataSetId, rematchedSelectedDataSetIds) === -1){
						rematchedSelectedDataSetIds.push(selectedDataSetId);
						updateSelectedDataSets(selectedDataSetId, $(selectTag));
						listAllDataSets(dataSetsContainer, rematchedSelectedDataSetIds)
					}
				}
			})
			
			selectAllButton.click(function(){
				$(selectTag).find('option').each(function(){
					if($.inArray($(this).val(), rematchedSelectedDataSetIds) === -1){
						rematchedSelectedDataSetIds.push($(this).val());
					}
				});
				listAllDataSets(dataSetsContainer, rematchedSelectedDataSetIds)
			});
			
			removeAllButton.click(function(){
				rematchedSelectedDataSetIds = [];
				listAllDataSets(dataSetsContainer, rematchedSelectedDataSetIds)
			});
			
			matchButton.click(function(){
				removeSelectedMappings(molgenis.hrefToId(feature.href), rematchedSelectedDataSetIds, function(data){
					molgenis.getFeatureFromIndex(feature, function(featureFromIndex){
						modal.modal('hide');
						var storedRowInfo = $('body').data('clickedRow');
						var existingRow = storedRowInfo[molgenis.hrefToId(feature.href)];
						updateExistingRow(featureFromIndex.columnValueMap, existingRow, rematchedSelectedDataSetIds);
					});
				});
			});
			
			selectAllButton.click();
		});
		
		function listAllDataSets(dataSetsContainer, rematchedSelectedDataSetIds){
			dataSetsContainer.find('div:gt(0)').remove();
			$.each(rematchedSelectedDataSetIds, function(index, dataSetId){
				var dataSet = restApi.get('/api/v1/dataset/' + dataSetId);
				var newRow = $('<div />').addClass('row-fluid');
				$('<div />').addClass('offset2 span3').append(dataSet.Name).appendTo(newRow);
				var removeButton = $('<button type="btn" class="btn btn-link">Remove</button>');
				$('<div />').addClass('offset3 span2').append(removeButton).appendTo(newRow);
				dataSetsContainer.append(newRow);
				removeButton.click(function(){
					rematchedSelectedDataSetIds.splice($.inArray(dataSetId, rematchedSelectedDataSetIds), 1);
					newRow.remove();
				});
			});
		}
		
		function updateSelectedDataSets(targetDataSetId, rematchedSelectedDataSetIds){
			var index = 0;
			var options = $(rematchedSelectedDataSetIds).find('option');
			options.attr('selected', false).each(function(){
				if(targetDataSetId !== $(this).val()){
					index++;
				}else return false;
			});
			index = index === options.length - 1 ? 0 : index + 1;
			$(options[index]).attr('selected', true);
		}
	}
	
	function compareTwoArrays(selectedFeatureMap, mappedFeatureNames){
		var selectedFeatureNames = Object.keys(selectedFeatureMap);
		if(selectedFeatureNames.length !== mappedFeatureNames.length) {
			return false;
		}
		for(var i = 0; i < selectedFeatureNames.length; i++){
			if($.inArray(selectedFeatureNames[i], mappedFeatureNames) === -1){
				return false;
			}
		}
		return true;
	}
	
	molgenis.MappingManager.prototype.downloadMappings = function(){
		var mappedDataSetIds = [];
		$.each(biobankDataSets, function(index, dataSet){
			mappedDataSetIds.push(molgenis.hrefToId(dataSet.href));
		});
		var deleteRequest = {
			'dataSetId' : molgenis.hrefToId(selectedDataSet.href),
			'matchedDataSetIds' : mappedDataSetIds,
			'documentType' : selectedDataSet.Identifier
		};
		console.log(deleteRequest);
		$.download(molgenis.getContextUrl() + '/download',{request : JSON.stringify(deleteRequest)});
	};
	
	molgenis.MappingManager.prototype.createHelpModal = function(){
		var container = $('<div />');
		$('<div />').append('<i class="icon-ok"></i><span class="float-right text-success">Mappings have been selected</span>').appendTo(container);
		$('<div />').append('<i class="icon-pencil"></i><span class="float-right text-info">Select the mappings</span>').appendTo(container);
		$('<div />').append('<i class="icon-trash"></i><span class="float-right text-warning">Delete all mappings</span>').appendTo(container);
		$('<div />').append('<i class="icon-ban-circle"></i><span class="float-right text-error">No candidate available</span>').appendTo(container);
		standardModal.createModalCallback('Icon meanings', function(modal){
			modal.find('.modal-body:eq(0)').append(container);
			modal.modal('show').css({
				'width' : '50%',
				'margin-left' : '-25%',
				'top' : '30%'
			});
		});
	};
	
	molgenis.MappingManager.prototype.getAllMappedDataSetIds = function (userName, selectedDataSetId){
		var mappings = restApi.get('/api/v1/dataset/', {
			'q' : {
				'q' : [{
					'field' : 'Identifier',
					'operator' : 'LIKE',
					'value' : userName + '-' + selectedDataSetId
				}]
			},
			'expand' : ['ProtocolUsed']
		}).items;
		var mappedDataSetIds = [];
		$.each(mappings, function(index, dataSet){
			if(dataSet.ProtocolUsed.Identifier === 'store_mapping'){
				var identifier = dataSet.Identifier;
				var dataSetIdArray = identifier.split('-');
				mappedDataSetIds.push(dataSetIdArray[dataSetIdArray.length - 1]);
			}
		});
		return mappedDataSetIds;
	};
	
	function remove(object, array){
		var newArray = [];
		$.each(array, function(index, element){
			if(element !== object){
				newArray.push(element);
			}
		});
		return newArray;;
	}
	
	function setUserName(name){
		userName = name;
	}
	
	function getUserName(){
		return userName;
	}
	
	function createDataSetIdentifier(targetDataSet, sourceDataSet){
		return getUserName() + '-' + molgenis.hrefToId(targetDataSet.href) + '-' + molgenis.hrefToId(sourceDataSet.href);
	}
}($, window.top.molgenis = window.top.molgenis || {}, window.top));