(function($, molgenis) {
	"use strict";

	var sortRule = null;

	molgenis.setEntityExplorerUrl = function(entityExplorerUrl) {
		molgenis.entityExplorerUrl = entityExplorerUrl;
	};
	
	molgenis.ResultsTable = function ResultsTable() {
	};

	molgenis.ResultsTable.prototype.getMaxRows = function() {
		return 20;
	};

	molgenis.ResultsTable.prototype.getSortRule = function() {
		return sortRule;
	};

	molgenis.ResultsTable.prototype.resetSortRule = function() {
		sortRule = null;
	};

	molgenis.ResultsTable.prototype.build = function(searchResponse, selectedFeatures, restApi) {
		var nrRows = searchResponse.totalHitCount;

		var items = [];
		items.push('<thead>');
		$.each(selectedFeatures, function(i, val) {
			var feature = restApi.get(this);
			if (sortRule && sortRule.value == feature.identifier) {
				if (sortRule.operator == 'SORTASC') {
					items.push('<th>' + feature.name + '<span data-value="' + feature.identifier
							+ '" class="ui-icon ui-icon-triangle-1-s down"></span></th>');
				} else {
					items.push('<th>' + feature.name + '<span data-value="' + feature.identifier
							+ '" class="ui-icon ui-icon-triangle-1-n up"></span></th>');
				}
			} else {
				items.push('<th>' + feature.name + '<span data-value="' + feature.identifier
						+ '" class="ui-icon ui-icon-triangle-2-n-s updown"></span></th>');
			}
		});
		items.push('</thead>');

		items.push('<tbody>');

		if (nrRows == 0) {
			items.push('<tr><td class="nothing-found" colspan="' + selectedFeatures.length + '">Nothing found</td></tr>');
		}

		for ( var i = 0; i < searchResponse.searchHits.length; ++i) {
			items.push('<tr>');
			var columnValueMap = searchResponse.searchHits[i].columnValueMap;

			$.each(selectedFeatures, function(i, val) {
				var feature = restApi.get(this);
				var value = columnValueMap[feature.identifier];
				var cellValue = "";
				if ((value != null) && (value != undefined)) {
					if (feature.dataType.toLowerCase() == "xref" && (typeof molgenis.entityExplorerUrl !== 'undefined')){
						var valueKey = columnValueMap['key-' + feature.identifier];
						var valueValue = formatTableCellValue(value, feature.dataType);
						cellValue = '<a href="'+ molgenis.entityExplorerUrl +'?entity=Characteristic&identifier=' + valueKey + '">' + valueValue + '</a>';
					}	
					else if (feature.dataType.toLowerCase() == "mref" && (typeof molgenis.entityExplorerUrl !== 'undefined')){
						var valueKeys = columnValueMap['key-' + feature.identifier];
						var valueValues = value.split(',');
						for (var i = 0; i < valueValues.length; i++) {
							var valueKey = valueKeys[i];
							var valueValue = formatTableCellValue(valueValues[i], feature.dataType);
						    if(i > 0) cellValue +=  ',';
						    cellValue += '<a href="'+ molgenis.entityExplorerUrl +'?entity=Characteristic&identifier=' + valueKey + '">' + valueValue + '</a>';
						}
					}
					else{
						cellValue = formatTableCellValue(value, feature.dataType);
					}
					items.push('<td class="multi-os-datacell">' + cellValue + '</td>');
				} else {
					items.push('<td></td>');
				}
			});

			items.push('</tr>');
		}
		items.push('</tbody>');
		$('#data-table').html(items.join(''));
		$('.show-popover').popover({trigger:'hover', placement: 'bottom'});
		
		// Sort click
		$('#data-table thead th .ui-icon').click(function() {
			if (nrRows == 0) {
				return;
			}

			var featureIdentifier = $(this).data('value');
			console.log("select sort column: " + featureIdentifier);
			if (sortRule && sortRule.operator == 'SORTASC') {
				sortRule = {
					value : featureIdentifier,
					operator : 'SORTDESC'
				};
			} else {
				sortRule = {
					value : featureIdentifier,
					operator : 'SORTASC'
				};
			}

			molgenis.updateObservationSetsTable();
			return false;
		});
	};

}($, window.top.molgenis = window.top.molgenis || {}));