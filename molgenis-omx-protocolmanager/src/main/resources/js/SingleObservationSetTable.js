(function($, w) {
	"use strict";

	var molgenis = w.molgenis = w.molgenis || {};

	molgenis.ResultsTable = function ResultsTable() {
	};

	molgenis.ResultsTable.prototype.getMaxRows = function() {
		return 1;
	};

	molgenis.ResultsTable.prototype.getSortRule = function() {
		return null;
	};

	molgenis.ResultsTable.prototype.resetSortRule = function() {
	};

	molgenis.ResultsTable.prototype.build = function(searchResponse, selectedFeatures, restApi) {
		var nrRows = searchResponse.totalHitCount;

		var items = [];
		items.push('<thead>');
		items.push('<th>Feature</th><th>Value</th>');
		items.push('</thead>');

		items.push('<tbody>');

		if (nrRows == 0) {
			items.push('<tr><td class="nothing-found" colspan="2">Nothing found</td></tr>');
			return;
		}

		var columnValueMap = searchResponse.searchHits[0].columnValueMap;
		$.each(selectedFeatures, function(i, val) {
			var feature = restApi.get(this);
			var value = columnValueMap[feature.identifier];

			items.push('<tr>');
			items.push('<td>' + feature.name + '</td>');
			if ((value != null) && (value != undefined)) {
				items.push('<td>' + molgenis.formatValue(value, feature.dataType) + '</td>');
			} else {
				items.push('<td></td>');
			}

			items.push('</tr>');
		});

		items.push('</tbody>');
		$('#data-table').html(items.join(''));
		$('.show-popover').popover({trigger:'hover', placement: 'right'});
	};

}($, window.top));