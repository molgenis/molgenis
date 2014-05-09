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
		
		//TODO JJ change filter handling
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
	////TODO JJ change filter handling
	function createFilterValuesRepresentation(filter) {
		var s = "TEST";
		console.log("filter: ", filter);
		if(filter.isType('complex')) {
			var filters =filter.getFilters();
			if(filters){
				if(filters.length > 1){
					s += '(';
				}
				for (var i = 0; i < filters.length; i++)
				{
					if(i > 0){
						s += ' ' + filter.operator + ' ';
					}
					s += createSimpleFilterValuesRepresentation(filters[i]);
				}
				if(filters.length > 1){
					s += ')';
				}
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
	////TODO JJ change filter handling
	function createSimpleFilterValuesRepresentation(filter) {
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
				return htmlEscape(filter.values[0] ? filter.values[0] : '');
			case 'CATEGORICAL':
			case 'MREF':
				var operator = (filter.operator?filter.operator.toLocaleLowerCase():'or');
				var array = [];
				$.each(filter.values, function(key, value) {
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

		$.each(attributeFilters, function(attributeUri, attributeFilter) {
			if (count > 0) {
				entityCollectionRequest.q.push({
					operator : 'AND'
				});
			}
			var attribute = attributeFilter.attribute;
			var rangeQuery = attribute.fieldType === 'DATE' || attribute.fieldType === 'DATE_TIME' || attribute.fieldType === 'DECIMAL' || attribute.fieldType === 'INT' || attribute.fieldType === 'LONG';
			
			if (rangeQuery) {
				// Range filter
				var fromValue = attributeFilter.fromValue;
				var toValue = attributeFilter.toValue;
				
				if(attribute.fieldType === 'DATE_TIME'){
					if(fromValue){
						fromValue = fromValue.replace("'T'", "T");
					}
					if(toValue){
						toValue = toValue.replace("'T'", "T");
					}
				}
				
				// add range fromValue / toValue
				if (fromValue && toValue) {
					entityCollectionRequest.q.push({
						operator: 'NESTED',
						nestedRules:[
						{
							field : attribute.name,
							operator : 'GREATER_EQUAL',
							value : fromValue
						},
						{
							operator : 'AND'
						},
						{
							field : attribute.name,
							operator : 'LESS_EQUAL',
							value : toValue
						}]
					});
					
				} else if (fromValue) {
					entityCollectionRequest.q.push({
						field : attribute.name,
						operator : 'GREATER_EQUAL',
						value : fromValue
					});
					
				} else if (toValue) {
					entityCollectionRequest.q.push({
						field : attribute.name,
						operator : 'LESS_EQUAL',
						value : toValue
					});
					
				}
			} else {
				
				if (attributeFilter.values.length > 1) {
					var nestedRule = {
						operator: 'NESTED',
						nestedRules:[]
					};
				
					$.each(attributeFilter.values, function(index, value) {
						if (index > 0) {
							var operator = attributeFilter.operator ? attributeFilter.operator : 'OR';
							nestedRule.nestedRules.push({
								operator : operator
							});
						}

						nestedRule.nestedRules.push({
							field : attribute.name,
							operator : 'EQUALS',
							value : value
						});
					});
				
					entityCollectionRequest.q.push(nestedRule);
				
				} else {
					entityCollectionRequest.q.push({
						field : attribute.name,
						operator : 'EQUALS',
						value : attributeFilter.values[0]
					});
					
				}
			}
			
			count++;
		});

		return entityCollectionRequest;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createFilterControls(attribute, attributeFilter, addLabel) {
		var label;
		var controls = $('<div class="controls">');
		controls.data('attribute', attribute);
		var name = 'input-' + attribute.name + '-' + new Date().getTime();
		var values = attributeFilter ? attributeFilter.values : null;
		var fromValue = attributeFilter ? attributeFilter.fromValue : null;
		var toValue = attributeFilter ? attributeFilter.toValue : null;
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
				controls.append($('<div class="control-group">').append(inputFrom)).append($('<div class="control-group">').append(inputTo));
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
				controls.addClass('complexFilter');
				controls.append($('<select class="complexFilter operator"><option value="OR">OR</option><option value="AND">AND</option></select>'));
				
				var filter1 = $('<div class="simpleFilter">');
				var element1 = $('<div />').css( "width", 525);
				var operator1 = attributeFilter ? attributeFilter.operator : 'OR';
				element1.xrefsearch({attribute: attribute, values: values, operator: operator1});
				filter1.append(element1)
				controls.append(filter1);
				
				var filter2 = $('<div class="simpleFilter">');
				var element2 = $('<div />').css( "width", 525);
				var operator2 = attributeFilter ? attributeFilter.operator : 'OR';
				element2.xrefsearch({attribute: attribute, values: values, operator: operator2});
				filter2.append(element2);
				controls.append(filter2);
				
				break;
			case 'COMPOUND' :
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + attribute.fieldType;
			default:
				throw 'Unknown data type: ' + attribute.fieldType;			
		}
		
		if(addLabel === true) 
		{
			label = $('<label class="control-label" for="' + name + '">' + attribute.label + '</label>');
			return $('<div class="control-group">').append(label).append(controls);
		}
		else 
		{
			return $('<div class="control-group">').append(controls);
		}

	}
	
	
	function Filter(){
		this.operators = {'OR' : 'OR', 'AND' : 'AND'};
		this.types = {'simple' : 'simple', 'complex': 'complex'};
		this.type = undefined;
		this.operator = this.operators['OR'];
		this.attribute = undefined;
		
		/**
		 * TODO	JJ write comment 
		 */
		//	this.update = function ($domElement) {
		// 	Implement this method in Different filters
		//	}
		
		/**
		 * TODO	JJ write comment 
		 */
		//	this.isEmpty = function () {
		//	Implement this method in Different filters
		//	}
		
		this.isType = function(type)
		{
			return type && this.types[type] && this.type === type;
		}
		
		return this;
	}
	
	function SimpleFilter(attribute){
		this.fromValue = undefined;
		this.toValue = undefined;
		this.values;
		this.type = 'simple';
		this.attribute = attribute;
		
		this.isEmpty = function() 
		{
			return !(this.values.length > 0 || this.fromValue || this.toValue);
		}
		
		this.update = function ($domElement) {
			var values = [];
			
			$(":input",$domElement).not('[type=radio]:not(:checked)').not('[type=checkbox]:not(:checked)').each(function(){
				var value = $(this).val();
				var name =  $(this).attr("name");
				
				
				// TODO JJ 2014-05-09 17:00
				
				if(value) {
					// Add operator
					if ($(this).hasClass('operator')) {
						filter.operator = value;
					} 
					
					// Add values
					else 
					{
		                if(attribute.fieldType === 'MREF'){
		                    var mrefValues = value.split(',');
		                    $(mrefValues).each(function(i){
		                    	values.push(mrefValues[i]);
		                    });
		                } 
		                else if(attribute.fieldType === 'INT'
							|| attribute.fieldType === 'LONG'
								|| attribute.fieldType === 'DECIMAL'
									|| attribute.fieldType === 'DATE'
										|| attribute.fieldType === 'DATE_TIME'
								){
							
							// Add toValue
							if(name && (name.match(/-to$/g) || name === 'sliderright')){
								filter.toValue = value;
								if(!filter.hasOwnProperty('fromValue')){
									filter.fromValue = undefined;
								}
							}
							
							// Add fromValue
							if(name && (name.match(/-from$/g) || name === 'sliderleft')){
								filter.fromValue = value;
							}
						}
		                else
		                {
		                	values[values.length] = value;
		                }
					}
				}
			});	

			this.values = values;
			
			return this;
		}

		return this;
	}
	SimpleFilter.prototype = new Filter();
	
	function ComplexFilter(attribute){
		var filters = [];
		this.type = 'complex';
		this.attribute = attribute;
		
		this.update = function ($domElement) {
			this.operator = $(":input.complexFilter.operator", $domElement);
			$(".simpleFilter", $domElement).each(function(){
				filters.push((new SimpleFilter(attribute)).update($(this)));
			});
		};
		
		this.isEmpty = function () {
			for(var i = 0; i < filters.length; i++)
			{
				if(!filters[i].isEmpty()){
					return false;
				}
			}
			return true;
		};
		
		this.getFilters = function () {
			return filters;
		};
		
		return this;
	}
	ComplexFilter.prototype = new Filter();

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createFilters(form) {
        var filters = {};
        var filter;
        
		$('.controls.complexFilter', form).each(function() {
			filter = new ComplexFilter($(this).data('attribute'));
			filter.update($(this));
			filters[filter.attribute.url] = filter;
		});

		$('.controls.simpleFilter', form).each(function() {
			alert("simpleFilter");
			filter = new SimpleFilter($(this).data('attribute'));
			console.log('filter', filter);
			filter.update($(this));
			filters[filter.attribute.url] = filter;
			
			console.log("TEST: " + filter);
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
			//$(document).trigger('changeQuery', createEntityQuery());
		});
		
		$(document).on('removeAttributeFilter', function(e, data) {
			delete attributeFilters[data.attributeUri];
			createFiltersList(attributeFilters);
			//$(document).trigger('changeQuery', createEntityQuery());
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
			var attributeFilter = attributeFilters[$(this).data('href')];
			console.log(attributeFilters);
			console.log(attributeFilters[$(this).data('href')]);
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
