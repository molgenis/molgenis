$.when( $, 
		window.top.molgenis = window.top.molgenis || {}, 
		$.get('dataexplorer/settings') 
).then(
function($, molgenis, settings) {	
	"use strict";
	var self = molgenis.dataexplorer = molgenis.dataexplorer || {};
	
	// module api
	self.getSelectedEntityMeta = getSelectedEntityMeta;
	self.getSelectedAttributes = getSelectedAttributes;
	self.setShowWizardOnInit = setShowWizardOnInit; 
	self.getEntityQuery = getEntityQuery;
    self.setNoResultMessage = setNoResultMessage;
    self.getNoResultMessage = getNoResultMessage;
    self.createHeader = createHeader;
	
	var restApi = new molgenis.RestClient();
	var selectedEntityMetaData = null;
	var attributeFilters = {};
	var selectedAttributes = [];
	var searchQuery = null;
	var showWizardOnInit = false;
	var modules = [];
    var noResultMessage = '';
    
    /**
     * @memberOf molgenis.dataexplorer
     */
    function setNoResultMessage(message) {
        noResultMessage = message;
    }

    /**
     * @memberOf molgenis.dataexplorer
     */
    function getNoResultMessage() {
        return noResultMessage;
    }

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function getSelectedEntityMeta() {
		return selectedEntityMetaData;
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function getSelectedAttributes() {
		return selectedAttributes;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function setShowWizardOnInit(show) {
		showWizardOnInit = show;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function getEntityQuery() {
		return createEntityQuery();
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createModuleNav(modules, container) {
		var items = [];
		items.push('<ul class="nav nav-tabs pull-left">');
		$.each(modules, function() {
			var href = molgenis.getContextUrl() + '/module/' + this.id;
			items.push('<li data-id="' + this.id + '"><a href="' + href + '" data-target="#tab-' + this.id + '" data-toggle="tab"><img src="/img/' + this.icon + '"> ' + this.label + '</a></li>');
		});
		items.push('</ul>');
		items.push('<div class="tab-content span9">');
		$.each(modules, function() {
			items.push('<div class="tab-pane" id="tab-' + this.id + '">Loading...</div>');
		});
		items.push('</div>');
		
		// add menu to container 
		container.html(items.join(''));
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createEntityMetaTree(entityMetaData, attributes) {
		var container = $('#feature-selection');
		container.tree({
			'entityMetaData' : entityMetaData,
			'selectedAttributes' : attributes,
			'onAttributesSelect' : function(selects) {
				selectedAttributes = container.tree('getSelectedAttributes');
				$(document).trigger('changeAttributeSelection', {'attributes': selectedAttributes});
			},
			'onAttributeClick' : function(attribute) {
				$(document).trigger('clickAttribute', {'attribute': attribute});
			}
		});
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createHeader(entityMetaData) {
		$('#entity-class-name').html(entityMetaData.label);
		
		if (entityMetaData.description) {
			var description = $('<span data-placement="bottom"></span>');
			description.html(abbreviate(entityMetaData.description, 
					settings['header.abbreviate']||180));
			description.attr('data-title', entityMetaData.description);
			$('#entity-class-description').html(description.tooltip());
		} else {
			$('#entity-class-description').html('');
		}
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createEntityQuery() {
		var entityCollectionRequest = {
			q : []
		};
		
		// add rules for the search term to the query
		if (searchQuery) {
			if (/\S/.test(searchQuery)) {
				var searchQueryRegex = /^\s*(?:chr)?([\d]{1,2}|X|Y|MT|XY):([\d]+)(?:-([\d]+)+)?\s*$/g;
				
				if(searchQueryRegex && searchQuery.match(searchQueryRegex)) {
					var match = searchQueryRegex.exec(searchQuery);
					
					// only chromosome and position
					if(match[3] === undefined){			
						var chromosome = match[1];
						var position = match[2];
						
				        entityCollectionRequest.q = 
				        	    [{
				        	        operator: "NESTED",
				        	        nestedRules: [{
				        	            field: "Chr",
				        	            operator: "EQUALS",
				        	            value: chromosome
				        	        }]
				        	    }, {
				        	        operator: "AND"
				        	    }, {
				        	        operator: "NESTED",
				        	        nestedRules: [{
				        	            field: "Pos",
				        	            operator: "EQUALS",
				        	            value: position
				        	        }]
				        	    }];
					// chromosome:startPos - endPos	
					}else if(match[3]) {
						
						var chromosome = match[1];
						var startPosition = match[2];
						var stopPosition = match[3];
						
						if(parseInt(startPosition, 10) > parseInt(stopPosition, 10)) {
							molgenis.createAlert([{message: 'The start position of the queried range is larger than the stop position. Please check the search query.'}], 'warning');
						}else{
							$('.alerts').empty();
						}
						
						entityCollectionRequest.q = 
							[{
								operator: "NESTED",
						        nestedRules: [{
							            operator: "NESTED",
							            nestedRules: [{
							                field: "Chr",
							                operator: "EQUALS",
							                value: chromosome
						            }]
						        }]
						    }, {
						    	operator: "AND"
						    }, {
						    	operator: "NESTED",
						        nestedRules: [{
				                    field: "Pos",
				                    operator: "GREATER_EQUAL",
				                    value: startPosition
				                }, {
				                	operator: "AND"
				                }, {
				                    field: "Pos",
				                    operator: "LESS_EQUAL",
				                    value: stopPosition
				                }]
						    }];
					}
				} else {
					entityCollectionRequest.q.push({
						operator : 'SEARCH',
						value : searchQuery
					});
				}
			}
		}

		// add rules for attribute filters to the query
		$.each(attributeFilters, function(attributeUri, filter) {
			var rule = filter.createQueryRule();
			if(rule){
				if (entityCollectionRequest.q.length > 0) {
					entityCollectionRequest.q.push({
						operator : 'AND'
					});
				}
				entityCollectionRequest.q.push(rule);
			}
		});
		
		/**
		 * Debug info: 
		 * Activate this code to see the query
		 * 
		 * $("#debugFilterQuery").remove();
		 * $("#tab-data").append($('<div id="debugFilterQuery"><p>QUERY : </p><p>' + JSON.stringify(entityCollectionRequest) + '</p></div>'));
		 */
		
		return entityCollectionRequest;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	$(function() {
		
		var searchTerm = $("#observationset-search").val();
		
		// lazy load tab contents
		$(document).on('show', 'a[data-toggle="tab"]', function(e) {
			var target = $($(e.target).attr('data-target'));
			if(target.data('status') !== 'loaded') {
				target.load($(e.target).attr('href'), function() {
					target.data('status', 'loaded');
				});
			}
		});
			
		$(document).on('changeEntity', function(e, entityUri) {
			// reset			
			selectedEntityMetaData = null;
			attributeFilters = {};
			selectedAttributes = [];
			searchQuery = null;
			
			$('#feature-filters p').remove();
			$("#observationset-search").val("");
			$('#data-table-pager').empty();
			
			// reset: unbind existing event handlers
			$.each(modules, function() {
				$(document).off('.' + this.id);	
			});
			
			restApi.getAsync(entityUri + '/meta', {'expand': ['attributes']}, function(entityMetaData) {
				selectedEntityMetaData = entityMetaData;

				// get modules config for this entity
				$.get(molgenis.getContextUrl() + '/modules?entity=' + entityMetaData.name).done(function(data) {
					modules = data.modules;
					createModuleNav(data.modules, $('#module-nav'));
				
					selectedAttributes = $.map(entityMetaData.attributes, function(attribute) {
						return attribute.fieldType !== 'COMPOUND' ? attribute : null;
					});
					
					createEntityMetaTree(entityMetaData, selectedAttributes);
					
					// select first tab
					$('a[data-toggle="tab"]', $('#module-nav')).first().click();
					
					//Show wizard on show of dataexplorer if url param 'wizard=true' is added
					if (showWizardOnInit) {
						self.filter.wizard.openFilterWizardModal(selectedEntityMetaData, attributeFilters);
						showWizardOnInit = false;
					}
					
				});
				
				self.createHeader(entityMetaData);
			});
		});
		
		$(document).on('updateAttributeFilters', function(e, data) {
			$.each(data.filters, function() {
				attributeFilters[this.attribute.href] = this;
			});
			self.filter.createFilterQueryUserReadableList(attributeFilters);
			$(document).trigger('changeQuery', createEntityQuery());
		});
		
		$(document).on('removeAttributeFilter', function(e, data) {
			delete attributeFilters[data.attributeUri];
			self.filter.createFilterQueryUserReadableList(attributeFilters);
			$(document).trigger('changeQuery', createEntityQuery());
		});
		
		$(document).on('clickAttribute', function(e, data) {
			if(data.attribute.fieldType !== 'COMPOUND')
				self.filter.dialog.openFilterModal(data.attribute, attributeFilters[data.attribute.href]);
		});
		
		var container = $("#plugin-container");
		
		if ($('#dataset-select').length > 0) {
			$('#dataset-select').select2({ width: 'resolve' });
			$('#dataset-select').change(function() {
				$(document).trigger('changeEntity', $(this).val());
			});
		}

		$("#observationset-search").focus();
		
		$("#observationset-search").change(function(e) {
			searchQuery = $(this).val().trim();
			$(document).trigger('changeQuery', createEntityQuery());
		});
	
		$('#filter-wizard-btn').click(function() {
			self.filter.wizard.openFilterWizardModal(selectedEntityMetaData, attributeFilters);
		});

		$(container).on('click', '.feature-filter-edit', function(e) {
			e.preventDefault();
			var filter = attributeFilters[$(this).data('href')];
			self.filter.dialog.openFilterModal(filter.attribute, filter);
		});
		
		$(container).on('click', '.feature-filter-remove', function(e) {
			e.preventDefault();
			$(document).trigger('removeAttributeFilter', {'attributeUri': $(this).data('href')});
		});
		
		// fire event handler
		$('#dataset-select').change();
		
		// restore the search term and trigger change event to filter data table
		if(searchTerm){
			$("#observationset-search").val(searchTerm).change();
		}
		
	});
});
