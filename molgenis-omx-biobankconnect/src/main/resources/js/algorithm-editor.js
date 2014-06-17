(function($, molgenis) {
	var standardModal = new molgenis.StandardModal();
	var restApi = new molgenis.RestClient();
	var selectedDataSet = null;
	var userName = null;
	var biobankDataSets = null;
	var storeMappingFeature = 'store_mapping_feature';
	var storeMappingMappedFeature = 'store_mapping_mapped_feature';
	var mappingScript = 'store_mapping_algorithm_script';
	var observationSet = 'observationsetid';
	
	molgenis.AlgorithmEditor = function AlgorithmEditor(){};
	
	molgenis.AlgorithmEditor.prototype.changeDataSet = function(userName, selectedDataSetId, dataSetIds){
		if(selectedDataSetId !== '' && dataSetIds.length > 0){
			setUserName(userName); 
			selectedDataSet = restApi.get('/api/v1/dataset/' + selectedDataSetId, {'expand' : ['protocolUsed']});
			biobankDataSets = restApi.get('/api/v1/dataset/', {
				'q' : {
					'q' : [{
						'field' : 'id',
						'operator' : 'IN',
						'value' : dataSetIds
					}]
				},
				'expand' : ['protocolUsed']
			}).items;
			
			var involvedDataSetNames = [];
			involvedDataSetNames.push(selectedDataSet.Name);
			$.each(biobankDataSets, function(index, dataSet){
				involvedDataSetNames.push(dataSet.Name);
			});
			$('#dataitem-number').empty().append(molgenis.getTotalNumberOfItems(selectedDataSetId));
			updateMatrix({'tableHeaders' : involvedDataSetNames});
			initSearchDataItems();
		}else{
			$('#dataitem-number').empty().append('Nothing selected');
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
	
	molgenis.AlgorithmEditor.prototype.createTableForRetrievedMappings = function(searchHits, parentDiv, style){
		if(searchHits.length === 0) {
			$('<div />').append('No mappings were found!').appendTo(parentDiv);
			return;
		}
		var tableForSuggestedMappings = $('<table />').addClass('table table-bordered'); 
		var header = $('<thead><tr><th>Name</th><th>Description</th><th>Data type</th><th>Unit</th>/tr></thead>');
		tableForSuggestedMappings.append(header);
		$.each(searchHits, function(index, hit){
			var row = $('<tr />');
			var featureId = hit.columnValueMap.id;
			var featureEntity = restApi.get('/api/v1/observablefeature/' + featureId, {'expand' : ['unit']});
			row.append('<td>' + featureEntity.Name + '</td>');
			row.append('<td>' + featureEntity.description + '</td>');
			if(featureEntity.unit !== undefined && featureEntity.unit !== null){
				row.append('<td>' + featureEntity.unit.Name + '</td>');
			}else{
				row.append('<td />');
			}
			row.append('<td>' + featureEntity.dataType + '</td>');
			tableForSuggestedMappings.append(row);
			row.css('cursor', 'pointer').click(function(){
				retrieveAllInfoForFeature(row, featureEntity);
			});
		});
		if(style == undefined || style == null){
			tableForSuggestedMappings.appendTo(parentDiv);
		}else{
			$('<div />').append(tableForSuggestedMappings).addClass('well').css(style).appendTo(parentDiv);
		}
		
		function retrieveAllInfoForFeature(clickedRow, featureEntity){
			var detailInfoTable = $('<table class="table table-bordered"></table>');
			detailInfoTable.append('<tr><th>Id</th><td>' + molgenis.hrefToId(featureEntity.href) + '</td></tr>');
			detailInfoTable.append('<tr><th>Name</th><td>' + featureEntity.Name + '</td></tr>');
			if(featureEntity.unit !== undefined && featureEntity.unit !== null){
				detailInfoTable.append('<tr><th>Unit</th><td>' + featureEntity.unit.Name + '</td></tr>');
			}
			detailInfoTable.append('<tr><th>Data type</th><td>' + featureEntity.dataType + '</td></tr>');
			detailInfoTable.append('<tr><th>Description</th><td>' + featureEntity.description + '</td></tr>');
			var categories = getCategoriesByFeatureIdentifier(featureEntity.Identifier);
			if(categories.length > 0){
				var categoryDiv = $('<div />');
				$.each(categories, function(index, category){
					categoryDiv.append('<div>' + category.valueCode + ' = ' + category.Name + '</div>');
				});
				detailInfoTable.append('<tr><th>Categories</th><td>' + categoryDiv.html() + '</td></tr>');
			}
			var parentTable = clickedRow.parents('table:eq(0)');
			var backButton = $('<button class="btn btn-primary">Go back</button>');
			parentTable.hide().before(detailInfoTable).before(backButton);
			backButton.click(function(){
				detailInfoTable.remove();
				backButton.remove();
				parentTable.show();
			});
			detailInfoTable.click(function(){
				backButton.click();
			});
		}
	};
	
	function updateMatrix(options){
		var default_options = {
			'dataSetId' : molgenis.hrefToId(selectedDataSet.href),
			'tableHeaders' : ['Name', 'Description'],
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
	
	function createTableRow(featureFromIndex){
		var feature = restApi.get('/api/v1/observablefeature/' + featureFromIndex.id);
		var row = $('<tr />');
		var description = '<strong>' + feature.Name + '</strong> : ' + molgenis.i18nDescription(feature).en;
		var isPopOver = description.length < 90;
		var popover = $('<span />').html(isPopOver ? description : description.substring(0, 90) + ' ...');
		if(!isPopOver){
			popover.addClass('show-popover').popover({
				content : molgenis.i18nDescription(feature).en,
				trigger : 'hover',
				placement : 'bottom'
			});
		}
		$('<td />').addClass('show-popover').css('width', '20%').append(popover).appendTo(row);
		
		$.each(biobankDataSets, function(index, mappedDataSet){
			var mapping = {};
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/getmapping',
				async : false,
				data : JSON.stringify({'dataSetIdentifier' : createDataSetIdentifier(selectedDataSet, mappedDataSet) , 'featureIds' : [molgenis.hrefToId(feature.href)]}),
				contentType : 'application/json',
				success : function(data, textStatus, request){
					if(data.searchHits.length > 0){
						var columnValueMap = data.searchHits[0].columnValueMap;
						mapping = {
							mappedFeatureId : columnValueMap[storeMappingFeature],
							mappingScript : columnValueMap[mappingScript],
							observationSet : columnValueMap[observationSet],
							confirmed : columnValueMap.confirmed,
							documentId : columnValueMap.id
						};
					}
					if(!mapping.mappingScript) mapping.mappingScript = '';
				}
			});
			var newCell = $('<td />').css('cursor','pointer').append(createCellInMappingPanel(mapping.mappingScript)).click(function(){
				standardModal.createModalCallback('Algorithm editor', function(modal){
					$(document).data('clickedCell', newCell).data('mapping', mapping);
					createAlgorithmEditorContent(feature, mappedDataSet, modal);
					modal.attr('data-backdrop', true).css({
						'width' : '90%',
						'left' : '5%',
						'top' : ($(document) - $(this).height())/2,
						'margin-left' : 0,
						'margin-top' : 0
					}).modal('show');
				});
			});
			row.append(newCell);
		});
		return row;
	}
	
	function createAlgorithmEditorContent(feature, mappedDataSet, modal){
		var searchRequest = {
			featureId : molgenis.hrefToId(feature.href),
			sourceDataSetId : molgenis.hrefToId(selectedDataSet.href),
			selectedDataSetIds : [molgenis.hrefToId(mappedDataSet.href)]
		};
		$(document).data('searchRequest', searchRequest);
		var tableDiv = $('<div class="span6"></div>');
		var metaInfoDiv = $('<div class="span6"></div>');
		var body = modal.find('.modal-body:eq(0)').css('max-height','100%');
		var featureInfoDiv = $('<div class="row-fluid"></div>').after('</br>').appendTo(body);
		$('<div class="row-fluid"></div>').append(metaInfoDiv).append(tableDiv).appendTo(body);
		var controlDiv = $('<div class="row-fluid"></div>').appendTo(body);
		
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/createmapping',
			async : false,
			data : JSON.stringify(searchRequest),
			contentType : 'application/json',
			success : function(data, textStatus, request) {	
				createFeatureInfoPanel(feature, featureInfoDiv);
				$('<div />').addClass('row-fuild').css('margin-bottom', '10px').append('<strong>' + mappedDataSet.Name + '</strong>').appendTo(tableDiv);
				var style= {
					'overflow-y' : 'scroll',
					'height' : $(document).height()/4
				};
				molgenis.AlgorithmEditor.prototype.createTableForRetrievedMappings(data.searchHits, tableDiv, style);
				$('<div />').addClass('row-fuild').css('margin-bottom', '10px').append('<strong>' + selectedDataSet.Name + '</strong>').appendTo(metaInfoDiv);
				var editor = createEditorInModel(metaInfoDiv, mappedDataSet);
				$('<div />').addClass('row-fuild').css('margin-bottom', '10px').before(controlDiv);
				addButtonsToControl(controlDiv, editor);
			},
			error : function(request, textStatus, error){
				console.log(error);
			}
		});
		
		function createFeatureInfoPanel(feature, parentDiv){
			var infoDiv = $('<div />').addClass('span3');
			$('<div />').append('<span class="info"><strong>Data item : </strong></span>').append('<span>' + feature.Name + '</span>').appendTo(infoDiv);
			if(feature.unit !== undefined && feature.unit !== null){
				$('<div />').append('<span class="info"><strong>Unit : </strong></span>').append('<span>' + feature.unit.Name + '</span>').appendTo(infoDiv);
			}
			$('<div />').append('<span class="info"><strong>Data type : </strong></span>').append('<span>' + feature.dataType + '</span>').appendTo(infoDiv);
			$('<div />').append('<span class="info"><strong>Description : </strong></span>').append('<span>' + molgenis.i18nDescription(feature).en + '</span>').appendTo(infoDiv);
			var middleDiv = $('<div />').addClass('span9');
			var categories = getCategoriesByFeatureIdentifier(feature.Identifier);
			if(categories.length > 0){
				var categoryDiv = $('<div />').addClass('span8').css('margin-left', '30px');
				$.each(categories, function(index, category){
					categoryDiv.append('<div>' + category.valueCode + ' = ' + category.Name + '</div>');
				});
				$('<div />').addClass('row-fluid').append('<div class="span1"><strong>Categories: </strong></div>').append(categoryDiv).appendTo(middleDiv);
			}
			parentDiv.append(infoDiv).append(middleDiv);
		}
		
		function createEditorInModel(parentDiv, mappedDataSet){
			var algorithmEditorDiv = $('<div id="algorithmEditorDiv"></div>');
			algorithmEditorDiv.addClass('well').css('height', $(document).height()/4).appendTo(parentDiv);
			var langTools = ace.require("ace/ext/language_tools");
			var editor = ace.edit('algorithmEditorDiv');
			editor.setOptions({
			    enableBasicAutocompletion: true
			});
			var script = null;
			if($(document).data('previousScript')){
				script = $(document).data('previousScript');
				$(document).removeData('previousScript');
			}
			if(script === null) {
				script = $(document).data('mapping').mappingScript	
			}
			editor.setValue(script);
			editor.setTheme("ace/theme/chrome");
			editor.getSession().setMode("ace/mode/javascript");
			var algorithmEditorCompleter = {
		        getCompletions: function(editor, session, pos, prefix, callback) {
		            if (prefix.length === 0) { callback(null, []); return }
		            molgenis.dataItemsTypeahead(molgenis.hrefToId(mappedDataSet.href), prefix, function(results){
		            	callback(null, $.each(results, (function(index, featureName) {
		            		var map = $(document).data('dataMap')[featureName];
	                        return {'name' : '$(\'' + map.name + '\')', 'value' : '$(\'' + map.name + '\')', 'score' : map.score, 'meta': mappedDataSet.name};
	                    })));
		            }, true);
		        }
		    }
		    langTools.addCompleter(algorithmEditorCompleter);
			return editor;
		}
		
		function addButtonsToControl(parentDiv, editor){
			var testStatisticsButton = $('<button class="btn btn-info">Test</button>');
			var suggestScriptButtion = $('<button class="btn btn-primary">Suggestion</button>');
			var saveScriptButton = $('<button class="btn btn-success">Save script</button>').css('float','right');
			$('<div class="span7"></div>').append(testStatisticsButton).append(' ').append(suggestScriptButtion).append(' ').append(saveScriptButton).appendTo(parentDiv);
			testStatisticsButton.click(function(){
				console.log('The testStatisticsButton button has been clicked!');
				var modalBody = parentDiv.parents('.modal-body:eq(0)');
				var ontologyMatcherRequest = $.extend($(document).data('searchRequest'), {
					'algorithmScript' : editor.getValue()
				});
				$.ajax({
					type : 'POST',
					url : molgenis.getContextUrl() + '/testscript',
					async : false,
					data : JSON.stringify(ontologyMatcherRequest),
					contentType : 'application/json',
					success : function(data, textStatus, request){	
						if(data.results.length === 0) return;
						var graphDivId = 'featureId-' + ontologyMatcherRequest['featureId'];
						var statisticsDivHeight = modalBody.height()/4*3;
						modalBody.data('originalContent', modalBody.children());
						modalBody.children().remove();
						var backButton = $('<button />').addClass('btn btn-primary').append('Go back');
						var featureObject = restApi.get('/api/v1/observablefeature/' + ontologyMatcherRequest['featureId']);
						var dataSetObject = restApi.get('/api/v1/dataset/' + ontologyMatcherRequest['selectedDataSetIds'][0]);
						var algorithmDiv = $('<div />').addClass('offset3 span6 well text-align-center').append('Test for variable <strong>' + featureObject.Name + '</strong> in dataset <strong>' + dataSetObject.Name + '</strong>');
						var tableDiv = $('<div />').addClass('span6 well').css('min-height', statisticsDivHeight).append('<div class="legend-align-center">Summary statistics</div>').append(statisticsTable(data));
						var graphDiv = $('<div />').attr('id', graphDivId).addClass('span6 well').css('min-height', statisticsDivHeight).append('<div class="legend-align-center">Distribution plot</div>').bcgraph(data.results);
						$('<div />').addClass('row-fluid').append(algorithmDiv).appendTo(modalBody);
						$('<div />').addClass('row-fluid').append(tableDiv).append(graphDiv).appendTo(modalBody);
						modalBody.next('div:eq(0)').prepend(backButton);
						backButton.click(function(){
							$(document).data('previousScript',editor.getValue());
							$(document).data('clickedCell').click();
						});
					},
					error : function(request, textStatus, error){
						console.log(error);
					}
				});
			});
			suggestScriptButtion.click(function(){
				console.log('The suggestScript button has been clicked!');
				$.ajax({
					type : 'POST',
					url : molgenis.getContextUrl() + '/suggestscript',
					async : false,
					data : JSON.stringify($(document).data('searchRequest')),
					contentType : 'application/json',
					success : function(data, textStatus, request) {	
						if(data.suggestedScript){
							editor.setValue(data.suggestedScript);
						}
					},
					error : function(request, textStatus, error){
						console.log(error);
					}
				});
			});
			saveScriptButton.click(function(){
				console.log('The saveScriptButton button has been clicked!' + editor.getValue());
				var modalBody = parentDiv.parents('.modal-body:eq(0)');
				var ontologyMatcherRequest = $.extend($(document).data('searchRequest'), {
					'algorithmScript' : editor.getValue()
				});
				$.ajax({
					type : 'POST',
					url : molgenis.getContextUrl() + '/savescript',
					async : false,
					data : JSON.stringify(ontologyMatcherRequest),
					contentType : 'application/json',
					success : function(data, textStatus, request) {	
						console.log(data);
						var alerts = [];
						alerts.push(data);
						modalBody.find('.alert').remove();
						molgenis.createAlert(alerts, 'success', modalBody);
						$(document).data('clickedCell').empty().append(createCellInMappingPanel(editor.getValue()));
						$(document).data('mapping').mappingScript = editor.getValue();
					},
					error : function(request, textStatus, error){
						console.log(error);
					}
				});
			});
		}
		
		function statisticsTable(data){
			var array = data.results;
			var table = $('<table />').addClass('table table-bordered');
			table.append('<tr><th>Total cases</th><td>' + data.totalCounts + '</td></tr>');
			table.append('<tr><th>Valid cases</th><td>' + array.length + '</td></tr>');
			table.append('<tr><th>Mean</th><td>' + jStat.mean(array) + '</td></tr>');
			table.append('<tr><th>Median</th><td>' + jStat.median(array) + '</td></tr>');
			table.append('<tr><th>Standard Deviation</th><td>' + jStat.stdev(array) + '</td></tr>');
			return table;
		}
	}
	
	function createCellInMappingPanel(mappingScript) {
		var editIcon = $('<i />').addClass('show-popover ' + (mappingScript === '' ? 'icon-pencil' : 'icon-ok'));
		var iconHolderDiv = $('<div />').css({
			'float' : 'right',
			'margin-right' : '20%'
		}).append(editIcon);
		var algorithmStatusDiv = $('<div />').css({
			'float' : 'left',
			'margin-left' : '30%'
		});
		if(mappingScript === '') algorithmStatusDiv.append('<strong>Edit algorithm</strong>');
		else algorithmStatusDiv.append('<strong>Complete</strong>').css('color','#04B4AE');
		return $('<div />').addClass('row-fluid').append(algorithmStatusDiv).append(iconHolderDiv);
	}

	function getCategoriesByFeatureIdentifier(featureIdentifier){
		var categories = restApi.get('/api/v1/category/', {
			'q' : { 
				'q' : [{
					'field' : 'observableFeature',
					'operator' : 'EQUALS',
					'value' : featureIdentifier
				}]
			}
		});
		return categories.items; 
	}
	
	function createDataSetIdentifier(targetDataSet, sourceDataSet){
		return getUserName() + '-' + molgenis.hrefToId(targetDataSet.href) + '-' + molgenis.hrefToId(sourceDataSet.href);
	}
	
	function setUserName(name){
		userName = name;
	}
	
	function getUserName(){
		return userName;
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));