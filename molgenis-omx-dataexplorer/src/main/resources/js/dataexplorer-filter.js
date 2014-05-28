/**
 * Attribute filter modal
 * 
 * Dependencies: dataexplorer.js
 *  
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	molgenis.dataexplorer = molgenis.dataexplorer || {};
	var self = molgenis.dataexplorer.filter = molgenis.dataexplorer.filter || {};
	
	self.createFilterControls = function createFilterControls(attribute, filter, addLabel) {
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
	};
	
	self.createFilters = function createFilters(form) {
        var filters = {};
        var filter;
        
		$('.complexFilterContainer', form).each(function() {
			filter = new self.ComplexFilter($(this).data('attribute'));
			filter.update($(this));
			if(!filter.isEmpty()){
				filters[filter.attribute.href] = filter;
			}
		});

		$('.simpleFilterContainer', form).each(function() {
			filter = new self.SimpleFilter($(this).data('attribute'));
			filter.update($(this));
			filters[filter.attribute.href] = filter;
		});
		
		return Object.keys(filters).map(function (key) { return filters[key]; }).filter(
				function(filter)
				{
					return !filter.isEmpty();
				});
	};
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	self.createFiltersList = function (attributeFilters) {
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
	};
	
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
				});
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
	
	function createComplexFilterSelectOperator(operator, container){
		var operatorLowerCase = (operator ? operator.toLowerCase() : 'or');
		var andOrSwitch = $('<input type="checkbox" class="complexFilter operator">');
		andOrSwitch.attr('checked', operatorLowerCase == 'or');
		container.append(andOrSwitch);
		
		andOrSwitch.bootstrapSwitch({
			onText: 'OR',
			offText: 'AND',
		});
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
					addComplexFilterControlsElementsToContainer(container, attribute, filter.operator, value, addLabel, (index > 0 ? true : false));
				});
			}
		}else{
			addComplexFilterControlsElementsToContainer(container, attribute, undefined, undefined, addLabel, false);
		}
		
		return container;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createComplexFilterControlsContainer(attribute, filter, addLabel)
	{
		var container = $('<div class="complexFilterContainer"></div>');
		container.data('attribute', attribute);
		return container;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function addComplexFilterControlsElementsToContainer(container, attribute, operator, simpleFilter, addLabel, addRemoveCapability) {
		var elements = createSimpleFilterControlsElements(attribute, simpleFilter, false);
		if(addRemoveCapability) {
			addRemoveButton(elements);
			elements.append($('<label class="control-label"></label>'));
		} else {
			var label = attribute.label || attribute.name;
			elements.append($('<label class="control-label" data-placement="right" data-title="' + attribute.description + '">' + label + '</label>').tooltip());
			var row = $('<div class="controls controls-row">');
			$('.controls.controls-row', elements).parent().append(row.append($('<button class="btn"  type="button"><i class="icon-plus"></i></button>').click(function(){
				addComplexFilterControlsElementsToContainer(container, attribute, operator, undefined, addLabel, true);
				if(!$('.complexFilter.operator', container).length){
					createComplexFilterSelectOperator(operator, row);
				}
			})));
		}
		
		return container.append(elements);
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function addRemoveButton(elements){
		$('.controls.controls-row', elements)
			.parent().append($('<div class="controls controls-row">').append($('<button class="btn" type="button"><i class="icon-trash"></i></button>').click(function(){
				if($('.icon-trash', elements.parent()).length === 1){
					$('.bootstrap-switch', elements.parent()).remove();
				}
				$(this).parent().parent().remove();
		})));

		return elements;
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
		var container = $('<div class="control-group">');
		var controls = $('<div class="controls controls-row">').width('40%');
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
				var restApi = new molgenis.RestClient();
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
		
	function Filter(){
		this.operators = {'OR' : 'OR', 'AND' : 'AND'};
		this.types = {'simple' : 'simple', 'complex': 'complex'};
		this.type = undefined;
		this.operator = this.operators['OR'];
		this.attribute = undefined;
		
		/**
		 * this.update = function ($domElement) 
		 * {
		 * 		Implement this method in subclass filters
		 * }
		 */
		
		/**
		 * this.isEmpty = function () 
		 * {
		 * 		Implement this method in subclass filters
		 * }
		 */
		
		/**
		 * this.createQueryRule = function (){ 
		 * {
		 * 		Implement this method in subclass filters
		 * }
		 */
		
		this.isType = function(type)
		{
			return type && this.types[type] && this.type === type;
		};
		
		return this;
	}
	
	self.SimpleFilter = function(attribute, fromValue, toValue, value){
		this.fromValue = fromValue;
		this.toValue = toValue;
		var values = [];
		this.type = 'simple';
		this.attribute = attribute;

        if(value !== undefined) values.push(value);

		this.isEmpty = function() 
		{
			return !(values.length || this.fromValue || this.toValue);
		};
		
		this.getValues = function ()
		{
			return values;
		};
		
		this.update = function ($domElement) {
			var fromValue = this.fromValue;
			var toValue = this.toValue;
			var operator = this.operator;
			
			$(":input",$domElement).not('[type=radio]:not(:checked)').not('[type=checkbox]:not(:checked)').not('.exclude').each(function(){
				var value = $(this).val();
				var name =  $(this).attr("name");
				
				if(value) {
					// Add operator
					if ($(this).hasClass('operator')) {
						operator = value;
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
								toValue = value;
							}
							
							// Add fromValue
							if(name && (name.match(/-from$/g) || name === 'sliderleft')){
								fromValue = value;
							}
						}
		                else
		                {
		                	values[values.length] = value;
		                }
					}
				}
			});
			this.fromValue = fromValue;
			this.toValue = toValue;
			this.operator = operator;
			return this;
		};
		
		this.createQueryRule = function (){
			var attribute = this.attribute;
			var fromValue = this.fromValue;
			var toValue = this.toValue;
			var operator = this.operator;
			var rule;
			var rangeQuery = attribute.fieldType === 'DATE' || attribute.fieldType === 'DATE_TIME' || attribute.fieldType === 'DECIMAL' || attribute.fieldType === 'INT' || attribute.fieldType === 'LONG';

			if (rangeQuery) {
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
					rule = {
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
					};
				} else if (fromValue) {
					rule = {
						field : attribute.name,
						operator : 'GREATER_EQUAL',
						value : fromValue
					};
				} else if (toValue) {
					rule = {
						field : attribute.name,
						operator : 'LESS_EQUAL',
						value : toValue
					};
				}
			} else {
				if(values){
					if (values.length > 1) {
						var nestedRule = {
							operator: 'NESTED',
							nestedRules:[]
						};
					
						$.each(values, function(index, value) {
							if (index > 0) {
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
						rule = nestedRule;
					} else {
						rule = {
							field : attribute.name,
							operator : 'EQUALS',
							value : values[0]
						};
					}
				}else{
					alert("values is empty: " + values);
				}
			}
			
			return rule;
		};

		return this;
	};
	self.SimpleFilter.prototype = new Filter();
	
	self.ComplexFilter = function(attribute){
		var filters = [];
		this.type = 'complex';
		this.attribute = attribute;
		
		this.update = function ($domElement) {
			var simpleFilter;
			this.operator = $(":input.complexFilter.operator", $domElement).attr('checked') === 'checked' ? 'OR' : 'AND' ;
			
			$(".controls", $domElement).each(function(){
				simpleFilter = (new self.SimpleFilter(attribute)).update($(this));
				if(!simpleFilter.isEmpty())
				{
					filters.push(simpleFilter);
				}
			});
			return this;
		};

        this.addFilter = function (simpleFilter, operator) {
            this.operator = operator;

            if(!simpleFilter.isEmpty())
            {
                filters.push(simpleFilter);
            }
			return this;
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
		
		this.createQueryRule = function() {
			var nestedRules = [];
			var operator = this.operator;
			var rule;
			
			$.each(filters, function(index, subFilter) {
				if(index > 0){
					nestedRules.push({
						operator : operator
					});
				}
				nestedRules.push(subFilter.createQueryRule());
			});
			
			rule = {
				operator: 'NESTED',
				nestedRules: nestedRules
			};
			
			return rule;
		};
		
		return this;
	};
	self.ComplexFilter.prototype = new Filter();
}($, window.top.molgenis = window.top.molgenis || {}));