(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();
	
	molgenis.setDataExplorerUrl = function(dataExplorerUrl) {
		molgenis.dataExplorerUrl = dataExplorerUrl;
	};
	
	molgenis.onEntityChange = function(name) {
		restApi.getAsync('/api/v1/' + name, null, function(entities) {
			var items = [];
			// TODO deal with multiple entity pages
			$.each(entities.items, function(key, val) {
				items.push('<option value="' + val.href + '">' + val.Name + '</option>');
			});
			$('#entity-instance-select').html(items.join(''));

			// select first option
			$('#entity-instance-select').val($("#entity-instance-select option:first").val());
			$('#entity-instance-select').change();
		});
	};

	molgenis.onEntitySelectionChange = function(entityUrl) {
		restApi.getAsync(entityUrl, null, function(entity) {
			molgenis.updateEntityTable(entity);
			molgenis.updateEntitySearchResults(entity);
		});
	};

	molgenis.updateEntityTable = function(entity) {
		var name = typeof entity.Name !== 'undefined' ? entity.Name : 'N/A';
		var identifier = typeof entity.Identifier !== 'undefined' ? entity.Identifier : 'N/A';
		var description = typeof entity.description !== 'undefined' ? entity.description : 'N/A';

		var items = [];
		items.push('<tbody>');
		items.push('<tr><td class="first">Name</td><td>' + name + '</td></tr>');
		items.push('<tr><td class="first">Identifier</td><td>' + identifier + '</td></tr>');
		items.push('<tr><td class="first">Description</td><td>' + description + '</td></tr>');
		items.push('</tbody>');
		$('#entity-table').html(items.join(''));
	};

	molgenis.updateEntitySearchResults = function(entity) {
		
		molgenis.getAllRelatedEntities(entity, function(searchResponse) {
			if (searchResponse.totalHitCount == 0) {
				$('#entity-search-results-header').html('No search results');
				$('#entity-search-results').empty();
				return;
			}
			
			// sort search results by matching feature
			var featuresMap = {};
			$.each(searchResponse.searchHits, function(key, searchHit) {
				$.each(searchHit.columnValueMap, function(key, val) {
					if(key.indexOf('key-') === 0) {
						// find matching features in xrefs/mrefs using entity identifier
						var match = false;
						var featureIdentifier = key.substring(4);
						if($.isArray(val)) {
							$.each(val, function(key, val) {
								if(val === entity.Identifier) {
									match = true;
									return false;
								}
							});
						} else if(val === entity.Identifier) {
							match = true;
						}
						
						if(match) {
							var resultList = featuresMap[featureIdentifier];
							if(!resultList) {
								resultList = [];
								featuresMap[featureIdentifier] = resultList;
							}
							resultList.push(searchHit);
						}
					}
				});
			});
            //create a map of protocols with the datasets they are used in
            var protocolsMap = {};
            var datasets = restApi.get('/api/v1/dataset');
            $.each(datasets.items, function(key, dataset) {
                var protocolUsed = dataset.ProtocolUsed.href;
                protocolsMap = getSubProtocols(dataset.Identifier, protocolUsed, protocolsMap);
            });

            function getSubProtocols (datasetIdentifier, rootProtocolUri, protocolsMap){
                var rootProtocol = restApi.get(rootProtocolUri, {'expand' : ['subprotocols']});
                //check if the protocol was already found in another dataset
                //add dataset to list of datasets in which the protocol occurs
                var datasetIdentifiers = protocolsMap[rootProtocol.Identifier];
                if(!datasetIdentifiers) {
                    datasetIdentifiers = [];
                }
                datasetIdentifiers.push(datasetIdentifier);
                protocolsMap[rootProtocol.Identifier] = datasetIdentifiers;
                if(rootProtocol.subprotocols.items.length>0){
                    $.each(rootProtocol.subprotocols.items, function(key, protocol) {
                         protocolsMap = getSubProtocols(datasetIdentifier, protocol.href, protocolsMap);
                    });
                }
                return protocolsMap;
            }

			// get all protocol features
            restApi.getAsync('/api/v1/protocol', {'expand': ['features']}, function(protocols) {
                var items = [];
                items.push('<div class="accordion" id="accordion">');

                var nrProtocols = 0;
                var firstProtocol = true;
                $.each(protocols.items, function(key, protocol) {
                    var datasets = protocolsMap[protocol.Identifier];
                   	// determine features that reference the given entity
					var matchedFeatures = [];
					var remainingFeatures = [];
					$.each(protocol.Features.items, function(key, feature) {
						if(featuresMap[feature.Identifier])
							matchedFeatures.push(feature);
						else
							remainingFeatures.push(feature);
					});
					if(matchedFeatures.length > 0) {
						++nrProtocols;
						
						// order searchHits 
						var searchHits = [];
						$.each(matchedFeatures, function(key, feature) {
							if(featuresMap[feature.Identifier]) {
								$.each(featuresMap[feature.Identifier], function(key, searchHit) {
									searchHits.push(searchHit);
								});
							}
						});
						
						items.push('<div class="accordion-group">');
						items.push('<div class="accordion-heading">');
						items.push('<a class="accordion-toggle" data-toggle="collapse" href="#collapse-' + protocol.Identifier + '"><i class="icon-chevron-');
						if(firstProtocol) {
							items.push('down');
						} else {
							items.push('right');
						}
						items.push('"></i> ');
					    items.push(protocol.Name);
					    items.push('</a>');
					    items.push('</div>');
					    items.push('<div id="collapse-' + protocol.Identifier + '" class="accordion-body collapse');
					    if(firstProtocol) {
					    	items.push(' in');
					    	firstProtocol = false;
					    }
					    items.push('">');
					    items.push('<div class="accordion-inner">');
					    items.push('<div class="container-fluid">');
					    items.push('<div class="row-fluid">');
					    items.push('<div class="span3">');
					    items.push('<h3>Protocol summary</h3>');
					    items.push('<p>' + (protocol.description || 'N/A') + '</p>');
					    items.push('</div>');
					    items.push('<div id="table-protocol-container" class="span9">');
					    
						// build protocol result table
						var features = $.merge(matchedFeatures, remainingFeatures);
						items.push('<table class="table table-protocol">');
						items.push('<tbody>');
						$.each(features, function(key, feature) {
							items.push('<tr>');
							items.push('<td class="first">' + feature.Name + '</td>');
							$.each(searchHits, function(key, searchHit) {
                                //only include data that was found in a dataset where the current protocol is part of
								if(datasets.indexOf(searchHit.columnValueMap['partOfDataset'])!=-1){
                                    if(searchHit.columnValueMap[feature.Identifier]){
                                        items.push('<td>' + formatTableCellValue(searchHit.columnValueMap[feature.Identifier],feature.dataType) + '</td>');
                                    }
                                    else{
                                        items.push('<td/>');
                                    }
                                }
							});
							items.push('</tr>');
						});
						items.push('<tr><td class="first"></td>');
						$.each(searchHits, function(key, searchHit) {
                            //only include data that was found in a dataset where the current protocol is part of
                            if(datasets.indexOf(searchHit.columnValueMap['partOfDataset'])!=-1){
                                if(typeof molgenis.dataExplorerUrl !== 'undefined'){
                                    items.push('<td><a href="'+molgenis.dataExplorerUrl+'?dataset=' + searchHit.documentType + '">View data set</a></td>');
                                }
                            }
						});
						items.push('</tr>');
						items.push('</tbody>');
						items.push('</table>');
						
						items.push('</div>');
						items.push('</div>');
						items.push('</div>');
						
						items.push('</div>');
						items.push('</div>');
						items.push('</div>');
					}
				});
				items.push('</div>');
				
				$('#entity-search-results-header').html(searchResponse.totalHitCount + ' search results in ' + nrProtocols + ' protocols');
				$('#entity-search-results').html(items.join(''));
				$('.show-popover').popover({trigger:'hover', placement: 'bottom'});
			});
		});
	};
	
	molgenis.getAllRelatedEntities = function(entity, callback){
    	$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/entities',
			data : JSON.stringify(entity.Identifier),
			contentType : 'application/json',
			success : function(searchResponse) {
				callback(searchResponse);
			}
		});
    }

	// on document ready
	$(function() {
		$('#entity-select').change(function() {
			molgenis.onEntityChange($(this).val());
		});
		$('#entity-instance-select').change(function() {
			molgenis.onEntitySelectionChange($(this).val());
		});

		$(document).on('show', '#accordion .collapse', function() {
		    $(this).parent().find(".icon-chevron-right").removeClass("icon-chevron-right").addClass("icon-chevron-down");
		}).on('hide', '#accordion .collapse', function() {
		    $(this).parent().find(".icon-chevron-down").removeClass("icon-chevron-down").addClass("icon-chevron-right");
		});
		
		var selected = $('#entity-instance-select').val();
		if (selected != null)
			$('#entity-instance-select').change();
	});
}($, window.top.molgenis = window.top.molgenis || {}));