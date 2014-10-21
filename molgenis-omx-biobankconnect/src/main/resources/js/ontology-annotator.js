(function($, molgenis) {
	"use strict";
	
	var standardModal = new molgenis.StandardModal();
	var restApi = new molgenis.RestClient();
	var ontologyTermIRI = "ontologyTermIRI";
	var selectedDataSet = null;
	
	molgenis.OntologyAnnotator = function OntologyAnnotator(){};
	
	molgenis.OntologyAnnotator.prototype.changeDataSet = function(selectedDataSetId){
		if(selectedDataSetId !== undefined && selectedDataSetId !== null && selectedDataSetId !== ''){
			selectedDataSet = restApi.get('/api/v1/dataset/' + selectedDataSetId);
			$('#catalogue-name').empty().append(selectedDataSet.Name);
			$('#dataitem-number').empty().append(molgenis.getTotalNumberOfItems(selectedDataSetId));
			updateMatrix();
			initSearchDataItems();
		
		}else{
			$('#catalogue-name').empty().append('Nothing selected');
			$('#dataitem-number').empty().append('Nothing selected');
		}
		
		function initSearchDataItems () {
			var options = {'updatePager' : true};
			$('#search-dataitem').typeahead({
				source: function(query, process) {
					molgenis.dataItemsTypeahead(molgenis.hrefToId(selectedDataSet.href), query, process);
				},
				minLength : 3,
				items : 20
			});
			$('#search-button').click(function(){
				updateMatrix(options);
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
					updateMatrix(options);
			    }
			});
		};
	};
	
	function updateMatrix(options){
		var default_options = {
			'dataSetId' : molgenis.hrefToId(selectedDataSet.href),
			'tableHeaders' : ['Name', 'Description', 'Annotation'],
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
	
	function createTableRow(feature){	
		var row = $('<tr />').addClass('show-popover').data('feature', feature).click(function(){
			var featureId = $(this).data('feature').id;
			var restApiFeature = restApi.get('/api/v1/observablefeature/' + featureId, {'expand': ["unit", "definitions"]});
			molgenis.OntologyAnnotator.prototype.createFeatureTable('Annotate data item', restApiFeature, updateMatrix);
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
		var featureEntity = restApi.get('/api/v1/observablefeature/' + feature.id, {'expand': ["unit", "definitions"]});
		if(featureEntity.definitions.items !== 0){
			var moreToShow = featureEntity.definitions.items.length;
			$.each(featureEntity.definitions.items, function(index, ontologyTerm){
				var newAnnotationText = annotationText + ontologyTerm.Name + ' , ';
				if(newAnnotationText.length > 130) {
					annotationText = annotationText.substring(0, annotationText.length - 3) + ' , ';
				}else{
					moreToShow--;
					annotationText += ontologyTerm.Name + ' , ';
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
	
	molgenis.OntologyAnnotator.prototype.createFeatureTable = function(title, restApiFeature, callback){
		var modal = standardModal.createModal(title, [], {});
		var body = modal.find('div.modal-body:eq(0)').addClass('overflow-y-visible');
		body.append(createFeatureInfo(title, restApiFeature, callback));
		body.append(createSearchDiv(title, restApiFeature, callback));
		modal.css({
			'width' : '50%',
			'left' : '25%',
			'top' : '30%',
			'margin-left' : 0,
			'margin-top' : 0
		}).modal('show');
		return modal;
		
		function createFeatureInfo(title, restApiFeature, callback){
			var table = $('<table />').addClass('table table-bordered'); 
			var featureTableContainer = $('<div />');
			featureTableContainer.css({
					'max-height' : 350, 
					'overflow' : 'auto'
			}).append(table);
			
			featureTableContainer.scrollTop(featureTableContainer.height());
			$('<tr><th class="feature-detail-th">ID : </th><td class="feature-detail-td">' + molgenis.hrefToId(restApiFeature.href) + '</td></tr>').appendTo(table);
			$('<tr><th>Name : </th><td>' + restApiFeature.Name + '</td></tr>').appendTo(table);
			$('<tr><th>Description : </th><td>' + molgenis.i18nDescription(restApiFeature).en + '</td></tr>').appendTo(table);
			
			if(restApiFeature.definitions.items.length !== 0){
				molgenis.getFeatureFromIndex(restApiFeature, function(hit){
					var ontologyTermAnnotations = $('<ul />');
					var boostedOntologyTerms = hit.columnValueMap['boostOntologyTerms'] === '' ? [] : hit.columnValueMap['boostOntologyTerms'].split(',');
					$.each(restApiFeature.definitions.items, function(index, ontologyTerm){
						var removeIcon = $('<i class="icon-remove float-right"></i>').click(function(){
							updateAnnotation(restApiFeature, molgenis.hrefToId(ontologyTerm.href), false);
							restApi.getAsync(restApiFeature.href, {'expand': ["unit", "definitions"]}, function(updatedFeature){
								molgenis.OntologyAnnotator.prototype.createFeatureTable(title, updatedFeature, callback);
								if(callback !== undefined && callback !== null) callback(updatedFeature);
							});
						});
						var selectBoostIcon = $('<i class="icon-star-empty float-right"></i>').popover({
							content : 'Select as key concept and give more weight!',
							trigger : 'hover',
							placement : 'bottom'
						});
						if($.inArray(ontologyTerm.termAccession, boostedOntologyTerms) !== -1 ) selectBoostIcon.removeClass('icon-star-empty').addClass('icon-star');
						selectBoostIcon.click(function(){
							if($(this).hasClass('icon-star-empty')){
								updateIndex(hit, ontologyTerm, true);
								$(this).removeClass('icon-star-empty').addClass('icon-star');
							}
							else {
								updateIndex(hit, ontologyTerm, false);
								$(this).removeClass('icon-star').addClass('icon-star-empty');
							}
						});
						var ontologyTermLink = $('<a href="' + ontologyTerm.termAccession + '" target="_blank">' + ontologyTerm.Name + '</a>');
						$('<li />').append(ontologyTermLink).append(removeIcon).append(selectBoostIcon).appendTo(ontologyTermAnnotations);
					});
					$('<tr />').append('<th>Annotation : </th>').append($('<td />').append(ontologyTermAnnotations)).appendTo(table);
				});
			}else{
				table.append('<tr><th>Annotation : </th><td>Not available</td></tr>');
			}
			return table;
		}
	};
	
	function createSearchDiv(title, feature, callback){
		var searchDiv = $('<div class="row"></div>').css('z-index', 10000);
		var searchGroup = $('<div class="group-append col-md-4"></div>');
		var searchField = $('<input type="text" data-provide="typeahead" />');
		var addTermButton = $('<button class="btn btn-default" type="button">Add annotation</button>');
		searchField.appendTo(searchGroup);
		addTermButton.appendTo(searchGroup);
		searchField.typeahead({
			source: function(query, process) {
				molgenis.ontologyTermTypeahead('ontologyTermSynonym', query, process);
			},
			minLength : 3,
			items : 20
		});
		addTermButton.click(function(){
			var termFound = false;
			$.each($(document).data('dataMap'), function(key, value){
				termFound = true;
				return false;
			});
			if(termFound){
				checkOntologyTerm(searchField, feature);
				restApi.getAsync(feature.href, {'expand': ["unit", "definitions"]}, function(updatedFeature){
					molgenis.OntologyAnnotator.prototype.createFeatureTable(title, updatedFeature, callback);
					if(callback !== undefined && callback !== null) callback(updatedFeature);
				});
			}
		});
		return searchDiv.append(searchGroup);
	}
	
	molgenis.OntologyAnnotator.prototype.searchOntologies = function(ontologyDiv, hiddenInput){
		var indexedOntologies = restApi.get('/api/v1/ontologyindex/');
		if(ontologyDiv.find('input').length === 0){
			$.each(indexedOntologies.items, function(index, ontology){
				var ontologyUri = ontology.ontologyIRI;
				var ontologyName = ontology.ontologyName;
			    $('<label />').addClass('checkbox').append('<input type="checkbox" value="' + ontologyUri + '" checked>' + ontologyName).click(function(){
			    	$('#selectedOntologies').val(getAllOntologyUris(ontologyDiv));
			    }).appendTo(ontologyDiv);
			});
		}
		$('#selectedOntologies').val(getAllOntologyUris(ontologyDiv));
		
		function getAllOntologyUris(ontologyDiv){
			var selectedOntologies = [];
	    	$(ontologyDiv).find('input').each(function(){
	    		if($(this).attr("checked")){
	    			selectedOntologies.push($(this).val());
	    		}
	    	});
	    	return selectedOntologies;
		}
	};

	molgenis.OntologyAnnotator.prototype.annotateConfirmation = function(title, isAdd){
		standardModal.createModalCallback(title, function(modal){
			var message = isAdd ? 'Are you sure that you want to overwrite all pre-defined annotations?' : 'Are you sure that you want to remove all pre-defined annotations?';
			var confirmButton = $('<button type="btn" class="btn btn-primary">Confirm</button>').click(function(){
				$('#wizardForm').attr({
					'action' : molgenis.getContextUrl() + (isAdd ? '/annotate' : '/annotate/remove'),
					'method' : 'POST'
				}).submit();
			});
			modal.find('div.modal-body:eq(0)').append('<p style="font-size:16px"><strong>' + message + '</strong></p>');
			modal.find('div.modal-footer:eq(0)').prepend(confirmButton);
			modal.css({
				'margin-top' : 200
			}).modal('show');
		});
	};
	
	function updateIndex(hit, ontologyTerm, boost){
		var updateScript = null;
		var documentIds = [];
		var documentType = hit.documentType;
		documentIds.push(hit.id);
		var boostOntologyTerms = hit.columnValueMap['boostOntologyTerms'] === '' ? [] : hit.columnValueMap['boostOntologyTerms'].split(','); 
		if(boost){
			boostOntologyTerms.push(ontologyTerm.termAccession);
			updateScript = boostOntologyTerms.length === 1 ? boostOntologyTerms[0] : boostOntologyTerms.join(',');
		}else{
			var index = boostOntologyTerms.indexOf(ontologyTerm.termAccession);
			boostOntologyTerms.splice(index, 1);
			updateScript = boostOntologyTerms.join(','); 
		}
		var updateRequest = {
			'documentType' : documentType,
			'documentIds' : documentIds,
			'updateScript' : 'boostOntologyTerms="' + updateScript + '"'
		};
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/annotate/update',
			async : false,
			data : JSON.stringify(updateRequest),
			contentType : 'application/json',
		});
	}

	function checkOntologyTerm(searchField, feature){
		var dataMap = $(document).data('dataMap');
		var ontologyTerm = searchField.val();
		var toCreate = true;
		var toUpdate = false;
		if(dataMap && ontologyTerm !== '' && dataMap[ontologyTerm]){
			var ontologyTermFromIndex = dataMap[ontologyTerm];
			var ontology = createOntology(ontologyTermFromIndex);
			var queryRules = [];
			queryRules.push({
				field : 'Identifier',
				operator : 'EQUALS',
				value : ontology.Name + ':' + ontologyTermFromIndex.ontologyTermIRI
			});
			var request = {
				'q' : queryRules
			};
			var result = restApi.get('/api/v1/ontologyterm/', {'expand': ['ontology'], 'q' : request});
			var ontologyTermId = null;
			var ontologyTermRestApi = null;
			if(result.items.length !== 0) {
				toCreate = false;
				ontologyTermId = molgenis.hrefToId(result.items[0].href);
				ontologyTermRestApi = result.items[0];
				toUpdate = (result.items[0].Name !== ontologyTermFromIndex.ontologyTermSynonym);
			}
			if(toCreate) ontologyTermId = createOntologyTerm(dataMap[ontologyTerm]);
			if(toUpdate) updateOntologyTerm(ontologyTermRestApi, dataMap[ontologyTerm]);
			if(ontologyTermId != null) updateAnnotation(feature, ontologyTermId, true);
		}
	}

	function updateAnnotation(feature, ontologyTermId, add){
		var data = {};
		feature.description = molgenis.i18nDescription(feature).en;
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
	
	function updateOntologyTerm(ontologyTermRestApi, data){
		var query = {};
		$.map(ontologyTermRestApi, function(value, key){
			if(key === 'ontology') value = molgenis.hrefToId(value.href);
			query[key] = value;
		});
		query.Name = data.ontologyName + ':' + data.ontologyTermSynonym;
		query.definition = data.ontologyTermSynonym;
		$.ajax({
			type : 'PUT',
			dataType : 'json',
			url : ontologyTermRestApi.href,
			cache: true,
			data : JSON.stringify(query),
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
		query.Name =  data.ontologyName + ':' + data.ontologyTerm;
		query.Identifier = data.ontologyName + ':' + data.ontologyTermIRI;
		query.termAccession = data.ontologyTermIRI;
		query.description = data.ontologyTermLabel;
		query.ontology = molgenis.hrefToId(ontology.href);
		query.definition = data.ontologyTermSynonym;
		
		$.ajax({
			type : 'POST',
			dataType : 'json',
			url : '/api/v1/ontologyterm/',
			cache: true,
			data : JSON.stringify(query),
			contentType : 'application/json',
			async : false,
			success : function(data, textStatus, request) {
				var ontologyTerm = restApi.get('/api/v1/ontologyterm/', {
					'q':  {
						'q' : [{
							field : 'Identifier',
							operator : 'EQUALS',
							value : query.Identifier
						}]
					}
				});
				ontologyTermId = molgenis.hrefToId(ontologyTerm.items[0].href);
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
		var existingOntology = restApi.get('/api/v1/ontology/', {'q': query});
		if(existingOntology.items.length === 0){
			var ontologyData = {};
			ontologyData.Name = data.ontologyName;
			ontologyData.Identifier = data.ontologyIRI;
			ontologyData.ontologyURI = data.ontologyIRI;
			
			$.ajax({
				type : 'POST',
				dataType : 'json',
				url : '/api/v1/ontology/',
				cache: true,
				data : JSON.stringify(ontologyData),
				contentType : 'application/json',
				async : false,
				success : function(data, textStatus, request) {
					
				},
				error : function(request, textStatus, error){
					console.log(error);
				} 
			});
			existingOntology = restApi.get('/api/v1/ontology/', {'q': query});
		}
		return existingOntology.items[0];
	}
}($, window.top.molgenis = window.top.molgenis || {}));
