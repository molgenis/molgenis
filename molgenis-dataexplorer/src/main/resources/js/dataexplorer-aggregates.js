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

	var totalTemplate, missingTemplate, messageTemplate;
	
	/**
	 * @memberOf molgenis.dataexplorer.aggregates
	 */
	function createAggregatesTable() {
		var attributes = getAttributes();
		var aggregableAttributes = $.grep(attributes, function(attribute) {
			if(attribute.aggregateable) {
				if(attribute.nillable) {
					return attribute.fieldType !== 'CATEGORICAL' && attribute.fieldType !== 'XREF' && attribute.fieldType !== 'MREF' && attribute.fieldType !== 'CATEGORICAL_MREF';
				}
				return true;
			}
			return false;
		});
		
		if (aggregableAttributes.length > 0) {
			createAttributeDropdown($('#x-aggr-div'), aggregableAttributes, 'x-aggr-attribute', aggregableAttributes[0], true);
			createAttributeDropdown($('#y-aggr-div'), aggregableAttributes, 'y-aggr-attribute', aggregableAttributes.length > 1 ? aggregableAttributes[1] : false);
			$('#distinct-attr-select').empty();
			if (molgenis.dataexplorer.settings['agg_distinct'] === false){
				$('#distinct-attr').hide();
			} else {
				$('#distinct-attr').show();
				if (molgenis.dataexplorer.settings['agg_distinct_overrides'] && JSON.parse(molgenis.dataexplorer.settings['agg_distinct_overrides'])[getEntity().name]) {
					// show fixed value for this entity
					var distinctAttr = JSON.parse(molgenis.dataexplorer.settings['agg_distinct_overrides'])[getEntity().name];
					var distinctAttrLabel = getEntity().attributes[distinctAttr].label;
					$('#distinct-attr-select').append($('<p>').addClass('form-control-static').text(distinctAttrLabel));
				} else {
					var distinctAttributes = $.grep(attributes, function(attribute) {
						// see: https://github.com/molgenis/molgenis/issues/1938
						return attribute.nillable !== true;
					});
					createAttributeDropdown($('#distinct-attr-select'), distinctAttributes, 'distinct-aggr-attribute', false);
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
		parent.empty();
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
		attributeSelect.select2({ width: '100%' });
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
					items.push('<th><div class="text-center">' + (label === null ? missingTemplate({}) : htmlEscape(label)) + '</div></th>');
				});
				items.push('<th><div class="text-center">' + totalTemplate({}) + '</div></th></tr>');

				var columnCounts = [];
				$.each(aggregateResult.matrix, function(index, row) {
					items.push('<tr>');
					var label = aggregateResult.xLabels[index];
					items.push('<th>' + (label === null ? missingTemplate({}) : htmlEscape(label)) + '</th>');

					var rowCount = 0;
					var rowCountIsAnonimized = false;
					$.each(row, function(index, count) {
                        if(!countAboveZero) {
                            countAboveZero = count > 0 || count == -1;
                        }
						if (!columnCounts[index]) {
							columnCounts[index] = {count: 0, anonymized: false};
						}
                        if (count == AGGREGATE_ANONYMIZATION_VALUE) {
                            rowCountIsAnonimized = true;
                            rowCount += aggregateResult.anonymizationThreshold;
                            columnCounts[index].count += aggregateResult.anonymizationThreshold;
                            columnCounts[index].anonymized = true;
                        } else {
                            rowCount += count;
                            columnCounts[index].count += count;
                        }

                        if(yAttributeName!==undefined&&yAttributeName!=="") {
                            items.push('<td><div class="text-center">');
                            if (count == AGGREGATE_ANONYMIZATION_VALUE) {
                                items.push('&le;' + aggregateResult.anonymizationThreshold);
                            } else {
                                items.push(count);
                            }
                            items.push('</div></td>');
                        }
					});

					items.push('<td><div class="text-center">');
					if (rowCountIsAnonimized) {
						items.push('&le;');
					}
					items.push(rowCount + '</div></td>');
					items.push('</tr>');
				});
				
				items.push('<tr>');
				items.push('<th>' + totalTemplate({}) + '</th>');
				
				var grandTotal = {count: 0, anonymized: false};
				$.each(columnCounts, function(){
					items.push('<td><div class="text-center">');
					if (this.anonymized) {
						items.push('&le;');
						grandTotal.anonymized = true;
					}
					
					grandTotal.count += this.count;
					items.push(this.count);
					items.push('</div></td>');
				});

                if(yAttributeName!==undefined&&yAttributeName!=="") {
                    items.push('<td><div class="text-center">');
                    if (grandTotal.anonymized) items.push('&le;');
                    items.push(grandTotal.count);
                    items.push('</div></td>');
                }
				
				items.push('</tr>');
				
				items.push('</table>');
				if(!countAboveZero){
                    items.length = 0;
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
		var selectedEntityMeta = getEntity();
		
		//No 'nested' mref attributes
		attributes = $.grep(attributes, function(attribute) {
			return selectedEntityMeta.attributes[attribute.name] !== undefined;
		});
		
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
		totalTemplate = Handlebars.compile($("#aggregates-total-template").html());
		missingTemplate = Handlebars.compile($("#aggregates-missing-template").html());
		messageTemplate = Handlebars.compile($("#aggregates-no-result-message-template").html());
		
		// bind event handlers with namespace
		$(document).on('changeAttributeSelection.aggregates', function(e, data) {
			molgenis.dataexplorer.aggregates.createAggregatesTable();
		});
		
		$(document).on('changeQuery.aggregates', function(e, entitySearchQuery) {
			molgenis.dataexplorer.aggregates.updateAggregatesTable($('#x-aggr-attribute').val(), $('#y-aggr-attribute').val(), $('#distinct-aggr-attribute').val());
		});
	});
})($, window.top.molgenis = window.top.molgenis || {});