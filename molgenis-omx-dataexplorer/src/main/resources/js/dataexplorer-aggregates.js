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
	self.updateAggregatesTable = updateAggregatesTable;
	
	var restApi = new molgenis.RestClient();

	/**
	 * @memberOf molgenis.dataexplorer.aggregates
	 */
	function createAggregatesTable() {
		var attributes = getAttributes();
		var aggregableAttributes = $.grep(attributes, function(attribute) {
			return attribute.aggregateable;
		});

		if (aggregableAttributes.length > 0) {
			$('#feature-select').empty();
			createAtributeDropdown($('#feature-select'), aggregableAttributes, 'x-aggr-attribute', aggregableAttributes[0], true);
			$('#feature-select').append(' x ');
			if(aggregableAttributes.length > 1) createAtributeDropdown($('#feature-select'), aggregableAttributes, 'y-aggr-attribute', aggregableAttributes[1]);
			else createAtributeDropdown($('#feature-select'), aggregableAttributes, 'y-aggr-attribute', false);

			$('#feature-select-container').show();
			$('#aggregate-table-container').empty();
			
			$('.attribute-dropdown').on('change', function() {
				updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val());
			});

			//render first results
			updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val());
		} else {
			$('#feature-select-container').hide();
			$('#aggregate-table-container').html('<p>No aggregable items</p>');
		}
	}
	
	function createAtributeDropdown(parent, aggregableAttributes, id, defaultValue, hasDefault) {
        if(defaultValue && hasDefault){
            var attributeSelect = $('<select id="' + id + '" class="attribute-dropdown"/>');
        }
        else{
            var attributeSelect = $('<select id="' + id + '" class="attribute-dropdown" data-placeholder="Select ..." />');
            attributeSelect.append('<option value="">Select ...</option>');
        }
		$.each(aggregableAttributes, function() {
		    if(this == defaultValue) attributeSelect.append('<option selected value="' + this.name + '">' + this.label + '</option>');
	        else attributeSelect.append('<option value="' + this.name + '">' + this.label + '</option>');
		});
		
		parent.append(attributeSelect);
		attributeSelect.select2({ width: 'resolve' });
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.aggregates
	 */
	function updateAggregatesTable(xAttributeName, yAttributeName) {
		if (!xAttributeName && !yAttributeName) {
			$('#aggregate-table-container').html('');
			return;
		}
		
		var data = {
			'entityName': getEntity().name,
			'xAxisAttributeName': xAttributeName,
			'yAxisAttributeName': yAttributeName,
			'q': getEntityQuery()
		};
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/aggregate',
			data : JSON.stringify(data),
			contentType : 'application/json',
			success : function(aggregateResult) {
				var countAboveZero = false;
                var items = ['<table class="table table-striped" >'];
				var noResultMessage = molgenis.dataexplorer.getNoResultMessage()
				items.push('<tr>');
				items.push('<td style="width: 18%"></td>');
				$.each(aggregateResult.yLabels, function(index, label){
					items.push('<th><div class="text-center">' + label + '</div></th>');
				});
				
				$.each(aggregateResult.matrix, function(index, row) {
					items.push('<tr>');
					items.push('<th>' + aggregateResult.xLabels[index] + '</th>');
					$.each(row, function(index, count) {
                        countAboveZero = count > 0 || countAboveZero;
						items.push('<td><div class="text-center">' + count + '</div></td>');
					});
					items.push('</tr>');
				});
				
				items.push('</table>');
				if(!countAboveZero && noResultMessage !== undefined){
                    items.length = 0;
                    items.push("<br><div>"+noResultMessage+"<div>");
                }
				$('#aggregate-table-container').html(items.join(''));
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
	}
	
	$(function() {		
		// bind event handlers with namespace
		$(document).on('changeAttributeSelection.aggregates', function(e, data) {
			molgenis.dataexplorer.aggregates.createAggregatesTable();
		});
		
		$(document).on('updateAttributeFilters.aggregates', function(e, data) {
			molgenis.dataexplorer.aggregates.updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val());
		});
		
		$(document).on('removeAttributeFilter.aggregates', function(e, data) {
			molgenis.dataexplorer.aggregates.updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val());
		});
		
		$(document).on('changeQuery.aggregates', function(e, entitySearchQuery) {
			molgenis.dataexplorer.aggregates.updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val());
		});
	});
})($, window.top.molgenis = window.top.molgenis || {});