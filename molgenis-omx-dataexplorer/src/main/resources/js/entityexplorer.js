(function($, w) {
	"use strict";

	var ns = w.molgenis = w.molgenis || {};

	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();

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
						items.push('<a class="accordion-toggle" data-toggle="collapse" href="#collapse-' + protocol.identifier + '">');
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
					    items.push('<div class="span9">');
					    
						// build protocol result table
						var features = $.merge(matchedFeatures, remainingFeatures);
						items.push('<table class="table table-protocol">');
						items.push('<tbody>');
						$.each(features, function(key, feature) {
							items.push('<tr>');
							items.push('<td class="first">' + feature.name + '</td>');
							$.each(searchHits, function(key, searchHit) {
								items.push('<td>' + searchHit.columnValueMap[feature.identifier] + '</td>');
							});
							items.push('</tr>');
						});
						items.push('<tr><td class="first"></td>');
						$.each(searchHits, function(key, searchHit) {
							items.push('<td><a href="/plugin/dataexplorer?dataset=' + searchHit.documentType + '" target="_blank">View data set</a></td>');
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

		var selected = $('#entity-instance-select').val();
		if (selected != null)
			$('#entity-instance-select').change();
	});
}($, window.top));
