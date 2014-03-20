(function($, molgenis) {
	"use strict";
	var self = molgenis.dataexplorer = molgenis.dataexplorer || {};
	
	// module api
	self.getSelectedEntityMeta = getSelectedEntityMeta;
	self.getSelectedAttributes = getSelectedAttributes;
	self.setShowWizardOnInit = setShowWizardOnInit; 
	self.getEntityQuery = getEntityQuery;
	self.createFilterControls = createFilterControls;
	self.createFilters = createFilters;
	
	var restApi = new molgenis.RestClient();
	var selectedEntityMetaData = null;
	var attributeFilters = {};
	var selectedAttributes = [];
	var searchQuery = null;
	var showWizardOnInit = false;
	
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
		items.push('<ul class="nav nav-tabs">');
		$.each(modules, function() {
			var href = molgenis.getContextUrl() + '/module/' + this.id;
			items.push('<li data-id="' + this.id + '"><a href="' + href + '" data-target="#tab-' + this.id + '" data-toggle="tab"><img src="/img/' + this.icon + '"> ' + this.label + '</a></li>');
		});
		items.push('</ul>');
		items.push('<div class="tab-content">');
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
	function createFiltersList(attributeFilters) {
		var items = [];
		$.each(attributeFilters, function(attributeUri, attributeFilter) {
			var attribute = attributeFilter.attribute;
			var joinChars = attributeFilter.operator ? ' ' + attributeFilter.operator + ' ' : ',';
			var attributeLabel = attribute.label || attribute.name;
			items.push('<p><a class="feature-filter-edit" data-href="' + attributeUri + '" href="#">'
					+ attributeLabel + ' (' + attributeFilter.values.join(joinChars)
					+ ')</a><a class="feature-filter-remove" data-href="' + attributeUri + '" href="#" title="Remove '
					+ attributeLabel + ' filter" ><i class="icon-remove"></i></a></p>');
		});
		items.push('</div>');
		$('#feature-filters').html(items.join(''));
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

		$.each(attributeFilters, function(attributeUri, attributeFilter) {
			if (count > 0) {
				entityCollectionRequest.q.push({
					operator : 'AND'
				});
			}
			var attribute = attributeFilter.attribute;
			var rangeQuery = attribute.fieldType === 'DATE' || attribute.fieldType === 'DATE_TIME' || attribute.fieldType === 'DECIMAL' || attribute.fieldType === 'INT' || attribute.fieldType === 'LONG';
			
			$.each(attributeFilter.values, function(index, value) {
				if (rangeQuery) {

					// Range filter
					var rangeAnd = false;
					if ((index == 0) && (value != '')) {
						entityCollectionRequest.q.push({
							field : attribute.name,
							operator : 'GREATER_EQUAL',
							value : value
						});
						rangeAnd = true;
					}
					if (rangeAnd) {
						entityCollectionRequest.q.push({
							operator : 'AND'
						});
					}
					if ((index == 1) && (value != '')) {
						entityCollectionRequest.q.push({
							field : attribute.name,
							operator : 'LESS_EQUAL',
							value : value
						});
					}
				} else {
					if (index > 0) {
						var operator = attributeFilter.operator ? attributeFilter.operator : 'OR';
						entityCollectionRequest.q.push({
							operator : operator
						});
					}
					entityCollectionRequest.q.push({
						field : attribute.name,
						operator : 'EQUALS',
						value : value
					});
				}
			});
			count++;
		});

		return entityCollectionRequest;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createFilterControls(attribute, attributeFilter) {
		var label;
		var controls = $('<div class="controls">');
		controls.data('attribute', attribute);
		
		var name = 'input-' + attribute.name + '-' + new Date().getTime();
		var values = attributeFilter ? attributeFilter.values : null;
		switch(attribute.fieldType) {
			case 'BOOL':
				label = $('<span class="control-label">' + attribute.label + '</label>');
				var attrs = {'name': name};
				var attrsTrue = values && values[0] === 'true' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
				var attrsFalse = values && values[0] === 'false' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
				var inputTrue = createInput(attribute.fieldType, attrsTrue, true);
				var inputFalse = createInput(attribute.fieldType, attrsFalse, false);
				controls.append(inputTrue.addClass('inline')).append(inputFalse.addClass('inline'));
				break;
			case 'CATEGORICAL':
				label = $('<label class="control-label" for="' + name + '">' + attribute.label + '</label>');
				var entityMeta = restApi.get(attribute.refEntity.href);
				var entitiesUri = entityMeta.href.replace(new RegExp('/meta[^/]*$'), ""); // TODO do not manipulate uri

				var entities = restApi.get(entitiesUri);
				$.each(entities.items, function() {
					var attrs = {'name': name, 'id': name};
					if(values && $.inArray(this[entityMeta.labelAttribute], values) > -1)
						attrs.checked = 'checked';
					controls.append(createInput(attribute.fieldType, attrs, this[entityMeta.labelAttribute], this[entityMeta.labelAttribute]));
				});
				break;
			case 'DATE':
			case 'DATE_TIME':
				label = $('<span class="control-label">' + attribute.label + '</label>');
				var nameFrom = name + '-from', nameTo = name + '-to';
				var valFrom = values ? values[0] : undefined;
				var valTo = values ? values[1] : undefined;
				var inputFrom = $('<div class="control-group">').append(createInput(attribute.fieldType, {'name': nameFrom, 'placeholder': 'Start date'}, valFrom ? valFrom.replace("T", "'T'") : valFrom));
				var inputTo = createInput(attribute.fieldType, {'name': nameTo, 'placeholder': 'End date'}, valTo ? valTo.replace("T", "'T'") : valTo);
				controls.append(inputFrom).append(inputTo);
				break;
			case 'DECIMAL':
			case 'INT':
			case 'LONG':
				label = $('<span class="control-label">' + attribute.label + '</label>');
				var nameFrom = name + '-from', nameTo = name + '-to';
				var labelFrom = $('<label class="horizontal-inline" for="' + nameFrom + '">From</label>');
				var labelTo = $('<label class="horizontal-inline inbetween" for="' + nameTo + '">To</label>');
				var inputFrom = createInput(attribute.fieldType, {'name': nameFrom, 'id': nameFrom}, values ? values[0] : undefined).addClass('input-small');
				var inputTo = createInput(attribute.fieldType, {'name': nameTo, 'id': nameTo}, values ? values[1] : undefined).addClass('input-small');
				controls.addClass('form-inline').append(labelFrom).append(inputFrom).append(labelTo).append(inputTo);
				break;
			case 'EMAIL':
			case 'HTML':
			case 'HYPERLINK':
			case 'STRING':
			case 'TEXT':
				label = $('<label class="control-label" for="' + name + '">' + attribute.label + '</label>');
				controls.append(createInput(attribute.fieldType, {'name': name, 'id': name}, values ? values[0] : undefined)); 
				break;
			case 'MREF':
			case 'XREF':
				label = $('<label class="control-label" for="' + name + '">' + attribute.label + '</label>');
				var element = $('<div />');
				var operator = attributeFilter ? attributeFilter.operator : 'OR';
				element.xrefsearch({attribute: attribute, values: values, operator: operator});
				controls.append(element);
				break;
			case 'COMPOUND' :
			case 'ENUM':
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + dataType;
			default:
				throw 'Unknown data type: ' + dataType;			
		}
		
		// show description in tooltip
		if (attribute.description) {
			label.attr('data-toggle', 'tooltip');
			label.attr('title', attribute.description);
		}
		return $('<div class="control-group">').append(label).append(controls);	
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createFilters(form) {
		var filters = {};
		$('.controls', form).each(function() {
			var attribute = $(this).data('attribute');
			var filter = filters[attribute.href];
			$(":input", $(this)).not('[type=radio]:not(:checked)').not('[type=checkbox]:not(:checked)').each(function(){
				var value = $(this).val();
				if(value) {
					if(!filter) {
						filter = {};
						filters[attribute.href] = filter;
						filter.attribute = attribute;
					}
					var values = filter.values;
					if(!values) {
						values = [];
						filter.values = values;
					}
					
					if ($(this).hasClass('operator')) {
						filter.operator = value;
					} else {
						values.push(value);
					}
				}
			});	
		});
		return Object.keys(filters).map(function (key) { return filters[key]; }).filter(function(filter){return filter.values.length > 0;});
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
			
			restApi.getAsync(entityUri + '/meta', {'expand': ['attributes']}, function(entityMetaData) {
				selectedEntityMetaData = entityMetaData;

				// get modules config for this entity
				$.get(molgenis.getContextUrl() + '/modules?entity=' + entityMetaData.name).done(function(data) {
					createModuleNav(data.modules, $('#module-nav'));
				
					selectedAttributes = $.map(entityMetaData.attributes, function(attribute) {
						return attribute.fieldType !== 'COMPOUND' ? attribute : null;
					});
					
					createEntityMetaTree(entityMetaData, selectedAttributes);
					
					// select first tab
					$('a[data-toggle="tab"]', $('#module-nav')).first().click();
					
					//Show wizard on show of dataexplorer if url param 'wizard=true' is added
					if (showWizardOnInit) {
						molgenis.dataexplorer.wizard.openFilterWizardModal(selectedEntityMetaData, attributeFilters);
						showWizardOnInit = false;
					}
				});
			});
		});
		
		$(document).on('updateAttributeFilters', function(e, data) {
			$.each(data.filters, function() {
				attributeFilters[this.attribute.href] = this;
			});
			createFiltersList(attributeFilters);
			$(document).trigger('changeQuery', createEntityQuery());
		});
		
		$(document).on('removeAttributeFilter', function(e, data) {
			delete attributeFilters[data.attributeUri];
			createFiltersList(attributeFilters);
			$(document).trigger('changeQuery', createEntityQuery());
		});
		
		$(document).on('clickAttribute', function(e, data) {
			molgenis.dataexplorer.filter.openFilterModal(data.attribute, attributeFilters[data.attribute.href]);
		});
		
		var container = $("#plugin-container");
		
		// use chosen plugin for data set select
		$('#dataset-select').select2({ width: 'resolve' });
		$('#dataset-select').change(function() {
			$(document).trigger('changeEntity', $(this).val());
		});

		$("#observationset-search").focus();
		
		$("#observationset-search").change(function(e) {
			searchQuery = $(this).val();
			$(document).trigger('changeQuery', createEntityQuery());
		});
	
		$('#filter-wizard-btn').click(function() {
			molgenis.dataexplorer.wizard.openFilterWizardModal(selectedEntityMetaData, attributeFilters);
		});

		$(container).on('click', '.feature-filter-edit', function(e) {
			e.preventDefault();
			var attributeFilter = attributeFilters[$(this).data('href')];
			molgenis.dataexplorer.filter.openFilterModal(attributeFilter.attribute, attributeFilter);
		});
		
		$(container).on('click', '.feature-filter-remove', function(e) {
			e.preventDefault();
			$(document).trigger('removeAttributeFilter', {'attributeUri': $(this).data('href')});
		});
				
		// fire event handler
		$('#dataset-select').change();
	});
}($, window.top.molgenis = window.top.molgenis || {}));
