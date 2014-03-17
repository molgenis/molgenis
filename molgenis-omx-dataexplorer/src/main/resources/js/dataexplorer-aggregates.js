/**
 * Aggregates module
 * 
 * Dependencies: dataexplorer.js
 *  
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	molgenis.dataexplorer = molgenis.dataexplorer || {};
	var self = molgenis.dataexplorer.aggregates = molgenis.dataexplorer.aggregates || {};
	
	// module api
	self.createAggregatesTable = createAggregatesTable;
	
	var restApi = new molgenis.RestClient();

	/**
	 * @memberOf molgenis.dataexplorer.aggregates
	 */
	function createAggregatesTable() {
		var attributes = getAttributes();
		var aggregableAttributes = $.grep(attributes, function(attribute) {
			return attribute.fieldType === 'BOOL' || attribute.fieldType === 'CATEGORICAL' || attribute.fieldType === 'XREF';
		});

		if (aggregableAttributes.length > 0) {
			var attributeSelect = $('<select id="selectFeature"/>');
			$.each(aggregableAttributes, function() {
				attributeSelect.append('<option value="' + this.name + '">' + this.label + '</option>');
			});
			$('#feature-select').html(attributeSelect);
			$('#feature-select-container').show();
			$('#aggregate-table-container').empty();
			if (attributeSelect.val()) {
				updateAggregatesTable(attributeSelect.val());
				attributeSelect.chosen();
				attributeSelect.change(function() {
					updateAggregatesTable($(this).val());
				});
			}
		} else {
			$('#feature-select-container').hide();
			$('#aggregate-table-container').html('<p>No aggregable items</p>');
		}
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.aggregates
	 */
	function updateAggregatesTable(attributeName) {
		showSpinner();
		var data = {
			'entityName': getEntity().name,
			'xAxisAttributeName': attributeName,
			'q': getEntityQuery()
		};
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/aggregate',
			data : JSON.stringify(data),
			contentType : 'application/json',
			success : function(aggregateResult) {
				hideSpinner();
				var table = $('<table />').addClass('table table-striped');
				table.append('<tr><th>Category name</th><th>Count</th></tr>');
				$.each(aggregateResult.hashCategories, function(categoryName, count) {
					table.append('<tr><td>' + categoryName + '</td><td>' + count + '</td></tr>');
				});
				$('#aggregate-table-container').html(table);
			},
			error : function(xhr) {
				hideSpinner();
				molgenis.createAlert(JSON.parse(xhr.responseText).errors);
			}
		});
	}
	
	/**
	 * Returns the selected attributes from the data explorer
	 * 
	 * @memberOf molgenis.dataexplorer.aggregates
	 */
	function getAttributes() {
		var attributes = molgenis.dataexplorer.getSelectedAttributes();
		return molgenis.getAtomicAttributes(attributes, restApi);
	}
	
	/**
	 * Returns the selected entity from the data explorer
	 * 
	 * @memberOf molgenis.dataexplorer.aggregates
	 */
	function getEntity() {
		return molgenis.dataexplorer.getSelectedEntityMeta();
	}
	
	/**
	 * Returns the selected entity query from the data explorer
	 * 
	 * @memberOf molgenis.dataexplorer.aggregates
	 */
	function getEntityQuery() {
		return molgenis.dataexplorer.getEntityQuery().q;
	};
	
	$(function() {
		// unbind existing event handlers before binding new ones
		$(document).off('.aggregates');
		
		// bind event handlers with namespace
		$(document).on('changeAttributeSelection.aggregates', function(e, data) {
			molgenis.dataexplorer.aggregates.createAggregatesTable();
		});
		
		$(document).on('updateAttributeFilters.aggregates', function(e, data) {
			molgenis.dataexplorer.aggregates.createAggregatesTable();
		});
		
		$(document).on('removeAttributeFilter.aggregates', function(e, data) {
			molgenis.dataexplorer.aggregates.createAggregatesTable();
		});
		
		$(document).on('changeQuery.aggregates', function(e, entitySearchQuery) {
			molgenis.dataexplorer.aggregates.createAggregatesTable();
		});
	});
})($, window.top.molgenis = window.top.molgenis || {});