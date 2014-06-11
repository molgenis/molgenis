(function($, molgenis) {
	"use strict";
	var self = molgenis.dataexplorer = molgenis.dataexplorer || {};
	
	// module api
	self.getSelectedEntityMeta = getSelectedEntityMeta;
	self.getSelectedAttributes = getSelectedAttributes;
	self.setShowWizardOnInit = setShowWizardOnInit; 
	self.getEntityQuery = getEntityQuery;
	
	var restApi = new molgenis.RestClient();
	var selectedEntityMetaData = null;
	var attributeFilters = {};
	var selectedAttributes = [];
	var searchQuery = null;
	var showWizardOnInit = false;
	var modules = [];
	
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
	
	function createHeader(entityName) {
		restApi.getAsync('/api/v1/' + entityName + '/meta', {attributes:['label', 'description']}, function(meta) {
			$('#entity-class-name').html(meta.label);
			
			if (meta.description) {
				var description = $('<span data-placement="bottom"></span>');
				description.html(abbreviate(meta.description, 180));
				description.attr('data-title', meta.description);
				$('#entity-class-description').append(description.tooltip());
			} else {
				$('#entity-class-description').html('');
			}
		});
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createEntityQuery() {
		var entityCollectionRequest = {
			q : []
		};
		
		var count = 0;

		if (searchQuery) {
			if (/\S/.test(searchQuery)) {
				entityCollectionRequest.q.push({
					operator : 'SEARCH',
					value : searchQuery
				});
				count++;
			}
		}

		$.each(attributeFilters, function(attributeUri, filter) {
			var rule = filter.createQueryRule();
			
			if (count > 0) {
				entityCollectionRequest.q.push({
					operator : 'AND'
				});
			}
			
			if(rule){
				entityCollectionRequest.q.push(rule);
				count++;
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
				
				createHeader(entityMetaData.name);
			});
		});
		
		$(document).on('updateAttributeFilters', function(e, data) {
			$.each(data.filters, function() {
				attributeFilters[this.attribute.href] = this;
			});
			self.filter.createFiltersList(attributeFilters);
			$(document).trigger('changeQuery', createEntityQuery());
		});
		
		$(document).on('removeAttributeFilter', function(e, data) {
			delete attributeFilters[data.attributeUri];
			self.filter.createFiltersList(attributeFilters);
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
			searchQuery = $(this).val();
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
	});
}($, window.top.molgenis = window.top.molgenis || {}));
