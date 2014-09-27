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
	
	var AGGREGATE_ANONYMIZATION_VALUE = -1;
	
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
			createAttributeDropdown($('#feature-select'), aggregableAttributes, 'x-aggr-attribute', aggregableAttributes[0], true);
			$('#feature-select').append(' x ');
			if(aggregableAttributes.length > 1) createAttributeDropdown($('#feature-select'), aggregableAttributes, 'y-aggr-attribute', aggregableAttributes[1]);
			else createAttributeDropdown($('#feature-select'), aggregableAttributes, 'y-aggr-attribute', false);
			$('#distinct-attr-select').empty();
			if( molgenis.dataexplorer.settings && (molgenis.dataexplorer.settings['mod.aggregates.distinct.hide']==='true') ){
				$('#distinct-attr').hide();
			} else {
				$('#distinct-attr').show();
				if( molgenis.dataexplorer.settings && 
						molgenis.dataexplorer.settings.hasOwnProperty('mod.aggregates.distinct.override.'+getEntity().name)) {
					// show fixed value for this entity
					$('#distinct-attr-select').append($('<p>').addClass('form-control-static')
							.text(molgenis.dataexplorer.settings['mod.aggregates.distinct.override.'+getEntity().name]));
				} else {
					createAttributeDropdown($('#distinct-attr-select'), attributes, 'distinct-aggr-attribute', false);
				}
			}
			
			$('#feature-select-container').show();
			$('#aggregate-table-container').empty();
			
			$('.attribute-dropdown').on('change', function() {
				updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val(), $('#distinct-aggr-attribute').val());
			});

			//render first results
			updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val(), $('#distinct-aggr-attribute').val());
		} else {
			$('#feature-select-container').hide();
			$('#aggregate-table-container').html('<p>No aggregable items</p>');
		}
	}
	
	function createAttributeDropdown(parent, aggregableAttributes, id, defaultValue, hasDefault) {
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
		attributeSelect.select2();
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.aggregates
	 */
	function updateAggregatesTable(xAttributeName, yAttributeName, distinctAttributeName) {
		if (!xAttributeName && !yAttributeName) {
			$('#aggregate-table-container').html('');
			return;
		}
		
		var data = {
			'entityName': getEntity().name,
			'xAxisAttributeName': xAttributeName,
			'yAxisAttributeName': yAttributeName,
			'distinctAttributeName': distinctAttributeName,
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
				items.push('<tr>');
				items.push('<td style="width: 18%"></td>');
				
				$.each(aggregateResult.yLabels, function(index, label){
					items.push('<th><div class="text-center">' + htmlEscape(label) + '</div></th>');
				});
				items.push('<th><div class="text-center">' + totalCaption + '</div></th></tr>');
				
				var columnCounts = [];
				$.each(aggregateResult.matrix, function(index, row) {
					items.push('<tr>');
					items.push('<th>' + htmlEscape(aggregateResult.xLabels[index]) + '</th>');
					
					var rowCount = 0;
					var rowCountIsAnonimized = false;
					$.each(row, function(index, count) {
						countAboveZero = count > 0 || countAboveZero;						
						if (!columnCounts[index]) {
							columnCounts[index] = {count: 0, anonymized: false};
						}
						
                    	items.push('<td><div class="text-center">');
                    	
                    	if (count == AGGREGATE_ANONYMIZATION_VALUE) {
                    		rowCountIsAnonimized = true;
                    		items.push('&lt;' + aggregateResult.anonymizationThreshold);
                    		rowCount += aggregateResult.anonymizationThreshold - 1;
                            columnCounts[index].count += aggregateResult.anonymizationThreshold - 1;
                            columnCounts[index].anonymized = true;
                    	} else {
                    		rowCount += count;
                    		columnCounts[index].count += count;
                    		items.push(count);
                    	}
                		
                    	items.push('</div></td>');
					});
					
					items.push('<td><div class="text-center">');
					if (rowCountIsAnonimized) {
						items.push('&lt;' + (rowCount + 1));
					} else {
						items.push(rowCount);
					}
					items.push('</div></td>');
					items.push('</tr>');
				});
				
				items.push('<tr>');
				items.push('<th>' + totalCaption + '</th>');
				
				var grandTotal = {count: 0, anonymized: false};
				$.each(columnCounts, function(){
					items.push('<td><div class="text-center">');
					if (this.anonymized) {
						items.push('&lt');
						grandTotal.anonymized = true;
						items.push(this.count + 1);
					} else {
						items.push(this.count);
					}
					grandTotal.count += this.count;
					items.push('</div></td>');
				});
				
				items.push('<td><div class="text-center">');
				if (grandTotal.anonymized) {
					items.push('&lt;' + (grandTotal.count + 1));
				} else {
					items.push(grandTotal.count);
				}
				items.push('</div></td>');
				
				items.push('</tr>');
				
				items.push('</table>');
				if(!countAboveZero){
                    items.length = 0;
                    var messageTemplate = Handlebars.compile($("#aggregates-no-result-message-template").html());
                    items.push(messageTemplate({}));
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
		
		$(document).on('changeQuery.aggregates', function(e, entitySearchQuery) {
			molgenis.dataexplorer.aggregates.updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val(), $('#distinct-aggr-attribute').val());
		});
	});
})($, window.top.molgenis = window.top.molgenis || {});