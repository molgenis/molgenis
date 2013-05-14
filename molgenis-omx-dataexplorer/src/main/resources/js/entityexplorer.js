(function($, w) {
	"use strict";

	var ns = w.molgenis = w.molgenis || {};

	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();

	ns.onEntityChange = function(name) {
		restApi.getAsync('/api/v1/' + name, null, function(entities) {
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
		restApi.getAsync(entityUrl, null, function(entity) {
			ns.updateEntityTable(entity);
			ns.updateEntitySearchResults(entity);
		});
	};

	ns.updateEntityTable = function(entity) {
		var name = typeof entity.name !== 'undefined' ? entity.name : 'N/A';
		var identifier = typeof entity.identifier !== 'undefined' ? entity.identifier : 'N/A';
		var description = typeof entity.description !== 'undefined' ? entity.description : 'N/A';

		var items = [];
		items.push('<thead><tr><th colspan="2">Entity details</th></tr></thead>');
		items.push('<tbody>');
		items.push('<tr><td>Name</td><td>' + name + '</td></tr>');
		items.push('<tr><td>Identifier</td><td>' + identifier + '</td></tr>');
		items.push('<tr><td>Description</td><td>' + description + '</td></tr>');
		items.push('</tbody>');
		$('#entity-table').html(items.join(''));
	};

	ns.updateEntitySearchResults = function(entity) {
		searchApi.search(ns.createSearchRequest(entity), function(searchResponse) {
			var items = [];
			if (searchResponse.totalHitCount == 0) {
				items.push('<h3>No search results</h3>');
			} else {
				items.push('<ul>');
				$.each(searchResponse.searchHits, function(key, val) {
					items.push('<li>' + JSON.stringify(val) + '</li>');
				});
				items.push('</ul>');
			}
			$('#entity-search-results').html(items.join(''));
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
