(function($, molgenis) {
	"use strict";

	var ns = molgenis;

	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	
	ns.setDataExplorerUrl = function(dataExplorerUrl) {
		ns.dataExplorerUrl = dataExplorerUrl;
	};
	
	ns.onEntityChange = function(name) {
		restApi.getAsync('/api/v1/' + name, null, null, function(entities) {
			var items = [];
			// TODO deal with multiple entity pages
			$.each(entities.items, function(key, val) {
				items.push('<option value="' + val.href + '">' + val.name + '</option>');
			});
			$('#entity-instance-select').html(items.join(''));

			// select first option
			$('#entity-instance-select').val($("#entity-instance-select option:first").val());
			$('#entity-instance-select').change();
		});
	};

	ns.onEntitySelectionChange = function(entityUrl) {
		restApi.getAsync(entityUrl, null, null, function(entity) {
			ns.updateEntityTable(entity);
			ns.updateEntitySearchResults(entity);
		});
	};

	ns.updateEntityTable = function(entity) {
		var name = typeof entity.name !== 'undefined' ? entity.name : 'N/A';
		var identifier = typeof entity.identifier !== 'undefined' ? entity.identifier : 'N/A';
		var description = typeof entity.description !== 'undefined' ? entity.description : 'N/A';

		var items = [];
		items.push('<tbody>');
		items.push('<tr><td class="first">Name</td><td>' + name + '</td></tr>');
		items.push('<tr><td class="first">Identifier</td><td>' + identifier + '</td></tr>');
		items.push('<tr><td class="first">Description</td><td>' + description + '</td></tr>');
		items.push('</tbody>');
		$('#entity-table').html(items.join(''));
	};

	ns.updateEntitySearchResults = function(entity) {
		searchApi.search(ns.createSearchRequest(entity), function(searchResponse) {
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
								if(val === entity.identifier) {
									match = true;
									return false;
								}
							});
						} else if(val === entity.identifier) {
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
			
			// get all protocol features
			restApi.getAsync('/api/v1/protocol', ['features'], null, function(protocols) {
				var items = [];
				items.push('<div class="accordion" id="accordion">');
				
				var nrProtocols = 0;
				var firstProtocol = true;
				$.each(protocols.items, function(key, protocol) {
					// determine features that reference the given entity
					var matchedFeatures = [];
					var remainingFeatures = [];
					$.each(protocol.features.items, function(key, feature) {
						if(featuresMap[feature.identifier])
							matchedFeatures.push(feature);
						else
							remainingFeatures.push(feature);
					});
					if(matchedFeatures.length > 0) {
						++nrProtocols;
						
						// order searchHits 
						var searchHits = [];
						$.each(matchedFeatures, function(key, feature) {
							if(featuresMap[feature.identifier]) {
								$.each(featuresMap[feature.identifier], function(key, searchHit) {
									searchHits.push(searchHit);
								});
							}
						});
						
						items.push('<div class="accordion-group">');
						items.push('<div class="accordion-heading">');
						items.push('<a class="accordion-toggle" data-toggle="collapse" href="#collapse-' + protocol.identifier + '"><i class="icon-chevron-');
						if(firstProtocol) {
							items.push('down');
						} else {
							items.push('right');
						}
						items.push('"></i> ');
					    items.push(protocol.name);
					    items.push('</a>');
					    items.push('</div>');
					    items.push('<div id="collapse-' + protocol.identifier + '" class="accordion-body collapse');
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
					    items.push('<p>' + protocol.description + '</p>');
					    items.push('</div>');
					    items.push('<div id="table-protocol-container" class="span9">');
					    
						// build protocol result table
						var features = $.merge(matchedFeatures, remainingFeatures);
						items.push('<table class="table table-protocol">');
						items.push('<tbody>');
						$.each(features, function(key, feature) {
							items.push('<tr>');
							items.push('<td class="first">' + feature.name + '</td>');
							$.each(searchHits, function(key, searchHit) {
								if(searchHit.columnValueMap[feature.identifier]){
									items.push('<td>' + formatTableCellValue(searchHit.columnValueMap[feature.identifier],feature.dataType) + '</td>');
								}
								else{
									items.push('<td/>');
								}
							});
							items.push('</tr>');
						});
						items.push('<tr><td class="first"></td>');
						$.each(searchHits, function(key, searchHit) {
							if(typeof ns.dataExplorerUrl !== 'undefined'){
								items.push('<td><a href="'+ns.dataExplorerUrl+'?dataset=' + searchHit.documentType + '" target="_blank">View data set</a></td>');
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

	ns.createSearchRequest = function(entity) {
		var searchRequest = {
			queryRules : [ {
				field : '_xrefvalue',
				operator : 'EQUALS',
				value : entity.identifier
			} ]
		};
		return searchRequest;
	};

	// on document ready
	$(function() {
		$('#entity-select').change(function() {
			ns.onEntityChange($(this).val());
		});
		$('#entity-instance-select').change(function() {
			ns.onEntitySelectionChange($(this).val());
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