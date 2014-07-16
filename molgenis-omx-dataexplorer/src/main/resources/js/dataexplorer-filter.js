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
	
	self.createFilterControls = function createFilterControls(attribute, filter, wizard) {
		switch(attribute.fieldType) {
			case 'BOOL':
			case 'CATEGORICAL':
			case 'XREF':
				return createSimpleFilterControls(attribute, filter, wizard);
				break;
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
				return createComplexFilterControls(attribute, filter, wizard);
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
			
			console.log("complexFilterContainer, filter: ", filter); 
			
			filter.update($(this));
			
			console.log("filter.isEmpty(): ", filter.isEmpty());
			
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
			var complexFilterElements = filter.getComplexFilterElements();
			if(complexFilterElements){
				$.each(complexFilterElements, function(index, complexFilterElement){
					if(complexFilterElement.operator)
					{
						s += ' ' + complexFilterElement.operator.toLowerCase() + ' ';
					}
					s += '(' + createSimpleFilterValuesRepresentation(complexFilterElement.simpleFilter) + ')';
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
			case 'ENUM':
				return htmlEscape(values[0] ? values[0] : '');
			case 'CATEGORICAL':
			case 'MREF':
			case 'XREF':
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
	
	function createComplexFilterSelectOperator(complexOperator){
		var $controlGroup = $('<div class="controls">').width('275px');
		
		var operator = (complexOperator === "AND" ? "AND" : "OR");
		var $operatorInput = $('<input type="hidden" data-filter="complex-operator" value="' + operator + '"/>');
		var $dropdown = $('<div class="btn-group" data-filter="complex-operator-container" style="margin-left: 140px"><div>');
		var orValue = "OR&nbsp;&nbsp;";
		var andValue = "AND";
		$dropdown.append($('<a class="btn btn-mini dropdown-toggle" data-toggle="dropdown" href="#">' + (operator === "AND" ? andValue : orValue) + ' <b class="caret"></a>'));
		$controlGroup.append($operatorInput);
		$dropdown.append($('<ul class="dropdown-menu"><li><a data-value="OR">' + orValue + '</a></li><li><a data-value="AND">' + andValue + '</a></li></ul>'));

		$.each($dropdown.find('.dropdown-menu li a'), function(index, element){
			$(element).click(function(){
				var dataValue = $(this).attr('data-value');
				$operatorInput.val(dataValue);
				$dropdown.find('a:first').html((dataValue === "AND" ? andValue : orValue) + ' <b class="caret"></b>');
				$dropdown.find('a:first').val(dataValue);
			});
		});
		
		$dropdown.find('div:first').remove();//This is a workaround FIX
		
		$controlGroup.append($dropdown);
		
		return $controlGroup;
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createComplexFilterControls(attribute, filter, wizard) 
	{		
		var $container = $('<div class="complexFilterContainer"></div>').data('attribute', attribute);
		
		if(filter){
			if(filter.isType('complex')){
				$.each(filter.getComplexFilterElements(), function(index, complexFilterElement){
					addComplexFilterElementToContainer($container, attribute, complexFilterElement.operator, complexFilterElement.simpleFilter, wizard, (index > 0 ? false : true), filter.getComplexFilterElements().length);
				});
			}
		}else{
			addComplexFilterElementToContainer($container, attribute, undefined, undefined, wizard, true, null);
		}
		
		return $container;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function addComplexFilterElementToContainer($container, attribute, complexFilterOperator, simpleFilter, wizard, isFirstElement, totalNumberElements) 
	{
		// TODO complex-element
		// $controlGroup
		var $complexElementContainer = $('<div class="control-group complex-element-container"></div>');
		var $complexElement = $('<div class="controls"></div>');
		$complexElement.attr('data-filter', "complex-element");
		$complexElement.addClass('complex-element');
		
		// Make label
		var $complexElementLabel = createFilterLabel(attribute, isFirstElement, wizard);
		$complexElementContainer.append($complexElementLabel);
		
		//Simple filter
		var $controlGroupSimpleFilter = createSimpleFilterControlsElements(attribute, simpleFilter);
		$controlGroupSimpleFilter.attr('data-filter', 'complex-simplefilter');
		$controlGroupSimpleFilter.addClass('complex-simplefilter');
				
		if(isFirstElement) 
		{
			// Add simple filter
			$complexElement.append($controlGroupSimpleFilter);
			
			if(totalNumberElements && totalNumberElements > 1) {
				
				// Add remove
				$complexElement.append(createRemoveFirstComplexElementButton($container));
			}
			
			// Add plus button
			$complexElementContainer.append(createComplexFilterPlusButton($container, attribute, complexFilterOperator, wizard));
		}else{
			// Add select complex filter operator
			var $complexOperatorControlGroup = $('<div class="control-group">');
			$complexOperatorControlGroup.append(createFilterLabel(attribute, isFirstElement, wizard));
			$complexOperatorControlGroup.append(createComplexFilterSelectOperator(complexFilterOperator));
			
			// Add operator
			$complexElement.append($complexOperatorControlGroup);	
			
			// Add simple filter
			$complexElement.append($controlGroupSimpleFilter);
			
			// Add remove
			$complexElement.append(createRemoveButton($complexElementContainer));
		}
		
		

		// Add complex element container to container
		$container.append($complexElementContainer.prepend($complexElement));
		
		return $complexElementContainer;
	}
	
	/**
	 * Create filter label
	 */
	function createFilterLabel(attribute, isFirstElement, wizard)
	{
		var label = attribute.label || attribute.name;
		if(isFirstElement && wizard) 
		{
			return $('<label class="control-label" data-placement="right" data-title="' + attribute.description + '">' + label + '</label>').tooltip();
		}
		else if (!isFirstElement && wizard) 
		{
			return $('<label class="control-label"></label>');
		} 
		else
		{
			return null;
		}
	}
	
	/**
	 * Create plus button
	 */
	function createComplexFilterPlusButton($container, attribute, complexFilterOperator, wizard)
	{
		return $('<div class="controls"></div>').append($('<button class="btn" type="button"><i class="icon-plus"></i></button>').click(function(){
			addComplexFilterElementToContainer($container, attribute, complexFilterOperator, undefined, wizard, false);
		}));
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createRemoveButton($toRemoveElement){
		return $('<div class="controls"></div>')
					.append($('<button class="btn" type="button"><i class="icon-minus"></i></button>').click(function(){
						$toRemoveElement.remove();
					}));
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createRemoveFirstComplexElementButton($container){
		return $('<div class="controls"></div>')
		.append($('<button class="btn" type="button"><i class="icon-minus"></i></button>').click(function(){
			
		//	$firstElementContainer = $('.control-group.complex-element-container', $container)[0];
		//	$secondElementContainer = $('.control-group.complex-element-container', $container)[1];
//			console.log($container);
			var $firstElement = $('[data-filter=complex-element]', $container)[0];
			var $secondElement = $('[data-filter=complex-element]', $container)[1];
			
			var $simpleFilterFirstElement = $('[data-filter=complex-simplefilter]', $firstElement);
			var $simpleFilterSecondElement = $('[data-filter=complex-simplefilter]', $secondElement);
			var $simpleFilterSecondElementContainer = $('.complex-element-container', $container)[1];
			
			$simpleFilterFirstElement.empty();
			
			alert($simpleFilterSecondElement.html());
			
			$simpleFilterFirstElement.append($simpleFilterSecondElement);
			$simpleFilterSecondElementContainer.remove();
			//console.log("$('[data-filter=complex-element]', $container): ", $('[data-filter=complex-element]', $container));
		}));
	}
	
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createSimpleFilterControls(attribute, filter, wizard) {
		var $container = $('<div class="simpleFilterContainer"></div>');
		var $label = createFilterLabel(attribute, true, wizard);
		$container.append($label);
		$container.append(createSimpleFilterControlsElements(attribute, filter));
		$container.data('attribute', attribute);
		return $container;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createSimpleFilterControlsElements(attribute, simpleFilter) {
		var $controls = $('<div class="controls">').width('314px');
		var name = 'input-' + attribute.name + '-' + new Date().getTime();
		var values = simpleFilter ? simpleFilter.getValues() : null;
		var fromValue = simpleFilter ? simpleFilter.fromValue : null;
		var toValue = simpleFilter ? simpleFilter.toValue : null;
		switch(attribute.fieldType) {
			case 'BOOL':
				var attrs = {'name': name};
				var attrsTrue = values && values[0] === 'true' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
				var attrsFalse = values && values[0] === 'false' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
				var inputTrue = createInput(attribute, attrsTrue, true);
				var inputFalse = createInput(attribute, attrsFalse, false);
				$controls.append(inputTrue.addClass('inline')).append(inputFalse.addClass('inline'));
				break;
			case 'CATEGORICAL':
				var restApi = new molgenis.RestClient();
				var entityMeta = restApi.get(attribute.refEntity.href);
				var entitiesUri = entityMeta.href.replace(new RegExp('/meta[^/]*$'), ""); // TODO do not manipulate uri
				var entities = restApi.get(entitiesUri, {
					q : {
						sort : {
							orders : [ {
								direction : 'ASC',
								property : 'id'
							} ]
						}
					}
				});
				$.each(entities.items, function() {
					var attrs = {'name': name, 'id': name};
					if(values && $.inArray(this[entityMeta.labelAttribute], values) > -1)
						attrs.checked = 'checked';
					$controls.append(createInput(attribute, attrs, this[entityMeta.labelAttribute], this[entityMeta.labelAttribute]));
				});
				break;
			case 'DATE':
			case 'DATE_TIME':
				var nameFrom = name + '-from', nameTo = name + '-to';
				var valFrom = fromValue ? fromValue : undefined;
				var valTo = toValue ? toValue : undefined;
				var inputFrom = createInput(attribute, {'name': nameFrom, 'placeholder': 'Start date', 'style' : 'width: 244px'}, valFrom);
				var inputTo = createInput(attribute, {'name': nameTo, 'placeholder': 'End date', 'style' : 'width: 244px'}, valTo);
				$controls.append($('<div class="control-group">').append(inputFrom)).append($('<div class="control-group">').append(inputTo));
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
					$controls.append(slider);
				} else {
					var nameFrom = name + '-from', nameTo = name + '-to';
					var labelFrom = $('<label class="horizontal-inline" for="' + nameFrom + '">From</label>');
					var labelTo = $('<label class="horizontal-inline inbetween" for="' + nameTo + '">To</label>');
					var inputFrom = createInput(attribute, {'name': nameFrom, 'id': nameFrom, 'style' : 'width: 111px'}, values ? fromValue : undefined).addClass('input-small');
					var inputTo = createInput(attribute, {'name': nameTo, 'id': nameTo, 'style' : 'width: 111px'}, values ? toValue : undefined).addClass('input-small');
					$controls.addClass('form-inline').append(labelFrom).append(inputFrom).append(labelTo).append(inputTo);
				}
				break;
			case 'EMAIL':
			case 'HTML':
			case 'HYPERLINK':
			case 'STRING':
			case 'TEXT':
			case 'ENUM':
				$controls.append(createInput(attribute, {'name': name, 'id': name, 'style' : 'width: 300px'}, values ? values[0] : undefined));
				break;
			case 'MREF':
			case 'XREF':
				var operator = simpleFilter ? simpleFilter.operator : 'OR';
				$controls.addClass("xrefmrefsearch");
				$controls.xrefmrefsearch({
					attribute : attribute,
					values : values,
					operator : operator,
					autofocus : 'autofocus',
					isfilter : true,
					width : '244px'
				});
				break;
			case 'COMPOUND' :
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + attribute.fieldType;
			default:
				throw 'Unknown data type: ' + attribute.fieldType;			
		}
		return $controls;
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
		
		this.formatOperator = function(operator)
		{
			return this.operators[operator];
		}
		
		return this;
	}
	
	self.SimpleFilter = function(attribute, fromValue, toValue, value)
	{
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
			
			$(":input",$domElement).not('[type=radio]:not(:checked)')
					.not('[type=checkbox]:not(:checked)')
					.not('[data-filter=complex-operator]')
					.not('.exclude').each(function(){
				var value = $(this).val();
				var name =  $(this).attr("name");
				
				if(value) {
					// Add operator
					if ($(this).attr('data-filter') === 'xrefmref-operator') {
						operator = value;
					} 
					
					// Add values
					else 
					{
		                if(attribute.fieldType === 'MREF' || attribute.fieldType === 'XREF'){
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
	
	self.ComplexFilterElement = function(attribute)
	{
		this.simpleFilter = null;
		this.type = 'complex-element';
		this.attribute = attribute;
		
		this.update = function ($domElement)
		{
			this.operator = this.formatOperator($(':input[data-filter=complex-operator]', $domElement).val());
			this.simpleFilter = (new self.SimpleFilter(attribute)).update($('[data-filter=complex-simplefilter]', $domElement));
			
//			console.log("this.operator: ", this.operator);
//			console.log("this.simpleFilter: ", this.simpleFilter);
			
			return this;
		};
	};
	self.ComplexFilterElement.prototype = new Filter();
	
	self.ComplexFilter = function(attribute)
	{
		var complexFilterElements = [];
		this.type = 'complex';
		this.attribute = attribute;
		
		this.update = function ($domElement) {			
			$('[data-filter=complex-element]', $domElement).each(function(){
				var complexFilterElement = (new self.ComplexFilterElement(attribute)).update($(this));
				if(!complexFilterElement.simpleFilter.isEmpty())
				{
					complexFilterElements.push(complexFilterElement);
				}
			});
			return this;
		};

        this.addComplexFilterElement = function (complexFilterElement) {
            if(!complexFilterElement.isEmpty())
            {
            	complexFilterElements.push(complexFilterElement);
            }
			return this;
		};
		
		this.isEmpty = function () {
			for(var i = 0; i < complexFilterElements.length; i++)
			{
				if(!complexFilterElements[i].simpleFilter.isEmpty()){
					return false;
				}
			}
			return true;
		};
		
		this.getComplexFilterElements = function () {
			return complexFilterElements;
		};
		
		this.createQueryRule = function() {
			var nestedRules = [];
			var operator = this.operator;
			var rule;
			
			$.each(complexFilterElements, function(index, complexFilterElement) {
				if(index > 0){
					nestedRules.push({
						operator : complexFilterElement.operator
					});
				}
				nestedRules.push(complexFilterElement.simpleFilter.createQueryRule());
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