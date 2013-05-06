(function($, w) {
	"use strict";

	var ns = w.molgenis = w.molgenis || {};

	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();

	ns.selectEntity = function(name) {
		restApi.getAsync('/api/v1/' + name, null, function(entities) {
			var items = [];
			// TODO deal with multiple entity pages
			$.each(entities.items, function(key, val) {
				items.push('<option value="' + val.href + '">' + val.name + '</option>');
			});
			$('#entity-instance-select').html(items.join(''));
			$('#entity-instance-select').change(function() {
				ns.onEntitySelectionChange($(this).val());
			});
			$('#entity-instance-select').change();
		});
	};

	ns.onEntitySelectionChange = function(entityUrl) {
		restApi.getAsync(entityUrl, null, function(entity) {
			ns.updateEntityTable(entity);
			ns.updateEntitySearchResults(entity);
		});
	};

	ns.updateEntityTable = function(entity) {
		var items = [];
		items.push('<thead><tr><th colspan="2">Entity details</th></tr></thead>');
		items.push('<tbody>');
		items.push('<tr><td>Name</td><td>' + entity.name + '</td></tr>');
		items.push('<tr><td>Identifier</td><td>' + entity.identifier + '</td></tr>');
		items.push('<tr><td>Description</td><td>' + entity.description + '</td></tr>');
		items.push('</tbody>');
		$('#entity-table').html(items.join(''));
	};

	ns.updateEntitySearchResults = function(entity) {
		searchApi.search(ns.createSearchRequest(entity), function(searchResponse) {
			var items = [];
			$.each(searchResponse.searchHits, function(key, val) {
				items.push('<li>' + JSON.stringify(val) + '</li>');
			});
			$('#entity-search-results').html(items.join(''));
		});
	};

	ns.createSearchRequest = function(entity) {
		var searchRequest = {
			// documentType : selectedDataSet.name,
			queryRules : [ {
				operator : 'EQUALS',
				value : entity.identifier
			} ]
		};
		return searchRequest;
	};

	// on document ready
	$(function() {
		$('#entity-select').change(function() {
			ns.selectEntity($(this).val());
		});
	});
}($, window.top));
