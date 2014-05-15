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
		
		$.each(attributeFilters, function(attributeUri, filter) {
			var attributeLabel = filter.attribute.label || filter.attribute.name;
			items.push('<p><a class="feature-filter-edit" data-href="' + attributeUri + '" href="#">'
					+ attributeLabel + ': ' + createFilterValuesRepresentation(filter)
					+ '</a><a class="feature-filter-remove" data-href="' + attributeUri + '" href="#" title="Remove '
					+ attributeLabel + ' filter" ><i class="icon-remove"></i></a></p>');
		});
		items.push('</div>');
		$('#feature-filters').html(items.join(''));
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createFilterValuesRepresentation(filter) {
		var s = '';
		if(filter.isType('complex')) {
			var filters = filter.getFilters();
			if(filters){
				$.each(filters, function(index, value){
					s += '(' + createSimpleFilterValuesRepresentation(filters[index]) + ')';
					if((filters.length - 1)  > index)
					{
						s += ' ' + filter.operator.toLowerCase() + ' ';
					}
				})

			}
		}
		else if(filter.isType('simple'))
		{
			s += createSimpleFilterValuesRepresentation(filter);
		}
		
		return s;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createSimpleFilterValuesRepresentation(filter) {
		var values = filter.getValues();
		switch(filter.attribute.fieldType) {
			case 'DATE':
			case 'DATE_TIME':
			case 'DECIMAL':
			case 'INT':
			case 'LONG':
				return htmlEscape((filter.fromValue ? 'from ' + filter.fromValue : '') + (filter.toValue ? ' to ' + filter.toValue : ''));
			case 'EMAIL':
			case 'HTML':
			case 'HYPERLINK':
			case 'STRING':
			case 'TEXT':
			case 'BOOL':
			case 'XREF':
			case 'ENUM':
				return htmlEscape(values[0] ? values[0] : '');
			case 'CATEGORICAL':
			case 'MREF':
				var operator = (filter.operator ? filter.operator.toLocaleLowerCase() : 'or');
				var array = [];
				$.each(values, function(key, value) {
					array.push('\'' + value + '\'');
				});
				return htmlEscape(array.join(' ' + operator + ' '));
			case 'COMPOUND' :
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + filter.attribute.fieldType;
			default:
				throw 'Unknown data type: ' + filter.attribute.fieldType;
		}
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
		
		$("#debugFilterQuery").remove();
		$("#tab-data").append($('<div id="debugFilterQuery"><p>QUERY : </p><p>' + JSON.stringify(entityCollectionRequest) + '</p></div>'));
		
		return entityCollectionRequest;
	}
			
	function createComplexFilterSelectOperator(operator){
		var orOption = $('<option value="OR">OR</option>');
		var andOption = $('<option value="AND">AND</option>');
		var select = $('<select class="complexFilter operator"></select>').width(70);
		var operatorLowerCase = (operator? operator.toLowerCase(): undefined);
		if(operatorLowerCase === 'and') andOption.attr('selected', 'selected');
		else orOption.attr('selected', 'selected');
		return select.append(orOption).append(andOption);
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createComplexFilterControls(attribute, filter, addLabel) 
	{		
		var container = createComplexFilterControlsContainer(attribute, filter, addLabel);		
		if(filter){
			if(filter.isType('complex')){
				$.each(filter.getFilters(), function(index, value){
					addComplexFilterControlsElementsToContainer(container, attribute, value, addLabel, (index > 0 ? true : false));
				});
			}
		}else{
			addComplexFilterControlsElementsToContainer(container, attribute, undefined, addLabel, false);
		}
		
		return container;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createComplexFilterControlsContainer(attribute, filter, addLabel)
	{
		var container = $('<div class="complexFilterContainer"></div>');
		var controlGroup = $('<div class="control-group">');
		var btnGroup = $('<div class="controls btn-group">');
		var operator = (filter?filter.operator:null);
		
		btnGroup.append(createComplexFilterSelectOperator(operator));
		btnGroup
			.append($('<button class="btn" type="button"><i class="icon-trash icon-plus"></i></button></button>').click(function(){
				addComplexFilterControlsElementsToContainer(container, attribute, undefined, addLabel, true);
			}));
		
		if(addLabel) controlGroup.append($('<label class="control-label">' + attribute.name + '</label>'));
		controlGroup.append(btnGroup);
		
		container.append(controlGroup);
		container.data('attribute', attribute);
		return container;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function addComplexFilterControlsElementsToContainer(container, attribute, simpleFilter, addLabel, addRemoveCapability) {
		var elements = createSimpleFilterControlsElements(attribute, simpleFilter, false);
		if(addLabel) elements.append($('<label class="control-label"></label>'));
		if(addRemoveCapability) addRemoveButton(elements);
		return container.append(elements);
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function addRemoveButton(container){
		$('.controls.controls-row', container)
			.append($('<span class="add-on">&nbsp;&nbsp;&nbsp;</span>'))
			.append($('<button class="btn" type="button"><i class="icon-trash"></i></button>').click(function(){
			$(this).parent().parent().remove();
		}));
		
		return container;
	}
	
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createSimpleFilterControls(attribute, filter, addLabel) {
		var container = $('<div class="simpleFilterContainer"></div>');
		container.append(createSimpleFilterControlsElements(attribute, filter, addLabel));
		container.data('attribute', attribute);
		return container;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createSimpleFilterControlsElements(attribute, filter, addLabel) {
		var label;
		var container = $('<div class="control-group">');
		var controls = $('<div class="controls controls-row">').width(565);
		var name = 'input-' + attribute.name + '-' + new Date().getTime();
		var values = filter ? filter.getValues() : null;
		var fromValue = filter ? filter.fromValue : null;
		var toValue = filter ? filter.toValue : null;
		switch(attribute.fieldType) {
			case 'BOOL':
				var attrs = {'name': name};
				var attrsTrue = values && values[0] === 'true' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
				var attrsFalse = values && values[0] === 'false' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
				var inputTrue = createInput(attribute.fieldType, attrsTrue, true);
				var inputFalse = createInput(attribute.fieldType, attrsFalse, false);
				controls.append(inputTrue.addClass('inline')).append(inputFalse.addClass('inline'));
				break;
			case 'CATEGORICAL':
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
				var nameFrom = name + '-from', nameTo = name + '-to';
				var valFrom = fromValue ? fromValue : undefined;
				var valTo = toValue ? toValue : undefined;
				var inputFrom = createInput(attribute.fieldType, {'name': nameFrom, 'placeholder': 'Start date'}, valFrom);
				var inputTo = createInput(attribute.fieldType, {'name': nameTo, 'placeholder': 'End date'}, valTo);
				controls.append(inputFrom).append($('<span class="add-on">&nbsp;&nbsp;&nbsp;</span>')).append(inputTo);
				break;
			case 'DECIMAL':
			case 'INT':
			case 'LONG':
				if (attribute.range) {
					var slider = $('<div id="slider" class="control-group"></div>');
					var min = fromValue ? fromValue : attribute.range.min;
					var max = toValue ? toValue : attribute.range.max;
					slider.editRangeSlider({
						symmetricPositionning: true,
						bounds: {min: attribute.range.min, max: attribute.range.max},
						defaultValues: {min: min, max: max},
						type: "number"
					});
					controls.append(slider);
				} else {
					var nameFrom = name + '-from', nameTo = name + '-to';
					var labelFrom = $('<label class="horizontal-inline" for="' + nameFrom + '">From</label>');
					var labelTo = $('<label class="horizontal-inline inbetween" for="' + nameTo + '">To</label>');
					var inputFrom = createInput(attribute.fieldType, {'name': nameFrom, 'id': nameFrom}, values ? fromValue : undefined).addClass('input-small');
					var inputTo = createInput(attribute.fieldType, {'name': nameTo, 'id': nameTo}, values ? toValue : undefined).addClass('input-small');
					controls.addClass('form-inline').append(labelFrom).append(inputFrom).append(labelTo).append(inputTo);
				}
				break;
			case 'EMAIL':
			case 'HTML':
			case 'HYPERLINK':
			case 'STRING':
			case 'TEXT':
			case 'ENUM':
				controls.append(createInput(attribute.fieldType, {'name': name, 'id': name}, values ? values[0] : undefined));
				break;
			case 'MREF':
			case 'XREF':
				var operator = filter ? filter.operator : 'OR';
				controls.addClass("xrefsearch");
				controls.xrefsearch({attribute: attribute, values: values, operator: operator});
				break;
			case 'COMPOUND' :
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + attribute.fieldType;
			default:
				throw 'Unknown data type: ' + attribute.fieldType;			
		}

		if(addLabel) container.append($('<label class="control-label" for="' + name + '">' + attribute.name + '</label>'));
		return container.append(controls);
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createFilterControls(attribute, filter, addLabel) {
		switch(attribute.fieldType) {
			case 'BOOL':
				return createSimpleFilterControls(attribute, filter, addLabel);
				break;
			case 'CATEGORICAL':
			case 'DATE':
			case 'DATE_TIME':
			case 'DECIMAL':
			case 'LONG':
			case 'EMAIL':
			case 'HTML':
			case 'HYPERLINK':
			case 'STRING':
			case 'ENUM':
			case 'INT':
			case 'TEXT':
			case 'MREF':
			case 'XREF':
				return createComplexFilterControls(attribute, filter, addLabel);
				break;
			case 'COMPOUND' :
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + attribute.fieldType;
			default:
				throw 'Unknown data type: ' + attribute.fieldType;
		}
	}
	
	

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createFilters(form) {
        var filters = {};
        var filter;
        
		$('.complexFilterContainer', form).each(function() {
			filter = new self.filter.ComplexFilter($(this).data('attribute'));
			filter.update($(this));
			if(!filter.isEmpty()){
				filters[filter.attribute.href] = filter;
			}
		});

		$('.simpleFilterContainer', form).each(function() {
			filter = new self.filter.SimpleFilter($(this).data('attribute'));
			filter.update($(this));
			filters[filter.attribute.href] = filter;
		});
		
		return Object.keys(filters).map(function (key) { return filters[key]; }).filter(
				function(filter)
				{
					return !filter.isEmpty();
				});
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
			if(data.attribute.fieldType !== 'COMPOUND')
				molgenis.dataexplorer.filter.openFilterModal(data.attribute, attributeFilters[data.attribute.href]);
		});
		
		var container = $("#plugin-container");
		
		// use chosen plugin for data set select
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
			molgenis.dataexplorer.wizard.openFilterWizardModal(selectedEntityMetaData, attributeFilters);
		});

		$(container).on('click', '.feature-filter-edit', function(e) {
			e.preventDefault();
			var filter = attributeFilters[$(this).data('href')];
			molgenis.dataexplorer.filter.openFilterModal(filter.attribute, filter);
		});
		
		$(container).on('click', '.feature-filter-remove', function(e) {
			e.preventDefault();
			$(document).trigger('removeAttributeFilter', {'attributeUri': $(this).data('href')});
		});
		
		// fire event handler
		$('#dataset-select').change();
	});
}($, window.top.molgenis = window.top.molgenis || {}));
